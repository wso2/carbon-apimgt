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
import React, { useState, useEffect } from 'react';
import {
    ListItemIcon, List, withStyles, ListItem, ListItemText,
} from '@material-ui/core';
import ScopesIcon from '@material-ui/icons/VpnKey';
import PropTypes from 'prop-types';
import { Link, withRouter } from 'react-router-dom';
import NotificationsIcon from '@material-ui/icons/Notifications';
import { FormattedMessage } from 'react-intl';
import classNames from 'classnames';
import clsx from 'clsx';
import AuthManager from 'AppData/AuthManager';
import DnsIcon from '@material-ui/icons/Dns';

const styles = (theme) => ({

    categoryHeader: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    categoryHeaderPrimary: {
        color: theme.palette.common.white,
        fontSize: 16,
        fontWeight: 200,
    },
    item: {
        paddingTop: 1,
        paddingBottom: 1,
        color: 'rgba(255, 255, 255, 0.7)',
        '&:hover,&:focus': {
            backgroundColor: 'rgba(255, 255, 255, 0.08)',
        },
    },
    itemActiveItem: {
        color: '#4fc3f7',
        '& svg': {
            color: '#4fc3f7',
        },
    },
    itemPrimary: {
        fontSize: 12,
    },
    itemIcon: {
        minWidth: 'auto',
        marginRight: theme.spacing(2),
        color: '#fff',
    },
    divider: {
        border: 'solid 1px #555',
        marginTop: theme.spacing(2),
    },
    listRoot: {
        padding: 0,
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
    listTextSmall: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    links: {
        display: 'flex',
        height: 63,
    },
    selected: {
        background: theme.palette.background.activeMenuItem,
        alignItems: 'center',
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.background.activeMenuItem),
    },
    selectedText: {
        color: theme.palette.getContrastText(theme.palette.background.activeMenuItem),
    },
});

/**
 *
 *
 * @param {*} props
 * @returns
 */
function GlobalNavLinks(props) {
    const [selected, setSelected] = useState('apis');
    const {
        classes, smallView, history,
    } = props;

    const publisherUser = !AuthManager.isNotPublisher();
    const ditectCurrentMenu = (location) => {
        const { pathname } = location;
        if (/\/apis$/g.test(pathname) || /\/apis\//g.test(pathname)) {
            setSelected('apis');
        } else if (/\/api-products$/g.test(pathname) || /\/api-products\//g.test(pathname)) {
            setSelected('api-products');
        } else if (/\/scopes$/g.test(pathname) || /\/scopes\//g.test(pathname)) {
            setSelected('scopes');
        }
    };
    useEffect(() => {
        const { location } = history;
        ditectCurrentMenu(location);
    }, []);
    history.listen((location) => {
        ditectCurrentMenu(location);
    });
    return (
        <List className={classes.listRoot}>
            <ListItem className={classes.categoryHeader}>
                <ListItemText
                    classes={{
                        primary: classes.categoryHeaderPrimary,
                    }}
                >
                    <FormattedMessage id='Base.Header.navbar.GlobalNavBar.apis' defaultMessage='APIs' />
                </ListItemText>
            </ListItem>
            <Link to='/apis'>
                <ListItem
                    button
                    className={clsx(classes.item, selected === 'apis' && classes.itemActiveItem)}
                >
                    <ListItemIcon className={clsx(selected === 'apis' && classes.itemActiveItem, classes.itemIcon)}>
                        <DnsIcon />
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            primary: classes.itemPrimary,
                        }}
                    >
                        <FormattedMessage id='Base.Header.navbar.GlobalNavBar.apis.list' defaultMessage='List' />
                    </ListItemText>
                </ListItem>
            </Link>
            <Link
                to='/scopes'
                className={classNames({ [classes.selected]: selected === 'scopes', [classes.links]: true })}
            >
                <ListItem button>
                    <ListItemIcon classes={{ root: classNames({ [classes.smallIcon]: smallView }) }}>
                        <ScopesIcon />
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            primary: classes.itemPrimary,
                        }}
                    >
                        <FormattedMessage
                            id='Base.Header.navbar.GlobalNavBar.endpoint.registry'
                            defaultMessage='Endpoint Registry'
                        />
                    </ListItemText>
                </ListItem>
            </Link>
            <hr className={classes.divider} />
            { publisherUser
            && (
                <>
                    <ListItem className={classes.categoryHeader}>
                        <ListItemText
                            classes={{
                                primary: classes.categoryHeaderPrimary,
                            }}
                        >
                            <FormattedMessage
                                id='Base.Header.navbar.GlobalNavBar.api.products'
                                defaultMessage='API Products'
                            />
                        </ListItemText>
                    </ListItem>
                    <Link
                        to='/api-products'
                    >
                        <ListItem
                            button
                            className={clsx(classes.item, selected === 'api-products' && classes.itemActiveItem)}
                        >
                            <ListItemIcon className={clsx(selected === 'api-products'
                            && classes.itemActiveItem, classes.itemIcon)}
                            >
                                <DnsIcon />
                            </ListItemIcon>
                            <ListItemText
                                classes={{
                                    primary: classNames({
                                        [classes.selectedText]: selected === 'api-products',
                                        [classes.listText]: selected !== 'api-products' && !smallView,
                                        [classes.listTextSmall]: selected !== 'api-products' && smallView,
                                    }),
                                }}
                                primary={(
                                    <FormattedMessage
                                        id='Base.Header.navbar.GlobalNavBar.api.products.list'
                                        defaultMessage='List'
                                    />
                                )}
                            />
                        </ListItem>
                    </Link>
                    <hr className={classes.divider} />
                </>
            )}
            <ListItem className={classes.categoryHeader}>
                <ListItemText
                    classes={{
                        primary: classes.categoryHeaderPrimary,
                    }}
                >
                    <FormattedMessage
                        id='Base.Header.navbar.GlobalNavBar.settings'
                        defaultMessage='Settings'
                    />
                </ListItemText>
            </ListItem>
            <Link to='/settings'>
                <ListItem
                    button
                    className={clsx(classes.item, selected === 'settings' && classes.itemActiveItem)}
                >
                    <ListItemIcon className={clsx(selected === 'settings' && classes.itemActiveItem, classes.itemIcon)}>
                        <NotificationsIcon />
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            primary: classes.itemPrimary,
                        }}
                    >
                        <FormattedMessage
                            id='Base.Header.navbar.GlobalNavBar.manage.alerts'
                            defaultMessage='Manage Alerts'
                        />
                    </ListItemText>
                </ListItem>
            </Link>
            <hr className={classes.divider} />
        </List>
    );
}
GlobalNavLinks.propTypes = {
    classes: PropTypes.shape({
        drawerStyles: PropTypes.string,
        list: PropTypes.string,
        listText: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        palette: PropTypes.shape({
            getContrastText: PropTypes.func,
            background: PropTypes.shape({
                drawer: PropTypes.string,
                leftMenu: PropTypes.string,
            }),
        }),
    }).isRequired,
    history: PropTypes.func.isRequired,
    smallView: PropTypes.bool.isRequired,
};

export default withRouter(withStyles(styles, { withTheme: true })(GlobalNavLinks));
