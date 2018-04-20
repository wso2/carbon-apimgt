/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { withStyles } from 'material-ui/styles';
import Snackbar from 'material-ui/Snackbar';
import IconButton from 'material-ui/IconButton';
import CloseIcon from '@material-ui/icons/Close';

import Info from '@material-ui/icons/Info';
import Error from '@material-ui/icons/Error';
import Warning from '@material-ui/icons/Warning';
import Done from '@material-ui/icons/Done';
import { CircularProgress as Loading } from 'material-ui/Progress';

const styles = {
    root: {
        position: 'relative' /* Overriding the default Snackbar root properties to stack messages */,
        padding: '5px' /* To add some space between messages when stacking messages */,
    },
};
/**
 * Build the message which is displayed in the Alert content
 */
const Message = (props) => {
    const {
        classes, message, handleClose, type,
    } = props;
    const Icon = Message.iconTypes[type];
    return (
        <div>
            <Snackbar
                classes={{ root: classes.root }}
                open
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'center',
                }}
                SnackbarContentProps={{
                    'aria-describedby': 'message-id',
                }}
                message={
                    <span id='message-id'>
                        {Icon}
                        {message}
                    </span>
                }
                action={[
                    <IconButton
                        key='close'
                        aria-label='Close'
                        color='default'
                        className={classes.close}
                        onClick={handleClose}
                    >
                        <CloseIcon />
                    </IconButton>,
                ]}
            />
        </div>
    );
};
/* TODO: Need to add color accordingly ~tmkb */
Message.iconTypes = {
    info: <Info color='primary' />,
    success: <Done />,
    error: <Error color='error' />,
    warning: <Warning />,
    loading: <Loading />,
};

Message.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    message: PropTypes.string.isRequired,
    handleClose: PropTypes.func.isRequired,
    type: PropTypes.string.isRequired,
};

export default withStyles(styles)(Message);
