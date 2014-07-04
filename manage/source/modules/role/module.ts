var roleModule = angular.module('app.role', ['app.root', 'api.backend']);

userModule.factory('roleService', function (RestfulService) {
    return new backend.RoleService('role', RestfulService);
});