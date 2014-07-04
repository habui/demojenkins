/// <reference path="../../common/scopes.ts"/>
/// <reference path="../../common/common.ts"/>
'use strict';
module scopes {

    export interface IOrderScope extends common.IListScope<models.Order>, common.IPermissionScope {
        actionMenus: common.ActionMenu[];
        searchField: string;
        search();
        goTab(tabname: string);
        isActiveTab(tabname: string): string;
        doIt(actionName: string);
    }

    export interface IOrderBreadcumNavScope extends common.IMenuScope {
        items: common.MenuItem[];
        orderId: number;
        campaignId: number;
        itemId: number;
        isActive(menu: string);
        isShow():boolean;
    }
    /*
     * --------------------- ORDER -----------------
     */
    export interface IOrderListScope extends common.IListScope<models.Order> {
        isChosen: boolean;
        checkBoxes: {};
        searchText: string;
        formatNumber(num: number): string;
        formatDateTime(timestamp: number): string;
        terminate();
        pause();
        resume();
        search();
        gotoOrder(id: number);
        gotoOrderSetting(id: number);
        isActiveClass(): string;
        check(id: number);
        filterBy: string;
        filterText: string;
        filter(type: string);
        sortField: {};
        getSortClass(type: string): string;
        getStatusClass(status: string): string;
        switchSort(type: string);
    }

    export interface IOrderItemListScope extends common.IListScope<models.AdItem> {
        orderId: number;
        isChosen: boolean;
        check(id: number);
        isActiveClass(): string;
        isBannerImg(filename: string): boolean;
        isBannerFlash(filename: string): boolean;
        checkboxes: {};
        gotoItem(id: number);
        gotoItemSetting(id: number);
        itemPreview: models.AdItem;
        previewItem(id: number);
        link();
        unlink();

        contentDic: {};
        sizeDic: {};
        isKind(type: string, kind: string): boolean;
        searchText: string;
        search();
        getKind(kind: string): string;
        view(id: number, name: string, kind: string);
        previewSource: string;
        previewContent: string;
        previewTitle: string;
        previewWidth: number;
        previewHeight: number;

        isVideo(): boolean;
    }

    export interface IOrderDetailScope extends ng.IScope, common.IPermissionScope {
        orderId: number;
        title: string;
        isActiveTab(tabname: string): string;
        gotoTab(tabname: string);
    }

    export interface IOrderCreateScope extends common.IItemScope<models.Order> {
        checkValidate: boolean;
        option: string;
        owners: models.UserInfo[];
        ownerName: string;
        isInvalidOwner(): boolean;
        isAdminRole(): boolean;
        isCampaignTypeSelected(): boolean;
        goto(dest: string);
        choose(ownerId: number);
        getOwnerName(ownerId: number): string;
    }

    export interface IOrderSettingScope extends common.IItemScope<models.Order> {
        orderId: number;
        owners: models.UserInfo[];
        checkValidate: boolean;
        ownerName: string;
        isFound(): boolean;
        isAdminRole(): boolean;
        goto(dest: string);
        choose(ownerId: number);
        getOwnerName(ownerId: number): string;
    }

    export interface IOrderAssignedUserScope extends common.IListScope<models.UserInfo> {
        orderId: number;
        searchText: string;
        roles: string[];
        userroles: models.UserRoleInfo[];
        currentUserRoles: models.UserRoleInfo[];
        users: models.UserInfo[];
        selectUser: models.UserRoleInfo;
        hasSave: boolean;
        add();
        search();
        save();
        choose(user: models.UserRoleInfo);
        hasChecked(user: number, role: number);
        onchange(user: number, role: number);

        currentOffset: number;
        count: number;
        totalItems: number;
    }

    /*
     * -------------------------- CAMPAIGN -------------------------------------
     */ 
    export interface ICampaignDetailScope extends common.IItemScope<models.Campaign> {
        orderId: number;
        campaignId: number;
        title: string;
        freeItemCount: number;
        isBookingCampaign: boolean;
        isNetworkPRCampaign: boolean;
        isActiveTab(tabname: string): string;
        gotoTab(tabname: string);
        goUnlinkBookingZoneList();
        goUnlinkBookingItemList();
    }

