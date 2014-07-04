
module controllers {
    'use strict';
    export class RootController {
        constructor($scope: scopes.IRootScope, Page: common.PageUtils, BodyClass, $state) {

            $scope.page = Page;
            $scope.bodyClass = BodyClass;

            $scope.isLoginPage = (): boolean => {
                if (BodyClass.getClass() === 'login') {
                    return true;
                }
                return false;
            };

            $scope.getBodyClass = (): string => {
                if (BodyClass.getClass() === 'login') {
                    return 'login';
                }
                return '';
            };
        }
    }
    export class ModalAlertController {
        constructor($scope, $modalInstance,title, body) {
            $scope.bodyMessage = title;
            $scope.bodyMessage = body;
            
           
            $scope.cancel = () => {
                $modalInstance.dismiss('message');
            }
        }
    }
    export class MainController{
        constructor($scope: scopes.IMainScope, $state, $stateParams, $location,
            CurrentTab: common.CurrentTab, Page: common.PageUtils, $modal, factory: backend.Factory) {

            $scope.permissionDefine = new models.PermissionDefine();
            factory.permissionUtils.getPermission(utils.PermissionUtils.WEBSITE, 0, function (ret) {
                if (ret)
                    $scope.currentPermission = ret;
            });
            $scope.username = "unknown";
            var userinfo: models.UserInfo = factory.userInfo.getUserInfo();
            if (userinfo !== null && userinfo !== undefined) {
                $scope.username = userinfo.name;
            } else {
                var sessionid: string = common.Utils.getCookie('123click');
                if (sessionid.length > 0) {
                    factory.sessionService.getCurrentUser(sessionid, function (result) {
                        if (result.code === 1) {
                            factory.userInfo.setUserInfo(result.data);
                            $scope.username = result.data.name;
                        } 
                    });
                }
            }


            // function
            $scope.checkPermission = (permission) => {
                switch (permission) {
                    case $scope.permissionDefine.AGENCY:
                        if (factory.userInfo.getUserInfo())
                            return factory.userInfo.getUserInfo().ownerId == 0 && $scope.currentPermission !== $scope.permissionDefine.ROOT;
                        break;
                    case $scope.permissionDefine.WEBSITE_CREATE:
                        if (factory.userInfo.getUserInfo())
                            return factory.userInfo.getUserInfo().ownerId == 0;
                }
                return ($scope.currentPermission & permission) != 0;
            }

            $scope.isActiveTab = (tabname: string) => {
                var currTab: common.Tab = CurrentTab.getTab();
                if (currTab === null || currTab === undefined)
                    return "";
                if (tabname === currTab.tabName)
                    return "active";
                return "";
            };

            $scope.goTab = (tabname: string) => {
                var curTab: common.Tab = CurrentTab.getTab();

                if (tabname === 'website') {
                    Page.setTitle('Website');
                    $state.transitionTo('main.website.list', {});
                    return;
                }
                if (tabname === 'order') {
                    Page.setTitle('Order');
                    $state.transitionTo('main.order.list', {});
                    return;
                }
                if (tabname === 'report') {
                    Page.setTitle('Report');
                    $state.transitionTo('main.report.list');
                    return;
                }
                if (tabname === 'admin') {
                    Page.setTitle('Admin');
                    $state.transitionTo('main.admin.account_list', {});
                    return;
                }
                if (tabname == 'agency') {
                    $state.transitionTo('main.agency.account', {});
                    return;
                }
                if (tabname == 'system') {
                    $state.transitionTo('main.system.config', {});
                    return;
                }
            };

            $scope.logout = () => {
                var sessionid: string = common.Utils.getCookie('123click');
                if (sessionid.length > 0) {
                    factory.sessionService.logout(sessionid, function (response) {
                        if (response.code === 1) {
                            $state.transitionTo('login');
                            common.LocalStorageUtils.remove("selectedCol");
                            return;
                        }
                    });
                }
            };

            $scope.changePassword = () => {
                var modalInstance = $modal.open({
                    templateUrl: 'views/admin/modal.account.password',
                    controller: AccountPasswordModalController,
                    resolve: {
                        name: function () { return $scope.username }
                    }
                });
                modalInstance.result.then(function (item) {
                    var sessionId: string = common.Utils.getCookie('123click');
                    factory.sessionService.getCurrentUser(sessionId, function (response) {
                        if (response.code === 1) {
                            var userInfo: models.UserInfo = response.data;
                            userInfo.password = item.new_password;

                            factory.userService.update(userInfo, function (result) {
                                if (result === "success") {
                                    //$modalInstance.close(item);
                                }
                            });
                        }
                    });
                });
            };
        }
    }


