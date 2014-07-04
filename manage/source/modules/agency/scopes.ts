'use strict';
module scopes {

    export interface IAgencyScope extends ng.IScope, common.IPermissionScope{
        goTab(tabname: string);
        doIt(actionName: string);
        isActiveTab(tabname: string): string;
        actionMenus: common.ActionMenu[];
    }
    export interface IAgencyAccountScope extends common.IListScope<models.AgencyAccount> {
        filterText: string;
        searchText: string;
        checkBoxes: {};
        check(id);
        filter(type: string);
        getAssignOrder(username: string);
        getAssignWebsite(username: string);
        goto(page: string, uid: number);
        disable(id: number);
        enable(id: number);
        isActiveClass();
        edit(user: models.AgencyAccount);
    }
    export interface IAgencyAccountOrderScope extends common.IListScope<common.Item> {
        userid: number;
        orders: any;
        addOrder();
        save();
        cancel();
        choose(order);
        roles: Array;
        mapRole: {};
        checkRole: {};
        checkAllRole(id);
    }
    export interface IAgencyAccountWebsiteScope extends common.IListScope<common.Item> {
        userid: number;
        websites: any;
        addSite();
        save();
        cancel();
        choose(site);
        roles: Array;
        mapRole: {};
        checkRole: {};
        checkAllRole(id);
    }
    
    export interface IAgencyOrderScope extends common.IListScope<models.Order> {
        searchText: string;
        checkBoxes: any;
        countAssigned: any;
        gotoAssignDetail(orderid: number);
        check(id);
        popupAssign();
        isActiveClass();
        isChosen: boolean;
        search();
    }

    export interface IAgencyAssignOrderScope extends common.IListScope<models.UserInfo> {
        order: any;
        roles: Array;
        users: Array;
        mapRole: {};
        save();
        cancel();
        choose(item);
        remove(index);
    }
    export interface IAgencyAssignWebsiteScope extends common.IListScope<models.UserInfo> {
        website: any;
        roles: Array;
        users: Array;
        mapRole: {};
        save();
        cancel();
        choose(item);
        remove(index);
    }
    export interface IAgencyWebsiteScope extends common.IListScope<models.Website> {
        searchText: string;
        checkBoxes: any;
        countAssigned: any;
        gotoAssignDetail(id: number);
        check(id);
        popupAssign();
        isActiveClass();
        isChosen: boolean;
        search();
    }

    export interface IModalAssignOrderScope extends common.IListScope<models.UserInfo> {
        selectedOrders: Array<models.Order>;
        orders: any;
        roles: Array;
        users: Array;
        mapRole: {};
        ok();
        cancel();
        choose(item);
        remove(index);
        showWarning: boolean;
        checkRole: {};
        checkAllRole(id);
    }

    export interface IModalAssignWebsiteScope extends common.IListScope<models.UserInfo> {
        selectedWebsites: Array<models.Website>;
        orders: any;
        roles: Array;
        users: Array;
        mapRole: {};
        ok();
        cancel();
        choose(item);
        remove(index);
        showWarning: boolean;
        checkRole: {};
        checkAllRole(id);
    }
}