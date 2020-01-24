/* eslint-disable react/jsx-props-no-spreading */
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

import React from 'react';
import { Link } from 'react-router-dom';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import AccountCircle from '@material-ui/icons/AccountCircle';
import Button from '@material-ui/core/Button';
import Hidden from '@material-ui/core/Hidden';
import {
    MenuItem, MenuList, ListItemIcon, ListItemText, Divider,
} from '@material-ui/core';
import Icon from '@material-ui/core/Icon';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { findDOMNode } from 'react-dom';
import Typography from '@material-ui/core/Typography';
import Popper from '@material-ui/core/Popper';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import { FormattedMessage } from 'react-intl';
import Drawer from '@material-ui/core/Drawer';
import HeaderSearch from 'AppComponents/Base/Header/Search/HeaderSearch';
import Settings from 'AppComponents/Shared/SettingsContext';
import { app } from 'Settings';
import AuthManager from '../../data/AuthManager';
import ConfigManager from '../../data/ConfigManager';
import EnvironmentMenu from './Header/EnvironmentMenu';
import GlobalNavBar from './Header/GlobalNavbar';
import Utils from '../../data/Utils';
import VerticalDivider from '../Shared/VerticalDivider';

const styles = (theme) => {
    const pageMaxWidth = theme.custom.page.style === 'fluid' ? 'none' : theme.custom.page.width;
    return {
        appBar: {
            position: 'relative',
            background: theme.custom.appBar.background,
        },
        icon: {
            marginRight: theme.spacing(2),
        },
        menuIcon: {
            color: theme.palette.getContrastText(theme.custom.appBar.background),
            fontSize: 35,
        },
        userLink: {
            color: theme.palette.getContrastText(theme.custom.appBar.background),
        },
        publicStore: {
            color: theme.palette.getContrastText(theme.custom.appBar.background),
        },
        // Page layout styles
        drawer: {
            top: 64,
        },
        wrapper: {
            minHeight: '100%',
            marginBottom: -50,
            background: theme.palette.background.default + ' url(' + theme.custom.backgroundImage + ') repeat left top',
        },
        contentWrapper: {
            display: 'flex',
            flexDirection: 'row',
            overflowY: 'hidden',
            position: 'relative',
            minHeight: 'calc(100vh - 114px)',
        },
        push: {
            height: 50,
        },
        footer: {
            background: theme.custom.footer.background,
            color: theme.custom.footer.color,
            paddingLeft: theme.spacing(3),
            height: 50,
            alignItems: 'center',
            display: 'flex',
        },
        toolbar: {
            minHeight: 56,
            [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
                minHeight: 48,
            },
            [theme.breakpoints.up('sm')]: {
                minHeight: 64,
            },
        },
        list: {
            width: theme.custom.appBar.drawerWidth,
        },
        drawerStyles: {
            top: theme.mixins.toolbar['@media (min-width:600px)'].minHeight,
        },
        listInline: {
            '& ul': {
                display: 'flex',
                flexDirection: 'row',
            },
        },
        reactRoot: {
            maxWidth: pageMaxWidth,
            margin: 'auto',
            borderLeft: theme.custom.page.border,
            borderRight: theme.custom.page.border,
        },
        icons: {
            marginRight: theme.spacing(),
            fontSize: theme.spacing(3),
        },
        banner: {
            color: theme.custom.banner.color,
            background: theme.custom.banner.background,
            padding: theme.custom.banner.padding,
            margin: theme.custom.banner.margin,
            fontSize: theme.custom.banner.fontSize,
            display: 'flex',
            distributeContent: theme.custom.banner.textAlign,
            justifyContent: theme.custom.banner.textAlign,
        },
    };
};

/**
 *
 * @class Layout
 * @extends {React.Component}
 */
class Layout extends React.Component {
    static contextType = Settings;

    /**
     * @inheritdoc
     * @param {*} props
     * @memberof Layout
     */
    constructor(props) {
        super(props);
        this.toggleGlobalNavBar = this.toggleGlobalNavBar.bind(this);
    }

    state = {
        environments: {},
        environmentId: 0,
        nightMode: false,
        themeIndex: 0,
        left: false,
        openNavBar: false,
        openUserMenu: false,
    };

    componentWillMount() {
        const { theme } = this.props;
        document.body.style.backgroundColor = theme.custom.page.emptyAreadBackground || '#ffffff';
    }

