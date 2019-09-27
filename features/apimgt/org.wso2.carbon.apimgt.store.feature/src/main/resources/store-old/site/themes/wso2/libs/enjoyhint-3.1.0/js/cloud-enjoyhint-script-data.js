/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Get the name of the API used in tutorial
function getApiName() {
    var apiName = localStorage.getItem("apiName");
    return apiName;
}

function getSelectedAPIName() {
    var apiName = localStorage.getItem("selectedAPIName");
    return apiName
}

function getHTTPMethod() {
    var httpMethod = localStorage.getItem("intractiveTutorialHTTPMethod");
    return httpMethod
}

var login_apistore_script_data = [
    {
        'click #btn-login': 'First of all lets <b>login</b> to API Cloud.',
        'showSkip' : false
    }
];

var api_info_script_data = [
    {
        'click #subscribe-button': 'Here is the new API that you published in the API Store. This is' +
                                   ' how your subscribers see it. Click <b>Subscribe</b> to start using the API.',
        'showSkip' : false
    }
];

var goto_mysubscription_message_script_data = [
    {
        'click #btn-primary': 'You are subscribed now. Click <b>View Subscriptions</b> to go to the ' +
                              'OAuth key generation page.',
        'showSkip' : false
    }
];

var generate_production_keys_data = [
    {
        'click #production-keys-tab': 'Click <b>Production keys</b> to go to the ' +
                                      'Production OAuth key generation tab.',
        'showSkip' : false
    },
    {
        'click .generatekeys': 'Click <b>Generate keys</b> to get your OAuth token, which, by ' +
                               'default, is valid for 60 minutes.',
        'showSkip' : false
    },
    {
        'click #subscriptions-tab': 'Now you have generated an OAuth access token for accessing the API you created. ' +
                                    'Click <b>Subscriptions</b> to see your subscription information.',
        'showSkip' : false
    },
    {
        'click #subscription-table': 'Now, click the <b>' + getApiName() + ' API</b> to open the API\'s overview again.',
        'showSkip' : false
    }

];

var re_generate_production_keys_data = [
    {
        'click #production-keys-tab': 'Click <b>Production keys</b> to go to the ' +
                                      'Production OAuth key generation tab.',
        'showSkip' : false
    },
    {
        'click #production': 'Since you have already generated a token, lets Regenerate a new token. ' +
                             'Click the highlighted area to start.',
        'showSkip' : false
    },
    {
        'click .regenerate': 'Click <b>Regenerate keys</b> to generate the new OAuth token.',
        'showSkip' : false,
        onBeforeStart:function(){
            $(window).scrollTop($('.regenerate').offset().top);
        }
    },
    {
        'click #subscriptions-tab': 'Now you have generated an OAuth access token for accessing the API you created. ' +
                                    'Click <b>Subscriptions</b> to see your subscription information',
        'showSkip' : false,
        onBeforeStart:function(){
            $(window).scrollTop($('#application_name').offset().top);
        }
    },
    {
        'click #subscription-table': 'Now, click the <b>' + getApiName() + ' API</b> to open the API\'s overview again.',
        'showSkip' : false
    }

];

var subscription_token_regenerate_script_data = [
    {
        'click #btn-regeneratekeys-prod': 'Click <b>Regenerate keys</b> to get your OAuth token.'
    },
    {
        selector: '#' + getApiName(),
        event: 'click',
        description: 'Now, click the <b>' + getApiName() + ' API</b> to open the API\'s overview again.'
    }
];

var ui_tab_script_data = [
    {
        'click #1': 'Click the <b>API Console</b> tab to invoke the API. The API Console is an integrated Swagger UI.',
        'showSkip' : false
    }
];

var swagger_script_data = [
    {
        'click #default_get_countries_code': 'Click the <b>GET /countries/{code}</b> method that you created ' +
                                             'earlier to expand it.',
        'showSkip' : false,
        onBeforeStart:function(){
            $(window).scrollTop($('#default_get_countries_code').offset().top);
        }
    },
    {
        'key .parameter required': 'Type a two-letter country code <b class="enjoy_hint_emphasize_text">(e.g., "us")</b> and press Tab',
        'keyCode': 9,
        'showSkip' : false,
        timeout : 1000

    },
    {
        'click .submit': 'Now, click <b>Try it out!</b> button to invoke the API',
        'showSkip' : false,
        onBeforeStart:function(){
            $(window).scrollTop($('#default_get_countries_code').offset().top);
        }
    },
    {
        'click #default_get_countries_code': 'The API is successfully invoked. You can see the response, invocation ' +
                                             'URL, and sample Curl command in the API Console.This concludes our tutorial to publish and ' +
                                             'invoke your first API',
        'showSkip' : false
    }
];

