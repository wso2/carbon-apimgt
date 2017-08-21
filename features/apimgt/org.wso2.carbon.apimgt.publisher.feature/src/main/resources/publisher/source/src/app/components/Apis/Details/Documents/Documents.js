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
import API from '../../../../data/api.js'
import {Button, message} from 'antd';
import DocumentsTable from './DocumentsTable';
import NewDocDiv from './NewDocDiv';
import Loading from '../../../Base/Loading/Loading'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'

/*
 Documents tab related React components.
 # Component hierarchy
 -Documents
    -DocumentsTable
        -InlineEditor
    -NewDocDiv
        -NewDocInfoDiv
        -NewDocSourceDiv
 */
class Documents extends Component {
    constructor(props) {
        super(props);
        this.client = new API();
        this.api_id = this.props.match.params.api_uuid;
        //New or editing documents' information are maintained in state
        // (docName, documentId, docSourceType, docSourceURL, docFilePath, docSummary, docFile)
        this.state = {
            docName: "",
            documentId: "",
            docSourceType: "INLINE",
            docSourceURL: "",
            docFilePath: null,
            docSummary: "",
            docFile: null,
            addingNewDoc: false,
            documentsList: null,
            updatingDoc: false
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
        this.getDocumentsList();
    }

    /*
     Get the document list attached to current API and set it to the state
     */
    getDocumentsList() {
        let docs = this.client.getDocuments(this.api_id);
        docs.then(response => {
            this.setState({documentsList: response.obj.list});
        }).catch(error_response => {
            let error_data = JSON.parse(error_response.message);
            let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
            message.error("Error in fetching documents list of the API");
        });
    }

    /*
     Onclick Listener for the button "Add New Document"
     */
    addNewDocBtnListener() {
        if (!this.state.addingNewDoc) {
            this.setState({addingNewDoc: true});
        }
    }

    /*
     Onclick listener for the 'Add' button in the new document adding div.
     */
    submitAddNewDocListener() {
        if (
            (this.state.docSourceType == null) || (this.state.docName === "") ||
            (this.state.docSourceType === "URL" && this.state.docSourceURL === "") ||
            (this.state.docSourceType === "FILE" && this.state.docFile == null)
        ) {
            message.error("Enter the required details before adding the document");
            return;
        }

        const api_documents_data = {
            documentId: "",
            name: this.state.docName,
            type: "HOWTO",
            summary: this.state.docSummary,
            sourceType: this.state.docSourceType,
            sourceUrl: this.state.docSourceURL,
            inlineContent: "",
            permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
            visibility: "API_LEVEL",
            fileName: (this.state.docFilePath != null) ? this.state.docFilePath.replace(/^.*[\\\/]/, '') : null
        }
        const promised_add = this.client.addDocument(this.api_id, api_documents_data);
        promised_add.then(done => {
            const dt_data = done.obj;
            const docId = dt_data.documentId;
            let promised_add_file;
            if (api_documents_data.sourceType === "FILE") {
                const file = this.state.docFile;
                promised_add_file = this.client.addFileToDocument(this.api_id, docId, file);
                promised_add_file.catch(function (error_response) {
                    const error_data = JSON.parse(error_response.mesage);
                    const messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                    console.error(messageTxt);
                    message.error("Failed adding file to the newly added document");
                    return;
                });
            }

            api_documents_data.documentId = docId;
            const updatedDocList = this.state.documentsList;
            updatedDocList.push(api_documents_data);
            this.setState({
                documentsList: updatedDocList,
            });
            this.resetNewDocDetails();
            message.success("New document added successfully");


        }).catch(error_response => {
            const error_data = JSON.parse(error_response.message);
            const messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
            message.error("Failure in adding new document");
        });
    }

    /*
     On change handler for the input fields of new/editing document information
     */
    handleDocInputChange(event) {
        const name = event.target.name;
        this.setState({
            [name]: event.target.value
        });

        if (name === "docSourceType") {
            if (event.target.value === "URL") {
                this.setState({
                    docFilePath: null
                });
            }
            else if (event.target.value === "INLINE") {
                this.setState({
                    docSourceURL: "",
                    docFilePath: null
                });
            } else if (event.target.value === "FILE") {
                this.setState({
                    docSourceURL: ""
                });
            }
        }

        if (event.target.type === "file") {
            this.setState({
                docFile: event.target.files[0]
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
            docName: "",
            docSourceType: "INLINE",
            docSourceURL: "",
            docFilePath: null,
            addingNewDoc: false,
            docSummary: "",
            docFile: null,
            updatingDoc: false
        });
    }

    /*
     OnClick listener for 'Delete' action button on each document related row in the documents table
     -Delete the given document from the API
     */
    deleteDocHandler(documentID) {
        let promised_delete = this.client.deleteDocument(this.api_id, documentID);
        promised_delete.then(
            (response) => {
                if (!response) {
                    return;
                }
                this.getDocumentsList();
            }
        );
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
            updatingDoc: true
        });
        this.initialDocSourceType = document.sourceType;
    }

