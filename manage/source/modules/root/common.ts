
module common {
    'use strict';

    export class PageUtils {
        title = '123Click';
        page = '';
        constructor(title: string) { this.title = title; }
        getTitle() { return this.title; }
        setTitle(newTitle: string) { this.title = newTitle; }
        getCurrPage() { return this.page; }
        setCurrPage(newPage: string) { this.page = newPage; }
    }

    export class CurrentTab {
        tab: common.Tab;
        constructor() { }
        getTab() { return this.tab; }
        setTab(tab: common.Tab) { this.tab = tab; }
    }

    export class ActionMenu {
        icon: string;
        name: string;
        action: any;
        allow: boolean;
        permission: number;
        constructor(icon: string, name: string, action: any, permission?: number) {
            this.icon = icon;
            this.name = name;
            this.action = action;
            this.permission = permission;
        }
    }
    export class BreadCumNode {
        key: any;
        name: any;
        parent: any;

        constructor(key, name, parent) {
            this.key = key;
            this.name = name;
            this.parent = parent;
        }
    }
    export class BreadCumTree {
        nodes: {};

        constructor() {
            this.nodes = {};
            this.push(new BreadCumNode(-1, "", -1));
        }
        push(node: BreadCumNode) {
            if (!this.nodes[node.key])
                this.nodes[node.key] = node;
        }
        getNode(nodeKey: any): BreadCumNode {
            return this.nodes[nodeKey];
        }
        getPath(nodeKey: any): Array<any> {
            var items = [];
            var node: BreadCumNode = this.nodes[nodeKey];
            if (node === undefined)
                return [];
            while (node.key != -1) {
                items.push(node.key);
                node = this.getNode(node.parent);
            }
            return items;
        }
        setNameNode(key, name) {
            if (this.nodes[key] !== undefined || this.nodes[key] !== null)
                this.nodes[key].name = name;
        }
    }

    export class BreadCum {
        tree: common.BreadCumTree;
        private linksByState: {};

        constructor() {
            this.tree = new common.BreadCumTree();
            this.linksByState = {};
        }
        pushState(state, link) {
            this.linksByState[state] = link;
        }
        getLink(state) {
            return this.linksByState[state];
        }
        getMenuItemList(nodeKey): common.MenuItem[] {
            var items: common.MenuItem[] = [];
            var path = this.tree.getPath(nodeKey);
            for (var i = path.length - 1; i >= 0; i--)
                items.push(new common.MenuItem(this.tree.getNode(path[i]).name,
                    path[i].toString()));
            return items;
        }
    }

    export interface IIDSearch {
        search(text: string, callback);
    }

    export class IDSearch implements IIDSearch {
        static WEBSITE_URL_TEMPLATE: string = '/websites/website-id/zones';
        static ZONEGROUP_URL_TEMPLATE: string = '/websites/website-id/zonegrp/zonegrp-id/zones';
        static ZONE_URL_TEMPLATE: string = '/websites/website-id/zone/zone-id/type/zone-type/zones';
        static ORDER_URL_TEMPLATE: string = '/orders/order-id/campaigns';
        static CAMPAIGN_URL_TEMPLATE: string = '/orders/order-id/camp-id/items';
        static ITEM_URL_TEMPLATE: string = '/orders/order-id/camp-id/item-id/setting';
        static ARTICLE_URL_TEMPLATE: string = '/orders/order-id/camp-id/item-id/setting-pr';

        private static website: backend.IWebsite;
        private static zonegroup: backend.IZoneGroup;
        private static zone: backend.IZone;
        private static order: backend.IOrder;
        private static campaign: backend.ICampaign;
        private static item: backend.ICampaignItem; 

        constructor(factory: backend.Factory) {
                IDSearch.website = factory.websiteService; IDSearch.zonegroup = factory.zoneGroupService; IDSearch.zone = factory.zoneService;
                IDSearch.order = factory.orderService; IDSearch.campaign = factory.campaignService; IDSearch.item = factory.campaignItemService;
        }

        

        getWebsite(websiteId: number, callback) {
            IDSearch.website.load(websiteId, function (website: models.Website) {
                if (website !== undefined && website.id > 0) {
                    var url: string = IDSearch.WEBSITE_URL_TEMPLATE;
                    url = url.replace("website-id", website.id.toString());
                    callback(url);
                }
            });
        }

        getZoneGroup(zoneGroupId: number, callback) {
            IDSearch.zonegroup.load(zoneGroupId, function (zonegroup: models.ZoneGroup) {
                if (zonegroup && zonegroup.id > 0) {
                    var url: string = IDSearch.ZONEGROUP_URL_TEMPLATE;
                    url = url.replace("website-id", zonegroup.siteId.toString()).replace("zonegrp-id", zonegroup.id.toString());
                    callback(url);
                }
            });
        }

        getZone(zoneId: number, callback) {
            IDSearch.zone.load(zoneId, function (zone: models.Zone) {
                if (zone && zone.id > 0) {
                    var url: string = IDSearch.ZONE_URL_TEMPLATE;
                    url = url.replace("website-id", zone.siteId.toString()).replace("zone-id", zone.id.toString()).replace("zone-type", zone.kind);
                    callback(url);
                }
            });
        }

        getOrder(orderId: number, callback) {
            IDSearch.order.load(orderId, function (order: models.Order) {
                if (order !== undefined && order.id > 0) {
                    var url: string = IDSearch.ORDER_URL_TEMPLATE;
                    url = url.replace("order-id", order.id.toString());
                    callback(url);
                }
            });
        }

        getCampaign(campId: number, callback) {
            IDSearch.campaign.load(campId, function (campaign: models.Campaign) {
                if (campaign && campaign.id > 0) {
                    var url: string = IDSearch.CAMPAIGN_URL_TEMPLATE;
                    url = url.replace("order-id", campaign.orderId.toString()).replace("camp-id", campaign.id.toString());
                    callback(url);
                }
            });
        }

        getItem(itemId: number, callback) {
            IDSearch.item.load(itemId, function (item: models.AdItem) {
                if (item && item.id > 0) {
                    IDSearch.campaign.load(item.campaignId, function (campaign: models.Campaign) {
                        if (campaign && campaign.id > 0) {
                            var url: string = item.kind === models.CampaignItemType.NETWORK.PR ? IDSearch.ARTICLE_URL_TEMPLATE : IDSearch.ITEM_URL_TEMPLATE;
                            url = url.replace("order-id", campaign.orderId.toString()).replace("camp-id", campaign.id.toString()).replace("item-id", item.id.toString());
                            callback(url);
                        }
                    });
                }
            });
        }

        search(text: string, callback) {
            if (!text)
                return;

            text = text.toUpperCase();
            var kind: string = text.charAt(0);
            var idstr: string = text.substr(1);

            if (!Utils.isNumber(idstr))
                return;

            var id: number = parseInt(idstr);
            switch (kind) {
                case 'W': this.getWebsite(id, callback); break;
                case 'G': this.getZoneGroup(id, callback); break;
                case 'Z': this.getZone(id, callback); break;
                case 'O': this.getOrder(id, callback); break;
                case 'C': this.getCampaign(id, callback); break;
                case 'I': this.getItem(id, callback); break;
            }
        }
    }
}