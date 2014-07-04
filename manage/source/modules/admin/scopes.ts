/// <reference path="../../common/scopes.ts"/>
/// <reference path="../../common/common.ts"/>

module scopes {
    'use strict'

    export interface IAdminBreadcumScope extends common.IMenuScope {
        items: common.MenuItem[];
        isActive(menu: string);
        isShow(): boolean;
    }

    export interface IAdminScope extends common.IListScope<models.Order>, common.IPermissionScope {
        gotoTab(tabname: string);
        isActiveTab(tabname: string): string;
        actionMenus: common.ActionMenu[];
        doIt(actionName: string);
        searchField: string;
        search();
    }

    export interface IAccountListScope extends common.IListScope<models.UserInfo> {
        searchAccount: string;
        createAccount();
        currentSelectedRow: number;
        clicked(id: number);
        setEditItem(user: models.UserInfo);
        assignedWebsites(uid: number, uname : string );
        assignedOrders(uid: number, uname : string );
    }
    export interface IAccountAssignedViewScope extends common.IItemScope<models.UserInfo> {

        isActiveTab(tabname: string): string;
        gotoTab(tabname: string);
        goto(dest: string);
    }
    export interface ICategoryListScope extends common.IListScope<common.Item>{
        itemId: number;
        createCategory();
        editItem(id: number);
        deleteItem(id: number);
        setEditItem(item: models.Category);
        setDeleteItem(id: number);
        actionMenus: common.ActionMenu[];
    }

    export interface ICategoryCreateScope extends common.IItemScope<models.Category> {
        cateCreate: any;
        isValidated: boolean;
        createCode: number;
    }
    export interface IUserlogListScope extends common.IListScope<common.Item> {
        check: {};
        datepicker: {};
        currentTimeOption: string;
        dateFrom: string;
        dateTo: string;
        timeOption: string;
        toggleOpen();
        getOpenClass(): string;
        isOpenMenu: boolean;
        range: models.TimeRange;
        chose(type: string, e);
        click(e);
        query();

        key: string;
        account: models.UserInfo;
        action: string;
        objType: string;

        accounts: models.UserInfo[];
        listLog: models.UserLog[];
        logDetail(objId: number, objType: string);

        logParsed: {old: string; new: string}
        detailLog(logId: number);
    }
    export interface IUserlogDetailScope extends common.IListScope<common.Item> {
        obj: models.BookingItem
        listLog: models.UserLog[];
        logParsed: { old: string; new: string }
        detailLog(logId: number);
        accounts: models.UserInfo[];
        datepicker;
        timeOption: string;
        toggleOpen();
        getOpenClass(): string;
        isOpenMenu: boolean;
        account: models.UserInfo;
        check: {};
        click(event);
        query();
        range: models.TimeRange;
        chose(type, value);
    }

    export interface IAccountCreateScope extends common.IItemScope<models.UserInfo> {
        accCreate: any;
        name: string;
        email: string;
        password: string;
        confirmPassword: string;
        listRoles: any[];
        selectedRoles: any[];
        checkPassword(): boolean;
        isInvalid(): boolean;

        createCode: number;//0- success, -1 : failed
    }

    export interface IWebsiteAssignedScope extends common.IItemScope < models.UserInfo >  {
        pageSize: number;
        pageIndex: number;
        roles: models.RoleInfo[];
        websiteRoles: models.UserAssigned[];
        totalRecord: number;
        hasSave: boolean;
        websites: models.Website[];
        selectWebsite: models.Website;
        choose(web: models.Website);
        addWebsite();
        hasChecked(websiteId: number, role: number)
        onchange(websiteId: number, role: number);
        checkedList: Object;
        save();
    }
    export interface IOrderAssignedScope extends common.IItemScope<models.UserInfo>{
        pageSize: number;
        pageIndex: number;
        roles: models.RoleInfo[];
        orderRoles: models.UserAssigned[];
        totalRecord: number;
        hasSave: boolean;
        orders: models.Order[];
        selectOrder: models.Order;
        choose(web: models.Order);
        addOrder();
        hasChecked(orderId: number, role: number)
        onchange(orderId: number, role: number);
        checkedList: Object;
        save();
    }
    export interface IUniqueUserScope extends ng.IScope, common.IPermissionScope {
        process: number;
    }

    export interface IUniqueUserScopeRun extends ng.IScope {
        apiKey: string;
        from: number;
        duration: number;
        times: string;
        filterSite: Array<number>;
        email: string;
        websites: Array<models.Website>;
        fromDate: Date;
        checkboxWebsite: {};
        checkWebsite(siteId);
        orders: Array<models.Order>;
        campaignByOrder: {};
        checkboxOrder: {};
        checkboxCampaign: {};
        checkOrder(id: number);
        checkCampaign(campaign: models.Campaign);
        searchOrder: string;
        expandOrder: {};
        run();
    }
    export interface IUniqueUserStopScope extends ng.IScope {
        apiKey: string;
        stop();
    }
}
    
        
        