import React, {Component} from 'react'
import {Input, Col, Row, Select} from 'antd'
const Option = Select.Option;

export default class GenericEndpointInputs extends Component {
    constructor(props) {
        super(props);
        this.state = {};
        this.handleTextInputs = this.handleTextInputs.bind(this);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.handleSecurityType = this.handleSecurityType.bind(this);
    }

    handleTextInputs(e) {
        this.setState({[e.target.name]: e.target.value});
    }

    handleEndpointType(type) {
        this.setState({'endpointType': type});
    }

    handleSecurityType(type) {
        this.setState({'securityType': type});
    }

    render() {
        return (
            <div>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Select Defined Endpoint</Col>
                    <Col span={10}>
                        <Select defaultValue="Banking Endpoint" style={{width: '60%'}}
                                onChange={this.handleEndpointType}>
                            <Option value="banking">Banking Endpoint</Option>
                            <Option value="aws">AWS Endpoint</Option>
                            <Option value="openshift">Openshift Endpoint</Option>
                        </Select>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Endpoint URL </Col>
                    <Col span={10}>
                        <Input
                            name="url"
                            onChange={this.handleTextInputs}
                            placeholder="https://sample.wso2.org/api/endpoint"/>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Security Scheme </Col>
                    <Col span={10}>
                        <Select
                            name="security"
                            onChange={this.handleSecurityType}
                            defaultValue="Basic Auth" style={{width: '40%'}}>
                            <Option value="basic">Basic Auth</Option>
                            <Option value="oauth">OAuth</Option>
                        </Select>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Username </Col>
                    <Col span={10}>
                        <Input name="username" onChange={this.handleTextInputs} placeholder="Enter Username"/>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}><Col span={6} offset={2}>Password </Col>
                    <Col span={10}>
                        <Input name="password" onChange={this.handleTextInputs} placeholder="Basic usage"/>
                    </Col>
                </Row>
            </div>
        );
    }
}