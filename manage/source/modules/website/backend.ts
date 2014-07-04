module backend {
    'use strict';
    /*
     * ------------------------------ Website --------------------------------
     */
    export interface IWebsite extends IAdvancedDataService<models.Website> {
        load(websiteId: number, callback);
        save(website: models.Website, callback);
        remove(websiteId: number, callback, failcb?);
        enable(websiteId: number, callback, failcb?);
        update(website: models.Website, callback, failcb?);
        search(query: string, parentId: number, isDisable: boolean, callback);
        list(from: number, count: number, callback, sortBy?: string, sortType?: string);
        listByReferenceId(from: number, count: number, id: number, callback);
        approve(action: number, data: any, callback, failcb?);
        listAdsByStatus(status: number, websiteId: number, from: number, count: number, callback);
        listByType(from: number, count: number, types: Array<string>, callback);
        listByFilter(from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string);
        listByTypeMinimize(from: number, count: number, types: Array<string>, callback, sortBy?: string, sortType?: string);
        listAllItem(callback);
    }
    export class WebsiteService extends HttpDataService<models.Website> implements IWebsite {

        listByReferenceId(from: number, count: number, id: number, callback) {
            var params = { id: id, count: count, from: from };
            var actionUrl: string = this.restUrl + "/listByReferenceId";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Website> = new ReturnList<models.Website>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        approve(action: number, data: any, callback, failcb?) {
            var param = { action: action, data: data };
            
            var actionUrl: string = this.restUrl + "/approve";

            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                callback(response);
            }, function (msg, status) {
                failcb(msg, status);
            });
        }       
        listAdsByStatus(status: number, websiteId: number, from: number, count: number, callback) {
            var param = { status: status, site: websiteId, from: from, count: count };

            var actionUrl: string = this.restUrl + "/listAdsByStatus";
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                var returnList: ReturnList<models.ApprovedAds> =
                    new ReturnList<models.ApprovedAds>(response.data.data, response.data.total);
                callback(returnList);
            });
        }
        listByType(from: number, count: number, types: Array<string>, callback) {
            var params = { from: from, count: count, types: types.toString() };
            var actionUrl: string = this.restUrl + "/listByType";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Website> = new ReturnList<models.Website>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        listByFilter(from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string) {
            var params = { from: from, count: count, filterBy: filterBy };
            if (sortBy && sortBy != "") params['sortBy'] = sortBy;
            if (sortType && sortType != "") params['dir'] = sortType;
            var actionUrl: string = this.restUrl + "/listByFilter";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response) {
                    var returnList: ReturnList<models.Website> = new ReturnList<models.Website>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        listByTypeMinimize(from: number, count: number, types: Array<string>, callback, sortBy?: string, sortType?: string) {
            var params = { from: from, count: count, types: types.toString() };
            if (sortBy && sortBy != "") params['sortBy'] = sortBy;
            if (sortType && sortType != "") params['dir'] = sortType;
            var actionUrl: string = this.restUrl + "/listByTypeMinimize";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Website> = new ReturnList<models.Website>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
    }
    /*
     * ---------------------   ZoneGroupService   -----------------------
     */
    export interface IZoneGroup {
        load(zoneGroupId: number, callback);
        save(zoneGroup: models.ZoneGroup, callback);
        remove(zoneGroupId: number, callback, failcb?);
        update(zoneGroup: models.ZoneGroup, callback, failcb?);
        search(query: string, parentId: number, isDisable: boolean, callback);
        listByWebsiteId(websiteId: number, from: number, count: number, callback, sortBy?: string, sortType?: string);
        getZones(zoneGroupId: number, callback);
    }
    export class ZoneGroupService extends HttpDataService<models.ZoneGroup> implements IZoneGroup{

        getZones(zonegrId: number, callback) {
            var params = { id: zonegrId };
            params['id'] = zonegrId;
            var actionUrl: string = this.restUrl + "/getZones";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }
        save(item: models.ZoneGroup, callback) {
            var params = {};
            for (var att in item) {
                if (att === 'zones' && item[att].length <= 0) {

                } else {
                    params[att] = item[att];
                }
            }
            var actionUrl: string = this.restUrl + "/save";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }

        listByWebsiteId(websiteId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            this.listByRefId(websiteId, from, count, callback, sortBy, sortType);
        }
    }
    /*
     * ---------------------   ZoneService   -----------------------
     */
    export interface IZone {
        load(zoneId: number, callback);
        save(zone: models.Zone, callback);
        update(zone: models.Zone, callback, failcb?);
        remove(zoneId: number, calback, failcb?);
        enable(zoneId: number, calback);
        search(query: string, parentId: number, isDisable: boolean, callback);
        listByIds(ids: Array<number>, callback);
        listByWebsiteId(websiteId: number, from: number, count: number, callback);
        listByWebsiteIdFilter(websiteId: number, from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string);
        listByZoneGroupIdFilter(zoneGroupId: number, from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string);
        listByRefIdAndRunningMode(websiteId: number, from: number, count: number, runningMode: string, callback);
        countPrByStatus(pId: number, pType: string, from: number, count: number, filterBy:string, status: number, callback);
    }

    export class ZoneService extends HttpDataService<models.Zone> implements IZone {

        listByWebsiteId(websiteId: number, from: number, count: number, callback) {
            this.listByRefId(websiteId, from, count, callback);
        }
        listByRefIdAndRunningMode(websiteId: number, from: number, count: number, runningMode: string, callback) {
            var params = { id: websiteId, count: count, from: from, runningMode: runningMode };
            var actionUrl: string = this.restUrl + "/listByRefIdAndRunningMode";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Zone> = new ReturnList<models.Zone>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        listByWebsiteIdFilter(websiteId: number, from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string) {
            var params = { pId: websiteId, count: count, from: from, filterBy: filterBy, pType: 'website' };
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/listByFilter";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<models.Zone> = new ReturnList<models.Zone>([], 0);
                if (response !== undefined && response !== null) {
                    returnList.data = response.data.data;
                    returnList.total = response.data.total;
                }
                callback(returnList);
            });
        }

        listByZoneGroupIdFilter(zonegroupId: number, from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string) {
            var params = { pId: zonegroupId, count: count, from: from, filterBy: filterBy, pType: 'zonegroup' };
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;

            var actionUrl = this.restUrl + "/listByFilter";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                var returnList: ReturnList<models.Zone> = new ReturnList<models.Zone>([], 0);
                if (response !== undefined && response !== null) {
                    returnList.data = response.data.data;
                    returnList.total = response.data.total;
                }
                callback(returnList);
            });
        }

        countPrByStatus(pId: number, pType: string, from: number, count: number, filterBy: string, status: number, callback) {
            var params = { pId: pId, count: count, from: from, pType: pType, filterBy: filterBy, status: status };

            var actionUrl = this.restUrl + "/countPrByStatus";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response.data !== undefined && response.data !== null) {
                    callback(response.data);
                }
            });
        }
    }
    /*
     * ---------------------   MiscService   -----------------------
     */
    export interface IMisc {
        getCategories(callback);
        getCountries(callback);
    }

    export class MiscService implements IMisc {
        restUrl: string;
        api: any;
        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL + "/misc";
        }

        getCategories(callback) {
            var params = {};
            var actionUrl: string = this.restUrl + "/getCategories";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }
        getCountries(callback) {
            var params = {};
            var actionUrl: string = this.restUrl + "/getCountries";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }
    }
}
