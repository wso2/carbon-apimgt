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
import React from 'react';
import SingleInput from '../FormComponents/SingleInput';
import CheckboxOrRadioGroup from '../FormComponents/CheckboxOrRadioGroup';
import API from '../../../../data/api.js'
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';
import './ApiCreateSwagger.css'
import Dropzone from 'react-dropzone'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../../data/ScopeValidation'

import { Form, Icon, Input, Button, message, Upload, Radio } from 'antd';
const FormItem = Form.Item;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

class SwaggerForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {uploadMethod:'file',file:{}}
    }
    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
    };
    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit = (e) => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                console.log('Received values of form: ', values);
                let input_type = this.state.uploadMethod;
                if (input_type === "url") {
                    let url = values.url;
                    let data = {};
                    data.url = url;
                    data.type = 'swagger-url';
                    let new_api = new API('');
                    new_api.create(data)
                        .then(this.createAPICallback)
                        .catch(
                            function (error_response) {
                                let error_data = JSON.parse(error_response.data);
                                let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                                message.error(messageTxt);
                                console.debug(error_response);
                            });
                } else if (input_type === "file") {
                    let swagger = this.state.file.originFileObj;
                    let new_api = new API('');
                    new_api.create(swagger)
                        .then(this.createAPICallback)
                        .catch(
                            function (error_response) {
                                let error_data;
                                let messageTxt;
                                if (error_response.obj) {
                                    error_data = error_response.obj;
                                    messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                                } else {
                                    error_data = error_response.data;
                                    messageTxt = "Error: " + error_data + ".";

                                }
                                message.error(messageTxt);
                                console.debug(error_response);
                            });
                }
            } else {

            }
        });
    };
    normFile = (e) => {
        console.log('Upload event:', e);
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };
    handleUploadMethodChange = (e) => {
        this.setState({uploadMethod:e.target.value});
    };
    toggleSwaggerType = (containerType) =>  {
        return this.state.uploadMethod === containerType ? '' : 'hidden';
    };
    handleUploadFile = (info) => {
        const status = info.file.status;
        if (status !== 'uploading') {
            console.log(info.file, info.fileList);
        }
        if (status === 'done') {
            message.success(`${info.file.name} file uploaded successfully.`);
            this.setState({file:info.file})
        } else if (status === 'error') {
            message.error(`${info.file.name} file upload failed.`);
        }
    }

    render(){
        const { getFieldDecorator } = this.props.form;

        const props = {
            name: 'file',
            multiple: false,
            showUploadList: true,
            action: '//jsonplaceholder.typicode.com/posts/'
        };
        return(
            <Form onSubmit={this.handleSubmit} className="login-form">


                <FormItem>
                    {getFieldDecorator('radio-button')(
                        <RadioGroup initialValue="file" onChange={this.handleUploadMethodChange}>
                            <RadioButton value="file">Swagger File</RadioButton>
                            <RadioButton value="url">Swagger URL</RadioButton>
                        </RadioGroup>
                    )}
                </FormItem>
                <FormItem className={this.toggleSwaggerType("file")}>
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
                <FormItem className={this.toggleSwaggerType("url")}>
                    {getFieldDecorator('userName', {
                        rules: [{ required: false, message: 'Please input your username!' }],
                    })(
                        <Input name="url" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Username" />
                    )}
                </FormItem>
                <FormItem >
                    <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.APIS}>
                        <Button type="primary" htmlType="submit">
                            Create
                        </Button>
                    </ScopeValidation>
                    <Button type="default" htmlType="button" onClick={() => this.props.history.push("/api/create/home")}>
                        Cancel
                    </Button>

                </FormItem>

            </Form>
        );
    }
}

const SwaggerFormGenerated = Form.create()(SwaggerForm);

class ApiCreateSwagger extends React.Component {
    constructor(props) {
        super(props);

    }
    render = () => {return  <SwaggerFormGenerated history={this.props.history} /> }
}

export default ApiCreateSwagger;
