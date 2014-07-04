interface String {
    startWith(prefix: string): boolean;
}

String.prototype.startWith = function (prefix) {
    return this.slice(0, prefix.length) === prefix;
};

module common {
    'use strict';
    export class Utils {
        static isNumber(value: string): boolean {
            return /^\+?(0|[1-9]\d*)$/.test(value);
        } 

        static removeArray(arr, args: any[]) {
            var i: number = args.length;
            var index: number;
            while (i > 0 && arr.length) {
                var what: any = args[--i];
                while ((index = arr.indexOf(what)) !== -1) {
                    arr.splice(index, 1);
                }
            }
            return arr;
        }

        static generateID(numChar: number): string {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            for (var i = 0; i < numChar; i++)
                text += possible.charAt(Math.floor(Math.random() * possible.length));
            return text;
        }

        static setCookie(cookieName: string, cookieVal: string, ndays: number) {
            var today: Date = new Date();
            var expire: Date = new Date();
            if (ndays == null || ndays == 0) ndays = 1;
            expire.setTime(today.getTime() + 3600000 * 24 * ndays);
            document.cookie = cookieName + "=" + encodeURI(cookieVal) + ";expires=" + expire.toUTCString();
        }

        static setCookieHour(cookieName: string, cookieVal: string, expiredHour: number) {
            var today: Date = new Date();
            var expire: Date = new Date();
            expire.setTime(today.getTime() + 3600000 * expiredHour);
            document.cookie = cookieName + "=" + encodeURI(cookieVal) + ";expires=" + expire.toUTCString();
        }

        static getCookie(cookieName: string) {
            var theCookie = " " + document.cookie;
            var ind = theCookie.indexOf(" " + cookieName + "=");
            if (ind == -1) ind = theCookie.indexOf(";" + cookieName + "=");
            if (ind == -1 || cookieName == "") return "";
            var ind1 = theCookie.indexOf(";", ind + 1);
            if (ind1 == -1) ind1 = theCookie.length;
            return decodeURI(theCookie.substring(ind + cookieName.length + 2, ind1));
        }
    }

    export class ConvertUtils {
        static convertToObject(kvItems: Array<models.kvItem>): any {
            var obj: any = null;
            kvItems.forEach(function (entry: models.kvItem) {
                if (entry.key && entry.key.length > 0 && entry.val && entry.val.length > 0) {
                    if (obj === null)
                        obj = {};
                    obj[entry.key] = entry.val;
                }
            });
            return obj;
        }
    }


    export class StringUtils {
        static formatNumber(num: number): string {
            return num.toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
        }

        static paddingZero(num: number, width: number) {
            var str:string = num.toString() + '';
            return str.length >= width ? str : new Array(width - str.length + 1).join('0') + str;
        }

        static capitaliseFirstLetter(str: string): string {
            return str.charAt(0).toUpperCase() + str.slice(1);
        }
    }

    export class DateTimeUtils {
        static get0hDate(date: Date): Date {
            return new Date(date.getFullYear(), date.getMonth(), date.getDate());
        }
        static getUTCStartOfDate(timeUTC: number) {
            var date = new Date(timeUTC - new Date().getTimezoneOffset() * 60000);
            return date;
        }
        static getStartTimeOfDate(date: Date) {
            return new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
        }
        static getEndTimeOfDate(date: Date) {
            return new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1).getTime();
        }
    }

    export class ObjectUtils {
        static clone(obj: any): any {
            // Handle the 3 simple types, and null or undefined
            if (null == obj || "object" != typeof obj) return obj;

            var copy: any;
            // Handle Date
            if (obj instanceof Date) {
                copy = new Date();
                copy.setTime(obj.getTime());
                return copy;
            }

            // Handle Array
            if (obj instanceof Array) {
                copy = [];
                for (var i = 0, len = obj.length; i < len; i++) {
                    copy[i] = ObjectUtils.clone(obj[i]);
                }
                return copy;
            }

            // Handle Object
            if (obj instanceof Object) {
                copy = {};
                for (var attr in obj) {
                    if (obj.hasOwnProperty(attr)) copy[attr] = ObjectUtils.clone(obj[attr]);
                }
                return copy;
            }
        }
    }

    export class HttpUtils {
        static convertQueryParams(obj: any): string {
                var query: string = '';
                var name, value, fullSubName, subName, subValue, innerObj, i;

                for (name in obj) {
                    value = obj[name];

                    if (value instanceof Array) {
                        var val: string = '';
                        for (i = 0; i < value.length; ++i) {
                            if (value[i] instanceof Object)
                                subValue = JSON.stringify(value[i]);
                            else
                                subValue = value[i];
                            val = val + subValue;
                            if (i < value.length - 1)
                                val += ",";
                        }
                        innerObj = {};
                        innerObj[name] = val;
                        query += HttpUtils.convertQueryParams(innerObj) + '&';
                    }
                    else if (value instanceof Object) {
                        query += name + "=" + JSON.stringify(value) + "&";
                        //for (subName in value) {
                        //    subValue = value[subName];
                        //    fullSubName = name + '[' + subName + ']';
                        //    innerObj = {};
                        //    innerObj[fullSubName] = subValue;
                        //    query += HttpUtils.convertQueryParams(innerObj) + '&';
                        //}
                    }
                    else if (value !== undefined && value !== null) {
                        query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
                    }
                }

                return query.length ? query.substr(0, query.length - 1) : query;
        }
    }

    export class LocalStorageUtils {
        static store(key: string, item: any, expire?: number) {
            if (expire === null || expire === undefined)
                expire = common.Config.LocalStorageExpire;
            var data = { value: item, expire: new Date().getTime() + expire };
            localStorage.setItem(key, JSON.stringify(data));
        }
        static get(key: string) {
            var data: { value; expire: number} = JSON.parse(localStorage.getItem(key));
            if (!!data && data.expire < new Date().getTime()) {
                localStorage.removeItem(key);
                return null;
            }
            if (data == null || data == undefined) return null;
            return data.value;
        }
        static remove(key: string) {
            localStorage.removeItem(key);
        }
    }
}