    export interface ICampaignListScope extends common.IListScope<models.Campaign> {
        orderId: number;
        filterText: string;
        searchText: string;
        checkBoxes: {};
        isChosen: boolean;
        gotoCampaign(id: number);
        gotoCampaignSetting(id: number);
        formatNumber(num: number): string;
        formatDateTime(timestamp: number): string;
        disable();
        enable();
        pause();
        resume();
        search();
        changeStatus(id: number, status: number);
        isActiveClass(): string;
        check(id: number);
        getCampaignTypeClass(type: string): string;
        getCampaignStatusClass(type: string): string;
        sortField: {};
        getSortClass(type: string): string;
        switchSort(type: string);
        select(type: string);
    }

    export interface ICampaignScope extends common.IItemScope<models.Campaign> {
        orderId: number;
        datepicker: {};
        timeinday: {};
        title: string;
        startDate: string;
        endDate: string;
        campaignType: string;
        order: models.Order;
        isAllDayTime(): boolean;
        isDisable(frm: any): boolean;
        isNetworkCampaign(): boolean;
        hasSave: boolean;

        impression: number;
        time: number;
        timeunit: string;
        timeType: string;
        displayType: number;
        displayTypeString: string;
        timeUnitString: string;

        save();
        goto(dest: string);

        checkValidate: boolean;
        validateStartTime(): boolean;
        validateEndTime(): boolean;
        validateNumber(num: number);
        chooseTimeUnit(time: string);
        chooseDisplayType(displayType: number);
        getCampaignStatusClass(status: string): string;

        // frequency capping
        freq_impression: number;
        freq_time: number;
        freq_time_string: string;
        chooseFreqTimeUnit(time: string);
    }

    export interface ICampaignMediaScope extends ng.IScope, common.IPermissionScope {
        datepicker: any;
        item: BookingItem;  // zoneid, start, end, percent
        orderId: number;
        isEditZone: boolean;
        selecteditem: any;
        campaign_name: string;
        startTimestamp: number;
        endTimestamp: number;
        data: any;
        bookRecordDic: {};
        hasMediaPlan(): boolean;
        goBookingPlan();
        editbooking();
        editPopupStyle: any;
        editPopupTop: number;
        editPopupLeft: number;
        available: number;
        share: string;
        action: string;
        isActive(type: string): string;
        changeAction(action: string);
        isAction(action: string): boolean;
        close();
        doAction();
        isDisable(type: string): boolean;
        items: Array<models.AdItem>;
        hasItem: boolean;
    }

    export interface ICampaignItemListScope extends common.IListScope<models.AdItem> {
        load();
        campaignZones: Array<models.BookingZone>;
        orderId: number;
        isChosen: boolean;
        campaign: models.Campaign;
        chooseItem: models.AdItem;
        checkBoxes: {};
        checkZones: {};
        gotoItem(itemId: number);
        gotoItemSetting(itemId: number);
        isBannerImg(filename: string): boolean;
        isBannerFlash(filename: string): boolean;
        isNetworkCampaign: boolean;
        isPRCampaign: boolean;
        //function
        newItem();
        deleteItems();
        enableItems();
        check(id: number);
        isActiveClass(type: string): string;
        getSource(url: string): string;
        itemPreview: models.AdItem;
        previewItem(id: number);

        isBooking(type: string): boolean;
        search();
        searchText: string;
        isNetwork(kind: string, zone: number): boolean;
        isNetworkWithoutZone(kind: string, zone: number): boolean;
        getKind(kind: string): string;
        view(id: number, name: string, kind: string);
        previewSource: string;
        previewContent: string;
        previewTitle: string;
        contentDic: {};
        sizeDic: {};
        previewWidth: number;
        previewHeight: number;

        isShownCloneItem: boolean;
        isDisableList: boolean;
        cloneItem(itemId: number, itemName: string, event: any);
        cloneId: number;
        cloneName: string;
        cloneItemName: string;
        createItem();
        closeCloneItem();
        position: any;
        filterText: string;
        select(type: string);
    }

