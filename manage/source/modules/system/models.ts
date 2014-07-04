/// <reference path="../../common/common.ts"/>

module models {
    'use strict';
    export class ConfigSystem extends common.Item{
        key: string;
        value: string;
        constructor(id: number, key: string, value: string) {
            super(id);
            this.key = key;
            this.value = value;
        }
    }
}
