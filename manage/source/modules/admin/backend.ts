module backend {
    'use strict';

    export class CategoryService extends HttpDataService<models.Category> {
        listAll(callback) {
            var actionUrl: string = this.restUrl + "/listAll";
            this.api.post(actionUrl, {}, null, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }
    }

    export class AssignedService extends HttpDataService<models.UserAssigned> {
        getRoleByUser(from: number, count: number, id: number, obj: string, callback) {
            var params: common.ActionModel = new common.ActionModel();
            params.from = from;
            params.count = count;
            params.userId = id;
            params.obj = obj;
            var actionUrl: string = this.restUrl + "/getByUser";
            this.api.post(actionUrl, {}, params, function (response) {
                if (response.data !== undefined && response.data !== null) {
                    var listRoleInfo: models.UserAssigned[] = [];
                    var data = response.data.data;
                    for (var item in data) {
                        var roles: models.RoleInfo[] = [];
                        if (data[item].item !== undefined && data[item].item !== null) {
                            for (var role in data[item].roles) {
                                roles.push(new models.RoleInfo(data[item].roles[role].id, data[item].roles[role].name, null, null, null));
                            }
                            listRoleInfo.push(new models.UserAssigned(data[item].item.id, data[item].item.ownerId, data[item].item.name, roles));
                        }
                    }
                    var result = {};
                    result['data'] = listRoleInfo;
                    result['total'] = response.data.total;
                    callback(result);
                    return;
                }
                callback(null);
            });
        }

        setRoleByUser(obj: string, userId: number, data: string, callback) {
            var params = { obj: obj, userId: userId, data: data };
            var actionUrl: string = this.restUrl + "/setByUser";
            this.api.post(actionUrl, {}, params, function (response) {
                callback(response.data);
            });
        }

    }

    export interface IUserLog {
        list(from: number, count: number, callback);
        advancedSearch(from: number, count: number, key: string, userId: number, objectId: number, objectType: string, action: string, fromTime: number, toTime: number, callback);
        detailLog(logId, callback);
    } 

    export class UserLogService extends HttpDataService<models.UserLog> implements IUserLog {

        advancedSearch(from: number, count: number, key: string, userId: number, objectId: number, objectType: string, action: string, fromTime: number, toTime: number, callback) {
            var params = {};
            params['count'] = count;
            params['from'] = from;
            params['key'] = key;
            params['userId'] = userId;
            params['objectId'] = objectId;
            params['objectType'] = objectType;
            params['action'] = action.toLowerCase();
            params['fromTime'] = fromTime;
            params['toTime'] = toTime;
            var actionUrl = this.restUrl + "/listBy";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<models.UserLog> = new ReturnList<models.UserLog>([], 0);
                returnList.data = response.data.data;
                returnList.total = response.data.total;
                callback(returnList);
            });
        }

        detailLog(logId, callback) {
            var params = { id: logId };
            var actionUrl = this.restUrl + "/detailMessage";
            this.api.post(actionUrl, this.headers, params, function(response, status, header) {
                if (!!response && response.data) {
                    callback(response.data);
                }
            });
        }
    }

    export interface IUniqueUser {
        run(apiKey: string, from: number, duration: number, times: string, filterSite: Array<number>, filterCampaign: Array, email: string, callback);
        stop(apiKey: string, callback);
        getStatus(callback);
    }

    export class UniqueUserService implements IUniqueUser {
        restUrl: string;
        api: IRestfulService;
        headers: any;

        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL + "/uniqueuser";
            this.headers = {};
        }

        run(apiKey: string, from: number, duration: number, times: string, filterSite: Array<number>, filterCampaign: Array, email: string, callback) {
            var params = {};
            params['apikey'] = apiKey;
            params['from'] = from;
            params['duration'] = duration;
            params['times'] = times;
            params['filtersite'] = filterSite;
            params['filtercamp'] = filterCampaign;
            params['email'] = email;
            
            var actionUrl = this.restUrl + "/run";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response);
            });
        }
        stop(apiKey: string, callback) {
            var params = { apikey: apiKey }
            var actionUrl = this.restUrl + "/stop";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response);
            });
        }
        getStatus(callback) {
            var actionUrl = this.restUrl + "/getStatus";
            this.api.post(actionUrl, this.headers, {}, function (response, status, header) {
                if (response && response.data)
                callback(response.data);
            });
        }
    }
}
