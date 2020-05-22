/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CreateAPICategory from './CreateAPICategory';
import EditAPICategory from './EditAPICategory';
import APICategories from './APICategories';
import settings from '../../../../../../site/public/conf/settings';

const APICategory = () => {
    return (
        <Switch>
            <Route
                exact
                path={settings.app.context + '/categories/api-categories'}
                component={APICategories}
            />
            <Route
                exact
                path={
                    settings.app.context +
                    '/categories/api-categories/create-api-category'
                }
                component={(props) => <CreateAPICategory {...props} />}
            />
            <Route
                exact
                path={
                    settings.app.context +
                    '/categories/api-categories/edit-api-category/:id'
                }
                component={(props) => <EditAPICategory {...props} />}
            />
            <Route component={ResourceNotFound} />
        </Switch>
    );
};

export default APICategory;
