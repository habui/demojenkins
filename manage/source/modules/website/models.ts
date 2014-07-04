/// <reference path="../../common/common.ts"/>
'use strict';
module models {

    export class Website extends common.Item {
        name: string;
        ownerId: number;
        description: string;
        zoneGroupCount: number;
        zoneGroupIds: number[];
        zoneCount: number;
        zoneIds: number[];
        zoneModel: Array<Zone>;
        categories: number[];//list of type code
        reviewType: string;
        reviewTypeArr: string[];
        roles: string[];
        kind: string;
        frequencyCapping: number;
        frequencyCappingTime: number;

        constructor(id: number, name: string, ownerId: number, des: string, zoneGrpIds: number[], zoneIds: number[], categories : number [], frequencyCapping: number, frequencyCappingTime: number) {
            super(id);
            this.name = name;
            this.ownerId = ownerId;
            this.description = des;
            this.zoneGroupIds = zoneGrpIds;
            this.zoneGroupCount = zoneGrpIds.length;
            this.zoneIds = zoneIds;
            this.zoneCount = zoneIds.length;
            this.categories = categories;
            this.frequencyCapping = frequencyCapping;
            this.frequencyCappingTime = frequencyCappingTime;
        }

        static KIND = {
            PR: "pr",
            AD: "ad"
        }
    }

    export class EZoneRunningMode {
        static BOOKING: string = "booking";
        static NETWORK: string = "network";
        static NETWORK_TVC: string = "tvc";
        static NETWORKBANNER: string = "networkBanner";
    }

    export class EZoneType {
        static BANNER: string = "banner";
        static VIDEO: string = "video";
    }

    export class EZoneRenderKind {
        static NORMAL: string = "";
        static CATFISH: string = "catfish";
        static BALLOON: string = "balloon";
        static POPUP: string = "popup";
        static BANNER_SKIN: string = "banner_skin";
    }

    export class Zone extends common.Item {
        name: string;
        siteId: number;
        zoneGroupIds: number[];
        links: number;
        usageRatio: number;
        width: number;
        height: number;
        kind: string;
        allowedExpand: boolean;
        renderKind: string;
        frequencyCapping: number;
        frequencyCappingTime: number;

        constructor(id: number, name: string, siteId: number, zoneGroupIds: number[], usageRatio: number, links: number) {
            super(id);
            this.name = name;
            this.siteId = siteId;
            this.zoneGroupIds = zoneGroupIds;
            this.usageRatio = usageRatio;
            this.links = links;
            this.kind = "";
            this.renderKind = "";
        }
    }

    export class BannerZone extends Zone {
        bookingPrice: number;
        categories: any[];
        runningMode: string;
        minCPC: number;
        minCPM: number;
        constructor(id: number, name: string, siteId: number, zoneGroupIds: number[], usageRatio: number, links: number,
            categories: number[], runningMode: string, minCPC: number, minCPM: number) {
                super(id, name, siteId, zoneGroupIds, usageRatio, links);
                this.categories = categories;
                this.runningMode = runningMode;
                this.minCPC = minCPC;
                this.minCPM = minCPM;
                this.bookingPrice = 0;
                this.allowedExpand = false;
        }
    }

    export class VideoZoneConst {
        static ALLOWED_TYPE_BANNER: string = "banner";
        static ALLOWED_TYPE_PAUSE_AD: string = "pauseAd";
        static ALLOWED_TYPE_TVC: string = "tvc";

        static BANNER_RUNNING_MODE_BOOKING: string = "booking";
        static BANNER_RUNNING_MODE_NETWORK: string = "network";

        static BANNER_POSITION_MODE_TOP: string = "top";
        static BANNER_POSITION_MODE_BOTTOM: string = "bottom";


        static TVC_POSITION_PREROLL: string = "Pre";
        static TVC_POSITION_MIDROLL: string = "Mid";
        static TVC_POSITION_POSTROLL: string = "Post";

        static getMinCPMType(type: number): string {
            if (type === 3)
                return "create view";
            if (type === 4)
                return "first quatile";
            if (type === 5)
                return "midpoint";
            if (type === 6)
                return "third quatile";
            if (type === 7)
                return "full video";
            return "";

        }

