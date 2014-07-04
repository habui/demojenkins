/// <reference path="./libs/jquery.d.ts"/>
/// <reference path="./libs/jquery.flot.d.ts"/>
/// <reference path="libs/jquery.form.d.ts"/>
/// <reference path="libs/jquery.gantt.d.ts"/>

var mod = angular.module('app.directives', []);

mod.directive('paging', function ($location) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/common/pagination',
        scope: {
            total: '@total',
            pagesize: '=',
            pageindex: '=',
            onchange: '&'
        },
        link: function (scope, element, attrs) {
            scope.choose = (value: number) => {
                scope.pagesize = value;
                scope.pageindex = 1;
                scope.onchange({ start: 0, size: value});

            };

            scope.isShow = function () {
                if (parseInt(scope.total) > parseInt(scope.pagesize))
                    return true;
                return false;
            };

            scope.range = function () {
                var num: number = Math.ceil(scope.total / scope.pagesize);
                return new Array(num);
            };


            scope.checkDisable = function (index, type) {
                if (type === 'prev') {
                    if (index == 1)
                        return "disabled";
                }

                if (type === 'next') {
                    if (index >= Math.ceil(scope.total / scope.pagesize))
                        return "disabled";
                }
                return "active";
            };

            

            scope.getPageNum = function () {
                if (scope.pagesize === null || scope.pagesize === undefined || scope.pagesize === 0)
                    return 0;
                return Math.ceil(parseInt(scope.total) / parseInt(scope.pagesize));
            };

            scope.goto = function () {
                //if (scope.pageindex <= 0 || scope.pageindex > Math.ceil(scope.total / scope.pagesize))
                //    return;
                //$location.search('page', scope.pageindex);
            };

            scope.gotoPage = function (index) {
                var page = parseInt(index);
                if (page <= 0 || page > scope.getPageNum())
                    return;
                $location.search('page', page);
            };

            scope.prev = function () {
                if (parseInt(scope.pageindex) === 1)
                    return;
                scope.pageindex--;
                var start = (scope.pageindex  - 1) * scope.pagesize;
                scope.onchange({ start: start, size: scope.pagesize})
            };

            scope.next = function () {
                if (parseInt(scope.pageindex) === Math.ceil(scope.total / scope.pagesize))
                    return;
                var start: number = scope.pageindex * scope.pagesize;
                scope.pageindex++;
                scope.onchange({ start: start, size: parseInt(scope.pagesize)});
            };

            scope.getNumRecord = function () {
                if (scope.total === null || scope.total === undefined)
                    return 0;

                return parseInt(scope.total);
            };

            scope.getStartIndex = function () {
                if (scope.total == 0)
                    return 0;
                return (parseInt(scope.pageindex) - 1) * parseInt(scope.pagesize) + 1;
            };

            scope.getEndIndex = function () {
                if (parseInt(scope.total) > parseInt(scope.pagesize)) {
                    var startIndex = (parseInt(scope.pageindex) - 1) * parseInt(scope.pagesize);
                    if ((startIndex + parseInt(scope.pagesize)) < scope.total)
                        return startIndex + parseInt(scope.pagesize);
                }
                return parseInt(scope.total);
            };

            scope.validate = function () {
                var inputVal = scope.pageindex;
                inputVal = inputVal.replace(/[^0-9]+/g, "");
                if (inputVal != "" && (parseInt(inputVal) < 1 ||
                    parseInt(inputVal) > scope.getPageNum()))
                    scope.pageindex = 1;
                else
                    scope.pageindex = inputVal;
                var start = (scope.pageindex - 1) * scope.pagesize;
                scope.onchange({ start: start, size: scope.pagesize });
            }
        }
    };
});

mod.directive('selectBox', function ($location) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/common/category.selectbox.listtype',
        scope: {
            data: '='
            , selectedData: '='
            //,checkedMap : '@checkedMap'
        },
        link: function (scope, element, attrs) {
            //init checkMap base on data and selected data
            if (scope.selectedData == null || scope.selectedData == undefined) {
                scope.selectedData = [];
            }
            scope.checkedMap = {};

            scope.$watch('chechMap', function () {
                scope.selectedData.forEach(function (obj) {
                    scope.checkedMap[obj.id] = true;
                });
            });
            scope.itemChoice = (id: number) => {
                //remove in selected
                if (scope.checkedMap[id] == false) {
                    scope.selectedData.forEach(function (obj) {
                        if (obj.id == id) {
                            var index = scope.selectedData.indexOf(obj);
                            scope.selectedData.splice(index, 1);
                            scope.checkedMap[id] = false;
                        }
                    });
                } else {
                    scope.data.forEach(function (obs) {
                        if (obs.id == id) {
                            scope.selectedData.push(obs);
                            scope.checkedMap[id] = true;
                        }
                    });
                }
            };

            scope.itemRemove = (id: number) => {
                scope.selectedData.forEach(function (obj) {
                    if (obj.id == id) {
                        var index = scope.selectedData.indexOf(obj);
                        scope.selectedData.splice(index, 1);
                        scope.checkedMap[id] = false;
                    }
                });
            };
        }
    };
});

