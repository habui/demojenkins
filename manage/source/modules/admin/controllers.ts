/// <reference path="../../libs/angular/angular.d.ts"/>
/// <reference path="../../libs/moment.d.ts"/>
/// <reference path="../../common/common.ts"/>

module controllers {
    'use strict';
    export class AdminController extends PermissionController {
        constructor($scope: scopes.IAdminScope, $location, factory: backend.Factory,
            $state, CurrentTab: common.CurrentTab, BodyClass, ActionMenuStore: utils.DataStore, IDSearch: common.IIDSearch) {
                super($scope, $state, factory);
                factory.permissionUtils.getPermission(utils.PermissionUtils.WEBSITE, 0, function (ret: number) {
                    if (ret !== undefined && ret !== null && (ret & $scope.permissionDefine.ROOT) === 0)
                        $scope.gotoDeny()
                });
                $scope.getPermission(utils.PermissionUtils.WEBSITE, 0);
            // set Body Class
            BodyClass.setClass('');

            //---------------------------- function -----------------------------------
            $scope.gotoTab = (tabName: string) => {
                if (tabName === 'accounts') {
                    $state.transitionTo('main.admin.account_list', { page: 1 });
                    return;
                }
                if (tabName === 'categories') {
                    $state.transitionTo('main.admin.category_list', { page: 1 });
                    return;
                }
                if (tabName === 'user_logs') {
                    $state.transitionTo('main.admin.userlog_list', { page: 1 });
                    return;
                }
                if (tabName === "unique_visitor") {
                    $state.transitionTo('main.admin.unique_user', {});
                    return;
                }
            };

            $scope.isActiveTab = (tabName: string): string => {
                var currTab: common.Tab = CurrentTab.getTab();
                if (currTab === null || currTab === undefined)
                    return "";
                if (tabName === currTab.tabChildName)
                    return "active";
                return "";
            };

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
            $scope.search = () => {
                if ($scope.searchField) {
                    if ($scope.searchField.search(/^([wzoci]|zg)[0-9]+/g) !== -1) {
                        IDSearch.search($scope.searchField, function (value) {
                            if (value) {
                                $location.path(value);
                            }
                        });
                    } else
                        $state.transitionTo("main.website.search", { keywork: $scope.searchField });
                }
            };
        }
    }

