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
"use strict";
import React, {Component} from 'react';
import Message from './Message'
import Notification from 'rc-notification';

/**
 * Common alerting/message displaying component for Store application, Pre-set vertical: 'top',
 horizontal: 'center' and close action for consistent UX through out the app.
 */
class Alert {
    constructor(message, type, duration, onClose) {
        this.defaultTop = 1;
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
        const onClose = this.onClose;
        promisedInstance.then(instance => {
            instance.notice({
                closable: true,
                onClose,
                key: this.key,
                duration: this.duration,
                content: (
                    <div >
                        <Message handleClose={this.remove} message={this.message} type={this.type}/>
                    </div>
                ),
            });
        }).catch(error => console.error("Error while showing alert" + error))
    }

    /**
     * Remove current message from view
     */
    remove() {
        const promisedInstance = this._getMessageInstance();
        promisedInstance.then(instance => {
            instance.removeNotice(this.key);
        });
    }

    /**
     * Return a promise resolving to an instance of RC-Notification which can be use to display a notification on screen
     * @returns {Promise}
     * @private
     */
    _getMessageInstance() {
        return new Promise((resolve, reject) => {
            if (Alert.messageInstance) {
                resolve(Alert.messageInstance);
            } else {
                Notification.newInstance({
                    transitionName: 'move-down',
                    style: {top: 0, marginLeft: '45%', position: 'absolute'},
                }, (instance) => {
                    Alert.messageInstance = instance;
                    resolve(Alert.messageInstance);
                });
            }
        });
    }

    /**
     * Can be used to configure the global Alert configurations, Currently support position top alignment and message display duration in seconds
     * If set here , will use in all the places where Alert has been used
     * @param options i:e {top: '10px', duration: 30}
     */
    static config(options) {
        if (options.top !== undefined) {
            Alert.defaultTop = options.top;
            Alert.messageInstance = null; // delete messageInstance for new defaultTop
        }
        if (options.duration !== undefined) {
            Alert.defaultDuration = options.duration;
        }
    }

}

Alert.messageInstance = null;
/* Class property to hold a RC-Notification instance*/
Alert.count = 1;
/* Number of Notifications showed, This is used to generate unique key for each message */
Alert.defaultDuration = 5;
/* In seconds */
Alert.defaultTop = 0;

export default {
    info: (message, duration, onClose) => {
        let msg = new Alert(message, 'info', duration, onClose);
        msg.show();
        return msg
    },
    success: (message, duration, onClose) => {
        let msg = new Alert(message, 'success', duration, onClose);
        msg.show();
        return msg
    },
    error: (message, duration, onClose) => {
        let msg = new Alert(message, 'error', duration, onClose);
        msg.show();
        return msg
    },
    warning: (message, duration, onClose) => {
        let msg = new Alert(message, 'warning', duration, onClose);
        msg.show();
        return msg
    },
    loading: (message, duration, onClose) => {
        let msg = new Alert(message, 'loading', duration, onClose);
        msg.show();
        return msg
    },
    configs: Alert.config
};