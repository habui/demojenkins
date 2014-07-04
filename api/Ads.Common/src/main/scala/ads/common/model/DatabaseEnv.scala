package ads.common.model

import ads.common.database._
import ads.common.services.{SessionService, ISessionService}

trait DatabaseEnv {

//    lazy val userDataService: IDataService[User] = new SqlDataService[User](()=>new User(0,0,"", "",""))
//    lazy val zoneGroupDataService: IDataService[ZoneGroup] = new SqlDataService[ZoneGroup](()=>new ZoneGroup(0,0,""))
//    lazy val zoneToZoneGroupDataService: IDataService[ZoneToZoneGroup] = new SqlDataService[ZoneToZoneGroup](()=>new ZoneToZoneGroup(0,0,0))
//    lazy val zoneDataService: IDataService[Zone] = new SqlDataService[Zone](()=>new Zone(0,0,null,null,null,0,0,0,0,0,ZoneKind.Banner,null))
//    lazy val zoneToBannerDataService: IDataService[ZoneToBanner] = new SqlDataService[ZoneToBanner](()=>new ZoneToBanner(0,0,0,0,0))
//    lazy val websiteDataService: IDataService[Website] = new SqlDataService[Website](()=>new Website(0,0,"","",""))
//    lazy val bannerDataService: IDataService[Banner] = new SqlDataService[Banner](()=>new Banner(0,0,null,null,null,0,0,null,null,0,0))
//    lazy val orderDataService: IDataService[Order] = new SqlDataService[Order](()=>new Order(0,0,null,null))
//    lazy val campaignDataService: IDataService[Campaign] = new SqlDataService[Campaign](()=>new Campaign(0,0,null,CampaignStatus.RUNNING,0,0,null,false,null))
//    lazy val bookDataService: IDataService[BookRecord] = new SqlDataService[BookRecord](()=>new BookRecord(0, 0,0,0,0,0))
////    lazy val agencyDataService:IDataService[Agency] = new SqlDataService[Agency](()=>new Agency(0,"","","",0))
////    lazy val userRoleDataService : IDataService[UserRole] = new SqlDataService[UserRole](() => new UserRole(0,0,"",0,null))
//    lazy val userRoleDataService : IDataService[UserRole] = new SqlDataService[UserRole](() => new UserRole(0,0,"",0,null))
//    lazy val roleDataService : IDataService[Role] = new SqlDataService[Role](() => new Role(0,"","",0,""))
//    lazy val categoryDataService : IDataService[Category] = new SqlDataService[Category](() => new Category(0,null,null))
//    lazy val actionLogDataService : IDataService[ActionLog] = new SqlDataService[ActionLog](() => new ActionLog(0,0,null,null,0,null,0,null,null))
//    lazy val conversionDataService: IDataService[Conversion] = new SqlDataService[Conversion](() => new Conversion(0,"",0,0,"",0,"",0))
//    //DAO for article
//    lazy val articleDataService:IDataService[Article] = new SqlDataService[Article](()=>new Article(0,"","","","","",0,null,null,0,"","","",false,null,0,0,0,0,0,0,0,0,0,0))
//    lazy val articleCommentDataService:IDataService[ArticleComment] = new  SqlDataService[ArticleComment](()=> new ArticleComment(0,0,"","",0,0))
//    lazy val articleStatsDataService:IDataService[ArticleStats] = new  SqlDataService[ArticleStats](()=> new ArticleStats(0,0,0,0,0,0))
//    lazy val articleLocationDataService:IDataService[ArticleLocation] = new  SqlDataService[ArticleLocation](()=> new ArticleLocation(0,0,"",0,0))
//    lazy val articleGenderDataService:IDataService[ArticleGender] = new  SqlDataService[ArticleGender](()=> new ArticleGender(0,0,"",0,0))
//    lazy val articleAgeDataService:IDataService[ArticleAge] = new  SqlDataService[ArticleAge](()=> new ArticleAge(0,0,"",0,0))
//
//    lazy val newBookDataService:IDataService[NewBookRecord] = new SqlDataService[NewBookRecord](()=>new NewBookRecord(0, 0,0,0,0,0,ZoneToBannerStatus.PENDING))

