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
function ImportConfig(props) {
    const intl = useIntl();
    const classes = useStyles();
    const { callBack } = props;

    const [url, setUrl] = useState('');
    const [validating, setValidating] = useState(false);
    const onChange = (e) => {
        const { target: { name: field, value: fieldValue } } = e;
        if (field === 'url') setUrl(fieldValue);
    };

    const hasErrors = (fieldName, fieldValue, validatingActive) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'url':
                error = fieldValue === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const formHasErrors = (validatingActive = false) => {
        if (hasErrors('url', url, validatingActive)
        ) {
            return true;
        } else {
            return false;
        }
    };
    const formSaveCallback = () => {
        setValidating(true);
        if (!formHasErrors(true)) {
            return ((setOpen) => {
                callBack();
                setOpen(false);
            });
        }
        return false;
    };
    return (
        <FormDialogBase
            title={intl.formatMessage({
                id: 'KeyManagers.ImportConfig.dialog.tilte.add.new',
                defaultMessage: 'Import Key Manager Configuration',
            })}
            saveButtonText={intl.formatMessage({
                id: 'KeyManagers.ImportConfig.dialog.btn.import',
                defaultMessage: 'Import',
            })}
            triggerButtonText={intl.formatMessage({
                id: 'KeyManagers.ImportConfig.dialog.trigger.import',
                defaultMessage: 'Import',
            })}
            icon={null}
            triggerButtonProps={{
                color: 'primary',
                variant: 'contained',
                size: 'small',
            }}
            formSaveCallback={formSaveCallback}
        >
            <TextField
                margin='dense'
                name='url'
                value={url}
                onChange={onChange}
                label={(
                    <span>
                        <FormattedMessage
                            id='KeyManagers.ImportConfig.form.url'
                            defaultMessage='Url'
                        />
                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                multiline
                helperText={hasErrors('url', url, validating) || intl.formatMessage({
                    id: 'KeyManagers.ImportConfig.form.url.help',
                    defaultMessage: 'Provide Url',
                })}
                variant='outlined'
                error={hasErrors('url', url, validating)}
            />
        </FormDialogBase>
    );
}
ImportConfig.propTypes = {
    dataRow: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default ImportConfig;