mod.directive('selectBox2', function ($location) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/common/category.selectbox.listtype',
            scope: {
                data: '=',
                selectedData: '='
            },
            link: function (scope, element, attrs) {
                if (scope.selectedData == null || scope.selectedData == undefined) {
                    scope.selectedData = [];
                }              

                scope.checkParent = (id: number) => {
                    var i = 0;
                    var sel = null;
                    for (i = 0; i < scope.data.length; i++) {
                        if (scope.data[i].id == id) {
                            sel = scope.data[i];

                            break;
                        }
                    }
                    if (sel != null) {
                        if (sel.checked == undefined || sel.check == null || sel.checked == false) {
                            sel.checked = true;
                            sel.childs.forEach(function (object: any) {
                                object.checked = true;
                            });
                            //add to selectedData
                            scope.selectedData.push(sel);
                        }
                        else if (sel.checked == true) {
                            sel.checked = false;
                            sel.childs.forEach(function (object: any) {
                                object.checked = false;
                            });
                            //remove from selected Data
                            for (var j = 0; j < scope.selectedData.length; j++) {
                                if (scope.selectedData[i].id == id) {
                                    scope.selectedData.splice(i, 1);
                                    break;
                                }
                            }
                        }
                    }

                }
                scope.checkChild = (pid : number, id: number) => {
                    //  check/uncheck from source data
                    var par = null;
                    var sel = null;
                    for (var i = 0; i < scope.data.length; i++) {
                        if (scope.data[i].id === pid) {
                            par = scope.data[i];
                            for (var j = 0; j < scope.data[i].childs.length; j ++) {
                                if (scope.data[i].childs[j].id == id) {
                                    sel = scope.data[i].childs[j];
                                }
                            }
                        }
                    }
                    if (sel != null) {
                        if (sel.checked == false) {
                            sel.checked = true;
                            //add to selected cate
                            

                        } else if (sel.checked == true) {
                            sel.checked = false;
                        }
                    }
                    
                    

                }
                scope.selectCate = (cate: common.ItemType) => {
                    var i = scope.data.length;
                    while (i--) {
                        if (scope.data[i]['code'] == cate.code) {
                            scope.selectedData.push(scope.data[i]);
                            scope.data.splice(i, 1);
                            break;
                        }
                    }
                };
                scope.unselectCate = (cate: common.ItemType) => {
                    var i = scope.selectedData.length;
                    while (i--) {
                        if (scope.selectedData[i]['code'] == cate.code) {
                            scope.data.push(scope.selectedData[i]);
                            scope.selectedData.splice(i, 1);
                            break;
                        }
                    }
                };
            }
        };
});

mod.directive('checkboxGroup', function () {
    return {
        restrict: 'E',
        controller: function ($scope, $attrs) {
            var self = this;
            var ngModels = [];
            var minRequired;
            self.validate = function () {
                var checkedCount = 0;
                angular.forEach(ngModels, function (ngModel) {
                    if (ngModel.$modelValue) {
                        checkedCount++;
                    }
                });
                var minRequiredValidity = checkedCount >= minRequired;
                angular.forEach(ngModels, function (ngModel) {
                    ngModel.$setValidity('checkboxGroup-minRequired', minRequiredValidity, self);
                });
            };

            self.register = function (ngModel) {
                ngModels.push(ngModel);
            };

            self.deregister = function (ngModel) {
                if (this.ngModels === null || this.ngModels === undefined)
                    return;
                var index = this.ngModels.indexOf(ngModel);
                if (index != -1) {
                    this.ngModels.splice(index, 1);
                }
            };

            $scope.$watch($attrs.minRequired, function (value) {
                minRequired = parseInt(value, 10);
                self.validate();
            });
        }
    };
});

mod.directive('input', function () {
    return {
        restrict: 'E',
        require: ['?^checkboxGroup', '?ngModel'],
        link: function (scope, element, attrs, controllers) {
            var checkboxGroup = controllers[0];
            var ngModel = controllers[1];
            if (attrs.type == 'checkbox' && checkboxGroup && ngModel) {
                checkboxGroup.register(ngModel);
                scope.$watch(function () { return ngModel.$modelValue; }, checkboxGroup.validate);
                // In case we are adding and removing checkboxes dynamically we need to tidy up after outselves.
                scope.$on('$destroy', function () { checkboxGroup.deregister(ngModel); });
            }
        }
    };
});

