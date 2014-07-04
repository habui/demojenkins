
module scopes {
    'use strict';
    export interface IReportBreadcumNavScope extends common.IMenuScope {
        items: common.MenuItem[];
        type: string;
        orderId: number;
        campaignId: number;
        websiteId: number;
        itemId: number;
        isActive(menu: string);
        isShow(): boolean;
    }

    export interface IReportTabScope extends ng.IScope, common.IPermissionScope, IReportScope {
        orders: Array<models.Order>;
        orderTotal: number;
        websites: Array<models.Website>;
        websiteTotal: number;
        numRecord: number;
        isPublisherUser: boolean;
        isAdvertiserUser: boolean;
        toggleSubRecord(type: string, event: any);
        orderSubCollapse: boolean;
        websiteSubCollapse: boolean;
        isActiveTab(menu: string): string;
        isActiveOrderTab(orderId: number): string;
        isActiveWebsiteTab(websiteId: number): string;
        isShowOrderMenu(): boolean;
        isShowWebsiteMenu(): boolean;
        goTab(dest: string);
        showMore(type: string): boolean;
        goMore(type: string);
        goOrderReport(orderId: number);
        goWebsiteReport(websiteId: number);
        getClass(type: string): string;
        orderFilter: {name: string};
        siteFilter: {name: string};
        filterName(type: string, event);
    }

    export interface IReportScope extends ng.IScope, common.IPermissionScope {
        data: models.LineChart;
        pies: {};
        dataPie: models.PieChart;
        dateFrom: string;
        dateTo: string;
        compareDateFrom: string;
        compareDateTo: string;
        pageIndex: number;
        pageSize: number;
        total: number;
        title: string;
        isLoading: boolean;
        colNames: Array<string>;
        colNamesLabel: Array<string>;
        selectedCol: {};
        report: models.ReportFE;
        check: {};
        datepicker: {};
        timeOption: string;
        currentTimeOption: string;
        isShowChart: boolean;
        isCompareReport: boolean;
        csvFileHeader: string;
        graphType: string;
        timeRange: models.TimeRange;
        compareTimeRange: models.TimeRange;
        optionQuery: Array<any>;
        resolution: string;
        gotoDetail(id: number);
        getABS(num: number): number;
        chose(type: string, e);
        formatDateTime(timestamp: number): string;
        formatTime(timestamp: number): string;
        formatNumber(num: number): string;
        formatCTR(num: number): string;
        isCompare(): boolean;
        getDirection(change: number): string;
        query();
        click(e);
        showHideChart();
        enableCompare();
        queryReport(range: models.TimeRange, compareRange: models.TimeRange);
        exportCSV();
        downloadPDF();
        genCSV(): string;
        getTimeReport(): models.TimeRange;
        saveChart();
        setDirectiveFn(directiveFn: any);
        getData(range: models.TimeRange, compareRange: models.TimeRange, options: Array<any>);
        getChartData(chartType: string, range: models.TimeRange, compareRange: models.TimeRange, options: Array<any>);
        changeGraph(kind: string);
        chooseResolution(resolution: string);
        isResolution(resolution: string): string;
        changeChart();
        isChartPie: boolean;
        isGetChartData: boolean;
        toggleOpen();
        getOpenClass(): string;
        isFilterColumn: boolean;
        toggeOpenColumn();
        showColumnOpenClass(): string;
        isWarningFilterColumn: boolean;
        isOpenMenu: boolean;
        xvalues: Array<any>;
        disableResolution: {};
        dataChart: Array<models.Point>;
        totalChartValue: { [key: string]: number; };
        totalConversionValue: { [key: string]: number; }
        totalConversionCount: { [key: string]: number; }
        conversionDetails: Array<models.ConversionDetail>;
        hasChartValue: boolean;
        isMinimizeChartData(): boolean;
        isMaximizeChartData(): boolean;
        isMinimize: boolean;
        toggleMinimize();

        sortField: {};
        getSortClass(type: string): string;
        switchSort(type: string);
        switchSortGraph(type: string);

        isConversion(): boolean;
        isConversionDetail(): boolean;
        reportType: string;
        //isFilterCampaign: boolean;
        //filterCampaign: string;
        //filterOption: string;
        //chooseFilter(filterType: string);
        settingState: string;
        settingParams: {}
        objectIdString: string;
        gotoSetting(id: number);
        mouseover(index: number);
        mouseleave();
        currentSelectedRow: number;
        upperCaseFirst(name: string);

        searchItems: Array<{ id: number; name: string }>;
        search: any;
        chooseSearchItem(item: { name: string; id: number });
        orderNames: Array<string>;

        //paging report
        paging: {
            limitItem: number;
            startFrom: number;
        }
        totalRecord: number;
        checkDisable(start: number, type: string);
        getPageNum();
        next();
        prev();
        chooseLimit(limit: number);
        getEndIndexCurPage();

        //filter item
        filterItems: Array<any>;
        filterItemList: Array<any>;
        addFilterItem(item);
        removeFilterItem(index);
        viewReportFilter();

        currentService: any;
        currentObjId: number;

        typeof(value);
    }

    export interface IDetailReportScope extends IReportScope {
        isTab(tab: string): boolean;
        isActiveTab(tab: string): string;
        goTab(tabName: string);
        currentTab: string;
        gotoConversionDetail(id: number);
        gotoDelivery(id: number);
    }
}