'use strict'
module utils {

    export class BookingItem {
        name: string;
        zoneId: number;
        from: number;
        to: number;
        share: number;
        maxshare: number;
        constructor(name: string, zoneId: number, from: number, to: number, share: number) {
            this.name = name;
            this.zoneId = zoneId;
            this.from = from;
            this.to = to;
            this.share = share;
        }
    }

    export class TimeLine {
        id: string;
        from: number;
        to: number;
        label: string;
        customClass: string;
        value: number;
        dataObj: any;
        descHTML: string;

        constructor(id: string, from: number, to: number, label: string, customClass: string, value: number, dataObj: any) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.label = label;
            this.customClass = customClass;
            this.value = value;
            this.dataObj = dataObj;
            this.descHTML = "";
        }

        setDesc(zoneName: string, startDate: number, endDate: number, share: number) {
            var start: string = moment.unix(Math.round(startDate / 1000)).format('DD - MM - YYYY');;
            var end: string = moment.unix(Math.round(endDate / 1000) - 1).format('DD - MM - YYYY');
            this.descHTML = "<b>" + zoneName + "</b><br/>" +
            "Start date: " + start + "</br>" +
            "End date: " + end + "<br/>" +
            "Share: " + share + "%";
        }
    }

    export class ZoneSchedule {
        id: number;
        name: string;
        desc: string;
        total: number;
        values: TimeLine[];

        constructor(id: number, name: string, desc: string, total: number, values: TimeLine[]) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.total = total;
            this.values = values;
        }
    }

    export class BookingUtils {

        // [NEW BOOKING] get Booking Schedule of Zone
        public static getZoneBookingSchedule(bookingScheduleList: Array<models.BookRecord>): Array<ZoneSchedule> {
            var zoneSchedules: Array<ZoneSchedule> = [];

            var zoneIds: Array<number> = [];
            var zones: {} = {};
            for (var i: number = 0, len: number = bookingScheduleList.length; i < len; i++) {
                var bookRecord: models.BookRecord = bookingScheduleList[i];

                if (zoneIds.indexOf(bookRecord.zoneId) < 0) {
                    zoneIds.push(bookRecord.zoneId);

                    var zoneSchedule: ZoneSchedule = new ZoneSchedule(bookRecord.zoneId, bookRecord.zoneName, "", 0, []);
                    zoneSchedule.values.push(BookingUtils.createBookingZoneTimeLine(bookRecord.from, bookRecord.to, bookRecord.share));

                    zones[bookRecord.zoneId] = zoneSchedule;
                } else {
                    var zoneSchedule: ZoneSchedule = zones[bookRecord.zoneId];
                    zoneSchedule.values.push(BookingUtils.createBookingZoneTimeLine(bookRecord.from, bookRecord.to, bookRecord.share));
                }
            }

            for (var i: number = 0, len: number = zoneIds.length; i < len; i++) {
                var zoneSchedule: ZoneSchedule = zones[zoneIds[i]];
                zoneSchedules.push(zoneSchedule);
            }
            return zoneSchedules;
        }

        // [NEW BOOKING] get Item Booking Schedule of Zone
        public static getItemZoneBookingSchedule(bookingScheduleList: Array<models.BookRecord>): Array<ZoneSchedule> {
            var zoneSchedules: Array<ZoneSchedule> = [];

            for (var i: number = 0, len: number = bookingScheduleList.length; i < len; i++) {
                var bookRecord: models.BookRecord = bookingScheduleList[i];
                var time: string = moment.unix(Math.round(bookRecord.from / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.floor(bookRecord.to / 1000)).format('DD/MM/YYYY');
                var zoneSchedule: ZoneSchedule = new ZoneSchedule(bookRecord.itemId, bookRecord.itemName, time, 0, []);
                var timeline: TimeLine = BookingUtils.createBookingZoneTimeLine(bookRecord.from, bookRecord.to, bookRecord.share);
                timeline.setDesc(bookRecord.itemName, bookRecord.from, bookRecord.to, bookRecord.share);
                zoneSchedule.values.push(timeline);
                zoneSchedules.push(zoneSchedule);
            }
            zoneSchedules.sort((a, b) => {
                return (a.name < b.name ? -1 : 1);;
            });
            return zoneSchedules;
        }

        public static getZoneSchedules(data: any[], itemBanner: models.AdItem, zoneSizeDic: {}): ZoneSchedule[]{
            var zoneSchedules: ZoneSchedule[] = [];

            for (var i: number = 0; i < data.length; i++) {
                var zone: any = data[i];
                var zoneId: number = zone.zoneId;
                if (zoneSizeDic[zoneId]["w"] >= itemBanner.width && zoneSizeDic[zoneId]["h"] >= itemBanner.height) {
                    var zoneName: string = zone.zoneName;
                    var siteName: string = "";

                    var timelines: TimeLine[] = [];
                    var valueItems: any[] = zone.availables;
                    for (var j: number = 0; j < valueItems.length; j++) {
                        var valueItem: any = valueItems[j];
                        var from: number = valueItem.from;
                        var to: number = valueItem.to;
                        var value: number = 100 - valueItem.share;
                        var timeline: TimeLine = BookingUtils.createTimeLine(from, to, value);
                        timelines.push(timeline);
                    }

                    var zoneSchedule: ZoneSchedule = new ZoneSchedule(zoneId, zoneName, siteName, 0, timelines);
                    zoneSchedules.push(zoneSchedule);
                }
            }
            return zoneSchedules;
        }

        public static getBookingBill(data: any[]): ZoneSchedule[] {
            var zoneSchedules: ZoneSchedule[] = [];
            for (var i: number = 0; i < data.length; i++) {
                var item: any = data[i];
                var zoneId: number = item.zoneId;
                var zoneName: string = item.zoneName;
                var siteName: string = item.site;


                var timelines: TimeLine[] = [];
                var valueItems: any[] = item.availables;
                for (var j: number = 0; j < valueItems.length; j++) {
                    var valueItem: any = valueItems[j];
                    var from: number = valueItem.from;
                    var to: number = valueItem.to;
                    var value: number = valueItem.share;
                    var timeline: TimeLine = BookingUtils.createBookingItem("", zoneName + '_' + (j + 1).toString(), zoneId, from, to, value);
                    timelines.push(timeline);
                }

                var zoneSchedule: ZoneSchedule = new ZoneSchedule(zoneId, zoneName, siteName, item.cost, timelines);
                zoneSchedules.push(zoneSchedule);
            }
            return zoneSchedules;
        }

        // [NEW BOOKING] media plan of campaign
        public static getBookingPlan(bookingScheduleList: Array<models.BookRecord>): ZoneSchedule[] {
            var zoneSchedules: ZoneSchedule[] = [];
            var dicZone: {} = {};
            var itemSorted: Array<{}> = [];
            for (var i: number = 0, len: number = bookingScheduleList.length; i < len; i++) {
                var item: models.BookRecord = bookingScheduleList[i];
                var bookId: number = item.id;
                var zoneId: number = item.zoneId;
                var zoneName: string = item.zoneName;
                var itemId: number = item.itemId;
                var itemName: string = item.itemName;
                var siteName: string = item.siteName;
                var share: number = item.share;

                var from: number = item.from;
                var to: number = item.to;

                if (dicZone[bookId] === undefined || dicZone[bookId] === null) {
                    var timelines: TimeLine[] = [];
                    var timeline: TimeLine = BookingUtils.createBookingItem(bookId.toString(), "", zoneId, from, to, share);
                    timeline.setDesc(zoneName, from, to, share);
                    timelines.push(timeline);
                    var desc = zoneName + "[" + siteName + "]";
                    dicZone[bookId] = new utils.ZoneSchedule(itemId, itemName, desc, 0, timelines);
                    itemSorted.push({name: itemName, id: bookId, zoneName: zoneName});
                } else {
                    var newtimeline: TimeLine = BookingUtils.createBookingItem(bookId.toString(), "", zoneId, from, to, share);
                    newtimeline.setDesc(zoneName, from, to, share);
                    var timelines: TimeLine[] = dicZone[bookId].values;

                    var temptimelines: Array<TimeLine> = [];
                    var isPush: boolean = false;
                    for (var i: number = 0; i < timelines.length; i++) {
                        if (isPush && newtimeline.to < timelines[i].from) {
                            temptimelines.push(newtimeline);
                            isPush = true;
                        }
                        temptimelines.push(timelines[i]);
                    }

                    if (isPush === false)
                        temptimelines.push(newtimeline);

                    dicZone[bookId].values = temptimelines;
                }
            }
            itemSorted.sort((a, b) => {
                if (a["name"] === b["name"])
                    return (a["zoneName"] < b["zoneName"] ? -1 : 1);
                return (a["name"] < b["name"] ? -1 : 1);
            });
            for (var i = 0; i < itemSorted.length; i++) {
                zoneSchedules.push(dicZone[itemSorted[i]["id"]]);
            }
            return zoneSchedules;
        }

        public static getBookingItems(data: any[]): BookingItem[]{
            var bookingItems: BookingItem[] = [];
            for (var i: number = 0; i < data.length; i++) {
                var item: any = data[i];
                var zoneId: number = item.zoneId;
                var zoneName: string = item.zoneName;
                var siteName: string = item.site;

                var valueItems: any[] = item.availables;
                for (var j: number = 0; j < valueItems.length; j++) {
                    var valueItem: any = valueItems[j];
                    var from: number = valueItem.from;
                    var to: number = valueItem.to;
                    var value: number = valueItem.share;
                    bookingItems.push(new BookingItem(zoneName + '_' + (j + 1).toString(), zoneId, from, to, value));
                }
            }
            return bookingItems;
        }


        // [NEW BOOKING]
        private static createBookingZoneTimeLine(from: number, to: number, value: number): TimeLine {
            var id: string = "";
            var customClass: string = "";
            var label: string = "";
            label = value.toString() + "%";
            if (value === 100) {
                customClass = "ganttDarkRed";
            } else if (value >= 75) {
                customClass = "ganttRed";
            } else if (value >= 50) {
                customClass = "ganttOrange";
            } else if (value >= 25) {
                customClass = "ganttGreen";
            } else {
                customClass = "ganttBlue"; 
            }
            return new TimeLine(id, from, to, label, customClass, value, null);
        }

        private static createTimeLine(from: number, to: number, value: number): TimeLine {
            var id: string = "";
            var customClass: string = "";
            var label: string = "";
            if (value >= 75) {
                customClass = "ganttBlue"; label = (100 - value).toString() + "%";
            } else if (value >= 50) {
                customClass = "ganttGreen"; label = (100 - value).toString() + "%";
            } else if (value >= 25) {
                customClass = "ganttOrange"; label = (100 - value).toString() + "%";
            } else if (value > 0) {
                customClass = "ganttRed"; label = (100 - value).toString() + "%";
            } else {
                customClass = "ganttDarkRed"; label = (100 - value).toString() + "%";
            }
            var dataObj = null;//{ zoneId: zoneid, from: from, to: to, share: value};
            return new TimeLine(id, from, to, label, customClass, value, dataObj);
        }

        private static createBookingItem(id: string, name: string,zoneId: number, from: number, to: number, value: number): TimeLine {
            var customClass: string = "ganttLightBlue";
            var label: string = name + ' (' + value.toString() + '%)';
            var dataObj = { zoneId: zoneId, from: from, to: to, share: value, id: id };
            return new TimeLine(id, from, to, label, customClass, value, dataObj);
        }
    }

    export class DataStore {
        dataStore: {};

        constructor() {
            this.dataStore = {};
        }

        store(key: string, data: any): boolean {
            this.dataStore[key] = data;
            return true;
        }

        get(key: string): any {
            if (this.dataStore[key] !== undefined)
                return this.dataStore[key];
            return null;
        }

        remove(key: string): boolean {
            delete this.dataStore[key];
            return true;
        } 
    }
}
