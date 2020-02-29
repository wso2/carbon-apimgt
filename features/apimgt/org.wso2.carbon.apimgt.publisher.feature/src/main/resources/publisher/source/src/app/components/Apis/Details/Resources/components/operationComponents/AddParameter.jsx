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
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import {
    iff,
    getParameter,
    getParameterTypes,
    getSupportedDataTypes,
} from 'AppComponents/Apis/Details/Resources/components/operationComponents/parameterUtils';

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
        operation, operationsDispatcher, target, verb, specVersion, intl,
    } = props;
    const inputLabel = useRef(null);
    const [labelWidth, setLabelWidth] = useState(0);
    const initParameter = getParameter(specVersion);

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
            case 'required':
                return { ...state, [type]: value };
            case 'type': {
                if (specVersion === '2.0') {
                    return { ...state, [type]: value };
                } else {
                    return { ...state, schema: { [type]: value } };
                }
            }
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
            operationsDispatcher(specVersion === '2.0'
                ? {
                    action: 'parameter',
                    data: {
                        target,
                        verb,
                        value: {
                            name: newParameter.name,
                            description: '',
                            required: newParameter.required,
                            in: newParameter.in,
                            schema: {
                                type: newParameter.type,
                            },
                        },
                    },
                }
                : {
                    action: 'requestBody',
                    data: {
                        target,
                        verb,
                        value: {
                            description: '',
                            required: newParameter.required,
                            content: { [newParameter.name]: { schema: { type: 'object' } } },
                        },
                    },
                });
        } else {
            operationsDispatcher({ action: 'parameter', data: { target, verb, value: newParameter } });
        }
        clearInputs();
    }

    function isDisabled() {
        if (specVersion === '2.0') {
            return (!newParameter.name || !newParameter.type || !newParameter.in || isParameterExist);
        }
        return (!newParameter.name || !newParameter.schema.type || !newParameter.in || isParameterExist);
    }

    function getParameterNameHelperText(paramIn) {
        if (isParameterExist) {
            return intl.formatMessage({
                id: 'Apis.Details.Resources.components.operationComponents.AddParameter.parameter.name.already.exists',
                defaultMessage: 'Parameter name already exists',
            });
        }
        if (paramIn === 'body') {
            if (specVersion !== '2.0') {
                return intl.formatMessage({
                    id: 'Apis.Details.Resources.components.operationComponents.AddParameter.enter.content.type',
                    defaultMessage: 'Enter Content Type',
                });
            }
        }
        return intl.formatMessage({
            id: 'Apis.Details.Resources.components.operationComponents.AddParameter.enter.parameter.name',
            defaultMessage: 'Enter Parameter Name',
        });
    }

    return (
        <Grid container direction='row' spacing={1} className={classes.parameterContainer}>
            <Grid item xs={2} md={2}>
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
                        {getParameterTypes(specVersion).map((paramType) => {
                            if ((paramType === 'body' || paramType === 'formData')
                                && !['post', 'put', 'patch'].includes(verb)) {
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
                        ? (
                            <FormHelperText id='my-helper-text' error>
                                <FormattedMessage
                                    id='Apis.Details.Resources.components.operationComponents.parameter.name.exists'
                                    defaultMessage='Parameter type already exists'
                                />
                            </FormHelperText>
                        )
                        : (
                            <FormHelperText id='my-helper-text'>
                                <FormattedMessage
                                    id='Apis.Details.Resources.components.operationComponents.select.parameter.type'
                                    defaultMessage='Select the parameter type'
                                />
                            </FormHelperText>
                        )}
                </FormControl>
            </Grid>
            <Grid item xs={2} md={2}>
                <TextField
                    id='parameter-name'
                    label={newParameter.in === 'body'
                        ? iff(specVersion === '2.0',
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.parameter.name'
                                defaultMessage='Parameter Name'
                            />,
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.content.type'
                                defaultMessage='Content Type'
                            />)
                        : (
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.parameter.name'
                                defaultMessage='Parameter Name'
                            />
                        )}
                    name='name'
                    value={newParameter.name}
                    onChange={({ target: { name, value } }) => newParameterDispatcher({ type: name, value })}
                    helperText={getParameterNameHelperText(newParameter.in)}
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
            <Grid item xs={2} md={2}>
                <FormControl margin='dense' variant='outlined' className={classes.formControl}>
                    <InputLabel ref={inputLabel} htmlFor='data-type' error={isParameterExist}>
                        <FormattedMessage
                            id='Apis.Details.Resources.components.operationComponents.data.type'
                            defaultMessage='Data Type'
                        />
                    </InputLabel>

                    <Select
                        value={newParameter.schema !== undefined ? newParameter.schema.type : newParameter.type}
                        onChange={({ target: { name, value } }) => newParameterDispatcher({ type: name, value })}
                        labelWidth={labelWidth}
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
                        error={isParameterExist}
                    >
                        {getSupportedDataTypes(specVersion, newParameter.in).map((paramType) => {
                            return (
                                <MenuItem value={paramType} dense>
                                    {capitalizeFirstLetter(paramType)}
                                </MenuItem>
                            );
                        })}
                    </Select>
                    <FormHelperText id='my-helper-text'>Select the data type</FormHelperText>
                </FormControl>
            </Grid>
            <Grid item xs={2} md={2}>
                <FormControl component='fieldset' className={classes.formControl}>
                    <FormControlLabel
                        className={classes.checkBox}
                        control={(
                            <Checkbox
                                checked={newParameter.required}
                                onChange={
                                    ({
                                        target: { name, value },
                                    }) => newParameterDispatcher({ type: name, value: !value })
                                }
                                value={newParameter.required}
                                inputProps={{
                                    name: 'required',
                                }}
                            />
                        )}
                        label={(
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.required'
                                defaultMessage='Required'
                            />
                        )}
                    />
                    <FormHelperText>
                        <FormattedMessage
                            id='Apis.Details.Resources.components.operationComponents.required.helper.text'
                            defaultMessage='Check whether the parameter is required.'
                        />
                    </FormHelperText>
                </FormControl>
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
                            disabled={isDisabled()}
                            size='small'
                            variant='outlined'
                            aria-label='add'
                            color='primary'
                            onClick={addNewParameter}
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
    specVersion: PropTypes.string.isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default React.memo(injectIntl(AddParameter));
