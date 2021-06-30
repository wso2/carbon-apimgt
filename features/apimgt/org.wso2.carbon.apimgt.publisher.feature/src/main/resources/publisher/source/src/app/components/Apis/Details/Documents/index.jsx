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

import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Route, Switch } from 'react-router-dom';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import Listing from './Listing';
import View from './View';
import Edit from './Edit';
import EditContent from './EditContent';
import DocCreate from './Create';

const Documents = (props) => {
    const { isAPIProduct } = useContext(APIContext);
    const urlPrefix = isAPIProduct ? 'api-products' : 'apis';
    return (
        <div>
            <Switch>
                <Route exact path={'/' + urlPrefix + '/:apiUUID/documents'} component={Listing} />
                <Route exact path={'/' + urlPrefix + '/:apiUUID/documents/:documentId/details'} component={View} />
                <Route exact path={'/' + urlPrefix + '/:apiUUID/documents/:documentId/edit'} component={Edit} />
                <Route
                    exact
                    path={'/' + urlPrefix + '/:apiUUID/documents/:documentId/edit-content'}
                    component={EditContent} />
                <Route exact path={'/' + urlPrefix + '/:apiUUID/documents/create'} component={DocCreate} />
                <Route component={ResourceNotFound} />
            </Switch>
        </div>
    );
};

Documents.propTypes = {
    api: PropTypes.shape({}).isRequired,
};

export default Documents;
