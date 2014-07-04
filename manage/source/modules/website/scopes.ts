/// <reference path="../../common/scopes.ts"/>
/// <reference path="../../common/common.ts"/>
/// <reference path="../website/models.ts"/>

'use strict';
module scopes {
    // -------------- WEBSITE ------------------------
    export interface IWebsiteScope extends common.IListScope<models.Website>, common.IPermissionScope {
        searchField: string;
        search();
        actionMenus: common.ActionMenu[];
        goTab(tabname: string);
        isActiveTab(tabname: string): string;
        doIt(actionName: string);
    }

    export interface IWebsiteCreateScope extends common.IItemScope<models.Website>, IFrequency {
        error_message: string;
        checkValidate: boolean;
        owners: models.UserInfo[];
        ownerName: string;
        networkType: boolean;
        bookingType: boolean;
        tvcType: boolean;
        allowBooking: boolean;
        gotoWebsiteList();
        choose(ownerId: number);
        getOwnerName(ownerId: number): string;
        getAllowBookingClass(): string;
    }
    export interface IWebsiteTestScope extends common.IItemScope<models.Website> {
        name: string;
        num: number;
    }
    export interface IWebsiteSettingScope extends common.IItemScope<models.Website>, IFrequency {
        siteId: number;
        owners: models.UserInfo[];
        networkType: boolean;
        bookingType: boolean;
        tvcType: boolean;
        ownerName: string;
        choose(ownerId: number);
        getOwnerName(ownerId: number): string;
    }

    export interface IWebsiteListScope extends common.IListScope<models.Website> {
        isChosen: boolean;
        checkBoxes: {};
        filterBy: string;
        filterText: string;
        websiteOwnerName: {};
        check(siteid: number);
        goto(siteid: number);
        getOwnerName(ownerid: number): string;
        getApproveTypeName(typeId: number): string;
        gotoSiteSetting(siteId: number);
        goZoneList(siteId: number);
        goZoneGroupList(siteId: number);
        isActiveClass(): string;
        filter(type: string);
        delete();
        search();
        searchText: string;
    }

    export interface IWebsiteDetailScope extends ng.IScope, common.IPermissionScope {
        siteId: number;
        title: string;
        page: any;

        isActiveTab(tabname: string): string;
        gotoTab(tabname: string);
        isPRPublisher: boolean;
    }

    export interface IZoneGroupDetailScope extends ng.IScope, common.IPermissionScope {
        siteId: number;
        zoneGroupId: number;
        title: string;
        isActiveTab(tabname: string): string;
        gotoTab(tabname: string);
    }
    // ------------------- Zone -------------------
    export interface IZoneSettingScope extends ng.IScope, common.IPermissionScope, IFrequency {
        item: any;
        siteId: number;

        isBannerZone: boolean;
        isVideoZone: boolean;
        isPrZone: boolean;

        bookingRunningMode: boolean;
        networkRunningMode: boolean;
        checkValidate: boolean;
        categories: models.Category[];
        selectedCategories: models.Category[];
        checkedMap: any;
        isBooking: () => boolean;
        isNetwork: () => boolean;
        selectCate: (item: models.Category) => void;
        unselectCate: (item: models.Category) => void;
        saveBannerZone: (form: any) => void;
        validateRunningMode(): boolean;
        cancel();
        isTargetChosen(): boolean;

        validateNumber(num: number): boolean;
        validateTvcBannerPosition(): boolean;
        validateTvcBannerRunningMode(): boolean;
        validateTvcPosition(): boolean;
        validateTvcItemType(): boolean;
        // video 
        isBannerType: boolean; isTvcType: boolean; isPauseAdType: boolean;
        bookingmode: boolean; networkmode: boolean;
        bottom_position: boolean; top_position: boolean;
        isPreRoll: boolean; isMidRoll: boolean; isPostRoll: boolean;
        pre_mincpmtype: string; mid_mincpmtype: string; post_mincpmtype: string;
        timeScheduledUnit: string;

        selectCPMType(type: string, option: string);
        selectTimeScheduleUnit(unit: string);

        chooseSize(value: string);
        sizes: any;
        search: any;
        zone_style: string;
        chooseStyle(style: string);

        timeappearances: Array<any>;
        addTimeAppearance();
        removeTimeAppearance(index: number);

    }

    export interface IBannerZoneScope extends common.IItemScope<models.BannerZone>, IFrequency {
        bookingRunningMode: boolean;
        networkRunningMode: boolean;
        checkValidate: boolean;
        categories: models.Category[];
        selectedCategories: models.Category[];
        checkedMap: any;
        isBooking: () => boolean;
        isNetwork: () => boolean;
        selectCate: (item: models.Category) => void;
        unselectCate: (item: models.Category) => void;
        saveBannerZone: (form: any) => void;
        validateRunningMode(): boolean;
        cancel();
        isTargetChosen(): boolean;
        chooseSize(value: string);
        keydown(e);
        chooseAll: boolean;
        zone_style: string;
        chooseStyle(style: string);
        sizes: any;
        search: any;
    }

    export interface IVideoZoneScope extends common.IItemScope<models.VideoZone>, IFrequency {
        checkValidate: boolean;
        isBannerType: boolean;
        isTvcType: boolean;
        isPauseAdType: boolean;

        categories: Array<models.Category>;
        selectedCategories: Array<models.Category>;
        checkedMap: {};

        bookingmode: boolean; networkmode: boolean;
        bottom_position: boolean; top_position: boolean;

        //tvc 
        isPreRoll: boolean;
        isPostRoll: boolean;
        isMidRoll: boolean;

