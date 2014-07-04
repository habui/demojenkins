package ads.website.modules

import ads.common.model.{ZoneModel, INetwork}
import ads.common.sessions.{RetargetingStore, SiteZoneFreqStore, Session}
import ads.website.Environment

object FrequencyUtils {
    def writeFreq(cookie: String, bannerId: Int, model: ZoneModel, time: Long) {
        writeFreqBanner(cookie, bannerId, time)
        writeFreqSiteZone(cookie, bannerId, model, time)
    }
    
    private def writeFreqBanner(cookie: String, bannerId: Int, now: Long) = {
        try {
            val banner = Environment.bannerCache.get(bannerId)
            if(banner != null && banner.kind.contains("network") && banner.asInstanceOf[INetwork].freqCapping > 0) {
                var retry = 0
                while(retry < 5) {
                    val freq = Session.store.getOrAdd(cookie)
                    freq.put(bannerId, (now/1000).toInt)
                    if(Session.store.put(cookie, freq)) retry = 10
                    else retry += 1
                }
            }
        }catch {
            case ex : Throwable => {
            }
        }
    }

    private def writeFreqSiteZone(cookie: String, bannerId: Int, model: ZoneModel, time: Long) {
        if(model == null) return

        val banner = Environment.bannerCache.get(bannerId)
        if(banner == null || !banner.checkFreqSiteOrZone) return

        val site = Environment.websiteService.load(model.siteId)
        if(site == null) return
        if (site.frequencyCapping == 0 && model.frequencyCapping == 0) return
        val freqCappingTime = {
            if(model.frequencyCapping != 0) model.frequencyCappingTime
            else site.frequencyCappingTime
        }
        val freq = SiteZoneFreqStore.store.getOrAdd(cookie)

        //check expire data
        var retry = 0
        val now = (System.currentTimeMillis()/1000).toInt
        do {
            val data = freq.logs
            val v = freq.version
            val times = (data zip data.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList
            var changed = false

            val newTimes = times.filter(c=>{
                if(c._2 >= now - freqCappingTime) true
                else {
                    changed = true
                    false
                }
            }).toArray :+ (model.id, now)
            if(changed) {
                val session = new Session
                session.logs = newTimes.map(c=>List(c._1,c._2)).flatten.toArray
                session.version = v
                SiteZoneFreqStore.store.put(cookie, session)
//                if(freq.set(newTimes.map(c=>List(c._1,c._2)).flatten.toArray, v.get())) retry = 5
            } else {
                freq.put(model.id, (time/1000).toInt)
                retry = 5
                SiteZoneFreqStore.store.put(cookie, freq)
            }
            retry += 1
        } while(retry < 5)


    }
}

object RetargetingUtils {
    def writeRetargetingData(cookie: String, retargeting: Int) {
        try {
            val freq = RetargetingStore.store.getOrAdd(cookie)
            if(!freq.logs.exists(p=>p==retargeting)) {
                freq.put(retargeting, (System.currentTimeMillis()/1000).toInt)
                RetargetingStore.store.put(cookie, freq)
            } else {
                val index = freq.logs.indexOf(retargeting)
                freq.logs(index + 1) = (System.currentTimeMillis()/1000).toInt
                RetargetingStore.store.put(cookie, freq)
            }
            //TODO: should update expire item
        } catch {
            case x: Throwable => x.printStackTrace()
        }

    }
}
