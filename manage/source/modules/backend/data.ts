/// <reference path="../common/common.ts"/>

module backend {
    'use strict';
    
    export interface IDataService<T extends common.Item> {
        save(item: T, callback);
        load(id: number, callback);
        update(item: T, callback, failcb?);
        remove(id: number, callback, failcb?);
        list(from: number, amount: number, callback, sortBy?: string, sortType?: string);
        listByIds(ids: number[], callback);
        listDisable(refId: number, from: number, count: number, callback, sortBy?: string, sortType?: string);
        search(query: string, parentId: number, isDisable: boolean, callback);
        listSimple(from: number, amount: number, callback, sortBy?: string, sortType?: string);
        enable(campaignId: number, callback, failcb?)
        listAllItem(callback);
    }
    

    export interface IAdvancedDataService<T extends common.Item> extends IDataService<T> {
        listByRefId(refId: number, from: number, amount: number, callback, sortBy?: string, sortType?: string);
    }
    export interface IAdvancedLocalDataService<T extends common.Item> {
        saveWithRefId(refId: number, item: T): number;
        listByRefId(refId: number, from: number, amount: number): {
            data: Array<T>;
            total: number;
        };
    }

    export class ReturnList<T> {
        data: T[];
        total: number;

        constructor(items: T[], total: number) {
            this.data = items;
            this.total = total;
        }
    }

    
    
    export interface IRestfulService {
        post(url: string, headers: any, params: any, successcb: any, failcb?: any);
        get(url: string, headers: any, params: any, successcb: any, failcb?: any);
    }

    export class RestfulService implements IRestfulService{
        private http: any;

        constructor($http) {
            this.http = $http;
            this.http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded;charset=utf-8";
        }

        
        post(url: string, headers: any, params: any, successcb: any, failcb?: any) {
            var config: any = { headers: headers, timeout: common.Config.Timeout };
            this.http.post(url, common.HttpUtils.convertQueryParams(params), config)
                .success(function (data, status, headers) {
                    successcb(data, status, headers);
                })
                .error(function (data, status) {
                    if (failcb !== undefined && failcb !== null)
                        failcb(data, status);
                });
        }

        get(url: string, headers: any, params: any, successcb: any, failcb?: any) {
            var config: any = { headers: headers, timeout: common.Config.Timeout };
            this.http.get(url + "?" + common.HttpUtils.convertQueryParams(params), config)
                .success(function (data, status, headers) {
                    successcb(data, status, headers);
                })
                .error(function (data, status) {
                    if (failcb !== undefined && failcb !== null)
                        failcb(data, status);
                });
        }

    }

    /*
     * ---------------------   HttpDataService   -----------------------
     */
    export class HttpDataService<T extends common.Item> implements IAdvancedDataService<T>{
        restUrl: string;
        api: IRestfulService;
        headers: any;
        constructor(model : string, rest: IRestfulService) {
            this.restUrl = common.Config.API_URL + "/" + model;
            this.api = rest;
            this.headers = {};
        }

        save(item: T,callback) {
            var params = item;

            var actionUrl = this.restUrl + "/save";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.data);
            });
        }
        

        update(item: T, callback, failcb?){
            var params = {};
            for (var att in item) {
                if (att === "constructor") continue;
                params[att] = item[att];
            }

            var actionUrl = this.restUrl + "/update";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.result);
            }, function (msg, status) {
                failcb(msg, status);
            });
        }

        load(id: number, callback) {
            var params = { id: id };            

            var actionUrl = this.restUrl + "/load";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.data);
            });
        }
        
        remove(id: number, callback, failcb?) {
            var params = { id: id };

            var actionUrl = this.restUrl + "/disable";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.result);
            }, function (msg, status) {
                failcb(msg, status);
            });
        }

        enable(id: number, callback, failcb?) {
            var params = { id: id };

            var actionUrl = this.restUrl + "/enable";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.result);
            }, function (msg, status) {
                failcb(msg, status);
            });
        }

        list(from: number, count: number, callback, sortBy?: string, sortType?: string) {
            var ret = {data : [], total  : 0 };
            var params = {};
            params['count'] = count;
            params['from'] = from;
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/list";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<T> = new ReturnList<T>([], 0);
                returnList.data = response.data.data;
                returnList.total = response.data.total;
                callback(returnList);
            });
        }

        listByIds(ids: number[], callback) {
            if (ids == null || ids == undefined || ids.length == 0)
                return;
            var params = { ids: ids.toString() };

            var actionUrl = this.restUrl + "/listByIds";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response && response.data)
                    callback(response.data);
                else
                    callback([]);
            });
        }

        listByRefId(refId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            var params = { id: refId, count: count, from: from };
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/listByReferenceId";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<T> = new ReturnList<T>([], 0);
                if (response !== undefined && response !== null && !!response.data) {
                    returnList.data = response.data.data;
                    returnList.total = response.data.total;
                }
                callback(returnList);
            });
        }

        search(query: string, parentId: number, isDisable: boolean, callback) {
            if (query === undefined || query === null || query.length === 0)
                return;
            var params = { query: query, pId: parentId, isDisable: isDisable };

            var actionUrl = this.restUrl + "/search";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }
        listSimple(from: number, count: number, callback, sortBy?: string, sortType?: string) {
            var params = {};
            params['count'] = count;
            params['from'] = from;
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/listSimple";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<T> = new ReturnList<T>([], 0);
                returnList.data = response.data.data;
                returnList.total = response.data.total;
                callback(returnList);
            });
        }

        listAllItem(callback) {
            var curIndex = 0, count = 1000;
            var retList = [];
            var services = this;
            list(curIndex, count);
            function list(curIndex, count) {
                services.listSimple(curIndex, count, function (ret) {
                    if (!!ret && ret.data) {
                        retList = retList.concat(ret.data);
                        var size = Math.min(count, ret.data.length);
                        curIndex += size;
                        if (curIndex < ret.total) {
                            list(curIndex, count);
                        }
                        else {
                            callback(retList);
                        }
                    }
                }, 'name', 'asc');
            }
        }
        listDisable(refId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            var params = {};
            params['count'] = count;
            params['from'] = from;
            params['ref'] = refId;
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/listDisable";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }
    } 
    
    export interface IFullSearch {
        searchFull(text: string, callback);
    }
    export class FullSearch extends HttpDataService<common.Item> implements IFullSearch {
        constructor(rest: backend.IRestfulService) {
            super('extend', rest);
        }

        searchFull(text: string, callback) {
            var params = { key: text };

            var actionUrl = this.restUrl + "/search";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }
    }

    export class Factory{
        websiteService: IWebsite;
        zoneService: IZone;
        zoneGroupService: IZoneGroup;
        sessionService: ISession;
        userService: IUser;
        campaignItemService: ICampaignItem;
        campaignService: ICampaign;
        orderService: IOrder
        reportService: IReportService;
        agencyAccountService: IAgencyAccount;
        paramURLStore: utils.DataStore;
        permissionUtils: utils.PermissionUtils;
        userInfo: any;
        categoryService: CategoryService;
        assignedService: AssignedService;
        uniqueUserService: IUniqueUser;
        userLogService: IUserLog;
        articleService: IArticle;
        conversionService: IConversion;
        bookService: IBook;
        miscService: IMisc;
        roleService: IRole;
        userRoleService: IUserRole;
        fullSearchService: IFullSearch;
        systemService: ISystemService;
    }
}
