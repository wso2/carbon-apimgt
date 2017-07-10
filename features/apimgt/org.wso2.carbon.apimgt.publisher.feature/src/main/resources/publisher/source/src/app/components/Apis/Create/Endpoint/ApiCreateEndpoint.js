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
import {Redirect} from 'react-router-dom'
import API from '../../../../data/api.js'
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';


import { Form, Icon, Input, Button, message, Radio, Collapse, Row, Col} from 'antd'
const Panel = Collapse.Panel;
const FormItem = Form.Item;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

class EndpointForm extends React.Component {
    constructor(props) {
        super(props);
    }
    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
        message.success("Api Created Successfully. Now you can add resources, define endpoints etc..",opts);
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
                let endpoint = {
                    inline: {
                        endpointConfig: JSON.stringify({serviceUrl: values.apiEndpoint}),
                        endpointSecurity: {enabled: false},
                        type: "http"
                    }
                };
                let api_data = {
                    name: values.apiName,
                    context: values.apiContext,
                    version: values.apiVersion
                };
                if (values.apiEndpoint) {
                    api_data['endpoint'] =  [endpoint];
                }
                let new_api = new API('');
                let promised_create = new_api.create(api_data);
                promised_create
                    .then(this.createAPICallback)
                    .catch(
                        function (error_response) {
                            let error_data = JSON.parse(error_response.data);
                            let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";

                            message.error(messageTxt);
                        });

                console.log('Send this in a POST request:', api_data);

            } else {
                message.error("Error creating API");
            }
        });
    };



    render(){
        const { getFieldDecorator } = this.props.form;
        return(
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem  label="Name"
                           labelCol={{ span: 4 }}
                           wrapperCol={{ span: 8 }}>
                    {getFieldDecorator('apiName', {
                        rules: [{ required: false, message: 'Please input Api Name' }],
                    })(
                        <Input name="apiName" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Api Name" />
                    )}
                </FormItem>
                <FormItem  label="Version"
                           labelCol={{ span: 4 }}
                           wrapperCol={{ span: 8 }}>
                    {getFieldDecorator('apiVersion', {
                        rules: [{ required: false, message: 'Please input Api Version' }],
                    })(
                        <Input name="apiVersion" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Api Version" />
                    )}
                </FormItem>
                <FormItem  label="Context"
                           labelCol={{ span: 4 }}
                           wrapperCol={{ span: 8 }}>
                    {getFieldDecorator('apiContext', {
                        rules: [{ required: false, message: 'Please input Api Context' }],
                    })(
                        <Input name="apiContext" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Api Context" />
                    )}
                </FormItem>
                <Row>
                    <Col offset={4} span={8}>
                        <Collapse bordered={false}>
                            <Panel header="More options" key="1">
                                <FormItem  label="Endpoint"
                                           labelCol={{ span: 6 }}
                                           wrapperCol={{ span: 16 }}>
                                    {getFieldDecorator('apiEndpoint', {
                                        rules: [{ required: false, message: 'Please input Api endpoint' }],
                                    })(
                                        <Input name="apiEndpoint" prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Api Endpoint" />
                                    )}
                                </FormItem>
                            </Panel>
                        </Collapse>
                    </Col>
                </Row>


                <FormItem >

                    <Button type="primary" htmlType="submit">
                        Create
                    </Button>
                    <Button type="default" htmlType="button" onClick={() => this.props.history.push("/api/create/home")}>
                        Cancel
                    </Button>
                </FormItem>
            </Form>
        );
    }
}

const EndpointFormGenerated = Form.create()(EndpointForm);

class ApiCreateEndpoint extends React.Component {
    render = () => {return  <EndpointFormGenerated history={this.props.history} /> }
}


export default ApiCreateEndpoint;
