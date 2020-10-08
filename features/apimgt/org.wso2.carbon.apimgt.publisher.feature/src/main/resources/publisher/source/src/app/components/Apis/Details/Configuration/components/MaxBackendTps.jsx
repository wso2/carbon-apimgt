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
import HelpOutline from '@material-ui/icons/HelpOutline';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormHelperText from '@material-ui/core/FormHelperText';
import WrappedExpansionPanel from 'AppComponents/Shared/WrappedExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import InputAdornment from '@material-ui/core/InputAdornment';
import FormLabel from '@material-ui/core/FormLabel';
import RadioGroup from '@material-ui/core/RadioGroup';
import Radio from '@material-ui/core/Radio';
import Grid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import { Collapse } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(1), // TODO: replace with <Box /> element `mb`
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    iconSpace: {
        marginLeft: theme.spacing(0.5),
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
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function MaxBackendTps(props) {
    const { api, configDispatcher } = props;
    const classes = useStyles();

    return (
        <>
            <Grid item xs={12}>
                <WrappedExpansionPanel className={classes.expansionPanel} defaultExpanded id='maxBackendTps'>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography className={classes.subHeading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Components.MaxBackendTps.maximum.backend.throughput'
                                defaultMessage='Backend Throughput'
                            />
                            <Tooltip
                                title={(
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.MaxBackendTps.tooltip'
                                        defaultMessage={'Limits the total number of calls the API Manager is allowed'
                                        + ' to make to the backend'}
                                    />
                                )}
                                aria-label='APISecurity'
                                placement='right-end'
                                interactive
                            >
                                <HelpOutline className={classes.iconSpace} />
                            </Tooltip>
                        </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                        <FormControl component='fieldset'>
                            <FormLabel component='legend'>Maximum Throughput</FormLabel>
                            <RadioGroup
                                aria-label='change-max-TPS'
                                value={api.maxTps === null ? 'unlimited' : 'specify'}
                                onChange={(event) => {
                                    configDispatcher({
                                        action: 'maxTps',
                                        value:
                                            event.target.value === 'specify' ? { production: null, sandbox: null }
                                                : null,
                                    });
                                }}
                                row
                            >
                                <FormControlLabel
                                    value='unlimited'
                                    control={(
                                        <Radio
                                            color='primary'
                                            disabled={isRestricted(['apim:api_create'], api)}
                                        />
                                    )}
                                    label='Unlimited'
                                    labelPlacement='end'

                                />
                                <FormControlLabel
                                    value='specify'
                                    control={(
                                        <Radio
                                            color='primary'
                                            disabled={isRestricted(['apim:api_create'], api)}
                                        />
                                    )}
                                    label='Specify'
                                    labelPlacement='end'
                                    disabled={isRestricted(['apim:api_create'], api)}
                                />
                            </RadioGroup>
                        </FormControl>
                        <Collapse in={api.maxTps !== null}>
                            <Grid item xs={12} style={{ marginBottom: 10, position: 'relative' }}>
                                <TextField
                                    label='Max Production TPS'
                                    margin='normal'
                                    variant='outlined'
                                    onChange={(event) => {
                                        configDispatcher({
                                            action: 'maxTps',
                                            value: { ...api.maxTps, production: event.target.value },
                                        });
                                    }}
                                    value={api.maxTps !== null ? api.maxTps.production : ''}
                                    disabled={isRestricted(['apim:api_create'], api)}
                                    InputProps={{
                                        endAdornment: <InputAdornment position='end'>TPS</InputAdornment>,
                                    }}
                                />
                            </Grid>
                            <Grid item xs={12} style={{ marginBottom: 10, position: 'relative' }}>
                                <TextField
                                    label='Max Sandbox TPS'
                                    margin='normal'
                                    variant='outlined'
                                    onChange={(event) => {
                                        configDispatcher({
                                            action: 'maxTps',
                                            value: { ...api.maxTps, sandbox: event.target.value },
                                        });
                                    }}
                                    value={api.maxTps !== null ? api.maxTps.sandbox : ''}
                                    disabled={isRestricted(['apim:api_create'], api)}
                                    InputProps={{
                                        endAdornment: <InputAdornment position='end'>TPS</InputAdornment>,
                                    }}
                                />
                                <FormHelperText>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.MaxBackendTps.formattedMessage'
                                        defaultMessage='Maximum backend transactions per second in integers'
                                    />
                                </FormHelperText>
                            </Grid>
                        </Collapse>
                    </ExpansionPanelDetails>
                </WrappedExpansionPanel>
            </Grid>
        </>
    );
}

MaxBackendTps.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
