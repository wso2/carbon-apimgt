import React, {Component} from 'react'
import {Input, Col, Row, Select} from 'antd'
const Option = Select.Option;

export default class GenericEndpointInputs extends Component {

    constructor(props) {
        super(props);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.handleSecurityType = this.handleSecurityType.bind(this);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.state = {url: this.props.endpoint.url};
    }

    handleEndpointType(type) {
        this.setState({'endpointType': type});
    }

    handleSecurityType(type) {
        let isSecured = false;
        if(type != 'nonsecured') {
            isSecured = true;
        }
        this.setState({'isSecured': isSecured});
    }

    populateDropdown(){
        return this.props.dropdownItems;
    }

    handleEndpointType(endpointUUID) {

        let ep = null;
        let isGlobalEPSelected = false;
        const e = {
            target: {
                name: "uuid",
                value: endpointUUID
            }
        };

        if(endpointUUID != 'custom') {
            ep = JSON.parse(this.props.epList[endpointUUID].endpointConfig).serviceUrl;
            isGlobalEPSelected = true;
        } else { // custom
            e.target.value = {};
        }
        this.props.handleInputs(e);
        this.setState({url: ep, isGlobalEPSelected: isGlobalEPSelected});
    }


    render() {
        return (
            <div>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Select Defined Endpoint</Col>
                    <Col span={10}>
                        <Select style={{width: '60%'}}
                                defaultValue={ this.props.endpoint.selectedep || "Custom..."}
                                onChange={this.handleEndpointType} >
                            {this.populateDropdown()}
                        </Select>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Endpoint URL </Col>
                    <Col span={10}>
                        <Input
                            disabled={this.props.endpoint.isGlobalEPSelected || this.state.isGlobalEPSelected}
                            value={this.props.endpoint.url || this.state.url}
                            name="url"
                            onChange={this.props.handleInputs}
                            placeholder="https://sample.wso2.org/api/endpoint"/>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Security Scheme </Col>
                    <Col span={15}>
                        <Select
                            disabled={this.props.endpoint.isGlobalEPSelected || this.state.isGlobalEPSelected}
                            name="security"
                            onChange={this.handleSecurityType}
                            defaultValue="None Secured" style={{width: '40%'}}>
                            <Option value="nonsecured">None Secured</Option>
                            <Option value="basic">Basic Auth</Option>
                            <Option value="digest">Digest Auth</Option>
                        </Select>
                    </Col>
                </Row>
                <div hidden={!this.state.isSecured}>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Username </Col>
                    <Col span={10}>
                        <Input name="username" defaultValue={this.props.endpoint.username}
                               onChange={this.props.handleInputs} placeholder="Enter Username"/>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}><Col span={6} offset={2}>Password </Col>
                    <Col span={10}>
                        <Input name="password" onChange={this.handleTextInputs} placeholder="Basic usage"/>
                    </Col>
                </Row>
                </div>
            </div>
        );
    }
}