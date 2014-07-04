/// <reference path="../../common/basecontroller.ts"/>
/// <reference path="../../common/scopes.ts"/>
/// <reference path="../backend/data.ts"/>
/// <reference path="../user/models.ts"/>
/// <reference path="../website/models.ts"/>
/// <reference path="scopes.ts"/>
/// <reference path="../user/scopes.ts"/>

module controllers {

    'use strict';
    export class WebsiteBreadcumNavBarController {
        constructor($scope: common.IBreadcumNavBarScope, $state, $stateParams,
            factory: backend.Factory,
            BreadCumStore: common.BreadCum) {

                BreadCumStore.pushState("main.website.list", "list_site");
                BreadCumStore.pushState("main.website.detail.zonegroup", "detail_site");
                BreadCumStore.pushState("main.website.detail.setting", "detail_site");
                BreadCumStore.pushState("main.website.detail.zone", "detail_site");
                BreadCumStore.pushState("main.website.detail.shared", "detail_site");
                BreadCumStore.pushState("main.website.detail.inventory", "detail_site");
                BreadCumStore.pushState("main.website.zonegroup_detail.zone", "detail_zonegroup");
                BreadCumStore.pushState("main.website.zonegroup_detail.setting", "detail_zonegroup");
                BreadCumStore.pushState("main.website.zonegroup_detail.przones", "detail_zonegroup");
                BreadCumStore.pushState("main.website.zonegroup_create", "create_zonegroup");
                BreadCumStore.pushState("main.website.zone_type", "zone_type");
                BreadCumStore.pushState("main.website.banner_zone_create", "banner_zone_create");
                BreadCumStore.pushState("main.website.video_zone_create", "video_zone_create");
                BreadCumStore.pushState("main.website.pr_zone_create", "pr_zone_create");
                BreadCumStore.pushState("main.website.zone_detail.setting", "detail_zone");
                BreadCumStore.pushState("main.website.zone_detail.linked_items", "detail_zone");
                BreadCumStore.pushState("main.website.zone_detail.articles", "detail_zone");
                BreadCumStore.pushState("main.website.zone_detail.booking", "detail_zone");
                BreadCumStore.pushState("main.website.article_setting", "detail_article");

                BreadCumStore.tree.push(new common.BreadCumNode("list_site", "All Websites", -1));
                BreadCumStore.tree.push(new common.BreadCumNode("detail_site", "", "list_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("detail_zonegroup", "", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("create_zonegroup", "Create ZoneGroup", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("zone_type", "Zone Types", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("banner_zone_create", "Create Banner Zone", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("video_zone_create", "Create Video Zone", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("pr_zone_create", "Create PR Zone", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("detail_zone", "", "detail_site"));
                BreadCumStore.tree.push(new common.BreadCumNode("detail_article", "", "detail_zone"));

                $scope.items = new Array();
                if ($stateParams.websiteId === undefined || $stateParams.websiteId === null) {
                    $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                    $scope.currview = BreadCumStore.getLink($state.current.name);
                }

                if ($stateParams.websiteId !== undefined && $stateParams.websiteId !== null) {
                    factory.websiteService.load($stateParams.websiteId, function (website: models.Website) {
                        if (website !== undefined && website.id > 0)
                            BreadCumStore.tree.setNameNode("detail_site", "Website (" + website.name + ")");

                        if (!$stateParams.zoneGroupId && !$stateParams.zoneId) {
                            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                            $scope.currview = BreadCumStore.getLink($state.current.name);
                            return;
                        }
                        if ($stateParams.zoneGroupId !== undefined) {
                            factory.zoneGroupService.load($stateParams.zoneGroupId, function (zoneGroup: models.ZoneGroup) {
                                if (zoneGroup !== undefined && zoneGroup.id > 0)
                                    BreadCumStore.tree.setNameNode("detail_zonegroup", "ZoneGroup (" + zoneGroup.name + ")");

                                if ($stateParams.zoneId === undefined || $stateParams.zoneId === null) {
                                    $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                    $scope.currview = BreadCumStore.getLink($state.current.name);
                                    return;
                                }

                                factory.zoneService.load($stateParams.zoneId, function (zone: models.Zone) {
                                    if (zone !== undefined && zone.id > 0)
                                        BreadCumStore.tree.setNameNode("detail_zone", "Zone (" + zone.name + ")");

                                    $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                    $scope.currview = BreadCumStore.getLink($state.current.name);

                                    if ($stateParams.articleId) {
                                        factory.articleService.load($stateParams.articleId, function (article: models.Article) {
                                            if (article !== undefined && article !== null)
                                                BreadCumStore.tree.setNameNode("detail_article", "Article (" + article.name + ")");

                                            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                            $scope.currview = BreadCumStore.getLink($state.current.name);
                                        })
                                    }
                                });
                            });
                        } else if ($stateParams.zoneId !== undefined) {
                            factory.zoneService.load($stateParams.zoneId, function (zoneRet: models.Zone) {
                                if (zoneRet && zoneRet.id > 0)
                                    BreadCumStore.tree.setNameNode("detail_zone", "Zone (" + zoneRet.name + ")");
                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);

                                if ($stateParams.articleId) {
                                    factory.articleService.load($stateParams.articleId, function(article: models.Article) {
                                        if (article !== undefined && article !== null)
                                            BreadCumStore.tree.setNameNode("detail_article", "Article (" + article.name + ")");

                                        $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                        $scope.currview = BreadCumStore.getLink($state.current.name);
                                    });
                                }
                            });
                        }
                    });
                }

            $scope.goto = (dest: string) => {
                switch (dest) {
                    case "list_site":
                        $state.transitionTo("main.website.list", { page: 1 });
                        break;
                    case "detail_site":
                        $state.transitionTo("main.website.detail.zone", { websiteId: $stateParams.websiteId, page: 1 });
                        break;
                    case "detail_zonegroup":
                        $state.transitionTo("main.website.zonegroup_detail.zone", { websiteId: $stateParams.websiteId, zoneGroupId: $stateParams.zoneGroupId, page: 1 });
                        break;
                    case "detail_zone":
                        $state.transitionTo("main.website.zone_detail.articles", { websiteId: $stateParams.websiteId, zoneId: $stateParams.zoneId, type: "pr" });
                        break;
                }
            };

            $scope.isShow = (): boolean => {
                if ($scope.items !== undefined && $scope.items.length > 0)
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


    export class WebsiteController extends PermissionController{
        constructor($scope: scopes.IWebsiteScope, $state, $location,
            CurrentTab: common.CurrentTab, BodyClass, ActionMenuStore: utils.DataStore, 
            factory: backend.Factory, IDSearch: common.IIDSearch) {
            super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            // set Body Class
            BodyClass.setClass('');
            $scope.actionMenus = [];
            $scope.searchField = "";
            // receive change action menu notification
            $scope.$on('change_action_menu', function (event, args: string[]) {                    
                var action_menus: common.ActionMenu[] = ActionMenuStore.get($state.current.name);
                if (action_menus !== null && args && args[0] && !isNaN(parseInt(args[0]))) {
                    factory.permissionUtils.getPermission(utils.PermissionUtils.WEBSITE, parseInt(args[0]), function (ret) {
                        if(ret) $scope.currentPermission = ret;
                    });
                    $scope.actionMenus = action_menus;
                }
                else
                    $scope.actionMenus = [];
            });


            // function
            $scope.goTab = (tabName: string) => {
                if (tabName === 'list_sites') {
                    $state.transitionTo('main.website.list', { page: 1 });
                    return;
                }
                if (tabName === 'create_site') {
                    $state.transitionTo('main.website.create');
                    return;
                }
                if (tabName === 'approve_ads') {
                    $state.transitionTo('main.website.approveAds');
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

            $scope.doIt = (actionName: string) => {
                for (var i: number = 0; i < $scope.actionMenus.length; i++) {
                    if ($scope.actionMenus[i].name === actionName) {
                        $scope.actionMenus[i].action();
                        return;
                    }
                }
            };

            $scope.search = () => {
                if ($scope.searchField) {
                    if ($scope.searchField.search(/^([wzoci]|zg)[0-9]+/g) !== -1) {
                        IDSearch.search($scope.searchField, function (value) {
                            if (value) {
                                $location.path(value);
                            }
                        });
                    }else
                        $state.transitionTo("main.website.search", {keywork: $scope.searchField});
                }
            };
        }
    }
    // website
    export class WebsiteListController extends PermissionController{
        constructor($scope: scopes.IWebsiteListScope, $state, $stateParams,
            CurrentTab: common.CurrentTab, Page: common.PageUtils, $modal, factory: backend.Factory) {

            super($scope, $state, factory);
            // init 
            $scope.sortField = { ownerId: common.SortDefinition.DEFAULT };
            $scope.sortField['name'] = common.SortDefinition.DEFAULT;
            $scope.sortField['reviewType'] = common.SortDefinition.DEFAULT;

            //set current tab & title
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            Page.setTitle('Website List | 123Click');
            // notify parent controller
            $scope.$emit('change_action_menu');
            //scope initialization

            $scope.currentSelectedRow = -1;
            var pageIndex: number = 1;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;

            $scope.items = [];
            $scope.totalRecord = 0;
            $scope.checkBoxes = {};
            $scope.checkBoxes['all'] = false;
            $scope.websiteOwnerName = {};
            $scope.isChosen = false;
            $scope.filterBy = "a";

            listwebsite(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);

            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            };

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            };

            var currentSortField = "", currentSortType = "";
            //Function
            function listwebsite(from: number, count: number, filterBy: string, orderCol?: string, orderType?: string) {
                factory.websiteService.listByFilter(from, count, filterBy, function (ret) {
                    if (ret.data !== null) {
                        $scope.items = ret.data;
                        var ids: Array<number> = [];
                        var wIds: number[] = [];
                        for (var i: number = 0; i < $scope.items.length; i++) {
                            if ($scope.items[i].reviewType)
                                $scope.items[i].reviewTypeArr = $scope.items[i].reviewType.split(',');
                            var id: number = $scope.items[i].ownerId;
                            if (ids.indexOf(id) < 0)
                                ids.push(id);
                            wIds.push($scope.items[i].id);
                        }

                        $scope.getPermissions(utils.PermissionUtils.WEBSITE, wIds, $scope.permissionDefine.EDITWEBSITE);

                        $scope.totalRecord = ret.total;
                        factory.userService.listByIds(ids, function (data) {
                            for (var i: number = 0; i < data.length; i++) {
                                var user: models.UserInfo = data[i];
                                if (user === null || user === undefined)
                                    continue;
                                $scope.websiteOwnerName[user.id] = user.name;
                            }
                        });
                        $scope.checkBoxes = {};
                        $scope.isChosen = false;
                    }
                }, orderCol, orderType);
                currentSortField = orderCol;
                currentSortType = orderType;
            }
            $scope.filterText = "All website";
            // ---------------- Scope function ------------------
            $scope.$watch("searchText", function (newVal, oldVal) {
                if (oldVal && oldVal.length != 0 && newVal === "") {
                    listwebsite(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                }
            });
            $scope.filter = (type: string) => {
                switch (type) {
                    case 'all':
                        $scope.filterBy = 'a';
                        $scope.filterText = "All website";
                        break;
                    case 'completed':
                        $scope.filterBy = "completed";
                        $scope.filterText = "Completed websites";
                        break;
                    case 'uncompleted':
                        $scope.filterBy = "uncompleted";
                        $scope.filterText = "Uncompleted websites";
                        break;
                    case 'disable':
                        $scope.filterBy = "disable";
                        $scope.filterText = "Disable websites";
                        break;
                }
                $scope.pageIndex = 1
                listwebsite($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, $scope.filterBy);;
            }

            $scope.getSortClass = (type: string): string => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            };

            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    listwebsite(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                    listwebsite(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    listwebsite(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type);
                }
                for (var aType in $scope.sortField)
                    if (type != aType)
                        $scope.sortField[aType] = common.SortDefinition.DEFAULT;
            };

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.check = (siteid: number) => {
                if ($scope.checkBoxes[siteid]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            };
            $scope.goto = (siteid: number) => {
                $state.transitionTo('main.website.detail.zone', { websiteId: siteid });
            };

            $scope.getOwnerName = (ownerid: number) => {
                var name: string = $scope.websiteOwnerName[ownerid];
                if (name !== null && name !== undefined)
                    return name;
                return "----";
            };

            $scope.gotoSiteSetting = (siteId: number) => {
                $state.transitionTo('main.website.detail.setting', { websiteId: siteId });
            };

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.paging = (start: number, size: number) => {
                listwebsite(start, size, $scope.filterBy, currentSortField, currentSortType);
            }

            $scope.delete = () => {
                //check there have selected Item
                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }
                if (isHave) {
                    var webModal = $modal.open({
                        templateUrl: 'views/common/modal.delete',
                        controller: common.ModalDeleteController,
                        resolve: {
                            checkedList: function () {
                                var checkedList = [];
                                for (var att in $scope.checkBoxes) {
                                    if ($scope.checkBoxes[att] == true) {
                                        for (var i = 0; i < $scope.items.length; i++) {
                                            if ($scope.items[i].id == att) {
                                                checkedList.push({ id: att, name: $scope.items[i].name });
                                            }
                                        }
                                    }
                                }
                                return checkedList;
                            },
                            type: function () {
                                if ($scope.filterBy !== 'disable')
                                    return 'website';
                                return 'enable_website';
                            }
                        }
                    });
                    webModal.result.then(
                        function (checkedList) {
                            var siteIds: number[] = [];
                            var isUpdate: boolean = false;
                            $scope.itemsWarning = [];
                            for (var i: number = 0; i < checkedList.length; i++) {
                                var siteid: number = checkedList[i].id;
                                if ($scope.filterBy !== 'disable') {
                                    factory.websiteService.remove(siteid, function (data) {
                                        listwebsite($scope.pageSize * ($scope.pageIndex - 1),
                                            $scope.pageSize, "");
                                        $scope.checkBoxes = {};
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "disable", "website"));
                                    }, function (msg, status) {
                                    });
                                } else {
                                    factory.websiteService.enable(siteid, function (data) {
                                        listwebsite($scope.pageSize * ($scope.pageIndex - 1),
                                            $scope.pageSize, "disable");
                                        $scope.checkBoxes = {};
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "enable", "website"));
                                    }, function (msg, status) {
                                    });
                                }
                            }
                        },
                        function (message) {
                        });
                }

            };

            $scope.goZoneList = (siteId: number) => {
                $state.transitionTo('main.website.detail.zone', { websiteId: siteId });
            };

            $scope.goZoneGroupList = (siteId: number) => {
                $state.transitionTo('main.website.detail.zonegroup', { websiteId: siteId });
            };

            $scope.search = () => {
                factory.websiteService.search($scope.searchText, 0, false, function (list: Array<models.Website>) {
                    $scope.items = list;
                    $scope.totalRecord = $scope.items.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };
        }
    }
    export class WebsiteDetailController extends PermissionController {
        constructor($scope: scopes.IWebsiteDetailScope, $state, $stateParams, Page: common.PageUtils,
            ActionMenuStore: utils.DataStore, $window, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            $scope.siteId = $stateParams.websiteId;
            $scope.title = 'Detail';
            $scope.page = Page;
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

            factory.websiteService.load($scope.siteId, function(website: models.Website) {
                if (website)
                    $scope.isPRPublisher = (website.kind === models.Website.KIND.PR);
            });
            // setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create Zone Group", function () {
                $state.transitionTo('main.website.zonegroup_create', { websiteId: $scope.siteId });
            }, $scope.permissionDefine.EDITWEBSITE));
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.website.zonegroup', { websiteId: $scope.siteId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.siteId, objType: "Website" }));
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store('main.website.detail.zonegroup', action_menus);

