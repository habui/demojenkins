/// <reference path="../../libs/angular/angular.d.ts"/>
/// <reference path="../../libs/moment.d.ts"/>
/// <reference path="../../common/common.ts"/>
/// <reference path="../../libs/bootstrap.d.ts"/>


module controllers {
    'use strict';
    export class OrderBreadcumNavController {
        constructor($scope: scopes.IOrderBreadcumNavScope, $state, $stateParams, $location,
            BreadCumStore: common.BreadCum, factory: backend.Factory) {

            BreadCumStore.pushState("main.order.list", "orders");
            BreadCumStore.pushState("main.order.detail.campaign", "order_detail");
            BreadCumStore.pushState("main.order.detail.setting", "order_detail");
            BreadCumStore.pushState("main.order.detail.item", "order_detail");
            BreadCumStore.pushState("main.order.detail.assigned", "order_detail");
            BreadCumStore.pushState("main.order.campaign_detail.setting", "campaign_detail");
            BreadCumStore.pushState("main.order.campaign_detail.items", "campaign_detail");
            BreadCumStore.pushState("main.order.campaign_detail.linkedbooking", "campaign_detail");
            BreadCumStore.pushState("main.order.campaign_detail.media_plan", "campaign_detail");
            BreadCumStore.pushState("main.order.booking_campaign", "campaign_booking");
            BreadCumStore.pushState("main.order.booking_ticket", "booking_zone");
            BreadCumStore.pushState("main.order.campaign_create", "campaign_create");
            BreadCumStore.pushState("main.order.campaign_types", "campaign_create");
            BreadCumStore.pushState("main.order.bookingitemtypeselect", "create_item");
            BreadCumStore.pushState("main.order.networkitemtypeselect", "create_item");
            BreadCumStore.pushState("main.order.newitem", "create_item");
            BreadCumStore.pushState("main.order.unlink_booking_zone", "unlink_booking_zone");
            BreadCumStore.pushState("main.order.unlink_booking_item", "unlink_booking_item");
            BreadCumStore.pushState("main.order.item_detail.linkedbooking", "item_detail");
            BreadCumStore.pushState("main.order.item_detail.setting", "item_detail");
            BreadCumStore.pushState("main.order.conversion", "conversion");
            BreadCumStore.pushState("main.order.edit_conversion", "conversion_update");
            BreadCumStore.pushState("main.order.item-booking", "item_detail");
            BreadCumStore.pushState("main.order.campaign_detail.articles", "campaign_articles");
            BreadCumStore.pushState("main.order.newprarticle", "article_create");
            BreadCumStore.pushState("main.order.article_setting", "article_detail");

            BreadCumStore.tree.push(new common.BreadCumNode("orders", "All Orders", -1));
            BreadCumStore.tree.push(new common.BreadCumNode("order_detail", "", "orders"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign_detail", "", "order_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign_booking", "Booking Campaign", "order_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("booking_zone", "Booking Zone", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign_create", "Create Campaign", "order_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("create_item", "Create Item", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("create_article", "Create Article", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("unlink_booking_zone", "Unlink Booking Zone", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("unlink_booking_item", "Unlink Booking Item", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("item_detail", "", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("conversion", "Conversion tracking", -1));
            BreadCumStore.tree.push(new common.BreadCumNode("conversion_update", "Conversion update", "conversion"));
            BreadCumStore.tree.push(new common.BreadCumNode("article_create", "Create Article", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("article_detail", "", "campaign_detail"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign_articles", "Campaign articles", "campaign_detail"));

            if ($stateParams.orderId === undefined || $stateParams.orderId === null) {
                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                $scope.currview = BreadCumStore.getLink($state.current.name);
            }

            if ($stateParams.orderId !== undefined && $stateParams.orderId !== null) {
                factory.orderService.load($stateParams.orderId, function (order: models.Order) {
                    if (order !== undefined && order !== null) {
                        BreadCumStore.tree.setNameNode("order_detail", "Order ( " + order.name + ")");
                    }

                    if ($stateParams.campaignId === undefined || $stateParams.campaignId === null) {
                        $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                        $scope.currview = BreadCumStore.getLink($state.current.name);
                        return;
                    }

                    factory.campaignService.load($stateParams.campaignId, function (campaign: models.Campaign) {
                        if (campaign !== undefined && campaign !== null) {
                            BreadCumStore.tree.setNameNode("campaign_detail", "Campaign ( " + campaign.name + ")");
                        }

                        if ($stateParams.itemId === undefined || $stateParams.itemId === null) {
                            // Process get Article Name
                            if ($stateParams.articleId === undefined || $stateParams.articleId === null) {
                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);
                                return;
                            }

                            factory.articleService.load($stateParams.articleId, function (item: models.Article) {
                                if (item !== undefined && item !== null)
                                    BreadCumStore.tree.setNameNode("article_detail", "Article (" + item.name + ")");

                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);
                            });
                        }
                        if ($stateParams.itemId) {
                            factory.campaignItemService.load($stateParams.itemId, function(item: models.AdItem) {
                                if (item !== undefined && item !== null)
                                    BreadCumStore.tree.setNameNode("item_detail", "Item (" + item.name + ")");

                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);
                            });
                        }
                        if ($stateParams.articleId) {
                            factory.articleService.load($stateParams.articleId, function(article: models.Article) {
                                if (article !== undefined && article !== null)
                                    BreadCumStore.tree.setNameNode("item_detail", "Article (" + article.name + ")");

                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);
                            })
                        }
                    });
                });
            }

            $scope.goto = (dest: string) => {
                switch (dest) {
                    case "orders":
                        $state.transitionTo("main.order.list", {});
                        break;
                    case "order_detail":
                        $state.transitionTo("main.order.detail.campaign", { orderId: $stateParams.orderId });
                        break;
                    case "campaign_detail":
                        $state.transitionTo("main.order.campaign_detail.items", { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
                        break;
                    case "article_detail":
                        $state.transitionTo("main.order.article_detail.setting", { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId,
                            articleId: $stateParams.articleId });
                        break;
                    case "conversion":
                        $state.transitionTo("main.order.conversion", { });
                        break;
                }
            };

            $scope.isShow = (): boolean => {
                if ($scope.items && $scope.items.length > 0)
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

    /*
     *----------------------- ORDER ------------------------------------------
     */
    export class OrderController extends PermissionController {
        constructor($scope: scopes.IOrderScope, $location, factory: backend.Factory,
            $state, CurrentTab: common.CurrentTab, BodyClass, ActionMenuStore: utils.DataStore, IDSearch: common.IIDSearch) {
            super($scope, $state, factory);
            // set Body Class
            BodyClass.setClass('');
            $scope.searchField = "";
            // receive change action menu notification
                $scope.$on('change_action_menu', function (event, args: string[]) {
                    var action_menus: common.ActionMenu[] = ActionMenuStore.get($state.current.name);
                    if (action_menus !== null && args && args[0] && !isNaN(parseInt(args[0]))) {
                        factory.permissionUtils.getPermission(utils.PermissionUtils.ORDER, parseInt(args[0]), function (ret) {
                            if (ret) $scope.currentPermission = ret;
                        });
                        $scope.actionMenus = action_menus;
                    }
                    else
                        $scope.actionMenus = [];
                });

            //---------------------------- function -----------------------------------
            $scope.goTab = (tabName: string) => {
                if (tabName === 'list_order') {
                    $state.transitionTo('main.order.list', { page: 1 });
                    return;
                }
                if (tabName === 'create_order') {
                    $state.transitionTo('main.order.create');
                    return;
                }
                if (tabName === "conversion_tracking") {
                    $state.transitionTo("main.order.conversion");
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
                        $state.transitionTo("main.order.search", { keywork: $scope.searchField });
                }
            };
        }

    }

    export class OrderDetailController extends PermissionController {
        constructor($scope: scopes.IOrderDetailScope, factory: backend.Factory,
            $state, $stateParams, ActionMenuStore: utils.DataStore, $window) {
            super($scope, $state, factory);
            $scope.orderId = $stateParams.orderId;
            $scope.title = 'Detail';
            
            // setup action menu
            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-plus", "Create Campaign", function () {
                $state.transitionTo('main.order.campaign_types', { orderId: $scope.orderId });
            }, $scope.permissionDefine.EDITORDER));
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.order.campaign', { orderId: $scope.orderId }));
            }, $scope.permissionDefine.REPORTORDER));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function() {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.orderId, objType: "Order" }));
            }, $scope.permissionDefine.ROOT));
            ActionMenuStore.store('main.order.detail.campaign', action_menus);
            ActionMenuStore.store('main.order.detail.setting', action_menus);
            ActionMenuStore.store('main.order.detail.assigned', action_menus);

            //----------------------------------- function -------------------------------
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'campaigns' && $state.current.name === 'main.order.detail.campaign')
                    return 'active';
                if (tabname === 'items' && $state.current.name === 'main.order.detail.item')
                    return 'active';
                if (tabname === 'articles' && $state.current.name === 'main.order.detail.article')
                    return 'active';
                if (tabname === 'setting' && $state.current.name === 'main.order.detail.setting')
                    return 'active';
                if (tabname === 'assigned' && $state.current.name === 'main.order.detail.assigned')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string) => {
                if (tabname === 'campaigns') {
                    $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 });
                    return;
                }
                if (tabname === 'items') {
                    $state.transitionTo('main.order.detail.item', { orderId: $scope.orderId, page: 1 });
                    return;
                }
                if (tabname === 'articles') {
                    $state.transitionTo('main.order.detail.article', { orderId: $scope.orderId, page: 1 });
                    return;
                }
                if (tabname === 'setting') {
                    $state.transitionTo('main.order.detail.setting', { orderId: $scope.orderId, page: 1 });
                    return;
                }
                if (tabname === 'assigned') {
                    $state.transitionTo('main.order.detail.assigned', { orderId: $scope.orderId, page: 1 });
                    return;
                }
            };

        }
    }

    export class OrderDetailItemController extends PermissionController {
        constructor($scope: scopes.IOrderItemListScope, $state, $stateParams,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
                $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
                super($scope, $state, factory);
                CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.orderId = $stateParams.orderId;
            $scope.pageIndex = 1;
            $scope.pageSize = 10;
            $scope.checkboxes = {};
            $scope.items = [];
            $scope.checkboxes['all'] = false;
            $scope.isChosen = false;
            $scope.itemPreview = null;
            $scope.contentDic = {}; $scope.previewSource = ""; $scope.previewContent = ""; $scope.previewTitle = ""; $scope.sizeDic = {};
            $scope.sortField = {name: common.SortDefinition.DEFAULT};
            if ($scope.orderId) {
                listItems($scope.orderId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);
            }
                var currentOrderField = "", currentOrderType = "";
                function listItems(orderId: number, from: number, count: number, orderBy?: string, orderType?: string) {
                    factory.campaignItemService.getItemsByOrderId(orderId, from, count, function (ret) {
                        $scope.items = ret.data.filter((it) => it.kind !== models.CampaignItemType.NETWORK.PR);
                        $scope.totalRecord = ret.total - ret.data.filter((it) => it.kind === models.CampaignItemType.NETWORK.PR).length;

                        for (var i: number = 0; i < $scope.items.length; i++) {
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
                            }
                            else if ($scope.items[i].kind === models.CampaignItemType.NETWORK.TVC) {
                                var tvcFile: models.TvcItem = ret.data[i];
                                $scope.contentDic[id] = tvcFile.tvcFile;
                            }
                        }
                    }, orderBy, orderType);
                    currentOrderField = orderBy;
                    currentOrderType = orderType;
                }

            //Scope function
            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    listItems($scope.orderId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    listItems($scope.orderId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    listItems($scope.orderId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type);
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

            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            };

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            };
            $scope.$watch('checkboxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkboxes[$scope.items[i].id] = $scope.checkboxes['all'];
                }
                $scope.isChosen = $scope.checkboxes['all'];
            });
            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };
            $scope.check = (itemId: number) => {
                if ($scope.checkboxes[itemId]) {
                    $scope.isChosen = true; return;
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkboxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            }
            $scope.unlink = () => {
                var selectIds: Array<number> = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkboxes[$scope.items[i].id])
                        selectIds.push($scope.items[i].id);
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

            $scope.isKind = (type: string, kind: string): boolean => {
                if (type === kind.toLowerCase())
                    return true;
                return false;
            };

            $scope.paging = (start: number, size: number) => {
                listItems($scope.orderId, start, size, currentOrderField, currentOrderType);
            }
            $scope.gotoItem = (itemId: number) => {
                factory.campaignItemService.load(itemId, (bannerRet: models.AdItem) => {
                    if (bannerRet) {
                        if (models.CampaignItemType.isBookingType(bannerRet.kind))
                            $state.transitionTo('main.order.item_detail.linkedbooking',
                            {
                                orderId: $scope.orderId,
                                campaignId: bannerRet.campaignId,
                                itemId: itemId
                            });
                        else {
                            $state.transitionTo('main.order.item_detail.setting',
                            {
                                orderId: $scope.orderId,
                                campaignId: bannerRet.campaignId,
                                itemId: itemId
                            });
                        }
                    }
                });
            };

            $scope.gotoItemSetting = (itemId: number) => {
                factory.campaignItemService.load(itemId, function (bannerRet: models.AdItem) {
                    if (bannerRet)
                        $state.transitionTo('main.order.item_detail.setting',
                            {
                                orderId: $scope.orderId,
                                campaignId: bannerRet.campaignId,
                                itemId: itemId
                            });
                });
            };
            $scope.previewItem = (itemId: number) => {
                factory.campaignItemService.load(itemId, function (bannerRet) {
                    $scope.itemPreview = bannerRet;
                    jQuery("#previewModal").modal('show');
                });
            };

            $scope.search = () => {
                factory.campaignItemService.searchItems($scope.searchText, $stateParams.orderId, 'order', function (list: Array<any>) {
                    $scope.items = list;
                    $scope.totalRecord = list.length;
                    $scope.pageIndex = 1;
                    $scope.checkboxes = {};
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


        }
    }

    export class OrderListController extends PermissionController{
        constructor($state, $stateParams, $location: ng.ILocationService, factory: backend.Factory,
            $scope: scopes.IOrderListScope, CurrentTab: common.CurrentTab, Page: common.PageUtils, $modal) {
            super($scope, $state, factory);
            $scope.sortField = { name: common.SortDefinition.DEFAULT };
            $scope.$emit('change_action_menu');
            // set current tab & title
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            Page.setTitle('Order List | 123Click');


            //scope initialization
            $scope.currentSelectedRow = -1;
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.checkBoxes = {};
            $scope.checkBoxes['all'] = false;
            $scope.searchText = '';
            $scope.totalRecord = 0;
            $scope.items = [];
            $scope.isChosen = false;
            $scope.filterBy = "a";

            listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);

            var currentSortField = "", currentSortType = "";
            function listOrder(from: number, count: number, filterBy: string, sortBy?: string, sortType?: string) {
                factory.orderService.listByFilter(from, count, filterBy, function (orderRet) {
                    $scope.items = orderRet.data;
                    $scope.totalRecord = orderRet.total;
                    $scope.checkBoxes = {};
                    var oIds: number[] = [];
                    $scope.items.forEach((o, index) => {
                        oIds.push(o.id);
                    });
                    $scope.getPermissions(utils.PermissionUtils.ORDER, oIds, $scope.permissionDefine.EDITORDER);

                }, sortBy, sortType);

                currentSortField = sortBy;
                currentSortType = sortType;
            }
            $scope.filterText = "All order";
            //------------- Scope function------------------
            $scope.$watch("searchText", function (newVal, oldVal) {
                if (oldVal && oldVal.length != 0 && newVal === "") {
                    listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                }
            });
            $scope.getSortClass = (type: string): string => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            };

            $scope.getStatusClass = (status: string): string => {
                if (status.toLowerCase() === models.OrderStatus.RUNNING.toLowerCase())
                    return "label-green";
                if (status.toLowerCase() === models.OrderStatus.PAUSED.toLowerCase())
                    return "label-orange";
                if (status.toLowerCase() === models.OrderStatus.TERMINATED.toLowerCase())
                    return "label-violet";
                return "";
            };

            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                    listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy, type);
                }
            };
            $scope.filter = (type: string) => {
                switch (type) {
                    case 'all':
                        $scope.filterBy = 'a';
                        $scope.filterText = "All order";
                        break;
                    case 'running':
                        $scope.filterBy = "running";
                        $scope.filterText = "Order running";
                        break;
                    case 'finished':
                        $scope.filterBy = "finished";
                        $scope.filterText = "Order finished";
                        break;
                }
                $scope.pageIndex = 1;
                listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
            };

            $scope.paging = (start: number, size: number) => {
                listOrder(start, size, $scope.filterBy, currentSortField, currentSortType);
            };

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

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

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = - 1;
            }
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }

            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MM - YYYY');
            };

            $scope.formatNumber = (num: number): string => {
                return num.toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
            };

            $scope.gotoOrder = (orderid: number) => {
                $state.transitionTo("main.order.detail.campaign", {orderId: orderid});
            };

            $scope.gotoOrderSetting = (orderid: number) => {
                $state.transitionTo('main.order.detail.setting', { orderId: orderid });
            };
            $scope.terminate = () => {
                var orderIds: number[] = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    var orderid: number = $scope.items[i].id;

                    if ($scope.checkBoxes[orderid]) {
                        orderIds.push(orderid);
                    }
                }
                for (var i: number = 0; i < orderIds.length; i++) {
                    factory.orderService.setStatus(orderIds[i], models.OrderStatus.TERMINATED, function (response) {
                        listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "terminate", "order"));
                    }, function (msg, status) {
                    });
                }
            };

            $scope.pause = () => {
                var orderIds: number[] = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    var orderid: number = $scope.items[i].id;

                    if ($scope.checkBoxes[orderid]) {
                        orderIds.push(orderid);
                    }
                }

                for (var i: number = 0; i < orderIds.length; i++) {
                    factory.orderService.pause(orderIds[i], function (response) {
                        listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "pause", "order"));
                    }, function (msg, status) {
                    });
                }
            };

            $scope.resume = () => {
                var orderIds: number[] = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    var orderid: number = $scope.items[i].id;

                    if ($scope.checkBoxes[orderid]) {
                        orderIds.push(orderid);
                    }
                }
                for (var i: number = 0; i < orderIds.length; i++) {
                    factory.orderService.resume(orderIds[i], function (response) {
                        listOrder(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filterBy);
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "resume", "order"));
                    }, function (msg, status) {
                    });
                }
            };

            $scope.search = () => {
                factory.orderService.search($scope.searchText, 0, false, function (list: Array<models.Order>) {
                    $scope.items = list;
                    $scope.totalRecord = $scope.items.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };

        }
    }

    export class OrderCreateController {
        constructor($scope: scopes.IOrderCreateScope, $location,
            $state, CurrentTab: common.CurrentTab, Page: common.PageUtils, factory: backend.Factory) {

            CurrentTab.setTab(new common.Tab("order", "create_order"));
            Page.setTitle('Create Order | 123Click');
            $scope.$emit('change_action_menu');
            $scope.checkValidate = false;
            var userId: number = 0;
            var userinfo: models.UserInfo = factory.userInfo.getUserInfo();
            if (userinfo !== null && userinfo !== undefined) {
                userId = userinfo.id;
            }
            $scope.item = new models.Order(0, '', userId, '', 0, 0, 0, 0);
            $scope.ownerName = '';
            $scope.option = '';
            $scope.hasSave = true;
            //------------------- function ----------------------------
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

            $scope.isCampaignTypeSelected = (): boolean => {                
                if ($scope.checkValidate && $scope.option !== 'booking' && $scope.option !== 'network' && $scope.option !== 'tvc' && $scope.option !== 'pr')
                    return false;
                return true;
            };

            $scope.isInvalidOwner = (): boolean => {
                if ($scope.item.ownerId != 0)
                    return false;
                return true;
            };

            $scope.isAdminRole = (): boolean => {
                var user_info: models.UserInfo = factory.userInfo.getUserInfo();
                if (user_info === null || user_info === undefined)
                    return false;
                return user_info.isAdmin();
            };

            $scope.save = () => {
                if ($scope.hasSave === false) return;
                $scope.checkValidate = true;
                if ($scope.item.ownerId == 0) $scope.item.ownerId = factory.userInfo.getUserInfo().id;
                if ($scope.item.name.length == 0 || $scope.item.ownerId == 0)
                    return;

                if ($scope.option !== 'booking' && $scope.option !== 'network' && $scope.option !== 'tvc' && $scope.option !== 'pr')
                    return;
                $scope.hasSave = false; //disable button Save when method save is running
                $scope.item.startDate = new Date().getTime();
                $scope.item.endDate = new Date().getTime();
                $scope.item.id = 0;
                $scope.item.pendingCampaign = 0;
                $scope.item.runningCampaign = 0;

                factory.orderService.save($scope.item, function (result) {
                    if (result !== undefined && result !== null && result.id > 0) {
                        // success
                        
                        if ($scope.option === 'booking') {
                            $state.transitionTo('main.order.campaign_create', { orderId: result.id, type: 'booking' });
                            return;
                        }
                        if ($scope.option === 'network') {
                            $state.transitionTo('main.order.campaign_create', { orderId: result.id, type: 'network' });
                            return;
                        }
                        if ($scope.option === 'tvc') {
                            $state.transitionTo('main.order.campaign_create', { orderId: result.id, type: 'networktvc' });
                            return;
                        }
                        if ($scope.option === 'pr') {
                            $state.transitionTo('main.order.campaign_create', { orderId: result.id, type: 'prnetwork' });
                            return;
                        }
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "order"));
                    $scope.hasSave = true; //enable button Save when method save is complete
                });
            };
            $scope.goto = (dest: string) => {
                $state.transitionTo('main.order.list', { page: 1 });
            };
        }
    }

    export class OrderSettingController {
        constructor($scope: scopes.IOrderSettingScope, $timeout, $state, $stateParams, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            $scope.$emit('change_action_menu', [$stateParams.orderId]);
            $scope.orderId = $stateParams.orderId;
            $scope.checkValidate = false;
            $scope.ownerName = '';
            
            factory.userService.list(0, 10000, function (list: backend.ReturnList<models.UserInfo>) {
                $scope.owners = list.data;
                factory.orderService.load($scope.orderId, function (item) {
                    if (item) {
                        $scope.item = item;
                        $scope.ownerName = $scope.getOwnerName($scope.item.ownerId);
                    }
                });
            });
            //---------------------- function -------------------
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

            $scope.isFound = (): boolean => {
                if ($scope.item !== null)
                    return true;
                return false;
            };

            $scope.isAdminRole = (): boolean => {
                var user_info: models.UserInfo = factory.userInfo.getUserInfo();
                if (user_info === null || user_info === undefined)
                    return false;
                return user_info.isAdmin();
            };

            $scope.save = () => {
                $scope.checkValidate = true;
                if ($scope.item.name.length == 0 || $scope.item.ownerId == 0 || $scope.item.description.length == 0)
                    return;
                factory.orderService.update($scope.item, function (result) {
                    if (result === 'success') {
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "order"));
                        $timeout(function () {
                            $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 });
                        }, 1000);

                        return;
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "order"));
                }, function (msg, status) {
                });

            };

            $scope.goto = (dest: string) => {
                if (dest === 'campaign_list') {
                    $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 });
                    return;
                }
            };
        }
    }

    export class OrderAssignedUserController extends PermissionController{
        constructor($scope: scopes.IOrderAssignedUserScope, $state, $stateParams, Page,
            CurrentTab: common.CurrentTab, $timeout, factory: backend.Factory) {
                super($scope, $state, factory);
                $scope.checkDenyAccess(utils.PermissionUtils.ORDER, $stateParams.orderId);

            CurrentTab.setTab(new common.Tab("order", "list_order"));
            // notify change action menu
            $scope.hasSave = false;
                $scope.$emit('change_action_menu',[$stateParams.orderId]);
            $scope.orderId = $stateParams.orderId;
            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.searchText = '';
            $scope.userroles = [];
            $scope.orderId = $stateParams.orderId;
            factory.roleService.listByObject("order", function (data) {
                if (data != null) {
                    $scope.roles = data;
                }
            });

            factory.userRoleService.getRole(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.orderId, "order", function (data) {
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
                factory.userRoleService.getRole(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.orderId, "order", function (data) {
                    if (data != null) {
                        $scope.userroles = (data.data !== undefined && data.data !== null) ? data.data : [];
                        $scope.totalRecord = (data.total !== undefined && data.total !== null) ? data.total : 0;
                        $state.userroles = $scope.userroles;
                    }
                });
            };

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
            }

            $scope.search = () => {
            };

            $scope.choose = (user: models.UserRoleInfo) => {
                $scope.selectUser = user;
            }

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
                    var obj = "order";
                    for (var r in $scope.userroles[u].roles) {
                        roles.push($scope.userroles[u].roles[r].id);
                    }
                    user['userid'] = $scope.userroles[u].id;
                    user['roles'] = roles;
                    data.push(user);
                }
                factory.userRoleService.setRole(obj, $scope.orderId, JSON.stringify(data), function (result) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "assigned"));
                    $scope.hasSave = false;
                });
            };
            $state.scope = $scope;
        }
    }


    /*
     *----------------------- CAMPAIGN ITEM ------------------------------------------
     */
    export class ItemDetailController extends PermissionController {
        constructor($scope: scopes.IItemDetailScope, factory: backend.Factory,
            $state, $stateParams, Page: common.PageUtils, $resource, $window, ActionMenuStore: utils.DataStore) {

            $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
            super($scope, $state, factory);
            $scope.orderId = $stateParams.orderId;
            $scope.campaignId = $stateParams.campaignId;
            $scope.itemId = $stateParams.itemId;
            $scope.title = 'Detail';
            $scope.page = Page;
            $scope.isNetworkItem = false;
            var item: models.AdItem = null;
            if ($stateParams.itemId) {
                factory.campaignItemService.load($stateParams.itemId, function(itemRet: models.AdItem) {
                    if (itemRet) {
                        $scope.isNetworkItem = models.CampaignItemType.isNetworkType(itemRet.kind);
                    }
                });
            }

            var action_menus: common.ActionMenu[] = [];
            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                $window.open($state.href('main.report.campaign.item', { orderId: $scope.orderId, campaignId: $scope.campaignId, itemId: $scope.itemId }));
            }, $scope.permissionDefine.REPORTORDER));
            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.itemId, objType: "Banner" }));
            }, $scope.permissionDefine.ROOT));

            ActionMenuStore.store('main.order.item_detail.setting', action_menus);
            ActionMenuStore.store('main.order.item_detail.linkedbooking', action_menus);
            
            $scope.$emit('change_action_menu', [$stateParams.orderId]);


            // function
            $scope.isActiveTab = (tabname: string): string => {
                if (tabname === 'linked_booking' && $state.current.name === 'main.order.item_detail.linkedbooking')
                    return 'active';
                if (tabname === 'setting' && $state.current.name === 'main.order.item_detail.setting')
                    return 'active';
                return '';
            };

            $scope.gotoTab = (tabname: string) => {
                if (tabname === 'linked_booking' && !$scope.isNetworkItem) {
                    $state.transitionTo('main.order.item_detail.linkedbooking',
                        {
                            orderId: $scope.orderId,
                            campaignId: $scope.campaignId,
                            itemId: $scope.itemId,
                        });
                    return;
                }
                if (tabname === 'setting') {
                    $state.transitionTo('main.order.item_detail.setting',
                        {
                            orderId: $scope.orderId,
                            campaignId: $scope.campaignId,
                            itemId: $stateParams.itemId,
                        });
                    return;
                }
            };

            $scope.goto = (dest: string) => {
                if (dest === 'create_item') {
                    // TODO:
                    return;
                }
                if (dest === 'report') {
                    // TODO:
                }
            };
        }
    }

    export class CampaignItemListController extends PermissionController {
        constructor($modal, $scope: scopes.ICampaignItemListScope, $state, $stateParams,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
                $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
                super($scope, $state, factory);
                $scope.orderId = $stateParams.orderId;
            var pageIndex = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.totalRecord = 0;
            var campaignType = 'network';
            $scope.checkBoxes = {};
            $scope.checkZones = {};
            $scope.isChosen = false; 
            $scope.items = [];
            $scope.currentSelectedRow = -1;
            $scope.filterText = "All items";
            $scope.$emit('change_action_menu',[$stateParams.orderId]);
            $scope.isNetworkCampaign = false; $scope.contentDic = {}; $scope.previewSource = ""; $scope.previewContent = ""; $scope.previewTitle = "";
            $scope.sizeDic = {};
            $scope.isDisableList = false;

            $scope.isShownCloneItem = false; $scope.cloneItemName = ""; $scope.cloneName = ""; $scope.position = {};
            $scope.sortField = {name: common.SortDefinition.DEFAULT};
            factory.campaignService.load($stateParams.campaignId, function (ret: models.Campaign) {
                if (ret) {
                    $scope.isNetworkCampaign = (ret.campaignType == models.CampaignType.NETWORK || ret.campaignType == models.CampaignType.NETWORK_TVC);
                    $scope.isPRCampaign = (ret.campaignType == models.CampaignType.NETWORK_PR);
                }
            });
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            
            listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);

            var currentSortType = "", currentSortField = "";
            function listCampaignItems(campId: number, from: number, count: number, sortBy?: string, sortType?: string) {
                factory.campaignItemService.listByCampaignId(campId, from, count, function (list: backend.ReturnList<any>) {
                    $scope.isDisableList = false;
                    $scope.checkBoxes['all'] = false; $scope.isChosen = false; 
                    if (list !== null) {
                        $scope.items = list.data;
                        $scope.totalRecord = list.total;

                        for (var i: number = 0; i < $scope.items.length; i++) {
                            $scope.checkBoxes[$scope.items[i].id] = false;

                            var id: number = $scope.items[i].id;
                            $scope.sizeDic[id] = { width: $scope.items[i].width, height: $scope.items[i].height };
                            if (models.CampaignItemType.isBannerNotExpandable($scope.items[i].kind)) {
                                var mediaBanner: models.MediaItem = list.data[i];
                                $scope.contentDic[id] = mediaBanner.bannerFile;
                            } else if (models.CampaignItemType.isBannerWithExpandable($scope.items[i].kind)) {
                                var expandBanner: models.ExpandableItem = list.data[i];
                                $scope.contentDic[id] = expandBanner.standardFile;
                            } else if (models.CampaignItemType.isHTML($scope.items[i].kind)) {
                                var htmlItem: models.HtmlItem = list.data[i];
                                $scope.contentDic[id] = htmlItem.embeddedHtml;
                            } else if ($scope.items[i].kind === models.CampaignItemType.NETWORK.TVC) {
                                var tvcItem: models.TvcItem = list.data[i];
                                $scope.contentDic[id] = tvcItem.tvcFile;
                            }
                        }
                    }
                }, sortBy, sortType);
                currentSortField = sortBy;
                currentSortType = sortType;
            }

            function listDisableCampaignItems(campId: number, from: number, count: number) {
                factory.campaignItemService.listDisable(campId, from, count, function (list: backend.ReturnList<any>) {
                    $scope.isDisableList = true; $scope.checkBoxes['all'] = false; $scope.isChosen = false; 
                    if (list !== null) {

                        $scope.items = list.data;
                        $scope.totalRecord = list.total;

                        for (var i: number = 0; i < $scope.items.length; i++) {
                            $scope.checkBoxes[$scope.items[i].id] = false;

                            var id: number = $scope.items[i].id;
                            $scope.sizeDic[id] = { width: $scope.items[i].width, height: $scope.items[i].height };
                            if (models.CampaignItemType.isBannerNotExpandable($scope.items[i].kind)) {
                                var mediaBanner: models.MediaItem = list.data[i];
                                $scope.contentDic[id] = mediaBanner.bannerFile;
                            } else if (models.CampaignItemType.isBannerWithExpandable($scope.items[i].kind)) {
                                var expandBanner: models.ExpandableItem = list.data[i];
                                $scope.contentDic[id] = expandBanner.standardFile;
                            } else if (models.CampaignItemType.isHTML($scope.items[i].kind)) {
                                var htmlItem: models.HtmlItem = list.data[i];
                                $scope.contentDic[id] = htmlItem.embeddedHtml;
                            } else if ($scope.items[i].kind === models.CampaignItemType.NETWORK.TVC) {
                                var tvcItem: models.TvcItem = list.data[i];
                                $scope.contentDic[id] = tvcItem.tvcFile;
                            }
                        }
                    }
                });
                    
            }
            //-------------------------------------------------------------------------
            //-----------SCOPE FUNCTIONS
            //------------------------------------------------------------------------
            $scope.select = (type: string) => {
                switch (type) {
                    case "all":
                        $scope.filterText = "All items";
                        $scope.isDisableList = false;
                        listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);
                        break;
                    case "disable":
                        $scope.filterText = "Disable items";
                        $scope.isDisableList = true;
                        listDisableCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);
                        break;

                }
            };

            $scope.cloneItem = (itemId: number, itemName: string, event: any) => {
                $scope.cloneId = itemId; $scope.cloneName = itemName;
                $scope.isShownCloneItem = true;
                var x: number = event.pageX - jQuery("#maincontent").offset().left;
                var y: number = event.pageY - jQuery("#maincontent").offset().top;
                $scope.position = { top: y, left: x };
                    
            };

            $scope.createItem = () => {
                if ($scope.cloneItemName.length === 0)
                    return;

                factory.campaignItemService.load($scope.cloneId, function (item: models.AdItem) {
                    if (item) {
                        item.id = 0;
                        item.name = $scope.cloneItemName;

                        factory.campaignItemService.save(item, function (item: models.AdItem) {
                            if (item && item.id > 0) {
                                $scope.isShownCloneItem = false;
                                listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize);
                            }
                        });
                    }
                });
            };

            $scope.closeCloneItem = () => {
                $scope.isShownCloneItem = false;
            };

            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                    listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type, "asc");
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    listCampaignItems($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, type);
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
                    $scope.previewSource = "";
                    $scope.previewTitle = name;
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

            $scope.isBooking = (type: string): boolean => {
                if (models.CampaignItemType.isBookingType(type))
                    return true;
                return false;
                
            };

            $scope.isNetwork = (kind: string, zone: number): boolean => {
                if (models.CampaignItemType.isNetworkType(kind) && (zone > 0))
                    return true;
                return false;
            };

            $scope.isNetworkWithoutZone = (kind: string, zone: number): boolean => {
                if (models.CampaignItemType.isNetworkType(kind) && (zone === 0))
                    return true;
                return false;
            };

            $scope.previewItem = (itemId: number) => {
                factory.campaignItemService.load(itemId, function (bannerRet) {
                    $scope.itemPreview = bannerRet;
                    jQuery("#previewModal").modal('show');
                });
            }

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }

            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.isActiveClass = (type: string): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.check = (itemId: number) => {
                if ($scope.checkBoxes[itemId]) {
                    $scope.isChosen = true; 
                    return;
                }

                $scope.isChosen = false;

                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true;
                    }
                }
            };

            $scope.enableItems = () => {
                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }
                if (isHave) {
                    var orderModal = $modal.open({
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
                                return 'enable_item';
                            }
                        }
                    });

                    orderModal.result.then(function (checkList) {
                        var ids: number[] = [];
                        for (var i: number = 0; i < checkList.length; i++) {
                            var orderid: number = checkList[i].id;
                            ids.push(orderid);
                        }
                        for (var i: number = 0; i < ids.length; i++) {
                            factory.campaignItemService.enable(ids[i], function (result) {
                                if (result === 'success') {
                                    factory.campaignItemService.listByCampaignId($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (returnedList) {
                                        $scope.items = returnedList.data;
                                        $scope.totalRecord = returnedList.total;
                                    });
                                }
                            });
                        }
                        $scope.isDisableList = false;
                        $scope.checkBoxes['all'] = false; $scope.isChosen = false; 
                        $scope.filterText = "All items";
                    }, function (message) {});
                }
            };

            $scope.deleteItems = () => {

                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }
                if (isHave) {
                    var orderModal = $modal.open({
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
                                return 'campaignItem';
                            }
                        }
                    });

                    orderModal.result.then(function (checkList) {
                        var camIds: number[] = [];
                        for (var i: number = 0; i < checkList.length; i++) {
                            var orderid: number = checkList[i].id;
                            camIds.push(orderid);
                        }
                        for (var i: number = 0; i < camIds.length; i++) {
                            factory.campaignItemService.remove(camIds[i], function (result) {
                                if (result === 'success') {
                                    factory.campaignItemService.listByCampaignId($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (returnedList) {
                                        $scope.items = returnedList.data;
                                        $scope.totalRecord = returnedList.total;
                                    });
                                }
                            });
                        }
                        $scope.isChosen = false; $scope.filterText = "Disable items items";
                    }, function (message) {

                        });
                }
            };

            $scope.gotoItem = (itemId: number) => {
                factory.campaignItemService.load(itemId, function (itemRet: models.AdItem) {
                    if (itemRet && models.CampaignItemType.isNetworkType(itemRet.kind)) {
                        $state.transitionTo('main.order.item_detail.setting', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: itemId });
                    }
                    else
                        $state.transitionTo('main.order.item_detail.linkedbooking', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: itemId });
                    return;
                });
            };

            $scope.gotoItemSetting = (itemId: number) => {
                $state.transitionTo('main.order.item_detail.setting', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: itemId });
                return;
            };

            $scope.isBannerImg = (filename: string): boolean => {
                if (filename === undefined || filename.length === 0)
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
                if (filename === undefined || filename.length === 0)
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };

            $scope.getSource = (url: string): string => {
                if (url === undefined || url === null)
                    return "";
                return url;
            };

            $scope.paging = (start: number, size: number) => {
                listCampaignItems($stateParams.campaignId, start, size, currentSortField, currentSortType);
                //campaignItemService.listByCampaignId($stateParams.campaignId, start, size, function (list: backend.ReturnList<models.AdItem>) {
                //    if (list !== null) {
                //        $scope.items = list.data;
                //        $scope.totalRecord = list.total;
                //    }
                //});
            };

            $scope.search = () => {
                if ($scope.searchText === undefined || $scope.searchText.length === 0)
                    return;
                factory.campaignItemService.searchItems($scope.searchText, $stateParams.campaignId, 'campaign', function (list: Array<models.AdItem>) {
                    $scope.items = list;
                    $scope.totalRecord = $scope.items.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };

        }
        getSelectedItemIds($scope): number[] {
            var itemIds = [];
            for (var i = 0; i < $scope.items.length; i++) {
                var itemId = $scope.items[i].id;
                if ($scope.checkBoxes[itemId]) {
                    itemIds.push(itemId);
                }
            }
            return itemIds;
        }
    }

    export class CampaignBookingItemTypeSelectController {
        constructor($stateParams, $state, $scope: scopes.ICampaignItemTypeSelect,
            $location: ng.ILocationService, $timeout: ng.ITimeoutService) {

            $scope.choose = (type: string) => {
                $state.transitionTo('main.order.newitem', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, kind: type });
                
            };
        }
    }

    export class CampaignNetworkItemTypeSelectController {
        constructor($stateParams, $state, $scope: scopes.ICampaignItemTypeSelect, $location: ng.ILocationService,
            $timeout: ng.ITimeoutService, CurrentTab: common.CurrentTab, factory: backend.Factory) {
                $scope.$emit('change_action_menu',[$stateParams.orderId]);
            $scope.isNetworkTVC = false;
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            factory.campaignService.load($stateParams.campaignId, function (ret: models.Campaign) {
                if (ret && ret.campaignType === models.CampaignType.NETWORK_TVC)
                    $scope.isNetworkTVC = true;
            });
            $scope.choose = (type: string) => {
                switch (type) {
                    case "tvc":
                        type = "network_tvc";
                        break;;
                    case "tvcOverlay":
                        type = "network_overlay_tvc";
                        break;
                    case "pauseAd":
                        type = "network_pause_ad_tvc";
                        break;
                    case "prbanner":
                        type = models.CampaignItemType.NETWORK.PRBANNER;
                        break;
                    default:
                        type = "network_" + type;
                }
                $state.transitionTo('main.order.newitem', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, kind: type });
            };
        }
    }
                                    
    export class CampaignItemCreateController extends PermissionController {
        constructor($stateParams, $state, $location, $scope: scopes.ICreateItemScope, $timeout, $anchorScroll,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {

            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.ORDER, $stateParams.orderId);

            $scope.title = ""; $scope.kind = $stateParams.kind;
            $scope.$emit('change_action_menu');
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            $scope.freq_time_string = "Minutes";
            // [INIT] -----------
            $scope.bannerUrl = ''; 

            $scope.checkValidate = false; $scope.uploadUrl = common.Config.UPLOAD_URL;
            $scope.websites = []; $scope.selectedWebsites = []; $scope.checkboxWebsite = {};
            $scope.zones = []; $scope.selectedZones = []; $scope.checkboxZone = {}; $scope.checkboxZoneWeb = {};
            $scope.checkboxLocation = {};
            $scope.uploadResult = {
                bannerUploadResult: '',
                bannerFallbackUploadResult: '',
                expandBannerUploadResult: '',
                tvcFileUploadResult: '',
                barUploadResult: '',
                mimetype: '',
                duration: 0
            };
            $scope.locations = models.Location.CITIES.VN;
            $scope.isWarningSize = false;
            $scope.chooseTVCContent = "tvcFile";
            $scope.targeting = {}; $scope.runningTimeCustom = false;
            $scope.isExtlnk = false; $scope.extlnkflash = false; $scope.extlnkimage = false;
            $scope.unlimitStartDate = true; $scope.unlimitEndDate = true;
            $scope.kind_banner = "Item Information";
            $scope.xdata = false; $scope.xitems = [];
            $scope.belongLocation = {};
            $scope.isNetworkItem = false;
            $scope.datepicker = {
                "startDate": new Date(), "endDate": new Date()
            };
            $scope.showTrackingLink = false;
            $scope.freq_impression = 0; $scope.freq_time = 0;
            $scope.close_btn_action = "Close";
                $scope.tvc_extension = "NONE";
                $scope.bStatus = {
                    iTVCExtension: false
                };
            $scope.companionTargetingValues = [];
            var types: Array<string>;
            switch ($scope.kind) {
                case "banner":
                    $scope.title = "Create booking banner item";
                    $scope.item = new models.MediaItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.BANNER);
                    types = ["booking"]; $scope.kind_banner = "Banner Ads";
                    break;
                case "expandable":
                    $scope.title = "Create booking expandable item";
                    $scope.item = new models.ExpandableItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.EXPANDABLE);
                    types = ["booking"]; $scope.kind_banner = "Expandable Ads";
                    break;
                case "popup":
                    $scope.title = "Create popup item";
                    $scope.item = new models.MediaItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.POPUP);
                    types = ["booking"]; $scope.kind_banner = "Popup Ads";
                    break;
                case "balloon":
                    $scope.title = "Create booking balloon item";
                    $scope.item = new models.BalloonItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.BALLOON);
                    types = ["booking"]; $scope.kind_banner = "Balloon Ads";
                    break;
                case "html":
                    $scope.title = "Create html balloon item";
                    $scope.item = new models.HtmlItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.HTML);
                    types = ["booking"]; $scope.kind_banner = "HTML Ads";
                    break;
                case "tracking":
                    $scope.title = "Create tracking item";
                    $scope.item = new models.TrackingItem("", $stateParams.campaignId, models.CampaignItemType.BOOKING.TRACKING);
                    types = ["booking"]; $scope.kind_banner = "Tracking Ads";
                    break;
                // --- Network ---
                case "network_banner":
                    $scope.title = "Create network banner item";
                    $scope.item = new models.MediaItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.BANNER);
                    types = ["network"]; $scope.kind_banner = "Banner Ads"; 
                    break;
                case "network_popup":
                    $scope.title = "Create network popup item";
                    $scope.item = new models.MediaItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.POPUP);
                    types = ["network"]; $scope.kind_banner = "Popup Ads";
                    break;
                case "network_balloon":
                    $scope.title = "Create network balloon item";
                    $scope.item = new models.BalloonItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.BALLOON);
                    types = ["network"]; $scope.kind_banner = "Balloon Ads"; 
                    break;
                case "network_expandable":
                    $scope.title = "Create network expandable item";
                    $scope.item = new models.ExpandableItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.EXPANDABLE);
                    types = ["network"]; $scope.kind_banner = "Expandable Ads"; 
                    break;
                case "network_html":
                    $scope.title = "Create network HTML item";
                    $scope.item = new models.HtmlItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.HTML);
                    types = ["network"]; $scope.kind_banner = "HTML Ads"; 
                    break;
                case "network_tvc":
                    $scope.title = "Create network TVC item";
                    $scope.item = new models.TvcItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.TVC);
                    types = ["tvc"]; $scope.kind_banner = "TVC Ads";
                    break;
                case "network_pause_ad_tvc":
                    $scope.title = "Create network TVC Banner item";
                    $scope.item = new models.OverlayMediaItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.TVC_PAUSE_AD);
                    types = ["tvc"]; $scope.kind_banner = "Pause Ad Banner";
                    break;
                case "network_overlay_tvc":
                    $scope.title = "Create network TVC Banner item";
                    $scope.item = new models.OverlayMediaItem("", $stateParams.campaignId, models.CampaignItemType.NETWORK.TVCBANNER);
                    types = ["tvc"]; $scope.kind_banner = "TVC Banner Ads";
                    break;
                case models.CampaignItemType.NETWORK.PRBANNER:
                    $scope.title = "Create network PR Banner";
                    $scope.item = new models.PrBanner("", $stateParams.campaignId, models.CampaignItemType.NETWORK.PRBANNER);
                    types = ["network"];
                    $scope.kind_banner = "Network PR Banner";
                    break;
                case models.CampaignItemType.BOOKING.PRBANNER:
                    $scope.title = "Create booking PR Banner";
                    $scope.item = new models.PrBanner("", $stateParams.campaignId, models.CampaignItemType.BOOKING.PRBANNER);
                    types = ["booking"];
                    $scope.kind_banner = "Booking PR Banner";
                    break;
                default:
                    window.history.back();
            }

            if ($scope.kind.toLowerCase().indexOf("network") > -1)
                $scope.isNetworkItem = true;
            $scope.targetZonesOption = "all";
            $scope.targeting = { website: true }
            
            $scope.strBound = { "0": "Is equal", "1": "Is not equal" };
            $scope.osTargeting = {};
            $scope.retargeting = [];
            $scope.variables = [new models.NetworkVariable("", null, models.NetworkVariable.ISEQUAL)];
            // --- expandable network
            $scope.iTVCExtension = false;
            $scope.expand_display_style = 'Overlay'; $scope.expand_direction = 'Right To Left'; 

            $scope.item.displayStyle = models.EExpandDisplayStyle.OVERLAY;
            $scope.item.expandDirection = models.EExpandDirection.RIGHT_TO_LEFT;
            if ($scope.item.kind === models.CampaignItemType.BOOKING.EXPANDABLE || $scope.item.kind === models.CampaignItemType.NETWORK.EXPANDABLE) {
                $scope.item.expandStyle = models.EExpandActiveStyle.MOUSE_OVER;
                $scope.expand_style = 'Mouse over';
            } else if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON || $scope.item.kind === models.CampaignItemType.NETWORK.BALLOON) {
                $scope.item.expandStyle = models.BalloonExpandStyle.NONE;
                $scope.expand_style = 'None';
            }
            $scope.item.limitUnit = "Impression";

            $scope.item.tvcType = $scope.item.skip === true ? "skip" : "standard";

            //---------------- PR Banner ----------------
            if ($scope.kind === models.CampaignItemType.NETWORK.PRBANNER
                || $scope.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                $scope.option = { targetZone: "all" };
                $scope.cateDic = {};
                $scope.checkBoxCate = {};
                factory.articleService.listCategories((ret) => {
                    if (ret) {
                        $scope.articleCates = ret;
                        ret.forEach((cate: any)=>$scope.cateDic[cate.id] = cate.name);
                    }
                })

                // Select Publisher Category
                $scope.selectCate = (cate: any) => {
                    if ($scope.checkBoxCate[cate.id] === true) {
                        ($scope.selectedCates = $scope.selectedCates || []).push(cate)
                    } else {
                        var i = $scope.categories.indexOf(cate);
                        if (i !== -1)
                            $scope.selectedCates.splice(i, 1);
                    }
                }
                $scope.removeCate = (cId: number) => {
                    $scope.selectedCates.forEach((cate, i) => {
                        if (cate["id"] === cId) {
                            $scope.selectedCates.splice(i, 1);
                            $scope.checkBoxCate[cId] = false;
                        }
                    });
                }
            }
            // ----------- End Pr Banner ---------------
            // [INIT] --- end ----
            // <<<< -- watch -- >>>>
            $scope.$watch('xdata', function (newVal, oldVal) {
                if (newVal && $scope.xitems.length === 0) {
                    $scope.xitems.push(new models.kvItem("", ""));
                }
            });

            $scope.$watch('uploadResult.duration', function (newVal, oldVal) {
                if (newVal && $scope.item.kind === models.CampaignItemType.NETWORK.TVC) {
                    $scope.item.duration = newVal;
                }
            });

            $scope.$watch('uploadResult.bannerUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== "BEGIN") {

                    if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON ||
                        $scope.item.kind === models.CampaignItemType.BOOKING.EXPANDABLE ||
                        $scope.item.kind === models.CampaignItemType.NETWORK.EXPANDABLE ||
                        $scope.item.kind === models.CampaignItemType.NETWORK.BALLOON) {

                        $scope.item.standardFile = newVal;
                        return;
                    }
                    $scope.item.bannerFile = newVal;
                }
            });

            $scope.$watch('uploadResult.barUploadResult', function (newVal, oldVal) {
                if (newVal !== undefined && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    $scope.item.barFile = newVal;
                }
            });

            $scope.$watch('uploadResult.mimetype', function (newVal, oldVal) {
                if (newVal && newVal.length > 0) {
                    if (newVal === 'video/x-flv' || newVal === 'video/mp4' || newVal === 'video/x-ms-wmv')
                        $scope.item.iTVCExtension = models.BalloonTVCExtension.INTEGRATED;
                    else
                        $scope.item.iTVCExtension = models.BalloonTVCExtension.NO_TVC;
                }
            });    

            $scope.$watch('uploadResult.bannerFallbackUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== "BEGIN") {
                    if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON ||
                        $scope.item.kind === models.CampaignItemType.BOOKING.EXPANDABLE ||
                        $scope.item.kind === models.CampaignItemType.NETWORK.EXPANDABLE ||
                        $scope.item.kind === models.CampaignItemType.NETWORK.BALLOON) {
                        $scope.item.backupFile = newVal;
                        return;
                    }
                    $scope.item.bannerFileFallback = newVal;
                }
            });

            $scope.$watch('uploadResult.expandBannerUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== "BEGIN") {
                    $scope.item.expandFile = newVal;
                }
            });

            $scope.$watch('uploadResult.tvcFileUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== "BEGIN") {
                    $scope.item.tvcFile = newVal;
                }
            });
            
            $scope.$watch('extlnkflash', function (newVal, oldVal) {
                if (newVal === true)
                    $scope.extlnkimage = false;
            });

            $scope.$watch('extlnkimage', function (newVal, oldVal) {
                if (newVal === true)
                    $scope.extlnkflash = false;
            });
            // >>>> -- watch -- <<<<
            $scope.chooseTVCExtension = (kind: string) => {
                $scope.tvc_extension = kind.toUpperCase();
            };

            $scope.addXData = () => {
                $scope.xitems.push(new models.kvItem("", ""));
            };
            // --- balloon
            $scope.chooseExpandStyle = (type: string) => {
                if (type === 'none') {
                    $scope.isExpand = false;
                    $scope.item.expandStyle = models.BalloonExpandStyle.NONE;
                    $scope.expand_style = "None";
                } else if (type === 'standard') {
                    $scope.isExpand = true;
                    $scope.item.expandStyle = models.BalloonExpandStyle.STANDARD;
                    $scope.expand_style = "Standard";
                } else if (type === 'auto') {
                    $scope.isExpand = true;
                    $scope.item.expandStyle = models.BalloonExpandStyle.AUTO;
                    $scope.expand_style = "Auto";
                }
            };

            $scope.chooseFreqTimeUnit = (unit: string) => {
                if (unit === models.EFrequencyCappingUnit.MINUTE) {
                    $scope.freq_time_string = "Minutes";
                } else if (unit === models.EFrequencyCappingUnit.HOUR) {
                    $scope.freq_time_string = "Hours";
                } else if (unit === models.EFrequencyCappingUnit.DAY) {
                    $scope.freq_time_string = "Days";
                }
            };

            // --- popup banner
            $scope.chooseCloseBtnAction = (action: string) => {
                if (action === models.EPopupActionClose.CLOSE) {
                    $scope.close_btn_action = "Close";
                    $scope.item.actionCloseBtn = models.EPopupActionClose.CLOSE;
                    return;
                }

                if (action === models.EPopupActionClose.MOVE_TO_TARGET) {
                    $scope.close_btn_action = "Move to target";
                    $scope.item.actionCloseBtn = models.EPopupActionClose.MOVE_TO_TARGET;
                    return;
                }
            };

            // ------------------
            var rateUnits = ["", "CPM", "CPC", "Creative view", "1/4 Video", "1/2 Video", "3/4 Video", "Full video"];
            $scope.rateUnit = rateUnits[$scope.item.rateUnit];
            $scope.hours = 0; $scope.minutes = 0; $scope.seconds = 0;

            var currentOffset = 0, count = 10000, totalItem;
            var websitesCache: Array<models.Website> = [];
            $scope.siteNameDic = {};
            listWebsite();

            function listWebsite() {
                factory.websiteService.listByTypeMinimize(currentOffset, count, types, function (ret: backend.ReturnList<models.Website>) {
                    if (ret && ret.data.length > 0) {
                        $scope.websites = $scope.websites.concat(ret.data);
                        websitesCache = $scope.websites;
                        ret.data.forEach((w, i) => {
                            $scope.siteNameDic[w.id] = w.name;
                        });
                        totalItem = ret.total;
                        var size = Math.min(count, ret.data.length);
                        currentOffset += size;
                        if (currentOffset < totalItem)
                            listWebsite();
                    }
                }, "name", "asc");
            }

            $scope.campaign = null;
            factory.campaignService.load($stateParams.campaignId, function (campRet) {
                if (campRet && campRet.id) $scope.campaign = campRet;
            });
            // ------------------ functions -------------------------------


            $scope.changeKey = (index: number) => {
                if ($scope.variables[index].key.search(/[^a-z0-9A-Z]+/g) !== -1 || $scope.variables[index].key.search(/^[0-9]+/g) !== -1) {
                    $scope.variables[index]["key"] = $scope.variables[index]["key"].replace(/[^a-z0-9A-Z]+/g, "");
                    $scope.variables[index]["key"] = $scope.variables[index]["key"].replace(/^[0-9]+/g, "");
                }
            }

            $scope.changeValue = (index: number) => {
                if ($scope.variables[index].value.search(/[|]+/g) !== -1)
                    $scope.variables[index]["value"] = $scope.variables[index]["value"].replace(/[|]+/g, "");
            }
            
            $scope.add_variable = () => {
                $scope.variables.push(new models.NetworkVariable("", null, 0));
            }
            $scope.remove_variable = (index) => {
                if (!!$scope.variables[index])
                    $scope.variables.splice(index, 1);
            }

            $scope.sizes = common.Size.ZoneSize;
            $scope.search = {};

            $scope.chooseSize = (value: string) => {
                $scope.search.value = value;
            };
            // --- Expandable Network
            $scope.chooseDirection = (type: string) => {
                if (type === 'top_push_down') {
                    $scope.item.expandDirection = models.EExpandDirection.TOP_PUSH_DOWN;
                    $scope.expand_direction = 'Top Push Down';
                    return;
                }
                if (type === 'right_to_left') {
                    $scope.item.expandDirection = models.EExpandDirection.RIGHT_TO_LEFT;
                    $scope.expand_direction = 'Right To Left';
                    return;
                }
            };

            $scope.chooseDisplayStyle = (type: string) => {
                if (type === 'overlay') {
                    $scope.item.displayStyle = models.EExpandDisplayStyle.OVERLAY;
                    $scope.expand_display_style = 'Overlay';
                    return;
                }
                if (type === 'push_down') {
                    $scope.item.displayStyle = models.EExpandDisplayStyle.PUSH_DOWN;
                    $scope.expand_display_style = 'Push Down';
                    return;
                }
            };

            $scope.chooseStyle = (type: string) => {
                if (type === 'mouse_over') {
                    $scope.item.expandStyle = models.EExpandActiveStyle.MOUSE_OVER;
                    $scope.expand_style = 'Mouse Over';
                    return;
                }
                if (type === 'auto') {
                    $scope.item.expandStyle = models.EExpandActiveStyle.AUTO;
                    $scope.expand_style = 'Auto';
                    return;
                }
            };
            // ---- End Expandable 
            $scope.$watch('search.value', function (newVal, oldVal) {
                if ($scope.search.value !== undefined && $scope.search.value.match("\\d+[\\x]\\d+")) {
                    var size = $scope.search.value.split('x');
                    $scope.item.width = size[0];
                    $scope.item.height = size[1];
                } else {
                    $scope.item.width = 0;
                    $scope.item.height = 0;
                }
                checkValidSize();
            });

            $scope.$watch("searchSite", function (newVal, oldVal) {
                if (newVal !== undefined) {
                    if (newVal.length > 0) {
                        $scope.websites = websitesCache.filter(function (w) {
                            if (w.name.toLowerCase().indexOf(newVal.toLowerCase()) !== -1)
                                return true;
                            return false;
                        });
                    }
                    else
                        $scope.websites = websitesCache;
                }
            });
            $scope.$watch("selectedZones", function (newVal, oldVal) {
                $scope.selectedZones.sort((a, b) => {
                    if (a['zone'] == b['zone'])
                        return a['pos'] - b['pos'];
                    return parseInt(a['zone']) - parseInt(b['zone']);
                });
            });

            

            $scope.choose = (type: string, value: string) => {
                switch (type) {
                    case "rate":
                        var rateUnits = ["", "CPM", "CPC", "Creative view", "1/4 Video", "1/2 Video", "3/4 Video", "Full video"];
                        var limitUnits = ["", "Impression", "Click"];
                        $scope.item.rateUnit = parseInt(value);
                        $scope.rateUnit = rateUnits[$scope.item.rateUnit];
                        $scope.item.limitUnit = limitUnits[$scope.item.rateUnit];
                        break;
                    case "limit":
                        $scope.item.limitUnit = value;
                        break;
                    case "platform":
                        $scope.item.targetPlatform = value;
                        break;
                    case "radio":
                        switch (value) {
                            case "specific":
                                $scope.selectedZones = [];
                                $scope.checkboxZone = {};
                                listZoneWebsite();
                                break;
                            case 'all':
                                listAllZone();
                                $scope.isWarningZoneSize = false;
                                break;
                            case 'notVN':
                                $scope.item.geographicTargetings = [{value: "VN", in: false}];
                                $scope.checkboxLocation = {};
                                break;
                            case 'VN':
                                $scope.item.geographicTargetings = [];
                                $scope.locations.forEach((a, i) => {
                                    $scope.item.geographicTargetings.push({ value: a.id, in: true });
                                    $scope.checkboxLocation[a.id] = true;
                                });
                                break;
                        }
                    case "tvc-content":
                        break;
                    case "targeting":
                        $scope.targeting[value] = true;
                        switch (value) {
                            case "website": $scope.sTargeting = "Website zone"; break;
                            case "age": $scope.sTargeting = "Demographic Age"; break;
                            case "gender": $scope.sTargeting = "Demographic gender"; break;
                            case "channel":
                                $scope.sTargeting = "Categories Channel";
                                factory.categoryService.listAll(function (categories: models.Category[]) {
                                    $scope.categories = categories;
                                });
                                break;
                            case "location":
                                $scope.sTargeting = "Location";
                                $scope.item.geographicTargetings = [];
                                $scope.belongLocation = { VN: 1 };
                                $scope.locations.forEach((a, i) => $scope.checkboxLocation[a.id] = true);
                                break;
                            case "interest": $scope.sTargeting = "User Interest"; break;
                            case "os": $scope.sTargeting = "Technology OS"; break;
                            case "resolution": $scope.sTargeting = "Technology Resolution"; break;
                            case "device": $scope.sTargeting = "Technology Device"; break;
                            case "site_variable": $scope.sTargeting = "Site Variable"; break;
                        }
                        break;

                    case 'changeLocation':
                        if ($scope.belongLocation["VN"] === 1) {
                            $scope.item.geographicTargetings = [];
                            $scope.locations.forEach((a, i) => {
                                if ($scope.checkboxLocation[a.id] === true)
                                    $scope.item.geographicTargetings.push({ value: a.id, in: true });
                            });
                            if ($scope.item.geographicTargetings.length === 0)
                                $scope.item.geographicTargetings = [{ value: "VN", in: true }];
                        }
                        break;
                }
            };

            $scope.checkWebsite = (id: number) => {
                if ($scope.checkboxWebsite[id] == true) {
                    $scope.selectedWebsites.push(id);
                }
                else {
                    var index = $scope.selectedWebsites.indexOf(id);
                    if (index >= 0) {
                        $scope.selectedWebsites.splice(index, 1);
                        $scope.checkboxZoneWeb[id] = false;
                        for (var i = $scope.selectedZones.length - 1; i >= 0; i--)
                            if ($scope.selectedZones[i]['website'] == id) {
                                $scope.selectedZones.splice(i, 1);
                            }
                    }
                }
                listZoneWebsite();
                if ($scope.targetDisplay == "suitable")
                    listAllZone()
                checkValidSize();
            };

            $scope.checkZone = (websiteId: number, zoneId: number, pos: string) => {
                if ($scope.checkboxZone[websiteId][zoneId][pos] == true) {
                    var isContain = false;
                    $scope.selectedZones.forEach((o, i) => {
                        if (o['website'] == websiteId && o['zone'] == zoneId && o['pos'] == pos) {
                            isContain = true; return;
                        }
                    });
                    if (!isContain)
                        $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: pos });
                }
                else {
                    $scope.selectedZones.forEach((o, i) => {
                        if (o['website'] == websiteId && o['zone'] == zoneId && o['pos'] == pos) {
                            $scope.selectedZones.splice(i, 1); return;
                        }
                    });
                }
                var ok = true;
                for (var zId in $scope.zones[websiteId]) {
                    if ($scope.item.kind.toLowerCase() !== models.CampaignItemType.NETWORK.TVC && $scope.checkboxZone[websiteId][zoneId][pos] !== $scope.checkboxZone[websiteId][zId][pos]) {
                        if (!document.getElementById("zone1" + zId).disabled)
                            ok = false;
                    }
                    else if ($scope.item.kind.toLowerCase() == models.CampaignItemType.NETWORK.TVC) {
                        var zone = $scope.zones[websiteId][zId];
                        zone.tvcPositions.forEach((p, __) => {
                            if ($scope.checkboxZone[websiteId][zoneId][pos] !== $scope.checkboxZone[websiteId][zId][p])
                                ok = false;
                        });
                    }
                }
                if (ok) $scope.checkboxZoneWeb[websiteId] = $scope.checkboxZone[websiteId][zoneId][pos];
            };

            $scope.checkZoneWeb = (websiteId: number) => {
                for (var zoneId in $scope.zones[websiteId]) {
                    if ($scope.kind === 'network_tvc') {//Filt by tvc
                        if (document.getElementById("zone1" + zoneId + "_Pre") && !document.getElementById("zone1" + zoneId + "_Pre").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Pre'] = $scope.checkboxZoneWeb[websiteId];
                        if (document.getElementById("zone1" + zoneId + "_Post") && !document.getElementById("zone1" + zoneId + "_Post").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Post'] = $scope.checkboxZoneWeb[websiteId];
                        if (document.getElementById("zone1" + zoneId + "_Mid") && !document.getElementById("zone1" + zoneId + "_Mid").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Mid'] = $scope.checkboxZoneWeb[websiteId];
                    } else {
                        if (!document.getElementById("zone1" + zoneId).disabled)
                            $scope.checkboxZone[websiteId][zoneId]['null'] = $scope.checkboxZoneWeb[websiteId];
                    }
                    if ($scope.checkboxZoneWeb[websiteId] == true) {
                        var isContain = false;
                        $scope.selectedZones.forEach((o, i) => {
                            if (o['website'] == websiteId && o['zone'] == zoneId) {
                                isContain = true; return;
                            }
                        });

                        if ($scope.kind === 'network_tvc') {//Filt by tvc
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Pre") && !document.getElementById("zone1" + zoneId + "_Pre").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Pre" });
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Mid") && !document.getElementById("zone1" + zoneId + "_Mid").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Mid" });
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Post") && !document.getElementById("zone1" + zoneId + "_Post").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Post" });
                        } else {
                            if (!isContain && !document.getElementById("zone1" + zoneId).disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "null" });
                        }
                    }
                    else {
                        $scope.selectedZones.forEach((o, i) => {
                            if (o['website'] == websiteId && o['zone'] == zoneId) {
                                $scope.selectedZones.splice(i, 1);
                            }
                        });
                    }
                }
            };
            $scope.uncheck = (type: string, index: number) => {
                switch (type) {
                    case "website":
                        var id = $scope.selectedWebsites[index];
                        $scope.selectedWebsites.splice(index, 1);
                        $scope.checkboxWebsite[id] = false;
                        $scope.checkboxZoneWeb[id] = false;
                        for (var i = $scope.selectedZones.length - 1; i >= 0; i--)
                            if ($scope.selectedZones[i]['website'] == id) {
                                $scope.selectedZones.splice(i, 1);
                            }
                        listZoneWebsite();
                        if ($scope.targetDisplay == "suitable")
                            listAllZone()
                        break;
                    case "zone":
                        var obj = $scope.selectedZones[index];
                        $scope.selectedZones.splice(index, 1);
                        $scope.checkboxZone[obj['website']][obj['zone']][obj['pos']] = false;
                        checkSelectZoneWeb(obj['website']);
                        break;
                }
            };
            $scope.remove_target = (target: string) => {
                $scope.targeting[target] = false;
                switch (target) {
                    case "website":
                        $scope.item.targetContent = [];
                        $scope.item.targetZones = [];
                        $scope.item.positions = [];
                        return;
                    case "site_variable":
                        $scope.variables = [];
                        return;
                    case "location": 
                        $scope.item.geographicTargetings = null;
                        $scope.checkboxLocation = {};
                        $scope.belongLocation = {};
                        return;
                }
            };
            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD/MM/YYYY');
            };

            $scope.save = () => {
                if ($scope.hasSave) return;
                if (($scope.item.name || "").length == 0)
                    return;
                if ($scope.item.kind === models.CampaignItemType.BOOKING.BANNER || $scope.item.kind === models.CampaignItemType.BOOKING.POPUP || $scope.item.kind === models.CampaignItemType.BOOKING.BALLOON ||
                    $scope.item.kind === models.CampaignItemType.NETWORK.BANNER || $scope.item.kind === models.CampaignItemType.NETWORK.BALLOON ||
                    $scope.item.kind === models.CampaignItemType.NETWORK.TVCBANNER) {

                    var size = $scope.search.value.split('x');
                    $scope.item.width = size[0];
                    $scope.item.height = size[1];
                }
                $scope.checkValidate = true;
                if ($scope.item.name && $scope.item.name.length == 0 )
                    return;
                if ($scope.item.kind !== models.CampaignItemType.NETWORK.TVC
                    && $scope.item.kind !== models.CampaignItemType.BOOKING.TRACKING
                    && $scope.item.kind !== models.CampaignItemType.NETWORK.PRBANNER
                    && $scope.item.kind !== models.CampaignItemType.BOOKING.PRBANNER
                    && (!$scope.item.width || !$scope.item.height))
                    return;
                if (($scope.item.kind === models.CampaignItemType.BOOKING.BANNER || $scope.item.kind === models.CampaignItemType.BOOKING.POPUP ||
                    $scope.item.kind === models.CampaignItemType.NETWORK.BANNER || $scope.item.kind === models.CampaignItemType.NETWORK.TVCBANNER) &&
                    (!$scope.item.bannerFile || $scope.item.bannerFile && $scope.item.bannerFile.length == 0))
                    return;
                if (($scope.item.kind === models.CampaignItemType.BOOKING.POPUP ||$scope.item.kind === models.CampaignItemType.NETWORK.TVCBANNER) &&
                    (!$scope.item.bannerFileFallback || $scope.item.bannerFileFallback && $scope.item.bannerFileFallback.length == 0))
                    return;
                if ($scope.item.kind === models.CampaignItemType.NETWORK.TVC && !$scope.item.wrapper && (!$scope.item.tvcFile || $scope.item.tvcFile && $scope.item.tvcFile.length == 0))
                    return;
                if ($scope.item.kind === models.CampaignItemType.NETWORK.TVC  && $scope.item.wrapper && (!$scope.item.extendURL || $scope.item.extendURL && $scope.item.extendURL.length == 0))
                    return;
                
                if (($scope.item.kind === models.CampaignItemType.BOOKING.EXPANDABLE ||
                    $scope.item.kind === models.CampaignItemType.NETWORK.EXPANDABLE) &&
                    (!$scope.item.standardFile || $scope.item.standardFile.length === 0 ||
                    !$scope.item.expandFile || $scope.item.expandFile.length === 0 ||
                    $scope.item.expandWidth === 0 || $scope.item.expandHeight === 0 ||
                    !$scope.item.backupFile || $scope.item.backupFile.length === 0))
                    return;

                 if ($scope.item.kind === models.CampaignItemType.BOOKING.BANNER || $scope.item.kind === models.CampaignItemType.NETWORK.BANNER) {
                     if ($scope.isExtlnk) {
                         if ($scope.extlnkflash)
                             $scope.item.bannerFile += "#adtima.swf";
                         if ($scope.extlnkimage)
                             $scope.item.bannerFile += "#adtima.jpg";
                     }
                 }

                // POP UP
                if ($scope.item.kind === models.CampaignItemType.BOOKING.POPUP || $scope.item.kind === models.CampaignItemType.NETWORK.POPUP) {
                    $scope.item.actionCloseBtn = models.EPopupActionClose.CLOSE;
                    if ($scope.close_btn_action === "Move to target")
                        $scope.item.actionCloseBtn = models.EPopupActionClose.MOVE_TO_TARGET;
                }

                 if ($scope.item.targetUrl !== undefined && !$scope.item.targetUrl.startWith("http")) {
                     $scope.item.targetUrl = "http://" + $scope.item.targetUrl;
                 }

                if ($scope.bStatus.iTVCExtension) {
                    if ($scope.item.tvcFile === undefined || $scope.item.tvcFile.length === 0)
                        return;
                    $scope.item.iTVCExtension = models.ETVCExtension.INTEGRATED;
                } else {
                    if ($scope.tvc_extension.toLowerCase() === 'tvc') {
                        if ($scope.item.tvcFile === undefined || $scope.item.tvcFile.length === 0)
                            return;
                        $scope.item.iTVCExtension = models.ETVCExtension.INTEGRATED;
                    } else if ($scope.tvc_extension.toLowerCase() === 'vast') {
                        if ($scope.item.tvcFile === undefined || $scope.item.tvcFile.length === 0)
                            return;
                        $scope.item.iTVCExtension = models.ETVCExtension.VAST;
                    }
                }

                // <<<< -- network -- >>>>
                if ($scope.isNetworkItem || $scope.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                    if (!$scope.item.limit || !$scope.item.rate || !$scope.item.lifetimeLimit)
                        return;
                    if ($scope.targetZonesOption === "specific" && $scope.selectedZones.length === 0) {
                        $scope.isWarningSelectZone = true;
                        $timeout(() => $scope.isWarningSelectZone = false, 3000);
                        return;
                    }
                    if ($scope.item.kind === models.CampaignItemType.NETWORK.TVC) {
                        if ($scope.item.wrapper)
                            $scope.item.tvcFile = "";
                        else
                            $scope.item.extendURL = "";
                    }
                    if (!$scope.unlimitStartDate) {
                        if ($scope.datepicker["startDate"] !== null) {
                            if (common.DateTimeUtils.getStartTimeOfDate($scope.datepicker['startDate']) < $scope.campaign.startDate) {
                                notify(new common.ActionMessage(common.ActionMessage.WARN, "Limit range time beyond the campaign time range ("
                                    + $scope.formatDateTime($scope.campaign.startDate) + " - " + $scope.formatDateTime($scope.campaign.endDate) + ")"), 5000)
                                return;
                            }
                            $scope.item.startDate = common.DateTimeUtils.getStartTimeOfDate($scope.datepicker['startDate']);
                        }
                    } else {
                        $scope.item.startDate = 0;
                    }
                    if (!$scope.unlimitEndDate) {
                        if ($scope.datepicker["endDate"] !== null) {
                            if (common.DateTimeUtils.getStartTimeOfDate($scope.datepicker['endDate']) > $scope.campaign.endDate) {
                                notify(new common.ActionMessage(common.ActionMessage.WARN, "Limit range time beyond the campaign time range ("
                                    + $scope.formatDateTime($scope.campaign.startDate) + " - " + $scope.formatDateTime($scope.campaign.endDate) + ")"), 5000)
                                return;
                            }
                            $scope.item.endDate = common.DateTimeUtils.getEndTimeOfDate($scope.datepicker["endDate"]);
                        }
                    } else {
                        $scope.item.endDate = 0;
                    }
                    $scope.item.timeSpan = ($scope.hours * 3600 + $scope.minutes * 60 + $scope.seconds * 1) * 1000;
                    $scope.item.targetContent = [];
                    $scope.selectedWebsites.forEach((v, i) => $scope.item.targetContent.push(v));

                    var targetZones = [];
                    if ($scope.targetZonesOption != "all") {
                        if ($scope.item.kind === models.CampaignItemType.NETWORK.TVC) {
                            $scope.selectedZones.forEach((v, i) => targetZones.push({ zoneId: v['zone'], position: v['pos'].toLowerCase() }));
                            $scope.item.positions = targetZones;
                        }
                        else {
                            $scope.selectedZones.forEach((v, i) => targetZones.push(v['zone']));
                            $scope.item.targetZones = targetZones;
                        }
                    }
                    else {
                        if ($scope.item.kind.toLowerCase() == 'networktvc')
                            $scope.item.positions = null;
                        else
                            $scope.item.targetZones = null;
                    }

                    // frequency capping
                    $scope.item.freqCapping = $scope.freq_impression;
                    if ($scope.freq_time_string.toLowerCase() === "minutes")
                        $scope.item.freqCappingTime = $scope.freq_time * 60;
                    else if ($scope.freq_time_string.toLowerCase() === "hours")
                        $scope.item.freqCappingTime = $scope.freq_time * 3600;
                    else if ($scope.freq_time_string.toLowerCase() === "days")
                        $scope.item.freqCappingTime = $scope.freq_time * 86400;

                    $scope.item.variables = [];
                    Object.keys($scope.osTargeting).forEach((a, _) => {
                        if ($scope.osTargeting[a] === true) {
                            $scope.item.variables.push({
                                "key": "os",
                                "value": a,
                                "bound": models.NetworkVariable.ISEQUAL
                            });
                        }
                    });
                    var variables = [], i = 0;
                    ($scope.variables||[]).forEach((v) => {
                        if (v.key.length !== 0 && v.value !== null) {
                            variables.push({});
                            Object.keys(v).forEach((key, __) => {
                                if (key.indexOf("$$hashKey") == -1) variables[i][key] = v[key];
                            });
                            i++;
                        }
                    });
                    $scope.item.variables = $scope.item.variables.concat(variables);

                    // option running time option
                    $scope.item.from = 0; $scope.item.to = 0;
                    if ($scope.runningTimeCustom) {
                        var startDate: Date = new Date($scope.datepicker['startDate'].getTime());
                        $scope.item.from = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate()).getTime();
                        var endDate: Date = new Date($scope.datepicker['endDate'].getTime());
                        $scope.item.to = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()).getTime() + 86400000;
                    }

                    if ($scope.item.tvcType === "skip") {
                        $scope.item.skip = true;
                        $scope.item.skipAfter = $scope.item.skipAfter || 0;
                    } else {
                        $scope.item.skip = false;
                        $scope.item.skipAfter = 0;
                    }

                    $scope.item.variables = $scope.item.variables || [];
                    $scope.retargeting.forEach((a) => $scope.item.variables.push(new models.NetworkVariable("retargeting", a.value, a.bound)));
                    // >>>> -- network -- <<<<

                    //--------- pr banner -----------
                    if ($scope.kind === models.CampaignItemType.NETWORK.PRBANNER ||
                        $scope.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                        $scope.item.categoryTypes = [];
                        $scope.selectedCates.forEach((a) => {
                            $scope.item.categoryTypes.push(a.id);
                        })
                        if ($scope.option.targetZone === "specific") {
                            $scope.item.targetZones = [];
                            $scope.selectedZones.forEach((v, i) => $scope.item.targetZones.push(v['zone']));
                        } else {
                            $scope.item.targetZones = null;
                        }
                    }
                    //--------- end pr banner -----------
                    $scope.item.companionTargetingValues = [];
                    $scope.companionTargetingValues.forEach((a) => $scope.item.companionTargetingValues.push(a.value));
                }
                // extendData
                if ($scope.xdata) {
                    var xObj: any = common.ConvertUtils.convertToObject($scope.xitems);
                    if (xObj) {
                        $scope.item.extendData = JSON.stringify(xObj);
                    }
                }

                if ($scope.item.thirdParty !== null && $scope.item.thirdParty !== undefined) {
                    $scope.item.thirdParty.impression = encodeURIComponent($scope.item.thirdParty.impression);
                    $scope.item.thirdParty.click = encodeURIComponent($scope.item.thirdParty.click);
                    $scope.item.thirdParty.complete = encodeURIComponent($scope.item.thirdParty.complete);
                }
                
                factory.campaignItemService.save($scope.item, function (response) {
                    if (response && response.id > 0) {
                        $location.hash("topPage");
                        $anchorScroll();
                        notify(new common.ActionMessage(0, "create", "network banner"));
                        if ($scope.isNetworkItem || $scope.item.kind === models.CampaignItemType.BOOKING.TRACKING) {
                            $timeout(function () {
                                $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
                            }, 1000);
                        } else {
                            $timeout(function () {
                                $state.transitionTo('main.order.item-booking', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: response.id });
                            }, 1000);
                        }
                        $scope.hasSave = true;
                        return;
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "network banner"))
                });
            }
            $scope.cancel = () => {
                $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
            };
            $scope.isBannerImage = (filename: string): boolean => {
                if (filename === undefined || filename.length === 0)
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
                if (filename === undefined || filename.length === 0)
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };
            //-------------
            function checkValidSize() {
                $scope.isWarningZoneSize = false;
                $scope.isWarningSize = false;
                if ($scope.kind !== "network_tvc" && $scope.kind !== "network_overlay_tvc" && $scope.kind !== "network_pause_ad_tvc" &&
                    $scope.kind !== models.CampaignItemType.NETWORK.PRBANNER &&
                    $scope.kind !== models.CampaignItemType.BOOKING.PRBANNER) {
                    $scope.selectedWebsites.forEach((wid, i) => {
                        for (var zid in $scope.zones[wid]) {
                            if ($scope.item.width >= 0 && $scope.item.width > $scope.zones[wid][zid].width ||
                                $scope.item.height >= 0 && $scope.item.height > $scope.zones[wid][zid].height) {
                                $("form #zone1" + zid).attr("disabled", "");
                                $("form #li1" + zid).each((i, el) => el.setAttribute("class", "unavailable"));
                                if($scope.targetZonesOption === "specific")
                                    $scope.isWarningZoneSize = true;
                                if ($scope.checkboxZone[wid][zid]['null']) {
                                    $scope.isWarningSize = true;
                                    $("form #li2" + zid).attr("style", "color: #d14; opacity: 0.5");
                                }
                            }
                            else if (!$scope.item.width || !$scope.item.height) {
                                if ($scope.targetZonesOption === "specific")
                                    $scope.isWarningZoneSize = true;
                                $("form #zone1" + zid).attr("disabled", "");
                                $("form #li1" + zid).each((i, el) => el.setAttribute("class", "unavailable"));
                            } else if ($scope.kind === 'expandable' && !$scope.zones[wid][zid].allowedExpand) {
                                // check expandable
                                $("form #zone1" + zid).attr("disabled", "");
                                $("form #li1" + zid).each((i, el) => el.setAttribute("class", "unavailable"));
                            } else if ($scope.item.width <= $scope.zones[wid][zid].width && $scope.item.height <= $scope.zones[wid][zid].height) {
                                $("form #zone1" + zid).removeAttr("disabled");
                                $("form #li1" + zid).each((i, el) => el.setAttribute("class", ""));
                                if ($scope.checkboxZone[wid][zid]['null']) {
                                    $("form #li2" + zid).attr("style", "");
                                }
                            }
                        }
                    });
                }
            }
            function checkSelectZoneWeb(websiteId: number) {
                var selected = false;
                for (var zId in $scope.checkboxZone[websiteId]) {
                    for (var pos in $scope.checkboxZone[websiteId][zId])
                        if ($scope.checkboxZone[websiteId][zId][pos] == true)
                            selected = true;
                }
                if (!selected && $scope.checkboxZoneWeb[websiteId] == true)
                    $scope.checkboxZoneWeb[websiteId] = false;
            }
            function listZoneWebsite() {
                $scope.zones = [];
                if (!$scope.checkboxZone)
                    $scope.checkboxZone = {};
                var zones: {};
                $scope.selectedWebsites.forEach(function (websiteId, index) {
                    var runningMode: string = "";
                    if ($scope.kind === "network_tvc")
                        runningMode = models.EZoneRunningMode.NETWORK_TVC;
                    else if ($scope.kind === models.CampaignItemType.NETWORK.TVCBANNER)
                        runningMode = models.EZoneRunningMode.NETWORKBANNER;
                    else
                        runningMode = models.EZoneRunningMode.NETWORK;
                    factory.zoneService.listByRefIdAndRunningMode(websiteId, 0, 10000, runningMode, function (zonesRet: backend.ReturnList<models.Zone>) {
                        if (zonesRet && zonesRet.data.length > 0) {
                            zones = {};
                            $scope.checkboxZone[websiteId] = {};
                            zonesRet.data.forEach((zone, index) => {
                                zones[zone.id] = zone;
                                $scope.checkboxZone[websiteId][zone.id] = {};
                                if ($scope.kind === 'network_tvc') {
                                    $scope.checkboxZone[websiteId][zone.id]["Pre"] = false;
                                    $scope.checkboxZone[websiteId][zone.id]["Mid"] = false;
                                    $scope.checkboxZone[websiteId][zone.id]["Post"] = false;
                                } else {
                                    $scope.checkboxZone[websiteId][zone.id]["null"] = false;
                                }
                            });
                            $scope.zones[websiteId] = zones;

                            $scope.selectedZones.forEach((o, i) => {
                                if ($scope.kind === 'network_tvc') {
                                    $scope.checkboxZone[o['website']][o['zone']]["Pre"] = true;
                                    $scope.checkboxZone[o['website']][o['zone']]["Mid"] = true;
                                    $scope.checkboxZone[o['website']][o['zone']]["Post"] = true;
                                } else {
                                    $scope.checkboxZone[o['website']][o['zone']]["null"] = true;
                                }
                            });
                            $timeout(() => {
                                checkValidSize();
                            }, 20);
                        }
                    });
                });
            }
            function listAllZone() {
                $scope.selectedZones = [];
                $scope.selectedWebsites.forEach(function (websiteId, index) {
                    var runningMode: string = "";
                    if ($scope.kind === "network_tvc")
                        runningMode = models.EZoneRunningMode.NETWORK_TVC;
                    else if ($scope.kind === models.CampaignItemType.NETWORK.TVCBANNER)
                        runningMode = models.EZoneRunningMode.NETWORKBANNER;
                    else
                        runningMode = models.EZoneRunningMode.NETWORK;
                    factory.zoneService.listByRefIdAndRunningMode(websiteId, 0, 10000, runningMode, function (zonesRet: backend.ReturnList<models.Zone>) {
                        if (zonesRet && zonesRet.data.length > 0) {
                            zonesRet.data.forEach((zone, index) => {
                                if ($scope.item.width >= 0 && $scope.item.width <= zone.width &&
                                    $scope.item.height >= 0 && $scope.item.height <= zone.height) {
                                    if ($scope.kind === 'network_tvc') {
                                        $scope.selectedZones.push({ website: websiteId, zone: zone.id, pos: "Pre" },
                                            { website: websiteId, zone: zone.id, pos: "Mid" },
                                            { website: websiteId, zone: zone.id, pos: "Post" });
                                        $scope.checkboxZone[websiteId][zone.id]["Pre"] = true;
                                        $scope.checkboxZone[websiteId][zone.id]["Mid"] = true;
                                        $scope.checkboxZone[websiteId][zone.id]["Post"] = true;
                                    } else {
                                        $scope.selectedZones.push({ website: websiteId, zone: zone.id, pos: "null" });
                                        $scope.checkboxZone[websiteId][zone.id]["null"] = true;
                                    }
                                }
                            });
                        }
                    });
                });
            }
        }
    }

    /*
     * ------------------------------------ CAMPAIGN ----------------------------------------------
     */
    export class CampaignTypeListController extends PermissionController {
        constructor($scope, $state, $stateParams, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.ORDER, $stateParams.orderId);

            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.createBookingCampaign = () => {
                $state.transitionTo("main.order.campaign_create", { orderId: $stateParams.orderId, type: "booking" });
                //$state.transitionTo('main.order.campaign_create', { orderId: $stateParams.orderId, type: 'booking' });

            };

            $scope.createNetworkCampaign = () => {
                $state.transitionTo('main.order.campaign_create', { orderId: $stateParams.orderId, type: 'network' });
            };

            $scope.createNetworkVideoCampaign = () => {
                $state.transitionTo('main.order.campaign_create', { orderId: $stateParams.orderId, type: 'networktvc' });
            }

            $scope.createPrNetworkCampaign = () => {
                $state.transitionTo('main.order.campaign_create', { orderId: $stateParams.orderId, type: 'prnetwork' });
            }
        }
    }

    export class CampaignDetailController extends PermissionController {
        constructor($scope: scopes.ICampaignDetailScope, factory: backend.Factory,
            $state, $stateParams, ActionMenuStore: utils.DataStore, $window) {

                $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
                super($scope, $state, factory);

                $scope.orderId = $stateParams.orderId;
                $scope.campaignId = $stateParams.campaignId;
                $scope.freeItemCount = 0;
                $scope.isBookingCampaign = false;
                $scope.isNetworkPRCampaign = false;

                reload();

                function reload() {
                    factory.campaignService.load($scope.campaignId, function (item: models.Campaign) {
                        if (item.id) {
                            $scope.item = item;
                            $scope.freeItemCount = item.unlinkItemCount;
                            // setup action menu
                            var action_menus: common.ActionMenu[] = [];
                            if ($scope.item.campaignType === models.CampaignType.BOOKING) {
                                $scope.isBookingCampaign = true;
                                action_menus.push(new common.ActionMenu("icon-plus", "Create Item", function () {
                                    $state.transitionTo('main.order.bookingitemtypeselect', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                                }, $scope.permissionDefine.EDITORDER));
                            } else if ($scope.item.campaignType === models.CampaignType.NETWORK) {
                                action_menus.push(new common.ActionMenu("icon-plus", "Create Item", function () {
                                    $state.transitionTo('main.order.networkitemtypeselect', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                                }, $scope.permissionDefine.EDITORDER));
                            }
                            else if ($scope.item.campaignType === models.CampaignType.NETWORK_TVC) {
                                action_menus.push(new common.ActionMenu("icon-plus", "Create Item", function () {
                                    $state.transitionTo('main.order.networkitemtypeselect', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                                }, $scope.permissionDefine.EDITORDER));
                            }
                            else if ($scope.item.campaignType === models.CampaignType.NETWORK_PR) {
                                action_menus.push(new common.ActionMenu("icon-plus", "Create Article", function () {
                                    $state.transitionTo('main.order.newprarticle', { orderId: $scope.orderId, campaignId: $scope.campaignId, kind: "pr" });
                                }, $scope.permissionDefine.EDITORDER));
                                $scope.isNetworkPRCampaign = true;
                            }
                            action_menus.push(new common.ActionMenu("icon-report-right", "Report", function () {
                                $window.open($state.href('main.report.campaign.item', { orderId: $scope.orderId, campaignId: $scope.campaignId, page: 1 }));
                            }, $scope.permissionDefine.REPORTORDER));
                            action_menus.push(new common.ActionMenu("icon-list-alt", "View log", function () {
                                $window.open($state.href("main.admin.userlog_detail", { objId: $scope.campaignId, objType: "Campaign" }));
                            }, $scope.permissionDefine.ROOT));

                            ActionMenuStore.store('main.order.campaign_detail.items', action_menus);
                            ActionMenuStore.store('main.order.campaign_detail.articles', action_menus);
                            ActionMenuStore.store('main.order.campaign_detail.setting', action_menus);

                            ActionMenuStore.store('main.order.campaign_detail.linkedbooking', action_menus);

                            $scope.$emit('change_action_menu', [$stateParams.orderId]);
                        }
                    });
                }


                //-------------------------- function ---------------------------
                $scope.$on("reload_campaign_detail", function () {
                    reload();
                });
                $scope.isActiveTab = (tabname: string): string => {
                    if (tabname === 'items' && $state.current.name === 'main.order.campaign_detail.items')
                        return 'active';
                    if (tabname === 'articles' && $state.current.name === 'main.order.campaign_detail.articles')
                        return 'active';
                    if (tabname === 'linked_booking' && $state.current.name === 'main.order.campaign_detail.linkedbooking')
                        return 'active';
                    if (tabname === 'setting' && $state.current.name === 'main.order.campaign_detail.setting')
                        return 'active';
                    if (tabname === 'media_plan' && $state.current.name === 'main.order.campaign_detail.media_plan')
                        return 'active';
                    return '';
                };

                $scope.gotoTab = (tabname: string) => {
                    if (tabname === 'items') {
                        $state.transitionTo('main.order.campaign_detail.items', { orderId: $scope.orderId, campaignId: $scope.campaignId, page: 1 });
                        return;
                    }
                    if (tabname === 'linked_booking') {
                        $state.transitionTo('main.order.campaign_detail.linkedbooking', { orderId: $scope.orderId, campaignId: $scope.campaignId, page: 1 });
                        return;
                    }
                    if (tabname === 'articles') {
                        $state.transitionTo('main.order.campaign_detail.articles', { orderId: $scope.orderId, campaignId: $scope.campaignId, page: 1 });
                        return;
                    }
                    if (tabname === 'setting') {
                        $state.transitionTo('main.order.campaign_detail.setting', { orderId: $scope.orderId, campaignId: $scope.campaignId, page: 1 });
                        return;
                    }
                    if (tabname === 'media_plan') {
                        $state.transitionTo('main.order.campaign_detail.media_plan', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                        return;
                    }
                }
                $scope.goUnlinkBookingZoneList = () => {
                    $state.transitionTo('main.order.unlink_booking_zone', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                };

                $scope.goUnlinkBookingItemList = () => {
                    $state.transitionTo('main.order.unlink_booking_item', { orderId: $scope.orderId, campaignId: $scope.campaignId });
                };
        }
    }

    export class CampaignUnlinkBookingItemListController extends PermissionController{
        constructor($state, $stateParams, $location: ng.ILocationService,
            $modal, $scope: scopes.IBookingCampaignItemListScope,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
            super($scope, $state, factory);
            $scope.$emit('change_action_menu', [$stateParams.orderId]);
            CurrentTab.setTab(new common.Tab('order', 'list_order'));

            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 20;
            $scope.totalRecord = 0;
            $scope.campaignZones = [];
            $scope.items = [];
            $scope.check = {}; $scope.bookingZoneDic = {};
            $scope.contentDic = {};
            $scope.orderId = $stateParams.orderId;
            $scope.sizeDic = {};
            factory.campaignItemService.listByCampaignId($stateParams.campaignId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, listCampaignItem);
            function listCampaignItem(result: backend.ReturnList<any>) {
                if (result === undefined || result === null)
                    return;

                // Will be remove soon
                var total: number = 0;
                for (var i: number = 0, len: number = result.data.length; i < len; i++) {
                    var bookingBannerItem: models.AdItem = result.data[i];
                    if (bookingBannerItem.linkedZones > 0)
                        continue;
                    $scope.items.push(bookingBannerItem);

                    if (models.CampaignItemType.isBannerNotExpandable(bookingBannerItem.kind)) {
                        var mediaItem: models.MediaItem = result.data[i];
                        $scope.contentDic[bookingBannerItem.id] = mediaItem.bannerFile;
                    } else if (models.CampaignItemType.isBannerWithExpandable(bookingBannerItem.kind)) {
                        var expandableItem: models.ExpandableItem = result.data[i];
                        $scope.contentDic[bookingBannerItem.id] = expandableItem.standardFile;
                    } else if (models.CampaignItemType.isHTML(bookingBannerItem.kind)) {
                        var htmlItem: models.HtmlItem = result.data[i];
                        $scope.contentDic[bookingBannerItem.id] = htmlItem.embeddedHtml;
                    }
                    $scope.sizeDic[bookingBannerItem.id] = { width: bookingBannerItem.width, height: bookingBannerItem.height };
                    total++;
                }
                $scope.totalRecord = total;
            }
            $scope.paging = (start: number, size: number) => {
                factory.campaignItemService.listByCampaignId($stateParams.campaignId, start, size, listCampaignItem);
            };

            $scope.book = (itemId: number) => {
                $state.transitionTo('main.order.item-booking', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: itemId });
            };

            $scope.getFullUrl = (image_url: string): string => {
                if (image_url === undefined || image_url === null || image_url.length === 0)
                    return "";
                return image_url;
            };

            

            $scope.closeModal = (target: string) => {
                jQuery(target).modal('hide');
            };
            
            $scope.isBannerImage = (filename: string): boolean => {
                if (filename === undefined  || filename.length === 0)
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
                if (filename === undefined || filename.length === 0)
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };

            $scope.isDisableLinkStyle = (zoneId: number): any => {
                var zone: models.BookingZone = $scope.bookingZoneDic[zoneId];
                var item_width: number = 0;
                var item_height: number = 0;
                var zone_width: number = zone.width; var zone_height: number = zone.height;

                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.items[i].id === $scope.bookingItemId) {
                        item_width = $scope.items[i].width;
                        item_height = $scope.items[i].height;
                        break;
                    }
                }

                if (item_width !== 0 && item_height !== 0) {
                    if (item_width > zone_width || item_height > zone_height)
                        return { color: '#BBBBBB' };
                    return {};
                }
                return {};
            };

            $scope.isDisable = (zoneId: number): boolean => {
                var zone: models.BookingZone = $scope.bookingZoneDic[zoneId];
                var item_width: number = 0;
                var item_height: number = 0;
                var zone_width: number = zone.width; var zone_height: number = zone.height;

                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.items[i].id === $scope.bookingItemId) {
                        item_width = $scope.items[i].width;
                        item_height = $scope.items[i].height;
                        break;
                    }
                }

                if (item_width !== 0 && item_height !== 0) {
                    if (item_width > zone_width || item_height > zone_height)
                        return true;
                    return false;
                }
                return false;
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
                    $scope.previewSource = "";
                    $scope.previewTitle = name;
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

    export class CampaignListController extends PermissionController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $modal, $scope: scopes.ICampaignListScope,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
            super($scope, $state, factory);
            $scope.$emit('change_action_menu', [$stateParams.orderId]);
            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.sortField = { status: common.SortDefinition.DEFAULT, campaignType: common.SortDefinition.DEFAULT };
            $scope.sortField['name'] = common.SortDefinition.DEFAULT;
            $scope.filterText = 'All Campaign';
            //scope initializationbvc

            var pageIndex: number = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;
            $scope.checkBoxes = {};
            $scope.checkBoxes['all'] = false;
            $scope.searchText = '';
            $scope.orderId = $stateParams.orderId;
            $scope.items = [];
            $scope.isChosen = false;
            listCampaign($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);

            $scope.currentSelectedRow = -1;
            var currentSortField = "", currentSortType = "";

            function listCampaign(from: number, count: number, sortBy?: string, sortType?: string) {
                factory.campaignService.listByOrderId($scope.orderId, from, count, function (returnedList) {
                    $scope.items = returnedList.data;
                    $scope.totalRecord = returnedList.total;
                }, sortBy, sortType);
                $scope.checkBoxes = {};
                currentSortField = sortBy;
                currentSortType = sortType;
            }
                ////----------------------------
                //// SCOPE FUNCTIONS
                ////-----------------------------------

            $scope.paging = (start: number, size: number) => {
                //var sortType: string = "desc";
                //if ($scope.sortField[currentSortField] === common.SortDefinition.UP)
                //    sortType = "asc";
                listCampaign(start, size, currentSortField, currentSortType);
            };
            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

            $scope.getSortClass = (type: string): string => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            };

            $scope.getCampaignTypeClass = (type: string): string => {
                if (type === "network")
                    return "label-pink";
                else if (type === "networkTVC")
                    return "label-violet";
                return "label-green";
            };

            $scope.getCampaignStatusClass = (status: string): string => {
                if (status === "Running")
                    return "label-blue";
                else if (status === "Paused")
                    return "label-orange";
            };

            $scope.switchSort = (type: string) => {
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    listCampaign($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "asc");
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    listCampaign($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "asc");
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    listCampaign($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "desc");
                    $scope.sortField[type] = common.SortDefinition.UP;
                }
                currentSortField = type;
                for (var aType in $scope.sortField)
                    if (type != aType)
                        $scope.sortField[aType] = common.SortDefinition.DEFAULT;
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

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            };
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            };

            $scope.formatDateTime = (timestamp: number): string => {
                if (timestamp == 0) return "---";
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MM - YYYY');
            };

            $scope.formatNumber = (num: number): string => {
                return num.toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
            };
            $scope.gotoCampaign = (campaignid: number) => {
                factory.campaignService.load(campaignid, function (campaign: models.Campaign) {
                    //if (campaign.campaignType === models.CampaignType.NETWORK_PR)
                    //    $state.transitionTo('main.order.campaign_detail.articles', { orderId: $scope.orderId, campaignId: campaignid });
                    //else
                        $state.transitionTo('main.order.campaign_detail.items', { orderId: $scope.orderId, campaignId: campaignid });
                });
            };

            $scope.gotoCampaignSetting = (campaignid: number) => {
                $state.transitionTo('main.order.campaign_detail.setting', { orderId: $scope.orderId, campaignId: campaignid });
            };


            $scope.disable = () => {
                var isHave: boolean = false;
                for (var att in $scope.checkBoxes) {
                    if ($scope.checkBoxes[att] == true) {
                        isHave = true;
                        break;
                    }
                }
                if (isHave) {
                    var orderModal = $modal.open({
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
                                return 'campaign';
                            }
                        }
                    });

                    orderModal.result.then(function (checkList) {
                        var camIds: number[] = [];
                        for (var i: number = 0; i < checkList.length; i++) {
                            var orderid: number = checkList[i].id;
                            camIds.push(orderid);
                        }
                        for (var i: number = 0; i < camIds.length; i++) {
                            factory.campaignService.remove(camIds[i], function (result) {
                                if (result === 'success') {
                                    reloadCampaign();
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "disable", "campaign"));
                                }
                            });
                        }
                    }, function(message) {
                    });
                }
            };
                $scope.enable = function() {
                    var isHave: boolean = false;
                    for (var att in $scope.checkBoxes) {
                        if ($scope.checkBoxes[att] == true) {
                            isHave = true;
                            break;
                        }
                    }
                    if (isHave) {
                        var orderModal = $modal.open({
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
                                    return 'enable_camp';
                                }
                            }
                        });

                        orderModal.result.then(function (checkList) {
                            var camIds: number[] = [];
                            for (var i: number = 0; i < checkList.length; i++) {
                                var orderid: number = checkList[i].id;
                                camIds.push(orderid);
                            }
                            for (var i: number = 0; i < camIds.length; i++) {
                                factory.campaignService.enable(camIds[i], function (result) {
                                    if (result === 'success') {
                                        reloadCampaign();
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "enable", "campaign"));
                                    }
                                });
                            }
                        }, function (message) { });
                    }
                };
            $scope.pause = () => {
                var campaignIds: number[] = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    var orderid: number = $scope.items[i].id;
                    if ($scope.checkBoxes[orderid]) {
                        campaignIds.push(orderid);
                    }
                }
                for (var i: number = 0; i < campaignIds.length; i++) {
                    factory.campaignService.setStatus(campaignIds[i], models.CampaignStatus.PAUSED, function (response) {
                        reloadCampaign();
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "pause", "campaign"));
                    });
                }
            };

            $scope.resume = () => {
                var campaignIds: number[] = [];
                for (var i: number = 0; i < $scope.items.length; i++) {
                    var orderid: number = $scope.items[i].id;

                    if ($scope.checkBoxes[orderid]) {
                        campaignIds.push(orderid);
                    }
                }
                for (var i: number = 0; i < campaignIds.length; i++) {
                    factory.campaignService.setStatus(campaignIds[i], models.CampaignStatus.RUNNING, function (response) {
                        reloadCampaign();
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "resume", "campaign"));
                    });
                }
            };

            $scope.search = () => {
                factory.campaignService.search($scope.searchText, $scope.orderId, false, function (list: Array<models.Campaign>) {
                    $scope.items = list;
                    $scope.totalRecord = list.length;
                    $scope.pageIndex = 1;
                    $scope.checkBoxes = {};
                });
            };

            $scope.select = (type: string) => {
                if (type === 'all' && $scope.filterText.toLowerCase() !== 'all campaign') {
                    listCampaign($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                    $scope.filterText = 'All Campaign';
                }
                if (type === 'disable' && $scope.filterText.toLowerCase() !== 'disable campaign') {
                    factory.campaignService.listDisable($scope.orderId, $scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, function (returnedList: backend.ReturnList<models.Campaign>) {
                        $scope.items = returnedList.data;
                        $scope.totalRecord = returnedList.total;
                        $scope.checkBoxes = {};
                    });
                    $scope.filterText = 'Disable Campaign';
                }

            };

            function reloadCampaign() {
                factory.campaignService.listByOrderId($scope.orderId, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, function (returnedList) {
                    $scope.items = returnedList.data;
                    $scope.totalRecord = returnedList.total;
                    $scope.checkBoxes = {};
                    $scope.select("all");
                    $scope.isChosen = false;
                }); 
            }
        }
    }

    export class CampaignCreateController extends PermissionController {
        constructor($scope: scopes.ICampaignScope, $timeout, $state, $stateParams,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.ORDER, $stateParams.orderId);
            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.orderId = $stateParams.orderId; $scope.campaignType = $stateParams.type;
            if ($scope.campaignType === 'networktvc')
                $scope.campaignType = models.CampaignType.NETWORK_TVC;
            else if ($scope.campaignType === 'prnetwork')
                $scope.campaignType = models.CampaignType.NETWORK_PR;

            $scope.datepicker = { "startDate": new Date(), "endDate": new Date() };
            $scope.displayTypeString = "None";
            $scope.displayType = 0;

            if ($scope.campaignType === 'booking') {
                $scope.title = 'Booking Campaign';
                $scope.datepicker['startDate'] = new Date();
                $scope.datepicker['endDate'] = new Date();
            } else if ($scope.campaignType === 'network') {
                $scope.freq_impression = 0;
                $scope.freq_time = 0;
                $scope.freq_time_string = "Minutes";
                $scope.title = 'Network Campaign';
            } else if ($scope.campaignType === 'networktvc') {
                $scope.title = 'TVC Campaign';
            } else {
                $scope.title = 'PR Network Campaign';
            }

            $scope.item = new models.Campaign(0, '', $scope.orderId, 0, 0, '', '');
            $scope.item.companion = false;

            $scope.startDate = '';
            $scope.endDate = '';
            $scope.checkValidate = false;
            $scope.timeunit = 'minute';
            $scope.timeUnitString = "Minutes";
            $scope.impression = 0;
            $scope.time = 0;
            $scope.hasSave = false;
            
            //--------------------------- function ----------------------------
            $scope.$watch('timeinday', function (newValue, oldValue) {

            });
            $scope.isNetworkCampaign = (): boolean => {
                if ($scope.campaignType === models.CampaignType.BOOKING)
                    return false;
                return true;
            };

            $scope.validateStartTime = () => {
                var cur = new Date();
                cur.setDate(cur.getDate() - 1);
                var startDate = $scope.datepicker['startDate'];

                if (startDate < cur)
                    return true;
                return false;
            };

            $scope.validateEndTime = () => {
                if ($scope.datepicker['startDate'].getTime() > $scope.datepicker['endDate'].getTime()) {
                    return true;
                }
                return false;
            };

            $scope.validateNumber = (num: number) => {
                if ($scope.checkValidate) {
                    if (num > 0)
                        return false; // valid : return false;
                    return true;
                }
                return false;
            };

            $scope.isDisable = (frm: any): boolean => {
                if ($scope.campaignType === models.CampaignType.BOOKING) {
                    var result = $scope.item.name.length == 0 || ($scope.datepicker['endDate'] == null) || ($scope.datepicker['startDate'] == null) || $scope.hasSave;
                    return result;
                }
                if ($scope.campaignType === models.CampaignType.NETWORK || $scope.campaignType === models.CampaignType.NETWORK_TVC) {
                    return $scope.hasSave;
                }

            };

            

            $scope.chooseTimeUnit = (unit: string) => {
                $scope.timeunit = unit;
                switch (unit) {
                    case "minute":
                        $scope.timeUnitString = "Minutes";
                        break;
                    case "hour":
                        $scope.timeUnitString = "Hours";
                        break;
                    case "day":
                        $scope.timeUnitString = "Days";
                        break;
                }
            };

            $scope.chooseDisplayType = (displayType: number) => {
                $scope.displayType = displayType;
                switch (displayType) {
                    case 0:
                        $scope.displayTypeString = "All";
                        $scope.item.companion = true;
                        break;
                    case 1:
                        $scope.displayTypeString = "As many as possible";
                        $scope.item.companion = false;
                        break;
                    case 2:
                        $scope.displayTypeString = "One or more";
                        $scope.item.companion = false;
                        break;
                    case 3:
                        $scope.displayTypeString = "Only one";
                        $scope.item.companion = false;
                        break;
                    case -1:
                        $scope.displayTypeString = "None";
                        $scope.item.companion = false;
                }
            };

            $scope.save = () => {
                $scope.checkValidate = true;
                if ($scope.hasSave) return;
                if ($scope.isNetworkCampaign()) {
                    if ($scope.validateStartTime() || $scope.validateEndTime())
                        return;
                    /*
                    if ($scope.impression <= 0 || $scope.time <= 0)
                        return;
                    */
                    var item: models.NetworkCampaign = new models.NetworkCampaign(0, '', $scope.orderId, 0, 0, '', '', 0, 0, 0, 0, '', 0);
                    if ($scope.item.name === undefined || $scope.item.name.length === 0)
                        return;
                    if ($scope.datepicker["startDate"] !== null) {
                        var startDate: Date = common.DateTimeUtils.get0hDate($scope.datepicker["startDate"]);
                        item.startDate = startDate.getTime();
                    }
                    if ($scope.datepicker["endDate"] !== null) {
                        var endDate: Date = common.DateTimeUtils.get0hDate($scope.datepicker["endDate"]); 
                        item.endDate = endDate.getTime() + 86400000;
                    }
                    item.name = $scope.item.name;
                    item.campaignType = $scope.campaignType;
                    if ($scope.timeinday && $scope.timeinday['data'] !== undefined && $scope.timeinday['time'] !== undefined) {
                        var timeinday = [];
                        for (var day in $scope.timeinday['data']) {
                            timeinday.push($scope.timeinday['data'][day]);
                        }
                        item.timeScheduled = timeinday;
                        item.timeZone = models.NetworkCampaignTimeZoneType.USER;
                        if ($scope.timeinday['time'] === 'publisher')
                            item.timeZone = models.NetworkCampaignTimeZoneType.PUBLISHER;
                    } else {
                        // running all time with user timezone
                        item.timeZone = models.NetworkCampaignTimeZoneType.USER;
                        item.timeScheduled = [];
                        for (var i: number = 0; i < 7; i++) {
                            item.timeScheduled.push(Math.pow(2, 24) - 1);
                        }
                    }
                    item.freqCapping = $scope.impression;
                    item.freqCappingTime = $scope.time;
                    item.freqCappingTimeUnit = $scope.timeunit;
                    item.displayType = $scope.displayType;
                    item.status = 'Running';
                    $scope.hasSave = true;
                    factory.campaignService.save(item, function (campaign: models.Campaign) {
                        if (campaign !== undefined && campaign.id > 0) {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "campaign"));
                            $timeout(function () {
                                $state.transitionTo('main.order.detail.campaign', { orderId: $stateParams.orderId });
                            }, 1000);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "campaign"));
                    });
                } else {
                    // BOOKING
                    $scope.checkValidate = true;
                    if ($scope.item.name === undefined || $scope.item.name.length === 0)
                        return;
                    if ($scope.datepicker["startDate"] !== null) {
                        var startDate: Date = new Date($scope.datepicker["startDate"].getFullYear(), $scope.datepicker["startDate"].getMonth(), $scope.datepicker["startDate"].getDate());
                        $scope.item.startDate = startDate.getTime();
                    }
                    if ($scope.datepicker["endDate"] !== null) {
                        $scope.item.endDate = $scope.datepicker["endDate"].getTime() + new Date().getTimezoneOffset() * 60000 + 86400000;
                    }
                    $scope.item.campaignType = $scope.campaignType;
                    $scope.item.status = 'Running';
                    $scope.hasSave = true;
                    factory.campaignService.save($scope.item, function (campaign: models.Campaign) {
                        if (campaign !== undefined && campaign.id) {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "campaign"));
                            $timeout(() => $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 }), 1000);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "create", "campaign"));
                    });
                };
            }

            $scope.goto = (dest: string) => {
                if (dest === 'campaigns') {
                    $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 });
                }
            };

            $scope.isAllDayTime = (): boolean => {
                if ($scope.timeinday === undefined || $scope.timeinday === null)
                    return true;
                return false;
            };
        }
    }

    /*
     *  Linked Zone ----------------------------------------------------------------
     */

    export class CampaignLinkedZoneListController {
        constructor($state, $stateParams, $location: ng.ILocationService,
            $scope: scopes.ILinkedZoneListScope, $modal, CurrentTab: common.CurrentTab, factory: backend.Factory) {
                $scope.$emit('change_action_menu', [$stateParams.orderId]);
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            var pageIndex = $stateParams.page;
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;

            $scope.campaignId = $stateParams.campaignId;
            $scope.checkBoxes = {};
            $scope.isChosen = false;
            $scope.items = []; $scope.totalRecord = 0;
            $scope.linkItems = [];
            $scope.checkItems = {};
            $scope.isMultiChoice = false;
            $scope.isChosen = false;
            var returnedList: backend.ReturnList<models.Zone> = null;
                listBooked();

                function listBooked() {
                    factory.bookService.getBookedByKind($stateParams.campaignId, "camp", 0, 0, function (ret: Array<models.BookRecord>) {
                        if (ret) {
                            $scope.items = ret;
                            $scope.totalRecord = ret.length;
                        }
                        $scope.isChosen = false;
                        $scope.checkBoxes = {};
                    });
                }

            // --------------- function ---------------------------------               
            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.items[i].disable) continue;
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
                if ($scope.checkBoxes['all'])
                    $scope.isMultiChoice = true;
            });


            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MM - YYYY');
            };
            $scope.check = (itemId: number) => {
                if ($scope.checkBoxes[itemId]) {
                    $scope.isChosen = true;
                    $scope.isMultiChoice = false;
                    for (var i: number = 0; i < $scope.items.length; i++) {
                        if ($scope.items[i].id === itemId)
                            continue;

                        if ($scope.checkBoxes[$scope.items[i].id]) {
                            $scope.isMultiChoice = true; return;
                        }
                    }
                    return;
                }

                $scope.isChosen = false;
                $scope.isMultiChoice = false;

                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        if ($scope.isChosen) {
                            $scope.isMultiChoice = true; return;
                        }
                        $scope.isChosen = true;
                    }
                }
            };

            $scope.isActiveClass = (): string => {
                if ($scope.isChosen)
                    return "";
                return "disabled";
            };

            $scope.closeModal = (target: string) => {
                jQuery(target).modal("hide");

            };

            $scope.delete = () => {
                var orderModal = $modal.open({
                    templateUrl: 'views/common/modal.delete',
                    controller: common.ModalDeleteController,
                    resolve: {
                        checkedList: function () {
                            var checkedList = [];//list of object contain id and name
                            for (var att in $scope.checkBoxes) {
                                if ($scope.checkBoxes[att] == true) {
                                    for (var i = 0; i < $scope.items.length; i++) {
                                        if ($scope.items[i].id == att) {
                                            checkedList.push({ id: att, name: "Zone: " + $scope.items[i].zoneName + ", Website: " + $scope.items[i].siteName });
                                        }
                                    }
                                }
                            }
                            return checkedList;
                        },
                        type: function () {
                            return 'delete_booking_item';
                        }
                    }
                });

                orderModal.result.then(function (checkList) {
                    var ids: Array<number> = [];
                    for (var i = 0; i < $scope.items.length; i++) {
                        if ($scope.checkBoxes[$scope.items[i].id])
                            ids.push($scope.items[i].id);
                    }
                    factory.bookService.removeBook(ids, function (respond) {
                        listBooked();
                    });

                }, function (message) { });

            };
        }
    }

    export class ItemLinkedBookingController {
        constructor($state, $stateParams, $location: ng.ILocationService, $scope: scopes.ILinkedZoneListScope,
            $modal, Page: common.PageUtils, CurrentTab: common.CurrentTab, factory: backend.Factory) {

            var pageIndex = $stateParams.page;
            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            Page.setCurrPage('List Linked Zone');
            $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
            $scope.pageSize = 10;

            $scope.campaignId = $stateParams.campaignId;
            $scope.itemId = $stateParams.itemId;
            $scope.checkBoxes = {};
            $scope.items = [];
            $scope.totalRecord = 0;
            $scope.checkZones = {};
            $scope.datepicker = {};

            listBooked();

            factory.campaignItemService.load($stateParams.itemId, function (item: models.AdItem) {
                $scope.chooseItem = item;
            });
            

            // function
            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                for (var i: number = 0; i < $scope.items.length; i++) {
                    $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });
            $scope.$watch("datepicker.book_zone_start", function (newVal, oldVal) {
                if ($scope.datepicker["book_zone_start"] && $scope.datepicker["book_zone_end"]) {
                    var start_time: number = parseInt($scope.datepicker["book_zone_start"].getTime()) + new Date().getTimezoneOffset() * 60000;
                    var end_time: number = parseInt($scope.datepicker["book_zone_end"].getTime()) + new Date().getTimezoneOffset() * 60000 + 86400000;
                    if (start_time < end_time) {
                        factory.bookService.getUsageByZone($scope.editingItem.zoneId, start_time, end_time, function (response) {
                            $scope.zone_available = 100 - response.total;
                        });
                    }
                }
            });
            $scope.$watch("datepicker.book_zone_end", function (newVal, oldVal) {
                if ($scope.datepicker["book_zone_start"] && $scope.datepicker["book_zone_end"]) {
                    var start_time: number = parseInt($scope.datepicker["book_zone_start"].getTime()) + new Date().getTimezoneOffset() * 60000;
                    var end_time: number = parseInt($scope.datepicker["book_zone_end"].getTime()) + new Date().getTimezoneOffset() * 60000 + 86400000;
                    if (start_time < end_time) {
                        factory.bookService.getUsageByZone($scope.editingItem.zoneId, start_time, end_time, function (response) {
                            $scope.zone_available = 100 - response.total;
                        });
                    }
                }
            });
            
            $scope.closeModal = (target: string) => {
                jQuery(target).modal("hide");
            }
                
            $scope.delete = () => {
                var orderModal = $modal.open({
                    templateUrl: 'views/common/modal.delete',
                    controller: common.ModalDeleteController,
                    resolve: {
                        checkedList: function () {
                            var checkedList = [];//list of object contain id and name
                            for (var att in $scope.checkBoxes) {
                                if ($scope.checkBoxes[att] == true) {
                                    for (var i = 0; i < $scope.items.length; i++) {
                                        if ($scope.items[i].id == att) {
                                            checkedList.push({ id: att, name: "Zone: " + $scope.items[i].zoneName + ", Website: " + $scope.items[i].siteName });
                                        }
                                    }
                                }
                            }
                            return checkedList;
                        },
                        type: function () {
                            return 'delete_booking_item';
                        }
                    }
                });

                orderModal.result.then(function (checkList) {
                    var ids: Array<number> = [];
                    for (var i = 0; i < $scope.items.length; i++) {
                        if ($scope.checkBoxes[$scope.items[i].id])
                            ids.push($scope.items[i].id);
                    }
                    factory.bookService.removeBook(ids, function (respond) {
                        listBooked();
                    });
                    
                }, function (message) { });

            };

            $scope.isActiveClass = () => {
                if ($scope.isChosen)
                    return "";
                return " disabled";
            }
            $scope.check = (id: number) => {
                if ($scope.checkBoxes[id]) {
                    $scope.isChosen = true;
                    return;
                }

                $scope.isChosen = false;
            }
            $scope.newBooking = () => {
                $state.transitionTo("main.order.item-booking", {orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: $stateParams.itemId});
            }
            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MM - YYYY');
            };

            $scope.formatNumber = (num: number): string => {
                return num.toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
            };

            $scope.gotoEdit = (id: number, targetModel: string) => {
                $scope.warningExtend = false;
                $scope.items.forEach((item, _) => {
                    if (item.id == id) {
                        $scope.editingItem = new models.BookRecord(0, 0, "", 0, "", 0, 0, 0);
                        jQuery.extend($scope.editingItem, item);
                    }
                });
                $scope.datepicker["book_zone_start"] = new Date($scope.editingItem.from - new Date().getTimezoneOffset() * 60000);
                $scope.datepicker["book_zone_end"] = new Date($scope.editingItem.to - new Date().getTimezoneOffset() * 60000 - 1);
                factory.bookService.getUsageByZone($scope.editingItem.zoneId, $scope.editingItem.from, $scope.editingItem.to, function (response) {
                    $scope.zone_available = 100 - response.total;
                    jQuery(targetModel).modal("show");
                });
            }
            $scope.update = (targetModel: string) => {
                //var item: models.BookRecord = new models.BookRecord($scope.editingItem.id, $scope.editingItem.zoneId, $scope.editingItem.itemId, $scope.editingItem.from, $scope.editingItem.to, $scope.editingItem.share);
                $scope.editingItem.from = parseInt($scope.datepicker["book_zone_start"].getTime()) + new Date().getTimezoneOffset() * 60000;
                $scope.editingItem.to = parseInt($scope.datepicker["book_zone_end"].getTime()) + new Date().getTimezoneOffset() * 60000 + 86400000;
                factory.bookService.update($scope.editingItem, function (respond) {
                    listBooked();
                    jQuery(targetModel).modal("hide");
                }, function (data, status) {
                    if (status === 500)
                        $scope.warningExtend = true;
                });
            }

            function listBooked() {
                factory.bookService.getBookedByKind($stateParams.itemId, "item", 0, 0, function (rets: Array<models.BookRecord>) {
                    if (rets) {
                        $scope.items = rets;
                        $scope.totalRecord = rets.length;
                    }
                });
            }
        }
    }

    export class CampaignItemSettingController extends PermissionController {
        constructor($scope: scopes.ICampaignItemSettingScope, $state, $stateParams, $timeout, $location, $anchorScroll,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {

            $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
            super($scope, $state, factory);
            $scope.$emit('change_action_menu', [$stateParams.orderId]);
            //scope initialization
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            $scope.orderId = $stateParams.orderId;
            
            $scope.checkValidate = false;
            $scope.uploadUrl = common.Config.UPLOAD_URL;
            $scope.bookedZones = []; $scope.item_kind = "Item Information";
            $scope.uploadResult = {
                bannerUploadResult: '',
                bannerFallbackUploadResult: '',
                barUploadResult: '',
                expandBannerUploadResult: '',
                tvcFileUploadResult: '',
                mimetype: '',
                duration: 0
            };
                $scope.bStatus = {
                    iTVCExtension: false
                };

            $scope.selectedWebsites = [];
            $scope.checkboxWebsite = {};
            $scope.zones = [];
            $scope.selectedZones = [];
            $scope.checkboxZone = {};
            $scope.targetDisplay = "all";
            $scope.websites = [];
            $scope.locations = models.Location.CITIES.VN;
            $scope.checkboxLocation = {};
                $scope.belongLocation = {};
            $scope.isWarningSize = false;
            $scope.datepicker = {
                "startDate": new Date(), "endDate": new Date()
            };
            var rateUnits = ["", "CPM", "CPC", "Creative view", "1/4 Video", "1/2 Video", "3/4 Video", "Full video"];

            $scope.sizes = common.Size.ZoneSize;
            $scope.search = {}; 
            $scope.expandSearch = {};
            $scope.expandableZone = {};
            $scope.chooseSize = (value: string) => {
                $scope.search.value = value;
            }
            $scope.isUpload = false;
            $scope.unlimitStartDate = false;
            $scope.unlimitEndDate = false;
            var types: Array<string> = [];
            var currentOffset = 0, count = 3000, totalItem;
            var websitesCache: Array<models.Website> = [];
            $scope.siteNameDic = {};

            $scope.extlnkflash = false; $scope.extlnkimage = false; $scope.isExtlnk = false;

            $scope.tvc_extension = "NONE";
            $scope.xdata = false; $scope.xitems = [];
            $scope.strBound = { "0": "Is equal", "1": "Is not equal" };

            factory.campaignItemService.load($stateParams.itemId, function (item: any) {
                $scope.item = item;
                $scope.search.value = $scope.item.width + 'x' + $scope.item.height;
                $scope.rateUnit = rateUnits[$scope.item.rateUnit];
                if ($scope.item.bannerFile !== undefined && $scope.item.bannerFile.length > 0) {
                    $scope.uploadResult.bannerUploadResult = $scope.item.bannerFile;
                    $scope.uploadResult.bannerFallbackUploadResult = $scope.item.bannerFileFallback;
                    if ($scope.uploadResult.bannerUploadResult.lastIndexOf("#adtima.jpg") > 0) {
                        $scope.isExtlnk = true; $scope.extlnkimage = true;
                    } else if ($scope.uploadResult.bannerUploadResult.lastIndexOf("#adtima.swf") > 0) {
                        $scope.isExtlnk = true; $scope.extlnkflash = true;
                    }
                } else if ($scope.item.standardFile !== undefined && $scope.item.standardFile.length > 0) {
                    $scope.uploadResult.bannerUploadResult = $scope.item.standardFile;
                    $scope.uploadResult.bannerFallbackUploadResult = $scope.item.backupFile;
                    if ($scope.item.expandFile !== undefined && $scope.item.expandFile.length > 0) 
                        $scope.uploadResult.expandBannerUploadResult = $scope.item.expandFile;
                } 

                

                if ($scope.item.kind === models.CampaignItemType.BOOKING.BANNER || $scope.item.kind === models.CampaignItemType.NETWORK.BANNER)
                    $scope.item_kind = "Media Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.BOOKING.HTML || $scope.item.kind === models.CampaignItemType.NETWORK.HTML)
                    $scope.item_kind = "HTML Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.BOOKING.EXPANDABLE || $scope.item.kind === models.CampaignItemType.NETWORK.EXPANDABLE)
                    $scope.item_kind = "Expandable Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON || $scope.item.kind === models.CampaignItemType.NETWORK.BALLOON)
                    $scope.item_kind = "Balloon Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.BOOKING.POPUP)
                    $scope.item_kind = "Popup Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.BOOKING.TRACKING) {
                    $scope.item_kind = "Tracking Ads Information"; $scope.showTrackingLink = true;
                } else if ($scope.item.kind === models.CampaignItemType.NETWORK.TVC)
                    $scope.item_kind = "TVC Ads Information";
                else if ($scope.item.kind === models.CampaignItemType.NETWORK.TVCBANNER)
                    $scope.item_kind = "Overlay TVC Ads Information";

                if ($scope.item.extendData) {
                    for (var key in $scope.item.extendData) {
                        $scope.xitems.push(new models.kvItem(key, $scope.item.extendData[key]));
                    }

                    if ($scope.xitems.length > 0)
                        $scope.xdata = true;
                }


                if ($scope.item.tvcFile && $scope.item.tvcFile.length > 0) {
                    $scope.chooseTVCContent = "tvcFile";
                    $scope.bStatus.iTVCExtension = true;
                    $scope.uploadResult.tvcFileUploadResult = $scope.item.tvcFile;
                } else if ($scope.item.extendURL && $scope.item.extendURL.length > 0)
                    $scope.chooseTVCContent = "extendURL";

                if ($scope.item.startDate && $scope.item.startDate !== 0) {
                    $scope.datepicker['startDate'] = common.DateTimeUtils.getUTCStartOfDate($scope.item.startDate);
                } else {
                    $scope.unlimitStartDate = $scope.item.startDate === 0 || $scope.item.startDate === undefined;
                }
                if ($scope.item.endDate && $scope.item.endDate !== 0) {
                    $scope.datepicker['endDate'] = common.DateTimeUtils.getUTCStartOfDate($scope.item.endDate - 86400000);
                } else {
                    $scope.unlimitEndDate = $scope.item.endDate === 0 || $scope.item.endDate === undefined;
                }

                if ($scope.item.popupBanner === undefined) 
                    $scope.item.popupBanner = false;

                $scope.close_btn_action = "Close";
                if (models.CampaignItemType.isBannerWithExpandable($scope.item.kind)) {
                    $scope.uploadResult.bannerUploadResult = $scope.item.standardFile;
                    $scope.uploadResult.bannerFallbackUploadResult = $scope.item.backupFile;
                    $scope.expandBannerUploadResult = $scope.item.expandFile;
                    if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON)
                        $scope.uploadResult.barUploadResult = $scope.item.barFile;
                }
                // --- popup ---
                if (models.CampaignItemType.isPopupBanner($scope.item.kind)) {
                    if ($scope.item.actionCloseBtn === models.EPopupActionClose.CLOSE)
                        $scope.close_btn_action = "Close";
                    if ($scope.item.actionCloseBtn === models.EPopupActionClose.MOVE_TO_TARGET)
                        $scope.close_btn_action = "Move to target";
                }
                // --- balloon ---
                if (models.CampaignItemType.isBalloonBanner($scope.item.kind)) {
                    if ($scope.item.expandStyle === models.BalloonExpandStyle.AUTO) {
                        $scope.expand_style = "Auto";
                        $scope.isExpand = true;
                    } else if ($scope.item.expandStyle === models.BalloonExpandStyle.STANDARD) {
                        $scope.expand_style = "Standard";
                        $scope.isExpand = true;
                    } else if ($scope.item.expandStyle === models.BalloonExpandStyle.NONE) {
                        $scope.expand_style = "None";
                        $scope.isExpand = false;
                    }

                    if ($scope.item.iTVCExtension === models.ETVCExtension.INTEGRATED)
                        $scope.tvc_extension = "TVC";
                    else if ($scope.item.iTVCExtension === models.ETVCExtension.VAST)
                        $scope.tvc_extension = "VAST";
                }    

                // third party
                if ($scope.item.thirdParty !== null && $scope.item.thirdParty !== undefined) {
                    $scope.item.thirdParty.impression = decodeURIComponent($scope.item.thirdParty.impression);
                    $scope.item.thirdParty.click = decodeURIComponent($scope.item.thirdParty.click);
                    $scope.item.thirdParty.complete = decodeURIComponent($scope.item.thirdParty.complete);
                }

                // --------------- EXPANDABLE ZONE -------------------------
                if (models.CampaignItemType.isExpandableBanner($scope.item.kind)) {
                    
                    $scope.expandSearch.value = $scope.item.expandWidth + 'x' + $scope.item.expandHeight;

                    // expand direction
                    if ($scope.item.expandDirection === models.EExpandDirection.TOP_PUSH_DOWN)
                        $scope.expand_direction = 'Top Push Down';
                    if ($scope.item.expandDirection === models.EExpandDirection.RIGHT_TO_LEFT)
                        $scope.expand_direction = 'Right To Left';

                    // expand style
                    if ($scope.item.expandStyle === models.EExpandActiveStyle.MOUSE_OVER)
                        $scope.expand_style = 'Mouse Over';
                    if ($scope.item.expandStyle === models.EExpandActiveStyle.AUTO)
                        $scope.expand_style = 'Auto';

                    // display style
                    if ($scope.item.displayStyle === models.EExpandDisplayStyle.OVERLAY)
                        $scope.expand_display_style = 'Overlay';
                    if ($scope.item.displayStyle === models.EExpandDisplayStyle.PUSH_DOWN)
                        $scope.expand_display_style = 'Push Down';

                    if ($scope.item.iTVCExtension === models.ETVCExtension.INTEGRATED) {
                        $scope.bStatus.iTVCExtension = true;
                        $scope.uploadResult.tvcFileUploadResult = $scope.item.tvcFile;
                    }
                }

                // --------------- EXPANDABLE ZONE -------------------------
                if (models.CampaignItemType.isTrackingBanner($scope.item.kind)) {
                    factory.campaignItemService.getTrackingLinks($stateParams.itemId, function (trackingItem: models.TrackingLinkItem) {
                        if (trackingItem) {
                            if (trackingItem.impression)
                                $scope.item.impression_tracking = trackingItem.impression;
                            if (trackingItem.impressionPixel)
                                $scope.item.impression_pixel_tracking = trackingItem.impressionPixel;
                            if (trackingItem.click)
                                $scope.item.click_tracking = trackingItem.click;
                            if (trackingItem.clickPixel)
                                $scope.item.click_pixel_tracking = trackingItem.clickPixel;
                        }
                    });
                }
                
                //--------------------Network item----------------
                if (models.CampaignItemType.isNetworkType($scope.item.kind) || $scope.item.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                    if (models.CampaignItemType.isTVC($scope.item.kind))
                        types = ['tvc'];
                    else
                        types = ['network'];
                    $scope.targeting = { website: true };

                    //-------------Variables targeting--------------------
                    if (!$scope.item.variables)
                        $scope.item.variables = [new models.NetworkVariable("", null, 0)];

                    var isOsTargeting = false;
                    $scope.osTargeting = {};
                    ($scope.item.variables || []).forEach((vari, i) => {
                        if (vari.key === 'os') {
                            isOsTargeting = true;
                            $scope.osTargeting[vari.value] = true;
                        }
                    });
                    $scope.targeting['os'] = isOsTargeting;

                    $scope.retargeting = [];
                    if ($scope.item.variables) {
                        $scope.item.variables.filter((a) => a.key === 'retargeting')
                            .forEach((b) => $scope.retargeting.push(b));
                    }

                    $scope.variables = $scope.item.variables.filter((a, _) => a.key !== 'os' && a.key !== 'retargeting');

                    if ($scope.variables.length !== 0)
                        $scope.targeting["site_variable"] = true;

                    $scope.companionTargetingValues = [];
                    ($scope.item.companionTargetingValues||[]).forEach((a) => $scope.companionTargetingValues.push({ value: a }));

                    // frequency capping
                    $scope.freq_impression = $scope.item.freqCapping;
                    if ($scope.item.freqCappingTime >= 86400) {
                        $scope.freq_time_string = "Days";
                        $scope.freq_time = $scope.item.freqCappingTime / 86400;
                    } else if ($scope.item.freqCappingTime >= 3600) {
                        $scope.freq_time_string = "Hours";
                        $scope.freq_time = $scope.item.freqCappingTime / 3600;
                    } else {
                        $scope.freq_time_string = "Minutes";
                        $scope.freq_time = $scope.item.freqCappingTime / 60;
                    }


                    if (!!$scope.item.geographicTargetings && $scope.item.geographicTargetings.length > 0) {
                        $scope.targeting["location"] = true;
                        $scope.item.geographicTargetings.forEach((loc, _) => {
                            if (loc.value.search(/VN[0-9]*/) !== -1 && loc.in === true) {
                                $scope.checkboxLocation[loc.value] = true;
                                $scope.belongLocation['VN'] = 1;
                            } else {
                                $scope.checkboxLocation = {};
                                $scope.belongLocation['VN'] = 0;
                            }
                        });
                    } else $scope.item.geographicTargetings = [];

                    $scope.hours = 0;
                    $scope.minutes = 0;
                    $scope.seconds = 0;
                    if ($scope.item.timeSpan) {
                        $scope.item.timeSpan /= 1000;
                        $scope.hours = Math.floor($scope.item.timeSpan / 3600);
                        $scope.minutes = Math.floor(($scope.item.timeSpan % 3600) / 60);
                        $scope.seconds = $scope.item.timeSpan % 60;
                    }
                    listWebsite();
                    $scope.checkboxZoneWeb = {};
                    ($scope.item.targetContent || []).forEach((id, i) => {
                        $scope.selectedWebsites.push(id);
                        $scope.checkboxWebsite[id] = true;
                    });
                    listZoneWebsite();
                    var zoneIds: Array<number> = [];
                    if ($scope.item.kind.toLowerCase() != "networktvc" && $scope.item.targetZones)
                        zoneIds = $scope.item.targetZones;
                    else if ($scope.item.kind.toLowerCase() === "networktvc" && $scope.item.positions)
                        $scope.item.positions.forEach((obj, i) => zoneIds.push(obj['zoneId']));

                    if (zoneIds.length > 0)
                        factory.zoneService.listByIds(zoneIds, function(zonesRet) {
                            if (zonesRet) {
                                var c = {};
                                $scope.item.targetContent.forEach((v, i) => c[v] = 0);
                                zonesRet.forEach((z, i) => {
                                    if (!$scope.checkboxZone[z.siteId])
                                        $scope.checkboxZone[z.siteId] = {};
                                    if (!$scope.checkboxZone[z.siteId][z.id])
                                        $scope.checkboxZone[z.siteId][z.id] = {};
                                    c[z.siteId]++;

                                    if ($scope.item.kind.toLowerCase() != "networktvc") {
                                        $scope.selectedZones.push({ website: z.siteId, zone: z.id, pos: "null" });
                                        $scope.checkboxZone[z.siteId][z.id]["null"] = true;
                                    } else if (z["tvcPositions"]) {
                                        var pos: string = $scope.item.positions[i]["position"];
                                        pos = pos.charAt(0).toUpperCase() + pos.slice(1);
                                        $scope.selectedZones.push({ website: z.siteId, zone: z.id, pos: pos });
                                        $scope.checkboxZone[z.siteId][z.id][pos] = true;
                                    }
                                });
                                $timeout(() => {
                                    $scope.item.targetContent.forEach((w, i) => {
                                        if ($scope.item.kind.toLowerCase() !== "networktvc") {
                                            if ($scope.zones[w] && c[w] == Object.keys($scope.zones[w]).length)
                                                $scope.checkboxZoneWeb[w] = true;
                                        } else {
                                            var cntZ = 0;
                                            if ($scope.zones[w])
                                                Object.keys($scope.zones[w]).forEach((z, __) => { cntZ += $scope.zones[w][z].tvcPositions.length });
                                            console.log(c[w], cntZ);
                                            if (c[w] === cntZ)
                                                $scope.checkboxZoneWeb[w] = true;
                                        }
                                    });
                                }, 20);
                            }
                        });


                    //---------------- PR Banner ----------------
                    if ($scope.item.kind === models.CampaignItemType.NETWORK.PRBANNER ||
                        $scope.item.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                        $scope.checkBoxCate = {};
                        $scope.option = { targetZone: "all" }
                        if ($scope.item.targetZones)
                            $scope.option.targetZone = "specific";

                        factory.articleService.listCategories((ret) => {
                            if (ret) {
                                $scope.cateDic = {};
                                ret.forEach((c: any) => $scope.cateDic[c.id] = c.name);
                                $scope.articleCates = ret;
                                $scope.item.categoryTypes.forEach((cId) => {
                                    ($scope.selectedCates = $scope.selectedCates || []).push({
                                        id: cId,
                                        name: $scope.cateDic[cId]
                                    });
                                    $scope.checkBoxCate[cId] = true;
                                })
                            }
                        });

                        // ----------- End Pr Banner ---------------
                    }
                }
                if (item.positions && item.positions.length > 0) {
                    $timeout(() => {
                        $("form #width").each((idx, el) => el.setAttribute("disabled", "true"));
                        $("form #height").each((idx, el) => el.setAttribute("disabled", "true"));
                    }, 20);
                }

                if ($scope.item.kind.toLowerCase() === "networktvc" && ($scope.item.positions === null || $scope.item.positions === undefined) ||
                    $scope.item.kind.toLowerCase() !== "networktvc" && ($scope.item.targetZones === null || $scope.item.targetZones === undefined))
                    $scope.targetZonesOption = "all";
                else
                    $scope.targetZonesOption = "specific";

                $scope.item.tvcType = $scope.item.skip === true ? "skip" : "standard";

                if (models.CampaignItemType.isBannerNotExpandable($scope.item.kind) || models.CampaignItemType.isPopupBanner($scope.item.kind)) {
                    $scope.uploadResult.bannerUploadResult = $scope.item.bannerFile;
                    $scope.uploadResult.bannerFallbackUploadResult = $scope.item.bannerFileFallback;
                }
                if (models.CampaignItemType.isExpandableBanner($scope.item.kind) || $scope.item.kind == models.CampaignItemType.BOOKING.BALLOON) {
                    $scope.uploadResult.bannerUploadResult = $scope.item.standardFile;
                    $scope.uploadResult.bannerFallbackUploadResult = $scope.item.backupFile;
                    $scope.uploadResult.expandBannerUploadResult = $scope.item.expandFile;
                }
            });

            $scope.campaign = null;
            factory.campaignService.load($stateParams.campaignId, function (campRet) {
                if (campRet && campRet.id) $scope.campaign = campRet;
            });

            function listWebsite() {
                factory.websiteService.listByTypeMinimize(currentOffset, count, types, function (ret: backend.ReturnList<models.Website>) {
                    if (ret && ret.data.length > 0) {
                        $scope.websites = $scope.websites.concat(ret.data);
                        websitesCache = $scope.websites;
                        ret.data.forEach((w, i) => {
                            $scope.siteNameDic[w.id] = w.name;
                        });
                        totalItem = ret.total;
                        var size = Math.min(count, ret.data.length);
                        currentOffset += size;
                        if (currentOffset < totalItem)
                            listWebsite();
                    }
                    
                }, "name", "asc");
            }
            // ------------------ functions -------------------------------
            $scope.$watch('xdata', function (newVal, oldVal) {
                if (newVal && $scope.xitems.length === 0) {
                    $scope.xitems.push(new models.kvItem("", ""));
                }
            });

            $scope.$watch("searchSite", function (newVal, oldVal) {
                if (newVal !== undefined) {
                    if (newVal.length > 0) {
                        $scope.websites = websitesCache.filter(function (w) {
                            if (w.name.toLowerCase().indexOf(newVal.toLowerCase()) !== -1)
                                return true;
                            return false;
                        });
                    }
                    else
                        $scope.websites = websitesCache;
                }
            });
            
            $scope.$watch('uploadResult.barUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    $scope.item.barFile = newVal;
                }
            });

            $scope.$watch('uploadResult.bannerUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    if (models.CampaignItemType.isBannerWithExpandable($scope.item.kind)) {
                        $scope.item.standardFile = newVal;
                        return;
                    }
                    $scope.item.bannerFile = newVal;
                }
            });

            $scope.$watch('uploadResult.bannerFallbackUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    if (models.CampaignItemType.isBannerWithExpandable($scope.item.kind)) {
                        $scope.item.backupFile = newVal;
                        return;
                    }
                    $scope.item.bannerFileFallback = newVal;
                }
            });

            $scope.$watch('uploadResult.expandBannerUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    $scope.item.expandFile = newVal;
                }
            });

            $scope.$watch('uploadResult.tvcFileUploadResult', function (newVal, oldVal) {
                if (newVal && newVal.length > 0 && newVal !== 'FAIL' && newVal !== 'BEGIN') {
                    $scope.item.tvcFile = newVal;
                }
            });

            $scope.$watch('uploadResult.duration', function (newVal, oldVal) {
                if (newVal && $scope.item.kind === models.CampaignItemType.NETWORK.TVC) {
                    $scope.item.duration = newVal;
                }
            });

            $scope.$watch('item.wrapper', function (newVal, oldVal) {
                if (newVal !== undefined && oldVal !== undefined) {
                    if (newVal) {
                        $scope.chooseTVCContent = "extendURL";
                        $scope.bStatus.iTVCExtension = false;
                    } else {
                        $scope.chooseTVCContent = "tvcFile";
                        $scope.bStatus.iTVCExtension = true;
                    }
                }
            });

            $scope.$watch('search.value', function (newVal, oldVal) {
                if (newVal === undefined)
                    return;
                if ($scope.search.value !== undefined && $scope.search.value.match("\\d+[\\x]\\d+")) {
                    var size = $scope.search.value.split('x');
                    $scope.item.width = size[0];
                    $scope.item.height = size[1];
                } else {
                    $scope.item.width = 0;
                    $scope.item.height = 0;
                }
                checkValidSize();
            });
            $scope.$watch("item.width", function (newVal, oldVal) { checkValidSize() });
            $scope.$watch("item.height", function (newVal, oldVal) { checkValidSize() });

            $scope.$watch('uploadResult.mimetype', function (newVal, oldVal) {
                if (newVal && newVal.length > 0) {
                    if (newVal === 'video/x-flv' || newVal === 'video/mp4' || newVal === 'video/x-ms-wmv')
                        $scope.item.iTVCExtension = models.BalloonTVCExtension.INTEGRATED;
                    else
                        $scope.item.iTVCExtension = models.BalloonTVCExtension.NO_TVC;
                }
            });

            $scope.chooseFreqTimeUnit = (unit: string) => {
                if (unit === models.EFrequencyCappingUnit.MINUTE) {
                    $scope.freq_time_string = "Minutes";
                } else if (unit === models.EFrequencyCappingUnit.HOUR) {
                    $scope.freq_time_string = "Hours";
                } else if (unit === models.EFrequencyCappingUnit.DAY) {
                    $scope.freq_time_string = "Days";
                }
            };

            $scope.chooseExpandStyle = (type: string) => {
                if ($scope.item.kind !== models.CampaignItemType.BOOKING.BALLOON && $scope.item.kind !== models.CampaignItemType.NETWORK.BALLOON)
                    return;
                if (type === 'none') {
                    $scope.isExpand = false;
                    $scope.item.expandStyle = models.BalloonExpandStyle.NONE;
                    $scope.expand_style = "None";
                } else if (type === 'standard') {
                    $scope.isExpand = true;
                    $scope.item.expandStyle = models.BalloonExpandStyle.STANDARD;
                    $scope.expand_style = "Standard";
                } else if (type === 'auto') {
                    $scope.isExpand = true;
                    $scope.item.expandStyle = models.BalloonExpandStyle.AUTO;
                    $scope.expand_style = "Auto";
                }
            };

            $scope.chooseCloseBtnAction = (action: string) => {
                if (action === models.EPopupActionClose.CLOSE) {
                    $scope.item.actionCloseBtn = models.EPopupActionClose.CLOSE;
                    $scope.close_btn_action = "Close";
                    return;
                }

                if (action === models.EPopupActionClose.MOVE_TO_TARGET) {
                    $scope.item.actionCloseBtn = models.EPopupActionClose.MOVE_TO_TARGET;
                    $scope.close_btn_action = "Move to target";
                    return;
                }
            };

            $scope.addXData = () => {
                $scope.xitems.push(new models.kvItem("", ""));
            };

            $scope.changeKey = (index: number) => {
                if ($scope.variables[index].key.search(/[^a-z0-9A-Z]+/g) !== -1 || $scope.variables[index].key.search(/^[0-9]+/g) !== -1) {
                    $scope.variables[index]["key"] = $scope.variables[index]["key"].replace(/[^a-z0-9A-Z]+/g, "");
                    $scope.variables[index]["key"] = $scope.variables[index]["key"].replace(/^[0-9]+/g, "");
                }
            };

            $scope.chooseTVCExtension = (kind: string) => {
                $scope.tvc_extension = kind.toUpperCase();
            };

            $scope.changeValue = (index: number) => {
                if ($scope.variables[index].value.search(/[|]+/g) !== -1)
                    $scope.variables[index]["value"] = $scope.variables[index]["value"].replace(/[|]+/g, "");
            };
            $scope.add_variable = () => {
                $scope.variables.push(new models.NetworkVariable("", null, 0));
            }
            $scope.remove_variable = (index) => {
                if (!!$scope.variables[index])
                    $scope.variables.splice(index, 1);
            }

            $scope.isShowBannerUrl = (): boolean => {
                if ($scope.item !== undefined && $scope.item !== null) {
                    if ($scope.item.bannerFile && $scope.isUpload === false)
                        return true;
                }
                return false;
            };

            $scope.isNetworkOverlay = (kind: string): boolean => {
                if (kind && kind.toLowerCase() === 'networkoverlaybanner')
                    return true;
                return false;
            };

           $scope.isNetworkMedia = (kind: string): boolean => {
                if (kind && kind.toLowerCase() === 'networkmedia')
                    return true;
                return false;
            };

            $scope.uploadFile = () => {
                $scope.isUpload = !$scope.isUpload;
            };

            $scope.choose = (type: string, value: string) => {
                switch (type) {
                    case "rate":
                        var limitUnits = ["", "Impression", "Click"];
                        $scope.item.rateUnit = parseInt(value);
                        $scope.rateUnit = rateUnits[$scope.item.rateUnit];
                        $scope.item.limitUnit = limitUnits[$scope.item.rateUnit];
                        break;
                    case "limit":
                        $scope.item.limitUnit = value;
                        break;
                    case "platform":
                        $scope.item.targetPlatform = value;
                        break;
                    case "radio":
                        switch (value) {
                            case "specific":
                                $scope.selectedZones = [];
                                $scope.checkboxZone = {};
                                listZoneWebsite();
                                break;
                            case 'all':
                                listAllZone();
                                $scope.isWarningZoneSize = false;
                                break;
                            case 'notVN':
                                $scope.item.geographicTargetings = [{value: "VN", in: false}];
                                $scope.checkboxLocation = {};
                                break;
                            case 'VN':
                                $scope.item.geographicTargetings = [];
                                $scope.locations.forEach((a, i) => {
                                    $scope.item.geographicTargetings.push({ value: a.id, in: true });
                                    $scope.checkboxLocation[a.id] = true;
                                });
                                break;
                        }
                        break;
                    case "tvc-content":
                        break;
                    case "targeting":
                        $scope.targeting[value] = true;
                        switch (value) {
                            case "website": $scope.sTargeting = "Website zone"; break;
                            case "age": $scope.sTargeting = "Demographic Age"; break;
                            case "gender": $scope.sTargeting = "Demographic gender"; break;
                            case "channel":
                                $scope.sTargeting = "Categories Channel";
                                //categoryService.listAll(function (categories: models.Category[]) {
                                //    $scope.categories = categories;
                                //});
                                break;
                            case "location":
                                $scope.sTargeting = "Location";
                                $scope.item.geographicTargetings = [];
                                $scope.belongLocation = { VN: 1 };
                                $scope.locations.forEach((a, i) => $scope.checkboxLocation[a.id] = true);
                                break;
                            case "interest": $scope.sTargeting = "User Interest"; break;
                            case "os": $scope.sTargeting = "Technology OS"; break;
                            case "resolution": $scope.sTargeting = "Technology Resolution"; break;
                            case "device": $scope.sTargeting = "Technology Device"; break;
                            case "site_variable": $scope.sTargeting = "Site Variable"; break;
                        }
                        break;
                    case 'changeLocation':
                        if ($scope.belongLocation['VN'] = 1) {
                            $scope.item.geographicTargetings = [];
                            $scope.locations.forEach((a, i) => {
                                if ($scope.checkboxLocation[a.id] === true)
                                    $scope.item.geographicTargetings.push({ value: a.id, in: true });
                            });
                            if ($scope.item.geographicTargetings.length === 0)
                                $scope.item.geographicTargetings = [{value: "VN", in: true}];
                        }
                        break;
                }
            };

            $scope.checkWebsite = (id: number) => {
                if ($scope.checkboxWebsite[id] == true) {
                    $scope.selectedWebsites.push(id);
                }
                else {
                    var index = $scope.selectedWebsites.indexOf(id);
                    if (index >= 0) {
                        $scope.selectedWebsites.splice(index, 1);
                        $scope.checkboxZoneWeb[id] = false;
                        for (var i = $scope.selectedZones.length - 1; i >= 0; i--)
                            if ($scope.selectedZones[i]['website'] == id) {
                                $scope.selectedZones.splice(i, 1);
                            }
                    }
                }
                listZoneWebsite();
                if ($scope.targetDisplay == "suitable")
                    listAllZone();
                checkValidSize();
            };

            $scope.checkZone = (websiteId: number, zoneId: number, pos: string) => {
                if ($scope.checkboxZone[websiteId][zoneId][pos] == true) {
                    var isContain = false;
                    $scope.selectedZones.forEach((o, i) => {
                        if (o['website'] == websiteId && o['zone'] == zoneId && o['pos'] == pos) {
                            isContain = true; return;
                        }
                    });
                    if (!isContain)
                        $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: pos });
                }
                else {
                    $scope.selectedZones.forEach((o, i) => {
                        if (o['website'] == websiteId && o['zone'] == zoneId && o['pos'] == pos) {
                            $scope.selectedZones.splice(i, 1); return;
                        }
                    });
                }

                var ok = true;
                for (var zId in $scope.zones[websiteId]) {
                    if ($scope.item.kind.toLowerCase() !== 'networktvc' && $scope.checkboxZone[websiteId][zoneId][pos] !== $scope.checkboxZone[websiteId][zId][pos]) {
                        if (!document.getElementById("zone1" + zId).disabled)
                            ok = false;
                    }
                    else if ($scope.item.kind.toLowerCase() == 'networktvc') {
                        var zone = $scope.zones[websiteId][zId];
                        zone.tvcPositions.forEach((p ,__) => {
                            if ($scope.checkboxZone[websiteId][zoneId][pos] !== $scope.checkboxZone[websiteId][zId][p])
                                ok = false;
                        });
                    }
                }
                if (ok || !ok && !$scope.checkboxZone[websiteId][zoneId][pos])
                    $scope.checkboxZoneWeb[websiteId] = $scope.checkboxZone[websiteId][zoneId][pos];
            };

            $scope.checkZoneWeb = (websiteId: number) => {
                for (var zoneId in $scope.zones[websiteId]) {
                    if ($scope.item.kind.toLowerCase() == 'networktvc') {//Filt by tvc
                        if (document.getElementById("zone1" + zoneId + "_Pre") && !document.getElementById("zone1" + zoneId + "_Pre").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Pre'] = $scope.checkboxZoneWeb[websiteId];
                        if (document.getElementById("zone1" + zoneId + "_Post") && !document.getElementById("zone1" + zoneId + "_Post").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Post'] = $scope.checkboxZoneWeb[websiteId];
                        if (document.getElementById("zone1" + zoneId + "_Mid") && !document.getElementById("zone1" + zoneId + "_Mid").disabled)
                            $scope.checkboxZone[websiteId][zoneId]['Mid'] = $scope.checkboxZoneWeb[websiteId];
                    } else {
                        if (!document.getElementById("zone1" + zoneId).disabled)
                            $scope.checkboxZone[websiteId][zoneId]['null'] = $scope.checkboxZoneWeb[websiteId];
                    }
                    if ($scope.checkboxZoneWeb[websiteId] == true) {
                        var isContain = false;
                        $scope.selectedZones.forEach((o, i) => {
                            if (o['website'] == websiteId && o['zone'] == zoneId) {
                                isContain = true; return;
                            }
                        });

                        if ($scope.item.kind.toLowerCase() == 'networktvc') {//Filt by tvc
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Pre") && !document.getElementById("zone1" + zoneId + "_Pre").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Pre" });
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Mid") && !document.getElementById("zone1" + zoneId + "_Mid").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Mid" });
                            if (!isContain && document.getElementById("zone1" + zoneId + "_Post") && !document.getElementById("zone1" + zoneId + "_Post").disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "Post" });
                        } else {
                            if (!isContain && !document.getElementById("zone1" + zoneId).disabled)
                                $scope.selectedZones.push({ website: websiteId, zone: zoneId, pos: "null" });
                        }
                    }
                    else {
                        $scope.selectedZones.forEach((o, i) => {
                            if (o['website'] == websiteId && o['zone'] == zoneId) {
                                $scope.selectedZones.splice(i, 1);
                            }
                        });
                    }
                }
            };
            $scope.uncheck = (type: string, index: number) => {
                switch (type) {
                    case "website":
                        var id = $scope.selectedWebsites[index];
                        $scope.selectedWebsites.splice(index, 1);
                        $scope.checkboxWebsite[id] = false;
                        $scope.checkboxZoneWeb[id] = false;
                        for (var i = $scope.selectedZones.length - 1; i >= 0; i--)
                            if ($scope.selectedZones[i]['website'] == id) {
                                $scope.selectedZones.splice(i, 1);
                            }
                        listZoneWebsite();
                        if ($scope.targetDisplay == "suitable")
                            listAllZone()
                        break;
                    case "zone":
                        var obj = $scope.selectedZones[index];
                        $scope.selectedZones.splice(index, 1);
                        $scope.checkboxZone[obj['website']][obj['zone']][obj['pos']] = false;
                        checkSelectZoneWeb(obj['website']);
                        break;
                }
            };

            $scope.remove_target = (target: string) => {
                $scope.targeting[target] = false;
                switch (target) {
                    case "website":
                        $scope.item.targetContent = [];
                        $scope.item.targetZones = [];
                        $scope.item.positions = [];
                        return;
                    case "site_variable":
                        $scope.item.variables = [];
                        return;
                    case "location":
                        $scope.item.geographicTargetings = null;
                        $scope.checkboxLocation = {};
                        $scope.belongLocation = {};
                        return;
                }
            };

            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD/MM/YYYY');
            };
            
            $scope.save = () => {

                $scope.checkValidate = true;
                if ($scope.item.name === undefined || $scope.item.name.length == 0)
                    return;
                if ((!$scope.item.width || !$scope.item.height) && $scope.item.kind.toLowerCase() !== "networktvc" &&
                    $scope.item.kind !== models.CampaignItemType.NETWORK.PRBANNER &&
                    $scope.item.kind !== models.CampaignItemType.BOOKING.PRBANNER)
                    return;

                if ($scope.item.kind.toLowerCase().indexOf("media") != -1 && (!$scope.item.bannerFile || $scope.item.bannerFile && $scope.item.bannerFile.length == 0))
                    return;
                //if ($scope.item.kind.toLowerCase() == "networktvc" && !$scope.item.wrapper && (!$scope.item.tvcFile || $scope.item.tvcFile && $scope.item.tvcFile.length == 0 || $scope.item.targetUrl.length == 0))
                //    return;
                if ($scope.item.kind.toLowerCase() == "networktvc" && $scope.item.wrapper && (!$scope.item.extendURL || $scope.item.extendURL && $scope.item.extendURL.length == 0))
                    return;

                if (models.CampaignItemType.isNetworkType($scope.item.kind) && ($scope.item.rate == undefined
                    || $scope.item.limit == undefined || $scope.item.lifetimeLimit == undefined))
                    return;

                if (!$scope.item.popupBanner) {
                    $scope.item.actionCloseBtn = "";
                }

                if ($scope.item.kind === models.CampaignItemType.BOOKING.POPUP || $scope.item.kind === models.CampaignItemType.NETWORK.POPUP) {
                    $scope.item.actionCloseBtn = models.EPopupActionClose.CLOSE;
                    if ($scope.close_btn_action === "Move to target")
                        $scope.item.actionCloseBtn = models.EPopupActionClose.MOVE_TO_TARGET;
                }

                if ($scope.chooseTVCContent === "tvcFile")
                    $scope.item.extendURL = "";
                else if ($scope.chooseTVCContent === "extendURL")
                    $scope.item.tvcFile = "";
                if ($scope.uploadResult.bannerUploadResult && $scope.uploadResult.bannerUploadResult.length > 0) {
                    if ($scope.isExtlnk) {
                        if ($scope.uploadResult.bannerUploadResult.lastIndexOf("#adtima.jpg") < 0 && $scope.uploadResult.bannerUploadResult.lastIndexOf("#adtima.swf") < 0) {
                            if ($scope.extlnkflash) {
                                $scope.uploadResult.bannerUploadResult += "#adtima.swf";
                            } else if ($scope.extlnkimage) {
                                $scope.uploadResult.bannerUploadResult += "#adtima.jpg";
                            }
                        }
                    }
                    $scope.item.bannerFile = $scope.uploadResult.bannerUploadResult;
                }
                if ($scope.uploadResult.bannerFallbackUploadResult && $scope.uploadResult.bannerFallbackUploadResult.length > 0)
                    $scope.item.bannerFileFallback = $scope.uploadResult.bannerFallbackUploadResult;


                if ($scope.item.kind.indexOf("network") < 0 && $scope.item.targetUrl !== undefined && !$scope.item.targetUrl.startWith("http")) {
                    $scope.item.targetUrl = "http://" + $scope.item.targetUrl;
                }

                $scope.item.iTVCExtension = models.ETVCExtension.NONE;
                if ($scope.bStatus.iTVCExtension) {
                    if ($scope.item.tvcFile === undefined || $scope.item.tvcFile.length === 0) {
                        return;
                    }
                    $scope.item.iTVCExtension = models.ETVCExtension.INTEGRATED;
                }

                if ($scope.item.kind === models.CampaignItemType.BOOKING.BALLOON) {
                    $scope.item.iTVCExtension = models.ETVCExtension.NONE;

                    if ($scope.tvc_extension.toLowerCase() !== 'none') {

                        if ($scope.item.tvcFile === undefined || $scope.item.tvcFile.length === 0) 
                            return;

                        if ($scope.tvc_extension.toLowerCase() === 'tvc')
                            $scope.item.iTVCExtension = models.ETVCExtension.INTEGRATED;
                        else  // vast
                            $scope.item.iTVCExtension = models.ETVCExtension.VAST;
                    }
                }

                if ($scope.item.kind.toLowerCase().indexOf("network") >= 0 || $scope.item.kind === models.CampaignItemType.BOOKING.PRBANNER) {

                    // frequency capping
                    $scope.item.freqCapping = $scope.freq_impression;
                    if ($scope.freq_time_string.toLowerCase() === "minutes")
                        $scope.item.freqCappingTime = $scope.freq_time * 60;
                    else if ($scope.freq_time_string.toLowerCase() === "hours")
                        $scope.item.freqCappingTime = $scope.freq_time * 3600;
                    else if ($scope.freq_time_string.toLowerCase() === "days")
                        $scope.item.freqCappingTime = $scope.freq_time * 86400;
                    $scope.item.variables = [];
                    Object.keys($scope.osTargeting).forEach((a, _) => {
                        if ($scope.osTargeting[a] === true) {
                            $scope.item.variables.push({
                                "key": "os",
                                "value": a,
                                "bound": models.NetworkVariable.ISEQUAL
                            });
                        }
                    });
                    var variables = [], i = 0;
                    $scope.variables.forEach((v) => {
                        if (v.key.length !== 0 && v.value !== null) {
                            variables.push({});
                            Object.keys(v).forEach((key, __) => {
                                if (key.indexOf("$$hashKey") == -1) variables[i][key] = v[key];
                            });
                            i++;
                        }
                    });

                    $scope.item.variables = $scope.item.variables.concat(variables);
                    $scope.item.targetContent = [];
                    $scope.selectedWebsites.forEach((v, i) => $scope.item.targetContent.push(v));
                    $scope.item.timeSpan = ($scope.hours * 3600 + $scope.minutes * 60 + $scope.seconds * 1) * 1000;

                    if ($scope.targetZonesOption === "specific" && $scope.selectedZones.length === 0) {
                        $scope.isWarningSelectZone = true;
                        $timeout(() => $scope.isWarningSelectZone = false, 3000);
                        return;
                    }
                    
                    if (!$scope.unlimitStartDate) {
                        if ($scope.datepicker["startDate"] !== null) {
                            if (common.DateTimeUtils.getStartTimeOfDate($scope.datepicker["startDate"]) < $scope.campaign.startDate) {
                                notify(new common.ActionMessage(common.ActionMessage.WARN, "Limit range time beyond the campaign time range ("
                                    + $scope.formatDateTime($scope.campaign.startDate) + " - " + $scope.formatDateTime($scope.campaign.endDate) + ")"), 5000)
                                return;
                            }
                            $scope.item.startDate = common.DateTimeUtils.getStartTimeOfDate($scope.datepicker["startDate"]);
                        }
                    } else {
                        $scope.item.startDate = 0;
                    }
                    if (!$scope.unlimitEndDate) {
                        if ($scope.datepicker["endDate"] !== null) {
                            if (common.DateTimeUtils.getStartTimeOfDate($scope.datepicker["endDate"]) > $scope.campaign.endDate) {
                                notify(new common.ActionMessage(common.ActionMessage.WARN, "Limit range time beyond the campaign time range ("
                                    + $scope.formatDateTime($scope.campaign.startDate) + " - " + $scope.formatDateTime($scope.campaign.endDate) + ")"), 5000)
                                return;
                            }
                            $scope.item.endDate = common.DateTimeUtils.getEndTimeOfDate($scope.datepicker["endDate"]);
                        }
                    } else {
                        $scope.item.endDate = 0;
                    }

                    if ($scope.targetZonesOption != "all") {
                        var targetZones = [];
                        if ($scope.item.kind.toLowerCase() == 'networktvc') {
                            $scope.selectedZones.forEach((v, i) => targetZones.push({ zoneId: v['zone'], position: v['pos'].toLowerCase() }));
                            $scope.item.positions = targetZones;
                        } else {
                            $scope.selectedZones.forEach((v, i) => targetZones.push(v['zone']));
                            $scope.item.targetZones = targetZones;
                        }
                    }
                    else {
                        if ($scope.item.kind.toLowerCase() == 'networktvc') 
                            $scope.item.positions = null;
                        else
                            $scope.item.targetZones = null;
                    }

                    if ($scope.item.tvcType == "skip") {
                        $scope.item.skip = true;
                        $scope.item.skipAfter = $scope.item.skipAfter || 0;
                    }
                    else {
                        $scope.item.skip = false;
                        $scope.item.skipAfter = 0;
                    }

                    //--------- pr banner -----------
                    if ($scope.item.kind === models.CampaignItemType.NETWORK.PRBANNER ||
                        $scope.item.kind === models.CampaignItemType.BOOKING.PRBANNER) {
                        $scope.item.categoryTypes = [];
                        $scope.selectedCates.forEach((a) => {
                            $scope.item.categoryTypes.push(a.id);
                        })
                        if ($scope.option.targetZone === "specific") {
                            $scope.item.targetZones = [];
                            $scope.selectedZones.forEach((v) => $scope.item.targetZones.push(v['zone']));
                        } else {
                            $scope.item.targetZones = null;
                        }
                    }
                    //--------- end pr banner -----------

                    $scope.item.variables = $scope.item.variables || [];
                    $scope.retargeting.forEach((a) => $scope.item.variables.push(new models.NetworkVariable("retargeting", a.value, models.NetworkVariable.ISEQUAL)));

                    $scope.item.companionTargetingValues = [];
                    $scope.companionTargetingValues.forEach((a) => $scope.item.companionTargetingValues.push(a.value));
                }

                if ($scope.xdata) {
                    var xObj: any = common.ConvertUtils.convertToObject($scope.xitems);
                    if (xObj) {
                        $scope.item.extendData = JSON.stringify(xObj);
                    }
                } else {
                    $scope.item.extendData = null;
                }
                if ($scope.item.thirdParty !== null && $scope.item.thirdParty !== undefined) {
                    $scope.item.thirdParty.impression = encodeURIComponent($scope.item.thirdParty.impression);
                    $scope.item.thirdParty.click = encodeURIComponent($scope.item.thirdParty.click);
                    $scope.item.thirdParty.complete = encodeURIComponent($scope.item.thirdParty.complete);
                }

                factory.campaignItemService.update($scope.item, function (result: string) {
                    if (result === "success") {
                        if ($scope.item.kind.indexOf("network") >= 0) {
                            $location.hash("topPage");
                            $anchorScroll();
                        }
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "banner"));
                        $timeout(function () {
                            $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
                        }, 1000);
                        return;
                    }
                    notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "banner"));
                }, function (msg, status) {
                });
            }
            $scope.cancel = () => {
                $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
            };

            $scope.getFullUrl = (relativeUrl: string): string => {
                if (relativeUrl !== undefined && relativeUrl !== null)
                    return common.Config.ImageRootUrl + relativeUrl;
                return "";
            };

            $scope.isBannerImage = (filename: string): boolean => {
                if ($scope.isExtlnk)
                    return false;
                if (filename === undefined || filename.length === 0)
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
                if ($scope.isExtlnk)
                    return false;
                if (filename === undefined || filename.length === 0)
                    return false;
                var index: number = filename.lastIndexOf(".");
                if (index < filename.length - 1) {
                    var ext: string = filename.substr(index + 1, filename.length).toLowerCase();
                    if (ext === "swf")
                        return true;
                }
                return false;
            };

            $scope.chooseDirection = (type: string) => {
                if (type === 'top_push_down') {
                    $scope.item.expandDirection = models.EExpandDirection.TOP_PUSH_DOWN;
                    $scope.expand_direction = 'Top Push Down';
                    return;
                }
                if (type === 'right_to_left') {
                    $scope.item.expandDirection = models.EExpandDirection.RIGHT_TO_LEFT;
                    $scope.expand_direction = 'Right To Left';
                    return;
                }
            };

            $scope.chooseDisplayStyle = (type: string) => {
                if (type === 'overlay') {
                    $scope.item.displayStyle = models.EExpandDisplayStyle.OVERLAY;
                    $scope.expand_display_style = 'Overlay';
                    return;
                }
                if (type === 'push_down') {
                    $scope.item.displayStyle = models.EExpandDisplayStyle.PUSH_DOWN;
                    $scope.expand_display_style = 'Push Down';
                    return;
                }
            };

            $scope.chooseStyle = (type: string) => {
                if (type === 'mouse_over') {
                    $scope.item.expandStyle = models.EExpandActiveStyle.MOUSE_OVER;
                    $scope.expand_style = 'Mouse Over';
                    return;
                }
                if (type === 'auto') {
                    $scope.item.expandStyle = models.EExpandActiveStyle.AUTO;
                    $scope.expand_style = 'Auto';
                    return;
                }
            };

            //----------------- PR Banner ------------

            $scope.selectCate = (cate: any) => {
                if ($scope.checkBoxCate[cate.id] === true) {
                    ($scope.selectedCates = $scope.selectedCates || []).push(cate)
                } else {
                    var i = $scope.categories.indexOf(cate);
                    if (i !== -1)
                        $scope.selectedCates.splice(i, 1);
                }
            }
            $scope.removeCate = (cId: number) => {
                $scope.selectedCates.forEach((cate, i) => {
                    if (cate["id"] === cId) {
                        $scope.selectedCates.splice(i, 1);
                        $scope.checkBoxCate[cId] = false;
                    }
                })
            }
            //-------------
            function checkValidZone() {
                if ($scope.item.kind.toLowerCase() === "expandable") {
                    $scope.bookedZones.forEach((zone, i) => {
                        if ($scope.expandableZone[zone.zoneId] > 0) {
                            $("form #zone" + zone.zoneId).each((i, el) => el.setAttribute("disabled", ""));
                            $("form #li" + zone.zoneId).each((i, el) => el.setAttribute("class", "unavailable"));
                        }
                    });
                }
            }
            function checkValidSize() {
                $scope.isWarningZoneSize = false;
                $scope.isWarningSize = false;
                if ($scope.item && ($scope.item.kind.toLowerCase() == "media" || $scope.item.kind.toLowerCase() == "html")) {
                }
                else if ($scope.item && models.CampaignItemType.isNetworkType($scope.item.kind)
                    && !models.CampaignItemType.isTVC($scope.item.kind) && $scope.targetZonesOption !== 'all' &&
                    $scope.item.kind !== models.CampaignItemType.NETWORK.PRBANNER &&
                    $scope.item.kind !== models.CampaignItemType.BOOKING.PRBANNER) {
                    $scope.selectedWebsites.forEach((wid, i) => {
                        for (var zid in $scope.zones[wid]) {
                            if ($scope.item.width >= 0 && $scope.item.width > $scope.zones[wid][zid].width ||
                                $scope.item.height >= 0 && $scope.item.height > $scope.zones[wid][zid].height) {
                                document.getElementById("zone1" + zid).disabled = true;
                                document.getElementById("li1" + zid).setAttribute("class", "unavailable");
                                $scope.isWarningZoneSize = true;
                                if ($scope.checkboxZone[wid][zid]['null']) {
                                    $scope.isWarningSize = true;
                                    document.getElementById("li2" + zid).setAttribute("style", "color: #d14; opacity: 0.5");
                                }
                            }
                            else if (!$scope.item.width || !$scope.item.height) {
                                $scope.isWarningZoneSize = true;
                                document.getElementById("zone1" + zid).disabled = true;
                                document.getElementById("li1" + zid).setAttribute("class", "unavailable");
                            } else if ($scope.item.kind.toLocaleLowerCase() === 'networkexpandable' && !$scope.zones[wid][zid].allowedExpand) {
                                document.getElementById("zone1" + zid).disabled = true;
                                document.getElementById("li1" + zid).setAttribute("class", "unavailable");
                            } else if ($scope.item.width <= $scope.zones[wid][zid].width && $scope.item.height <= $scope.zones[wid][zid].height) {
                                document.getElementById("zone1" + zid).disabled = false;
                                document.getElementById("li1" + zid).setAttribute("class", "");
                                if ($scope.checkboxZone[wid][zid]['null']) {
                                    document.getElementById("li2" + zid).removeAttribute("style");
                                }
                            }
                        }
                    });
                }
            }
            function checkSelectZoneWeb(websiteId: number) {
                var selected = false;
                for (var zId in $scope.checkboxZone[websiteId])
                    if ($scope.checkboxZone[websiteId][zId] == true)
                        selected = true;
                if (!selected && $scope.checkboxZoneWeb[websiteId] == true)
                    $scope.checkboxZoneWeb[websiteId] = false;
            }
            function listZoneWebsite() {
                $scope.zones = [];
                $scope.checkboxZone = $scope.checkboxZone || {};
                var zones: {};
                $scope.selectedWebsites.forEach(function (websiteId, index) {
                    var runningMode: string = "";
                    if ($scope.item && ($scope.item.kind === models.CampaignItemType.NETWORK.TVC))
                        runningMode = models.EZoneRunningMode.NETWORK_TVC;
                    else if ($scope.item && ($scope.item.kind === models.CampaignItemType.NETWORK.TVCBANNER))
                        runningMode = models.EZoneRunningMode.NETWORKBANNER;
                    else
                        runningMode = models.EZoneRunningMode.NETWORK;
                    factory.zoneService.listByRefIdAndRunningMode(websiteId, 0, 10000, runningMode, function (zonesRet: backend.ReturnList<models.Zone>) {
                        if (zonesRet && zonesRet.data.length > 0) {
                            zones = {};
                            $scope.checkboxZone[websiteId] = {};
                            zonesRet.data.forEach((zone, index) => {
                                zones[zone.id] = zone;
                                $scope.checkboxZone[websiteId][zone.id] = {};
                                if ($scope.item && $scope.item.kind.toLowerCase() == 'networktvc') {
                                    $scope.checkboxZone[websiteId][zone.id]["Pre"] = false;
                                    $scope.checkboxZone[websiteId][zone.id]["Mid"] = false;
                                    $scope.checkboxZone[websiteId][zone.id]["Post"] = false;
                                } else {
                                    $scope.checkboxZone[websiteId][zone.id]["null"] = false;
                                }
                            });
                            $scope.zones[websiteId] = zones;

                            $scope.selectedZones.forEach((o, i) => {
                                if ($scope.item.kind.toLowerCase() == 'networktvc') {
                                    $scope.checkboxZone[o['website']][o['zone']][o["pos"]] = true;
                                } else {
                                    $scope.checkboxZone[o['website']][o['zone']]["null"] = true;
                                }
                            });
                            $timeout(() => checkValidSize(), 20);
                        }
                    });
                });
            }
            function listAllZone() {
                $scope.selectedZones = [];
                $scope.selectedWebsites.forEach(function (websiteId, index) {
                    factory.zoneService.listByWebsiteId(websiteId, 0, 10000, function (zonesRet: backend.ReturnList<models.Zone>) {
                        if (zonesRet && zonesRet.data.length > 0) {
                            zonesRet.data.forEach((zone, index) => {
                                if ($scope.item.width >= 0 && $scope.item.width <= zone.width &&
                                    $scope.item.height >= 0 && $scope.item.height <= zone.height) {
                                    if ($scope.item.kind.toLowerCase() == 'networktvc') {
                                        $scope.selectedZones.push({ website: websiteId, zone: zone.id, pos: "Pre" },
                                            { website: websiteId, zone: zone.id, pos: "Mid" },
                                            { website: websiteId, zone: zone.id, pos: "Post" });
                                        $scope.checkboxZone[websiteId][zone.id]["Pre"] = true;
                                        $scope.checkboxZone[websiteId][zone.id]["Mid"] = true;
                                        $scope.checkboxZone[websiteId][zone.id]["Post"] = true;
                                    } else {
                                        $scope.selectedZones.push({ website: websiteId, zone: zone.id, pos: "null" });
                                        $scope.checkboxZone[websiteId][zone.id]["null"] = true;
                                    }
                                }
                            });
                        }
                    });
                });
            }
        }
    }
    
    export class CampaignMediaPlanController extends PermissionController{
        constructor($state, $stateParams, $timeout, $scope: scopes.ICampaignMediaScope, 
            CurrentTab: common.CurrentTab, Action: utils.DataStore, factory: backend.Factory) {

            $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
            super($scope, $state, factory);

            $scope.campaign_name = '';
            $scope.startTimestamp = 0; $scope.endTimestamp = 0; $scope.available = 0;
            $scope.data = null; $scope.item = null; $scope.share = "0";
            $scope.$emit('change_action_menu', [$stateParams.orderId]);
            $scope.isEditZone = false; $scope.editPopupTop = 0; $scope.editPopupLeft = 0; $scope.editPopupStyle = {};
            $scope.action = ""; $scope.datepicker = {};
            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.orderId = $stateParams.orderId;
            $scope.items = [];

            var today = new Date();
            today = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            var endDay: Date = today;
            endDay.setDate(endDay.getDate() + 150);
            $scope.datepicker['startDate'] = new Date(today.getTime() - new Date().getTimezoneOffset() * 60000);
            $scope.datepicker['endDate'] = new Date(endDay.getTime() - new Date().getTimezoneOffset() * 60000);
            $scope.startTimestamp = today.getTime();
            $scope.endTimestamp = endDay.getTime();
            $scope.bookRecordDic = {};
            factory.campaignService.load($stateParams.campaignId, function (item: models.Campaign) {
                if (item !== undefined) {
                    if (item.campaignType !== "booking")
                        window.history.back(-1);
                    $scope.campaign_name = item.name;
                    $scope.startTimestamp = item.startDate;
                    $scope.endTimestamp = item.endDate;
                }

                factory.bookService.getBookedByKind($stateParams.campaignId, models.EBookingKind.CAMPAIGN, 0, 0, function (bookRecords: Array<models.BookRecord>) {
                    if (bookRecords.length === 0) {
                        $scope.data = null;
                        return;
                    }
                    for (var i: number = 0, len: number = bookRecords.length; i < len; i++) {
                        $scope.bookRecordDic[bookRecords[i].id] = bookRecords[i];
                    }
                    var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getBookingPlan(bookRecords);
                    $scope.data = { type: "mediaplan", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                });
            });

            //-------------------- function
            $scope.hasMediaPlan = (): boolean => {
                if ($scope.data === null || $scope.data.length === 0)
                    return false;
                return true;
            }

            $scope.goBookingPlan = () => {
                Action.store(common.ActionDefinition.BOOK_MORE_ZONE, { campaignId: $stateParams.campaignId });
                $state.transitionTo('main.order.booking_campaign', { orderId: $stateParams.orderId });
            }

            $scope.editbooking = () => {

                if ($scope.item !== null) {
                    $scope.$apply(function () {
                        $scope.datepicker['book_zone_start'] = new Date($scope.item.start - new Date().getTimezoneOffset() * 60000);
                        $scope.datepicker['book_zone_end'] = new Date($scope.item.end - new Date().getTimezoneOffset() * 60000 - 86400000);
                        factory.bookService.getUsageByZone($scope.item.zoneid, $scope.item.start, $scope.item.end, function (response) {
                            if (response !== undefined) {
                                if ($scope.isEditZone)
                                    $scope.isEditZone = false;
                                $scope.available = 100 - response.total;
                                $scope.action = 'edit';
                                $scope.share = $scope.item.share.toString();
                                $scope.editPopupTop = $scope.editPopupTop - jQuery("#maincontent").offset().top;
                                $scope.editPopupLeft = $scope.editPopupLeft - jQuery("#maincontent").offset().left;
                                $scope.editPopupStyle = { top: $scope.editPopupTop, left: $scope.editPopupLeft };
                                factory.campaignItemService.getItemsByZoneId($scope.item.zoneid, 0, 10, function (list: backend.ReturnList<models.AdItem>) {
                                    $scope.items = [];
                                    $scope.hasItem = false;
                                    for (var i: number = 0; i < list.data.length; i++) {
                                        var item: models.AdItem = list.data[i];
                                        if (item.campaignId == $stateParams.campaignId)
                                            $scope.items.push(item);
                                    }
                                    if ($scope.items.length > 0)
                                        $scope.hasItem = true;
                                    $scope.isEditZone = true;
                                });
                                
                            }

                        });
                    });
                }
            };

            $scope.isActive = (type: string): string => {
                if ($scope.action === type)
                    return "active";
                return "";
            };

            $scope.changeAction = (action: string) => {
                if ($scope.action === action)
                    return;
                $scope.action = action;
            };

            $scope.isAction = (action: string): boolean => {
                if ($scope.action === action)
                    return true;
                return false;
            };

            $scope.close = () => {
                $scope.isEditZone = false;
            };

            $scope.isDisable = (type: string): boolean => {
                if (type === "book_start_date") {
                    var timestamp: number = new Date().getTime();
                    if ($scope.datepicker['book_zone_start'] < timestamp)
                        return true;
                    return false;
                }

                if (type === "book_end_date") {
                    var timestamp: number = new Date().getTime();
                    if ($scope.datepicker['book_zone_end'] < timestamp)
                        return true;
                    return false;
                }
            };

            $scope.doAction = () => {
                if ($scope.action === 'delete') {
                    $scope.isEditZone = false;
                    var ids: Array<number> = [];
                    ids.push($scope.item.id);

                    factory.bookService.removeBook(ids, function (response) {
                        if (response === "success") {
                            $scope.bookRecordDic = {};
                            factory.bookService.getBookedByKind($stateParams.campaignId, models.EBookingKind.CAMPAIGN, 0, 0, function (bookRecords: Array<models.BookRecord>) {
                                for (var i: number = 0, len: number = bookRecords.length; i < len; i++) {
                                    $scope.bookRecordDic[bookRecords[i].id] = bookRecords[i];
                                }
                                var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getBookingPlan(bookRecords);
                                $scope.data = { type: "mediaplan", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                            });
                            $scope.$emit('reload_campaign_detail');
                        }
                    }, function (msg, status) {
                    });
                    return;
                }
                if ($scope.action === 'edit' && (((parseInt($scope.share) - $scope.item.share) <= $scope.available && parseInt($scope.share) > $scope.item.share) || (parseInt($scope.share) < $scope.item.share && parseInt($scope.share) > 0)) && parseInt($scope.share) > 0) {
                    $scope.isEditZone = false;
                    var bookRecord: models.BookRecord = $scope.bookRecordDic[$scope.item.id];
                    bookRecord.share = parseInt($scope.share);

                    factory.bookService.update(bookRecord, function (response) {
                        if (response === "success") {
                            $scope.bookRecordDic = {};
                            factory.bookService.getBookedByKind($stateParams.campaignId, models.EBookingKind.CAMPAIGN, 0, 0, function (bookRecords: Array<models.BookRecord>) {
                                for (var i: number = 0, len: number = bookRecords.length; i < len; i++) {
                                    $scope.bookRecordDic[bookRecords[i].id] = bookRecords[i];
                                }
                                var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getBookingPlan(bookRecords);
                                $scope.data = { type: "mediaplan", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                            });
                        }
                    }, function (msg, status) {
                    });
                }

                if ($scope.action === 'edit_date') {
                    var start_timestamp: number = $scope.datepicker['book_zone_start'].getTime() + new Date().getTimezoneOffset() * 60000;
                    var end_timestamp: number = $scope.datepicker['book_zone_end'].getTime() + new Date().getTimezoneOffset() * 60000 + 86400000;

                    var bookRecord: models.BookRecord = $scope.bookRecordDic[$scope.item.id];

                    if ($scope.item.start <= start_timestamp && $scope.item.end >= end_timestamp) {
                        bookRecord.from = start_timestamp; bookRecord.to = end_timestamp;

                        factory.bookService.update(bookRecord, function (response) {
                            if (response === 'success') {
                                $scope.isEditZone = false;
                                $scope.bookRecordDic = {};
                                factory.bookService.getBookedByKind($stateParams.campaignId, models.EBookingKind.CAMPAIGN, 0, 0, function (bookRecords: Array<models.BookRecord>) {
                                    for (var i: number = 0, len: number = bookRecords.length; i < len; i++) {
                                        $scope.bookRecordDic[bookRecords[i].id] = bookRecords[i];
                                    }
                                    var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getBookingPlan(bookRecords);
                                    $scope.data = { type: "mediaplan", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                                });
                            }
                        }, function (msg, status) {
                        });
                    }
                }
            };
        }
    }

    export class CampaignSettingController {
        constructor($state, $stateParams, $timeout, $scope: scopes.ICampaignScope, CurrentTab: common.CurrentTab, factory: backend.Factory) {

                $scope.$emit('change_action_menu', [$stateParams.orderId]); $scope.datepicker = { "startDate": new Date(), "endDate": new Date() };
            CurrentTab.setTab(new common.Tab('order', 'list_order'));
            $scope.orderId = $stateParams.orderId;
            var campaign: models.NetworkCampaign;
            factory.campaignService.load($stateParams.campaignId, function (ret) {
                if (ret.id) {
                    $scope.item = ret;
                    campaign = ret;
                    var sD = new Date($scope.item.startDate - new Date().getTimezoneOffset() * 60000);
                    var eD = new Date($scope.item.endDate - 1);
                    $scope.startDate = sD.getTime().toString();
                    $scope.endDate = eD.getTime().toString();
                    $scope.datepicker = { "startDate": sD, "endDate": eD };

                    $scope.campaignType = ret.campaignType;
                    if ($scope.campaignType === models.CampaignType.BOOKING) {
                        $scope.title = 'Booking Campaign';
                    } else if ($scope.campaignType === models.CampaignType.NETWORK) {
                        $scope.title = 'Network Campaign';
                    } else if ($scope.campaignType === models.CampaignType.NETWORK_TVC) {
                        $scope.title = 'TVC Campaign';
                    }

                    if ($scope.campaignType === models.CampaignType.NETWORK || $scope.campaignType === models.CampaignType.NETWORK_TVC) {
                        var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
                        var timeinday = { time: 'user', data: {} };
                        for (var i: number = 0; i < ret.timeScheduled.length; i++) {
                            timeinday.data[days[i]] = ret.timeScheduled[i];
                        }
                        if (ret.timeZone === 1)
                            timeinday.time = 'publisher';
                        $scope.timeinday = timeinday;
                        $scope.impression = ret.freqCapping;
                        $scope.time = ret.freqCappingTime;
                        $scope.chooseTimeUnit(ret.freqCappingTimeUnit);
                        //$scope.chooseDisplayType(ret.displayType);
                        if (ret.companion == true)
                            $scope.chooseDisplayType(0);
                        else {
                            $scope.chooseDisplayType(-1);
                        }
                    }
                }
            });
            // ---------- function --------------
            $scope.$watch('timeinday', function (newValue, oldValue) {
                if (newValue && oldValue) {
                    // update time 
                    factory.campaignService.load($stateParams.campaignId, function (ret) {
                        if (ret.id) {
                            var item: models.NetworkCampaign = ret;

                            var timeinday = [];
                            for (var day in newValue['data']) {
                                timeinday.push(newValue['data'][day]);
                            }
                            item.timeScheduled = timeinday;
                            if ($scope.timeinday['time'] === 'user')
                                item.timeZone = models.NetworkCampaignTimeZoneType.USER;
                            else
                                item.timeZone = models.NetworkCampaignTimeZoneType.PUBLISHER;

                            factory.campaignService.update(item, function (ret) { });
                        }
                    });
                }
                    
            });
            // scope functions
            $scope.isNetworkCampaign = (): boolean => {
                var ctype = $scope.campaignType;
                if (ctype === models.CampaignType.NETWORK || ctype === models.CampaignType.NETWORK_TVC) {
                    return true;
                }
                return false;
            };

            $scope.validateEndTime = () => {
                if ($scope.datepicker['startDate'].getTime() > $scope.datepicker['endDate'].getTime()) {
                    return true;
                }
                return false;
            };

            $scope.validateNumber = (num: number) => {
                if ($scope.checkValidate) {
                    if (num > 0)
                        return false; // valid : return false;
                    return true;
                }
                return false;
            };

            $scope.isDisable = (frm: any): boolean => {
                if ($scope.campaignType === 'booking') {
                    var result = $scope.item.name.length == 0 || ($scope.datepicker['endDate'] == null) || ($scope.datepicker['startDate'] == null);
                    return result;
                }
                if ($scope.campaignType === 'network' && $scope.campaignType === 'networktvc') {
                    return false;
                }

            };

            $scope.getCampaignStatusClass = (status: string): string => {
                if (status === "Running")
                    return "label-blue";
                else if (status === "Paused")
                    return "label-orange";
            };

            $scope.chooseTimeUnit = (unit: string) => {
                $scope.timeunit = unit;
                switch (unit) {
                    case "minute":
                        $scope.timeUnitString = "Minutes";
                        break;
                    case "hour":
                        $scope.timeUnitString = "Hours";
                        break;
                    case "day":
                        $scope.timeUnitString = "Days";
                        break;
                }
            };

            $scope.chooseDisplayType = (displayType: number) => {
                $scope.displayType = displayType;
                switch (displayType) {
                    case 0:
                        $scope.displayTypeString = "All";
                        $scope.item.companion = true;
                        break;
                    case 1:
                        $scope.displayTypeString = "As many as possible";
                        $scope.item.companion = false;
                        break;
                    case 2:
                        $scope.displayTypeString = "One or more";
                        $scope.item.companion = false;
                        break;
                    case 3:
                        $scope.displayTypeString = "Only one";
                        $scope.item.companion = false;
                        break;
                    case -1:
                        $scope.displayTypeString = "None";
                        $scope.item.companion = false;
                }
            };

            $scope.update = () => {
                $scope.checkValidate = true;

                if ($scope.isNetworkCampaign()) {
                    if ($scope.validateEndTime())
                        return;
                    //if ($scope.impression <= 0 || $scope.time <= 0)
                    //    return;

                    var item: models.NetworkCampaign = campaign;
                    if ($scope.item.name === undefined || $scope.item.name.length === 0)
                        return;
                    if ($scope.datepicker["startDate"] !== null) {
                        var startDate: Date = common.DateTimeUtils.get0hDate($scope.datepicker["startDate"]);
                        item.startDate = startDate.getTime();
                    }
                    if ($scope.datepicker["endDate"] !== null) {
                        var endDate: Date = common.DateTimeUtils.get0hDate($scope.datepicker["endDate"]);
                        item.endDate = endDate.getTime() + 86400000;
                    }
                    item.name = $scope.item.name;
                    item.campaignType = $scope.campaignType;
                    item.status = models.ECampaignStatus.Pending.toString();
                    if ($scope.timeinday) {
                        var timeinday = [];
                        for (var day in $scope.timeinday['data']) {
                            timeinday.push($scope.timeinday['data'][day]);
                        }
                        item.timeScheduled = timeinday;
                        if ($scope.timeinday['time'] === 'user')
                            item.timeZone = models.NetworkCampaignTimeZoneType.USER;
                        else
                            item.timeZone = models.NetworkCampaignTimeZoneType.PUBLISHER;
                    }
                    item.status = models.CampaignStatus.RUNNING;
                    item.freqCappingTime = $scope.time;
                    item.freqCapping = $scope.impression;
                    item.freqCappingTimeUnit = $scope.timeunit;
                    item.displayType = $scope.displayType;
                    factory.campaignService.update(item, function (ret) {
                        if (ret === 'success') {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "campaign"));
                            $timeout(function () {
                                $state.transitionTo('main.order.detail.campaign', { orderId: $stateParams.orderId });
                            }, 1000);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "campaign"));
                    }, function (msg, status) {
                    });
                } else {
                    if ($scope.datepicker["startDate"] !== null)
                        $scope.item.startDate = $scope.datepicker["startDate"].getTime() + new Date().getTimezoneOffset() * 60000;
                    if ($scope.datepicker["endDate"] !== null)
                        $scope.item.endDate = $scope.datepicker["endDate"].getTime() + new Date().getTimezoneOffset() * 60000 + 86400000;

                    $scope.item.campaignType = $scope.campaignType;
                    $scope.item.status = models.CampaignStatus.RUNNING;

                    factory.campaignService.update($scope.item, function (ret) {
                        if (ret === 'success') {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "campaign"));
                            $timeout(function () {
                                $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, page: 1 });
                            }, 1000);
                            return;
                        }
                        notify(new common.ActionMessage(common.ActionMessage.FAIL, "update", "campaign"));
                    }, function (msg, status) {
                    });
                }

            };

            $scope.goto = (dest: string) => {
                if (dest === 'campaigns') {
                    $state.transitionTo('main.order.detail.campaign', { orderId: $scope.orderId, page: 1 });
                }
            };

            $scope.isAllDayTime = (): boolean => {
                if ($scope.timeinday === undefined || $scope.timeinday === null)
                    return true;
                return false;
            };

        }
    }
    // --------------------------- Booking --------------------------------------------------
    export class BookingController extends PermissionController {
        constructor($scope: scopes.IBookingScope, $timeout, $stateParams, $state,
            Page: common.PageUtils, CurrentTab: common.CurrentTab, Action: utils.DataStore, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.checkDenyAccess(utils.PermissionUtils.ORDER, $stateParams.orderId);


            Page.setTitle('Booking | 123Click');
            CurrentTab.setTab(new common.Tab("order", "list_order"));
            $scope.datepicker = { "startDate": new Date(), "endDate": new Date(), "book_zone_start": new Date(), "book_zone_end": new Date() };
            $scope.campaigns = [];
            $scope.siteId = 0;
            $scope.sitename = ''; $scope.campaign_name = ''; $scope.orderId = $stateParams.orderId;
            $scope.data = {}; $scope.bookingItems = []; $scope.websites = [];
            $scope.bookingView = true; $scope.selecteditem = ''; $scope.selectSiteName = '';
            var currentDate = new Date();
            var tmpStartDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
            var tmpEndDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
            $scope.datepicker['startDate'] = new Date(tmpStartDate.getTime() - new Date().getTimezoneOffset() * 60000);
            tmpEndDate.setDate(tmpEndDate.getDate() + 150); $scope.datepicker['endDate'] = new Date(tmpEndDate.getTime() - new Date().getTimezoneOffset() * 60000);

            $scope.startTimestamp = tmpStartDate.getTime();// - new Date().getTimezoneOffset() * 60000;
            $scope.endTimestamp = tmpEndDate.getTime();// - new Date().getTimezoneOffset() * 60000;
            $scope.isShowBookingMenu = false; $scope.isBookByDrag = false;

            $scope.zone_available = 0; $scope.zone_share = 0; $scope.zone_name = ""; $scope.zone_id = 0;
            $scope.isShowBookingPopup = false; $scope.bookingPopupPosition = {}; $scope.xcoor = 0; $scope.ycoor = 0;
            $scope.exdata = {};
            $scope.bookedItems = [];

            $scope.currentOffset = 0; $scope.count = 100; $scope.totalItems = 0;
            var itemBanner: models.AdItem = null, zIds = [], zSizeDic = {};
                factory.websiteService.listByTypeMinimize($scope.currentOffset, $scope.count, ["booking"], function (list: backend.ReturnList<models.Website>) {
                $scope.websites = list.data;
                var returnSize = Math.min(list.data.length, $scope.count);
                $scope.currentOffset += returnSize;
                $scope.totalItems = list.total;
                $scope.websites.sort((a, b) => (a.name.toLowerCase() > b.name.toLowerCase() ? 1 : a.name.toLowerCase() === b.name.toLowerCase() ? 0 : -1));
                listSite();

                    factory.campaignItemService.load($stateParams.itemId, function (itemBannerRet: models.AdItem) {
                    if (itemBannerRet == null || itemBannerRet == undefined) return;

                    itemBanner = itemBannerRet;
                        factory.campaignService.listByOrderId($scope.orderId, 0, 100, function (returnedList) {
                        $scope.campaigns = returnedList.data;
                        if ($scope.websites.length > 0) {
                            getValidSite($scope.websites, 0, itemBanner, $scope.startDateCamp, $scope.endDateCamp);
                        }
                    });
                });
            });

                $scope.startDateCamp = 0; $scope.endDateCamp = 0;
                factory.campaignService.load($stateParams.campaignId, function (camp: models.Campaign) {
                    if (camp && camp.id) {
                        $scope.startDateCamp = camp.startDate;
                        $scope.endDateCamp = camp.endDate;
                    }
                });
                // -------------------- function -------------------------
                function listSite() {
                    factory.websiteService.listByTypeMinimize($scope.currentOffset, $scope.count, ["booking"], (siteList) => {
                        if (siteList && siteList.data.length != 0) {
                            $scope.websites = $scope.websites.concat(siteList.data);
                            $scope.websites.sort((a, b) => (a.name.toLowerCase() > b.name.toLowerCase() ? 1 : a.name.toLowerCase() === b.name.toLowerCase() ? 0 : -1));
                            var returnSize = Math.min(siteList.data.length, $scope.count);
                            $scope.currentOffset += returnSize;
                            if ($scope.currentOffset < $scope.totalItems)
                                listSite();
                        }
                    });
                }
                function getValidSite(sites: Array<models.Website>, index: number, banner: models.AdItem, from: number, to: number) {
                    $scope.siteId = sites[index].id;
                    $scope.sitename = sites[index].name;
                    $scope.selectSiteName = $scope.sitename;
                    var zIds = [], zSizeDic: {} = {};
                    factory.bookService.getAvailableBooks($scope.siteId, from, to, function (data: Array<models.AvailableBooked>) {
                        data.forEach((a, _) => zIds.push(a.zoneId));
                        factory.zoneService.listByIds(zIds, function (zonesRet: Array<models.Zone>) {
                            zonesRet.forEach((z, __) => zSizeDic[z.id] = { w: z.width, h: z.height });

                            var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneSchedules(data, banner, zSizeDic);
                            if (checkZoneSchedule(zoneSchedules)) {
                                
                                $scope.data = { type: "booking", data: zoneSchedules, minDate: $scope.startTimestamp, maxDate: $scope.endTimestamp };
                                data.forEach((a, _) => {
                                    if (a.availables) {
                                        a.availables.forEach((booked, __) => {
                                            $scope.bookedItems.push(booked);
                                        });
                                    }
                                });
                            } else if (index + 1 < sites.length - 1){
                                getValidSite(sites, index + 1, banner, from, to);
                            }
                        });
                    });
                }


            $scope.$watch('datepicker.book_zone_end', function (newValue, oldValue) {
                if ($scope.isShowBookingPopup) {
                    var start_time: number = parseInt($scope.datepicker["book_zone_start"].getTime()) + new Date().getTimezoneOffset() * 60000;
                    var end_time: number = parseInt($scope.datepicker["book_zone_end"].getTime()) + new Date().getTimezoneOffset() * 60000 + 86400000;
                    if (start_time < end_time) {
                        factory.bookService.getUsageByZone($scope.zone_id, start_time, end_time, function (response) {
                            $scope.zone_available = 100 - response.total - checkUsageAvailable($scope.zone_id, start_time, end_time);
                            $scope.zone_share = $scope.zone_available;
                        });
                    }
                }
            });

            $scope.$watch('datepicker.book_zone_start', function (newValue, oldValue) {
                if ($scope.isShowBookingPopup) {
                    var start_time: number = parseInt($scope.datepicker["book_zone_start"].getTime()) + new Date().getTimezoneOffset() * 60000;
                    var end_time: number = parseInt($scope.datepicker["book_zone_end"].getTime()) + new Date().getTimezoneOffset() * 60000 + 86400000;
                    if (start_time < end_time) {
                        factory.bookService.getUsageByZone($scope.zone_id, start_time, end_time, function (response) {
                            $scope.zone_available = 100 - response.total - checkUsageAvailable($scope.zone_id, start_time, end_time);;
                            $scope.zone_share = $scope.zone_available;
                        });
                    }
                }
            });

            $scope.isDisable = () => {
                if ($scope.bookingItems)
                    return $scope.bookingItems.length === 0;
                return false;
            };

            $scope.init = () => {
                $scope.zone_available = 0; $scope.zone_share = 0; $scope.zone_name = ""; $scope.zone_id = 0;
                $scope.isShowBookingPopup = false; $scope.bookingPopupPosition = {}; $scope.xcoor = 0; $scope.ycoor = 0;
                $scope.exdata = {};
            };

            $scope.choose = (website: models.Website) => {
                $scope.siteId = website.id;
                $scope.selectSiteName = website.name;
                $scope.bookingView = true;
                // load new booking chart
                $scope.sitename = $scope.selectSiteName;
                var currentDate = new Date();
                var tmpStartDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
                $scope.datepicker['startDate'] = tmpStartDate;
                var tmpEndDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 3, currentDate.getDate());
                $scope.datepicker['endDate'] = tmpEndDate;
                $scope.startTimestamp = tmpStartDate.getTime();
                $scope.endTimestamp = tmpEndDate.getTime();
                $scope.init();
                factory.bookService.getAvailableBooks($scope.siteId, $scope.startTimestamp, $scope.endTimestamp, function (data) {
                    zIds = []; zSizeDic = {};
                    data.forEach((a, _) => zIds.push(a.zoneId));
                    factory.zoneService.listByIds(zIds, function (zonesRet: Array<models.Zone>) {
                        zonesRet.forEach((z, __) => zSizeDic[z.id] = { w: z.width, h: z.height });

                        var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneSchedules(data, itemBanner, zSizeDic);
                        checkZoneSchedule(zoneSchedules);
                        $scope.data = {
                            type: "booking",
                            data: zoneSchedules,
                            minDate: $scope.startTimestamp,
                            maxDate: $scope.endTimestamp
                        };
                    });
                });
            };

            $scope.getSiteName = (siteId: number): string => {
                for (var i: number = 0; i < $scope.websites.length; i++) {
                    var siteItem: models.Website = $scope.websites[i];
                    if (siteId === siteItem.id) {
                        return siteItem.name;
                    }
                }
                return '';
            };

            $scope.gotoBookingChart = () => {
                $scope.bookingView = true;
                $scope.bookingItems = [];
                factory.bookService.getAvailableBooks($scope.siteId, $scope.startTimestamp, $scope.endTimestamp, function (data) {
                    zIds = []; zSizeDic = {};
                    data.forEach((a, _) => zIds.push(a.zoneId));
                    factory.zoneService.listByIds(zIds, function (zonesRet: Array<models.Zone>) {
                        zonesRet.forEach((z, __) => zSizeDic[z.id] = { w: z.width, h: z.height });

                        var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneSchedules(data, itemBanner, zSizeDic);
                        checkZoneSchedule(zoneSchedules);
                        $scope.data = { type: "booking", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                        factory.campaignService.listByOrderId($scope.orderId, 0, 100, function (returnedList) {
                            $scope.campaigns = returnedList.data;
                        });
                    });
                });
            };

            $scope.isDisableDel = () => {
                if ($scope.selecteditem === undefined || $scope.selecteditem === null || $scope.selecteditem.length == 0)
                    return "disabled"
                return "";
            };
            
            $scope.select = (zoneId: number, startTime: number, endTime: number) => {
                //console.log('Booking Controller : ' + zoneId + ' - ' + startTime + ' - ' + endTime);
            };

            $scope.viewBookingSchedule = () => {
                if ($scope.siteId == 0 || ($scope.datepicker['startDate'].getTime() >= $scope.datepicker['endDate'].getTime()))
                    return;

                var siteId: number = $scope.siteId;
                var startDate: Date = new Date($scope.datepicker['startDate'].getTime());
                startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
                var endDate: Date = new Date($scope.datepicker['endDate'].getTime());
                endDate = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
                $scope.startTimestamp = startDate.getTime();
                $scope.endTimestamp = endDate.getTime();

                factory.bookService.getAvailableBooks($scope.siteId, $scope.startTimestamp, $scope.endTimestamp, function (data) {
                    $scope.bookingView = true;
                    zIds = []; zSizeDic = {};
                    data.forEach((a, _) => zIds.push(a.zoneId));
                    factory.zoneService.listByIds(zIds, function (zonesRet: Array<models.Zone>) {
                        zonesRet.forEach((z, __) => zSizeDic[z.id] = { w: z.width, h: z.height });
                        var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneSchedules(data, itemBanner, zSizeDic);
                        checkZoneSchedule(zoneSchedules);
                        $scope.data = { type: "booking", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                        $scope.bookingView = true;
                        factory.websiteService.load($scope.siteId, function (website: models.Website) {
                            if (website !== undefined)
                                $scope.sitename = website.name;
                        });
                    });
                });
            };
            
            $scope.createCampaign = () => {
                if ($scope.campaign_name.length === 0)
                    return;
                var campaign: models.Campaign = new models.Campaign(0, $scope.campaign_name, $scope.orderId, new Date().getTime(), new Date().getTime(), models.CampaignType.BOOKING, '');
                factory.campaignService.save(campaign, function (item: models.Campaign) {
                    if (item.id > 0) {
                        $scope.addTo(item.id);
                    }
                });
            };

            $scope.createNew = () => {
                $state.transitionTo('main.order.campaign_create', { orderId: $scope.orderId, type: 'booking' });
            };

            // mapping booking directive
            $scope.setDirectiveFn = (directiveFn: any) => {
                $scope.booked = directiveFn;
            };

            $scope.click = (event: any) => {
                event.stopPropagation();
            };

            $scope.showBookingPopup = () => {
                if ($scope.exdata === undefined || $scope.exdata === null ||
                    $scope.exdata.start === undefined || $scope.exdata.zonename === undefined ||
                    $scope.exdata.zoneid === undefined) {
                    return;
                }
                $scope.isBookByDrag = false;
                if ($scope.isShowBookingPopup) {
                    $scope.isShowBookingPopup = false;
                }
                var top: number = $scope.ycoor - jQuery("#maincontent").offset().top;
                var left: number = $scope.xcoor - jQuery("#maincontent").offset().left;
                // check view
                if ($scope.xcoor + 400 > window.innerWidth)
                    left = left - 400;
                $scope.bookingPopupPosition = { top: top, left: left };
                $scope.zone_name = $scope.exdata.zonename;
                $scope.zone_id = $scope.exdata.zoneid;

                var start_timestamp: number = parseInt($scope.exdata.start) - new Date().getTimezoneOffset() * 60000;
                $scope.datepicker['book_zone_start'] = new Date(start_timestamp);

                $scope.datepicker['book_zone_end'] = new Date(start_timestamp);
                if ($scope.exdata.end !== undefined && $scope.exdata.end > 0) {
                    // book by drag
                    $scope.isBookByDrag = true;
                    var end_timestamp: number = parseInt($scope.exdata.end) - new Date().getTimezoneOffset() * 60000 - 86400000; // subtract 1 days to display
                    $scope.datepicker['book_zone_end'] = new Date(end_timestamp);

                    $scope.$apply(function () {
                        factory.bookService.getUsageByZone($scope.zone_id, parseInt($scope.exdata.start), parseInt($scope.exdata.end), function (response) {
                            $scope.zone_available = 100 - response.total - checkUsageAvailable($scope.zone_id, parseInt($scope.exdata.start), parseInt($scope.exdata.end));
                            $scope.zone_share = $scope.zone_available;
                            $scope.isShowBookingPopup = true;
                        });
                    });
                    return;
                }
                $scope.isShowBookingPopup = true;
                $scope.$apply();
            };

            $scope.closeBookingPopup = () => {
                $scope.isShowBookingPopup = false;
            };

            $scope.bookZone = () => {
                var start_time: number = $scope.datepicker['book_zone_start'].getTime() + new Date().getTimezoneOffset() * 60000;
                var end_time: number = $scope.datepicker['book_zone_end'].getTime() + new Date().getTimezoneOffset() * 60000;

                if (start_time < $scope.startDateCamp || end_time >= $scope.endDateCamp) {
                    $scope.showWarning = true;
                    $scope.isShowBookingPopup = false;
                    $timeout(() => $scope.showWarning = false, 5000);
                    return;
                }

                if ($scope.isBookByDrag) {
                    end_time = end_time + 86400000; // add 1 day to show booking char;
                    if (start_time < end_time) {
                        // mark time with label "booked"
                        $scope.item.share = $scope.zone_share;
                        $scope.item.percent = $scope.zone_available;
                        if ($scope.item === null || $scope.item === undefined)
                            return;
                        if ($scope.item.share === 0 || $scope.item.share > $scope.item.percent || $scope.item.share < 0)
                            return;
                        $scope.item.start = start_time; $scope.item.end = end_time;
                        var item: models.BookRecord = new models.BookRecord(0, $scope.item.zoneid, "", $stateParams.itemId, "", $scope.item.start, $scope.item.end, $scope.item.share);
                        item.share = $scope.zone_share;
                        $scope.bookingItems.push(item);
                        $scope.item = null;
                        // booked , mark timeline
                        $scope.booked();
                    }

                    $scope.isShowBookingPopup = false;
                    $scope.init();
                    return;
                }

                if (start_time <= end_time && $scope.zone_share <= $scope.zone_available && $scope.zone_share > 0) {
                    if (start_time < $scope.startTimestamp || end_time > $scope.endTimestamp) {

                        if (start_time < $scope.startTimestamp)
                            $scope.startTimestamp = start_time - 5 * 86400000;
                        if (end_time > $scope.endTimestamp)
                            $scope.endTimestamp = end_time + 5 * + 86400000;

                        factory.bookService.getAvailableBooks($scope.siteId, $scope.startTimestamp, $scope.endTimestamp, function (data) {
                            // update datepicker
                            $scope.datepicker['startDate'] = new Date($scope.startTimestamp - new Date().getTimezoneOffset() * 60000);
                            $scope.datepicker['endDate'] = new Date($scope.endTimestamp - new Date().getTimezoneOffset() * 60000);

                            $scope.bookingView = true;
                            zIds = []; zSizeDic = {};
                            data.forEach((a, _) => zIds.push(a.zoneId));
                            factory.zoneService.listByIds(zIds, function (zonesRet: Array<models.Zone>) {
                                zonesRet.forEach((z, __) => zSizeDic[z.id] = { w: z.width, h: z.height });
                                var zoneSchedules: utils.ZoneSchedule[] = utils.BookingUtils.getZoneSchedules(data, itemBanner, zSizeDic);
                                checkZoneSchedule(zoneSchedules);
                                $scope.data = { type: "booking", data: zoneSchedules, minDate: $scope.startTimestamp + new Date().getTimezoneOffset() * 60000, maxDate: $scope.endTimestamp + new Date().getTimezoneOffset() * 60000 };
                                $scope.bookingView = true;
                            });

                            $timeout(function () {
                                // add booking zone to chart
                                $scope.exdata['from'] = start_time; $scope.exdata['to'] = end_time;
                                $scope.exdata['share'] = $scope.zone_share; $scope.exdata['percent'] = $scope.zone_available;
                                var item: models.BookRecord = new models.BookRecord(0, $scope.item.zoneid, "", $stateParams.itemId, "", $scope.item.start, $scope.item.end, $scope.item.share);
                                item.share = $scope.zone_share;
                                $scope.bookingItems.push(item);
                                $scope.addBook();
                                $scope.isShowBookingPopup = false;
                                $scope.init();
                            }, 1000);
                        });
                        return;
                    }

                    $scope.exdata['from'] = start_time; $scope.exdata['to'] = end_time + 86400000 - 1;
                    $scope.exdata['share'] = $scope.zone_share; $scope.exdata['percent'] = $scope.zone_available;
                    var item: models.BookRecord = new models.BookRecord(0, $scope.zone_id, "", $stateParams.itemId, "", start_time, end_time + 86400000, $scope.zone_available);
                    item.share = $scope.zone_share;
                    $scope.bookingItems.push(item);
                    $scope.addBook();
                    $scope.isShowBookingPopup = false;
                    $scope.init();
                }
            };
            // mapping 
            $scope.addBookingFn = (directiveFn: any) => {
                $scope.addBook = directiveFn;
            };

            $scope.showBookingMenu = () => {
                var action: any = Action.get(common.ActionDefinition.BOOK_MORE_ZONE);
                if (action !== null && action !== undefined && action.campaignId > 0) {
                    $scope.addTo(action.campaignId);
                    Action.remove(common.ActionDefinition.BOOK_MORE_ZONE);
                    return;
                }

                if ($scope.campaigns.length === 0) {
                    $scope.createNew();
                    return;
                }
                $scope.isShowBookingMenu = true;
            };

            $scope.booking = () => {
                if ($scope.bookingItems.length === 0)
                    return;

                var data_arr: Array<models.BookRecord> = [];
                for (var i: number = 0; i < $scope.bookingItems.length; i++) {
                    var item: models.BookRecord = $scope.bookingItems[i];
                    data_arr.push(new models.BookRecord(0, item.zoneId, "", item.itemId, "", item.from, item.to, item.share));
                }
                factory.bookService.book(data_arr, function (res) {
                    $state.transitionTo('main.order.item_detail.linkedbooking', { orderId: $scope.orderId, campaignId: $stateParams.campaignId, itemId: $stateParams.itemId });
                });
            }
            $scope.cancel = () => {
                $state.transitionTo('main.order.item_detail.linkedbooking', { orderId: $scope.orderId, campaignId: $stateParams.campaignId, itemId: $stateParams.itemId });
            }
            $scope.deleteItem = () => {
                if ($scope.selecteditem !== null && $scope.selecteditem !== undefined) {
                    var dataObj = jQuery($scope.selecteditem).data('dataObj');
                    if (dataObj !== undefined) {
                        for (var i = $scope.bookingItems.length - 1; i >= 0; i--) {
                            var item: models.BookRecord = $scope.bookingItems[i];
                            if (dataObj.zoneId === item.zoneId && dataObj.from === item.from && (dataObj.to === item.to || dataObj.to === item.to - 1)) {
                                $scope.bookingItems.splice(i, 1);
                            }
                        }
                    }
                    jQuery($scope.selecteditem).remove();
                    $scope.selecteditem = null;
                }
            }

            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD/MM/YYYY');
            };

            function checkUsageAvailable(zoneId: number, from: number, to: number) {
                var usage: number = 0;
                if ($scope.bookingItems !== undefined && $scope.bookingItems !== null) {
                    for (var i = 0; i < $scope.bookingItems.length; i++) {
                        var iFrom = $scope.bookingItems[i].from, iTo = $scope.bookingItems[i].to;
                        if (!((from < iFrom) && (to <= iFrom)) && !((from >= iTo) && (to > iTo)) && $scope.bookingItems[i].zoneId == zoneId) {
                            if (!isNaN(parseInt($scope.bookingItems[i].share.toString())))
                                usage += parseInt($scope.bookingItems[i].share.toString());
                        }
                    }
                }
                return usage;
            }
            function checkZoneSchedule(zoneSchedule) {
                $scope.isNullSchedule = (zoneSchedule === null || zoneSchedule !== null && zoneSchedule.length == 0);
                return !$scope.isNullSchedule;
            }

        }
    }

    //----------- conversion tracking
    
    export class ConversionListController {
        constructor($scope: scopes.IConversionListScope, $state, $timeout, $stateParams, $modal, ActionMenuStore: utils.DataStore, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            var action_menu: Array<common.ActionMenu> = [];
            CurrentTab.setTab(new common.Tab('order', 'conversion_tracking'));
            $scope.pageIndex = 1;
            $scope.pageSize = 10;
            $scope.checkBoxes = {};
            $scope.isEditName = {};
            $scope.orders = {};
            $scope.sortField = { name: common.SortDefinition.DEFAULT };
            listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
            var currentSortField = "", currentSortType = "";
            
            function listConversion(from: number, count: number, sortBy?: string, sortType?: string) {
                factory.conversionService.list(from, count, function (conversionRet: backend.ReturnList<models.Conversion>) {
                    if (conversionRet) {
                        $scope.items = conversionRet.data;
                        $scope.totalRecord = conversionRet.total;
                        var orderIds = [];
                        $scope.items.forEach((conversion, index) => {
                            orderIds.push(conversion.orderId);
                        });
                        currentSortField = sortBy;
                        currentSortType = sortType;
                    }
                }, sortBy, sortType);
            }


            $scope.$watch('checkBoxes.all', function (newValue, oldValue) {
                if ($scope.items) {
                    for (var i: number = 0; i < $scope.items.length; i++) {
                        $scope.checkBoxes[$scope.items[i].id] = $scope.checkBoxes['all'];
                    }
                }
                $scope.isChosen = $scope.checkBoxes['all'];
            });

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
                    listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "asc");
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "asc");
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, type, "desc");
                    $scope.sortField[type] = common.SortDefinition.UP;
                }
                for (var aType in $scope.sortField)
                    if (type != aType)
                        $scope.sortField[aType] = common.SortDefinition.DEFAULT;
            };

            $scope.check = (id) => {
                if ($scope.checkBoxes[id] === true) {
                    $scope.isChosen = true;return
                }
                for (var i: number = 0; i < $scope.items.length; i++) {
                    if ($scope.checkBoxes[$scope.items[i].id]) {
                        $scope.isChosen = true; return;
                    }
                }
                $scope.isChosen = false;
            }

            $scope.gotoConversion = (id) => {
                $state.transitionTo("main.order.edit_conversion", {conversionId: id});
            };

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            }
            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            }
            $scope.editName = (id) => {
                $scope.isEditName = {};
                $scope.isEditName[id] = true;
            }
            $scope.saveName = (index) => {
                factory.conversionService.update($scope.items[index], function (res) {
                    $scope.isEditName[$scope.items[index].id] = false;
                    if (res === "success") {
                        listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize);
                    }
                });
            }
            $scope.saveByEnter = (index: number, $event) => {
                if ($event.keyCode === 13) {
                    $scope.saveName(index);
                }
            }
            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD/MM/YYYY');
            };
            $scope.formatWindows = (windows: number) => {
                if (windows % 7 == 0 && windows / 7 >= 1 && windows / 7 <= 4)
                    return (windows / 7) + " week" + (windows / 7 === 1 ? "" : "s");
                return windows + " day" + (windows === 1 ? "" : "s");
            }

            $scope.paging = (start: number, size: number) => {
                listConversion(start, size, currentSortField, currentSortType);
            }

            $scope.viewCode = (index: number) => {
                $scope.itemPreviewCode = $scope.items[index];
                factory.conversionService.genCode($scope.itemPreviewCode.orderId, function (codeRet) {
                    if (codeRet) {
                        $scope.itemPreviewCode.code = codeRet;
                        jQuery("#previewCode").modal("show");
                        setTimeout(function () {
                            var clip = new ZeroClipboard.Client();
                            clip.setHandCursor(true);

                            clip.addEventListener('load', function (client) {
                                console.log("Flash movie loaded and ready.");
                            });

                            clip.addEventListener('mouseOver', function (client) {
                                // update the text on mouse over
                                clip.setText($scope.itemPreviewCode.code);
                            });

                            clip.addEventListener('complete', function (client, text) {
                                $scope.$apply(function () { $scope.isCopied = true });
                                $timeout(() => $scope.$apply(function () { $scope.isCopied = false }), 1500);
                                console.log("Copied: " + text);
                            });

                            clip.glue('d_clip_button', "contain_clip");
                        }, 500);
                    }
                });
            }
            $scope.search = () => {
                factory.conversionService.search($scope.searchText, 0, false, function (list: Array<models.Conversion>) {
                    if (list !== null) {
                        $scope.items = list;
                        $scope.totalRecord = list.length;
                        $scope.pageIndex = 1;
                        $scope.checkBoxes = {};
                    }
                });
            }
            $scope.$watch("searchText", function (newVal, oldVal) {
                if (newVal === null || newVal.length === 0)
                    listConversion($scope.pageSize * ($scope.pageIndex - 1), $scope.pageSize, currentSortField, currentSortType);
            });
        }
    }

    export class ConversionEditController {
        constructor($scope: scopes.ICreateConversion, $state, $stateParams, $timeout, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.$emit("change_action_menu");
            CurrentTab.setTab(new common.Tab('conversion', 'conversion_tracking'));
            factory.conversionService.load($stateParams.conversionId, function (res: models.Conversion) {
                if (res && res.id > 0) {
                    $scope.item = res;
                    factory.conversionService.genCode(res.orderId, function (codeRet) {
                        if (codeRet)
                            $scope.item.code = codeRet;
                    });
                    if (res.windows % 7 === 0 && res.windows / 7 <= 4)
                        $scope.conversionWindows = (res.windows / 7) + " Week" + (res.windows !== 7 ? "s" : "");
                    else
                        $scope.conversionWindows = res.windows + " Days";
                }
                else {
                    $state.transitionTo('main.order.conversion', {});
                }
            });
            $scope.isDone = false;
            $scope.$watch("windows", function (newVal, oldVal) {
                if (newVal && !isNaN(parseInt($scope.windows))) {
                    $scope.item.windows = parseInt($scope.windows);
                }
            });
            $scope.choose = (value: string) => {
                if (value === "custom") {
                    $scope.isCustomWindow = true;
                    $scope.conversionWindows = "Custom";
                    $scope.windows = $scope.item.windows + "";
                }
                else {
                    $scope.isCustomWindow = false;
                    var arr: Array<string> = value.split(" ");
                    if (arr[0]) {
                        $scope.item.windows = parseInt(arr[0]);
                        $scope.conversionWindows = arr[0];
                    }
                    if (arr[1] && arr[1] === "w") {
                        $scope.conversionWindows += " Week";
                        if ($scope.item.windows > 1) $scope.conversionWindows += "s";
                        $scope.item.windows *= 7;
                    }
                    else if (arr[1] && arr[1] === "d") {
                        $scope.conversionWindows += " Days";
                    }
                }
            }

            $scope.save = () => {
                $scope.checkValidate = true;
                if ($scope.item.windows === 0 || $scope.item.windows < 7 || $scope.item.windows > 90)
                    return;
                $scope.item.updateDate = new Date().getTime();
                factory.conversionService.update($scope.item, function (res) {
                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "conversion"));
                    $timeout(function () {
                        $state.transitionTo('main.order.conversion', {});
                    }, 1000);
                });
            };
            $scope.cancel = () => {
                $state.transitionTo("main.order.conversion", {});
            };
        }
    }
}

//------------
function fixPreviewPosition(targetModal: string) {
    setTimeout(function () {
        var w = jQuery(targetModal).width();
        var h = jQuery(targetModal).height();
        jQuery(targetModal).css("top", (window.innerHeight - h) / 2);
        jQuery(targetModal).css("left", (window.innerWidth - w) / 2 + 280);
    }, 200);
    
};

