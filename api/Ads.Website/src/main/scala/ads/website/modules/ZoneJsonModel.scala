package ads.website.modules

import java.util


class BannerJsonModel (
    val iheight: Int,
    val iwidth: Int,
    val zheight: Int,
    val zwidth: Int,
    val format: String,
    val mediaurl: String,
    val bannerFileTracking: String,
    val embeddedHtml: String,
    val usage: Float,
    val trackingurl: String,
    val clickurl: String,
    val thirdPartyImpressionUrl: List[String],
    val extend: util.HashMap[String, String],
    val kind: String)


