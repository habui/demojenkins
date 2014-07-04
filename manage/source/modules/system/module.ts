var systemModule = angular.module('app.system', ['ui.router', 'app.user', 'api.backend', 'app.root']);

systemModule.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state("main.system", {
            url: '/system',
            templateUrl: "views/system/system",
            controller: controllers.SystemController
        })
        .state("main.system.config", {
            url: '/config',
            views: {
                "breadcumnav": {
                    templateUrl: "views/common/breadcum.navbar",
                    controller: controllers.SytemBreadcumController
                },
                'content': {
                    templateUrl: "views/system/system.config",
                    controller: controllers.ConfigSystemController
                }
            }
        })
});