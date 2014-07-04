/// <reference path="../../common/common.ts"/>

module models {
    'use strict';

    export class kvItem {
        key: string;
        val: string;

        constructor(key: string, value: string) {
            this.key = key;
            this.val = value;
        }
    }

    export enum EModelType {
        Website = 1,
        Zone = 2,
        ZoneGroup  = 3,
        Order = 4,
        Campaign = 5,
        CampaignItem = 6
    }
    export enum EPaymentType {
        PaymentType1 = 1,
        PaymentType2 = 2
    }

    export enum ECampaignStatus {
        Pending = 1,
        Running = 2,
        Done = 3
    }

    export class EFrequencyCappingUnit {
        static MINUTE: string = "minute";
        static HOUR: string = "hour";
        static DAY: string = "day";
    }

    export class CampaignItemType {
        static BOOKING = {
            BANNER: 'media',
            HTML: 'html',
            TRACKING: "tracking",
            POPUP: "popup",
            EXPANDABLE: 'expandable',
            BALLOON: "balloon",
            PRBANNER: "prBanner"
        };
        static NETWORK = {
            TVC: 'networkTVC',
            TVCBANNER: "networkOverlayBanner",
            TVC_PAUSE_AD: "networkPauseAd",
            BANNER: "networkMedia",
            HTML: "networkHtml",
            BALLOON: "networkBalloon",
            POPUP: "networkPopup",
            EXPANDABLE: "networkExpandable",
            PR: "article",
            PRBANNER: "networkPrBanner"
        };
        static isBookingType(type) {
            var ret = false;
            Object.keys(this.BOOKING).forEach((_k, _) => {
                if (this.BOOKING[_k] === type) return ret = true;
            });
            return ret;
        }
        static isNetworkType(type) {
            var ret = false;
            Object.keys(this.NETWORK).forEach((_k, _) => {
                if (this.NETWORK[_k] === type) return ret = true;
            });
            return ret;
        }
        static isBanner(type){
            return type === this.BOOKING.BANNER || type === this.BOOKING.EXPANDABLE ||
                type === this.NETWORK.BANNER || type === this.NETWORK.TVCBANNER ||
                type === this.NETWORK.EXPANDABLE || type === this.BOOKING.POPUP || type === this.BOOKING.BALLOON;
        }
        static isHTML(type) {
            return type === this.BOOKING.HTML || type === this.NETWORK.HTML;
        }
        static isBannerNotExpandable(type) {
            return type === this.BOOKING.BANNER || type === this.NETWORK.BANNER ||
                type === this.NETWORK.TVCBANNER || type === this.BOOKING.POPUP;
        }
        static isBannerWithExpandable(type) {
            return type === this.BOOKING.EXPANDABLE || type === this.BOOKING.BALLOON ||
                type === this.NETWORK.EXPANDABLE || type === this.NETWORK.BALLOON;
        }
        static isPopupBanner(type: string) {
            return type === this.BOOKING.POPUP || type === this.NETWORK.POPUP;
        }
        static isBalloonBanner(type: string) {
            return type === this.BOOKING.BALLOON || type === this.NETWORK.BALLOON;
        }
        static isExpandableBanner(type: string) {
            return type === this.BOOKING.EXPANDABLE || type === this.NETWORK.EXPANDABLE;
        }
        static isTrackingBanner(type: string) {
            return type === this.BOOKING.TRACKING;
        }
        static isTVC(type) {
            return type === this.NETWORK.TVC || type === this.NETWORK.TVCBANNER || type === this.NETWORK.TVC_PAUSE_AD;
        }
    }

    export class ETVCExtension {
        static NONE: number = 1;
        static INTEGRATED: number = 2;
        static VAST: number = 3;
    }

    /*  Expandable Item */
    export class EExpandDirection {
        static TOP_PUSH_DOWN: number = 1;
        static RIGHT_TO_LEFT: number = 2;
    }

    export class EExpandActiveStyle {
        static MOUSE_OVER: number = 1;
        static AUTO: number = 2;
    }

