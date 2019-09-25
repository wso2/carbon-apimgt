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

/**
 * The base component of the endpoints view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
import React, { useState } from 'react';
import { Grid } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import { isRestricted } from 'AppData/AuthManager';
import Alert from 'AppComponents/Shared/Alert';
import isEmpty from 'lodash/isEmpty';
import InFlow from './InFlow';
import OutFlow from './OutFlow';
import FaultFlow from './FaultFlow';

const styles = theme => ({

    buttonWrapper: {
        paddingTop: 20,
    },
    paperRoot: {
        padding: 20,
        marginTop: 20,
        display: 'flex',
    },
    formControl: {
        flexDirection: 'row',
        padding: `${theme.spacing.unit * 2}px 2px`,
    },
    itemWrapper: {
        width: 500,
    },
});
/**
 * The base component of the mediation policy view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Overview(props) {
    const { classes, api } = props;
    const inFlowMediationPolicy = (api.mediationPolicies.filter(seq => seq.type === 'IN')[0]);
    const outFlowMediationPolicy = (api.mediationPolicies.filter(seq => seq.type === 'OUT')[0]);
    const faultFlowMediationPolicy = (api.mediationPolicies.filter(seq => seq.type === 'FAULT')[0]);
    const [inPolicyName, setInPolicyName] = useState(inFlowMediationPolicy !== (null || undefined) ?
        { id: inFlowMediationPolicy.id, name: inFlowMediationPolicy.name, type: inFlowMediationPolicy.type } : {});
    const [outPolicyName, setOutPolicyName] = useState(outFlowMediationPolicy !== (null || undefined) ?
        { id: outFlowMediationPolicy.id, name: outFlowMediationPolicy.name, type: outFlowMediationPolicy.type } : {});
    const [faultPolicyName, setFaultPolicyName] = useState(faultFlowMediationPolicy !== (null || undefined) ?
        { id: faultFlowMediationPolicy.id, name: faultFlowMediationPolicy.name, type: faultFlowMediationPolicy.type } :
        {});
    const NONE = 'none';
    const mediationPolicies = [];
    if (!(isEmpty(inPolicyName) || inPolicyName.name === NONE)) {
        mediationPolicies.push(inPolicyName);
    }
    if (!(isEmpty(outPolicyName) || outPolicyName.name === NONE)) {
        mediationPolicies.push(outPolicyName);
    }
    if (!(isEmpty(faultPolicyName) || faultPolicyName.name === NONE)) {
        mediationPolicies.push(faultPolicyName);
    }

    /**
     * Method to update the api.
     *
     * @param {function} updateAPI The api update function.
     */
    const saveAPI = (updateAPI) => {
        const promisedApi = api.get(api.id);
        promisedApi
            .then((getResponse) => {
                const apiData = getResponse.body;
                apiData.mediationPolicies = mediationPolicies;
                updateAPI(apiData);
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error('Error occurred while retrieving API to save mediation policies');
            });
    };
    const updateInMediationPolicy = (policies) => {
        setInPolicyName({ id: policies.id, name: policies.name, type: policies.type });
    };
    const updateOutMediationPolicy = (policies) => {
        setOutPolicyName({ id: policies.id, name: policies.name, type: policies.type });
    };
    const updateFaultMediationPolicy = (policies) => {
        setFaultPolicyName({ id: policies.id, name: policies.name, type: policies.type });
    };
    return (
        <div >
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies'
                        defaultMessage='Message Mediation Policies'
                    />
                </Typography>
            </div>
            <ApiContext.Consumer>
                {({ updateAPI }) => (
                    <div className={classes.formControl}>
                        <Grid
                            container
                            spacing={12}
                        >
                            <Grid item xs={12}>
                                <Paper className={classes.paperRoot} elevation={1}>
                                    <InFlow api={api} updateMediationPolicy={updateInMediationPolicy} />
                                    <OutFlow api={api} updateMediationPolicy={updateOutMediationPolicy} />
                                    <FaultFlow api={api} updateMediationPolicy={updateFaultMediationPolicy} />
                                </Paper>
                            </Grid>
                        </Grid>
                        <Grid
                            container
                            direction='row'
                            alignItems='center'
                            spacing={4}
                            className={classes.buttonSection}
                            style={{ marginTop: 20 }}
                        >
                            <Grid item>
                                <div>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        onClick={() => saveAPI(updateAPI)}
                                        disabled={isRestricted(['apim:api_create'], api)}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.Overview.save'
                                            defaultMessage='Save'
                                        />
                                    </Button>
                                </div>
                            </Grid>
                            <Grid item>
                                <Link to={'/apis/' + api.id + '/overview'}>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.Overview.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            </Grid>
                        </Grid>
                    </div>

                )}
            </ApiContext.Consumer>
        </div>
    );
}
Overview.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.shape({}),
        buttonSection: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Overview);