    export class PermissionController {
        constructor($scope: common.IPermissionScope, $state, factory: backend.Factory) {

            $scope.permissionDefine = new models.PermissionDefine();

            $scope.checkDenyAccess = (object: string, id: number) => {
                factory.permissionUtils.getPermission(object, id, function (ret: number) {
                    if (ret !== undefined && ret !== null && (object === utils.PermissionUtils.ORDER && (ret & $scope.permissionDefine.EDITORDER) === 0 
                        || object === utils.PermissionUtils.WEBSITE && (ret & $scope.permissionDefine.EDITWEBSITE) === 0))
                        $state.transitionTo("main.deny_access");
                });
            }

            $scope.getPermission = (object, id) => {
                if (!object) {
                    console.log("null object")
                    return 
                }
                factory.permissionUtils.getPermission(object, id, function (ret) {
                    if (ret)
                        $scope.currentPermission = ret;
                });
            }

            $scope.checkPermission = (permission, showMessage?: boolean) => {
                //permission prevent agency account to create website
                switch (permission) {
                    case $scope.permissionDefine.AGENCY:
                        if (factory.userInfo.getUserInfo())
                            return factory.userInfo.getUserInfo().ownerId == 0 && $scope.currentPermission !== $scope.permissionDefine.ROOT;
                        break;
                    case $scope.permissionDefine.WEBSITE_CREATE:
                        if (factory.userInfo.getUserInfo())
                            return factory.userInfo.getUserInfo().ownerId == 0;
                }

                var ret: boolean = ($scope.currentPermission & permission) != 0;
                if (permission === $scope.permissionDefine.ORDER_ALL_PERMISSION || permission === $scope.permissionDefine.WEBSITE_ALL_PERMISSION)
                    ret = (($scope.currentPermission & permission) === permission);
                return ret;
            }

            $scope.getPermissions = (object, ids: number[], permission: number) => {
                if (ids.length == 0 || ids == null || ids == undefined) return;
                $scope.itemsPermission = {};
                factory.permissionUtils.getPermissions(object, ids, function (res: models.Permission[]) {
                    res.forEach((p, index) => {
                        if (p.permission & permission)
                            $scope.itemsPermission[p.id] = true;
                        else
                            $scope.itemsPermission[p.id] = false;
                    });
                });
            }

            $scope.gotoDeny = () => $state.transitionTo("main.deny_access");
            $scope.gotoNotFound = () => $state.transitionTo("main.404_notfound");
        }
    }

