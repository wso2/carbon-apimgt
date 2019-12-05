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
import React, { useEffect, useState, useContext } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import LaunchIcon from '@material-ui/icons/Launch';
import Grid from '@material-ui/core/Grid';
import green from '@material-ui/core/colors/green';
import CheckIcon from '@material-ui/icons/Check';
import CloseIcon from '@material-ui/icons/Close';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import ArrowForwardIosRoundedIcon from '@material-ui/icons/ArrowForwardIosRounded';
import Tooltip from '@material-ui/core/Tooltip';
import Button from '@material-ui/core/Button';
import ApiContext, { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import AuthManager from 'AppData/AuthManager';


const useStyles = makeStyles((theme) => ({
    root: {
        width: '90%',
    },
    button: {
        color: theme.palette.secondary,
        marginRight: theme.spacing(1),
    },
    gridRequirements: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        marginTop: theme.spacing(2),
    },
    gridSmall: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginLeft: 0,
        marginTop: theme.spacing(1),
    },
    gridEndpoint: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginLeft: 0,
        marginRight: theme.spacing(4.2),
        marginTop: theme.spacing(0.5),
    },
    iconTrue: {
        color: green[500],
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
        marginRight: theme.spacing(1),
    },
    iconFalse: {
        color: theme.palette.grey[500],
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
        marginRight: theme.spacing(1),
    },
    arrowIcon: {
        position: 'absolute',
        top: '16px',
        right: '-76px',
        fontSize: '7.9461rem',
        color: theme.palette.background.default,
        zIndex: theme.zIndex.overviewArrow,
    },
    label: {
        paddingLeft: '0',
        paddingRight: '0',
        flexBasis: '33.3333%',
        maxWidth: '33.3333%',
    },
    stepper: {
        background: theme.palette.background.default,
        marginLeft: theme.spacing(10),
    },
    box: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'white',
        padding: '2px',
        height: '82px',
        borderRight: '0',
        marginRight: '0',
    },
}));

/**
 * This component renders the API steps
 *
 */
