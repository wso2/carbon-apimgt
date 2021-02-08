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
import React, { useState, useContext } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { Typography } from '@material-ui/core';
import CopyToClipboard from 'react-copy-to-clipboard';
import FileCopy from '@material-ui/icons/FileCopy';
import Tooltip from '@material-ui/core/Tooltip';
import { FormattedMessage, injectIntl } from 'react-intl';

const useStyles = makeStyles(theme => ({
    code: {
        padding: theme.spacing(1),
        marginBottom: theme.spacing(2),
        background: theme.palette.grey[200],
        color: '#da2316',
        flex: 1,
    },
    command: {
        color: '#2b62b0',
    },
    encodeVisible: {
        cursor: 'pointer',
        textDecoration: 'underline',
    },
    contentWrapper: {
        display: 'flex',
    },
}));

/**
 *
 * @param {*} props
 */
function ViewCurl(props) {
    const classes = useStyles();

    const {
        keys: { consumerKey, consumerSecret },
        intl,
        keyType,
        keyManagerConfig,
    } = props;
    const bas64Encoded = window.btoa(consumerKey + ':' + consumerSecret);
    const [showReal, setShowReal] = useState(false);
    const [tokenCopied, setTokenCopied] = useState(false);
    const onCopy = () => {
        setTokenCopied(true);
        const caller = function () {
            setTokenCopied(false);
        };
        setTimeout(caller, 4000);
    };

    const applyReal = () => {
        setShowReal(!showReal);
    };
    // Check for additional properties for token endpoint and revoke endpoints.
    let { tokenEndpoint } = keyManagerConfig;
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
                        <span className={classes.command}>curl -k -X POST </span> {tokenEndpoint}
                        <span className={classes.command}> -d </span>{' '}
                        {'"grant_type=password&username=Username&password=Password"'}
                    </div>
                    <div>
                        <span className={classes.command}> -H </span>
                        {'"Authorization: Basic'}
                        <a onClick={applyReal} className={classes.encodeVisible}>
                            {showReal ? ' ' + bas64Encoded : ' Base64(consumer-key:consumer-secret)'}
                        </a>
                        {'"'}
                    </div>
                </div>
                <div>
                    <Tooltip
                        title={
                            tokenCopied
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
                            text={`curl -k -X POST ${tokenEndpoint} -d ` +
                                '"grant_type=password&username=Username&password=Password" -H ' +
                                `"Authorization: Basic ${bas64Encoded}"`}
                            onCopy={onCopy}
                        >
                            <FileCopy color='secondary' />
                        </CopyToClipboard>
                    </Tooltip>
                </div>
            </div>
            <Typography>
                <FormattedMessage
                    id='Shared.AppsAndKeys.ViewCurl.help.in.a.similar'
                    defaultMessage={`In a similar manner, you can generate an access token using the
                    Client Credentials grant type with the following cURL command.`}
                />
            </Typography>
            <div className={classes.contentWrapper}>
                <div className={classes.code}>
                    <div>
                        <span className={classes.command}>curl -k -X POST </span> {tokenEndpoint}
                        <span className={classes.command}> -d </span>{' '}
                        {'"grant_type=client_credentials"'}
                    </div>
                    <div>
                        <span className={classes.command}> -H </span>
                        {'"Authorization: Basic'}
                        <a onClick={applyReal} className={classes.encodeVisible}>
                            {showReal ? ' ' + bas64Encoded : ' Base64(consumer-key:consumer-secret)'}
                        </a>
                        {'"'}
                    </div>
                </div>
                <div>
                    <Tooltip title={tokenCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                        <CopyToClipboard
                            text={`curl -k -X POST ${tokenEndpoint} -d ` +
                                '"grant_type=client_credentials" -H' +
                                `"Authorization: Basic ${bas64Encoded}"`}
                            onCopy={onCopy}
                        >
                            <FileCopy color='secondary' />
                        </CopyToClipboard>
                    </Tooltip>
                </div>
            </div>
        </React.Fragment>
    );
}

ViewCurl.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    keys: PropTypes.shape({}).isRequired,
    apis: PropTypes.shape({}).isRequired,
};

export default injectIntl(ViewCurl);
