/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 This js function will populate the UI after metadata generation in pages/my_applications.jag
 */
$(function () {
    var obtainFormMeta = function (formId) {
        return $(formId).data();
    };

    var refreshApplicationList = function () {
        $.ajax({
            url: caramel.context + '/apis/applications', success: function (result) {
                var partial = 'list_applications';
                var container = 'list_applications';
                var data = {};
                data.applications = JSON.parse(result);
                renderPartial(partial, container, data);
            }
        });
    };

    var partial = function (name) {
        return '/extensions/assets/api/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };
    var id = function (name) {
        return '#' + name;
    };

    var renderPartial = function (partialName, containerName, data, fn) {
        fn = fn || function () {
        };
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        if (!containerName) {
            throw 'A container name has not been specified for container key ' + containerKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function () {
            var template = Handlebars.partials[partialName](data);
            $(id(containerName)).html(template);
            fn(containerName);
        });
    };

    $(document).ready(function () {
        var appName = $('#overview_name').val();
        $('#form-application-create').ajaxForm({
            success: function () {
                var options = obtainFormMeta('#form-application-create');
                var message = {};
                message.text = '<div><i class="icon-briefcase"></i> Application: ' +
                appName + ' has been created.</div>';
                message.type = 'success';
                message.layout = 'topRight';
                noty(message);
                refreshApplicationList();
            },
            error: function () {
                var message = {};
                message.text = '<div><i class="icon-briefcase"></i> Application: ' +
                appName + ' has not been created.</div>';
                message.type = 'error';
                message.layout = 'topRight';
                noty(message);
            }
        });
    });

});