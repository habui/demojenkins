var campaignModule = angular.module('app.campaign', ['ui.router', 'app.user', 'api.backend', 'app.root']);

campaignModule.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('main.order', {
            templateUrl: "views/order/order",
            controller: controllers.OrderController
        })
        .state('main.order.list', {
// list orders
            url: '/orders',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/orders.list",
                    controller: controllers.OrderListController
                }
            }
        })
        .state('main.order.create', {
            url: '/orders/create',
            views: {
                'content': {
                    templateUrl: "views/order/order.create",
                    controller: controllers.OrderCreateController
                }
            }
        })
        .state('main.order.detail', {
            url: "/orders/:orderId",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/order.detail",
                    controller: controllers.OrderDetailController
                }
            }
        })
        .state('main.order.detail.item', {
// list items of order
            url: '/items',
            templateUrl: "views/order/order.detail.item",
            controller: controllers.OrderDetailItemController
        })
        .state('main.order.detail.campaign', {
// list campaign of order
            url: '/campaigns',
            templateUrl: "views/order/campaign.list",
            controller: controllers.CampaignListController
        })
        .state('main.order.detail.setting', {
// order setting
            url: '/setting',
            templateUrl: "views/order/order.setting",
            controller: controllers.OrderSettingController
        })
        .state('main.order.detail.assigned', {
// order assigned
            url: '/assigned',
            templateUrl: "views/order/order.detail.assigned",
            controller: controllers.OrderAssignedUserController,
            onExit: function($stateParams, $state, $modal) {
                if ($state.scope.hasSave) {
                    var webModal = $modal.open({
                        templateUrl: 'views/common/modal.confirm',
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
        .state('main.order.campaign_types', {
// list campaign type
            url: '/orders/:orderId/campaigns/type',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.type.list",
                    controller: controllers.CampaignTypeListController
                }
            }
        })
        .state('main.order.campaign_create', {
// create campaign (network, tvc)
            url: '/orders/:orderId/campaigns/create?type',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.create",
                    controller: controllers.CampaignCreateController
                }
            }
        })
        .state('main.order.booking_campaign', {
// booking campaign
            url: '/orders/:orderId/booking-campaign',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.booking",
                    controller: controllers.BookingController
                }
            }
        })
        .state('main.order.campaign_detail', {
            url: "/orders/:orderId/:campaignId",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/campaign.detail",
                    controller: controllers.CampaignDetailController
                }
            }
        })
        .state('main.order.campaign_detail.setting', {
// campaign setting
            url: '/setting',
            templateUrl: "views/order/campaign.setting",
            controller: controllers.CampaignSettingController
        })
        .state('main.order.campaign_detail.items', {
// list item of campaign
            url: '/items',
            templateUrl: "views/order/item.list",
            controller: function($scope, factory: backend.Factory, $stateParams) {
                factory.campaignService.load($stateParams.campaignId, function(camp: models.Campaign) {
                    if (camp) {
                        $scope.isPRCampaign = camp.campaignType === models.CampaignType.NETWORK_PR;
                    }
                });
            }
        })
        .state('main.order.campaign_detail.linkedbooking', {
// list zones of campaign
            url: '/linked-booking',
            templateUrl: "views/order/campaign.linkedzone.list",
            controller: controllers.CampaignLinkedZoneListController
        })
        .state('main.order.campaign_detail.media_plan', {
// list zones of campaign
            url: '/media-plan',
            templateUrl: "views/order/campaign.media_plan",
            controller: controllers.CampaignMediaPlanController
        })
//        .state('main.order.unlink_booking_zone', {
//// TODO:
//            url: "/orders/:orderId/:campaignId/unlink-booking-zone",
//            views: {
//                "breadcumnav": {
//                    templateUrl: "views/common/breadcum.navbar",
//                    controller: controllers.OrderBreadcumNavController
//                },
//                "content": {
//                    templateUrl: "views/order/campaign_zone.notlink.list",
//                    controller: controllers.CampaignUnlinkBookingZoneListController
//                }
//            }
//        })
        .state('main.order.unlink_booking_item', {
            url: "/orders/:orderId/:campaignId/unlink-booking-item",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/campaign_item.notlink.list",
                    controller: controllers.CampaignUnlinkBookingItemListController
                }
            }
        })
        //when create new item in campaign
        .state('main.order.bookingitemtypeselect', {
// select type when create campaignItem
            url: '/orders/:orderId/:campaignId/select-booking-item-type',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.booking.item.type",
                    controller: controllers.CampaignBookingItemTypeSelectController
                }
            }
        })
        .state('main.order.networkitemtypeselect', {
// select type when create campaignItem
            url: '/orders/:orderId/:campaignId/select-network-item-type',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.network.item.type",
                    controller: controllers.CampaignNetworkItemTypeSelectController
                }
            }
        })
        .state('main.order.newitem', {
            url: '/orders/:orderId/:campaignId/new-item?kind',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.item.create",
                    controller: controllers.CampaignItemCreateController
                }
            }
        })

    ////////////////////PR Article
    // Create New PR Article
        .state('main.order.newprarticle', {
            url: '/orders/:orderId/:campaignId/new-pr-article',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/article/create.pr.article",
                    controller: controllers.CreatePrArticleController
                }
            }
        })
    // PR Article setting
        .state('main.order.article_setting', {
            url: '/orders/:orderId/:campaignId/:articleId/setting-pr',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/article/create.pr.article",
                    controller: controllers.CreatePrArticleController
                }
            }
        })
        .state('main.order.item_detail', {
            url: "/orders/:orderId/:campaignId/:itemId",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/item.detail",
                    controller: controllers.ItemDetailController
                }
            }
        })
        .state('main.order.item_detail.linkedbooking', {
// linked zone list
            url: '/linked-booking',
            templateUrl: "views/order/item.linkedbooking",
            controller: controllers.ItemLinkedBookingController
        })
        .state('main.order.item_detail.setting', {
// linked zone list
            url: '/setting',
            templateUrl: "views/order/campaign.item.setting",
            controller: controllers.CampaignItemSettingController
        })
        .state("main.order.item-booking", {
            url: "/orders/:orderId/:campaignId/:itemId/booking-item",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                'content': {
                    templateUrl: "views/order/campaign.booking",
                    controller: controllers.BookingController
                }
            }
        })
        // -----------------Conversion tracking------------------
        .state("main.order.conversion", {
            url: '/conversion',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/conversion.list",
                    controller: controllers.ConversionListController
                }
            }
        })
        .state("main.order.edit_conversion", {
            url: '/conversion/edit/:conversionId',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/order/conversion.edit",
                    controller: controllers.ConversionEditController
                }
            }
        })
        .state("main.order.search", {
            url: '/search/:keywork',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.OrderBreadcumNavController
                },
                "content": {
                    templateUrl: "views/common/common.search-display",
                    controller: controllers.FullSearchController
                }
            }
        });

});



