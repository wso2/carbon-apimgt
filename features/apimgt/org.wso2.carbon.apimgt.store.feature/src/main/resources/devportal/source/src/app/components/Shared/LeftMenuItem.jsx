/* eslint-disable react/prop-types */
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
import { withStyles, makeStyles } from '@material-ui/core/styles';
import Tooltip from '@material-ui/core/Tooltip';
import CustomIcon from './CustomIcon';

const useStylesBootstrap = makeStyles((theme) => ({
    arrow: {
        color: theme.palette.common.black,
    },
    tooltip: {
        backgroundColor: theme.palette.common.black,
    },
}));

function BootstrapTooltip(props) {
    const classes = useStylesBootstrap();

    return <Tooltip arrow classes={classes} {...props} />;
}
const styles = (theme) => ({
    leftLInkText: {
        color: theme.palette.getContrastText(theme.custom.leftMenu.background),
        textTransform: 'capitalize',
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
        backgroundColor: theme.custom.leftMenu.background,
        width: theme.custom.leftMenu.width,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInk: {
        paddingTop: theme.spacing(0.6),
        paddingBottom: theme.spacing(0.6),
        paddingLeft: theme.spacing(1),
        [theme.breakpoints.down('sm')]: {
            paddingLeft: 0,
        },
        paddingRight: 0,
        fontSize: theme.typography.caption.fontSize,
        cursor: 'pointer',
        textDecoration: 'none',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    leftLink_Icon: {
        color: theme.palette.getContrastText(theme.custom.leftMenu.background),
        fontSize: theme.custom.leftMenu.iconSize + 'px',
    },
    leftLink_IconLeft: {
        display: 'flex',
        alignItems: 'center',
    },
    noIcon: {
        display: 'none',
    },
    leftLInkText_NoText: {
        display: 'none',
    },
    leftLInkText_NoTextWhenSmall: {
        [theme.breakpoints.down('sm')]: {
            display: 'none !important',
        }
    },
    submenu: {
        paddingLeft: 12,
        [theme.breakpoints.down('sm')]: {
            paddingLeft: 0,
            color: theme.palette.grey[500],
        }
    },
});
/**
 * Renders the left menu section.
 * @param {JSON} props props passed from parent
 * @returns {JSX} Leftmenu element.
 */
function LeftMenuItem(props) {
    const [selected, setSelected] = useState(false);

    const {
        classes, theme, Icon, to, history, text, route, submenu, open
    } = props;
    const routeToCheck = route || text;
    const { leftMenu } = theme.custom;
    const strokeColor = theme.palette.getContrastText(leftMenu.background);
    const { iconSize } = leftMenu;
    const ditectCurrentMenu = (location = null) => {
        if (!location) {
            location = window.location;
        }
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
        ditectCurrentMenu();
    }, []);
    history.listen((location) => {
        ditectCurrentMenu(location);
    });
    let activeBackground = '';
    if (selected && !submenu) {
        activeBackground = leftMenu.leftMenuActive;
    } else if (selected && submenu) {
        activeBackground = leftMenu.leftMenuActiveSubmenu;
    }
    return (
        <BootstrapTooltip title={props.text} placement="right">
            <Link
                className={classNames(
                    classes.leftLInk,
                    {
                        [classes.leftLink_IconLeft]: leftMenu === 'icon left',
                        [classes.submenu]: submenu,
                    },
                    'leftLInk',
                )}
                to={to}
                style={{ backgroundColor: activeBackground }}
                title={text}
            >
                {
                    // If the icon pro ( which is coming from the React Material library )
                    // is coming we add css class and render.
                    // If leftMenu='no icon' at the theme object we hide the icon. Also we add static classes to
                    // allow customers theme
                    // the product without compiling.
                    Icon ? (
                        React.cloneElement(Icon, {
                            className: classNames(
                                classes.leftLink_Icon,
                                {
                                    [classes.noIcon]: leftMenu.style === 'no icon',
                                    [classes.submenu]: submenu,
                                },
                                'leftLink_Icon',
                            ),
                        })
                    ) : (
                            // We can also add custom icons
                            <CustomIcon
                                strokeColor={submenu ? '#cccccc' : strokeColor}
                                width={submenu ? iconSize - 10 : iconSize}
                                height={submenu ? iconSize - 10 : iconSize}
                                icon={props.iconText}
                                className={classNames(
                                    classes.leftLInk,
                                    {
                                        [classes.noIcon]: leftMenu.style === 'no icon',
                                    },
                                    'leftLink_Icon',
                                )}
                            />

                        )}
                {open && (
                    <Typography
                        className={classNames(
                            classes.leftLInkText,
                            {
                                [classes.leftLInkText_IconLeft]: leftMenu.style === 'icon left',
                                [classes.leftLInkText_NoText]: leftMenu.style === 'no text',
                            },
                            classes.leftLInkText_NoTextWhenSmall,
                            'leftLInkText',
                        )}
                    >
                        {props.text}
                    </Typography>
                )}
                {!open && (
                    <Typography
                        className={classNames(
                            {
                                [classes.leftLInkText_IconLeft]: leftMenu.style === 'icon left',
                            },
                            classes.leftLInkText_NoTextWhenSmall,
                            'leftLInkText',
                        )}
                    />
                )}


            </Link>
        </BootstrapTooltip>
    );
}
LeftMenuItem.defaultProps = {
    route: null,
    iconText: null,
    Icon: null,
    submenu: false,
};
LeftMenuItem.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    Icon: PropTypes.element,
    text: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.shape({}),
    ]).isRequired,
    to: PropTypes.string.isRequired,
    route: PropTypes.string,
    iconText: PropTypes.string,
    history: PropTypes.shape({
        location: PropTypes.shape({}).isRequired,
    }).isRequired,
    submenu: PropTypes.bool,
};
export default withRouter(withStyles(styles, { withTheme: true })(LeftMenuItem));
