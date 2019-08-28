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
import Log from 'log4javascript';
import PropTypes from 'prop-types';

import LifeCycleIcon from '@material-ui/icons/Autorenew';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import ScopesIcon from '@material-ui/icons/VpnKey';
import SecurityIcon from '@material-ui/icons/Security';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
import CommentsIcon from '@material-ui/icons/CommentRounded';
import BusinessIcon from '@material-ui/icons/Business';
import SubscriptionsIcon from '@material-ui/icons/Bookmarks';
import ConfigurationIcon from '@material-ui/icons/Build';
import PropertiesIcon from '@material-ui/icons/List';
import { withStyles } from '@material-ui/core/styles';
import { Redirect, Route, Switch, Link } from 'react-router-dom';
import Utils from 'AppData/Utils';
import ConfigManager from 'AppData/ConfigManager';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import Overview from './NewOverview/Overview';
import Configuration from './Configuration/Configuration';
import LifeCycle from './LifeCycle/LifeCycle';
import Documents from './Documents';
import Resources from './Resources/Resources';
import Endpoints from './Endpoints/Endpoints';
import Subscriptions from './Subscriptions/Subscriptions';
import Comments from './Comments/Comments';
import Scope from './Scopes';
import Security from './Security';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import { PageNotFound } from '../../Base/Errors/index';
import APIDetailsTopMenu from './components/APIDetailsTopMenu';
import BusinessInformation from './BusinessInformation/BusinessInformation';
import Properties from './Properties/Properties';
import ApiContext from './components/ApiContext';

const styles = theme => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        color: theme.palette.getContrastText(theme.palette.background.leftMenuActive),
        textDecoration: 'none',
    },
    detailsContent: {
        display: 'flex',
        flex: 1,
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom: theme.spacing.unit * 3,
    },
    contentInside: {
        paddingLeft: theme.spacing.unit * 3,
        paddingRight: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 2,
    },
});

/**
 * Base component for API specific Details page, This component will be mount for any request coming for /apis/:api_uuid
 */
class Details extends Component {
    /**
     * Creates an instance of Details.
     * @param {any} props @inheritDoc
     * @memberof Details
     */
    constructor(props) {
        super(props);
        this.handleMenuSelect = this.handleMenuSelect.bind(this);
        const { location } = this.props;
        const currentLink = location.pathname.match(/[^/]+(?=\/$|$)/g);
        let active = null;
        if (currentLink && currentLink.length > 0) {
            [active] = currentLink;
        }
        this.state = {
            api: null,
            apiNotFound: false,
            active: active || 'overview',
            updateAPI: this.updateAPI, // eslint-disable-line react/no-unused-state
        };
        this.setAPI = this.setAPI.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Details
     */
    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then(() => {
                // const multiEnvironments = response.data.environments.length > 1;
                // const more = multiEnvironments && (
                //     <Link to='/apis'>
                //         <Button variant='raised' color='secondary'>
                //             Go Home
                //         </Button>
                //     </Link>
                // );
                // this.setState({
                //     resourceNotFountMessage: { more, multiEnvironments },
                // });
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
    updateAPI(newAPI) {
        const restAPI = new Api();
        /* eslint no-underscore-dangle: ["error", { "allow": ["_data"] }] */
        /* eslint no-param-reassign: ["error", { "props": false }] */
        if (newAPI._data) delete newAPI._data;
        if (newAPI.client) delete newAPI.client;

        const promisedApi = restAPI.update(JSON.parse(JSON.stringify(newAPI)));
        promisedApi
            .then((api) => {
                Alert.info(`${api.name} updated successfully.`);
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
    handleMenuSelect(menuLink) {
        this.props.history.push({ pathname: '/apis/' + this.props.match.params.apiUUID + '/' + menuLink });
        this.setState({ active: menuLink });
    }
    /**
     * Renders Grid container layout with NavBar place static in LHS, Components which coming as children for
     * Details page
     * should wrap it's content with <Grid item > element
     * @returns {Component} Render API Details page
     */
    render() {
        const { api, apiNotFound, active } = this.state;
        const { classes, theme, match } = this.props;
        const redirectUrl = '/apis/' + match.params.api_uuid + '/' + active;

        if (apiNotFound) {
            const { apiUUID } = match.params;
            const resourceNotFountMessage = {
                title: `API is Not Found in the "${Utils.getCurrentEnvironment().label}" Environment`,
                body: `Can't find the API with the id "${apiUUID}"`,
            };
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!api) {
            return <Progress />;
        }
        const { leftMenuIconMainSize } = theme.custom;

        return (
            <React.Fragment>
                <ApiContext.Provider value={this.state}>
                    <div className={classes.LeftMenu}>
                        <Link to='/apis'>
                            <div className={classes.leftLInkMain}>
                                <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon='api' />
                            </div>
                        </Link>
                        <LeftMenuItem text='overview' handleMenuSelect={this.handleMenuSelect} active={active} />
                        <LeftMenuItem
                            text='configuration'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ConfigurationIcon />}
                        />
                        <LeftMenuItem
                            text='lifecycle'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<LifeCycleIcon />}
                        />
                        <LeftMenuItem
                            text='endpoints'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<EndpointIcon />}
                        />
                        <LeftMenuItem
                            text='resources'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ResourcesIcon />}
                        />
                        <LeftMenuItem
                            text='scopes'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ScopesIcon />}
                        />
                        <LeftMenuItem
                            text='documents'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<DocumentsIcon />}
                        />
                        <LeftMenuItem
                            text='subscriptions'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<SubscriptionsIcon />}
                        />
                        <LeftMenuItem
                            text='security'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<SecurityIcon />}
                        />
                        <LeftMenuItem
                            text='comments'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<CommentsIcon />}
                        />
                        <LeftMenuItem
                            text='business info'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<BusinessIcon />}
                        />
                        <LeftMenuItem
                            text='properties'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<PropertiesIcon />}
                        />
                    </div>
                    <div className={classes.content}>
                        <APIDetailsTopMenu api={api} />
                        <div className={classes.contentInside}>
                            <Switch>
                                <Redirect exact from='/apis/:api_uuid' to={redirectUrl} />
                                <Route path='/apis/:api_uuid/overview' component={() => <Overview />} />
                                <Route path='/apis/:api_uuid/lifecycle' component={() => <LifeCycle api={api} />} />
                                <Route
                                    path='/apis/:api_uuid/configuration'
                                    component={() => <Configuration api={api} />}
                                />
                                <Route path='/apis/:api_uuid/endpoints' component={() => <Endpoints api={api} />} />
                                <Route path='/apis/:api_uuid/resources' component={() => <Resources api={api} />} />
                                <Route path='/apis/:api_uuid/scopes' component={() => <Scope api={api} />} />
                                <Route path='/apis/:api_uuid/documents' component={() => <Documents api={api} />} />
                                <Route
                                    path='/apis/:api_uuid/subscriptions'
                                    component={() => <Subscriptions api={api} />}
                                />
                                <Route path='/apis/:api_uuid/security' component={() => <Security api={api} />} />
                                <Route path='/apis/:api_uuid/comments' component={() => <Comments api={api} />} />
                                <Route path='/apis/:api_uuid/business info' component={() => <BusinessInformation />} />
                                <Route path='/apis/:api_uuid/properties' component={() => <Properties />} />
                                <Route component={PageNotFound} />
                            </Switch>
                        </div>
                    </div>
                </ApiContext.Provider>
            </React.Fragment>
        );
    }
}

Details.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.object,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.object,
    }).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(Details);
