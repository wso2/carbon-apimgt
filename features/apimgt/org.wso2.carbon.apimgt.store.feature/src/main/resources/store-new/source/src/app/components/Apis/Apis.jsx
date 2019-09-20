/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Route, Switch, Redirect } from 'react-router-dom';
import CONSTS from 'AppData/Constants';
import { withStyles } from '@material-ui/core';
import CommonListing from './Listing/CommonListing';
import TagCloudListing from './Listing/TagCloudListing';
import Details from './Details/index';
import { PageNotFound } from '../Base/Errors';

/**
 * Default API Store overview page
 *
 * @returns {React.Component}
 */
function Apis() {
    return (
        <Switch>
            <Route
                exact
                path='/apiGroups'
                render={props => (
                    <TagCloudListing {...props} apiType={CONSTS.API_TYPE} />)}
            />
            <Route
                exact
                path='/apis'
                render={props => (
                    <CommonListing {...props} apiType={CONSTS.API_TYPE} />)}
            />
            <Route
                exact
                path='/api-products'
                render={props => (
                    <CommonListing {...props} apiType={CONSTS.API_PRODUCT_TYPE} />)}
            />
            <Route
                path='/apis/search'
                render={props => (
                    <CommonListing {...props} apiType={CONSTS.API_TYPE} />)}
            />
            <Route
                path='/apis/:api_uuid/'
                render={props => (
                    <Details {...props} apiType={CONSTS.API_TYPE} />)}
            />
            <Route
                path='/api-products/:api_uuid/'
                render={props => (
                    <Details {...props} apiType={CONSTS.API_PRODUCT_TYPE} />)}
            />
            <Route component={PageNotFound} />
        </Switch>
    );
}

export default withStyles({}, { withTheme: true })(Apis);
