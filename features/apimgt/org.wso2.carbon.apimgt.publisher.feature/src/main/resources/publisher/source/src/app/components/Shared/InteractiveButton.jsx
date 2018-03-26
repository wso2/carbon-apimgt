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
import { Button, CircularProgress } from 'material-ui/Progress';
import { withStyles } from 'material-ui/styles';
import green from 'material-ui/colors/green';
import PropTypes from 'prop-types';
import classNames from 'classnames';

const styles = theme => ({
    wrapper: {
        margin: theme.spacing.unit,
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
    const {
        loading, success, children, classes,
    } = props;
    const buttonClassName = classNames({
        [classes.buttonSuccess]: success,
    });
    return (
        <div className={classes.wrapper}>
            <Button {...props} className={buttonClassName} disabled={loading}>
                {children}
            </Button>
            {loading && <CircularProgress size={24} className={classes.buttonProgress} />}
        </div>
    );
};

InteractiveButton.defaultProps = {
    loading: false,
    success: false,
};

InteractiveButton.propTypes = {
    loading: PropTypes.bool,
    success: PropTypes.bool,
    children: PropTypes.node.isRequired,
    classes: PropTypes.shape({}).isRequired,
};
export default withStyles(styles)(InteractiveButton);