// for API Store Interactive Tutorial
var api_store_application_page_add_tutorial = [
    {
        selector: '#application-name',
        description: 'Let’s type <b class="enjoy_hint_emphasize_text">TestApplication</b> as the Application Name and press Tab. Note that the application name should be unique. ',
        'showSkip' : false,
        event : 'key',
        'keyCode': 9,
    },
    {
        selector: '#application-add-button',
        event: 'click',
        description: 'Next, click <b class="enjoy_hint_emphasize_text">Add</b> button to add this application to the list of applications.',
        'showSkip' : false
    },
    {
        selector: 'a[title="messageButton"]',
        event: 'click',
        description: 'Click <b class="enjoy_hint_emphasize_text">Yes</b> button to return to the API Info page..',
        'showSkip' : false,
        timeout : 500
    }
];

var api_store_select_one_api = [
    {
        selector : '.page-header',
        event: 'click',
        description: 'Welcome to WSO2 API Store. In this interactive tutorial, you see how to invoke  APIs. Click the highlighted area to  get started.' 
        + "<p> Go through these steps to complete the tutorial:," 
        + "<ul> "
        + "<li> <b class='enjoy_hint_emphasize_text'>STEP 1 : </b>Create an application or use the default application to subscribe to an API.  </li>"
        + "<li> <b class='enjoy_hint_emphasize_text'>STEP 2 : </b>Generate access tokens (Production Keys) to invoke the subscribed APIs.  </li>"
        + "<li> <b class='enjoy_hint_emphasize_text'>STEP 3 : </b>Make a call to the API’s endpoint. </li>"
        + "</ul> </p>",
        'showSkip' : false
    },
    {
        selector: '.page-content',
        selectNextElement : '.square-element',
        targetNext : 'INSIDE_PARENT',
        event: 'click',
        description: 'Shown here is an API that is published in  our API Store. Click this API to open it.',
        'showSkip' : false
    }
];

var api_store_subscribe_to_default_application = [
    {
        selector: '#subscribe-button',
        event: 'click',
        description: 'Let’s subscribe to this API using <b class="enjoy_hint_emphasize_text">DefaultApplication</b>, which is already selected for you. <br/> Click <b class="enjoy_hint_emphasize_text">Subscribe</b>. <br/>'
        + 'An application is a logical collection of APIs. Applications allow you to use a single access token to invoke a collection of APIs and to subscribe to one API multiple times with different SLA levels.',
        'showSkip' : false,
    },
    {
        selector: '.modal-content',
        selectNextElement : '.btn-primary',
        targetNext : 'INSIDE_PARENT',
        event: 'click',
        description: 'Now you have subscribed this Application to this selected API. Click <b class="enjoy_hint_emphasize_text">View Subscriptions</b> button to get more information about the Application Subscriptions.',
        'showSkip' : false,
        timeout : 600
    }
];

var api_store_subscribe_to_new_application = [
    {
        selector: '#application-selection-list',
        event: 'click',
        description: 'Click this dropdown to select the application list <br/> '
        + 'An application is a logical collection of APIs. Applications allow you to use a single access token to invoke a collection of APIs and to subscribe to one API multiple times with different SLA levels. ',
        'showSkip' : false
    },
    {
        selector: '#application-selection-list',
        selectNextElement : '.dropdown-menu',
        targetNext : 'INSIDE_PARENT',
        event: 'click', 
        description: 'Let’s select <b class="enjoy_hint_emphasize_text">NewApplication</b> from drop-down list..',
        'showSkip' : false
    }
];

