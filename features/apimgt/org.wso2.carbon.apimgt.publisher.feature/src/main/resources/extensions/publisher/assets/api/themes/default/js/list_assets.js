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
 * To render the next set of assets by appending to the available container
 * @param {string} partial  : to which partial should be added
 * @param {JSON}   data     : data for the partial
 * @param {String} container : container to be appended
 * @param {String} cb       : callback function if any
 */
function renderView(partial, data, container, cb) {
    var obj = {};
    obj[partial] = '/themes/default/partials/' + partial + '.hbs';
    delete data.content;
    caramel.partials(obj, function() {
        var template = Handlebars.partials[partial](data);
        $(container).append(template);
        if (cb) {
            cb();
        }
    });
}
/**
 * To convert asset.attributes.overview_createdtime to UTC string
 * @param {JSON} assets  The asset object list
 */
function convertTimeToUTC(assets) {
    for (var index in assets) {
        var asset = assets[index];
        if (asset.attributes.overview_createdtime) {
            var value = asset.attributes.overview_createdtime;
            var date = new Date();
            date.setTime(value);
            asset.attributes.overview_createdtime = date.toUTCString();
        }
    }
    return assets;
}

/**
 * To convert asset.attributes.overview_thumbnail string null value to null
 */
function convertStringNull(assets) {
    for (var index in assets) {
        var asset = assets[index];
        if (asset.thumbnail) {
            var value = asset.thumbnail;
            if(value=='null'){
            asset.thumbnail=null;
            }
        }
    }
    return assets;
}
/**
 * Build sorting parameters based on page path
 * @param {string} path  : string
 */
var setSortingParams = function(path) {
    var sorting = '';
    var obj = path.split('?');
    if(obj[1]){
        var params = obj[1].split("&");
        for(var j=0; j<params.length;j++){
            var paramsPart = params[j];
            if(paramsPart.indexOf("sort=") != -1){
                sorting = '&&' + paramsPart;
            }
        }
    }else{
        sorting = '&&sort=+overview_createdtime';
    }
    return sorting;
};
/**
 * Build query parameters based on page path
 * @param {string} path  : string
 */
var setQueryParams = function(path) {
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
var propCount = function(obj) {
    var count = 0;
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            count++;
        }
    }
    return count;
};
var parseArrToJSON = function(items){
    var item;
    var components;
    var obj = {};
    var key;
    var value;
    for(var index = 0; index < items.length; index++){
        item = items[index];
        components = item.split(':');
        if(components.length == 2) {
            key = components[0];
            value = components[1];
            obj[key]=value;
        }
    }
    return obj;
};
var isTokenizedTerm = function(term){
    return term.indexOf(':')>-1;
};
var isEmpty = function(input) {
    return (input.length === 0); 
};
/**
 * Takes the users input and builds a query.This method
 * first checks if the user is attempting to search by name , if not
 * it will look for a : delimited complex query
 *    E.g. name:wso2 tags:bubble
 * @param  {[type]} input [description]
 * @return {[type]}       [description]
 */
var parseUsedDefinedQuery = function(input) {
    var terms;
    var q = {};
    var current;
    var term;
    var arr =[];
    var previous;
    //Use case #1 : The user has only entered a name
    if((!isTokenizedTerm(input)) &&(!isEmpty(input))){
        q.name = input;
        return q;
    }
    //Remove trailing whitespaces if any
    input = input.trim();
    //Use case #2: The user has entered a complex query
    //and one or more properties in the query could values
    //with spaces
    //E.g. name:This is a test tags:wso2
    terms = input.split(' ');

    for(var index = 0; index < terms.length; index++){
        term = terms[index];
        term = term.trim(); //Remove any whitespaces
        //If this term is not empty and does not have a : then it should be appended to the
        //previous term
        if((!isEmpty(term))&&(!isTokenizedTerm(term))){
            previous = arr.length -1;
            if(previous>=0) {
                arr[previous]= arr[previous]+' '+term;
            }
        } else {
            arr.push(term);
        }
    }
    return parseArrToJSON(arr);
};
var createQuery = function(options) {
    options = options || {};
    var searchUrl = caramel.url('/assets/' + store.publisher.type + '/list');
    var q = {};
    var input = $('#inp_searchAsset').val();
    var category = options.category || undefined;
    var searchQueryString = '?';
    q = parseUsedDefinedQuery(input);
    // if (name) {
    //     q.name = name;
    // }
    if (category) {
        if(category == "All Categories"){
            category = "";
        }
        q.category = category;
    }
    if (propCount(q) >= 1) {
        searchQueryString += 'q=';
        searchQueryString += JSON.stringify(q);
        searchQueryString = searchQueryString.replace('{', '').replace('}', '');
    }
    return searchUrl + searchQueryString;
};
var initSearch = function() {
    //Support for searching when pressing enter
    $('#assetSearchForm').submit(function(e) {
        e.preventDefault();
        window.location = createQuery();
    });
    //Support for searching by clicking on the search button
    $('#searchButton').click(function(e) {
        e.preventDefault();
        window.location = createQuery();
    });
};
var initCategorySelection = function() {
    $('div.wr-filter-category ul.dropdown-menu li a').click(function(e) {
        e.preventDefault();
        var selectedCategory = $(this).text();
        window.location = createQuery({
            category: selectedCategory
        });
    });
};
var clearWaiting = function(){
    var cookieName = 'new-asset-'+store.publisher.type;
    $.removeCookie(cookieName);
};
var  initAssetCreationChecker = function(){
    var cookieName = 'new-asset-'+store.publisher.type;
    var newAsset = $.cookie(cookieName);
    if(!newAsset){
        return;
    }
    var newAssetId =  newAsset.split(":")[0].trim();
    var newAssetType = newAsset.split(":")[1].trim();
    var newAssetName = newAsset.split(":")[2].trim();

    var urlApi = caramel.url('/apis/assets'+'?type='+newAssetType + '&q="name":"'+newAssetName+'"');
    var url = caramel.url('/assets/'+newAssetType + '/details/' + newAssetId);


    $.ajax({
        url:urlApi,
        type:'GET',
        success:function(data){
            if(data.list.length == 0 ){
                if($('#assetLoader').length < 1) {
                    messages.alertInfoLoader('Asset added successfully. Please wait. <i class="fa fa-spinner fa-pulse" id="assetLoader"></i> <i class="fa fa-close" onclick="clearWaiting()"></i>');
                }
                setTimeout(initAssetCreationChecker,3000);
            }else{
                $('#assetLoader').parent().parent().remove();
                messages.alertInfoLoader('Now you can access the asset. <a href="'+url+'">'+ newAssetName + '</a>');
                $.removeCookie(cookieName);
            }
        },
        error:function(){
            $.removeCookie(cookieName);
        }
   });
};
// bind to window function
//$(window).bind('scroll', scroll);
$(window).load(function() {
    initSearch();
    initCategorySelection();
    initAssetCreationChecker();
});
