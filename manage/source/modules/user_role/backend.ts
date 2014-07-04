module backend {
    'use strict';

    export interface IUserRole {
        getRole(from: number, count: number, id: number, obj: string, callback);
        setRole(obj: string, objId: number, data: string, callback);
        getRoleByAgency(id: number, obj: string, callback);
        getRoleByObject(obj: string, id: number, callback);
        getRolesByObjectIds(obj: string, ids: Array<number>, callback);
    }
    export class UserRoleService extends backend.HttpDataService<models.UserRoleInfo> implements IUserRole {
        getRole(from: number, count: number, id: number, obj: string, callback) {
            var params = { obj: obj, id: id, from: from, count: count };
            var actionUrl: string = this.restUrl + "/get";
            this.api.post(actionUrl, {}, params, function (response) {
                if (response.data !== undefined && response.data !== null) {
                    var userRoleInfo = [];
                    for (var user in response.data.data) {
                        var roles = [];
                        for (var role in response.data.data[user].roles) {
                            roles.push(new models.RoleInfo(response.data.data[user].roles[role].id, response.data.data[user].roles[role].name, null, null, null));
                        }
                        userRoleInfo.push(new models.UserRoleInfo(response.data.data[user].user.id, response.data.data[user].user.name, response.data.data[user].user.email, roles));
                    }
                    var result = {};
                    result['data'] = userRoleInfo;
                    result['total'] = response.data.total;
                    callback(result);
                    return;
                }
                callback(null);
            });
        }
        setRole(obj: string, objId: number, data: string, callback) {
            var params = { obj: obj, id: objId, data: data };
            var actionUrl: string = this.restUrl + "/set";
            this.api.post(actionUrl, {}, params, function (response) {
                callback(response.data);
            });
        }
        getRoleByAgency(id: number, obj: string, callback) {
            var params = { obj: obj, id: id};
            var actionUrl: string = this.restUrl + "/getByAgency";
            this.api.post(actionUrl, {}, params, function (response) {
                if (response.data !== undefined && response.data !== null) {
                    var userRoleInfo = [];
                    for (var i = 0; i < response.data.length; i++) {
                        var roles = [];
                        for (var role in response.data[i].roles) {
                            roles.push(new models.RoleInfo(response.data[i].roles[role].id, response.data[i].roles[role].name, null, null, null));
                        }
                        userRoleInfo.push(new models.UserRoleInfo(response.data[i].user.id, response.data[i].user.name, response.data[i].user.email, roles));
                    }
                    var result = {};
                    result['data'] = userRoleInfo;
                    result['total'] = response.data.length;
                    callback(result);
                    return;
                }
                callback(null);
            });
        }
        getRoleByObject(obj: string, id: number, callback) {
            var params = { obj: obj, id: id};
            var actionUrl: string = this.restUrl + "/getByObject";
            this.api.post(actionUrl, {}, params, response => {
                if (response)
                    callback(response.data);
            });
        }
        getRolesByObjectIds(obj: string, ids: Array<number>, callback) {
            var params = { obj: obj, ids: ids };
            var actionUrl: string = this.restUrl + "/getByObjectIds";
            this.api.post(actionUrl, {}, params, response => {
                if (response)
                    callback(response.data);
            });
        }
    }
}