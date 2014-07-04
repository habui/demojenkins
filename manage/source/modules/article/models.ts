/// <reference path="../../common/common.ts"/>

module models {

    export class Article extends common.Item {
        // Article Properties
        name: string;
        tags: string;
        author: string;
        source: string;
        createDate: number;
        updateDate: number;
        publishedDate: number;
        isReview: boolean;
        status: number;
        pubArticleId: number;
        notes: Array;
        trackingLink: string;

        // Article Contents
        contentType: string;
        content: string;
        summary: string;
        photos: Array<string>;
        videos: Array<string>;
        campaignId: number;
        zoneId: number;
        websiteId: number;
        comments: ArticleComment[];
        creator: models.UserInfo;
        kind: string;
        retargeting: number;
        retargetingEnable: boolean;
        category: number;
        price: number;
        companionTargeting: string;

        constructor(id: number, campaignId: number) {
            super(id);
            this.campaignId = campaignId;
            this.photos = [];
            this.videos = [];
            this.kind = models.CampaignItemType.NETWORK.PR;
            this.retargetingEnable = false;
        }
    }
    export class ArticleComment extends common.Item{
        articleId: number;
        comment: string;
        kind: string;
        owner: models.UserInfo;
        date: number

        constructor(articleId, comment) {
            super(0);
            this.articleId = articleId;
            this.comment = comment;
        }
    }

    export class ArticleStatus {
        static PENDING: { name: string; value: number; user: string } =               {name: "Pending", value: 1, user: "Reviewer"};
        static COMPOSING: { name: string; value: number; user: string } = { name: "--", value: 2, user: "Editor" };
        static REJECT_BY_REVIEWER: { name: string; value: number; user: string } = { name: "Reject by reviewer", value: 3, user: "Saleman" };
        static WAITING_APPROVAL_BY_SALE: { name: string; value: number; user: string } = { name: "--", value: 4, user: "Saleman" };
        static WAITING_FOR_SYNC_TO_CMS: { name: string; value: number} = { name: "Waiting for sync to CMS", value: 5 };
        static WAITING_APPROVAL_BY_PUBLISHER: { name: string; value: number; user: string } = { name: "Waiting for approval by publisher", value: 6, user: "Publisher" };
        static WAITING_FOR_PUBLISHING: { name: string; value: number} =        { name: "Waiting for publishing", value: 7 };
        static PUBLISHED: { name: string; value: number} =                     { name: "Published", value: 8 };
        static EXPIRED: { name: string; value: number } = { name: "Expired", value: 9 };
        static COMPOSING_AFTER_REJECTED_BY_PUBLISHER: { name: string; value: number; user: string } = { name: "Composing after rejected by publisher", value: 10, user: "Editor"};

        static changeStatus(action, currentStatus) {
            var disStatus = 0;
            switch (currentStatus) {
                case this.PENDING.value:
                    if (action === ArticleAction.APPROVED)
                        disStatus = this.WAITING_FOR_SYNC_TO_CMS.value;
                    else if (action === ArticleAction.REJECTED)
                        disStatus = this.REJECT_BY_REVIEWER.value;
                    break;
                case this.WAITING_FOR_SYNC_TO_CMS.value:
                    if (action === ArticleAction.SYNC_CMS)
                        disStatus = this.WAITING_APPROVAL_BY_PUBLISHER.value;
                    break;
                case this.REJECT_BY_REVIEWER.value:
                    if (action === ArticleAction.EDITED)
                        disStatus = this.PENDING.value;
                    break;
                case this.WAITING_APPROVAL_BY_PUBLISHER.value:
                    break;
                case this.WAITING_FOR_PUBLISHING.value:
                    break;
            }
            return disStatus;
        }

    }

    export class ArticleAction {
        static REJECTED = "Reject";
        static APPROVED = "Approve";
        static REQUEST_EDIT = "Request edit";
        static EDITED = "Edit";
        static DELETED = "Delete";
        static VIEW_REASON = "Reason";
        static VIEW_REPORT = "View report";
        static SYNC_CMS = "Sync to CMS";
    }

    export var articleCategoryById =  {
        "0": "Thế giới",
        "1": "Văn hóa",
        "2": "Xã hội",
        "3": "Kinh tế",
        "4": "Công nghệ",
        "5": "Thể thao",
        "6": "Giải trí",
        "7": "Pháp luật",
        "8": "Giáo dục",
        "9": "Sức khỏe",
        "10": "Xe 360",
        "11": "Bất động sản"
    }
}