    export class AdminBreadCrumbController {
        constructor($scope: scopes.IOrderBreadcumNavScope, $state, $stateParams, $location, factory: backend.Factory) {

            $scope.items = new Array();

            if ($state.current.name === "main.admin.account_list") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Accounts management', "acc_man"));
                $scope.currview = "acc_man";

            } else if ($state.current.name === "main.admin.account_create") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Accounts management', "acc_man"));
                $scope.items.push(new common.MenuItem('Create new account', "acc_create"));
                $scope.currview = "acc_create";
            }
            else if ($state.current.name === "main.admin.category_list") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Categories management', "cat_man"));
                $scope.currview = "cat_man";
            } else if ($state.current.name === "main.admin.category_create") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Categories management', "cat_man"));
                $scope.items.push(new common.MenuItem('Create new category', "cat_create"));
                $scope.currview = "cat_create";
            }
            else if ($state.current.name === "main.admin.userlog_list") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('User activities log', "user_log"));
                $scope.currview = "user_log";
            }
            else if ($state.current.name === "main.admin.assigned.websites") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Accounts management', "acc_man"));
                $scope.items.push(new common.MenuItem('Assigned', "ass_web"));
                $scope.currview = "ass_web";
            }
            else if ($state.current.name === "main.admin.assigned.orders") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Accounts management', "acc_man"));
                $scope.items.push(new common.MenuItem('Assigned', "ass_ord"));
                $scope.currview = "ass_ord";
            }
            else if ($state.current.name === "main.admin.userlog_detail") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('User activities log', "user_log"));
                $scope.items.push(new common.MenuItem('User activities log detail', "log_detail"));
                $scope.currview = "log_detail";
            }
            else if ($state.current.name === "main.admin.unique_user") {
                $scope.items.push(new common.MenuItem("Admin", "admin"));
                $scope.items.push(new common.MenuItem('Unique user', "unique_visitor"));
                $scope.currview = "log_detail";
            }
            $scope.goto = (dest: string) => {
                switch (dest) {
                    case "acc_man":
                        $state.transitionTo("main.admin.account_list", { page: 1 });
                        break;
                    case "cat_man":
                        $state.transitionTo("main.admin.category_list", { page: 1 });
                        break;

                    case "user_log":
                        $state.transitionTo("main.admin.userlog_list");
                        break;
                }
            };
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
        }
    }

    export class AccountListController {
        constructor($state, $stateParams, $location: ng.ILocationService, $scope: scopes.IAccountListScope,
            $log, ActionMenuStore: utils.DataStore, CurrentTab: common.CurrentTab, Page, $modal, factory: backend.Factory) {
            
            // set current tab & title
            CurrentTab.setTab(new common.Tab("admin", "accounts"));
            Page.setTitle('Account management');
            Page.setCurrPage('Account management');
            //scope initialization
            $scope.currentSelectedRow = -1;
            $scope.totalRecord = 0;

            $scope.items = [];
            $scope.searchAccount = "";

            //setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create New Account", function () {
                $state.transitionTo("main.admin.account_create");
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store("main.admin.account_list", action_menus);
            $scope.$emit("change_action_menu");

            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;

            $scope.$watch('searchAccount', function (newVal, oldVal) {
                if ($scope.searchAccount.length > 0)
                    factory.userService.search($scope.searchAccount, 0, false, function (data) {
                        $scope.items = data;
                        $scope.totalRecord = data.length;
                    });
                else
                    listUser();
            });
            function listUser() {
                factory.userService.list(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (returnList) {
                    $scope.items = returnList.data;
                    $scope.totalRecord = returnList.total;
                });
            }
            listUser();

            //scope functions
            $scope.clicked = (row: number) => {
                $scope.currentSelectedRow = row;
            };
            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }

            $scope.createAccount = () => {
                $state.transitionTo('main.admin.account_create');
            };
            $scope.assignedWebsites = (uid: number, uname: string) => {
                $state.transitionTo('main.admin.assigned.websites', { uid: uid, page: 1 });
            };

            $scope.assignedOrders = (uid: number, uname: string) => {
                $state.transitionTo('main.admin.assigned.orders', { uid: uid, page: 1 });
            };
            $scope.paging = (start: number, size: number) => {
                factory.userService.list(start, size, function (returnList) {
                    $scope.items = returnList.data;
                    $scope.totalRecord = returnList.total;
                });

            }

            $scope.setEditItem = (user: models.UserInfo) => {
                var modalInstance = $modal.open({
                    templateUrl: 'views/admin/modal.account.edit',
                    controller: AccountEditModalController,
                    resolve: {
                        item: function () {
                            return user;
                        }
                    }
                });
                modalInstance.result.then(function (item: models.UserInfo) {
                    factory.userService.update(item, function (ret) {
                        if (ret) {
                            factory.userService.list(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (returnList) {
                                $scope.items = returnList.data;
                                $scope.totalRecord = returnList.total;
                            });
                        }
                    });
                }, function (message) {
                        $log.info('modal dismiss b/c ' + message + ' ::at ' + new Date());
                    });
            }
        }
    }

    export class AccountAssignedViewController {
        constructor($state, $stateParams, $location: ng.ILocationService, $scope: scopes.IAccountAssignedViewScope,
            CurrentTab: common.CurrentTab, Page: common.PageUtils, factory: backend.Factory) {
            
            // set current tab & title
            //Page.setTitle('Account management');
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'websites' && $state.current.name === 'main.admin.assigned.websites')
                    return 'active';
                if (tabname === 'orders' && $state.current.name === 'main.admin.assigned.orders')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string) => {
                if (tabname === 'websites') {
                    $state.transitionTo('main.admin.assigned.websites', { uid: $stateParams.uid, page: 1 });
                    return;
                }
                if (tabname === 'orders') {
                    $state.transitionTo('main.admin.assigned.orders', { uid: $stateParams.uid, page: 1 });
                    return;
                }
            };

        }
    }
    export class WebsiteAssignedController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.IWebsiteAssignedScope, CurrentTab: common.CurrentTab, Page, $timeout, factory: backend.Factory) {

            // set current tab & title
                CurrentTab.setTab(new common.Tab("admin", "accounts"));
            Page.setTitle('Assigned websites');
            factory.userService.load($stateParams.uid, function (result) {
                Page.setCurrPage(result.name);
            });

            //scope initialization
            $scope.totalRecord = 0;
            $scope.websiteRoles = [];
            $scope.pageSize = 10;
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.$emit('change_action_menu');

            factory.websiteService.listByReferenceId(0, 100000, factory.userInfo.getUserInfo().id, function (data) {
                if (data !== undefined) {
                    $scope.websites = data.data;
                }
            });

            factory.roleService.listByObject("website", function (data) {
                if (data != null) {
                    $scope.roles = data;
                }
            });
            factory.assignedService.getRoleByUser(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $stateParams.uid, 'website', function (data) {
                if (data != null) {
                    $scope.websiteRoles = (data.data !== undefined && data.data !== null) ? data.data : [];
                    $scope.totalRecord = (data.total !== undefined && data.total !== null) ? data.total : 0;
                }
            });

            $scope.addWebsite = () => {
                for (var u in $scope.websiteRoles) {
                    if ($scope.websiteRoles[u].id === $scope.selectWebsite.id) return;
                }
                $scope.websiteRoles.push(new models.UserAssigned($scope.selectWebsite.id, $scope.selectWebsite.ownerId, $scope.selectWebsite.name, []));
                for (var u in $scope.websites) {
                    if ($scope.websites[u].id === $scope.selectWebsite.id)
                        $scope.websites.splice(u, 1);
                }
                $scope.selectWebsite = null;
            }

            $scope.choose = (web: models.Website) => {
                $scope.selectWebsite = web;
            }

            $scope.hasChecked = (websiteid: number, roleid: number) => {
                for (var u in $scope.websiteRoles) {
                    for (var r in $scope.websiteRoles[u].roles) {
                        if ($scope.websiteRoles[u].id === websiteid && $scope.websiteRoles[u].roles[r].id == roleid)
                            return true;
                    }
                }
                return false;
            }

            $scope.onchange = (websiteid: number, roleid: number) => {
                $scope.hasSave = true;
                for (var u in $scope.websiteRoles) {
                    for (var r in $scope.websiteRoles[u].roles) {
                        if ($scope.websiteRoles[u].id === websiteid && $scope.websiteRoles[u].roles[r].id == roleid) {
                            //remove
                            $scope.websiteRoles[u].roles.splice(r, 1);
                            return;
                        }
                    }
                    if ($scope.websiteRoles[u].id === websiteid) {
                        //push
                        $scope.websiteRoles[u].roles.push(new models.RoleInfo(roleid, null, null, null, null));
                        return;
                    }
                }
                return;
            }

            $scope.save = () => {

                var data = [];
                for (var u in $scope.websiteRoles) {
                    var website = {};
                    var roles = [];
                    var obj = "website";
                    for (var r in $scope.websiteRoles[u].roles) {
                        roles.push($scope.websiteRoles[u].roles[r].id);
                    }
                    website['websiteId'] = $scope.websiteRoles[u].id;
                    website['roles'] = roles;
                    data.push(website);
                }
                factory.assignedService.setRoleByUser(obj, $stateParams.uid, JSON.stringify(data), function (result) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "assigned"));
                    $scope.hasSave = false;
                });
            };
            $state.scope = $scope;

        }
    }
    export class OrderAssignedController {
        constructor($state, $stateParams, $location: ng.ILocationService, $scope: scopes.IOrderAssignedScope,
            CurrentTab: common.CurrentTab, Page, $timeout, factory: backend.Factory) {
                
            // set current tab & title
                CurrentTab.setTab(new common.Tab("admin", "accounts"));
            Page.setTitle('Account manCurrentTab.setTab(new common.Tab("admin", "accounts"));agement');
            factory.userService.load($stateParams.uid, function (result) {
                Page.setCurrPage(result.name);
            });
            //scope initialization
            $scope.totalRecord = 0;
            $scope.orderRoles = [];
            $scope.pageSize = 10;
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.$emit('change_action_menu');

            factory.orderService.listByReferenceId(0, 1000, factory.userInfo.getUserInfo().id, function (data) {
                if (data !== undefined) {
                    $scope.orders = data.data;
                }
            });

            factory.roleService.listByObject("order", function (data) {
                if (data != null) {
                    $scope.roles = data;
                }
            });

            factory.assignedService.getRoleByUser(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $stateParams.uid, 'order', function (data) {
                if (data != null) {
                    $scope.orderRoles = (data.data !== undefined && data.data !== null) ? data.data : [];
                    $scope.totalRecord = (data.total !== undefined && data.total !== null) ? data.total : 0;
                }
            });

            $scope.addOrder = () => {
                for (var u in $scope.orderRoles) {
                    if ($scope.orderRoles[u].id === $scope.selectOrder.id) return;
                }
                $scope.orderRoles.push(new models.UserAssigned($scope.selectOrder.id, $scope.selectOrder.ownerId, $scope.selectOrder.name, []));
                for (var u in $scope.orders) {
                    if ($scope.orders[u].id === $scope.selectOrder.id)
                        $scope.orders.splice(u, 1);
                }
                $scope.selectOrder = null;
            }

            $scope.choose = (order: models.Order) => {
                $scope.selectOrder = order;
            }

            $scope.hasChecked = (orderId: number, roleId: number) => {
                for (var u in $scope.orderRoles) {
                    for (var r in $scope.orderRoles[u].roles) {
                        if ($scope.orderRoles[u].id === orderId && $scope.orderRoles[u].roles[r].id == roleId)
                            return true;
                    }
                }
                return false;
            }

            $scope.onchange = (orderId: number, roleId: number) => {
                $scope.hasSave = true;
                for (var u in $scope.orderRoles) {
                    for (var r in $scope.orderRoles[u].roles) {
                        if ($scope.orderRoles[u].id === orderId && $scope.orderRoles[u].roles[r].id == roleId) {
                            //remove
                            $scope.orderRoles[u].roles.splice(r, 1);
                            return;
                        }
                    }
                    if ($scope.orderRoles[u].id === orderId) {
                        //push
                        $scope.orderRoles[u].roles.push(new models.RoleInfo(roleId, null, null, null, null));
                        return;
                    }
                }
                return;
            }
            $scope.save = () => {
                var data = [];
                for (var u in $scope.orderRoles) {
                    var order = {};
                    var roles = [];
                    var obj = "order";
                    for (var r in $scope.orderRoles[u].roles) {
                        roles.push($scope.orderRoles[u].roles[r].id);
                    }
                    order['orderId'] = $scope.orderRoles[u].id;
                    order['roles'] = roles;
                    data.push(order);
                }
                factory.assignedService.setRoleByUser(obj, $stateParams.uid, JSON.stringify(data), function (result) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "assigned"));
                    $scope.hasSave = false;
                });
            };
            $state.scope = $scope;
        }
    }
    export class AccountCreateController {
        constructor($state, $stateParams, $location: ng.ILocationService, $scope: scopes.IAccountCreateScope,
            CurrentTab: common.CurrentTab, Page: common.PageUtils, $timeout, factory: backend.Factory) {
                
            //set page title 
                CurrentTab.setTab(new common.Tab("admin", "accounts"));
            Page.setCurrPage('Create new account');
            //scope initialization
            $scope.name = ''; $scope.email = ''; $scope.password = ''; $scope.confirmPassword = '';
            $scope.createCode = - 10;
            $scope.listRoles = [{ id: 1, name: 'Admin' },
                { id: 2, name: 'Advertiser' },
                { id: 3, name: 'Publisher' },
                { id: 4, name: 'Reporter' }];
            $scope.selectedRoles = [];

            //scope funtion
            $scope.cancel = () => {
                $state.transitionTo('main.admin.account_list', { page: 1 });
            };
            $scope.checkPassword = () => {
                if ($scope.password !== $scope.confirmPassword)
                    return true;
                return false;
            };

            $scope.isInvalid = (): boolean => {
                if ($scope.accCreate.$valid && $scope.password === $scope.confirmPassword)
                    return false;
                return true;
            }

            $scope.save = () => {
                factory.userService.signup($scope.name, $scope.email, $scope.password, function (response) {
                    if (response !== null) {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "account"));
                        $timeout(function () {
                            $state.transitionTo('main.admin.account_list', { page: 1 });
                        }, 1000);
                    } else {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "account"));
                    }
                });

            };
        }
    }

    export class CategoryListController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.ICategoryListScope,
            CurrentTab: common.CurrentTab,
            Page: common.PageUtils, $log,
            $modal,
            ActionMenuStore: utils.DataStore, factory: backend.Factory) {

            // set current tab & title
            CurrentTab.setTab(new common.Tab("admin", "categories"));
            Page.setTitle('Category management');
            Page.setCurrPage('Category management');
            //scopes initializations
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;

            //setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create New Category", function () {
                $state.transitionTo("main.admin.category_create");
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store("main.admin.category_list", action_menus);
            $scope.$emit("change_action_menu");



            factory.categoryService.list(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (ret) {
                if (ret) {
                    $scope.items = ret.data;
                    $scope.totalRecord = ret.total;
                }
            });

            $scope.paging = (start: number, size: number) => {
                factory.categoryService.list(start, size, function (ret) {
                    if (ret) {
                        $scope.items = ret.data;
                        $scope.totalRecord = ret.total;
                    }
                });
            }

            //scope functions 
            $scope.createCategory = () => {
                $state.transitionTo('main.admin.category_create');
            }
                $scope.editItem = (id: number) => {
            }
                $scope.deleteItem = (id: number) => {
            }

            $scope.setEditItem = (item: models.Category) => {
                $scope.itemId = item.id;
                var modalInstance = $modal.open({
                    templateUrl: 'views/admin/modal.category.edit',
                    controller: CategoryDeleteModalController,
                    resolve: {
                        item: function () {
                            return new models.Category(item.id, item.name, item.description);
                        }
                    }
                });
                modalInstance.result.then(function (item: models.Category) {
                    factory.categoryService.update(item, function (ret) {
                        if (ret) {
                            factory.categoryService.list(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (remain) {
                                if (remain) {
                                    $scope.items = remain.data;
                                    $scope.totalRecord = remain.total;
                                }
                            });

                        }
                    });
                }, function (message) {
                        $log.info('modal dismiss b/c ' + message + ' ::at ' + new Date());
                    });
            }

            $scope.setDeleteItem = (item: any) => {
                $scope.itemId = item.id;
                var modalInstance = $modal.open({
                    templateUrl: 'views/admin/modal.category.delete',
                    controller: CategoryDeleteModalController,
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                modalInstance.result.then(function (item: any) {
                    var itemId: number = item.id;
                    factory.categoryService.remove(itemId, function (ret) {
                        if (ret) {
                            //go to current page
                            //$state.transitionTo($state.current.name, { page: $scope.pageIndex });
                            factory.categoryService.list(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (remain) {
                                if (remain) {
                                    $scope.items = remain.data;
                                    $scope.totalRecord = remain.total;
                                }
                            });

                        }
                    });
                }, function (message) {
                        $log.info('modal dismiss b/c ' + message + ' ::at ' + new Date());
                    });

            }

        }
    }

    export class AccountEditModalController {
        constructor($scope,
            $modalInstance, item,
            factory: backend.Factory) {
            $scope.item = item;
            $scope.ok = function () {
                $modalInstance.close(item);
            };
            $scope.item.password = '';
            $scope.item.confirmPassword = '';

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            $scope.checkPassword = () => {
                if ($scope.item.password !== $scope.item.confirmPassword)
                    return true;
                return false;
            }
        }
    }

    export class AccountPasswordModalController {
        constructor($scope, $modalInstance, name, factory: backend.Factory) {
            $scope.item = {};
            $scope.item.name = name;
            $scope.ok = function () {
                if ($scope.item.password.length === 0 || $scope.item.new_password.length === 0 || $scope.item.new_password !== $scope.item.confirm_password)
                    return;

                $modalInstance.close($scope.item);
            };
            $scope.item.password = '';
            $scope.item.new_password = '';
            $scope.item.confirm_password = '';

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            $scope.checkPassword = () => {
                if ($scope.new_password !== $scope.confirm_password)
                    return true;
                return false;
            }
        }
    }

    export class CategoryDeleteModalController {
        constructor($scope,
            $modalInstance, item,
            factory: backend.Factory) {
            $scope.item = item;
            $scope.ok = function () {
                $modalInstance.close(item);
            };

            $scope.err_msg = '';
            $scope.password = '1234';
            $scope.confirmPassword = '';

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            $scope.checkPassword = () => {
                $scope.err_msg = ($scope.password !== $scope.confirmPassword) ? "Passwords don't match!" : "";
            }
        }
    }
    export class CategoryCreateController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.ICategoryCreateScope, CurrentTab: common.CurrentTab, Page: common.PageUtils,
            $timeout, factory: backend.Factory) {

            // set page title
                CurrentTab.setTab(new common.Tab("admin", "categories"));
            Page.setCurrPage('Create new category');

            //scope initialization
            $scope.createCode = - 10;
            $scope.isValidated = false;
            //scope funtion
            $scope.cancel = () => {
                $state.transitionTo('main.admin.category_list', { page: 1 });
            }
            $scope.save = () => {
                if ($scope.cateCreate.$valid) {
                    factory.categoryService.save($scope.item, function (ret) {
                        if (ret) {
                            $state.transitionTo('main.admin.category_list', { page: 1 });
                        }
                    });
                }
            }
        }
        validate(form: any, $scope: scopes.ICategoryCreateScope) {

        }
    }

    export class UserlogListController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.IUserlogListScope, ActionMenuStore: utils.DataStore,
            CurrentTab: common.CurrentTab, Page, factory: backend.Factory) {
            
            // set current tab & title
            CurrentTab.setTab(new common.Tab("admin", "user_logs"));
            Page.setTitle('User logs');
            Page.setCurrPage('User activities log');
            var pageIndex: number = $stateParams.page;

            //setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create New Account", function () {
                $state.transitionTo("main.admin.account_create");
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store("main.admin.userlog_list", action_menus);
            $scope.$emit("change_action_menu");

            $scope.logDetail = (objId: number, objType: string) => {
                $state.transitionTo("main.admin.userlog_detail", { objId: objId, objType: (objType === 'Item') ? 'Banner' : objType });
            }

            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;

            $scope.account = new models.UserInfo(-1, "All User");
            $scope.action = "";
            $scope.objType = "";

            var today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
            var currentTimeRange = new models.TimeRange(today.getTime(), new Date().getTime());

            $scope.check = {};
            $scope.datepicker = { "startDate": new Date(), "endDate": new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 1), "startCompareDate": new Date(), "endCompareDate": new Date() };
            $scope.check['today'] = false; $scope.check['lastweek'] = false; $scope.check['lastmonth'] = false;
            $scope.check['alltime'] = true; $scope.check['custom'] = false; $scope.check['isCompare'] = false;
            $scope.check['compare'] = '';

            $scope.currentTimeOption = 'alltime';
            $scope.dateFrom = '';
            $scope.dateTo = '';
            $scope.key = '';
            $scope.timeOption = 'All Time';
            $scope.isOpenMenu = false;

            // ---------------------------------- function -----------------------------------
            $scope.$watch('datepicker.startDate', function (newVal, oldVal) {
                if ($scope.check['custom'])
                    $scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
            });

            $scope.$watch('datepicker.endDate', function (newVal, oldVal) {
                if ($scope.check['custom'])
                    $scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
            });

            $scope.toggleOpen = () => {
                $scope.isOpenMenu = !$scope.isOpenMenu;
            };

            $scope.getOpenClass = (): string => {
                if ($scope.isOpenMenu)
                    return "open";
                return "";
            };

            $scope.click = (e) => {
                if (!jQuery(e.target).is("#query"))
                    e.stopPropagation();
            };

            $scope.chose = (option: string, e) => {
                if (option === 'account')
                    $scope.account = e;
                else if (option === 'action')
                    $scope.action = e;
                else if (option === 'objType')
                    $scope.objType = e;
                else {
                    e.stopPropagation();
                    if (option === 'datepicker')
                        return;
                    if ($scope.currentTimeOption.length > 0)
                        $scope.check[$scope.currentTimeOption] = false;
                    $scope.check[option] = true;
                    if (option === 'today')
                        $scope.timeOption = 'Today';
                    else if (option === 'lastweek')
                        $scope.timeOption = 'Last Week';
                    else if (option === 'lastmonth')
                        $scope.timeOption = 'Last Month';
                    else if (option === 'alltime')
                        $scope.timeOption = 'All Time';
                    else if (option === 'custom')
                        $scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
                    $scope.currentTimeOption = option;
                    return;
                }
                $scope.pageIndex = 1;
            };

            $scope.query = () => {
                //get range
                $scope.isOpenMenu = false;
                $scope.range = new models.TimeRange(-1, -1); // all time
                if ($scope.check['today']) {
                    var today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
                    $scope.range = new models.TimeRange(today.getTime(), new Date().getTime());
                } else if ($scope.check['lastweek']) {
                    $scope.range = utils.DateUtils.getLastWeek();
                } else if ($scope.check['lastmonth']) {
                    $scope.range = utils.DateUtils.getLastMonth();
                } else if ($scope.check['custom']) {
                    $scope.range = new models.TimeRange($scope.datepicker['startDate'].getTime() + new Date().getTimezoneOffset() * 60000, $scope.datepicker['endDate'].getTime() + new Date().getTimezoneOffset() * 60000);
                }
                $scope.pageIndex = 1;
                factory.userLogService.advancedSearch(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.key, $scope.account.id, -1, ($scope.objType === 'Item') ? 'Banner' : $scope.objType, $scope.action, $scope.range.from, $scope.range.to, (data) => {
                    if (data !== undefined && data !== null) {
                        for (var i in data.data) {
                            if (data.data[i].time !== undefined && data.data[i].time !== null)
                                data.data[i].stringTime = new Date(data.data[i].time).toLocaleString("en-GB");
                        }
                        $scope.listLog = data.data;
                        $scope.totalRecord = data.total;
                    }
                });
            }

            factory.userService.list(0, 10 * 1000, function (list: backend.ReturnList<models.UserInfo>) {
                $scope.accounts = [new models.UserInfo(-1, "All User")].concat(list.data);
            });

            $scope.query();

            $scope.paging = (start: number, size: number) => {
                factory.userLogService.advancedSearch(start, size, $scope.key, $scope.account.id, -1, ($scope.objType === 'Item') ? 'Banner' : $scope.objType, $scope.action, $scope.range.from, $scope.range.to, (data) => {
                    if (data !== undefined && data !== null) {
                        for (var i in data.data) {
                            if (data.data[i].time !== undefined && data.data[i].time !== null)
                                data.data[i].stringTime = new Date(data.data[i].time).toLocaleString("en-GB");
                        }
                        $scope.listLog = data.data;
                        $scope.totalRecord = data.total;
                    }
                });
            };

            $scope.mouseover = function(index) {
                $scope.currentSelectedRow = index;
            };
            $scope.mouseleave = function() {
                $scope.currentSelectedRow = -1;
            }

            var compareObject = (val1, val2): boolean => {
                if ((val1 == undefined && val2 != undefined) || (val1 != undefined && val2 == undefined))
                    return false;
                else if (val1.toString() != val2.toString())
                    return false;
                return true;
            }

            $scope.detailLog = function (logId: number) {
                $scope.logParsed = { old: "", new: "" };
                factory.userLogService.detailLog(logId, function(resp) {
                    if (!!resp) {
                        var oldVal = JSON.parse(unescape(resp.oldVal));
                        var newVal = JSON.parse(unescape(resp.newVal));
                        if (oldVal != null && newVal != null) {
                            Object.keys(oldVal).forEach((key) => {
                                if (key != "extra" && !compareObject(oldVal[key], newVal[key])) {
                                    oldVal[key] = "<span class='hightlight'>" + unescape(JSON.stringify(oldVal[key])) + "</span>";
                                    newVal[key] = "<span class='hightlight'>" + unescape(JSON.stringify(newVal[key])) + "</span>";
                                } else if (key == "extra" && oldVal[key] != null && newVal[key] != null) {
                                    var oldValExtra = JSON.parse(unescape(oldVal[key]));
                                    var newValExtra = JSON.parse(unescape(newVal[key]));
                                    Object.keys(oldValExtra).forEach((keyExtra) => {
                                        if (!compareObject(oldValExtra[keyExtra], newValExtra[keyExtra])) {
                                            oldValExtra[keyExtra] = "<span class='hightlight'>" + JSON.stringify(oldValExtra[keyExtra], undefined, 2) + "</span>";
                                            newValExtra[keyExtra] = "<span class='hightlight'>" + JSON.stringify(newValExtra[keyExtra], undefined, 2) + "</span>";
                                        }
                                    });
                                    oldVal[key] = oldValExtra
                                newVal[key] = newValExtra
                            }
                            });
                        } else {
                            oldVal = unescapeObj(oldVal)
                            newVal = unescapeObj(newVal)
                        }
                        $scope.logParsed.old = (JSON.stringify(oldVal, undefined, 2) || "").replace(/\\n/g, "").replace(/\\"/g, '"') + "</span>";
                        $scope.logParsed.new = (JSON.stringify(newVal, undefined, 2) || "").replace(/\\n/g, "").replace(/\\"/g, '"') + "</span>";
                        fixPreviewPosition("#detailLogModal");
                        $("#detailLogModal").modal("show");
                    }
                });

                function unescapeObj(obj) {
                    var newObj = {};
                    if (obj == null) return;
                    if (typeof obj === "object") {
                        Object.keys(obj).forEach(function(key) {
                            newObj[key] = key === "extra" ? JSON.parse(obj[key]) : unescape(obj[key]);
                        });
                    }
                    return newObj;
                }
            }
        }
    }

    export class UserlogDetailController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.IUserlogDetailScope, ActionMenuStore: utils.DataStore,
            CurrentTab: common.CurrentTab, Page, factory: backend.Factory) {

            CurrentTab.setTab(new common.Tab("admin", "user_logs"));
            Page.setTitle('User log detail');
            Page.setCurrPage('User activities log detail');
            var pageIndex: number = 1;

            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;
            $scope.datepicker = { startDate: new Date(), endDate: new Date() };
            $scope.timeOption = "All time";
                $scope.account = new models.UserInfo(-1, "All user");
            $scope.check = { alltime: true };

            factory.userLogService.advancedSearch(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, "", -1, $stateParams.objId, $stateParams.objType, '', -1, -1, function (data) {
                if (data !== undefined && data !== null) {
                    for (var i in data.data) {
                        if (data.data[i].time !== undefined && data.data[i].time !== null)
                            data.data[i].stringTime = new Date(data.data[i].time).toLocaleString("en-GB");
                    }
                    $scope.listLog = data.data;
                    $scope.totalRecord = data.total;
                    Page.setCurrPage($scope.listLog[0].objectName);
                }
            });
            factory.userService.list(0, 1000, function(userRet: backend.ReturnList<models.UserInfo>) {
                $scope.accounts = ([new models.UserInfo(-1, "All user")]).concat(userRet.data);
            });

            $scope.paging = (start: number, size: number) => {
                factory.userLogService.advancedSearch(start, size, "", -1, $stateParams.objId, $stateParams.objType, '', -1, -1, function (data) {
                    if (data !== undefined && data !== null) {
                        for (var i in data.data) {
                            if (data.data[i].time !== undefined && data.data[i].time !== null)
                                data.data[i].stringTime = new Date(data.data[i].time).toLocaleString("en-GB");
                        }
                        $scope.listLog = data.data;
                        $scope.totalRecord = data.total;
                    }
                });
            };

                $scope.mouseover = function (index) {
                    $scope.currentSelectedRow = index;
                };
                $scope.mouseleave = function () {
                    $scope.currentSelectedRow = -1;
                }

                var compareObject = (val1, val2): boolean => {
                    if ((val1 == undefined && val2 != undefined) || (val1 != undefined && val2 == undefined))
                        return false;
                    else if (val1.toString() != val2.toString())
                        return false;
                    return true;
                }
                $scope.detailLog = function (logId: number) {
                    $scope.logParsed = { old: "", new: "" };
                    factory.userLogService.detailLog(logId, function (resp) {
                        if (!!resp) {
                            var oldVal = JSON.parse(unescape(resp.oldVal));
                            var newVal = JSON.parse(unescape(resp.newVal));
                            if (oldVal != null && newVal != null) {
                                Object.keys(oldVal).forEach((key) => {
                                    if (key != "extra" && !compareObject(oldVal[key], newVal[key])) {
                                        oldVal[key] = "<span class='hightlight'>" + unescape(JSON.stringify(oldVal[key])) + "</span>";
                                        newVal[key] = "<span class='hightlight'>" + unescape(JSON.stringify(newVal[key])) + "</span>";
                                    } else if (key == "extra" && oldVal[key] != null && newVal[key] != null) {
                                        var oldValExtra = JSON.parse(unescape(oldVal[key]));
                                        var newValExtra = JSON.parse(unescape(newVal[key]));
                                        Object.keys(oldValExtra).forEach((keyExtra) => {
                                            if (!compareObject(oldValExtra[keyExtra], newValExtra[keyExtra])) {
                                                oldValExtra[keyExtra] = "<span class='hightlight'>" + JSON.stringify(oldValExtra[keyExtra], undefined, 2) + "</span>";
                                                newValExtra[keyExtra] = "<span class='hightlight'>" + JSON.stringify(newValExtra[keyExtra], undefined, 2) + "</span>";
                                            }
                                        });
                                        oldVal[key] = oldValExtra;
                                        newVal[key] = newValExtra;
                                    }
                                });
                            } else {
                                oldVal = unescapeObj(oldVal);
                                newVal = unescapeObj(newVal);
                            }
                            $scope.logParsed.old = (JSON.stringify(oldVal, undefined, 2) || "").replace(/\\n/g, "").replace(/\\"/g, '"') + "</span>";
                            $scope.logParsed.new = (JSON.stringify(newVal, undefined, 2) || "").replace(/\\n/g, "").replace(/\\"/g, '"') + "</span>";
                            fixPreviewPosition("#detailLogModal");
                            $("#detailLogModal").modal("show");
                        }
                    });

                    function unescapeObj(obj) {
                        var newObj = {};
                        if (obj == null) return;
                        if (typeof obj === "object") {
                            Object.keys(obj).forEach(function (key) {
                                newObj[key] = key === "extra" ? JSON.parse(obj[key]) : unescape(obj[key]);
                            });
                        }
                        return newObj;
                    }
                }
                $scope.toggleOpen = function() {
                    $scope.isOpenMenu = !$scope.isOpenMenu;
                }
                $scope.getOpenClass = function() {
                    return $scope.isOpenMenu ? "open" : "";
                }
            $scope.click = (e) => {
                    if (!jQuery(e.target).is("#query"))
                        e.stopPropagation();
            };
            $scope.query = function () {
                $scope.isOpenMenu = false;
                $scope.range = new models.TimeRange(-1, -1);
                
                if ($scope.check["today"] === true) {
                    $scope.range = utils.DateUtils.getToday();
                }
                else if ($scope.check["lastweek"] == true) {
                    $scope.range = utils.DateUtils.getLastWeek();
                } else if($scope.check["lastmonth"] == true) {
                    $scope.range = utils.DateUtils.getLastMonth();
                }
                else if ($scope.check["custom"] == true) {
                    $scope.range = new models.TimeRange($scope.datepicker['startDate'].getTime() + new Date().getTimezoneOffset() * 60000, $scope.datepicker['endDate'].getTime() + new Date().getTimezoneOffset() * 60000);
                }
                $scope.pageIndex = 1;
                factory.userLogService.advancedSearch(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, "", $scope.account.id, $stateParams.objId, $stateParams.objType, "", $scope.range.from, $scope.range.to, (data) => {
                    if (data !== undefined && data !== null) {
                        for (var i in data.data) {
                            if (data.data[i].time !== undefined && data.data[i].time !== null)
                                data.data[i].stringTime = new Date(data.data[i].time).toLocaleString("en-GB");
                        }
                        $scope.listLog = data.data;
                        $scope.totalRecord = data.total;
                    }
                });
            }
            $scope.chose = function (type, value) {
                switch (type) {
                    case "today":
                        $scope.timeOption = "Today";
                        break;
                    case "lastweek":
                        $scope.timeOption = "Last week";
                        break;
                    case "lastmonth":
                        $scope.timeOption = "Last month";
                        break;
                    case "alltime":
                        $scope.timeOption = "All time";
                        break;
                    case "datepicker":
                        type = "custom";
                    case "custom":
                        $scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
                        break;
                    case "account":
                        $scope.account = value;
                        return;
                }
                $scope.check = {};
                $scope.check[type] = true;
                value.stopPropagation();
            }
        }
    }

    export class UniqueUserController {
        constructor($scope: scopes.IUniqueUserScope, $state, ActionMenuStore: utils.DataStore, CurrentTab: common.CurrentTab, Page, factory: backend.Factory) {
            CurrentTab.setTab(new common.Tab("admin", "unique_visitor"));
            Page.setTitle('Unique User');
            //setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Run unique visitor", function () {
                $state.transitionTo("main.admin.unique_user.run");
            }, $scope.permissionDefine.ROOT));
            action_menus.push(new common.ActionMenu("icon-plus", "Stop unique visitor", function () {
                $state.transitionTo("main.admin.unique_user.stop");
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store("main.admin.unique_user", action_menus);
            ActionMenuStore.store("main.admin.unique_user.run", action_menus);
            ActionMenuStore.store("main.admin.unique_user.stop", action_menus);
            $scope.$emit("change_action_menu");

            $scope.$on("refresh_status", function() {
                $scope.process = -1;
                factory.uniqueUserService.getStatus(function (resp) {
                    if (resp) {
                        $scope.process = resp;
                        if ($scope.process < 0)
                            $state.transitionTo("main.admin.unique_user.run", {})
                    }
                });
            })

            $scope.$emit("refresh_status");
        }
    }

    export class UniqueUserRunController {
        constructor($state, $stateParams, $scope: scopes.IUniqueUserScopeRun, $timeout,
        CurrentTab: common.CurrentTab, Page, factory: backend.Factory) {
            // set current tab & title
            CurrentTab.setTab(new common.Tab("admin", "unique_visitor"));
            Page.setTitle('Unique User');
            $scope.$emit("change_action_menu");
            $scope.fromDate = new Date();
            $scope.filterSite = [];
            $scope.apiKey = "";
            $scope.email = ""
            var totalSite;
            var listAllSite = function(from: number, count: number) {
                factory.websiteService.list(from, count, function(ret: backend.ReturnList<models.Website>) {
                    if (ret.data) {
                        $scope.websites = $scope.websites.concat(ret.data);
                        totalSite = ret.total;
                        if (from + count < totalSite)
                            listAllSite(from + count, count);
                        else {
                            $scope.websites.sort((a, b) => {
                                return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
                            });
                        }
                    }
                });
            }
            var totalOrder = 0;
            var listAllOrder = (from: number, count: number) => {
                factory.orderService.list(from, count, function (ret: backend.ReturnList<models.Order>) {
                    if (ret.data) {
                        $scope.orders = $scope.orders.concat(ret.data);
                        totalOrder = ret.total;
                        if (from + count < totalOrder)
                            listAllOrder(from + count, count);
                        else {
                            $scope.orders.sort((a, b) => {
                                return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
                            });
                        }
                    }
                });
            }
            $scope.websites = [];
            $scope.orders = [];
            $scope.campaignByOrder = {};
            listAllSite(0, 100);
            listAllOrder(0, 100);
            $scope.checkboxWebsite = {};
            $scope.checkboxCampaign = {};
            $scope.checkboxOrder = {};
            $scope.expandOrder = {};
            var campaignOfOrder = {};
            var checkedCampaignOfOrder = {};

            $scope.checkWebsite = (siteId) => {
                if ($scope.checkboxWebsite[siteId] === true) {
                    $scope.filterSite.push(siteId)
                } else {
                    var i = $scope.filterSite.lastIndexOf(siteId.toString());
                    $scope.filterSite.splice(i, 1);
                }
            }

            $scope.$watch("times", function (newVal, oldVal) {
                if (newVal && newVal.search(/([^0-9,]+)|(^,)|,{2,}/g) !== -1)
                    $scope.times = oldVal;
            })


            $scope.checkOrder = (id: number) => {
                if ($scope.campaignByOrder[id]) {
                    $scope.campaignByOrder[id].forEach((camp) => {
                        $scope.checkboxCampaign[camp.id] = $scope.checkboxOrder[id];
                    });
                    checkedCampaignOfOrder[id] = $scope.campaignByOrder[id].length;
                } else {
                    listCampaignByOrder(id);
                    $scope.expandOrder[id] = true;
                    $scope.checkboxOrder[id] = false;
                }
            }

            $scope.checkCampaign = (camp: models.Campaign) => {
                if ($scope.checkboxCampaign[camp.id] === true)
                    checkedCampaignOfOrder[camp.orderId] = (checkedCampaignOfOrder[camp.orderId] || 0) + 1;
                else {
                    checkedCampaignOfOrder[camp.orderId] = (checkedCampaignOfOrder[camp.orderId] || 0) - 1;
                }
                $scope.checkboxOrder[camp.orderId] = checkedCampaignOfOrder[camp.orderId] === campaignOfOrder[camp.orderId];
            }

            $scope.run = () => {
                if (!$scope.apiKey || !$scope.duration || !$scope.times || !$scope.email)
                    return;
                $scope.times.replace(/,$/g, "");
                var filterCampaign: Array<string> = Object.keys($scope.checkboxCampaign).filter((campId) => $scope.checkboxCampaign[campId] === true);
                var from = new Date($scope.fromDate.getFullYear(), $scope.fromDate.getMonth(), $scope.fromDate.getDate()).getTime();
                factory.uniqueUserService.run($scope.apiKey, from, $scope.duration, $scope.times,
                    $scope.filterSite, filterCampaign, $scope.email, function(res) {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, res, ""));
                        $state.transitionTo("main.admin.unique_user", {});
                        $scope.$emit("refresh_status");
                });
            }

            function listCampaignByOrder(orderId) {
                factory.campaignService.listByOrderId(orderId, 0, 1000, (ret: backend.ReturnList<models.Campaign>) => {
                    if (ret) {
                        $scope.campaignByOrder[orderId] = ret.data;
                        campaignOfOrder[orderId] = ret.total;
                    }
                });
            }
        }
    }
    
    export class UniqueUserStopController {
        constructor($scope: scopes.IUniqueUserStopScope, $state, $timeout, factory: backend.Factory) {
            $scope.$emit("change_action_menu");
            $scope.stop = () => {
                if (!$scope.apiKey) return;
                factory.uniqueUserService.stop($scope.apiKey, function (res) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, res, ""));
                    $state.transitionTo("main.admin.unique_user", {});
                    $scope.$emit("refresh_status");
                })
            }
        }
    }
}
