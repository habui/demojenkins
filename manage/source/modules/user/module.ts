var userModule = angular.module('app.user', ['ui.router', 'app.root','api.backend']);


userModule.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('login', {
            url: "/login",
            views: {
                "login": {
                    templateUrl: "views/user/login",
                    controller: user.LoginController
                }
            }
        })
        .state('signup', {
            url: "/signup",
            views: {
                "login": {
                    templateUrl: "views/user/signup",
                    controller: user.SignUpController
                }
            }
        });
});