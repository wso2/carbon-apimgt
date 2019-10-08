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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Route, Switch, Redirect, Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import API from 'AppData/api';
import { PageNotFound } from 'AppComponents/Base/Errors/index';
import Loading from 'AppComponents/Base/Loading/Loading';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import TokenManager from 'AppComponents/Shared/AppsAndKeys/TokenManager';
import ApiKeyManager from 'AppComponents/Shared/AppsAndKeys/ApiKeyManager';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Paper from '@material-ui/core/Paper';
import Subscriptions from './Subscriptions';
import InfoBar from './InfoBar';
/**
 *
 *
 * @param {*} theme theme details
 * @returns {Object}
 */
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
    contentDown: {
        width: theme.custom.contentAreaWidth,
    },
});
/**
 *
 *
 * @class Details
 * @extends {Component}
 */
class Details extends Component {
    /**
     *
     * @param {Object} props props passed from above
     */
    constructor(props) {
        super(props);
        this.state = {
            application: null,
            active: 'overview',
            secScheme: 'Oauth',
        };
    }

    /**
     *
     *
     * @memberof Details
     */
    componentDidMount() {
        const { match } = this.props;
        const client = new API();
        const promisedApplication = client.getApplication(match.params.application_uuid);
        promisedApplication
            .then((response) => {
                this.setState({ application: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     * @param {String} menuLink selected menu name
     * @memberof Details
     */
    handleMenuSelect = (menuLink) => {
        const { history, match } = this.props;
        history.push({ pathname: '/applications/' + match.params.application_uuid + '/' + menuLink });
        this.setState({ active: menuLink });
    };


    handleChange = (event, secScheme) => {
        this.setState({ secScheme });
    }

    renderManager = (application, keyType) => {
        return (
            <Paper>
                <Tabs
                    value={this.state.secScheme}
                    onChange={this.handleChange}
                    indicatorColor='primary'
                    textColor='primary'
                    variant='fullWidth'
                    scrollButtons='auto'
                >
                    <Tab label='Oauth' value='Oauth' />
                    <Tab label='ApiKey' value='ApiKey' />
                </Tabs>
                {this.state.secScheme === 'Oauth' && (
                    <div>
                        <TokenManager
                            keyType={keyType}
                            selectedApp={{
                                appId: application.applicationId,
                                label: application.name,
                                tokenType: application.tokenType,
                                owner: application.owner,
                            }}
                        />
                    </div>
                )}
                {this.state.secScheme === 'ApiKey' && (
                    <div>
                        <ApiKeyManager
                            keyType={keyType}
                            selectedApp={{
                                appId: application.applicationId,
                                label: application.name,
                                tokenType: application.tokenType,
                                owner: application.owner,
                            }}
                        />
                    </div>
                )}
            </Paper>
        );
    }

    /**
     *
     *
     * @returns {Component}
     * @memberof Details
     */
    render() {
        const { classes, match } = this.props;
        const { notFound, application, active } = this.state;
        const redirectUrl = '/applications/' + match.params.application_uuid + '/productionkeys';
        if (notFound) {
            return <ResourceNotFound />;
        } else if (!application) {
            return <Loading />;
        }
        return (
            <React.Fragment>
                <div className={classes.LeftMenu}>
                    <Link to='/applications'>
                        <div className={classes.leftLInkMain}>
                            <CustomIcon width={52} height={52} icon='applications' />
                        </div>
                    </Link>
                    <LeftMenuItem text='production keys' handleMenuSelect={this.handleMenuSelect} active={active} />
                    <LeftMenuItem text='sandbox keys' handleMenuSelect={this.handleMenuSelect} active={active} />
                    <LeftMenuItem text='subscriptions' handleMenuSelect={this.handleMenuSelect} active={active} />
                </div>
                <div className={classes.content}>
                    <InfoBar applicationId={match.params.application_uuid} innerRef={node => (this.infoBar = node)} />
                    <div className={classes.contentDown}>
                        <Switch>
                            <Redirect exact from='/applications/:applicationId' to={redirectUrl} />
                            <Route
                                path='/applications/:applicationId/productionkeys'
                                component={() => (this.renderManager(application, 'PRODUCTION'))}
                            />
                            <Route
                                path='/applications/:applicationId/sandBoxkeys'
                                component={() => (this.renderManager(application, 'SANDBOX'))}
                            />
                            <Route path='/applications/:applicationId/subscriptions' component={Subscriptions} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </div>
                </div>
            </React.Fragment>
        );
    }
}

Details.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            application_uuid: PropTypes.string.isRequired,
        }).isRequired,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(Details);
