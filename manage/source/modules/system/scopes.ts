/// <reference path="../../common/scopes.ts"/>
/// <reference path="../../common/common.ts"/>
'use strict';
module scopes {
    export interface ISystemScope extends ng.IScope, common.IPermissionScope {
        goTab(tabname: string);
        doIt(actionName: string);
        isActiveTab(tabname: string): string;
        actionMenus: common.ActionMenu[];
    }
    export interface IConfigSystemScope extends common.IListScope<models.ConfigSystem> {
        checkBoxes: {};
        check(id);
        disable(id: number);
        enable(id: number);
        isActiveClass();
        edit(item);
        filterText: string;
        filter(type);
    }
}
