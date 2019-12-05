/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CreateScope from './CreateScope';
import EditScope from './EditScope';
import Scopes from './Scopes';

const Scope = () => {
    const [api, updateAPI] = useAPI();
    const urlPrefix = api.isAPIProduct() ? 'api-products' : 'apis';
    return (
        <Switch>
            <Route exact path={'/' + urlPrefix + '/:api_uuid/scopes/'} component={Scopes} />
            <Route
                exact
                path={'/' + urlPrefix + '/:api_uuid/scopes/create'}
                component={(props) => <CreateScope api={api} {...props} updateAPI={updateAPI} />}
            />
            <Route
                exact
                path={'/' + urlPrefix + '/:api_uuid/scopes/edit'}
                component={(props) => <EditScope api={api} {...props} updateAPI={updateAPI} />}
            />
            <Route component={ResourceNotFound} />
        </Switch>
    );
};

Scope.propTypes = {
    api: PropTypes.shape({
        id: PropTypes.string,
        additionalProperties: PropTypes.shape({
            key: PropTypes.string,
            value: PropTypes.string,
        }).isRequired,
    }).isRequired,
};

export default Scope;
