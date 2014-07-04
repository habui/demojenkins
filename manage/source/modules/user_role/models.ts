'use strict';
module models {
    // Class
    export class UserRoleInfo extends common.Item {
        name: string;
        email: string;
        roles: models.RoleInfo[];
        // Constructor
        constructor(id: number, name: string, email: string, roles: models.RoleInfo[]) {
            super(id);
            this.roles = roles;
            this.name = name;
            this.email = email;
        }
    }

}