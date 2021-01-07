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
import Notification from 'rc-notification';
import userThemes from 'userCustomThemes';
import { ThemeProvider } from '@material-ui/core/styles';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import defaultTheme from 'AppData/defaultTheme';
import Message from './Message';

const theme = createMuiTheme(defaultTheme);

/**
 * Common alerting/message displaying component for Store application, Pre-set vertical: 'top',
 horizontal: 'center' and close action for consistent UX through out the app.
 Alert messages are mounted outside the app's root DOM element
 */
class Alert {
    /**
     * Creates an instance of Alert.
     * @param {String} message Message which needs to be displayed
     * @param {any} type Message category, i:e Alert, Info, Error ect
     * @param {any} duration Duration of the massage needs to be visible on the page
     * @param {any} onClose Callback function to trigger when message get closed
     * @memberof Alert
     */
    constructor(message, type, duration, onClose) {
        this.defaultTop = Alert.defaultTop;
        this.key = Alert.count++;
        this.type = type;
        this.message = message;
        this.onClose = onClose;
        this.duration = duration || Alert.defaultDuration;
        if (typeof duration === 'function') {
            this.onClose = duration;
            this.duration = Alert.defaultDuration;
        }
        this.remove = this.remove.bind(this);
    }

    /**
     * Show the alert message according to the parameters given in the constructor, Using the Alert.messageInstance
     */
    show() {
        const promisedInstance = this._getMessageInstance();
        const { onClose } = this;
        promisedInstance
            .then((instance) => {
                instance.notice({
                    closable: true,
                    onClose,
                    key: this.key,
                    duration: this.duration,
                    content: (
                        <ThemeProvider theme={createMuiTheme(defaultTheme)}>
                            <ThemeProvider theme={(currentTheme) => createMuiTheme(
                                { ...userThemes.light(currentTheme), ...currentTheme },
                            )}
                            >
                                <Message handleClose={this.remove} message={this.message} type={this.type} />
                            </ThemeProvider>
                        </ThemeProvider>
                    ),
                });
            })
            .catch((error) => console.error('Error while showing alert' + error));
        /* TODO: Remove above console error with logging library error method */
    }

    /**
     * Remove current message from view
     */
    remove() {
        const promisedInstance = this._getMessageInstance();
        promisedInstance.then((instance) => {
            instance.removeNotice(this.key);
        });
    }

    /**
     * Return a promise resolving to an instance of RC-Notification which can be use to display a notification on screen
     * @returns {Promise} Promise object with new React component for alert
     * @private
     */
    _getMessageInstance() {
        return new Promise((resolve) => {
            if (Alert.messageInstance) {
                resolve(Alert.messageInstance);
            } else {
                Notification.newInstance(
                    {
                        transitionName: 'move-down',
                        style: {
                            zIndex: theme.zIndex.snackbar,
                            top: 0,
                            right: 0,
                            marginLeft: '2%',
                            position: 'fixed',
                        },
                    },
                    (instance) => {
                        Alert.messageInstance = instance;
                        resolve(Alert.messageInstance);
                    },
                );
            }
        });
    }

    /**
     * Can be used to configure the global Alert configurations, Currently support position top alignment and
     * message display duration in seconds
     * If set here , will use in all the places where Alert has been used
     * @param {Object} options i:e {top: '10px', duration: 30}
     */
    static config(options) {
        const { top, duration } = options;
        if (top !== undefined) {
            Alert.defaultTop = top;
            Alert.messageInstance = null; // delete messageInstance for new defaultTop
        }
        if (duration !== undefined) {
            Alert.defaultDuration = duration;
        }
    }
}

Alert.messageInstance = null;
/* Class property to hold a RC-Notification instance */
Alert.count = 1;
/* Number of Notifications showed, This is used to generate unique key for each message */
Alert.defaultDuration = 5;
/* In seconds */
Alert.defaultTop = 0;
Alert.CONSTS = {
    INFO: 'info',
    SUCCESS: 'success',
    ERROR: 'error',
    WARN: 'warning',
    LOADING: 'loading',
};
Object.freeze(Alert.CONSTS);
export default {
    info: (message, duration, onClose) => {
        const msg = new Alert(message, Alert.CONSTS.INFO, duration, onClose);
        msg.show();
        return msg;
    },
    success: (message, duration, onClose) => {
        const msg = new Alert(message, Alert.CONSTS.SUCCESS, duration, onClose);
        msg.show();
        return msg;
    },
    error: (message, duration, onClose) => {
        const msg = new Alert(message, Alert.CONSTS.ERROR, duration, onClose);
        msg.show();
        return msg;
    },
    warning: (message, duration, onClose) => {
        const msg = new Alert(message, Alert.CONSTS.WARN, duration, onClose);
        msg.show();
        return msg;
    },
    loading: (message, duration, onClose) => {
        const msg = new Alert(message, Alert.CONSTS.LOADING, duration, onClose);
        msg.show();
        return msg;
    },
    configs: Alert.config,
    CONSTS: Alert.CONSTS,
};