        pre_mincpmtype: string; mid_mincpmtype: string; post_mincpmtype: string;
        selectCPMType(type: string, option: string);
        timeScheduledUnit: string;
        selectTimeScheduleUnit(unit: string);
        validateZoneName(): boolean;
        validateItemType(): boolean;
        validateBannerSize(): boolean;
        validateRunningMode(): boolean;
        validateBannerPosition(): boolean;
        validateTVCPosition(): boolean;
        validateNumber(num: number): boolean;

        selectCate: (category: models.Category) => void;
        unselectCate: (category: models.Category) => void;
        cancel();

        chooseSize(value: string);
        sizes: any;
        search: any;
        timeappearances: Array<any>;
        addTimeAppearance();
        removeTimeAppearance(index: number);
        chooseAll: boolean;
    }

    export interface IZoneDetailScope extends ng.IScope, common.IPermissionScope {
        siteId: number;
        zoneId: number;
        title: string;
        zoneType: string;
        isActiveTab(tabname: string): string;
        gotoTab(tabname: string, type: string);
    }

    export interface ICreateZoneGroupScope extends common.IItemScope<models.ZoneGroup> {


        siteId: number;
        siteName: string;
        zoneGroupId: number;
        error_message: string;
        sitezones: models.Zone[];
        selectedZones: models.Zone[];
        isCreateSuccess: boolean;
        isCreateFailed: boolean;
        isUpdateSuccess: boolean;
        isLoadingDone :boolean ;
        checkedMap: {};

        zoneChoice(id: number, name: string);
        zoneRemove(id: number);

        create(form: any);
        updateSetting(form: any);
        goto(dest: string);
        addNewZone();
    }

    export interface IUserSharedScope extends common.IListScope<models.UserInfo> {
        siteid: number;
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
        hasViewRight(permission: number): boolean;
        hasViewReportRight(permission: number): boolean;
        hasPostAdsRight(permission: number): boolean;
        hasCreateRight(permission: number): boolean;

        currentOffset: number;
        count: number;
        totalItems: number;
    }

    export interface IListZoneGroupScope extends common.IListScope<models.ZoneGroup> {
        siteId: number;
        checkBoxes: {};
        searchText: string;
        isChosen: boolean;
        isActiveClass(): string;
        check(zoneGroupId: number);
        delete();
        search();
        gotoZoneGroup(zoneGroupId: number);
        gotoZoneGroupSetting(zoneGroupId: number);
    }

    export interface IListZoneScope extends common.IListScope<models.Zone> {
        siteId: number;
        checkBoxes: {};
        searchText: string;
        zoneGroupId: number;
        isChosen: boolean;
        filterBy: string;
        filterText: string;
        search();
        delete();
        unlink();
        getZoneGroupName(zoneGroupId: number): string;
        isShowSearchBox: boolean;

        showModel: boolean;
        isShowModal(): boolean;
        check(zoneId: number);
        isActiveClass(): string;
        gotoZone(zoneId: number, kind: string);
        gotoZoneSetting(zoneId: number);
        modaltitle: string;
        modalcontent: string;
        showContentBox(type: string, id: number);
        getZoneTypeClass(type: string): string;
        filter(type: string);

        isBannerZone(links: number): boolean;
        gotoPrZone(zoneId: number);
        isPRPublisher: boolean;

        validateAllZone();
        prCount: {}
    }

    export interface IListLinkedItemScope extends common.IListScope<any> {
        siteId: number;
        zoneId: number;
        checkBoxes: {};
        searchText: string;
        unlink();
        unlinkItem(itemId: number);
        getZoneGroupName(zoneGroupId: number): string;
        gotoZoneSetting(zoneGroupId: number);
        getSource(url: string): string;
        isMultiChoice: boolean; isChosen: boolean;
        gotoItem(id: number);
        isActiveClass(): string;
        check(id: number);
        isBannerImg(filename: string): boolean;
        isBannerFlash(filename: string): boolean;

        getKind(kind: string): string;
        view(id: number, name: string, kind: string);
        previewSource: string;
        previewContent: string;
        previewTitle: string;
        contentDic: {};
        campaignDic: {};
        sizeDic: {};
        previewWidth: number;
        previewHeight: number;
        getCampaignName(campaignId: number): string;
        formatCurrentUsage(currentUsage: number): string;
        positionDic: {};
        isVideoZone: boolean;
    }

    export interface IInventoryScope extends ng.IScope, common.IPermissionScope {
        datepicker: {};
        data: any;
        startTimestamp: number;
        endTimestamp: number;
        view();
        showInventory(): boolean;
    }

    export interface IZoneBookingScheduleScope extends ng.IScope, common.IPermissionScope {
        datepicker: {};
        data: any;
        startTimestamp: number;
        endTimestamp: number;
        view();
    }

    export interface IZoneTypeScope extends ng.IScope, common.IPermissionScope {
        siteId: number;
        option: number;
        goto(dest: string);
    }

    export interface IApprovedAdsScope extends common.IListScope<models.ApprovedAds> {
        check(id: number);
        checkBoxes: {};
        isChosen: boolean;
        selectedFilter: string;
        websites: Array<models.Website>;
        isBannerImg(filename: string): boolean;
        isBannerFlash(filename: string): boolean;
        gotoItem(id: number);
        gotoWebsite(id: number);
        gotoZone(siteId: number, id: number);
        isActiveClass();
        approve();
        reject();
        websiteFilter(websiteId: number);

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

    export interface  IFrequency {
        chooseFC(type: string);
        fcLimitUnit: string;
    }

    //---------------------------------
    export interface IValidateZone {

    }


}