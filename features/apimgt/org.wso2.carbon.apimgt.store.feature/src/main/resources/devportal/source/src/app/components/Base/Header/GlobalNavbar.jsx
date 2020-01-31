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
import React, { useEffect, useState } from 'react';
import { Link, withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import classNames from 'classnames';
import Icon from '@material-ui/core/Icon';
import { ListItemIcon, List, withStyles, ListItem, ListItemText } from '@material-ui/core';
import CustomIcon from '../../Shared/CustomIcon';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    listRoot: {
        padding: 0,
    },
    listItemTextRoot: {
        padding: 0,
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
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
});
/**
 * GlobalNavBar
 *
 * @param {*} props Properties
 * @returns {React.Component}
 */
function GlobalNavBar(props) {
    const [selected, setSelected] = useState('home');
    const {
        classes, theme, intl, smallView, history,
    } = props;
    const ditectCurrentMenu = (location) => {
        const { pathname } = location;
        if (/\/apis$/g.test(pathname) || /\/apis\//g.test(pathname)) {
            setSelected('apis');
        } else if (/\/api-products$/g.test(pathname) || /\/api-products\//g.test(pathname)) {
            setSelected('api-products');
        } else if (/\/home$/g.test(pathname) || /\/home\//g.test(pathname)) {
            setSelected('home');
        } else if (/\/applications$/g.test(pathname) || /\/applications\//g.test(pathname)) {
            setSelected('applications');
        }
    };
    useEffect(() => {
        const { location } = history;
        ditectCurrentMenu(location);
    }, []);
    history.listen((location) => {
        ditectCurrentMenu(location);
    });

    let strokeColor = theme.palette.getContrastText(theme.custom.leftMenu.background);
    let iconWidth = 32;
    if (smallView) {
        iconWidth = 16;
        strokeColor = theme.palette.getContrastText(theme.custom.appBar.background);
    }
    return (
        <List className={classes.listRoot}>
            {theme.custom.landingPage.active
                && (
                    <Link to='/home' className={classNames({ [classes.selected]: selected === 'home', [classes.links]: true })}>
                        <ListItem button>
                            <ListItemIcon classes={{ root: classNames({ [classes.smallIcon]: smallView }) }}>
                                <Icon
                                    style={{ fontSize: iconWidth, color: strokeColor }}
                                    className={classes.listText}
                                >
                                home
                                </Icon>
                            </ListItemIcon>
                            <ListItemText
                                classes={{
                                    root: classes.listItemTextRoot,
                                    primary: classNames({
                                        [classes.selectedText]: selected === 'home',
                                        [classes.listText]: selected !== 'home' && !smallView,
                                        [classes.listTextSmall]: selected !== 'home' && smallView,
                                    }),
                                }}
                                primary={intl.formatMessage({
                                    id: 'Base.Header.GlobalNavbar.menu.home',
                                    defaultMessage: 'Home',
                                })}
                            />
                        </ListItem>
                    </Link>
                ) }
            <Link
                to={(theme.custom.tagWise.active && theme.custom.tagWise.style === 'page') ? '/api-groups' : '/apis'}
                className={classNames({ [classes.selected]: selected === 'apis', [classes.links]: true })}
            >
                <ListItem button>
                    <ListItemIcon classes={{ root: classNames({ [classes.smallIcon]: smallView }) }}>
                        <CustomIcon
                            width={iconWidth}
                            height={iconWidth}
                            icon='api'
                            className={classes.listText}
                            strokeColor={strokeColor}
                        />
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            root: classes.listItemTextRoot,
                            primary: classNames({
                                [classes.selectedText]: selected === 'apis',
                                [classes.listText]: selected !== 'apis' && !smallView,
                                [classes.listTextSmall]: selected !== 'apis' && smallView,
                            }),
                        }}
                        primary={intl.formatMessage({
                            id: 'Base.Header.GlobalNavbar.menu.apis',
                            defaultMessage: 'APIs',
                        })}
                    />
                </ListItem>
            </Link>
            <Link to='/applications' className={classNames({ [classes.selected]: selected === 'applications', [classes.links]: true })}>
                <ListItem button>
                    <ListItemIcon classes={{ root: classNames({ [classes.smallIcon]: smallView }) }}>
                        <CustomIcon
                            width={iconWidth}
                            height={iconWidth}
                            icon='applications'
                            className={classes.listText}
                            strokeColor={strokeColor}
                        />
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            root: classes.listItemTextRoot,
                            primary: classNames({
                                [classes.selectedText]: selected === 'applications',
                                [classes.listText]: selected !== 'applications' && !smallView,
                                [classes.listTextSmall]: selected !== 'applications' && smallView,
                            }),
                        }}
                        primary={intl.formatMessage({
                            id: 'Base.Header.GlobalNavbar.menu.applications',
                            defaultMessage: 'Applications',
                        })}
                    />
                </ListItem>
            </Link>
        </List>
    );
}

GlobalNavBar.propTypes = {
    intl: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withRouter(injectIntl(withStyles(styles, { withTheme: true })(GlobalNavBar)));
