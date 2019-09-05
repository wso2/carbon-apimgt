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
import React, { useEffect, useState } from 'react';
import { Grid } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
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
    const [mediationPolicies, setMediationPolices] = useState([]);

    const [inFlowMediationPolicy, setInFlowMediationPolicy] = useState(api.mediationPolicies.inSequence);
    const [outFlowMediationPolicy, setOutFlowMediationPolicy] = useState(api.mediationPolicies.outSequence);
    const [faultFlowMediationPolicy, setFaultFlowMediationPolicy] = useState(api.mediationPolicies.faultSequence);
    /**
     * Method to update the api.
     *
     * @param {function} updateAPI The api update function.
     */
    const saveAPI = (updateAPI) => {
        updateAPI(mediationPolicies);
    };
    const updateInMediationPolicy = (policies) => {
        setInFlowMediationPolicy(policies);
    };
    const updateOutMediationPolicy = (policies) => {
        setOutFlowMediationPolicy(policies);
    };
    const updateFaultMediationPolicy = (policies) => {
        setFaultFlowMediationPolicy(policies);
    };
    useEffect(() => {
        setInFlowMediationPolicy(inFlowMediationPolicy);
        setFaultFlowMediationPolicy(faultFlowMediationPolicy);
        setOutFlowMediationPolicy(outFlowMediationPolicy);
        setMediationPolices([inFlowMediationPolicy, outFlowMediationPolicy, faultFlowMediationPolicy]);
    }, [inFlowMediationPolicy, outFlowMediationPolicy, faultFlowMediationPolicy]);

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
                            alignItems='flex-start'
                            spacing={16}
                            className={classes.buttonSection}
                        >
                            <Grid item>
                                <div>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        onClick={() => saveAPI(updateAPI)}
                                    >
                                        <FormattedMessage id='save' defaultMessage='Save' />
                                    </Button>
                                </div>
                            </Grid>
                            <Grid item>
                                <Link to={'/apis/' + api.id + '/overview'}>
                                    <Button>
                                        <FormattedMessage id='cancel' defaultMessage='Cancel' />
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
