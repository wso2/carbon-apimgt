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
import Snackbar from 'material-ui/Snackbar';
import IconButton from 'material-ui/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Info from '@material-ui/icons/Info';
import Error from '@material-ui/icons/Error';
import Warning from '@material-ui/icons/Warning';

class Message extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            open: false,
            message: '',
            type: '',
        };
        this.info = this.info.bind(this);
        this.error = this.error.bind(this);
        this.warning = this.warning.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
    }
    info(message) {
        this.setState({ open: true, type: 'info', message });
    }
    error(message) {
        this.setState({ open: true, type: 'error', message });
    }
    warning(message) {
        this.setState({ open: true, type: 'warning', message });
    }

    handleRequestClose(event, reason) {
        if (reason === 'clickaway') {
            return;
        }
        this.setState({ open: false });
    }

    render() {
        return (
            <Snackbar
                anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'center',
                }}
                open={this.state.open}
                autoHideDuration={12000}
                onClose={this.handleRequestClose}
                SnackbarContentProps={{
                    'aria-describedby': 'message-id',
                }}
                message={
                    <div id='message-id' className='message-content-box'>
                        {this.state.type === 'info' && <Info />}
                        {this.state.type === 'error' && <Error />}
                        {this.state.type === 'warning' && <Warning />}
                        <span>{this.state.message}</span>
                    </div>
                }
                action={[
                    <IconButton key='close' aria-label='Close' color='inherit' onClick={this.handleRequestClose}>
                        <CloseIcon />
                    </IconButton>,
                ]}
            />
        );
    }
}
export default Message;
