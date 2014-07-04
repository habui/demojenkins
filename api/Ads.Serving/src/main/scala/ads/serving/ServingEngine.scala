package ads.serving

import ads.common.database.IDataDriver
import ads.common.model._
import ads.common.services.serving.{CompanionAdsResponse, AdsRequest, Const, IServingEngine}
import ads.common.Syntaxs._
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import java.util.concurrent.{ConcurrentSkipListSet, ThreadLocalRandom, ConcurrentHashMap}
import java.util.{TimeZone, Calendar, Date}
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.concurrent.ops._
import scala.util.control.NonFatal
import ads.web.WebUtils
import scala.collection.mutable
import ads.common.services.{IFastReportService}
import com.maxmind.geoip.Location
import ads.common.geoip.GeoipService
import scala.util.Random
import ads.common.JsonExt
import com.google.gson.internal.LinkedTreeMap
import ads.common.sessions.{RetargetingStore, SiteZoneFreqStore, Session}
import scala.actors.threadpool.AtomicInteger
import scala.Some


class ZoneRecord {
    val links = new ArrayBuffer[NewBookRecord]()
}

class NetworkRecord(val banner: Banner, val model: BannerModel with INetwork)

class NetworkZone(var total: Long, val links: ArrayBuffer[NetworkRecord])

