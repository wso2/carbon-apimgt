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

import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import deburr from 'lodash/deburr';
import Downshift from 'downshift';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import MenuItem from '@material-ui/core/MenuItem';
import InputAdornment from '@material-ui/core/InputAdornment';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';

/**
 * The renderInput function.
 * @param {any} inputProps The props that are being passed to the function.
 * @returns {any} HTML view of the inputs.
 */
function renderInput(inputProps) {
    const {
        InputProps, classes, ref, ...other
    } = inputProps;
    return (
        <TextField
            InputProps={{
                inputRef: ref,
                classes: {
                    root: classes.inputRoot,
                    input: classes.inputInput,
                },
                ...InputProps,
            }}
            {...other}
        />
    );
}

renderInput.propTypes = {
    // eslint-disable-next-line react/forbid-prop-types
    classes: PropTypes.shape({}).isRequired,
    // eslint-disable-next-line react/forbid-prop-types, react/require-default-props
    InputProps: PropTypes.shape({}),
};

/**
 * The renderSuggestion function.
 * @param {any} suggestionProps The props that are being passed to the function.
 * @returns {any} HTML view of the suggestions.
 */
function renderSuggestion(suggestionProps) {
    const {
        suggestion, index, itemProps, highlightedIndex, selectedItem,
    } = suggestionProps;
    const isHighlighted = highlightedIndex === index;
    const isSelected = (selectedItem || '').indexOf(suggestion) > -1;

    return (
        <MenuItem
            {...itemProps}
            key={suggestion}
            selected={isHighlighted}
            component='div'
            style={{
                fontWeight: isSelected ? 500 : 400,
            }}
        >
            {suggestion}
        </MenuItem>
    );
}

renderSuggestion.propTypes = {
    highlightedIndex: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.number]).isRequired,
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    itemProps: PropTypes.shape({}).isRequired,
    selectedItem: PropTypes.string.isRequired,
    suggestion: PropTypes.shape('').isRequired,
};

/**
 * The getSuggestions function.
 * @param {any} value The value that are being passed to the function.
 * @param {any} arns The arns that are being passed to the function.
 * @returns {any} suggestion values.
 */
function getSuggestions(value, { showEmpty = false } = {}, arns) {
    const inputValue = deburr(value.trim()).toLowerCase();
    const inputLength = inputValue.length;
    let count = 0;
    return inputLength === 0 && !showEmpty
        ? []
        : arns.filter((suggestion) => {
            const keep = count < 5 && suggestion.slice(0, inputLength).toLowerCase() === inputValue;
            if (keep) {
                count += 1;
            }
            return keep;
        });
}

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
    },
    container: {
        flexGrow: 1,
        position: 'relative',
    },
    paper: {
        position: 'absolute',
        zIndex: 1000,
        marginTop: theme.spacing(-2),
        left: 0,
        right: 0,
    },
    chip: {
        margin: theme.spacing(0.5, 0.25),
    },
    inputRoot: {
        flexWrap: 'wrap',
    },
    inputInput: {
        width: 'auto',
        flexGrow: 1,
    },
}));

/**
 * The autocomplete component. This component lists the ARNs of a specific user role.
 * @returns {any} HTML view of the autocomplete component.
 * @param {any} props The input parameters.
 */
