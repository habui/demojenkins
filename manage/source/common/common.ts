'use strict';
module common {
    export class Item {
        id: number;
        constructor(id: number) {
            this.id = id;
        }
    }
    export class ItemType {
        code: number;
        name: string;
        type: string;
        constructor(type: string, code: number, name: string) {
            this.code = code;
            this.name = name;
            this.type = type;
        }
    }

    export enum EMessageType {
        INFO = 0,
        SUCCESS = 1,
        ERROR = 2
    }
    export class ResultMessage {
        type: EMessageType;
        message: string;
        constructor(type: EMessageType, message: string) {
            this.type = type;
            this.message = message;
        }
    }

    export class Tab {
        tabName: string;
        tabChildName: string;
        constructor(tabName: string, tabChild: string) {
            this.tabName = tabName;
            this.tabChildName = tabChild;
        }
    }
}