    ////////////JSON -> ZOOKEEPER


//    lazy val userDataService: IDataService[User] = new JsonDataStore[User]()
//    lazy val zoneGroupDataService: IDataService[ZoneGroup] = new JsonDataStore[ZoneGroup]()
//    lazy val zoneToZoneGroupDataService: IDataService[ZoneToZoneGroup] = new JsonDataStore[ZoneToZoneGroup]()
//    lazy val zoneDataService: IDataService[Zone] = new JsonDataStore[Zone]()
//    lazy val zoneToBannerDataService: IDataService[ZoneToBanner] = new JsonDataStore[ZoneToBanner]()
//    lazy val websiteDataService: IDataService[Website] = new JsonDataStore[Website]()
//    lazy val bannerDataService: IDataService[Banner] = new JsonDataStore[Banner]()
//    lazy val orderDataService: IDataService[Order] = new JsonDataStore[Order]()
//    lazy val campaignDataService: IDataService[Campaign] = new JsonDataStore[Campaign]()
//    lazy val bookDataService: IDataService[BookRecord] = new JsonDataStore[BookRecord]()
//    //    lazy val agencyDataService:IDataService[Agency] = new SqlDataService[Agency](()=>new Agency(0,"","","",0))
//    //    lazy val userRoleDataService : IDataService[UserRole] = new SqlDataService[UserRole](() => new UserRole(0,0,"",0,null))
//    lazy val userRoleDataService : IDataService[UserRole] = new JsonDataStore[UserRole]()
//    lazy val roleDataService : IDataService[Role] = new JsonDataStore[Role]()
//    lazy val categoryDataService : IDataService[Category] = new JsonDataStore[Category]()
//    lazy val actionLogDataService : IDataService[ActionLog] = new JsonDataStore[ActionLog]()
//        lazy val conversionDataService: IDataService[Conversion] = new JsonDataStore[Conversion]()
//    //DAO for article
//    lazy val articleDataService:IDataService[Article] = new JsonDataStore[Article]()
//    lazy val articleCommentDataService:IDataService[ArticleComment] = new  JsonDataStore[ArticleComment]()
//    lazy val articleStatsDataService:IDataService[ArticleStats] = new  JsonDataStore[ArticleStats]()
//    lazy val articleLocationDataService:IDataService[ArticleLocation] = new  JsonDataStore[ArticleLocation]()
//    lazy val articleGenderDataService:IDataService[ArticleGender] = new  JsonDataStore[ArticleGender]()
//    lazy val articleAgeDataService:IDataService[ArticleAge] = new  JsonDataStore[ArticleAge]()
//
//    lazy val newBookDataService:IDataService[NewBookRecord] = new JsonDataStore[NewBookRecord]()
//
//    /////////////////////////
//
    lazy val userDataDriver: IDataDriver[User] = new MysqlDriver[User]()
    lazy val zoneGroupDataDriver: IDataDriver[ZoneGroup] = new MysqlDriver[ZoneGroup]()
    lazy val configTableDataDriver: IDataDriver[ConfigTable] = new MysqlDriver[ConfigTable]()
    lazy val zoneToZoneGroupDataDriver: IDataDriver[ZoneToZoneGroup] = new MysqlDriver[ZoneToZoneGroup]()
    lazy val zoneDataDriver: IDataDriver[Zone] = new MysqlDriver[Zone]()
    lazy val zone2DataDriver: IDataDriver[Zone] = new MysqlDriver[Zone]()
    lazy val zoneToBannerDataDriver: IDataDriver[ZoneToBanner] = new MysqlDriver[ZoneToBanner]()
    lazy val websiteDataDriver: IDataDriver[Website] = new MysqlDriver[Website]()
    lazy val bannerDataDriver: IDataDriver[Banner] = new MysqlDriver[Banner]()
    lazy val banner2DataDriver: IDataDriver[Banner] = new MysqlDriver[Banner]()
    lazy val orderDataDriver: IDataDriver[Order] = new MysqlDriver[Order]()
    lazy val campaignDataDriver: IDataDriver[Campaign] = new MysqlDriver[Campaign]()
    lazy val campaign2DataDriver: IDataDriver[Campaign] = new MysqlDriver[Campaign]()
    lazy val bookDataDriver: IDataDriver[BookRecord] = new MysqlDriver[BookRecord]()
    //    lazy val agencyDataService:IDataService[Agency] = new SqlDataService[Agency](()=>new Agency(0,"","","",0))
    //    lazy val userRoleDataService : IDataService[UserRole] = new SqlDataService[UserRole](() => new UserRole(0,0,"",0,null))
    lazy val userRoleDataDriver : IDataDriver[UserRole] = new MysqlDriver[UserRole]()
    lazy val roleDataDriver : IDataDriver[Role] = new MysqlDriver[Role]()
    lazy val categoryDataDriver : IDataDriver[Category] = new MysqlDriver[Category]()
    lazy val actionLogDataDriver : IDataDriver[ActionLog] = new MysqlDriver[ActionLog]()
    lazy val conversionDataDriver: IDataDriver[Conversion] = new MysqlDriver[Conversion]()
    lazy val conversionTypeDataDriver: IDataDriver[ConversionType] = new MysqlDriver[ConversionType]()
    //DAO for article
    lazy val articleCommentDataDriver:IDataDriver[ArticleComment] = new  MysqlDriver[ArticleComment]()
    lazy val articleStatsDataDriver:IDataDriver[ArticleStats] = new  MysqlDriver[ArticleStats]()
    lazy val articleLocationDataDriver:IDataDriver[ArticleLocation] = new  MysqlDriver[ArticleLocation]()
    lazy val articleGenderDataDriver:IDataDriver[ArticleGender] = new  MysqlDriver[ArticleGender]()
    lazy val articleAgeDataDriver:IDataDriver[ArticleAge] = new  MysqlDriver[ArticleAge]()

