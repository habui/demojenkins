
module utils {
    'use strict';
    export class DateUtils {
        static getWeek(date): models.TimeRange {
            var lastDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            var from: number = DateUtils.getMonday(lastDate).getTime();
            var to: number = DateUtils.getNextMonday(lastDate).getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getToday(): models.TimeRange {
            var today = new Date();
            var from: number = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
            var to: number = today.getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getYesterday(): models.TimeRange {
            var today = new Date();
            var from: number = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 1).getTime();
            var to: number = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getCurrentWeek(): models.TimeRange {
            var today = new Date();
            var from: number = DateUtils.getMonday(new Date(today.getFullYear(), today.getMonth(), today.getDate())).getTime();
            //var to: number = DateUtils.getNextMonday(new Date(today.getFullYear(), today.getMonth(), today.getDate())).getTime();
            var to: number = today.getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getLast7Days(date): models.TimeRange {
            var firstDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 7);
            var lastDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            var timeRange: models.TimeRange = new models.TimeRange(firstDate.getTime(), lastDate.getTime());
            return timeRange;
        }

        static getLastWeek(): models.TimeRange {
            var currentDate = new Date();
            var lastDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - 7);
            var from: number = DateUtils.getMonday(lastDate).getTime();
            var to: number = DateUtils.getNextMonday(lastDate).getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getPreviousWeek(date): models.TimeRange {
            var lastDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 7);
            var from: number = DateUtils.getMonday(lastDate).getTime();
            var to: number = DateUtils.getNextMonday(lastDate).getTime();
            var timeRange: models.TimeRange = new models.TimeRange(from, to);
            return timeRange;
        }

        static getCurrentMonth(): models.TimeRange {
            var today = new Date();
            var firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
            var lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 1);
            var timeRange: models.TimeRange = new models.TimeRange(firstDay.getTime(), today.getTime());
            return timeRange;
        }

        static getMonth(date): models.TimeRange {
            var firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
            var lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 1);
            return new models.TimeRange(firstDay.getTime(), lastDay.getTime());
        }

        static getLastMonth(): models.TimeRange {
            var date = new Date();
            var firstDay = new Date(date.getFullYear(), date.getMonth() - 1, 1);
            var lastDay = new Date(date.getFullYear(), date.getMonth(), 1);
            return new models.TimeRange(firstDay.getTime(), lastDay.getTime());
        }

        static getPreviousMonth(date): models.TimeRange {
            var firstDay = new Date(date.getFullYear(), date.getMonth() - 1, 1);
            var lastDay = new Date(date.getFullYear(), date.getMonth(), 1);
            return new models.TimeRange(firstDay.getTime(), lastDay.getTime());
        }

        static getPreviousTimeRange(fromDate: Date, toDate: Date): models.TimeRange {
            var startDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
            var endDate = new Date(toDate.getFullYear(), toDate.getMonth() - 1, toDate.getDate());
            return new models.TimeRange(startDate.getTime(), toDate.getTime());
        }

        static getPreviousYearTimeRange(fromDate: Date, toDate: Date): models.TimeRange {
            var startDate = new Date(fromDate.getFullYear() - 1, fromDate.getMonth(), fromDate.getDate());
            var endDate = new Date(toDate.getFullYear() - 1, toDate.getMonth() - 1, toDate.getDate());
            return new models.TimeRange(startDate.getTime(), toDate.getTime());
        }

        static getMonday(d): any {
            d = new Date(d);
            var day = d.getDay();
            var diff = d.getDate() - day + (day == 0 ? -6 : 1); // adjust when day is sunday
            return new Date(d.setDate(diff));
        }

        static getSunday(d) {
            d = new Date(d);
            var day = d.getDay();
            var diff = d.getDate() - day + (day == 0 ? -6 : 1) + 6; // adjust when day is sunday
            return new Date(d.setDate(diff));
        }

        static getNextMonday(d) {
            d = new Date(d);
            var day = d.getDay();
            var diff = d.getDate() - day + (day == 0 ? -6 : 1) + 7; // adjust when day is sunday
            return new Date(d.setDate(diff));
        }
    }

    export class StringUtils {
        static firstCaseLetter(s: string): string {
            s = s.charAt(0).toUpperCase() + s.slice(1);
            return s;
        }
    }
}