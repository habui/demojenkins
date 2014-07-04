/// <reference path="../../libs/angular/angular.d.ts"/>
/// <reference path="../../libs/moment.d.ts"/>
/// <reference path="../../common/common.ts"/>
/// <reference path="../../libs/bootstrap.d.ts"/>
/// <reference path="../../libs/jquery.custom.d.ts"/>
/// <reference path="../../libs/define.d.ts"/>

module controllers {
    'use strict';
    export class CampaignArticleListController extends PermissionController {
        constructor($scope: scopes.ICampaignArticleListScope, $state, $stateParams, $modal, $timeout,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
                // Pre-load scope
                super($scope, $state, factory);
                $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
                CurrentTab.setTab(new common.Tab('order', 'list_order'));

                // Initialize Scope Value
                var pageIndex: number = $stateParams.page;
                $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
                $scope.pageSize = 10;
                $scope.totalRecord = 0;
                $scope.orderId = $stateParams.orderId;
                $scope.campaignId = $stateParams.campaignId;
                $scope.items = [];
                $scope.query = "";
                $scope.filter = { selectedStatus: { name: "All articles", value: null } };
                $scope.orderBy = "";
                $scope.sortField = {};
                $scope.articleStatus = [];
                $scope.checkBoxes = {};
                Object.keys(models.ArticleStatus).forEach((key, _) => {
                    if (typeof models.ArticleStatus[key] === 'object')
                        $scope.articleStatus.push(models.ArticleStatus[key]);
                });
            
                listCampaignArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filter.selectedStatus.value, "");

                $scope.siteNameDic = {};
                $scope.zoneNameDic = {};
                factory.articleService.listPRZonesOfPublisher(function (ret) {
                    if (!!ret) {
                        ret.forEach((w, _) => {
                            $scope.siteNameDic[w.id] = w.name;
                            w.zoneModels.forEach((z, __) => $scope.zoneNameDic[z.id] = z.name);
                        });
                    }
                });

                function listCampaignArticlesByQuery(from: number, count: number, status: number, query: string, sortBy?: string, sortType?: string) {
                    var orderBy = sortBy || "name";
                    var dir = sortType || "asc";
                    factory.articleService.searchArticlesInCampaign(query, $stateParams.campaignId, orderBy, dir, from, count, status,
                        function (ret) {
                            if (ret.data) {
                                $scope.items = ret.data;
                                $scope.totalRecord = ret.total;
                                $scope.checkBoxes = {};
                            }
                        });
                }

                $scope.$watch('filter.selectedStatus', function (newValue, oldValue) {
                    if ($scope.filter.selectedStatus.value !== null && $scope.filter.selectedStatus.value !== undefined) {
                        var query = $scope.query || "";
                        var status = $scope.filter.selectedStatus.value || 0;
                        listCampaignArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, status, query, $scope.orderBy, "asc");
                    }
                });
                $scope.$watch("checkBoxes.all", function (newVal, oldVal) {
                    $scope.items.forEach((it, _) => {
                        $scope.checkBoxes[it.id] = $scope.checkBoxes['all'];
                    });
                });
                // Scope Functions
                $scope.dateParse = (input: number) => {
                    return moment.unix(Math.round(input / 1000)).format('DD/MM/YYYY');
                }

