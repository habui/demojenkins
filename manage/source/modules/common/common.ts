/// <reference path="../../libs/jquery.d.ts"/>
/// <reference path="../../libs/jquery.flot.d.ts"/>
// Module
module common {
    'use strict';
    export class Config {
        static COOKIE_NAME: string = "123click";
        static API_URL: string = "http://dev.api.adtima.vn/rest";
        static UPLOAD_URL: string = Config.API_URL + "/misc/upload";
        static ImageRootUrl: string = "http://dev.adtima.vn";

        static Timeout: number = 10000;// milisecond
        static LocalStorageExpire: number = 3600000;
        static ADMINCMSZINGNEW = "http://admin.news.zing.vn/Default.aspx?cmd=edit&contentid=";
        static LimitLengthPRArticle = 8000;
    }

    export class ActionDefinition {
        static NEW_CAMP_ITEM_WITH_ZONES: string = "new_item_with_zones";
        static BOOK_MORE_ZONE: string = "book_more_zone";
    }

    export class SortDefinition {
        static NO_SORT: number = -1;
        static DEFAULT: number = 0;
        static UP: number = 1;
        static DOWN: number = 2;
    }

    export class Size {
        static ZoneSize = [
            { text: 'IAB Full Banner (468 x 60)', value: '468x60', },
            { text: 'IAB Skyscraper (120 x 600)', value: '120x600', },
            { text: 'IAB Leaderboard (728 x 90)', value: '728x90', },
            { text: 'IAB Button 1 (120 x 90)', value: '120x90', },
            { text: 'IAB Button 2 (120 x 60)', value: '120x60', },
            { text: 'IAB Half Banner (234 x 60)', value: '234x60', },
            { text: 'IAB Micro Bar (88 x 31)', value: '88x31', },
            { text: 'IAB Square Button (125 x 125)', value: '125x125', },
            { text: 'IAB Vertical Banner (120 x 240)', value: '120x240', },
            { text: 'IAB Rectangle (180 x 150)', value: '180x150', },
            { text: 'IAB Medium Rectangle (300 x 250)', value: '300x250', },
            { text: 'IAB Large Rectangle (336 x 280)', value: '336x280', },
            { text: 'IAB Vertical Rectangle (240 x 400)', value: '240x400', },
            { text: 'IAB Square Pop-up (250 x 250)', value: '250x250', },
            { text: 'IAB Wide Skyscraper (160 x 600)', value: '160x600', },
            { text: 'IAB Pop-Under (720 x 300)', value: '720x300', },
            { text: 'IAB 3:1 Rectangle (300 x 100)', value: '300x100', },
        ];
    }

    // Class
    export class Charts {
        colors: Array<string>;
        // Constructor
        constructor() {
            this.colors = ['#94BA65', '#2B4E72', '#2790B0', '#777', '#555', '#999', '#bbb', '#ccc', '#eee'];
        }
        line(target, data, xticks?): jquery.flot.plot {
            var plot: jquery.flot.plot;
            var options = {
                colors: this.colors,
                series: {
                    lines: {
                        show: true,
                        fill: true,
                        lineWidth: 4,
                        steps: false,
                        fillColor: { colors: [{ opacity: 0.4 }, { opacity: 0 }] }
                    },
                    points: {
                        show: true,
                        radius: 4,
                        fill: true
                    }
                },
                legend: {
                    position: 'ne'
                },
                tooltip: true,
                tooltipOpts: {
                    content: '%x : %y'
                },
                xaxis: { mode: "time", ticks: xticks },
                yaxis: { tickFormatter: function (val) { return (Math.round(val * 100) / 100).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,"); } },
                grid: { borderWidth: 2, hoverable: true }
            };

            var el = jQuery(target);

            if (el.length) {
                plot = jQuery.plot(el, data, options);
            }
            return plot;
        }

        pie(target, data): jquery.flot.plot {
            var plot: jquery.flot.plot;
            var options = {
                colors: this.colors,

                series: {
                    pie: {
                        show: true,
                        innerRadius: 0,
                        stroke: {
                            width: 4
                        }
                    }
                },

                legend: {
                    position: 'ne'
                },

                tooltip: true,
                tooltipOpts: {
                    content: '%s: %y'
                },
                grid: {
                    borderWidth: 2,
                    hoverable: true
                }
            };

            var el = jQuery(target);

            if (el.length) {
                plot = jQuery.plot(el, data, options);
            }
            return plot;
        }

        donut(target, data): jquery.flot.plot {
            var plot: jquery.flot.plot;
            var options = {
                colors: this.colors,

                series: {
                    pie: {
                        show: true,
                        innerRadius: .5,
                        stroke: {
                            width: 4
                        }
                    }
                },

                legend: {
                    position: 'ne'
                },

                tooltip: true,
                tooltipOpts: {
                    content: '%s: %y'
                },

                grid: {
                    borderWidth: 2,
                    hoverable: true
                }
            };

            var el = jQuery(target);

            if (el.length) {
                plot = jQuery.plot(el, data, options);
            }
            return plot;
        }
    }

    export class ActionModel {
        action: string;
        model: string;
        id: number;
        obj: string;
        data: string;
        count: number;
        from: number;
        userId: number;
    }
}

