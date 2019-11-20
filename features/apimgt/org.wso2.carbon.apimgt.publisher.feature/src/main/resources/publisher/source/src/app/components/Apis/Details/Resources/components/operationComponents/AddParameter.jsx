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
import React, { useState, useRef, useReducer } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
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
import { capitalizeFirstLetter } from 'AppData/stringFormatter';

const useStyles = makeStyles(() => ({
    formControl: {
        minWidth: 120,
    },
}));

const SUPPORTED_PARAM_TYPES = ['query', 'header', 'cookie', 'body']; // 'Path'

/**
 *
 * Add resource parameter according to swagger spec
 * A unique parameter is defined by a combination of a name and param type and schema or content is required
 * OpenAPI 3.0 spec: https://swagger.io/specification/#parameterObject
 *
 * @export
 * @param {*} props
 * @returns
 */
function AddParameter(props) {
    const {
        operation, operationsDispatcher, target, verb,
    } = props;
    const inputLabel = useRef(null);
    const [labelWidth, setLabelWidth] = useState(0);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);
    // For more info about Data Models (Schemas) refer https://swagger.io/docs/specification/data-models/
    const initParameter = { in: '', name: '', schema: { type: 'string' } };

    /**
     *
     *
     * @param {*} state
     * @param {*} action
     * @returns
     */
    function newParameterReducer(state, action) {
        const { type, value } = action;
        switch (type) {
            case 'in':
            case 'name':
                return { ...state, [type]: value };
            case 'clear':
                return initParameter;
            default:
                return state;
        }
    }
    const [newParameter, newParameterDispatcher] = useReducer(newParameterReducer, initParameter);
    React.useEffect(() => {
        setLabelWidth(inputLabel.current.offsetWidth);
    }, []);

    let isParameterExist = false;
    const isParameterExistValue = operation.parameters && operation.parameters.map(
        (operations) => (operations.in === newParameter.in && operations.name === newParameter.name),
    );

    if (isParameterExistValue && isParameterExistValue.includes(true)) {
        isParameterExist = true;
    } else {
        isParameterExist = false;
    }

    const classes = useStyles();

    /**
     *
     *
     */
    function clearInputs() {
        newParameterDispatcher({ type: 'clear' });
    }
    /**
     *
     *
     */
    function addNewParameter() {
        if (newParameter.in === 'body') {
            operationsDispatcher({
                action: 'requestBody',
                data: {
                    target,
                    verb,
                    value: {
                        description: '',
                        required: false,
                        content: { [newParameter.name]: { schema: { type: 'object' } } },
                    },
                },
            });
        } else {
            operationsDispatcher({ action: 'parameter', data: { target, verb, value: newParameter } });
        }
        clearInputs();
    }
    return (
        <Grid container direction='row' spacing={1} justify='center' alignItems='center'>
            <Grid item xs={4} md={3}>
                <FormControl margin='dense' variant='outlined' className={classes.formControl}>
                    <InputLabel ref={inputLabel} htmlFor='param-in' error={isParameterExist}>
                        Parameter Type
                    </InputLabel>

                    <Select
                        value={newParameter.in}
                        onChange={({ target: { name, value } }) => newParameterDispatcher({ type: name, value })}
                        labelWidth={labelWidth}
                        inputProps={{
                            name: 'in',
                            id: 'param-in',
                        }}
                        MenuProps={{
                            getContentAnchorEl: null,
                            anchorOrigin: {
                                vertical: 'bottom',
                                horizontal: 'left',
                            },
                        }}
                        error={isParameterExist}
                    >
                        {SUPPORTED_PARAM_TYPES.map((paramType) => {
                            if (paramType === 'body' && !['post', 'put', 'patch'].includes(verb)) {
                                return null;
                            }
                            return (
                                <MenuItem value={paramType} dense>
                                    {capitalizeFirstLetter(paramType)}
                                </MenuItem>
                            );
                        })}
                    </Select>
                    {isParameterExist
                        ? (<FormHelperText id='my-helper-text' error>Parameter type already exists</FormHelperText>)
                        : (<FormHelperText id='my-helper-text'>Select the parameter type</FormHelperText>) }

                </FormControl>
            </Grid>
            <Grid item xs={4} md={4}>
                <TextField
                    id='parameter-name'
                    label={newParameter.in === 'body' ? 'Content Type' : 'Name'}
                    name='name'
                    value={newParameter.name}
                    onChange={({ target: { name, value } }) => newParameterDispatcher({ type: name, value })}
                    helperText={isParameterExist ? 'Parameter name already exists'
                        : iff(
                            newParameter.in === 'body',
                            'Enter content type', 'Enter parameter name',
                        )}
                    fullWidth
                    margin='dense'
                    variant='outlined'
                    onKeyPress={(event) => {
                        if (event.key === 'Enter') {
                            // key code 13 is for `Enter` key
                            event.preventDefault(); // To prevent form submissions
                            addNewParameter();
                        }
                    }}
                    error={isParameterExist}
                />
            </Grid>
            <Grid item xs={4} md={5}>
                <Tooltip
                    title={(
                        <FormattedMessage
                            id='Apis.Details.Resources.components.AddParameter.add.tooltip'
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
                            disabled={!newParameter.in || !newParameter.name || isParameterExist}
                            size='small'
                            variant='outlined'
                            aria-label='add'
                            onClick={addNewParameter}
                        >
                            Add
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
                            <IconButton onClick={clearInputs} size='small'>
                                <ClearIcon />
                            </IconButton>
                        </span>
                    </Tooltip>
                </sup>
            </Grid>
        </Grid>
    );
}

AddParameter.propTypes = {
    operation: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
};

export default React.memo(AddParameter);
