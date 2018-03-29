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

import React, { Component } from 'react';
import { Link, Redirect, Route, Switch } from 'react-router-dom';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import Log from 'log4javascript';
import PropTypes from 'prop-types';

import Overview from './Overview';
import LifeCycle from './LifeCycle/LifeCycle';
import Documents from './Documents/Documents';
import { PageNotFound } from '../../Base/Errors/index';
import Resources from './Resources/Resources';
import PermissionFormWrapper from './Permission';
import Endpoints from './Endpoints';
import Subscriptions from './Subscriptions/Subscriptions';
import Scopes from './Scopes/Scopes';
import Security from './Security';
import NavBar from './NavBar';
import EnvironmentOverview from './EnvironmentOverview/EnvironmentOverview';
import Utils from '../../../data/Utils';
import ConfigManager from '../../../data/ConfigManager';

/**
 * Base component for API specific Details page, This component will be mount for any request coming for /apis/:api_uuid
 */
export default class Details extends Component {
    /**
     * Creates an instance of Details.
     * @param {any} props @inheritDoc
     * @memberof Details
     */
    constructor(props) {
        super(props);
        this.state = {
            resourceNotFountMessage: { multi_environments: false },
        };
    }

    /**
     * @inheritDoc
     * @memberof Details
     */
    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const multiEnvironments = response.data.environments.length > 1;
                const more = multiEnvironments && (
                    <Link to='/apis'>
                        <Button variant='raised' color='secondary'>
                            Go Home
                        </Button>
                    </Link>
                );
                this.setState({
                    resourceNotFountMessage: { more, multiEnvironments },
                });
            })
            .catch((error) => {
                Log.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * Renders Grid container layout with NavBar place static in LHS, Components which coming as children for
     * Details page
     * should wrap it's content with <Grid item > element
     * @returns {Component} Render API Details page
     */
    render() {
        const { apiUUID } = this.props.match.params;
        const redirectUrl = `/apis/${apiUUID}/overview`;
        const environmentOverview = Utils.isMultiEnvironmentOverviewEnabled();
        const { resourceNotFountMessage } = this.state;

        if (resourceNotFountMessage.multi_environments) {
            resourceNotFountMessage.title = `API is Not Found in the "${
                Utils.getCurrentEnvironment().label
            }" Environment`;
            resourceNotFountMessage.body = `Can't find the API with the id "${apiUUID}"`;
        }

        return (
            <Grid container spacing={0}>
                <Grid item xs={12}>
                    <NavBar apiUUID={apiUUID} />
                </Grid>
                <Grid item xs={12}>
                    <Switch>
                        <Redirect exact from='/apis/:api_uuid' to={redirectUrl} />
                        <Route
                            path='/apis/:apiUUID/overview'
                            render={props => <Overview resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        {environmentOverview ? (
                            <Route
                                path='/apis/:api_uuid/environment view'
                                render={props => (
                                    <EnvironmentOverview resourceNotFountMessage={resourceNotFountMessage} {...props} />
                                )}
                            />
                        ) : null}
                        <Route
                            path='/apis/:api_uuid/lifecycle'
                            render={props => <LifeCycle resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route
                            path='/apis/:api_uuid/resources'
                            render={props => <Resources resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route
                            path='/apis/:api_uuid/permission'
                            render={props => (
                                <PermissionFormWrapper resourceNotFountMessage={resourceNotFountMessage} {...props} />
                            )}
                        />
                        <Route
                            path='/apis/:api_uuid/documents'
                            render={props => <Documents resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route
                            path='/apis/:apiUUID/endpoints'
                            render={props => <Endpoints resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route
                            path='/apis/:api_uuid/subscriptions'
                            render={props => (
                                <Subscriptions resourceNotFountMessage={resourceNotFountMessage} {...props} />
                            )}
                        />
                        <Route
                            path='/apis/:api_uuid/security'
                            render={props => <Security resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route
                            path='/apis/:api_uuid/scopes'
                            render={props => <Scopes resourceNotFountMessage={resourceNotFountMessage} {...props} />}
                        />
                        <Route component={PageNotFound} />
                    </Switch>
                </Grid>
            </Grid>
        );
    }
}

Details.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
};