    export interface ICampaignItemTypeSelect extends ng.IScope {
        choose(type: string); //banner or html
        isNetworkTVC: boolean;
    }

    export interface ICampaignItemSettingScope extends common.IItemScope<any> {
        orderId: number;
        bookedZones: Array<models.BookingZone>;
        datepicker: {};
        checkValidate: boolean;
        uploadUrl: string;
        uploadResult: any;
        getFullUrl(relativeUrl: string): string;
        isBannerImage(bannerUrl: string): boolean;
        isBannerFlash(bannerUrl: string): boolean;
        rateUnit: string;
        isWarningSize: boolean;
        isWarningZoneSize: boolean;
        isWarningSelectZone: boolean;
        showBookingZoneDetail(zoneId: number, zoneName: string, event: any);
        isExtlnk: boolean;
        extlnkimage: boolean;
        extlnkflash: boolean;
        item_kind: string;

        timeRanges: Array<any>;
        isShowBookingCustom: boolean;
        positionCustomBooking: {};
        chooseLinkedZoneOption(option: string);
        custom_linkedzone: boolean;
        addValidTime();
        removeValidTime(index: number);
        saveLinkedZoneOption();
        closeLinkedZoneOption();

        targetDisplay: string;
        choose(type: string, value: string);
        check(type: string, index: number);
        checkWebsite(id: number);
        checkZone(websiteId: number, zoneId: number, pos: string);
        checkZoneWeb(websiteId: number); checkboxZoneWeb: {};
        uncheck(type: string, index: number);
        websites: Array<models.Website>;
        siteNameDic: {};
        searchSite: string;
        selectedWebsites: Array<number>;
        checkboxWebsite: {};
        chooseTVCContent: string;

        selectedZones: Array<{}>;
        targetZonesOption: string;
        zones: {}; // all zones from selectedWebsites
        checkboxZone: {};
        locations: Array<models.Location>;
        checkboxLocation: {};
        belongLocation: {};
        osTargeting: {};
        variables: Array<models.NetworkVariable>;
        companionTargetingValues: Array<any>;

        chooseSize(value: string);
        sizes: any;
        search: any;

        expandBannerUploadResult: string;
        expandableZone: {};
        expandSearch: any;
        expand_direction: string;
        expand_style: string;
        expand_display_style: string;
        chooseDisplayStyle(type: string);
        chooseDirection(type: string);
        chooseStyle(type: string);
        bStatus: any;

        showWarning: boolean;
        categories: models.Category[];
        targeting: {};
        sTargeting: string;
        hours: number;
        minutes: number;
        seconds: number;
        remove_target(target: string);
        add_variable();
        remove_variable(index: number);
        changeKey(index: number);
        changeValue(index: number);

        strBound: {};
        retargeting: Array<models.NetworkVariable>

        // frequency capping
        freq_impression: number;
        freq_time: number;
        freq_time_string: string;
        chooseFreqTimeUnit(time: string);

        // --------- Tracking Item
        isUpload: boolean;
        isShowBannerUrl(): boolean;
        uploadFile();
        campaign: models.Campaign;
        formatDateTime(timestamp: number);
        unlimitStartDate: boolean;
        unlimitEndDate: boolean;
        isNetworkOverlay(kind: string): boolean;
        isNetworkMedia(kind: string): boolean;

        // POP UP
        close_btn_action: string;
        chooseCloseBtnAction(action: string);

        // Tracking 
        showTrackingLink: boolean;

        // BALLOON
        chooseExpandStyle(style: string);
        mimetype: string;
        isExpand: boolean;
        tvc_extension: string;
        chooseTVCExtension(kind: string);

        xdata: boolean;
        xitems: Array<models.kvItem>;
        addXData();

        //---------- Pr Banner ---------------
        searchCates: {};
        articleCates: Array<any>;
        selectedCates: Array<any>;
        checkBoxCate: {};
        selectCate(cate: any);
        removeCate(id: number);
        cateDic: {};
        option: { targetZone: string }
    }

