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
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
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


const useStyles = makeStyles(theme => ({
    expansionPanel: {
        marginBottom: theme.spacing(1),
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
        lineHeight: '38px',
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
        <React.Fragment>
            <Grid item xs={12}>
                <ExpansionPanel className={classes.expansionPanel}>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography className={classes.subHeading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Components.MaxBackendTps.maximum.backend.throughput'
                                defaultMessage='Backend Throughput'
                            />
                            <Tooltip
                                title={
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.MaxBackendTps.tooltip'
                                        defaultMessage={'Limits the total number of calls the API Manager is allowed' +
                                        ' to make to the backend'}
                                    />
                                }
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
                                name={api.maxTps}
                                value={api.maxTps === null}
                                onChange={(event) => {
                                    configDispatcher({
                                        action: 'maxTps',
                                        value:
                                            event.target.value === 'false' ? { production: null, sandbox: null } : null,
                                    });
                                }}
                                row
                            >
                                <FormControlLabel
                                    value
                                    control={<Radio color='default' />}
                                    label='Unlimited'
                                    labelPlacement='end'
                                />
                                <FormControlLabel
                                    value={false}
                                    control={<Radio color='default' />}
                                    label='Specify'
                                    labelPlacement='end'
                                />
                            </RadioGroup>
                        </FormControl>
                        <Collapse in={api.maxTps !== null}>
                            <Grid item xs={12} style={{ marginBottom: 15, position: 'relative' }}>
                                <TextField
                                    id='outlined-name'
                                    label='Max Production TPS'
                                    margin='normal'
                                    variant='outlined'
                                    onChange={(event) => {
                                        configDispatcher({
                                            action: 'maxTps',
                                            value: {
                                                production: event.target.value,
                                                sandbox: api.maxTps !== null ? api.maxTps.sandbox : null,
                                            },
                                        });
                                    }}
                                    value={api.maxTps !== null ? api.maxTps.production : ''}
                                    disabled={isRestricted(['apim:api_create'], api)}
                                    style={{ display: 'flex' }}
                                />
                            </Grid>
                            <Grid item xs={12} style={{ marginBottom: 15, position: 'relative' }}>
                                <TextField
                                    id='outlined-name'
                                    label='Max Sandbox TPS'
                                    margin='normal'
                                    variant='outlined'
                                    onChange={(event) => {
                                        configDispatcher({
                                            action: 'maxTps',
                                            value: {
                                                production: api.maxTps !== null ? api.maxTps.production : null,
                                                sandbox: event.target.value,
                                            },
                                        });
                                    }}
                                    value={api.maxTps !== null ? api.maxTps.sandbox : ''}
                                    disabled={isRestricted(['apim:api_create'], api)}
                                    style={{ display: 'flex' }}
                                />
                            </Grid>
                        </Collapse>
                        <FormHelperText>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.MaxBackendTps.formattedMessage'
                                defaultMessage='Maximum backend transactions per seconds in integers'
                            />
                        </FormHelperText>
                    </ExpansionPanelDetails>
                </ExpansionPanel>
            </Grid>
        </React.Fragment>
    );
}

MaxBackendTps.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
