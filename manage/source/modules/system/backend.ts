module backend {
    'use strict';
    export interface ISystemService extends backend.IDataService<models.ConfigSystem> {
        
    }

    export class SystemService extends HttpDataService<models.ConfigSystem> {
        
    }
}