    export class FullSearchController {
        constructor($scope: scopes.IFullSearchScope, $state, $stateParams, $modal, factory: backend.Factory) {
            //$scope.filterKind = "z";
            $scope.startFrom = 1;
            $scope.limitItem = 10;
            $scope.sortField = {};
            $scope.currentTab = "websites";
            $scope.checkBoxes = {};
            $scope.isChosen = false;
            var currentService, currentItemLabel;
            var labelOfKind = { 'w': 'Website', 'zg': 'Zone group', 'z': 'Zone', 'o': 'Order', 'c': 'Campaign', 'i': 'Item' };
            var contentOfTab: Array<{ kind: string; service: any}>;
            contentOfTab = [];
            contentOfTab["websites"] = { kind: "w", service: factory.websiteService };
            contentOfTab["zonegroups"] = { kind: "zg", service: factory.zoneGroupService};
            contentOfTab["zones"] = { kind: "z", service: factory.zoneService };
            contentOfTab["orders"] = { kind: "o", service: factory.orderService };
            contentOfTab["campaigns"] = { kind: "c", service: factory.campaignService };
            contentOfTab["items"] = { kind: "i", service: factory.campaignItemService};
                
            if (!!$stateParams.keywork) {
                search();
            }

            function search() {
                factory.fullSearchService.searchFull($stateParams.keywork, function (items: Array<any>) {
                    if (!!items) {
                        $scope.allItems = items;
                        $scope.checkBoxes = {};
                        $scope.isChosen = false;
                        if (!!contentOfTab[$scope.currentTab])
                            $scope.items = filterItemKind(contentOfTab[$scope.currentTab].kind);
                    }
                });
            }
            function filterItemKind(kind: string) {
                return $scope.allItems.filter((it, _) => it.kind === kind);
            }

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                console.log(newValue, oldValue);
                if (!!$scope.items) {
                    for (var i: number = 0; i < $scope.items.length; i++) {
                        $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                    }
                    $scope.isChosen = $scope.checkBoxes['all'];
                }
            });
            $scope.$watch("startFrom", function (_new, _old) {
                if (_new.toString().search(/[^0-9]/g) !== -1)
                    $scope.startFrom = parseInt(_old);
                else if (typeof (_new) == "string")
                    $scope.startFrom = parseInt(_new);
                if (_new !== undefined && _new !== null) {
                    if (_new < 1 || _new > $scope.getPageNum())
                        $scope.startFrom = parseInt(_old);
                }
            })


