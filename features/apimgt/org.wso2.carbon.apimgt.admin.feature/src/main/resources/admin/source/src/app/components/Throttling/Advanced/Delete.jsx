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

import React from 'react';
import PropTypes from 'prop-types';
import DialogContentText from '@material-ui/core/DialogContentText';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import { useIntl, FormattedMessage } from 'react-intl';
import API from 'AppData/api';

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function Delete({ updateList, dataRow }) {
    const { policyId, displayName } = dataRow;
    const intl = useIntl();
    const formSaveCallback = () => {
        // Do the API call
        const restApi = new API();
        return restApi
            .deleteThrottlingPoliciesAdvanced(policyId)
            .then(() => {
                updateList();
                return `${displayName} ${intl.formatMessage({
                    id: 'Throttling.Advanced.Delete.success',
                    defaultMessage: 'Policy Deleted Successfully',
                })}`;
            })
            .catch((e) => {
                return (e);
            });
    };

    return (
        <FormDialogBase
            title={intl.formatMessage({
                id: 'Throttling.Advanced.Delete.title',
                defaultMessage: 'Delete Policy',
            })}
            saveButtonText={intl.formatMessage({
                id: 'Throttling.Advanced.Delete.save.text',
                defaultMessage: 'Delete',
            })}
            icon={<DeleteForeverIcon />}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                <FormattedMessage
                    id='Throttling.Advanced.Delete.confirm.text'
                    defaultMessage={'Policy deletion might affect current subscriptions.'
                    + ' Are you sure you want to delete this policy?'}
                />
            </DialogContentText>
        </FormDialogBase>
    );
}
Delete.propTypes = {
    updateList: PropTypes.number.isRequired,
    dataRow: PropTypes.shape({
        policyId: PropTypes.number.isRequired,
        displayName: PropTypes.string.isRequired,
    }).isRequired,
};
export default Delete;