// hook enter key press event
mod.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if (event.which === 13) {
                scope.$apply(function () {
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});

mod.directive('booking', function ($timeout) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/order/booking.chart',
        scope: {
            data: '=',
            item: '=',
            selecteditem: '=',
            setfn: '&',
            editbookingfn: '&',
            sitename: '=',
            starttime: '=',
            endtime: '=',
            xcoor: '=',
            ycoor: '=',
            exdata: '=',
            addbookingfn: '&',
            removebookingfn: '&'
        },
        link: function (scope, element, attrs) {
            scope.currentItem = null;
            scope.item = new scopes.BookingItem(0, 0, 0, 0);
            scope.dicAvailable = null;
            scope.addbooktimeline = null;
            scope.$watch('data', function (newVal, oldVal) {
                if (newVal === null || newVal.type === null || newVal.type === undefined)
                    return;
                if (newVal.type === "booking") {
                    $timeout(function () {
                        (<JQueryGantt>jQuery(".gantt")).gantt({
                            source: newVal.data,
                            navigate: "scroll",
                            scale: "days",
                            maxScale: "months",
                            minScale: "days",
                            itemsPerPage: 10,
                            minDate: newVal.minDate,
                            maxDate: newVal.maxDate,
                            onItemClick: function (item, x, y, zoneId, zoneName, startTime, rowIndex) {
                                if ($(item).data("dataObj") !== null && $(item).data("dataObj") !== undefined) {
                                    if (scope.currentItem !== null) {
                                        $(scope.currentItem).css("border-color", "").css("border-style", "");
                                    }
                                    if (scope.selecteditem !== null && scope.selecteditem !== undefined)
                                        $(scope.selecteditem).css("border-color", "").css("border-style", "");
                                    $(item).css("border-color", "#8E2F44")
                                        .css("border-style", "solid");
                                    scope.currentItem = item;
                                    scope.selecteditem = item;
                                    scope.$apply();
                                }
                                scope.xcoor = x; scope.ycoor = y;
                                scope.exdata = { start: startTime, zonename: zoneName, zoneid: zoneId, rowindex: rowIndex };
                                scope.$apply();
                                scope.editbookingfn();
                            },
                            onAddClick: function (dt, rowId, rowIndex, rowName, x, y) {
                                if (scope.item !== null && scope.item !== undefined) {
                                    if (scope.currentItem !== null)
                                        scope.currentItem.remove();
                                    scope.item = null;
                                }
                                scope.xcoor = x; scope.ycoor = y;
                                scope.exdata = { start: dt, zonename: rowName, zoneid: rowId, rowindex: rowIndex };
                                scope.$apply();
                                scope.editbookingfn();
                            },
                            onRender: function (data, fn) {
                                scope.dicAvailable = data;
                                scope.addbooktimeline = fn;
                            },
                            onMouseDrag: function (item, zoneid, zonename, start_time, end_time, percent, xcoor, ycoor) {
                                if (item !== null && zoneid !== null && zoneid !== undefined && start_time > 0 && end_time > 0) {
                                    var today: Date = new Date();
                                    var curTime = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
                                    if (parseInt(end_time) <= curTime) return;
                                    if (parseInt(start_time) < curTime)
                                        start_time = curTime;
                                    scope.currentItem = item;
                                    scope.item = new scopes.BookingItem(zoneid, start_time, end_time, percent);
                                    scope.item.share = percent;
                                    scope.xcoor = xcoor; scope.ycoor = ycoor;
                                    scope.exdata = { start: start_time, end: end_time, zonename: zonename, zoneid: zoneid };
                                    scope.$apply();
                                    scope.editbookingfn();
                                }
                            }
                        });
                    }, 200);
                } else if (newVal.type === "mediaplan") {
                    $timeout(function () {
                        (<JQueryGantt>jQuery(".gantt")).gantt({
                            source: newVal.data,
                            navigate: "scroll",
                            scale: "days",
                            maxScale: "months",
                            minScale: "days",
                            itemsPerPage: 10,
                            minDate: newVal.minDate,
                            maxDate: newVal.maxDate,
                            preventDrag: true,
                            showDesciption: true,
                            onItemClick: function (item, x, y) {
                                if (scope.currentItem !== null) {
                                    $(scope.currentItem).css("border-color", "").css("border-style", "");
                                }
                                $(item).css("border-color", "#8E2F44")
                                    .css("border-style", "solid");
                                scope.currentItem = item;
                                scope.selecteditem = item;

                                scope.xcoor = x; scope.ycoor = y;
                                var data: any = $(item).data("dataObj");
                                scope.item = new scopes.BookingItem(data.zoneId, data.from, data.to, 0);
                                scope.item.share = data.share;
                                scope.item.id = parseInt(data.id);
                                scope.$apply();
                                scope.editbookingfn();

                            },
                            onAddClick: function (dt, rowId, rowIndex) {
                                // TODO :
                            },
                        });
                    }, 200);
                } else if (newVal.type === "zone_booking_schedule") {
                    $timeout(function () {
                        (<JQueryGantt>jQuery(".gantt")).gantt({
                            source: newVal.data,
                            navigate: "scroll",
                            scale: "days",
                            maxScale: "months",
                            minScale: "days",
                            itemsPerPage: 10,
                            minDate: newVal.minDate,
                            maxDate: newVal.maxDate,
                            onItemClick: function (data) {
                                // no action
                            },
                            onAddClick: function (dt, rowId, rowIndex, rowName, x, y) {
                                // no action
                            },
                            onRender: function (data, fn) {
                                // no action
                            },
                            onMouseDrag: function (item, zoneid, zonename, start_time, end_time, percent, xcoor, ycoor) {
                                // no action
                            }
                        });
                    }, 200);
                }
            }, true);

            scope.book = function () {
                if (scope.currentItem !== null) {
                    $(scope.currentItem).data('booked', true);
                    $(scope.currentItem).data("dataObj", { zoneId: scope.item.zoneid, from: scope.item.start, to: scope.item.end, share: scope.item.share});
                    $(scope.currentItem).children('div').text('Booked : ' + scope.item.share + '%');
                    var listAvailable: Array<any> = scope.dicAvailable[scope.item.zoneid];
                    if (listAvailable === undefined || listAvailable === null)
                        listAvailable = [];

                    var tmpListAvailable: Array<any> = [];
                    var isPushed: boolean = false;
                    for (var i: number = 0; i < listAvailable.length; i++) {
                        var timeAvailable: any = listAvailable[i];

                        if (isPushed) {
                            tmpListAvailable.push(timeAvailable);
                            continue;
                        }

                        var startTime: number = scope.item.start + new Date().getTimezoneOffset() * 60000;
                        var endTime: number = scope.item.end + new Date().getTimezoneOffset() * 60000;

                        if (endTime <= timeAvailable.from) {
                            var available: number = scope.item.percent - scope.item.share;
                            tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                            tmpListAvailable.push(timeAvailable);
                            isPushed = true;
                            continue;
                        }

                        if (startTime < timeAvailable.from && endTime <= timeAvailable.to) {
                            var available: number = scope.item.percent - scope.item.share;
                            tmpListAvailable.push({ from: startTime, to: timeAvailable.from, val: 100 - scope.item.share });
                            tmpListAvailable.push({ from: timeAvailable.from, to: endTime, val: available });
                            tmpListAvailable.push({ from: endTime, to: timeAvailable.to, val: timeAvailable.val });
                            isPushed = true;
                            continue;
                        }

                        if (startTime < timeAvailable.from && timeAvailable.to < endTime) {
                            var available: number = scope.item.percent - scope.item.share;
                            tmpListAvailable.push({ from: startTime, to: timeAvailable.from, val: 100 - scope.item.share });
                            tmpListAvailable.push({ from: timeAvailable.from, to: timeAvailable.to, val: available });
                            tmpListAvailable.push({ from: timeAvailable.to, to: endTime, val: 100 - scope.item.share });
                            isPushed = true;
                            continue;
                        }

                        if (timeAvailable.from <= startTime && endTime <= timeAvailable.to) {
                            var available: number = scope.item.percent - scope.item.share;
                            tmpListAvailable.push({ from: timeAvailable.from, to: startTime, val: timeAvailable.val });
                            tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                            tmpListAvailable.push({ from: endTime, to: timeAvailable.to, val: timeAvailable.val });
                            isPushed = true;
                            continue;
                        }

                        if (timeAvailable.from <= startTime && startTime < timeAvailable.to && timeAvailable.to < endTime) {
                            var available: number = scope.item.percent - scope.item.share;
                            tmpListAvailable.push({ from: timeAvailable.from, to: startTime, val: timeAvailable.val });
                            tmpListAvailable.push({ from: startTime, to: timeAvailable.to, val: available });
                            tmpListAvailable.push({ from: timeAvailable.to, to: endTime, val: 100 - scope.item.share });
                            isPushed = true;
                            continue;
                        }

                        tmpListAvailable.push(timeAvailable);
                    }
                    if (!isPushed) {
                        var available: number = scope.item.percent - scope.item.share;
                        var startTime: number = scope.item.start + new Date().getTimezoneOffset() * 60000;
                        var endTime: number = scope.item.end + new Date().getTimezoneOffset() * 60000;
                        tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                    }

                    scope.dicAvailable[scope.item.zoneid] = tmpListAvailable;
                }
            };

            scope.setfn({ theDirFn: scope.book });

            scope.insertbook = () => {
                scope.addbooktimeline(scope.exdata.zoneid, scope.exdata.rowindex, scope.exdata.from, scope.exdata.to, scope.exdata.share);
                scope.updateAvailables(scope.exdata.zoneid, scope.exdata.from, scope.exdata.to, scope.exdata.share, scope.exdata.percent);
            }

            scope.addbookingfn({ theDirFn: scope.insertbook });

            scope.getFormat = (timestamp: number): string => {
                return moment.unix(Math.round(timestamp / 1000)).format('DD/MM/YYYY');
            }

            scope.updateAvailables = (zoneid: number, startTime: number, endTime: number, share: number, percent: number) => {
                var listAvailable: Array<any> = scope.dicAvailable[zoneid];
                if (listAvailable === undefined || listAvailable === null)
                    listAvailable = [];

                var tmpListAvailable: Array<any> = [];
                var isPushed: boolean = false;
                for (var i: number = 0; i < listAvailable.length; i++) {
                    var timeAvailable: any = listAvailable[i];

                    if (isPushed) {
                        tmpListAvailable.push(timeAvailable);
                        continue;
                    }

                    if (endTime <= timeAvailable.from) {
                        var available: number = percent - share;
                        tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                        tmpListAvailable.push(timeAvailable);
                        isPushed = true;
                        continue;
                    }

                    if (startTime < timeAvailable.from && endTime <= timeAvailable.to) {
                        var available: number = percent - share;
                        tmpListAvailable.push({ from: startTime, to: timeAvailable.from, val: 100 - share });
                        tmpListAvailable.push({ from: timeAvailable.from, to: endTime, val: available });
                        tmpListAvailable.push({ from: endTime, to: timeAvailable.to, val: timeAvailable.val });
                        isPushed = true;
                        continue;
                    }

                    if (startTime < timeAvailable.from && timeAvailable.to < endTime) {
                        var available: number = percent - share;
                        tmpListAvailable.push({ from: startTime, to: timeAvailable.from, val: 100 - share });
                        tmpListAvailable.push({ from: timeAvailable.from, to: timeAvailable.to, val: available });
                        tmpListAvailable.push({ from: timeAvailable.to, to: endTime, val: 100 - share });
                        isPushed = true;
                        continue;
                    }

                    if (timeAvailable.from <= startTime && endTime <= timeAvailable.to) {
                        var available: number = percent - share;
                        tmpListAvailable.push({ from: timeAvailable.from, to: startTime, val: timeAvailable.val });
                        tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                        tmpListAvailable.push({ from: endTime, to: timeAvailable.to, val: timeAvailable.val });
                        isPushed = true;
                        continue;
                    }

                    if (timeAvailable.from <= startTime && startTime < timeAvailable.to && timeAvailable.to < endTime) {
                        var available: number = percent - share;
                        tmpListAvailable.push({ from: timeAvailable.from, to: startTime, val: timeAvailable.val });
                        tmpListAvailable.push({ from: startTime, to: timeAvailable.to, val: available });
                        tmpListAvailable.push({ from: timeAvailable.to, to: endTime, val: 100 - share });
                        isPushed = true;
                        continue;
                    }

                    tmpListAvailable.push(timeAvailable);
                }
                if (!isPushed) {
                    var available: number = percent - share;
                    tmpListAvailable.push({ from: startTime, to: endTime, val: available });
                }

                scope.dicAvailable[zoneid] = tmpListAvailable;
            }
        }
    };
});

