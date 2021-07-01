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
import { Link, withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import Hidden from '@material-ui/core/Hidden';
import {
    MenuItem, MenuList,
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
import Settings, { useSettingsContext } from 'AppComponents/Shared/SettingsContext';
import { app } from 'Settings';
import ReactSafeHtml from 'react-safe-html';
import AuthManager from '../../data/AuthManager';
import LanuageSelector from './Header/LanuageSelector';
import GlobalNavBar from './Header/GlobalNavbar';
import VerticalDivider from '../Shared/VerticalDivider';

const styles = (theme) => {
    const pageMaxWidth = theme.custom.page.style === 'fluid' ? 'none' : theme.custom.page.width;
    const footerHeight = theme.custom.footer.active ? theme.custom.footer.height: 0;
    return {
        appBar: {
            position: 'fixed',
            backgroundColor: theme.custom.appBar.background,
            backgroundImage: `url(${app.context}${theme.custom.appBar.backgroundImage})`,
            backgroundRepeat: 'no-repeat',
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
            minWidth: 'auto',
        },
        linkWrapper: {
            display: 'flex',
            marginLeft: 'auto',
        },
        // Page layout styles
        drawer: {
            top: 64,
        },
        wrapper: {
            minHeight: '100%',
            marginBottom: theme.custom.footer.active ? -1 * (theme.custom.footer.height || 50): 0,
            background: theme.palette.background.default + ' url(' + app.context + theme.custom.backgroundImage + ') repeat left top',
        },
        contentWrapper: {
            display: 'flex',
            flexDirection: 'row',
            overflowY: 'auto',
            overflowX: 'hidden',
            position: 'relative',
            minHeight: `calc(100vh - ${64 + footerHeight}px)`,
            marginLeft: -4,
            marginTop: 64,
        },
        push: {
            height: footerHeight || 50,
        },
        footer: {
            background: theme.custom.footer.background,
            color: theme.custom.footer.color,
            paddingLeft: theme.spacing(3),
            height: footerHeight || 50,
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
            '&.material-icons': {
                fontSize: theme.spacing(2),
            }
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
        listRoot: {
            padding: 0,
        },
        listRootInline: {
            padding: 0,
            display: 'flex',
        },
        listItemTextRoot: {
            padding: 0,
        },
        listText: {
            color: theme.palette.getContrastText(theme.custom.appBar.background),
        },
        listTextSmall: {
            color: theme.palette.getContrastText(theme.custom.appBar.background),
        },
        smallIcon: {
            marginRight: 5,
            minWidth: 'auto',
        },
        links: {
            display: 'flex',
            justifyContent: 'center',
        },
        selected: {
            background: theme.custom.appBar.activeBackground,
            alignItems: 'center',
            textDecoration: 'none',
            color: theme.palette.getContrastText(theme.custom.appBar.activeBackground),
        },
        selectedText: {
            color: theme.palette.getContrastText(theme.custom.appBar.activeBackground),
        },
        triangleDown: {
            width: 0,
            height: 0,
            borderLeft: '6px solid transparent',
            borderRight: '6px solid transparent',
            borderTop: `6px solid ${theme.custom.appBar.activeBackground}`,
            fontSize: 0,
            lineHeight: 0,
            position: 'absolute',
            bottom: -5,
        },
        listIconRoot: {
            minWidth: 'auto',
        },
        listItemRoot: {
            padding: `0 ${theme.spacing(1)}px 0 ${theme.spacing(1)}px `,
            height: 30,
        },
        logoutLink: {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        }
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
        const { history } = props;
        history.listen((location) => {
            this.ditectCurrentMenu();
        });
    }

    state = {
        nightMode: false,
        themeIndex: 0,
        left: false,
        openNavBar: false,
        openUserMenu: false,
        selected: 'home',
    };
    ditectCurrentMenu = () => {
        // We can't use history location since with logout redirection, it behaves strangely.
        const pathname = window.location.pathname;
        if (/\/apis$/g.test(pathname) || /\/apis\//g.test(pathname)) {
            this.setState({ selected: 'apis' });
        } else if (/\/home$/g.test(pathname) || /\/home\//g.test(pathname)) {
            this.setState({ selected: 'home' });
        } else if (/\/applications$/g.test(pathname) || /\/applications\//g.test(pathname)) {
            this.setState({ selected: 'applications' });
        } else if (/\/settings$/g.test(pathname) || /\/settings\//g.test(pathname)) {
            this.setState({ selected: 'settings' });
        }
    };
    componentWillMount() {
        const { theme } = this.props;
        document.body.style.backgroundColor = theme.custom.page.emptyAreadBackground || '#ffffff';
    }

    componentDidMount() {
        const { history: { location } } = this.props;
        this.ditectCurrentMenu(location);
    }

    handleRequestCloseUserMenu = () => {
        this.setState({ openUserMenu: false });
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

    getPasswordChangeEnabled = () => {
        const { settings: { IsPasswordChangeEnabled } } = useSettingsContext();
        return IsPasswordChangeEnabled;
    };

    /**
     * @inheritdoc
     * @returns {Component}
     * @memberof Layout
     */
    render() {
        const { classes, theme, children, intl } = this.props;
        const {
            custom: {
                banner: {
                    style, text, image, active,
                },
                appBar: {
                    showSearch,
                },
                footer: {
                    active: footerActive, text: footerText, footerHTML,
                },
                languageSwitch: { active: languageSwitchActive },
                publicTenantStore,
            },
        } = theme;
        const { openNavBar, selected } = this.state;
        const { tenantDomain, setTenantDomain } = this.context;
        const { customUrl: { tenantDomain: customUrlEnabledDomain } } = app;

        const user = AuthManager.getUser();
        // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
        const commonStyle = {
            style: { top: 64 },
        };
        const paperStyles = {
            style: {
                top: 64,
                backgroundColor: theme.custom.appBar.background,
            },
        };

        const strokeColor = theme.palette.getContrastText(theme.custom.appBar.background);
        const strokeColorSelected = theme.palette.getContrastText(theme.custom.appBar.activeBackground);

        let publicTenantStoreVisible = true;

        if (publicTenantStore) {
            const { active: publicTenantStoreActive } = publicTenantStore;
            publicTenantStoreVisible = publicTenantStoreActive;
        }
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
                                <Link to='/' id='logoLink' aria-label='Go to home page'>
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
                                        <GlobalNavBar
                                            selected={selected}
                                            drawerView={false}
                                            iconWidth={16}
                                            strokeColor={strokeColor}
                                            strokeColorSelected={strokeColorSelected}
                                            classes={classes}
                                        />
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
                                                <GlobalNavBar
                                                    selected={selected}
                                                    drawerView
                                                    iconWidth={24}
                                                    strokeColor={strokeColor}
                                                    strokeColorSelected={strokeColorSelected}
                                                    classes={classes}
                                                />
                                            </div>
                                        </div>
                                    </Drawer>
                                </Hidden>
                                <VerticalDivider height={32} />
                                {showSearch && (<HeaderSearch id='headerSearch' />)}
                                {tenantDomain && customUrlEnabledDomain === 'null' && tenantDomain !== 'INVALID'
                                    && publicTenantStoreVisible && (
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
                                <VerticalDivider height={64} />
                                {languageSwitchActive && <LanuageSelector />}
                                {user ? (
                                    <>
                                        <div className={classes.linkWrapper}>
                                            <Button
                                                buttonRef={(node) => {
                                                    this.anchorEl = node;
                                                }}
                                                aria-owns={this.openUserMenu ? 'menu-list-grow' : null}
                                                aria-haspopup='true'
                                                onClick={this.handleToggleUserMenu}
                                                className={classes.userLink}
                                                id='userToggleButton'
                                            >
                                                <Icon className={classes.icons}>person</Icon>
                                                {user.name}
                                            </Button>
                                            <Popper
                                                id='userPopup'
                                                open={this.state.openUserMenu}
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
                                                placement='bottom-end'
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
                                                                    <MenuItem className={classes.logoutLink}>
                                                                        <Link
                                                                            to='/settings/manage-alerts'
                                                                            onClick={this.handleCloseUserMenu}
                                                                        >
                                                                            <FormattedMessage
                                                                                id='Base.index.settingsMenu.alertConfiguration'
                                                                                defaultMessage='Configure Alerts'
                                                                            />
                                                                        </Link>
                                                                    </MenuItem>
                                                                    {this.getPasswordChangeEnabled() ?
                                                                        <MenuItem className={classes.logoutLink}>
                                                                            <Link
                                                                                to={'/settings/change-password/'}
                                                                                onClick={this.handleCloseUserMenu}
                                                                            >
                                                                                <FormattedMessage
                                                                                    id='Base.index.settingsMenu.changePassword'
                                                                                    defaultMessage='Change Password'
                                                                                />
                                                                            </Link>
                                                                        </MenuItem> :
                                                                        null
                                                                    }
                                                                    <MenuItem onClick={this.doOIDCLogout} className={classes.logoutLink}>
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
                                        </div>
                                    </>
                                ) : (
                                        <div className={classes.linkWrapper}>
                                            <a href={app.context + '/services/configs'}>
                                                <Button className={classes.userLink}>
                                                    <Icon>person</Icon>
                                                    <FormattedMessage id='Base.index.sign.in' defaultMessage=' Sign-in' />
                                                </Button>
                                            </a>
                                        </div>
                                    )}
                            </Toolbar>
                        </AppBar>
                        <div className={classes.contentWrapper}>{children}</div>
                        {footerActive && <div className={classes.push} />}
                    </div>
                    {footerActive && (
                        <footer className={classes.footer} id='footer'>
                            {footerHTML && footerHTML !== '' ? (
                                <>
                                    {<ReactSafeHtml html={footerHTML} />}
                                </>
                            ) : (<Typography noWrap>
                                {footerText && footerText !== '' ? <span>{footerText}</span> : (
                                    <FormattedMessage
                                        id='Base.index.copyright.text'
                                        defaultMessage='WSO2 API-M v3.2.0 | © 2020 WSO2 Inc'
                                    />
                                )}
                            </Typography>)}
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

export default injectIntl(withRouter(withStyles(styles, { withTheme: true })(Layout)));