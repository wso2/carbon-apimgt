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
import React, { useState, useContext } from 'react';
import { Grid } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import isEmpty from 'lodash/isEmpty';
import cloneDeep from 'lodash.clonedeep';
import { isRestricted } from 'AppData/AuthManager';
import Flow from './Flow';
import Diagram from './Diagram';

const styles = (theme) => ({
    paperRoot: {
        padding: 20,
        marginTop: 20,
        display: 'flex',
    },
    formControl: {
        flexDirection: 'row',
        padding: `${theme.spacing(2)}px 2px`,
    },
    itemWrapper: {
        width: 500,
    },
    root: {
        paddingTop: 0,
        paddingLeft: 0,
        maxWidth: theme.custom.contentAreaWidth,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    button: {
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonWrapper: {
        paddingTop: theme.spacing(3),
    },
    addProperty: {
        marginRight: theme.spacing(2),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    diagramDown: {
        marginTop: 80,
        textAlign: 'center',
    },
});
/**
 * The base component of the mediation policy view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Overview(props) {
    const { classes } = props;
    const { api, updateAPI } = useContext(ApiContext);
    const mediationPolicies = cloneDeep(api.mediationPolicies || []);
    const [inPolicy, setInPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'IN')[0]);
    const [outPolicy, setOutPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'OUT')[0]);
    const [faultPolicy, setFaultPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'FAULT')[0]);
    const [updating, setUpdating] = useState(false);

    /**
     * Method to update the api.
     *
     */
    const saveAPI = () => {
        setUpdating(true);
        const NONE = 'none';
        const newMediationPolicies = [];
        if (!(isEmpty(inPolicy) || inPolicy.name === NONE)) {
            newMediationPolicies.push(inPolicy);
        }
        if (!(isEmpty(outPolicy) || outPolicy.name === NONE)) {
            newMediationPolicies.push(outPolicy);
        }
        if (!(isEmpty(faultPolicy) || faultPolicy.name === NONE)) {
            newMediationPolicies.push(faultPolicy);
        }

        updateAPI({ mediationPolicies: newMediationPolicies })
            .then(() => {
                Alert.success('Successfully Updated API mediation policies');
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error('Error occurred while retrieving API to save mediation policies');
            })
            .finally(() => {
                setUpdating(false);
            });
    };
    const updateInMediationPolicy = (policy) => {
        setInPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };
    const updateOutMediationPolicy = (policy) => {
        setOutPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };
    const updateFaultMediationPolicy = (policy) => {
        setFaultPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };

    return (
        <div className={classes.root}>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.MediationPolicies.MediationPolicies'
                        defaultMessage='Message Mediation Policies'
                    />
                </Typography>
            </div>
            <Grid container spacing={1}>
                <Grid item xs={12} md={4}>
                    <Flow
                        updateMediationPolicy={updateInMediationPolicy}
                        saveAPI={saveAPI}
                        selectedMediationPolicy={inPolicy}
                        type='IN'
                        api={api}
                    />
                    <Flow
                        updateMediationPolicy={updateOutMediationPolicy}
                        saveAPI={saveAPI}
                        selectedMediationPolicy={outPolicy}
                        type='OUT'
                        api={api}
                    />
                    <Flow
                        updateMediationPolicy={updateFaultMediationPolicy}
                        saveAPI={saveAPI}
                        selectedMediationPolicy={faultPolicy}
                        type='FAULT'
                        api={api}
                    />
                </Grid>
                <Grid item xs={12} md={8} className={classes.diagramDown}>
                    <Diagram inPolicy={inPolicy} outPolicy={outPolicy} faultPolicy={faultPolicy} />
                </Grid>
                <Grid item xs={12}>
                    <div className={classes.buttonWrapper}>
                        <Grid
                            container
                            direction='row'
                            alignItems='flex-start'
                            spacing={1}
                            className={classes.buttonSection}
                        >
                            <Grid item>
                                <div>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        onClick={saveAPI}
                                        disabled={isRestricted(['apim:api_create'], api) || updating}
                                    >
                                        {updating ? (
                                            <>
                                                <FormattedMessage
                                                    id='Apis.Details.MediationPolicies.MediationPolicies.saving'
                                                    defaultMessage='Saving..'
                                                />
                                                <CircularProgress className={classes.progress} size={16} />
                                            </>
                                        ) : (
                                            <FormattedMessage
                                                id='Apis.Details.MediationPolicies.MediationPolicies.save'
                                                defaultMessage='Save'
                                            />
                                        )}
                                    </Button>
                                </div>
                            </Grid>
                            <Grid item>
                                <Link to={'/apis/' + api.id + '/overview'}>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MediationPolicies.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            </Grid>
                        </Grid>
                    </div>
                </Grid>
            </Grid>
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
