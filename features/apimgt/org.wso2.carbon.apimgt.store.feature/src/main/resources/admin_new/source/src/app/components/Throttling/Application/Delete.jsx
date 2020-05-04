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
import { injectIntl, FormattedMessage } from 'react-intl';
import API from 'AppData/api';

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function Delete(props) {
    const restApi = new API();
    const {
        selectedPolicyName, applicationThrottlingPolicyList,
        // updateList,
    } = props;

    const formSaveCallback = () => {
        const selectedPolicy = applicationThrottlingPolicyList.filter(
            (policy) => policy.policyName === selectedPolicyName,
        );
        const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;

        const promiseAPICall = restApi
            .deleteApplicationThrottlingPolicy(policyId)
            .then(() => {
                // add after chanaka aiyas fix
                // updateList();
                return (
                    <FormattedMessage
                        id='Throttling.Application.Policy.policy.delete.success'
                        defaultMessage='Application Throttling Policy successfully deleted.'
                    />
                );
            })
            .catch(() => {
                return (
                    <FormattedMessage
                        id='Throttling.Application.Policy.policy.delete.error'
                        defaultMessage='Application Throttling Policy could not be deleted.'
                    />
                );
            });

        return (promiseAPICall);
    };

    return (
        <FormDialogBase
            title='Delete Gateway Label?'
            saveButtonText='Delete'
            icon={<DeleteForeverIcon />}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                <FormattedMessage
                    id='Throttling.Application.Policy.policy.delete.error'
                    defaultMessage={selectedPolicyName + ' Application Throttling Policy will be deleted.'}
                />
            </DialogContentText>
        </FormDialogBase>
    );
}
Delete.propTypes = {
    updateList: PropTypes.number.isRequired,
    selectedPolicyName: PropTypes.shape({
        name: PropTypes.number.isRequired,
    }).isRequired,
    applicationThrottlingPolicyList: PropTypes.shape({
        applicationThrottlingPolicyList: PropTypes.array.isRequired,
    }).isRequired,
};
export default injectIntl(Delete);
