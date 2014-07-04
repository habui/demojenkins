'use strict';
module scopes {
    export interface IRootScope extends ng.IScope {
        page: any;
        bodyClass: any;
        isLoginPage(): boolean;
        getBodyClass(): string;
    }

    export interface IMainScope extends ng.IScope, common.IPermissionScope {
        username: string;
        goTab(tabname: string);
        isActiveTab(tabname: string);
        logout();
        changePassword();
    }

    export interface IFullSearchScope extends common.IListScope<common.Item>, common.IPermissionScope {
        gotoTab(tabName: string);
        isActiveTab(tabName: string);
        currentTab: string;
        gotoItem(itemKind: string, id: number);
        filterKind: string;
        getKind(kind: string);
        allItems: Array<any>;
        checkBoxes: {};
        isChosen: boolean;
        check(id);
        isActiveClass();
        disable();
        showWarning: boolean;
        messageBody: string;

        //paging item
        limitItem: number;
        startFrom: number;
        checkDisable(start: number, type: string);
        getPageNum();
        getEndIndex();
        next();
        prev();
        chooseLimit(limit: number);
    }
}