module backend {
    'use strict';
    export interface ICampaignItem {
        save(item: models.AdItem, callback);
        update(item: models.AdItem, callback, failcb?);
        remove(itemId: number, callback, failcb?);
        load(bannerId: number, callback);
        search(query: string, parentId: number, isDisable: boolean, callback);
        listByCampaignId(campaignId: number, from: number, count: number, callback, sortBy?: string, sortType?: string);
        getItemsByZoneId(zoneId: number, from: number, count: number, callback);
        getItemsByOrderId(orderId: number, from: number, count: number, callback, orderBy?: string, orderType?: string);
        searchItems(query: string, parentId: number, searchBy: string, callback);
        listDisable(ref: number, from: number, count: number, callback);
        enable(id: number, callback);
        getTrackingLinks(itemId: number, callback);
    }
    /*
     * -------------------------------- Campaign -------------------------------------------
     */
    export interface ICampaign {
        listByIds(ids: Array<number>, callback);
        save(item: models.Campaign, callback);
        remove(campaignId: number, callback, failcb?);
        enable(campaignId: number, callback, failcb?)
        load(campaignId: number, callback);
        update(item: models.Campaign, callback, failcb?);
        listByOrderId(orderId: number, from: number, amount: number, callback, sortBy?: string, sortType?: string);
        getBooked(campaignId: number, callback);
        getUnlinkItem(campaignId: number, callback);
        getLinkedZonesByItemId(itemId: number, callback);
        search(query: string, parentId: number, isDisable: boolean, callback);
        pause(campaignId: number, callback);
        resume(campaignId: number, callback);
        setStatus(campaignId: number, status: string, callback);
        listDisable(ref: number, from: number, count: number, callback);
    }

    export class CampaignService extends HttpDataService<models.Campaign> implements ICampaign {
        constructor(rest: IRestfulService) {
            super('campaign', rest);
        }

        listByOrderId(orderId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            this.listByRefId(orderId, from, count, callback, sortBy, sortType);
        }

        getBooked(campaignId: number, callback) {
            var params = { campId: campaignId };

            var actionUrl = this.restUrl + "/getBooked";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }

        getUnlinkItem(campaignId: number, callback) {
            var params = { campId: campaignId };

            var actionUrl = this.restUrl + "/getUnlinkedItem";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }

        getLinkedZonesByItemId(itemId: number, callback) {
            var params = { itemId: itemId };

            var actionUrl: string = this.restUrl + "/getLinkedZones";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }

