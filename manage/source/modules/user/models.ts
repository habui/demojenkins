/// <reference path="../../common/common.ts"/>

module models {
    'use strict';
    export enum UserPermission {
        VIEW = 1,
        VIEW_REPORT = 2,
        POST_ADS = 4,
        CREATE = 8
    }

    export enum EUserRole {
        USER = 1,
        ADMIN = 2,
        ADVERTISER = 4,
        PUBLISHER = 8,
        REPORTER = 16
    }
    

    export class UserInfo extends common.Item {
        name: string;
        email: string;
        password: string;
        assignedWebsiteCount: number;
        assignedOrderCount: number;
        ownerId: number;

        constructor(uid: number, uname: string, email?: string, ownerId?: number) {
            super(uid);
            this.name = uname;
            this.email = email;
            this.ownerId = ownerId;
        }

        addPermission(permission: number) {
            // this.permission = this.permission | permission;
        }

        removePermission(permission: number) {
            // this.permission = this.permission & (~permission);
        }

        isPublisher(): boolean {
            // return ((this.role & EUserRole.PUBLISHER) > 0) ? true : false;
            return false;
        }

        isAdvertiser(): boolean {
            // return ((this.role & EUserRole.ADVERTISER) > 0) ? true : false;
            return true;
        }

        isAdmin():boolean {
            // return ((this.role & EUserRole.ADMIN) > 0) ? true : false;
            return true;
        }

        static checkPermission(permission: number, permissionVal: number): boolean {
            /*
            if ((permission & permissionVal) > 0)
                return true;
            return false;
            */
            return true;
        }
    }

    export class Role {
        name: string;
        description: string;
        static VIEWER = 1 << 1;
        static REPORTER = 1 << 2;
        static SALEMAN = 1 << 3;
        static APPROVAL = 1 << 4;
        static ADMIN = 1 << 5;
        static OWNERWEBSITE = 1 << 6;
        static STANDARDUSER = 1 << 7;
    }

    export class Permission{
        object: string;
        id: number;
        permission: number;

        constructor() {
            
        }

    }

    export class PermissionDefine {
        VIEWWEBSITE: number;
        REPORTWEBSITE: number;
        BOOKING: number;
        APPROVE: number;
        MANAGEUSER: number;
        EDITWEBSITE: number;
        OWNWEBSITE: number;
        PREDITOR: number;
        WEBSITE_ALL_PERMISSION: number;
        WEBSITE_CREATE: number;

        VIEWORDER: number;
        REPORTORDER: number;
        EDITORDER: number;
        ORDER_ALL_PERMISSION: number;
        ROOT: number;
        AGENCY: number;

        constructor() {
            this.VIEWWEBSITE = 1;
            this.REPORTWEBSITE = 1 << 1;
            this.BOOKING = 1 << 2;
            this.APPROVE = 1 << 3;
            this.MANAGEUSER = 1 << 4;
            this.EDITWEBSITE = 1 << 5;
            this.OWNWEBSITE = 1 << 6;
            this.PREDITOR = 1 << 7;
            this.WEBSITE_CREATE = 1 << 8;
            this.WEBSITE_ALL_PERMISSION = (1 << 21) - 1;

            this.VIEWORDER = (1 << 22);
            this.REPORTORDER = (1 << 23);
            this.EDITORDER = (1 << 24);
            this.ORDER_ALL_PERMISSION = ((1 << 22) * (1 << 20)) - this.WEBSITE_ALL_PERMISSION;

            this.AGENCY = 1 << 30;
            this.ROOT = ((1 << 23) * (1<<20)) - 1;
        }
    }
}
