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

import React, { useState, useReducer } from 'react';
import Accordion from '@material-ui/core/ExpansionPanel';
import AccordionDetails from '@material-ui/core/ExpansionPanelDetails';
import AccordionSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import AccordionActions from '@material-ui/core/ExpansionPanelActions';
import Button from '@material-ui/core/Button';
import CopyToClipboard from 'react-copy-to-clipboard';
import TextField from '@material-ui/core/TextField';
import Alert from 'AppComponents/Shared/Alert';
import { makeStyles } from '@material-ui/core/styles/index';
import Grid from '@material-ui/core/Grid';
import Radio from '@material-ui/core/Radio';
import { RadioGroup } from '@material-ui/core';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { FormattedMessage, injectIntl } from 'react-intl';
import Utils from 'AppData/Utils';
import Badge from '@material-ui/core/Badge';

function reducer(state, { field, value }) {
    return { ...state, [field]: value };
}

function WebhookSubscriptionUI(props) {
    const verb = props.topic.type.toLowerCase();
    const trimmedVerb = verb === 'publish' || verb === 'subscribe' ? verb.substr(0, 3) : verb;
    const useStyles = makeStyles((theme) => {
        const backgroundColor = theme.custom.resourceChipColors[trimmedVerb];
        return {
            bootstrapRoot: {
                padding: 0,
                'label + &': {
                    marginTop: theme.spacing(1),
                },
            },
            bootstrapInput: {
                borderRadius: 4,
                backgroundColor: theme.palette.common.white,
                border: '1px solid #ced4da',
                padding: '5px 12px',
                marginTop: '11px',
                marginBottom: '11px',
                width: '100%',
                transition: theme.transitions.create(['border-color', 'box-shadow']),
                '&:focus': {
                    borderColor: '#80bdff',
                    boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
                },
                fontSize: 12,
            },
            bootstrapCurl: {
                borderRadius: 4,
                backgroundColor: theme.custom.curlGenerator.backgroundColor,
                color: theme.custom.curlGenerator.color,
                border: '1px solid #ced4da',
                padding: '5px 12px',
                marginTop: '11px',
                marginBottom: '11px',
                width: '100%',
                transition: theme.transitions.create(['border-color', 'box-shadow']),
                '&:focus': {
                    borderColor: '#80bdff',
                    boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
                },
                fontSize: 12,
                fontFamily: 'monospace',
                fontWeight: 600,
            },
            subscriptionSummary: {
                backgroundColor: Utils.hexToRGBA(backgroundColor, 0.1),
                maxHeight: '40px',
                borderColor: '#80bdff',
                '&$expanded': {
                    maxHeight: '40px',
                },
            },
            customButton: {
                backgroundColor: '#ffffff',
                borderColor: backgroundColor,
                color: backgroundColor,
                width: theme.spacing(2),
            },
            subscription: {
                marginBottom: '10px',
                border: `1px solid ${backgroundColor}`,
            },
        };
    });
    const { generateGenericWHSubscriptionCurl, topic, intl } = props;
    const initialSubscriptionState = {
        topic: topic.name,
        secret: null,
        lease: 50000,
        mode: 'subscribe',
        callback: null,
    };
    const [curl, setCurl] = useState(generateGenericWHSubscriptionCurl(initialSubscriptionState));
    const [formError, setFormError] = useState(false);
    const [state, dispatch] = useReducer(reducer, initialSubscriptionState);

    const handleClick = () => {
        if (!state.callback || state.callback.length < 1) {
            setFormError(true);
        } else {
            setFormError(false);
            setCurl(generateGenericWHSubscriptionCurl(state));
        }
    };

    const handleChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const classes = useStyles();

    return (
        <Accordion className={classes.subscription}>
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls='wh-subscription-content'
                id='wh-subscription-header'
                className={classes.subscriptionSummary}
            >
                <Grid container direction='row' justify='space-between' alignItems='center' spacing={0}>
                    <Grid item md={11}>
                        <Badge invisible='false' color='error' variant='dot'>
                            <Button
                                disableFocusRipple
                                variant='outlined'
                                size='small'
                                className={classes.customButton}
                            >
                                {trimmedVerb.toUpperCase()}
                            </Button>
                        </Badge>
                        <Typography display='inline' style={{ margin: '0px 30px' }} gutterBottom>
                            {topic.name}
                        </Typography>
                    </Grid>
                </Grid>
            </AccordionSummary>
            <AccordionDetails>
                <Grid container direction='column' wrap='nowrap'>
                    <Grid item xs={6}>
                        <RadioGroup aria-label='mode' name='mode' value={state.mode} row onChange={handleChange}>
                            <FormControlLabel
                                value='subscribe'
                                control={<Radio />}
                                label={intl.formatMessage({
                                    defaultMessage: 'Subscribe',
                                    id: 'Apis.Details.AsyncApiConsole.Webhooks.Subscribe',
                                })}
                            />
                            <FormControlLabel
                                value='unsubscribe'
                                control={<Radio />}
                                label={intl.formatMessage({
                                    defaultMessage: 'Unsubscribe',
                                    id: 'Apis.Details.AsyncApiConsole.Webhooks.Unsubscribe',
                                })}
                            />
                        </RadioGroup>
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            name='callback'
                            id='standard-full-width'
                            label={intl.formatMessage({
                                defaultMessage: 'Callback URL',
                                id: 'Apis.Details.AsyncApiConsole.Webhooks.callback',
                            })}
                            error={formError}
                            required
                            placeholder='www.webhook.site'
                            onChange={handleChange}
                            fullWidth
                            InputProps={{
                                disableUnderline: true,
                                classes: {
                                    root: classes.bootstrapRoot,
                                    input: classes.bootstrapInput,
                                },
                            }}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </Grid>
                    {state.mode === 'subscribe' && (
                        <>
                            <Grid item xs={6}>
                                <TextField
                                    name='secret'
                                    id='standard-full-width'
                                    label={intl.formatMessage({
                                        defaultMessage: 'Secret',
                                        id: 'Apis.Details.AsyncApiConsole.Webhooks.secret',
                                    })}
                                    placeholder='secret'
                                    onChange={handleChange}
                                    fullWidth
                                    InputProps={{
                                        disableUnderline: true,
                                        classes: {
                                            root: classes.bootstrapRoot,
                                            input: classes.bootstrapInput,
                                        },
                                    }}
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    name='lease'
                                    id='standard-full-width'
                                    label={intl.formatMessage({
                                        defaultMessage: 'Lease Seconds',
                                        id: 'Apis.Details.AsyncApiConsole.Webhooks.lease',
                                    })}
                                    onChange={handleChange}
                                    defaultValue={50000}
                                    fullWidth
                                    InputProps={{
                                        disableUnderline: true,
                                        classes: {
                                            root: classes.bootstrapRoot,
                                            input: classes.bootstrapInput,
                                        },
                                    }}
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                />
                            </Grid>
                        </>
                    )}
                    <Grid item xs={12}>
                        <TextField
                            label={intl.formatMessage({
                                defaultMessage: 'cURL',
                                id: 'Apis.Details.AsyncApiConsole.Webhooks.curl',
                            })}
                            defaultValue=''
                            value={curl}
                            fullWidth
                            multiline
                            InputProps={{
                                disableUnderline: true,
                                classes: {
                                    root: classes.bootstrapRoot,
                                    input: classes.bootstrapCurl,
                                },
                            }}
                            InputLabelProps={{
                                shrink: true,
                                className: classes.bootstrapFormLabel,
                            }}
                        />
                    </Grid>
                </Grid>
            </AccordionDetails>
            <AccordionActions style={{ paddingRight: '18px' }}>
                <Button size='small' onClick={handleClick}>
                    <FormattedMessage id='Apis.Details.AsyncApiConsole.Curl' defaultMessage='Generate Curl' />
                </Button>
                <CopyToClipboard text={curl} onCopy={() => Alert.info('cURL copied')}>
                    <Button size='small'>
                        <FormattedMessage id='Apis.Details.AsyncApiConsole.Copy' defaultMessage='Copy Curl' />
                    </Button>
                </CopyToClipboard>
            </AccordionActions>
        </Accordion>
    );
}

export default injectIntl(WebhookSubscriptionUI);
