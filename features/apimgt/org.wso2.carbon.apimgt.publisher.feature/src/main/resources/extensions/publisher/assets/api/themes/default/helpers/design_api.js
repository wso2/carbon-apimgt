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
var resources = function(page,meta){
    return {
        js:['jquery/jquery-ace.min.js','typeahead.bundle.js','jquery/jquery.buttonLoader.js',
            'jquery/jquery.form.js','jquery/jquery.cookie.js','bootstrap/bootstrap-tagsinput.min.js','jquery/jquery.validate.min.js',
            'js-yaml.min.js','bootstrap/bootstrap-dialog.min.js','api_designer.js','create_asset.js','bootstrap/bootstrap-editable.js' ,
            'bootstrap/bootstrap-multiselect.js', 'custom-validation.js','jsonpath-0.8.0.js','handlebars.js',
            'jquery/jquery.noty.packaged.min.js' ,'typeaheadjs.js'],
        code:['api_meta.hbs', 'design_api_meta.hbs'],
        css:['bootstrap/bootstrap-editable.css','buttonLoader.css',
             'bootstrap/bootstrap-tagsinput.css','api_designer.css','bootstrap/bootstrap-dialog.min.css', 'bootstrap/bootstrap-multiselect.css']
    }
};
