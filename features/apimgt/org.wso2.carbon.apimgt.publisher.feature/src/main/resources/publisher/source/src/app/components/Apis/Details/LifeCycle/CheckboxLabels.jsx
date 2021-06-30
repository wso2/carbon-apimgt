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
import React from 'react';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import Paper from '@material-ui/core/Paper';
import CloseIcon from '@material-ui/icons/Close';
import CheckIcon from '@material-ui/icons/Check';
import green from '@material-ui/core/colors/green';
import yellow from '@material-ui/core/colors/yellow';
import LaunchIcon from '@material-ui/icons/Launch';

const useStyles = makeStyles((theme) => ({
    stateButton: {
        marginRight: theme.spacing(),
    },
    paperCenter: {
        padding: theme.spacing(2),
        display: 'block',
        alignItems: 'left',
        justifyContent: 'center',
    },
    iconTrue: {
        color: green[500],
        marginRight: theme.spacing(1),
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconFalse: {
        color: theme.palette.grey[500],
        marginRight: theme.spacing(1),
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconWarn: {
        color: yellow[700],
        marginRight: theme.spacing(1),
        display: 'block',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    grid: {
        marginTop: theme.spacing(2),
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'left',
        alignItems: 'left',
    },
    labelsGrid: {
        fontWeight: 600,
        marginTop: theme.spacing(3),
    },
}));
/**
 * This component renders the requirements list
 * @param {*} props
 */
export default function CheckboxLabels(props) {
    const classes = useStyles();
    const {
        api, isMutualSSLEnabled, isCertAvailable, isAppLayerSecurityMandatory, isBusinessPlanAvailable,
    } = props;
    const isEndpointAvailable = api.endpointConfig !== null && !api.endpointConfig.implementation_status;
    const isPrototypedAvailable = (api.endpointConfig !== null
        && api.endpointConfig.implementation_status === 'prototyped')
        || api.endpointImplementationType === 'INLINE';

    return (
        <Paper className={classes.paperCenter}>
            <Grid xs={12}>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.Configuration.requirements'
                        defaultMessage='Requirements'
                    />
                </Typography>
                <Typography variant='caption'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.Configuration.requirements.state.transition'
                        defaultMessage='Requirements for next state transition'
                    />
                </Typography>
            </Grid>
            {(api.lifeCycleStatus === 'CREATED' || api.lifeCycleStatus === 'PROTOTYPED') && (
                <>
                    <Grid xs={12} className={classes.labelsGrid}>
                        <Typography variant='subtitle2'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.publisher'
                                defaultMessage='Publish'
                            />
                        </Typography>
                    </Grid>
                    <Grid xs={12}>
                        {api.type !== 'WEBSUB' && (
                            <Grid xs={12} className={classes.grid}>
                                {isEndpointAvailable ? (
                                    <CheckIcon className={classes.iconTrue} />
                                ) : (
                                    <CloseIcon className={classes.iconFalse} />
                                )}
                                <Typography>
                                    <FormattedMessage
                                        id='Apis.Details.LifeCycle.CheckboxLabels.endpoints.provided'
                                        defaultMessage='Endpoint provided'
                                    />
                                </Typography>
                                <Link to={'/apis/' + api.id + '/endpoints'}>
                                    <LaunchIcon style={{ marginLeft: '2px' }} color='primary' fontSize='small' />
                                </Link>
                            </Grid>
                        )}
                        <>
                            {isAppLayerSecurityMandatory && (
                                <Grid xs={12} className={classes.grid}>
                                    {isBusinessPlanAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography>
                                        <FormattedMessage
                                            id='Apis.Details.LifeCycle.CheckboxLabels.business.plans.selected'
                                            defaultMessage='Business Plan(s) selected'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/subscriptions'}>
                                        <LaunchIcon style={{ marginLeft: '2px' }} color='primary' fontSize='small' />
                                    </Link>
                                </Grid>
                            )}
                            {isMutualSSLEnabled && (
                                <Grid xs={12} className={classes.grid}>
                                    {isCertAvailable ? (
                                        <CheckIcon className={classes.iconTrue} />
                                    ) : (
                                        <CloseIcon className={classes.iconFalse} />
                                    )}
                                    <Typography>
                                        <FormattedMessage
                                            id='Apis.Details.LifeCycle.CheckboxLabels.cert.provided'
                                            defaultMessage='Certificate provided'
                                        />
                                    </Typography>
                                    <Link to={'/apis/' + api.id + '/runtime-configuration'}>
                                        <LaunchIcon style={{ marginLeft: '2px' }} color='primary' fontSize='small' />
                                    </Link>
                                </Grid>
                            ) }
                        </>
                    </Grid>
                    { api.type !== 'GRAPHQL' && (
                        <>
                            <Grid xs={12} className={classes.labelsGrid}>
                                <Typography variant='subtitle2'>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.Configuration.prototype'
                                        defaultMessage='Deploy as a Prototype'
                                    />
                                </Typography>
                            </Grid>
                            <Grid xs={12}>
                                {api.type !== 'WEBSUB' && (
                                    <Grid xs={12} className={classes.grid}>
                                        {isPrototypedAvailable ? (
                                            <CheckIcon className={classes.iconTrue} />
                                        ) : (
                                            <CloseIcon className={classes.iconFalse} />
                                        )}
                                        <Typography>
                                            <FormattedMessage
                                                id={'Apis.Details.Configuration.Configuration.prototype.endpoints'
                                                    + '.provided'}
                                                defaultMessage='Prototype Endpoint provided'
                                            />
                                        </Typography>
                                        <Link to={'/apis/' + api.id + '/endpoints'}>
                                            <LaunchIcon
                                                style={{ marginLeft: '2px' }}
                                                color='primary'
                                                fontSize='small'
                                            />
                                        </Link>
                                    </Grid>
                                )}
                            </Grid>
                        </>
                    )}
                </>
            )}
            {api.lifeCycleStatus === 'PUBLISHED' && (
                <>
                    <Grid xs={12} className={classes.labelsGrid}>
                        <Typography variant='subtitle2'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.prototype'
                                defaultMessage='Deploy as a Prototype'
                            />
                        </Typography>
                    </Grid>
                    <Grid xs={12}>
                        <Grid xs={12} className={classes.grid}>
                            {isPrototypedAvailable ? (
                                <CheckIcon className={classes.iconTrue} />
                            ) : (
                                <CloseIcon className={classes.iconFalse} />
                            )}
                            <Typography>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.prototype.endpoints.provided'
                                    defaultMessage='Prototype Endpoint provided'
                                />
                            </Typography>
                            <Link to={'/apis/' + api.id + '/endpoints'}>
                                <LaunchIcon style={{ marginLeft: '2px' }} color='primary' fontSize='small' />
                            </Link>
                        </Grid>
                    </Grid>
                </>
            )}
        </Paper>
    );
}

CheckboxLabels.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
