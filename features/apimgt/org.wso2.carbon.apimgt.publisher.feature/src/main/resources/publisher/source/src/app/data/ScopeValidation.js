/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
"use strict";

import React from 'react';
import AuthManager from './AuthManager'
import {Button} from 'antd'

const resourcePath = {
    APIS : "/apis",
    SINGLE_API :"/apis/{apiId}",
    API_SWAGGER : "/apis/{apiId}/swagger",
    API_WSDL : "/apis/{apiId}/wsdl",
    API_GW_CONFIG : "/apis/{apiId}/gateway-config",
    API_THUMBNAIL : "/apis/{apiId}/thumbnail",
    API_COPY : "/apis/copy-api",
    API_LC_HISTORY : "/apis/{apiId}/lifecycle-history",
    API_CHANGE_LC : "/apis/change-lifecycle",
    API_LC : "/apis/{apiId}/lifecycle",
    API_LC_PENDING_TASK : "/apis/{apiId}/lifecycle/lifecycle-pending-task",
    API_DEF : "/apis/import-definition",
    API_VALIDATE_DEF : "/apis/validate-definition",
    API_DOCS : "/apis/{apiId}/documents",
    API_DOC : "'/apis/{apiId}/documents/{documentId}'",
    API_DOC_CONTENT : "'/apis/{apiId}/documents/{documentId}/content'",
    EXPORT_APIS : "/export/apis",
    IMPORT_APIS : "/import/apis",
    SUBSCRIPTION : "/subscriptions",
    SUBSCRIPTIONS : "/subscriptions",
    BLOCK_SUBSCRIPTION : "/subscriptions/block-subscription:",
    UNBLOCK_SUBSCRIPTION : "/subscriptions/unblock-subscription",
    POLICIES : "'/policies/{tierLevel}'",
    POLICY : "'/policies/{tierLevel}/{tierName}'",
    ENDPOINTS : "/endpoints",
    ENDPOINT : "/endpoints/{endpointId}",
    LABLES : "/labels",
    WORKFLOW : "/workflows/{workflowReferenceId}"
};

const resourceMethod = {
    POST : "post",
    PUT : "put",
    GET : "get",
    DELETE : "delete"
}

class ScopeValidation extends React.Component {
    constructor(props){
        super(props);
        this.state = {};
    }

    componentDidMount(){
        let hasScope = AuthManager.hasScopes(this.props.resourcePath, this.props.resourceMethod);
        hasScope.then(haveScope => {this.setState({haveScope: haveScope})})
    }

    render() {
        if(this.state.haveScope) {
            return (this.props.children);
        }
        return null;
    }
}

module.exports = {
    ScopeValidation,
    resourceMethod,
    resourcePath
}