export default function IntegrationDownshift(props) {
    const classes = useStyles();
    const {
        operation,
        operationsDispatcher,
        target,
        verb,
        arns,
    } = props;
    const [timeout, setTimeout] = useState(50000);
    useEffect(() => {
        if (operation['x-amzn-resource-timeout']) {
            setTimeout(operation['x-amzn-resource-timeout']);
        }
    }, []);
    const handleTimeoutMin = (event) => {
        if (event.target.value !== '') {
            const minutes = parseInt(event.target.value, 10);
            const seconds = (timeout / 1000) % 60;
            const milliSeconds = (minutes * 60 + seconds) * 1000;
            let newTimeout = 0;
            if (milliSeconds > 900000) {
                newTimeout = 900000;
            } else if (milliSeconds < 1000) {
                newTimeout = 1000;
            } else {
                newTimeout = milliSeconds;
            }
            setTimeout(newTimeout);
            operationsDispatcher({
                action: 'amznResourceTimeout',
                data: { target, verb, value: newTimeout },
            });
        }
    };
    const handleTimeoutSec = (event) => {
        if (event.target.value !== '') {
            const minutes = Math.floor((timeout / 1000) / 60);
            const seconds = parseInt(event.target.value, 10);
            const milliSeconds = (minutes * 60 + seconds) * 1000;
            let newTimeout = 0;
            if (milliSeconds > 900000) {
                newTimeout = 900000;
            } else if (milliSeconds < 1000) {
                newTimeout = 1000;
            } else {
                newTimeout = milliSeconds;
            }
            setTimeout(newTimeout);
            operationsDispatcher({
                action: 'amznResourceTimeout',
                data: { target, verb, value: newTimeout },
            });
        }
    };
    return (
        <>
            <Grid item md={12} xs={12}>
                <Typography variant='subtitle1'>
                    <FormattedMessage
                        id='Apis.Details.Resources.components.operationComponents.AWSLambdaSettings.Title'
                        defaultMessage='AWS Lambda Settings'
                    />
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} xs={1} />
            <Grid item md={7} xs={7}>
                <Downshift
                    id='downshift-options'
                    onSelect={(changes) => {
                        if (changes !== null) {
                            operationsDispatcher({
                                action: 'amznResourceName',
                                data: { target, verb, value: changes },
                            });
                        }
                    }}
                >
                    {({
                        clearSelection,
                        getInputProps,
                        getItemProps,
                        getLabelProps,
                        getMenuProps,
                        highlightedIndex,
                        inputValue,
                        isOpen,
                        openMenu,
                        selectedItem,
                    }) => {
                        const {
                            onBlur, onChange, onFocus, ...inputProps
                        } = getInputProps({
                            onChange: (event) => {
                                operationsDispatcher({
                                    action: 'amznResourceName',
                                    data: { target, verb, value: event.target.value },
                                });
                                if (event.target.value === '') {
                                    clearSelection();
                                }
                            },
                            onBlur: () => {
                                clearSelection();
                            },
                            value: operation['x-amzn-resource-name'],
                            onFocus: openMenu,
                            placeholder: 'Select or type an ARN',
                        });
                        return (
                            <div className={classes.container}>
                                {renderInput({
                                    variant: 'outlined',
                                    required: true,
                                    fullWidth: true,
                                    classes,
                                    InputLabelProps: getLabelProps({ shrink: true }),
                                    label: 'Amazon Resource Name (ARN)',
                                    helperText: 'Select or type an ARN',
                                    InputProps: {
                                        onBlur,
                                        onChange,
                                        onFocus,
                                        endAdornment: (
                                            <InputAdornment position='end'>
                                                <Icon className={classes.helpIcon}>keyboard_arrow_down</Icon>
                                            </InputAdornment>
                                        ),
                                    },
                                    inputProps,
                                })}

                                <div {...getMenuProps()}>
                                    {isOpen ? (
                                        <Paper className={classes.paper} square>
                                            {getSuggestions(inputValue, { showEmpty: true }, arns)
                                                .map((suggestion, index) => renderSuggestion({
                                                    suggestion,
                                                    index,
                                                    itemProps: getItemProps({ item: suggestion }),
                                                    highlightedIndex,
                                                    selectedItem,
                                                }))}
                                        </Paper>
                                    ) : null}
                                </div>
                            </div>
                        );
                    }}
                </Downshift>
            </Grid>
            <Grid item md={1} xs={1} />
            <Grid item md={1} xs={1}>
                <TextField
                    id='timeout-min'
                    label='min'
                    variant='outlined'
                    helperText='Set Timeout'
                    type='number'
                    inputProps={{
                        min: 0,
                        max: 15,
                        step: 1,
                    }}
                    value={Math.floor((timeout / 1000) / 60)}
                    onChange={(event) => {
                        handleTimeoutMin(event);
                    }}
                />
            </Grid>
            <Grid item md={1} xs={1}>
                <TextField
                    id='timeout-sec'
                    label='sec'
                    variant='outlined'
                    type='number'
                    inputProps={{
                        min: 0,
                        max: 59,
                        step: 1,
                    }}
                    value={(timeout / 1000) % 60}
                    onChange={(event) => {
                        handleTimeoutSec(event);
                    }}
                />
            </Grid>
            <Grid item md={1} xs={1} />
        </>
    );
}

IntegrationDownshift.propTypes = {
    operation: PropTypes.isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    arns: PropTypes.shape([]).isRequired,
};