            $scope.getKind = (kind: string) => {
                return labelOfKind[kind];
            }
            $scope.isActiveTab = (tabName) => {
                if (tabName === $scope.currentTab)
                    return "active";
            }
            $scope.gotoTab = (tabName) => {
                $scope.sortField = {};
                $scope.currentTab = tabName;
                if (!!contentOfTab[tabName]) {
                    var kind = contentOfTab[tabName].kind;
                    currentService = contentOfTab[tabName].service;
                    currentItemLabel = labelOfKind[kind].toLowerCase();
                    $scope.checkBoxes = {};
                    $scope.items = filterItemKind(kind);
                    $scope.startFrom = 1;
                }
            }
            $scope.gotoItem = (kind: string, id: number) => {
                switch (kind) {
                    case "w": 
                        $state.transitionTo("main.website.detail.setting", {websiteId: id});
                        break;
                    case "zg":
                        factory.zoneGroupService.load(id, function (zg: models.ZoneGroup) {
                            if (!!zg) {
                                $state.transitionTo("main.website.zonegroup_detail.setting", { websiteId: zg.siteId, zoneGroupId: id });
                            }
                        });
                        break;
                    case "z":
                        factory.zoneService.load(id, function (zone: models.Zone) {
                            if (!!zone) {
                                $state.transitionTo("main.website.zone_detail.setting", { websiteId: zone.siteId, zoneId: id });
                            }
                        });
                        break;
                    case "o":
                        $state.transitionTo("main.order.detail.setting", { orderId: id });
                        break;
                    case "c":
                        factory.campaignService.load(id, function (camp: models.Campaign) {
                            if (!!camp) {
                                $state.transitionTo("main.order.campaign_detail.setting", { orderId: camp.orderId, campaignId: id });
                            }
                        });
                        break;
                    case "i":
                        factory.campaignItemService.load(id, function (item: models.AdItem) {
                            if (!!item) {
                                factory.campaignService.load(item.campaignId, function (camp: models.Campaign) {
                                if (!!camp) {
                                    if (item.kind === models.CampaignItemType.NETWORK.PR)
                                        $state.transitionTo("main.order.article_setting", { orderId: camp.orderId, campaignId: camp.id, articleId: id });
                                    else {
                                        $state.transitionTo("main.order.item_detail.setting", { orderId: camp.orderId, campaignId: camp.id, itemId: id });
                                    }
                                }
                            });
                            }
                        });
                        break;
                }
            }


            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.check = (id: number) => {
                if ($scope.checkBoxes[id]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            };
            $scope.disable = () => {
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
                            checkedList: function (websiteService) {
                                var checkedList = [];
                                for (var att in $scope.checkBoxes) {
                                    if ($scope.checkBoxes[att] == true) {
                                        for (var i = 0; i < $scope.items.length; i++) {
                                            if ($scope.items[i].id == att) {
                                                checkedList.push({ id: att, name: $scope.items[i]["name"] });
                                            }
                                        }
                                    }
                                }
                                return checkedList;
                            },
                            type: function () {
                                return "delete_item_" + currentItemLabel;
                            }
                        }
                    });
                    webModal.result.then(
                        function (checkedList) {
                            $scope.itemsWarning = [];
                            for (var i: number = 0; i < checkedList.length; i++) {
                                var id: number = checkedList[i].id;
                                currentService.remove(id, function (data) {
                                    search();
                                }, function (msg, status) {
                                    if (status === 500) {
                                        $scope.showWarning = true;
                                        $scope.messageBody = msg;
                                        $scope.isChosen = false;
                                        $scope.checkBoxes = {};
                                    }
                                });
                            }
                        },
                        function (message) {
                        });
                }

            };

            //paging function
            $scope.getPageNum = () => {
                if (!!$scope.items && $scope.items.length !== 0)
                    return Math.ceil($scope.items.length / $scope.limitItem);
            }
            $scope.checkDisable = (start: number, type: string) => {
                if (type === "next" && start >= $scope.getPageNum() || type === "prev" && start <= 1) {
                    return "disabled";
                }
                return "active";
            }
            $scope.next = () => {
                if ($scope.startFrom + 1 > $scope.getPageNum()) return;
                $scope.startFrom++;
            }
            $scope.prev = () => {
                if ($scope.startFrom - 1 < 1) return;
                $scope.startFrom--;
            }
            $scope.chooseLimit = (limit: number) => {
                $scope.limitItem = limit;
                $scope.startFrom = 1;
            }
            $scope.getEndIndex = () => $scope.startFrom * $scope.limitItem < $scope.items.length ? $scope.startFrom * $scope.limitItem : $scope.items.length;

            //sort function

            $scope.getSortClass = (type: string): string => {
                if ($scope.sortField[type] == undefined)
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            };

            $scope.switchSort = (type: string) => {
                var down = -1;
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    down = 1;
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                }
                $scope.items.sort((a, b) => {
                    var ret = (a['name'].toLowerCase() < b['name'].toLowerCase() ? -1 : (a['name'] > b['name'] ? 1 : 0));
                    return ret * down;
                });
            };
        }
    }


    //------------------------------------
    export class ValidateZoneController {
        constructor($scope: scopes.IValidateZone, factory: backend.Factory, $stateParams, $http) {
            if ($stateParams.websiteId !== null && $stateParams.websiteId !== undefined) {
                factory.zoneService.listByWebsiteId($stateParams.websiteId, 0, 1000, function (zonesRet: backend.ReturnList<models.Zone>) {
                    if (zonesRet) {
                        var zones: Array<models.Zone> = zonesRet.data;
                        for (var i = 0; i < zones.length; i++){
                            var zone: models.Zone = zones[i];
                            if (zone.id === 45)
                                console.log("tesT");
                            var xhr = new XMLHttpRequest();
                            xhr.open("GET", common.Config.API_URL + '/zone/getHtmlCode?zoneId=' + zone.id, false);
                            var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
                            xhr.setRequestHeader("X-sessionId", sessionid);
                            xhr.onload = function () {
                                if (xhr.readyState === 4) {
                                    if (xhr.status === 200) {
                                        var res = xhr.responseText.replace(/<script[\w\W\d]+<\/script>/, "");
                                        var zId_decode = jQuery(res).filter("div").attr("z2-zoneid");
                                        var html = "<div style='text-align:center; border-bottom: 1px solid #bebebe'><b>" + zone.name + "</b>"
                                            + " (" + zId_decode + ")" + "<br/>Size: " + zone.width + "x" + zone.height + res + "</div>\n\r";
                                        jQuery("#innerAds").append(html);
                                    }
                                }
                            };
                            xhr.send(null);
                        }
                        setTimeout(function () {
                            ZADS.process();
                            ZADS.ZA.process();
                        }, 400);
                    }
                });
            }
        }
    }
}
declare var ZADS;