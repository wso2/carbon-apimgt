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

import React, { useReducer } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import TextField from '@material-ui/core/TextField';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import Checkbox from '@material-ui/core/Checkbox';
import Select from '@material-ui/core/Select';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormLabel from '@material-ui/core/FormLabel';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';

const useStyles = makeStyles((theme) => ({
    form: {
        display: 'flex',
        flexDirection: 'column',
        margin: 'auto',
        width: 'fit-content',
    },
    formControl: {
        marginTop: theme.spacing(2),
        minWidth: 120,
    },
    formControlLabel: {
        marginTop: theme.spacing(1),
    },
}));

/**
 *
 *
 * @export
 * @returns
 */
export default function EditParameter(props) {
    const {
        operationsDispatcher, target, verb, editingParameter, setEditingParameter,
    } = props;
    /**
     *
     *
     * @param {*} currentParameter
     * @param {*} paramAction
     */
    function parameterActionReducer(currentParameter, paramAction) {
        const { action, value } = paramAction;
        const nextParameter = { ...currentParameter, schema: { ...currentParameter.schema } };
        switch (action) {
            case 'description':
            case 'required':
                nextParameter[action] = value;
                break;

            default:
                break;
        }
        return nextParameter;
    }
    const [parameter, parameterActionDispatcher] = useReducer(parameterActionReducer, editingParameter);
    const classes = useStyles();

    const handleClose = () => {
        setEditingParameter(null);
    };

    /**
     *
     *
     */
    function handelDone() {
        operationsDispatcher({ action: 'parameter', data: { target, verb, value: parameter } });
    }
    const isEditing = parameter !== null;
    if (!isEditing) {
        return null;
    }
    return (
        <Dialog fullWidth maxWidth='md' open={isEditing} onClose={handleClose} aria-labelledby='edit-parameter'>
            <DialogTitle disableTypography id='edit-parameter'>
                <Typography variant='h6'>
                    Edit
                    {' '}
                    <Typography display='inline' variant='subtitle1'>
                        {capitalizeFirstLetter(parameter.in)}
                        {' '}
:
                        {parameter.name}
                    </Typography>
                </Typography>
            </DialogTitle>
            <DialogContent>
                <Grid container direction='row' spacing={2} justify='flex-start' alignItems='center'>
                    <Grid item md={6}>
                        <TextField
                            value={capitalizeFirstLetter(parameter.in)}
                            disabled
                            fullWidth
                            label='Parameter Type'
                            margin='dense'
                            variant='outlined'
                        />
                    </Grid>
                    <Grid item md={6}>
                        <TextField
                            value={parameter.name}
                            disabled
                            fullWidth
                            label='Name'
                            margin='dense'
                            variant='outlined'
                        />
                    </Grid>
                    <Grid item md={12}>
                        <TextField
                            fullWidth
                            label='Description'
                            multiline
                            rows='4'
                            name='description'
                            margin='normal'
                            variant='outlined'
                            onChange={({ target: { name, value } }) => {
                                parameterActionDispatcher({ action: name, value });
                            }}
                        />
                    </Grid>
                    <Grid item md={6}>
                        <FormControl
                            required
                            fullWidth
                            margin='dense'
                            variant='outlined'
                            className={classes.formControl}
                        >
                            <InputLabel required htmlFor='edit-parameter-type'>
                                Type
                            </InputLabel>

                            <Select
                                value=''
                                onChange={
                                    ({ target: { name, value } }) => parameterActionDispatcher({ type: name, value })
                                }
                                inputProps={{
                                    name: 'type',
                                    id: 'edit-parameter-type',
                                }}
                                MenuProps={{
                                    getContentAnchorEl: null,
                                    anchorOrigin: {
                                        vertical: 'bottom',
                                        horizontal: 'left',
                                    },
                                }}
                            >
                                {['number', 'integer'].map((dataType) => (
                                    <MenuItem value={dataType} dense>
                                        {capitalizeFirstLetter(dataType)}
                                    </MenuItem>
                                ))}
                            </Select>
                            <FormHelperText>Select schema type</FormHelperText>
                        </FormControl>
                    </Grid>
                    <Grid item md={6}>
                        <FormControl fullWidth margin='dense' variant='outlined' className={classes.formControl}>
                            <InputLabel htmlFor='edit-parameter-format'>Format</InputLabel>
                            <Select
                                value=''
                                onChange={
                                    ({ target: { name, value } }) => parameterActionDispatcher({ type: name, value })
                                }
                                inputProps={{
                                    name: 'format',
                                    id: 'edit-parameter-format',
                                }}
                                MenuProps={{
                                    getContentAnchorEl: null,
                                    anchorOrigin: {
                                        vertical: 'bottom',
                                        horizontal: 'left',
                                    },
                                }}
                            >
                                {['float', 'double', 'int32', 'int64'].map((dataType) => (
                                    <MenuItem value={dataType} dense>
                                        {capitalizeFirstLetter(dataType)}
                                    </MenuItem>
                                ))}
                            </Select>
                            <FormHelperText>Select format of data type</FormHelperText>
                        </FormControl>
                    </Grid>
                    <Grid item>
                        <FormControl component='fieldset'>
                            <FormLabel component='legend'>Require</FormLabel>
                            <FormControlLabel
                                control={<Checkbox checked={parameter.required} onChange={() => {}} value='required' />}
                            />
                        </FormControl>
                    </Grid>
                    <Grid container direction='row' justify='flex-end' alignItems='center'>
                        <DialogContentText>Use SAVE button in the page to persist changes</DialogContentText>
                    </Grid>
                </Grid>
            </DialogContent>
            <DialogActions>
                <Button size='small' onClick={handleClose} color='primary'>
                    Close
                </Button>
                <Button size='small' onClick={handelDone} variant='contained' color='primary'>
                    Done
                </Button>
            </DialogActions>
        </Dialog>
    );
}

EditParameter.propTypes = {
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    editingParameter: PropTypes.shape({}).isRequired,
    setEditingParameter: PropTypes.func.isRequired,
};
