/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { useIntl, FormattedMessage } from 'react-intl';
import DialogContentText from '@material-ui/core/DialogContentText';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import TextField from '@material-ui/core/TextField';
import CreateIcon from '@material-ui/icons/Create';
import CON_CONSTS from 'AppComponents/Throttling/Advanced/CON_CONSTS';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
}));

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function AddEditConditionPolicy(props) {
    const intl = useIntl();
    const classes = useStyles();
    const { row } = props;
    const { row: { labelPrefix, description, name: conditionName }, callBack, item } = props;
    let initName = '';
    let initValue = '';
    if (item) {
        if (item.type === CON_CONSTS.HEADERCONDITION) {
            initName = item.headerCondition.headerName;
            initValue = item.headerCondition.headerValue;
        } else if (item.type === CON_CONSTS.QUERYPARAMETERCONDITION) {
            initName = item.queryParameterCondition.parameterName;
            initValue = item.queryParameterCondition.parameterValue;
        } else if (item.type === CON_CONSTS.JWTCLAIMSCONDITION) {
            initName = item.jwtClaimsCondition.claimUrl;
            initValue = item.jwtClaimsCondition.attribute;
        }
    }

    const [name, setName] = useState(initName);
    const [value, setValue] = useState(initValue);
    const [validating, setValidating] = useState(false);
    const onChange = (e) => {
        const { target: { name: field, value: fieldValue } } = e;
        switch (field) {
            case 'name':
                setName(fieldValue);
                break;
            case 'value':
                setValue(fieldValue);
                break;
            default:
                break;
        }
    };

    const hasErrors = (fieldName, fieldValue, validatingActive) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'name':
                error = fieldValue === '' ? fieldName + ' is Empty' : false;
                break;
            case 'value':
                error = fieldValue === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const formHasErrors = (validatingActive = false) => {
        if (hasErrors('name', name, validatingActive)
        || hasErrors('value', value, validatingActive)) {
            return true;
        } else {
            return false;
        }
    };
    const formSaveCallback = () => {
        setValidating(true);
        if (!formHasErrors(true)) {
            return ((setOpen) => {
                callBack(row, { name, value }, item);
                setOpen(false);
            });
        }
        return false;
    };
    return (
        <FormDialogBase
            title={item
                ? `${intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicy.dialog.tilte.add.new',
                    defaultMessage: 'Add New ',
                })}${conditionName}`
                : `${intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicy.dialog.tilte.edit',
                    defaultMessage: 'Edit ',
                })}${conditionName}`}
            saveButtonText={intl.formatMessage({
                id: 'Throttling.Advanced.AddEditConditionPolicy.dialog.btn.save',
                defaultMessage: 'Save',
            })}
            triggerButtonText={item ? null : intl.formatMessage({
                id: 'Throttling.Advanced.AddEditConditionPolicy.dialog.trigger.add',
                defaultMessage: 'Add',
            })}
            icon={item ? <CreateIcon /> : null}
            triggerButtonProps={{
                color: 'default',
                variant: 'contained',
                size: 'small',
            }}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                {description}
            </DialogContentText>
            <TextField
                margin='dense'
                name='name'
                value={name}
                onChange={onChange}
                label={(
                    <span>
                        {labelPrefix}
                        {' '}
                        <FormattedMessage
                            id='Throttling.Advanced.AddEditConditionPolicy.form.name'
                            defaultMessage='Name'
                        />
                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                multiline
                helperText={hasErrors('name', name, validating) || intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicy.form.name.help',
                    defaultMessage: 'Provide Name',
                })}
                variant='outlined'
                error={hasErrors('name', name, validating)}
            />
            <TextField
                margin='dense'
                name='value'
                value={value}
                onChange={onChange}
                label={(
                    <span>
                        {labelPrefix}
                        {' '}
                        <FormattedMessage
                            id='Throttling.Advanced.AddEditConditionPolicy.form.value'
                            defaultMessage='Value'
                        />

                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                multiline
                helperText={hasErrors('value', value, validating) || intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicy.form.value.help',
                    defaultMessage: 'Provide Value',
                })}
                variant='outlined'
                error={hasErrors('value', value, validating)}
            />
        </FormDialogBase>
    );
}
AddEditConditionPolicy.propTypes = {
    dataRow: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default AddEditConditionPolicy;
