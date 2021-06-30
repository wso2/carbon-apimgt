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
import React, { useReducer } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import ClearIcon from '@material-ui/icons/Clear';
import Tooltip from '@material-ui/core/Tooltip';

const useStyles = makeStyles(() => ({
    formControl: {
        minWidth: 120,
    },
    parameterContainer: {
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    checkBox: {
        color: '#7c7c7c',
    },
}));

/**
 *
 * Add payload properties according to the AsyncAPI spec.
 *
 * @export
 * @param {*} props
 * @returns
 */
function AddPayloadProperty(props) {
    const {
        operationsDispatcher, target, verb,
    } = props;

    /**
     *
     * @param {*} state
     * @param {*} action
     */
    function newPropertyReducer(state, action) {
        const { type, value } = action;
        const nextState = { ...state };
        switch (type) {
            case 'name':
            case 'description':
            case 'type':
                nextState[type] = value;
                break;
            case 'clear':
                return {
                    name: '',
                    description: '',
                    type: '',
                };
            default:
                return nextState;
        }
        return nextState;
    }

    const [property, newPropertyDispatcher] = useReducer(newPropertyReducer, { });
    const classes = useStyles();

    /**
     * Get supported data types for properies.
     * @returns {*} data types
     */
    function getSupportedDataTypes() {
        return ['integer', 'string', 'long', 'double', 'float'];
    }

    /**
     * Clear inputs
     */
    function clearInputs() {
        newPropertyDispatcher({ type: 'clear' });
    }

    /**
     * Add new property
     */
    function addNewProperty() {
        operationsDispatcher({
            action: 'addPayloadProperty',
            data: {
                target, verb, value: property,
            },
        });
        clearInputs();
    }

    return (
        <Grid container direction='row' spacing={1} className={classes.parameterContainer}>
            <Grid item xs={2} md={2}>
                <TextField
                    id='parameter-name'
                    label='Name'
                    name='name'
                    value={property.name}
                    onChange={({ target: { name, value } }) => newPropertyDispatcher({ type: name, value })}
                    helperText='Enter property name'
                    margin='dense'
                    variant='outlined'
                    onKeyPress={(event) => {
                        if (event.key === 'Enter') {
                            // key code 13 is for `Enter` key
                            event.preventDefault(); // To prevent form submissions
                            addNewProperty();
                        }
                    }}
                />
            </Grid>
            <Grid item xs={2} md={2}>
                <FormControl margin='dense' variant='outlined' className={classes.formControl}>
                    {/* <InputLabel ref={inputLabel} htmlFor='data-type' error={isParameterExist}> */}
                    <InputLabel htmlFor='data-type'>
                        <FormattedMessage
                            id='Apis.Details.Resources.components.operationComponents.data.type'
                            defaultMessage='Data Type'
                        />
                    </InputLabel>

                    <Select
                        value={property.type}
                        onChange={({ target: { name, value } }) => newPropertyDispatcher({ type: name, value })}
                        inputProps={{
                            name: 'type',
                            id: 'data-type',
                        }}
                        MenuProps={{
                            getContentAnchorEl: null,
                            anchorOrigin: {
                                vertical: 'bottom',
                                horizontal: 'left',
                            },
                        }}
                    >
                        {
                            getSupportedDataTypes().map((dataType) => {
                                return (
                                    <MenuItem value={dataType} dense>
                                        {dataType}
                                    </MenuItem>
                                );
                            })
                        }
                    </Select>
                    <FormHelperText id='my-helper-text'>Select the data type</FormHelperText>
                </FormControl>
            </Grid>
            <Grid item xs={6} md={6}>
                <TextField
                    id='parameter-description'
                    label='Description'
                    name='description'
                    value={property.description}
                    onChange={({ target: { name, value } }) => newPropertyDispatcher({ type: name, value })}
                    helperText='Enter property description'
                    margin='dense'
                    variant='outlined'
                    onKeyPress={(event) => {
                        if (event.key === 'Enter') {
                            // key code 13 is for `Enter` key
                            event.preventDefault(); // To prevent form submissions
                            addNewProperty();
                        }
                    }}
                />
            </Grid>
            <Grid item xs={2} md={2}>
                <Tooltip
                    title={(
                        <FormattedMessage
                            id='Apis.Details.Resources.components.operationComponents.AddParameter.add.tooltip'
                            defaultMessage='Add new parameter'
                        />
                    )}
                    aria-label='AddParameter'
                    placement='bottom'
                    interactive
                >
                    <span>
                        <Button
                            style={{ marginLeft: '20px', marginBottom: '15px', marginRight: '20px' }}
                            size='small'
                            variant='outlined'
                            aria-label='add'
                            color='primary'
                            onClick={addNewProperty}
                        >
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.AddParameter.add'
                                defaultMessage='Add'
                            />
                        </Button>
                    </span>
                </Tooltip>
                <sup>
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Resources.components.AddParameter.clear.inputs.tooltip'
                                defaultMessage='Clear inputs'
                            />
                        )}
                        aria-label='clear-inputs'
                        placement='bottom'
                        interactive
                    >
                        <span>
                            <IconButton
                                onClick={clearInputs}
                                size='small'
                            >
                                <ClearIcon />
                            </IconButton>
                        </span>
                    </Tooltip>
                </sup>
            </Grid>
        </Grid>
    );
}

AddPayloadProperty.propTypes = {
    operation: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default React.memo(injectIntl(AddPayloadProperty));