    export interface ICreateBookingBannerItemScope extends common.IItemScope<models.MediaItem> {
        orderId: number;
        bookedZones: Array<models.BookingZone>;
        datepicker: {};
        checkValidate: boolean;
        bannerUploadResult: string;// bannerUrl: string;
        uploadUrl: string;
        bannerFallbackUploadResult: string; //bannerFallbackUrl: string;
        tvcFileUploadResult: string;
        getFullUrl(relativeUrl: string): string;
        isBannerImg(bannerUrl: string): boolean;
        isBannerFlash(bannerUrl: string): boolean;
        rateUnit: string;
        isWarningSize: boolean;
        isWarningZoneSize: boolean;
        isWarningSelectZone: boolean;
        showBookingZoneDetail(zoneId: number, zoneName: string, event: any);
        isExtlnk: boolean;
        extlnkimage: boolean;
        extlnkflash: boolean;
        xdata: boolean;
        xitems: Array<models.kvItem>;
        addXData();


        timeRanges: Array<any>;
        isShowBookingCustom: boolean;
        positionCustomBooking: {};
        chooseLinkedZoneOption(option: string);
        custom_linkedzone: boolean;
        addValidTime();
        removeValidTime(index: number);
        saveLinkedZoneOption();
        closeLinkedZoneOption();

        targetDisplay: string;
        choose(type: string, value: string);
        check(type: string, index: number);
        checkWebsite(id: number);
        checkZone(websiteId: number, zoneId: number, pos: string);
        checkZoneWeb(websiteId: number); checkboxZoneWeb: {};
        uncheck(type: string, index: number);
        websites: Array<models.Website>;
        siteNameDic: {};
        searchSite: string;
        selectedWebsites: Array<number>;
        checkboxWebsite: {};
        chooseTVCContent: string;

        selectedZones: Array<{}>;
        targetZonesOption: string;
        zones: {}; // all zones from selectedWebsites
        checkboxZone: {};
        locations: Array<models.Location>;
        checkboxLocation: {};

        chooseSize(value: string);
        sizes: any;
        search: any;

        expandBannerUploadResult: string;
        expandableZone: {};
        expandSearch: any;
        expand_direction: string;
        expand_style: string;
        expand_display_style: string;
        chooseDisplayStyle(type: string);
        chooseDirection(type: string);
        chooseStyle(type: string);
        TVCUploadResult: string;
        iTVCExtension: boolean;

        showWarning: boolean;
        categories: models.Category[];
        targeting: {};
        sTargeting: string;
        hours: number;
        minutes: number;
        seconds: number;
        remove_target(target: string);
        sBound: Array<string>;
        add_variable();
        remove_variable(index: number);
        changeKey(index: number);
        changeValue(index: number);
        chooseBound(index: number, value: number);
        // --------- Tracking Item
        isUpload: boolean;
        isShowBannerUrl(): boolean;
        uploadFile();
        campaign: models.Campaign;
        formatDateTime(timestamp: number);
        unlimitStartDate: boolean;
        unlimitEndDate: boolean;
        isNetworkOverlay(kind: string): boolean;
        isNetworkMedia(kind: string): boolean;

        // POP UP
        close_btn_action: string;
        chooseCloseBtnAction(action: string);

        // BALLOON
        chooseExpandStyle(style: string);
        mimetype: string;
        isExpand: boolean;
    }

    export interface ICreateBalloonItemScope extends common.IItemScope<models.BalloonItem> {
        uploadUrl: string;
        chooseSize(value: string);
        checkValidate: boolean;
        sizes: any;
        search: any;
        isExpand: boolean;
        expand_style: string;
        barUploadResult: string;
        bannerUploadResult: string;
        expandBannerUploadResult: string;
        bannerFallbackUploadResult: string;
        mimetype: string;
        chooseExpandStyle(type: string);
        save();
        isBannerImage(filename: string): boolean;
        isBannerFlash(filename: string): boolean;
        TVCUploadResult: string;
        tvc_extension: string;
        chooseTVCExtension(kind: string);
    }

