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
'use strict';

import React, {Component} from 'react'
import {Row, Col, Input, Radio, InputNumber, Switch, Button, message} from 'antd'
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

import API from '../../../data/api'

export default class EndpointCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
            endpointType: 'http',
            secured: false,
            creating: false
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.handleSwitch = this.handleSwitch.bind(this);
        this.handleMaxTPS = this.handleMaxTPS.bind(this);
    }

    handleSwitch(secured) {
        this.setState({secured: secured})
    }

    handleInputs(e) {
        this.setState({[e.target.name]: e.target.value});
    }

    handleMaxTPS(maxTPS) {
        this.setState({maxTPS: maxTPS});
    }

    handleSubmit(e) {
        this.setState({loading: true});
        let endpointSecurity = {enabled: false};
        if (this.state.secured) {
            endpointSecurity = {
                enabled: true,
                username: this.state.username,
                password: this.state.password,
                type: this.state.securityType
            }
        }
        let endpointDefinition = {
            endpointConfig: JSON.stringify({serviceUrl: this.state.endpointType + '://' + this.state.serviceUrl}),
            endpointSecurity: endpointSecurity,
            type: this.state.endpointType,
            name: this.state.name,
            maxTps: this.state.maxTPS
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint.then(
            response => {
                const {name, id} = response.obj;
                message.success("New endpoint " + name + " created successfully");
                let redirect_url = "/endpoints/" + id + "/";
                this.props.history.push(redirect_url);
            }
        ).catch(
            error => {
                console.error(error);
                message.error("Error occurred while creating the endpoint!");
                this.setState({loading: false});
            }
        )
    }

    render() {
        return (
            <div>
                <h4>
                    Add new Global Endpoint
                </h4>
                <form onSubmit={this.handleSubmit}>
                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                        <Col span={4}>Name</Col>
                        <Col span={8}>
                            <Input name="name" onChange={this.handleInputs} placeholder="Endpoint Name"/>
                        </Col>
                    </Row>

                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                        <Col span={4}>Type</Col>
                        <Col span={8}>
                            <RadioGroup value={this.state.endpointType}
                                        onChange={e => {
                                            e.target["name"] = "endpointType";
                                            this.handleInputs(e)
                                        }}>
                                <RadioButton value={"http"}>HTTP</RadioButton>
                                <RadioButton value={"https"}>HTTPS</RadioButton>
                            </RadioGroup>
                        </Col>
                    </Row>
                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                        <Col span={4}>Max TPS</Col>
                        <Col span={8}>
                            <InputNumber onChange={this.handleMaxTPS} defaultValue={10}/>
                        </Col>
                    </Row>
                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                        <Col span={4}>Service URL</Col>
                        <Col span={8}>
                            <Input name="serviceUrl" onChange={this.handleInputs} style={{width: '100%'}}
                                   addonBefore={this.state.endpointType + "://"}/>
                        </Col>
                    </Row>
                    <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                        <Col span={4}>Secured</Col>
                        <Col span={8}>
                            <Switch onChange={this.handleSwitch} defaultChecked={this.state.secured}/>
                        </Col>
                    </Row>
                    {this.state.secured && (
                        <Row style={{marginBottom: "10px"}}>
                            <Col offset={10}>
                                <Row style={{marginBottom: "10px"}}>
                                    <Col span={4}>Type</Col>
                                    <Col span={8}>
                                        <RadioGroup value={this.state.securityType}
                                                    onChange={e => {
                                                        e.target["name"] = "securityType";
                                                        this.handleInputs(e)
                                                    }}>
                                            <RadioButton value={"basic"}>Basic</RadioButton>
                                            <RadioButton value={"digest"}>Digest</RadioButton>
                                        </RadioGroup>
                                    </Col>
                                </Row>
                                <Row style={{marginBottom: "10px"}}>
                                    <Col span={4}>Username</Col>
                                    <Col span={8}>
                                        <Input name="username" onChange={this.handleInputs} placeholder="Username"/>
                                    </Col>
                                </Row>
                                <Row style={{marginBottom: "10px"}}>
                                    <Col span={4}>Password</Col>
                                    <Col span={8}>
                                        <Input name="password" onChange={this.handleInputs} placeholder="Password"/>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    )}
                    <Button loading={this.state.creating} type="primary" onClick={this.handleSubmit}>Create</Button>
                </form>
            </div>
        );
    }
}
