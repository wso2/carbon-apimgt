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
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

const styles = theme => ({
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
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
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
        paddingLeft: theme.spacing.unit,
        paddingRight: theme.spacing.unit,
        fontSize: theme.typography.caption.fontSize,
        cursor: 'pointer',
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
function LeftMenuItem(props) {
    const { classes, theme, Icon } = props;
    const { leftMenu } = theme.custom;
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    const iconSize = theme.custom.leftMenuIconSize;

    return (
        <div
            className={classNames(
                classes.leftLInk,
                {
                    [classes.leftLink_IconLeft]: leftMenu === 'icon left',
                },
                'leftLInk',
            )}
            onClick={() => props.handleMenuSelect(props.text)}
            onKeyDown={() => props.handleMenuSelect(props.text)}
            style={{ backgroundColor: props.active === props.text ? theme.palette.background.appBar : '' }}
        >
            {// If the icon pro ( which is comming from the React Material library )
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
                        icon={props.text}
                        className={classNames(
                            classes.leftLInk,
                            {
                                [classes.noIcon]: leftMenu === 'no icon',
                            },
                            'leftLink_Icon',
                        )}
                    />
                )}
            <Typography
                className={classNames(
                    classes.leftLInkText,
                    {
                        [classes.leftLInkText_IconLeft]: leftMenu === 'icon left',
                        [classes.leftLInkText_NoText]: leftMenu === 'no text',
                    },
                    'leftLInkText',
                )}
                style={{ textTransform: 'uppercase' }}
            >
                {props.text}
            </Typography>
        </div>
    );
}
LeftMenuItem.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    Icon: PropTypes.element.isRequired,
    text: PropTypes.string.isRequired,
    handleMenuSelect: PropTypes.func.isRequired,
    active: PropTypes.string.isRequired,
};
export default withStyles(styles, { withTheme: true })(LeftMenuItem);
