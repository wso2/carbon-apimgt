/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
$(function() {
    var ajaxApi = function(data, type) {
        $.ajax({
            url: '/asts/api/apis/tiers',
            type: 'POST',
            data: data,
            success: function(response) {
                alert('Successfully updated ' + type + ' tier details.');
            },
            error: function(response) {
                alert('Failed to update ' + type + ' tier details.');
            }
        });
    };
    $(document).ready(function() {
        $('#bronze_update_permissions').on('click', function() {
            ajaxApi({}, 'bronze');
        });
        $('#gold_update_permissions').on('click', function() {
            ajaxApi({}, 'gold');
        });
        $('#silver_update_permissions').on('click', function() {
            ajaxApi({}, 'silver');
        });
        $('#unlimited_update_permissions').on('click', function() {
            ajaxApi({}, 'unlimited');
        });
    });
});