export default function CustomizedSteppers() {
    const [api, updateAPI] = useAPI();
    const classes = useStyles();
    const { settings, user } = useAppContext();
    const isEndpointAvailable = api.endpointConfig !== null;
    const isTierAvailable = api.policies.length !== 0;
    const isPrototypedAvailable = api.endpointConfig !== null
        && api.endpointConfig.implementation_status === 'prototyped';
    const [lifecycleState, setlifecycleState] = useState([]);
    const [isUpdating, setUpdating] = useState(false);
    const { tenantList } = useContext(ApiContext);
    const userNameSplit = user.name.split('@');
    const tenantDomain = userNameSplit[userNameSplit.length - 1];
    let devportalUrl = `${settings.storeUrl}/apis/${api.id}/overview`;
    if (tenantList && tenantList.length > 0) {
        devportalUrl = `${settings.storeUrl}/apis/${api.id}/overview?tenant=${tenantDomain}`;
    }

    useEffect(() => {
        api.getLcState(api.id)
            .then((result) => {
                setlifecycleState(result.body.state);
            });
    }, []);

    /**
 * Update the LifeCycle state of the API
 *
 */
    function updateLCStateOfAPI(apiId, state) {
        setUpdating(true);
        const promisedUpdate = api.updateLcState(apiId, state);
        promisedUpdate
            .then(() => {
                updateAPI()
                    .then()
                    .catch((error) => {
                        if (error.response) {
                            Alert.error(error.response.body.description);
                        } else {
                            Alert.error('Something went wrong while updating the API');
                        }
                        console.error(error);
                    });
                Alert.info('Lifecycle state updated successfully');
            })
            .finally(() => setUpdating(false))
            .catch((errorResponse) => {
                console.log(errorResponse);
                Alert.error(JSON.stringify(errorResponse.message));
            });
    }

    /**
 * This function renders the final lifecycle state
 * @param {*} state
 */
    function finalLifecycleState(state) {
        switch (state) {
            case 'Published':
                return (
                    <Grid xs={12} display='block'>
                        <Grid xs={12}>
                            <Typography variant='h5'>
                                <FormattedMessage
                                    id='Apis.Details.Overview.CustomizedStepper.published'
                                    defaultMessage='Published'
                                />
                            </Typography>
                        </Grid>
                        <Grid xs={12}>
                            <a
                                target='_blank'
                                rel='noopener noreferrer'
                                href={devportalUrl}
                                className={classes.viewInStoreLauncher}
                            >
                                <Typography
                                    variant='caption'
                                    color='primary'
                                >
                                    <FormattedMessage
                                        id='Apis.Details.components.APIDetailsTopMenu.view.in.portal'
                                        defaultMessage='View in Dev Portal'
                                    />
                                    <LaunchIcon style={{ marginLeft: '5px' }} fontSize='small' />
                                </Typography>
                            </a>
                        </Grid>
                    </Grid>
                );
            case 'Prototyped':
                return (
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Details.Overview.CustomizedStepper.prototyped'
                            defaultMessage='Prototyped'
                        />
                    </Typography>
                );
            case 'Blocked':
                return (
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Details.Overview.CustomizedStepper.blocked'
                            defaultMessage='Blocked'
                        />
                    </Typography>
                );
            case 'Deprecated':
                return (
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Details.Overview.CustomizedStepper.deprecated'
                            defaultMessage='Deprecated'
                        />
                    </Typography>
                );
            case 'Retired':
                return (
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Details.Overview.CustomizedStepper.retired'
                            defaultMessage='Retired'
                        />
                    </Typography>
                );
            default:
                return (
                    <Grid xs={12} display='block'>
                        <Grid xs={12}>
                            {isPrototypedAvailable ? (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() => updateLCStateOfAPI(api.id, 'Deploy as a Prototype')}
                                    disabled={api.workflowStatus === 'CREATED' || AuthManager.isNotPublisher()}
                                >
                                        Deploy as a prototype
                                    {isUpdating && <CircularProgress size={20} />}
                                </Button>

                            ) : (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() => updateLCStateOfAPI(api.id, 'Publish')}
                                    disabled={(!isEndpointAvailable || !isTierAvailable)
                                        || AuthManager.isNotPublisher() || api.workflowStatus === 'CREATED'}
                                >
                                        Publish
                                    {isUpdating && <CircularProgress size={20} />}
                                </Button>
                            )}
                        </Grid>
                        {api.workflowStatus === 'CREATED' && (
                            <Grid xs={12}>
                                <Typography variant='caption' color='error'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.CustomizedStepper.pending'
                                        defaultMessage='The request is pending'
                                    />
                                </Typography>
                            </Grid>
                        )}
                    </Grid>
                );
        }
    }


    let activeStep = 0;
    if (lifecycleState === 'Created' && ((isEndpointAvailable && isTierAvailable) || isPrototypedAvailable)) {
        activeStep = 2;
    } else if (lifecycleState === 'Created') {
        activeStep = 1;
    } else if (lifecycleState !== 'Created') {
        activeStep = 3;
    }

    return (
        <div className={classes.root}>
            <Stepper alternativeLabel activeStep={activeStep} className={classes.stepper}>
                <Step className={classes.label}>
                    <StepLabel style={{ position: 'relative' }}>
                        <Box className={classes.box}>
                            <Typography variant='h5'>
                                <FormattedMessage
                                    id='Apis.Details.Overview.CustomizedStepper.create'
                                    defaultMessage='Created'
                                />
                            </Typography>
                        </Box>
                        <ArrowForwardIosRoundedIcon className={classes.arrowIcon} />
                    </StepLabel>
                </Step>
                <Step className={classes.label}>
                    <StepLabel style={{ position: 'relative' }}>
                        <Box p={2} bgcolor='white' borderLeft='0' borderRight='0'>
                            <Tooltip
                                title={isEndpointAvailable ? '' : 'You have to specify an endpoint for the API'}
                                placement='top'
                            >
                                <Grid className={classes.gridEndpoint}>
                                    {isEndpointAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography variant='h7'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.CustomizedStepper.business.plan.endpoint'
                                            defaultMessage='Endpoint'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/endpoints'}>
                                        <LaunchIcon style={{ marginLeft: '5px' }} color='primary' fontSize='small' />
                                    </Link>
                                </Grid>
                            </Tooltip>
                            <Tooltip
                                title={isTierAvailable ? '' : 'You have to select the business plan for the API'}
                                placement='bottom'
                            >
                                <Grid xs={12} className={classes.gridSmall}>
                                    {isTierAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography variant='h7'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.CustomizedStepper.business.plan.businessPlans'
                                            defaultMessage=' Business plans'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/subscriptions'}>
                                        <LaunchIcon style={{ marginLeft: '5px' }} color='primary' fontSize='small' />
                                    </Link>
                                </Grid>
                            </Tooltip>
                        </Box>
                        <ArrowForwardIosRoundedIcon className={classes.arrowIcon} />
                    </StepLabel>
                </Step>
                <Step className={classes.label}>
                    <StepLabel style={{ position: 'relative' }}>
                        <Box className={classes.box}>
                            {finalLifecycleState(lifecycleState)}
                        </Box>
                    </StepLabel>
                </Step>
            </Stepper>
        </div>
    );
}

CustomizedSteppers.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

CustomizedSteppers.contextType = ApiContext;
