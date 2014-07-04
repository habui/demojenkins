/// <reference path="../../libs/angular/angular.d.ts"/>
/// <reference path="../../libs/moment.d.ts"/>
/// <reference path="../../common/common.ts"/>
module models {
    'use strict'

    export class Category extends common.Item {
        name: string; 
        description: string;
        constructor(id: number, name: string, description: string) {
            super(id);
            this.name = name;
            this.description = description;
        }
    }

    export class UserAssigned extends common.Item {
        ownerId: number;
        name: string;
        roles: models.RoleInfo[];
        constructor(id: number, ownerId: number, name: string, roles: models.RoleInfo[]) {
            super(id);
            this.ownerId = ownerId;
            this.name = name;
            this.roles = roles;
        }
    }

    export class UserLog extends common.Item {
        userId: number;
        userName: string;
        action: string;
        objectId: number;
        objectName: string;
        objectType: string;
        time: number;
        stringTime: string;
        detail: string;

        constructor(id: number, userId: number, userName: string, action: string, objectId: number, objectName: string, objectType: string, time: number, detail: string) {
            super(id);
            this.userId = userId;
            this.userName = userName;
            this.action = action;
            this.objectId = objectId;
            this.objectName = objectName;
            this.objectType = objectType;
            this.time = time;
            this.detail = detail;
            this.stringTime = new Date(time).toLocaleTimeString();
        }
    }
}