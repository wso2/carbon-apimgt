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

/* DEPRECATED : Use the ApiCreate component instead */
const Landing = () => {
    return (
        <div>
            <div>
                <h1 className='page-header text-center'>{"Let's"} get started...</h1>
                <p className='text-center'>
                    It only takes few minutes to design, publish and manage APIs in WSO2 API Manager
                </p>
                <div className='ch-grid-container'>
                    <ul className='ch-grid'>
                        <li>
                            <div className='test_button ch-item depth-1'>
                                <div className='ch-info-wrap'>
                                    <div className='ch-info'>
                                        <div className='ch-info-front ch-img-1'>
                                            <i className='fw fw-document fw-4x' />
                                            <span>I Have an Existing API</span>
                                        </div>
                                        <div className='ch-info-back'>
                                            <p className='unselectable'>
                                                Use an existing {"API's"} endpoint or the API Swagger definition to
                                                create an API
                                            </p>
                                        </div>
                                    </div>
                                    <div
                                        id='swagger-form-container'
                                        className='col-lg-9 form-container flex-align hide'
                                    >
                                        <form className='flex-align-item-center-full'>
                                            <div className='form-group is-empty is-fileinput label-floating'>
                                                <div className='col-md-10'>
                                                    <label htmlFor='inputFile' className='col-md-2 control-label'>
                                                        Swagger File
                                                        <input type='text' readOnly className='form-control' />
                                                        <input type='file' id='inputFile' multiple />
                                                    </label>
                                                </div>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div className='test_button ch-item depth-1 ripple-effect'>
                                <div className='ch-info-wrap'>
                                    <div className='ch-info'>
                                        <div className='ch-info-front ch-img-2'>
                                            <i className='fw fw-endpoint fw-4x' />
                                            <span>I Have a SOAP Endpoint</span>
                                        </div>
                                        <div className='ch-info-back'>
                                            <p className='unselectable'>
                                                Use an existing SOAP endpoint to create a managed API. Import the WSDL
                                                of the SOAP service
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div className='test_button ch-item depth-1 ripple-effect'>
                                <div className='ch-info-wrap'>
                                    <div className='ch-info'>
                                        <div className='ch-info-front ch-img-3'>
                                            <i className='fw fw-rest-api fw-4x' />
                                            <span>Design a New REST API</span>
                                        </div>
                                        <div className='ch-info-back'>
                                            <p className='unselectable'>Design and prototype a new REST API</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div className='test_button ch-item depth-1 ripple-effect'>
                                <div className='ch-info-wrap'>
                                    <div className='ch-info'>
                                        <div className='ch-info-front ch-img-4'>
                                            <i className='fw fw-web-clip fw-4x' />
                                            <span>Design New Websocket API</span>
                                        </div>
                                        <div className='ch-info-back'>
                                            <p className='unselectable'>Design and prototype a new Websocket API</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div className='main-actions text-right'>
                    <button
                        id='btn-close-step1'
                        type='button'
                        className='btn btn-circle btn-raised ripple-effect btn-default btn-xs'
                    >
                        <i className='fw fw-cancel fw-lg' />
                    </button>
                    <button
                        id='btn-next-step'
                        type='button'
                        className='btn btn-circle btn-raised ripple-effect btn-success btn-lg'
                    >
                        <i className='fw fw-right-arrow fw-lg' />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Landing;
