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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import EditIcon from '@material-ui/icons/Edit';
import SaveIcon from '@material-ui/icons/Save';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { injectIntl } from 'react-intl';

const useStyles = makeStyles(theme => ({
    addProperty: {
        marginRight: theme.spacing.unit * 2,
    },
    buttonIcon: {
        marginRight: 10,
    },
    link: {
        cursor: 'pointer',
    },
}));

/**
 *
 * @param {*} props properties
 */
function EditableRow(props) {
    const {
        oldKey, oldValue, handleUpdateList, handleDelete, apiAdditionalProperties, intl,
    } = props;
    const [newKey, setKey] = useState(null);
    const [newValue, setValue] = useState(null);
    const [editMode, setEditMode] = useState(false);

    const updateEditMode = function () {
        setEditMode(!editMode);
    };
    const handleKeyChange = (event) => {
        const { value } = event.target;
        setKey(value);
    };
    const handleValueChange = (event) => {
        const { value } = event.target;
        setValue(value);
    };
    const validateEmpty = function (itemValue) {
        if (itemValue === null) {
            return false;
        } else if (itemValue === '') {
            return true;
        } else {
            return false;
        }
    };
    const saveRow = function () {
        const oldRow = { oldKey, oldValue };
        const newRow = { newKey: newKey || oldKey, newValue: newValue || oldValue };
        handleUpdateList(oldRow, newRow);
        setEditMode(false);
    };
    const deleteRow = function () {
        handleDelete(apiAdditionalProperties, oldKey);
    };
    const handleKeyDown = function (e) {
        if (e.key === 'Enter') {
            saveRow();
        }
    };
    const classes = useStyles();

    return (
        <TableRow>
            {editMode ? (
                <TableCell>
                    <TextField
                        required
                        id='outlined-required'
                        label={intl.formatMessage({
                            id: 'Apis.Details.Properties.Properties.editable.row.property.name',
                            defaultMessage: 'Property Name',
                        })}
                        margin='normal'
                        variant='outlined'
                        className={classes.addProperty}
                        value={newKey || oldKey}
                        onChange={handleKeyChange}
                        onKeyDown={handleKeyDown}
                        error={validateEmpty(newKey)}
                    />
                </TableCell>
            ) : (
                <TableCell>{oldKey}</TableCell>
            )}
            {editMode ? (
                <TableCell>
                    <TextField
                        required
                        id='outlined-required'
                        label={intl.formatMessage({
                            id: 'Apis.Details.Properties.Properties.editable.row.edit.mode.property.name',
                            defaultMessage: 'Property Name',
                        })}
                        margin='normal'
                        variant='outlined'
                        className={classes.addProperty}
                        value={newValue || oldValue}
                        onChange={handleValueChange}
                        onKeyDown={handleKeyDown}
                        error={validateEmpty(newValue)}
                    />
                </TableCell>
            ) : (
                <TableCell>{oldValue}</TableCell>
            )}
            <TableCell align='right'>
                {editMode ? (
                    <React.Fragment>
                        <a className={classes.link} onClick={saveRow} onKeyDown={() => {}}>
                            <SaveIcon className={classes.buttonIcon} />
                        </a>
                    </React.Fragment>
                ) : (
                    <a className={classes.link} onClick={updateEditMode} onKeyDown={() => {}}>
                        <EditIcon className={classes.buttonIcon} />
                    </a>
                )}
                <a className={classes.link} onClick={deleteRow} onKeyDown={() => {}}>
                    <DeleteForeverIcon className={classes.buttonIcon} />
                </a>
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
};

export default injectIntl(EditableRow);
