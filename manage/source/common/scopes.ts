/// <reference path="../libs/angular/angular.d.ts"/>

'use strict';

module common {
    export interface IMenuScope extends ng.IScope {
        currview: string;
        goto(menu: string);
        getActiveClass(menu: string);
    }

    export interface INaviBarScope extends IMenuScope {
        username: string;
        logout();
    }

    export class MenuItem {
        name: string;
        link: string;
        constructor(name: string, link: string) {
            this.name = name;
            this.link = link;
        }
    }

    export interface IBreadcumNavBarScope extends IMenuScope {
        items: MenuItem[];
        siteid: number;
        zonegroupid: number;
        zoneid: number;
        isActive(menu: string);
        isShow();
    }

    export interface ILoginScope extends ng.IScope {
        message: string;
        login(uname: string, pass: string);
        logout(sessionid: string);
    }
    // Class
    export interface IItemScope<T extends common.Item> extends ng.IScope, IPermissionScope {
        item: T;
        extra: {};
        validateFailed: boolean;
        update(): void;
        save();
        cancel();
        hasSave: boolean;
    }


    export interface IListScope<T extends common.Item> extends ng.IScope, IPermissionScope {
        pageIndex: number;
        pageSize: number;
        totalRecord: number;
        items: Array<T>;
        remove: (id: number) => void;
        extra: {};

        sortField: {};
        getSortClass(type: string): string;
        switchSort(type: string);
        currentSortCol: string;
        currentSortType: string;

        currentSelectedRow: number;
        mouseover(row: number);
        mouseleave();
        getData();
        paging(start: number, size: number);

        itemsWarning: Array<{}>;
    }

    export interface IPermissionScope {
        getPermission(object: string, id: number);
        getPermissions(object: string, ids: number[], permissionIndex: number);
        message: string;
        currentPermission: number;
        checkPermission(permissionIndex, showMessage?: boolean): boolean;
        itemsPermission: {};
        checkDenyAccess(object: string, id: number);
        permissionDefine: models.PermissionDefine;
        gotoDeny();
        gotoNotFound();
    }
    export class ActionMessage {
        static SUCCESS: number = 0;
        static FAIL: number = 1;
        static WARN: number = 2;
        static INFO: number = 3;

        messageType: string;
        title: string;
        body: string;
        
        constructor(messageId: number, action: string, item?: string) {
            switch (messageId) {
                case 0:
                    this.messageType = "success";
                    this.body = "You successfully " + action + " " + item;
                    break;
                case 1:
                    this.messageType = "error";
                    this.body = "You failed to " + action + " " + item;
                    break;
                case 2:
                    this.messageType = "warn";
                    this.body = action;
                    break;
                case 3:
                    this.messageType = "info";
                    this.body = action;
                    break;
            }
        }
    }
}