var api_store_view_application_subscriptions = [
    {
        selector: '#actionLink-productionKeys',
        event: 'click',
        description: 'Click the <b class="enjoy_hint_emphasize_text">Production keys</b> to generate production keys for the application.',
        'showSkip' : false
    },
    {
        selector: '.generatekeys',
        event: 'click',
        description: 'Click <b class="enjoy_hint_emphasize_text">Generate keys</b> to get your access token, which is valid for 60 minutes by default.',
        'showSkip' : false,
        timeout : 500
    },
    {
        selector: '#Key',
        targetNext : 'PARENT',
        event : 'key',
        'keyCode': 9,
        description: 'You have  now generated an access token that you can use to invoke the APIs.  ' 
        + 'This token will be automatically populated in the API Console. Next, Press <b class="enjoy_hint_emphasize_text"> Tab </b> to goto Subscriptions. ',
        'showSkip' : false,
        timeout : 1200
    },
    {
        selector: '#subscriptions-tab',
        event: 'click',
        description: 'Click <b class="enjoy_hint_emphasize_text">Subscriptions</b> to view the APIs that you have subscribed to.',
        'showSkip' : false
    },
    {
        selector: '#subscription-table',
        event: 'click',
        description: 'You can see the list of APIs that are subscribed to this application. Next, Click the <b class="enjoy_hint_emphasize_text" id="selectedAPIName"> ' + getSelectedAPIName() + '</b> API link.',
        'showSkip' : false
    }
];

var api_store_view_application_subscriptions_for_generated_keys = [
    {
        selector: '#actionLink-productionKeys',
        event: 'click',
        description: 'Click the <b class="enjoy_hint_emphasize_text">Production keys</b> tab.',
        'showSkip' : false
    },
    {
        selector: '#Key',
        targetNext : 'PARENT',
        event : 'key',
        'keyCode': 9,
        description: 'You have  now generated an access token that you can use to invoke the APIs.  ' 
        + 'This token will be automatically populated in the API Console. Next, Press <b class="enjoy_hint_emphasize_text"> Tab </b> to goto Subscriptions. ',
        'showSkip' : false,
        timeout : 1200
    },
    {
        selector: '#subscriptions-tab',
        event: 'click',
        description: 'Click <b class="enjoy_hint_emphasize_text">Subscriptions</b> to view the APIs that you have subscribed to.',
        'showSkip' : false
    },
    {
        selector: '#subscription-table',
        event: 'click',
        description: 'You can see the list of APIs that are subscribed to this application. Next, Click the <b class="enjoy_hint_emphasize_text">' + getSelectedAPIName() + '</b> API link.',
        'showSkip' : false
    }
];

var api_store_api_info_page_subscribe = [
    {
        selector: '#application-selection-list',
        event: 'click',
        description: 'Click this dropdown to select the application list',
        'showSkip' : false
    },
    {
        selector: '#application-selection-list',
        selectNextElement : '.dropdown-menu',
        targetNext : 'INSIDE_PARENT',
        event: 'click',
        description: 'Let’s select <b class="enjoy_hint_emphasize_text">TestApplication</b> from the drop-down list.',
        'showSkip' : false
    },
    {
        selector: '#application-selection-list',
        event: 'click',
        description: 'Please wait...',
        'showSkip' : false
    },
    {
        selector: '#subscribe-button',
        event: 'click',
        description: 'Next, click <b class="enjoy_hint_emphasize_text">Subscribe</b> and wait until subscription is granted.',
        'showSkip' : false,
        timeout : 500
    },
    {
        selector: '.modal-content',
        selectNextElement : '.btn-primary',
        targetNext : 'INSIDE_PARENT',
        event: 'click',
        description: 'You have now subscribed to an API. Click <b class="enjoy_hint_emphasize_text">View Subscriptions</b> to see details of the subscriptions.',
        'showSkip' : false,
        timeout : 400
    }
];

var api_store_api_info_page_api_console = [
    {
        selector: '#1',
        event: 'click',
        description: 'Let’s click the <b class="enjoy_hint_emphasize_text">API Console</b> tab of this API. You can test the API using the API Console.',
        'showSkip' : false
    },
    {
        selector: '#access_token',
        event : 'key',
        'keyCode': 9,
        description: 'This is the generated access token, which you can use to invoke this API. Click here and press Tab.',
        'showSkip' : false,
        timeout : 400
    },
    {
        selector: '#operations-tag-default',
        selectNextElement : '.opblock-summary-method',
        targetNext : 'INSIDE_PARENT',
        event: 'click',
        description: 'Next, click this <b class="enjoy_hint_emphasize_text" id="interactiveTutorialHTTPMethod">' + getHTTPMethod() + '</b> box to extend the API testing environment.<br/>' 
        + 'You may need some parameters to get the response from the API. You can get the response from this API while Click this button. So, Click <b class="enjoy_hint_emphasize_text">Try it out</b> to test the API.',
        'showSkip' : false
    }
];