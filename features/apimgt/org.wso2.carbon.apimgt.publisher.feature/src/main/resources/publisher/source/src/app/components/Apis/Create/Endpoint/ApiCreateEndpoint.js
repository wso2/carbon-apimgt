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
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';
import Policies from '../../Details/LifeCycle/Policies.js'

import {Form, Icon, Input, Button, message, Radio, Collapse, Card, Row, Col} from 'antd'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../../data/ScopeValidation';

const Panel = Collapse.Panel;
const FormItem = Form.Item;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

class EndpointForm extends React.Component {
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            api: this.api
        };
        this.updateData = this.updateData.bind(this);
    }

    componentWillMount() {
        this.updateData();
    }

    updateData() {
        let promised_tier = this.api.policies('api');
        promised_tier.then(response => {
            let tiers = response.obj;
            this.setState({policies: tiers});
        })
    }

    handlePolicies(policies) {
        this.state.selectedPolicies = policies;
    }

    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
        message.success("Api Created Successfully. Now you can add resources, define endpoints etc..");
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
                let production = {
                    type: "production",
                    inline: {
                        name: values.apiName + values.apiVersion.replace(/\./g, "_"), // TODO: It's better to add this name property from the REST api itself, making sure no name conflicts with other inline endpoint definitions ~tmkb
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
                    let sandbox = JSON.parse(JSON.stringify(production)); // deep coping the object
                    sandbox.type = "sandbox";
                    sandbox.inline.name += "_sandbox";
                    api_data['endpoint'] = [production, sandbox];
                }
                let new_api = new API();
                let promised_create = new_api.create(api_data);
                promised_create
                    .then(response => {
                        let uuid = JSON.parse(response.data).id;
                        let promisedApi = this.api.get(uuid);
                        promisedApi.then(response => {
                            let api_data = JSON.parse(response.data);
                            api_data.policies = this.state.selectedPolicies;
                            let promised_update = this.api.update(api_data);
                            promised_update.then(response => {
                                this.createAPICallback(response);
                            })
                        });
                    })
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


    render() {
        const props = {
            policies: this.state.policies,
            handlePolicies: this.handlePolicies.bind(this),
            selectedPolicies: this.state.selectedPolicies
        }
        const {getFieldDecorator} = this.props.form;
        return (
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem label="Name"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiName', {
                        rules: [{required: false, message: 'Please input Api Name'}],
                    })(
                        <Input name="apiName" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Name"/>
                    )}
                </FormItem>
                <FormItem label="Version"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiVersion', {
                        rules: [{required: false, message: 'Please input Api Version'}],
                    })(
                        <Input name="apiVersion" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Version"/>
                    )}
                </FormItem>
                <FormItem label="Context"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiContext', {
                        rules: [{required: false, message: 'Please input Api Context'}],
                    })(
                        <Input name="apiContext" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Context"/>
                    )}
                </FormItem>
                <Row>
                    <Col offset={4} span={8}>
                        <Collapse bordered={false}>
                            <Panel header="More options" key="1">
                                <FormItem label="Endpoint"
                                          labelCol={{span: 6}}
                                          wrapperCol={{span: 16}}>
                                    {getFieldDecorator('apiEndpoint', {
                                        rules: [{required: false, message: 'Please input Api endpoint'}],
                                    })(
                                        <Input name="apiEndpoint" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                                               placeholder="Api Endpoint"/>
                                    )}
                                </FormItem>
                            </Panel>
                        </Collapse>
                    </Col>
                </Row>
                <Row>
                    <Col span={12}>
                        <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>
                            <Card title="Business Plans" bordered={false} style={{margin: '5px'}}>
                                {this.state.policies ? <Policies {...props}/> : ''}
                            </Card>
                        </ScopeValidation>
                    </Col>
                </Row>

                <FormItem >
                    <ScopeValidation resourcePath={resourcePath.APIS} resourceMethod={resourceMethod.POST}>
                        <Button id="action-create" type="primary" htmlType="submit">
                            Create
                        </Button>
                    </ScopeValidation>
                    <Button type="default" htmlType="button"
                            onClick={() => this.props.history.push("/api/create/home")}>
                        Cancel
                    </Button>
                </FormItem>
            </Form>
        );
    }
}

const EndpointFormGenerated = Form.create()(EndpointForm);

class ApiCreateEndpoint extends React.Component {
    render = () => {
        return <EndpointFormGenerated history={this.props.history}/>
    }
}


export default ApiCreateEndpoint;