    export class EExpandDisplayStyle {
        static OVERLAY: number = 1;
        static PUSH_DOWN: number = 2;
    }

    /* ---------------- */
    export class CampaignType {
        static BOOKING: string = 'booking';
        static NETWORK: string = 'network';
        static NETWORK_TVC: string = 'networkTVC';
        static NETWORK_PR: string = 'networkPR';
    }
    export class NetworkCampaignDisplayType {
        static ONLY_ONE: number = 1;
        static ONE_OR_MORE: number = 2;
        static AS_MANY_AS_POSSIBLE: number = 3;
    }

    export class NetworkCampaignTimeZoneType {
        static USER: number = 0;
        static PUBLISHER: number = 1;
    }

    export class OrderStatus {
        static PAUSED: string = "Paused";
        static RUNNING: string = "Running";
        static TERMINATED: string = "Terminated";
    }

    // Order Entity
    export class Order extends common.Item {
        name: string;
        ownerId: number;
        description: string;
        startDate: number;
        endDate: number;
        runningCampaign: number;
        pendingCampaign: number;
        scheduledCampaign: number;
        terminatedCampaign: number;
        pausedCampaign: number;
        outOfBudgetCampaign: number;
        expiredCampaign: number;
        finishedCampaign: number;

        constructor(id: number, name: string, ownerId: number, description: string, startDate: number, endDate: number, 
                 runningCampaign: number, pendingCampaign: number) {
            super(id);
            this.name = name;
            this.ownerId = ownerId;
            this.description = description;
            this.startDate = startDate;
            this.endDate = endDate;
            this.runningCampaign = runningCampaign;
            this.pendingCampaign = pendingCampaign;
        }

        getTotalCampaign(): number {
            return this.runningCampaign + this.pendingCampaign;
        }

    }

    export class CampaignStatus {
        static PAUSED: string = "Paused";
        static RUNNING: string = "Running";
        static TERMINATED: string = "Terminated";
        static PENDING: string = "Pending";
        static SCHEDULED: string =  "Scheduled"
        static OUT_OF_BUDGET: string = "Out_of_budget";
        static EXPIRED: string = "Expired";
        static FINISHED: string = "Finished";
    }

    // POPUP Banner
    export class EPopupActionClose {
        static CLOSE: string = "close";
        static MOVE_TO_TARGET: string = "move_to_target";
    }

    // Balloon Banner
    export class BalloonExpandStyle {
        static NONE: number = 0;
        static STANDARD: number = 1;
        static AUTO: number = 2;
    }

    export class BalloonTVCExtension {
        static NO_TVC: number = 1;
        static INTEGRATED: number = 2;
    }

    // Campaign Entity
    export class Campaign extends common.Item {
        name: string;
        orderId: number;
        startDate: number;
        endDate: number;
        campaignType: string;
        status: string;
        unlinkZoneCount: number;
        unlinkItemCount: number;
        itemCount: number;
        companion: boolean;

        constructor(id: number, name: string, orderId: number, startDate: number, endDate: number, type: string, status: string) {
            super(id);
            this.name = name;
            this.orderId = orderId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.campaignType = type;
            this.status = status;
            this.unlinkItemCount = 0;
            this.unlinkZoneCount = 0;
            this.itemCount = 0;
            this.companion = false;
        }

    }

    export class NetworkCampaign extends Campaign {

        timeScheduled: any;
        timeZone: number;
        freqCapping: number;
        freqCappingTime: number;
        freqCappingTimeUnit: string;
        displayType: number;


        constructor(id: number, name: string, orderId: number,
            startDate: number, endDate: number,
            type: string, status: string, timeScheduled: any, timeZone: number, freqCapping: number,
            freqCappingTime: number, freqCappingTimeUnit: string, displayType: number) {
                super(id, name, orderId, startDate, endDate, type, status);
                this.timeScheduled = timeScheduled;
                this.timeZone = timeZone;
                this.freqCapping = freqCapping;
                this.freqCappingTime = freqCappingTime;
                this.freqCappingTimeUnit = freqCappingTimeUnit;
                this.displayType = displayType;
        }
    }

