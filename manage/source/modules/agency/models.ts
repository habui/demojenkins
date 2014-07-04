'use strict';
module models {
    export class AgencyAccount extends UserInfo {
        confirmemail: string;
        ownerId: number;
        constructor(id: number, name: string,email: string,confirmemail:string,password: string) {
            super(id, name);
            this.name = name;
            this.email = email;
            this.password = password;
            this.confirmemail = confirmemail;
        }
    }

    ////////////////////////
    export class AgencyAccountOrder extends common.Item {
        orderId: number;
        uname: string;
        reporter: boolean;
        standarduser: boolean;

        constructor(id: number, orderId: number,uname: string, reporter: boolean, standarduser: boolean) {
            super(id);
            this.orderId = orderId;          
            this.reporter = reporter;
            this.standarduser = standarduser;
            this.uname = uname;
        }
    }
    export class AgencyAccountWebsite extends common.Item {
        websiteId: number;
        uname: string;
        viewer: boolean;
        reporter: boolean;
        saleman: boolean;
        approver: boolean;
        constructor(id: number, orderId: number, uname: string, viewer: boolean, reporter: boolean, saleman: boolean, approver: boolean) {
            super(id);
            this.websiteId = orderId;
            this.viewer = viewer;
            this.reporter = reporter;
            this.saleman = saleman;
            this.approver = approver;
            this.uname = uname;
        }
    }
    
}