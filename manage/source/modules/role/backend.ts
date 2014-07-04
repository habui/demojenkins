module backend {
    'use strict';
    export interface IRole {
        listByObject(obj: string, callback);
    }
    export class RoleService extends HttpDataService<models.RoleInfo> implements IRole {
        listByObject(obj: string, callback) {
            var params = { obj: obj };

            var actionUrl = this.restUrl + "/listByObject";
            this.api.post(actionUrl, this.headers, params, function (response) {
                if (response.data !== null) {
                    callback(response.data);
                    return;
                }
                callback(null);
            });
        }
    }
}