    export interface ICreateExpandableBannerItemScope extends common.IItemScope<models.ExpandableItem> {
        bookedZones: Array<models.BookingZone>;
        expandableZone: {};
        uploadUrl: string;
        checkValidate: boolean;
        bannerUploadResult: string; 
        expandBannerUploadResult: string;
        bannerFallbackUploadResult: string; 
        TVCUploadResult: string;
        iTVCExtension: boolean;
        isBannerImg(bannerUrl: string): boolean;
        isBannerFlash(bannerUrl: string): boolean;
        datepicker: {};

        isWarningSize: boolean;
        isWarningZoneSize: boolean;

        expand_direction: string;
        expand_style: string;
        expand_display_style: string;

        chooseDisplayStyle(type: string);
        chooseDirection(type: string);
        chooseStyle(type: string);

        chooseSize(value: string);
        sizes: any;
        search: any;
        expandSearch: any;
    }
    export interface ICreateBookingHtmlItemScope extends common.IItemScope<models.HtmlItem> {
        //sizes: common.ItemType[];
        checkValidate: boolean;
        bookedZones: Array<models.BookingZone>;
        createCode: number;
        isWarningSize: boolean;
        bookingHtmlItemFrom: any;
        datepicker: {};

        chooseSize(value: string);
        sizes: any;
        search: any;
    }

    export interface ICreateTrackingItemScope extends common.IItemScope<models.TrackingItem> {
        bannerUploadResult: string;
        uploadUrl: string;
        checkValidate: boolean;
        isBannerImg(bannerUrl: string): boolean;
        isBannerFlash(bannerUrl: string): boolean;
    }

    export interface ICreateItemScope extends common.IItemScope<any> {
        checkValidate: boolean;
        targetDisplay: string;
        bannerUploadResult: string;
        bannerUrl: string;
        uploadUrl: string;

        uploadResult: any;
        mimetype: string;
        getFullUrl(relativeUrl: string): string;
        isBannerImage(bannerUrl: string): boolean;
        isBannerFlash(bannerUrl: string): boolean;
        rateUnit: string;

        runningTimeCustom: boolean;
        datepicker: {};
        kind_banner: string;
        kind: string;
        title: string;

        choose(type: string, value: string);
        check(type: string, index: number);
        checkWebsite(id: number);
        checkZone(websiteId: number, zoneId: number, pos: string);
        checkZoneWeb(websiteId: number);
        checkboxZoneWeb: {};
        uncheck(type: string, index: number);
        websites: Array<models.Website>;
        seachSite: string;
        siteNameDic: {};
        selectedWebsites: Array<number>;
        checkboxWebsite: {};
        isWarningSize: boolean;
        isWarningZoneSize: boolean;
        isWarningSelectZone: boolean;
        chooseTVCContent: string;
        bStatus: any;

        selectedZones: Array<{}>;
        targetZonesOption: string;
        zones: {}; // all zones from selectedWebsites
        checkboxZone: {};
        locations: Array<models.Location>;
        checkboxLocation: {};
        belongLocation: {};
        osTargeting: {};
        variables: Array<models.NetworkVariable>;

        chooseSize(value: string);
        sizes: any;
        search: any;
        isNetworkItem: boolean;
        companionTargetingValues: Array<any>;
        // frequency capping
        freq_impression: number;
        freq_time: number;
        freq_time_string: string;
        chooseFreqTimeUnit(time: string);

        // ----- Media ----
        isExtlnk: boolean;
        extlnkimage: boolean;
        extlnkflash: boolean;

        // ----- Popup ----
        chooseCloseBtnAction(action: string);
        close_btn_action: string;

        // ----- Expandable ----
        iTVCExtension: boolean;
        expand_direction: string;
        expand_display_style: string;
        expand_style: string;
        chooseDirection(direction: string);
        chooseDisplayStyle(style: string);
        chooseStyle(style: string);
        // ---------------------

        // ------- Balloon -------
        tvc_extension: string;
        chooseTVCExtension(kind: string);
        chooseExpandStyle(style: string);
        isExpand: boolean;
        // -------- Tracking --------
        showTrackingLink: boolean;

