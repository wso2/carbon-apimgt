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
"use strict";
import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Snackbar from '@material-ui/core/Snackbar';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';

const styles = theme => ({
    close: {
        width: theme.spacing(4),
        height: theme.spacing(4),
    },
});
/**
 * Common alerting/message displaying component for Dev Portal, Pre-set vertical: 'top',
 horizontal: 'center' and close action for consistent UX through out the app.
 */
class Alert extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            open: Boolean(props.message),
        };
    }

    componentWillReceiveProps(nextProps) {
        this.setState({open: Boolean(nextProps.message)});
    }

    handleClick = () => {
        this.setState({open: true});
    };

    handleRequestClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }

        this.setState({open: false});
    };

    render() {
        const {classes, message} = this.props;
        return (
            <div>
                <Snackbar
                    anchorOrigin={{
                        vertical: 'top',
                        horizontal: 'center',
                    }}
                    open={this.state.open}
                    autoHideDuration={6000}
                    onClose={this.handleRequestClose}
                    SnackbarContentProps={{
                        'aria-describedby': 'message-id',
                    }}
                    message={<span id="message-id">{message}</span>}
                    action={[
                        <IconButton
                            key="close"
                            aria-label="Close"
                            color="inherit"
                            className={classes.close}
                            onClick={this.handleRequestClose}
                        >
                            <Icon>close</Icon>
                        </IconButton>,
                    ]}
                />
            </div>
        );
    }
}

Alert.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Alert);