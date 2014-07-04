/// <reference path="../../common/scopes.ts"/>
/// <reference path="../../common/common.ts"/>
'use strict';

module scopes {

    export interface ICampaignArticleListScope extends common.IListScope<models.Article> {
        orderId: number;
        campaignId: number;
        dateParse(input: number);
        formatCommentTime(time: number);
        filter: {
            selectedStatus: { name: string; value: number }
        };
        query: string;
        orderDir: string;
        orderBy: string;
        articleStatus: Array<{ name: string; value: number }>;
        // scope functions
        search();
        goToArticle(campaignId: number, articleId: number);
        getStatus(statusValue: number);
        siteNameDic: {};
        zoneNameDic: {};
        checkBoxes: {};
        getActions(articleId: number);
        doAction(item: models.Article, action);
        view(articleId);
        commentList: models.ArticleComment[];

        selectItem: models.Article;
        rejectReason:{content: string};
        approve();
        reject();
        cancel();
    }
    export interface ICreateArticleScope extends common.IItemScope<models.Article> {
        isUpdate: boolean;
        isPreview: boolean;
        isCreate: boolean;
        orderId: number;
        campaignId: number;
        checkValidate: boolean;
        contentUploadResult: string;
        publishers: Array<any>;
        selectedZones: Array<any>;
        checkBoxZone: {};
        checkBoxSite: {};
        publishedDate: any;
        photoUploadResults: {};
        videoUploadResults: {};
        isReview: number;
        searchPublishers: {};
        siteDic: {};
        zoneNameDic: {};
        comment: string;
        commentList: models.ArticleComment[];
        formatCommentTime(time);
        // API link to upload file
        uploadUrl: string;
        getStatus(statusValue: number);

        // Button actions on form
        selectZone(id: number, siteId: number);
        selectSite(id: number);
        removeZone(id: number);
        photoAdd();
        photoRemove(id: number);
        videoAdd();
        videoRemove(id: number);
        videoPreview(id: number);
        save();
        cancel();
        isApprove();
        isPublisher();
        approve(action: string);
        sendComment();
        sync2CMS();
        isSubmited: boolean;
        viewContent();
    }

    export interface IZoneArticlesListScope extends common.IListScope<models.ArticleItem> {
        websiteId: number;
        zoneId: number;
        dateParse(input: number);
        formatCommentTime(time);
        selectedStatus: { name: string; value: number };
        query: string;
        orderDir: string;
        orderBy: string;
        articleStatus: Array<{ name: string; value: number }>;
        // scope functions
        search();
        goToArticle(campaignId: number, articleId: number);
        getStatus(statusValue: number);
        getActions(articleId: number);
        doAction(item: models.Article, action);
        view(articleId);
        commentList: models.ArticleComment[];

        selectItem: models.Article;
        rejectReason: string;
        approve();
        reject();
        cancel();
    }
}