    /*
     OnClick listener for 'Update' button on document editing page.
     -Updates the document with the new information entered
     */
    submitUpdateDocumentListener() {
        if (
            (this.state.docSourceType == null) || (this.state.docName === "") ||
            (this.state.docSourceType === "URL" && this.state.docSourceURL === "")
        ) {
            message.error("Enter the required details before adding the document");
            return;
        }

        const api_documents_data = {
            documentId: this.state.documentId,
            name: this.state.docName,
            type: "HOWTO",
            summary: this.state.docSummary,
            sourceType: this.state.docSourceType,
            sourceUrl: this.state.docSourceURL,
            inlineContent: "",
            permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
            visibility: "API_LEVEL",
            fileName: (this.state.docFilePath != null) ? this.state.docFilePath.replace(/^.*[\\\/]/, '') : null
        }
        const promised_update = this.client.updateDocument(this.api_id, api_documents_data.documentId, api_documents_data);
        promised_update.then(response => {
            const dt_data = response.obj;
            const docId = dt_data.documentId;
            let promised_add_file = new Promise(() => {});
            let promised_add_empty_inline_content = new Promise(() => {});
            if (dt_data.sourceType === "FILE") {
                if (this.state.docFile != null) {
                    promised_add_file = this.client.addFileToDocument(this.api_id, docId, this.state.docFile);
                    promised_add_file.catch((error_response) => {
                        const error_data = JSON.parse(error_response.message);
                        const messageTxt =
                            "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                        console.error(messageTxt);
                        message.error("Failed updating document file")
                    });
                }
            } else {
                promised_add_file = Promise['resolve']();
            }
            if (this.initialDocSourceType != api_documents_data.sourceType) { //source type has been changed
                if (api_documents_data.sourceType === "INLINE") {
                    //Add empty inline content to document when the source type was changed to 'INLINE'
                    promised_add_empty_inline_content =
                        this.client.addInlineContentToDocument(this.api_id, docId, "<p></p>");
                    promised_add_empty_inline_content.catch((error_response) => {
                        const error_data = JSON.parse(error_response.data);
                        const messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " +
                            error_data.message + ".";
                        console.error(messageTxt);
                        message.error("Failed adding empty inline content to document");
                    });
                } else {
                    promised_add_empty_inline_content = Promise['resolve']();
                }
                if(api_documents_data.sourceType === "FILE") {
                    if (api_documents_data.fileName == null) {
                        message.error("A File resource is not selected. Select a File resource to update the source " +
                            "type to 'File'");
                    }
                }
            } else {
                promised_add_empty_inline_content = Promise['resolve']();
                if (api_documents_data.fileName == null) {
                    promised_add_file = Promise['resolve']();
                    if(api_documents_data.sourceType === "FILE") {
                        message.info("The FILE resource of the document is not updated as a file resource is not selected");
                    }
                }
            }
            Promise.all([promised_add_file, promised_add_empty_inline_content]).then(() => {
                this.resetNewDocDetails();
                this.getDocumentsList();
                message.success("Document updated successfully");
            });
            Promise.all([promised_add_file, promised_add_empty_inline_content]).catch(() => {
                message.error("Error occurred in updating the document")
            });
        }).catch(function (error_response) {
            let error_data = JSON.parse(error_response.message);
            let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
            message.error(messageTxt);
        });
    }

    /*
     Download the document related file
     */
    downloadFile(response) {
        let fileName = "";
        const contentDisposition = response.headers["content-disposition"];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers["content-type"];
        const blob = new Blob([response.data], {
            type: contentType
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement("a");
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

            setTimeout(function () {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }

    render() {
        if (!this.state.api) {
            return <Loading/>
        }
        return (
            <div>
                <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                    <Button style={{marginBottom: 30}} onClick={this.addNewDocBtnListener}
                         type="primary">Add New Document</Button>
                </ApiPermissionValidation>
                <div>
                    {(this.state.addingNewDoc || this.state.updatingDoc) &&
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
                    />}
                </div>
                <hr color="#f2f2f2"/>
                {
                    (this.state.documentsList && (this.state.documentsList.length > 0) ) ? (
                        <DocumentsTable apiId={this.api_id}
                                        client={this.client}
                                        documentsList={this.state.documentsList}
                                        deleteDocHandler={this.deleteDocHandler}
                                        onEditAPIDocument={this.editAPIDocumentListener}
                                        viewDocContentHandler={this.viewDocContentHandler}
                                        downloadFile={this.downloadFile}
                        /> ) :
                        (<div style={{paddingTop: 20}}><p>No documents added into the API</p></div>)
                }
            </div>
        );
    }
}

export default Documents;