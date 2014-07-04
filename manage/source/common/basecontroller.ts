/// <reference path="../modules/user/models.ts"/>
/// <reference path="../modules/backend/data.ts"/>

module common {
    'use strict';
    
    export class ModalDeleteController {
        constructor($scope, $modalInstance, checkedList, type) {            
            $scope.title = "Delete confirm"
            switch (type) {
                case 'zone':
                    $scope.bodyMessage = "Are you sure to disable these zone(s) ?";
                    break;
                case 'zone_group':
                    $scope.bodyMessage = "Are you sure to disable these zone group(s) ?";
                    break;
                case  'website':
                    $scope.bodyMessage = "Are you sure to disable these website(s) ?";
                    break;
                case 'order':
                    $scope.bodyMessage = "Are you sure to disable these order(s) ?";
                    break;
                case 'campaign':
                    $scope.bodyMessage = "Are you sure to disable these campaign(s) ?";
                    break;
                case  'campaignItem':
                    $scope.bodyMessage = "Are you sure to disable these item(s) ?";
                    break;
                case  'unlink_campaignItem':
                    $scope.bodyMessage = "Are you sure to unlink these item(s) ?";
                    $scope.title = "Unlink confirm"
                    break;
                case 'unlink_item_zone':
                    $scope.bodyMessage = "Are you sure to unlink all items from following zone ?";
                    $scope.title = "Unlink confirm"
                    break;
                case 'enable_website':
                    $scope.bodyMessage = "Are you sure to enable these website(s) ?";
                    $scope.title = "Enable confirm"
                    break;
                case  'enable_zone':
                    $scope.bodyMessage = "Are you sure to enable these zone(s) ?";
                    $scope.title = "Enable confirm"
                    break;
                case 'enable_item':
                    $scope.bodyMessage = "Are you sure to enable these item(s) ?";
                    $scope.title = "Enable confirm"
                    break;
                case "conversion":
                    $scope.bodyMessage = "Are you sure to delete these(s) conversion ?"
                    $scope.title = "Delete conversion";
                    break;
                case "delete_booking_item":
                    $scope.bodyMessage = "Are you sure to delete these(s) booking?"
                    $scope.title = "Delete booking item";
                    break;
                case (type.match(/delete_item_\w+/)||[])[0]:
                    var itemName = type.replace("delete_item_", "");
                    $scope.bodyMessage = "Are you sure to delete these(s) " + itemName + "?";
                    $scope.title = "Delete " + itemName + " item";
                    break;
                case 'article':
                    $scope.bodyMessage = "Are you sure to delete these articles?"
                    $scope.title = "Delete article";
                    break;
                case "approve_article":
                    $scope.bodyMessage = "Are you sure you want to approve these articles before it going to live?";
                    break;
                case "reject_article":
                    $scope.bodyMessage = "Are you sure you to reject these articles?";
                    break;
                case 'enable_camp':
                    $scope.bodyMessage = "Are you sure to enable these campaign(s) ?";
                    $scope.title = "Enable confirm"
                    break;
                case "sync_article":
                    $scope.bodyMessage = "Are you sure to synchronise this article to CMS ?";
                    $scope.title = "Article";
                    break;
                case 'agency':
                    $scope.bodyMessage = "Are you sure to disable these agency account(s) ?";
                    break;
                case "enable_agency":
                    $scope.bodyMessage = "Are you sure to enable these agency account(s) ?";
                    $scope.title = "Enable confirm"
                    break;
                default:
            }

            $scope.items = checkedList;
            $scope.ok = () => {
                $modalInstance.close(checkedList);
            }
            $scope.cancel = () => {
                $modalInstance.dismiss('message');
            }
        }
    }

    export class ModalConfirmController {
        constructor($scope, $modalInstance, title, bodyMessage, data) {                        
            $scope.title = title;
            $scope.bodyMessage = bodyMessage;
            $scope.ok = () => {
                $modalInstance.close(data);
            }
            $scope.cancel = () => {
                $modalInstance.dismiss('message');
            }
        }
    }
}