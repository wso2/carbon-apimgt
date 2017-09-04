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

import React, {Component} from 'react';
import ReactQuill from 'react-quill';
import ReactModal from 'react-modal';
import {Button, Row, Col, message} from 'antd';
import "../../../../../../../node_modules/react-quill/dist/quill.snow.css"
import API from '../../../../data/api.js'
import Loading from '../../../Base/Loading/Loading'
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation'

class InlineEditor extends Component {
    constructor(props) {
        super(props);
        this.api_id = this.props.apiId;
        this.state = {
            editorHtml: "",
        }

        this.handleChange = this.handleChange.bind(this);
        this.saveInlineDocContent = this.saveInlineDocContent.bind(this);
    }

    handleChange(html) {
        this.setState({editorHtml: html});
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

        const promised_inline_content = this.props.client.getInlineContentOfDocument(this.props.apiId, this.props.documentId);
        promised_inline_content.then(response => {
            const contentType = response.headers["content-type"];
            const blob = new Blob([response.data], {
                type: contentType
            });
            var reader = new FileReader();
            reader.onload =  (value) => {
                this.setState({
                    editorHtml: value.target.result
                });
            };
            reader.readAsText(blob);
        }).catch(error_response => {
            const error_data = JSON.parse(error_response.data);
            const messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
            message.error("Failed getting inline content of the document");
        });
    }

    /*
      Save the inline document content
     */
    saveInlineDocContent(close_after_save) {
        const promised_add_inline_content = this.props.client.addInlineContentToDocument(this.props.apiId, this.props.documentId, this.state.editorHtml);
        promised_add_inline_content.then(() => {
            message.success("Saved inline content successfully");
            if (close_after_save) {
                this.props.handleCloseModal();
            }
        }).catch( (error_response) => {
            const error_data = JSON.parse(error_response.data);
            const messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
            message.error("Failed adding inline content to the document");
        });
    }

    render() {
        if (!this.state.api) {
            return <Loading/>
        }
        return (
            <div>
                <div>
                    <ReactModal
                        isOpen={true}
                        contentLabel="Document Inline Editor"
                        style={InlineEditor.customStyle}
                    >
                        <h3 style={{paddingTop: 20}}>Inline Editor</h3>
                        <hr/>
                        <h2>Document Name : {this.props.documentName}</h2>
                        <Row gutter={1} style={{paddingTop: 20}}>
                            <Col span={100}>
                                <div>
                                    <ReactQuill
                                        theme='snow'
                                        onChange={(html) => this.handleChange(html)}
                                        value={this.state.editorHtml}
                                        modules={InlineEditor.modules}
                                        formats={InlineEditor.formats}
                                        bounds={'.app'}
                                        placeholder={this.props.placeholder}
                                        style={{paddingBottom: 50}}
                                    />
                                </div>
                            </Col>
                        </Row>
                        <Row gutter={1}>
                            <ApiPermissionValidation userPermissions={this.state.api.userPermissionsForApi}>
                            <Col span={2}>
                                    <Button type="primary"
                                            onClick={() => this.saveInlineDocContent(false)}>Save</Button>
                            </Col>
                            <Col span={3}>
                                <Button type="primary"
                                        onClick={() => this.saveInlineDocContent(true)}>Save And
                                                                                        Close</Button>
                            </Col>
                            </ApiPermissionValidation>
                            <Col span={3}>
                                <Button type="primary"
                                        onClick={this.props.handleCloseModal}>Close</Button>
                            </Col>
                        </Row>
                    </ReactModal>
                </div>
            </div>
        )
    }
}

/*
 * Quill modules to attach to editor
 * See https://quilljs.com/docs/modules/ for complete options
 */
InlineEditor.modules = {
    toolbar: [
        [{'font': []}],
        [{size: []}, {'header': '1'}, {'header': '2'}, {'header': '3'}, {'header': '4'}],
        [{'color': []}, {'background': []}, 'bold', 'italic', 'underline', 'strike', 'blockquote'],
        [{'list': 'ordered'}, {'list': 'bullet'}, {'align': []}, {'indent': '-1'}, {'indent': '+1'}],
        ['link', 'image', 'video'],
        [{'direction': 'rtl'}, {'script': 'sub'}, {'script': 'super'}, 'clean'],
        ['history']
    ],
    history: {
        delay: 2000,
        maxStack: 500,
        userOnly: true
    }
}
/*
 * Quill editor formats
 * See https://quilljs.com/docs/formats/
 */
InlineEditor.formats = [
    'header', 'font', 'size',
    'bold', 'italic', 'underline', 'strike', 'blockquote',
    'list', 'bullet', 'indent',
    'link', 'image', 'video',
    'clean', 'color', 'background', 'align', 'direction', 'script', 'history'
]

/*
 * PropType validation
 */
InlineEditor.propTypes = {
    placeholder: React.PropTypes.string,
}

InlineEditor.customStyle = {
    overlay : {
        position          : 'fixed',
        top               : 0,
        left              : 0,
        right             : 0,
        bottom            : 0,
        backgroundColor   : 'rgba(79, 79, 101, 0.5)'
    },
    content : {
        top           : '50%',
        left          : '50%',
        right         : 'auto',
        bottom        : 'auto',
        maxWidth      : '90%',
        maxHeight     : '80%',
        marginRight   : '-50%',
        minWidth      : '40%',
        minHeight     : '40%',
        transform     : 'translate(-50%, -50%)'
    }
};

export default InlineEditor;