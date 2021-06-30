/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState } from 'react';
import CopyToClipboard from 'react-copy-to-clipboard';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import InputAdornment from '@material-ui/core/InputAdornment';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import { useRevisionContext } from 'AppComponents/Shared/RevisionContext';
import Utils from 'AppData/Utils';
import { FileCopy } from '@material-ui/icons';

/**
 * Renders the callback URL for WebSub and URI mapping for WebSocket.
 *
 * @param {*} props
 */
export default function Runtime(props) {
    const {
        operation, operationsDispatcher, target, verb, api,
    } = props;
    const { settings } = useAppContext();
    const { allEnvRevision } = useRevisionContext();
    const allEnvDeployments = Utils.getAllEnvironmentDeployments(settings.environment, allEnvRevision);

    const buildCallbackURL = (protocol, host, port) => {
        const context = api.context.substr(0, 1) !== '/' ? '/' + api.context : api.context;
        let url = `${protocol}://${host || '{GATEWAY_HOST}'}:${port || `{websub_event_receiver_${protocol}_endpoint}`}`;
        url += context;
        url += '/' + api.version;
        url += '/webhooks_events_receiver_resource?topic=';
        url += target.toLowerCase();
        return url;
    };

    const getUriMappingHelperText = (value) => {
        let fqPath;
        if (api.endpointConfig
            && api.endpointConfig.production_endpoints
            && api.endpointConfig.production_endpoints.url
            && api.endpointConfig.production_endpoints.url.length > 0
            && value
            && value.length > 0) {
            const { url } = api.endpointConfig.production_endpoints;
            const seperator = url.substr(url.length - 1, 1) !== '/' ? '/' : '';
            fqPath = url + seperator + value;
        }
        return fqPath ? 'Production URL will be ' + fqPath : '';
    };
    const [uriMappingHelperText, setUriMappingHelperText] = useState(
        getUriMappingHelperText(operation[verb]['x-uri-mapping']),
    );

    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography variant='subtitle1'>
                    Runtime
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            {api.type === 'WS' && (
                <>
                    <Grid item md={1} />
                    <Grid item md={5}>
                        <TextField
                            margin='dense'
                            fullWidth
                            label='URL Mapping'
                            value={operation[verb]['x-uri-mapping']}
                            variant='outlined'
                            helperText={uriMappingHelperText}
                            onChange={(e) => {
                                let { value } = e.target;
                                if (value.length > 0 && value.substr(0, 1) !== '/') {
                                    value = '/' + value;
                                }
                                setUriMappingHelperText(getUriMappingHelperText(value));
                                operationsDispatcher({ action: 'uriMapping', data: { target, verb, value } });
                            }}
                        />
                    </Grid>
                    <Grid item md={6} />
                </>
            )}
            {api.type === 'WEBSUB' && settings.environment.map((env) => (
                <>
                    <Grid item md={1} />
                    <Grid item md={10}>
                        <Typography variant='subtitle1'>{env.displayName}</Typography>
                        <TextField
                            margin='dense'
                            fullWidth
                            label='HTTP Callback URL'
                            disabled
                            value={buildCallbackURL('http', allEnvDeployments[env.name].vhost.host,
                                allEnvDeployments[env.name].vhost.websubHttpPort)}
                            variant='outlined'
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position='end'>
                                        <CopyToClipboard
                                            text={buildCallbackURL('http',
                                                allEnvDeployments[env.name].vhost.host,
                                                allEnvDeployments[env.name].vhost.websubHttpPort)}
                                        >
                                            <IconButton>
                                                <Icon>
                                                    <FileCopy />
                                                </Icon>
                                            </IconButton>
                                        </CopyToClipboard>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <TextField
                            margin='dense'
                            fullWidth
                            label='HTTPS Callback URL'
                            disabled
                            value={buildCallbackURL('https', allEnvDeployments[env.name].vhost.host,
                                allEnvDeployments[env.name].vhost.websubHttpsPort)}
                            variant='outlined'
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position='end'>
                                        <CopyToClipboard
                                            text={buildCallbackURL('https',
                                                allEnvDeployments[env.name].vhost.host,
                                                allEnvDeployments[env.name].vhost.websubHttpsPort)}
                                        >
                                            <IconButton>
                                                <Icon>
                                                    <FileCopy />
                                                </Icon>
                                            </IconButton>
                                        </CopyToClipboard>
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Grid>
                    <Grid item md={1} />
                </>
            ))}
        </>
    );
}

Runtime.propTypes = {
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
        spec: PropTypes.shape({}).isRequired,
    }).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
};