mod.directive('bookingSchedule', function ($timeout) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/website/booking.schedule',
        scope: {
            data: '='
        },
        link: function (scope, element, attrs) {
            scope.$watch('data', function (newVal, oldVal) {
                if (newVal === null || newVal === undefined || newVal.type === null || newVal.type === undefined)
                    return;
                if (newVal.type === "zone_booking_schedule" || newVal.type === "item_zone_booking_schedule") {
                    $timeout(function () {
                        (<JQueryGantt>jQuery(".gantt")).gantt({
                            source: newVal.data,
                            navigate: "scroll",
                            scale: "days",
                            maxScale: "months",
                            minScale: "days",
                            itemsPerPage: 10,
                            minDate: newVal.minDate,
                            maxDate: newVal.maxDate,
                            showDesciption: true,
                            onItemClick: function (data) {
                                // TODO :
                            },
                            onAddClick: function (dt, rowId, rowIndex, rowName, x, y) {
                                // no action
                            },
                            onRender: function (data, fn) {
                                // no action
                            },
                            onMouseDrag: function (item, zoneid, zonename, start_time, end_time, percent, xcoor, ycoor) {
                                // no action
                            }
                        });
                    }, 200);
                }
            }, true);
        }
    };
});

mod.directive('bookingdaytime', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/order/booking.daytime',
        scope: {
            timeinday: '='
        },
        link: function (scope, element, attrs) {
            scope.timeType = 'user';
            scope.mode = "custom"; scope.maxtimerange = 3; scope.num = 1;
            scope.show = false; scope.copy = false;
            scope.day = '';
            scope.timeranges = [{ shour: 0, shourtxt: '00', smin: 0, smintxt: '00', ehour: 0, ehourtxt: '00', emin: 0, emintxt: '00' }];

            scope.isAddMore = () => {
                if (scope.num < scope.maxtimerange)
                    return true;
                return false;
            }

            scope.addMore = () => {
                scope.timeranges.push({ shour: 0, shourtxt: '', smin: 0, smintxt: '', ehour: 0, ehourtxt: '', emin: 0, emintxt: '' });
                scope.num++;
            }

            scope.removeTimeRange = (index) => {
                var tmp: Array<any> = [];
                for (var i: number = 0; i < scope.timeranges.length; i++) {
                    if (i === index)
                        continue;
                    tmp.push(scope.timeranges[i]);
                }
                scope.timeranges = tmp;
                scope.num--;
            }

            scope.showPopup = (day: string) => {
                scope.day = day;
                scope.show = true;
            }

            scope.closePopup = () => {
                scope.show = false;
            }

            scope.copyAllDay = () => {
                scope.copy = !scope.copy;
            }

            scope.isCopyAllDay = (): string => {
                if (scope.copy)
                    return "btn-primary";
                return "";
            }

            scope.configRunning = ()  => {
                if (scope.mode === 'not-running') {
                    if (scope.copy) {
                        var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

                        for (var i: number = 0; i < days.length; i++) 
                            deactiveAllDay(days[i]);
                    } else {
                        deactiveAllDay(scope.day);
                    }
                }

                if (scope.mode === 'custom') {
                    if (scope.copy) {
                        var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

                        for (var i: number = 0; i < days.length; i++)
                            deactiveAllDay(days[i]);
                    } else {
                        deactiveAllDay(scope.day);
                    }
                    var listtimerange: Array<any> = [];
                    for (var i: number = 0; i < scope.timeranges.length; i++) {
                        var timerange: any = scope.timeranges[i];
                        if (timerange.shour === 0 && timerange.smin === 0 && timerange.ehour === 0 && timerange.emin === 0)
                            continue;

                        if (listtimerange.length === 0) {
                            listtimerange.push(timerange);
                            continue;
                        }

                        for (var j: number = 0; j < listtimerange.length; j++) {
                            var tmptimerange: any = listtimerange[j];

                            if (timerange.shour > tmptimerange.ehour) {
                                listtimerange.push(timerange);
                                break;
                            }
                        }
                    }

                    if (listtimerange.length === 0) {
                        if (scope.copy) {
                            var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

                            for (var i: number = 0; i < days.length; i++) 
                                scope.activeAllDay(days[i]);
                        } else {
                            scope.activeAllDay(scope.day);
                        }
                    } else {
                        for (var i: number = 0; i < listtimerange.length; i++) {
                            var timerange: any = listtimerange[i];
                            var from: number = timerange.shour;
                            var to: number = 0;

                            if (timerange.ehour !== 0) {
                                to = timerange.ehour;
                                if (timerange.emin > 0)
                                    to = timerange.ehour + 1;
                            }

                            if (scope.copy) {
                                var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

                                for (var j: number = 0; j < days.length; j++) {
                                    scope.activeSomeDay(days[j], from, to);
                                }
                            } else { // not copy
                                scope.activeSomeDay(scope.day, from, to);
                            }
                        }
                    }
                }

                scope.day = '';
                scope.closePopup();
            }

            scope.chooseStartHour = (index: number, value: number) => {
                for (var i: number = 0; i < scope.timeranges.length; i++) {
                    if (i === index) {
                        var timerange: any = scope.timeranges[i];
                        if (value >= 0 && value < 24) {
                            timerange.shour = value; timerange.shourtxt = common.StringUtils.paddingZero(value, 2);
                        }
                        break;
                    }
                }
            }

            scope.chooseEndHour = (index: number, value: number) => {
                for (var i: number = 0; i < scope.timeranges.length; i++) {

                    if (i === index) {
                        var timerange: any = scope.timeranges[i];

                        if (value !== 0 && value <= timerange.shour)
                            return;

                        if (value >= 0 && value < 24) {
                            timerange.ehour = value; timerange.ehourtxt = common.StringUtils.paddingZero(value, 2);
                        }
                        break;
                    }
                }
            }

            scope.chooseStartMin = (index: number, value: number) => {
                for (var i: number = 0; i < scope.timeranges.length; i++) {
                    if (i === index) {
                        var timerange: any = scope.timeranges[i];

                        switch (value) {
                            case 0:
                                timerange.smin = value; timerange.smintxt = '00'; break;
                            case 30:
                                timerange.smin = value; timerange.smintxt = '30'; break;
                        }
                        break;
                    }
                }
            }

            scope.chooseEndMin = (index: number, value: number) => {
                for (var i: number = 0; i < scope.timeranges.length; i++) {
                    if (i === index) {
                        var timerange: any = scope.timeranges[i];

                        switch (value) {
                            case 0:
                                timerange.emin = value; timerange.emintxt = '00'; break;
                            case 30:
                                timerange.emin = value; timerange.emintxt = '30'; break;
                        }
                        break;
                    }
                }
            }

            scope.saveDayTime = () => {
                var data: {} = readSetting();
                scope.timeinday = { time: scope.timeType, data: data };
            };

            scope.resetAllDay = () => {
                var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
                for (var i = 0; i < days.length; i++) {
                    deactiveAllDay(days[i]);
                }
            };

            scope.activeSomeDay = (day, from, to) => {
                for (var i: number = 0; i < 24; i++) {

                    if (to > 0 && i >= from && i < to) {
                        var sel = '.' + day + i;
                        jQuery(sel).attr('checked', 'checked');
                    } else if (to === 0 && i >= from) {
                        var sel = '.' + day + i;
                        jQuery(sel).attr('checked', 'checked');
                    }
                }
            }; 

            scope.activeAllDay = (day) => {

                for (var i = 0; i < 24; i++) {
                    var sel = '.' + day + i;
                    jQuery(sel).attr('checked', 'checked');
                }
            };

            scope.checkTimezone = (kind: string) => {
                if (kind === scope.timeType)
                    return true;
                return false;
            };

            scope.$watch('timeinday', function (newVal, oldVal) {
                var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
                if (scope.timeinday && scope.timeinday['data']) {
                    for(var i = 0; i < days.length; i++)
                        for (var j: number = 0; j < 24; j++) {
                            var time = scope.timeinday['data'][days[i]];
                            if (time & Math.pow(2, j))
                                jQuery('.' + days[i] + j).attr('checked', 'checked');
                            else
                                jQuery('.' + days[i] + j).removeAttr('checked');
                        }
                }
                if (scope.timeinday !== undefined && scope.timeinday.time !== undefined) {
                    scope.timeType = scope.timeinday.time;
                }
            });
            function deactiveAllDay(day) {
                for (var i = 0; i < 24; i++) {
                    var sel = '.' + day + i;
                    jQuery(sel).removeAttr('checked');
                }
            }

            function readSetting() {
                var result = {};
                var days = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
                for (var i = 0; i < days.length; i++) {
                    var setting = readSettingDay(days[i]);
                    result[days[i]] = setting;
                }
                return result;
            }

            function readSettingDay(day) {
                var setting = 0;
                for (var i = 0; i < 24; i++) {
                    var sel = '.' + day + i;
                    if (jQuery(sel).is(':checked') === true)
                        setting = setting | Math.pow(2, i);
                }
                return setting;
            }
        }

    }
});