    /*
    *----------Campaign Item----------
    */
    export class AdItem extends common.Item{
        name: string;
        campaignId: number;
        width: number;
        height: number;
        kind: string;
        currentUsage: number;
        linkedZones: number;
        extendData: any;

        // Network properties
        rate: number;
        rateUnit: number;
        limit: number;
        limitUnit: string;
        lifetimeLimit: number;
        targetPlatform: string;
        highPriority: boolean;
        companionTargetingValues: Array<string>

        // --- frequency capping
        freqCapping: number;
        freqCappingTime: number;
        checkFreqSiteOrZone: boolean;

        targetContent: Array<number>;
        targetZones: Array<number>;
        duration: number;
        geographicTargetings: Array<{ value: string; in: boolean}>;

        channels: string[];
        timeSpan: number;
        startDate: number;
        endDate: number;
        variables: Array<{}>;

        constructor(name: string, campaignId: number, kind: string) {
            super(0);
            this.name = name;
            this.campaignId = campaignId;
            this.width = 0;
            this.height = 0;
            this.kind = kind;
            this.extendData = {};

            if (kind.toLowerCase().indexOf("network") >= 0) {
                this.targetContent = [];
                this.targetZones = [];
                this.channels = [];
                this.variables = [];
                this.rateUnit = 1;
                this.geographicTargetings = [];
                this.freqCapping = 0;
                this.freqCappingTime = 0;
                this.highPriority = false;
                this.companionTargetingValues = [];
            }
        }
    }

