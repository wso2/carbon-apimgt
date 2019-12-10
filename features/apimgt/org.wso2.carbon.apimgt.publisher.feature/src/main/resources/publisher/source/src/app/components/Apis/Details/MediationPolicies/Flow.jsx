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
import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { withStyles } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import { isRestricted } from 'AppData/AuthManager';
import EditMediationPolicy from './EditMediationPolicy';

const styles = {
    content: {
        flexGrow: 1,
    },
    itemWrapper: {
        width: 'auto',
        display: 'flex',
    },
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    subTitle: {
        marginTop: 20,
    },
    subTitleDescription: {
        marginBottom: 10,
    },
    flowWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    heading: {
        flex: 1,
    },
    paper: {
        padding: 10,
        width: 'auto',
    },
};

/**
 * The base component of the IN mediation policy.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function InFlow(props) {
    const {
        classes, updateMediationPolicy, selectedMediationPolicy, type, api,
    } = props;
    const [editing, setEditing] = useState(false);

    function startEditing() {
        setEditing(true);
    }
    return (
        <>
            <Typography variant='h6' align='left' className={classes.subTitle}>
                {type === 'IN' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.in.flow.title'
                        defaultMessage='In Flow'
                    />
                )}
                {type === 'OUT' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.out.flow.title'
                        defaultMessage='Out Flow'
                    />
                )}
                {type === 'FAULT' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.fault.flow.title'
                        defaultMessage='Fault Flow'
                    />
                )}
            </Typography>
            <Typography variant='caption' align='left' className={classes.subTitleDescription} component='div'>
                {type === 'IN' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.flow.in.content'
                        defaultMessage='Mediation policy engaged in the Request Flow'
                    />
                )}
                {type === 'OUT' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.flow.out.content'
                        defaultMessage='Mediation policy engaged in the Out Flow'
                    />
                )}
                {type === 'FAULT' && (
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies.flow.fault.content'
                        defaultMessage='Mediation policy engaged in the Fault Flow'
                    />
                )}
            </Typography>
            <Paper className={classes.paper}>
                <div className={classes.flowWrapper}>
                    <Typography className={classes.heading} component='div'>
                        {selectedMediationPolicy && selectedMediationPolicy.name ? (
                            <span>{selectedMediationPolicy.name}</span>
                        ) : (
                            <span>none</span>
                        )}
                    </Typography>
                    <Button
                        variant='contained'
                        className={classes.button}
                        onClick={startEditing}
                        disabled={isRestricted(['apim:api_create'], api)}
                    >
                        <FormattedMessage
                            id='Apis.Details.MediationPolicies.MediationPolicies.flow.update.btn'
                            defaultMessage='Update'
                        />
                    </Button>
                </div>
            </Paper>
            <EditMediationPolicy
                setEditing={setEditing}
                editing={editing}
                updateMediationPolicy={updateMediationPolicy}
                selectedMediationPolicy={selectedMediationPolicy}
                type={type}
            />
        </>
    );
}

InFlow.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(InFlow);