                $scope.search = () => {
                    var query = $scope.query || "";
                    var status = $scope.filter.selectedStatus.value || 0;
                    // Process Search
                    listCampaignArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, status, query, $scope.orderBy, "asc");
                };

                $scope.paging = (start: number, size: number) => {
                    var query = $scope.query || "";
                    var status = $scope.filter.selectedStatus.value || 0;
                    // Process Search
                    listCampaignArticlesByQuery(start, size, status, query, $scope.orderBy, "asc");
                };

                $scope.goToArticle = (campaignId: number, articleId: number) => {
                    $state.transitionTo('main.order.article_detail.setting',
                        { orderId: $stateParams.orderId, campaignId: campaignId, articleId: articleId });
                };

                $scope.switchSort = (type: string) => {
                    $scope.orderBy = type;
                    $scope.orderDir = "asc";

                    if ($scope.sortField[type] == undefined)
                        $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        $scope.orderDir = "desc";
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.UP) {
                        $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                        $scope.sortField[type] = common.SortDefinition.UP;
                    }
                    listCampaignArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filter.selectedStatus.value, $scope.query, $scope.orderBy, $scope.orderDir);
                    for (var aType in $scope.sortField)
                        if (type != aType)
                            $scope.sortField[aType] = common.SortDefinition.DEFAULT;
                }

                $scope.getSortClass = (type: string) => {
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
                $scope.getStatus = (statusValue: number) => {
                    var ret = "";
                    Object.keys(models.ArticleStatus).forEach((key, _) => {
                        if (statusValue === models.ArticleStatus[key].value)
                            ret = models.ArticleStatus[key].name;
                    });
                    return ret;
                }

                $scope.getActions = function (statusId: number) {
                    var actions = [];
                    var articleStatus = models.ArticleStatus;
                    var articleAction = models.ArticleAction;

                    //permission for sale
                    if ($scope.checkPermission($scope.permissionDefine.EDITORDER) === true) {
                        switch (statusId) {
                            case articleStatus.REJECT_BY_REVIEWER.value:
                                actions = actions.concat([articleAction.EDITED, articleAction.DELETED, articleAction.VIEW_REASON]);
                                break;
                        }
                        return actions;
                    }
                }
                $scope.doAction = function (item: models.Article, action: string) {
                    var _actions = models.ArticleAction;
                    var cloneItem = jQuery.extend(true, {}, item);
                    switch (action) {
                        case _actions.EDITED:
                            $state.transitionTo("main.order.article_setting", { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, articleId: item.id });
                            break;
                        case _actions.DELETED:
                            var articleModal = $modal.open({
                                templateUrl: 'views/common/modal.delete',
                                controller: common.ModalDeleteController,
                                resolve: {
                                    checkedList: function () {
                                        var checkedList = [{ id: item.id, name: item.name }];//list of object contain id and name
                                        return checkedList;
                                    },
                                    type: function () {
                                        return 'article';
                                    }
                                }
                            });

                            articleModal.result.then(function(checkList) {
                                var id = checkList[0].id;
                                factory.articleService.remove(id, function(ret) {
                                    listCampaignArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.filter.selectedStatus.value, $scope.query, $scope.orderBy, $scope.orderDir);
                                    notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "delete", "article"));
                                });
                            }, function (message) { });
                            break;
                        case _actions.VIEW_REASON:
                            $scope.selectItem = cloneItem;
                            $scope.commentList = [];
                            factory.articleService.listComments(cloneItem.id, 0, 1000, function (resp) {
                                if (!!resp && resp.length !== 0)
                                    $scope.commentList = resp;
                                jQuery("#reasonModal").modal('show');
                            });
                            break;
                    }
                }
                $scope.view = (articleId) => {
                    $state.transitionTo("main.order.article_setting", { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId, articleId: articleId});
                }
                
                $scope.cancel = function () {
                    jQuery("#reasonModal").modal("hide");
                }
                $scope.formatCommentTime = function (time) {
                    var curTime = new Date().getTime();
                    var diff = curTime - time;
                    if (diff < 3600000)
                        return Math.floor(diff / 60000) + " minute" + (diff !== 60000 ? 's' : "") + " ago";
                    return moment.unix(Math.floor(time / 1000)).format("H:mm DD/MM/YYYY");
                }
        }
    }
    
    export class CreatePrArticleController extends PermissionController {
        constructor($scope: scopes.ICreateArticleScope, $state, $stateParams, $location, $timeout, $anchorScroll,
            $window, CurrentTab: common.CurrentTab, factory: backend.Factory) {
                // Generate tab display
                super($scope, $state, factory);
                if ($stateParams.orderId) {
                    CurrentTab.setTab(new common.Tab('order', 'list_order'));
                    $scope.getPermission(utils.PermissionUtils.ORDER, $stateParams.orderId);
                } else if ($stateParams.websiteId) {
                    CurrentTab.setTab(new common.Tab("website", "list_sites"));
                    $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);
                }

                // Scope initialize
                $scope.checkValidate = false;
                $scope.uploadUrl = common.Config.UPLOAD_URL;
                $scope.item = new models.Article(0, $stateParams.campaignId);
                $scope.item.isReview = false;
                $scope.checkBoxZone = {};
                $scope.selectedZones = [];
                $scope.photoUploadResults = {};
                $scope.videoUploadResults = {};

                $scope.searchPublishers = {};
                $scope.publishedDate = new Date();
                $scope.item.contentType = "file";

                $scope.publishers = [];
                $scope.checkBoxSite = {};
                $scope.checkBoxZone = {};
                $scope.siteDic = {};
                $scope.zoneNameDic = {};
                $scope.comment = "";

                $scope.isUpdate = false;
                $scope.isPreview = false;
                $scope.isCreate = false;
                $scope.isSubmited = false;
                $timeout(initialEditor, 100);
                
                factory.articleService.listPRZonesOfPublisher(function (ret) {
                    if (!!ret) {
                        $scope.publishers = ret;
                        ret.forEach((w, _) => {
                            $scope.siteDic[w.id] = $scope.siteDic[w.id] || {};
                            $scope.siteDic[w.id]['name'] = w.name;
                            $scope.siteDic[w.id]['length'] = w.zoneModels.length;
                            w.zoneModels.forEach((z, __) => $scope.zoneNameDic[z.id] = z.name);
                        });

                        if ($stateParams.articleId !== undefined) {
                            factory.articleService.load($stateParams.articleId, function (articleRet: models.Article) {
                                if (articleRet) {
                                    if ($stateParams.orderId && articleRet.status === models.ArticleStatus.REJECT_BY_REVIEWER.value) {
                                        $scope.isUpdate = true;
                                        $("#editor").html(articleRet.content);
                                    } else {
                                        $scope.isPreview = true;
                                        $("#previewEditor").html(articleRet.content);
                                    }

                                    $scope.item = articleRet;
                                    $scope.selectedZones.push({
                                        id: $scope.item.zoneId,
                                        name: $scope.zoneNameDic[$scope.item.zoneId],
                                        siteId: $scope.item.websiteId,
                                        siteName: $scope.siteDic[$scope.item.websiteId]['name']
                                    });
                                    $scope.checkBoxZone[$scope.item.zoneId] = true;
                                    $scope.publishedDate = new Date($scope.item.publishedDate);

                                    factory.articleService.listComments($scope.item.id, 0, 1000, function(commentRet: models.ArticleComment[]) {
                                        if (!!commentRet && commentRet.length !== 0)
                                            $scope.commentList = commentRet;
                                    });

                                    if ($scope.item.photos.length !== 0)
                                        jQuery("uploader#photoUploader input#result").removeAttr("required");
                                }
                            });
                        } else {
                            $scope.isCreate = true;
                        }
                    }
                });
                var categories = [];
                factory.articleService.listCategories((ret) => categories = ret);

                var selectedPublishers = [];
                var selectedZones = {};
                
                //Handle upload
                $scope.$watch("contentUploadResult", function(newVal) {
                    if (!!newVal && newVal !== "FAIL")
                        $scope.item.content = $scope.contentUploadResult;
                })

                $scope.$watch("photoUploadResults.notify", function () {
                    if (!!$scope.photoUploadResults["data"] && $scope.photoUploadResults["data"].length !== 0) {
                        $scope.photoUploadResults["data"].forEach((photo: string) => {
                            if ($scope.item.photos.indexOf(photo) == -1)
                                $scope.item.photos.push(photo);
                        });
                    }
                })
                // Select Publisher Category
                $scope.selectZone = (id: number, siteId: number) => {

                    if ($scope.checkBoxZone[id] == true) {
                        var item = { 'id': id, 'name': $scope.zoneNameDic[id], 'siteId': siteId, 'siteName': $scope.siteDic[siteId]['name']};
                        $scope.selectedZones.push(item);

                        if (selectedPublishers.indexOf(siteId) === -1) selectedPublishers.push(siteId);
                        selectedZones[siteId] = selectedZones[siteId] || [];
                        selectedZones[siteId].push(id);
                        if (selectedZones[siteId].length === $scope.siteDic[siteId]['length'])
                            $scope.checkBoxSite[siteId] = true;
                    } else {
                        // Remove category from selected list
                        $scope.selectedZones.forEach(function (item) {
                            if (item.id == id) {
                                var index = $scope.selectedZones.indexOf(item);
                                $scope.selectedZones.splice(index, 1);
                                $scope.checkBoxZone[id] = false;

                                selectedZones[siteId].splice(selectedZones[siteId].indexOf(id), 1);
                                if (selectedZones[siteId].length < $scope.siteDic[siteId]["length"]) {
                                    $scope.checkBoxSite[siteId] = false;
                                    selectedPublishers.splice(selectedPublishers.indexOf(siteId), 1);
                                }
                            }
                        });
                    }
                }
                $scope.selectSite = (id: number) => {
                    $scope.publishers.forEach((w, i) => {
                        if (w.id === id) {
                            selectedZones[id] = selectedZones[id] || [];
                            w.zoneModels.forEach((z, _) => {
                                var item = { 'id': z.id, 'name': z.name, 'siteId': id, 'siteName': w.name };
                                if ($scope.checkBoxSite[id] === true && !$scope.checkBoxZone[z.id]) {
                                    $scope.selectedZones.push(item);
                                    selectedZones[id].push(z.id);
                                }
                                $scope.checkBoxZone[z.id] = $scope.checkBoxSite[id];
                            });
                        }
                    });
                    if ($scope.checkBoxSite[id] === false) {
                        $scope.selectedZones.filter((it, _) => it.siteId === id)
                            .forEach((a, __) => {
                                $scope.selectedZones.splice($scope.selectedZones.indexOf(a), 1);
                                if (selectedZones[id])
                                    selectedZones[id].splice(selectedZones[id].indexOf(a.id), 1);
                            });
                        $scope.checkBoxSite[id] = false;
                        selectedPublishers.splice(selectedPublishers.indexOf(id), 1);
                    }
                }
                // Remove Selected Publisher Category
                $scope.removeZone = (id: number) => {
                    $scope.selectedZones.forEach(function (item) {
                        if (item.id == id) {
                            var index = $scope.selectedZones.indexOf(item);
                            $scope.selectedZones.splice(index, 1);
                            $scope.checkBoxZone[id] = false;
                            if (selectedZones[item.siteId]) {
                                selectedZones[item.siteId].splice(selectedZones[item.siteId].indexOf(id), 1);
                                if (selectedZones[item.siteId].length < $scope.siteDic[item.siteId]['length']) {
                                    $scope.checkBoxSite[item.siteId] = false;
                                    selectedPublishers.splice(selectedPublishers.indexOf(item.siteId), 1);
                                }
                            }
                        }
                    });
                }
                // Add new photo upload
                $scope.photoAdd = () => {
                    $scope.photoUploadResults['data'].push({ 'id': $scope.photoUploadResults['index']++, 'value': "" });
                }
                // Remove specific photo upload
                $scope.photoRemove = (id: number) => {
                    for (var i = 0; i < $scope.photoUploadResults['data'].length; i++) {
                        if ($scope.photoUploadResults['data'][i]['id'] == id) {
                            $scope.photoUploadResults['data'].splice(i, 1);
                        }
                    }
                }
                // Add new video upload
                $scope.videoAdd = () => {
                    $scope.videoUploadResults['data'].push({ "id": $scope.videoUploadResults['index']++, 'value': "" });
                }

                // Remove specific video upload
                $scope.videoRemove = (id: number) => {
                    for (var i = 0; i < $scope.videoUploadResults['data'].length; i++) {
                        if ($scope.videoUploadResults['data'][i]['id'] == id) {
                            $scope.videoUploadResults['data'].splice(i, 1);
                        }
                    }
                }

                // Preview specific video upload
                $scope.videoPreview = (id: number) => {
                    for (var i = 0; i < $scope.videoUploadResults['data'].length; i++) {
                        if ($scope.videoUploadResults['data'][i]['id'] == id) {
                            flowplayer("player-" + id, "resource/flash/flowplayer-3.2.16.swf", $scope.videoUploadResults['data'][i]['value']);
                            jQuery("#previewVideo-" + id).modal('show');
                        }
                    }
                }

                $scope.formatCommentTime = function(time) {
                    var curTime = new Date().getTime();
                    var diff = curTime - time;
                    if (diff < 3600000)
                        return Math.floor(diff / 60000) + " minute"+ (diff !== 60000 ? 's' : "") + " ago";
                    return moment.unix(Math.floor(time / 1000)).format("H:mm-DD/MM/YYYY");
                }
                
                // Create PR article submit
                $scope.save = () => {
                    $scope.checkValidate = true;
                    if ($scope.hasSave === true) return;
                    // Content from editor
                    if ($scope.item.contentType ==='text')
                        $scope.item.content = $("#editor").html();
                    //validate form
                    if (!$scope.item.content || !$scope.item.name || !$scope.item.summary || !$scope.item.tags || !$scope.item.source)
                        return;

                    if ($scope.selectedZones.length === 0 || $scope.item.name.length === 0 || $scope.item.summary.length === 0 || $scope.item.tags.length === 0 ||
                        $scope.item.content.length === 0 || $scope.item.photos.length === 0 || $scope.item.source.length === 0)
                        return;

                    var articleInfo = jQuery.extend({}, $scope.item);
                    // Update pushlish date
                    articleInfo.publishedDate = new Date($scope.publishedDate).getTime();
                    articleInfo.updateDate = new Date().getTime();

                    var category = {};
                    $scope.selectedZones.forEach((z) => {
                        if (selectedPublishers.indexOf(z.siteId) === -1) {
                            selectedPublishers.push(z.siteId);
                            selectedZones[z.siteId] = selectedZones[z.siteId] || [];
                        }
                        if (selectedZones[z.siteId].indexOf(z.id) === -1)
                            selectedZones[z.siteId].push(z.id);
                        categories.filter((a) => a.name.toLowerCase().localeCompare(z.name.toLowerCase()) === 0)
                            .forEach((c) => category[z.id] = c.id);
                    });

                    selectedPublishers.forEach(function (websiteid, i) {
                        selectedZones[websiteid].forEach((zoneid) => {
                            articleInfo.websiteId = websiteid;
                            articleInfo.zoneId = zoneid;
                            articleInfo.category = category[zoneid];
                            if (!$scope.isUpdate) {
                                factory.articleService.save(articleInfo, function(article: models.Article) {
                                    if (article != null && article.id > 0 && i == selectedPublishers.length - 1) {
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "create", "article"));
                                        if ($scope.comment.length !== 0) {
                                            factory.articleService.addComment(article.id, $scope.comment, function() {});
                                        }
                                        $timeout(gotoHome, 2000);
                                    }
                                });
                            } else {
                                articleInfo.status = models.ArticleStatus.changeStatus(models.ArticleAction.EDITED, articleInfo.status);
                                if ($scope.comment.length !== 0) {
                                    factory.articleService.addComment(articleInfo.id, $scope.comment, function() {});
                                }
                                factory.articleService.update(articleInfo, function(resp) {
                                    if (resp) {
                                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "update", "article"));
                                        $timeout(gotoHome, 2000);
                                    }
                                })
                            }
                        });
                    });
                };
                // Cancel create action
                $scope.cancel = () => {
                    gotoHome();
                };

                $scope.isApprove = function () {
                    return $scope.item.status === models.ArticleStatus.WAITING_APPROVAL_BY_SALE.value && !!$stateParams.orderId && $scope.checkPermission($scope.permissionDefine.EDITORDER)
                        || $scope.item.status === models.ArticleStatus.PENDING.value && $stateParams.websiteId && $scope.checkPermission($scope.permissionDefine.APPROVE);
                }
                $scope.isPublisher = function() {
                    return $scope.item.status === models.ArticleStatus.WAITING_FOR_SYNC_TO_CMS.value && !!$stateParams.websiteId && $scope.checkPermission($scope.permissionDefine.WEBSITE_ALL_PERMISSION);
                }

                $scope.approve = function (action: string) {
                    if ($scope.item.status !== models.ArticleStatus.WAITING_APPROVAL_BY_PUBLISHER.value && $scope.item.status !== models.ArticleStatus.WAITING_APPROVAL_BY_SALE.value &&
                        action !== models.ArticleAction.APPROVED && action !== models.ArticleAction.REJECTED)
                        return;
                    $scope.isSubmited = true;
                    var cloneItem = jQuery.extend({}, $scope.item);
                    cloneItem.status = models.ArticleStatus.changeStatus(action, cloneItem.status);
                    factory.articleService.update(cloneItem, function(resp) {
                        if (resp) {
                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, action.toLowerCase(), "article"));
                            $timeout(gotoHome, 2000);
                        }
                    });
                    if ($scope.comment.length !== 0) {
                        factory.articleService.addComment(cloneItem.id, $scope.comment, function () { });
                    }
                }
                $scope.sendComment = function() {
                    if ($scope.comment.length !== 0) {
                        factory.articleService.addComment($scope.item.id, $scope.comment, function() {
                            factory.articleService.listComments($scope.item.id, 0, 100, function(resp: Array<any>) {
                                if (resp && resp.length !== 0) {
                                    $scope.commentList = resp;
                                    $scope.comment = "";
                                }
                            })
                        });
                    }
                }

                $scope.getStatus = (statusValue: number) => {
                    var ret = "";
                    Object.keys(models.ArticleStatus).forEach((key, _) => {
                        if (statusValue === models.ArticleStatus[key].value)
                            ret = models.ArticleStatus[key].name;
                    });
                    return ret;
                }
                $scope.sync2CMS = function () {
                    var cloneItem = jQuery.extend({}, $scope.item);
                    $scope.isSubmited = true;
                    factory.articleService.syncToCMS(cloneItem.id, function (resp, status) {
                        if (resp) {
                            if (resp.result) {
                                notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "synchronize", "article"));
                                $window.open(common.Config.ADMINCMSZINGNEW + resp.result);
                            } else {
                                notify(new common.ActionMessage(common.ActionMessage.FAIL, "synchronize", "article"));
                            }
                        }
                        $timeout(gotoHome, 2000);
                    });
                }

                $scope.viewContent = function() {
                    if ($scope.item.contentType == "file")
                        jQuery("#viewContent").modal("show");
                }
            ///function
            function gotoHome() {
                if ($stateParams.websiteId && $stateParams.zoneId)
                    $state.transitionTo('main.website.zone_detail.articles', { websiteId: $stateParams.websiteId, zoneId: $stateParams.zoneId, type: "pr" });
                else if ($stateParams.orderId && $stateParams.campaignId)
                    $state.transitionTo('main.order.campaign_detail.items', { orderId: $stateParams.orderId, campaignId: $stateParams.campaignId });
            }
        }
    }

    export class ZoneArticleListController extends PermissionController{
        constructor($scope: scopes.IZoneArticlesListScope, $state, $stateParams, $modal, $http, $timeout, $window,
            CurrentTab: common.CurrentTab, factory: backend.Factory) {
                super($scope, $state, factory);
                $scope.getPermission(utils.PermissionUtils.WEBSITE, $stateParams.websiteId);

                // Initialize Scope Value
                var pageIndex: number = $stateParams.page;
                $scope.pageIndex = (pageIndex != null && pageIndex !== undefined) ? pageIndex : 1;
                $scope.pageSize = 10;
                $scope.totalRecord = 0;
                $scope.websiteId = $stateParams.websiteId;
                $scope.zoneId = $stateParams.zoneId;
                $scope.items = [];
                $scope.query = "";
                $scope.selectedStatus =  { name: "All articles", value: null } ;
                $scope.orderBy = "";
                $scope.sortField = {};
                $scope.articleStatus = [];
                Object.keys(models.ArticleStatus).forEach((key, _) => {
                    if (typeof models.ArticleStatus[key] === 'object')
                        $scope.articleStatus.push(models.ArticleStatus[key]);
                });
                listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.selectedStatus.value, "");
                
                
                function listZoneArticlesByQuery(from: number, count: number, status: number, query: string, sortBy?: string, sortType?: string) {
                    var orderBy = sortBy || "name";
                    var dir = sortType || "asc";
                    factory.articleService.searchArticlesInZone(query, $stateParams.zoneId, orderBy, dir, from, count, status,
                        function (ret) {
                            if (ret.data) {
                                $scope.items = ret.data;
                                $scope.totalRecord = ret.total;
                            }
                        });
                }

                $scope.$watch('selectedStatus', function (newValue, oldValue) {
                    if ($scope.selectedStatus.value !== null && $scope.selectedStatus.value !== undefined) {
                        var query = $scope.query || "";
                        var status = $scope.selectedStatus.value || 0;
                        listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, status, query, $scope.orderBy, "asc");
                    }
                });
                // Scope Functions
                $scope.dateParse = (input: number) => {
                    return moment.unix(Math.round(input / 1000)).format('DD/MM/YYYY');
                }

                $scope.search = () => {
                    var query = $scope.query || "";
                    var status = $scope.selectedStatus.value || 0;
                    // Process Search
                    listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, status, query, $scope.orderBy, "asc");
                };

                $scope.paging = (start: number, size: number) => {
                    var query = $scope.query || "";
                    var status = $scope.selectedStatus.value || 0;
                    // Process Search
                    listZoneArticlesByQuery(start, size, status, query, $scope.orderBy, "asc");
                };

                $scope.goToArticle = (campaignId: number, articleId: number) => {
                    $state.transitionTo('main.order.article_detail.setting',
                        { orderId: $stateParams.orderId, campaignId: campaignId, articleId: articleId });
                };

                $scope.switchSort = (type: string) => {
                    $scope.orderBy = type;
                    $scope.orderDir = "asc";

                    if ($scope.sortField[type] == undefined)
                        $scope.sortField[type] = common.SortDefinition.DEFAULT;
                    if ($scope.sortField[type] === common.SortDefinition.DEFAULT) {
                        $scope.sortField[type] = common.SortDefinition.DOWN;
                        $scope.orderDir = "desc";
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.UP) {
                        $scope.sortField[type] = common.SortDefinition.DEFAULT;
                        $scope.orderDir = "asc";
                    }
                    else if ($scope.sortField[type] === common.SortDefinition.DOWN) {
                        $scope.sortField[type] = common.SortDefinition.UP;
                    }
                    listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.selectedStatus.value, $scope.query, $scope.orderBy, $scope.orderDir);
                    for (var aType in $scope.sortField)
                        if (type != aType)
                            $scope.sortField[aType] = common.SortDefinition.DEFAULT;
                }

                $scope.getSortClass = (type: string) => {
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
                $scope.getStatus = (statusValue: number) => {
                    var ret = "";
                    Object.keys(models.ArticleStatus).forEach((key, _) => {
                        if (statusValue === models.ArticleStatus[key].value)
                            ret = models.ArticleStatus[key].name;
                    });
                    return ret;
                }
                $scope.getActions = function (statusId: number) {
                    var actions = [];
                    var articleStatus = models.ArticleStatus;
                    var articleAction = models.ArticleAction;
                    //permission for reviewer
                    if ($scope.checkPermission($scope.permissionDefine.APPROVE) === true) {
                        switch (statusId) {
                            case articleStatus.PENDING.value:
                                actions = actions.concat([articleAction.APPROVED, articleAction.REJECTED]);
                                break;
                        }
                    }

                    //permission for editor
                    if ($scope.checkPermission($scope.permissionDefine.PREDITOR) === true) {
                        switch (statusId) {
                            case articleStatus.WAITING_FOR_SYNC_TO_CMS.value:
                                actions = actions.concat([articleAction.SYNC_CMS]);
                                break;
                        }
                    }
                    return actions;
                }

                $scope.doAction = function (item: models.Article, action: string) {
                    var _actions = models.ArticleAction;
                    var cloneItem = jQuery.extend(true, {}, item);

                    switch (action) {
                        case _actions.APPROVED:
                            $scope.selectItem = cloneItem;
                            jQuery("#approveModal").modal('show');
                            break;
                        case _actions.REJECTED:
                            $scope.selectItem = cloneItem;
                            jQuery("#rejectModal").modal('show');
                            break;
                        //case _actions.VIEW_REASON:
                        //    $scope.selectItem = cloneItem;
                        //    $scope.commentList = [];
                        //    articleService.listComments(cloneItem.id, 0, 1000, function (resp) {
                        //        if (!!resp && resp.length !== 0)
                        //            $scope.commentList = resp;
                        //        jQuery("#approveModal").modal('show');
                        //    });
                        //    break;
                        case _actions.SYNC_CMS:
                            var articleModal = $modal.open({
                                templateUrl: 'views/common/modal.delete',
                                controller: common.ModalDeleteController,
                                resolve: {
                                    checkedList: function () {
                                        var checkedList = [{ id: item.id, name: item.name }];//list of object contain id and name
                                        return checkedList;
                                    },
                                    type: function () {
                                        return 'sync_article';
                                    }
                                }
                            });

                            articleModal.result.then(function (checkList) {
                                var id = checkList[0].id;
                                factory.articleService.syncToCMS(cloneItem.id, function (resp) {
                                    if (resp) {
                                        if (resp.result) {
                                            listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.selectedStatus.value, $scope.query, $scope.orderBy, "asc");
                                            notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "synchronize", "article"));
                                            $window.open(common.Config.ADMINCMSZINGNEW + resp.result);
                                        } else {
                                            notify(new common.ActionMessage(common.ActionMessage.FAIL, "synchronize", "article"));
                                        }
                                    }
                                });
                            }, function (message) { });
                            break;
                        default:
                    }
                }

                $scope.view = (articleId) => {
                    $state.transitionTo("main.website.article_setting", { websiteId: $stateParams.websiteId, zoneId: $stateParams.zoneId, articleId: articleId });
                }

                $scope.approve = function () {
                    $scope.selectItem.status = models.ArticleStatus.changeStatus(models.ArticleAction.APPROVED, $scope.selectItem.status);
                    factory.articleService.update($scope.selectItem, function (ret) {
                        listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.selectedStatus.value, $scope.query, $scope.orderBy, "asc");
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "approve", "article"));
                        $scope.cancel();
                    })
                }
                $scope.reject = function () {
                    if ($scope.rejectReason && $scope.rejectReason.length !== 0) {
                        factory.articleService.addComment($scope.selectItem.id, $scope.rejectReason, function () { });
                    }
                    $scope.selectItem.status = models.ArticleStatus.changeStatus(models.ArticleAction.REJECTED, $scope.selectItem.status);
                    factory.articleService.update($scope.selectItem, function (ret) {
                        listZoneArticlesByQuery(($scope.pageIndex - 1) * $scope.pageSize, $scope.pageSize, $scope.selectedStatus.value, $scope.query, $scope.orderBy, "asc");
                        notify(new common.ActionMessage(common.ActionMessage.SUCCESS, "reject", "article"));
                        $scope.cancel();
                    })
                }
                $scope.cancel = function () {
                    jQuery("#approveModal").modal("hide");
                    jQuery("#rejectModal").modal("hide");
                    //jQuery("#reasonModal").modal("hide");
                }
                //$scope.formatCommentTime = function(time) {
                //    var curTime = new Date().getTime();
                //    var diff = curTime - time;
                //    if (diff < 3600000)
                //        return Math.floor(diff / 60000) + " minute" + (diff !== 60000 ? 's' : "") + " ago";
                //    return moment.unix(Math.floor(time / 1000)).format("H:mm DD/MM/YYYY");
                //}
        }
    }
}

