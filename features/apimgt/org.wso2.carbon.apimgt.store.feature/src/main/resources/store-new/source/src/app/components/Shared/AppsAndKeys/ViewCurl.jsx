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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Typography } from '@material-ui/core';
import CopyToClipboard from 'react-copy-to-clipboard';
import FileCopy from '@material-ui/icons/FileCopy';
import Tooltip from '@material-ui/core/Tooltip';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, injectIntl, } from 'react-intl';

const styles = theme => ({
    code: {
        padding: theme.spacing.unit,
        marginBottom: theme.spacing.unit * 2,
        background: theme.palette.grey[200],
        color: '#da2316',
        flex: 1,
    },
    command: {
        color: '#2b62b0',
    },
    encodeVisible: {
        cursor: 'pointer',
    },
    contentWrapper: {
        display: 'flex',
    },
});

class ViewCurl extends React.Component {
    state = {
        showReal: false,
    };

    onCopy = name => (event) => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = function () {
            that.setState({
                [elementName]: false,
            });
        };
        setTimeout(caller, 4000);
    };

    applyReal = () => {
        this.setState({ showReal: !this.state.showReal });
    };

    render() {
        const {
            classes, consumerKey, consumerSecret, intl,
        } = this.props;
        const { showReal } = this.state;
        const bas64Encoded = window.btoa(consumerKey + ':' + consumerSecret);
        return (
            <React.Fragment>
                <Typography>
                    <FormattedMessage
                        id='Shared.AppsAndKeys.ViewCurl.help'
                        defaultMessage='The following cURL command shows how to generate an access token using
                            the Password Grant type.'
                    />
                </Typography>
                <div className={classes.contentWrapper}>
                    <div className={classes.code}>
                        <div>
                            <span className={classes.command}>curl -k -d </span>
                            "grant_type=password&username=Username&password=Password" \
                        </div>
                        <div>
                            <span className={classes.command}>-H</span>
                            "Authorization: Basic
                            <a onClick={this.applyReal} className={classes.encodeVisible}>
                                {showReal ? ' ' + bas64Encoded : ' Base64(consumer-key:consumer-secret)'}
                            </a>
                            " \
                        </div>
                        <div>
                            <span className={classes.command}>https://localhost:8248/token</span>
                        </div>
                    </div>
                    <div>
                        <Tooltip
                            title={
                                this.state.tokenCopied
                                    ? intl.formatMessage({
                                        defaultMessage: 'Copied',
                                        id: 'Shared.AppsAndKeys.ViewCurl.copied',
                                    })
                                    : intl.formatMessage({
                                        defaultMessage: 'Copy to clipboard',
                                        id: 'Shared.AppsAndKeys.ViewCurl.copy.to.clipboard',
                                    })
                            }
                            placement='right'
                        >
                            <CopyToClipboard
                                text={
                                    'curl -k -d "grant_type=password&username=Username&password=Password" "Authorization: Basic '
                                    + bas64Encoded
                                    + '" https://10.100.1.24:8248/token'
                                }
                                onCopy={this.onCopy('tokenCopied')}
                            >
                                <FileCopy color='secondary' />
                            </CopyToClipboard>
                        </Tooltip>
                    </div>
                </div>
                <Typography>
                    <FormattedMessage
                        id='Shared.AppsAndKeys.ViewCurl.help.in.a.similar'
                        defaultMessage='In a similar manner, you can generate an access token using the Client Credentials grant type with
                        the following cURL command.'
                    />
                </Typography>
                <div className={classes.contentWrapper}>
                    <div className={classes.code}>
                        <div>
                            <span className={classes.command}>curl -k -d </span>
                            "grant_type=client_credentials" \
                        </div>
                        <div>
                            <span className={classes.command}>-H</span>
                            "Authorization: Basic
                            <a onClick={this.applyReal} className={classes.encodeVisible}>
                                {showReal ? ' ' + bas64Encoded : ' Base64(consumer-key:consumer-secret)'}
                            </a>
                            " \
                        </div>
                        <div>
                            <span className={classes.command}>https://10.100.1.24:8248/token</span>
                        </div>
                    </div>
                    <div>
                        <Tooltip title={this.state.tokenCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                            <CopyToClipboard
                                text={
                                    'curl -k -d "grant_type=client_credentials" "Authorization: Basic '
                                    + bas64Encoded
                                    + '" https://10.100.1.24:8248/token'
                                }
                                onCopy={this.onCopy('tokenCopied')}
                            >
                                <FileCopy color='secondary' />
                            </CopyToClipboard>
                        </Tooltip>
                    </div>
                </div>
            </React.Fragment>
        );
    }
}

ViewCurl.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(ViewCurl));
