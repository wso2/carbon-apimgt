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

var item_listing_with_worldbank_script_data = [
    {
        'click #listing-title': 'Welcome to WSO2 API Manager. In this tutorial, we will lead you through' +
        ' publishing and invoking an API based on World Banks country statistics data. As you already ' +
        'have an API named WorldBank, let\'s use a different name and a context when creating the API.' +
        ' Click the highlighted area to continue the interactive tutorial.',
        'showSkip' : false
    },
    {
        'click #top-menu-api-add': 'Click "ADD NEW API" to get started.',
        'showSkip' : false
    }
];

var item_listing_with_apis_script_data = [
    {
        'click #top-menu-api-add': 'Welcome to WSO2 API Manager. In this tutorial, we will lead you through' +
        ' publishing and invoking an API based on World Bank\'s country statistics data. Click "Add" ' +
        'to get started',
        'showSkip' : false

    }
];

var item_listing_script_data = [
    {
        'click #listing': 'Welcome to WSO2 API Manager. In this tutorial, we will lead you through ' +
        'publishing and invoking an API based on World Banks country statistics data. Click the ' +
        'highlighted area to continue the interactive tutorial.',
        'showSkip' : false
    },
    {
        'click #btn-add-new-api': 'Click New API to get started.',
        'showSkip' : false
    }
];

var item_add_script_data = [
    {
        selector: '#create-new-api',
        event: 'click',
        description: 'Let\'s create an API from scratch. Select the Design new API option.',
        shape: 'circle',
        'radius': 15,
        'bottom':2,
        'showSkip' : false
    },
    {
        selector: '#designNewAPI',
        event: 'click',
        description: 'Start creating a new API',
        'showSkip' : false
    }

];

var item_design_script_data = [
    {
        'key #name': 'Lets type WorldBank as the API display name and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #context': 'The API\'s context is included in its URL path. Type "wb" as the context ' +
        'and press Tab.',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #version': 'Type 1.0.0 as the API version and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #resource_url_pattern': 'Let\'s create a REST resource called "countries" with the ' +
        'country\'s code as a parameter. Type "countries/{code}" as the URL Pattern and press Tab.',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        selector: '#get',
        event: 'click',
        description: 'Click the GET option to allow the GET HTTP method for this URL pattern.',
        shape: 'circle',
        'showSkip' : false
    },
    {
        'click #add_resource': 'Click Add to add this pattern to the list of allowed REST resources',
        'showSkip' : false
    },
    {
        'click #go_to_implement': 'Next, click Implement to proceed to the implementation phase of' +
        ' the API creation.',
        'showSkip' : false
    }
];

var item_design_with_worldbank_api_script_data = [
    {
        'key #name': 'Lets type API display name. API name is unique. As you already have an ' +
        'API named "WorldBank" let\'s type a different name. Ex: WorldBank2, WorldBankTest etc.. ' +
        'and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #context': 'Context is what API will have in the URL path. API context also cannot be ' +
        'duplicate. So let\'s type a different context than "wb". Ex: wb1, wbtest etc... and press Tab.',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #version': 'Type 1.0.0 as the API version and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'key #resource_url_pattern': 'Lets define REST resource "countries" that then takes country ' +
        'code as the parameter. To do this, type "countries/{code}" as the URL Pattern and press Tab.',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        selector: '#get',
        event: 'click',
        description: 'Select the GET checkbox to allow GET method for this URL pattern',
        shape: 'circle',
        'showSkip' : false
    },
    {
        'click #add_resource': 'Click Add to add this pattern to the list of allowed REST resources',
        'showSkip' : false
    },
    {
        'click #go_to_implement': 'Now click Implement to proceed to the implementation phase of the' +
        ' API creation',
        'showSkip' : false
    }
];

var item_implement_script_data = [

    {
        'click #select-managed-api': 'Let\'s implement the actual API rather than a prototype. Click' +
        ' the Managed API option to proceed.',
        'showSkip' : false
    },
    {
        'key #endpoint-input': 'Provide an existing backend service URL for World ' +
        'Bank data in our example, type: http://api.worldbank.org and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'click #go_to_manage': 'Next, click the "Next: Manage" button to proceed to the last phase of' +
        ' the API creation.',
        'showSkip' : false
    }

];

var item_info_script_data = [
    {
        'click #goToStore': 'Click the Go to API Store link to open the API in your default API Store.',
        'showSkip' : false
    }

];

var item_manage_script_data = [
    {
        'click #Gold': 'Let\'s select usage tiers or subscription plans that you offer to subscribers. ' +
        'Select Gold from the Subscription Tiers.',
        'showSkip' : false
    },
    {
        'click #publish_api': 'Click Save & Publish to publish the API in the API Store.',
        'showSkip' : false
    }
];

var item_manage_success_message_script_data = [
    {
        'click #goToStore-btn': 'Your API is now published. Click "Go to API Store" to see the API ' +
        'in the API Store and subscribe to it.',
        'showSkip' : false
    }
];
