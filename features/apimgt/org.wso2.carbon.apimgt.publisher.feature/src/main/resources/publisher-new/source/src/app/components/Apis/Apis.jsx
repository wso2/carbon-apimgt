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
import { Route, Switch } from 'react-router-dom';

import ApiCreate from './Create/ApiCreate';
import APIProductListing from './Listing/APIProductListing';
import APIListing from './Listing/APIListing';
import Details from './Details/index';

const Apis = () => {
    return (
        <Switch>
            <Route exact path='/apis' component={APIListing} />
            <Route exact path='/api-products' component={APIProductListing} />
            <Route path='/apis/create' component={ApiCreate} />
            <Route path='/api-products/create' component={ApiCreate} />
            <Route path='/apis/:apiUUID/' render={props => <Details {...props} isAPIProduct={false} />} />
            <Route path='/api-products/:apiProdUUID/' render={props => <Details {...props} isAPIProduct />} />
        </Switch>
    );
};

export default Apis;
