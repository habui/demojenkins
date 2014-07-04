'use strict';
module models {
    // Class
    export class RoleInfo extends common.Item {
        name: string;
        description: string;
        permissions: number;
        objName: string;
        // Constructor
        constructor(id: number, name: string, description: string, permissions: number, objName: string) {
            super(id);
            this.name = name;
            this.description = description;
            this.permissions = permissions;
            this.objName = objName;
        }
    }

}