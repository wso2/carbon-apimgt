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

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import FormGroup from '@material-ui/core/FormGroup';
import Grid from '@material-ui/core/Grid';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Paper from '@material-ui/core/Paper';
import { Button } from '@material-ui/core';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContentText from '@material-ui/core/DialogContentText';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    vhostPaper: {
        padding: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
}));

function AddEditVhost(props) {
    const { onVhostChange, initialVhosts } = props;
    const classes = useStyles();

    const [userVhosts, setUserVhosts] = useState(initialVhosts);
    const [id, setId] = useState(0);
    const defaultVhost = {
        host: '', httpContext: '', httpsPort: 443, httpPort: 80, wssPort: 8099, wsPort: 9099, isNew: true,
    };

    // change handlers
    const updateChanges = (key, field, newValue) => {
        const theVhost = userVhosts.find((vhost) => vhost.key === key);
        theVhost[field] = newValue;
        let tempItems = userVhosts.filter((vhost) => vhost.key !== key);
        tempItems = [...tempItems, theVhost];
        setUserVhosts(tempItems);
    };
    const changeHandler = (field) => ({ target: { name, value } }) => {
        let theValue = value;
        if (field === 'httpContext') {
            // remove slashes in start and end of httpContext
            theValue = value.replace(/(^\/|\/$)/g, '');
        }
        updateChanges(name, field, theValue);
    };

    const [openRemoveVhost, setOpenRemoveVhost] = useState(false);
    const [selectedKey, setSelectedKey] = useState(0);

    const handleRemoveVhost = (key) => {
        const theKey = key || selectedKey;
        const tempItems = userVhosts.filter((vhost) => vhost.key !== theKey);
        setUserVhosts(tempItems);
        setOpenRemoveVhost(false);
    };

    const handleRemoveVhostConfirm = (key, isNew) => {
        if (isNew) { // not already existing one
            handleRemoveVhost(key);
        } else { // already existing vhost
            setSelectedKey(key);
            setOpenRemoveVhost(true);
        }
    };

    const handleClose = () => {
        setOpenRemoveVhost(false);
    };

    const handleNewVhost = () => {
        const vhost = defaultVhost;
        vhost.key = '' + id;
        setId(id + 1);
        const tempItems = [...userVhosts, vhost];
        setUserVhosts(tempItems);
    };

    useEffect(() => {
        const nonEmptyItems = [];
        for (const userVhost of userVhosts) {
            if (userVhost.host && userVhost.host.trim() !== '') {
                nonEmptyItems.push(userVhost);
            }
        }
        onVhostChange({ target: { name: 'vhosts', value: nonEmptyItems } });
    }, [userVhosts]);

    useEffect(() => {
        if (initialVhosts && initialVhosts.length > 0) {
            let i = 0;
            setUserVhosts(initialVhosts.map((vhost) => {
                const keyedVhost = vhost;
                keyedVhost.key = '' + i++;
                return keyedVhost;
            }));
            setId(i);
        } else {
            setId(id + 1);
            const vhost = defaultVhost;
            vhost.key = '' + id;
            setUserVhosts([vhost]);
        }
    }, []);

    let vhostCounter = 1;
    return (
        <FormGroup>
            <Grid container spacing={0}>
                {userVhosts && userVhosts.map((vhost) => (
                    <Grid item xs={12}>
                        <Paper variant='outlined' className={classes.vhostPaper}>
                            <Grid container direction='row' spacing={0}>
                                {/* VHost's host name */}
                                <Grid item xs={9}>
                                    <TextField
                                        margin='dense'
                                        name={vhost.key}
                                        disabled={!vhost.isNew}
                                        onChange={changeHandler('host')}
                                        label={`VHost ${vhostCounter++}`}
                                        value={vhost.host}
                                        helperText='Host name (ex: mg.wso2.com)'
                                        variant='outlined'
                                    />
                                </Grid>
                                {/* Remove VHost Button */}
                                <Grid item xs={3}>
                                    <Button
                                        name={vhost.key}
                                        variant='outlined'
                                        color='primary'
                                        onClick={() => handleRemoveVhostConfirm(vhost.key, vhost.isNew)}
                                        disabled={userVhosts.length === 1}
                                    >
                                        Remove
                                    </Button>
                                    <Dialog
                                        open={openRemoveVhost}
                                        onClose={handleClose}
                                        aria-labelledby='alert-dialog-title'
                                        aria-describedby='alert-dialog-description'
                                    >
                                        <DialogTitle id='alert-dialog-title'>
                                            Remove Existing Vhost?
                                        </DialogTitle>
                                        <DialogContent>
                                            <DialogContentText id='alert-dialog-description'>
                                                Removing an existing VHost may result in inconsistent state if APIs
                                                are deployed with this VHost. Please make sure there are no APIs
                                                deployed with this VHost or redeploy those APIs.
                                            </DialogContentText>
                                        </DialogContent>
                                        <DialogActions>
                                            <Button onClick={handleClose} color='primary' autoFocus>
                                                No, Don&apos;t Remove
                                            </Button>
                                            <Button onClick={() => handleRemoveVhost('')} color='primary'>
                                                Yes
                                            </Button>
                                        </DialogActions>
                                    </Dialog>
                                </Grid>
                                {/* Advanced Settings */}
                                <Grid item xs={12}>
                                    <ExpansionPanel>
                                        <ExpansionPanelSummary
                                            expandIcon={<ExpandMoreIcon />}
                                            aria-controls='panel1a-content'
                                            id='panel1a-header'
                                        >
                                            <Typography className={classes.heading}>Advanced Settings</Typography>
                                        </ExpansionPanelSummary>
                                        <ExpansionPanelDetails>
                                            <Grid container>
                                                {/* HTTP Context */}
                                                <Grid item xs={12}>
                                                    <TextField
                                                        margin='dense'
                                                        name={vhost.key}
                                                        disabled={!vhost.isNew}
                                                        onChange={changeHandler('httpContext')}
                                                        label='http context'
                                                        value={vhost.httpContext}
                                                        helperText='HTTP context ex: pets'
                                                        variant='outlined'
                                                    />
                                                </Grid>
                                                {/* Ports of VHost */}
                                                <Grid item xs={12}>
                                                    <Grid container spacing={2}>
                                                        {
                                                            [
                                                                { name: 'httpPort', caption: 'HTTP Port' },
                                                                { name: 'httpsPort', caption: 'HTTPS Port' },
                                                                { name: 'wsPort', caption: 'WS Port' },
                                                                { name: 'wssPort', caption: 'WSS Port' },
                                                            ].map((field) => (
                                                                <Grid item xs={3}>
                                                                    <TextField
                                                                        margin='dense'
                                                                        name={vhost.key}
                                                                        disabled={!vhost.isNew}
                                                                        onChange={changeHandler(field.name)}
                                                                        label={field.caption}
                                                                        value={vhost[field.name]}
                                                                        type='number'
                                                                        variant='outlined'
                                                                    />
                                                                </Grid>
                                                            ))
                                                        }
                                                    </Grid>
                                                </Grid>
                                            </Grid>
                                        </ExpansionPanelDetails>
                                    </ExpansionPanel>
                                </Grid>
                            </Grid>
                        </Paper>
                    </Grid>
                ))}
                {/* Add new VHost */}
                <Grid item xs={12}>
                    <Button
                        name='newVhost'
                        variant='outlined'
                        color='primary'
                        onClick={handleNewVhost}
                    >
                        New VHost
                    </Button>
                </Grid>
            </Grid>
        </FormGroup>
    );
}

AddEditVhost.defaultProps = {
    initialVhosts: [],
};

AddEditVhost.propTypes = {
    onVhostChange: PropTypes.func.isRequired,
    initialVhosts: PropTypes.arrayOf(PropTypes.shape({
        host: PropTypes.string.isRequired,
        httpContext: PropTypes.string,
        httpPort: PropTypes.number.isRequired,
        httpsPort: PropTypes.number.isRequired,
        wsPort: PropTypes.number.isRequired,
        wssPort: PropTypes.number.isRequired,
    })),
};

export default AddEditVhost;