function initialEditor() {
    function initToolbarBootstrapBindings() {
        var fonts = ['Serif', 'Sans', 'Arial', 'Arial Black', 'Courier',
            'Courier New', 'Comic Sans MS', 'Helvetica', 'Impact', 'Lucida Grande', 'Lucida Sans', 'Tahoma', 'Times',
            'Times New Roman', 'Verdana'],
            fontTarget = $('[title=Font]').siblings('.dropdown-menu');
        $.each(fonts, function (idx, fontName) {
            fontTarget.append($('<li><a data-edit="fontName ' + fontName + '" style="font-family:\'' + fontName + '\'">' + fontName + '</a></li>'));
        });
        $('a[title]').tooltip({ container: 'body' });
        $('.dropdown-menu input').click(function () { return false; })
            .change(function () { $(this).parent('.dropdown-menu').siblings('.dropdown-toggle').dropdown('toggle'); })
            .keydown('esc', function () { this.value = ''; $(this).change(); });

        $('[data-role=magic-overlay]').each(function () {
            var overlay = $(this), target = $(overlay.data('target'));
            overlay.css('opacity', 0).css('position', 'absolute').offset(target.offset()).width("100%").height("100%");
        });
        if ("onwebkitspeechchange" in document.createElement("input")) {
            var editorOffset = $('#editor').offset();
            $('#voiceBtn').css('position', 'absolute').offset({ top: editorOffset.top, left: editorOffset.left + $('#editor').innerWidth() - 35 });
        } else {
            $('#voiceBtn').hide();
        }
    };
    function showErrorAlert(reason, detail) {
        var msg = '';
        if (reason === 'unsupported-file-type') { msg = "Unsupported format " + detail; }
        else {
            console.log("error uploading file", reason, detail);
        }
        $('<div class="alert"> <button type="button" class="close" data-dismiss="alert">&times;</button>' +
            '<strong>File upload error</strong> ' + msg + ' </div>').prependTo('#alerts');
    };
    initToolbarBootstrapBindings();
    (<JQueryCustom>$('#editor')).wysiwyg({ fileUploadError: showErrorAlert });
}