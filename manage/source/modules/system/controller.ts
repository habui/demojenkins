/// <reference path="../../libs/angular/angular.d.ts"/>
/// <reference path="../../libs/moment.d.ts"/>
/// <reference path="../../common/common.ts"/>
/// <reference path="../../libs/bootstrap.d.ts"/>


module controllers {
    'use strict';
    export class SystemController extends PermissionController {
        constructor($scope: scopes.ISystemScope, $state, $stateParams, CurrentTab, ActionMenuStore: utils.DataStore, factory: backend.Factory, $timeout) {
            super($scope, $state, factory)
            $timeout(() => {
                if ($scope.checkPermission($scope.permissionDefine.ROOT) == false) {
                    $scope.gotoDeny();
                    return;
                }
            }, 500);
            CurrentTab.setTab(new common.Tab("system", "config"));
            $scope.isActiveTab = (tabName: string): string => {
                var currTab: common.Tab = CurrentTab.getTab();
                if (currTab === null || currTab === undefined)
                    return "";
                if (tabName === currTab.tabChildName)
                    return "active";
                return "";
            };
            $scope.goTab = (name: string) => {
                switch (name) {
                    case "config":
                        $state.transitionTo("main.system.config");
                        break;
                }
            }
            $scope.$on("change_action_menu", function () {
                var action_menus: common.ActionMenu[] = ActionMenuStore.get($state.current.name);
                if (action_menus != null) {
                    $scope.actionMenus = action_menus;
                }
                else {
                    $scope.actionMenus = [];
                }
            });
            $scope.doIt = (actionName: string) => {
                for (var i: number = 0; i < $scope.actionMenus.length; i++) {
                    if ($scope.actionMenus[i].name === actionName) {
                        $scope.actionMenus[i].action();
                        return;
                    }
                }
            }
        }
    }

    export class SytemBreadcumController {
        constructor() {
            
        }
    }