        categories: models.Category[];
        targeting: {};
        sTargeting: string;
        hours: number;
        minutes: number;
        seconds: number;
        showWarning: boolean;
        remove_target(target: string);
        campaign: models.Campaign;
        formatDateTime(timestamp: number);
        unlimitStartDate: boolean;
        unlimitEndDate: boolean;
        add_variable();
        remove_variable(index: number);
        changeKey(index: number);
        changeValue(index: number);
        strBound: {};
        retargeting: Array<models.NetworkVariable>

// --------- extendData -------------
        xdata: boolean;
        xitems: Array<models.kvItem>;
        addXData();

        //---------- Pr Banner ---------------
        searchCates: {};
        articleCates: Array;
        selectedCates: Array<any>;
        checkBoxCate: {};
        selectCate(cate: any);
        removeCate(id: number)
        cateDic: {};
        option: {targetZone: string}
    }

    export interface ICreateNetworkHtmlItemScope extends common.IItemScope<models.AdItem> {
        sizes: common.ItemType[];
        platforms: common.ItemType[];

        websiteCategories: common.ItemType[];
        websites: models.Website[];
        selectedWebsites: models.Website[];
        zones: models.Zone[]; // all zones from selectedWebsites
        selectedZones: models.Zone[];
        countries: string[];

        //functions
        testTargetUrl();
        test3rdImpressionLink();
    }

    export interface ICreateNetworkTVCItemScope extends common.IItemScope<models.AdItem> {
        checkValidate: boolean;
        sizes: common.ItemType[];
        platforms: common.ItemType[];

        websiteCategories: common.ItemType[];
        websites: models.Website[];
        selectedWebsites: models.Website[];
        zones: models.Zone[]; // all zones from selectedWebsites
        selectedZones: models.Zone[];
        countries: string[];

        //functions
        testTargetUrl();
        test3rdImpressionLink();
    }


    export interface IItemDetailScope extends ng.IScope, common.IPermissionScope {
        orderId: number;
        campaignId: number;
        itemId: number;
        title: string;
        page: any;
        isActiveTab(tabname: string): string;
        isNetworkItem: boolean;
        gotoTab(tabname: string);
        goto(dest: string);
    }

    export interface ILinkedZoneListScope extends common.IListScope<models.BookRecord> {
        orderId: number;
        campaignId: number;
        itemId: number;
        isChosen: boolean;
        chooseItem: models.AdItem;
        chooseZone: models.BookingZone;
        linkItems: Array<models.AdItem>;
        bookedZones: Array<models.BookingZone>;
        getSource(url: string): string;
        check(id: number);
        isMultiChoice: boolean;
        isActiveClass(): string;
        //isBannerImg(filename: string): boolean;
        //isBannerFlash(filename: string): boolean;
        checkBoxes: {};
        //gotoItem(id: number);
        //gotoItemSetting(id: number);
        checkItems: {};
        checkZones: {};
        linkToItem(target: string);
        closeModal(tartet: string);
        isValidZone(zone: models.BookingZone): string;
        isValidItem(item: models.AdItem): string;
        link(target: string);
        unlink();

        contentDic: {};
        sizeDic: {};
        isKind(type: string, kind: string): boolean;
        searchText: string;
        search();
        getKind(kind: string): string;
        view(id: number, name: string, kind: string);

        datepicker: {};
        newBooking();
        gotoEdit(id: number, targetModel: string);
        update(targetModel: string);
        delete();
        editingItem: models.BookRecord;
        zone_available: number;
        formatDateTime(timestamp: number): string;
        formatNumber(num: number): string;
        warningExtend: boolean;
    }

    export class BookingItem {
        id: number;
        zoneid: number;
        start: number;
        end: number;
        share: number;
        percent: number;
        constructor(zoneid: number, start: number, end: number, percent: number) {
            this.id = 0;
            this.zoneid = zoneid;
            this.start = start;
            this.end = end;
            this.percent = percent;
            this.share = 0;
        }
    }

