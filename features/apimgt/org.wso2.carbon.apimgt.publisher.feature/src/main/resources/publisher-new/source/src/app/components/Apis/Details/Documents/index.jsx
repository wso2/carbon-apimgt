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
import PropTypes from 'prop-types';
import { Route, Switch } from 'react-router-dom';

import { PageNotFound } from 'AppComponents/Base/Errors';
import Listing from './Listing';
import Details from './Details';
import DocCreate from './Create';

const Documents = (props) => {
    const { api } = props;
    return (
        <div>
            <Switch>
                <Route
                    exact
                    path='/apis/:apiUUID/documents'
                    render={routerprops => <Listing {...routerprops} api={api} />}
                />
                <Route
                    exact
                    path='/api-products/:apiProductUUID/documents'
                    render={routerprops => <Listing {...routerprops} api={api} />}
                />
                <Route
                    path='/apis/:apiUUID/documents/:documentId/details'
                    render={routerprops => <Details {...routerprops} api={api} />}
                />
                <Route
                    path='/api-products/:apiProductUUID/documents/:documentId/details'
                    render={routerprops => <Details {...routerprops} api={api} />}
                />
                <Route path='/apis/:apiUUID/documents/create' component={DocCreate} />
                <Route component={PageNotFound} />
            </Switch>
        </div>
    );
};

Documents.propTypes = {
    api: PropTypes.shape({}).isRequired,
};

export default Documents;
