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
import SingleInput from '../../FormComponents/SingleInput';
import {Redirect} from 'react-router-dom'
import API from '../../../../../data/api.js'
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';


import {Form, Icon, Input, Button, message, Radio, Collapse, Row, Col, Select} from 'antd'
const Panel = Collapse.Panel;
const FormItem = Form.Item;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

class WSDLFillAPIInfoForm extends React.Component {
    constructor(props) {
        super(props);
        this.handleFillAPIInfoStepUpdate = this.handleFillAPIInfoStepUpdate.bind(this);
        this.getEndpoints = this.getEndpoints.bind(this);
    }

    getEndpoints() {
        let options = [];
        let endpoints = this.props.endpoints;
        endpoints.forEach(function (value) {
            options.push(<Select.Option  key={value} value={value}>{value}</Select.Option>);
        });
        return options;
    }

    handleFillAPIInfoStepUpdate (e) {   
        this.props.handleFillAPIInfoStepUpdate(e);
    };

    render() {
        const {getFieldDecorator} = this.props.form;
        const radioStyle = {
            display: 'block',
            height: '30px',
            lineHeight: '30px',
        };
        return (
            <Form className="login-form">
                <FormItem label="Name"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiName', {
                        rules: [{required: false, message: 'Please input Api Name'}],
                    })(
                        <Input name="apiName" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Name" onChange={this.handleFillAPIInfoStepUpdate}/>
                    )}
                </FormItem>
                <FormItem label="Version"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiVersion', {
                        rules: [{required: false, message: 'Please input Api Version'}],
                    })(
                        <Input name="apiVersion" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Version" onChange={this.handleFillAPIInfoStepUpdate}/>
                    )}
                </FormItem>
                <FormItem label="Context"
                          labelCol={{span: 4}}
                          wrapperCol={{span: 8}}>
                    {getFieldDecorator('apiContext', {
                        rules: [{required: false, message: 'Please input Api Context'}],
                    })(
                        <Input name="apiContext" prefix={<Icon type="user" style={{fontSize: 13}}/>}
                               placeholder="Api Context" onChange={this.handleFillAPIInfoStepUpdate}/>
                    )}
                </FormItem>
                <Row>
                    <Col offset={4} span={8}>
                        <Collapse bordered={false}>
                            <Panel header="More options" key="1">
                                <FormItem label="Endpoint"
                                          labelCol={{span: 9}}
                                          wrapperCol={{span: 13}}>
                                    <Select
                                        name="apiEndpoint"
                                        mode="combobox"
                                        size='default'
                                        onChange={this.handleFillAPIInfoStepUpdate}
                                        defaultValue={this.props.apiEndpoint}
                                        prefix={<Icon type="user" style={{fontSize: 13}}/>}>
                                        {this.getEndpoints()}
                                    </Select>
                                </FormItem>
                                {
                                    (this.props.hasHttpBinding || this.props.hasSoapBinding)
                                        &&
                                    <FormItem
                                        label="Implementation Type"
                                        labelCol={{span: 9}}
                                        wrapperCol={{span: 13}}>
                                        <RadioGroup onChange={this.handleFillAPIInfoStepUpdate}
                                                    defaultValue={this.props.implementationType}>
                                            <Radio style={radioStyle} name="implementationType" value={"soap"}
                                                   disabled={!this.props.hasSoapBinding}>Pass-through SOAP API</Radio>
                                            <Radio style={radioStyle} name="implementationType" value={"httpBinding"}
                                                   disabled={!this.props.hasHttpBinding}>With HTTP binding
                                                operations</Radio>
                                        </RadioGroup>
                                    </FormItem>
                                }
                            </Panel>
                        </Collapse>
                    </Col>
                </Row>
            </Form>
        );
    }
}

const WSDLFillAPIInfoFormGenerated = Form.create()(WSDLFillAPIInfoForm);

class WSDLFillAPIInfoStep extends React.Component {
    render = () => {
        return <WSDLFillAPIInfoFormGenerated 
            history={this.props.history} 
            endpoints={this.props.endpoints}
            handleFillAPIInfoStepUpdate={this.props.handleFillAPIInfoStepUpdate}
            hasSoapBinding={this.props.hasSoapBinding}
            hasHttpBinding={this.props.hasHttpBinding}
            implementationType={this.props.implementationType}
            apiEndpoint={this.props.apiEndpoint}
        />
    }
}


export default WSDLFillAPIInfoStep;