    export class ConfigSystemController {
        constructor($scope: scopes.IConfigSystemScope, $state,
            $stateParams,
            factory: backend.Factory,
            CurrentTab,
            Page,
            ActionMenuStore: utils.DataStore,
            $modal) {
                Page.setTitle("123Click | System manage");
                CurrentTab.setTab(new common.Tab("system", "config"));

                //setup action menu
                var action_menus: common.ActionMenu[] = [];
                action_menus.push(new common.ActionMenu("icon-plus", "Add config", function () {
                    $scope.edit(null);
                }));
                ActionMenuStore.store("main.system.config", action_menus);

                $scope.$emit("change_action_menu");

                $scope.pageSize = 10;
                $scope.pageIndex = 1;
                $scope.totalRecord = 0;
                $scope.filterText = 'All config'

                var list = () => {
                    factory.systemService.listAllItem((ret) => {
                        if (ret) {
                            $scope.items = ret;
                            $scope.totalRecord = ret.length;
                        }
                    });
                    $scope.checkBoxes = {};
                }
                var listDisable = (start, size) => {
                    factory.systemService.listDisable(0, start, size, (ret) => {
                        if (ret) {
                            $scope.items = ret.data;
                            $scope.totalRecord = ret.total;
                        }
                    });
                    $scope.checkBoxes = {};
                }

                list()
                $scope.edit = (item) => {
                    if ($scope.filterText == "Disable config" && item !== null) return;
                    var modal = $modal.open({
                        templateUrl: "views/system/system.config.modal",
                        controller: controllers.ModalConfigController,
                        resolve: {
                            item: () => item
                        }
                    });

                    modal.result.then((item) => {
                        if (item && item.id === null) {
                            factory.systemService.save(item, (ret) => {
                                if (ret) {
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "save", "system config"));
                                    list()
                                }
                            });
                        } else {
                            factory.systemService.update(item, (ret) => {
                                if (ret === "success") {
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "system config"));
                                    list()
                                }
                            });
                        }
                    });
                }

                $scope.check = (id) => {
                    if (id.toString() === 'all') {
                        $scope.items.forEach((item) => {
                            $scope.checkBoxes[item.id] = $scope.checkBoxes["all"] == true;
                        });
                    } else {
                        var allCheck = $scope.checkBoxes[id] == true;
                        $scope.items.forEach((item) => {
                            if ($scope.checkBoxes[item.id] !== true) {
                                return allCheck = false;
                            }
                        });
                        $scope.checkBoxes["all"] = allCheck;
                    }
                }

                $scope.isActiveClass = () => {
                    var isChosen = false;
                    Object.keys($scope.checkBoxes).forEach(id => {
                        if (id !== 'all' && $scope.checkBoxes[id] == true)
                            return isChosen = true;
                    })
                    return isChosen ? "" : "disabled";
                }

                $scope.disable = () => {
                    var isHave: boolean = false;
                    for (var att in $scope.checkBoxes) {
                        if ($scope.checkBoxes[att] == true) {
                            isHave = true;
                            break;
                        }
                    }
                    if (isHave) {
                        var modal = $modal.open({
                            templateUrl: 'views/common/modal.delete',
                            controller: common.ModalDeleteController,
                            resolve: {
                                checkedList: function () {
                                    var checkedList = [];//list of object contain id and name
                                    for (var i = 0; i < $scope.items.length; i++) {
                                        if ($scope.checkBoxes[$scope.items[i].id] == true) {
                                            checkedList.push({ id: $scope.items[i].id, name: $scope.items[i].key });
                                        }
                                    }
                                    return checkedList;
                                },
                                type: () => 'config'
                            }
                        });

                        modal.result.then(function (checkList) {
                            for (var i = 0; i < checkList.length; i++) {
                                factory.systemService.remove(checkList[i]["id"], function (result) {
                                    if (result === 'success') {
                                        list();
                                        $scope.checkBoxes = {};
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "disable", "system config"));
                                    }
                                });
                            }
                        }, message => { });
                    }
                };
                $scope.enable = () => {
                    var isHave: boolean = false;
                    for (var att in $scope.checkBoxes) {
                        if ($scope.checkBoxes[att] == true) {
                            isHave = true;
                            break;
                        }
                    }
                    if (isHave) {
                        var modal = $modal.open({
                            templateUrl: 'views/common/modal.delete',
                            controller: common.ModalDeleteController,
                            resolve: {
                                checkedList: function () {
                                    var checkedList = [];//list of object contain id and name
                                    for (var i = 0; i < $scope.items.length; i++) {
                                        if ($scope.checkBoxes[$scope.items[i].id] == true) {
                                            checkedList.push({ id: $scope.items[i].id, name: $scope.items[i].key });
                                        }
                                    }
                                    return checkedList;
                                },
                                type: () => 'enable_agency'
                            }
                        });

                        modal.result.then(function (checkList) {
                            for (var i = 0; i < checkList.length; i++) {
                                factory.systemService.enable(checkList[i]["id"], function (result) {
                                    if (result === 'success') {
                                        listDisable($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                                        $scope.checkBoxes = {};
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "enable", "agency account"));
                                    }
                                });
                            }
                        }, message => { });
                    }
                };
                $scope.paging = (start: number, size: number) => {
                    switch ($scope.filterText) {
                        case "All config":
                            break;
                        case "Disable config":
                            listDisable(start, size);
                            break;
                    }
                }

                $scope.filter = (type) => {
                    switch (type) {
                        case "all":
                            $scope.filterText = "All config";
                            list();
                            break;
                        case "disable":
                            $scope.filterText = "Disable config";
                            listDisable(0, $scope.pageSize);
                            break;
                    }
                }
        }
    }
    export class ModalConfigController {
        constructor($scope, $modalInstance, factory: backend.Factory, item) {
            $scope.item = angular.copy(item || new models.ConfigSystem(null, "", "")); 
            $scope.ok = () => {
                $modalInstance.close($scope.item);
            }
            $scope.cancel = () => {
                $modalInstance.dismiss('message');
            }
        }
    }
}
