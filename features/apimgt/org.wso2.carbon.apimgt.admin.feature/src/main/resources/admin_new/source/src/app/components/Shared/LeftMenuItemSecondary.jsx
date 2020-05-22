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

import React, { useEffect, useState } from 'react';
import { Link, withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

const styles = (theme) => ({
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        textTransform: theme.custom.leftMenuTextStyle,
        width: '100%',
        textAlign: 'left',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
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
        paddingBottom: theme.spacing(1),
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(1),
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
});

/**
 *
 *
 * @param {*} props
 * @returns
 */
function LeftMenuItemSecondary(props) {
    const [selected, setSelected] = useState(false);

    const {
        classes, theme, Icon, to, history, text,
    } = props;
    const routeToCheck = text.toLowerCase();
    const { leftMenu } = theme.custom;
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const iconSize = theme.custom.leftMenuIconSize;
    const ditectCurrentMenu = (location) => {
        const { pathname } = location;
        const test1 = new RegExp('/' + routeToCheck + '$', 'g');
        const test2 = new RegExp('/' + routeToCheck + '/', 'g');
        if (pathname.match(test1) || pathname.match(test2)) {
            setSelected(true);
        } else {
            setSelected(false);
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
        <Link
            className={classNames(
                classes.leftLInk,
                {
                    [classes.leftLink_IconLeft]: leftMenu === 'icon left',
                },
                'leftLInk',
            )}
            to={to}
            style={{ backgroundColor: selected ? theme.palette.background.appBarSelected : '' }}
        >
            {
                // If the icon pro ( which is comming from the React Material library )
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
                            icon={text}
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
        </Link>
    );
}
LeftMenuItemSecondary.defaultProps = {
    route: null,
};
LeftMenuItemSecondary.propTypes = {
    classes: PropTypes.shape({
        divider: PropTypes.string,
        leftLInk: PropTypes.string,
        leftLink_IconLeft: PropTypes.string,
        noIcon: PropTypes.string,
        leftLink_Icon: PropTypes.string,
        leftLInkText: PropTypes.string,
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
    route: PropTypes.string,
    history: PropTypes.shape({
        listen: PropTypes.func.isRequired,
        location: PropTypes.string.isRequired,
    }).isRequired,
};
export default withRouter(withStyles(styles, { withTheme: true })(LeftMenuItemSecondary));