        static changeMinCPMType(type: string): number {
            if (type === "create view")
                return 3;
            if (type === "first quatile")
                return 4;
            if (type === "midpoint")
                return 5;
            if (type === "third quatile")
                return 6;
            if (type === "full video")
                return 7;
            return 0;

        }
    }

    export class VideoZone extends Zone {
        

        categories: Array<number>;
        allowedType: Array<string>;
        // banner
        startTime: number;  // deprecate
        timeSegments: Array<any>;
        runningMode: Array<string>;
        positions: Array<string>;
        maxAdsDuration: number;
        maxAdPodDuration: number;
        minCPC: number;
        minCPM: number;
        // tvc
        tvcPositions: Array<string>;
        preSkiptime: number;
        preMaxDuration: number;
        preMaxPodDuration: number;
        preMinCPM: number;
        preMinCPMType: number;

        midSkipTime: number;
        midMaxDuration: number;
        midMaxPodDuration: number;
        midMinCPM: number;
        midMinCPMType: number;
        midStartTime: number;
        midMaxAdpod: number;
        midTimeScheduled: number;
        midTimeScheduledUnit: string;

        postSkipTime: number;
        postMaxDuration: number;
        postMaxPodDuration: number;
        postMinCPM: number;
        postMinCPMType: number;

        constructor(id: number, name: string, siteId: number, zoneGroupIds: number[], usageRatio: number, links: number, kind: string) {
            super(id, name, siteId, zoneGroupIds, usageRatio, links);
            this.kind = kind;
        }

        setBanner(width: number, height: number, runningMode: Array<string>, positions: Array<string>, maxAdsDuration: number,
            maxAdPodDuration: number, minCPCPrice: number, minCPMPrice: number) {
                this.width = width;
                this.height = height;
                this.runningMode = runningMode;
                this.positions = positions;
                this.maxAdsDuration = maxAdsDuration;
                this.maxAdPodDuration = maxAdPodDuration;
                this.minCPC = minCPCPrice;
                this.minCPM = minCPMPrice;
        }

        setTVC(tvcPositions: Array<string>, preSkiptime: number, preMaxDuration: number, preMaxPodDuration: number, preMinCPM: number, preMinCPMType: number,
            midSkipTime: number, midMaxDuration: number, midMaxPodDuration: number, midMinCPM: number, midMinCPMType: number, midStartTime: number, midMaxAdpod: number, midTimeScheduled: number, midTimeScheduledUnit: string,
            postSkipTime: number, postMaxDuration: number, postMinCPM: number, postMinCPMType: number) {
                this.tvcPositions = tvcPositions;
                this.preSkiptime = preSkiptime;
                this.preMaxDuration = preMaxDuration;
                this.preMaxPodDuration = preMaxPodDuration;
                this.preMinCPM = preMinCPM;
                this.preMinCPMType = preMinCPMType;

                this.midSkipTime = midSkipTime;
                this.midMaxDuration = midMaxDuration;
                this.midMaxPodDuration = midMaxPodDuration;
                this.midMinCPM = midMinCPM;
                this.midMinCPMType = midMinCPMType;
                this.midStartTime = midStartTime;
                this.midMaxAdpod = midMaxAdpod;
                this.midTimeScheduled = midTimeScheduled;
                this.midTimeScheduledUnit = midTimeScheduledUnit

                this.postSkipTime = postSkipTime;
                this.postMaxDuration = postMaxDuration;
                this.postMinCPM = postMinCPM;
                this.postMinCPMType = postMinCPMType;
        }
    }

    export class ZoneGroupMin {
        id: number;
        name: string;
        siteid: number;
    }

    export class ZoneGroup extends common.Item {
        name: string;
        zones: number[];
        zoneCount: number;
        siteId: number;
        constructor(id: number, name: string, zoneids: number[], siteid: number) {
            super(id);
            this.name = name;
            this.zones = zoneids;
            this.siteId = siteid;
            if (zoneids != null) {
                this.zoneCount = zoneids.length;
            }
        }
    }

    export class ApprovedAds extends common.Item {
        id: number;
        website: string;
        websiteId: number;
        owner: string;
        item: any;
        zoneIds: Array<number>;
        zones: Array<string>;
        constructor(id: number) {
            super(id);
            this.id = id;
        }
    }
}

