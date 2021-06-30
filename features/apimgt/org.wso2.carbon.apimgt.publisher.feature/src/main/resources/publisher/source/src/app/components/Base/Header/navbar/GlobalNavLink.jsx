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
import {
    ListItemIcon, withStyles, ListItem, ListItemText, useTheme,
} from '@material-ui/core';
import clsx from 'clsx';
import PropTypes from 'prop-types';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import classNames from 'classnames';
import Tooltip from '@material-ui/core/Tooltip';

const styles = (theme) => ({
    listRoot: {
        padding: 0,
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
    smallIcon: {
        marginRight: 5,
        minWidth: 'auto',
    },
    selected: {
        background: '#868686b5',
        borderLeft: '2px solid',
        color: '#f9f9f9',
    },
    notSelected: {
        borderLeft: '2px solid',
        color: '#18202c',
    },
    listHover: {
        paddingBottom: theme.spacing(2),
        paddingTop: theme.spacing(2),
        '&:hover': {
            backgroundColor: '#b3b3b373',
        },
    },
    selectedText: {
        color: theme.palette.getContrastText(theme.palette.background.activeMenuItem),
    },
    scopeIconColor: {
        fill: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    alertIconColor: {
        fill: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    divider: {
        marginTop: theme.spacing(1),
        backgroundColor: theme.palette.background.divider,
    },
    categoryHeader: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        '& svg': {
            color: theme.palette.common.white,
        },
    },
    categoryHeaderPrimary: {
        color: theme.palette.common.white,
    },
    itemIcon: {
        minWidth: 'auto',
        marginRight: theme.spacing(2),
    },
    arrow: {
        color: theme.palette.common.black,
    },
    tooltip: {
        backgroundColor: theme.palette.common.black,
        ...theme.typography.body2,
    },
});

/**
 *
 *
 * @param {*} props
 * @returns
 */
function GlobalNavLinks(props) {
    const {
        classes, active, title, children, to, type, icon, isExternalLink,
    } = props;
    const theme = useTheme();
    let tooltipTitle = title;
    if (!title) {
        tooltipTitle = children;
    }

    const iconWidth = 25;
    return (
        <Link
            underline='none'
            component={!isExternalLink && RouterLink}
            to={!isExternalLink && to}
        >
            <Tooltip
                classes={{ arrow: classes.arrow, tooltip: classes.tooltip }}
                PopperProps={{
                    popperOptions: {
                        modifiers: {
                            flip: {
                                enabled: false,
                            },
                            offset: {
                                offset: '0,0',
                            },
                        },
                    },
                }}
                arrow
                interactive
                title={tooltipTitle}
                placement='right'
            >
                <ListItem
                    className={clsx(classes.listHover, {
                        [classes.selected]: active,
                        [classes.notSelected]: !active,
                    })}
                    button
                >
                    <ListItemIcon
                        className={classes.itemIcon}
                    >
                        {icon || (
                            <CustomIcon
                                width={iconWidth}
                                strokeColor={active ? theme.custom.globalNavBar.active : '#f2f2f2'}
                                height={iconWidth}
                                icon={type}
                            />
                        )}
                    </ListItemIcon>
                    <ListItemText
                        classes={{
                            primary: classNames({
                                [classes.selectedText]: active,
                                [classes.listText]: !active,
                            }),
                        }}
                        primary={children}
                    />
                </ListItem>
            </Tooltip>
        </Link>
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
};

export default withStyles(styles, { withTheme: true })(GlobalNavLinks);
