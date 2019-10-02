import React, { useEffect, useState } from 'react';
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
import Api from 'AppData/api';
import CheckIcon from '@material-ui/icons/Check';
import CloseIcon from '@material-ui/icons/Close';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import ArrowForwardIosRoundedIcon from '@material-ui/icons/ArrowForwardIosRounded';


const useStyles = makeStyles(theme => ({
    root: {
        width: '90%',
    },
    button: {
        marginRight: theme.spacing(1),
    },
    instructions: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
    gridRequirements: {
        display: 'flex',
        flexDirection: 'row',
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
    },
    gridTopic: {
        marginTop: theme.spacing(1),
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginLeft: 0,
    },
    iconTrue: {
        color: green[500],
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconFalse: {
        color: theme.palette.grey[500],
        marginLeft: theme.spacing(1),
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    rectangle: {
        width: theme.spacing(15),
        height: theme.spacing(12),
        background: 'grey',
    },
    triangleRight: {
        borderTop: '45px solid transparent',
        borderLeft: '100px solid grey',
        borderBottom: '50px solid transparent',
    },
    arrow1: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginTop: theme.spacing(0.5),
    },
    text: {
        marginTop: theme.spacing(3),
        marginLeft: theme.spacing(3),
    },
    largeIcon: {
        width: theme.spacing(5),
        height: theme.spacing(5),
    },
    arrowIcon: {
        position: 'absolute',
        top: '16px',
        right: '-76px',
        fontSize: '7.9461rem',
        color: theme.palette.background.default,
        zIndex: '1',
    },
    label: {
        paddingLeft: '0',
        paddingRight: '0',
    },
    stepper: {
        background: theme.palette.background.default,
    },
    box: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'white',
        padding: '2px',
        width: '405px',
        height: '82px',
        borderRight: '0',
        marginRight: '0',
    },
}));

/**
 * This component renders the requirements list
 * @param {*} props
 */
export default function CustomizedSteppers(props) {
    const restApi = new Api();
    const { api } = props;
    const classes = useStyles();
    const isEndpointAvailable = api.endpointConfig !== null;
    const isTierAvailable = api.policies.length !== 0;
    const isPrototypedAvailable =
        api.endpointConfig !== null && api.endpointConfig.implementation_status === 'prototyped';

    const [lifecycleState, setlifecycleState] = useState([]);


    useEffect(() => {
        restApi.getLcState(api.id)
            .then((result) => {
                setlifecycleState(result.body.state);
            });
    }, []);

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
                            <Typography variant='h6'>
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
                    <StepLabel style={{ position: 'relative' }} >
                        <Box p={2} bgcolor='white' width='377px' borderColor='grey.500' borderLeft='0' borderRight='0' >
                            <Typography variant='h10'>
                                <FormattedMessage
                                    id='Apis.Details.Overview.CustomizedStepper.requirements'
                                    defaultMessage='Requirements'
                                />
                            </Typography>
                            <Grid xs={12} className={classes.gridRequirements}>
                                <Grid xs={4} className={classes.gridSmall}>
                                    {isEndpointAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography variant='caption'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.CustomizedStepper.business.plan'
                                            defaultMessage='Endpoint'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/endpoints'}>
                                        <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                                    </Link>
                                </Grid>
                                <Grid xs={6} className={classes.gridSmall}>
                                    {isTierAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography variant='caption'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.CustomizedStepper.business.plan'
                                            defaultMessage=' Business plans'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/subscriptions'}>
                                        <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                                    </Link>
                                </Grid>
                            </Grid>
                        </Box>
                        <ArrowForwardIosRoundedIcon className={classes.arrowIcon} />
                    </StepLabel>
                </Step>
                <Step className={classes.label}>
                    <StepLabel style={{ position: 'relative' }}>
                        {lifecycleState === 'Published' && (
                            <Box className={classes.box}>
                                <Typography variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.CustomizedStepper.published'
                                        defaultMessage='Published'
                                    />
                                </Typography>
                            </Box>
                        )}
                        {lifecycleState === 'Prototyped' && (
                            <Box className={classes.box}>
                                <Typography variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.CustomizedStepper.prototyped'
                                        defaultMessage='Prototyped'
                                    />
                                </Typography>
                            </Box>
                        )}
                        {lifecycleState === 'Created' && (
                            <Box className={classes.box}>
                                <Typography variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.CustomizedStepper.publish'
                                        defaultMessage='Publish'
                                    />
                                </Typography>
                                <Link to={'/apis/' + api.id + '/lifecycle'}>
                                    <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                                </Link>
                            </Box>
                        )}
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
