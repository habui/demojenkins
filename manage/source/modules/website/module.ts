var websiteModule = angular.module('app.website', ['ui.router', 'app.user', 'app.root', 'api.backend']);


websiteModule.config(function ($stateProvider, $urlRouterProvider) {

    $stateProvider
        .state('main.website', {
            templateUrl: "views/website/website",
            controller: controllers.WebsiteController
        })
        .state('main.website.create', {
            url: "/websites/create",
            views: {
                "content": {
                    templateUrl: "views/website/website.create",
                    controller: controllers.CreateWebsiteController
                }
            }
        })
        .state('main.website.approveAds', {
            url: "/websites/approveads",
            views: {
                "content": {
                    templateUrl: "views/website/approveads",
                    controller: controllers.ApproveAdsController
                }
            }
        })
        .state('main.website.list', {
            url: "/websites",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/website.list",
                    controller: controllers.WebsiteListController
                }

            }
        })
        .state('main.website.detail', {
            url: "/websites/:websiteId",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/website.detail",
                    controller: controllers.WebsiteDetailController
                }
            }
        })
        .state('main.website.detail.zonegroup', {
            url: "/zonegrps",
            templateUrl: "views/website/zonegroup.list",
            controller: controllers.ZoneGroupListController
        })
        .state('main.website.detail.shared', {
            url: "/shared",
            templateUrl: "views/website/website.detail.shared",
            controller: controllers.SharedController,
            onExit: function($stateParams, $state, $modal) {
                if ($state.scope && $state.scope.hasSave) {
                    var webModal = $modal.open({
                        templateUrl: "views/views/common/modal.confirm",
                        controller: common.ModalConfirmController,
                        resolve: {
                            data: function() {
                                return $state.scope;
                            },
                            title: function() {
                                return 'Save Confirm';
                            },
                            bodyMessage: function() {
                                return 'Do you want to save these changes?';
                            }
                        }
                    });
                    webModal.result.then(
                        function(scope) {
                            scope.save();
                        },
                        function(message) {
                        });
                }
            }
        })
        .state('main.website.detail.setting', {
            url: "/setting",
            templateUrl: "views/website/website.detail.setting",
            controller: controllers.WebsiteSettingController
        })
        .state('main.website.detail.zone', {
            url: "/zones",
            templateUrl: "views/website/zone.list",
            controller: controllers.ZoneListController
        })
        .state('main.website.detail.inventory', {
            url: "/inventory",
            templateUrl: "views/website/inventory",
            controller: controllers.InventoryController
        })
        .state('main.website.zonegroup_detail', {
            url: "/websites/:websiteId/zonegrp/:zoneGroupId",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zonegroup.detail",
                    controller: controllers.ZoneGroupDetailController
                }
            }
        })
        .state('main.website.zonegroup_detail.zone', {
            url: "/zones",
            templateUrl: "views/website/zone.list",
            controller: controllers.ListZoneInZoneGroupController
        })
        .state('main.website.zonegroup_detail.setting', {
            url: "/setting",
            templateUrl: "views/website/zonegroup.setting",
            controller: controllers.ZoneGroupSettingController
        })
        .state('main.website.zonegroup_detail.przones', {
            url: "/przones",
            templateUrl: "views/website/przone.list",
            controller: controllers.ListZoneInZoneGroupController
        })
        .state('main.website.zone_detail', {
            url: "/websites/:websiteId/zone/:zoneId/type/:type",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.detail",
                    controller: controllers.ZoneDetailController
                }
            }
        })
        .state('main.website.zone_detail.linked_items', {
            url: "/zones",
            templateUrl: "views/website/linked_item.list",
            controller: controllers.LinkedItemListController
        })
        .state('main.website.zone_detail.setting', {
            url: "/setting",
            templateUrl: "views/website/zone.setting",
            controller: controllers.ZoneSettingController
        })
        .state('main.website.zone_detail.articles', {
            url: "/articles",
            templateUrl: "views/article/zone.articles.list",
            controller: controllers.ZoneArticleListController
        })
        .state('main.website.zone_detail.booking', {
            url: "/booking-schedule",
            templateUrl: "views/website/zone.booking.schedule",
            controller: controllers.ZoneBookingScheduleController
        })
        .state('main.website.zonegroup_create', {
            url: "/websites/:websiteId/zonegroup/create",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zonegroup.create",
                    controller: controllers.ZoneGroupCreateController
                }
            }
        })
        .state('main.website.zone_type', {
            url: "/websites/:websiteId/zone_type",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.type",
                    controller: controllers.ZoneTypeController
                }
            }
        })
        .state('main.website.banner_zone_create', {
            url: "/websites/:websiteId/zone/banner/create",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.banner.create",
                    controller: controllers.BannerZoneCreateController
                }
            }
        })
        .state('main.website.video_zone_create', {
            url: "/websites/:websiteId/zone/video/create",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.video.create",
                    controller: controllers.VideoZoneCreateController
                }
            }
        })
    //review soon
        .state('main.website.pr_zone_create', {
            url: "/websites/:websiteId/zone/pr/create",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.pr.create",
                    controller: controllers.PrZoneCreateController
                }
            }
        })
        .state('main.website.create_przone', {
            url: "/websites/:websiteId/zone/pr/create",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/website/zone.pr.create",
                    controller: controllers.ZonePRCreateController
                }
            }
        })
    //-----------
        .state("main.website.search", {
            url: '/search/:keywork',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/common/common.search-display",
                    controller: controllers.FullSearchController
                }
            }
        })
    // PR Article setting
        .state('main.website.article_setting', {
            url: '/websites/:websiteId/:zoneId/:articleId/setting',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.WebsiteBreadcumNavBarController
                },
                "content": {
                    templateUrl: "views/article/create.pr.article",
                    controller: controllers.CreatePrArticleController
                }
            }
        })
});