    componentDidMount() {
        // Get Environments
        const promised_environments = ConfigManager.getConfigs()
            .environments.then((response) => {
                this.setState({
                    environments: response.data.environments,
                });
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });

        const storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex) });
            let nightMode = false;
            if (parseInt(storedThemeIndex) === 1) {
                nightMode = true;
            }
            this.setState({ nightMode });
        }
    }

    handleRequestCloseUserMenu = () => {
        this.setState({ openUserMenu: false });
    };

    handleEnvironmentChange = (event) => {
        this.setState({ openEnvironmentMenu: false });
        // TODO: [rnk] Optimize Rendering.
        const environmentId = parseInt(event.target.id);
        Utils.setEnvironment(this.state.environments[environmentId]);
        this.setState({ environmentId });
    };

    /**
     * Do OIDC logout redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doOIDCLogout = (e) => {
        e.preventDefault();
        window.location = app.context + '/services/logout';
    };

    handleClickButton = (key) => {
        this.setState({
            [key]: true,
            anchorEl: findDOMNode(this.button),
        });
    };

    handleRequestClose = (key) => {
        this.setState({
            [key]: false,
        });
    };

    toggleGlobalNavBar(event) {
        this.setState({ openNavBar: !this.state.openNavBar });
    }

    handleToggleUserMenu = () => {
        this.setState((state) => ({ openUserMenu: !state.openUserMenu }));
    };

    handleCloseUserMenu = (event) => {
        if (this.anchorEl.contains(event.target)) {
            return;
        }

        this.setState({ openUserMenu: false });
    };

    getLogoPath = () => {
        const settingsContext = this.context;
        const { tenantDomain } = settingsContext;
        const { theme } = this.props;
        const { custom: { appBar: { logo } } } = theme;
        let logoWithTenant = logo;
        if (logo && logoWithTenant) {
            logoWithTenant = logo.replace('<tenant-domain>', tenantDomain);
        }
        if (logoWithTenant && /^(ftp|http|https):\/\/[^ "]+$/.test(logoWithTenant)) {
            return logoWithTenant;
        } else {
            return app.context + logoWithTenant;
        }
    };

    /**
     * @inheritdoc
     * @returns {Component}
     * @memberof Layout
     */
    render() {
        const { classes, theme, children } = this.props;
        const {
            custom: {
                banner: {
                    style, text, image, active,
                },
                footer: {
                    active: footerActive, text: footerText,
                },
            },
        } = theme;
        const { openNavBar, openUserMenu, environments } = this.state;
        const { tenantDomain, setTenantDomain } = this.context;
        const user = AuthManager.getUser();
        // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
        const commonStyle = {
            style: { top: 64 },
        };
        const paperStyles = {
            style: {
                backgroundColor: theme.palette.background.drawer,
                top: 64,
            },
        };
        return (
            <>
                {active && (
                    <div className={classes.banner}>
                        {style === 'text' ? text
                            : (
                                <img
                                    alt={(
                                        <FormattedMessage
                                            id='Base.index.banner.alt'
                                            defaultMessage='Dev Portal Banner'
                                        />
                                    )}
                                    src={`${app.context}/${image}`}
                                />
                            )}
                    </div>
                )}
                <div className={classes.reactRoot} id='pageRoot'>
                    <div className={classes.wrapper}>
                        <AppBar position='fixed' className={classes.appBar} id='appBar'>
                            <Toolbar className={classes.toolbar} id='toolBar'>
                                <Hidden mdUp>
                                    <IconButton onClick={this.toggleGlobalNavBar} color='inherit'>
                                        <Icon className={classes.menuIcon}>menu</Icon>
                                    </IconButton>
                                </Hidden>
                                <Link to='/' id='logoLink'>
                                    <img
                                        alt={(
                                            <FormattedMessage
                                                id='Base.index.logo.alt'
                                                defaultMessage='Dev Portal'
                                            />
                                        )}
                                        src={this.getLogoPath()}
                                        style={{
                                            height: theme.custom.appBar.logoHeight,
                                            width: theme.custom.appBar.logoWidth,
                                        }}
                                    />
                                </Link>
                                <Hidden smDown>
                                    <VerticalDivider height={32} />
                                    <div className={classes.listInline}>
                                        <GlobalNavBar smallView />
                                    </div>
                                </Hidden>
                                <Hidden mdUp>
                                    <Drawer
                                        className={classes.drawerStyles}
                                        PaperProps={paperStyles}
                                        SlideProps={commonStyle}
                                        ModalProps={commonStyle}
                                        BackdropProps={commonStyle}
                                        open={openNavBar}
                                        onClose={this.toggleGlobalNavBar}
                                    >
                                        <div
                                            tabIndex={0}
                                            role='button'
                                            onClick={this.toggleGlobalNavBar}
                                            onKeyDown={this.toggleGlobalNavBar}
                                        >
                                            <div className={classes.list}>
                                                <GlobalNavBar smallView={false} />
                                            </div>
                                        </div>
                                    </Drawer>
                                </Hidden>
                                <VerticalDivider height={32} />
                                <HeaderSearch id='headerSearch' />
                                {tenantDomain && tenantDomain !== 'INVALID' && (
                                    <Link
                                        style={{
                                            textDecoration: 'none',
                                            color: '#ffffff',
                                        }}
                                        to='/'
                                        onClick={() => setTenantDomain('INVALID')}
                                        id='gotoPubulicDevPortal'
                                    >
                                        <Button className={classes.publicStore}>
                                            <Icon className={classes.icons}>public</Icon>
                                            <Hidden mdDown>
                                                <FormattedMessage
                                                    id='Base.index.go.to.public.store'
                                                    defaultMessage='Go to public Dev Portal'
                                                />
                                            </Hidden>
                                        </Button>
                                    </Link>
                                )}
                                <VerticalDivider height={72} />
                                {/* Environment menu */}
                                <EnvironmentMenu
                                    environments={environments}
                                    environmentLabel={Utils.getEnvironment().label}
                                    handleEnvironmentChange={this.handleEnvironmentChange}
                                    id='environmentMenu'
                                />
                                {user ? (
                                    <>
                                        <Link to='/settings' id='settingsLink'>
                                            <Button className={classes.userLink}>
                                                <Icon className={classes.icons}>settings</Icon>
                                                <Hidden mdDown>
                                                    <FormattedMessage
                                                        id='Base.index.settings.caption'
                                                        defaultMessage='Settings'
                                                    />
                                                </Hidden>
                                            </Button>
                                        </Link>
                                        <Button
                                            buttonRef={(node) => {
                                                this.anchorEl = node;
                                            }}
                                            aria-owns={open ? 'menu-list-grow' : null}
                                            aria-haspopup='true'
                                            onClick={this.handleToggleUserMenu}
                                            className={classes.userLink}
                                            id='userToggleButton'
                                        >
                                            <AccountCircle className={classes.icons} />
                                            {user.name}
                                            <Icon style={{ fontSize: '22px', marginLeft: '1px' }}>
                                                keyboard_arrow_down
                                            </Icon>
                                        </Button>
                                        <Popper
                                            id='userPopup'
                                            open={openUserMenu}
                                            anchorEl={this.anchorEl}
                                            transition
                                            disablePortal
                                            anchorOrigin={{
                                                vertical: 'bottom',
                                                horizontal: 'center',
                                            }}
                                            transformOrigin={{
                                                vertical: 'top',
                                                horizontal: 'center',
                                            }}
                                        >
                                            {({ TransitionProps, placement }) => (
                                                <Grow
                                                    {...TransitionProps}
                                                    id='menu-list-grow'
                                                    style={{
                                                        transformOrigin:
                                                            placement === 'bottom' ? 'center top' : 'center bottom',
                                                    }}
                                                >
                                                    <Paper>
                                                        <ClickAwayListener onClickAway={this.handleCloseUserMenu}>
                                                            <MenuList>
                                                                <MenuItem onClick={this.doOIDCLogout}>
                                                                    <FormattedMessage
                                                                        id='Base.index.logout'
                                                                        defaultMessage='Logout'
                                                                    />
                                                                </MenuItem>
                                                            </MenuList>
                                                        </ClickAwayListener>
                                                    </Paper>
                                                </Grow>
                                            )}
                                        </Popper>
                                    </>
                                ) : (
                                    <>
                                        {/* TODO: uncomment when the feature is working */}
                                        {/* <Link to={'/sign-up'}>
                                     <Button className={classes.userLink}>
                                     <HowToReg /> sign-up
                                     </Button>
                                     </Link> */}
                                        <a href={app.context + '/services/configs'}>
                                            <Button className={classes.userLink}>
                                                <Icon>person</Icon>
                                                <FormattedMessage id='Base.index.sign.in' defaultMessage=' Sign-in' />
                                            </Button>
                                        </a>
                                    </>
                                )}
                            </Toolbar>
                        </AppBar>
                        <div className={classes.contentWrapper}>{children}</div>
                        {footerActive && <div className={classes.push} />}
                    </div>
                    {footerActive && (
                        <footer className={classes.footer} id='footer'>
                            <Typography noWrap>
                                {footerText && footerText !== '' ? <span>{footerText}</span> : (
                                    <FormattedMessage
                                        id='Base.index.copyright.text'
                                        defaultMessage='WSO2 API-M v3.1.0 | © 2020 WSO2 Inc'
                                    />
                                )}
                            </Typography>
                        </footer>
                    )}
                </div>
            </>
        );
    }
}

Layout.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(Layout);
