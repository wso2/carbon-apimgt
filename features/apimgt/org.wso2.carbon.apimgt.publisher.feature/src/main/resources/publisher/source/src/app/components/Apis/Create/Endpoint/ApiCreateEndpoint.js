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


class ApiCreateEndpoint extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apiName: '',
            apiContext: '',
            apiVersion: ''
        };
        this.handleFormSubmit = this.handleFormSubmit.bind(this);
        this.handleClearForm = this.handleClearForm.bind(this);
        this.handleApiNameChange = this.handleApiNameChange.bind(this);
        this.handleApiContextChange = this.handleApiContextChange.bind(this);
        this.handleApiVersionChange = this.handleApiVersionChange.bind(this);
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

    handleClearForm(e) {
        e.preventDefault();
        this.setState({
            apiName: '',
            apiContext: '',
            apiVersion: ''

        });
    }
    componentDidMount(){
    }
    createAPICallback = (response) => {
        let that = this;
        const opts = {
            position: toast.POSITION.TOP_CENTER,
            onClose: () => {
                let uuid = JSON.parse(response.data).id;
                let redirect_url = "/apis/" + uuid + "/overview";
                that.props.history.push(redirect_url);
            }
        };

        toast.success("Api Created Successfully. Now you can add resources, define endpoints etc..",opts);
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
            .then(this.createAPICallback)
            .catch(
                function (error_response) {
                    let error_data = JSON.parse(error_response.data);
                    let message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";

                    alert(message);
                });

        console.log('Send this in a POST request:', api_data);
        this.handleClearForm(e);
    }
    render() {
        return (
            <div>
                <div className="ch-info-wrap tmp-form-style">
                    <div className="ch-info flex-stay-200">
                        <div className="ch-info-front ch-img-3">
                            <i className="fw fw-rest-api fw-4x"></i>
                            <span>Design New REST API</span>
                        </div>
                        <div className="ch-info-back">
                            <p className="unselectable">Design and prototype a new REST API</p>
                        </div>
                    </div>
                    <div id="rest-form-container" className="form-container flex-1">

                        <form onSubmit={this.handleFormSubmit} className="bs-component text-left">
                            <SingleInput
                                inputType={'text'}
                                title={'Api name'}
                                name={'apiName'}
                                controlFunc={this.handleApiNameChange}
                                content={this.state.apiName}
                                helpText="Display name to be shown in the API Store."
                                placeholder={'Type api name here'} />
                            <SingleInput
                                inputType={'text'}
                                title={'Api Context'}
                                name={'apiContext'}
                                controlFunc={this.handleApiContextChange}
                                content={this.state.apiContext}
                                helpText="URI context path of the API (case sensitive)."
                                placeholder={'Type api context here'} />

                            <SingleInput
                                inputType={'text'}
                                title={'Api Version'}
                                name={'apiVersion'}
                                controlFunc={this.handleApiVersionChange}
                                content={this.state.apiVersion}
                                helpText=""
                                placeholder={'Type api version here'} />



                            <div className="form-group">
                                <div>
                                    <button id="btn-close-step1" type="button" className="btn btn-default" onClick={() => this.props.history.push("/api/create")}>Cancel<div className="ripple-container"></div></button>
                                    <button type="submit" className="btn btn-info btn-raised" >Continue</button>
                                </div>
                            </div>
                        </form>
                    </div>
                <span className="ink animate"></span>
                </div>



            </div>
        );
    }
}

export default ApiCreateEndpoint;
