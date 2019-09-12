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
// import TextField from '@material-ui/core/TextField';
import FormControl from '@material-ui/core/FormControl';
import { FormattedMessage } from 'react-intl';
import { withStyles, Switch, FormControlLabel } from '@material-ui/core';
import PropTypes from 'prop-types';
import API from 'AppData/api.js';
import Alert from 'AppComponents/Shared/Alert';
import EditOutMediationPolicy from './Edit/EditOutMediationPolicy';
import EngagedOutMediationPolicy from './Engaged/EngagedOutMediationPolicy';

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
function OutFlow(props) {
    const { api, classes, updateMediationPolicy } = props;
    const { mediationPolicies } = api;
    const type = 'OUT';
    const outMediationPolicy = mediationPolicies.filter(seq => seq.type === type)[0];
    const [engagedPolicyFile, setEngagedPolicyFile] = useState({
        id: outMediationPolicy !== (null || undefined) ? outMediationPolicy.id : '',
        name: outMediationPolicy !== (null || undefined) ? outMediationPolicy.name : '',
        type: outMediationPolicy !== (null || undefined) ? outMediationPolicy.type : '',
        content: {},
    });
    const [editable, setEditable] = useState(false);
    const handleInputChange = (event) => {
        const policy = event.target.value;
        setEngagedPolicyFile(policy);
    };
    const handleToggleOutFlowEdit = (event) => {
        const isEditable = event.target.checked;
        setEditable(isEditable);
    };
    useEffect(() => {
        if (!editable) {
            API.get(api.id).then((response) => {
                const outPolicy = response.mediationPolicies.filter(seq => seq.type === type)[0];
                setEngagedPolicyFile({
                    id: outPolicy !== (null || undefined) ? outPolicy.id : '',
                    name: outPolicy !== (null || undefined) ? outPolicy.name : '',
                    type: outPolicy !== (null || undefined) ? outPolicy.type : '',
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
                control={<Switch color='primary' />}
                label={<FormattedMessage
                    id='Apis.Details.MediationPolicies.out.flow.desc'
                    defaultMessage='Out Flow'
                />}
                labelPlacement='start'
                onChange={handleToggleOutFlowEdit}
            />
            {!editable ? (
                <EngagedOutMediationPolicy
                    engagedPolicyFile={engagedPolicyFile}
                    handleInputChange={handleInputChange}
                    api={api}
                />
            )
                : (
                    <div className={classes.itemWrapper}>
                        <EditOutMediationPolicy
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
OutFlow.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
};

export default withStyles(styles)(OutFlow);
