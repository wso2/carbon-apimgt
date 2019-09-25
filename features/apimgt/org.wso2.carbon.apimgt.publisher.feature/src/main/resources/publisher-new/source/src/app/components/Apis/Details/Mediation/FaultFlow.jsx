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
import FormControl from '@material-ui/core/FormControl';
import { FormattedMessage } from 'react-intl';
import { withStyles, Switch, FormControlLabel } from '@material-ui/core';
import { isRestricted } from 'AppData/AuthManager';
import PropTypes from 'prop-types';
import API from 'AppData/api.js';
import Alert from 'AppComponents/Shared/Alert';
import EngagedFaultMediationPolicy from './Engaged/EngagedFaultMediationPolicy';
import EditFaultMediationPolicy from './Edit/EditFaultMediationPolicy';

const styles = {
    content: {
        flexGrow: 1,
    },
    itemWrapper: {
        width: 500,
    },
};

/**
 * The base component of the mediation policy view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function FaultFlow(props) {
    const { api, classes, updateMediationPolicy } = props;
    const { mediationPolicies } = api;
    const type = 'FAULT';
    const faultMediationPolicy = mediationPolicies.filter(seq => seq.type === type)[0];
    const [engagedPolicyFile, setEngagedPolicyFile] = useState({
        id: faultMediationPolicy !== (null || undefined) ? faultMediationPolicy.id : '',
        name: faultMediationPolicy !== (null || undefined) ? faultMediationPolicy.name : '',
        type: faultMediationPolicy !== (null || undefined) ? faultMediationPolicy.type : '',
        content: {},
    });
    const [editable, setEditable] = useState(false);

    const handleInputChange = (event) => {
        const policy = event.target.value;
        setEngagedPolicyFile(policy);
    };
    const handleToggleFaultFlowEdit = (event) => {
        const isEditable = event.target.checked;
        setEditable(isEditable);
    };
    useEffect(() => {
        if (!editable) {
            API.get(api.id).then((response) => {
                const faultPolicy = response.mediationPolicies.filter(seq => seq.type === type)[0];
                setEngagedPolicyFile({
                    id: faultPolicy !== (null || undefined) ? faultPolicy.id : '',
                    name: faultPolicy !== (null || undefined) ? faultPolicy.name : '',
                    type: faultPolicy !== (null || undefined) ? faultPolicy.type : '',
                    content: {},
                });
            }).catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(<FormattedMessage
                        id='Apis.Details.MediationPolicies.Edit.EditOutMediationPolicy.global.error'
                        defaultMessage='Error retrieving API mediation policies'
                    />);
                }
            });
        }
    }, [editable]);
    return (
        <FormControl className={classes.formControl}>
            <FormControlLabel
                value='start'
                checked={editable}
                control={
                    <Switch
                        color='primary'
                        disabled={isRestricted(['apim:api_create'], api)}
                    />
                }
                label={<FormattedMessage
                    id='Apis.Details.MediationPolicies.MediationPolicies.fault.flow.desc'
                    defaultMessage='Fault Flow'
                />}
                labelPlacement='start'
                onChange={handleToggleFaultFlowEdit}
            />
            {!editable ? (
                <EngagedFaultMediationPolicy
                    engagedPolicyFile={engagedPolicyFile}
                    handleInputChange={handleInputChange}
                    api={api}
                />
            )
                : (
                    <div className={classes.itemWrapper}>
                        <EditFaultMediationPolicy
                            handleInputChange={handleInputChange}
                            engagedPolicyFile={engagedPolicyFile}
                            api={api}
                            updateMediationPolicy={updateMediationPolicy}
                        />
                    </div>
                )}
        </FormControl>
    );
}
FaultFlow.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
};

export default withStyles(styles)(FaultFlow);
