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
import {Alert, Form, Icon, Input, Button, message, Upload, Radio , Steps, Spin} from 'antd';
import WSDLValidationStep from './Steps/WSDLValidationStep'
import WSDLFillAPIInfoStep from './Steps/WSDLFillAPIInfoStep'
import API from '../../../../data/api.js'

const Step = Steps.Step;

class ApiCreateWSDLSteppedForm extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            current: 0,
            uploadMethod: "file",
            file: null,
            wsdlUrl: null,
            isValidating: false,
            validationFailed: false,
            endpoints: [],
            apiName: null,
            apiVersion: null,
            apiContext: null,
            apiEndpoint: null,
            implementationType: null,
            validationFailedMessage: null,
            bindingInfo: {
                hasSoapBinding: false,
                hasHttpBinding: false
            }
        };
        
        this.handleFillAPIInfoStepUpdate = this.handleFillAPIInfoStepUpdate.bind(this);
        this.handleCreateWSDLAPI = this.handleCreateWSDLAPI.bind(this);
    }

    next() {
        if (this.state.current === 0) { // validation form
            this.setState({isValidating: true, validationFailed:false});
            if (this.state.uploadMethod === "url") {
                this.validateWSDLUrlInCurrentState();
            } else { // WSDL file
                this.validateWSDLFileInCurrentState();
            }
        }
    }
    prev() {
        const current = this.state.current - 1;
        this.setState({ current });
    }

    setWSDLUrl(url) {
        this.setState({wsdlUrl: url});
    }

    setUploadMethod(method) {
        this.setState({uploadMethod: method});
    }

    setWSDLFile(wsdlFile) {
        this.setState({file: wsdlFile});
    }

    handleFillAPIInfoStepUpdate(e) {
        if (e.hasOwnProperty('target')) {
            this.setState({[e.target.name]: e.target.value});
        } else {
            this.setState({"apiEndpoint": e});
        }
    }

    validateWSDLUrlInCurrentState() {
        let wsdlUrl = this.state.wsdlUrl;
        let new_api = new API('');
        let promised_validationResponse = new_api.validateWSDLUrl(wsdlUrl);
        promised_validationResponse.then(this.validateWSDLUrlCallback).catch(this.validateWSDLUrlCallbackOnError);
    }

    validateWSDLFileInCurrentState() {
        let file = this.state.file;
        let new_api = new API('');
        let promised_validationResponse = new_api.validateWSDLFile(file.originFileObj);
        promised_validationResponse.then(this.validateWSDLUrlCallback).catch(this.validateWSDLUrlCallbackOnError);
    }

    validateWSDLUrlCallback = (response) => {
        let data = JSON.parse(response.data);
        if (data.isValid) {
            const current = this.state.current + 1;
            let uniqueEndpointSet = new Set();
            let allEndpoints = data.wsdlInfo.endpoints;
            let apiEndpoint = null;
            let hasHttpBinding = false;
            let hasSoapBinding = false;
            let defaultImplementationType = "soap";
            
            for (let i = 0; i < allEndpoints.length; i++) {
                uniqueEndpointSet.add(allEndpoints[i].location);
            }
            let uniqueEndpoints = Array.from(uniqueEndpointSet);
            
            if (uniqueEndpoints.length > 0) {
                apiEndpoint = uniqueEndpoints[0];
            }

            if (data.wsdlInfo.bindingInfo) {
                hasHttpBinding = data.wsdlInfo.bindingInfo.hasHttpBinding;
                defaultImplementationType = "httpBinding"
            }

            if (data.wsdlInfo.bindingInfo) {
                hasSoapBinding = data.wsdlInfo.bindingInfo.hasSoapBinding;
                defaultImplementationType = "soap"
            }

            this.setState({
                current: current,
                isValidating: false,
                endpoints: uniqueEndpoints,
                apiEndpoint: apiEndpoint,
                hasSoapBinding: hasSoapBinding,
                hasHttpBinding: hasHttpBinding,
                implementationType: defaultImplementationType
            });
        } else {
            this.setState({
                isValidating: false,
                validationFailed: true,
                validationFailedMessage: "Invalid WSDL Content"
            });
        }
    };

    validateWSDLUrlCallbackOnError = (response) => {
        this.setState({
            isValidating: false,
            validationFailed: true,
            validationFailedMessage: response.response.body.description
        });
    };

    handleCreateWSDLAPI() {
        let new_api = new API('');
        let apiName = this.state.apiName;
        let apiVersion = this.state.apiVersion;
        let apiContext = this.state.apiContext;
        let apiEndpoint = this.state.apiEndpoint;
        let wsdlUrl = this.state.wsdlUrl;
        let file = this.state.file;
        let uploadMethod = this.state.uploadMethod;
        let implementationType = this.state.implementationType;
        let production = {
            type: "production",
            inline: {
                name: apiName + "_" + apiVersion + "_PRODUCTION",
                endpointConfig: JSON.stringify({serviceUrl: apiEndpoint}),
                endpointSecurity: {enabled: false},
                type: "http"
            }
        };
        
        let sandbox = {
            type: "sandbox",
            inline: {
                name: apiName + "_" + apiVersion + "_SANDBOX",
                endpointConfig: JSON.stringify({serviceUrl: apiEndpoint}),
                endpointSecurity: {enabled: false},
                type: "http"
            }
        };
        let api_attributes = {
            name: apiName,
            version: apiVersion,
            context: apiContext,
            endpoint: [production, sandbox]
        };
        let api_data = {
            additionalProperties: JSON.stringify(api_attributes),
            implementationType: implementationType
        };

        if (uploadMethod === "url"){
            api_data.url = wsdlUrl;
        } else {
            api_data.file = file.originFileObj;
        }
        new_api.importWSDL(api_data).then(this.handleAPICreateResponse).catch(
            function (error_response) {
                let error_data = JSON.parse(error_response.data);
                let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                message.error(messageTxt);
            });
    }

    handleAPICreateResponse = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
        message.success("API Created Successfully. Now you can add/modify resources, define endpoints etc..", opts);
    };

    render() {
        const { current } = this.state;
        let steps = [{
            title: 'Provide WSDL URL/File',
            content: <WSDLValidationStep
                onUploadMethodChanged={(method) => this.setUploadMethod(method)}
                onWSDLFileChanged={(file) => this.setWSDLFile(file)}
                onWSDLUrlChanged={(url) => this.setWSDLUrl(url)}
            />,
        }, {
            title: 'Create API',
            content: <WSDLFillAPIInfoStep endpoints={this.state.endpoints}
                                          handleFillAPIInfoStepUpdate={(e) => this.handleFillAPIInfoStepUpdate(e)}
                                          hasSoapBinding={this.state.hasSoapBinding}
                                          hasHttpBinding={this.state.hasHttpBinding}
                                          implementationType={this.state.implementationType}
                                          apiEndpoint={this.state.apiEndpoint}
            />,
        }];

        return (
            <div>
                <Steps current={current}>
                    {steps.map(item => <Step key={item.title} title={item.title} />)}
                </Steps>
                <div className="steps-content">{steps[this.state.current].content}</div>
                <div className="steps-action">
                    {
                        this.state.current < steps.length - 1
                        &&
                        <Button type="primary" onClick={() => this.next()}>Next</Button>
                    }
                    {
                        this.state.current === steps.length - 1
                        &&
                        <Button type="primary" onClick={this.handleCreateWSDLAPI}>Done</Button>
                    }
                    {
                        this.state.current > 0
                        &&
                        <Button style={{ marginLeft: 8 }} onClick={() => this.prev()}>
                            Previous
                        </Button>
                    }
                    {
                        this.state.isValidating &&
                        <Spin tip=" Validating..."/>
                    }
                    {
                        this.state.validationFailed &&
                        <Alert
                            message="Validation Failed"
                            type="error"
                            description={this.state.validationFailedMessage}
                            showIcon
                        />
                    }
                </div>
            </div>
        );
    }
}


const ApiCreateWSDLSteppedFormGenerated = Form.create()(ApiCreateWSDLSteppedForm);

class ApiCreateWSDL extends React.Component {
    constructor(props) {
        super(props);

    }
    render = () => {return  <ApiCreateWSDLSteppedFormGenerated history={this.props.history} /> }
}

export default ApiCreateWSDL;
