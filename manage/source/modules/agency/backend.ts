module backend {
    export interface IAgencyAccount extends IAdvancedDataService<models.AgencyAccount> {
        countAssigned(obj: String, ids: Array, callback);
    }

    export class AgencyAccountService extends HttpDataService<models.AgencyAccount> implements IAgencyAccount{
        listAllItem(callback) {
            var services = this;
            var curIndex = 0, count = 1000;
            var retList = [];
            var services = this;
            list(curIndex, count);
            function list(curIndex, count) {
                services.list(curIndex, count, function (ret) {
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
        countAssigned(obj: String, ids: Array, callback) {
            var params = {obj: obj, ids: ids};
            var actionUrl = this.restUrl + "/countAssigned";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response)
                    callback(response.data);
            });
        }
    }

}