    // --------------------- Booking ------------------------------------
    export interface IUnlinkBookingZoneListScope extends common.IListScope<models.BookingZone> {
        campaignItems: Array<models.AdItem>;
        bookingZoneId: number;
        orderId: number;
        campaignId: number;
        check: {};
        campaignItemDic: {};
        formatDateTime(timestamp: number): string;
        loadExistItem(target: string, zoneId: number);
        addZonesNewItem(zoneId: number);
        closeModal(target: string);
        link(target: string);
        getData();
        isActiveClass(): string;
        isDisableLinkStyle(itemId: number): any;
        isDisable(itemId: number): boolean;
    }

    export interface IBookingCampaignItemListScope extends common.IListScope<models.AdItem> {
        campaignZones: Array<models.BookingZone>;
        bookingItemId: number;
        orderId: number;
        campaignId: number;
        check: {};
        bookingZoneDic: {};
        getFullUrl(relativeUrl: string): string;
        closeModal(target: string);
        book(itemId: number);
        link(target: string);
        isBannerImage(url: string): boolean;
        isBannerFlash(url: string): boolean;
        isDisableLinkStyle(zoneId: number): any;
        isDisable(zoneId: number): boolean;

        getKind(kind: string): string;
        view(id: number, name: string, kind: string);
        previewSource: string;
        previewContent: string;
        previewTitle: string;
        contentDic: {};
        sizeDic: {};
        previewWidth: number;
        previewHeight: number;
    }

    export interface IBookingScope extends ng.IScope, common.IPermissionScope {
        siteId: number;
        sitename: string;
        orderId: number;
        campaign_name: string;
        bookingView: boolean; // if Booking View : bookingView = true, else false
        item: BookingItem;  // zoneid, start, end, percent
        selectSiteName: string;
        selecteditem: any;
        startTimestamp: number;
        endTimestamp: number;
        bookingItems: models.BookRecord[];
        campaigns: models.Campaign[];
        websites: models.Website[];
        data: any;
        datepicker: {};
        click(event: any);
        isChargeView();
        isDisable();
        isDisableDel();
        isDisableViewPlan();
        isDisableViewSchedule();
        isDisableBook();
        bookItem();
        select(zoneId: number, startTime: number, endTime: number);
        delete();
        viewPlan();
        viewBookingSchedule();
        viewBookingPlan();
        createNew();
        addTo(campaignId: number);
        gotoBookingChart();
        setDirectiveFn(directiveFn: any);
        booked();
        choose(website: models.Website);
        getSiteName(siteId: number): string;
        createCampaign();
        init();

        showBookingMenu();
        isShowBookingMenu: boolean;
        isNullSchedule: boolean;

        zone_name: string;
        zone_id: number;
        zone_available: number;
        zone_share: number;
        book_start_timestamp: number;
        book_end_timestamp: number;
        bookingPopupPosition: any;
        isShowBookingPopup: boolean;
        xcoor: number; ycoor: number;
        exdata: any;
        bookZone();
        closeBookingPopup();
        showBookingPopup();
        addBookingFn(directiveFn: any);
        addBook();
        isBookByDrag: boolean;

        bookedItems: Array<models.BookRecord>;
        booking();
        cancel();
        editingItem: models.BookRecord;
        isEditItem: boolean;
        editItem();
        deleteItem();
        updateItem();
        showWarning: boolean;
        startDateCamp: number;
        endDateCamp: number;
        formatDateTime(timestamp: number): string;

        currentOffset: number;
        count: number;
        totalItems: number;
        listSite();
    }

    export interface IConversionListScope extends common.IListScope<models.Conversion> {
        checkBoxes: {};
        isChosen: boolean;
        check(id: number);
        gotoConversion(id: number);
        formatDateTime(timestamp: number);
        formatWindows(windows: number);
        isEditName: {};
        editName(id);
        saveName(index);
        saveByEnter(index: number, event: any);
        viewCode(index: number);
        isCopied: boolean;
        itemPreviewCode: models.Conversion;
        closeViewCode();
        disable();
        orders: {};
        search();
        searchText: string;
    }

    export interface ICreateConversion extends common.IItemScope<models.Conversion> {
        conversionWindows: string;
        windows: string;
        choose(value: string);
        isCustomWindow: boolean;
        checkValidate: boolean;
        isDone: boolean;
        done();
    }
}
