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
    $(document).ready(function() {
        $('#form-api-endpoints').ajaxForm({
            success: function() {
                var options = obtainFormMeta('#form-api-endpoints');
                notify({
                    text: 'API endpoint details saved successfully'
                });
            },
            error: function() {
                notify({
                    text: 'Unable to save endpoint details'
                });
            }
        });

        $('.more-options').click(function(){
            $('.more-options').css({'display':'none'});
            $('.less-options').css({'display':'block'});
            $('#more-options-endpoints').css({'display':'block'});
       });

        $('.less-options').click(function(){
            $('.more-options').css({'display':'block'});
            $('.less-options').css({'display':'none'});
            $('#more-options-endpoints').css({'display':'none'});
       });

    });
});