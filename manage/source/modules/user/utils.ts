'use strict'
module utils {

    export class PermissionUtils {
        private permissionService: backend.IPermission;
        private permissionCache: {};

        //define object permission
        static ALLWEBSITE = "allwebsite";
        static WEBSITE = "website";
        static ALLORDER = "allorder";
        static ORDER = "order";
        static UNKNOWN = "";
        rest: backend.IRestfulService;

        constructor(IRestfulService) {
            this.permissionService = new backend.PermissionService(IRestfulService);
            this.rest = IRestfulService;
            this.permissionCache = {};
        }

        getPermission(object: string, id: number, callback) {
            this.permissionService.getCurrentUserPermission(object, id, (res) => {
                //callback(-1);
                if (res && !isNaN(parseInt(res))) {
                    callback(parseInt(res));
                }
            });
        }

        getPermissions(object: string, ids: number[], callback) {
            this.permissionService.getCurrentUserPermissions(object, ids, function (res: models.Permission[]) {
                if (res)
                    callback(res);
            });
        }
    }
}
