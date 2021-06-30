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
import Tooltip from '@material-ui/core/Tooltip';
import Checkbox from '@material-ui/core/Checkbox';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import WrappedExpansionPanel from 'AppComponents/Shared/WrappedExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { makeStyles } from '@material-ui/core/styles';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(3),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    iconSpace: {
        marginLeft: theme.spacing(0.5),
    },
    actionSpace: {
        margin: '-7px auto',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
}));

/**
 *
 * api.corsConfiguration possible values true and false
 * @export
 * @param {*} props
 * @returns
 */
export default function CORSConfiguration(props) {
    const [apiFromContext] = useAPI();
    const {
        configDispatcher,
        api: { corsConfiguration },
    } = props;
    const isCorsEnabled = corsConfiguration.corsConfigurationEnabled;
    const isAllowAllOrigins = corsConfiguration.accessControlAllowOrigins[0] === '*'
        && corsConfiguration.accessControlAllowOrigins.length === 1;
    const classes = useStyles();
    const generateElement = (isEnabled) => {
        if (isEnabled) {
            return (
                <ExpandMoreIcon />
            );
        }
        return (null);
    };
    return (
        <WrappedExpansionPanel className={classes.expansionPanel} id='corsConfiguration'>
            <ExpansionPanelSummary expandIcon={generateElement(corsConfiguration.corsConfigurationEnabled)}>
                <Typography className={classes.subHeading} variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.CORSConfiguration.cors.configuration'
                        defaultMessage='CORS Configuration'
                    />
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
                        <HelpOutline className={classes.iconSpace} />
                    </Tooltip>
                </Typography>
                <FormControlLabel
                    className={classes.actionSpace}
                    control={(
                        <Switch
                            disabled={isRestricted(['apim:api_create'], apiFromContext)}
                            checked={corsConfiguration.corsConfigurationEnabled}
                            onChange={({ target: { checked } }) => configDispatcher({
                                action: 'corsConfigurationEnabled',
                                value: checked,
                            })}
                            color='primary'
                        />
                    )}
                />
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                <Grid container>
                    <Grid item md={12}>
                        {isCorsEnabled && (
                            <Grid container>
                                <Grid item md={12}>
                                    <Typography variant='subtitle1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.
                                                    origins'
                                            defaultMessage='Access Control Allow Origins'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <Grid container>
                                        <Grid item md={12}>
                                            <FormControlLabel
                                                style={{ display: 'flex' }}
                                                control={(
                                                    <Checkbox
                                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                                        checked={isAllowAllOrigins}
                                                        onChange={({ target: { checked, value } }) => configDispatcher({
                                                            action: 'accessControlAllowOrigins',
                                                            event: { checked, value },
                                                        })}
                                                        value='*'
                                                        color='primary'
                                                    />
                                                )}
                                                label='Allow All Origins'
                                            />
                                        </Grid>
                                        {!isAllowAllOrigins && (
                                            <Grid item md={12}>
                                                <ChipInput
                                                    style={{ marginBottom: 40, display: 'flex' }}
                                                    value={corsConfiguration.accessControlAllowOrigins}
                                                    helperText={(
                                                        <FormattedMessage
                                                            id={
                                                                'Apis.Details.Configuration.components'
                                                                + '.CORSConfigurations.origin.helper'
                                                            }
                                                            defaultMessage={
                                                                'Press `Enter` after typing the origin name,'
                                                                + 'to add a new origin'
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
                                                                value: corsConfiguration
                                                                    .accessControlAllowOrigins
                                                                    .filter(
                                                                        (oldOrigin) => (
                                                                            oldOrigin !== accessControlAllowOrigin),
                                                                    ),
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
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.
                                                    headers'
                                            defaultMessage='Access Control Allow Headers'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <ChipInput
                                        style={{ marginBottom: 40, display: 'flex' }}
                                        value={corsConfiguration.accessControlAllowHeaders}
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                        helperText={(
                                            <FormattedMessage
                                                id={
                                                    'Apis.Details.Configuration.components.'
                                                    + 'CORSConfigurations.header.helper'
                                                }
                                                defaultMessage={
                                                    'Press `Enter` after typing the header name, '
                                                    + 'to add a new header'
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
                                                    .filter((oldHeader) => oldHeader !== accessControlAllowHeader),
                                            });
                                        }}
                                    />
                                </Grid>
                                <Grid item md={12}>
                                    <Typography variant='subtitle1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.components.CORSConfiguration.allow.
                                                    methods'
                                            defaultMessage='Access Control Allow Methods'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item md={12}>
                                    <ChipInput
                                        style={{ marginBottom: 40, display: 'flex' }}
                                        value={corsConfiguration.accessControlAllowMethods}
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                        helperText={(
                                            <FormattedMessage
                                                id={
                                                    'Apis.Details.Configuration.components'
                                                    + '.CORSConfigurations.method.helper'
                                                }
                                                defaultMessage={
                                                    'Press `Enter` after typing the method name,'
                                                    + ' to add a new method'
                                                }
                                            />
                                        )}
                                        onAdd={(newValue) => {
                                            let value = [...corsConfiguration.accessControlAllowMethods,
                                                newValue.toUpperCase()];
                                            if (
                                                corsConfiguration
                                                    .accessControlAllowMethods
                                                    .find((method) => method === newValue.toUpperCase())
                                            ) {
                                                value = [...corsConfiguration.accessControlAllowMethods];
                                            }
                                            configDispatcher({
                                                action: 'accessControlAllowMethods',
                                                value,
                                            });
                                        }}
                                        onDelete={(accessControlAllowMethod) => {
                                            configDispatcher({
                                                action: 'accessControlAllowMethods',
                                                value: corsConfiguration
                                                    .accessControlAllowMethods
                                                    .filter((oldMethod) => oldMethod !== accessControlAllowMethod),
                                            });
                                        }}
                                    />
                                </Grid>
                                <Grid item>
                                    <FormControlLabel
                                        control={(
                                            <Checkbox
                                                disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                                checked={corsConfiguration.accessControlAllowCredentials}
                                                onChange={({ target: { checked } }) => configDispatcher({
                                                    action: 'accessControlAllowCredentials',
                                                    value: checked,
                                                })}
                                                color='primary'
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
            </ExpansionPanelDetails>
        </WrappedExpansionPanel>
    );
}

CORSConfiguration.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
