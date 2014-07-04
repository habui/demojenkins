module backend {
    'use strict';
    /*
     * -------------------------------- Article -------------------------------------------
     */
    export interface IArticle {
        save(item: models.Article, callback);
        load(id: number, callback);
        update(item: models.Article, callback);
        remove(id: number, callback);
        listPRZonesOfPublisher(callback);
        getPRCategoriesByArticleId(itemId: number, callback);
        getArticlesByOrderId(orderId: number, from: number, count: number, status: Array<string>, callback);
        loadArticlesByCampaignId(campaignId: number, from: number, count: number, callback, status: number[], sortBy?: string, sortType?: string);
        loadArticlesByZoneId(zoneId:number, from: number, count: number, status: number[], callback);
        searchArticlesInOrder(query: string, orderId: number, sortBy: string, dir: string, from: number, count: number, status: string, callback);
        searchArticlesInCampaign(query: string, campaignId: number, sortBy: string, dir: string, from: number, count: number, status: number, callback);   
        searchArticlesInZone(query: string, zoneId: number, sortBy: string, dir: string, from: number, count: number, status: number, callback);
        addComment(articleId: number, comment: string, callback);
        listComments(articleId: number, from: number, count:number, callback);
        syncToCMS(articleId, callback);
        listCategories(callback);
    }

    export class ArticleService extends HttpDataService<models.Article> implements IArticle {
        constructor(rest: IRestfulService) {
            super('article', rest);
        }

        save(item: models.Article, callback) {
            var params = item;

            var actionUrl: string = this.restUrl + "/save";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }

        update(item: models.Article, callback) {
            var params = item;

            var actionUrl: string = this.restUrl + "/update";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.result);
                }
            });
        }

        listPRZonesOfPublisher(callback) {
            var actionUrl: string = this.restUrl + "/listPRZonesOfPublisher";
            this.api.post(actionUrl, this.headers, null, function (response, status, header) {
                if (response !== undefined) {
                    callback(response.data);
                }
            });
        }

        getPRCategoriesByArticleId(itemId: number, callback) {
            var actionUrl: string = this.restUrl;
        }

        getArticlesByOrderId(orderId: number, from: number, count: number, status: Array<string>, callback) {
            var param = { orderId: orderId, from: from, count: count, status: status };
            var actionUrl: string = this.restUrl + '/loadArticlesInOrderByOwner';
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                var returnList: ReturnList<models.Article> =
                    new ReturnList<models.Article>(response.data.data, response.data.total);
                callback(returnList);
            });
        }

        loadArticlesByCampaignId(campaignId: number, from: number, count: number, callback, status: number[],sortBy?: string, sortType?: string) {
            var param = { campaignId: campaignId, from: from, count: count, status: status };

            var actionUrl: string = this.restUrl + '/loadArticlesByCampaignId';
            this.api.post(actionUrl, this.headers, param, function (response, status, header) {
                if (response.data !== undefined) {
                    var returnList: ReturnList<models.Article> = new ReturnList<models.Article>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }

        searchArticlesInOrder(query: string, orderId: number, sortBy: string, dir: string, from: number, count: number, status: string, callback){
            var params = { query: query, orderId: orderId, sortBy: sortBy, dir: dir, from: from, count: count, status: status || 0 };
            var actionUrl: string = this.restUrl + "/searchArticlesInOrder";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response.data !== undefined) {
                    var returnList: ReturnList<models.Article> = new ReturnList<models.Article>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }

        searchArticlesInCampaign(query: string, campaignId: number, sortBy: string, dir: string, from: number, count: number, status: number, callback) {
            var params = { query: query, campaignId: campaignId, sortBy: sortBy, dir: dir, from: from, count: count, status: status || 0 };
            var actionUrl: string = this.restUrl + "/searchArticlesInCampaign";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response.data !== undefined) {
                    var returnList: ReturnList<models.Article> = new ReturnList<models.Article>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        loadArticlesByZoneId(zoneId: number, from: number, count: number, status: number[], callback) {
            var params = { zoneId: zoneId, from: from, count: count, status: status };
            var actionUrl: string = this.restUrl + "/loadArticlesByZoneId";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response.data !== undefined) {
                    var returnList: ReturnList<models.Article> = new ReturnList<models.Article>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        searchArticlesInZone(query: string, zoneId: number, sortBy: string, dir: string, from: number, count: number, status: number, callback) {
            var params = { query: query, zoneId: zoneId, sortBy: sortBy, dir: dir, from: from, count: count, status: status || 0 };
            var actionUrl: string = this.restUrl + "/searchArticlesInZone";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response.data !== undefined) {
                    var returnList: ReturnList<models.Article> = new ReturnList<models.Article>(response.data.data, response.data.total);
                    callback(returnList);
                }
            });
        }
        addComment(articleId: number, comment: string, callback) {
            var params = { articleId: articleId, comment: comment};
            var actionUrl: string = this.restUrl + "/addComment";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback();
            });
        }
        listComments(articleId: number, from: number, count: number, callback) {
            var params = { articleId: articleId, from: from, count: count };
            var actionUrl: string = this.restUrl + "/listComments";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (status === 200 && response.data)
                    callback(response.data.data);
            });
        }
        syncToCMS(articleId, callback) {
            var params = { articleId: articleId,};
            var actionUrl: string = this.restUrl + "/syncToCMS";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                callback(response);
            }, function(message, status) {
                callback(message, status);
            });
        }
        listCategories(callback) {
            var ret = [];
            Object.keys(models.articleCategoryById).forEach((cId) => ret.push({ "id": cId, "name": models.articleCategoryById[cId] }));
            callback(ret);
        }

    }
}