            action_menus = [];
                action_menus.push(new common.ActionMenu("icon-plus", "Create Zone", function () {
                    $state.transitionTo('main.website.zone_type', { websiteId: $scope.siteId });
                }, $scope.permissionDefine.EDITWEBSITE));
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.website.zone', { websiteId: $scope.siteId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.siteId, objType: "Website" }));
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store('main.website.detail.zone', action_menus);
            ActionMenuStore.store('main.website.detail.shared', action_menus);
            ActionMenuStore.store('main.website.detail.setting', action_menus);

            //------------------------ function
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'zonegroups' && $state.current.name === 'main.website.detail.zonegroup')
                    return 'active';
                if (tabname === 'zones' && $state.current.name === 'main.website.detail.zone')
                    return 'active';
                if (tabname === 'setting' && $state.current.name === 'main.website.detail.setting')
                    return 'active';
                if (tabname === 'shared' && $state.current.name === 'main.website.detail.shared')
                    return 'active';
                if (tabname === 'inventory' && $state.current.name === 'main.website.detail.inventory')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string) => {
                if (tabname === 'zonegroups') {
                    $state.transitionTo('main.website.detail.zonegroup', { websiteId: $scope.siteId, page: 1 });
                    return;
                }
                if (tabname === 'zones') {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $scope.siteId, page: 1 });
                    return;
                }
                if (tabname === 'setting') {
                    $state.transitionTo('main.website.detail.setting', { websiteId: $scope.siteId, page: 1 });
                    return;
                }
                if (tabname === 'inventory') {
                    $state.transitionTo('main.website.detail.inventory', { websiteId: $scope.siteId });
                    return;
                }
                if (tabname === 'shared') {
                    $state.transitionTo('main.website.detail.shared', { websiteId: $scope.siteId, page: 1 });
                    return;
                }                
            };
        }
    }

    export class WebsiteSettingController extends PermissionController {
        constructor($scope: scopes.IWebsiteSettingScope, $stateParams, $state,
            $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.siteId = $stateParams.websiteId;
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);

            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            $scope.ownerName = '';
            factory.websiteService.load($stateParams.websiteId, function (ret) {
                $scope.item = ret;
                var arr: string[] = $scope.item.reviewType.split(",");
                for (var i: number = 0; i < arr.length; i++) {
                    if (arr[i].length > 0 && arr[i] === 'network')
                        $scope.networkType = true;
                    if (arr[i].length > 0 && arr[i] === 'booking')
                        $scope.bookingType = true;
                    if (arr[i].length > 0 && arr[i] === 'tvc')
                        $scope.tvcType = true;
                }
                $scope.fcLimitUnit = "Seconds"
                if ($scope.item.frequencyCappingTime) {
                    if ($scope.item.frequencyCappingTime % 60 === 0) {
                        $scope.item.frequencyCappingTime /= 60;
                        $scope.fcLimitUnit = "Minutes"
                    }
                    if ($scope.item.frequencyCappingTime % 60 === 0) {
                        $scope.item.frequencyCappingTime /= 60;
                        $scope.fcLimitUnit = "Hours"
                    }
                }
                factory.userService.list(0, 100, function (list: backend.ReturnList<models.UserInfo>) {
                    $scope.owners = list.data;
                    $scope.ownerName = $scope.getOwnerName($scope.item.ownerId);
                });
            });

            $scope.networkType = false;
            $scope.bookingType = false;
            $scope.tvcType = false;
            $timeout(() => jQuery("#frequencyCapping").tooltip({ trigger: "focus", title: "Enter 0 to unlimit" }), 100);
            // notify change action menu
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            // -------------- function ------------------
            $scope.choose = (ownerId: number) => {
                $scope.ownerName = $scope.getOwnerName(ownerId);
                if ($scope.ownerName.length > 0)
                    $scope.item.ownerId = ownerId;
                else
                    $scope.item.ownerId = 0;
            };

            $scope.getOwnerName = (ownerId: number): string => {
                for (var i: number = 0; i < $scope.owners.length; i++) {
                    if (ownerId === $scope.owners[i].id)
                        return $scope.owners[i].name;
                }
                return '';
            };

            $scope.chooseFC = (type: string) => {
                switch (type) {
                    case "hours":
                        $scope.fcLimitUnit = "Hours"; break;
                    case "seconds":
                        $scope.fcLimitUnit = "Seconds";
                        break;
                    case "minutes":
                        $scope.fcLimitUnit = "Minutes";
                        break;
                }
            }

            $scope.update = () => {
                if ($scope.item.frequencyCapping && isNaN(parseInt($scope.item.frequencyCapping + ""))) return;
                $scope.item.reviewType = "";
                if ($scope.networkType === true)
                    $scope.item.reviewType += "network";
                if ($scope.bookingType === true) {
                    if ($scope.item.reviewType.length > 0)
                        $scope.item.reviewType += ",";
                    $scope.item.reviewType += "booking";
                }
                if ($scope.tvcType === true) {
                    if ($scope.item.reviewType.length > 0)
                        $scope.item.reviewType += ",";
                    $scope.item.reviewType += "tvc";
                }

                if ($scope.fcLimitUnit == "Minutes")
                    $scope.item.frequencyCappingTime *= 60;
                else if ($scope.fcLimitUnit == "Hours")
                    $scope.item.frequencyCappingTime *= 3600;

                //loading page
                factory.websiteService.update($scope.item, function (ret) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "website"));
                    $timeout(function () {
                        //disable loading and transit to 
                        $state.transitionTo('main.website.list', { page: 1 });
                    }, 1000);

                }, function (msg, status) {
                });
            };
        }
    }

    export class CreateWebsiteController extends PermissionController {
        constructor($scope: scopes.IWebsiteCreateScope, $state, $stateParams, $http,
            $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab, Page: common.PageUtils, factory: backend.Factory) {
            super($scope, $state, factory);
            // set current tab & title
            CurrentTab.setTab(new common.Tab("website", "create_site"));
            Page.setTitle('Create Website | 123Click');
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            if ($scope.checkPermission($scope.permissionDefine.WEBSITE_CREATE) == false) {
                window.history.back()
                return;
            }


            $scope.error_message = '';
            $scope.networkType = false;
            $scope.bookingType = false;
            $scope.tvcType = false;
            $scope.allowBooking = false;
            $scope.checkValidate = false;
            $scope.hasSave = false;
            var userId: number = 0;
            var userinfo: models.UserInfo = factory.userInfo.getUserInfo();
            if (userinfo !== null && userinfo !== undefined) {
                userId = userinfo.id;
            }
            $scope.item = new models.Website(0, '', userId, '', [], [], [], 0, 0);
            $scope.fcLimitUnit = "Minutes";

            $timeout(() => jQuery("#frequencyCapping").tooltip({ trigger: "focus", title: "Enter 0 to unlimit" }), 100);
            // ----------------- function ----------------------
            $scope.gotoWebsiteList = () => {
                $state.transitionTo('main.website.list', { page: 1 });
            };
            $scope.save = () => {
                if ($scope.item.frequencyCapping && isNaN(parseInt($scope.item.frequencyCapping + ""))) return;
                if ($scope.hasSave) return;
                $scope.checkValidate = true;
                if ($scope.item.name.length == 0 || (!$scope.networkType && !$scope.bookingType && !$scope.tvcType))
                    return;
                $scope.item = new models.Website(0, $scope.item.name, $scope.item.ownerId, $scope.item.description, [], [], [], $scope.item.frequencyCapping, $scope.item.frequencyCappingTime);
                $scope.item.reviewType = "";
                if ($scope.networkType === true)
                    $scope.item.reviewType += "network";
                if ($scope.bookingType === true) {
                    if ($scope.item.reviewType.length > 0)
                        $scope.item.reviewType += ",";
                    $scope.item.reviewType += "booking";
                }
                if ($scope.tvcType === true) {
                    if ($scope.networkType === false) {
                        if ($scope.item.reviewType.length > 0)
                            $scope.item.reviewType += ",";
                        $scope.item.reviewType += "network";
                    }
                    if ($scope.item.reviewType.length > 0)
                        $scope.item.reviewType += ",";
                    $scope.item.reviewType += "tvc";
                }
                if ($scope.fcLimitUnit == "Minutes")
                    $scope.item.frequencyCappingTime *= 60;
                else if ($scope.fcLimitUnit == "Hours")
                    $scope.item.frequencyCappingTime *= 3600;

                factory.websiteService.save($scope.item, function (data) {
                    if (data.id > 0) {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "website"));
                        $timeout(function () {
                            $state.transitionTo("main.website.list", { page: 1 });
                        }, 1000);
                        $scope.hasSave = true;
                        return;
                    } else {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "website"));
                        $scope.hasSave = true; //enable button Save when method save is complete
                    }
                });
            };

            $scope.choose = (ownerId: number) => {
                $scope.ownerName = $scope.getOwnerName(ownerId);
                if ($scope.ownerName.length > 0)
                    $scope.item.ownerId = ownerId;
                else
                    $scope.item.ownerId = 0;
            };

            $scope.getOwnerName = (ownerId: number): string => {
                for (var i: number = 0; i < $scope.owners.length; i++) {
                    if (ownerId === $scope.owners[i].id)
                        return $scope.owners[i].name;
                }
                return '';
            };

            $scope.getAllowBookingClass = (): string => {
                if ($scope.allowBooking)
                    return "checked";
                return "";
            };

            $scope.chooseFC = (type: string) => {
                switch (type) {
                    case "hours":
                        $scope.fcLimitUnit = "Hours";break;
                    case "seconds":
                        $scope.fcLimitUnit = "Seconds";
                        break;
                    case "minutes":
                        $scope.fcLimitUnit = "Minutes";
                        break;
                }
            }
        }
    }

    // approve ads
    export class ApproveAdsController {
        constructor($scope: scopes.IApprovedAdsScope, $state, $stateParams,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
            CurrentTab.setTab(new common.Tab("website", "approve_ads"));
                $scope.$emit('change_action_menu');
            $scope.pageIndex = 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;

            $scope.checkBoxes = {};
            $scope.items = [];
            $scope.checkBoxes['all'] = false;
            $scope.isChosen = false;
            $scope.selectedFilter = "All website";
            $scope.previewContent = ''; $scope.previewSource = ''; $scope.previewTitle = ''; $scope.contentDic = {};
            loadList(0, 0, $scope.pageSize);

            var curIndex = 0, count = 100;
            $scope.websites = [];
            listAllWebsite(curIndex, count);
            function listAllWebsite(curIndex, count) {
                factory.websiteService.list(curIndex, count, function (websiteRet) {
                    if (!!websiteRet && !!websiteRet.data) {
                        $scope.websites = $scope.websites.concat(websiteRet.data);
                        $scope.websites.sort((a, b) => (a.name.toLowerCase() < b.name.toLowerCase() ? -1 : (a.name.toLowerCase() > b.name.toLowerCase() ? 1 : 0)));
                        var size = Math.min(count, websiteRet.length);
                        curIndex += size;
                        if (curIndex < websiteRet.total)
                            listAllWebsite(curIndex, count);
                    }
                });
            }

            var selectedWebsiteId = 0;
            ///--------Scope function

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });
            $scope.check = (itemId: number) => {
                if ($scope.checkBoxes[itemId]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            }
                $scope.isBannerImg = (filename: string): boolean => {
                if (!filename || (filename && filename.length === 0))
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "png" || ext === "jpeg" || ext === "jpg" || ext === "gif")
                        return true;
                }
                return false;
            };

            $scope.isBannerFlash = (filename: string): boolean => {
                if (!filename || (filename && filename.length === 0))
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };
            $scope.paging = (start: number, size: number) => {
                loadList(selectedWebsiteId, start, size);
            }
            $scope.isActiveClass = () => {
                return $scope.isChosen ? "" : "disabled";
            }
            $scope.approve = () => {
                var param = getCheckedItem();
                factory.websiteService.approve(1, param, function (response) {
                    loadList(selectedWebsiteId, 0, $scope.pageSize);
                }, function (msg, status) {
                });
            }
            $scope.reject = () => {
                var param = getCheckedItem();
                factory.websiteService.approve(1, param, function (response) {
                    loadList(selectedWebsiteId, 0, $scope.pageSize);
                }, function (msg, status) {
                });
            };
            $scope.websiteFilter = (websiteId: number) => {
                if (websiteId === -1) {
                    $scope.selectedFilter = "All website";
                    loadList(0, 0, $scope.pageSize);
                }
                else {
                    factory.websiteService.load(websiteId, function (websiteRet) {
                        $scope.selectedFilter = websiteRet.name;
                        selectedWebsiteId = websiteRet.id;
                        loadList(selectedWebsiteId, 0, $scope.pageSize);
                    });
                }
            };
             

            $scope.getKind = (kind: string): string => {
                if (models.CampaignItemType.isBanner(kind))
                    return 'img';
                if (models.CampaignItemType.isHTML(kind))
                    return 'html';
                if (kind === models.CampaignItemType.NETWORK.TVC)
                    return 'tvc';
                return '';
            };

            $scope.view = (id: number, name: string, kind: string) => {
                var content: string = $scope.contentDic[id];

                if (models.CampaignItemType.isBanner(kind)) {
                    $scope.previewTitle = name;
                    $scope.previewSource = content;
                    $scope.previewWidth = $scope.sizeDic[id]["width"];
                    $scope.previewHeight = $scope.sizeDic[id]["height"];
                    jQuery("#previewBanner").modal('show');
                    fixPreviewPosition("#previewBanner");
                } else if (models.CampaignItemType.isHTML(kind)) {
                    $scope.previewContent = content;
                    $scope.previewWidth = $scope.sizeDic[id]["width"] + 20;
                    $scope.previewHeight = $scope.sizeDic[id]["height"] + 30;
                    $scope.previewTitle = name;
                    jQuery("#previewHtml").modal('show');
                    fixPreviewPosition("#previewHtml");
                } else if (kind === models.CampaignItemType.NETWORK.TVC) {
                    $scope.previewSource = ""; $scope.previewTitle = name;
                    if (content.length === 0) {
                        jQuery("#previewExternalVideo").modal('show');
                        fixPreviewPosition("#previewExternalVideo");
                        return;
                    }
                    $scope.previewSource = content;
                    flowplayer("player", "resource/flash/flowplayer-3.2.16.swf", $scope.previewSource);
                    jQuery("#previewVideo").modal('show');
                    fixPreviewPosition("#previewVideo");
                }
            };

            $scope.gotoItem = (id: number) => {
                factory.campaignItemService.load(id, function (item: models.AdItem) {
                    if (!!item) {
                        factory.campaignService.load(item.campaignId, function (camp: models.Campaign) {
                            if (!!camp)
                                $state.transitionTo("main.order.item_detail.setting", {
                                    orderId: camp.orderId, campaignId: camp.id, itemId: item.id
                                });
                        });
                    }
                });
            }
            $scope.gotoWebsite = (id: number) => {
                return $state.href("main.website.detail.setting", {websiteId: id});
            }
            $scope.gotoZone = (websiteId: number, id: number) => {
                return $state.href("main.website.zone_detail.setting", {websiteId: websiteId, zoneId: id});
            }
                   //--------

            function loadList(websiteId: number, from: number, count: number) {
                $scope.sizeDic = {};
                factory.websiteService.listAdsByStatus(0, websiteId, from, count, function (ret) {
                    $scope.items = ret.data;
                    $scope.totalRecord = ret.total;
                    for (var i: number = 0; i < $scope.items.length; i++) {
                        $scope.items[i].id = i;
                        $scope.sizeDic[$scope.items[i].item.id] = { width: $scope.items[i].item.width, height: $scope.items[i].item.height };
                        if (models.CampaignItemType.isBannerNotExpandable($scope.items[i].item.kind))
                            $scope.contentDic[$scope.items[i].item.id] = $scope.items[i].item.bannerFile;
                        else if (models.CampaignItemType.isBannerWithExpandable($scope.items[i].item.kind)) {
                            $scope.contentDic[$scope.items[i].item.id] = $scope.items[i].item.standardFile;
                        }
                        else if (models.CampaignItemType.isHTML($scope.items[i].item.kind))
                        {
                            $scope.contentDic[$scope.items[i].item.id] = $scope.items[i].item.embeddedHtml;
                        }
                        else if ($scope.items[i].item.kind === models.CampaignItemType.NETWORK.TVC)
                            $scope.contentDic[$scope.items[i].item.id] = $scope.items[i].item.tvcFile;
                    }
                    $scope.checkBoxes = {};
                    $scope.isChosen = false;
                });
            }

            function getCheckedItem() {
                var ret: Array<any> = [];
                for (var i = 0; i < $scope.items.length; i++) {
                    var approveItem: models.ApprovedAds = $scope.items[i];
                    if ($scope.checkBoxes[approveItem.id])
                        for (var j = 0; j < approveItem.zoneIds.length; j++) {
                            ret.push({ itemId: approveItem.item.id, zoneId: approveItem.zoneIds[j] });
                        }
                }
                return ret;
            }

        }
    }
    // zone group
    export class ZoneGroupDetailController extends PermissionController {
        constructor($scope: scopes.IZoneGroupDetailScope,
            $state, $stateParams, ActionMenuStore: utils.DataStore, factory: backend.Factory, $window) {

            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            $scope.siteId = $stateParams.websiteId;
            $scope.zoneGroupId = $stateParams.zoneGroupId;

            // setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create Zone", function () {
                $state.transitionTo('main.website.zone_type', { websiteId: $stateParams.websiteId });
            }, $scope.permissionDefine.EDITWEBSITE));
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.zonegroup.zone', { websiteId: $scope.siteId, zonegroupId: $scope.zoneGroupId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.zoneGroupId, objType: "Zonegroup" }));
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store('main.website.zonegroup_detail.zone', action_menus);
            ActionMenuStore.store('main.website.zonegroup_detail.setting', action_menus);

            // setup action menu for pr category
            var action_menus_pr: common.ActionMenu[] = [];
            action_menus_pr.push(new common.ActionMenu("icon-plus", "Create PR Category", function () {
                $state.transitionTo('main.website.create_przone', { websiteId: $stateParams.websiteId });
            }, $scope.permissionDefine.EDITWEBSITE));
            action_menus_pr.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.website.zonegroup', { websiteId: $scope.siteId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            ActionMenuStore.store('main.website.zonegroup_detail.przones', action_menus_pr);


            // function
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'zones' && $state.current.name === 'main.website.zonegroup_detail.zone')
                    return 'active';
                if (tabname === 'setting' && $state.current.name === 'main.website.zonegroup_detail.setting')
                    return 'active';
                if (tabname === 'przones' && $state.current.name === 'main.website.zonegroup_detail.przones')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string) => {
                if (tabname === 'zones') {
                    $state.transitionTo('main.website.zonegroup_detail.zone', { websiteId: $scope.siteId, zoneGroupId: $scope.zoneGroupId, page: 1 });
                    return;
                }
                if (tabname === 'setting') {
                    $state.transitionTo('main.website.zonegroup_detail.setting', { websiteId: $scope.siteId, zoneGroupId: $scope.zoneGroupId });
                    return;
                }

                if (tabname === 'przones') {
                    $state.transitionTo('main.website.zonegroup_detail.przones', { websiteId: $scope.siteId, zoneGroupId: $scope.zoneGroupId });
                    return;
                }
            };


        }
    }

    export class ZoneGroupCreateController extends PermissionController{
        constructor($scope: scopes.ICreateZoneGroupScope, $state, $stateParams,
            $timeout, StateStore: utils.DataStore, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

            //scope initialization
            if (StateStore.get($state.current.name) == true) {
                $scope.item = StateStore.get('item');
                StateStore.store($state.current.name, false);
                StateStore.store('item', null);
            }
            var siteid: number = $stateParams.websiteId;
            var site: models.Website = null;
            $scope.checkedMap = {};
            $scope.extra = {};
            $scope.selectedZones = [];
            $scope.sitezones = [];
            $scope.isCreateSuccess = false;
            $scope.isCreateFailed = false;
            $scope.error_message = '';
            factory.websiteService.load(siteid, function (ret) {
                $scope.siteId = siteid;
                $scope.siteName = ret.name;
            });

            factory.zoneService.listByWebsiteId(siteid, 0, 100000, function (ret) {
                $scope.sitezones = ret.data;
            });


            //scope functions
            $scope.zoneChoice = (id: number, name: string) => {
                //remove in selected
                if ($scope.checkedMap[id] == false) {
                    $scope.selectedZones.forEach(function (obj) {
                        if (obj.id == id) {
                            var index = $scope.selectedZones.indexOf(obj);
                            $scope.selectedZones.splice(index, 1);
                            $scope.checkedMap[id] = false;
                        }
                    });
                } else {
                    $scope.sitezones.forEach(function (obs: models.Zone) {
                        if (obs.id == id) {
                            $scope.selectedZones.push(obs);
                            $scope.checkedMap[id] = true;
                        }
                    });
                }
            };

            $scope.zoneRemove = (id: number) => {
                $scope.selectedZones.forEach(function (obj) {
                    if (obj.id == id) {
                        var index = $scope.selectedZones.indexOf(obj);
                        $scope.selectedZones.splice(index, 1);
                        $scope.checkedMap[id] = false;
                    }
                });
            };
            $scope.create = (zonegrpform) => {
                if ($scope.hasSave) return;
                $scope.validateFailed = false;

                if (zonegrpform.$valid /*&& $scope.selectedZones.length > 0 */) {
                    var zoneids: number[] = [];
                    $scope.selectedZones.forEach(function (item) {
                        zoneids.push(item.id);
                    });
                    $scope.item = new models.ZoneGroup(0, $scope.item.name, zoneids, $stateParams.websiteId);
                    factory.zoneGroupService.save($scope.item, function (item: models.ZoneGroup) {
                        if (item !== undefined && item.id > 0) {
                            $scope.isCreateSuccess = true;
                            notify(new common.ActionMessage(0, "update", "zonegroup"));
                            $timeout(function () {
                                $state.transitionTo('main.website.detail.zonegroup', { websiteId: $stateParams.websiteId, page: 1 });
                            }, 1000);
                            $scope.hasSave = true;
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zonegroup"));
                    });
                    return;
                }
                notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zonegroup"));
            };
            $scope.addNewZone = () => {
                StateStore.store($state.current.name, true);
                StateStore.store('item', $scope.item);
                $state.transitionTo('main.website.zone_type', { websiteId: $stateParams.websiteId });
            };

            $scope.cancel = () => {
                $state.transitionTo('main.website.detail.zonegroup', { websiteId: $stateParams.websiteId, page: 1 });
            };

        }
    }

    export class ZoneGroupSettingController extends PermissionController {
        constructor($scope: scopes.ICreateZoneGroupScope, $state, $stateParams,
            $timeout, StateStore: utils.DataStore, factory: backend.Factory) {

            // notify action menu
                $scope.$emit('change_action_menu', [$stateParams.websiteId]);
                $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
                super($scope, $state, factory);

            var zoneGroupId: number = $stateParams.zoneGroupId;
            $scope.siteId = $stateParams.websiteId;
            $scope.checkedMap = {};
            $scope.extra = {};
            $scope.selectedZones = [];
            $scope.sitezones = [];
            $scope.zoneGroupId = $stateParams.zoneGroupId;

            factory.websiteService.load($stateParams.websiteId, function (website: models.Website) {
                if (website !== null && website !== undefined && website.id > 0) {
                    $scope.siteId = website.id;
                    $scope.siteName = website.name;
                }

            });
            factory.zoneService.listByWebsiteId($scope.siteId, 0, 100000, function (ret) {
                $scope.sitezones = ret.data;

            });
                factory.zoneGroupService.load($scope.zoneGroupId, function (ret) {
                if (ret) {
                    $scope.item = ret;
                    factory.zoneService.listByIds(ret.zones, function (ret) {
                        $scope.selectedZones = ret;
                        $scope.selectedZones.forEach(function (ob) {
                            $scope.checkedMap[ob.id] = true;
                        });
                        $scope.isLoadingDone = true;
                    });
                } else {
                    console.log('load zone group failed !');
                }
            });
                factory.zoneService.listByWebsiteId($stateParams.websiteId, 0, 100000, function (ret) {
                if (ret) {
                    $scope.sitezones = ret.data;
                }
            });


            // ----------------------- function --------------------------------
            $scope.zoneChoice = (id: number, name: string) => {
                if ($scope.checkedMap[id] == false) {
                    $scope.selectedZones.forEach(function (obj) {
                        if (obj.id == id) {
                            var index = $scope.selectedZones.indexOf(obj);
                            $scope.selectedZones.splice(index, 1);
                            $scope.checkedMap[id] = false;
                        }
                    });
                } else {
                    $scope.sitezones.forEach(function (obs: models.Zone) {
                        if (obs.id == id) {
                            $scope.selectedZones.push(obs);
                            $scope.checkedMap[id] = true;
                        }
                    });
                }
            };

            $scope.zoneRemove = (id: number) => {
                $scope.selectedZones.forEach(function (obj) {
                    if (obj.id == id) {
                        var index = $scope.selectedZones.indexOf(obj);
                        $scope.selectedZones.splice(index, 1);
                        $scope.checkedMap[id] = false;
                    }
                });
            };

            $scope.addNewZone = () => {
                StateStore.store($state.current.name, true);
                StateStore.store('item', $scope.item);
                $state.transitionTo('main.website.zone_type', { websiteId: $stateParams.websiteId });
            };

            $scope.updateSetting = (zonegrpform) => {
                if (zonegrpform.$valid) {
                    var zoneids: number[] = [];
                    $scope.selectedZones.forEach(function (item) {
                        zoneids.push(item.id);
                    });

                    $scope.item = new models.ZoneGroup($scope.zoneGroupId, $scope.item.name, zoneids, $stateParams.websiteId);
                    factory.zoneGroupService.update($scope.item, function (result: string) {
                        if (result !== undefined && result === "success") {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "zonegroup"));
                            $timeout(function () {
                                $state.transitionTo('main.website.zonegroup_detail.zone', { websiteId: $stateParams.websiteId, zoneGroupId: $stateParams.zoneGroupId });
                            }, 1500);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zonegroup"));
                    }, function (msg, status) {
                        });
                }
            };
        }
    }
    export class ZoneGroupListController extends PermissionController {

        constructor($scope: scopes.IListZoneGroupScope, $state, $stateParams,
            Page: common.PageUtils, $modal, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            Page.setCurrPage('List Zone Group');

            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            //scope initialization

            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;
            $scope.checkBoxes = {};
            $scope.checkBoxes['all'] = false;
            $scope.searchText = '';
            $scope.siteId = $stateParams.websiteId;
            $scope.items = [];
            $scope.isChosen = false;
            $scope.sortField = { name: common.SortDefinition.DEFAULT };
            listZoneGroup(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);

            var currentSortField = "", currentSortType = "";
            function listZoneGroup(from: number, count: number, sortBy?: string, sortType?: string) {
                factory.zoneGroupService.listByWebsiteId($scope.siteId, from, count, function (list: backend.ReturnList<models.ZoneGroup>) {
                    $scope.items = list.data;
                    $scope.totalRecord = list.total;
                }, sortBy, sortType);
                currentSortField = sortBy;
                currentSortType = sortType;
            }

            $scope.currentSelectedRow = - 1;

                //----------------- functions ---------------------------
                $scope.switchSort = (type: string) => {
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        listZoneGroup(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.UP) {
                        $scope.sortField[type] = common.SortDefinition.DEFAULT
                        listZoneGroup(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                        $scope.sortField[type] = common.SortDefinition.UP;
                        listZoneGroup(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type);
                    }
                }

                $scope.getSortClass = (type: string) => {
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                        return "header";
                    if ($scope.sortField[type] === common.SortDefinition.UP)
                        return "headerSortUp";
                    if ($scope.sortField[type] === common.SortDefinition.DOWN)
                        return "headerSortDown";
                    return "";
                }

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.check = (zoneGroupId: number) => {
                if ($scope.checkBoxes[zoneGroupId]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            };

            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }
            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.delete = () => {
                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }
                if (isHave) {
                    var zgModal = $modal.open({
                        templateUrl: 'views/common/modal.delete',
                        controller: common.ModalDeleteController,
                        resolve: {
                            checkedList: function () {
                                var checkedList = [];//list of object contain id and name
                                for (var att in $scope.checkBoxes) {
                                    if ($scope.checkBoxes[att] == true) {
                                        for (var i = 0; i < $scope.items.length; i++) {
                                            if ($scope.items[i].id == att) {
                                                checkedList.push({ id: att, name: $scope.items[i].name });
                                            }
                                        }
                                    }
                                }
                                return checkedList;
                            },
                            type: function () {
                                return 'zone_group';
                            }
                        }
                    });

                    zgModal.result.then(function (checkList) {
                        var zoneGroupIds: number[] = [];
                        for (var i: number = 0; i < checkList.length; i++) {
                            var zonegrpid: number = checkList[i].id;
                            zoneGroupIds.push(zonegrpid);
                        }
                        for (var i: number = 0; i < zoneGroupIds.length; i++) {
                            var zonegrpid: number = zoneGroupIds[i];
                            factory.zoneGroupService.remove(zonegrpid, function (ret) {
                                factory.zoneGroupService.listByWebsiteId($scope.siteId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (list: backend.ReturnList<models.ZoneGroup>) {
                                    $scope.items = list.data;
                                    $scope.totalRecord = list.total;
                                });
                            });
                        }
                    }, function (message) {

                        });
                }


            };

            $scope.search = () => {
                if ($scope.searchText.length === 0)
                    return;
                factory.zoneGroupService.search($scope.searchText, $stateParams.websiteId, false, function (list: Array<models.ZoneGroup>) {
                    $scope.items = list;
                    $scope.totalRecord = list.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };

            $scope.gotoZoneGroup = (zoneGroupId: number) => {
                $state.transitionTo('main.website.zonegroup_detail.zone', { websiteId: $scope.siteId, zoneGroupId: zoneGroupId, page: 1 });
            };

            $scope.gotoZoneGroupSetting = (zoneGroupId: number) => {
                $state.transitionTo('main.website.zonegroup_detail.setting', { websiteId: $scope.siteId, zoneGroupId: zoneGroupId });
            };


            $scope.paging = (start: number, size: number) => {
                listZoneGroup(start, size, currentSortField, currentSortType);
            };
        }
    }

    /*
     *  ---------------------- ZONE ----------------------------------------------------
     */
    export class ZoneDetailController extends PermissionController {
        constructor($scope: scopes.IZoneDetailScope, $state, $stateParams, ActionMenuStore: utils.DataStore, $window, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            $scope.siteId = $stateParams.websiteId;
            $scope.zoneId = $stateParams.zoneId;
            $scope.zoneType = $stateParams.type;               
                            
            // setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create Zone", function () {
                $state.transitionTo('main.website.zone_type', { websiteId: $stateParams.websiteId });
            }, $scope.permissionDefine.EDITWEBSITE));
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.zone.linkeditem', { websiteId: $scope.siteId, zoneId: $scope.zoneId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.zoneId, objType: "Zone" }));
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store('main.website.zone_detail.linked_items', action_menus);
            ActionMenuStore.store('main.website.zone_detail.setting', action_menus);
            ActionMenuStore.store('main.website.zone_detail.booking', action_menus);

            // setup action menu for pr category
            var action_menus_pr: common.ActionMenu[] = [];
            action_menus_pr.push(new common.ActionMenu("icon-plus", "Create PR Category", function () {
                $state.transitionTo('main.website.create_przone', { websiteId: $stateParams.websiteId });
            }, $scope.permissionDefine.EDITWEBSITE));
            action_menus_pr.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.website.zonegroup', { websiteId: $scope.siteId }));
            }, $scope.permissionDefine.REPORTWEBSITE));
            ActionMenuStore.store('main.website.zone_detail.articles', action_menus_pr);
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);

            // function
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'linked_items' && $state.current.name === 'main.website.zone_detail.linked_items')
                    return 'active';
                if (tabname === 'setting' && $state.current.name === 'main.website.zone_detail.setting')
                    return 'active';
                if(tabname === 'articles' && $state.current.name === 'main.website.zone_detail.articles')
                    return 'active';
                if (tabname === 'booking' && $state.current.name === 'main.website.zone_detail.booking')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string, zone_type: string) => {
                if (tabname === 'linked_items') {
                    $state.transitionTo('main.website.zone_detail.linked_items', { websiteId: $scope.siteId, zoneId: $scope.zoneId, type: zone_type, page: 1 });
                    return;
                }
                if (tabname === 'setting') {                                        
                    $state.transitionTo('main.website.zone_detail.setting', { websiteId: $scope.siteId, zoneId: $scope.zoneId, type: zone_type });
                    return;
                }
                if (tabname === 'articles') {
                    var status = 'Pending';
                    $state.transitionTo('main.website.zone_detail.articles', { websiteId: $scope.siteId, zoneId: $scope.zoneId, status: status, type: zone_type, });
                    return;
                }
                if (tabname === 'booking') {
                    $state.transitionTo('main.website.zone_detail.booking', { websiteId: $scope.siteId, zoneId: $scope.zoneId, type: zone_type });
                    return;
                }
            };

        }
    }

    export class ZoneTypeController extends PermissionController{
        constructor($scope: scopes.IZoneTypeScope, $state, $stateParams, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

            $scope.siteId = $stateParams.websiteId;
            $scope.option = 1;
            // function
            $scope.goto = (dest: string) => {
                if (dest === 'create') {
                    if ($scope.option == 1) {
                        $state.transitionTo('main.website.banner_zone_create', { websiteId: $scope.siteId });
                        return;
                    } else if ($scope.option == 2) {
                        $state.transitionTo('main.website.video_zone_create', { websiteId: $scope.siteId });
                        return;
                    }
                    else {
                        $state.transitionTo('main.website.pr_zone_create', { websiteId: $scope.siteId });
                        return;
                    }
                }
                if (dest === 'cancel') {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $scope.siteId });
                    return;
                }
            };
        }
    }

    export class ZoneListController extends PermissionController{
        constructor($scope: scopes.IListZoneScope, $stateParams, $state, $modal, $http,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
                $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
                super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
                $scope.$emit('change_action_menu', [$stateParams.websiteId]);

            //scope initialization
            var pageIndex: number = 1;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;

            $scope.checkBoxes = {};
            $scope.checkBoxes['all'] = false;
            $scope.searchText = '';
            $scope.isChosen = false; $scope.isShowSearchBox = true;
            $scope.sortField = { name: common.SortDefinition.DEFAULT };

            var siteid: number = $stateParams.websiteId;
            var zonegrpid: number = $stateParams.zoneGroupId;

            $scope.siteId = siteid;
            $scope.zoneGroupId = zonegrpid;
            var page = $stateParams.page;
            if ($state.current.name === 'main.website.zonegroup_detail.zone')
                $scope.isShowSearchBox = false;

            var currentSortField = "", currentSortType = "";
            function listzone(siteId: number, from: number, count: number, filterBy: string, orderCol?: string, orderType?: string) {
                if (siteid != null && siteid != undefined) {
                    factory.zoneService.listByWebsiteIdFilter(siteId, from, count, filterBy, function (list: backend.ReturnList<models.Zone>) {
                        $scope.items = list.data;
                        $scope.totalRecord = list.total;
                    }, orderCol, orderType);
                    currentSortField = orderCol;
                    currentSortType = orderType;
                }
            }


            $scope.currentSelectedRow = -1;
            $scope.filterBy = "a";
            $scope.filterText = "All zones";
            factory.websiteService.load($stateParams.websiteId, function(website: models.Website) {
                if (website)
                    $scope.isPRPublisher = (website.kind === models.Website.KIND.PR);
            })

            $scope.prCount = {};
            factory.zoneService.countPrByStatus($stateParams.websiteId, "website", 0, 10, $scope.filterBy, models.ArticleStatus.PENDING.value, function(resp) {
                if (resp && resp.length) {
                    resp.forEach((val) => {
                        $scope.prCount[val.zoneID] = ($scope.prCount[val.zoneID] || {})
                        $scope.prCount[val.zoneID].approve =  val.count
                    });
                }
            });
            factory.zoneService.countPrByStatus($stateParams.websiteId, "website", 0, 10, $scope.filterBy, models.ArticleStatus.WAITING_FOR_SYNC_TO_CMS.value, function (resp) {
                if (resp) {
                    resp.forEach((val) => {
                        $scope.prCount[val.zoneID] = ($scope.prCount[val.zoneID] || {})
                        $scope.prCount[val.zoneID].sync = val.count
                    });
                }
            });
            //-----------------------------------
            $scope.$watch("searchText", function (newVal, oldVal) {
                if (!!oldVal && oldVal.length !== 0 && newVal.length === 0)
                    listzone($scope.siteId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
            });
            // ---------------- Scope function ------------------
            listzone($scope.siteId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);

            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    listzone($scope.siteId,($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                    listzone($scope.siteId,($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    listzone($scope.siteId,($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type);
                }
            }

            $scope.getSortClass = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            }

            $scope.filter = (type: string) => {
                switch (type) {
                    case 'all':
                        $scope.filterBy = 'a';
                        $scope.filterText = "All zones";
                        break;
                    case 'banner':
                        $scope.filterBy = "banner";
                        $scope.filterText = "Banner zones";
                        break;
                    case 'video':
                        $scope.filterBy = "video";
                        $scope.filterText = "Video zones";
                        break;
                    case 'disable':
                        $scope.filterBy = "disable";
                        $scope.filterText = "Disable zones";
                        break;
                }
                $scope.pageIndex = 1
                listzone($scope.siteId, $scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, $scope.filterBy);;
            }
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }
            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }

            $scope.getZoneTypeClass = (type: string): string => {
                if (type === 'banner')
                    return 'label-green';
                if (type === 'video')
                    return 'label-pink';
                return '';
            };

            $scope.showModel = true;
            // ----- function  -------------
            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                if ($scope.items === null || $scope.items === undefined)
                    return;
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.unlink = () => {
                // TODO:
            };

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.check = (zoneId: number) => {
                if ($scope.checkBoxes[zoneId]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            };

            $scope.delete = () => {
                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }                
                if (isHave) {
                    var zoneModal = $modal.open({
                        templateUrl: 'views/common/modal.delete',
                        controller: common.ModalDeleteController,
                        resolve: {
                            checkedList: function () {
                                var checkedList = [];//list of object contain id and name
                                for (var att in $scope.checkBoxes) {
                                    if ($scope.checkBoxes[att] == true) {
                                        for (var i = 0; i < $scope.items.length; i++) {
                                            if ($scope.items[i].id == att) {
                                                checkedList.push({ id: att, name: $scope.items[i].name });
                                            }
                                        }
                                    }
                                }
                                return checkedList;
                            },
                            type: function () {
                                if ($scope.filterBy !== 'disable')
                                    return 'zone';
                                return 'enable_zone';
                            }
                        }
                    });
                    zoneModal.result.then(function (checkList) {
                        var zoneIds: number[] = [];
                        for (var i: number = 0; i < checkList.length; i++) {
                            var zoneid: number = checkList[i].id;
                            zoneIds.push(zoneid);
                        }
                        var isUpdate: boolean = false;
                        for (var i: number = 0; i < zoneIds.length; i++) {
                            var zoneid: number = zoneIds[i];
                            if ($scope.filterBy !== 'disable') {
                                factory.zoneService.remove(zoneid, function (ret) {
                                    if ($scope.siteId != null && $scope.siteId != undefined && ($scope.zoneGroupId === null || $scope.zoneGroupId === undefined)) {
                                        listzone($scope.siteId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                                        $scope.checkBoxes = {};
                                    }
                                });
                            } else {
                                factory.zoneService.enable(zoneid, function (ret) {
                                    if ($scope.siteId != null && $scope.siteId != undefined && ($scope.zoneGroupId === null || $scope.zoneGroupId === undefined)) {
                                        listzone($scope.siteId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                                        $scope.checkBoxes = {};
                                    }
                                });
                            }
                        }
                    }, function (message) {

                        });
                }
            };

            $scope.search = () => {
                factory.zoneService.search($scope.searchText, $stateParams.websiteId, false, function (list: Array<models.Zone>) {
                    $scope.items = list;
                    $scope.totalRecord = list.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };

            $scope.gotoZoneSetting = (zoneId: number) => {
                $state.transitionTo('main.website.zone_detail.setting', { websiteId: $scope.siteId, zoneId: zoneId });
            };

            $scope.gotoZone = (zoneId: number, kind: string) => {                                  
                if (kind !== 'pr') {
                    $state.transitionTo('main.website.zone_detail.linked_items', { websiteId: $scope.siteId, zoneId: zoneId, type: kind });
                }
                else {
                    $state.transitionTo('main.website.zone_detail.articles', { websiteId: $scope.siteId, zoneId: zoneId, type: kind });
                }
            };

            $scope.showContentBox = (type: string, id: number) => {
                if (type === 'htmllink') {
                    $scope.modaltitle = "HTML Link";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkHtml?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
                if (type === 'jsonlink') {
                    $scope.modaltitle = "Link Json";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkJson?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
                if (type === 'htmlcode') {
                    $scope.modaltitle = "Link HTML";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getHtmlCode?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
                if (type === 'vast') {
                    $scope.modaltitle = "Link VAST";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkVast?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
            };

            $scope.isShowModal = (): boolean => {
                if ($scope.showModel == true)
                    return true;
                return false;
            };

            $scope.paging = (start: number, size: number) => {
                listzone($scope.siteId, start, size, $scope.filterBy, currentSortField, currentSortType);
            };

            $scope.isBannerZone = (links: number): boolean => {
                if (links === undefined)
                    return false;
                return true;
            };
            $scope.validateAllZone = () => {
                window.open("/#/validateZone/" + $stateParams.websiteId, "_blank");
                //$state.transitionTo("validateZone", {websiteId: $stateParams.websiteId});
            }
        }
    }

    export class ListZoneInZoneGroupController {
        constructor($scope: scopes.IListZoneScope, $state, $stateParams, $http, factory: backend.Factory) {

            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;

            $scope.checkBoxes = {};
            $scope.searchText = '';

            var siteid: number = $stateParams.websiteId;
            var zonegrpid: number = $stateParams.zoneGroupId;

            $scope.siteId = siteid;
            $scope.zoneGroupId = zonegrpid;
            $scope.totalRecord = 0;
            var page = $stateParams.page;
            var count = $scope.pageSize;

            $scope.filterBy = "a";
            $scope.filterText = "All zones";$scope.sortField = { name: common.SortDefinition.DEFAULT };
            if (zonegrpid != null && zonegrpid != undefined) {
                listZone(zonegrpid, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
            }

            function listZone(zonegrId, from, count, filter?: string, sortBy?: string, sortType?: string) {
                factory.zoneService.listByZoneGroupIdFilter(zonegrpid, from, count, filter, function (ret: backend.ReturnList<models.Zone>) {
                    $scope.items = ret.data;
                    $scope.totalRecord = ret.total;
                }, sortBy, sortType);
            }

            $scope.showModel = true;
            // notify action menu
                $scope.$emit('change_action_menu', [$stateParams.websiteId]);

            // ----- function  -------------
                $scope.filter = (type: string) => {
                    switch (type) {
                        case 'all':
                            $scope.filterBy = 'a';
                            $scope.filterText = "All zones";
                            break;
                        case 'banner':
                            $scope.filterBy = "banner";
                            $scope.filterText = "Banner zones";
                            break;
                        case 'video':
                            $scope.filterBy = "video";
                            $scope.filterText = "Video zones";
                            break;
                        case 'disable':
                            $scope.filterBy = "disable";
                            $scope.filterText = "Disable zones";
                            break;
                    }
                    $scope.pageIndex = 1
                    listZone($scope.zoneGroupId, $scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, $scope.filterBy);;
                }

                $scope.switchSort = (type: string) => {
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        listZone($scope.zoneGroupId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.UP) {
                        $scope.sortField[type] = common.SortDefinition.DEFAULT
                        listZone($scope.zoneGroupId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                        $scope.sortField[type] = common.SortDefinition.UP;
                        listZone($scope.zoneGroupId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type);
                    }
                }

            $scope.getSortClass = (type: string) => {
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                        return "header";
                    if ($scope.sortField[type] === common.SortDefinition.UP)
                        return "headerSortUp";
                    if ($scope.sortField[type] === common.SortDefinition.DOWN)
                        return "headerSortDown";
                    return "";
                }

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                if ($scope.items === null || $scope.items === undefined)
                    return;
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.getZoneTypeClass = (type: string): string => {
                if (type === 'banner')
                    return 'label-green';
                if (type === 'video')
                    return 'label-pink';
                return '';
            };

            $scope.currentSelectedRow = -1;
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.check = (zoneGroupId: number) => {
                if ($scope.checkBoxes[zoneGroupId]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            };

            $scope.unlink = () => {
                // TODO:
            };

            $scope.delete = () => {
                // TODO:
            };

            $scope.search = () => {
                // TODO:
            };

            $scope.gotoZoneSetting = (zoneId: number) => {
                $state.transitionTo('main.website.zone_detail.setting', { websiteId: $scope.siteId, zoneId: zoneId });
            };

            $scope.gotoZone = (zoneId: number, kind: string) => {
                if (kind !== 'pr') {
                    $state.transitionTo('main.website.zone_detail.linked_items', { websiteId: $scope.siteId, zoneId: zoneId, type: kind });
                }
                else {
                    $state.transitionTo('main.website.zone_detail.articles', { websiteId: $scope.siteId, zoneId: zoneId, status: 'Pending', type: kind });
                }                                
            };

            $scope.gotoPrZone = (zoneId: number) => {                
                $state.transitionTo('main.website.zone_detail.articles', { websiteId: $scope.siteId, zoneId: zoneId, status: 'Pending' });
            };

            $scope.showContentBox = (type: string, id: number) => {
                if (type === 'htmllink') {
                    $scope.modaltitle = "HTML Link";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkHtml?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });

                }
                if (type === 'jsonlink') {
                    $scope.modaltitle = "Link Json";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkJson?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
                if (type === 'htmlcode') {
                    $scope.modaltitle = "Link HTML";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getHtmlCode?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
                if (type === 'vast') {
                    $scope.modaltitle = "Link VAST";
                    $http({ method: 'GET', url: common.Config.API_URL + '/zone/getLinkVast?zoneId=' + id })
                        .success(function (data, status, headers) {
                            $scope.modalcontent = data;
                            $scope.showModel = true;
                            $scope.$apply;
                            jQuery('#myModal').modal('show');
                        })
                        .error(function (data, status) {
                            //callback({ code: 0, msg: "\"" + data + "\"" });
                        });
                }
            };

            $scope.isShowModal = (): boolean => {
                if ($scope.showModel == true)
                    return true;
                return false;
            };
            $scope.paging = (start: number, size: number) => {
                listZone($scope.zoneGroupId, start, size, $scope.filterBy);
            };

            $scope.isBannerZone = (links: number): boolean => {
                if (links === undefined)
                    return false;
                return true;
            };
        }
    }
    //create zone controller 
    export class BannerZoneCreateController extends PermissionController {

        constructor($scope: scopes.IBannerZoneScope, $stateParams, $state, $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab,
            StateStore: utils.DataStore, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            $scope.sizes = common.Size.ZoneSize;
            $scope.checkValidate = false;
            $scope.item = new models.BannerZone(0, '', $stateParams.websiteId, [], 0, 0, [], "", 0, 0);
            $scope.item.width = 0; $scope.item.height = 0;
            $scope.bookingRunningMode = false; $scope.networkRunningMode = false;
            $scope.checkedMap = {}; $scope.selectedCategories = [];
            CurrentTab.setTab(new common.Tab("website", "list_sites"));

            factory.categoryService.listAll(function (categories) {
                $scope.categories = categories;
            });

            $scope.search = {};
            $scope.zone_style = "Normal";
            $scope.item.frequencyCapping = 0;
            $scope.item.frequencyCappingTime = 0;
            $scope.fcLimitUnit = "Minutes";
            $timeout(() => jQuery("#frequencyCapping").tooltip({ trigger: "focus", title: "Enter 0 to unlimit" }), 100);
                // ------------- function -------------
            $scope.$watch("chooseAll", function (newVal, oldVal) {
                if ($scope.chooseAll == true) {
                    $scope.selectedCategories = [];
                    $scope.categories.forEach((cat, index) => {
                        $scope.checkedMap[cat.id] = true;
                        $scope.selectedCategories.push(cat);
                    });
                }
                else if ($scope.chooseAll == false) {
                    $scope.checkedMap = {};
                    $scope.selectedCategories = [];
                }
            });

            $scope.chooseSize = (value: string) => {
                $scope.search.value = value;
            }

            $scope.validateRunningMode = (): boolean => {
                if ($scope.checkValidate && $scope.bookingRunningMode === false && $scope.networkRunningMode === false)
                    return true;
                return false;
            }
            $scope.isNetwork = (): boolean => {
                return $scope.networkRunningMode;
            };

            $scope.isTargetChosen = (): boolean => {
                if ($scope.checkValidate && $scope.selectedCategories.length === 0)
                    return false;
                return true;
            };            

            $scope.chooseStyle = (style: string) => {
                if (style === 'normal')
                    $scope.item.renderKind = models.EZoneRenderKind.NORMAL;
                else if (style === 'catfish')
                    $scope.item.renderKind = models.EZoneRenderKind.CATFISH;
                else if (style === 'balloon')
                    $scope.item.renderKind = models.EZoneRenderKind.BALLOON;
                else if (style === 'popup')
                    $scope.item.renderKind = models.EZoneRenderKind.POPUP;
                else if (style === 'banner_skin')
                    $scope.item.renderKind = models.EZoneRenderKind.BANNER_SKIN;
                $scope.zone_style = common.StringUtils.capitaliseFirstLetter(style);
            };

            $scope.saveBannerZone = (form: any) => {
                if ($scope.item.frequencyCapping && isNaN(parseInt($scope.item.frequencyCapping + ""))) return;
                if ($scope.hasSave) return;
                $scope.checkValidate = true;
                var size = $scope.search.value.split('x');
                $scope.item.width = size[0];
                $scope.item.height = size[1];
                if ($scope.item.name.length === 0 || $scope.selectedCategories.length === 0)
                    return;
                if ($scope.bookingRunningMode == false && $scope.networkRunningMode == false)
                    return;
                if ($scope.networkRunningMode && ($scope.item.minCPC === undefined || $scope.item.minCPC === 0 ||
                    $scope.item.minCPM === undefined || $scope.item.minCPM === 0))
                    return;
                if ($scope.bookingRunningMode && $scope.item.bookingPrice === 0)
                    return;
                if ($scope.item.width === 0 || $scope.item.height === 0)
                    return;

                var runningMode: string[] = [];
                if ($scope.bookingRunningMode) {
                    runningMode.push('booking');
                }
                if ($scope.networkRunningMode)
                    runningMode.push('network');
                $scope.item.runningMode = runningMode.toString();
                $scope.item.siteId = $stateParams.websiteId;
                $scope.item.categories = []
                for (var cate in $scope.selectedCategories) {
                    $scope.item.categories.push($scope.selectedCategories[cate].id)
                }                
                if ($scope.fcLimitUnit == "Minutes")
                    $scope.item.frequencyCappingTime *= 60;
                else if ($scope.fcLimitUnit == "Hours")
                    $scope.item.frequencyCappingTime *= 3600;
                factory.zoneService.save($scope.item, function (ret) {
                    if (ret === undefined) {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "zone"));
                        return;
                    }
                    if (ret.id > 0) {

                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "zone"));

                        $timeout(function () {
                            var re = BannerZoneCreateController.prototype.gotoPreState($state, $stateParams, StateStore);
                            if (!re) {
                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }
                        }, 1000);
                        $scope.hasSave = true;
                        return;
                    }
                });
            };

            $scope.selectCate = (category: models.Category) => {
                if ($scope.checkedMap[category.id] == false) {//unselecte
                    var i: number = $scope.selectedCategories.length;
                    while (i--) {
                        if ($scope.selectedCategories[i] === category) {
                            $scope.selectedCategories.splice(i, 1);
                            break;
                        }
                    }
                }
                if ($scope.checkedMap[category.id] == true) {
                    if ($scope.selectedCategories.indexOf(category) == -1) {
                        $scope.selectedCategories.push(category);
                    }
                }
                if ($scope.selectedCategories.length === $scope.categories.length)
                    $scope.chooseAll = true;
                else if ($scope.selectedCategories.length === 0)
                    $scope.chooseAll = false;
            };

            $scope.unselectCate = (category: models.Category) => {
                var i: number = $scope.selectedCategories.length;
                while (i--) {
                    if ($scope.selectedCategories[i] === category) {
                        $scope.selectedCategories.splice(i, 1);
                        $scope.checkedMap[category.id] = false;
                        break;
                    }
                }
                if ($scope.selectedCategories.length === 0)
                    $scope.chooseAll = false;
            };

            $scope.cancel = () => {
                if (!this.gotoPreState($state, $stateParams, StateStore)) {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                }
            };

            $scope.chooseFC = (type: string) => {
                switch (type) {
                    case "hours":
                        $scope.fcLimitUnit = "Hours"; break;
                    case "seconds":
                        $scope.fcLimitUnit = "Seconds";
                        break;
                    case "minutes":
                        $scope.fcLimitUnit = "Minutes";
                        break;
                }
            }
        }
        gotoPreState($state, $stateParams, StateStore: utils.DataStore) {
            if (StateStore.get('main.website.zonegroup_create') === true) {
                StateStore.remove('main.website.zonegroup_create');
                $state.transitionTo('main.website.zonegroup_create', { websiteId: $stateParams.websiteId });
                return true;
            }
            if (StateStore.get('main.website.zonegroup_detail.setting') === true) {
                StateStore.remove('main.website.zonegroup_detail.setting');
                var item: models.ZoneGroup = StateStore.get('item');
                if (item !== undefined) {
                    $state.transitionTo('main.website.zonegroup_detail.setting', { websiteId: $stateParams.websiteId, zoneGroupId: item.id });
                    return true;
                }
            }
            return false;
        }
    }
    export class VideoZoneCreateController extends PermissionController {

        constructor($scope: scopes.IVideoZoneScope, $anchorScroll, $location,
            $timeout: ng.ITimeoutService, $stateParams, $state, StateStore: utils.DataStore, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            $scope.categories = [];
            $scope.checkedMap = {};

            $scope.bookingmode = false; $scope.networkmode = false;

            $scope.selectedCategories = [];
            $scope.pre_mincpmtype = 'Create view'; $scope.mid_mincpmtype = 'Create view'; $scope.post_mincpmtype = 'Create view';
            $scope.timeScheduledUnit = 'Minute'; $scope.checkValidate = false;
            $scope.timeappearances = [{ from: 0, duration: 0 }];
            $scope.item = new models.VideoZone(0, '', $stateParams.websiteId, [], 0, 0, "video");

            factory.categoryService.listAll(function (categories) {
                $scope.categories = categories;
            });

            $scope.sizes = common.Size.ZoneSize;
            $scope.search = {};
            $scope.item.frequencyCapping = 0;
            $scope.item.frequencyCappingTime = 0;
            $scope.fcLimitUnit = "Minutes";

            $scope.chooseSize = (value: string) => {
                $scope.search.value = value;
            }
            $timeout(() => jQuery("#frequencyCapping").tooltip({ trigger: "focus", title: "Enter 0 to unlimit" }), 100);
            // --------------------- function --------------------------
            $scope.$watch("chooseAll", function (newVal, oldVal) {
                if ($scope.chooseAll == true) {
                    $scope.selectedCategories = [];
                    $scope.categories.forEach((cat, index) => {
                        $scope.checkedMap[cat.id] = true;
                        $scope.selectedCategories.push(cat);
                    });
                }
                else if ($scope.chooseAll == false) {
                    $scope.checkedMap = {};
                    $scope.selectedCategories = [];
                }
            });
            $scope.validateZoneName = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.item.name.length > 0)
                    return true;
                return false;
            };

            $scope.validateItemType = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.isBannerType || $scope.isTvcType || $scope.isPauseAdType)
                    return true;
                return false;
            };

            $scope.validateBannerSize = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.item.width > 0 && $scope.item.height > 0)
                    return true;
                return false;
            };

            $scope.validateRunningMode = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.bookingmode || $scope.networkmode)
                    return true;
                return false;
            };

            $scope.validateBannerPosition = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.bottom_position || $scope.top_position)
                    return true;
                return false;
            };

            $scope.validateTVCPosition = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.isPreRoll || $scope.isMidRoll || $scope.isPostRoll)
                    return true;
                return false;
            };

            $scope.validateNumber = (num: number): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if (num > 0)
                    return true;
                return false;
            };

            $scope.addTimeAppearance = () => {
                if ($scope.timeappearances.length >= 5)
                    return;
                $scope.timeappearances.push({ from: 0, duration : 0 });
            };

            $scope.removeTimeAppearance = (index: number) => {
                $scope.timeappearances.splice(index, 1);
            };

            $scope.save = () => {
                if ($scope.item.frequencyCapping && isNaN(parseInt($scope.item.frequencyCapping + ""))) return;
                if ($scope.hasSave) return;
                if ($scope.search.value) {
                    var size = $scope.search.value.split('x');
                    $scope.item.width = size[0];
                    $scope.item.height = size[1];
                }
                $scope.checkValidate = true;
                $scope.item.categories = [];
                for (var i in $scope.selectedCategories) {
                    $scope.item.categories.push($scope.selectedCategories[i].id)
                }

                if ($scope.item.name !== undefined && $scope.item.name.length === 0)
                    return;

                if (!$scope.isBannerType && !$scope.isTvcType && !$scope.isPauseAdType)
                    return;

                $scope.item.allowedType = [];
                // --- Pause Ad ---
                if ($scope.isPauseAdType) {
                    $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_PAUSE_AD);
                }

                // --- Banner ---
                if ($scope.isBannerType) {
                    if ($scope.item.width <= 0 || $scope.item.height <= 0)
                        return;

                    if ($scope.item.startTime < 0)
                        return;

                    $scope.networkmode = true; // hard code
                    if (!$scope.bookingmode && !$scope.networkmode)
                        return;

                    if (!$scope.bottom_position && !$scope.top_position)
                        return;

                    if ($scope.item.minCPC < 0 || $scope.item.minCPM < 0)
                        return;

                    $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_BANNER);
                    $scope.item.runningMode = [];
                    if ($scope.bookingmode)
                        $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_BOOKING);
                    if ($scope.networkmode)
                        $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK);

                    $scope.item.positions = [];
                    if ($scope.bottom_position)
                        $scope.item.positions.push(models.VideoZoneConst.BANNER_POSITION_MODE_BOTTOM);
                    if ($scope.top_position)
                        $scope.item.positions.push(models.VideoZoneConst.BANNER_POSITION_MODE_TOP);
                    
                    // Time Appearance
                    $scope.item.timeSegments = [];
                    for (var j: number = 0; j < $scope.timeappearances.length; j++) {
                        var from: number = $scope.timeappearances[j].from;
                        var duration: number = $scope.timeappearances[j].duration;
                        if (duration > 0)
                            $scope.item.timeSegments.push({ start: from, spend: duration });
                    }
                }

                // --- Video ---
                if ($scope.isTvcType) {
                    if ($scope.item.runningMode === undefined)
                        $scope.item.runningMode = [];

                    if (!$scope.isBannerType) { // if not Banner, add running mode
                        $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK);
                    }
                    // validate tvc type
                    if (!$scope.isPreRoll && !$scope.isMidRoll && !$scope.isPostRoll)
                        return;

                    if ($scope.item.allowedType === undefined)
                        $scope.item.allowedType = [];

                    $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_TVC);
                    $scope.item.tvcPositions = [];

                    if ($scope.isPreRoll) {
                        if ($scope.item.preSkiptime < 0 || $scope.item.preMaxDuration < 0 || $scope.item.preMaxPodDuration < 0 || $scope.item.preMinCPM < 0)
                            return;

                        $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_PREROLL);
                        var mincpmtype: string = $scope.pre_mincpmtype.toLowerCase();
                        if (mincpmtype === 'create view')
                            $scope.item.preMinCPMType = 3;
                        else if (mincpmtype === 'first quatife')
                            $scope.item.preMinCPMType = 4;
                        else if (mincpmtype === 'midpoint')
                            $scope.item.preMinCPMType = 5;
                        else if (mincpmtype === 'third quatife')
                            $scope.item.preMinCPMType = 6;
                        else if (mincpmtype === 'full video')
                            $scope.item.preMinCPMType = 7;
                    }

                    if ($scope.isMidRoll) {
                        if ($scope.item.midSkipTime < 0 || $scope.item.midMaxDuration < 0 || $scope.item.midMaxPodDuration < 0 || $scope.item.midMinCPM < 0 ||
                            $scope.item.midStartTime < 0 || $scope.item.midMaxAdpod < 0 || $scope.item.midTimeScheduled < 0)
                            return;

                        $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_MIDROLL);
                        var mincpmtype: string = $scope.mid_mincpmtype.toLowerCase();
                        if (mincpmtype === 'create view')
                            $scope.item.midMinCPMType = 3;
                        else if (mincpmtype === 'first quatife')
                            $scope.item.midMinCPMType = 4;
                        else if (mincpmtype === 'midpoint')
                            $scope.item.midMinCPMType = 5;
                        else if (mincpmtype === 'third quatife')
                            $scope.item.midMinCPMType = 6;
                        else if (mincpmtype === 'full video')
                            $scope.item.midMinCPMType = 7;
                        $scope.item.midTimeScheduledUnit = $scope.timeScheduledUnit.toLowerCase();
                        if ($scope.item.midTimeScheduledUnit === 'hour')
                            $scope.item.midTimeScheduled = $scope.item.midTimeScheduled * 3600;
                        else if ($scope.item.midTimeScheduledUnit === 'minute')
                            $scope.item.midTimeScheduled = $scope.item.midTimeScheduled * 60;

                    }

                    if ($scope.isPostRoll) {
                        if ($scope.item.postSkipTime < 0 || $scope.item.postMaxDuration < 0 || $scope.item.postMaxPodDuration < 0 || $scope.item.postMinCPM < 0)
                            return;

                        $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_POSTROLL);
                        var mincpmtype: string = $scope.post_mincpmtype.toLowerCase();
                        if (mincpmtype === 'create view')
                            $scope.item.postMinCPMType = 3;
                        else if (mincpmtype === 'first quatife')
                            $scope.item.postMinCPMType = 4;
                        else if (mincpmtype === 'midpoint')
                            $scope.item.postMinCPMType = 5;
                        else if (mincpmtype === 'third quatife')
                            $scope.item.postMinCPMType = 6;
                        else if (mincpmtype === 'full video')
                            $scope.item.postMinCPMType = 7;
                    }

                }
                if ($scope.fcLimitUnit == "Minutes")
                    $scope.item.frequencyCappingTime *= 60;
                else if ($scope.fcLimitUnit == "Hours")
                    $scope.item.frequencyCappingTime *= 3600;
                //save
                factory.zoneService.save($scope.item, function (item: models.VideoZone) {

                    $location.hash("mainForm");
                    $anchorScroll();
                    if (item.id !== undefined && item.id > 0) {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "video zone"));
                        $timeout(function () {
                            var re = BannerZoneCreateController.prototype.gotoPreState($state, $stateParams, StateStore);
                            if (!re) {
                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId });
                            }
                        }, 1000);
                        $scope.hasSave = true;
                    } else {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "banner zone"));
                    }
                });
            };

            $scope.selectCate = (category: models.Category) => {
                if ($scope.checkedMap[category.id] == false) {//unselecte
                    var i: number = $scope.selectedCategories.length;
                    while (i--) {
                        if ($scope.selectedCategories[i] === category) {
                            $scope.selectedCategories.splice(i, 1);
                            break;
                        }
                    }
                }
                if ($scope.checkedMap[category.id] == true) {
                    if ($scope.selectedCategories.indexOf(category) == -1) {
                        $scope.selectedCategories.push(category);
                    }
                }

                if ($scope.selectedCategories.length === $scope.categories.length)
                    $scope.chooseAll = true;
                else if ($scope.selectedCategories.length === 0)
                    $scope.chooseAll = false;
            };

            $scope.unselectCate = (category: models.Category) => {
                var i: number = $scope.selectedCategories.length;
                while (i--) {
                    if ($scope.selectedCategories[i] === category) {
                        $scope.selectedCategories.splice(i, 1);
                        $scope.checkedMap[category.id] = false;
                        break;
                    }
                }
                if ($scope.selectedCategories.length === 0)
                    $scope.chooseAll = false;
            };



            $scope.cancel = () => {
                var re = BannerZoneCreateController.prototype.gotoPreState($state, $stateParams, StateStore);
                if (!re) {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                }

            };

            $scope.selectCPMType = (type: string, option: string) => {
                if (type === 'pre') {
                    $scope.pre_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                } else if (type === 'mid') {
                    $scope.mid_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                } else if (type === 'post') {
                    $scope.post_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                }
            };

            $scope.selectTimeScheduleUnit = (unit: string) => {
                $scope.item.midTimeScheduledUnit = unit;
                $scope.timeScheduledUnit = common.StringUtils.capitaliseFirstLetter(unit);
            };

            $scope.chooseFC = (type: string) => {
                switch (type) {
                    case "hours":
                        $scope.fcLimitUnit = "Hours"; break;
                    case "seconds":
                        $scope.fcLimitUnit = "Seconds";
                        break;
                    case "minutes":
                        $scope.fcLimitUnit = "Minutes";
                        break;
                }
            }
        }

    }

    // shared
    export class SharedController extends PermissionController {
        constructor($scope: scopes.IUserSharedScope, $state, $stateParams, Page,
            CurrentTab: common.CurrentTab, $timeout, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            // notify change action menu
            $scope.hasSave = false;
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            $scope.siteid = $stateParams.websiteId;
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.searchText = '';
            $scope.userroles = [];
            factory.roleService.listByObject("website", function (data) {
                if (data != null) {
                    $scope.roles = data;
                }
            });

            factory.userRoleService.getRole(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.siteid, "website", function (data) {
                if (data != null) {
                    $scope.userroles = (data.data !== undefined && data.data !== null) ? data.data : [];
                    $scope.totalRecord = (data.total !== undefined && data.total !== null) ? data.total : 0;
                    $state.userroles = $scope.userroles;
                }
            });
            $scope.currentOffset = 0; $scope.count = 100; $scope.totalItems = 0;
                var user_info: models.UserInfo = factory.userInfo.getUserInfo();
            $scope.users = [];
            listUser($scope.currentOffset, $scope.count);

            function listUser(currentOffset, count) {
                factory.userService.list(currentOffset, count, function (ret: backend.ReturnList<models.UserInfo>) {
                    if (!!ret && !!ret.data) {
                        if (!!user_info)
                            $scope.users = $scope.users.concat(ret.data.filter((v, i) => v.id !== user_info.id));
                        else $scope.users = $scope.users.concat(ret.data);
                        var size = Math.min($scope.count, ret.data.length);
                        currentOffset += size;
                        $scope.totalItems = ret.total - 1;
                        if (currentOffset < ret.total)
                            listUser(currentOffset, count);
                    }
                }, 'name', 'asc');
            }

            $scope.paging = (start: number, size: number) => {
                factory.userRoleService.getRole(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.siteid, "website", function (data) {
                    if (data != null) {
                        $scope.userroles = (data.data !== undefined && data.data !== null) ? data.data : [];
                        $scope.totalRecord = (data.total !== undefined && data.total !== null) ? data.total : 0;
                        $state.userroles = $scope.userroles;
                    }
                });

            }

            $scope.hasChecked = (userid: number, roleid: number) => {
                for (var u in $scope.userroles) {
                    for (var r in $scope.userroles[u].roles) {
                        if ($scope.userroles[u].id === userid && $scope.userroles[u].roles[r].id == roleid)
                            return true;
                    }
                }
                return false;
            };

            $scope.onchange = (userid: number, roleid: number) => {
                $scope.hasSave = true;
                for (var u in $scope.userroles) {
                    for (var r in $scope.userroles[u].roles) {
                        if ($scope.userroles[u].id === userid && $scope.userroles[u].roles[r].id == roleid) {
                            //remove
                            $scope.userroles[u].roles.splice(r, 1);
                            return;
                        }
                    }
                    if ($scope.userroles[u].id === userid) {
                        //push
                        $scope.userroles[u].roles.push(new models.RoleInfo(roleid, null, null, null, null));
                        return;
                    }
                }
                return;
            };

            $scope.search = () => {
            };

            $scope.choose = (user: models.UserRoleInfo) => {
                $scope.selectUser = user;
            };

            $scope.add = () => {
                for (var u in $scope.userroles) {
                    if ($scope.userroles[u].id === $scope.selectUser.id) return;
                }
                $scope.userroles.push(new models.UserRoleInfo($scope.selectUser.id, $scope.selectUser.name, null, []));
                for (var u in $scope.users) {
                    if ($scope.users[u].id === $scope.selectUser.id)
                        $scope.users.splice(u, 1);
                }

            };

            $scope.save = () => {
                var data = [];
                for (var u in $scope.userroles) {
                    var user = {};
                    var roles = [];
                    var obj = "website";
                    for (var r in $scope.userroles[u].roles) {
                        roles.push($scope.userroles[u].roles[r].id);
                    }
                    user['userid'] = $scope.userroles[u].id;
                    user['roles'] = roles;
                    data.push(user);
                }
                factory.userRoleService.setRole(obj, $scope.siteid, JSON.stringify(data), function (result) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "assigned"));
                    $scope.hasSave = false;
                });
            };
            $state.scope = $scope;
        }
    }

    // Inventory
    export class InventoryController extends PermissionController {
        constructor($scope: scopes.IInventoryScope, $state, $stateParams, CurrentTab: common.CurrentTab, $timeout, factory: backend.Factory) {

            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            $scope.datepicker = {};
            $scope.data = null;

            var today: Date = new Date();
            today = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            $scope.datepicker['startDate'] = new Date(today.getTime() - new Date().getTimezoneOffset() * 60000);
            var endDay = new Date();
            endDay.setDate(today.getDate() + 150);
            $scope.datepicker['endDate'] = new Date(endDay.getTime() - new Date().getTimezoneOffset() * 60000);

            $scope.startTimestamp = today.getTime(); $scope.endTimestamp = endDay.getTime();
            factory.bookService.getBookedByKind($stateParams.websiteId, models.EBookingKind.WEBSITE, today.getTime(), endDay.getTime(), function (booked: Array<models.BookRecord>) {
                $scope.data = null;
                if (booked.length === 0)
                    return;
                var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneBookingSchedule(booked);
                $scope.data = { type: "zone_booking_schedule", data: zoneSchedules, minDate: $scope.startTimestamp, maxDate: $scope.endTimestamp };

            });

            $scope.view = () => {
                $scope.startTimestamp = $scope.datepicker['startDate'].getTime() + new Date().getTimezoneOffset() * 60000;
                $scope.endTimestamp = $scope.datepicker['endDate'].getTime() + new Date().getTimezoneOffset() * 60000;

                factory.bookService.getBookedByKind($stateParams.websiteId, models.EBookingKind.WEBSITE, $scope.startTimestamp, $scope.endTimestamp, function (booked: Array<models.BookRecord>) {
                    $scope.data = null;
                    if (booked.length === 0)
                        return;
                    var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneBookingSchedule(booked);
                    $scope.data = { type: "zone_booking_schedule", data: zoneSchedules, minDate: $scope.startTimestamp, maxDate: $scope.endTimestamp };

                });
            };

            $scope.showInventory = (): boolean => {
                if ($scope.data === null)
                    return false;
                return true;
            };
        }
    }


    /*
     *  ------------- LINKED ITEM -----------------------------
     */
    export class LinkedItemListController {
        constructor($scope: scopes.IListLinkedItemScope, $state, $stateParams, $modal, CurrentTab: common.CurrentTab, factory: backend.Factory) {

            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            $scope.pageIndex = 1; $scope.pageSize = 10; 
            $scope.checkBoxes = {}; $scope.items = []; $scope.checkBoxes['all'] = false;
            $scope.isChosen = false; $scope.isMultiChoice = false;

            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            var zoneId = $stateParams.zoneId;
            $scope.previewContent = ''; $scope.previewSource = ''; $scope.previewTitle = ''; $scope.contentDic = {};
            $scope.sizeDic = {}; $scope.campaignDic = {};
            $scope.positionDic = {};
            factory.campaignItemService.getItemsByZoneId(zoneId, ($scope.pageIndex - 1) * $scope.pageSize,
                $scope.pageSize, function (ret) {
                    $scope.items = ret.data;
                    $scope.totalRecord = ret.data.length;

                    var campIds: Array<number> = [];
                    for (var i: number = 0; i < $scope.items.length; i++) {
                        $scope.checkBoxes[$scope.items[i].id] = false;
                        var campaignId: number = $scope.items[i].campaignId;
                        if (campIds.indexOf(campaignId) < 0) {
                            campIds.push(campaignId);
                        }

                        var id: number = $scope.items[i].id;
                        $scope.sizeDic[id] = { width: $scope.items[i].width, height: $scope.items[i].height };
                        if (models.CampaignItemType.isBannerNotExpandable($scope.items[i].kind)) {
                            var mediaItem: models.MediaItem = ret.data[i];
                            $scope.contentDic[id] = mediaItem.bannerFile;
                        } else if (models.CampaignItemType.isBannerWithExpandable($scope.items[i].kind)) {
                            var expandableItem: models.ExpandableItem = ret.data[i];
                            $scope.contentDic[id] = expandableItem.standardFile;
                        } else if (models.CampaignItemType.isHTML($scope.items[i].kind)) {
                            var htmlItem: models.HtmlItem = ret.data[i];
                            $scope.contentDic[id] = htmlItem.embeddedHtml;
                        } else if ($scope.items[i].kind === models.CampaignItemType.NETWORK.TVC) {
                            var tvcItem: models.TvcItem = ret.data[i];
                            $scope.contentDic[id] = tvcItem.tvcFile;
                        }

                        if ($scope.items[i].positions) {
                            $scope.positionDic[id] = [];
                            $scope.items[i].positions.forEach((pos) => {
                                if ($scope.positionDic[id].indexOf(pos["position"]) == -1)
                                    $scope.positionDic[id].push(pos["position"]);
                            });
                            $scope.positionDic[id] = $scope.positionDic[id].map((p) => p = utils.StringUtils.firstCaseLetter(p));
                        }
                    }

                    if (campIds.length > 0) {
                        factory.campaignService.listByIds(campIds, function (campaignList: Array<models.Campaign>) {
                            for (var i: number = 0, len: number = campaignList.length; i < len; i++) {
                                $scope.campaignDic[campaignList[i].id] = campaignList[i];
                            }
                        });
                    }
                });

            factory.zoneService.load(zoneId, (zone: models.VideoZone) => {
                if (zone.allowedType) {
                    $scope.isVideoZone = zone.allowedType.indexOf("tvc") !== -1;
                }
            })

            ///--------Scope function
            $scope.formatCurrentUsage = (currentUsage: number): string => {
                if (currentUsage === -1)
                    return "Network";
                return currentUsage.toString() + "%";
            };

            $scope.getCampaignName = (campaignId: number): string => {
                if ($scope.campaignDic[campaignId]) {
                    return $scope.campaignDic[campaignId].name;
                }
                return "";
            };

            $scope.getSource = (url: string): string => {
                if (url !== undefined && url.length > 0)
                    return url;
                return "";
            };
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            };

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            };

            $scope.gotoItem = (itemId: number) => {
                factory.campaignItemService.load(itemId, function (item: models.AdItem) {
                    if (item) {
                        factory.campaignService.load(item.campaignId, function (campaign: models.Campaign) {
                            if (campaign) {
                                $state.transitionTo('main.order.item_detail.setting', { orderId: campaign.orderId, campaignId: item.campaignId, itemId: itemId });
                            }
                        });
                    }
                });
                
            };

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen && $scope.checkBoxes['all'] === false && $scope.isMultiChoice === false)
                    return "";
                return "disabled";
            };

            $scope.check = (itemId: number) => {

                $scope.isChosen = false;
                $scope.isMultiChoice = false;
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        if ($scope.isChosen === false) {
                            $scope.isChosen = true;
                        } else {
                            $scope.isMultiChoice = true;
                        }
                    }
                }

            };

            $scope.isBannerImg = (filename: string): boolean => {
                if (!filename || (filename && filename.length === 0))
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "png" || ext === "jpeg" || ext === "jpg" || ext === "gif")
                        return true;
                }
                return false;
            };

            $scope.isBannerFlash = (filename: string): boolean => {
                if (!filename || (filename && filename.length === 0))
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };
            $scope.paging = (start: number, size: number) => {
                factory.campaignItemService.getItemsByZoneId(zoneId, start, size, function (ret) {
                    $scope.items = ret.data;
                    $scope.totalRecord = ret.total;
                });
            };

            $scope.getKind = (kind: string): string => {
                if (models.CampaignItemType.isBanner(kind))
                    return 'img';
                if (models.CampaignItemType.isHTML(kind))
                    return 'html';
                if (kind === models.CampaignItemType.NETWORK.TVC)
                    return 'tvc';
                return '';
            };

            $scope.view = (id: number, name: string, kind: string) => {
                var content: string = $scope.contentDic[id];

                if (models.CampaignItemType.isBanner(kind)) {
                    $scope.previewTitle = name;
                    $scope.previewSource = content;
                    $scope.previewWidth = $scope.sizeDic[id]["width"];
                    $scope.previewHeight= $scope.sizeDic[id]["height"];
                    jQuery("#previewBanner").modal('show');
                    fixPreviewPosition("#previewBanner");
                } else if (models.CampaignItemType.isHTML(kind)) {
                    $scope.previewContent = content;
                    $scope.previewWidth = $scope.sizeDic[id]["width"] + 20;
                    $scope.previewHeight = $scope.sizeDic[id]["height"] + 30;
                    $scope.previewTitle = name;
                    jQuery("#previewHtml").modal('show');
                    fixPreviewPosition("#previewHtml");
                } else if (kind === models.CampaignItemType.NETWORK.TVC) {
                    $scope.previewSource = ""; $scope.previewTitle = name;
                    if (content.length === 0) {
                        jQuery("#previewExternalVideo").modal('show');
                        fixPreviewPosition("#previewExternalVideo");
                        return;
                    }
                    $scope.previewSource = content;
                    flowplayer("player", "resource/flash/flowplayer-3.2.16.swf", $scope.previewSource);
                    jQuery("#previewVideo").modal('show');
                    fixPreviewPosition("#previewVideo");
                }
            };

        }
    }

    /*
     *  ------------- LIST ARTICLES -----------------------------
     */
    //export class ArticlesListController extends PermissionController {
        
    //}

    export class ZoneSettingController extends PermissionController {
        constructor($scope: scopes.IZoneSettingScope, $stateParams, $state, $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            $scope.getPermission("website", $stateParams.websiteId);
            super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            $scope.networkRunningMode = false; $scope.bookingRunningMode = false;
            $scope.checkValidate = false;
            $scope.checkedMap = {}; $scope.selectedCategories = [];
            var zoneId = $stateParams.zoneId;
            $scope.isBannerZone = false; $scope.isVideoZone = false;
            $scope.siteId = $stateParams.websiteId; 
            // video
            $scope.isBannerType = false; $scope.isTvcType = false; $scope.isPauseAdType = false;
            $scope.bookingmode = false; $scope.networkmode = false;
            $scope.bottom_position = false; $scope.top_position = false;
            $scope.isPreRoll = false; $scope.isMidRoll = false; $scope.isPostRoll = false;
            $scope.pre_mincpmtype = ''; $scope.mid_mincpmtype = ''; $scope.post_mincpmtype = '';
            $scope.timeScheduledUnit = '';

            $scope.sizes = common.Size.ZoneSize;
            $scope.search = {};
                $scope.timeappearances = [];
                $scope.zone_style = "Normal";
            $scope.chooseSize = (value: string) => {
                $scope.search.value = value;
            }
            $timeout(() => jQuery("#frequencyCapping").tooltip({ trigger: "focus", title: "Enter 0 to unlimit" }), 100);


            factory.categoryService.listAll(function (categories) {
                $scope.categories = categories;                
                factory.zoneService.load(zoneId, function (item) {
                    $scope.item = item;                    
                    $scope.search.value = $scope.item.width + 'x' + $scope.item.height;
                    if ($scope.item.kind === "banner")
                        $scope.isBannerZone = true;
                    if ($scope.item.kind === "video")
                        $scope.isVideoZone = true;
                    if ($scope.item.kind === "pr")
                        $scope.isPrZone = true;

                    if ($scope.item.renderKind !== undefined) {
                        if ($scope.item.renderKind === models.EZoneRenderKind.NORMAL)
                            $scope.zone_style = "Normal";
                        else
                            $scope.zone_style = common.StringUtils.capitaliseFirstLetter($scope.item.renderKind);
                    }

                    //listCategoriesById
                    for (var selectCate in item.categories) {
                        for (var cate in $scope.categories) {
                            if (item.categories[selectCate] === $scope.categories[cate].id) {
                                $scope.selectedCategories.push($scope.categories[cate]);
                                $scope.checkedMap[$scope.categories[cate].id] = true;
                                break;
                            }
                        }
                    }
                    $scope.item.categories = $scope.selectedCategories;
                    $scope.selectedCategories = $scope.item.categories;

                    if ($scope.isBannerZone) { // Banner zone
                        for (var i: number = 0; i < $scope.item.runningMode.length; i++) {
                            if ($scope.item.runningMode[i] === 'network')
                                $scope.networkRunningMode = true;
                            if ($scope.item.runningMode[i] === 'booking')
                                $scope.bookingRunningMode = true;
                        }
                    }

                    if ($scope.isVideoZone) { // Video zone

                        for (var i: number = 0; i < $scope.item.allowedType.length; i++) {
                            if ($scope.item.allowedType[i] === models.VideoZoneConst.ALLOWED_TYPE_BANNER)
                                $scope.isBannerType = true;
                            if ($scope.item.allowedType[i] === models.VideoZoneConst.ALLOWED_TYPE_TVC)
                                $scope.isTvcType = true;
                            if ($scope.item.allowedType[i] === models.VideoZoneConst.ALLOWED_TYPE_PAUSE_AD)
                                $scope.isPauseAdType = true;
                        }

                        if ($scope.isBannerType) {
                            // Running mode
                            for (var i: number = 0; i < $scope.item.runningMode.length; i++) {
                                if ($scope.item.runningMode[i] === models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK)
                                    $scope.networkmode = true;
                                if ($scope.item.runningMode[i] === models.VideoZoneConst.BANNER_RUNNING_MODE_BOOKING)
                                    $scope.bookingmode = true;
                            }

                            // Banner position
                            for (var i: number = 0; i < $scope.item.positions.length; i++) {
                                if ($scope.item.positions[i] === models.VideoZoneConst.BANNER_POSITION_MODE_BOTTOM)
                                    $scope.bottom_position = true;
                                if ($scope.item.positions[i] === models.VideoZoneConst.BANNER_POSITION_MODE_TOP)
                                    $scope.top_position = true;
                            }

                            // Time appearance
                            if ($scope.item.timeSegments !== undefined) {
                                for (var i: number = 0; i < $scope.item.timeSegments.length; i++) {
                                    var from: number = $scope.item.timeSegments[i].start;
                                    var duration: number = $scope.item.timeSegments[i].spend;
                                    $scope.timeappearances.push({ from: from, duration: duration });
                                }
                            }
                            
                        }

                        if ($scope.isTvcType) {
                            // TVC positions
                            for (var i: number = 0; i < $scope.item.tvcPositions.length; i++) {
                                if ($scope.item.tvcPositions[i] === models.VideoZoneConst.TVC_POSITION_PREROLL)
                                    $scope.isPreRoll = true;
                                if ($scope.item.tvcPositions[i] === models.VideoZoneConst.TVC_POSITION_MIDROLL)
                                    $scope.isMidRoll = true;
                                if ($scope.item.tvcPositions[i] === models.VideoZoneConst.TVC_POSITION_POSTROLL)
                                    $scope.isPostRoll = true;
                            }

                            if ($scope.isPreRoll) {
                                $scope.pre_mincpmtype = common.StringUtils.capitaliseFirstLetter(models.VideoZoneConst.getMinCPMType($scope.item.preMinCPMType));
                            }

                            if ($scope.isMidRoll) {
                                $scope.mid_mincpmtype = common.StringUtils.capitaliseFirstLetter(models.VideoZoneConst.getMinCPMType($scope.item.midMinCPMType));
                                $scope.timeScheduledUnit = common.StringUtils.capitaliseFirstLetter($scope.item.midTimeScheduledUnit);

                                if ($scope.item.midTimeScheduledUnit === 'hour')
                                    $scope.item.midTimeScheduled = Math.floor($scope.item.midTimeScheduled / 3600);
                                else if ($scope.item.midTimeScheduledUnit === 'minute')
                                    $scope.item.midTimeScheduled = Math.floor($scope.item.midTimeScheduled / 60);
                            }

                            if ($scope.isPostRoll) {
                                $scope.post_mincpmtype = common.StringUtils.capitaliseFirstLetter(models.VideoZoneConst.getMinCPMType($scope.item.postMinCPMType));
                            }
                        }

                        if ($scope.timeappearances.length === 0)
                            $scope.timeappearances.push({ from: 0, duration: 0 });

                    }
                    $scope.fcLimitUnit = "Seconds"
                    if ($scope.item.frequencyCappingTime) {
                            if ($scope.item.frequencyCappingTime % 60 === 0) {
                                $scope.item.frequencyCappingTime /= 60;
                                $scope.fcLimitUnit = "Minutes"
                        }
                            if ($scope.item.frequencyCappingTime % 60 === 0) {
                                $scope.item.frequencyCappingTime /= 60;
                                $scope.fcLimitUnit = "Hours"
                        }
                    }
                });
            });

            // ------------- function -------------
            $scope.addTimeAppearance = () => {
                if ($scope.timeappearances.length >= 5)
                    return;
                $scope.timeappearances.push({ from: 0, duration: 0 });
            };

            $scope.chooseStyle = (style: string) => {
                if (style === 'normal')
                    $scope.item.renderKind = models.EZoneRenderKind.NORMAL;
                else if (style === 'catfish')
                    $scope.item.renderKind = models.EZoneRenderKind.CATFISH;
                else if (style === 'balloon')
                    $scope.item.renderKind = models.EZoneRenderKind.BALLOON;
                else if (style === 'popup')
                    $scope.item.renderKind = models.EZoneRenderKind.POPUP;
                else if (style === 'banner_skin')
                    $scope.item.renderKind = models.EZoneRenderKind.BANNER_SKIN;
                $scope.zone_style = common.StringUtils.capitaliseFirstLetter(style);
            };

            $scope.removeTimeAppearance = (index: number) => {
                $scope.timeappearances.splice(index, 1);
            };

            $scope.isNetwork = (): boolean => {
                return $scope.networkRunningMode;
            };

            $scope.validateNumber = (num: number): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if (num > 0)
                    return true;
                return false;
            };

            $scope.validateTvcBannerPosition = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.bottom_position || $scope.top_position)
                    return true;
                return false;
            };

            $scope.validateTvcBannerRunningMode = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.bookingmode || $scope.networkmode)
                    return true;
                return false;
            };

            $scope.validateTvcItemType = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.isBannerType || $scope.isTvcType || $scope.isPauseAdType)
                    return true;
                return false;
            };

            $scope.validateTvcPosition = (): boolean => {
                if (!$scope.checkValidate)
                    return true;

                if ($scope.isPreRoll || $scope.isMidRoll || $scope.isPostRoll)
                    return true;
                return false;
            };

            $scope.saveBannerZone = (form: any) => {
                if ($scope.item.frequencyCapping && isNaN(parseInt($scope.item.frequencyCapping))) return;
                $scope.checkValidate = true;
                var size = $scope.search.value.split('x');
                $scope.item.width = size[0];
                $scope.item.height = size[1];
                if ($scope.isBannerZone) {
                    // banner
                    if ($scope.item.name.length === 0)
                        return;
                    if ($scope.item.width == 0 || $scope.item.height == 0)
                        return;
                    if (!$scope.bookingRunningMode && !$scope.networkRunningMode)
                        return;

                    if ($scope.networkRunningMode && ($scope.item.minCPC == 0 || $scope.item.minCPM == 0))
                        return;
                    var runningMode: Array<string> = [];
                    if ($scope.networkRunningMode)
                        runningMode.push('network');
                    if ($scope.bookingRunningMode)
                        runningMode.push('booking');

                    $scope.item.runningMode = runningMode.toString();
                    $scope.item.siteId = $stateParams.websiteId;
                    $scope.item.categories = []
                    for (var cate in $scope.selectedCategories) {
                        $scope.item.categories.push($scope.selectedCategories[cate].id)
                    }

                    if ($scope.fcLimitUnit == "Minutes")
                        $scope.item.frequencyCappingTime *= 60;
                    else if ($scope.fcLimitUnit == "Hours")
                        $scope.item.frequencyCappingTime *= 3600;

                    factory.zoneService.update($scope.item, function (ret) {
                        if (ret !== undefined && ret === 'success') {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "zone"));
                            $timeout(function () {

                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }, 100);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zone"));
                    }, function (msg, status) {
                        });
                }

                if ($scope.isPrZone) {                    
                    // pr zone
                    if ($scope.item.name.length === 0)
                        return;
                    
                    var runningMode: Array<string> = [];
                    runningMode.push('');

                    $scope.item.runningMode = runningMode.toString();
                    $scope.item.siteId = $stateParams.websiteId;
                    $scope.item.categories = []                    
                    factory.zoneService.update($scope.item, function (ret) {
                        if (ret !== undefined && ret === 'success') {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "zone"));
                            $timeout(function () {

                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }, 100);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zone"));
                    }, function (msg, status) {
                        });
                }

                if ($scope.isVideoZone) {
                    // VIDEO
                    if ($scope.item.name.length === 0)
                        return;
                    if (!$scope.isBannerType && !$scope.isPauseAdType && !$scope.isTvcType)
                        return;
                    
                    if ($scope.isBannerType) {
                        if ($scope.item.width <= 0 || $scope.item.height <= 0)
                            return;

                        if (!$scope.bookingmode && !$scope.networkmode)
                            return;

                        if (!$scope.bottom_position && !$scope.top_position)
                            return;

                        if ($scope.item.minCPC <= 0 || $scope.item.minCPM <= 0)
                            return;
                    }

                    if ($scope.isTvcType) {
                        if (!$scope.isPreRoll && !$scope.isMidRoll && !$scope.isPostRoll)
                            return;

                        if ($scope.isPreRoll) {
                            if ($scope.item.preSkiptime < 0 || $scope.item.preMaxDuration < 0 || $scope.item.preMaxPodDuration < 0 || $scope.item.preMinCPM < 0)
                                return;
                        }

                        if ($scope.isMidRoll) {
                            if ($scope.item.midSkipTime < 0 || $scope.item.midMaxDuration < 0 || $scope.item.midMaxPodDuration < 0 || $scope.item.midMinCPM < 0 ||
                                $scope.item.midStartTime < 0 || $scope.item.midMaxAdpod < 0 || $scope.item.midTimeScheduled < 0)
                                return;
                        }

                        if ($scope.isPostRoll) {
                            if ($scope.item.postSkipTime < 0 || $scope.item.postMaxDuration < 0 || $scope.item.postMaxPodDuration < 0 || $scope.item.postMinCPM < 0)
                                return;
                        }
                    }

                    $scope.item.categories = [];
                    for (var cate in $scope.selectedCategories) {
                        $scope.item.categories.push($scope.selectedCategories[cate].id)
                    }

                    $scope.item.allowedType = [];
                    if (!$scope.isBannerType) {
                        // reset
                        $scope.item.runningMode = [];
                        $scope.item.positions = [];
                        $scope.item.maxAdsDuration = 0;
                        $scope.item.maxAdPodDuration = 0;
                        $scope.item.minCPC = 0; $scope.item.minCPM = 0;
                        $scope.item.width = 0; $scope.item.height = 0;
                        $scope.item.startTime = 0;
                        $scope.item.timeSegments = [];
                    }

                    if (!$scope.isTvcType) {
                        // reset
                        $scope.item.tvcPositions = [];
                        $scope.item.preSkiptime = 0;
                        $scope.item.preMaxDuration = 0;
                        $scope.item.preMaxPodDuration = 0;
                        $scope.item.preMinCPM = 0;
                        $scope.item.preMinCPMType = 0;
                        $scope.item.midSkipTime = 0;
                        $scope.item.midMaxDuration = 0;
                        $scope.item.midMaxPodDuration = 0;
                        $scope.item.midMinCPM = 0;
                        $scope.item.midMinCPMType = 0;
                        $scope.item.midStartTime = 0;
                        $scope.item.midMaxAdpod = 0;
                        $scope.item.midTimeScheduled = 0;
                        $scope.item.midTimeScheduledUnit = "";
                        $scope.item.postSkipTime = 0;
                        $scope.item.postMaxDuration = 0;
                        $scope.item.postMaxPodDuration = 0;
                        $scope.item.postMinCPM = 0;
                        $scope.item.postMinCPMType = 0;
                    }

                    if ($scope.isPauseAdType) {
                        $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_PAUSE_AD);
                    }

                    if ($scope.isBannerType) { // BANNER
                        $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_BANNER);

                        // running mode
                        $scope.item.runningMode = [];
                        if ($scope.bookingmode)
                            $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_BOOKING);
                        if ($scope.networkmode)
                            $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK);

                        // position
                        $scope.item.positions = [];
                        if ($scope.bottom_position)
                            $scope.item.positions.push(models.VideoZoneConst.BANNER_POSITION_MODE_BOTTOM);
                        if ($scope.top_position)
                            $scope.item.positions.push(models.VideoZoneConst.BANNER_POSITION_MODE_TOP);

                        // Time appearance
                        $scope.item.timeSegments = [];
                        for (var i: number = 0; i < $scope.timeappearances.length; i++) {
                            var from: number = $scope.timeappearances[i].from;
                            var duration: number = $scope.timeappearances[i].duration;
                            if (duration > 0)
                                $scope.item.timeSegments.push({ start: from, spend: duration });
                        }
                    }

                    if ($scope.isTvcType) {  // TVC
                        $scope.item.allowedType.push(models.VideoZoneConst.ALLOWED_TYPE_TVC);

                        if ($scope.item.runningMode.length === 0 || $scope.item.runningMode.indexOf(models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK) < 0) // default TVC is in Network Running mode
                            $scope.item.runningMode.push(models.VideoZoneConst.BANNER_RUNNING_MODE_NETWORK);

                        // tvc positon
                        $scope.item.tvcPositions = [];
                        if ($scope.isPreRoll)
                            $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_PREROLL);
                        if ($scope.isMidRoll)
                            $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_MIDROLL);
                        if ($scope.isPostRoll)
                            $scope.item.tvcPositions.push(models.VideoZoneConst.TVC_POSITION_POSTROLL);

                        if (!$scope.isPreRoll) {
                            // reset
                            $scope.item.preSkiptime = 0;
                            $scope.item.preMaxDuration = 0;
                            $scope.item.preMaxPodDuration = 0;
                            $scope.item.preMinCPM = 0;
                            $scope.item.preMinCPMType = 0;
                        }

                        if (!$scope.isMidRoll) {
                            // reset
                            $scope.item.midSkipTime = 0;
                            $scope.item.midMaxDuration = 0;
                            $scope.item.midMaxPodDuration = 0;
                            $scope.item.midMinCPM = 0;
                            $scope.item.midMinCPMType = 0;
                            $scope.item.midStartTime = 0;
                            $scope.item.midMaxAdpod = 0;
                            $scope.item.midTimeScheduled = 0;
                            $scope.item.midTimeScheduledUnit = "";
                        }

                        if (!$scope.isPostRoll) {
                            // reset
                            $scope.item.postSkipTime = 0;
                            $scope.item.postMaxDuration = 0;
                            $scope.item.postMaxPodDuration = 0;
                            $scope.item.postMinCPM = 0;
                            $scope.item.postMinCPMType = 0;
                        }

                        if ($scope.isPreRoll) {
                            $scope.item.preMinCPMType = models.VideoZoneConst.changeMinCPMType($scope.pre_mincpmtype.toLowerCase());
                        }

                        if ($scope.isMidRoll) {
                            $scope.item.midMinCPMType = models.VideoZoneConst.changeMinCPMType($scope.mid_mincpmtype.toLowerCase());
                            $scope.item.midTimeScheduledUnit = $scope.timeScheduledUnit.toLowerCase();

                            if ($scope.item.midTimeScheduledUnit === 'hour')
                                $scope.item.midTimeScheduled = $scope.item.midTimeScheduled * 3600;
                            else if ($scope.item.midTimeScheduledUnit === 'minute')
                                $scope.item.midTimeScheduled = $scope.item.midTimeScheduled * 60;
                        }

                        if ($scope.isPostRoll) {
                            $scope.item.postMinCPMType = models.VideoZoneConst.changeMinCPMType($scope.post_mincpmtype.toLowerCase());
                        }
                    }
                    if ($scope.fcLimitUnit == "Minutes")
                        $scope.item.frequencyCappingTime *= 60;
                    else if ($scope.fcLimitUnit == "Hours")
                        $scope.item.frequencyCappingTime *= 3600;
                    
                    factory.zoneService.update($scope.item, function (ret) {
                        if (ret !== undefined && ret === 'success') {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "zone"));
                            $timeout(function () {

                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }, 100);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "zone"));
                    }, function (msg, status) {
                        });
                }
            };

            $scope.selectCate = (category: models.Category) => {
                if ($scope.checkedMap[category.id] == false) {//unselecte
                    var i: number = $scope.selectedCategories.length;
                    while (i--) {
                        if ($scope.selectedCategories[i] === category) {
                            $scope.selectedCategories.splice(i, 1);
                            break;
                        }
                    }
                }
                if ($scope.checkedMap[category.id] == true) {
                    if ($scope.selectedCategories.indexOf(category) == -1) {
                        $scope.selectedCategories.push(category);
                    }
                }
            };

            $scope.unselectCate = (category: models.Category) => {
                var i: number = $scope.selectedCategories.length;
                while (i--) {
                    if ($scope.selectedCategories[i] === category) {
                        $scope.selectedCategories.splice(i, 1);
                        $scope.checkedMap[category.id] = false;
                        break;
                    }
                }
            };

            $scope.cancel = () => {
                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
            };

            $scope.selectCPMType = (type: string, option: string) => {
                if (type === 'pre') {
                    $scope.pre_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                } else if (type === 'mid') {
                    $scope.mid_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                } else if (type === 'post') {
                    $scope.post_mincpmtype = common.StringUtils.capitaliseFirstLetter(option);
                }
            };

            $scope.selectTimeScheduleUnit = (unit: string) => {
                $scope.item.midTimeScheduledUnit = unit;
                $scope.timeScheduledUnit = common.StringUtils.capitaliseFirstLetter(unit);
            };

            $scope.chooseFC = (type: string) => {
                switch (type) {
                    case "hours":
                        $scope.fcLimitUnit = "Hours"; break;
                    case "seconds":
                        $scope.fcLimitUnit = "Seconds";
                        break;
                    case "minutes":
                        $scope.fcLimitUnit = "Minutes";
                        break;
                }
            }
        }
    }

    export class ZoneBookingScheduleController extends PermissionController {
        constructor($scope: scopes.IZoneBookingScheduleScope, $state, $stateParams, CurrentTab: common.CurrentTab, $timeout, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
            super($scope, $state, factory);
            CurrentTab.setTab(new common.Tab("website", "list_sites"));
            $scope.$emit('change_action_menu', [$stateParams.websiteId]);
            $scope.datepicker = {};

            var today: Date = new Date();
            today = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            $scope.datepicker['startDate'] = new Date(today.getTime() - new Date().getTimezoneOffset() * 60000);
            var endDay = new Date();
            endDay.setDate(today.getDate() + 150);
            $scope.datepicker['endDate'] = new Date(endDay.getTime() - new Date().getTimezoneOffset() * 60000);

            $scope.startTimestamp = today.getTime(); $scope.endTimestamp = endDay.getTime();
            factory.bookService.getBookedByKind($stateParams.zoneId, models.EBookingKind.ZONE, 0, 0, function (booked: Array<models.BookRecord>) {
                var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getItemZoneBookingSchedule(booked);
                $scope.data = { type: "item_zone_booking_schedule", data: zoneSchedules, minDate: $scope.startTimestamp, maxDate: $scope.endTimestamp };

            });

            $scope.view = () => {
                $scope.startTimestamp = $scope.datepicker['startDate'].getTime() + new Date().getTimezoneOffset() * 60000;
                $scope.endTimestamp = $scope.datepicker['endDate'].getTime() + new Date().getTimezoneOffset() * 60000;

                factory.bookService.getBookedByKind($stateParams.zoneId, models.EBookingKind.ZONE, $scope.startTimestamp, $scope.endTimestamp, function (booked: Array<models.BookRecord>) {
                    var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneBookingSchedule(booked);
                    $scope.data = { type: "item_zone_booking_schedule", data: zoneSchedules, minDate: $scope.startTimestamp, maxDate: $scope.endTimestamp };

                });
            };
        }
    }

    //create pr zone controller 
    export class ZonePRCreateController extends PermissionController {

        constructor($scope: scopes.IBannerZoneScope, $stateParams, $state, $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab,
            StateStore: utils.DataStore, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

            $scope.checkValidate = false;
            $scope.item = new models.BannerZone(0, '', $stateParams.websiteId, [], 0, 0, [], "", 0, 0);
            $scope.item.width = 0; $scope.item.height = 0;
            $scope.bookingRunningMode = false; $scope.networkRunningMode = false;
            $scope.checkedMap = {}; $scope.selectedCategories = [];
            CurrentTab.setTab(new common.Tab("website", "list_sites"));            

            $scope.saveBannerZone = (form: any) => {                
                if ($scope.hasSave) return;
                $scope.checkValidate = true;
                //var size = $scope.search.value.split('x');
                $scope.item.width = 468;
                $scope.item.height = 60;
                if ($scope.item.name.length === 0)
                    return;                
                
                var runningMode: string[] = [];
                runningMode.push('network');                
                
                $scope.item.runningMode = runningMode.toString();
                $scope.item.siteId = $stateParams.websiteId;
                $scope.item.categories = []
                $scope.item.categories.push(1)                

                factory.zoneService.save($scope.item, function (ret) {                              
                    if (ret === undefined) {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "zone"));
                        return;
                    }
                    if (ret.id > 0) {

                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "zone"));

                        $timeout(function () {                            
                            var re = BannerZoneCreateController.prototype.gotoPreState($state, $stateParams, StateStore);
                            if (!re) {
                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }

                        }, 100);
                        $scope.hasSave = true;
                        return;
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "zone"));
                });
            };            

            $scope.cancel = () => {
                if (!this.gotoPreState($state, $stateParams, StateStore)) {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                }
            };
        }
        gotoPreState($state, $stateParams, StateStore: utils.DataStore) {
            if (StateStore.get('main.website.zonegroup_create') === true) {
                StateStore.remove('main.website.zonegroup_create');
                $state.transitionTo('main.website.zonegroup_create', { websiteId: $stateParams.websiteId });
                return true;
            }
            if (StateStore.get('main.website.zonegroup_detail.setting') === true) {
                StateStore.remove('main.website.zonegroup_detail.setting');
                var item: models.ZoneGroup = StateStore.get('item');
                if (item !== undefined) {
                    $state.transitionTo('main.website.zonegroup_detail.setting', { websiteId: $stateParams.websiteId, zoneGroupId: item.id });
                    return true;
                }
            }
            return false;
        }
    }

    //create pr zone controller 
    export class PrZoneCreateController {
        constructor($scope: scopes.IBannerZoneScope, $stateParams, $state, $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab,
            StateStore: utils.DataStore, factory: backend.Factory) {

            $scope.checkValidate = false;
            $scope.item = new models.BannerZone(0, '', $stateParams.websiteId, [], 0, 0, [], "", 0, 0);
            $scope.item.width = 0; $scope.item.height = 0;
            $scope.bookingRunningMode = false; $scope.networkRunningMode = false;
            $scope.checkedMap = {}; $scope.selectedCategories = [];
            CurrentTab.setTab(new common.Tab("website", "list_sites"));

            $scope.saveBannerZone = (form: any) => {
                if ($scope.hasSave) return;
                $scope.checkValidate = true;
                //var size = $scope.search.value.split('x');
                $scope.item.width = 100;
                $scope.item.height = 100;
                if ($scope.item.name.length === 0)
                    return;

                var runningMode: string[] = [];
                runningMode.push('');

                $scope.item.runningMode = runningMode.toString();
                $scope.item.siteId = $stateParams.websiteId;
                $scope.item.categories = []
                //$scope.item.categories.push(1)
                $scope.item.kind = 'pr';
                
                factory.zoneService.save($scope.item, function (ret) {
                    if (ret === undefined) {
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "zone"));
                        return;
                    }
                    if (ret.id > 0) {

                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "zone"));

                        $timeout(function () {
                            var re = BannerZoneCreateController.prototype.gotoPreState($state, $stateParams, StateStore);
                            if (!re) {
                                $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                            }

                        }, 100);
                        $scope.hasSave = true;
                        return;
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "zone"));
                });
            };

            $scope.cancel = () => {
                if (!this.gotoPreState($state, $stateParams, StateStore)) {
                    $state.transitionTo('main.website.detail.zone', { websiteId: $stateParams.websiteId, page: 1 });
                }
            };
        }
        gotoPreState($state, $stateParams, StateStore: utils.DataStore) {
            if (StateStore.get('main.website.zonegroup_create') === true) {
                StateStore.remove('main.website.zonegroup_create');
                $state.transitionTo('main.website.zonegroup_create', { websiteId: $stateParams.websiteId });
                return true;
            }
            if (StateStore.get('main.website.zonegroup_detail.setting') === true) {
                StateStore.remove('main.website.zonegroup_detail.setting');
                var item: models.ZoneGroup = StateStore.get('item');
                if (item !== undefined) {
                    $state.transitionTo('main.website.zonegroup_detail.setting', { websiteId: $stateParams.websiteId, zoneGroupId: item.id });
                    return true;
                }
            }
            return false;
        }
    }


}
