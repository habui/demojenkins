/// <reference path="libs/angular/angular.d.ts"/>
/// <reference path="modules/backend/data.ts"/>


var app = angular.module('123click', ['ngRoute', 'ui.router', '$strap.directives', 'ngResource', "ngMockE2E", 'ngSanitize',
    'app.root', 'app.directives', 'app.user', 'app.website', 'app.campaign', 'app.admin',
    'app.agency', 'ui.bootstrap', 'app.report', 'app.article', "app.system"]);

app.controller('RootController', controllers.RootController);

app.factory("ParamURLStore", function () { return new utils.DataStore() });

app.factory("UrlStore", function () { return new utils.DataStore() });

app.factory('ActionMenuStore', function () {
    return new utils.DataStore();
});

app.factory('Action', function () {
    return new utils.DataStore();
});
app.factory('StateStore', function () {
    return new utils.DataStore();
});
app.factory("BreadCumStore", function () {
    return new common.BreadCum();
});

app.factory("permissionUtils", function (RestfulService) {
    return new utils.PermissionUtils(RestfulService);
});

app.value('$strap.config', {
    datepicker: {
        language: 'fr',
        format: 'M d, yyyy',
        startDate: '15/10/2013'
    }
});

app.config(function($sceProvider) {
    $sceProvider.enabled(false);
})

app.run(function ($state, $rootScope, $location, factory: backend.Factory, $httpBackend, $http) {
    
    $rootScope.$on("$stateChangeStart", function (event, to) {
        if (to.name === 'main.report.list') {
            event.preventDefault();
            var userinfo: models.UserInfo = factory.userInfo.getUserInfo();
            if (userinfo !== undefined && userinfo !== null) {
                if (userinfo.isAdvertiser())
                    $state.transitionTo('main.report.orders')
                else if (userinfo.isPublisher())
                    $state.transitionTo('main.report.websites');
            } else {
                $state.transitionTo('login')
            }
        }
    });

    $httpBackend.whenGET().passThrough();
    $httpBackend.whenJSONP().passThrough();
    $httpBackend.whenPOST().passThrough();
});