mod.directive('pagesizeselect', function ($timeout) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/common/pagesize.select',
        scope: {
            pagesize: '=',
            pageindex: '=',
            onchange: '&'
        },
        link: function (scope, element, attrs) {
            scope.choose = (value: number) => {
                scope.pagesize = value;
                scope.pageindex = 1;
                //scope.$apply();
                scope.onchange({start: 0,  size: value, pageindex: 1});
                
            };
        }
    };
});

mod.directive('uploader', function ($timeout) {
    return {
        restrict: 'E',
        scope: {
            action: '@',
            result: '=',
            filter: '@',
            ext: '=',
            mimetype: '=',
            multi: '@',
            duration: '=',
            required: "@"
        },
        controller: ['$scope', function ($scope) {
            $scope.progress = 0;
            $scope.success = false;
            $scope.avatar = '';
            //$scope.filter = "mp4, flv, wav, swf";
            $scope.isInvalid = false;
            $scope.paste = function (el) {
                if ($scope.ext !== undefined) 
                    $scope.$apply(function () {
                        $scope.ext = true;
                    });
            }
            $scope.$watch("resultText", (newval) => {
                if (newval && newval.length !== 0 && !$scope.isMultiple)
                    $scope.result = newval;
            })
            //* Multiple upload result: result = {notify: [0|1] //notice when insert new photo; data: Array<[URL of file]>}
            //  Single upload result: result = [URL of file | message]
            //*/
            $scope.sendFile = function (el) {
                $scope.ext = false;
                $scope.isInvalid = false;
                $scope.isInvalidMultiple = false;
                $scope.resultText = "";
                //if (el && el.files.length == 1) {
                if (!!$scope.filter) {
                    $scope.progress = 0;
                    if ($scope.isMultiple) {
                        $scope.result = {notify: 0, data: []};
                    } else
                        $scope.result = 'BEGIN';
                    for (var i = 0; i < el.files.length; i++) {
                        if (checkValidFileType(el.files[i].type, el.files[i].name)) {

                            var file = el.files[i];
                            var xhr = new XMLHttpRequest();
                            var data = new FormData();
                            data.append('file', file, file.name);

                            xhr.open('POST', $scope.action, false);
                            var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
                            xhr.setRequestHeader("X-sessionId", sessionid);
                            xhr.upload.onprogress = function (evt) {
                                var percentComplete = (evt.loaded / evt.total) * 100;
                                $scope.progress = percentComplete;
                            };
                            xhr.onerror = function (evt) {
                                $scope.$apply(function () {
                                    if (!$scope.isMultiple)
                                        $scope.result = 'FAIL';
                                });
                            };
                            xhr.onloadend = function (evt) {
                                $scope.progress = 100;
                                var res = xhr.response;

                                $scope.$apply(function () {
                                    var obj = JSON.parse(res);
                                    if (!$scope.isMultiple) {
                                        if (obj.result === 'error') {
                                            $scope.result = 'FAIL';
                                        } else {
                                            $scope.success = true;
                                            $scope.avatar = file.name;
                                            $scope.result = obj.msg;
                                            if ($scope.mimetype !== undefined)
                                                $scope.mimetype = obj.type;
                                            $scope.resultText = obj.msg;
                                            if (obj.duration !== undefined)
                                                $scope.duration = obj.duration;
                                        }
                                    } else {
                                        if (obj.result !== 'error') {
                                            $scope.success = true;
                                            $scope.result.data.push(obj.msg);
                                            $scope.result.notify = 1;
                                            $scope.resultText = $scope.result.data.toString();
                                        }
                                    }
                                });
                            };
                            if ($scope.isMultiple) {
                                $scope.$apply(function() {
                                    $scope.result.notify = 0;
                                });
                            }
                            xhr.send(data);
                        } else {
                            $scope.$apply(function () {
                                if (!$scope.isMultiple)
                                    $scope.isInvalid = true;
                                else if ($scope.isMultiple)
                                    $scope.isInvalidMultiple = true;
                            });
                        }
                    }
                }
            }
            function checkValidFileType(contentType: string, filename: string) {
                var filterTypes: Array<string> = $scope.filter.split(", ");
                var ret = false;
                filterTypes.forEach((type: string, index) => {
                    if (type.toLowerCase() === "flv") {
                        if (filename.indexOf("flv") != -1)
                            ret = true;
                    }
                    else
                    if (getContentType(type.toLowerCase()) === contentType.toLowerCase())
                        ret = true;
                });
                return ret;
            }
            function getContentType(type: string) {
                switch (type) {
                    case "mp4": return "video/mp4";
                    case "wmv": return "video/x-ms-wmv";
                    case "flv": return "video/x-flv";
                    case "swf": return "application/x-shockwave-flash";
                    case "jpg": return "image/jpeg";
                    case "png": return "image/png";
                    case "gif": return "image/gif";
                    case "txt": return "text/plain";
                    case "doc": return "application/msword";
                    case "pdf": return "application/pdf";
                    case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    case "zip": return "application/zip";
                }
            }
        }],
        link: function (scope, elem, attrs, ctrl) {
            // link function 
            // here you should register listeners
            scope.isMultiple = false;
            if (scope.multi === "true") {
                elem.find("input[type='file']").attr("multiple", "true");
                scope.isMultiple = true;
            }

            if (scope.required === "true") {
                elem.find("input#result").attr("required", "true");
            }

            elem.find('.browse').click(function () {
                elem.find('input[type="file"]').click();
            });
            if (!scope.isMultiple)
                 $timeout(()=>scope.resultText = scope.result, 1000);
        },
        replace: false,
        templateUrl: 'views/common/uploader'
    };

});

