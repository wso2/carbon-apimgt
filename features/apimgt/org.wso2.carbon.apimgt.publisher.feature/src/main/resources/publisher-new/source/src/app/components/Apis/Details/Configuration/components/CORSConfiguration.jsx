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
import ChipInput from 'material-ui-chip-input';
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import Checkbox from '@material-ui/core/Checkbox';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import Switch from '@material-ui/core/Switch';
import AuthManager from '../../../../../data/AuthManager';

/**
 *
 * api.corsConfiguration possible values true and false
 * @export
 * @param {*} props
 * @returns
 */
export default function CORSConfiguration(props) {
    const {
        api: { corsConfiguration },
        configDispatcher,
    } = props;
    const isCorsEnabled = corsConfiguration.corsConfigurationEnabled;
    const isAllowAllOrigins =
        corsConfiguration.accessControlAllowOrigins[0] === '*' &&
        corsConfiguration.accessControlAllowOrigins.length === 1;
    const isNotCreator = AuthManager.isNotCreator();

    return (
        <React.Fragment>
            <Grid container>
                <Grid item>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.CORSConfiguration.cors.configuration'
                                defaultMessage='CORS Configuration'
                            />
                        </FormLabel>
                        <FormControlLabel
                            control={(
                                <Switch
                                    disabled={isNotCreator}
                                    checked={corsConfiguration.corsConfigurationEnabled}
                                    onChange={({ target: { checked } }) => configDispatcher({
                                        action: 'corsConfigurationEnabled',
                                        value: checked,
                                    })
                                    }
                                    color='primary'
                                />
                            )}
                        />
                    </FormControl>
                </Grid>
                <Grid item>
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.CORSConfiguration.tooltip'
                                defaultMessage='If enabled, the CORS configuration for the API will be enabled.'
                            />
                        )}
                        aria-label='Response cache'
                        placement='right-end'
                        interactive
                    >
                        <HelpOutline />
                    </Tooltip>
                </Grid>
                <Grid container>
                    <Grid item md={12}>
                        {isCorsEnabled && (
                            <Grid container>
                                <Grid item md={12}>
                                    <Typography variant='subtitle1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.origins'
                                            defaultMessage='Access Control Allow Origins'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <Grid container>
                                        <Grid item md={12}>
                                            <FormControlLabel
                                                control={(
                                                    <Checkbox
                                                        disabled={isNotCreator}
                                                        checked={isAllowAllOrigins}
                                                        onChange={({ target: { checked, value } }) => configDispatcher({
                                                            action: 'accessControlAllowOrigins',
                                                            event: { checked, value },
                                                        })
                                                        }
                                                        value='*'
                                                    />
                                                )}
                                                label='Allow All Origins'
                                            />
                                        </Grid>
                                        {!isAllowAllOrigins && (
                                            <Grid item md={12}>
                                                <ChipInput
                                                    style={{ marginBottom: 40 }}
                                                    value={corsConfiguration.accessControlAllowOrigins}
                                                    helperText={(
                                                        <FormattedMessage
                                                            id={
                                                                'Apis.Details.Configuration.components'
                                                                + '.CORSConfigurations.origin.helper'
                                                            }
                                                            defaultMessage={
                                                                'Press `enter` after typing the origin name,'
                                                                + 'To add a new origin'
                                                            }
                                                        />
                                                    )}
                                                    onAdd={(accessControlAllowOrigin) => {
                                                        configDispatcher({
                                                            action: 'accessControlAllowOrigins',
                                                            event: {
                                                                value: [
                                                                    ...corsConfiguration.accessControlAllowOrigins,
                                                                    accessControlAllowOrigin,
                                                                ],
                                                            },
                                                        });
                                                    }}
                                                    onDelete={(accessControlAllowOrigin) => {
                                                        configDispatcher({
                                                            action: 'accessControlAllowOrigins',
                                                            event: {
                                                                value: corsConfiguration.accessControlAllowOrigins
                                                                    .filter(oldOrigin => oldOrigin !==
                                                                        accessControlAllowOrigin),
                                                            },
                                                        });
                                                    }}
                                                />
                                            </Grid>
                                        )}
                                    </Grid>
                                </Grid>
                                <Grid item md={12}>
                                    <Typography variant='subtitle1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.headers'
                                            defaultMessage='Access Control Allow Headers'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <ChipInput
                                        style={{ marginBottom: 40 }}
                                        value={corsConfiguration.accessControlAllowHeaders}
                                        disabled={isNotCreator}
                                        helperText={(
                                            <FormattedMessage
                                                id={
                                                    'Apis.Details.Configuration.components.'
                                                    + 'CORSConfigurations.header.helper'
                                                }
                                                defaultMessage={
                                                    'Press `enter` after typing the header name, '
                                                    + 'To add a new header'
                                                }
                                            />
                                        )}
                                        onAdd={(accessControlAllowHeader) => {
                                            configDispatcher({
                                                action: 'accessControlAllowHeaders',
                                                value: [
                                                    ...corsConfiguration.accessControlAllowHeaders,
                                                    accessControlAllowHeader,
                                                ],
                                            });
                                        }}
                                        onDelete={(accessControlAllowHeader) => {
                                            configDispatcher({
                                                action: 'accessControlAllowHeaders',
                                                value: corsConfiguration.accessControlAllowHeaders
                                                    .filter(oldHeader => oldHeader !== accessControlAllowHeader),
                                            });
                                        }}
                                    />
                                </Grid>
                                <Grid item md={12}>
                                    <Typography variant='subtitle1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.methods'
                                            defaultMessage='Access Control Allow Methods'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <ChipInput
                                        style={{ marginBottom: 40 }}
                                        value={corsConfiguration.accessControlAllowMethods}
                                        disabled={isNotCreator}
                                        helperText={(
                                            <FormattedMessage
                                                id={
                                                    'Apis.Details.Configuration.components'
                                                    + '.CORSConfigurations.method.helper'
                                                }
                                                defaultMessage={
                                                    'Press `enter` after typing the method name,'
                                                    + ' To add a new method'
                                                }
                                            />
                                        )}
                                        onAdd={(accessControlAllowMethod) => {
                                            configDispatcher({
                                                action: 'accessControlAllowMethods',
                                                value: [
                                                    ...corsConfiguration.accessControlAllowMethods,
                                                    accessControlAllowMethod,
                                                ],
                                            });
                                        }}
                                        onDelete={(accessControlAllowMethod) => {
                                            configDispatcher({
                                                action: 'accessControlAllowMethods',
                                                value: corsConfiguration.accessControlAllowMethods
                                                    .filter(oldMethod => oldMethod !== accessControlAllowMethod),
                                            });
                                        }}
                                    />
                                </Grid>
                                <Grid item>
                                    <FormControlLabel
                                        control={(
                                            <Checkbox
                                                disabled={isNotCreator}
                                                checked={corsConfiguration.accessControlAllowCredentials}
                                                onChange={({ target: { checked } }) => configDispatcher({
                                                    action: 'accessControlAllowCredentials',
                                                    value: checked,
                                                })
                                                }
                                            />
                                        )}
                                        label={(
                                            <FormattedMessage
                                                id={
                                                    'Apis.Details.Configuration.components'
                                                    + '.CORSConfiguration.allow.credentials'
                                                }
                                                defaultMessage='Access Control Allow Credentials'
                                            />
                                        )}
                                    />
                                </Grid>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}

CORSConfiguration.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
