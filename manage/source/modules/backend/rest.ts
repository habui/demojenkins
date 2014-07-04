/// <reference path="../../libs/angular/angular.d.ts"/>

var api = angular.module('api.backend', ['ngResource']);

api.factory('httpInterceptor', function ($q, $location, $rootScope, $timeout, UrlStore: utils.DataStore, Page: common.PageUtils) {
    return {
        request: function (config) {
            var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);                       
            if (sessionid.length > 0)
                config.headers['X-sessionId'] = sessionid;
            return config || $q.when(config);
        },
        requestError: function (rejection) {
            $q.reject(rejection);
        },

        // optional method
        response: function (response) {
            return response || $q.when(response);
        },
        responseError: function (rejection) {
            if (rejection.status === 401 || rejection.status === 0) {
                var url = $location.path();
                if (url !== undefined && url && url !== "/login") {
                    UrlStore.store("cburl", { url: url, title: Page.getTitle() });
                }
                $location.path("/login");
                common.LocalStorageUtils.remove("selectedCol");
            }
            if (rejection.status === 404)
                $location.path("/404-notfound");
            if (rejection.status === 500) {
                notify(new common.ActionMessage(common.ActionMessage.WARN, rejection.data));
            }
            return $q.reject(rejection);
        }
    };
});

api.config(function ($httpProvider) {
    $httpProvider.interceptors.push('httpInterceptor');
});

api.factory('RestfulService', function ($http) {
    return new backend.RestfulService($http);
});