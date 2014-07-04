var adminModule = angular.module('app.admin', ['ui.router', 'app.user', 'api.backend', 'app.root']);

adminModule.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('main.admin', {
            url : '/admin',
            templateUrl: "views/admin/admin",
            controller: controllers.AdminController,
        })
        .state('main.admin.account_list', {
            url: '/accounts?page',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/account.list",
                    controller: controllers.AccountListController
                }
            }
        })
        .state('main.admin.account_create', {
            url: '/create-account',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/account.create",
                    controller: controllers.AccountCreateController
                }
            }
        })
        .state('main.admin.category_create', {
            url: '/create-category',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/category.create",
                    controller: controllers.CategoryCreateController
                }
            }
        })
        .state('main.admin.category_list', {
            url: '/categories?page',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/category.list",
                    controller: controllers.CategoryListController
                }
            }
        })
        .state('main.admin.userlog_list', {
            url: '/logs?page',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/userlog.list",
                    controller: controllers.UserlogListController
                }
            }
        })
        .state('main.admin.userlog_detail', {
            url: '/logs/:objId/:objType',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/userlog.detail",
                    controller: controllers.UserlogDetailController
                }
            }
        })
        .state('main.admin.assigned', {
            url: '/:uid',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/account.assigned",
                    controller: controllers.AccountAssignedViewController
                }
            }
        })
        .state('main.admin.assigned.websites', {
            url: '/assigned-websites?page',
            templateUrl: 'views/admin/website.assigned.list',
            controller: controllers.WebsiteAssignedController,
            onExit: function ($stateParams, $state, $modal) {
                if ($state.scope && $state.scope.hasSave !== undefined && $state.scope.hasSave) {
                    var webModal = $modal.open({
                        templateUrl: 'views/common/modal.confirm',
                        controller: common.ModalConfirmController,
                        resolve: {
                            data: function () {
                                return $state.scope;
                            },
                            title: function () {
                                return 'Save Confirm';
                            },
                            bodyMessage: function () {
                                return 'Do you want to save these changes?';
                            }
                        }
                    });
                    webModal.result.then(
                        function (scope) {
                            scope.save();
                        },
                        function (message) {
                        });
                }
            }
        })
        .state('main.admin.assigned.orders', {
            url: '/assigned-orders?page',
            templateUrl: 'views/admin/order.assigned.list',
            controller: controllers.OrderAssignedController,
            onExit: function ($stateParams, $state, $modal) {
                if ($state.scope && $state.scope.hasSave !== undefined && $state.scope.hasSave) {
                    var webModal = $modal.open({
                        templateUrl: 'views/common/modal.confirm',
                        controller: common.ModalConfirmController,
                        resolve: {
                            data: function () {
                                return $state.scope;
                            },
                            title: function () {
                                return 'Save Confirm';
                            },
                            bodyMessage: function () {
                                return 'Do you want to save these changes?';
                            }
                        }
                    });
                    webModal.result.then(
                        function (scope) {
                            scope.save();
                        },
                        function (message) {
                        });
                }
            }
        })

        .state('main.admin.unique_user', {
            url: '/tool/unique_user',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.AdminBreadCrumbController
                },
                'content': {
                    templateUrl: "views/admin/unique_user",
                    controller: controllers.UniqueUserController
                }
            }
        })
        .state('main.admin.unique_user.run', {
            url: '/run',
            templateUrl: "views/admin/unique_user.run",
            controller: controllers.UniqueUserRunController
        })
        .state('main.admin.unique_user.stop', {
            url: '/stop',
            templateUrl: "views/admin/unique_user.stop",
            controller: controllers.UniqueUserStopController
        })
    });