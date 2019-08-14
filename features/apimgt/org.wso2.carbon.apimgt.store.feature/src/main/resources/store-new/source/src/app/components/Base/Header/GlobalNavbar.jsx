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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import classNames from 'classnames';
import Icon from '@material-ui/core/Icon';
import {
    ListItemIcon, List, withStyles, ListItem, ListItemText,
} from '@material-ui/core';
import CustomIcon from '../../Shared/CustomIcon';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
    small: {
        padding: 0,
    },
    smallIcon: {
        marginRight: 5,
    },
});
/**
 * GlobalNavBar
 *
 * @param {*} props Properties
 * @returns {React.Component}
 */
function GlobalNavBar(props) {
    const {
        classes, theme, intl, smallView,
    } = props;
    let iconWidth = 32;
    if (smallView) {
        iconWidth = 16;
    }
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const linkTextClasses = classNames({ [classes.small]: smallView });
    return (
        <List>
            {theme.custom.landingPage.active
                && (
                    <Link to='/home'>
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
                                classes={{ primary: classes.listText, root: linkTextClasses }}
                                primary={intl.formatMessage({
                                    id: 'Base.Generic.GlobalNavbar.menu.home',
                                    defaultMessage: 'Home',
                                })}
                            />
                        </ListItem>
                    </Link>
                ) }
            <Link to='/apis'>
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
                        classes={{ primary: classes.listText, root: linkTextClasses }}
                        primary={intl.formatMessage({
                            id: 'Base.Generic.GlobalNavbar.menu.apis',
                            defaultMessage: 'APIs',
                        })}
                    />
                </ListItem>
            </Link>
            <Link to='/api-products'>
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
                        classes={{ primary: classes.listText, root: linkTextClasses }}
                        primary={intl.formatMessage({
                            id: 'Base.Generic.GlobalNavbar.menu.apiproducts',
                            defaultMessage: 'API Products',
                        })}
                    />
                </ListItem>
            </Link>
            <Link to='/applications'>
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
                        classes={{ primary: classes.listText, root: linkTextClasses }}
                        primary={intl.formatMessage({
                            id: 'Base.Generic.GlobalNavbar.menu.applications',
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

export default injectIntl(withStyles(styles, { withTheme: true })(GlobalNavBar));
