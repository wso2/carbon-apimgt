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
import Grid from '@material-ui/core/Grid';
import Checkbox from '@material-ui/core/Checkbox';
import FormGroup from '@material-ui/core/FormGroup';
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import Typography from '@material-ui/core/Typography';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { API_SECURITY_MUTUAL_SSL } from './APISecurity/components/apiSecurityConstants';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.main,
    },
}));
/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Transports(props) {
    const { api, configDispatcher, securityScheme } = props;
    const [apiFromContext] = useAPI();
    const classes = useStyles();
    const isMutualSSLEnabled = securityScheme.includes(API_SECURITY_MUTUAL_SSL);
    const Validate = () => {
        if (api.transport && api.transport.length === 0) {
            return (
                <Typography>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.transport.empty'
                        defaultMessage='Please select at least one transport!'
                    />
                </Typography>
            );
        } else if (isMutualSSLEnabled && !api.transport.includes('https')) {
            return (
                <FormattedMessage
                    id='Apis.Details.Configuration.components.transport.sslHttps'
                    defaultMessage='Please select Https as transport with mutual SSL!'
                />
            );
        }
        return null; // No errors :-)
    };
    return (
        <Grid container spacing={1} alignItems='flex-start'>
            <Grid item>
                <FormControl component='fieldset'>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.transports'
                            defaultMessage='Transports'
                        />
                    </FormLabel>
                    <FormGroup style={{ display: 'flow-root' }}>
                        <FormControlLabel
                            control={(
                                <Checkbox
                                    disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                    checked={api.transport
                                        ? api.transport.includes('http') : null}
                                    onChange={({ target: { checked } }) => configDispatcher({
                                        action: 'transport',
                                        event: { checked, value: 'http' },
                                    })}
                                    value='http'
                                    color='primary'
                                />
                            )}
                            label='HTTP'
                        />
                        <FormControlLabel
                            control={(
                                <Checkbox
                                    disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                    checked={api.transport
                                        ? api.transport.includes('https') : null}
                                    onChange={({ target: { checked } }) => configDispatcher({
                                        action: 'transport',
                                        event: { checked, value: 'https' },
                                    })}
                                    value='https'
                                    color='primary'
                                />
                            )}
                            label='HTTPS'
                        />
                    </FormGroup>
                </FormControl>
            </Grid>
            <Grid item>
                <Tooltip
                    title={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.Transports.tooltip'
                            defaultMessage={
                                'API will be exposed in selected transport(s) in the gateway(s)'
                                + ' If Mutual SSL option is selected, a trusted client'
                                + ' certificate should be presented to access the API'
                            }
                        />
                    )}
                    aria-label='Transports'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            <Grid item>
                <span className={classes.error}>
                    <Validate />
                </span>
            </Grid>
        </Grid>
    );
}

Transports.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