mod.directive('linechart', function ($timeout) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/common/chart.line',
        scope: {
            data: '@data',
            xaxis: '=',
            setfn: '&'
        },
        link: function (scope, element, attrs) {
            scope.$watch('data', function (newVal, oldVal) {
                if (newVal === undefined || newVal.length === 0)
                    return;
                var chartData = JSON.parse(newVal);
                if (chartData.items.length > 0) {
                    $timeout(function () {
                        var dataChart = chartData;
                        var lines: Array< any> = [];
                        var lineValues: Array<any> = [];
                        for (var i: number = 0; i < dataChart.items.length; i++) {
                            lineValues[i] = [];
                        }
                        for (var i: number = 0; i < dataChart.values.length; i++) {
                            var value: any = dataChart.values[i];
                            for (var j: number = 0; j < lineValues.length; j++) {
                                lineValues[j].push([value.time, value.values[j]]);
                            }
                        }
                        var chart: common.Charts = new common.Charts();
                        var data = [];
                        for (var i: number = 0; i < dataChart.items.length; i++) {
                            data.push({data: lineValues[i], label: dataChart.items[i]});
                        }

                        //scope.xaxis = JSON.parse(scope.xaxis);
                        if (scope.xaxis !== undefined && scope.xaxis !== null && scope.xaxis.length > 0)
                            scope.plot = chart.line('#line-chart', data, scope.xaxis);
                        else
                            scope.plot = chart.line('#line-chart', data);
                        
                    }, 200);
                } 
            }, true);


            scope.saveChart = function () {
                var image = scope.plot.getCanvas().toDataURL();
		        image = image.replace("image/png","image/octet-stream");
		        document.location.href=image;
            };

            scope.setfn({theDirFn: scope.saveChart});
        }
    };
});

