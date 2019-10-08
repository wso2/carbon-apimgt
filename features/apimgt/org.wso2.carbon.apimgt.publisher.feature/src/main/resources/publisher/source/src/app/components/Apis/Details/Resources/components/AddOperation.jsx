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
import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import AddIcon from '@material-ui/icons/Add';
import { FormattedMessage } from 'react-intl';

import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import Fab from '@material-ui/core/Fab';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import ClearIcon from '@material-ui/icons/Clear';
import Tooltip from '@material-ui/core/Tooltip';
import Badge from '@material-ui/core/Badge';
import CircularProgress from '@material-ui/core/CircularProgress';
import APIValidation from 'AppData/APIValidation';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles(() => ({
    formControl: {
        minWidth: 120,
    },
}));

/**
 *
 *
 * @param {*} props
 * @returns
 */
function VerbElement(props) {
    const { verb, onClick, isButton } = props;

    const useMenuStyles = makeStyles((theme) => {
        const backgroundColor = theme.custom.resourceChipColors[verb.toLowerCase()];
        return {
            customMenu: {
                '&:hover': { backgroundColor },
                backgroundColor,
            },
            customButton: {
                '&:hover': { backgroundColor },
                backgroundColor,
                width: theme.spacing(12),
            },
        };
    });
    const classes = useMenuStyles();
    if (isButton) {
        return (
            <Button disableFocusRipple variant='contained' className={classes.customButton} size='small'>
                {verb}
            </Button>
        );
    } else {
        return (
            <MenuItem dense className={classes.customMenu} onClick={onClick}>
                {verb}
            </MenuItem>
        );
    }
}

const SUPPORTED_VERBS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'];
/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function AddOperation(props) {
    const { updateOpenAPI } = props;
    const inputLabel = useRef(null);
    const [labelWidth, setLabelWidth] = useState(0);
    const [isAdding, setIsAdding] = useState(false);
    const [operation, setOperation] = useState({ });
    React.useEffect(() => {
        setLabelWidth(inputLabel.current.offsetWidth);
    }, []);
    const classes = useStyles();

    /**
     *
     *
     */
    function clearInputs() {
        setOperation({ target: '', verb: '' });
    }
    /**
     *
     *
     */
    function addOperation() {
        if (
            APIValidation.operationTarget.validate(operation.target).error !== null ||
            APIValidation.operationVerb.validate(operation.verb).error !== null
        ) {
            Alert.warning("Operation target or operation verb can't be empty");
            return;
        }
        setIsAdding(true);
        updateOpenAPI('add', operation)
            .then(clearInputs)
            .finally(() => setIsAdding(false));
    }
    return (
        <Paper style={{ marginTop: '12px' }}>
            <Grid container direction='row' spacing={3} justify='center' alignItems='center'>
                <Grid item md={2}>
                    <FormControl margin='dense' variant='outlined' className={classes.formControl}>
                        <InputLabel ref={inputLabel} htmlFor='outlined-age-simple'>
                            HTTP Verb
                        </InputLabel>

                        <Select
                            renderValue={verb => <VerbElement isButton verb={verb} />}
                            value={operation.verb}
                            onChange={({ target: { name, value } }) => setOperation({ ...operation, [name]: value })}
                            labelWidth={labelWidth}
                            inputProps={{
                                name: 'verb',
                                id: 'operation-verb',
                            }}
                        >
                            {SUPPORTED_VERBS.map(verb => (
                                <VerbElement value={verb.toLowerCase()} verb={verb} />
                            ))}
                        </Select>

                        <FormHelperText id='my-helper-text'>
                            {operation.verb && operation.verb.toLowerCase() === 'option' ? (
                                // TODO: Add i18n to tooltip text ~tmkb
                                <Tooltip
                                    title={
                                        'Select the OPTIONS method to send OPTIONS calls to the backend.' +
                                        ' If the OPTIONS method is not selected, OPTIONS calls will be returned ' +
                                        'from the Gateway with allowed methods.'
                                    }
                                    placement='bottom'
                                >
                                    <Badge color='error' variant='dot'>
                                        Select a Verb
                                    </Badge>
                                </Tooltip>
                            ) : (
                                'Select a Verb'
                            )}
                        </FormHelperText>
                    </FormControl>
                </Grid>
                <Grid item md={6}>
                    <TextField
                        id='operation-target'
                        label='URI Pattern'
                        autoFocus
                        name='target'
                        value={operation.target}
                        onChange={({ target: { name, value } }) =>
                            setOperation({ ...operation, [name]: value.startsWith('/') ? value : `/${value}` })
                        }
                        placeholder='Enter the URI pattern'
                        helperText='Enter URI pattern'
                        fullWidth
                        margin='dense'
                        variant='outlined'
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                </Grid>
                <Grid item md={2}>
                    <Tooltip
                        title={
                            <FormattedMessage
                                id='Apis.Details.Resources.components.AddOperation.add.tooltip'
                                defaultMessage='Add new operation'
                            />
                        }
                        aria-label='AddOperation'
                        placement='bottom'
                        interactive
                    >
                        <Fab
                            disabled={isAdding}
                            style={{ marginLeft: '20px', marginBottom: '15px', marginRight: '20px' }}
                            size='small'
                            color='primary'
                            aria-label='add'
                            onClick={addOperation}
                        >
                            {!isAdding && <AddIcon />}
                            {isAdding && <CircularProgress size={24} />}
                        </Fab>
                    </Tooltip>

                    <sup>
                        <Tooltip
                            title={
                                <FormattedMessage
                                    id='Apis.Details.Resources.components.AddOperation.clear.inputs.tooltip'
                                    defaultMessage='Clear inputs'
                                />
                            }
                            aria-label='clear-inputs'
                            placement='bottom'
                            interactive
                        >
                            <IconButton disabled={isAdding} onClick={clearInputs} size='small'>
                                <ClearIcon />
                            </IconButton>
                        </Tooltip>
                    </sup>
                </Grid>
            </Grid>
        </Paper>
    );
}

AddOperation.propTypes = {
    updateOpenAPI: PropTypes.func.isRequired,
};
