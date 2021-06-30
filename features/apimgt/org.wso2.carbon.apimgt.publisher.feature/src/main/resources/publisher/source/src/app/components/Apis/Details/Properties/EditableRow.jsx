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
/* eslint no-param-reassign: ["error", { "props": false }] */
/* eslint-disable react/jsx-no-bind */

import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import EditIcon from '@material-ui/icons/Edit';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { injectIntl, FormattedMessage } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import VisibilityIcon from '@material-ui/icons/Visibility';

const useStyles = makeStyles(() => ({
    link: {
        cursor: 'pointer',
    },
    checkBoxStyles: {
        whiteSpace: 'nowrap',
        marginLeft: 0,
        paddingLeft: 0,
    },
    colorPrimary: {
        marginLeft: 0,
        paddingLeft: 0,
    },
    cancelButton: {
        marginLeft: 4,
    },
}));

/**
 *
 * @param {*} props properties
 */
function EditableRow(props) {
    const {
        oldKey, oldValue, handleUpdateList, handleDelete, intl, setEditing, isRestricted, api,
        isDisplayInStore, isKeyword, validateEmpty,
    } = props;
    const [newKey, setKey] = useState(oldKey);
    const [newValue, setValue] = useState(oldValue);
    const [editMode, setEditMode] = useState(false);
    const [isVisibleInStore, setIsVisibleInStore] = useState(isDisplayInStore);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    const resetText = () => {
        setIsVisibleInStore(isDisplayInStore);
        setKey(oldKey);
        setValue(oldValue);
    };
    useEffect(() => {
        resetText();
    }, [oldKey, oldValue]);

    const updateEditMode = () => {
        setEditMode(!editMode);
        setEditing(true);
        resetText();
    };
    const handleKeyChange = (event) => {
        const { value } = event.target;
        setKey(value);
    };
    const handleValueChange = (event) => {
        const { value } = event.target;
        setValue(value);
    };
    const saveRow = () => {
        const oldRow = { oldKey, oldValue, isDisplayInStore };
        const newRow = { newKey, newValue, display: isVisibleInStore };
        if (handleUpdateList(oldRow, newRow)) {
            setEditMode(false);
            setEditing(false);
        }
    };
    const deleteRow = () => {
        handleDelete(oldKey);
    };
    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            saveRow();
        }
    };
    const handleChangeVisibleInStore = (event) => {
        setIsVisibleInStore(event.target.checked);
    };
    const classes = useStyles();

    return (
        <TableRow>
            {editMode ? (
                <TableCell>
                    <TextField
                        fullWidth
                        required
                        id='outlined-required'
                        label={intl.formatMessage({
                            id: 'Apis.Details.Properties.Properties.editable.row.property.name',
                            defaultMessage: 'Property Name',
                        })}
                        margin='dense'
                        variant='outlined'
                        className={classes.addProperty}
                        value={newKey}
                        onChange={handleKeyChange}
                        onKeyDown={handleKeyDown}
                        helperText={validateEmpty(newKey) ? ''
                            : iff(isKeyword(newKey), intl.formatMessage({
                                id: `Apis.Details.Properties.Properties.
                                show.add.property.invalid.error`,
                                defaultMessage: 'Invalid property name',
                            }), '')}
                        error={validateEmpty(newKey) || isKeyword(newKey)}
                    />
                </TableCell>
            ) : (
                <TableCell>
                    {oldKey}
                </TableCell>
            )}
            {editMode ? (
                <>
                    <TableCell>
                        <TextField
                            fullWidth
                            required
                            id='outlined-required'
                            label={intl.formatMessage({
                                id: 'Apis.Details.Properties.Properties.editable.row.edit.mode.property.value',
                                defaultMessage: 'Property Value',
                            })}
                            margin='dense'
                            variant='outlined'
                            className={classes.addProperty}
                            value={newValue}
                            onChange={handleValueChange}
                            onKeyDown={handleKeyDown}
                            error={validateEmpty(newValue)}
                        />
                    </TableCell>
                    <TableCell>
                        <FormControlLabel
                            control={(
                                <Checkbox
                                    checked={isVisibleInStore}
                                    onChange={handleChangeVisibleInStore}
                                    name='checkedB'
                                    color='primary'
                                />
                            )}
                            label={intl.formatMessage({
                                id: 'Apis.Details.Properties.Properties.editable.show.in.devporal',
                                defaultMessage: 'Show in devportal',
                            })}
                            classes={{ root: classes.checkBoxStyles, colorPrimary: classes.colorPrimary }}
                        />
                    </TableCell>
                </>
            ) : (
                <>
                    <TableCell>
                        <Box display='inline-block' minWidth={150}>
                            {oldValue}
                        </Box>
                    </TableCell>
                    <TableCell>
                        {isVisibleInStore && (
                            <Box display='flex' alignItems='center'>
                                <VisibilityIcon />
                                <Box ml={1} display='inline-block'>
                                    <FormattedMessage
                                        id='Apis.Details.Properties.Properties.editable.visible.in.store'
                                        defaultMessage='Visible in devportal'
                                    />
                                </Box>
                            </Box>
                        )}
                    </TableCell>
                </>
            )}
            <TableCell align='right'>
                {editMode ? (
                    <>
                        <Button
                            color='primary'
                            onClick={saveRow}
                            onKeyDown={() => { }}
                            variant='contained'
                        >
                            <Typography variant='caption' component='div'>
                                <FormattedMessage
                                    id='Apis.Details.Properties.Properties.editable.update'
                                    defaultMessage='Update'
                                />
                            </Typography>
                        </Button>
                        <Button onClick={updateEditMode} className={classes.cancelButton}>
                            <Typography variant='caption' component='div'>
                                <FormattedMessage
                                    id='Apis.Details.Properties.Properties.editable.cancel'
                                    defaultMessage='Cancel'
                                />
                            </Typography>
                        </Button>

                    </>
                ) : (
                    <IconButton
                        className={classes.link}
                        aria-label='edit'
                        onClick={updateEditMode}
                        onKeyDown={() => { }}
                        color='inherit'
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                    >
                        <EditIcon className={classes.buttonIcon} />
                    </IconButton>
                )}
                <IconButton
                    className={classes.link}
                    aria-label='remove'
                    onClick={deleteRow}
                    onKeyDown={() => { }}
                    color='inherit'
                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                >
                    <DeleteForeverIcon className={classes.buttonIcon} />
                </IconButton>
            </TableCell>
        </TableRow>
    );
}
EditableRow.propTypes = {
    oldKey: PropTypes.shape({}).isRequired,
    oldValue: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    handleUpdateList: PropTypes.shape({}).isRequired,
    handleDelete: PropTypes.shape({}).isRequired,
    apiAdditionalProperties: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    setEditing: PropTypes.func.isRequired,
};

export default injectIntl(EditableRow);
