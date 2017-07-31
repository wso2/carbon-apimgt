import React, {Component} from 'react'
import {Input, Col, Row, Select} from 'antd'
const Option = Select.Option;

export default class GenericEndpointInputs extends Component {

    constructor(props) {
        super(props);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.handleSecurityType = this.handleSecurityType.bind(this);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.endpoint = props.endpoint;
        this.epList = props.epList;
        this.state = {url: this.endpoint.url};
        this.epMap = {};
    }

    handleEndpointType(type) {
        this.setState({'endpointType': type});
    }

    handleSecurityType(type) {
        this.setState({'securityType': type});
    }

    populateDropdown(){
        const dropdownItems = [<Option key="custom">Custom...</Option>];

        for (var i in this.epList) {
            let endpointId = this.epList[i].id;
            this.epMap[endpointId] = this.epList[i];
            dropdownItems.push(<Option key={endpointId}>{this.epList[i].name}</Option>);
        }
        return dropdownItems;
    }

    handleEndpointType(endpointUUID) {

        let ep = null;
        if(endpointUUID != 'custom') {
            ep = JSON.parse(this.epMap[endpointUUID].endpointConfig).serviceUrl;
        } else {
            ep = "previous - value";
        }
        console.log("Global EP map : " + ep);
        this.setState({url: ep});
    }

    render() {
        return (
            <div>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Select Defined Endpoint</Col>
                    <Col span={10}>
                        <Select style={{width: '60%'}}
                                defaultValue="Custom..."
                                onChange={this.handleEndpointType} >
                            {this.populateDropdown()}
                        </Select>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Endpoint URL </Col>
                    <Col span={10}>
                        <Input
                            value={this.state.url}
                            name="url"
                            onChange={this.props.handleInputs}
                            placeholder="https://sample.wso2.org/api/endpoint"/>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Security Scheme </Col>
                    <Col span={10}>
                        <Select
                            name="security"
                            onChange={this.props.handleInputs}
                            defaultValue="Basic Auth" style={{width: '40%'}}>
                            <Option value="basic">Basic Auth</Option>
                            <Option value="oauth">OAuth</Option>
                        </Select>
                    </Col>
                </Row>
                <Row style={{marginTop: '10px'}}>
                    <Col span={6} offset={2}>Username </Col>
                    <Col span={10}>
                        <Input name="username" defaultValue={this.endpoint.username}
                               onChange={this.props.handleInputs} placeholder="Enter Username"/>
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