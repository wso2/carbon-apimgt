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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import SingleInput from '../../FormComponents/SingleInput';
import CheckboxOrRadioGroup from '../../FormComponents/CheckboxOrRadioGroup';
import API from '../../../../../data/api.js'
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';
import Dropzone from 'react-dropzone'

import { Form, Icon, Input, Button, message, Upload, Radio } from 'antd';
const FormItem = Form.Item;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

class WSDLValidationForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {uploadMethod:'file',file:{}}
    }

    normFile = (e) => {
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };
    handleUploadMethodChange = (e) => {
        this.setState({uploadMethod:e.target.value});
        this.props.onUploadMethodChanged(e.target.value);
    };
    toggleWSDLType = (containerType) =>  {
        return this.state.uploadMethod === containerType ? '' : 'hidden';
    };
    handleUploadFile = (info) => {
        const status = info.file.status;
        if (status !== 'uploading') {
            console.log(info.file, info.fileList);
        }
        if (status === 'done') {
            message.success(`${info.file.name} file uploaded successfully.`);
            this.setState({file:info.file});
            this.props.onWSDLFileChanged(info.file);
        } else if (status === 'error') {
            message.error(`${info.file.name} file upload failed.`);
        }
    };

    handleWSDLUrlChanged = (e) => {
        this.props.onWSDLUrlChanged(e.target.value);
    };

    render(){
        const { getFieldDecorator } = this.props.form;

        const props = {
            name: 'file',
            multiple: false,
            showUploadList: true,
            action: '//jsonplaceholder.typicode.com/posts/'
        };
        return(
            <Form className="login-form">


                <FormItem>
                    {getFieldDecorator('radio-button')(
                        <RadioGroup initialValue="file" onChange={this.handleUploadMethodChange}>
                            <RadioButton value="file">WSDL File</RadioButton>
                            <RadioButton value="url">WSDL URL</RadioButton>
                        </RadioGroup>
                    )}
                </FormItem>
                <FormItem className={this.toggleWSDLType("file")}>
                    <div className="dropbox">
                        {getFieldDecorator('dragger', {
                            valuePropName: 'fileList',
                            getValueFromEvent: this.normFile,
                        })(
                            <Upload.Dragger {...props} onChange={this.handleUploadFile}>
                                <p className="ant-upload-drag-icon">
                                    <Icon type="inbox" />
                                </p>
                                <p className="ant-upload-text">Click or drag file to this area to upload</p>
                                <p className="ant-upload-hint">Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files</p>
                            </Upload.Dragger>
                        )}
                    </div>
                </FormItem>
                <FormItem className={this.toggleWSDLType("url")}>
                    {getFieldDecorator('userName', {
                        rules: [{ required: false, message: 'Please input WSDL URL!' }],
                    })(
                        <Input name="url" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="WSDL URL" onChange={this.handleWSDLUrlChanged}/>
                    )}
                </FormItem>
            </Form>
        );
    }
}

const WSDLValidationFormGenerated = Form.create()(WSDLValidationForm);

class WSDLValidationStep extends React.Component {
    constructor(props) {
        super(props);

    }

    render = () => {
        return <WSDLValidationFormGenerated 
            history={this.props.history}
            onWSDLUrlChanged={this.props.onWSDLUrlChanged}
            onWSDLFileChanged={this.props.onWSDLFileChanged}
            onUploadMethodChanged={this.props.onUploadMethodChanged}
        />
    };
}

export default WSDLValidationStep;
