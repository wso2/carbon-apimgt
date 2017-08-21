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

import React, {Component} from 'react'
import {Table, Popconfirm} from 'antd';
import InlineEditor from "./InlineEditor";
import API from '../../../../data/api.js'
import Loading from '../../../Base/Loading/Loading'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'

class DocumentsTable extends Component {
    constructor(props) {
        super(props);
        this.api_id = this.props.apiId;
        this.viewDocContentHandler=this.viewDocContentHandler.bind(this);
        this.handleCloseModal = this.handleCloseModal.bind(this);
        this.state = {
            showInlineEditor: false,
            documentId: null,
            selectedDocName: null
        }
        //TODO: Add permission/valid scope checks for document Edit/Delete actions
    }

    componentDidMount() {
        const api = new API();
        let promised_api = api.get(this.api_id);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    /*
     On click listener for 'View' link on each document related row in the documents table.
     1- If the document type is 'URL' open it in new tab
     2- If the document type is 'INLINE' open the content with an inline editor
     3- If the document type is 'FILE' download the file
     */
    viewDocContentHandler(document) {
        if (document.sourceType === "URL") {
            window.open(document.sourceUrl, '_blank');
        } else if (document.sourceType === "INLINE") {
                this.setState({
                    documentId:document.documentId,
                    showInlineEditor:true,
                    selectedDocName:document.name
                });
        } else if (document.sourceType === "FILE") {
            let promised_get_content = this.props.client.getFileForDocument(this.props.apiId, document.documentId);
            promised_get_content.then((done) => {
                this.props.downloadFile(done);
            }).catch((error_response) => {
                let error_data = JSON.parse(error_response.data);
                let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                console.error(messageTxt);
            });
        }
    }

    handleCloseModal () {
        this.setState({ showInlineEditor: false });
    }

    render() {
        if (!this.state.api) {
            return <Loading/>
        }
        this.columns = [{
            title: 'Name',
            dataIndex: 'name',
            key: 'name'
        }, {
            title: 'Source',
            dataIndex: 'sourceType',
            key: 'sourceType'
        }, {
            title: 'Actions',
            dataIndex: 'actions',
            key: 'actions',
            render: (text1, record) => (<div>
                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                    <a href="#" onClick={() => this.props.onEditAPIDocument(record)}>Edit | </a>
                </ApiPermissionValidation>
                <a href="#" onClick={() => this.viewDocContentHandler(record)}>View | </a>
                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                    <Popconfirm title="Are you sure you want to delete this document?"
                                onConfirm={() => this.props.deleteDocHandler(record.documentId)}
                                okText="Yes" cancelText="No">
                        <a href="#">Delete</a>
                    </Popconfirm>
                </ApiPermissionValidation>
            </div>)
        }
        ];
        return (
            <div style={{paddingTop: 20}}>
                <h3 style={{paddingBottom: 15}}>Current Documents</h3>
                <Table dataSource={ this.props.documentsList } columns={this.columns}/>
                {this.state.showInlineEditor &&
                <InlineEditor showInlineEditor={this.state.showInlineEditor}
                              handleCloseModal={this.handleCloseModal}
                              apiId={this.props.apiId}
                              documentId={this.state.documentId}
                              documentName={this.state.selectedDocName}
                              client={this.props.client}
                />
                }
            </div>
        );
    }
}

export default DocumentsTable;