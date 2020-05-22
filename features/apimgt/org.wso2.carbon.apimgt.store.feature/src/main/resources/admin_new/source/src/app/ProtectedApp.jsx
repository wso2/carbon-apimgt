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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Route, Switch, Redirect } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import Hidden from '@material-ui/core/Hidden';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import User from 'AppData/User';
import Utils from 'AppData/Utils';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/Avatar';
import Themes from 'Themes';
import merge from 'lodash.merge';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import RedirectToLogin from 'AppComponents/Shared/RedirectToLogin';
import { IntlProvider, injectIntl } from 'react-intl';
import { AppContextProvider } from 'AppComponents/Shared/AppContext';
import Configurations from 'Config';
import Navigator from 'AppComponents/Base/Navigator';
import RouteMenuMapping from 'AppComponents/Base/RouteMenuMapping';
import Dashboard from 'AppComponents/AdminPages/Dashboard/Dashboard';

const drawerWidth = 256;

const themeJSON = merge(Themes.light, {
    palette: {
        primary: {
            light: '#63ccff',
            main: '#009be5',
            dark: '#006db3',
        },
    },
    typography: {
        h5: {
            fontWeight: 500,
            fontSize: 26,
            letterSpacing: 0.5,
        },
    },
    shape: {
        borderRadius: 8,
    },
    props: {
        MuiTab: {
            disableRipple: true,
        },
    },
    mixins: {
        toolbar: {
            minHeight: 48,
        },
    },
    custom: {
        drawerWidth,
    },
});
let theme = createMuiTheme(themeJSON);

theme = {
    ...theme,
    overrides: {
        MuiDrawer: {
            paper: {
                backgroundColor: '#18202c',
            },
        },
        MuiButton: {
            label: {
                textTransform: 'none',
            },
            contained: {
                boxShadow: 'none',
                '&:active': {
                    boxShadow: 'none',
                },
            },
        },
        MuiTabs: {
            root: {
                marginLeft: theme.spacing(1),
            },
            indicator: {
                height: 3,
                borderTopLeftRadius: 3,
                borderTopRightRadius: 3,
                backgroundColor: theme.palette.common.white,
            },
        },
        MuiTab: {
            root: {
                textTransform: 'none',
                margin: '0 16px',
                minWidth: 0,
                padding: 0,
                [theme.breakpoints.up('md')]: {
                    padding: 0,
                    minWidth: 0,
                },
            },
        },
        MuiIconButton: {
            root: {
                padding: theme.spacing(1),
            },
        },
        MuiTooltip: {
            tooltip: {
                borderRadius: 4,
            },
        },
        MuiDivider: {
            root: {
                backgroundColor: '#404854',
            },
        },
        MuiListItemText: {
            primary: {
                fontWeight: theme.typography.fontWeightMedium,
            },
        },
        MuiListItemIcon: {
            root: {
                color: 'inherit',
                marginRight: 0,
                '& svg': {
                    fontSize: 20,
                },
            },
        },
        MuiAvatar: {
            root: {
                width: 32,
                height: 32,
            },
        },
    },
};


/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;
const allRoutes = [];

/**
 * Render protected application paths, Implements container presenter pattern
 */
class Protected extends Component {
    /**
     * Creates an instance of Protected.
     * @param {any} props @inheritDoc
     * @memberof Protected
     */
    constructor(props) {
        super(props);
        this.state = {
            clientId: Utils.getCookieWithoutEnvironment(User.CONST.ADMIN_CLIENT_ID),
            sessionStateCookie: Utils.getCookieWithoutEnvironment(User.CONST.ADMIN_SESSION_STATE),
            mobileOpen: false,
        };
        this.environments = [];
        this.checkSession = this.checkSession.bind(this);
        this.handleMessage = this.handleMessage.bind(this);
        const { intl } = props;
        const routeMenuMapping = RouteMenuMapping(intl);
        for (let i = 0; i < routeMenuMapping.length; i++) {
            const childRoutes = routeMenuMapping[i].children;
            if (childRoutes) {
                for (let j = 0; j < childRoutes.length; j++) {
                    allRoutes.push(childRoutes[j]);
                }
            } else {
                allRoutes.push(routeMenuMapping[i]);
            }
        }
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const user = AuthManager.getUser();
        window.addEventListener('message', this.handleMessage);
        if (user) {
            this.setState({ user });
            this.checkSession();
        } else {
            // If no user data available , Get the user info from existing token information
            // This could happen when OAuth code authentication took place and could send
            // user information via redirection
            const userPromise = AuthManager.getUserFromToken();
            userPromise.then((loggedUser) => this.setState({ user: loggedUser }));
        }
    }

    /**
     * Handle iframe message
     * @param {event} e Event
     */
    handleMessage(e) {
        if (e.data === 'changed') {
            window.location = Configurations.app.context + '/services/auth/login?not-Login';
        }
    }

    /**
     * Invoke checksession oidc endpoint.
     */
    checkSession() {
        setInterval(() => {
            const { clientId, sessionStateCookie } = this.state;
            const msg = clientId + ' ' + sessionStateCookie;
            document.getElementById('iframeOP').contentWindow.postMessage(msg, 'https://' + window.location.host);
        }, 2000);
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const { user = AuthManager.getUser(), messages, mobileOpen } = this.state;
        const header = (
            <Header
                avatar={<Avatar user={user} />}
                user={user}
                handleDrawerToggle={() => {
                    this.setState((oldState) => ({ mobileOpen: !oldState.mobileOpen }));
                }}
            />
        );
        const { clientId } = this.state;
        const checkSessionURL = 'https://' + window.location.host + '/oidc/checksession?client_id='
            + clientId + '&redirect_uri=https://' + window.location.host
            + Configurations.app.context + '/services/auth/callback/login';
        if (!user) {
            return (
                <IntlProvider locale={language} messages={messages}>
                    <RedirectToLogin />
                </IntlProvider>
            );
        }
        const leftMenu = (
            <AppContextProvider value={{ user }}>
                <>
                    <Hidden smUp implementation='js'>
                        <Navigator
                            PaperProps={{ style: { width: drawerWidth } }}
                            variant='temporary'
                            open={mobileOpen}
                            onClose={() => {
                                this.setState((oldState) => ({ mobileOpen: !oldState.mobileOpen }));
                            }}
                        />
                    </Hidden>
                    <Hidden xsDown implementation='css'>
                        <Navigator PaperProps={{ style: { width: drawerWidth } }} />
                    </Hidden>
                </>
            </AppContextProvider>
        );
        return (
            <MuiThemeProvider theme={theme}>
                <AppErrorBoundary>

                    <Base header={header} leftMenu={leftMenu}>
                        <Route>
                            <Switch>
                                <Redirect exact from='/' to='/dashboard' />
                                <Route path='/dashboard' component={Dashboard} />
                                {allRoutes.map((r) => {
                                    return <Route path={r.path} component={r.component} />;
                                })}
                                <Route component={ResourceNotFound} />
                            </Switch>
                        </Route>
                    </Base>
                    <iframe
                        title='iframeOP'
                        id='iframeOP'
                        src={checkSessionURL}
                        width='0px'
                        height='0px'
                        style={{ color: 'red', position: 'absolute' }}
                    />
                </AppErrorBoundary>
            </MuiThemeProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};

export default injectIntl(Protected);
