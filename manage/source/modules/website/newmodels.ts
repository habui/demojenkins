
module newmodels {
    export class Website extends common.Item {
        name: string;
        ownerId: number;
        ownerName: string; // FE
        description: string;
        zoneGroupCount: number;
        zoneCount: number;
        reviewType: number;
    }

    export class ZoneGroup extends common.Item {
        name: string;
        zoneIds: number[];
        zones: {}; //FE
    }
    export class Zone extends common.Item {
        name: string;
        siteId: number;
        zoneGroupIds: number[];
        zoneGroups: {}; // FE
        linkHTML: string;
        linkJSON: string;
        htmlCode: string;
        width: number;
        height: number;
        links: number;
        usage: number;
        type: number; // 1: banner 2 video banner 3 video tvc
        
    }

    export class BannerZone extends Zone {

        runningMode: number; // 1 : network 2 booking
        type: number; // TBD
        defaultAds: any; // TBD
        minCPC: number;
        minCPM: number;
    }

    
    export class VideoBannerZone extends BannerZone {
        itemType: number; // 1 : Banner 2 : TVC
        displayType: number; // TBD
        adsPosition: number; // 1 : Top 2 : Bottom
        maxAdsDuration: number;
        maxGroupAdsDuration: number;

    }

    export class VideoTVCZone extends Zone {
        itemType: number; // 1 : Banner 2 : TVC
        displayType: number; // TBD
        adsPosition: number; // 1 : Top 2 : Bottom
        maxAdsDuration: number;
        maxGroupAdsDuration: number;
        minCPMprice: number;
        min14price: number;
        min12price: number;
        min34price: number;
        minfullprice: number;
    }
}