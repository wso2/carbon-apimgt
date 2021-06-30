/* eslint-disable react/prop-types */
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
import {
    Route, Switch, Redirect, Link,
} from 'react-router-dom';
import VpnKeyIcon from '@material-ui/icons/VpnKey';
import ScreenLockLandscapeIcon from '@material-ui/icons/ScreenLockLandscape';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import { app } from 'Settings';
import Loading from 'AppComponents/Base/Loading/Loading';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import TokenManager from 'AppComponents/Shared/AppsAndKeys/TokenManager';
import ApiKeyManager from 'AppComponents/Shared/AppsAndKeys/ApiKeyManager';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import { Helmet } from 'react-helmet';
import Subscriptions from './Subscriptions';
import InfoBar from './InfoBar';
import Overview from './Overview';
import WebHookDetails from './WebHookDetails';

/**
 *
 *
 * @param {*} theme theme details
 * @returns {Object}
 */
const styles = (theme) => {
    const {
        custom: {
            leftMenu: { width, position },
        },
    } = theme;
    const shiftToLeft = position === 'vertical-left' ? (width - 4) : 0;
    const shiftToRight = position === 'vertical-right' ? width : 0;
    const leftMenuPaddingLeft = position === 'horizontal' ? theme.spacing(3) : 0;

    return {
        LeftMenu: {
            backgroundColor: theme.custom.leftMenu.background,
            backgroundImage: `url(${app.context}${theme.custom.leftMenu.backgroundImage})`,
            textAlign: 'left',
            fontFamily: theme.typography.fontFamily,
            position: 'absolute',
            bottom: 0,
            paddingLeft: leftMenuPaddingLeft,
        },
        leftMenuHorizontal: {
            top: theme.custom.infoBar.height,
            width: '100%',
            overflowX: 'auto',
            height: 60,
            display: 'flex',
            left: 0,
        },
        leftMenuVerticalLeft: {
            width: theme.custom.leftMenu.width,
            top: 0,
            left: 0,
            overflowY: 'auto',
            [theme.breakpoints.down('sm')]: {
                width: 50,
            },
        },
        leftMenuVerticalRight: {
            width: theme.custom.leftMenu.width,
            top: 0,
            right: 0,
            overflowY: 'auto',
        },
        leftLInkMain: {
            borderRight: 'solid 1px ' + theme.custom.leftMenu.background,
            cursor: 'pointer',
            background: theme.custom.leftMenu.rootBackground,
            color: theme.palette.getContrastText(theme.custom.leftMenu.rootBackground),
            textDecoration: 'none',
            alignItems: 'center',
            justifyContent: 'center',
            display: 'flex',
            height: theme.custom.infoBar.height,
        },
        leftLInkMainText: {
            fontSize: 18,
            color: theme.palette.grey[500],
            textDecoration: 'none',
            paddingLeft: theme.spacing(2),
        },
        detailsContent: {
            display: 'flex',
            flex: 1,
        },
        content: {
            display: 'flex',
            flex: 1,
            flexDirection: 'column',
            marginLeft: shiftToLeft,
            marginRight: shiftToRight,
            paddingBottom: theme.spacing(3),
            overflowX: 'hidden',
            [theme.breakpoints.down('sm')]: {
                marginLeft: shiftToLeft !== 0 && 50,
                marginRight: shiftToRight !== 0 && 50,
            },
        },
        contentLoader: {
            paddingTop: theme.spacing(3),
        },
        contentLoaderRightMenu: {
            paddingRight: theme.custom.leftMenu.width,
        },
        titleWrapper: {
            paddingLeft: 25,
            paddingTop: 28,
            textTransform: 'capitalize',
        },
        contentWrapper: {
            paddingLeft: 25,
        },
        keyTitle: {
            textTransform: 'capitalize',
        },
    };
};
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
    };

    toTitleCase = (str) => {
        return str.replace(
            /\w\S*/g,
            (txt) => {
                return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
            },
        );
    };

    renderManager = (application, keyType, secScheme) => {
        const { classes } = this.props;
        return (
            <Paper>
                {secScheme === 'oauth' && (
                    <TokenManager
                        keyType={keyType}
                        selectedApp={{
                            appId: application.applicationId,
                            label: application.name,
                            tokenType: application.tokenType,
                            owner: application.owner,
                            hashEnabled: application.hashEnabled,
                        }}
                    />

                )}
                {secScheme === 'apikey' && (
                    <div className={classes.root}>
                        <div className={classes.titleWrapper}>
                            <Typography variant='h5' className={classes.keyTitle}>
                                {this.toTitleCase(keyType)}
                                <FormattedMessage
                                    id='Applications.Details.api.keys.title'
                                    defaultMessage=' API Key'
                                />
                            </Typography>
                        </div>
                        <div className={classes.contentWrapper}>
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
        const {
            classes, match, theme, intl,
        } = this.props;
        const { notFound, application } = this.state;
        const pathPrefix = '/applications/' + match.params.application_uuid;
        const redirectUrl = pathPrefix + '/overview';
        const {
            custom: {
                leftMenu: {
                    rootIconSize, rootIconTextVisible, rootIconVisible, position,
                },
                title: {
                    prefix, sufix,
                },
            },
        } = theme;
        if (notFound) {
            return <ResourceNotFound />;
        } else if (!application) {
            return <Loading />;
        }
        return (
            <>
                <Helmet>
                    <title>{`${prefix} ${application.name}${sufix}`}</title>
                </Helmet>
                <nav
                    role='navigation'
                    aria-label={intl.formatMessage({
                        id: 'Applications.Details.index.secondary.navigation',
                        defaultMessage: 'Secondary Navigation',
                    })}
                    className={classNames(
                        classes.LeftMenu,
                        {
                            [classes.leftMenuHorizontal]: position === 'horizontal',
                        },
                        {
                            [classes.leftMenuVerticalLeft]: position === 'vertical-left',
                        },
                        {
                            [classes.leftMenuVerticalRight]: position === 'vertical-right',
                        },
                        'left-menu',
                    )}
                >
                    {rootIconVisible && (
                        <Link to='/applications' className={classes.leftLInkMain} aria-label='All applications'>
                            <CustomIcon width={rootIconSize} height={rootIconSize} icon='applications' />
                            {rootIconTextVisible && (
                                <Typography className={classes.leftLInkMainText}>
                                    <FormattedMessage
                                        id='Applications.Details.applications.all'
                                        defaultMessage='ALL APPs'
                                    />
                                </Typography>
                            )}
                        </Link>
                    )}
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.overview'
                                defaultMessage='Overview'
                            />
                        )}
                        iconText='overview'
                        route='overview'
                        to={pathPrefix + '/overview'}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.prod.keys'
                                defaultMessage='Production Keys'
                            />
                        )}
                        iconText='productionkeys'
                        route='productionkeys'
                        to={pathPrefix + '/productionkeys/oauth'}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.oauth.tokens'
                                defaultMessage='OAuth2 Tokens'
                            />
                        )}
                        route='productionkeys/oauth'
                        to={pathPrefix + '/productionkeys/oauth'}
                        submenu
                        Icon={<ScreenLockLandscapeIcon />}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.api.key'
                                defaultMessage='API Key'
                            />
                        )}
                        route='productionkeys/apikey'
                        to={pathPrefix + '/productionkeys/apikey'}
                        submenu
                        Icon={<VpnKeyIcon />}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.sandbox.keys'
                                defaultMessage='Sandbox Keys'
                            />
                        )}
                        iconText='productionkeys'
                        route='sandboxkeys'
                        to={pathPrefix + '/sandboxkeys/oauth'}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.oauth.tokens'
                                defaultMessage='OAuth2 Tokens'
                            />
                        )}
                        route='sandboxkeys/oauth'
                        to={pathPrefix + '/sandboxkeys/oauth'}
                        submenu
                        Icon={<ScreenLockLandscapeIcon />}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.api.key'
                                defaultMessage='API Key'
                            />
                        )}
                        route='sandboxkeys/apikey'
                        to={pathPrefix + '/sandboxkeys/apikey'}
                        submenu
                        Icon={<VpnKeyIcon />}
                        open
                    />
                    <LeftMenuItem
                        text={(
                            <FormattedMessage
                                id='Applications.Details.menu.subscriptions'
                                defaultMessage='Subscriptions'
                            />
                        )}
                        iconText='subscriptions'
                        route='subscriptions'
                        to={pathPrefix + '/subscriptions'}
                        open
                    />
                </nav>
                <div className={classes.content}>
                    <InfoBar
                        applicationId={match.params.application_uuid}
                        innerRef={(node) => { this.infoBar = node; }}
                    />
                    <div
                        className={classNames(
                            { [classes.contentLoader]: position === 'horizontal' },
                            { [classes.contentLoaderRightMenu]: position === 'vertical-right' },
                        )}
                    >
                        <Switch>
                            <Redirect exact from='/applications/:applicationId' to={redirectUrl} />
                            <Route
                                path='/applications/:applicationId/overview'
                                component={Overview}
                            />
                            <Route
                                path='/applications/:applicationId/webhooks/:apiId'
                                component={WebHookDetails}
                            />
                            <Route
                                path='/applications/:applicationId/productionkeys/oauth'
                                component={() => (this.renderManager(application, 'PRODUCTION', 'oauth'))}
                            />
                            <Route
                                path='/applications/:applicationId/productionkeys/apikey'
                                component={() => (this.renderManager(application, 'PRODUCTION', 'apikey'))}
                            />
                            <Route
                                path='/applications/:applicationId/sandboxkeys/oauth'
                                component={() => (this.renderManager(application, 'SANDBOX', 'oauth'))}
                            />
                            <Route
                                path='/applications/:applicationId/sandboxkeys/apikey'
                                component={() => (this.renderManager(application, 'SANDBOX', 'apikey'))}
                            />
                            <Route path='/applications/:applicationId/subscriptions' component={Subscriptions} />
                            <Route component={ResourceNotFound} />
                        </Switch>
                    </div>
                </div>
            </>
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
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(injectIntl(Details));
