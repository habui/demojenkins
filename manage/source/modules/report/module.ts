
var reportModule = angular.module('app.report', ['ui.router', 'app.user', 'app.root', 'api.backend']);

reportModule.factory("filterIdsService", function ($rootScope) { 
    var services = {
        ids: [],
        addIds: function(ids) {
            this.ids = ids;
            $rootScope.$broadcast("idsPut");
        }
    }
    return services;
});


reportModule.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('main.report', {
            templateUrl: "views/report/report",
            controller: controllers.ReportTabController
        })
        .state('main.report.list', {
// list orders
            url: '/report',
            resolve: {
                loggedin: function($q, $location, factory: backend.Factory) {
                    var promise = $q.defer();
                    var userinfo: models.UserInfo = factory.userInfo.getUserInfo();
                    if (userinfo !== undefined && userinfo !== null) {
                        if (userinfo.isAdvertiser())
                            $location.path('/report/order');
                        else if (userinfo.isPublisher())
                            $location.path('/report/website');
                    } else {
                        $location.path('/login');
                    }
                }
            }
        })
        .state('main.report.orders', {
            url: '/report/order?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderController
                }
            }
        })
        .state('main.report.websites', {
            url: '/report/website?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteController
                }
            }
        })
        // -------- order --------
        .state('main.report.order', {
            url: '/report/order/:orderId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.order.campaign', {
            url: '/campaign?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderCampaignController
                }
            }
        })
        .state('main.report.order.linkedzone', {
            url: '/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderLinkedZoneController
                }
            }
        })
        .state('main.report.order.linkedsite', {
            url: '/linkedsite?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderLinkedSiteController
                }
            }
        })
        .state('main.report.order_linkedzonebysite', {
            url: '/report/order/:orderId/website/:websiteId/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderLinkedZoneBySiteController
                }
            }
        })
        .state('main.report.order.item', {
            url: '/item?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderItemController
                }
            }
        })
        //----------------campaign----------------------
        .state('main.report.campaign', {
            url: '/report/order/:orderId/campaign/:campaignId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.campaign.item', {
            url: '/item?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignItemController
                }
            }
        })
        .state('main.report.campaign.linkedzone', {
            url: '/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignLinkedZoneController
                }
            }
        })
        .state('main.report.campaign.linkedsite', {
            url: '/linkedsite?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignLinkedSiteController
                }
            }
        })
        .state('main.report.campaign_linkedzonebysite', {
            url: '/report/order/:orderId/campaign/:campaignId/website/:websiteId/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignLinkedZoneBySiteController
                }
            }
        })
        //----------------item----------------------
        .state('main.report.item', {
            url: '/report/order/:orderId/campaign/:campaignId/item/:itemId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.item.linkedzone', {
            url: '/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportItemLinkedZoneController
                }
            }
        })
        .state('main.report.item.linkedsite', {
            url: '/linkedsite?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportItemLinkedSiteController
                }
            }
        })
        .state('main.report.item_linkedzonebysite', {
            url: '/report/order/:orderId/campaign/:campaignId/item/:itemId/website/:websiteId/linkedzone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportItemLinkedZoneBySiteController
                }
            }
        })
        // -------- website --------
        .state('main.report.website', {
            url: '/report/website/:websiteId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.website.zonegroup', {
            url: '/zonegroup?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteZoneGroupController
                }
            }
        })
        .state('main.report.website.zone', {
            url: '/zone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteZoneController
                }
            }
        })
        .state('main.report.website.linkedcamp', {
            url: '/linkedcamp?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteLinkedCampaignController
                }
            }
        })
        .state('main.report.website.linkeditem', {
            url: '/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteLinkedItemController
                }
            }
        })
        .state('main.report.website_linkeditembycampaign', {
            url: '/report/website/:websiteId/campaign/:campaignId/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteLinkedItemByCampaignController
                }
            }
        })
        //.state('main.report.website.subzone', {
        //    url: '/videoclip?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
        //    views: {
        //        'content': {
        //            templateUrl: "views/report/report.common.list",
        //            controller: controllers.ReportSubZoneController
        //        }
        //    }
        //})
        //zone group
        .state('main.report.zonegroup', {
            url: '/report/website/:websiteId/zonegroup/:zonegroupId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.zonegroup.linkedcamp', {
            url: '/linkedcamp?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZonegroupLinkedCampaignController
                }
            }
        })
        .state('main.report.zonegroup.linkeditem', {
            url: '/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZonegroupLinkedItemController
                }
            }
        })
        .state('main.report.zonegroup_linkeditembycampaign', {
            url: '/report/website/:websiteId/zonegroup/:zonegroupId/campaign/:campaignId/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZonegroupLinkedItemByCampaignController
                }
            }
        })
        .state('main.report.zonegroup.zone', {
            url: '/zone?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZonegroupZoneController
                }
            }
        })
        //zone
        .state('main.report.zone', {
            url: '/report/website/:websiteId/zone/:zoneId',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.detail"
                }
            }
        })
        .state('main.report.zone.linkedcamp', {
            url: '/linkedcamp?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneLinkedCampaignController
                }
            }
        })
        .state('main.report.zone.linkeditem', {
            url: '/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneLinkedItemController
                }
            }
        })
        .state('main.report.zone_linkeditembycampaign', {
            url: '/report/website/:websiteId/zone/:zoneId/campaign/:campaignId/linkeditem?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneLinkedItemByCampaignController
                }
            }
        })
        .state('main.report.conversion', {
            url: '/report/orders/conversion?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrdersConversionController
                }
            }
        })
        .state('main.report.order.conversion', {
            url: '/conversion?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignsConversionController
                }
            }
        })
        .state('main.report.campaign.conversion', {
            url: '/conversion?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportItemsConversionController
                }
            }
        })
        .state('main.report.order.conversion_detail', {
            url: '/conversion_detail?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportOrderConversionDetail
                }
            }
        })
        .state('main.report.campaign.conversion_detail', {
            url: '/conversion_detail?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportCampaignConversionDetail
                }
            }
        })
        .state('main.report.item.conversion_detail', {
            url: '/conversion_detail?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportItemConversionDetail
                }
            }
        })

        //Delivery conversion
        .state('main.report.order.website_delivery', {
            url: '/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteOrderDelivery
                }
            }
        })
        .state('main.report.order.zone_delivery', {
            url: '/:websiteId/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneOrderDelivery
                }
            }
        })
        .state('main.report.campaign.website_delivery', {
            url: '/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteCampaignDelivery
                }
            }
        })
        .state('main.report.campaign.zone_delivery', {
            url: '/:websiteId/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneCampaignDelivery
                }
            }
        })
        .state('main.report.item.website_delivery', {
            url: '/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportWebsiteItemDelivery
                }
            }
        })
        .state('main.report.item.zone_delivery', {
            url: '/:websiteId/delivery?resolution&kind&from&to&type&cfrom&cto&ctype&filter',
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZoneItemDelivery
                }
            }
        })
        //--------------------Report Zing TV ----------------
        .state("main.report.zingtv", {
            url: "/report/zingtv?from&to&type",
            views: {
                'breadcumnav': {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.ReportBreadcumNavController
                },
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportZingTVController
                }
            }
        })

        //--------------------- Report PR banner ---------------
        .state("main.report.item.pr_item", {
            url: "/report/zingtv?resolution&kind&from&to&type&cfrom&cto&ctype&filter",
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportPRItemController
                }
            }
        })
        .state("main.report.item.pr_article", {
            url: "/report/zingtv?resolution&kind&from&to&type&cfrom&cto&ctype&filter",
            views: {
                'content': {
                    templateUrl: "views/report/report.common.list",
                    controller: controllers.ReportPrArticleController
                }
            }
        });
});