    // --------------  BANNER ------------------------
    export class TrackingItem extends AdItem {
        bannerFile: string;
        targetUrl: string;
        impression_tracking: string;
        impression_pixel_tracking: string;
        click_tracking: string;
        click_pixel_tracking: string;
        retargeting: number;
        zoneId: number;

        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.impression_tracking = "";
            this.impression_pixel_tracking = "";
            this.click_tracking = "";
            this.click_pixel_tracking = "";
            this.bannerFile = "";
            this.targetUrl = "";
        }
    }

    export class MediaItem extends AdItem {
        bannerFile: string;
        bannerFileFallback: string;
        targetUrl: string;
        thirdPartyImpressionUrl: string;
        thirdPartyClickUrl: string;
        popupBanner: boolean;
        actionCloseBtn: string;

        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.bannerFile = "";
            this.bannerFileFallback = "";
            this.targetUrl = "";
            this.thirdPartyClickUrl = "";
            this.thirdPartyImpressionUrl = "";

            if (kind === CampaignItemType.BOOKING.POPUP) {
                this.popupBanner = true;
                this.actionCloseBtn = EPopupActionClose.CLOSE;
            }
        }
    }

    export class ExpandableItem extends MediaItem {
        standardFile: string;
        backupFile: string;
        expandFile: string;
        expandWidth: number;
        expandHeight: number;
        expandDirection: number; // expand direction
        expandStyle: number;  // active style
        displayStyle: number; // display style
        iTVCExtension: number;
        tvcFile: string; // will be removed

        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.standardFile = "";
            this.backupFile = "";
            this.expandFile = "";
            this.tvcFile = "";
            this.expandHeight = 0;
            this.expandWidth = 0;
            this.iTVCExtension = ETVCExtension.NONE;
        } 
    }

    export class BalloonItem extends ExpandableItem {
        introText: string;
        barWidth: number;
        barHeight: number;
        barFile: string;

        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.introText = "";
            this.barFile = "";
            this.barWidth = 0; this.barHeight = 0;
            this.expandStyle = BalloonExpandStyle.NONE;
        }
    }
    // ----------------------- HTML -----------------------------

    export class HtmlItem extends AdItem {
        embeddedHtml: string;
        inline: boolean;
        autoLayout: boolean;

    }


    export class TvcItem extends AdItem {
        tvcFile: string;
        extendURL: string;
        wrapper: boolean;
        positions: Array<{}>;
        tvcType: string;
        skip: boolean;
        skipAfter: number;
        thirdParty: ThirdParty;


        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.tvcFile = "";
            this.extendURL = "";
            this.wrapper = false;
            this.positions = [];
            this.skip = true;
            this.thirdParty = new ThirdParty();
        }
    }

    export class OverlayMediaItem extends MediaItem {
        wrapper: boolean;
        extendURL: string;

        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
        }
    }

    export class NetworkVariable {
        key: string;
        value: any;
        bound: number;

        static ISEQUAL = 0;
        static ISNOTEQUAL = 1;

        constructor(key, value, bound) {
            this.key = key;
            this.value = value;
            this.bound = bound;
        }
    }
    

    /*
     * ------------------------- Booking -----------------------------
     */

    export class BookingZone extends common.Item {
        zoneId: number
        name: string;
        siteName: string;
        width: number;
        height: number;
        share: number;
        start_time: number;
        end_time: number;
        items: number;
        disable: boolean;
    }

    export class BookingItem {
        zoneId: number;
        from: number;
        to: number;
        share: number;
    }
    /*
     * -------------------------- Linked Zone ----------------------------
     */
    export class LinkedZone {
        zoneId: number;
        itemId: number;
        from: number;
        to: number;

        constructor(zoneId: number, itemId: number, from: number, to: number) {
            this.zoneId = zoneId;
            this.itemId = itemId;
            this.from = from;
            this.to = to;
        }
    }

    /*
    *-----------------Conversion tracking -------------
    */
    export class Conversion extends common.Item {
        windows: number;
        updateDate: number;
        trackingStatus: string;
        value: any;
        code: string;
        orderId: number;

        constructor(id: number, orderId: number) {
            super(id);
            this.windows = 0;
            this.orderId = orderId;
        }
    }

    export class ArticleItem {
        id: number;
        title: string;
        author: string;
        updateDate: number;
        status: string;
    }

    /*
    * --------------------- New Booking -------------------
    */
    export class BookRecord extends common.Item {
        zoneId: number;
        zoneName: string;
        itemId: number;
        itemName: string;
        siteId: number;
        siteName: string;
        from: number;
        to: number;
        share: number;
        disable: boolean;
        status: number;
        itemWidth: number;
        itemHeight: number;

        constructor(id: number, zoneId: number, zoneName: string, itemId: number, itemName: string, from: number, to: number, share: number) {
            super(id);
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.itemId = itemId;
            this.itemName = itemName;
            this.from = from;
            this.to = to;
            this.share = share;
            this.itemWidth = 0; 
            this.itemHeight = 0;
        }
    }

    export class AvailableBooked{
        zoneId: number;
        zoneName: string;
        site: string;
        disable: boolean;
        availables: Array<BookRecord>;

        constructor() {

        }
    }

    export class EBookingKind {
        static ITEM: string = "item";
        static CAMPAIGN: string = "camp";
        static ZONE: string = "zone";
        static WEBSITE: string = "website";
    }

    export class TrackingLinkItem {
        bannerFile: string;
        impression: string;
        impressionPixel: string;
        click: string;
        clickPixel: string;
    }

    export class Location {
        id: string;
        name: string;

        static CITIES = {
            VN: [
                { id: "VN20", name: "TP. Ho Chi Minh" },
                { id: "VN44", name: "Ha Noi" }]
        };

        static COUNTRIES: Array<models.Location> = [{ id: "VN", name: "Viet Nam" }];
    }

    export class ThirdParty {
        click: string;
        impression: string;
        complete: string;
    }

    export class PrBanner extends AdItem {
        templateType: number;
        categoryTypes: Array<number>;
        constructor(name: string, campaignId: number, kind: string) {
            super(name, campaignId, kind);
            this.categoryTypes = [];
            this.templateType = 1;
        }
    }
}
