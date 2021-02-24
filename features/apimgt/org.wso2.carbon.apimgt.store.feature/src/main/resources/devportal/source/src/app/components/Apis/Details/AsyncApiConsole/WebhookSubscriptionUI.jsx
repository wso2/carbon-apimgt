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

import React, {useState, useReducer} from 'react';
import Accordion from '@material-ui/core/ExpansionPanel';
import AccordionDetails from '@material-ui/core/ExpansionPanelDetails';
import AccordionSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import AccordionActions from '@material-ui/core/ExpansionPanelActions';
import Button from '@material-ui/core/Button';
import CopyToClipboard from 'react-copy-to-clipboard'
import TextField from '@material-ui/core/TextField';
import Alert from 'AppComponents/Shared/Alert';
import {makeStyles} from "@material-ui/core/styles/index";
import Grid from '@material-ui/core/Grid';
import Radio from '@material-ui/core/Radio';
import {RadioGroup} from '@material-ui/core';
import FormControlLabel from '@material-ui/core/FormControlLabel';

const useStyles = makeStyles((theme) => (
    {
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
            backgroundColor: '#41444e',
            color: '#fff',
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
            backgroundColor: '#89b7d1',
            maxHeight: '40px',
            borderColor: '#80bdff',
            '&$expanded': {
                maxHeight: '40px',
            }
        },
        subscription: {
            paddingBottom: '10px'
        }
    }
));

function reducer(state, {field, value}) {
    return {...state, [field]: value}
}

export default function WebhookSubscriptionUI(props) {

    const classes = useStyles();
    const {generateGenericWHSubscriptionCurl, topic} = props;
    const initialSubscriptionState = {
        topic: topic.name,
        secret: null,
        lease: 50000,
        mode: 'subscribe',
        callback: null
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
        dispatch({field: e.target.name, value: e.target.value});
    };

    function onCopy(message) {
        Alert.info(message);
    }

    return (
        <Accordion className={classes.subscription}>
            <AccordionSummary
                expandIcon={<ExpandMoreIcon/>}
                aria-controls="panel1bh-content"
                id="panel1bh-header"
                className={classes.subscriptionSummary}
            >
                <Typography>{topic.name}</Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Grid container direction="column" wrap={"nowrap"}>
                    <Grid item xs={6}>
                        <RadioGroup aria-label="mode" name="mode" value={state.mode} row onChange={handleChange}>
                            <FormControlLabel value="subscribe" control={<Radio/>} label="Subscribe"/>
                            <FormControlLabel value="unsubscribe" control={<Radio/>} label="Unsubscribe"/>
                        </RadioGroup>
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            name='callback'
                            id="standard-full-width"
                            label="Callback URL"
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
                                    id="standard-full-width"
                                    label="Secret"
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
                                    id="standard-full-width"
                                    label="Lease Seconds"
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
                            label="cURL"
                            defaultValue={''}
                            value={curl}
                            fullWidth
                            multiline={true}
                            id='bootstrap-input'
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
            <AccordionActions style={{paddingRight: '18px'}}>
                <Button size="small" onClick={handleClick}>Generate Curl</Button>
                <CopyToClipboard text={curl} onCopy={() => onCopy('cURL copied')}>
                    <Button size="small">Copy Curl</Button>
                </CopyToClipboard>
            </AccordionActions>
        </Accordion>
    );
}