class ServingEngine(env: {
    def connectReportService()
    val campaignDataDriver: IDataDriver[Campaign]
    val bannerDataDriver: IDataDriver[Banner]
    var fastReportService: IFastReportService
    val zoneDataDriver: IDataDriver[Zone]
    val newBookDataDriver: IDataDriver[NewBookRecord]
    val websiteDataDriver: IDataDriver[Website]
})
    extends IServingEngine {

    val active = new AtomicBoolean()
    var map = new ConcurrentHashMap[Int, ZoneRecord]()
    var default = new AdsResponse(null)
    var videoResponseDefault = new VideoAdsResponse(null)
    var banners = Map[Int, Banner]()
    var zones = Map[Int, Zone]()
    var sites = Map[Int, Website]()
    var companionBookingData = new ConcurrentHashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Float]]]()
    var companionNetworkData = new ConcurrentHashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Long]]]()
    var companionBookingBannerIds = new ConcurrentSkipListSet[Int]()
    var companionNetworkBannerIds = new ConcurrentSkipListSet[Int]()
    var companionNetworkCampaign = new ConcurrentHashMap[Int, List[Int]]()
    var counter = new ConcurrentHashMap[Int,AtomicLong]()
    var disabled = new ConcurrentSkipListSet[Int]()
    var zeroReport = new ConcurrentSkipListSet[Int]()

    var networkZones = Map[Int, NetworkZone]()

    val banking = new ConcurrentHashMap[Int, AtomicInteger]()
    val maxSteal = 20

    var prData : Map[Int, Iterable[ArticleModel]] = null
    var prCompanionValues : Array[String] = Array[String]()

    var statsInRecentBlock = new ConcurrentHashMap[Int, (AtomicInteger, Int)]()

    var zoneStats = new ConcurrentHashMap[Int, AtomicLong]()
    var timeUpdateZoneStats: Long = 0

    spawn {
        while (true) {
            Thread.sleep(15 * 1000)
            try {
                updateData()
                updateBooking()
                updateNetwork()
//                updateZoneStats
                updatePrBanner
            }
            catch {
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }
        }
    }

    def serve(request: AdsRequest): AdsResponse = {
        val location = GeoipService.getGeoipClient.getLocation(request.ip)
        val rs = serveBooking(request, (x: String) => location)
        if (rs != null && rs.banner != null) return rs

        // try network
        val network = serveNetwork(request, (x: String) => location)
        if (network != null) return network

        // default ads
        return default
    }

    def serveMulti(request: AdsRequest): AdsResponse = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return default
        }
        val bookings = map.get(zoneId)
        val networks = getNetworkRecords(request)._1

        (bookings, networks) match {
            case (null, null) => return default
            case (null, _) => {
                if (request.items >= networks.size)
                    return new AdsResponse(networks.map(b => b.banner).toArray)
            }
            case (_, null) => {
                if (request.items >= bookings.links.size)
                    return new AdsResponse(bookings.links.map(b => banners(b.itemId)).filter(b => b != null).toArray)
            }
            case (_, _) => {
                if (request.items >= bookings.links.size + networks.size)
                    return new AdsResponse((bookings.links.map(b => banners(b.itemId)).filter(b => b != null).toList ++ networks.map(b => b.banner).toList).toArray)
            }
            case _ => {}
        }

        val data = new ArrayBuffer[Banner]()

        if (bookings != null) {
            val minBookings = Math.abs(request.items - {
                if (networks == null) 0
                else networks.size
            })
            val maxBookings = Math.min(bookings.links.size, request.items)
            val bannerIds = new ArrayBuffer[Int]

            for (i <- 1 to maxBookings) {
                val links = bookings.links.filter(id => (!bannerIds.contains(id)))
                val totalShare = links.map(record => record.share).sum.toInt
                if(totalShare != 0) {
                    var seed = ThreadLocalRandom.current().nextInt(Const.totalShares * {
                        if (bannerIds.size < minBookings) totalShare
                        else 100
                    }) / 100
                    var flag = true
                    for (link <- links if flag) {
                        if (seed <= link.share * Const.totalShares / 100) {
                            bannerIds += link.itemId
                            if (banners.contains(link.itemId)) data += banners(link.itemId)
                            flag = false
                        }
                        seed -= (link.share * Const.totalShares / 100).toInt
                    }
                }
            }
        }

        // try network
        val network = serveNetwork(request, (x:String)=>null, false ,request.items - data.length)
        if (network != null) data ++= network.banner

        // default ads
        if (data.length > 0) new AdsResponse(data.filter(b => b != null).toArray)
        else default
    }

    def serveVideo(request: AdsRequest): VideoAdsResponse = {
        val data = {
            try {
                getVideoData(request)
            } catch {
                case ex:Throwable => ex.printStackTrace()
                    null
            }
        }
        if(data == null) return videoResponseDefault
        val videoData = data._1
        val freqData = data._2

        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return null
        }
        if (!networkZones.contains(zoneId)) return null

        val zone = zones(zoneId)
        val zoneModel = zone.kind match {
            case ZoneKind.Video => {
                val instance = WebUtils.fromJson(classOf[VideoZoneModel], zone.extra)
                if (instance != null) {
                    instance.id = zone.id
                }
                instance
            }
            case _ => null.asInstanceOf[VideoZoneModel]
        }


        val preAds = videoData(0).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val midAds = videoData(1).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val postAds = videoData(2).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val nonLinearAds = videoData(3).map(c=>c._2.asInstanceOf[NetworkOverlayBannerModel]).toList

        val result = new mutable.HashMap[String, Array[Array[BannerModel]]]()


        result.put(VideoZonePosition.PRE, (generateVideoAds(zoneId, preAds, zoneModel.preMaxPodDuration)))
        result.put(VideoZonePosition.MID, (generateVideoAds(zoneId, midAds, zoneModel.midMaxPodDuration, zoneModel.midMaxAdpod)))
        result.put(VideoZonePosition.POST, (generateVideoAds(zoneId, postAds, zoneModel.postMaxPodDuration)))
        result.put("nonLinear", (new ArrayBuffer[Array[BannerModel]]() += generateNonLinearAds(nonLinearAds, zoneModel)).toArray)

        return new VideoAdsResponse(result.toMap)
    }

    def servePauseAdVideo(request: AdsRequest, json: String): VideoAdsResponse = {
        if(json != null && json.length > 0) {
            val data = JsonExt.gson.fromJson(json.trim, classOf[java.util.HashMap[String,Any]])
            val ads = data.get("ads").asInstanceOf[Boolean]
            if(!ads) return new VideoAdsResponse(null)

            val pause = data.get("pause").asInstanceOf[Boolean]
            if(!pause) return new VideoAdsResponse(null)
        }

        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return videoResponseDefault
        }
        if (!networkZones.contains(zoneId)) return videoResponseDefault

        val zone = zones(zoneId)
        val zoneModel = zone.kind match {
            case ZoneKind.Video => {
                val instance = WebUtils.fromJson(classOf[VideoZoneModel], zone.extra)
                if (instance != null) {
                    instance.id = zone.id
                }
                instance
            }
            case _ => null.asInstanceOf[VideoZoneModel]
        }

        if (zoneModel == null) return videoResponseDefault

        val networkRecords = getNetworkRecords(request)._1
        if (networkRecords == null || networkRecords.length == 0) return videoResponseDefault

        val pauseAds = new ArrayBuffer[NetworkPauseAdBannerModel]()

        for (networkRecord <- networkRecords) {
            if (networkRecord.banner.kind == BannerKind.NetworkPauseAd) {
                pauseAds += networkRecord.model.asInstanceOf[NetworkPauseAdBannerModel]
            }
        }

        val result = new mutable.HashMap[String, Array[Array[BannerModel]]]()
        if(pauseAds.length > 0) {
            result.put("nonLinear", (new ArrayBuffer[Array[BannerModel]]() += Array(pauseAds(0))).toArray)
        } else result.put("nonLinear", (new ArrayBuffer[Array[BannerModel]]()).toArray)


        return new VideoAdsResponse(result.toMap)
    }

    def serveCompanion(request: AdsRequest): CompanionAdsResponse = {
        val zoneIds = request.zoneIds
        val rs = HashMap[Int, Array[Banner]]()

        //TODO: priority for pr banner
        if(request.companionTargeting != null && request.companionTargeting.length > 0) {
            for(z <- zoneIds) {
                val banners = serveNetwork(
                    new AdsRequest(request.site, List(z), request.url, request.ip, request.cookie, request.viewed, request.items, request.companionTargeting, request.listValues),
                    (x: String) => GeoipService.getGeoipClient.getLocation(x),
                    true, 1, true
                ).banner

                if(banners != null && banners.length > 0) rs.put(z, banners)
            }
        }

        var loopedZones = new ArrayBuffer[Int]
        val zoneUsage = new HashMap[Int, Float]()
        for (z <- zoneIds) {
            if (!rs.keys.toList.contains(z) && !loopedZones.contains(z) && companionBookingData.containsKey(z)) {
                val data = companionBookingData.get(z)
                var seed = ThreadLocalRandom.current().nextInt(Const.totalShares)
                var hasCompanion = false
                for (link <- data if !hasCompanion) {
                    val currentShare = link.keys.map(b => zoneUsage.getOrAdd(b._1, id => 0)).max * seed / 100
                    val share = link.toList(0)._2
                    if (seed <= share * Const.totalShares / 100 + currentShare) {
                        val siteId = zones(z).siteId
                        val zoneLinks = link.keys.toList.map(b => b._1).filter(z=>zones(z).siteId == siteId)
                        if(!(zoneLinks.exists(b=>rs.contains(b)) || zoneLinks.exists(b=>(!zoneIds.contains(b))))) {
                            //                            if (zoneLinks.filter(b => rs.contains(b)).length == 0 && zoneLinks.filter(p => (!zones.contains(p))).size == 0) {
                            link.foreach(l => {
                                val item = Array(banners(l._1._2))
                                if (item != null) rs.put(l._1._1, item)
                            })
                            hasCompanion = true
                        }
                    }
                    loopedZones ++= link.keys.toList.map(p => p._1)
                    link.foreach(l => {
                        val usage = zoneUsage.getOrAdd(l._1._1, id => 0)
                        zoneUsage.put(l._1._1, usage + l._2)
                    })
                    seed -= (link.filter(p => p._1._1 == z).toList(0)._2 * Const.totalShares / 100).toInt
                }
            }
        }

        for (zoneId <- zoneIds) {
            if (!rs.keys.toList.contains(zoneId)) {
                val record = serveBooking(new AdsRequest(request.site, List(zoneId), request.url, request.ip, request.cookie, request.viewed, request.items,request.companionTargeting, request.listValues), (x: String) => GeoipService.getGeoipClient.getLocation(x), true)
                if (record != null && record.banner != null) rs.put(zoneId, record.banner)
            }
        }

        //TODO: filter frequency capping site of zones
        val freqSite = try {
            SiteZoneFreqStore.store.get(request.cookie)
        } catch {
            case x:Throwable => x.printStackTrace
                null
        }
        for((k,v) <- rs) {
            rs += k -> v.filter(b=>freqSiteZoneFilter(request.cookie, k, b.checkFreqSiteOrZone, (x: String) => freqSite))
        }

        val zoneFilter = request.zoneIds.filter(z => (!rs.keySet.toList.contains(z))).toList
        if(zoneFilter.length > 0) serveCompanionNetwork(new AdsRequest(request.site, zoneFilter,
            request.url, request.ip, request.cookie, request.viewed, request.items, request.companionTargeting, request.listValues))
            .data.foreach(p => rs += p)

        new CompanionAdsResponse(rs)
    }

    def getPrData(request: AdsRequest, model: PrBannerModel): Array[ArticleModel] = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return Array[ArticleModel]()
        }

        val raw = new ArrayBuffer[ArticleModel]
        for((k, v) <- prData) {
            raw ++= v.toList
        }


        //        if (!networkZones.contains(zoneId)) return Array[ArticleModel]()
        //        val location = GeoipService.getGeoipClient.getLocation(request.ip)
        //
        //        val data = raw.filter(a => checkNetworkRecord(request, a, location)).sortBy(c=>{
        //            if (c.rateUnit == RateUnit.CPC) c.rate*10000
        //            else c.rate
        //        }).toList.map(c=>c.asInstanceOf[ArticleModel])

        val numOfElements = model.templateType match {
            case 1 => 1
            case 2 => 2
            case 3 => 3
        }
        //TODO: need using rate
        Random.shuffle(raw).slice(0,numOfElements).toArray
    }


    def serveVideoZingTv(request: AdsRequest, json: String) : ZingTVVideoAdsResponse = {
        val zoneId = request.zoneIds(0)
        val jsonData = JsonExt.gson.fromJson(json.trim, classOf[java.util.HashMap[String,Any]])

        val ads = jsonData.get("ads").asInstanceOf[Boolean]
        if(!ads) return new ZingTVVideoAdsResponse(null)

        val pause = jsonData.get("pause").asInstanceOf[Boolean]
        val conflict = jsonData.get("conflict").asInstanceOf[Boolean]
        if(conflict) return new ZingTVVideoAdsResponse(null)
        val brand = jsonData.get("brand").asInstanceOf[Boolean]

        val preroll = JsonExt.gson.fromJson(jsonData.get("preroll").toString.trim, classOf[java.util.ArrayList[LinkedTreeMap[String,Any]]])
        val midroll = JsonExt.gson.fromJson(jsonData.get("midroll").toString.trim, classOf[java.util.ArrayList[LinkedTreeMap[String,Any]]])
        val postroll = JsonExt.gson.fromJson(jsonData.get("postroll").toString.trim, classOf[java.util.ArrayList[LinkedTreeMap[String,Any]]])

        val data =  {
            try {
                getVideoData(request)
            } catch {
                case ex:Throwable => ex.printStackTrace()
                    null
            }
        }

        if(data == null) return new ZingTVVideoAdsResponse(null)

        val videoData = data._1
        if(videoData == null) return new ZingTVVideoAdsResponse(null)

        val freqData = data._2

        val preAds = videoData(0).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val midAds = videoData(1).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val postAds = videoData(2).map(c=>c._2.asInstanceOf[NetworkTVCBannerModel])
        val nonLinearAds = videoData(3).map(c=>c._2.asInstanceOf[NetworkOverlayBannerModel]).toList

        val prerollData = generateZingTVVideoAds(zoneId, preroll, preAds, nonLinearAds, freqData)
        val midrollData = generateZingTVVideoAds(zoneId, midroll, midAds, nonLinearAds, freqData)
        val postrollData = generateZingTVVideoAds(zoneId, postroll, postAds, nonLinearAds, freqData)

        val tvcItems = (prerollData("data") ++ midrollData("data") ++ postrollData("data")).map(c=>c._1).toArray.
            foldLeft[Array[BannerModel]](Array[BannerModel]()){
            (a,b) => if(!a.exists(c=>c.id == b.id)) a:+b else a
        }

        val companion = getCompanionVideo(request, tvcItems)

        val result = new mutable.HashMap[String, Array[(BannerModel, Int, Int, Map[Int, BannerModel])]]()
        result.put(VideoZonePosition.PRE, prerollData("data").map(c=>(c._1,c._2,c._3, companion.getOrElse[Map[Int, BannerModel]](c._1.id, null))).toArray)
        result.put(VideoZonePosition.MID, midrollData("data").map(c=>(c._1,c._2,c._3, companion.getOrElse[Map[Int, BannerModel]](c._1.id, null))).toArray)
        result.put(VideoZonePosition.POST, postrollData("data").map(c=>(c._1,c._2,c._3, companion.getOrElse[Map[Int, BannerModel]](c._1.id, null))).toArray)
        result.put("nonLinear", (prerollData("overlay") ++ midrollData("overlay") ++ postrollData("overlay")).map(c=>(c._1,c._2,c._3, null)).toArray)

        new ZingTVVideoAdsResponse(result.toMap)
    }


    def updateData() {
        banners = env.bannerDataDriver.loadAll().map(b => (b.id, b)).filter(b => (!b._2.disable)).toMap
        zones = env.zoneDataDriver.loadAll().map(z => (z.id, z)).filter(b => (!b._2.disable)).toMap
        sites = env.websiteDataDriver.loadAll().map(w => (w.id, w)).filter(b => (!b._2.disable)).toMap
    }

    def updateZoneStats() {
        val stats = new ConcurrentHashMap[Int, AtomicLong]
        val now = System.currentTimeMillis()
        val timeStartReport = now / 15*60*1000 * 15*60*1000 - 15*60*1000
        if(timeStartReport >= timeUpdateZoneStats + 15*60*1000) {
            timeUpdateZoneStats = timeStartReport
            println("Update zone stats : " + now)
            zones.foreach(z=>{
                val reportData = {
                    try {
                        env.fastReportService.getImpressionClick(z._2.id, timeStartReport, now, "zone")
                    } catch {
                        case _: Throwable => {
                            env.connectReportService
                            (0.toLong, 0.toLong)
                        }
                    }
                }
                stats.put(z._2.id, new AtomicLong(reportData._1/Config.numOfServers.get))
            })
            zoneStats = stats
        }
    }

    def run() {
    }

    def updatePrBanner {
        //TODO : update pr data
        val  prArticles = banners.map(c=>c._2).filter(c=>c.kind == BannerKind.Article).map(c=>{
            val instance = WebUtils.fromJson(classOf[ArticleModel], c.extra)
            instance.id = c.id
            instance
        }).filter(c=>c.status == ArticleStatus.PUBLISHED)
        prCompanionValues = prArticles.map(c=>c.companionTargeting).filter(c => c != null).toArray
        prData = prArticles.groupBy(c=>c.category).toMap
    }



    def serveBooking(request: AdsRequest, f: String => Location, withoutCompanionBanner: Boolean = false): AdsResponse = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return default
        }

        //TODO : priority for oversea
        val location = f(request.ip)
        if(location != null && location.countryCode != "VN" && banking.getOrAdd(zoneId, zoneId => new AtomicInteger(0)).get() < maxSteal) {
            val networkBanner = serveNetwork(request, (x: String) => location ,withoutCompanionBanner)
            if(networkBanner != null && networkBanner.banner != null){
                val temp = banking.get(zoneId).incrementAndGet()
                return networkBanner
            }
        }

        val record = map.get(zoneId)
        if (record != null) {

            val links = {
                if(withoutCompanionBanner) record.links.filter(link=>(!companionBookingBannerIds.contains(link.itemId)))
                else record.links
            }
            // try booking
            // straight forward implementation
            // @TODO: impression-steal (improve network reach)

            var seed = ThreadLocalRandom.current().nextInt(Const.totalShares)
            for (link <- links) {
                if (seed <= link.share * Const.totalShares / 100) {
                    return new AdsResponse(Array(banners(link.itemId)))
                }
                seed -= (link.share * Const.totalShares / 100).toInt
            }
        }
        return default
    }



    def filterNetwork(banner: BannerModel with INetwork, request: AdsRequest) : Boolean = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else false
        }
        banner.targetZones match {
            case null => banner.targetContent.contains(request.site)
            case _ => banner.targetZones.contains(zoneId)
        }
        false
    }

    def freqFilter(request: AdsRequest, networkRecord: BannerModel with INetwork, getFreq: String => Session) : Boolean = {
        try {
            if(request.cookie == null) return true

            val freqStore = getFreq(request.cookie)
            if(freqStore == null) return true
            val data = freqStore.logs
            if(data == null) return true
            val times = (data zip data.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList.filter(c=>c._1 == networkRecord.id).map(c=>c._2)
            val now = System.currentTimeMillis()/1000
            if(times.filter(t=>networkRecord.freqCappingTime == 0 || (t <= now && t >= now - networkRecord.freqCappingTime)).length >= networkRecord.freqCapping) return false
            true
        } catch {
            case ex : Throwable => ex.printStackTrace()
            true
        }
    }

    def sessionToMap(session: Session) : Map[Int, Array[Int]] = {
        if(session == null) return Map[Int, Array[Int]]()
        val data = session.logs
        if(data == null) return Map[Int, Array[Int]]()
        val map = mutable.Map[Int, Array[Int]]()
        val times = (data zip data.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList
        times.foreach(c => {
            if(!map.contains(c._1)) {
                map.put(c._1, times.filter(d => d._1 == c._1).map(c=>c._2).toArray)
            }
        })
        map.toMap
    }

    def freqSiteZoneFilter(cookie: String, zoneId: Int, checkFreqSiteOrZone: Boolean, getFreq: String => Session) : Boolean = {
        try {
            if(!checkFreqSiteOrZone) return true

            val zone = zones(zoneId)
            if(zone == null) return true
            val site = sites(zone.siteId)
            if(site == null) return true
            if(zone.frequencyCapping == 0 && site.frequencyCapping == 0) return true

            if(cookie == null) return true
            val freqStore = getFreq(cookie)
            if(freqStore == null) return true
            val data = freqStore.logs
            if(data == null) return true

            val times = (data zip data.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList
            val now = System.currentTimeMillis() / 1000

            if(zone.frequencyCapping != 0) {
                if(times.filter(c=>c._1 == zone.id).filter(t => (t._2 <= now && t._2 >= now - zone.frequencyCappingTime)).size >= zone.frequencyCapping) return false
            } else {
                val siteCaps = times.filter(c=>{
                    val zone = zones(c._1)
                    if(zone == null) false
                    else {
                        zone.siteId == site.id
                    }
                }).filter(t => (t._2 <= now && t._2 >= now - site.frequencyCappingTime))
                if(siteCaps.length >= site.frequencyCapping) return false
            }

            true
        } catch {
            case ex:Throwable => ex.printStackTrace()
            true
        }
    }

    def checkNetworkRecord(request: AdsRequest, networkRecord: BannerModel with INetwork, location: Location, freq: String => Session, freqSite: String => Session) : Boolean = {
        if(disabled.contains(networkRecord.id)) return false
        //TODO: frequency capping filter
        if(networkRecord.freqCapping != 0 && !freqFilter(request, networkRecord, freq)) return false

        //TODO: frequency capping of site or zone filter
        if(!freqSiteZoneFilter(request.cookie, request.zoneIds(0), networkRecord.checkFreqSiteOrZone, freqSite)) return false

        //TODO: country targeting
        if(location != null && networkRecord.geographicTargetings != null && networkRecord.geographicTargetings.length > 0) {
            val geoTargetings = networkRecord.geographicTargetings.filter(b=>{
                val country = b.value.toLowerCase.substring(0,2)
                if(b.value.length == 2) {
                    !(country.equals(location.countryCode.toLowerCase) ^ b.in)
                } else {
                    !(b.in ^ (country == location.countryCode.toLowerCase
                        && (location.region == null || b.value.toLowerCase.equals((location.countryCode + location.region).toLowerCase))))
                }
            })
            if(geoTargetings.length == 0) return false
        }

        //TODO: variable targeting filter
        if (request.listValues != null && request.listValues.size > 0) {
            for ((key, value) <- request.listValues) {
                if (networkRecord.variables != null){
                    val varTargettings = networkRecord.variables.filter(p => p.key.toLowerCase.equals(key.toLowerCase)).toList
                    if(varTargettings.length > 0) {
                        if(varTargettings.filter(p => value.contains(p.value.toLowerCase) && p.bound == VariableTargetingBound.NotEqual).length > 0) return false
                        else if(varTargettings.filter(p=>p.bound == VariableTargetingBound.Equal).length > 0 && varTargettings.filter(p => (value.contains(p.value.toLowerCase) && p.bound == VariableTargetingBound.Equal)).length == 0) return false
                    }
                }
            }
        }
        true
    }

    def  getNetworkRecords(request: AdsRequest): (ArrayBuffer[NetworkRecord], mutable.Map[Int, Int]) = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return (ArrayBuffer[NetworkRecord](), null)
        }
        if (!networkZones.contains(zoneId)) return (ArrayBuffer[NetworkRecord](), null)
        val location = GeoipService.getGeoipClient.getLocation(request.ip)

        //TODO: get site variable for retargeting
        try {
            val retargetingData = RetargetingStore.store.get(request.cookie)
            if(retargetingData != null && retargetingData.logs != null && retargetingData.logs.length > 0) {
                val retargeting = (retargetingData.logs zip retargetingData.logs.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList.map(c=>c._1.toString)
                request.listValues put ("retargeting", retargeting)
            }
        } catch {
            case x: Throwable =>
        }

        val freq = {
            try {
                if(request.cookie == null) null
                else Session.store.get(request.cookie)
            } catch {
                case x: Throwable =>
                null
            }

        }
        val freqSite = {
            try {
                if(request.cookie == null) null
                else SiteZoneFreqStore.store.get(request.cookie)
            } catch {
                case x: Throwable =>
                null
            }
        }

        if(!networkZones.contains(zoneId)) return (ArrayBuffer[NetworkRecord](), null)
        var data = {
            if(request.companionTargeting != null) networkZones(zoneId).links.filter(c=>c.model.companionTargetingValues != null && c.model.companionTargetingValues.exists(c=>c.toLowerCase() == request.companionTargeting.toLowerCase()))
            else networkZones(zoneId).links

        }
        if(data.length == 0) data = networkZones(zoneId).links
        val networkRecords = data.filter(n => checkNetworkRecord(request, n.model, location, (x: String) => freq, (y: String) => freqSite))

        val mapFreq = sessionToMap(freq)
        val remainCap = mutable.Map[Int, Int]()
        val now = System.currentTimeMillis() / 1000
        networkRecords.foreach(c=>{
            if(c.model.freqCapping > 0 && c.model.freqCappingTime > 0) {
                val remain = {
                    if(mapFreq.contains(c.banner.id)) {
                        c.model.freqCapping - mapFreq(c.banner.id).filter(x => x > now - c.model.freqCappingTime).length
                    } else c.model.freqCapping
                }
                remainCap(c.banner.id) = remain
            }
        })
//        (data.filter(n => checkNetworkRecord(request, n.model, location, (x: String) => freq, (y: String) => freqSite)), freq)
        (networkRecords, remainCap)
    }

//    def chooseNetworkRecord(zoneId : Int, networkRecords: Array[NetworkRecord], count: Int) : Array[NetworkRecord] = {
//        val rs = new ArrayBuffer[NetworkRecord]
//        val data = networkRecords.filter(c => statsInRecentBlock.containsKey(c.banner.id) && {
//            val item = statsInRecentBlock.get(c.banner.id)
//            item._1.get() > 0
//        })
//        val mapData = data.map(c=>{
//            val item = statsInRecentBlock.get(c.banner.id)
//            (c.banner.id, (item._1.get(), item._2))
//        }).toMap
//
//        var remain = data.sortWith((b,c)=>mapData(b.banner.id)._2 > mapData(c.banner.id)._2)
//
//        while(rs.length < count && remain.length > 0) {
//            var zoneStat = zoneStats.getOrAdd(zoneId, key => new AtomicLong(0)).getAndDecrement
//            if(zoneStat <= 0) zoneStat = 1
//
//            var seed = ThreadLocalRandom.current().nextLong(zoneStat)
//            var done = false
//            for (networkRecord <- remain if !done) {
//                val stat = mapData(networkRecord.banner.id)
//                if (seed <= stat._1.toLong) {
//                    statsInRecentBlock.get(networkRecord.banner.id)._1.decrementAndGet()
//                    val atomic = counter.get(networkRecord.banner.id)
//                    if(atomic != null) {
//                        if(atomic.decrementAndGet() == 0) disabled.add(networkRecord.banner.id)
//                    }
//                    rs += networkRecord
//                    done = true
//                }
//                seed -= (stat._1.toLong)
//            }
//            remain = remain.filter(n=>rs.exists(c => c.banner.id == n.banner.id))
//        }
//        rs.toArray
//    }


    def chooseNetworkRecord(zoneId : Int, networkRecords: Array[NetworkRecord], count: Int) : Array[NetworkRecord] = {
        if(networkRecords.length == 0) return Array[NetworkRecord]()
        var data = new ArrayBuffer[NetworkRecord]()
        var boundValue = statsInRecentBlock.getOrAdd(networkRecords(0).model.id, key => (new AtomicInteger(0), 0))._2

        for(networkRecord <- networkRecords) {
            val eCPM = statsInRecentBlock.getOrAdd(networkRecord.model.id, key => (new AtomicInteger(0), 0))._2
            if(boundValue == eCPM) data += networkRecord
            else if(count > data.length) {
                data += networkRecord
                boundValue = eCPM
            }
        }

        if (count < data.length) {
            val shuffle = Random.shuffle(data.filter(p=>{
                val eCPM = statsInRecentBlock.getOrAdd(p.model.id, key => (new AtomicInteger(0), 0))._2
                eCPM == boundValue
            })).slice(0, data.length - count).map(b=>b.model.id)
            data = data.filter(b=>(!shuffle.contains(b.model.id)))
        }

        data.foreach(n=>{
            val atomic = counter.get(n.model.id)
            if(atomic != null) {
                if(atomic.decrementAndGet() <= 0) disabled.add(n.model.id)
            }
        })
        data.toArray
    }

    def getItemByRate(banners : List[BannerModel with INetwork]): BannerModel = {
        if(banners == null || banners.length == 0) return null
        val data = banners.map(c=>new NetworkRecord(null, c)).toArray
        chooseNetworkRecord(0, data, 1)(0).model

//        if (banners.size > 0) {
//            val totalRate = banners.map(b => {
//                if(b.rateUnit == RateUnit.CPC) b.rate * 10000
//                else b.rate
//            }).sum
//            if(totalRate == 0) return banners(ThreadLocalRandom.current().nextInt(banners.length))
//            val seed = ThreadLocalRandom.current().nextLong(totalRate)
//
//            var t = seed
//            var index = -1
//            while (t >= 0 && index + 1 < banners.size) {
//                index += 1
//                t -= {
//                    if(banners(index).rateUnit == RateUnit.CPC) banners(index).rate * 10000
//                    else banners(index).rate
//                }
//            }
//            if(index >= 0)
//                return banners(index)
//        }
//        return null
    }

    def serveNetwork(request: AdsRequest,f: String => Location , withoutCompanion: Boolean = false, requiredNetworks: Int = 1, highPriority: Boolean = false): AdsResponse = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return default
        }

        //TODO : repayment
        val location = f(request.ip)
        if(location != null && location.countryCode == "VN" && banking.getOrAdd(zoneId, zoneId => new AtomicInteger(0)).get() > 0) {
            val bookingBanner = serveBooking(request, (x: String) => location,  withoutCompanion)
            if(bookingBanner != null && bookingBanner.banner != null) {
                banking.get(zoneId).decrementAndGet()
                return bookingBanner
            }
        }

        //TODO : filter with companion condition and high priority condition
        var networkRecords = getNetworkRecords(request)._1
        if (networkRecords == null || networkRecords.length == 0) return default

        if(withoutCompanion) networkRecords = networkRecords.filter(p => (!companionNetworkBannerIds.contains(p.banner.id)))
        if (networkRecords == null || networkRecords.length == 0) return default

//        val highPriorityData = networkRecords.filter(c=>c.model.highPriority)
//        if(highPriorityData != null && highPriorityData.length > 0) networkRecords = highPriorityData

        if(highPriority) {
            networkRecords = networkRecords.filter(p=>{p.model.companionTargetingValues != null &&
                p.model.companionTargetingValues.contains(request.companionTargeting)
            } || p.model.highPriority)
        }
        //TODO : choose networkRecords
        if(networkRecords.length == 0) return default
        val data = chooseNetworkRecord(zoneId, networkRecords.toArray, requiredNetworks)
        return new AdsResponse(data.map(c=>c.banner).toArray)
    }

    def lookup(items: Array[(Long, Banner, BannerModel with INetwork)], seed: Long): Int = {
        var from = 0
        var to = items.size - 1

        while (from < to) {
            val mid = (from + to) / 2

            val value = items(mid)._1

            if (seed > value) {
                from = mid + 1
            }
            else if (seed == value) {
                return mid
            }
            else {
                to = mid - 1
            }
        }
        return from
    }

    def mapToNetwork(banner: Banner): BannerModel with INetwork = {
        val clazz = banner.kind match {
            case BannerKind.NetworkHtml => classOf[NetworkHtmlBannerModel]
            case BannerKind.NetworkMedia => classOf[NetworkMediaBannerModel]
            case BannerKind.NetworkTVC => classOf[NetworkTVCBannerModel]
            case BannerKind.NetworkOverlayBanner => classOf[NetworkOverlayBannerModel]
            case BannerKind.NetworkPauseAd => classOf[NetworkPauseAdBannerModel]
            case BannerKind.NetworkExpandable => classOf[NetworkExpandableBannerModel]
            case BannerKind.NetworkCatfish => classOf[NetworkCatfishBannerModel]
            case BannerKind.NetworkBalloon => classOf[NetworkBalloonBannerModel]
            case BannerKind.NetworkPopup => classOf[NetworkPopupBannerModel]
            case BannerKind.NetworkPrBanner => classOf[NetworkPrBannerModel]
            case _ => null
        }

        if (clazz == null) null
        else {
            val instance = WebUtils.fromJson(clazz, banner.extra).asInstanceOf[BannerModel with INetwork]
            instance.id = banner.id
            instance
        }
    }

    def expandNetworkItem(item: NetworkRecord, siteToZone: Map[Int, List[Zone]]) = {

        val model = item.model
        (model.targetZones, model.targetContent) match {
            case (null, null) => Array()
            case (null, _) => model.targetContent.flatMap(site => siteToZone(site)).filter(z => z.width >= model.width && z.height >= model.height).map(z => z.id).distinct
            case (_, _) => model.targetZones
        }
    }



    def updateNetwork() {
        val now = System.currentTimeMillis()
        val localCalendar = Calendar.getInstance(TimeZone.getDefault())
        val today = {
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(now)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.getTimeInMillis
        }
        val activeCampaignData = env.campaignDataDriver.loadAll().
            filter(p => (!p.disable) && p.status == "Running" && (p.startDate <= now) && (p.endDate == 0 || p.endDate >= now) && {
            val timeScheduled = p.campaignType match {
                case CampaignType.Network => campaignToModel(p).asInstanceOf[NetworkCampaignModel].timeScheduled
                case CampaignType.NetworkTVC => campaignToModel(p).asInstanceOf[NetworkTVCCampaignModel].timeScheduled
                case _ => null
            }
            if (timeScheduled != null && timeScheduled.length == 7) (timeScheduled(localCalendar.get(Calendar.DAY_OF_WEEK) - 1) & (1 << localCalendar.get(Calendar.HOUR_OF_DAY))) > 0
            else true
        }).toList

        val activeCamps = activeCampaignData.map(p => p.id).toSet

        println(s"Active camps: ${activeCamps.size}")

        val allBanners = banners.map(c=>c._2).
            filter(b => (!b.disable) && activeCamps.contains(b.campaignId) && (b.from <= now) && (b.to == 0 || b.to >= now)).
            map(b => new NetworkRecord(b, mapToNetwork(b))).
            //filter(r=>r.model != null && r.model.status == "Running").toList
            filter(r => {
            r.model != null && r.model.startDate <= now && (r.model.endDate == 0 || r.model.endDate >= now) && (r.model.targetContent != null || r.model.targetZones != null)
        }).toList

        println(s"Active banner: ${allBanners.size}")

        val items = filterBannerByReport(allBanners, activeCampaignData)

        val siteToZone = zones.values.toList.groupBy(z => z.siteId)

        val hashMap = new HashMap[Int, ArrayBuffer[NetworkRecord]]()
        for ((id, record) <- items) {
            val zones = expandNetworkItem(record, siteToZone).toList.filter(z=>this.zones.contains(z))
            for (zone <- zones) {
                val buffer = hashMap.getOrAdd(zone, z => new ArrayBuffer[NetworkRecord]())
                if (!buffer.map(b => b.banner.id).contains(record.banner.id)) buffer += record
            }
        }
        for ((id, buffer) <- hashMap) {
            val temp = buffer.sortWith((a, b) => {
                statsInRecentBlock.getOrAdd(a.banner.id, key => (new AtomicInteger(0), 0))._2  >
                    statsInRecentBlock.getOrAdd(b.banner.id, key => (new AtomicInteger(0), 0))._2
            })
            hashMap.update(id, temp)
        }

        networkZones = hashMap.map(t => (t._1, new NetworkZone(0, t._2))).toMap

        for ((id, z) <- networkZones) {
            for (x <- z.links) {
                z.total = z.total + x.model.limit
            }
        }

        println("active network items: " + items.size)

        // update companion network banner
        val activeBannerIds = items.map(b => b._1).toList
        val activeCompanionCampaign = activeCampaignData.filter(p => p.companion).map(c => c.id)
        val companionBanners = allBanners.groupBy(b => b.banner.campaignId).filter(b => activeCompanionCampaign.contains(b._1) && b._2.filter(b => (!activeBannerIds.contains(b.banner.id))).length == 0).map(b => b._2)

        //update list banner companion by campaign
        val companionNetworkCampaignTemp : ConcurrentHashMap[Int, List[Int]] = new ConcurrentHashMap[Int, List[Int]]
        companionBanners.map(c=>(c(0).banner.campaignId, c.map(b=>b.banner.id))).foreach((c) => {
            companionNetworkCampaignTemp.put(c._1, c._2)
        })
        companionNetworkCampaign = companionNetworkCampaignTemp

        val companionData = new ConcurrentHashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Long]]]()
        var companionBannerIds = new ArrayBuffer[Int]

        for (companion <- companionBanners) {
            val zoneIds = companion.map(b => {
                if (b.model.targetZones != null) b.model.targetZones
                else if (b.model.targetContent != null) {
                    val zones = b.model.targetContent.flatMap(site => siteToZone(site)).filter(z => z.width >= b.model.width && z.height >= b.model.height).map(z => z.id).distinct
                    b.model.targetZones = zones
                    zones
                }
                else new Array[Int](0)
            }).flatten.toList.filter(z=>this.zones.contains(z)).distinct
            val groupZones = zoneIds.map(z => this.zones(z)).groupBy(z => z.siteId).filter(p => p._2.length > 1)
            for (group <- groupZones.values) {
                if (group.filter(z => companion.filter(p => p.model.targetZones.contains(z.id)).toList.length > 1).toList.length == 0) {
                    val tempMap = new mutable.HashMap[(Int, Int), Long]()
                    group.foreach(z => {
                        companion.find(c => c.model.targetZones.contains(z.id)) match {
                            case Some(p) => {
                                tempMap.put((z.id, p.banner.id), p.model.rate * {
                                    if(p.model.rateUnit == RateUnit.CPC) 100000
                                    else 1
                                })
                            }
                            case _ => {}
                        }
                    })
                    if (tempMap.keys.size > 1) {
                        companionBannerIds ++= tempMap.keys.toList.map(b => b._2)
                        tempMap.keys.foreach(p => {
                            var buffer = companionData.getOrAdd(p._1, id => new ArrayBuffer[HashMap[(Int, Int), Long]])
                            buffer += tempMap
//                            buffer = buffer.sortWith((b, c) => b.values.min >= c.values.min)
                            companionData.put(p._1, buffer.sortWith((b, c) => b.values.min >= c.values.min))
                        })
                    }
                }
            }
        }

        companionNetworkData = companionData //set data to memory
        synchronized({
            this.companionNetworkBannerIds.clear()
            companionBannerIds.toList.distinct.foreach(b => this.companionNetworkBannerIds.add(b))
        })

        println("Time for updating network : " + (System.currentTimeMillis() - now))
    }

    def getReportData(id: Int, start: Long, end: Long, kind: String) : (Long, Long) = {
        try {
            env.fastReportService.getImpressionClick(id, start, end , kind)
        } catch {
            case _: Throwable => {
                env.connectReportService
                (0.toLong, 0.toLong)
            }
        }
    }

    def calculateCTR(model : BannerModel with INetwork, reportData : (Long, Long), activeCampaignData : List[Campaign]) : Float = {
        if(model.kind == BannerKind.NetworkHtml && model.rateUnit == RateUnit.CPC) return Config.ctrDefault.getValue
        val now = System.currentTimeMillis()
        val startTime = {
            if(model.startDate > 0) model.startDate
            else {
                val camps = activeCampaignData.filter(p=>p.id == model.campaignId)
                if(camps != null && camps.length > 0) camps(0).startDate
                else now
            }
        }
        if(now - startTime > 24*60*60*1000 && reportData._1 != 0) reportData._2.toFloat / reportData._1
        else Config.ctrDefault.getValue
    }

    def filterBannerByReport(allBanners: List[NetworkRecord], activeCampaignData: List[Campaign]) : Map[Int, NetworkRecord] = {
        val now = System.currentTimeMillis()
        val today = {
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(now)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.getTimeInMillis
        }

        //TODO : must rewrite this code

        val impCounters = new ConcurrentHashMap[Int, AtomicLong]()
        val tmpZeroReport = new ConcurrentSkipListSet[Int]()

        val items = allBanners.filter(r => {
            if(r.model.lifetimeLimit == 0 && r.model.timeSpan * r.model.limit == 0) {
                disabled.remove(r.banner.id)
                true
            }
            else if(r.model.timeSpan * r.model.limit == 0) {
                val reportData = getReportData(r.banner.id, 0, now, "item")
                if((r.model.limitUnit.toLowerCase == LimitUnit.IMPRESSION && reportData._1 >= r.model.lifetimeLimit) ||
                    (r.model.limitUnit.toLowerCase == LimitUnit.CLICK && reportData._2 >= r.model.lifetimeLimit)) false
                else {
                    if(reportData._1 == 0 && reportData._2 == 0 && zeroReport.contains(r.banner.id) && counter.containsKey(r.banner.id)) {
                        impCounters.put(r.banner.id, counter.get(r.banner.id))
                    } else {
                        val remainImp = r.model.limitUnit.toLowerCase match {
                            case LimitUnit.CLICK => {
                                val ctr = calculateCTR(r.model, reportData, activeCampaignData)
                                ((r.model.lifetimeLimit - reportData._2) / ctr).toLong
                            }
                            case LimitUnit.IMPRESSION => {
                                r.model.lifetimeLimit - reportData._1
                            }
                        }
                        if(remainImp > 0) disabled.remove(r.banner.id)
                        impCounters.put(r.banner.id, new AtomicLong((remainImp / Config.numOfServers.getValue * 0.7).toLong))
                        if(reportData._1 == 0 && reportData._2 == 0) tmpZeroReport.add(r.banner.id)
                    }
                    true
                }
            } else if(r.model.lifetimeLimit == 0) {
                val timeStartReport = (now - today) / r.model.timeSpan * r.model.timeSpan + today
                val reportData = getReportData(r.banner.id, timeStartReport, now, "item")
                if((r.model.limitUnit.toLowerCase == LimitUnit.IMPRESSION && reportData._1 >= r.model.limit) ||
                    (r.model.limitUnit.toLowerCase == LimitUnit.CLICK && reportData._2 >= r.model.limit)) false
                else {
                    if(reportData._1 == 0 && reportData._2 == 0 && zeroReport.contains(r.banner.id) && counter.containsKey(r.banner.id)) {
                        impCounters.put(r.banner.id, counter.get(r.banner.id))
                    } else {
                        val remainImp = r.model.limitUnit.toLowerCase match {
                            case LimitUnit.CLICK => {
                                val lifetimeReportData = getReportData(r.banner.id, 0, now, "item")
                                val ctr = calculateCTR(r.model, lifetimeReportData, activeCampaignData)
                                ((r.model.limit - reportData._2) / ctr).toLong
                            }
                            case LimitUnit.IMPRESSION => {
                                r.model.limit - reportData._1
                            }
                        }
                        if(remainImp > 0) disabled.remove(r.banner.id)
                        impCounters.put(r.banner.id, new AtomicLong((remainImp / Config.numOfServers.getValue * 0.7).toLong))
                        if(reportData._1 == 0 && reportData._2 == 0) tmpZeroReport.add(r.banner.id)
                    }
                    true
                }
            } else {
                val timeStartReport = (now - today) / r.model.timeSpan * r.model.timeSpan + today
                val lifetimeReportData = getReportData(r.banner.id, 0, now, "item")

                if((r.model.limitUnit.toLowerCase == LimitUnit.IMPRESSION && lifetimeReportData._1 >= r.model.lifetimeLimit) ||
                    (r.model.limitUnit.toLowerCase == LimitUnit.CLICK && lifetimeReportData._2 >= r.model.lifetimeLimit)) false
                else {
                    val reportData = getReportData(r.banner.id, timeStartReport, now, "item")

                    val endTime = {
                        if(r.model.endDate > 0) r.model.endDate
                        else {
                            val camps = activeCampaignData.filter(p=>p.id == r.model.campaignId)
                            if(camps != null && camps.length > 0) camps(0).endDate
                            else now
                        }
                    }

                    val remainLifetime = r.model.lifetimeLimit -  {
                        if(r.model.limitUnit.toLowerCase == LimitUnit.IMPRESSION) lifetimeReportData._1
                        else lifetimeReportData._2
                    }
                    val requireInThisBlock = (remainLifetime.toFloat / (endTime - now) * r.model.timeSpan).toLong
                    if((r.model.limitUnit.toLowerCase == LimitUnit.IMPRESSION && reportData._1 >= requireInThisBlock) ||
                        (r.model.limitUnit.toLowerCase == LimitUnit.CLICK && reportData._2 >= requireInThisBlock)) false
                    else {
                        if(reportData._1 == 0 && reportData._2 == 0 && zeroReport.contains(r.banner.id) && counter.containsKey(r.banner.id)) {
                            impCounters.put(r.banner.id, counter.get(r.banner.id))
                        } else {
                            val remainImpInThisBlock = r.model.limitUnit.toLowerCase match {
                                case LimitUnit.CLICK => {
                                    val ctr = calculateCTR(r.model, lifetimeReportData, activeCampaignData)
                                    ((requireInThisBlock - reportData._2) / ctr).toLong
                                }
                                case LimitUnit.IMPRESSION => {
                                    requireInThisBlock - reportData._1
                                }
                                case _ => {
                                    0
                                }
                            }
                            if(remainImpInThisBlock > 0) disabled.remove(r.banner.id)
                            impCounters.put(r.banner.id, new AtomicLong((remainImpInThisBlock / Config.numOfServers.getValue * 0.7).toLong))
                            if(reportData._1 == 0 && reportData._2 == 0) tmpZeroReport.add(r.banner.id)
                        }
                        true
                    }
                }
            }
        }).map(r => (r.banner.id, r)).toMap

        counter = impCounters
        zeroReport = tmpZeroReport


        //make a map store current stats of banner in recent 15m block
        val block = 15 * 60 * 1000
        val startBlock = (now / block) * block
        val tempStatsInRecentBlock = new ConcurrentHashMap[Int, (AtomicInteger, Int)]()
        items.map(c=>c._2).foreach(r => {
            val reportData = {
                try {
                    env.fastReportService.getImpressionClick(r.banner.id, startBlock, now, "item")
                } catch {
                    case _: Throwable => {
                        env.connectReportService
                        (0.toLong, 0.toLong)
                    }
                }
            }
            val value : (Int, Int) = {
                r.model.rateUnit match {
                    case RateUnit.CPM => {
                        (
                            {
                                if(r.model.timeSpan == 0 || r.model.limit == 0) 1000*1000
                                else (15*60*1000f / r.model.timeSpan * r.model.limit - reportData._1).toInt
                            } / Config.numOfServers.getValue
                            , r.model.rate.toInt
                            )
                    }
                    case RateUnit.CPC => {
                        val lifetimeReportData = getReportData(r.banner.id, 0, now, "item")
                        val ctr : Float = calculateCTR(r.model, lifetimeReportData, activeCampaignData)
                        ( {
                            if (r.model.timeSpan == 0 || r.model.limit == 0) 1000 * 1000
                            else ((15 * 60 * 1000f / r.model.timeSpan * r.model.limit - reportData._2) / ctr).toInt
                        } / Config.numOfServers.getValue
                            , (r.model.rate * ctr * 1000).toInt
                            )
                    }
                    case _ => (0, 0)
                }
            }
            tempStatsInRecentBlock.put(r.banner.id, (new AtomicInteger(value._1), value._2))
        })

        statsInRecentBlock = tempStatsInRecentBlock
        items
    }

    def campaignToModel(item: Campaign): CampaignModel = {
        item.campaignType match {
            case CampaignType.Booking => {
                val instance = WebUtils.fromJson(classOf[BookingCampaignModel], item.extra)
                if (instance != null) {
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.Network => {
                val instance = WebUtils.fromJson(classOf[NetworkCampaignModel], item.extra)
                if (instance != null) {
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.NetworkTVC => {
                val instance = WebUtils.fromJson(classOf[NetworkTVCCampaignModel], item.extra)
                if (instance != null) {
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            //pr network
            case CampaignType.PRNetwork => {
                val instance = WebUtils.fromJson(classOf[PRNetworkCampaignModel], item.extra)
                if (instance != null) {
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
        }
    }

    //new updateBooking
    def updateBooking() {

        println("begin update --------------------------------- ")

        val fromTime = new Date().getTime

        // get running camps
        val campaignData = env.campaignDataDriver.
            loadAll().
            filter(p =>
            (!p.disable) && p.status == "Running" && (p.startDate <= fromTime) && (p.endDate == 0 || p.endDate >= fromTime)).toList
        val activeCamps = campaignData.map(p => p.id).toSet
        println(s"active camps: ${activeCamps.size}")

        // active item by camp
        val items = env.bannerDataDriver.loadAll().
            filter(b => (!b.disable) && activeCamps.contains(b.campaignId)).
            toList

        println(s"items: ${items.size}")

        //        val itemMap = items.groupBy(b => b.campaignId)
        val itemSet = items.map(i => i.id).toSet

        val bookingData = env.newBookDataDriver.loadAll().
            filter(i =>
            itemSet.contains(i.itemId) && (!i.disable) && i.from <= fromTime && i.to >= fromTime && this.banners.contains(i.itemId) && this.zones.contains(i.zoneId)).
            toList
        val links = bookingData.groupBy(b => b.zoneId)
        val linksByItem = bookingData.groupBy(b => b.itemId)

        val validItemIds = bookingData.map(b => b.itemId).toList.distinct
        val companionCampaignId = campaignData.filter(c => c.companion).map(c => c.id).toList
        val companionBanners = items.filter(i => validItemIds.contains(i.id)).groupBy(b => b.campaignId).filter(b => companionCampaignId.contains(b._1)).map(b => b._2).toList

        val companionData = new ConcurrentHashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Float]]]()
        var companionBannerIds = new ArrayBuffer[Int]

        for (companion <- companionBanners) {
            val zoneIds = companion.map(b => linksByItem(b.id)).flatten.toList.map(b => b.zoneId).toList.distinct
            val groupZones = zoneIds.map(z => this.zones(z)).groupBy(z => z.siteId).filter(p => p._2.length > 1)
            for (group <- groupZones.values) {
                val data = companion.map(c => {
                    val zids = group.map(b => b.id)
                    linksByItem(c.id).filter(b => zids.contains(b.zoneId))
//                    linksByItem(c.id).find(b => zids.contains(b.zoneId)) match {
//                        case Some(p) => p
//                        case _ => null
//                    }
                }).flatten.toList
                val tempMap = new mutable.HashMap[(Int, Int), Float]()
                data.filter(d => d != null).toList.foreach(b => tempMap.put((b.zoneId, b.itemId), b.share))
                data.filter(d => d != null).toList.map(b => b.zoneId).foreach(z => {
                    if (tempMap.keys.size > 1) {
                        companionBannerIds ++= tempMap.keys.toList.map(b => b._2)
                        val buffer = companionData.getOrAdd(z, id => new ArrayBuffer[HashMap[(Int, Int), Float]])
                        buffer += tempMap
                    }
                })
            }
        }

        companionBookingData = companionData //set data to memory
        synchronized({
            this.companionBookingBannerIds.clear()
            companionBannerIds.toList.distinct.foreach(b => this.companionBookingBannerIds.add(b))
        })

        val zones = new ConcurrentHashMap[Int, ZoneRecord]()

        for (link <- links) {
            val record = zones.getOrAdd(link._1, (id) => new ZoneRecord())
            record.links ++= link._2
        }

        map = zones
    }



    def filterCompanionNetwork(request: AdsRequest) : mutable.HashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Long]]] = {
        val zones = request.zoneIds
        val hashMap = new mutable.HashMap[Int,List[Int]]()

        for(zone <- zones) {
            if (networkZones.contains(zone))
                hashMap.put(zone, getNetworkRecords(new AdsRequest(request.site,List(zone),request.url,request.ip,request.cookie,request.viewed,request.items,request.companionTargeting,request.listValues))._1.toList.map(b=>b.banner.id))
        }

        val rs = new HashMap[Int, ArrayBuffer[mutable.HashMap[(Int, Int), Long]]]()

        for(zone <- zones) {
            if(companionNetworkData.containsKey(zone)) {
                rs.put(zone, companionNetworkData.get(zone).filter(p=>{
                    p.keys.map(k=>k._1).find(k=>(!hashMap.contains(k))) match {
                        case Some(p) => false
                        case _ => {
                            p.keys.find(k=>(!hashMap(k._1).contains(k._2))) match {
                                case Some(p) => false
                                case _ => true
                            }
                        }
                    }
                }))
            }
        }
        rs.filter(p => p._2.length > 0)
    }

    def serveCompanionNetwork(request: AdsRequest): CompanionAdsResponse = {
        val zones = request.zoneIds
        val rs = new HashMap[Int, Array[Banner]]
        val served = new ArrayBuffer[Int]
        val data = filterCompanionNetwork(request)
        for (z <- zones) {
            if (!served.contains(z)) {
                if (data.contains(z)) {
                    val links = data.getOrAdd(z, id => new ArrayBuffer[mutable.HashMap[(Int, Int), Long]]).
                        toList.filter(p => p.keySet.map(b => b._1).toList.filter(b => (!zones.contains(b))).length == 0).toList
                    if (links.length > 0) {
                        for (link <- links) {
                            if (link.keySet.map(b => b._1).toList.filter(p => served.contains(p)).length == 0) {
                                link.foreach(l => {
                                    rs.put(l._1._1, Array(banners(l._1._2)))
                                    val atomic = counter.get(l._1._2)
                                    if(atomic != null) {
                                        if(atomic.decrementAndGet() <= 0) disabled.add(l._1._2)
                                    }
                                    served += l._1._1
                                })
                            }
                        }
                    } else {
                        if (!networkZones.contains(z)) {
                            rs.put(z, null)
                        } else {
                            rs.put(z, serveNetwork(
                                new AdsRequest(request.site, List(z), request.url, request.ip, request.cookie, request.viewed, request.items, null, request.listValues),
                                (x: String) => GeoipService.getGeoipClient.getLocation(x),
                                true
                            ).banner)
                        }
                    }
                } else {
                    rs.put(z, serveNetwork(
                        new AdsRequest(request.site, List(z), request.url, request.ip, request.cookie, request.viewed, request.items, null, request.listValues),
                        (x: String) => GeoipService.getGeoipClient.getLocation(x),
                        true
                    ).banner)
                }
            }
        }
        new CompanionAdsResponse(rs)
    }



    def getCompanionVideo(request: AdsRequest, tvcItems: Array[BannerModel]): mutable.Map[Int, Map[Int, BannerModel]] = {
        if(request.zoneIds.length <= 1) return mutable.Map[Int, Map[Int, BannerModel]]()
        val zoneData = request.zoneIds.slice(1, request.zoneIds.length + 1)
        val rs : mutable.Map[Int, Map[Int, BannerModel]] = mutable.Map[Int, Map[Int, BannerModel]]()
        for(item <- tvcItems) {
            val data : List[Int] = companionNetworkCampaign.get(item.campaignId)
            if(data != null) {
                val bannerModels = data.filter(b=>banners.contains(b)).map(b => banners(b)).map(b=>mapToNetwork(b))
                val map : mutable.Map[Int, BannerModel] = mutable.Map[Int, BannerModel]()
                for(z <- zoneData) {
                    //map item and zone

                    val zone = if(zones contains(z)) zones(z) else null
                    val filterData = bannerModels.filter(b=>(b.id != item.id) && ((b.targetZones != null && b.targetZones.contains(z)) || (b.targetContent != null && zone != null && b.targetContent.contains(zone.siteId))))
                    if(filterData.length > 0) map.put(z, {
                        val filterDuplicate = filterData.filter(z=>map.values.exists(c=>c.id == z.id))
                        if(filterDuplicate.length > 0) filterDuplicate(0)
                        else filterData(0)
                    })
                }
                rs.put(item.id, map.toMap)
            }
        }
        rs
    }


    def generateZingTVVideoAds(zoneId: Int, requirement: java.util.ArrayList[LinkedTreeMap[String,Any]], tvc : ArrayBuffer[NetworkTVCBannerModel], overlays: List[NetworkOverlayBannerModel], freq : mutable.Map[Int, Int]) = {
        val videosData = mutable.HashMap[Int, ArrayBuffer[(BannerModel, Int, Int)]]()
        val overlayData = new ArrayBuffer[(BannerModel, Int, Int)]
        var currentVideo = (0,0) //(videoid,startTime)
        for(i <- 0 until requirement.size()) {
            val s = requirement.get(i)
            val adsType = s.get("t")
            val start = s.get("s").toString.toFloat.toInt
            val duration = s.get("d").toString.toFloat.toInt
            val maxPod = (s.get("i") ?? "1").toString.toFloat.toInt

            adsType match {
                case ZingTVVideoKind.Skip => {
                    if(start != currentVideo._2) currentVideo = (0, currentVideo._2)
                    val data = tvc.filter(c=>{
                        if(freq.contains(c.id)) {
                            freq(c.id) > 0
                        } else true
                    }) //filter
                    val videos = generatePodVideoAds(zoneId, data, duration, maxPod)
                    val block = new ArrayBuffer[(BannerModel, Int, Int)]
                    videos.foreach(video=>{
                        val item = (video, start, video.duration.toInt)
                        currentVideo = (item._1.id, start)
                        block += item
                    })
                    if(!videosData.contains(start)) {
                        videosData(start) = block
                        block.foreach(c=>{
                            if(freq.contains(c._1.id)) freq(c._1.id) -= 1
                        })
                    } else {
                        val existRate = videosData(start).map(c=>statsInRecentBlock.getOrAdd(c._1.id, key => (new AtomicInteger(0), 0))._2).sum
                        val currentRate = block.map(c=>statsInRecentBlock.getOrAdd(c._1.id, key => (new AtomicInteger(0), 0))._2).sum
                        if(existRate < currentRate) {
                            videosData(start).foreach(c=>{
                                if(freq.contains(c._1.id)) freq(c._1.id) += 1
                            })
                            videosData(start) = block
                            block.foreach(c=>{
                                if(freq.contains(c._1.id)) freq(c._1.id) -= 1
                            })
                        }
                    }
                }
                case ZingTVVideoKind.Standard => {
                }
                case ZingTVVideoKind.Trueview => {

                }
                case ZingTVVideoKind.Overlay => {
                    val items = generateNonLinearAds(overlays, null, maxPod)
                    items.foreach(i=>{
                        val item = (i, start, duration)
                        overlayData += item
                    })
                }
            }
        }
        val resultTvc : ArrayBuffer[(BannerModel, Int, Int)] = new ArrayBuffer[(BannerModel, Int, Int)] ++ videosData.values.toArray.flatten
        Map("data" -> resultTvc,
            "overlay" -> overlayData)
    }

    def getVideoData(request: AdsRequest) : (Array[ArrayBuffer[(Banner, BannerModel)]], mutable.Map[Int, Int]) = {
        val zoneId = {
            if(request.zoneIds.length > 0) request.zoneIds(0)
            else return null
        }
        if (!networkZones.contains(zoneId)) return null

        val zone = zones(zoneId)
        val zoneModel = zone.kind match {
            case ZoneKind.Video => {
                val instance = WebUtils.fromJson(classOf[VideoZoneModel], zone.extra)
                if (instance != null) {
                    instance.id = zone.id
                }
                instance
            }
            case _ => null.asInstanceOf[VideoZoneModel]
        }

        if (zoneModel == null) return null

        val networkData = getNetworkRecords(request)
        val networkRecords = networkData._1
        val freq = networkData._2
        if (networkRecords == null || networkRecords.length == 0) return null

        val preAds = new ArrayBuffer[(Banner, BannerModel)]()
        val midAds = new ArrayBuffer[(Banner, BannerModel)]()
        val postAds = new ArrayBuffer[(Banner, BannerModel)]()
        val nonLinearAds = new ArrayBuffer[(Banner, BannerModel)]()

        for (networkRecord <- networkRecords) {
            if (networkRecord.banner.kind == BannerKind.NetworkTVC && networkRecord.model.asInstanceOf[NetworkTVCBannerModel].duration > 0) {
                val bannerModel = networkRecord.model.asInstanceOf[NetworkTVCBannerModel]
                val positions = {
                    if(bannerModel.positions != null) bannerModel.positions
                    else if(bannerModel.targetContent != null){
                        val data = new ArrayBuffer[TVCItemPosition]
                        zones.map(c=>c._2).filter(z=>bannerModel.targetContent.exists(c=> z.siteId == c) && z.kind == ZoneKind.Video).
                            foreach(z=>{
                            data += new TVCItemPosition(z.id, VideoZonePosition.PRE)
                            data += new TVCItemPosition(z.id, VideoZonePosition.MID)
                            data += new TVCItemPosition(z.id, VideoZonePosition.POST)
                        })
                        data.toArray
                    } else new Array[TVCItemPosition](0)
                }
                for (pos <- positions) {
                    pos.position match {
                        case VideoZonePosition.PRE => {
                            if (zoneModel.preMaxPodDuration >= bannerModel.duration && pos.zoneId == zoneId) {
                                val tuple = (networkRecord.banner, bannerModel)
                                preAds += tuple
                            }
                        }
                        case VideoZonePosition.MID => {
                            if (zoneModel.midMaxPodDuration >= bannerModel.duration && pos.zoneId == zoneId) {
                                val tuple = (networkRecord.banner, bannerModel)
                                midAds += tuple
                            }
                        }
                        case VideoZonePosition.POST => {
                            if (zoneModel.postMaxPodDuration >= bannerModel.duration && pos.zoneId == zoneId) {
                                val tuple = (networkRecord.banner, bannerModel)
                                postAds += tuple
                            }
                        }
                    }
                }
            }
            else if (networkRecord.banner.kind == BannerKind.NetworkOverlayBanner) {
                val tuple = (networkRecord.banner, networkRecord.model)
                nonLinearAds += tuple
            }
        }
        (Array(preAds, midAds, postAds, nonLinearAds), freq)
    }





    def generateNonLinearAds(data: List[NetworkOverlayBannerModel], zoneModel: VideoZoneModel, maxOverlays: Int = Int.MaxValue): Array[BannerModel] = {
        val result = new ArrayBuffer[BannerModel]()


        if(zoneModel != null) {
            if (zoneModel.timeSegments == null) return result.toArray
            for (seg <- zoneModel.timeSegments) {
                val banner = getItemByRate(data)
                if(banner != null) result += banner
            }
        } else {
            (1 to maxOverlays).foreach(c => {
                val banner = getItemByRate(data)
                if(banner != null) result += banner
            })
        }

//        if(maxOverlays > 1) {
//            if (zoneModel.timeSegments == null) return result.toArray
//            for (seg <- zoneModel.timeSegments) {
//                val banner = getItemByRate(data)
//                if(banner != null) result += banner
//            }
//        } else {
//            val banner = getItemByRate(data)
//            if(banner != null) result += banner
//        }
        result.toArray
    }

    def generateVideoAds(zoneId: Int, data: ArrayBuffer[NetworkTVCBannerModel], podDuration: Int, maxPod: Int = 1): Array[Array[BannerModel]] = {
        val result = new ArrayBuffer[Array[BannerModel]]()
        while(result.length < maxPod) {
            val pod = generatePodVideoAds(zoneId, data, podDuration)
            result += pod.map(c=>c.asInstanceOf[BannerModel])
        }
        result.toArray
    }

    def generatePodVideoAds(zoneId: Int, data: ArrayBuffer[NetworkTVCBannerModel], podDuration: Int, videosInPod: Int = Int.MaxValue, filteredId: Int = 0): Array[NetworkTVCBannerModel] = {
        val result = new ArrayBuffer[NetworkTVCBannerModel]()
        var currentDurationPod = 0
        var remain = data.filter(c=>c.id != filteredId)
        val mapData = data.map(c=>(c.id, statsInRecentBlock.getOrAdd(c.id, key => (new AtomicInteger(0),0)))).toMap
        while (remain.length > 0) {
            val totalRate = remain.map(b=>mapData(b.id)._2).sum
            var index = -1
            if(totalRate > 0) {
                val seed = ThreadLocalRandom.current().nextLong(totalRate)
                var t = seed
                while (t >= 0 && index + 1 < remain.size) {
                    index += 1
                    t -= {
                        val temp = remain(index)
                        statsInRecentBlock.getOrAdd(temp.id, key => (new AtomicInteger(0), 0))._2
                    }
                }
            } else {
                index = ThreadLocalRandom.current().nextInt(remain.length)
            }


            result += remain(index)
            if(result.length >= videosInPod) return result.toArray
            currentDurationPod += remain(index).duration.toInt
            remain = data.filter(b => b.duration + currentDurationPod <= podDuration && !result.exists(c=>c.id == b.id))
            //            if(remain.length > 1) remain = data.filter(b=>b.id != remain(index).id)
        }
        return result.sortWith((b,c) =>
            statsInRecentBlock.getOrAdd(b.id, key => (new AtomicInteger(0), 0))._2 > statsInRecentBlock.getOrAdd(c.id, key => (new AtomicInteger(0), 0))._2
        ).toArray
    }



}
