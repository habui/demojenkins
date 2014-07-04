'use strict';
module controllers {
    export class BreadcumNavBar {
        constructor($scope: common.IBreadcumNavBarScope, $state, $stateParams, $location, BreadCumStore: common.BreadCum) {
            $scope.items = new Array();
            BreadCumStore.pushState("main.agency", "agency")
            BreadCumStore.pushState("main.agency.account", "list_acc");
            BreadCumStore.pushState("main.agency.createaccount", "create_acc");
            BreadCumStore.pushState("main.agency.assign_detail.order", "acc_assign_order");
            BreadCumStore.pushState("main.agency.assign_detail.website", "acc_assign_website");
            BreadCumStore.pushState("main.agency.listwebsite", "listwebsite");
            BreadCumStore.pushState("main.agency.assignwebsite", "listwebsite_assign");
            BreadCumStore.pushState("main.agency.listorder", "listorder");
            BreadCumStore.pushState("main.agency.assignorder", "listorder_assign");

            BreadCumStore.tree.push(new common.BreadCumNode("agency", "Agency", -1));
            BreadCumStore.tree.push(new common.BreadCumNode("list_acc", "Account management", "agency"));
            BreadCumStore.tree.push(new common.BreadCumNode("create_acc", "Create new account", "list_acc"));
            BreadCumStore.tree.push(new common.BreadCumNode("acc_assign_order", "Assign for user", "list_acc"));
            BreadCumStore.tree.push(new common.BreadCumNode("acc_assign_website", "Assign for user", "list_acc"));
            BreadCumStore.tree.push(new common.BreadCumNode("listwebsite", "Website management", "agency"));
            BreadCumStore.tree.push(new common.BreadCumNode("listwebsite_assign", "Assign", "listwebsite"));
            BreadCumStore.tree.push(new common.BreadCumNode("listorder", "Order management", "agency"));
            BreadCumStore.tree.push(new common.BreadCumNode("listorder_assign", "Assign", "listorder"));

            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
            $scope.currview = BreadCumStore.getLink($state.current.name);

            $scope.isShow = (): boolean => {
                if ($scope.items.length > 0)
                    return true;
                return false;
            };
            $scope.getActiveClass = (menu: string) => {
                if (menu === $scope.currview)
                    return "active";
                return "";
            };

            $scope.isActive = (menu: string) => {
                if (menu === $scope.currview)
                    return true;
                return false;
            };
            $scope.goto = (dest: string) => {
                switch (dest) {
                    case "agency":
                    case "list_acc":
                        $state.transitionTo("main.agency.account");
                        break;
                    case "listwebsite":
                        $state.transitionTo("main.agency.listwebsite");
                        break;
                    case "listorder":
                        $state.transitionTo("main.agency.listorder");
                        break;
                }
            };
        }
    }
    export class AgencyController extends PermissionController  {
        constructor($scope: scopes.IAgencyScope,
            $state,
            $stateParams,
            $location,
            CurrentTab,
            ActionMenuStore: utils.DataStore,
            factory: backend.Factory, $timeout) {
                super($scope, $state, factory)
                $timeout(() => {
                    if ($scope.checkPermission($scope.permissionDefine.AGENCY) == false) {
                        $scope.gotoDeny();
                    }
                }, 200)
                CurrentTab.setTab(new common.Tab("agency", "account"));
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
                        case "account_manage":
                            $state.transitionTo("main.agency.account");
                            break;
                        case "order_manage":
                            $state.transitionTo("main.agency.listorder");
                            break;
                        case "website_manage":
                            $state.transitionTo("main.agency.listwebsite");
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


    export class AgencyAccountCreateController {
        constructor($scope: common.IItemScope<models.AgencyAccount>,
            $state,
            $stateParams,
            $location,
            factory: backend.Factory,
            CurrentTab,
            BodyClass,
            $timeout: ng.ITimeoutService,
            Page) {

            Page.setTitle("123Click | Agency - Create account");
            $scope.cancel = () => {
                $state.transitionTo('main.agency.account');
            }
                $scope.save = () => {
                $scope.item = new models.AgencyAccount(0, $scope.item.name, $scope.item.email, $scope.item.confirmemail, $scope.item.password);
                factory.agencyAccountService.save($scope.item, function (data) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "agency account"));
                    $timeout(() => $state.transitionTo('main.agency.account'), 1000);
                });
            }
        }
    }

    export class AgencyAccountController {
        constructor($scope: scopes.IAgencyAccountScope,
            $state,
            $stateParams,
            factory: backend.Factory,
            CurrentTab,
            Page,
            ActionMenuStore: utils.DataStore,
            $modal) {
                Page.setTitle("123Click | Agency - Account management");
                CurrentTab.setTab(new common.Tab("agency", "account"));

                //setup action menu
                var action_menus: common.ActionMenu[] = [];
                action_menus.push(new common.ActionMenu("icon-plus", "Create New Account", function () {
                    $state.transitionTo("main.agency.createaccount", []);
                }));
                ActionMenuStore.store("main.agency.account", action_menus);

                $scope.$emit("change_action_menu");

                $scope.totalRecord = 0;
                $scope.pageSize = 10;
                $scope.pageIndex = 1;
                $scope.checkBoxes = {};
                $scope.filterText = "All account";

                var listAgency = () => {
                    factory.agencyAccountService.listAllItem(function (ret) {
                        if (ret !== null && ret !== undefined) {
                            $scope.items = ret;
                            $scope.totalRecord = ret.length;
                        }
                    });
                }
                var listDisableAgency = (start, size) => {
                    factory.agencyAccountService.listDisable(0, start, size, function (ret) {
                        if (ret.data !== null) {
                            $scope.items = ret.data;
                            $scope.totalRecord = ret.total;
                        }
                    });
                }

                listAgency();

                //-----------------Scope function--------------

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

                $scope.goto = (page: string, uid: number) => {
                    switch (page) {
                        case "website":
                            $state.transitionTo("main.agency.assign_detail.website", { userid: uid });
                            break;
                        case "order":
                            $state.transitionTo("main.agency.assign_detail.order", { userid: uid });
                            break;
                    }
                }
                $scope.filter = (type) => {
                    switch (type) {
                        case "all":
                            $scope.filterText = "All account";
                            listAgency();
                            break;
                        case "disable":
                            $scope.filterText = "Disable account";
                            listDisableAgency(0, $scope.pageSize);
                            break;
                    }
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
                                            checkedList.push({ id: $scope.items[i].id, name: $scope.items[i].name });
                                        }
                                    }
                                    return checkedList;
                                },
                                type: () => 'agency'
                            }
                        });

                        modal.result.then(function (checkList) {
                            for (var i = 0; i < checkList.length; i++) {
                                factory.agencyAccountService.remove(checkList[i]["id"], function (result) {
                                    if (result === 'success') {
                                        listAgency();
                                        $scope.checkBoxes = {};
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "disable", "agency account"));
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
                                            checkedList.push({ id: $scope.items[i].id, name: $scope.items[i].name });
                                        }
                                    }
                                    return checkedList;
                                },
                                type: () => 'enable_agency'
                            }
                        });

                        modal.result.then(function (checkList) {
                            for (var i = 0; i < checkList.length; i++) {
                                factory.agencyAccountService.enable(checkList[i]["id"], function (result) {
                                    if (result === 'success') {
                                        listDisableAgency($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
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
                        case "All account":
                            break;
                        case "Disable account":
                            listDisableAgency(start, size);
                            break;
                    }
                }
                $scope.isActiveClass = () => {
                    var isChosen = false;
                    Object.keys($scope.checkBoxes).forEach((key) => {
                        if (key !== 'all' && $scope.checkBoxes[key] === true)
                            isChosen = true;
                    });
                    return isChosen ? "" : "disabled";
                }

                $scope.mouseleave = () => {
                    $scope.currentSelectedRow = -1;
                };
                $scope.mouseover = (row: number) => {
                    $scope.currentSelectedRow = row;
                };
                $scope.edit = (acc) => {
                    var modal = $modal.open({
                        templateUrl: "views/agency/account.edit.modal",
                        controller: controllers.AgencyAccountEditModal,
                        resolve: {
                            item: () => acc
                        }
                    });

                    modal.result.then((item) => {
                        factory.agencyAccountService.update(item, (ret) => {
                            if (ret === "success")
                                notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "agency account"));
                        });
                    });
                }
        }
    }
    export class AgencyAccountEditModal {
        constructor($scope, item, $modalInstance) {
            $scope.item = item;
            $scope.cancel = () => {
                $modalInstance.dismiss('cancel');
            }
            $scope.update = () => {
                $modalInstance.close($scope.item);
            }
        }
    }

    export class AgencyAccountAssignTabController {
        constructor($scope, $state, $stateParams, factory: backend.Factory) {
            $scope.user = {};
            if ($stateParams.userid)
                factory.agencyAccountService.load($stateParams.userid, (ret) => $scope.user = ret);

            $scope.openPage = (page: string) => {
                switch (page) {
                    case "website":
                        $state.transitionTo("main.agency.assign_detail.website", { userid: $scope.user.id });
                        break;
                    case "order":
                        $state.transitionTo("main.agency.assign_detail.order", { userid: $scope.user.id });
                        break;
                }
            }
            $scope.isActiveClass = (page: string) => {
                switch (page) {
                    case "website":
                        return $state.current.name === "main.agency.assign_detail.website" ? "active" : "";
                    case "order":
                        return $state.current.name === "main.agency.assign_detail.order" ? "active" : "";
                }
            }
        }
    }
    export class AgencyAccountOrderController {
        constructor($scope: scopes.IAgencyAccountOrderScope,
            $state,
            $stateParams,
            factory: backend.Factory,
            CurrentTab,
            BodyClass,
            Page) {
                $scope.pageSize = 10
                $scope.pageIndex = 1
                $scope.totalRecord = 0
                $scope.userid = $stateParams.userid;
                var selectedOrder = null
                $scope.roles = []
                $scope.mapRole = {}
                $scope.checkRole = {}

                var listUserRole = (start, count) => {
                    $scope.items = []
                    if ($scope.userid == undefined) return;
                    factory.assignedService.getRoleByUser(start, count, $scope.userid, "order", (ret: backend.ReturnList<models.UserAssigned>) => {
                        if (ret) {
                            ret.data.forEach((urole: any) => {
                                $scope.items.push({id: urole.id, name: urole.name});
                                $scope.mapRole[urole.id] = {};
                                $scope.roles.forEach((role) => $scope.mapRole[urole.id][role.id] = { value: false, visible: false });
                                urole.roles.forEach((role) => {
                                    $scope.mapRole[urole.id][role.id].value = true;
                                });
                                if (urole.ownerId == factory.userInfo.getUserInfo().id) {
                                    $scope.roles.forEach(role => $scope.mapRole[urole.id][role.id]["visible"] = true);
                                }
                                factory.userRoleService.getRoleByObject("order", urole.id, urole1 => {
                                    if (urole1) {
                                        urole1.forEach(r => {
                                            $scope.mapRole[urole.id][r.id]["visible"] = true;
                                        });
                                    }
                                });
                            });
                            $scope.totalRecord = ret.total;
                        }
                    })
                }

                factory.orderService.listAllItem((orders: Array<models.Order>) => {
                    $scope.orders = orders;
                })

                factory.roleService.listByObject("order", (ret) => {
                    if (ret) {
                        $scope.roles = ret;
                        listUserRole($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize)
                    }
                })

                $scope.addOrder = () => {
                    if ($scope.items.filter((it) => it.id === selectedOrder.id).length === 0) {
                        $scope.items.push(selectedOrder);
                        $scope.totalRecord = $scope.items.length;

                        $scope.mapRole[selectedOrder.id] = {};
                        $scope.roles.forEach(role => $scope.mapRole[selectedOrder.id][role.id] = { value: false, visible: false });
                        factory.userRoleService.getRoleByObject("order", selectedOrder.id, ret => {
                            if (ret) {
                                ret.forEach(r => {
                                    $scope.mapRole[selectedOrder.id][r.id] = $scope.mapRole[selectedOrder.id][r.id] || {};
                                    $scope.mapRole[selectedOrder.id][r.id]["visible"] = true;
                                });
                            }
                        });
                        if (selectedOrder.ownerId == factory.userInfo.getUserInfo().id) {
                            $scope.roles.forEach(role => $scope.mapRole[selectedOrder.id][role.id]["visible"] = true);
                        }
                    }
                }

                $scope.cancel = () => {
                    $state.transitionTo("main.agency.account");
                }

                $scope.choose=(order) => {
                    selectedOrder = order;
                }

                $scope.paging = (start, size) => {
                    listUserRole(start, size);
                }

                $scope.checkAllRole = id => {
                    $scope.items.forEach(item => {
                        $scope.mapRole[item.id][id]['value'] = $scope.checkRole[id];
                    })
                }

                $scope.save = () => {
                    var data = [];
                    Object.keys($scope.mapRole).forEach((id) => {
                        var item = { "orderId": parseInt(id) };
                        var roles = [];
                        Object.keys($scope.mapRole[id]).forEach((roleId) => {
                            if ($scope.mapRole[id][roleId].value == true && $scope.mapRole[id][roleId].visible == true)
                                roles.push(parseInt(roleId));
                        });
                        item["roles"] = roles;
                        if (roles.length !== 0)
                            data.push(item);
                    });
                    factory.assignedService.setRoleByUser("order", $scope.userid, JSON.stringify(data), (ret) => {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "user role"));
                        listUserRole($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                    })
                }
        }
    }
    export class AgencyAccountWebsiteController {
        constructor($scope: scopes.IAgencyAccountWebsiteScope,
            $state,
            $stateParams,
            CurrentTab,
            BodyClass,
            Page,
            factory: backend.Factory) {
                $scope.pageSize = 10
                $scope.pageIndex = 1
                $scope.totalRecord = 0
                $scope.userid = $stateParams.userid;
                var selectedSite = null
                $scope.roles = []
                $scope.mapRole = {}
                $scope.checkRole = {}

                var listUserRole = (start, count) => {
                    $scope.items = []
                    if ($scope.userid == undefined) return;
                    factory.assignedService.getRoleByUser(start, count, $scope.userid, "website", (ret: backend.ReturnList<models.UserAssigned>) => {
                        if (ret) {
                            ret.data.forEach((urole:any) => {
                                $scope.items.push({ id: urole.id, name: urole.name });
                                $scope.mapRole[urole.id] = {};
                                $scope.roles.forEach((role) => $scope.mapRole[urole.id][role.id] = { value: false, visible: false });
                                urole.roles.forEach((role) => {
                                    $scope.mapRole[urole.id][role.id].value = true;
                                });
                                if (urole.ownerId == factory.userInfo.getUserInfo().id) {
                                    $scope.roles.forEach(role => $scope.mapRole[urole.id][role.id]["visible"] = true);
                                }
                                factory.userRoleService.getRoleByObject("website", urole.id, urole1 => {
                                    if (urole1) {
                                        urole1.forEach(r => {
                                            $scope.mapRole[urole.id][r.id]["visible"] = true;
                                        });
                                    }
                                });
                            });
                            $scope.totalRecord = ret.total;
                        }
                    })
                }

                factory.websiteService.listAllItem((websites: Array<models.Order>) => {
                    $scope.websites = websites;
                })

                factory.roleService.listByObject("website", (ret) => {
                    if (ret) {
                        $scope.roles = ret.filter((role) => role.name !== "ADMIN" && role.name !== "OWNER" && role.name !== "PR EDITOR")
                        listUserRole($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                    }
                })

                $scope.addSite = () => {
                    if ($scope.items.filter((it) => it.id === selectedSite.id).length === 0) {
                        $scope.items.push(selectedSite);
                        $scope.totalRecord = $scope.items.length;

                        $scope.mapRole[selectedSite.id] = {};
                        $scope.roles.forEach(role => $scope.mapRole[selectedSite.id][role.id] = { value: false, visible: false });
                        factory.userRoleService.getRoleByObject("website", selectedSite.id, ret => {
                            if (ret) {
                                ret.forEach(r => {
                                    $scope.mapRole[selectedSite.id][r.id] = $scope.mapRole[selectedSite.id][r.id] || {};
                                    $scope.mapRole[selectedSite.id][r.id]["visible"] = true;
                                });
                            }
                        });
                        if (selectedSite.ownerId == factory.userInfo.getUserInfo().id) {
                            $scope.roles.forEach(role => $scope.mapRole[selectedSite.id][role.id]["visible"] = true);
                        }
                    }
                }

                $scope.cancel = () => {
                    $state.transitionTo("main.agency.account");
                }

                $scope.choose = (item) => {
                    selectedSite = item;
                }

                $scope.paging = (start, size) => {
                    listUserRole(start, size);
                }

                $scope.checkAllRole = id => {
                    $scope.items.forEach(item => {
                        $scope.mapRole[item.id][id]['value'] = $scope.checkRole[id];
                    })
                }

                $scope.save = () => {
                    var data = [];
                    Object.keys($scope.mapRole).forEach((id) => {
                        var item = { "websiteId": parseInt(id) };
                        var roles = [];
                        Object.keys($scope.mapRole[id]).forEach((roleId) => {
                            if ($scope.mapRole[id][roleId].value == true && $scope.mapRole[id][roleId].visible == true)
                                roles.push(parseInt(roleId));
                        });
                        item["roles"] = roles;
                        if (roles.length !== 0)
                            data.push(item);
                    });
                    factory.assignedService.setRoleByUser("website", $scope.userid, JSON.stringify(data), (ret) => {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "user role"));
                        listUserRole($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                    })
                }

        }
    }

    export class AgencyOrderController {
        constructor($scope: scopes.IAgencyOrderScope,
            $state,
            $stateParams,
            $location,
            factory: backend.Factory,
            CurrentTab,
            BodyClass,
            $modal,
            Page) {
                Page.setTitle("123Click | Agency - Order management");
                CurrentTab.setTab(new common.Tab("agency", "order"));
                $scope.$emit("change_action_menu");

                $scope.totalRecord = 0;
                $scope.pageSize = 10;
                $scope.pageIndex = 1;
                $scope.checkBoxes = {};

                var listOrder = (from, count) => {
                    factory.orderService.list(from, count, (ret: backend.ReturnList<models.Order>) => {
                        if (ret) {
                            $scope.items = ret.data;
                            $scope.totalRecord = ret.total;
                            var ids = [];
                            $scope.items.forEach((item) => ids.push(item.id));
                            if (ids.length === 0) return;
                            $scope.countAssigned = {};
                            factory.agencyAccountService.countAssigned("order", ids, (ret) => {
                                if (ret) {
                                    ret.forEach((a) => {
                                        angular.extend($scope.countAssigned, a|| {});
                                    })
                                }
                            })
                        }
                    }, "name", "asc");
                }

                listOrder($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize)
                

                $scope.popupAssign = () => {
                    if (!$scope.isChosen)
                        return;
                    var modal = $modal.open({
                        templateUrl: "views/agency/modal.assignorder",
                        controller: controllers.ModalAssignOrderController,
                        size: 'lg',
                        resolve: {
                            items: () => {
                                var ret = [];
                                $scope.items.forEach((item) => {
                                    if ($scope.checkBoxes[item.id] == true)
                                        ret.push(item);
                                })
                                return ret;
                            }
                        }
                    });

                    modal.result.then(data => {
                        if (data) {
                            Object.keys(data).forEach(id => {
                                factory.userRoleService.setRole("order", parseInt(id), JSON.stringify(data[id]), () => {
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "assign", "orders"));
                                    listOrder($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize)
                                    $scope.checkBoxes = {};
                                    $scope.isChosen = false;
                                });
                            })
                        }
                    });
                };

                $scope.gotoAssignDetail = (orderid: number) => {
                    $state.transitionTo("main.agency.assignorder", { orderId: orderid });
                };

                $scope.paging = (start: number, size: number) => {
                    listOrder(start, size);
                }

                $scope.check = (id) => {
                    if (id.toString() === 'all') {
                        $scope.items.forEach((item) => {
                            $scope.checkBoxes[item.id] = $scope.checkBoxes["all"] == true;
                        });
                        $scope.isChosen = $scope.checkBoxes['all'] == true;
                    } else {
                        var allCheck = $scope.checkBoxes[id] == true;
                        $scope.items.forEach((item) => {
                            if ($scope.checkBoxes[item.id] !== true) {
                                return allCheck = false;
                            } else $scope.isChosen = true;
                        });
                        $scope.checkBoxes["all"] = allCheck;
                    }
                }

                $scope.isActiveClass = () => {
                    return $scope.isChosen ? "" : "disabled";
                }

                $scope.search = () => {
                    if (!$scope.searchText) return;
                    factory.orderService.search($scope.searchText, 0, false, (ret) => {
                        if (ret) {
                            $scope.items = ret;
                            $scope.totalRecord = ret.length;
                            $scope.checkBoxes = {};
                            $scope.isChosen = false;
                            var ids = [];
                            $scope.items.forEach((item) => ids.push(item.id));
                            if (ids.length === 0) return;
                            $scope.countAssigned = {};
                            factory.agencyAccountService.countAssigned("order", ids, (ret) => {
                                if (ret) {
                                    ret.forEach((a) => {
                                        angular.extend($scope.countAssigned, a || {});
                                    })
                                }
                            })
                        }
                    })
                }
                $scope.$watch("searchText",() => {
                    if ($scope.searchText !== undefined && $scope.searchText.length == 0)
                        listOrder(0, $scope.pageSize);
                })
        }
    }

    export class AgencyWebsiteController  {
        constructor($scope: scopes.IAgencyWebsiteScope,
            $state,
            $stateParams,
            $location,
            factory: backend.Factory,
            CurrentTab,
            BodyClass,
            $modal,
            $timeout,
            Page) {
                Page.setTitle("123Click | Agency - Website management");
                CurrentTab.setTab(new common.Tab("agency", "website"));
                $scope.$emit("change_action_menu");

                $scope.totalRecord = 0;
                $scope.pageSize = 10;
                $scope.pageIndex = 1;
                $scope.checkBoxes = {};

                var listWebsite = (from, count) => {
                    factory.websiteService.list(from, count, (ret: backend.ReturnList<models.Website>) => {
                        if (ret) {
                            $scope.items = ret.data;
                            $scope.totalRecord = ret.total;
                            var ids = [];
                            $scope.items.forEach((item) => ids.push(item.id));
                            if (ids.length === 0) return;
                            $scope.countAssigned = {};
                            factory.agencyAccountService.countAssigned("website", ids, (ret) => {
                                if (ret) {
                                    ret.forEach((a) => {
                                        angular.extend($scope.countAssigned, a || {});
                                    })
                                }
                            });
                            $scope.checkBoxes = {};
                            $scope.isChosen = false;
                        }
                    }, "name", "asc");
                }

                listWebsite($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize)


                $scope.popupAssign = () => {
                    if (!$scope.isChosen)
                        return;
                    var modal = $modal.open({
                        templateUrl: "views/agency/modal.assignwebsite",
                        controller: controllers.ModalAssignWebsiteController,
                        size: 'lg',
                        resolve: {
                            items: () => {
                                var ret = [];
                                $scope.items.forEach((item) => {
                                    if ($scope.checkBoxes[item.id] == true)
                                        ret.push(item);
                                })
                                return ret;
                            }
                        }
                    });

                    modal.result.then(data => {
                        if (data) {
                            Object.keys(data).forEach(id => {
                                factory.userRoleService.setRole("website", parseInt(id), JSON.stringify(data[id]), () => {
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "assign", "websites"));
                                    listWebsite($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize)
                                    $scope.checkBoxes = {};
                                    $scope.isChosen = false;
                                });
                            })
                            $timeout(() => listWebsite($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize), 1000);
                        }
                    });
                };

                $scope.gotoAssignDetail = (id: number) => {
                    $state.transitionTo("main.agency.assignwebsite", { websiteId: id });
                };

                $scope.paging = (start: number, size: number) => {
                    listWebsite(start, size);
                }

                $scope.check = (id) => {
                    if (id.toString() === 'all') {
                        $scope.items.forEach((item) => {
                            $scope.checkBoxes[item.id] = $scope.checkBoxes["all"] == true;
                        });
                        $scope.isChosen = $scope.checkBoxes['all'] == true;
                    } else {
                        var allCheck = $scope.checkBoxes[id] == true;
                        $scope.items.forEach((item) => {
                            if ($scope.checkBoxes[item.id] !== true) {
                                return allCheck = false;
                            } else $scope.isChosen = true;
                        });
                        $scope.checkBoxes["all"] = allCheck;
                    }
                }

                $scope.isActiveClass = () => {
                    return $scope.isChosen ? "" : "disabled";
                }

                $scope.search = () => {
                    if (!$scope.searchText) return;
                    factory.websiteService.search($scope.searchText, 0, false, (ret) => {
                        if (ret) {
                            $scope.items = ret;
                            $scope.totalRecord = ret.length;
                            $scope.checkBoxes = {};
                            $scope.isChosen = false;
                            var ids = [];
                            $scope.items.forEach((item) => ids.push(item.id));
                            if (ids.length === 0) return;
                            $scope.countAssigned = {};
                            factory.agencyAccountService.countAssigned("website", ids, (ret) => {
                                if (ret) {
                                    ret.forEach((a) => {
                                        angular.extend($scope.countAssigned, a || {});
                                    })
                                }
                            })
                        }
                    })
                }
                $scope.$watch("searchText", () => {
                    if ($scope.searchText !== undefined && $scope.searchText.length == 0)
                        listWebsite(0, $scope.pageSize);
                })
        }
    }

    export class ModalAssignOrderController {
        constructor($scope: scopes.IModalAssignOrderScope,
            $modalInstance,
            factory: backend.Factory,
            items) {
                $scope.selectedOrders = items;
                var roleOfObject = null
                $scope.checkRole = {}
                factory.roleService.listByObject("order", (ret) => {
                    if (ret) {
                        $scope.roles = ret;
                        factory.userRoleService.getRolesByObjectIds("order", items.map(i => i.id), res => {
                            if (res) {
                                roleOfObject = res;
                                Object.keys(roleOfObject).forEach(id => {
                                    if (roleOfObject[id].length !== $scope.roles.length)
                                        $scope.showWarning = true;
                                });
                            }
                        });
                    }
                })
                factory.agencyAccountService.listAllItem((ret) => $scope.users = ret)
                $scope.items = []
                $scope.totalRecord = 0
                $scope.pageSize = 10
                $scope.pageIndex = 1
                $scope.mapRole = {}

                $scope.choose = (item) => {
                    if ($scope.items.indexOf(item) !== -1) return;
                    $scope.items.push(item);
                    $scope.totalRecord++;
                    $scope.mapRole[item.id] = {};
                }
                $scope.ok = () => {
                    var data = {};
                    $scope.selectedOrders.forEach(order => {
                        var userRole = [];
                        Object.keys($scope.mapRole).forEach((userId) => {
                            var item = {
                                "userid": parseInt(userId),
                                "roles": []
                            };
                            Object.keys($scope.mapRole[userId]).forEach((roleId) => {
                                if ($scope.mapRole[userId][roleId] == true && ((roleOfObject[order.id] || []).filter(r => r.id == roleId).length !== 0 || order.ownerId == factory.userInfo.getUserInfo().id))
                                    item.roles.push(parseInt(roleId));
                            });
                            userRole.push(item);
                        });
                        data[order.id] = userRole;
                    });
                    $modalInstance.close(data);
                }
                $scope.cancel = () => {
                    $modalInstance.dismiss('message');
                }
                $scope.paging = (from, count) => { }
                $scope.mouseover = (index) => $scope.currentSelectedRow = index
                $scope.mouseleave = () => $scope.currentSelectedRow = -1
                $scope.remove = (index) => {
                    $scope.items.splice(index, 1)
                    $scope.totalRecord --;
                }
                $scope.checkAllRole = id => {
                    $scope.items.forEach(item => {
                        $scope.mapRole[item.id][id] = $scope.checkRole[id];
                    })
                }
        }
    }
    
    export class AgencyOrderAssignController {
        constructor($scope: scopes.IAgencyAssignOrderScope,
            $state,
            $stateParams,
            $location,
            factory: backend.Factory,
            BodyClass,
            CurrentTab,
            Page) {
                Page.setTitle("123Click | Agency - Website management");
                CurrentTab.setTab(new common.Tab("agency", "order"));
                $scope.$emit("change_action_menu");

                $scope.totalRecord = 0
                $scope.pageSize = 10
                $scope.pageIndex = 1
                if ($stateParams.orderId == undefined)
                    return;
                var listUserRole = () => {
                    $scope.mapRole = {}
                    $scope.items = []
                    factory.userRoleService.getRoleByAgency($stateParams.orderId, "order", (ret: backend.ReturnList<models.UserRoleInfo>) => {
                        if (ret) {
                            $scope.totalRecord = ret.total;
                            ret.data.forEach((roleInfo) => {
                                $scope.mapRole[roleInfo.id] = {};
                                roleInfo.roles.forEach((role) => $scope.mapRole[roleInfo.id][role.id] = true);
                                $scope.items.push(new models.UserInfo(roleInfo.id, roleInfo.name));
                            });
                        }
                    });
                }

                listUserRole()
                factory.agencyAccountService.listAllItem((ret) => $scope.users = ret)
                factory.orderService.load($stateParams.orderId, (ret) => {
                    $scope.order = ret;
                    if ($scope.order.ownerId === factory.userInfo.getUserInfo().id) {
                        factory.roleService.listByObject("order", (rolesret) => {
                            if (rolesret) {
                                $scope.roles = rolesret;
                            }
                        });
                    }
                    else {
                        factory.userRoleService.getRoleByObject("order", $stateParams.orderId, (uroles: Array<any>) => {
                            if (uroles) {
                                $scope.roles = uroles;
                            }
                        });
                    }
                })

                $scope.choose = (item) => {
                    if ($scope.items.filter((it)=>it.id === item.id).length !== 0) return;
                    $scope.items.push(item);
                    $scope.totalRecord++;
                    $scope.mapRole[item.id] = {};
                }

                $scope.save = () => {
                    if (!$scope.order || $scope.order.id == 0) return;
                    var data = [];
                    Object.keys($scope.mapRole).forEach((userId) => {
                        var item = {
                            "userid": parseInt(userId),
                            "roles": []
                        };
                        Object.keys($scope.mapRole[userId]).forEach((roleId) => {
                            if ($scope.mapRole[userId][roleId] == true)
                                item.roles.push(parseInt(roleId));
                        });
                        data.push(item);
                    });
                    factory.userRoleService.setRole("order", $scope.order.id, JSON.stringify(data), (ret) => {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "assign", "order"));
                        listUserRole();
                    })
                }
                $scope.cancel = () => {
                    $state.transitionTo("main.agency.listorder");
                }
                $scope.remove = (index) => {
                    if (index >= $scope.items.length) return;
                    $scope.items.splice(index, 1);
                    $scope.totalRecord--;
                }
        }
    }

    export class ModalAssignWebsiteController {
        constructor($scope: scopes.IModalAssignWebsiteScope,
            $modalInstance,
            factory: backend.Factory,
            items) {

                $scope.selectedWebsites = items;
                var roleOfObject = {}
                factory.roleService.listByObject("website", (ret) => {
                    if (ret) {
                        $scope.roles = ret.filter((role) => role.name !== "ADMIN" && role.name !== "OWNER" && role.name !== "PR EDITOR")
                        factory.userRoleService.getRolesByObjectIds("website", items.map(i => i.id), res => {
                            if (res) {
                                roleOfObject = res;
                                Object.keys(roleOfObject).forEach(id => {
                                    if (roleOfObject[id].length !== $scope.roles.length)
                                        $scope.showWarning = true;
                                });
                            }
                        });
                    }
                })
                factory.agencyAccountService.listAllItem((ret) => $scope.users = ret)
                $scope.items = []
                $scope.totalRecord = 0
                $scope.pageSize = 10
                $scope.pageIndex = 1
                $scope.mapRole = {}
                $scope.checkRole = {}

                $scope.choose = (item) => {
                    if ($scope.items.indexOf(item) !== -1) return;
                    $scope.items.push(item);
                    $scope.totalRecord++;
                    $scope.mapRole[item.id] = {};
                }
                $scope.ok = () => {
                    var data = {};
                    $scope.selectedWebsites.forEach(website => {
                        var userRole = [];
                        Object.keys($scope.mapRole).forEach((userId) => {
                            var item = {
                                "userid": parseInt(userId),
                                "roles": []
                            };
                            Object.keys($scope.mapRole[userId]).forEach((roleId) => {
                                if ($scope.mapRole[userId][roleId] == true && ((roleOfObject[website.id] || []).filter(r => r.id == roleId).length !== 0 || website.ownerId == factory.userInfo.getUserInfo().id))
                                    item.roles.push(parseInt(roleId));
                            });
                            userRole.push(item);
                        });
                        data[website.id] = userRole;
                    });
                    $modalInstance.close(data);
                }
                $scope.cancel = () => {
                    $modalInstance.dismiss('message');
                }
                $scope.paging = (from, count) => { }
                $scope.mouseover = (index) => $scope.currentSelectedRow = index
                $scope.mouseleave = () => $scope.currentSelectedRow = -1
                $scope.remove = (index) => {
                    $scope.items.splice(index, 1)
                    $scope.totalRecord--;
                }
                $scope.checkAllRole = id => {
                    $scope.items.forEach(item => {
                        $scope.mapRole[item.id][id] = $scope.checkRole[id];
                    })
                }
        }
    }

    export class AgencyWebsiteAssignController  {
        constructor($scope: scopes.IAgencyAssignWebsiteScope,
            $state,
            $stateParams,
            $location,
            factory: backend.Factory,
            BodyClass,
            CurrentTab,
            Page) {
                Page.setTitle("123Click | Agency - Website management");
                CurrentTab.setTab(new common.Tab("agency", "website"));
                $scope.$emit("change_action_menu");

                $scope.totalRecord = 0
                $scope.pageSize = 10
                $scope.pageIndex = 1
                if ($stateParams.websiteId == undefined)
                    return;
                var listUserRole = () => {
                    $scope.mapRole = {}
                    $scope.items = []
                    factory.userRoleService.getRoleByAgency($stateParams.websiteId, "website", (ret: backend.ReturnList<models.UserRoleInfo>) => {
                        if (ret) {
                            $scope.totalRecord = ret.total;
                            ret.data.forEach((roleInfo) => {
                                $scope.mapRole[roleInfo.id] = {};
                                roleInfo.roles.forEach((role) => $scope.mapRole[roleInfo.id][role.id] = true);
                                $scope.items.push(new models.UserInfo(roleInfo.id, roleInfo.name));
                            });
                        }
                    });
                }

                listUserRole()
                factory.agencyAccountService.listAllItem((ret) => $scope.users = ret)
                factory.websiteService.load($stateParams.websiteId, (ret) => {
                    $scope.website = ret;
                    if ($scope.website.ownerId === factory.userInfo.getUserInfo().id) {
                        factory.roleService.listByObject("website", (rolesret) => {
                            if (rolesret) {
                                $scope.roles = rolesret.filter(r=> r.name !== "ADMIN" && r.name !== "OWNER" && r.name !== "PR EDITOR");
                            }
                        });
                    }
                    else {
                        factory.userRoleService.getRoleByObject("website", $stateParams.websiteId, (uroles: Array<any>) => {
                            if (uroles) {
                                $scope.roles = uroles.filter(r=> r.name !== "ADMIN" && r.name !== "OWNER" && r.name !== "PR EDITOR");;
                            }
                        });
                    }
                })

                $scope.choose = (item) => {
                    if ($scope.items.filter((it) => it.id === item.id).length !== 0) return;
                    $scope.items.push(item);
                    $scope.totalRecord++;
                    $scope.mapRole[item.id] = {};
                }

                $scope.save = () => {
                    if (!$scope.website || $scope.website.id == 0) return;
                    var data = [];
                    Object.keys($scope.mapRole).forEach((userId) => {
                        var item = {
                            "userid": parseInt(userId),
                            "roles": []
                        };
                        Object.keys($scope.mapRole[userId]).forEach((roleId) => {
                            if ($scope.mapRole[userId][roleId] == true)
                                item.roles.push(parseInt(roleId));
                        });
                        data.push(item);
                    });
                    factory.userRoleService.setRole("website", $scope.website.id, JSON.stringify(data), (ret) => {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "assign", "website"));
                        listUserRole();
                    })
                }
                $scope.cancel = () => {
                    $state.transitionTo("main.agency.listwebsite");
                }
                $scope.remove = (index) => {
                    if (index >= $scope.items.length) return;
                    $scope.items.splice(index, 1);
                    $scope.totalRecord--;
                }
        }
    }
    
}