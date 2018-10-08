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

import { Button, message } from 'antd';

import React, { Component } from 'react';
import API from 'AppData/api.js';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import { ScopeValidation, resourcePath, resourceMethod } from 'AppData/ScopeValidation';
import { Progress } from 'AppComponents/Shared/';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

import DocumentsTable from './DocumentsTable';
import NewDocDiv from './NewDocDiv';
import DocTableView from './DocTableView';
import DocMenu from './DocMenu';

/**
 * Documents tab related React components.
 * Component hierarchy
 * -Documents
 *    -DocumentsTable
 *        -InlineEditor
 *    -NewDocDiv
 *        -NewDocInfoDiv
 *        -NewDocSourceDiv
 * @class Documents
 * @extends {Component}
 */
class Documents extends Component {
    /**
     * Creates an instance of Documents.
     * @param {any} props @inheritDoc
     * @memberof Documents
     */
    constructor(props) {
        super(props);
        this.client = new API();
        this.apiUUID = this.props.match.params.apiUUID;
        // New or editing documents' information are maintained in state
        // (docName, documentId, docSourceType, docSourceURL, docFilePath, docSummary, docFile)
        this.state = {
            docName: '',
            documentId: '',
            docSourceType: 'INLINE',
            docSourceURL: '',
            docFilePath: null,
            docSummary: '',
            docFile: null,
            addingNewDoc: false,
            documentsList: null,
            updatingDoc: false,
            notFound: false,
        };
        this.initialDocSourceType = null;
    }

    /**
     * @inheritDoc
     * @memberof Documents
     */
    componentDidMount() {
        const api = new API();
        this.getDocumentsList();
    }

    /*
     Get the document list attached to current API and set it to the state
     */
    getDocumentsList() {
        const docs = this.client.getDocuments(this.props.api.id);
        docs
            .then((response) => {
                this.setState({ documentsList: response.obj.list });
            })
            .catch((errorResponse) => {
                const errorData = JSON.parse(errorResponse.message);
                const messageTxt =
                    'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                console.error(messageTxt);
                message.error('Error in fetching documents list of the API');
            });
    }

    /** @inheritDoc */
    render() {
        const { api } = this.props;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        return (
            <div>
                <hr color='#f2f2f2' />
                {this.state.documentsList && this.state.documentsList.length > 0 ? (
                    <div>
                        <DocMenu />
                        <DocTableView docs={this.state.documentsList}/>
                    </div>
                ) : (
                    <div style={{ paddingTop: 20 }}>
                        <p>No documents added into the API</p>
                    </div>
                )}
            </div>
        );
    }
}

export default Documents;
