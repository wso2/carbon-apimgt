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
import Select from '../FormComponents/Select';

import API from '../../../../data/api.js'
import  './ApiCreateEndpoint.css'

class ApiCreateEndpoint extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apiName: '',
            apiContext: '',
            apiVersion: '',
            epProdName: '',
            epProdType: ["http","https"],
            epProdTypeSelected: 'http',
            epProdMaxTps: 'unlimited',
            epProdServiceUrl: ''
        };

        this.handleFormSubmit = this.handleFormSubmit.bind(this);
        this.handleClearForm = this.handleClearForm.bind(this);
        this.handleApiNameChange = this.handleApiNameChange.bind(this);
        this.handleApiContextChange = this.handleApiContextChange.bind(this);
        this.handleApiVersionChange = this.handleApiVersionChange.bind(this);
        this.handleEPProdNameChange = this.handleEPProdNameChange.bind(this);
        this.handleEpProdTypeSelect = this.handleEpProdTypeSelect.bind(this);
        this.handleEPProdServiceUrlChange = this.handleEPProdServiceUrlChange.bind(this);
    }

    componentDidMount() {

    }
    handleApiNameChange(e) {
        this.setState({ apiName: e.target.value }, () => console.log('name:', this.state.apiName));
    }
    handleApiContextChange(e) {
        this.setState({ apiContext: e.target.value }, () => console.log('name:', this.state.apiContext));
    }
    handleApiVersionChange(e) {
        this.setState({ apiVersion: e.target.value }, () => console.log('name:', this.state.apiVersion));
    }
    handleEPProdNameChange(e) {
        this.setState({ epProdName: e.target.value }, () => console.log('name:', this.state.epProdName));
    }
    handleEpProdTypeSelect(e) {
        this.setState({ epProdType: e.target.value }, () => console.log('name:', this.state.epProdType));
    }
    handleEPProdServiceUrlChange(e) {
        this.setState({ epProdServiceUrl: e.target.value }, () => console.log('name:', this.state.epProdServiceUrl));
    }
    handleClearForm(e) {
        e.preventDefault();
        this.setState({
            apiName: '',
            apiContext: '',
            apiVersion: '',
            epProdName: '',
            epProdType: [],
            epProdTypeSelected: '',
            epProdMaxTps: '',
            epProdServiceUrl: '',

        });
    }
    static createAPICallback(response){
        console.info(response);
    }
    handleFormSubmit(e) {
        e.preventDefault();

        let api_data = {
            name: this.state.apiName,
            context: this.state.apiContext,
            version: this.state.apiVersion
            //endpoint: this.constructEndpointsForApi()
        };
        let new_api = new API('');
        let promised_create = new_api.create(api_data);
        promised_create
            .then(ApiCreateEndpoint.createAPICallback)
            .catch(
                function (error_response) {
                    let error_data = JSON.parse(error_response.data);
                    let message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";

                    alert(message);
                });

        console.log('Send this in a POST request:', api_data);
        this.handleClearForm(e);
    }
    constructEndpointsForApi(){
        let endpointArray = [];

        let endpoint = {};
        let security = {enabled:false};
        let endpointConfig = {};

        endpoint.name = this.state.epProdName;
        endpoint.type = "http";//this.state.epProdType;
        endpoint.maxTps = this.state.epProdMaxTps;
        endpointConfig.serviceUrl = this.state.epProdServiceUrl;
        endpoint.endpointConfig = JSON.stringify(endpointConfig);
        endpoint.endpointSecurity=security;

        endpointArray.push({
            'inline': endpoint,
            'type': 'production'
        });

        endpointArray.push({inline: false, type: "sandbox"});
        return endpointArray;
    }


    render() {
        return (
            <div className="body-wrapper" id="bodyWrapper">
                <h2>Add New API</h2>
                <form onSubmit={this.handleFormSubmit}>
                    <SingleInput
                        inputType={'text'}
                        title={'Api name'}
                        name={'apiName'}
                        controlFunc={this.handleApiNameChange}
                        content={this.state.apiName}
                        placeholder={'Type api name here'} />
                    <SingleInput
                        inputType={'text'}
                        title={'Api Context'}
                        name={'apiContext'}
                        controlFunc={this.handleApiContextChange}
                        content={this.state.apiContext}
                        placeholder={'Type api context here'} />

                    <SingleInput
                        inputType={'text'}
                        title={'Api Version'}
                        name={'apiVersion'}
                        controlFunc={this.handleApiVersionChange}
                        content={this.state.apiVersion}
                        placeholder={'Type api version here'} />

                    <h4>Production Endpoint</h4>

                    <SingleInput
                        inputType={'text'}
                        title={'Production Endpoint Name'}
                        name={'epProdName'}
                        controlFunc={this.handleEPProdNameChange}
                        content={this.state.epProdName}
                        placeholder={'Production endpoint'} />

                    <Select
                        name={'epProdType'}
                        placeholder={'Choose production endpoint type.'}
                        controlFunc={this.handleEpProdTypeSelect}
                        options={this.state.epProdType}
                        selectedOption={this.state.epProdTypeSelected} />

                    <SingleInput
                        inputType={'text'}
                        title={'Service Url'}
                        name={'epProdServiceUrl'}
                        controlFunc={this.handleEPProdServiceUrlChange}
                        content={this.state.epProdServiceUrl}
                        placeholder={'http://prodendpoint.com'} />

                    <input
                        type="submit"
                        className="btn btn-primary float-right"
                        value="Submit"/>
                    <button
                        className="btn btn-link float-left"
                        onClick={this.handleClearForm}>Clear form</button>

                </form>

            </div>

        );
    }
}

export default ApiCreateEndpoint;
