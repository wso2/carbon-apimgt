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
import CheckboxOrRadioGroup from '../FormComponents/CheckboxOrRadioGroup';
import API from '../../../../data/api.js'
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';
import './ApiCreateSwagger.css'
import Dropzone from 'react-dropzone'


class ApiCreateSwagger extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            swaggerUrl: '',
            swaggerTypes: ["Swagger File", "Swagger URL"],
            swaggerTypeSelected: ['Swagger File'],
            files: []
        };
        this.handleFormSubmit = this.handleFormSubmit.bind(this);
        this.handleClearForm = this.handleClearForm.bind(this);
        this.handleSwaggerTypeSelection = this.handleSwaggerTypeSelection.bind(this);
    }
    handleSwaggerTypeSelection(e) {
        this.setState({swaggerTypeSelected: [e.target.value]})
    }

    handleClearForm(e) {
        e.preventDefault();
        this.setState({
            swaggerUrl: '',
            swaggerTypeSelected: ['Swagger File'],
            files: []
        });
    }

    handleSwaggerUrlChange() {
        this.state.swaggerUrl = this.target.value;
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

        toast.success("Api Created Successfully. Now you can add resources, define endpoints etc..", opts);
    };
    toggleSwaggerType = (containerType) =>  {
        return this.state.swaggerTypeSelected[0] === containerType ? '' : 'hidden';
    };
    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleFormSubmit(e) {
        e.preventDefault();

        let input_type = this.state.swaggerTypeSelected[0];
        if (input_type === "Swagger URL") {
            let url = this.state.swaggerUrl;
            let data = {};
            data.url = url;
            data.type = 'swagger-url';
            let new_api = new API('');
            new_api.create(data)
                .then(this.createAPICallback)
                .catch(
                    function (error_response) {
                        let error_data = JSON.parse(error_response.data);
                        let message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                        const opts = {
                            position: toast.POSITION.TOP_CENTER
                        };
                        toast.error(message, opts);
                        console.debug(error_response);
                    });
        } else if (input_type === "Swagger File") {
            let swagger = this.state.files[0];
            let new_api = new API('');
            new_api.create(swagger)
                .then(this.createAPICallback)
                .catch(
                    function (error_response) {
                        let error_data;
                        let message;
                        if (error_response.obj) {
                            error_data = error_response.obj;
                            message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                        } else {
                            error_data = error_response.data;
                            message = "Error: " + error_data + ".";

                        }
                        const opts = {
                            position: toast.POSITION.TOP_CENTER
                        };
                        toast.error(message, opts);
                        console.debug(error_response);
                    });
        }
        this.handleClearForm(e);
    }

    onDrop(files) {
        this.setState({
            files: files
        })
    }


    render() {
        return (
            <div>
                <div className="ch-info-wrap tmp-form-style">
                    <div className="ch-info-wrap">
                        <div className="ch-info">
                            <div className="ch-info-front ch-img-1">
                                <i className="fw fw-document fw-4x"/>
                                <span>I Have an Existing API</span>
                            </div>
                            <div className="ch-info-back">
                                <p className="unselectable">Use an existing API's endpoint or the API
                                    Swagger
                                    definition to create an API</p>
                            </div>
                        </div>
                    </div>
                    <div className="form-container swagger-form-container">

                        <form onSubmit={this.handleFormSubmit} className="bs-component text-left">
                            <CheckboxOrRadioGroup
                                title={'Select a method to provide the swagger definition.'}
                                setName={'siblings'}
                                controlFunc={this.handleSwaggerTypeSelection}
                                type={'radio'}
                                options={this.state.swaggerTypes}
                                selectedOptions={this.state.swaggerTypeSelected} />

                                <div className={this.toggleSwaggerType("Swagger File")}>
                                    <Dropzone onDrop={this.onDrop.bind(this)}>
                                        <p>Try dropping some files here, or click to select files to upload.</p>
                                    </Dropzone>

                                    <h2>Dropped files</h2>
                                    <ul>
                                        {
                                            this.state.files.map(f => <li>{f.name} - {f.size} bytes</li>)
                                        }
                                    </ul>
                                </div>
                                <div className={this.toggleSwaggerType("Swagger URL")}>
                                    <SingleInput
                                        inputType={'text'}
                                        title={'swagger url'}
                                        name={'swagger-url'}
                                        controlFunc={this.handleSwaggerUrlChange}
                                        content={this.state.swaggerURL}
                                        helpText="URL of the swagger file."
                                        placeholder={'http://petstore.swagger.io/v2/swagger.json'} />


                                </div>





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

export default ApiCreateSwagger;