mod.directive('piechart', function ($timeout) {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'views/common/chart.pie',
        scope: {
            data: '@',
            setfn: '&'
        },
        link: function (scope, element, attrs) {
            scope.$watch('data', function (newVal, oldVal) {
                if (newVal === undefined || newVal.length === 0)
                    return;
                var chartData = JSON.parse(newVal);
                if (chartData.elements.length > 0) {
                    $timeout(function () {

                        var chart: common.Charts = new common.Charts();
                        var data = chartData.elements;
                        
                        scope.plot = chart.donut('#pie-chart', data);

                    }, 200);
                }
            }, true);


            scope.saveChart = function () {
                var image = scope.plot.getCanvas().toDataURL();
                image = image.replace("image/png", "image/octet-stream");
                document.location.href = image;
            };

            scope.setfn({ theDirFn: scope.saveChart });
        }
    };
});

mod.directive('price', function () {
    return {
        require: 'ngModel',
        link: function (scope, element, attrs, modelCtrl) {

            modelCtrl.$parsers.push(function (inputValue) {
                // this next if is necessary for when using ng-required on your input. 
                // In such cases, when a letter is typed first, this parser will be called
                // again, and the 2nd time, the value will be undefined
                if (inputValue == undefined) return '';
                var transformedInput: string = inputValue.toString().replace(/[^0-9]+/g, '');
                var transformedPrice: string = transformedInput.charAt(0)
                for (var i = 1; i < transformedInput.length; i++) {
                    if ((transformedInput.length - i) % 3 == 0) {
                        transformedPrice += ',';
                    }
                    transformedPrice += transformedInput.charAt(i);
                }
                if (transformedPrice != inputValue) {
                    modelCtrl.$setViewValue(transformedPrice);
                    modelCtrl.$render();
                }
                return transformedInput;
            });

            modelCtrl.$formatters.push(function (inputValue) {
                // this next if is necessary for when using ng-required on your input. 
                // In such cases, when a letter is typed first, this parser will be called
                // again, and the 2nd time, the value will be undefined
                if (inputValue == undefined) return '';
                var transformedInput: string = inputValue.toString().replace(/[^0-9]+/g, '');
                var transformedPrice: string = transformedInput.charAt(0)
                for (var i = 1; i < transformedInput.length; i++) {
                    if ((transformedInput.length - i) % 3 == 0) {
                        transformedPrice += ',';
                    }
                    transformedPrice += transformedInput.charAt(i);
                }
                if (transformedPrice != inputValue) {
                    modelCtrl.$setViewValue(transformedPrice);
                    modelCtrl.$render();
                }
                return transformedPrice;
            });
        }
    };
});

mod.directive('numbersOnly', function () {
    return {
        require: 'ngModel',
        link: function (scope, element, attrs, modelCtrl) {
            modelCtrl.$parsers.push(function (inputValue) {
                // this next if is necessary for when using ng-required on your input. 
                // In such cases, when a letter is typed first, this parser will be called
                // again, and the 2nd time, the value will be undefined
                if (inputValue == undefined) return ''
           var transformedInput = inputValue.replace(/[^0-9]+/g, '');

                if (transformedInput != inputValue) {
                    modelCtrl.$setViewValue(transformedInput);
                    modelCtrl.$render();
                }

                return transformedInput;
            });
        }
    };
});

mod.directive('ngFocus', ['$parse', function ($parse) {
    return function (scope, element, attr) {
        var fn = $parse(attr['ngFocus']);
        element.on('focus', function (event) {
            scope.$apply(function () {
                fn(scope, { $event: event });
            });
        });
    };
}]);

mod.directive('ngBlur', ['$parse', function ($parse) {
        return function (scope, element, attr) {
            var fn = $parse(attr['ngBlur']);
            element.on('blur', function (event) {
                scope.$apply(function () {
                    fn(scope, { $event: event });
                });
            });
        };
    }]);