    lazy val newBookDataDriver:IDataDriver[NewBookRecord] = new MysqlDriver[NewBookRecord]()

    ///////////////////////////JSON -> ZOOKEEPER



//    lazy val userDataService: IDataDriver[User] = new ZKDataService[User]()
//    lazy val zoneGroupDataService: IDataDriver[ZoneGroup] = new ZKDataService[ZoneGroup]()
//    lazy val zoneToZoneGroupDataService: IDataDriver[ZoneToZoneGroup] = new ZKDataService[ZoneToZoneGroup]()
//    lazy val zoneDataService: IDataDriver[Zone] = new ZKDataService[Zone]()
//    lazy val zoneToBannerDataService: IDataDriver[ZoneToBanner] = new ZKDataService[ZoneToBanner]()
//    lazy val websiteDataService: IDataDriver[Website] = new ZKDataService[Website]()
//    lazy val bannerDataService: IDataDriver[Banner] = new ZKDataService[Banner]()
//    lazy val orderDataService: IDataDriver[Order] = new ZKDataService[Order]()
//    lazy val campaignDataService: IDataDriver[Campaign] = new ZKDataService[Campaign]()
//    lazy val bookDataService: IDataDriver[BookRecord] = new ZKDataService[BookRecord]()
//    //    lazy val agencyDataService:IDataService[Agency] = new SqlDataService[Agency](()=>new Agency(0,"","","",0))
//    //    lazy val userRoleDataService : IDataService[UserRole] = new SqlDataService[UserRole](() => new UserRole(0,0,"",0,null))
//    lazy val userRoleDataService: IDataDriver[UserRole] = new ZKDataService[UserRole]()
//    lazy val roleDataService: IDataDriver[Role] = new ZKDataService[Role]()
//    lazy val categoryDataService: IDataDriver[Category] = new ZKDataService[Category]()
//    lazy val actionLogDataService: IDataDriver[ActionLog] = new ZKDataService[ActionLog]()
//    lazy val conversionDataService: IDataDriver[Conversion] = new ZKDataService[Conversion]()
//    //DAO for article
//    lazy val articleDataService:IDataDriver[Article] = new ZKDataService[Article]()
//    lazy val articleCommentDataService:IDataDriver[ArticleComment] = new  ZKDataService[ArticleComment]()
//    lazy val articleStatsDataService:IDataDriver[ArticleStats] = new  ZKDataService[ArticleStats]()
//    lazy val articleLocationDataService:IDataDriver[ArticleLocation] = new  ZKDataService[ArticleLocation]()
//    lazy val articleGenderDataService:IDataDriver[ArticleGender] = new  ZKDataService[ArticleGender]()
//    lazy val articleAgeDataService:IDataDriver[ArticleAge] = new  ZKDataService[ArticleAge]()
//
//    lazy val newBookDataService:IDataDriver[NewBookRecord] = new ZKDataService[NewBookRecord]()
}
