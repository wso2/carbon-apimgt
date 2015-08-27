/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Description: Pagination
 *              Function 'showAll' bind to the page load event.
 *              'showAll' binds scroll and resize events to 'addItemsToPage' function
 *              'addItemsToPage' function calculate the number of assets to be displayed in a row depending on the size of each asset thumbnail and screen size.
 *              Then it calculate the number of rows already in the page and how many should be retrieve in order to fill the UI base on the scroll position.
 *              Requests next set of assets by calling API endpoint /publisher/apis/assets?type=<type>&sort=<sort-by-attribute>&start=<number-of-already-rendered-assets>&count=<number-of-assets-per-page>
 *              Renders retrieved set of assets by calling caramel client
 *              If the endpoint to retrieve assets reruns empty or an exception, doPagination is set to false, and the call to 'getItems' is never called.
 */
var rows_added = 0;
var last_to = 0;
var items_per_row = 0;
var doPagination = true;
var partialsLoaded = false;
store.infiniteScroll = {};
store.infiniteScroll.recalculateRowsAdded = function () {
    return (last_to - last_to % items_per_row) / items_per_row;
};
/**
 * Initial method for pagination
* */
store.infiniteScroll.addItemsToPage = function () {
    var screen_width = $(window).width();
    var screen_height = $(window).height();
    var thumb_width = 170;
    var thumb_height = 280;
    var gutter_width = 20;
    var header_height = 163;
    screen_width = screen_width - gutter_width; // reduce the padding from the screen size
    screen_height = screen_height - header_height;
    items_per_row = (screen_width - screen_width % thumb_width) / thumb_width;
    //var rows_per_page = (screen_height-screen_height%thumb_height)/thumb_height;
    var scroll_pos = $(document).scrollTop();
    var row_current = Math.floor((screen_height + scroll_pos - header_height) / thumb_height);
    row_current = row_current + 3; // We increase the row current by 2 since we need to provide one additional row to scroll down without loading it from backend
    var from = 0;
    var to = 0;
    if (row_current > rows_added && doPagination) {
        from = rows_added * items_per_row;
        to = row_current * items_per_row;
        last_to = to; //We store this os we can recalculate rows_added when resolution change
        rows_added = row_current;
        //console.info("from = " + from + " count = " + (to - from) + " row_current = ", row_current + " screen_height = " + screen_height + " scroll_pos = " + scroll_pos + " thumb_height = " + thumb_height);
        store.infiniteScroll.getItems(from, to);
    }
};
/**
 * Request for a set of assets and process the data set with handlebar template and insert the response to the HTML DOM
 * @param from {int} offset element number of the asset query ex:10
 * @param to {int} up to which element. Ex: 20
 */
store.infiniteScroll.getItems = function (from, to) {
    var count = to - from;
    var dynamicData = {};
    dynamicData["from"] = from;
    dynamicData["to"] = to;
    // Returns the jQuery ajax method
    var path = window.location.href; //current page path
    var param = '&&paginationLimit=' + to + '&&start=' + from + '&&count=' + count + setSortingParams(path) + setQueryParams(path);
    var assetType = store.publisher.type; //load type from store global object
    var url = '/publisher/apis/assets?type=' + assetType + param; // build url for the endpoint call
    caramel.render('loading','Loading assets from ' + from + ' to ' + to + '.', function( info , content ){
        $('.loading-animation-big').remove();
        $('#list_assets_content').prepend($(content));
        var loadingAnimationTop = $(document).height() - 320;
        $('.loading-animation-big').css('top',loadingAnimationTop+'px');
    });
    //var url = caramel.tenantedUrl(store.asset.paging.url+"&start="+from+"&count="+count);     //TODO enable tenanted url thing..
    var loadAssets = function () {
        $.ajax({
            url: url,
            type: 'GET',
            headers: {
                Accept: "application/json; charset=utf-8"
            },
            success: function (response) { //on success
                if (response) {
                    var assets = convertTimeToUTC(response.list);
                    assets=convertStringNull(response.list);
                    caramel.render('list_assets_table_body', assets, function (info, content) {
                        $('.loading-animation').addClass('loading-animation-big').remove();
                        $('#list_assets_content').append($(content));
                    });
                } else { //if no assets retrieved for this page
                    doPagination = false;
                    $('.loading-animation-big').remove();
                }
            },
            error: function (response) { //on error
                doPagination = false;
            }
        });
    };
    if (partialsLoaded) {
        loadAssets();
        return;
    }
    var initialUrl = '/publisher/assets/' + assetType + '/list';
    caramel.data({
        "title": null,
        "listassets": ["list-assets"]
    }, {
        url: initialUrl,
        success: function (data, status, xhr) {
            caramel.partials(data._.partials, function () {
                partialsLoaded = true;
                loadAssets();
            });
        },
        error: function (xhr, status, error) {
            doPagination = false;
        }
    });
};
/**
 * Callback registered at the document.ready event of jQuery
 * This method binds scroll and resize events to addItemsToPage callback
 */
store.infiniteScroll.showAll = function () {
    $('.assets-container section').empty();
    store.infiniteScroll.addItemsToPage();
    $(window).scroll(function () {
        store.infiniteScroll.addItemsToPage();
    });
    $(window).resize(function () {
        //recalculate "rows_added"
        rows_added = store.infiniteScroll.recalculateRowsAdded();
        store.infiniteScroll.addItemsToPage();
    });
};
/**
 * jQuery document.ready callback. Call after the page load.
 */
$(function () {
    store.infiniteScroll.showAll();
});
