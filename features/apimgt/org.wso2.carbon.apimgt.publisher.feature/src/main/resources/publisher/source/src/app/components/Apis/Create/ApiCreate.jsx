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
import { getAsyncComponent } from 'async-react-component';

import { Route, Switch, Link } from 'react-router-dom';
import './ApiCreate.css';
import ApiCreateEndpoint from './Endpoint/ApiCreateEndpoint';

const ApiCreateWSDL = () => import(/* webpackChunkName: "ApiCreateWSDL" */ './WSDL/ApiCreateWSDL');
const ApiCreateSwagger = () => import(/* webpackChunkName: "ApiCreateSwagger" */ './Swagger/ApiCreateSwagger');

const ApiCreate = () => {
    return (
        <div>
            <Switch>
                <Route
                    path='/api/create/home'
                    render={() => (
                        <div className='ch-grid-container'>
                            <ul className='ch-grid'>
                                <li>
                                    <Link to='/api/create/swagger'>
                                        <div className='test_button ch-item depth-1'>
                                            <div className='ch-info-wrap'>
                                                <div className='ch-info'>
                                                    <div className='ch-info-front ch-img-1'>
                                                        <i className='fw fw-document fw-4x' />
                                                        <span>I Have an Existing API</span>
                                                    </div>
                                                    <div className='ch-info-back'>
                                                        <p className='unselectable'>
                                                            Use an existing API endpoint or the API Swagger definition
                                                            to create an API
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                </li>
                                <li>
                                    <Link to='/api/create/wsdl'>
                                        <div className='test_button ch-item depth-1 ripple-effect'>
                                            <div className='ch-info-wrap'>
                                                <div className='ch-info'>
                                                    <div className='ch-info-front ch-img-2'>
                                                        <i className='fw fw-endpoint fw-4x' />
                                                        <span>I Have a SOAP Endpoint</span>
                                                    </div>
                                                    <div className='ch-info-back'>
                                                        <p className='unselectable'>
                                                            Use an existing SOAP endpoint to create a managed API.
                                                            Import the WSDL of the SOAP service
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                </li>
                                <li>
                                    <Link to='/api/create/rest'>
                                        <div className='test_button ch-item depth-1 ripple-effect'>
                                            <div className='ch-info-wrap'>
                                                <div className='ch-info'>
                                                    <div className='ch-info-front ch-img-3'>
                                                        <i className='fw fw-rest-api fw-4x' />
                                                        <span>Design a New REST API</span>
                                                    </div>
                                                    <div className='ch-info-back'>
                                                        <p className='unselectable'>
                                                            Design and prototype a new REST API
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                </li>
                                <li>
                                    <Link to='/api/create/rest'>
                                        <div className='test_button ch-item depth-1 ripple-effect'>
                                            <div className='ch-info-wrap'>
                                                <div className='ch-info'>
                                                    <div className='ch-info-front ch-img-4'>
                                                        <i className='fw fw-web-clip fw-4x' />
                                                        <span>Design New Websocket API</span>
                                                    </div>
                                                    <div className='ch-info-back'>
                                                        <p className='unselectable'>
                                                            Design and prototype a new Websocket API
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                </li>
                            </ul>
                        </div>
                    )}
                />
                <Route path='/api/create/rest' component={ApiCreateEndpoint} />
                <Route path='/api/create/swagger' component={getAsyncComponent(ApiCreateSwagger)} />
                <Route path='/api/create/wsdl' component={getAsyncComponent(ApiCreateWSDL)} />
            </Switch>
        </div>
    );
};

export default ApiCreate;
