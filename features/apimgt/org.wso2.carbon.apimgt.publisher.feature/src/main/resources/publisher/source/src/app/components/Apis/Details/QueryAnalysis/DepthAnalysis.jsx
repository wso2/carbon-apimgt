/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import FormGroup from '@material-ui/core/FormGroup';
import { makeStyles } from '@material-ui/core/styles';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import FormControl from '@material-ui/core/FormControl';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';

const useStyles = makeStyles((theme) => ({
    button: {
        height: 30,
        marginLeft: 30,
    },
    formControl: {
        textAlign: 'center',
        padding: `0 ${theme.spacing(7)}px`,
    },
    dialog: {
        width: '400px',
    },
    mandatoryStar: {
        color: theme.palette.error.main,
    },
}));

/**
 * Generate the Depth Analysis UI under Query Depth tab within Query Analysis
 * @returns {*} Depth Analysis page UI
 */
function DepthAnalysis() {
    const classes = useStyles();
    const [defaultDepth, setDefaultDepth] = React.useState(2);
    const [depthCheck, setDepthCheck] = React.useState(true);
    const [showPageContent, setShowPageContent] = React.useState(true);

    const handleDepthToggle = (event) => {
        setDepthCheck(event.target.checked);
        if (!event.target.checked) {
            setShowPageContent(false);
        }
    };

    const onDepthValueSave = () => {
        setDepthCheck(true);
        setShowPageContent(true);
    };

    const handleDefaultDepthInput = (event) => {
        setDefaultDepth(event.target.value);
    };

    let errors = '';
    let disableButton = false;
    if (!/^(\+?\d+|-?0+)$/.test(defaultDepth)) {
        errors = 'Default depth should be a non-negative integer';
        disableButton = true;
    }

    return (
        <>
            <Box>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.QueryAnalysis.DepthAnalysis.query.depth'
                        defaultMessage='Role-Depth Mappings'
                    />
                </Typography>
            </Box>
            <Box display='flex' justifyContent='center' m={1} p={1}>
                <FormControl component='fieldset'>
                    <FormGroup aria-label='position' row>
                        <FormControlLabel
                            control={(
                                <Switch
                                    checked={depthCheck}
                                    onChange={handleDepthToggle}
                                    value='depthCheck'
                                    color='primary'
                                />
                            )}
                            label='Enable / Disable Depth Limitation'
                            labelPlacement='start'
                        />
                    </FormGroup>
                </FormControl>
            </Box>
            {(depthCheck === true && showPageContent === false) ? (
                <Dialog
                    open={depthCheck}
                    onClose={() => setDepthCheck(false)}
                    aria-labelledby='alert-dialog-title'
                    aria-describedby='alert-dialog-description'
                >
                    <DialogTitle id='alert-dialog-title'>Default depth limitation value</DialogTitle>
                    <DialogContent>
                        <DialogContentText id='alert-dialog-description'>
                            This default depth limitation value is used to block malicious queries if none of the
                            role-depth mappings are applicable
                        </DialogContentText>
                    </DialogContent>
                    <FormControl className={classes.formControl}>
                        <TextField
                            id='defaultDepthLimitation'
                            type='number'
                            inputProps={{ min: '0' }}
                            error={errors}
                            helperText={errors && `${errors}`}
                            label={(
                                <>
                                    <FormattedMessage
                                        id='Apis.Details.QueryAnalysis.DepthAnalysis.dialog.depth.value'
                                        defaultMessage='Default Depth Limitation'
                                    />
                                    <sup className={classes.mandatoryStar}>*</sup>
                                </>
                            )}
                            margin='normal'
                            variant='outlined'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            value={defaultDepth}
                            onChange={(event) => handleDefaultDepthInput(event)}
                        />
                    </FormControl>
                    <DialogActions>
                        <Button
                            onClick={onDepthValueSave}
                            color='primary'
                            className={classes.button}
                            disabled={disableButton}
                        >
                            <FormattedMessage
                                id='Apis.Details.QueryAnalysis.DepthAnalysis.default.depth.limitation.add.button'
                                defaultMessage='Save'
                            />
                        </Button>
                        <Button onClick={() => setDepthCheck(false)} color='primary' autoFocus>
                            <FormattedMessage
                                id='Apis.Details.QueryAnalysis.DepthAnalysis.defualt.depth.limitation.cancel.button'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            ) : null}
            {(depthCheck === true && showPageContent === true) ? (
                <h1>{defaultDepth}</h1>
            ) : null}
        </>
    );
}

export default DepthAnalysis;
