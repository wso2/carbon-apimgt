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
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';

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
export default function Subscription(props) {
    const [apiFromContext] = useAPI();
    const {
        configDispatcher,
        api: { subscription },
    } = props;
    const classes = useStyles();

    function generateSecret() {
        return 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'.replace(/[x]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
          });
    }

    function getAlgorithms() {
        return [
            { id: 'sha-1', name: 'SHA-1'},
            { id: 'sha-2', name: 'SHA-2'},
            { id: 'sha-3', name: 'SHA-3'},
        ];
    }

    return (
        <WrappedExpansionPanel className={classes.expansionPanel} id='subscriptionConfigurations'>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                <Typography className={classes.subHeading} variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Subscription.subscription'
                        defaultMessage='Subscription'
                    />
                </Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                <Grid container>
                    <Grid item md={12}>
                        <Grid container>
                            <Grid item md={12}>
                                <TextField
                                    autoFocus
                                    fullWidth
                                    disabled
                                    label={(
                                        <>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Subscription.secret'
                                                defaultMessage='Secret'
                                            />
                                        </>
                                    )}
                                    value={generateSecret()}
                                    helperText='Use the above secret key while registering at the provider'
                                    name='secret'
                                    InputProps={{
                                        id: 'itest-id-runtime-subscription-secret',
                                        onBlur: ({ target: { value } }) => {
                                            // TODO: validate
                                        },
                                    }}
                                    margin='normal'
                                    variant='outlined'
                                />
                            </Grid>
                            <Grid item md={12}>
                                <TextField
                                    autoFocus
                                    fullWidth
                                    select
                                    label={(
                                        <>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Subscription.signingAlgorithm'
                                                defaultMessage='Signing Algortithm'
                                            />
                                        </>
                                    )}
                                    value={subscription.signingAlgorithm}
                                    helperText='Select an algorithm to sign the message'
                                    name='secret'
                                    InputProps={{
                                        id: 'itest-id-runtime-subscription-signing-algorithm',
                                        onBlur: ({ target: { value } }) => {
                                            // TODO: validate
                                        },
                                    }}
                                    margin='normal'
                                    variant='outlined'
                                    onChange={({ target: { value } }) => configDispatcher({
                                        action: 'signingAlgorithm',
                                        value,
                                    })}
                                >
                                    <MenuItem dense>
                                        Select an algorithm
                                    </MenuItem>
                                    {
                                        getAlgorithms().map((a) => {
                                            return (
                                                <MenuItem
                                                    dense
                                                    id={a.id}
                                                    key={a.id}
                                                    value={a.id}
                                                >
                                                    {a.name}
                                                </MenuItem>
                                            );
                                        })
                                    }
                                </TextField>
                            </Grid>
                            <Grid item md={12}>
                                <TextField
                                    autoFocus
                                    fullWidth
                                    label={(
                                        <>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Subscription.signature.header'
                                                defaultMessage='Signature Header'
                                            />
                                        </>
                                    )}
                                    value={subscription.signatureHeader}
                                    helperText='Set the HTTP header use by the provider to send the signature'
                                    name='secret'
                                    InputProps={{
                                        id: 'itest-id-runtime-subscription-signature-header',
                                        onBlur: ({ target: { value } }) => {
                                            // TODO: validate
                                        },
                                    }}
                                    margin='normal'
                                    variant='outlined'
                                    onChange={({ target: { value } }) => configDispatcher({
                                        action: 'signatureHeader',
                                        value,
                                    })}
                                />
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </ExpansionPanelDetails>
        </WrappedExpansionPanel>
    );
}

Subscription.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
