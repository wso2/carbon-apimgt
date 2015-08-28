/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

/*
 var timer;
 var details;
 ;
 */
var rows_added = 0;
var last_to = 0;
var items_per_row = 0;
var doPagination = true;
store.infiniteScroll ={};
store.infiniteScroll.recalculateRowsAdded = function(){
    return (last_to - last_to%items_per_row)/items_per_row;
};
store.infiniteScroll.addItemsToPage = function(){

    var screen_width = $(window).width();
    var screen_height = $(window).height();


    var header_height = 163;
    var thumb_width = 170;
    var thumb_height = 280;
    var gutter_width = 20;

    screen_width = screen_width - gutter_width; // reduce the padding from the screen size
    screen_height = screen_height - header_height;

    items_per_row = (screen_width-screen_width%thumb_width)/thumb_width;
    //var rows_per_page = (screen_height-screen_height%thumb_height)/thumb_height;
    var scroll_pos = $(document).scrollTop();
    var row_current =  (screen_height+scroll_pos-(screen_height+scroll_pos)%thumb_height)/thumb_height;
    row_current +=3 ; // We increase the row current by 2 since we need to provide one additional row to scroll down without loading it from backend


    var from = 0;
    var to = 0;
    if(row_current > rows_added && doPagination){
        from = rows_added * items_per_row;
        to = row_current*items_per_row;
        last_to = to; //We store this os we can recalculate rows_added when resolution change
        rows_added = row_current;
        store.infiniteScroll.getItems(from,to);
        console.info('getting items from ' + from + " to " + to + " screen_width " + screen_width + " items_per_row " + items_per_row);


    }

};

store.infiniteScroll.getItems = function(from,to){
    var count = to-from;
    var dynamicData = {};
    dynamicData["from"] = from;
    dynamicData["to"] = to;
    var path = window.location.href; //current page path
    // Returns the jQuery ajax method
    var url = caramel.tenantedUrl(store.asset.paging.url+"&paginationLimit=" + to + "&start="+from+"&count="+count+store.infiniteScroll.setQueryParams(path));

    caramel.render('loading','Loading assets from ' + from + ' to ' + to + '.', function( info , content ){
        $('.loading-animation-big').remove();
        $('body').append($(content));
    });

        caramel.data({
             title : null,
             body : ['assets']
         }, {
             url : url,
             success : function(data, status, xhr) {
                 caramel.partials(data._.partials, function() {
                     caramel.render('assets-thumbnails', data.body.assets.context, function (info, content) {
                         $('.assets-container section').append($(content));
                         $('.loading-animation-big').remove();
                     });
                 });
             },
             error : function(xhr, status, error) {
                 $('.loading-animation-big').remove();
                 doPagination = false;
             }
         });
    //}

};
store.infiniteScroll.showAll = function(){
    $('.assets-container section').empty();
    store.infiniteScroll.addItemsToPage();
    $(window).scroll(function(){
        store.infiniteScroll.addItemsToPage();
    });
    $(window).resize(function () {
        //recalculate "rows_added"
        rows_added = store.infiniteScroll.recalculateRowsAdded();
        store.infiniteScroll.addItemsToPage();
    });
};
/**
 * Build query parameters based on page path
 * @param {string} path  : string
 */
store.infiniteScroll.setQueryParams = function(path) {
    var query = '';
    var obj = path.split('?');
    if(obj[1]){
        var params = obj[1].split("&");
        for(var j=0; j<params.length;j++){
            var paramsPart = params[j];
            if(paramsPart.indexOf("q=") != -1){
                query = '&&' + paramsPart;
            }
        }
    }
    return query;
};
$(function() {
    /*
    * Bookmark event handler
    * */
    $('#assets-container').on('click', '.js_bookmark', function () {
        var elem = $(this);
        asset.process(elem.data('type'), elem.data('aid'), location.href);
    });

    /*
    * subscribe button event handler
    * */
	$(document).on('click', '#assets-container .asset-add-btn', function(event) {
		var parent = $(this).parent().parent().parent();
		asset.process(parent.data('type'), parent.data('id'), location.href);
		event.stopPropagation();
	});
    /*
    * Sort button event handler
    * */
     $('#sortDropdown').click(function(e){
         e.preventDefault();
     });
    /*
    * Pagination for listing page
    * */
    store.infiniteScroll.showAll();

	caramel.loaded('js', 'assets');
	caramel.loaded('js', 'sort-assets');
});
