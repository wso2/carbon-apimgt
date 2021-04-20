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

import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { useTheme } from '@material-ui/styles';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

const useStyles = makeStyles((theme) => ({
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        textTransform: theme.custom.leftMenuTextStyle,
        width: '100%',
        textAlign: 'left',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        fontSize: theme.typography.body1.fontSize,
        fontWeight: 250,
        whiteSpace: 'nowrap',
    },
    leftLInkTextHead: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        textTransform: theme.custom.leftMenuTextStyle,
        width: '100%',
        textAlign: 'left',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        fontWeight: 800,
        whiteSpace: 'nowrap',
    },
    leftLInkText_IconLeft: {
        paddingLeft: 10,
    },
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
    leftLInk: {
        paddingTop: theme.spacing(1),
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        height: '18px',
        fontSize: theme.typography.caption.fontSize,
        cursor: 'pointer',
        textDecoration: 'none',
    },
    leftLInkOverview: {
        paddingTop: theme.spacing(1),
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        height: '18px',
        fontSize: theme.typography.caption.fontSize,
        cursor: 'pointer',
        textDecoration: 'none',
    },
    leftLink_Icon: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        fontSize: theme.custom.leftMenuIconSize + 'px',
    },
    leftLink_IconLeft: {
        display: 'flex',
        alignItems: 'center',
    },
    noIcon: {
        display: 'none',
    },
    leftLInkText_NoText: {
        diplay: 'none',
    },
    selectedMenu: {
        backgroundColor: theme.palette.background.appBarSelected,
    },

}));

/**
 * NOTE: Incase displaying menu text is not equal to associated path segment should use `route` prop
 *
 * @param {*} props
 * @returns
 */
function LeftMenuItem(props) {
    const {
        Icon, to, text, route, head,
    } = props;
    const classes = useStyles();
    const theme = useTheme();
    const { leftMenu } = theme.custom;
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const iconSize = theme.custom.leftMenuIconSize;
    const { pathname } = useLocation();
    // Incase displaying menu text is not equal to associated path segment should use `route` prop
    const routeToCheck = route || text;
    const menuPathPattern = new RegExp('/' + routeToCheck + '($|/)', 'g');
    const isSelected = pathname.match(menuPathPattern);
    return (
        <Link
            className={classNames(
                head !== 'valueOnly' ? (
                    classes.leftLInk) : (classes.leftLInkOverview),
                {
                    [classes.leftLink_IconLeft]: leftMenu === 'icon left',
                },
                'leftLInk',
                isSelected && classes.selectedMenu,
            )}
            to={to}
        >
            {// If the icon pro ( which is coming from the React Material library )
                // is coming we add css class and render.
                // If leftMenu='no icon' at the theme object we hide the icon. Also we add static classes to
                // allow customers theme
                // the product without compiling.
                Icon ? (
                    React.cloneElement(Icon, {
                        className: classNames(
                            classes.leftLink_Icon,
                            {
                                [classes.noIcon]: leftMenu === 'no icon',
                            },
                            'leftLink_Icon',
                        ),
                    })
                ) : (
                // We can also add custom icons
                    <CustomIcon
                        strokeColor={strokeColor}
                        width={iconSize}
                        height={iconSize}
                        icon={props.iconText}
                        className={classNames(
                            classes.leftLInk,
                            {
                                [classes.noIcon]: leftMenu === 'no icon',
                            },
                            'leftLink_Icon',
                        )}
                    />
                )
            }
            {head === 'valueOnly' ? (
                <Typography
                    className={classNames(
                        classes.leftLInkTextHead,
                    )}
                >
                    {text}
                </Typography>
            )
                : (
                    <Typography
                        className={classNames(
                            classes.leftLInkText,
                            {
                                [classes.leftLInkText_IconLeft]: leftMenu === 'icon left',
                                [classes.leftLInkText_NoText]: leftMenu === 'no text',
                            },
                            'leftLInkText',
                        )}
                    >
                        {text}
                    </Typography>
                )}
        </Link>
    );
}
LeftMenuItem.defaultProps = {
    route: null,
    iconText: null,
};
LeftMenuItem.propTypes = {
    classes: PropTypes.shape({
        divider: PropTypes.string,
        leftLInk: PropTypes.string,
        leftLink_IconLeft: PropTypes.string,
        noIcon: PropTypes.string,
        leftLink_Icon: PropTypes.string,
        leftLInkText: PropTypes.string,
        leftLInkTextHead: PropTypes.string,
        leftLInkText_IconLeft: PropTypes.string,
        leftLInkText_NoText: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.shape({
            leftMenu: PropTypes.string,
            leftMenuIconSize: PropTypes.number,
        }),
        palette: PropTypes.shape({
            getContrastText: PropTypes.func,
            background: PropTypes.shape({
                leftMenu: PropTypes.string,
                appBar: PropTypes.string,
            }),
            leftMenu: PropTypes.string,
        }),
    }).isRequired,
    Icon: PropTypes.element.isRequired,
    text: PropTypes.string.isRequired,
    to: PropTypes.string.isRequired,
    head: PropTypes.string.isRequired,
    route: PropTypes.string,
    history: PropTypes.shape({
        listen: PropTypes.func.isRequired,
        location: PropTypes.string.isRequired,
    }).isRequired,
    iconText: PropTypes.string,
};
export default LeftMenuItem;
