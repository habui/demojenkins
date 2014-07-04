/// <reference path="../../common/common.ts"/>
var rootModule = angular.module('app.root', ['ui.router']);

rootModule.factory('Page', function () {
    return new common.PageUtils('123Click');
});

rootModule.factory('CurrentTab', function () {
    return new common.CurrentTab();
});

rootModule.factory('BodyClass', function () {
    var classValue: string = '';
    return {
        getClass: function () { return classValue; },
        setClass: function (newClass: string) { classValue = newClass; }
    };
});

rootModule.factory('IDSearch', function (factory: backend.Factory) {
    return new common.IDSearch(factory);
});

rootModule.factory("fullSearch", function (RestfulService) {
    return new backend.FullSearch(RestfulService);
});
rootModule.filter("startFrom", function () {
    return function (input, start) {
        if (!!input) {
            start = +start;
            return input.slice(start);
        }
    }
});
rootModule.factory("factory", function(RestfulService) {
    var factory = new backend.Factory();
    factory.articleService = new backend.ArticleService(RestfulService);
    factory.assignedService = new backend.AssignedService("user_role", RestfulService);
    factory.agencyAccountService = new backend.AgencyAccountService("agency", RestfulService);
    factory.bookService = new backend.BookService(RestfulService);
    factory.campaignItemService = new backend.CampaignItemService(RestfulService);
    factory.campaignService = new backend.CampaignService(RestfulService);
    factory.categoryService = new backend.CategoryService("category", RestfulService);
    factory.conversionService = new backend.ConversionService("conversion", RestfulService);
    factory.miscService = new backend.MiscService(RestfulService);
    factory.orderService = new backend.OrderService("order", RestfulService);
    factory.permissionUtils = new utils.PermissionUtils(RestfulService);
    factory.reportService = new backend.ReportService(RestfulService);
    factory.sessionService = new backend.SessionService(RestfulService);
    factory.uniqueUserService = new backend.UniqueUserService(RestfulService);
    factory.userLogService = new backend.UserLogService("action_log", RestfulService);
    factory.userService = new backend.UserService("user", RestfulService);
    factory.roleService = new backend.RoleService("role", RestfulService);
    factory.userRoleService = new backend.UserRoleService("user_role", RestfulService);
    factory.websiteService = new backend.WebsiteService("website", RestfulService);
    factory.zoneGroupService = new backend.ZoneGroupService("zone_group", RestfulService);
    factory.zoneService = new backend.ZoneService("zone", RestfulService);
    factory.fullSearchService = new backend.FullSearch(RestfulService)
    factory.systemService = new backend.SystemService("config", RestfulService)

    factory.paramURLStore = new utils.DataStore();
    var user: models.UserInfo;
    var sessionId: string;
    factory.userInfo  = {
        setUserInfo: (userinfo: models.UserInfo) => {
            user = userinfo;
        },
        getUserInfo: () => user,
        setSession: (sessionId: string) => {
            sessionId: sessionId;
        },
        getSession: () => sessionId
    }

    return factory;
})
rootModule.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/websites?page=1');
    $stateProvider
        .state('main', {
            views: {
                "main": {
                    templateUrl: "views/common/main",
                    controller: controllers.MainController
                }
            },
            resolve: {
                loggedin: function ($q, $location, factory: backend.Factory) {
                    var promise = $q.defer();
                    if (factory.userInfo.getUserInfo() !== undefined && factory.userInfo.getUserInfo() !== null) {
                        return promise.resolve();
                    }
                    var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
                    if (sessionid.length === 0) {
                        $location.path('/login');
                    }
                    factory.sessionService.getCurrentUser(sessionid, function (result) {
                        if (result.code === 1) {
                            factory.userInfo.setUserInfo(result.data);
                        }
                    });
                }
            }
        })
        .state("main.deny_access", {
            url: "/access-deny",
            templateUrl: "views/common/deny_access"
        })
        .state("main.404_notfound", {
            url: "/404-notfound",
            templateUrl: "views/common/404_notfound"
        })
        .state("validateZone", {
            url: "/validateZone/:websiteId",
            views: {
                "main": {
                    templateUrl: "views/website/validateZone",
                    controller: controllers.ValidateZoneController
                }
            }
        });;
});

