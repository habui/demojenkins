module backend {
    'use strict';
    /*
     * --------------------------------  Session -------------------------------------------
     */
    export interface ISession {
        login(username: string, password: string, callback);
        getCurrentUser(sessionid: string, callback);
        logout(sessionId: string, callback);
    }

    export class SessionService implements ISession {
        restUrl: string;
        api: any;
        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL + "/session";
        }

        login(username: string, password: string, callback) {
            var params = { userName: username, password: password };
            var actionUrl: string = this.restUrl + "/login";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                    if (response !== undefined) 
                        callback({ code: 1, msg: header('X-sessionId') });
                }, function (response, status) {
                    callback({ code: 0, msg: response, status: status });
                });
        }

        changePassword(username: string, password: string, new_password: string, callback) {
            var params = { username: username, password: password, newpass: new_password };
            var actionUrl: string = this.restUrl + "/changePass";

            this.api.post(actionUrl, {}, params, function (response, status, header) {

            });
        }

        getCurrentUser(sessionid: string, callback) {
            var params = { 'X-sessionId': sessionid };
            //var params = { '_sessionId': sessionid };
            var actionUrl: string = this.restUrl + "/getCurrentUser";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined)
                    callback({ code: 1, data: new models.UserInfo(response.data.id, response.data.name, response.data.email, response.data.ownerId) });
            }, function (response, status) {
                callback({ code: 0, msg: "\"" + response + "\"" });
            });
        }

        logout(sessionid: string, callback) {
            var params = { 'X-sessionId': sessionid };
            //var params = { '_sessionId': sessionid };
            var actionUrl: string = this.restUrl + "/logout";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined)
                    callback({ code: 1, msg: "\"" + response.data + "\"" });
            }, function (response, status) {
                    callback({ code: 0, msg: "\"" + response + "\"" });
            });
        }
    }
    /*
     * ---------------------   UserService   -----------------------
     */
    export interface IUser extends IAdvancedDataService<models.UserInfo> {
        signup(uname: string, email: string, password: string, callback);
        save(user: models.UserInfo, callback);
        load(id: number, callback);
        update(user: models.UserInfo, callback);
        list(from: number, count: number, callback, sortBy?: string, sortType?: string);
        listByIds(ids: number[], callback);
        search(query: string, parentId: number, isDisable: boolean, callback);
    }
    export class UserService extends HttpDataService<models.UserInfo> implements IUser {
        signup(uname: string, email: string, password: string, callback) {
            var params = { name: uname, email: email, password: password };
            var actionUrl: string = this.restUrl + "/save";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var user_info: models.UserInfo = new models.UserInfo(response.data.id, response.data.name);
                    callback(user_info);
                    return;
                }
            });
        }
    }

    /*
    * ---------------------Permission-------------------------
    */
    export interface IPermission {
        getCurrentUserPermission(obj: string, id: number, callback);
        getCurrentUserPermissions(obj: string, ids: number[], callback);
    }

    export class PermissionService implements IPermission {
        restUrl: string;
        api: any;
        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL;
        }

        getCurrentUserPermission(obj: string, id: number, callback) {
            var params = {obj: obj, id: id};
            var actionUrl: string = this.restUrl + "/user_role/getCurrentPermission";

            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response && response.permission) {
                    callback(response.permission);
                    return;
                }
                callback({data: "error"});
            });
        }

        getCurrentUserPermissions(obj: string, ids: number[], callback) {
            var params = { obj: obj, ids: ids };
            var actionUrl: string = this.restUrl + "/user_role/getCurrentPermissions";

            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response && response.data) {
                    callback(response.data);
                    return;
                }
                callback({ data: "error" });
            });
        }
    }
}