mod.directive("comboboxListsize", function ($timeout) {
    return {
        restrict: 'E',
        templateUrl: "views/common/combobox.listsize",
        scope: {
            listsizes: '=',
            search: '='
        },
        link: function (scope, element, attrs) {
            var chooseSize = null;
            scope.cls = '';
            scope.getClass = (): string => {
                return scope.cls;
            }

            scope.focus = () => {
                scope.cls = 'open';
                chooseSize = scope.listsizes[0];
            }

            scope.blur = () => {
                scope.cls = '';
            }

            scope.clickSize = () => {
                scope.search.value = chooseSize.value;
                scope.value = chooseSize.value;
            }

            scope.keydown = (e) => {
                if (e.keyCode === 40) { //press move down
                    if (scope.cls === '') {
                        scope.focus();
                        return;
                    }
                    for (var item in scope.listsizes) {
                        if (scope.listsizes[item] === chooseSize) {
                            for (var i = 0; i < scope.listsizes.length; i++) {
                                if (scope.listsizes[(i + parseInt(item) + 1) % scope.listsizes.length].value.search(scope.search.value) > -1) {
                                    chooseSize = scope.listsizes[(i + parseInt(item) + 1) % scope.listsizes.length];
                                    break;
                                }
                            }
                            break;
                        }

                    }
                } else if (e.keyCode === 38) { //press move up
                    if (scope.cls === '') {
                        scope.focus();
                        return;
                    }
                    for (var item in scope.listsizes) {
                        if (scope.listsizes[item] === chooseSize) {
                            for (var i = 0; i < scope.listsizes.length; i--) {
                                if (scope.listsizes[(i + parseInt(item) - 1 + scope.listsizes.length) % scope.listsizes.length].value.search(scope.search.value) > -1) {
                                    chooseSize = scope.listsizes[(i + parseInt(item) - 1 + scope.listsizes.length) % scope.listsizes.length];
                                    break;
                                }
                            }
                            break;
                        }
                    }
                } else if (e.keyCode === 13) { //press enter
                    event.preventDefault();
                    scope.search.value = chooseSize.value;
                    scope.value = chooseSize.value;
                    scope.cls = '';
                }
            }

            scope.mouseover = (size) => {
                chooseSize = size;
            }

            scope.isActiveClass = (size) => {
                if (size === chooseSize)
                    return 'active';
                return '';
            }
        }
    };
});

mod.directive("searchAutocomplete", function ($document) {
    return {
        restrict: "E",
        templateUrl: "views/common/auto-complete-search",
        scope: {
            filterby: '@',
            data: "=",
            onselected: '&',
            placeholder: '@',
            clearTextOnClick: '@',
            clearTextWhen: '@'
        },
        link: function ($scope, element, attrs) {
            $scope.search = {};
            $scope.search[$scope.filterby] = "";
            $scope.chooseItem = {};
            if ($scope.placeholder === undefined || $scope.placeholder === null) {
                setTimeout(function () { $scope.$apply($scope.placeholder = "Search...") }, 100);
            }

            $scope.$watch("clearTextWhen", function (newVal, oldVal) {
                if (newVal === "true") {
                    setTimeout(function () {
                        $scope.$apply($scope.search = {});
                    }, 10);
                }
            });
            $scope.focus = function () {
                if (!!$scope.clearTextOnClick && $scope.clearTextOnClick === "true") {
                    setTimeout(function () {
                        $scope.$apply($scope.search = {});
                        element.addClass("open");
                    },10);
                }
                else
                    element.addClass("open");
            }
            $scope.onselect = function (item) {
                $scope.search[$scope.filterby] = item[$scope.filterby];
                $scope.onselected({ item: item });
                element.removeClass("open");
            }
            $document.bind("click", function (event) {
                var listEl = element.find("ul");
                var inputEl = element.find("input");

                var el = document.elementFromPoint(event.clientX, event.clientY);
                if (!isContain(listEl[0], el) && !isContain(inputEl[0], el))
                    element.removeClass("open");

                function isContain(parent, child) {
                    while (!!child && child !== parent) child = child.parentNode;
                    return !!child;
                }
            });
            $scope.isActive = (item) => { return $scope.chooseItem == item ? "active" : ""; }
            $scope.mouseover = (item) => {
                $scope.chooseItem = item;
                element.removeClass("noHover");
            }
            $scope.change = function () {
                if (!!$scope.search[$scope.filterby] && $scope.search[$scope.filterby].length != 0)
                    element.addClass("open");
                var matchs = $scope.data;
                if ($scope.search[$scope.filterby].length !== 0)
                    matchs = $scope.data.filter((it, _) => it[$scope.filterby].toLowerCase().indexOf($scope.search[$scope.filterby].toLowerCase()) !== -1);
                $scope.chooseItem = matchs[0];
            }
            $scope.keydown = (event) => {
                var index = 0;
                var listEl = element.find("ul");
                var activeEl = element.find("li > a.active")[0];
                if ($scope.data && $scope.data.length !== 0) {
                    var matchs = $scope.data;
                    if ($scope.search[$scope.filterby] && $scope.search[$scope.filterby].length !== 0)
                        matchs = $scope.data.filter((it, _) => it[$scope.filterby].toLowerCase().indexOf($scope.search[$scope.filterby].toLowerCase()) !== -1);
                    index = matchs.indexOf($scope.chooseItem);
                    switch (event.keyCode) {
                        case 38: //key up
                            index = (index > 0 ? index - 1 : matchs.length - 1);
                            $scope.chooseItem = matchs[index];
                            element.addClass("noHover");
                            if (index === matchs.length - 1) {
                                listEl.scrollTop(listEl[0].scrollHeight - listEl[0].clientHeight)
                            }
                            else if (!!activeEl && listEl[0].offsetHeight - 20 < activeEl.offsetTop + activeEl.offsetHeight){
                                listEl.scrollTop(listEl.scrollTop() - activeEl.offsetHeight);
                            }
                            break;
                        case 40: //key down
                            index = (index < matchs.length - 1 ? index + 1 : 0);
                            $scope.chooseItem = matchs[index];
                            element.addClass("noHover");
                            if (index == 0) {
                                listEl.scrollTop(0);
                            }
                            else if (activeEl && listEl[0].offsetHeight - 20 < activeEl.offsetTop + activeEl.offsetHeight) {
                                listEl.scrollTop(listEl.scrollTop() + activeEl.offsetHeight);
                            }
                            break;
                        case 13: //enter
                            $scope.onselect($scope.chooseItem);
                            break;
                    }
                }
            }

        }
    }
});

mod.directive("tooltipText", function() {
    return function($scope, element, attrs) {
        element.tooltip({ trigger: "focus", title: attrs.tooltipText });
    }
});