// Interface
var agencyModule = angular.module('app.agency', ['ui.router', 'app.user', 'api.backend', "app.root"]);

agencyModule.config(function ($stateProvider) {
    $stateProvider
        .state(
            'main.agency', {
                url: '/agency',
                templateUrl: "views/agency/agency",
                controller: controllers.AgencyController
            })
        .state(
            'main.agency.account', {
                url: '/account-management',
                views: {
                    "breadcumnav": {
                        templateUrl: "views/common/breadcum.navbar",
                        controller: controllers.BreadcumNavBar
                    },
                    'content': {
                        templateUrl: "views/agency/account.manage",
                        controller: controllers.AgencyAccountController
                    }
                }
            })
        .state(
            'main.agency.createaccount', {
                url: '/account-management/create-account',
                views: {
                    "breadcumnav": {
                        templateUrl: "views/common/breadcum.navbar",
                        controller: controllers.BreadcumNavBar
                    },
                    'content': {
                        templateUrl: "views/agency/account.create",
                        controller: controllers.AgencyAccountCreateController
                    }
                }
            })
        .state("main.agency.assign_detail", {
            url: "/account-management/assign/:userid",
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.BreadcumNavBar
                },
                "content": {
                    templateUrl: "views/agency/account.assign.tab",
                    controller: controllers.AgencyAccountAssignTabController
                }
            }
        })
        .state('main.agency.assign_detail.order', {
            url: '/order',
            templateUrl: "views/agency/account.assign.order",
            controller: controllers.AgencyAccountOrderController
        })
        .state('main.agency.assign_detail.website', {
            url: '/website',
            templateUrl: "views/agency/account.assign.website",
            controller: controllers.AgencyAccountWebsiteController
        })

        .state('main.agency.listwebsite', {
            url: '/website-management/website',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.BreadcumNavBar
                },
                'content': {
                    templateUrl: "views/agency/listwebsite",
                    controller: controllers.AgencyWebsiteController
                }
            }
        })
        .state('main.agency.assignwebsite', {
            url: '/website-management/assign/:websiteId',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.BreadcumNavBar
                },
                'content': {
                    templateUrl: "views/agency/listwebsite.assign",
                    controller: controllers.AgencyWebsiteAssignController
                }
            }
        })
        .state('main.agency.listorder', {
            url: '/order-management/order',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.BreadcumNavBar
                },
                'content': {
                    templateUrl: "views/agency/listorder",
                    controller: controllers.AgencyOrderController
                }
            }
        })
        .state('main.agency.assignorder', {
            url: '/order-management/assign/:orderId',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.BreadcumNavBar
                },
                'content': {
                    templateUrl: "views/agency/listorder.assign",
                    controller: controllers.AgencyOrderAssignController
                }
            }
        })
        ;
});
