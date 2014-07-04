module models {
    'use strict';
    export class TimeRange {
        from: number;
        to: number;
        constructor(from: number, to: number) {
            this.from = parseInt(from + "");
            this.to = parseInt(to + "");
        }
    }

    export class FieldReport {
        label: string;
        value: number;
        compareValue: number;
        change: number;
        static COLUMNNAMELABEL: {} = {
            "impression": "Impression", "click": "Click", "validclick": "Valid click",
            "i": "Impression", "c": "Click",
            "click.spam": "Spam click", "ctr": "CTR (%)", "spent": "Spent (1000VND)",
            "creative": "Creative", "firstQuartile": "First Quartile",
            "midPoint": "Mid Point", "thirdQuartile": "Third Quartile",
            "complete": "Complete", "close": "Close",
            "pause": "Pause", "resume": "Resume", "skip": "Skip",
            "fullscreen": "Fullscreen", "mute": "Mute",
            "unmute": "Unmute", "revenue": "Revenue",
            "r": "Revenue",
            "revenueconversion": "Revenue conversion", "conversion": "Total Conversion",
            "costconversion": "Cost/Conversion", "conversionrate": "Covnersion Rate", "roi": "ROI",
            "requestimpression": "Request Impression", "pageview": "Page View", 'orderName': "Order",
            "collapse": "Collapse", "expand": "Expand",
            "adType": "Ad type", "duration": "Duration", "payType": "Pay type",
            "a": "Ad type", "p": "Pay type",
            "price": "Price", "completed": "Completed",
            "cp": "Completed"
        };
        static COLLUMNNAMEORDER = [
            "adType", "duration", "payType", "price",
            "i", "c", "cp", "r", 
            "impression", "click", "completed", "validclick", "click.spam",
            "revenue", "ctr", "spent", "roi", 
            "revenueconversion", "conversion", "costconversion",
            "conversionrate", "requestimpression", "pageview", 
            "creative", "firstQuartile", "midPoint", "thirdQuartile",
            "complete", "close", "pause", "resume",
            "fullscreen", "mute", "unmute",
            "collapse", "expand", 'orderName'
        ]
        constructor(label: string, value: number, compareValue: number, change: number) {
            this.label = label;
            this.value = value;
            this.compareValue = compareValue;
            this.change = change;
        }
    }

    export class ItemReport {
        id: number;
        name: string;
        extraField: Array<{ label: string; value: any}>;
        properties: Array<FieldReport>;
        constructor(id: number, name: string, properties: Array<FieldReport>) {
            this.id = id;
            this.name = name;
            this.properties = properties;
        }
    }

    export class ItemReportFE {
        id: number;
        name: string;
        fields: Array<string>;
        values: {};
        constructor(id: number, name: string, fields: Array<string>, values: {}) {
            this.id = id;
            this.name = name;
            this.fields = fields;
            this.values = values;
        }

        public static convert(itemReport: ItemReport): ItemReportFE {
            var fieldNames: Array<string> = [];
            var values = {};
            for (var i: number = 0; i < itemReport.properties.length; i++) {
                var fieldReport: FieldReport = common.ObjectUtils.clone(itemReport.properties[i]);
                fieldNames.push(fieldReport.label);
                if (fieldReport.label === 'ctr') {
                    fieldReport.value = parseFloat(fieldReport.value.toFixed(3));
                    if (fieldReport.compareValue !== undefined && fieldReport.compareValue !== null) {
                        fieldReport.compareValue = parseFloat(fieldReport.compareValue.toFixed(3));
                    }
                }
                if (fieldReport.compareValue !== undefined && fieldReport.compareValue !== null) {
                    fieldReport.change = parseFloat(fieldReport.change.toFixed(3));
                }
                values[fieldReport.label] = fieldReport;
            }
            if (itemReport.extraField !== null && itemReport.extraField !== undefined) {
                itemReport.extraField.forEach((o, _) => {
                    fieldNames.push(o.label);
                    values[o.label] = o.value;
                });
            }
            return new ItemReportFE(itemReport.id, itemReport.name, fieldNames, values);
        }
    }

    export class SummaryReport {
        properties: Array<FieldReport>;
        constructor(properites: Array<FieldReport>) {
            this.properties = properites;
        }
    }

    export class SummaryReportFE {
        labels: Array<string>;
        properties: {};
        constructor(labels: Array<string>, properties: {}) {
            this.labels = labels;
            this.properties = properties;
        }

        public static convert(summary: SummaryReport): SummaryReportFE {
            var labels: Array<string> = [];
            var properties: {} = {};
            for (var i: number = 0; i < summary.properties.length; i++) {
                var field: FieldReport = common.ObjectUtils.clone(summary.properties[i]);
                if (field.label === "ctr") { // hard coding, shouldn't 
                    field.value = parseFloat(field.value.toFixed(3));
                    if (field.compareValue !== undefined && field.compareValue !== null) {
                        field.compareValue = parseFloat(field.compareValue.toFixed(3));
                    }
                }
                if (field.compareValue !== undefined && field.compareValue !== null) {
                    field.change = parseFloat(field.change.toFixed(3));
                }
                labels.push(field.label);
                properties[field.label] = field;
            }
            return new SummaryReportFE(labels, properties);
        }
    }

    export class FieldReportResponse {
        value: number;
        compareValue: number;
        change: number;

        public static convert(label: string, fieldReportResp: FieldReportResponse): FieldReport {
            return new FieldReport(label, fieldReportResp.value, fieldReportResp.compareValue, fieldReportResp.change);
        }
    }

    export class ItemReportResponse {
        id: number;
        name: string;
        properties: { [key: string]: FieldReportResponse };

        constructor(id: number, name: string, properties: { [key: string]: FieldReportResponse; }) {
            this.id = id;
            this.name = name;
            this.properties = properties;
        }

        public static convert(itemReportResp: ItemReportResponse): ItemReport {
            var fieldReports:Array < FieldReport> = [];
            for (var key in itemReportResp.properties) {
                fieldReports.push(FieldReportResponse.convert(key, itemReportResp.properties[key]));
            }

            return new ItemReport(itemReportResp.id, itemReportResp.name, fieldReports);
        }
    }

    export class ReportResponse {
        range: TimeRange;
        compareRange: TimeRange;
        total: number;
        items: Array<ItemReport>;
        summary: SummaryReport;
        graph: GraphResponse;
        conversion: Array<ConversionDetail>;
        
        public static convertReport(reportResp: ReportResponse): Report {
            var itemReports: Array<ItemReport> = [];
            for (var key in reportResp.items) {
                itemReports.push(reportResp.items[key]);
            }
            return new Report(reportResp.range, reportResp.compareRange, reportResp.total, reportResp.items, reportResp.summary, GraphResponse.convert(reportResp.graph));
        }
        
    }


    export class ReportRequest {
        range: TimeRange;
        compareRange: TimeRange;
        offset: number;
        count: number;
    }

    export class Report {
        range: TimeRange;
        compareRange: TimeRange;
        total: number;
        items: Array<ItemReport>;
        summary: SummaryReport;
        graph: Graph;

        constructor(range: TimeRange, compareRange: TimeRange, total: number, items: Array<ItemReport>, summary: SummaryReport, graph: Graph) {
            this.range = range;
            this.compareRange = compareRange;
            this.total = total;
            this.items = items;
            this.summary = summary;
            this.graph = graph;
        }
    }

    export class ReportFE {
        range: TimeRange;
        compareRange: TimeRange;
        total: number;
        items: Array<ItemReportFE>;
        summary: SummaryReportFE;
        constructor(range: TimeRange, compareRange: TimeRange, total: number, items: Array<ItemReportFE>, summary: SummaryReportFE) {
            this.range = range;
            this.compareRange = compareRange;
            this.total = total;
            this.items = items;
            this.summary = summary;
        }
        public static convert(report: Report): ReportFE {
            var itemReports: Array<ItemReportFE> = [];
            for (var i: number = 0; i < report.items.length; i++) {
                itemReports.push(ItemReportFE.convert(report.items[i]));
            }
            return new ReportFE(report.range, report.compareRange, report.total, itemReports, SummaryReportFE.convert(report.summary));
        }
    }

    // ------------------ Chart ---------------------------
    export class PointResponse {
        click: number;
        ctr: number;
        impression: number;
        conversion: number;
        date: number;

        public static convert(pointResp: PointResponse): Point {
            return new Point(pointResp.date, pointResp.impression, pointResp.click, pointResp.ctr, pointResp.conversion?pointResp.conversion:0);
        }
    }

    export class Point {
        values: {};
        date: number;
        kind: string;

        constructor(date: number, impression: number, click: number, ctr: number, conversion: number) {
            this.date = date;
            this.values = {};
            this.values['impression'] = impression;
            this.values['click'] = click;
            this.values['ctr'] = ctr;
            this.values['conversion'] = conversion;
        }

        public getValue(kind: string): number {
            return this.values[kind] || 0;
        }
    }

    export class GraphResponse {
        points: Array<PointResponse>;
        comparePoints: Array<PointResponse>;

        public static convert(graphResp: GraphResponse): Graph {
            var points: Array<Point> = [];
            for (var i: number = 0; i < graphResp.points.length; i++) {
                points.push(PointResponse.convert(graphResp.points[i]));
            }

            var comparePoints: Array<Point> = [];
            for (var i: number = 0; i < graphResp.comparePoints.length; i++) {
                comparePoints.push(PointResponse.convert(graphResp.comparePoints[i]));
            }
            return new Graph(points, comparePoints);
        }
    }

    export class Graph {
        points: Array<Point>;
        comparePoints: Array<Point>;
        constructor(points: Array<Point>, comparePoints: Array<Point>) {
            this.points = points;
            this.comparePoints = comparePoints;
        }
    }

    export class LineChartValue {
        time: number;
        values: Array<number>;
        constructor(time: number, values: Array<number>) {
            this.time = time;
            this.values = values;
        }
    }

    export class LineChart {
        items: Array<string>;
        values: Array<LineChartValue>;
        constructor(items: Array<string>, values: Array<LineChartValue>) {
            this.items = items;
            this.values = values;
        }

        public static convert(graph: Graph, item: string, compareItem: string, kind: string): LineChart {
            var items: string[] = [];
            items.push(item); items.push(compareItem);

            var lineValues: Array<LineChartValue> = [];
            var isCompare = (compareItem !== null && compareItem.length > 0) ? true : false;

            if (graph.points === undefined)
                return new LineChart(items, lineValues);

            for (var i: number = 0; i < graph.points.length; i++) {
                var vals: number[] = [];
                vals.push(parseFloat(graph.points[i].getValue(kind).toFixed(3)));
                if (isCompare && graph.comparePoints[i])
                    vals.push(parseFloat(graph.comparePoints[i].getValue(kind).toFixed(3)));
                var lineChartValue = new LineChartValue(graph.points[i].date - new Date().getTimezoneOffset() * 60000, vals);
                lineValues.push(lineChartValue);
            }
            return new LineChart(items, lineValues);
        }
    }

    export class PieChart {
        elements: Array<PieElement>;

        constructor(elements: Array<PieElement>) {
            this.elements = elements;
        }
    }

    export class PieElement {
        label: string;
        data: number;

        constructor(label: string, data: number) {
            this.label = label;
            this.data = data;
        }
    }

    export class ConversionDetail {
        date: number;
        item: Array<{ label: string; value: number; conversion; number}>;
    }

    //------------Zing TV--------------------------
    export class ZingTVItemResponse {
        name: string;
        adType: string;
        payType: string;
        impression: number;
        click: number;
        completed: number;
        revenue: number;

        public static convertToFE(zingTvItem: ZingTVItemResponse): ItemReportFE {
            var fields = [];
            var values = {};
            Object.keys(zingTvItem).forEach((k) => {
                if (typeof k !== "function" && k !== "name") {
                    fields.push(k);
                    if (k === "adType")
                        zingTvItem[k] = ZingTVItemResponse.ADTYPEDEFINE[zingTvItem[k]];
                    if (k === "payType")
                        zingTvItem[k] = ZingTVItemResponse.PAYTYPEDEFINE[zingTvItem[k]];
                    values[k] = new FieldReport(k, zingTvItem[k], 0, 0);
                }
            });
            return new ItemReportFE(0, "", fields, values);
        }

        static ADTYPEDEFINE = {
            "1": "Overlay", "2": "Pause ad", "3": "Preroll", "4": "Midroll",
            "5": "Postroll", "6": "Trueview", "7": "Brander skin"
        };
        static PAYTYPEDEFINE = {
            "1": "CPC", "2": "CPM", "3": "Completed"
        };
    }
    export class ZingTVReportFE extends ReportFE{
        videos: ZingTVVideoReportFE[];
        constructor(range: TimeRange) {
            super(range, new TimeRange(0, 0), 0, [], null);
            this.range = range;
            this.videos = [];
        }
    }
    export class ZingTVVideoReportFE {
        id: string;
        name: string;
        banners: ItemReportFE[];
        programId: number;
        programName: string;
        categoryId: string;
        categoryName: string;
        impression: number;
        click: number;
        completed: number;
        revenue: number;
        constructor(id: string, name: string) {
            this.id = id;
            this.name = name;
            this.impression = 0;
            this.click = 0;
            this.completed = 0;
            this.revenue = 0;
            this.banners = [];
        }
    }
    export class ZingTVReportResponse {
        range: TimeRange;
        videos: {};
        graph: Graph;
        summary: SummaryReport;

        public static convert(zingTV: ZingTVReportResponse): ZingTVReportFE {
            var reportFE = new ZingTVReportFE(zingTV.range);
            if (zingTV.videos) {
                var properties = {};
                Object.keys(zingTV.videos).forEach((vId) => {
                    var video = new ZingTVVideoReportFE(vId, "");
                    var videoDetail = zingTV.videos[vId];
                    video.name = videoDetail.t;
                    video.programId = parseInt(videoDetail.pi);
                    video.programName = videoDetail.pn;
                    var adTypeProperty = videoDetail["a"];

                    Object.keys(adTypeProperty).forEach((adType) => {
                        
                        Object.keys(adTypeProperty[adType]).forEach((payType) => {
                            var item: ZingTVItemResponse = adTypeProperty[adType][payType];
                            item.adType = adType;
                            item.payType = payType;
                            var itemFE = ZingTVItemResponse.convertToFE(item);
                            video.banners.push(itemFE);
                            Object.keys(item).forEach((key) => {
                                if (key === "i" || key === "r" || key === "c" || key === "cp") {
                                    if (properties[key] === undefined) {
                                        properties[key] = new FieldReport(key, 0, 0, 0);
                                    }
                                    properties[key].value += item[key];
                                }
                                switch (key) {
                                    case "i":
                                        video.impression += item[key]; break;
                                    case "r":
                                        video.revenue += item[key]; break;
                                    case "c":
                                        video.click += item[key]; break;
                                    case "cp":
                                        video.completed += item[key]; break;
                                }
                            });
                        });
                    });
                    reportFE.videos.push(video);
                });
                reportFE.summary = new SummaryReportFE([], properties);
            }
            return reportFE;
        }
    }
}