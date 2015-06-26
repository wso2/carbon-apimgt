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
        js: ['swagger/lib/jquery-1.8.0.min.js', 'swagger/lib/jquery.slideto.min.js', 'swagger/lib/jquery.wiggle.min.js', 'swagger/lib/jquery.ba-bbq.min.js', 'swagger/lib/handlebars-2.0.0.js',
            'swagger/lib/underscore-min.js', 'swagger/lib/backbone-min.js', 'swagger/swagger-ui.js','swagger/lib/highlight.7.3.pack.js','swagger/lib/marked.js'],
        css: ['swagger/css/screen.css','swagger.css']        
    };
};

