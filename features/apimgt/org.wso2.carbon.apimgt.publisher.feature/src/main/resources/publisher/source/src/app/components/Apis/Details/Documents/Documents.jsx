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
import API from '../../../../data/api.js';
import DocumentsTable from './DocumentsTable';
import NewDocDiv from './NewDocDiv';
import { Progress } from '../../../Shared/';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import { ScopeValidation, resourcePath, resourceMethod } from '../../../../data/ScopeValidation';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

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
        this.api_id = this.props.match.params.api_uuid;
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
        this.addNewDocBtnListener = this.addNewDocBtnListener.bind(this);
        this.handleDocInputChange = this.handleDocInputChange.bind(this);
        this.submitAddNewDocListener = this.submitAddNewDocListener.bind(this);
        this.cancelAddOrEditDocListener = this.cancelAddOrEditDocListener.bind(this);
        this.resetNewDocDetails = this.resetNewDocDetails.bind(this);
        this.deleteDocHandler = this.deleteDocHandler.bind(this);
        this.addNewDocBtnListener = this.addNewDocBtnListener.bind(this);
        this.editAPIDocumentListener = this.editAPIDocumentListener.bind(this);
        this.submitUpdateDocumentListener = this.submitUpdateDocumentListener.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Documents
     */
    componentDidMount() {
        const api = new API();
        const promisedAPI = api.get(this.api_id);
        promisedAPI
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
        this.getDocumentsList();
    }

    /*
     Get the document list attached to current API and set it to the state
     */
    getDocumentsList() {
        const docs = this.client.getDocuments(this.api_id);
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

    /*
     Onclick Listener for the button "Add New Document"
     */
    addNewDocBtnListener() {
        if (!this.state.addingNewDoc) {
            this.setState({ addingNewDoc: true });
        }
    }

    /*
     Onclick listener for the 'Add' button in the new document adding div.
     */
    submitAddNewDocListener() {
        if (
            this.state.docSourceType == null ||
            this.state.docName === '' ||
            (this.state.docSourceType === 'URL' && this.state.docSourceURL === '') ||
            (this.state.docSourceType === 'FILE' && this.state.docFile == null)
        ) {
            message.error('Enter the required details before adding the document');
            return;
        }

        const apiDocumentsData = {
            documentId: '',
            name: this.state.docName,
            type: 'HOWTO',
            summary: this.state.docSummary,
            sourceType: this.state.docSourceType,
            sourceUrl: this.state.docSourceURL,
            inlineContent: '',
            permission:
                '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
            visibility: 'API_LEVEL',
            fileName: this.state.docFilePath != null ? this.state.docFilePath.replace(/^.*[\\\/]/, '') : null,
        };
        const promisedAdd = this.client.addDocument(this.api_id, apiDocumentsData);
        promisedAdd
            .then((done) => {
                const dtData = done.obj;
                const docId = dtData.documentId;
                let promisedAddFile;
                if (apiDocumentsData.sourceType === 'FILE') {
                    const file = this.state.docFile;
                    promisedAddFile = this.client.addFileToDocument(this.api_id, docId, file);
                    promisedAddFile.catch((errorResponse) => {
                        const errorData = JSON.parse(errorResponse.mesage);
                        const messageTxt =
                            'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                        console.error(messageTxt);
                        message.error('Failed adding file to the newly added document');
                    });
                }

                apiDocumentsData.documentId = docId;
                const updatedDocList = this.state.documentsList;
                updatedDocList.push(apiDocumentsData);
                this.setState({
                    documentsList: updatedDocList,
                });
                this.resetNewDocDetails();
                message.success('New document added successfully');
            })
            .catch((errorResponse) => {
                const errorData = JSON.parse(errorResponse.message);
                const messageTxt =
                    'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                console.error(messageTxt);
                message.error('Failure in adding new document');
            });
    }

    /*
     On change handler for the input fields of new/editing document information
     */
    handleDocInputChange(event) {
        const { name } = event.target;
        this.setState({
            [name]: event.target.value,
        });

        if (name === 'docSourceType') {
            if (event.target.value === 'URL') {
                this.setState({
                    docFilePath: null,
                });
            } else if (event.target.value === 'INLINE') {
                this.setState({
                    docSourceURL: '',
                    docFilePath: null,
                });
            } else if (event.target.value === 'FILE') {
                this.setState({
                    docSourceURL: '',
                });
            }
        }

        if (event.target.type === 'file') {
            this.setState({
                docFile: event.target.files[0],
            });
        }
    }

    /*
     On click listener for 'Cancel' button on new document adding or document editing form
     */
    cancelAddOrEditDocListener() {
        this.resetNewDocDetails();
    }

    /*
     Reset the new/editing document information in the state
     */
    resetNewDocDetails() {
        this.setState({
            docName: '',
            docSourceType: 'INLINE',
            docSourceURL: '',
            docFilePath: null,
            addingNewDoc: false,
            docSummary: '',
            docFile: null,
            updatingDoc: false,
        });
    }

    /*
     OnClick listener for 'Delete' action button on each document related row in the documents table
     -Delete the given document from the API
     */
    deleteDocHandler(documentID) {
        const promisedDelete = this.client.deleteDocument(this.api_id, documentID);
        promisedDelete.then((response) => {
            if (!response) {
                return;
            }
            this.getDocumentsList();
        });
    }

    /*
     On click listener for 'Edit' link on each document related row in the documents table
     -Opens the document information editing div
     */
    editAPIDocumentListener(document) {
        this.setState({
            documentId: document.documentId,
            docName: document.name,
            docSourceType: document.sourceType,
            docSourceURL: document.sourceUrl,
            docFilePath: document.fileName,
            addingNewDoc: false,
            docSummary: document.summary,
            updatingDoc: true,
        });
        this.initialDocSourceType = document.sourceType;
    }

    /*
     OnClick listener for 'Update' button on document editing page.
     -Updates the document with the new information entered
     */
    submitUpdateDocumentListener() {
        if (
            this.state.docSourceType == null ||
            this.state.docName === '' ||
            (this.state.docSourceType === 'URL' && this.state.docSourceURL === '')
        ) {
            message.error('Enter the required details before adding the document');
            return;
        }

        const apiDocumentsData = {
            documentId: this.state.documentId,
            name: this.state.docName,
            type: 'HOWTO',
            summary: this.state.docSummary,
            sourceType: this.state.docSourceType,
            sourceUrl: this.state.docSourceURL,
            inlineContent: '',
            permission:
                '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
            visibility: 'API_LEVEL',
            fileName: this.state.docFilePath != null ? this.state.docFilePath.replace(/^.*[\\\/]/, '') : null,
        };
        const promised_update = this.client.updateDocument(this.api_id, apiDocumentsData.documentId, apiDocumentsData);
        promised_update
            .then((response) => {
                const dt_data = response.obj;
                const docId = dt_data.documentId;
                let promised_add_file = new Promise(() => {});
                let promised_add_empty_inline_content = new Promise(() => {});
                if (dt_data.sourceType === 'FILE') {
                    if (this.state.docFile != null) {
                        promised_add_file = this.client.addFileToDocument(this.api_id, docId, this.state.docFile);
                        promised_add_file.catch((error_response) => {
                            const error_data = JSON.parse(error_response.message);
                            const messageTxt =
                                'Error[' +
                                error_data.code +
                                ']: ' +
                                error_data.description +
                                ' | ' +
                                error_data.message +
                                '.';
                            console.error(messageTxt);
                            message.error('Failed updating document file');
                        });
                    }
                } else {
                    promised_add_file = Promise.resolve();
                }
                if (this.initialDocSourceType != apiDocumentsData.sourceType) {
                    // source type has been changed
                    if (apiDocumentsData.sourceType === 'INLINE') {
                        // Add empty inline content to document when the source type was changed to 'INLINE'
                        promised_add_empty_inline_content = this.client.addInlineContentToDocument(
                            this.api_id,
                            docId,
                            '<p></p>',
                        );
                        promised_add_empty_inline_content.catch((error_response) => {
                            const error_data = JSON.parse(error_response.data);
                            const messageTxt =
                                'Error[' +
                                error_data.code +
                                ']: ' +
                                error_data.description +
                                ' | ' +
                                error_data.message +
                                '.';
                            console.error(messageTxt);
                            message.error('Failed adding empty inline content to document');
                        });
                    } else {
                        promised_add_empty_inline_content = Promise.resolve();
                    }
                    if (apiDocumentsData.sourceType === 'FILE') {
                        if (apiDocumentsData.fileName == null) {
                            message.error('A File resource is not selected. Select a File resource to update the source ' +
                                    "type to 'File'");
                        }
                    }
                } else {
                    promised_add_empty_inline_content = Promise.resolve();
                    if (apiDocumentsData.fileName == null) {
                        promised_add_file = Promise.resolve();
                        if (apiDocumentsData.sourceType === 'FILE') {
                            message.info('The FILE resource of the document is not updated as a file resource is not selected');
                        }
                    }
                }
                Promise.all([promised_add_file, promised_add_empty_inline_content]).then(() => {
                    this.resetNewDocDetails();
                    this.getDocumentsList();
                    message.success('Document updated successfully');
                });
                Promise.all([promised_add_file, promised_add_empty_inline_content]).catch(() => {
                    message.error('Error occurred in updating the document');
                });
            })
            .catch((error_response) => {
                const error_data = JSON.parse(error_response.message);
                const messageTxt =
                    'Error[' + error_data.code + ']: ' + error_data.description + ' | ' + error_data.message + '.';
                console.error(messageTxt);
                message.error(messageTxt);
            });
    }

    /*
     Download the document related file
     */
    downloadFile(response) {
        let fileName = '';
        const contentDisposition = response.headers['content-disposition'];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers['content-type'];
        const blob = new Blob([response.data], {
            type: contentType,
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement('a');
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }

    render() {
        const api = this.state.api;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        return (
            <div>
                {/* Allowing adding doc to an API based on scopes */}
                <ScopeValidation resourcePath={resourcePath.API_DOCS} resourceMethod={resourceMethod.POST}>
                    <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                        <Button style={{ marginBottom: 30 }} onClick={this.addNewDocBtnListener} type='primary'>
                            Add New Document
                        </Button>
                    </ApiPermissionValidation>
                </ScopeValidation>
                <div>
                    {(this.state.addingNewDoc || this.state.updatingDoc) && (
                        <NewDocDiv
                            docName={this.state.docName}
                            docSummary={this.state.docSummary}
                            docSourceURL={this.state.docSourceURL}
                            docFilePath={this.state.docFilePath}
                            selectedSourceType={this.state.docSourceType}
                            docFile={this.state.docFile}
                            onDocInfoChange={this.handleDocInputChange}
                            onSubmitAddNewDoc={this.submitAddNewDocListener}
                            onCancelAddOrEditNewDoc={this.cancelAddOrEditDocListener}
                            onSubmitUpdateDoc={this.submitUpdateDocumentListener}
                            updatingDoc={this.state.updatingDoc}
                            addingNewDoc={this.state.addingNewDoc}
                        />
                    )}
                </div>
                <hr color='#f2f2f2' />
                {this.state.documentsList && this.state.documentsList.length > 0 ? (
                    <DocumentsTable
                        apiId={this.api_id}
                        client={this.client}
                        documentsList={this.state.documentsList}
                        deleteDocHandler={this.deleteDocHandler}
                        onEditAPIDocument={this.editAPIDocumentListener}
                        viewDocContentHandler={this.viewDocContentHandler}
                        downloadFile={this.downloadFile}
                    />
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
