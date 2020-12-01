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
import CircularProgress from '@material-ui/core/CircularProgress';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import { Button } from '@material-ui/core';
import PropTypes from 'prop-types';
import classNames from 'classnames';

const styles = (theme) => ({
    wrapper: {
        margin: theme.spacing(1),
        position: 'relative',
    },
    buttonSuccess: {
        backgroundColor: green[500],
        '&:hover': {
            backgroundColor: green[700],
        },
    },
    buttonProgress: {
        color: green[500],
        position: 'absolute',
        top: '50%',
        left: '50%',
        marginTop: -12,
        marginLeft: -12,
    },
});

/**
 * Provide animating button element with loading animation and success icon.
 * @param {Object} props React properties
 * @returns {React.Component} Button component
 */
const InteractiveButton = (props) => {
    // TODO: This prop transformation can be nicely done with ES6  (Rest/Spread Properties) if use stage 2 support ~tmkb
    const {
        loading,
        success,
        children,
        classes,
        color,
        disableFocusRipple,
        disableRipple,
        fullWidth,
        href,
        mini,
        size,
        variant,
    } = props;
    const buttonProps = {
        color,
        disableFocusRipple,
        disableRipple,
        fullWidth,
        href,
        mini,
        size,
        variant,
    };
    const buttonClassName = classNames({
        [classes.buttonSuccess]: success,
    });
    return (
        <div className={classes.wrapper}>
            <Button {...buttonProps} className={buttonClassName} disabled={loading}>
                {children}
            </Button>
            {loading && <CircularProgress size={24} className={classes.buttonProgress} />}
        </div>
    );
};

InteractiveButton.defaultProps = {
    loading: false,
    success: false,
    color: 'default',
    disabled: false,
    disableFocusRipple: false,
    fullWidth: false,
    mini: false,
    size: 'medium',
    type: 'button',
    variant: 'flat',
    disableRipple: false,
    href: '',
};

/* porpTypes taken from MUI button class */
InteractiveButton.propTypes = {
    loading: PropTypes.bool,
    success: PropTypes.bool,
    children: PropTypes.node.isRequired,
    classes: PropTypes.shape({
        buttonSuccess: PropTypes.string,
        wrapper: PropTypes.string,
        buttonProgress: PropTypes.string,
    }).isRequired,
    /**
     * The color of the component. It supports those theme colors that make sense for this component.
     */
    color: PropTypes.oneOf(['default', 'inherit', 'primary', 'secondary']),
    /**
     * If `true`, the button will be disabled.
     */
    disabled: PropTypes.bool,
    /**
     * If `true`, the  keyboard focus ripple will be disabled.
     * `disableRipple` must also be true.
     */
    disableFocusRipple: PropTypes.bool,
    /**
     * If `true`, the ripple effect will be disabled.
     */
    disableRipple: PropTypes.bool,
    /**
     * If `true`, the button will take up the full width of its container.
     */
    fullWidth: PropTypes.bool,
    /**
     * The URL to link to when the button is clicked.
     * If defined, an `a` element will be used as the root node.
     */
    href: PropTypes.string,
    /**
     * If `true`, and `variant` is `'fab'`, will use mini floating action button styling.
     */
    mini: PropTypes.bool,
    /**
     * The size of the button.
     * `small` is equivalent to the dense button styling.
     */
    size: PropTypes.oneOf(['small', 'medium', 'large']),
    /**
     * @ignore
     */
    type: PropTypes.string,
    /**
     * The type of button.
     */
    variant: PropTypes.oneOf(['flat', 'raised', 'fab']),
};
export default withStyles(styles)(InteractiveButton);
