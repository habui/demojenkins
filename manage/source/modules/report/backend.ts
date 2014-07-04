module backend {
    'use strict';
    /*
     * --------------------------------  Report  -------------------------------------------
     */
    export class Params {
        key: string;
        value: string;
        constructor(key: string, value: string) {
            this.key = key;
            this.value = value;
        }
    }
    export interface IReportService {
        loadReport(range: models.TimeRange, compareRange: models.TimeRange, offset: number, count: number, options: Array<any>, callback);
        getPDFURL(range: models.TimeRange, compareRange: models.TimeRange, offset: number, count: number, options: Array<any>): string;
        loadReportZingTV(range: models.TimeRange, callback);

    }

    export class ReportService implements IReportService {
        restUrl: string;
        api: IRestfulService;
        headers: any;
        constructor(rest: IRestfulService) {
            this.api = rest;
            this.restUrl = common.Config.API_URL + "/report";
            this.headers = {};
        }

        loadReport(range: models.TimeRange, compareRange: models.TimeRange, offset: number, count: number, options: Array<any>, callback) {
            var requestData: any = "";
            if (compareRange != null)
                requestData = {
                    range: range,
                    compareRange: compareRange,
                    offset: offset,
                    count: count,
                    filters: options
                };
            else
                requestData = {
                    range: range,
                    offset: offset,
                    count: count,
                    filters: options
                };
            var params: {} = { data: JSON.stringify(requestData) };

            var actionUrl: string = this.restUrl + "/getReport";
            this.api.post(actionUrl, this.headers, params, function (response, status, header) {
                if (response !== undefined)
                    callback(response);
            }, function (response, status) {
                    
                });
        }

        //loadChart(range: models.TimeRange, compareRange: models.TimeRange, options: Array<any>, callback) {
        //}

        getPDFURL(range: models.TimeRange, compareRange: models.TimeRange, offset: number, count: number, options: Array<any>): string {
            var requestData: any = "";
            if (compareRange != null)
                requestData = {
                    range: range,
                    compareRange: compareRange,
                    offset: offset,
                    count: count,
                    filters: options
                };
            else
                requestData = {
                    range: range,
                    offset: offset,
                    count: count,
                    filters: options
                };
            var params: {} = { data: JSON.stringify(requestData) };

            var actionUrl: string = common.Config.API_URL + "/exportreport";
            return actionUrl + "?" + common.HttpUtils.convertQueryParams(params);
        }
        loadReportZingTV(range: models.TimeRange, callback) {
            var params = {range: JSON.stringify(range)};
            var actionUrl = this.restUrl + "/getZingTVReport";
            var xhr = new XMLHttpRequest(); // createXHR();
            xhr.open("GET", actionUrl + "?" + common.HttpUtils.convertQueryParams(params));
            var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
            xhr.setRequestHeader("X-sessionId", sessionid);
             
            //recent browser
            if ("responseType" in xhr) {
                xhr.responseType = "arraybuffer";
            }

            // older browser
            if (xhr.overrideMimeType) {
                xhr.overrideMimeType("text/plain; charset=x-user-defined");
            }

            xhr.onloadend = function () {
                var content = xhr.response;
                if (content.byteLength === 0) {
                    callback(null);
                    return;
                }
                var zip = new JSZip(content);
                var data = JSON.parse(zip.file(/ReportZingTV/)[0].asText());
                callback(data.data);
            }

            xhr.send();
        }

    }
}