        pause(campaignId: number, callback) {
            var params = { id: campaignId };

            var actionUrl: string = this.restUrl + "/pause";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            });
        }

        resume(campaignId: number, callback) {
            var params = { id: campaignId };

            var actionUrl: string = this.restUrl + "/resume";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            });
        }

        setStatus(campaignId: number, status: string, callback) {
            var params = { id: campaignId, status: status };

            var actionUrl: string = this.restUrl + "/setStatus";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            });
        }

        //listDisable(ref: number, from: number, count: number, callback) {
        //    var params = { ref: ref, from: from, count: count };

        //    var actionUrl = this.restUrl + "/listDisable";
        //    this.api.post(actionUrl, this.headers, params, function (response, status, header) {
        //        var returnList: ReturnList<any> = new ReturnList<any>([], 0);
        //        returnList.data = response.data.data;
        //        returnList.total = response.data.total;
        //        callback(returnList);
        //    });
        //}
    }
    /*
     * -------------------------------- Banner -------------------------------------------
     */
    export class CampaignItemService extends HttpDataService<models.AdItem> implements ICampaignItem {
        constructor(rest: IRestfulService) {
            super('banner', rest);
        }

        listByCampaignId(campaignId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            this.listByRefId(campaignId, from, count, callback, sortBy, sortType);
        }

        getItemsByOrderId(orderId: number, from: number, count: number, callback, sortBy?: string, sortType?: string) {
            var param = { id: orderId, from: from, count: count };
            if (sortBy)
                param['sortBy'] = sortBy;
            if (sortType)
                param['dir'] = sortType;

            var actionUrl: string = this.restUrl + "/getItemsByOrderId";
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                var returnList: ReturnList<models.AdItem> =
                    new ReturnList<models.AdItem>(response.data.data, response.data.total);
                callback(returnList)
            });
        }
        getItemsByZoneId(zoneId: number, from: number, count: number, callback) {
            var param = { id: zoneId, from: from, count: count };

            var actionUrl: string = this.restUrl + "/getItemsByZoneId";
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                var returnList: ReturnList<models.AdItem> = new ReturnList<models.AdItem>(response.data.data, response.data.total);
                callback(returnList);
            });
        }

        //listDisable(ref: number, from: number, count: number, callback) {
        //    var params = { ref: ref, from: from, count: count };

        //    var actionUrl = this.restUrl + "/listDisable";
        //    this.api.post(actionUrl, this.headers, params, function (response, status, header) {
        //        var returnList: ReturnList<any> = new ReturnList<any>([], 0);
        //        returnList.data = response.data.data;
        //        returnList.total = response.data.total;
        //        callback(returnList);
        //    });
        //}

        enable(id: number, callback) {
            var params = { id: id };

            var actionUrl = this.restUrl + "/enable";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response.result);
            });
        }


        searchItems(query: string, parentId: number, searchBy: string, callback) {
            var params = { query: query, pId: parentId, searchBy: searchBy };

            var actionUrl: string = this.restUrl + "/searchItems";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback([]);
            });
        }

        getArticlesByZoneId(zoneId: number, status: string, from: number, count: number, callback) {
            var param = { zoneId: zoneId, status: status, from: from, count: count };

            var actionUrl: string = common.Config.API_URL + "/article/loadArticlesByZoneId";
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback([]);
            });
        }

        getTrackingLinks(itemId: number, callback) {
            var params = { id: itemId };

            var actionUrl: string = this.restUrl + "/getTrackingLinks";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response.data);
                else
                    callback({});
            });
        }
    }
    /*
     * ---------------------- Order  ------------------------------
     */
    export interface IOrder {
        load(orderId: number, callback);
        save(order: models.Order, callback);
        search(query: string, parentId: number, isDisable: boolean, callback);
        update(order: models.Order, callback, failcb?);
        remove(orderId: number, callback, failcb?);
        list(from: number, count: number, callback, sortBy?: string, sortType?: string);
        listByUserId(userId: number, from: number, count: number, callback);
        listByReferenceId(from: number, count: number, id: number, callback);
        listByFilter(from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string);
        listByIds(ids: Array<number>, callback);
        pause(orderId: number, callback, failcb?);
        resume(orderId: number, callback, failcb?);
        setStatus(orderId: number, status: string, callback, failcb?);
        listAllItem(callback);
    }

    export class OrderService extends HttpDataService<models.Order> implements IOrder {
        listByUserId(userId: number, from: number, count: number, callback) {
            var params = { id: userId, count: count, from: from };

            var actionUrl: string = this.restUrl + "/listByUserId";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.LinkedZone> = new ReturnList<models.LinkedZone>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }

        listByReferenceId(from: number, count: number, id: number, callback) {
            var params = { id: id, count: count, from: from };
            var actionUrl: string = this.restUrl + "/listByReferenceId";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Order> = new ReturnList<models.Order>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        listByFilter(from: number, count: number, filterBy: string, callback, sortBy?: string, sortType?: string) {
            var params = { count: count, from: from, filterBy: filterBy };
            if (sortBy)
                params['sortBy'] = sortBy;
            if (sortType)
                params['dir'] = sortType;
            var actionUrl: string = this.restUrl + "/listByFilter";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response !== undefined) {
                    var returnList: ReturnList<models.Order> = new ReturnList<models.Order>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        listByIds(ids: Array<number>, callback) {
            var params = { ids: ids };
            var actionUrl: string = this.restUrl + "/listByIds";
            this.api.post(actionUrl, {}, params, function (response, status, header) {
                if (response.data !== undefined) {
                    callback(response.data);
                }
            });
        }

        pause(campaignId: number, callback, failcb?) {
            var params = { id: campaignId };

            var actionUrl: string = this.restUrl + "/pause";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            }, function (msg, status) {
                    failcb(msg, status);
                });
        }

        resume(campaignId: number, callback, failcb?) {
            var params = { id: campaignId };

            var actionUrl: string = this.restUrl + "/resume";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            }, function (msg, status) {
                    failcb(msg, status);
                });
        }

        setStatus(orderId: number, status: string, callback, failcb?) {
            var params = { id: orderId, status: status };

            var actionUrl: string = this.restUrl + "/setStatus";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
                else
                    callback({ result: "fail" });
            }, function (msg, status) {
                    failcb(msg, status);
                });
        }

    }
    /*
     * ---------------------   Booking Service  -----------------------------
     */
    export interface IBook {
        getAvailableBooks(siteId: number, startDate: number, endDate: number, callback);
        book(data: Array<models.BookRecord>, callback);
        removeBook(ids: Array<number>, callback, failcb?);
        getBookedByKind(id: number, kind: String, from: number, to: number, callback);
        getUsageByZone(zoneId: number, from: number, to: number, callback);
        update(item: models.BookRecord, callback, failcb?);
    }

    export class BookService implements IBook {
        restUrl: string;
        api: IRestfulService;
        headers: any;

        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL + "/new_book";
            this.headers = {};
        }

        getAvailableBooks(siteId: number, startDate: number, endDate: number, callback) {
            var params = { siteId: siteId, from: startDate, to: endDate };

            var actionUrl: string = this.restUrl + "/getAvailableBooks";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                } else {
                    callback([]);
                }
            });
        }

        book(data: Array<models.BookRecord>, callback) {
            var params = { data: JSON.stringify(data) };

            var actionUrl: string = this.restUrl + "/book";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                } else {
                    callback([]);
                }
            });
        }

        removeBook(ids: Array<number>, callback, failcb?) {
            var params = { ids: ids };

            var actionUrl: string = this.restUrl + "/removeBook";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.result);
                } else {
                    callback("fail");
                }
            }, function (msg, status) {
                    failcb(msg, status);
                });
        }

        getBookedByKind(id: number, kind: String, from: number, to: number, callback) {
            var params = { id: id, kind: kind, from: from, to: to };

            var actionUrl: string = this.restUrl + "/getBookedByKind";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                } else {
                    callback([]);
                }
            });
        }

        getUsageByZone(zoneId: number, from: number, to: number, callback) {
            var params = { zoneId: zoneId, from: from, to: to };

            var actionUrl: string = this.restUrl + "/getUsageByZone";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }

        update(item: models.BookRecord, callback, failcb?) {
            var params = item;

            var actionUrl: string = this.restUrl + "/update";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined && response.result !== undefined) {
                    callback(response.result);
                } else {
                    callback("fail");
                }
            }, function (msg, status) {
                    failcb(msg, status);
                });
        }
    }

    export interface IConversion {
        load(conversionId: number, callback);
        save(conversion: models.Conversion, callback);
        list(from: number, count: number, callback, sortBy?: string, sortType?: string);
        update(conversion: models.Conversion, callback);
        disable(id: number, callback);
        genCode(orderId: number, callback);
        search(query: string, parentId: number, isDisable: boolean, callback);
    }

    export class ConversionService extends HttpDataService<models.Conversion> implements IConversion {
        disable(id: number, callback) {
            var params = { id: id };

            var actionUrl: string = this.restUrl + "/disable";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined && response !== null)
                    callback(response.result);
                else
                    callback("fail");
            });
        }
        genCode(orderId: number, callback) {
            var params = { orderId: orderId };

            var actionUrl: string = this.restUrl + "/genCode";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined && response !== null && response.data)
                    callback(response.data);
                else
                    callback("fail");
            });
        }
    }
}
