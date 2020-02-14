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

import React, { Suspense, lazy } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import Progress from 'AppComponents/Shared/Progress';
import AuthManager from 'AppData/AuthManager';
import APICreateRoutes from './Create/APICreateRoutes';

import Listing from './Listing/Listing';


const Details = lazy(() => import('./Details/index' /* webpackChunkName: "DeferredDetails" */));
const DeferredDetails = (props) => (
    <Suspense fallback={<Progress message='Loading Details component ...' />}>
        <Details {...props} />
    </Suspense>
);

/**
 * Have used key={Date.now()} for `Route` element in `/apis` and `/api-products`
 */
const Apis = () => {
    return (
        <Switch>
            <Route
                exact
                path='/apis'
                key={Date.now()}
                render={(props) => <Listing {...props} isAPIProduct={false} />}
            />
            <Route
                exact
                path='/api-products'
                key={Date.now()}
                render={(props) => {
                    if (AuthManager.isNotPublisher()) {
                        return <Redirect to='/apis' />;
                    } else {
                        return <Listing {...props} isAPIProduct />;
                    }
                }}
            />
            <Route path='/apis/search' render={(props) => <Listing {...props} isAPIProduct={false} />} />
            <Route path='/apis/create' component={APICreateRoutes} />
            <Route
                path='/api-products/create'
                render={() => {
                    if (AuthManager.isNotPublisher()) {
                        return <Redirect to='/apis' />;
                    } else {
                        return <APICreateRoutes />;
                    }
                }}
            />
            <Route path='/apis/:apiUUID/' render={(props) => <DeferredDetails {...props} isAPIProduct={false} />} />
            <Route
                path='/api-products/:apiProdUUID/'
                render={(props) => {
                    if (AuthManager.isNotPublisher()) {
                        return <Redirect to='/apis' />;
                    } else {
                        return <DeferredDetails {...props} isAPIProduct />;
                    }
                }}
            />
        </Switch>
    );
};

export default Apis;
