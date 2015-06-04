/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var resources = function (page, meta) {
    return {
        js: ['handlebars-v1.3.0.js', 'events.js', 'views.js', 'console.js', 'subscription-selection.js',
            'bootstrap-dialog.min.js', 'bootstrap-editable.js', 'bootstrap-multiselect.js'],
        css: ['bootstrap/bootstrap-editable.css', 'bootstrap-dialog.min.css',
            'bootstrap/bootstrap-tagsinput.css', 'bootstrap-multiselect.css','apim_store_custom.css'],
        code: ['subscriptions/subscriptions-metadata.hbs']
    };
};