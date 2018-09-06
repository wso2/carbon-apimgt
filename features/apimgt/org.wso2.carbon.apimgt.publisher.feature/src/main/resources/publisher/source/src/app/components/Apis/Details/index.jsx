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
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Log from 'log4javascript';
import PropTypes from 'prop-types';

import OverviewIcon from '@material-ui/icons/SettingsOverscan';
import LifeCycleIcon from '@material-ui/icons/Autorenew';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import ScopesIcon from '@material-ui/icons/VpnKey';
import SecurityIcon from '@material-ui/icons/Security';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
import SubscriptionsIcon from '@material-ui/icons/Bookmarks';
import Overview from './Overview/Overview';
import LifeCycle from './LifeCycle/LifeCycle';
import Documents from './Documents/Documents';
import Resources from './Resources/Resources';
import Endpoints from './Endpoints';
import Subscriptions from './Subscriptions/Subscriptions';
import Scopes from './Scopes/Scopes';
import Security from './Security';

import PageContainer from '../../Base/container/';
import APIDetailsNavBar from './components/APIDetailsNavBar';
import Utils from '../../../data/Utils';
import ConfigManager from '../../../data/ConfigManager';
import APIDetailsTopMenu from './components/APIDetailsTopMenu';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Api from '../../../data/api';
import APIDetailsRoutes from './components/APIDetailsRoutes';
import { Progress } from '../../Shared';

const apiDetailPages = [
    {
        name: 'Overview',
        pathName: 'overview',
        PageComponent: Overview,
        NavIcon: <OverviewIcon />,
    },
    {
        name: 'LifeCycle',
        pathName: 'lifecycle',
        PageComponent: LifeCycle,
        NavIcon: <LifeCycleIcon />,
    },
    {
        name: 'Endpoints',
        pathName: 'endpoints',
        PageComponent: Endpoints,
        NavIcon: <EndpointIcon />,
    },
    {
        name: 'Resources',
        pathName: 'resources',
        PageComponent: Resources,
        NavIcon: <ResourcesIcon />,
    },
    {
        name: 'Scopes',
        pathName: 'scopes',
        PageComponent: Scopes,
        NavIcon: <ScopesIcon />,
    },
    {
        name: 'Documents',
        pathName: 'documents',
        PageComponent: Documents,
        NavIcon: <DocumentsIcon />,
    },
    {
        name: 'Subscription',
        pathName: 'subscription',
        PageComponent: Subscriptions,
        NavIcon: <SubscriptionsIcon />,
    },
    {
        name: 'Security',
        pathName: 'security',
        PageComponent: Security,
        NavIcon: <SecurityIcon />,
    },
];
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
            api: null,
            apiNotFound: false,
            multi_environments: false,
        };
        this.setAPI = this.setAPI.bind(this);
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
        this.setAPI();
    }

    /**
     *
     *
     * @returns
     * @memberof Details
     */
    componentDidUpdate() {
        const { api } = this.state;
        const { apiUUID } = this.props.match.params;
        if (!api || api.id === apiUUID) {
            return;
        }
        this.setAPI();
    }

    /**
     *
     *
     * @memberof Details
     */
    setAPI() {
        const { apiUUID } = this.props.match.params;
        const promisedApi = Api.get(apiUUID);
        promisedApi
            .then((api) => {
                this.setState({ api });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    }

    /**
     * Renders Grid container layout with NavBar place static in LHS, Components which coming as children for
     * Details page
     * should wrap it's content with <Grid item > element
     * @returns {Component} Render API Details page
     */
    render() {
        const { api, apiNotFound } = this.state;

        if (apiNotFound) {
            const { apiUUID } = this.props.match.params;
            const resourceNotFountMessage = {
                title: `API is Not Found in the "${Utils.getCurrentEnvironment().label}" Environment`,
                body: `Can't find the API with the id "${apiUUID}"`,
            };
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!api) {
            return <Progress />;
        }

        return (
            <PageContainer
                pageNav={<APIDetailsNavBar apiDetailPages={apiDetailPages} />}
                pageTopMenu={<APIDetailsTopMenu api={api} />}
            >
                <APIDetailsRoutes apiDetailPages={apiDetailPages} api={api} />
            </PageContainer>
        );
    }
}

Details.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
};
