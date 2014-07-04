module controllers {
    'use strict';
    export class ReportBreadcumNavController {
        constructor($scope: scopes.IReportBreadcumNavScope,
            $state, $stateParams, $location,
            BreadCumStore: common.BreadCum,
            factory: backend.Factory) {

            BreadCumStore.pushState("main.report.orders", "orders_report");
            BreadCumStore.pushState("main.report.order.campaign", "order");
            BreadCumStore.pushState("main.report.order.linkedzone", "order");
            BreadCumStore.pushState("main.report.order.item", "order");
            BreadCumStore.pushState("main.report.order_linkedzonebysite", "order_linkedzone");
            BreadCumStore.pushState("main.report.campaign.item", "campaign");
            BreadCumStore.pushState("main.report.campaign.linkedsite", "campaign");
            BreadCumStore.pushState("main.report.campaign.linkedzone", "campaign");
            BreadCumStore.pushState("main.report.campaign_linkedzonebysite", "campaign_linkedzone");
            BreadCumStore.pushState("main.report.item.linkedsite", "item");
            BreadCumStore.pushState("main.report.item.linkedzone", "item");
            BreadCumStore.pushState("main.report.item_linkedzonebysite", "item_linkedzone");
            BreadCumStore.pushState("main.report.conversion", "orders_report");
            BreadCumStore.pushState("main.report.order.conversion", "order");
            BreadCumStore.pushState("main.report.campaign.conversion", "campaign");
            BreadCumStore.pushState("main.report.order.conversion_detail", "order_conv_detail");
            BreadCumStore.pushState("main.report.campaign.conversion_detail", "camp_conv_detail");
            BreadCumStore.pushState("main.report.item.conversion_detail", "item_conv_detail");

            BreadCumStore.pushState("main.report.order.website_delivery", "website_order_delivery");
            BreadCumStore.pushState("main.report.order.zone_delivery", "zone_order_delivery");
            BreadCumStore.pushState("main.report.campaign.website_delivery", "website_campaign_delivery");
            BreadCumStore.pushState("main.report.campaign.zone_delivery", "zone_campaign_delivery");
            BreadCumStore.pushState("main.report.item.website_delivery", "website_item_delivery");
            BreadCumStore.pushState("main.report.item.zone_delivery", "zone_item_delivery");

            BreadCumStore.pushState("main.report.websites", "websites");
            BreadCumStore.pushState("main.report.website.zonegroup", "website");
            BreadCumStore.pushState("main.report.website.linkedcamp", "website");
            BreadCumStore.pushState("main.report.website.zone", "website");
            BreadCumStore.pushState("main.report.website_linkeditembycampaign", "website_linkeditem");
            BreadCumStore.pushState("main.report.zonegroup", "zonegroup");
            BreadCumStore.pushState("main.report.zonegroup.linkedcamp", "zonegroup");
            BreadCumStore.pushState("main.report.zonegroup.linkeditem", "zonegroup");
            BreadCumStore.pushState("main.report.zonegroup.zone", "zonegroup");
            BreadCumStore.pushState("main.report.zonegroup_linkeditembycampaign", "zonegroup_linkeditem");
            BreadCumStore.pushState("main.report.zone", "zone");
            BreadCumStore.pushState("main.report.zone.linkedcamp", "zone");
            BreadCumStore.pushState("main.report.zone.linkeditem", "zone");
            BreadCumStore.pushState("main.report.zone_linkeditembycampaign", "zone_linkeditem");

            BreadCumStore.pushState("main.report.zingtv", "zingtv");

            BreadCumStore.tree.push(new common.BreadCumNode("report", "Report", -1));
            BreadCumStore.tree.push(new common.BreadCumNode("orders_report", "Orders", "report"));
            BreadCumStore.tree.push(new common.BreadCumNode("order", "", "orders_report"));
            BreadCumStore.tree.push(new common.BreadCumNode("order_linkedzone", "", "order"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign", "", "order"));
            BreadCumStore.tree.push(new common.BreadCumNode("campaign_linkedzone", "", "campaign"));
            BreadCumStore.tree.push(new common.BreadCumNode("item", "", "campaign"));
            BreadCumStore.tree.push(new common.BreadCumNode("item_linkedzone", "", "item"));
            BreadCumStore.tree.push(new common.BreadCumNode("order_conv_detail", "", "order"));
            BreadCumStore.tree.push(new common.BreadCumNode("camp_conv_detail", "", "campaign"));
            BreadCumStore.tree.push(new common.BreadCumNode("item_conv_detail", "", "item"));

            BreadCumStore.tree.push(new common.BreadCumNode("website_order_delivery", "Delivery", "order"));
            BreadCumStore.tree.push(new common.BreadCumNode("zone_order_delivery", "Delivery", "order"));
            BreadCumStore.tree.push(new common.BreadCumNode("website_campaign_delivery", "Delivery", "campaign"));
            BreadCumStore.tree.push(new common.BreadCumNode("zone_campaign_delivery", "Delivery", "campaign"));
            BreadCumStore.tree.push(new common.BreadCumNode("website_item_delivery", "Delivery", "item"));
            BreadCumStore.tree.push(new common.BreadCumNode("zone_item_delivery", "Delivery", "item"));

            BreadCumStore.tree.push(new common.BreadCumNode("websites", "Websites", "report"));
            BreadCumStore.tree.push(new common.BreadCumNode("website", "", "websites"));
            BreadCumStore.tree.push(new common.BreadCumNode("website_linkeditem", "", "website"));
            BreadCumStore.tree.push(new common.BreadCumNode("zonegroup", "", "website"));
            BreadCumStore.tree.push(new common.BreadCumNode("zonegroup_linkeditem", "", "website"));
            BreadCumStore.tree.push(new common.BreadCumNode("zone", "", "website"));
            BreadCumStore.tree.push(new common.BreadCumNode("zone_linkeditem", "", "zone"));

            BreadCumStore.tree.push(new common.BreadCumNode("zingtv", "Zing TV", "report"));

            $scope.items = new Array();
            if ($stateParams.orderId) {
                factory.orderService.load($stateParams.orderId, function (orderRet: models.Order) {
                    if (orderRet && orderRet.id > 0) {
                        BreadCumStore.tree.setNameNode("order", "Order (" + orderRet.name + ")");
                        if ($stateParams.campaignId) {
                            factory.campaignService.load($stateParams.campaignId, function (campaignRet: models.Campaign) {
                                if (campaignRet && campaignRet.id > 0) {
                                    BreadCumStore.tree.setNameNode("campaign", "Campaign (" + campaignRet.name + ")");
                                    if ($stateParams.itemId) {
                                        factory.campaignItemService.load($stateParams.itemId, function (itemRet) {
                                            if (itemRet && itemRet.id > 0) {
                                                BreadCumStore.tree.setNameNode("item", "Item (" + itemRet.name + ")");
                                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                                $scope.currview = BreadCumStore.getLink($state.current.name);

                                                if ($stateParams.websiteId) {
                                                    factory.websiteService.load($stateParams.websiteId, function (website: models.Website) {
                                                        if (website && website.id) {
                                                            BreadCumStore.tree.setNameNode("item_linkedzone", "Linked website (" + website.name + ")");
                                                            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                                            $scope.currview = BreadCumStore.getLink($state.current.name);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                    else if ($stateParams.websiteId) {
                                        factory.websiteService.load($stateParams.websiteId, function (website: models.Website) {
                                            if (website && website.id) {
                                                BreadCumStore.tree.setNameNode("campaign_linkedzone", "Linked website (" + website.name + ")");
                                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                                $scope.currview = BreadCumStore.getLink($state.current.name);
                                            }
                                        });
                                    }
                                    else {
                                        $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                        $scope.currview = BreadCumStore.getLink($state.current.name);
                                    }
                                }
                            });
                        }
                        else if ($stateParams.websiteId) {
                            factory.websiteService.load($stateParams.websiteId, function (website: models.Website) {
                                if (website && website.id) {
                                    BreadCumStore.tree.setNameNode("order_linkedzone", "Linked website (" + website.name + ")");
                                    $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                    $scope.currview = BreadCumStore.getLink($state.current.name);
                                }
                            });
                        }
                        else {
                            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                            $scope.currview = BreadCumStore.getLink($state.current.name);
                        }
                    }
                });
            }
            else if ($stateParams.websiteId) {
                factory.websiteService.load($stateParams.websiteId, function (websiteRet: models.Website) {
                    if (websiteRet && websiteRet.id > 0) {
                        BreadCumStore.tree.setNameNode("website", "Website (" + websiteRet.name + ")");
                        if ($stateParams.zonegroupId) {
                            factory.zoneGroupService.load($stateParams.zonegroupId, function (zoneGroup) {
                                BreadCumStore.tree.setNameNode("zonegroup", "Zonegroup (" + zoneGroup.name + ")");
                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);

                                if ($stateParams.campaignId) {
                                    factory.campaignService.load($stateParams.campaignId, function (campaign: models.Campaign) {
                                        BreadCumStore.tree.setNameNode("zonegroup_linkeditem", "Linked campaign (" + campaign.name + ")");
                                        $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                        $scope.currview = BreadCumStore.getLink($state.current.name);
                                    });
                                }
                            });
                        }
                        else if ($stateParams.zoneId) {
                            factory.zoneService.load($stateParams.zoneId, function (zone) {
                                BreadCumStore.tree.setNameNode("zone", "Zone (" + zone.name + ")");
                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);

                                if ($stateParams.campaignId) {
                                    factory.campaignService.load($stateParams.campaignId, function (campaign: models.Campaign) {
                                        BreadCumStore.tree.setNameNode("zone_linkeditem", "Linked campaign (" + campaign.name + ")");
                                        $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                        $scope.currview = BreadCumStore.getLink($state.current.name);
                                    });
                                }
                            });
                        }
                        else if ($stateParams.campaignId) {
                            factory.campaignService.load($stateParams.campaignId, function (campaign: models.Campaign) {
                                BreadCumStore.tree.setNameNode("website_linkeditem", "Linked campaign (" + campaign.name + ")");
                                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                                $scope.currview = BreadCumStore.getLink($state.current.name);
                            });
                        }
                        else {
                            $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                            $scope.currview = BreadCumStore.getLink($state.current.name);
                        }
                    }
                });
            }
            else {
                $scope.items = BreadCumStore.getMenuItemList(BreadCumStore.getLink($state.current.name));
                $scope.currview = BreadCumStore.getLink($state.current.name);
            }

            $scope.goto = (dest: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" || key === "filter" && (dest === "order" || dest === "zone")) && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.orderId) params["orderId"] = $stateParams.orderId;
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;
                if ($stateParams.campaignId) params["campaignId"] = $stateParams.campaignId;
                if ($stateParams.zoneId) params["zoneId"] = $stateParams.zoneId;
                if ($stateParams.itemId) params["itemId"] = $stateParams.itemId;

                switch (dest) {
                    case "orders_report":
                        $state.transitionTo("main.report.orders", params);
                        break;
                    case "order":
                        $state.transitionTo("main.report.order.campaign", params);
                        break;
                    case "campaign":
                        $state.transitionTo("main.report.campaign.item", params);
                        break;
                    case "websites":
                        $state.transitionTo("main.report.websites", params);
                        break;
                    case "website":
                        $state.transitionTo("main.report.website.zone", params);
                        break;
                    case "item":
                        $state.transitionTo("main.report.item.linkedsite", params);
                        break;
                    case "zone":
                        $state.transitionTo("main.report.zone.linkedcamp", params);
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

    /*
     *  --------------- Report Controller ------------------------
     */
    export class ReportTabController extends PermissionController {
        constructor($scope: scopes.IReportTabScope, $location, $stateParams,
            $state, CurrentTab: common.CurrentTab, BodyClass, $timeout, filterIdsService, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.getPermission(utils.PermissionUtils.WEBSITE, 0);

            $scope.numRecord = 10;
            $scope.websites = [];
            $scope.orders = [];
            $scope.orderSubCollapse = false;
            $scope.websiteSubCollapse = true;

            $scope.orderTotal = 0; $scope.websiteTotal = 0;
            $scope.orderFilter = { name: "" };
            $scope.siteFilter = { name: "" }

                $scope.isPublisherUser = true;
            $scope.isAdvertiserUser = true;
            websiteList();
            orderList();

            function websiteList() {
                factory.websiteService.listAllItem(function (returnedList) {
                    $scope.websites = returnedList;
                });
            }
            function orderList() {
                factory.orderService.listAllItem(function (returnedList) {
                    $scope.orders = returnedList
                    });
            }

            // set Body Class
            BodyClass.setClass('');
            if ($location.path().indexOf("order") != -1) {
                $scope.orderSubCollapse = false;
                $scope.websiteSubCollapse = true;
            }
            if ($location.path().indexOf("website") != -1) {
                $scope.websiteSubCollapse = false;
                $scope.orderSubCollapse = true;
            }
            // ---------------------------- function -------------------
            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if (key !== "filter" && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                $scope.numRecord = 10;
                if (tabName === 'list_site') {
                    $state.transitionTo('main.report.websites', params);
                    if ($scope.websiteSubCollapse) {
                        $scope.websiteSubCollapse = !$scope.websiteSubCollapse;
                    }
                    $scope.orderSubCollapse = true;
                    $scope.orderFilter = { name: "" };
                    return;
                }
                if (tabName === 'list_order') {
                    $state.transitionTo('main.report.orders', params);
                    if ($scope.orderSubCollapse) {
                        $scope.orderSubCollapse = !$scope.orderSubCollapse;
                    }
                    $scope.websiteSubCollapse = true;
                    $scope.siteFilter = { name: "" };
                    $scope.numRecord = 10;
                    return;
                }
                if (tabName === "report_zingtv") {
                    $state.transitionTo("main.report.zingtv");
                    $scope.websiteSubCollapse = true;
                    $scope.orderSubCollapse = true;
                    $scope.orderFilter = { name: "" }
                    $scope.siteFilter = { name: "" };
                }
            };

            $scope.goOrderReport = (orderId: number) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if (factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if (orderId) params["orderId"] = orderId;

                $state.transitionTo('main.report.order.campaign', params);
                $scope.websiteSubCollapse = true;
            };

            $scope.goWebsiteReport = (websiteId: number) => {

                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if (key !== "filter" && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if (websiteId) params["websiteId"] = websiteId;

                $state.transitionTo('main.report.website.zone', params);
                $scope.orderSubCollapse = true;
            };

            $scope.goMore = (type: string) => {
                $scope.numRecord += 10;
            }

            $scope.isActiveTab = (tabName: string): string => {
                var currTab: common.Tab = CurrentTab.getTab();
                if (currTab === null || currTab === undefined)
                    return "";
                if (tabName === "list_order" && currTab.tabChildName.indexOf("order") !== -1) {
                    $scope.websiteSubCollapse = true;
                    $scope.orderSubCollapse = false;
                    return "active";
                }
                if (tabName === "list_site" && currTab.tabChildName.indexOf("site") !== -1) {
                    $scope.websiteSubCollapse = false;
                    $scope.orderSubCollapse = true;
                    return "active";
                }
                if (tabName === "report_zingtv" && currTab.tabChildName === tabName) {
                    $scope.websiteSubCollapse = true;
                    $scope.orderSubCollapse = true;
                    return "active";
                }
                return "";
            };

            $scope.isActiveOrderTab = (orderId: number): string => {
                var tab: common.Tab = CurrentTab.getTab();
                var menu_name: string = "order_" + orderId.toString();
                if (menu_name === tab.tabChildName)
                    return "active2";
                return "";
            };

            $scope.isActiveWebsiteTab = (websiteId: number): string => {
                var tab: common.Tab = CurrentTab.getTab();
                var menu_name: string = "site_" + websiteId.toString();
                if (menu_name === tab.tabChildName)
                    return "active2";
                return "";
            };

            $scope.isShowOrderMenu = (): boolean => {
                return $scope.isAdvertiserUser;
            };

            $scope.isShowWebsiteMenu = (): boolean => {
                return $scope.isPublisherUser;
            };

            $scope.showMore = (type: string): boolean => {
                if (type === 'order') {
                    return $scope.isAdvertiserUser && $scope.orderTotal > $scope.numRecord;
                }
                if (type === 'website') {
                    return $scope.isPublisherUser && $scope.websiteTotal > $scope.numRecord;
                }
            };

            $scope.toggleSubRecord = (type: string, event: any) => {
                event.stopPropagation();
                if (type === 'order') {
                    $scope.orderSubCollapse = !$scope.orderSubCollapse;
                }
                if (type === 'website') {
                    $scope.websiteSubCollapse = !$scope.websiteSubCollapse;
                }
            };

            $scope.getClass = (type: string): string => {
                if (type === 'order') {
                    if ($scope.orderSubCollapse)
                        return "icon-chevron-right";
                    return "icon-chevron-down";
                }
                if (type === 'website') {
                    if ($scope.websiteSubCollapse)
                        return "icon-chevron-right";
                    return "icon-chevron-down";
                }
            };

            $scope.filterName = (type, event) => {
                if (event.keyCode === 13) {
                    var ids = [];
                    if (type === 'order') {
                        $scope.orders.filter((a, _) => a.name.toLowerCase().indexOf($scope.orderFilter.name) !== -1)
                            .forEach((b, __) => ids.push(b.id));
                        $state.transitionTo("main.report.orders");
                    }
                    else if (type === 'website') {
                        $scope.websites.filter((a, _) => a.name.toLowerCase().indexOf($scope.siteFilter.name) !== -1)
                            .forEach((b, __) => ids.push(b.id));
                        $state.transitionTo("main.report.websites");
                    }
                    $timeout(() => filterIdsService.addIds(ids), 100);
                }
            }
        }
    }

    export class ReportController {
        constructor($scope: scopes.IReportScope, $state, $stateParams, permissionUtils: utils.PermissionUtils, factory: backend.Factory) {
            var userInfo = factory.userInfo.getUserInfo();
            if (userInfo !== null) {
                if (userInfo.isAdvertiser())
                    $state.transitionTo('main.report.orders');
                else if (userInfo.isPublisher())
                    $state.transitionTo('main.report.websites');
            }
        }
    }

    export class ReportBaseController extends PermissionController {
        constructor($scope: scopes.IReportScope, $state, $stateParams, CurrentTab: common.CurrentTab,
            $location, factory: backend.Factory) {
            super($scope, $state, factory);
            $scope.isLoading = false;
            CurrentTab.setTab(new common.Tab("report", "report_orders"));
            $scope.data = new models.LineChart([], []);
            $scope.pageIndex = 1;
            $scope.pageSize = 10;
            $scope.total = 0;
            $scope.report = new models.ReportFE(new models.TimeRange(0, 0), null, 0, [], new models.SummaryReportFE([], {}));
            $scope.isShowChart = true;

            $scope.colNames = []; $scope.check = {};
            $scope.datepicker = { "startDate": new Date(), "endDate": new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate(), 23, 59, 59), "startCompareDate": new Date(), "endCompareDate": new Date() };
            $scope.check['today'] = false; $scope.check['yesterday'] = false; $scope.check['thisweek'] = false; $scope.check['lastweek'] = false;
            $scope.check['thismonth'] = false; $scope.check['lastmonth'] = false; $scope.check['last7days'] = false;
            $scope.check['alltime'] = false; $scope.check['custom'] = false; $scope.check['isCompare'] = false;
            $scope.check['compare'] = '';

            $scope.currentTimeOption = 'today';
            $scope.dateFrom = '';
            $scope.dateTo = '';
            $scope.compareDateFrom = '';
            $scope.compareDateTo = '';
            $scope.csvFileHeader = ',,,,';
            $scope.graphType = 'Impression';
            $scope.resolution = 'hour'; $scope.isGetChartData = false;
            $scope.xvalues = [];
            $scope.isChartPie = false; $scope.pies = {}; $scope.dataPie = new models.PieChart([]);

            $scope.timeOption = 'Today';
            $scope.isOpenMenu = false;
            $scope.disableResolution = {}; $scope.dataChart = []; $scope.hasChartValue = false; $scope.isMinimize = false;
            $scope.disableResolution['hour'] = false; $scope.disableResolution['day'] = false; $scope.disableResolution['week'] = false; $scope.disableResolution['month'] = false;
            factory.paramURLStore.dataStore = {};
            $scope.searchItems = [];

            $scope.sortField = {};
            $scope.settingParams = {};
            $scope.paging = { startFrom: 1, limitItem: 10 };
            //$scope.limitItem = 10;
            //$scope.startFrom = 1;

            $scope.filterItems = [];

            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null
                && $stateParams.type !== undefined && $stateParams.type !== null) {
                var from = $stateParams.from, to = $stateParams.to, type = $stateParams.type;
                $scope.check[type] = true;
                $scope.currentTimeOption = type;
                switch (type) {
                    case "today": $scope.timeOption = "Today"; break;
                    case "yesterday": $scope.timeOption = "Yesterday"; break;
                    case "thisweek": $scope.timeOption = "This Week"; break;
                    case "lastweek": $scope.timeOption = "Last Week"; break;
                    case "thismonth": $scope.timeOption = "This Month"; break;
                    case "lastmonth": $scope.timeOption = "Last Month"; break;
                    case "last7days": $scope.timeOption = "Last 7 Days"; break;
                    case "alltime": $scope.timeOption = "All Time"; break;
                }
                if (type == "custom") {
                    $scope.datepicker['startDate'] = new Date(parseInt(from) - new Date().getTimezoneOffset() * 60000);
                    $scope.datepicker['endDate'] = new Date(parseInt(to) - new Date().getTimezoneOffset() * 60000 - 86400000);
                }

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null
                    && $stateParams.ctype !== undefined && $stateParams.ctype !== null) {
                    $scope.check["isCompare"] = true;
                    $scope.check["compare"] = $stateParams.ctype;
                    var cfrom = $stateParams.cfrom, cto = $stateParams.cto;
                    if ($stateParams.ctype === "custom") {
                        $scope.datepicker['startCompareDate'] = new Date(parseInt(cfrom));
                        $scope.datepicker['endCompareDate'] = new Date(parseInt(cto) - 86400000);
                    }
                }
                else {
                    $scope.check["isCompare"] = false;
                }
                if ($stateParams.resolution) {
                    $scope.resolution = $stateParams.resolution;
                }
            }
            else
                $scope.check["today"] = true;

            if ($stateParams.kind) {
                if ($stateParams.kind === "ctr")
                    $scope.graphType = $stateParams.kind.toUpperCase();
                else
                    $scope.graphType = utils.StringUtils.firstCaseLetter($stateParams.kind);
            }
            if ($stateParams.resolution)
                $scope.resolution = $stateParams.resolution;
            //if ($stateParams.filter) {
            //    $scope.filterCampaign = $stateParams.filter;

            //    switch ($scope.filterCampaign) {
            //        case "all": $scope.filterOption = "All campaign"; break;
            //        case "booking": $scope.filterOption = "Booking campaign"; break;
            //        case "networkads": $scope.filterOption = "Network Ads campaign"; break;
            //        case "networktvc": $scope.filterOption = "Network TVC campaign"; break;
            //    }
            //}

            //load search items
            $scope.searchItems = [];
            if ($scope.reportType === 'orders') {
                factory.orderService.listAllItem(function (orders) {
                    $scope.searchItems = orders;
                });
            } else if ($scope.reportType === 'websites') {
                factory.websiteService.listAllItem(function (websites) {
                    $scope.searchItems = websites;
                });
            }
            $scope.filterItemList = [];
            var timer = setInterval(function () {
                if (!!$scope.searchItems && $scope.searchItems.length !== 0) {
                    $scope.filterItemList = $scope.searchItems;
                    clearInterval(timer);
                } else
                    timer;
            }, 100);

            //get filter by id from param
            if ($stateParams.filter !== null && $stateParams.filter !== '') {
                var ids = $stateParams.filter.split(',');
                if (ids.length !== 0 && $scope.currentService)
                    $scope.currentService.listByIds(ids, ret => {
                        if (ret)
                            $scope.filterItems = ret;
                    });
            }
            // ---------------------------------- function -----------------------------------
            $scope.$watch("paging.startFrom", function (_new, _old) {
                if (_new.toString().search(/[^0-9]/g) !== -1)
                    $scope.paging.startFrom = parseInt(_old);
                else if (typeof (_new) == "string")
                    $scope.paging.startFrom = parseInt(_new);
                if (_new !== undefined && _new !== null) {
                    if (_new < 1 || _new > $scope.getPageNum())
                        $scope.paging.startFrom = parseInt(_old);
                }
            })
            $scope.getPageNum = () => Math.ceil($scope.report.items.length / $scope.paging.limitItem);
            $scope.getEndIndexCurPage = () => ($scope.paging.startFrom * $scope.paging.limitItem < $scope.report.items.length ? $scope.paging.startFrom * $scope.paging.limitItem : $scope.report.items.length);

            $scope.checkDisable = (start: number, type: string) => {
                if (type === "next" && start >= $scope.getPageNum() || type === "prev" && start <= 1) {
                    return "disabled";
                }
                return "active";
            }
            $scope.next = () => {
                if ($scope.paging.startFrom + 1 > $scope.getPageNum()) return;
                $scope.paging.startFrom++;
            }
            $scope.prev = () => {
                if ($scope.paging.startFrom - 1 < 1) return;
                $scope.paging.startFrom--;
            }
            $scope.chooseLimit = (limit: number) => {
                $scope.paging.limitItem = limit;
                $scope.paging.startFrom = 1;
            }

            $scope.getTimeReport = (): models.TimeRange => {
                return utils.DateUtils.getToday();
            };

            $scope.isConversion = (): boolean => {
                if ($scope.graphType && $scope.graphType.toLowerCase() === 'conversion')
                    return true;
                return false;
            };
            $scope.isConversionDetail = (): boolean=> {
                return ($scope.reportType || "").indexOf("conversion_detail") !== -1;
            }


            $scope.getSortClass = (type: string): string => {
                if ($scope.sortField[type] === undefined || $scope.sortField[type] === null)
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;

                if ($scope.sortField[type] === common.SortDefinition.DEFAULT)
                    return "header";
                if ($scope.sortField[type] === common.SortDefinition.UP)
                    return "headerSortUp";
                if ($scope.sortField[type] === common.SortDefinition.DOWN)
                    return "headerSortDown";
                return "";
            };

            $scope.switchSortGraph = (type: string) => {
                var kind: string = type.replace('_graph', '');
                if ($scope.sortField[type] === undefined || $scope.sortField[type] === null)
                    $scope.sortField[type] = common.SortDefinition.DEFAULT;
                if (type.indexOf("_conv") !== -1) {
                    var UP = 1;
                    kind = type.replace("_conv", "");
                    var field = "conversion";
                    if (type.indexOf("_count") !== -1) {
                        field = "conversion";
                        kind = kind.replace("_count", "");
                    }
                    else if (type.indexOf("_value") !== -1) {
                        field = "value";
                        kind = kind.replace("_value", "");
                    }
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        UP = -1;
                    } else if ($scope.sortField[type] === common.SortDefinition.UP)
                        $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN)
                        $scope.sortField[type] = common.SortDefinition.UP;
                    $scope.conversionDetails.sort((a, b) => {
                        var ret = 0;
                        a.item.forEach((cva, _) => {
                            if (cva.label === kind) {
                                b.item.forEach((cvb, __) => {
                                    if (cvb.label === kind)
                                        ret = cvb[field] - cva[field];
                                });
                            }
                        });
                        return ret * UP;
                    });
                }
                else {
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        $scope.dataChart.sort(function (a, b) {
                            if (a.getValue(kind) > b.getValue(kind))
                                return -1;
                            if (a.getValue(kind) < b.getValue(kind))
                                return 1;
                            return 0;
                        });
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.UP) {
                        $scope.sortField[type] = common.SortDefinition.DEFAULT
                    $scope.dataChart.sort(function (a, b) {
                            if (a.date < b.date)
                                return -1;
                            if (a.date > b.date)
                                return 1;
                            return 0;
                        });
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                        $scope.sortField[type] = common.SortDefinition.UP;
                        $scope.dataChart.sort(function (a, b) {
                            if (a.getValue(kind) < b.getValue(kind))
                                return -1;
                            if (a.getValue(kind) > b.getValue(kind))
                                return 1;
                            return 0;
                        });
                    }
                }
                for (var aType in $scope.sortField)
                    if (type != aType)
                        $scope.sortField[aType] = common.SortDefinition.DEFAULT;
            };

            $scope.switchSort = (type: string) => {

                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;

                    if (type === "orderName_linkedCamp") {
                        $scope.report.items.sort((a, b) => {
                            return (a["orderName"] < b["orderName"] ? 1 : (a["orderName"] > b["orderName"] ? -1 : 0));
                        });
                        $scope.orderNames = $scope.report.items.map((it, _) => it["orderName"]);
                    } else {
                        $scope.report.items.sort(function (a, b) {
                            if (type === 'name') {
                                return (a.name.toLowerCase() < b.name.toLowerCase() ? 1 : (a.name.toLowerCase() > b.name.toLowerCase() ? -1 : 0));
                            } else {
                                if (a.values[type].value > b.values[type].value)
                                    return -1;
                                if (a.values[type].value < b.values[type].value)
                                    return 1;
                                return 0;
                            }
                        });
                    }
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                    if (type === "orderName_linkedCamp") {
                        $scope.report.items.sort((a, b) => {
                            return (a["orderName"] < b["orderName"] ? 1 : (a["orderName"] > b["orderName"] ? -1 : 0));
                        });
                        $scope.orderNames = $scope.report.items.map((it, _) => it["orderName"]);
                    } else {
                        $scope.report.items.sort(function (a, b) {
                            if (a.name.toLowerCase() < b.name.toLowerCase())
                                return -1;
                            if (a.name.toLowerCase() > b.name.toLowerCase())
                                return 1;
                            return 0;
                        });
                    }
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    if (type === "orderName_linkedCamp") {
                        $scope.report.items.sort((a, b) => {
                            return (a["orderName"] < b["orderName"] ? -1 : (a["orderName"] > b["orderName"] ? 1 : 0));
                        });
                        $scope.orderNames = $scope.report.items.map((it, _) => it["orderName"]);
                    } else {
                        $scope.report.items.sort(function (a, b) {
                            if (type === 'name') {
                                return (a.name.toLowerCase() < b.name.toLowerCase() ? -1 : (a.name.toLowerCase() > b.name.toLowerCase() ? 1 : 0));
                            } else {
                                if (a.values[type].value < b.values[type].value)
                                    return -1;
                                if (a.values[type].value > b.values[type].value)
                                    return 1;
                                return 0;
                            }
                        });
                    }
                }
                for (var aType in $scope.sortField)
                    if (type != aType)
                        $scope.sortField[aType] = common.SortDefinition.DEFAULT;
            };

            $scope.queryReport = (range: models.TimeRange, compareRange: models.TimeRange) => {
                $scope.isCompareReport = true;
                if (compareRange.from === 0 || compareRange.to === 0) {
                    compareRange = null;
                    $scope.isCompareReport = false;
                }
                var params: {} = { from: range.from, to: range.to };
                Object.keys($scope.check).forEach((key, index) => { if (key !== "isCompare" && key !== "compare" && $scope.check[key] == true) { params["type"] = key; return }; });
                if (compareRange !== null) {
                    params["cfrom"] = compareRange.from;
                    params['cto'] = compareRange.to;
                    params["ctype"] = $scope.check["compare"];
                }

                params["resolution"] = $scope.resolution;
                params["kind"] = $scope.graphType.toLowerCase();
                if ($scope.filterItems)
                    params['filter'] = $scope.filterItems.map(i => i.id).toString();
                $location.search(params);
                if ($location.$$search === params)
                    $scope.isLoading = false;
            };

            $scope.toggleMinimize = () => {
                $scope.isMinimize = !$scope.isMinimize;
            };

            $scope.isMinimizeChartData = (): boolean => {
                if ($scope.hasChartValue && $scope.isMinimize)
                    return true;
                return false;
            };

            $scope.isMaximizeChartData = (): boolean => {
                if ($scope.hasChartValue && !$scope.isMinimize)
                    return true;
                return false;
            };

            $scope.$watch('datepicker.startDate', function (newVal, oldVal) {
                if ($scope.check['custom'])
                    $scope.timeOption = moment.unix(Math.round(($scope.datepicker['startDate'].getTime()) / 1000)).format('DD/MM/YYYY')
                    + ' - ' + moment.unix(Math.floor(($scope.datepicker['endDate'].getTime()) / 1000)).format('DD/MM/YYYY');
            });

            $scope.$watch('datepicker.endDate', function (newVal, oldVal) {
                if ($scope.check['custom'])
                    $scope.timeOption = moment.unix(Math.round(($scope.datepicker['startDate'].getTime()) / 1000)).format('DD/MM/YYYY')
                    + ' - ' + moment.unix(Math.floor(($scope.datepicker['endDate'].getTime()) / 1000)).format('DD/MM/YYYY');
            });


            $scope.toggleOpen = () => {
                $scope.isOpenMenu = !$scope.isOpenMenu;
            };

            $scope.getOpenClass = (): string => {
                if ($scope.isOpenMenu)
                    return "open";
                return "";
            };
            $scope.toggeOpenColumn = () => { $scope.isFilterColumn = !$scope.isFilterColumn; };
            $scope.showColumnOpenClass = (): string => {
                if ($scope.isFilterColumn)
                    return "open";
                return "";
            }

            $scope.enableCompare = () => {
                //$scope.check['isCompare'] = !$scope.check['isCompare'];
                if ($scope.check['isCompare'])
                    $scope.check['compare'] = "previous";
            };

            $scope.showHideChart = () => {
                $scope.isShowChart = !$scope.isShowChart;
            };
            $scope.click = (e) => {
                if (!jQuery(e.target).is("#query"))
                    e.stopPropagation();
            };

            $scope.chose = (option: string, e) => {
                e.stopPropagation();
                if (option === 'compare')
                    return;

                if (option === "selectCol") {
                    var numOfSecCol = 0;
                    $scope.isWarningFilterColumn = false;
                    $scope.colNames.forEach((key, i) => {
                        if ($scope.selectedCol[key] == true)
                            numOfSecCol++;
                        $("#col_" + key).removeAttr("disabled");
                        $("#li_" + key).removeClass("unavailable");
                    });
                    if (numOfSecCol >= 6) {
                        $scope.isWarningFilterColumn = true;
                        $scope.colNames.forEach((col, i) => {
                            if ($scope.selectedCol[col] == false || $scope.selectedCol[col] === undefined) {
                                $("#col_" + col).attr("disabled", "");
                                $("#li_" + col).addClass("unavailable");
                            }
                        });
                    }
                    var type = ($scope.graphType.toLowerCase() === "conversion" ? "conversion" : "general");
                    var localData = {}; localData[type] = $scope.selectedCol;
                    common.LocalStorageUtils.store("selectedCol", localData);
                    return;
                }



                if ($scope.currentTimeOption.length > 0)
                    $scope.check[$scope.currentTimeOption] = false;
                if (option === 'datepicker') {
                    $scope.check['custom'] = true;
                    //$scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
                    $scope.timeOption = moment.unix(Math.round(($scope.datepicker['startDate'].getTime()) / 1000)).format('DD/MM/YYYY')
                    + ' - ' + moment.unix(Math.floor(($scope.datepicker['endDate'].getTime()) / 1000)).format('DD/MM/YYYY');
                    $scope.currentTimeOption = 'custom';
                    return;
                }
                $scope.check[option] = true;
                if (option === 'today')
                    $scope.timeOption = 'Today';
                else if (option === 'yesterday')
                    $scope.timeOption = 'Yesterday';
                else if (option === 'thisweek')
                    $scope.timeOption = 'This Week';
                else if (option === 'lastweek')
                    $scope.timeOption = 'Last Week';
                else if (option === 'thismonth')
                    $scope.timeOption = 'This Month';
                else if (option === 'lastmonth')
                    $scope.timeOption = 'Last Month';
                else if (option === 'last7days')
                    $scope.timeOption = 'Last 7 days';
                else if (option === 'alltime')
                    $scope.timeOption = 'All Time';
                else if (option === 'custom')
                    $scope.timeOption = moment.unix(Math.round($scope.datepicker['startDate'].getTime() / 1000)).format('DD/MM/YYYY') + ' - ' + moment.unix(Math.round($scope.datepicker['endDate'].getTime() / 1000)).format('DD/MM/YYYY');
                $scope.currentTimeOption = option;
            };

            //$scope.chooseFilter = (filterType: string) => {
            //    $scope.filterCampaign = filterType;
            //    factory.paramURLStore.store("filter", filterType);
            //    $location.search(factory.paramURLStore.dataStore);
            //};

            $scope.getABS = (num: number): number => {
                return Math.abs(num);
            };

            $scope.isCompare = (): boolean => {
                if ($scope.report.compareRange !== undefined || $scope.report.compareRange === null)
                    return true;
                return false;
            };

            $scope.formatDateTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MM - YYYY');
            };

            $scope.formatTime = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD - MMM - YYYY   HH:mm:ss');
            };

            $scope.formatNumber = (num: number): string => {
                if (num !== undefined && num !== null)
                    return common.StringUtils.formatNumber(parseFloat(num.toFixed(3)));
                return "";
            };

            $scope.formatCTR = (num: number): string => {
                if (num !== undefined && num !== null)
                    return parseFloat(num.toFixed(6)).toString();
                return "";
            };

            $scope.getDirection = (change: number): string => {
                if (change > 0)
                    return "arrowup";
                if (change < 0)
                    return "arrowdown";
                return "arrow-none";
            };

            $scope.query = () => {
                $scope.isOpenMenu = false;
                var range: models.TimeRange = new models.TimeRange(0, 0); // all time
                if ($scope.check['today']) {
                    var today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
                    range = new models.TimeRange(today.getTime(), new Date().getTime());
                } else if ($scope.check['yesterday']) {
                    range = utils.DateUtils.getYesterday();
                } else if ($scope.check['thisweek']) {
                    range = utils.DateUtils.getCurrentWeek();
                } else if ($scope.check['lastweek']) {
                    range = utils.DateUtils.getLastWeek();
                } else if ($scope.check['thismonth']) {
                    range = utils.DateUtils.getCurrentMonth();
                } else if ($scope.check['lastmonth']) {
                    range = utils.DateUtils.getLastMonth();
                } else if ($scope.check['last7days']) {
                    range = utils.DateUtils.getLast7Days(new Date());
                } else if ($scope.check['custom']) {
                    var startDate: Date = new Date($scope.datepicker['startDate'].getTime());
                    startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
                    var endDate: Date = new Date($scope.datepicker['endDate'].getTime());
                    endDate = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());

                    range = new models.TimeRange(startDate.getTime(), endDate.getTime() + 86400000);
                }
                // compare
                var compareRange: models.TimeRange = new models.TimeRange(0, 0);
                if ($scope.check['isCompare']) {
                    if ($scope.check['compare'] === 'previous') {
                        if ($scope.check['today']) {
                            var yesterday = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() - 1);

                            compareRange = new models.TimeRange(yesterday.getTime(), today.getTime());
                        } else if ($scope.check['yesterday']) {
                            var yesterday = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() - 1);
                            var two_day_ago: Date = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() - 2);
                            compareRange = new models.TimeRange(two_day_ago.getTime(), yesterday.getTime());
                        } else if ($scope.check['thisweek']) {
                            compareRange = utils.DateUtils.getLastWeek();
                        } else if ($scope.check['lastweek']) {
                            var dayLastWeek = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() - 7);
                            compareRange = utils.DateUtils.getPreviousWeek(dayLastWeek);
                        } else if ($scope.check['thismonth']) {
                            compareRange = utils.DateUtils.getLastMonth();
                        } else if ($scope.check['lastmonth']) {
                            var dayLastMonth = new Date(new Date().getFullYear(), new Date().getMonth() - 1, new Date().getDate());
                            compareRange = utils.DateUtils.getPreviousMonth(dayLastMonth);
                        } else if ($scope.check['last7days']) {
                            var currentDate = new Date();
                            var previous7Date = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - 7);
                            var previous14Date = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() - 14);
                            compareRange = new models.TimeRange(previous14Date.getTime(), previous7Date.getTime());
                        } else if ($scope.check['custom']) {
                            compareRange = utils.DateUtils.getPreviousTimeRange($scope.datepicker['startDate'], $scope.datepicker['endDate']);
                        }
                    } else if ($scope.check['compare'] === 'previous-year') {
                        if ($scope.check['today']) {
                            var dayLastYear = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate());
                            compareRange = new models.TimeRange(dayLastYear.getTime(), dayLastYear.getTime() + 86400000);
                        } else if ($scope.check['yesterday']) {
                            var dayLastYear = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate() - 1);
                            compareRange = new models.TimeRange(dayLastYear.getTime(), dayLastYear.getTime() + 86400000);
                        } else if ($scope.check['thisweek']) {
                            var dayLastYear = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate());
                            compareRange = utils.DateUtils.getWeek(dayLastYear);
                        } else if ($scope.check['lastweek']) {
                            var dayLastWeek = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate() - 7);
                            compareRange = utils.DateUtils.getPreviousWeek(dayLastWeek);
                        } else if ($scope.check['thismonth']) {
                            var dayThisMonthLastYear = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate());
                            compareRange = utils.DateUtils.getMonth(dayThisMonthLastYear);
                        } else if ($scope.check['lastmonth']) {
                            var dayLastMonth = new Date(new Date().getFullYear() - 1, new Date().getMonth() - 1, new Date().getDate());
                            compareRange = utils.DateUtils.getPreviousMonth(dayLastMonth);
                        } else if ($scope.check['last7days']) {
                            var dayLastYear = new Date(new Date().getFullYear() - 1, new Date().getMonth(), new Date().getDate());
                            compareRange = utils.DateUtils.getLast7Days(dayLastYear);
                        } else if ($scope.check['custom']) {
                            compareRange = utils.DateUtils.getPreviousYearTimeRange($scope.datepicker['startDate'], $scope.datepicker['endDate']);
                        }
                    } else if ($scope.check['compare'] === 'custom') {
                        var startDate: Date = new Date($scope.datepicker['startCompareDate'].getTime());
                        startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
                        var endDate: Date = new Date($scope.datepicker['endCompareDate'].getTime());
                        endDate = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
                        compareRange = new models.TimeRange(startDate.getTime(), endDate.getTime() + 86400000);
                    }
                }
                $scope.isLoading = true;
                $scope.queryReport(range, compareRange);
            };

            $scope.changeChart = () => {
                $scope.dataPie = $scope.pies[$scope.graphType.toLowerCase()];
                $scope.isChartPie = !$scope.isChartPie;
                if (!$scope.isChartPie && $scope.isGetChartData) {
                    $scope.getChartData($scope.graphType, $scope.timeRange, $scope.compareTimeRange, $scope.optionQuery);
                    $scope.isGetChartData = false;
                }
            };

            $scope.setDirectiveFn = (theDirFn) => {
                $scope.saveChart = theDirFn;
            };

            $scope.downloadPDF = () => {
                // TODO : not support at this time
                return;
                var timeRange: models.TimeRange = null;
                var compareRange: models.TimeRange = null;
                if (factory.paramURLStore.get("from") !== undefined && factory.paramURLStore.get("from") !== null && factory.paramURLStore.get("to") !== undefined && factory.paramURLStore.get("to") !== null)
                    timeRange = new models.TimeRange(factory.paramURLStore.get("from"), factory.paramURLStore.get("to"));
                if (factory.paramURLStore.get("cfrom") !== undefined && factory.paramURLStore.get("cfrom") !== null && factory.paramURLStore.get("cto") !== undefined && factory.paramURLStore.get("cto") !== null)
                    compareRange = new models.TimeRange(factory.paramURLStore.get("cfrom"), factory.paramURLStore.get("cto"));

                var url = factory.reportService.getPDFURL(timeRange, compareRange, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.optionQuery);
                //window.open(factory.reportService.getPDFURL(timeRange, compareRange, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.optionQuery));
                if (navigator.appName !== "Microsoft Internet Explorer") {
                    var xhr = new XMLHttpRequest();
                    xhr.open("GET", url, true);
                    var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
                    xhr.setRequestHeader('X-sessionId', sessionid);
                    xhr.responseType = "blob";
                    xhr.onload = function (e) {
                        if (this.status === 200) {
                            var pdfFile = new Blob([this.response], { type: "application/pdf" });
                            saveAs(pdfFile, "export.pdf");
                        }
                    }
                xhr.send();
                }
            };

            $scope.exportCSV = () => {
                if ($scope.report === undefined || $scope.report === null || $scope.report.items.length == 0)
                    return;
                var today = new Date();
                var filename: string = 'report_csv_' + common.StringUtils.paddingZero(today.getDate(), 2) + common.StringUtils.paddingZero(today.getMonth(), 2) + today.getFullYear() + '.csv';
                var content: string = $scope.genCSV();
                saveCSV(filename, content);
            };

            $scope.genCSV = (): string => {
                var csv: string = '';
                csv += 'Time,Impression,Click,CTR,\r\n';
                csv += 'Total,' + $scope.totalChartValue['impression'] + ',' + $scope.totalChartValue['click'] + ',' + $scope.totalChartValue['ctr'] + ',\r\n';
                for (var i: number = 0; i < $scope.dataChart.length; i++) {
                    var point: models.Point = $scope.dataChart[i];
                    var time: string = $scope.formatTime(point.date);
                    var impression: string = point.getValue('impression').toString();
                    var click: string = point.getValue('click').toString();
                    var ctr: string = point.getValue('ctr').toFixed(3).toString();
                    csv += time + ',' + impression + ',' + click + ',' + ctr + ',\r\n';
                }
                csv += ',,,,\r\n';
                csv += ',,,,\r\n';
                // csv header title
                var titles: Array<string> = $scope.csvFileHeader.split(",");
                $scope.csvFileHeader = titles[0] + ",";
                $scope.colNames.forEach((key, i) => {
                    if ($scope.selectedCol[key] === true) {
                        $scope.csvFileHeader += $scope.colNamesLabel[i] + ",";
                    }
                });

                if ($scope.isCompareReport) {
                    var summary1: string = 'summary';
                    var summary2: string = 'summary';
                    for (var i: number = 0; i < $scope.colNames.length; i++) {
                        if ($scope.selectedCol[$scope.colNames[i]] === true) {
                            var colName: string = $scope.colNames[i];
                            if ($scope.report.summary.properties[colName]) {
                                summary1 += ',' + $scope.report.summary.properties[colName].change;
                                summary2 += ',' + $scope.report.summary.properties[colName].value + ' vs ' + $scope.report.summary.properties[colName].compareValue;
                            } else {
                                summary1 += ','; summary2 += ',';
                            }
                        }
                    }
                    // items
                    var content = '';
                    for (var i: number = 0; i < $scope.report.items.length; i++) {
                        var item: models.ItemReportFE = $scope.report.items[i];
                        var itemRow = item.name + ',,,';
                        var valueRow = $scope.formatDateTime($scope.report.range.from) + ' to ' + $scope.formatDateTime($scope.report.range.to) + ',';
                        var compareValueRow = $scope.formatDateTime($scope.report.compareRange.from) + ' to ' + $scope.formatDateTime($scope.report.compareRange.to) + ',';
                        var change = 'change,';
                        for (var j: number = 0; j < $scope.colNames.length; j++) {
                            var field: string = $scope.colNames[j];
                            if ($scope.selectedCol[$scope.colNames[i]] === true) {
                                if ($scope.selectedCol[field] === true) {
                                    if (item.values[field].value !== undefined) {
                                        valueRow += item.values[field].value;
                                        compareValueRow += item.values[field].compareValue;
                                        change += item.values[field].change;
                                    } else {
                                        // extra
                                        valueRow += item.values[field];
                                    }
                                    if (j < $scope.colNames.length - 1) {
                                        valueRow += ",";
                                        compareValueRow += ',';
                                        change += ',';
                                    }
                                }
                            }
                        }

                        content += itemRow + '\r\n' + valueRow + '\r\n' + compareValueRow + '\r\n';
                    }
                    csv += $scope.csvFileHeader + '\r\n' + summary1 + '\r\n' + summary2 + '\r\n' + content;
                } else {
                    var summary: string = 'SUMMARY';
                    for (var i: number = 0; i < $scope.colNames.length; i++) {
                        if ($scope.selectedCol[$scope.colNames[i]] === true) {
                            var colName: string = $scope.colNames[i];
                            if ($scope.report.summary.properties[colName])
                                summary += ',' + $scope.report.summary.properties[colName].value;
                            else
                                summary += ',';
                        }
                    }
                    // items
                    var content = '';
                    for (var i: number = 0; i < $scope.report.items.length; i++) {
                        var item: models.ItemReportFE = $scope.report.items[i];
                        var itemRow = item.name + ',';
                        for (var j: number = 0; j < $scope.colNames.length; j++) {
                            var field: string = $scope.colNames[j];
                            if ($scope.selectedCol[field] === true) {
                                if (item.values[field].value !== undefined) {
                                    itemRow += item.values[field].value;
                                } else {
                                    itemRow += item.values[field];
                                }
                                if (j < $scope.colNames.length - 1)
                                    itemRow += ",";
                            }
                        }
                        itemRow += '\r\n';
                        content += itemRow;
                    }
                    csv += $scope.csvFileHeader + '\r\n' + summary + '\r\n' + content;
                }
                return csv;
            };

            $scope.getData = (range: models.TimeRange, compareRange: models.TimeRange, options: Array<any>) => {
                // subtract 
                $scope.isLoading = true;
                if (range.to > 0)
                    range.to = range.to - 1;
                if (compareRange !== undefined && compareRange !== null && compareRange.to > 0)
                    compareRange.to = compareRange.to - 1;

                //Save parameter url
                factory.paramURLStore.store("from", range.from);
                factory.paramURLStore.store("to", range.to);
                factory.paramURLStore.store("kind", $scope.graphType.toLowerCase());
                factory.paramURLStore.store("resolution", $scope.resolution);
                Object.keys($scope.check).forEach((key, index) => {
                    if (key !== "isCompare" && key !== "compare" && $scope.check[key] == true) {
                        factory.paramURLStore.store("type", key); return
                    };
                });
                if (compareRange !== null) {
                    factory.paramURLStore.store("cfrom", compareRange.from);
                    factory.paramURLStore.store('cto', compareRange.to);
                    factory.paramURLStore.store("ctype", $scope.check["compare"]);
                }

                $scope.timeRange = range; $scope.compareTimeRange = compareRange;

                $scope.optionQuery = options;

                var resolutionTime = {
                    hour: 3600000,
                    day: 24 * 3600000,
                    week: 7 * 24 * 3600000,
                    month: 30 * 24 * 3600000
                };
                var timediff = range.to - range.from;
                if ($scope.check["alltime"] === true) {
                    var startDate = new Date(2014, 0, 9, 0, 0, 0).getTime();
                    timediff = (new Date().getTime()) - startDate;
                }
                var resolution = $scope.resolution;
                while (timediff / resolutionTime[resolution] > 500) {
                    timediff /= resolutionTime[resolution];
                    if (resolution == "hour") resolution = "day";
                    else if (resolution == "day") resolution = "week";
                    else if (resolution == "week") resolution = "month";
                }
                $scope.resolution = resolution;
                options.forEach((opt, i) => {
                    if (opt.key === "resolution" && opt.value !== resolution)
                        options[i] = { key: "resolution", value: resolution }
                });
                if ($stateParams.filter)
                    options.push({ key: 'ids', value: $stateParams.filter })

                console.log("[Report] - StartTime : " + new Date().getTime());
                factory.reportService.loadReport(range, compareRange, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, options, function (data) {
                    console.log("[Report] - ReceiveTime : " + new Date().getTime());
                    $scope.disableResolution['hour'] = false;
                    $scope.disableResolution['day'] = false;
                    $scope.disableResolution['week'] = false;
                    $scope.disableResolution['month'] = false;

                    if (data === undefined || data === null)
                        return;

                    var reportResp: models.ReportResponse = data.data;
                    var report: models.Report = models.ReportResponse.convertReport(reportResp);

                    if (report === undefined || report === null)
                        return;
                    $scope.isCompareReport = false;
                    if (compareRange !== null)
                        $scope.isCompareReport = true;

                    if (reportResp.items !== null && $scope.reportType !== 'orders' && $scope.reportType !== 'websites') {
                        $scope.searchItems = [];
                        reportResp.items.forEach((it, _) => {
                            $scope.searchItems.push({ id: it.id, name: it.name });
                        });
                    }
                    $scope.orderNames = [];
                    if (reportResp.items.length > 0 && reportResp.items[0]['orderName'] !== undefined) {
                        $scope.orderNames = reportResp.items.map((it, _) => it["orderName"]);
                    }

                    $scope.colNames = [];
                    $scope.colNamesLabel = [];
                    var type = ($scope.graphType.toLowerCase() === "conversion" ? "conversion" : "general");
                    var localData = common.LocalStorageUtils.get("selectedCol") || {};
                    $scope.selectedCol = localData[type] || {};
                    var cols = ["impression", "click", "ctr", "revenue"];
                    if ($scope.graphType.toLowerCase() === "conversion")
                        cols = ["revenueconversion", "conversion", "costconversion", "conversionrate", "roi"]
                    cols.forEach((col, _) => $scope.selectedCol[col] = true);

                    if (report.items && report.items.length != 0) {
                        if (report.items[0].extraField) {
                            report.items[0].extraField.forEach((o, _) => {
                                $scope.colNames.push(o.label);
                                $scope.selectedCol[o.label] = true;
                            });
                        }
                        if (report.items[0].properties && report.items[0].properties.length != 0) {
                            report.items[0].properties.forEach((p, i) => {
                                if (models.FieldReport.COLUMNNAMELABEL[p.label]) {
                                    $scope.colNames.push(p.label);
                                }
                            });
                        }
                    } else if ($scope.colNamesLabel.length == 0) {
                        cols.forEach((c, _) => {
                            $scope.colNames.push(c);
                            $scope.selectedCol[c] = true;
                        });
                    }
                    $scope.colNames.sort((a, b) => {
                        return models.FieldReport.COLLUMNNAMEORDER.indexOf(a) - models.FieldReport.COLLUMNNAMEORDER.indexOf(b);
                    });
                    $scope.colNames.forEach((col) => $scope.colNamesLabel.push(models.FieldReport.COLUMNNAMELABEL[col]));

                    for (var col in $scope.colNames) {
                        $scope.sortField[$scope.colNames[col]] = common.SortDefinition.DEFAULT;
                    }
                    if (report.graph !== null) {
                        console.log("[Report] - BuildGraphStart : " + new Date().getTime());
                        var nochange_x_ticks: boolean = false

                        if (timediff / resolutionTime.hour > 500) $scope.disableResolution["hour"] = true;
                        if (timediff / resolutionTime.day > 500) $scope.disableResolution["day"] = true;
                        if (timediff / resolutionTime.week > 500) $scope.disableResolution["week"] = true;

                        // chart
                        var dateFromString = moment.unix(Math.round(report.range.from / 1000)).format('DD/MM/YYYY'); // not need add timezone
                        var dateToString = moment.unix(Math.floor((report.range.to) / 1000)).format('DD/MM/YYYY'); // not need add timezone
                        var title: string = "";
                        if (dateFromString === dateToString)
                            title = $scope.graphType + " [" + dateFromString + "]";
                        else
                            title = $scope.graphType + " [" + dateFromString + " - " + dateToString + "]";
                        var compareTitle: string = null;
                        if ($scope.isCompareReport) {
                            dateFromString = moment.unix(Math.round(report.compareRange.from / 1000)).format('DD/MM/YYYY');
                            dateToString = moment.unix(Math.floor(report.compareRange.to / 1000)).format('DD/MM/YYYY');

                            if (dateFromString === dateToString)
                                compareTitle = $scope.graphType + " [" + dateFromString + "]";
                            else
                                compareTitle = $scope.graphType + " [" + dateFromString + ' - ' + dateToString + "]";

                        }
                        $scope.data = models.LineChart.convert(report.graph, title, compareTitle, $scope.graphType.toLowerCase());
                        // store data graph
                        if (report.graph.points.length > 0) {
                            $scope.dataChart = []
                            $scope.hasChartValue = true;
                            for (var i: number = 0; i < report.graph.points.length; i++) {
                                var point: models.Point = report.graph.points[i];
                                $scope.dataChart.push(point);
                            }
                        }

                        //load conversion detail
                        $scope.totalConversionValue = {};
                        $scope.totalConversionCount = {}
                        $scope.conversionDetails = [];
                        if (reportResp.conversion && reportResp.conversion.length > 0) {
                            reportResp.conversion.forEach((c, _) => $scope.conversionDetails.push(c));
                            $scope.conversionDetails.sort((a, b) => a.date - b.date);
                            reportResp.conversion[0].item.forEach((c, _) => {
                                $scope.totalConversionValue[c["label"]] = 0;
                                $scope.totalConversionCount[c["label"]] = 0;
                            });
                        }

                        // calculate total chart value
                        $scope.totalChartValue = {};
                        $scope.totalChartValue['impression'] = 0; $scope.totalChartValue['click'] = 0;
                        $scope.totalChartValue['ctr'] = 0; $scope.totalChartValue['conversion'] = 0;
                        for (var i: number = 0, len: number = reportResp.graph.points.length; i < len; i++) {
                            var pointResp: models.PointResponse = reportResp.graph.points[i];
                            $scope.totalChartValue['impression'] += pointResp.impression;
                            $scope.totalChartValue['click'] += pointResp.click;
                            $scope.totalChartValue['conversion'] += pointResp.conversion;
                        }
                        if (reportResp.conversion) {
                            reportResp.conversion.forEach((cd, _) => {
                                cd.item.forEach((c, __) => {
                                    $scope.totalConversionValue[c.label] += c.value;
                                    $scope.totalConversionCount[c.label] += c.conversion;
                                });
                            });
                        }
                        $scope.totalChartValue['ctr'] = ($scope.totalChartValue['impression'] === 0) ? 0 : $scope.totalChartValue['click'] / $scope.totalChartValue['impression'] * 100;
                        // add x axis values
                        if (!nochange_x_ticks && ($scope.resolution === 'day' || $scope.resolution === 'week' || $scope.resolution === 'month') && report.graph.points !== undefined) {
                            $scope.xvalues = [];
                            for (var i: number = 0; i < report.graph.points.length; i++) {
                                var timestamp: number = report.graph.points[i].date - new Date().getTimezoneOffset() * 60000;
                                $scope.xvalues.push([timestamp, moment.unix(Math.round(timestamp / 1000)).format("MMM DD")]);
                            }
                        } else {
                            $scope.xvalues = [];
                        }
                        console.log("[Report] - BuildGraphEnd : " + new Date().getTime());
                    }

                    for (var i: number = 0; i < $scope.colNames.length; i++) {
                        $scope.pies[$scope.colNames[i]] = new models.PieChart([]);
                    }
                    for (var i: number = 0; i < report.items.length; i++) {
                        var item: models.ItemReport = report.items[i];
                        var itemReport: models.ItemReportFE = models.ItemReportFE.convert(item);

                        for (var j: number = 0; j < $scope.colNames.length; j++) {
                            var pie: models.PieChart = $scope.pies[$scope.colNames[j]];
                            pie.elements.push(new models.PieElement(itemReport.name, itemReport.values[$scope.colNames[j]].value));
                        }
                    }
                    $scope.dataPie = $scope.pies[$scope.graphType.toLowerCase()];
                    $scope.report = models.ReportFE.convert(report);
                    $scope.isLoading = false;
                    $scope.totalRecord = $scope.report.items.length;
                    console.log("[Report] - EndTime : " + new Date().getTime());
                });
            }

            $scope.getChartData = (chartType: string, range: models.TimeRange, compareRange: models.TimeRange, options: Array<any>) => {
                if ($scope.isChartPie) {
                    $scope.isGetChartData = true;
                    $scope.dataPie = $scope.pies[chartType.toLowerCase()];
                    return;
                }

                factory.reportService.loadReport(range, compareRange, ($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, options, function (data) {
                    $scope.disableResolution['hour'] = false;
                    $scope.disableResolution['day'] = false;
                    $scope.disableResolution['week'] = false;
                    $scope.disableResolution['month'] = false;

                    if (data === undefined || data === null)
                        return;

                    var reportResp: models.ReportResponse = data.data;
                    var report: models.Report = models.ReportResponse.convertReport(reportResp);
                    if (report === undefined || report === null)
                        return;
                    $scope.isCompareReport = false;
                    if (compareRange !== null)
                        $scope.isCompareReport = true;

                    if (report.graph !== null) {
                        var nochange_x_ticks: boolean = false

                    if (reportResp.items !== null) {
                            $scope.searchItems = [];
                            reportResp.items.forEach((it, _) => {
                                $scope.searchItems.push({ id: it.id, name: it.name });
                            });
                        }
                        // check resolution
                        var timediff: number = report.range.to - report.range.from;
                        var seconds: number = 3600; var timeunit: number = 1 * seconds;
                        if ($scope.resolution === 'hour') {
                            timeunit = 1 * seconds;
                        } else if ($scope.resolution === 'day') {
                            timeunit = 24 * seconds;
                        } else if ($scope.resolution === 'week') {
                            timeunit = 7 * 24 * seconds;
                        }
                        timeunit = timeunit * 1000;
                        while ($scope.check['alltime'] && $scope.resolution !== 'month' && (timediff / timeunit) >= 500) {
                            if ($scope.resolution === 'hour') {
                                $scope.disableResolution['hour'] = true;
                                $scope.resolution = 'day';
                                timeunit = 24 * seconds * 1000;
                                nochange_x_ticks = true;
                            } else if ($scope.resolution === 'day') {
                                $scope.disableResolution['hour'] = true;
                                $scope.disableResolution['day'] = true;
                                $scope.resolution = 'week';
                                timeunit = 7 * 24 * seconds * 1000;
                                nochange_x_ticks = true;
                            } else if ($scope.resolution === 'week') {
                                $scope.disableResolution['hour'] = true;
                                $scope.disableResolution['day'] = true;
                                $scope.disableResolution['week'] = true;
                                $scope.resolution = 'month';
                                nochange_x_ticks = true;
                            }
                        }

                        // chart
                        var dateFromString: string = moment.unix(Math.round(report.range.from / 1000)).format('DD/MM/YYYY');
                        var dateToString: string = moment.unix(Math.floor(report.range.to / 1000)).format('DD/MM/YYYY');

                        var title: string = '';
                        if (dateFromString === dateToString)
                            title = chartType + " [" + dateFromString + "]";
                        else
                            title = chartType + " [" + dateFromString + ' - ' + dateToString + "]";

                        var compareTitle: string = '';
                        if ($scope.isCompareReport) {
                            dateFromString = moment.unix(Math.round(report.compareRange.from / 1000)).format('DD/MM/YYYY');
                            dateToString = moment.unix(Math.floor(report.compareRange.to / 1000)).format('DD/MM/YYYY');
                            if (dateFromString === dateToString)
                                compareTitle = chartType + " [" + dateFromString + "]";
                            else
                                compareTitle = chartType + " [" + dateFromString + ' - ' + dateToString + "]";
                        }
                        $scope.data = models.LineChart.convert(report.graph, title, compareTitle, $scope.graphType.toLowerCase());
                        // store data graph
                        if (report.graph.points.length > 0) {
                            $scope.dataChart = []
                            $scope.isMinimize = false;
                            $scope.hasChartValue = true;

                            for (var i: number = 0; i < report.graph.points.length; i++) {
                                var point: models.Point = report.graph.points[i];
                                $scope.dataChart.push(point);
                            }
                        }

                        //load conversion details
                        $scope.conversionDetails = [];
                        $scope.totalConversionValue = {};
                        $scope.totalConversionCount = {};
                        if (reportResp.conversion && reportResp.conversion.length > 0) {
                            reportResp.conversion.forEach((c, _) => $scope.conversionDetails.push(c));
                            $scope.conversionDetails.sort((a, b) => a.date - b.date);
                            reportResp.conversion[0].item.forEach((c, _) => {
                                $scope.totalConversionValue[c["label"]] = 0;
                                $scope.totalConversionCount[c["label"]] = 0;
                            });
                        }

                        // calculate total value
                        $scope.totalChartValue['impression'] = 0; $scope.totalChartValue['click'] = 0;
                        $scope.totalChartValue['ctr'] = 0; $scope.totalChartValue['conversion'] = 0;
                        $scope.totalConversionValue = {};
                        for (var i: number = 0, len: number = reportResp.graph.points.length; i < len; i++) {
                            var pointResp: models.PointResponse = reportResp.graph.points[i];
                            $scope.totalChartValue['impression'] += pointResp.impression;
                            $scope.totalChartValue['click'] += pointResp.click;
                            $scope.totalChartValue['conversion'] += pointResp.conversion;
                        }
                        if (reportResp.conversion) {
                            reportResp.conversion.forEach((cd, _) => {
                                cd.item.forEach((c, __) => {
                                    $scope.totalConversionValue[c.label] += c.value;
                                    $scope.totalConversionCount[c.label] += c.conversion;
                                });
                            });
                        }
                        $scope.totalChartValue['ctr'] = ($scope.totalChartValue['impression'] === 0) ? 0 : $scope.totalChartValue['click'] / $scope.totalChartValue['impression'] * 100;
                        // add x axis values
                        if (!nochange_x_ticks && ($scope.resolution === 'day' || $scope.resolution === 'week' || $scope.resolution === 'month')) {
                            $scope.xvalues = [];
                            for (var i: number = 0; i < report.graph.points.length; i++) {
                                var timestamp: number = report.graph.points[i].date - new Date().getTimezoneOffset() * 60000;
                                $scope.xvalues.push([timestamp, moment.unix(Math.round(timestamp / 1000)).format("MMM DD")]);
                            }
                        } else {
                            $scope.xvalues = [];
                        }
                    }
                });
            };

            $scope.changeGraph = (kind: string) => {
                $scope.resolution = 'hour';
                var options: Array<any> = [];
                for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                    var obj: any = $scope.optionQuery[i];
                    if (obj.key === "resolution")
                        obj.value = $scope.resolution.toLowerCase();
                    options.push(obj);
                }
                $scope.optionQuery = options;

                if (kind === 'impression') {
                    if (kind !== $scope.graphType.toLowerCase()) {
                        var options: Array<any> = [];
                        for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                            var obj: any = $scope.optionQuery[i];
                            if (obj.key === "kind")
                                obj.value = "impression";
                            options.push(obj);
                        }
                        factory.paramURLStore.store("kind", "impression");
                    }
                    $scope.graphType = "Impression";
                }

                if (kind === 'click') {
                    if (kind !== $scope.graphType.toLowerCase()) {
                        var options: Array<any> = [];
                        for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                            var obj: any = $scope.optionQuery[i];
                            if (obj.key === "kind")
                                obj.value = "click";
                            options.push(obj);
                        }
                        factory.paramURLStore.store("kind", "click");
                    }
                    $scope.graphType = "Click";
                }

                if (kind === 'ctr') {
                    if (kind !== $scope.graphType.toLowerCase()) {
                        var options: Array<any> = [];
                        for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                            var obj: any = $scope.optionQuery[i];
                            if (obj.key === "kind")
                                obj.value = "ctr";
                            options.push(obj);
                        }
                        factory.paramURLStore.store("kind", "ctr");
                    }
                    $scope.graphType = "CTR";
                }

                if (kind === 'spent') {
                    if (kind !== $scope.graphType.toLowerCase()) {
                        var options: Array<any> = [];
                        for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                            var obj: any = $scope.optionQuery[i];
                            if (obj.key === "kind")
                                obj.value = "spent";
                            options.push(obj);
                        }
                        factory.paramURLStore.store("kind", "spent");
                    }
                    $scope.graphType = "Spent";
                }

                if (kind === 'revenue') {
                    if (kind !== $scope.graphType.toLowerCase()) {
                        var options: Array<any> = [];
                        for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                            var obj: any = $scope.optionQuery[i];
                            if (obj.key === "kind")
                                obj.value = "revenue";
                            options.push(obj);
                        }
                        factory.paramURLStore.store("kind", "revenue");
                    }
                    $scope.graphType = "Revenue";
                }
                var params = factory.paramURLStore.dataStore;
                if ($scope.filterItems)
                    params['filter'] = $scope.filterItems.map(i => i.id).toString();
                $location.search(params);
            }

            $scope.chooseResolution = (resolution: string) => {
                if ($scope.disableResolution[resolution])
                    return;
                if (resolution.toLowerCase() !== $scope.resolution) {
                    var options: Array<any> = [];
                    for (var i: number = 0; i < $scope.optionQuery.length; i++) {
                        var obj: any = $scope.optionQuery[i];
                        if (obj.key === "resolution")
                            obj.value = resolution.toLowerCase();
                        options.push(obj);
                    }
                    $scope.optionQuery = options;
                    var title: string = '';
                    if ($scope.graphType.toLowerCase() === 'impression')
                        title = 'Impression';
                    else if ($scope.graphType.toLowerCase() === 'click')
                        title = 'Click';
                    else if ($scope.graphType.toLowerCase() === 'ctr')
                        title = 'CTR';
                    else if ($scope.graphType.toLowerCase() === 'spent')
                        title = 'Spent';
                    else if ($scope.graphType.toLowerCase() === 'revenue')
                        title = 'Revenue';
                    factory.paramURLStore.store("resolution", resolution);
                    var params = factory.paramURLStore.dataStore;
                    if ($scope.filterItems)
                        params['filter'] = $scope.filterItems.map(i => i.id).toString();
                    $location.search(params);
                    $scope.resolution = resolution.toLowerCase();
                }
            };

            $scope.isResolution = (resolution: string): string => {
                if (resolution.toLowerCase() === $scope.resolution)
                    return "active";
                return "";
            };

            $scope.gotoSetting = (id: number) => {
                $scope.settingParams[$scope.objectIdString] = id;
                switch ($scope.objectIdString) {
                    case "campaignId":
                        if (!$scope.settingParams["orderId"]) {
                            factory.campaignService.load(id, function (camp: models.Campaign) {
                                $scope.settingParams["orderId"] = camp.orderId;
                                $state.transitionTo($scope.settingState, $scope.settingParams);
                                return;
                            });
                            return;
                        }
                        break;
                    case "itemId":
                        if (!$scope.settingParams["campaignId"]) {
                            factory.campaignItemService.load(id, function (item: models.AdItem) {
                                $scope.settingParams["campaignId"] = item.campaignId;
                                if (!$scope.settingParams["orderId"]) {
                                    factory.campaignService.load(item.campaignId, function (camp: models.Campaign) {
                                        $scope.settingParams["orderId"] = camp.orderId;
                                        $state.transitionTo($scope.settingState, $scope.settingParams);
                                        return;
                                    });
                                    return;
                                } else {
                                    $state.transitionTo($scope.settingState, $scope.settingParams);
                                }
                            });
                            return;
                        }
                        break;
                    case "zoneId":
                        if (!$scope.settingParams["websiteId"]) {
                            factory.zoneService.load(id, function (zone: models.Zone) {
                                $scope.settingParams["websiteId"] = zone.siteId;
                                $state.transitionTo($scope.settingState, $scope.settingParams);
                                return;
                            });
                            return;
                        }
                        break;
                }
                $state.transitionTo($scope.settingState, $scope.settingParams);
            }

            $scope.mouseover = (row: number) => {
                $scope.currentSelectedRow = row;
            };

            $scope.mouseleave = () => {
                $scope.currentSelectedRow = -1;
            };
            $scope.upperCaseFirst = (name: string) => {
                return name.charAt(0).toUpperCase() + name.slice(1);
            }
            $scope.chooseSearchItem = (item: { name: string; id: number }) => {
                if ($scope.gotoDetail && typeof ($scope.gotoDetail) === 'function')
                    $scope.gotoDetail(item.id);
            }

            $scope.addFilterItem = (item) => {
                var isContain = false;
                $scope.filterItems.forEach((a, _) => isContain = item.id === a.id);
                if (!isContain)
                    $scope.filterItems.push(item);
            }
            $scope.removeFilterItem = (index) => {
                $scope.filterItems.splice(index, 1);
            }
            $scope.viewReportFilter = () => {
                var ids = [], paramIds, containFilter;
                $scope.filterItems.forEach((item, _) => ids.push(item.id));
                paramIds = JSON.stringify(ids);
                paramIds = paramIds.replace(/([\]\[])/g, "");

                containFilter = false;
                $scope.optionQuery.forEach((opt, index) => {
                    if (opt.key === 'ids') {
                        $scope.optionQuery[index].value = paramIds;
                        containFilter = true;
                    }
                });
                if (!containFilter)
                    $scope.optionQuery.push({ key: "ids", value: paramIds });
                var range: models.TimeRange = $scope.getTimeReport(), compareRange: models.TimeRange = null;
                if (!!$stateParams.from && !!$stateParams.to)
                    range = new models.TimeRange($stateParams.from, $stateParams.to);
                if (!!$stateParams.cfrom && !!$stateParams.cto)
                    compareRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);

                var params: {} = { from: range.from, to: range.to };
                Object.keys($scope.check).forEach((key, index) => { if (key !== "isCompare" && key !== "compare" && $scope.check[key] == true) { params["type"] = key; return }; });
                if (compareRange !== null) {
                    params["cfrom"] = compareRange.from;
                    params['cto'] = compareRange.to;
                    params["ctype"] = $scope.check["compare"];
                }

                params["resolution"] = $scope.resolution;
                params["kind"] = $scope.graphType.toLowerCase();
                if ($scope.filterItems)
                    params['filter'] = $scope.filterItems.map(i => i.id).toString();
                $location.search(params);
            }
            $scope.typeof = (value) => typeof value;
        }

    }

    // --------------------  Order -------------------
    export class ReportTotalOrderDetailController extends ReportBaseController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            CurrentTab.setTab(new common.Tab("report", "list_order"));
            $scope.reportType = "orders";

            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key != "filter" && tabName !== "conversion" || tabName === "conversion" && key !== "kind") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if (tabName === "conversion") {
                    $state.transitionTo("main.report.conversion", params);
                    return;
                }
                if (tabName == "orders") {
                    $state.transitionTo("main.report.orders", params);
                    return;
                }
            }

                $scope.isActiveTab = (tab: string) => {
                if (tab === $scope.currentTab)
                    return "active";
                return "";
            }
        }
    }

    export class ReportOrderController extends ReportTotalOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, filterIdsService, factory: backend.Factory) {
            $scope.title = "Orders";
            $scope.currentTab = "orders";
            $scope.reportType = "orders";
            $scope.currentService = factory.orderService
            super($scope, $state, $stateParams, $location, CurrentTab, factory);

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;
            $scope.settingState = "main.order.detail.setting";
            $scope.settingParams = {};
            $scope.objectIdString = "orderId";
            $scope.currentObjId = null;
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "order" }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });


            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------

            $scope.$on("idsPut", function () {
                var ids = filterIdsService.ids;
                ids = JSON.stringify(ids);
                ids = ids.replace(/([\]\[])/g, "");
                $scope.optionQuery.push({ key: "ids", value: ids });
                $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            });
            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order.campaign', params);
            };
        }
    }

    export class ReportOrderDetailController extends ReportBaseController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            CurrentTab.setTab(new common.Tab("report", "order_" + $stateParams.orderId));
            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.reportType = "order";
            //$scope.colNames = ["impression", "click", "ctr", "spent"];
            // ----------------------------- function --------------------------------
            $scope.isTab = (tab: string): boolean => {
                if (tab === $scope.currentTab)
                    return true;
                return false;
            };

            $scope.isActiveTab = (tab: string): string => {
                if (tab === $scope.currentTab)
                    return 'active';
                return '';
            };

            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" && tabName !== "conversion" || key === "filter" && tabName === "campaign" || tabName === "conversion" && key !== "kind") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.orderId) params["orderId"] = $stateParams.orderId;

                if (tabName === 'campaign') {
                    $state.transitionTo('main.report.order.campaign', params);
                    return;
                }
                if (tabName === 'item') {
                    $state.transitionTo('main.report.order.item', params);
                    return;
                }
                if (tabName === 'linked_website') {
                    $state.transitionTo('main.report.order.linkedsite', params);
                    return;
                }
                if (tabName === 'linked_zone') {
                    $state.transitionTo('main.report.order.linkedzone', params);
                    return;
                }
                if (tabName === "conversion") {
                    $state.transitionTo("main.report.order.conversion", params);
                    return;
                }
            };
        }
    }

    export class ReportOrderCampaignController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Campaign";
            $scope.currentTab = 'campaign';
            $scope.isCompareReport = false;
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;
            //$scope.isFilterCampaign = true;
            $scope.settingState = "main.order.campaign_detail.setting";
            $scope.settingParams = { orderId: $stateParams.orderId };
            $scope.objectIdString = "campaignId";
            $scope.currentObjId = $stateParams.orderId

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }


            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "campaign_by_order" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: $stateParams.orderId, campaignId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign.item', params);
            };
        }
    }

    export class ReportOrderItemController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            CurrentTab.setTab(new common.Tab("report", $stateParams.orderId));
            $scope.currentTab = 'item';
            $scope.title = "Item";
            $scope.isCompareReport = true;
            $scope.currentService = factory.campaignItemService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId };
            $scope.currentObjId = $stateParams.campaignId

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "item_by_order" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];

            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);


            // @Override
            $scope.gotoDetail = (id: number) => {
                factory.campaignItemService.load(id, function (item: models.AdItem) {
                    if (item && item.campaignId > 0) {
                        var params: {} = { orderId: $stateParams.orderId, campaignId: item.campaignId, itemId: id };
                        Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                            params[key] = factory.paramURLStore.get(key);
                        });
                        if ($stateParams.from) params["from"] = $stateParams.from;
                        if ($stateParams.to) params["to"] = $stateParams.to;
                        if ($stateParams.type) params["type"] = $stateParams.type;
                        if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                        if ($stateParams.cto) params["cto"] = $stateParams.cto;
                        if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                        $state.transitionTo('main.report.item.linkedsite', params);
                    }
                });
            };
        }
    }

    export class ReportOrderLinkedZoneController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone";
            $scope.isCompareReport = true;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };
            $scope.currentObjId = $stateParams.websiteId

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //            $scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "zone_by_order" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    export class ReportOrderLinkedSiteController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_website';
            $scope.title = "Linked Website";
            $scope.isCompareReport = true;
            $scope.currentService = factory.websiteService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};
            $scope.currentObjId = $stateParams.orderId

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "website_by_order" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);

            $scope.gotoDetail = (websiteId: number) => {
                var params: {} = { orderId: $stateParams.orderId, websiteId: websiteId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order_linkedzonebysite', params);
            };
        }
    }

    export class ReportOrderLinkedZoneBySiteController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone by Site";
            $scope.isCompareReport = true;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "order_zone_by_site";

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };
            $scope.currentObjId = $stateParams.websiteId

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "zone_of_site_by_order" }, { key: "orderId", value: $stateParams.orderId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    // --------------------  Campaign -------------------
    export class ReportCampaignDetailController extends ReportBaseController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            CurrentTab.setTab(new common.Tab("report", "order_" + $stateParams.orderId));
            $scope.csvFileHeader = 'Campaign,Impression,Click,CTR,Spent';
            $scope.reportType = "campaign";
            //$scope.colNames = ["impression", "click", "ctr", "spent"];
            $scope.isTab = (tab: string): boolean => {
                if (tab === $scope.currentTab)
                    return true;
                return false;
            };

            $scope.isActiveTab = (tab: string): string => {
                if (tab === $scope.currentTab)
                    return 'active';
                return '';
            };

            $scope.goTab = (tabName: string) => {

                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" && tabName !== "conversion" || tabName === "conversion" && key !== "kind") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.orderId) params["orderId"] = $stateParams.orderId;
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;
                if ($stateParams.campaignId) params["campaignId"] = $stateParams.campaignId;

                if (tabName === 'item') {
                    $state.transitionTo('main.report.campaign.item', params);
                    return;
                }
                if (tabName === 'linked_website') {
                    $state.transitionTo('main.report.campaign.linkedsite', params);
                    return;
                }
                if (tabName === 'linked_zone') {
                    $state.transitionTo('main.report.campaign.linkedzone', params);
                    return;
                }
                if (tabName === "conversion") {
                    $state.transitionTo("main.report.campaign.conversion", params);
                    return;
                }
            };
        }
    }

    export class ReportCampaignItemController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignItemService;
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Item";
            CurrentTab.setTab(new common.Tab("list_order", "order_" + $stateParams.orderId));
            $scope.currentTab = 'item';
            $scope.isCompareReport = false;

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;
            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId };
            $scope.currentObjId = $stateParams.campaignId

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "item_by_campaign" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                factory.campaignItemService.load(id, (itemRet: models.AdItem) => {
                    if (itemRet) {
                        if (itemRet.kind === models.CampaignItemType.NETWORK.PRBANNER || itemRet.kind === models.CampaignItemType.BOOKING.PRBANNER)
                            $state.transitionTo('main.report.item.pr_item', params);
                        else
                            $state.transitionTo('main.report.item.linkedsite', params);
                    }
                });
            };
        }
    }


    export class ReportCampaignLinkedSiteController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.websiteService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.currentTab = 'linked_website';
            $scope.title = "Linked Website";
            $scope.isCompareReport = true;
            //$scope.resolution = "hour";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};
            $scope.currentObjId = $stateParams.campaignId

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            $scope.optionQuery = [{ key: "report", value: "website_by_campaign" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);

            $scope.gotoDetail = (websiteId: number) => {
                var params: {} = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, websiteId: websiteId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign_linkedzonebysite', params);
            };
        }
    }

    export class ReportCampaignLinkedZoneController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone";
            $scope.isCompareReport = true;
            //$scope.resolution = "hour";
            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            $scope.optionQuery = [{ key: "report", value: "zone_by_campaign" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    export class ReportCampaignLinkedZoneBySiteController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone";
            $scope.isCompareReport = true;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "camp_zone_by_site";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "zone_of_site_by_campaign" }, { key: "campId", value: $stateParams.campaignId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    ///item detail

    export class ReportCampaignItemDetailController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);

            $scope.reportType = "item";
            $scope.goTab = (tabName: string) => {

                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if (key !== "filter" && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.orderId) params["orderId"] = $stateParams.orderId;
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;
                if ($stateParams.campaignId) params["campaignId"] = $stateParams.campaignId;
                if ($stateParams.itemId) params["itemId"] = $stateParams.itemId;
                if (tabName === 'linked_website') {
                    $state.transitionTo('main.report.item.linkedsite', params);
                    return;
                }
                if (tabName === 'linked_zone') {
                    $state.transitionTo('main.report.item.linkedzone', params);
                    return;
                }
            };
        }
    }
    export class ReportItemLinkedSiteController extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_website';
            $scope.title = "Linked Website";
            $scope.isCompareReport = true;
            $scope.currentService = factory.websiteService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "website_by_item" }, { key: "itemId", value: $stateParams.itemId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);

            $scope.gotoDetail = (websiteId: number) => {
                var params: {} = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, itemId: $stateParams.itemId, websiteId: websiteId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.item_linkedzonebysite', params);
            };
        }
    }

    export class ReportItemLinkedZoneController extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone";
            $scope.isCompareReport = true;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "zone_by_item" }, { key: "itemId", value: $stateParams.itemId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    export class ReportItemLinkedZoneBySiteController extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentTab = 'linked_zone';
            $scope.title = "Linked Zone by item";
            $scope.isCompareReport = true;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "item_zone_by_site";
            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "zone_of_site_by_item" }, { key: "itemId", value: $stateParams.itemId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }

    // ---------------------- Website -----------------------
    export class ReportWebsiteController extends ReportBaseController {
        constructor($scope: scopes.IReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory, filterIdsService) {
            $scope.reportType = "websites";
            $scope.currentService = factory.websiteService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Websites";
            CurrentTab.setTab(new common.Tab("report", "list_site"));
            //$scope.colNames = ["impression", "click", "ctr", "revenue"];
            $scope.csvFileHeader = 'Website,Impression,Click,CTR,Revenue';

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [{ key: "report", value: "website" }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------------- function ----------------------------------------

            $scope.$on("idsPut", function () {
                var ids = filterIdsService.ids;
                ids = JSON.stringify(ids);
                ids = ids.replace(/([\]\[])/g, "");
                $scope.optionQuery.push({ key: "ids", value: ids });
                $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            });
            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { websiteId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.website.zone', params);
            };
        }
    }

    export class ReportWebsiteDetailController extends ReportBaseController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            CurrentTab.setTab(new common.Tab("report", "site_" + $stateParams.websiteId));
            //$scope.colNames = ["impression", "click", "ctr", "revenue"];
            $scope.csvFileHeader = 'Website,Impression,Click,CTR,Revenue';
            $scope.reportType = "website";
            // ----------------------------- function --------------------------------
            $scope.isTab = (tab: string): boolean => {
                if (tab === $scope.currentTab)
                    return true;
                return false;
            };

            $scope.isActiveTab = (tab: string): string => {
                if (tab === $scope.currentTab)
                    return 'active';
                return '';
            };

            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" || key === "filter" && tabName === "linked_campaign") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;

                if (tabName === 'zonegroup') {
                    $state.transitionTo('main.report.website.zonegroup', params);
                    return;
                }
                if (tabName === 'zone') {
                    $state.transitionTo('main.report.website.zone', params);
                    return;
                }
                if (tabName === 'linked_campaign') {
                    $state.transitionTo('main.report.website.linkedcamp', params);
                    return;
                }
                if (tabName === 'linked_item') {
                    $state.transitionTo('main.report.website.linkeditem', params);
                    return;
                }
                if (tabName === 'subzone') {
                    $state.transitionTo('main.report.website.subzone', params);
                    return;
                }
            };
        }
    }

    export class ReportWebsiteZoneGroupController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Zone group";
            $scope.currentTab = 'zonegroup';
            $scope.isCompareReport = false;
            $scope.currentService = factory.zoneGroupService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);


            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.zonegroup_detail.setting";
            $scope.objectIdString = "zoneGroupId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "zonegroup_by_site" }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, zonegroupId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.zonegroup.zone', params);
            };
        }
    }

    export class ReportWebsiteZoneController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Zone";
            $scope.currentTab = 'zone';
            $scope.isCompareReport = false;
            $scope.currentService = factory.zoneService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);


            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "zone_by_site" }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, zoneId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.zone.linkedcamp', params);
            };
        }
    }

    export class ReportWebsiteLinkedCampaignController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Linked campaign";
            $scope.currentTab = 'linked_campaign';
            //$scope.isFilterCampaign = true;
            $scope.isCompareReport = false;
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);


            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.order.campaign_detail.setting";
            $scope.objectIdString = "campaignId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "campaign_by_site" }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (campaignId: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, campaignId: campaignId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.website_linkeditembycampaign', params);
            };
        }
    }

    export class ReportWebsiteLinkedItemController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Linked Item";
            $scope.currentTab = 'linked_item';
            $scope.isCompareReport = false;
            $scope.currentService = factory.campaignItemService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_by_site" }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                factory.campaignItemService.load(id, function (itemRet: models.AdItem) {
                    if (itemRet && itemRet.campaignId > 0)
                        factory.campaignService.load(itemRet.campaignId, function (campaign: models.Campaign) {
                            var params: {} = { orderId: campaign.orderId, campaignId: campaign.id, itemId: id };
                            Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                                params[key] = factory.paramURLStore.get(key);
                            });
                            if ($stateParams.from) params["from"] = $stateParams.from;
                            if ($stateParams.to) params["to"] = $stateParams.to;
                            if ($stateParams.type) params["type"] = $stateParams.type;
                            if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                            if ($stateParams.cto) params["cto"] = $stateParams.cto;
                            if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                            $state.transitionTo('main.report.item.linkedsite', params);
                        });
                });
            };
        }
    }

    export class ReportWebsiteLinkedItemByCampaignController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.title = "Linked items by campaign";
            $scope.currentTab = 'linked_campaign';

            $scope.isCompareReport = false;
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "site_item_by_camp"

                $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_of_campaign_by_site" }, { key: "websiteId", value: $stateParams.websiteId }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

        }
    }

    //----------------zone group-------------------------
    export class ReportZonegroupDetailController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "zonegroup";

            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" || key === "filter" && tabName === "linked_campaign") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;
                if ($stateParams.zonegroupId) params["zonegroupId"] = $stateParams.zonegroupId;

                if (tabName === 'zone') {
                    $state.transitionTo('main.report.zonegroup.zone', params);
                    return;
                }
                if (tabName === 'linked_campaign') {
                    $state.transitionTo('main.report.zonegroup.linkedcamp', params);
                    return;
                }
                if (tabName === 'linked_item') {
                    $state.transitionTo('main.report.zonegroup.linkeditem', params);
                    return;
                }
            };

        }
    }

    export class ReportZonegroupZoneController extends ReportZonegroupDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.zoneGroupService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Zone";
            $scope.currentTab = 'zone';
            $scope.isCompareReport = false;

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "zone_by_zonegroup" }, { key: "zoneGroupId", value: $stateParams.zonegroupId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (zoneId: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, zoneId: zoneId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.zone.linkedcamp', params);
            };
        }
    }
    export class ReportZonegroupLinkedItemController extends ReportZonegroupDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignItemService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked item";
            $scope.currentTab = 'linked_item';
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_by_zonegroup" }, { key: "zoneGroupId", value: $stateParams.zonegroupId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);
        }
    }
    export class ReportZonegroupLinkedCampaignController extends ReportZonegroupDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked campaign";
            $scope.currentTab = 'linked_campaign';
            $scope.isCompareReport = false;
            //$scope.isFilterCampaign = true;


            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.order.campaign_detail.setting";
            $scope.objectIdString = "campaignId";
            $scope.settingParams = {};
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "campaign_by_zonegroup" }, { key: "zoneGroupId", value: $stateParams.zonegroupId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (campaignId: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, zonegroupId: $stateParams.zonegroupId, campaignId: campaignId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.zonegroup_linkeditembycampaign', params);
            };
        }
    }

    export class ReportZonegroupLinkedItemByCampaignController extends ReportZonegroupDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked item by campaign";
            $scope.currentTab = 'linked_item';
            $scope.reportType = "zg_item_by_camp";
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_of_campaign_by_zonegroup" }, { key: "zoneGroupId", value: $stateParams.zonegroupId }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                $stateParams.transitionTo('', {});
            };
        }
    }

    //-----------------Zone-------------------------
    export class ReportZoneDetailController extends ReportWebsiteDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.reportType = "zone";

            $scope.goTab = (tabName: string) => {
                var params: {} = {};
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    if ((key !== "filter" || key === "filter" && tabName === "linked_campaign") && factory.paramURLStore.get(key) !== "conversion")
                        params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.websiteId) params["websiteId"] = $stateParams.websiteId;
                if ($stateParams.zoneId) params["zoneId"] = $stateParams.zoneId;

                if (tabName === 'linked_campaign') {
                    $state.transitionTo('main.report.zone.linkedcamp', params);
                    return;
                }
                if (tabName === 'linked_item') {
                    $state.transitionTo('main.report.zone.linkeditem', params);
                    return;
                }
            };

        }
    }
    export class ReportZoneLinkedCampaignController extends ReportZoneDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked campaign";
            $scope.currentTab = 'linked_campaign';
            $scope.isCompareReport = false;
            //$scope.isFilterCampaign = true;

            $scope.settingState = "main.order.campaign_detail.setting";
            $scope.objectIdString = "campaignId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "campaign_by_zone" }, { key: "zoneId", value: $stateParams.zoneId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (campaignId: number) => {
                var params: {} = { websiteId: $stateParams.websiteId, zoneId: $stateParams.zoneId, campaignId: campaignId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.zone_linkeditembycampaign', params);
            };
        }
    }
    export class ReportZoneLinkedItemController extends ReportZoneDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignItemService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked item";
            $scope.currentTab = 'linked_item';
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_by_zone" }, { key: "zoneId", value: $stateParams.zoneId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                $stateParams.transitionTo('', {});
            };
        }
    }

    export class ReportZoneLinkedItemByCampaignController extends ReportZoneDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            $scope.currentService = factory.campaignItemService
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Linked item by campaign";
            $scope.currentTab = 'linked_item';
            $scope.isCompareReport = false;
            $scope.reportType = "zone_item_by_camp";

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = {};
            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_of_campaign_by_zone" }, { key: "zoneId", value: $stateParams.zoneId }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                $stateParams.transitionTo('', {});
            };
        }
    }

    //Conversion report
    export class ReportOrdersConversionController extends ReportTotalOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, $location, CurrentTab, factory);

            $scope.currentTab = "conversion";
            $scope.title = "Orders conversion";
            $scope.graphType = "Conversion";
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.detail.setting";
            $scope.objectIdString = "orderId";
            $scope.settingParams = {};

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "order_conversion" }, { key: "kind", value: "conversion" }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order.conversion', params);
            };
            $scope.gotoConversionDetail = (id: number) => {
                var params: {} = { orderId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order.conversion_detail', params);
            }
                $scope.gotoDelivery = (id: number) => {
                var params: {} = { orderId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order.website_delivery', params);
            }
        }
    }


    export class ReportCampaignsConversionController extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.currentTab = "conversion";
            $scope.title = "Campaigns conversion";
            $scope.graphType = "Conversion";
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.campaign_detail.setting";
            $scope.objectIdString = "campaignId";
            $scope.settingParams = { orderId: $stateParams.orderId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "campaign_conversion" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: "conversion" }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: $stateParams.orderId, campaignId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign.conversion', params);
            };
            $scope.gotoConversionDetail = (id: number) => {
                var params: {} = { campaignId: id, orderId: $stateParams.orderId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign.conversion_detail', params);
            }
                $scope.gotoDelivery = (id: number) => {
                var params: {} = { campaignId: id, orderId: $stateParams.orderId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign.website_delivery', params);
            }
        }
    }

    export class ReportItemsConversionController extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, CurrentTab: common.CurrentTab, $location, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.currentTab = "conversion";
            $scope.title = "Items conversion";
            $scope.graphType = "Conversion";
            $scope.isCompareReport = false;

            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            $scope.settingParams = { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            var options = [{ key: "report", value: "item_conversion" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: "conversion" }, { key: "resolution", value: $scope.resolution }];
            $scope.getData(currentTimeRange, compareTimeRange, options);

            // @Override
            $scope.gotoDetail = (id: number) => {

            };
            $scope.gotoConversionDetail = (id: number) => {
                var params: {} = { itemId: id, orderId: $stateParams.orderId, campaignId: $stateParams.campaignId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.item.conversion_detail', params);
            }
                $scope.gotoDelivery = (id: number) => {
                var params: {} = { itemId: id, orderId: $stateParams.orderId, campaignId: $stateParams.campaignId };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.item.website_delivery', params);
            }
        }
    }

    export class ReportOrderConversionDetail extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Campaigns";
            $scope.currentTab = "order_conversion_detail";
            $scope.reportType = "order_conversion_detail";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "convt_of_order_detail_conversion" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------


        }
    }

    export class ReportCampaignConversionDetail extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Items";
            $scope.currentTab = "campaign_conversion_detail";
            $scope.reportType = "campaign_conversion_detail";
            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "convt_of_campaign_detail_conversion" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------

        }
    }

    export class ReportItemConversionDetail extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Item";
            $scope.currentTab = "item_conversion_detail";
            $scope.reportType = "item_conversion_detail";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "convt_of_item_detail_conversion" }, { key: "itemId", value: $stateParams.itemId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------


        }
    }

    export class ReportWebsiteOrderDelivery extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Website conversion";
            $scope.currentTab = "website_of_order_delivery";
            $scope.reportType = "website_of_order_delivery";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "website_of_order_delivery_conversion" }, { key: "orderId", value: $stateParams.orderId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
            $scope.gotoDetail = (id: number) => {
                var params: {} = { orderId: $stateParams.orderId, websiteId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.order.zone_delivery', params);
            }
        }
    }
    export class ReportZoneOrderDelivery extends ReportOrderDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Zone conversion";
            $scope.currentTab = "zone_of_order_delivery";
            $scope.reportType = "zone_of_order_delivery";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;
            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "zone_of_order_delivery_conversion" }, { key: "orderId", value: $stateParams.orderId },
                { key: "zoneId", value: $stateParams.zoneId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
        }
    }
    export class ReportWebsiteCampaignDelivery extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Website conversion";
            $scope.currentTab = "website_of_campaign_delivery";
            $scope.reportType = "website_of_campaign_delivery";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "website_of_campaign_delivery_conversion" }, { key: "campId", value: $stateParams.campaignId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
            $scope.gotoDetail = (id: number) => {
                var params: {} = { campaignId: $stateParams.campaignId, orderId: $stateParams.orderId, websiteId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.campaign.zone_delivery', params);
            }
        }
    }
    export class ReportZoneCampaignDelivery extends ReportCampaignDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Zone of Campaign delivery conversion";
            $scope.currentTab = "zone_of_campaign_delivery";
            $scope.reportType = "zone_of_campaign_delivery";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };
            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "zone_of_campaign_delivery_conversion" }, { key: "campId", value: $stateParams.campaignId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
        }
    }
    export class ReportWebsiteItemDelivery extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Website of Item delivery conversion";
            $scope.currentTab = "website_of_item_delivery";
            $scope.reportType = "website_of_item_delivery";

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            $scope.settingState = "main.website.detail.setting";
            $scope.objectIdString = "websiteId";
            $scope.settingParams = {};

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "website_of_item_delivery_conversion" }, { key: "itemId", value: $stateParams.itemId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
            $scope.gotoDetail = (id: number) => {
                var params: {} = { itemId: $stateParams.itemId, campaignId: $stateParams.campaignId, orderId: $stateParams.orderId, websiteId: id };
                Object.keys(factory.paramURLStore.dataStore).forEach((key, index) => {
                    params[key] = factory.paramURLStore.get(key);
                });
                if ($stateParams.from) params["from"] = $stateParams.from;
                if ($stateParams.to) params["to"] = $stateParams.to;
                if ($stateParams.type) params["type"] = $stateParams.type;
                if ($stateParams.cfrom) params["cfrom"] = $stateParams.cfrom;
                if ($stateParams.cto) params["cto"] = $stateParams.cto;
                if ($stateParams.ctype) params["ctype"] = $stateParams.ctype;
                $state.transitionTo('main.report.item.zone_delivery', params);
            }
        }
    }
    export class ReportZoneItemDelivery extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Zone of Item delivery conversion";
            $scope.currentTab = "zone_of_item_delivery";
            $scope.reportType = "zone_of_item_delivery";
            $scope.settingState = "main.website.zone_detail.setting";
            $scope.objectIdString = "zoneId";
            $scope.settingParams = { websiteId: $stateParams.websiteId };

            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "zone_of_item_delivery_conversion" }, { key: "itemId", value: $stateParams.itemId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
            // ---------------------------- function --------------------------------------
        }
    }


    export class ReportZingTVController extends ReportBaseController {
        constructor($scope: scopes.IReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            CurrentTab.setTab(new common.Tab("report", "report_zingtv"));
            $scope.title = "Report Zing TV"
            $scope.reportType = "zing_tv";
            $scope.isShowChart = false;
            var currentTimeRange = utils.DateUtils.getYesterday();
            var compareTimeRange = null;
            $scope.selectedCol = {};
            $scope.settingState = "main.order.item_detail.setting";
            $scope.objectIdString = "itemId";
            if ($stateParams.type === 'custom')
                $scope.timeOption = $scope.formatDateTime($stateParams.to);
            else
                $scope.timeOption = "Yesterday";
            $scope.check = {};
            $scope.check[$stateParams.type || "yesterday"] = true;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);
            }

            //$scope.resolution = "hour";
            var options = [];
            $scope.$watch('datepicker.startDate', function (newVal, oldVal) {
                if ($scope.check['custom'])
                    $scope.timeOption = moment.unix(Math.round(($scope.datepicker['startDate'].getTime()) / 1000)).format('DD/MM/YYYY');
            });

            $scope.getData = function (range, compareRange, options) {
                $scope.isLoading = true;
                if (range.to > 0)
                    range.to = range.to - 1;

                //Save parameter url
                factory.paramURLStore.store("from", range.from);
                factory.paramURLStore.store("to", range.to);
                Object.keys($scope.check).forEach((key, index) => {
                    if ($scope.check[key] == true) {
                        factory.paramURLStore.store("type", key); return
                    };
                });

                $scope.timeRange = range;

                factory.reportService.loadReportZingTV(range, function (reportResponse: models.ZingTVReportResponse) {
                    if (reportResponse) {
                        $scope.report = models.ZingTVReportResponse.convert(reportResponse);
                        $scope.colNames = [];
                        $scope.colNamesLabel = [];
                        if ($scope.report["videos"][0] && $scope.report["videos"][0].banners) {
                            $scope.report["videos"][0].banners[0].fields.forEach((k) => {
                                $scope.colNames.push(k);
                            });
                        }
                        $scope.colNames.sort((a, b) => models.FieldReport.COLLUMNNAMEORDER.indexOf(a) - models.FieldReport.COLLUMNNAMEORDER.indexOf(b));
                        $scope.colNames.forEach((col) => $scope.colNamesLabel.push(models.FieldReport.COLUMNNAMELABEL[col]));
                        $scope.totalRecord = $scope.report["videos"].length;
                    } else {
                        $scope.totalRecord = 0;
                    }
                    $scope.isLoading = false;
                    $scope.$apply();
                });
            }

            $scope.getData(currentTimeRange, compareTimeRange, options);

            $scope.switchSort = (type) => {
                var direction = -1; //descending
                if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                    $scope.sortField[type] = common.SortDefinition.DOWN;
                }
                else if ($scope.sortField[type] === common.SortDefinition.UP) {
                    $scope.sortField[type] = common.SortDefinition.DEFAULT
                    direction = 1;
                }
                else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                    $scope.sortField[type] = common.SortDefinition.UP;
                    direction = 1;
                }

                switch (type) {
                    case "title":
                        $scope.report["videos"].sort((a, b) => {
                            return a.name.toLowerCase().localeCompare(b.name.toLowerCase()) * direction;
                        });
                        break;
                    default:
                        if ($scope.report["videos"].length !== 0) {
                            if (type === "i" || type === "r" || type === "c" || type === "cp") {
                                $scope.report["videos"].sort((a, b) => {
                                    var diff;
                                    switch (type) {
                                        case "i":
                                            diff = a.impression - b.impression;
                                            break;
                                        case "c":
                                            diff = a.click - b.click;
                                            break;
                                        case "cp":
                                            diff = a.completed - b.completed;
                                            break;
                                        case "r":
                                            diff = a.revenue - b.revenue;
                                            break;
                                    }
                                    return direction * diff;
                                });
                            }
                            $scope.report["videos"].forEach((video, i) => {
                                $scope.report["videos"][i].banners.sort((a, b) => {
                                    var valA = a.values[type].value;
                                    var valB = b.values[type].value;

                                    if (typeof (valA) === "number" && typeof (valB) === "number") {
                                        return (valA - valB) * direction;
                                    } else if (typeof (valA) === "string" && typeof (valB) === "string") {
                                        return (valA.toLowerCase() < valB.toLowerCase() ? -1 : valA.toLowerCase() > valB.toLowerCase() ? 1 : 0) * direction;
                                    }
                                    return 0;
                                });
                            });
                        }
                }
                Object.keys($scope.sortField).forEach((field) => {
                    if (field !== type)
                        $scope.sortField[field] = common.SortDefinition.DEFAULT;
                });
            }

            $scope.getEndIndexCurPage = () => {
                return ($scope.paging.startFrom * $scope.paging.limitItem < $scope.totalRecord ? $scope.paging.startFrom * $scope.paging.limitItem : $scope.totalRecord);
            }
            $scope.genCSV = () => {
                var content = "Video info";
                $scope.colNamesLabel.forEach((col) => content += "," + col);
                content += "\r\n";
                if ($scope.totalRecord > 0) {
                    $scope.colNames.forEach((col) => {
                        content += ",";
                        if ($scope.report.summary.properties[col])
                            content += $scope.report.summary.properties[col].value;
                    });
                    content += "\r\n";
                }
                $scope.report["videos"].forEach((video) => {
                    content += "\"" + "Title: " + video.name + "; Video Id: " + video.id +
                    "; Program: " + video.programName + "; Program Id: " + video.programId + "\"" + ",,,";
                    content += video.impression + "," + video.click + "," + video.completed + "," + video.revenue + "\r\n";
                    if (video.banners) {
                        video.banners.forEach((item) => {
                            $scope.colNames.forEach((col) => {
                                content += "," + item.values[col].value;
                            });
                            content += "\r\n";
                        });
                    }
                });
                return content;
            }
            $scope.exportCSV = () => {
                if (!$scope.report || $scope.report["videos"].length == 0)
                    return;
                var toDay = new Date();
                var filename = "report_zingtv_csv_" + common.StringUtils.paddingZero(toDay.getDate(), 2)
                    + common.StringUtils.paddingZero(toDay.getMonth(), 2)
                    + common.StringUtils.paddingZero(toDay.getFullYear(), 2) + ".csv";
                var content = $scope.genCSV();
                saveCSV(filename, content);
            }
            $scope.getPageNum = () => {
                if ($scope.totalRecord == 0 || !$scope.totalRecord)
                    return 0;
                return Math.ceil($scope.report["videos"].length / $scope.paging.limitItem);
            };

            $scope.query = () => {
                $scope.isOpenMenu = false;
                var range: models.TimeRange = new models.TimeRange(0, 0); // all time
                if ($scope.check['today']) {
                    var today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
                    range = new models.TimeRange(today.getTime(), new Date().getTime());
                } else if ($scope.check['yesterday']) {
                    range = utils.DateUtils.getYesterday();
                } else if ($scope.check['custom']) {
                    var startDate: Date = new Date($scope.datepicker['startDate'].getTime());
                    startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
                    range = new models.TimeRange(startDate.getTime(), startDate.getTime() + 86400000);
                }
                $scope.isLoading = true;
                $scope.queryReport(range, new models.TimeRange(0, 0));
            };

            $scope.chose = (type, e) => {
                $scope.check = {};
                if (type == "custom" || type == "datepicker") {
                    $scope.timeOption = moment.unix(Math.round(($scope.datepicker['startDate'].getTime()) / 1000)).format('DD/MM/YYYY');
                    $scope.check["custom"] = true;
                } else if (type == "yesterday") {
                    $scope.timeOption = "Yesterday";
                    $scope.check["yesterday"] = true;
                }
            }
        }
    }

    export class ReportPRItemController extends ReportCampaignItemDetailController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory)
            $scope.title = "Report PR Item"
            $scope.reportType = "pr_item";
            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "???" }, { key: "itemId", value: $stateParams.itemId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }
    export class ReportPrArticleController extends ReportBaseController {
        constructor($scope: scopes.IDetailReportScope, $state, $stateParams, $location, CurrentTab: common.CurrentTab, factory: backend.Factory) {
            super($scope, $state, $stateParams, CurrentTab, $location, factory);
            $scope.title = "Report PR Article"
            $scope.reportType = "pr_article";
            var currentTimeRange = $scope.getTimeReport();
            var compareTimeRange = null;

            // get timestamp from params
            if ($stateParams.from !== undefined && $stateParams.to !== undefined && $stateParams.from !== null && $stateParams.to !== null) {
                currentTimeRange = new models.TimeRange($stateParams.from, $stateParams.to);

                if ($stateParams.cfrom !== undefined && $stateParams.cto !== undefined && $stateParams.cfrom !== null && $stateParams.cto !== null)
                    compareTimeRange = new models.TimeRange($stateParams.cfrom, $stateParams.cto);
            }

            //$scope.resolution = "hour";
            $scope.optionQuery = [];
            $scope.optionQuery.push({ key: "report", value: "???" }, { key: "itemId", value: $stateParams.itemId }, { key: "websiteId", value: $stateParams.websiteId }, { key: "kind", value: $scope.graphType.toLowerCase() }, { key: "resolution", value: $scope.resolution });

            $scope.csvFileHeader = 'Order,Impression,Click,CTR,Spent';
            $scope.getData(currentTimeRange, compareTimeRange, $scope.optionQuery);
        }
    }
}
