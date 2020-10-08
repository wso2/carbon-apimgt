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
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import WrappedExpansionPanel from 'AppComponents/Shared/WrappedExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import LaunchIcon from '@material-ui/icons/Launch';
import { Link } from 'react-router-dom';
import Tooltip from '@material-ui/core/Tooltip';
import Box from '@material-ui/core/Box';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { makeStyles } from '@material-ui/core/styles';

const showEndpoint = function (api, type) {
    if (api.endpointConfig) {
        if (type === 'prod') {
            return api.getProductionEndpoint();
        }
        if (type === 'sand') {
            return api.getSandboxEndpoint();
        }
    }
    return null;
};

const useStyles = makeStyles((theme) => ({
    subtitle: {
        marginTop: theme.spacing(0),
    },
    expansionPanel: {
        marginBottom: theme.spacing(1),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    notConfigured: {
        color: 'rgba(0, 0, 0, 0.40)',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
    textTrim: {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
}));

/**
 *
 *X
 * @param {*} props
 * @returns
 */
function Endpoints(props) {
    const { api } = props;
    const classes = useStyles();
    const isPrototypedAvailable = api.endpointConfig !== null
        && api.endpointConfig.implementation_status === 'prototyped';

    /**
     * Check whether the endpoint configuration is dynamic
     *
     * @param {object} endpointConfig The endpoint configuration of the api.
     * @return {boolean} True if the endpoint config is dynamic.
     * */
    const isDynamicEndpoints = (endpointConfig) => {
        if (!endpointConfig) {
            return false;
        }
        if (endpointConfig.production_endpoints && !Array.isArray(endpointConfig.production_endpoints)) {
            return endpointConfig.production_endpoints.url === 'default';
        }
        return false;
    };

    return (
        <>
            <WrappedExpansionPanel className={classes.expansionPanel} defaultExpanded id='endpoints'>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography className={classes.subHeading} variant='h6'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.Endpoints.endpoints'
                            defaultMessage='Endpoints'
                        />
                    </Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                    {isDynamicEndpoints(api.endpointConfig)
                        ? (
                            <Box pb={2}>
                                <Typography component='p' variant='body1' className={classes.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.Endpoints.dynamic'
                                        defaultMessage='Dynamic'
                                    />
                                </Typography>
                            </Box>
                        )
                        : (
                            <>
                                <Box pb={2}>
                                    {/* Production Endpoint (TODO) fix the endpoint
                                                    info when it's available with the api object */}

                                    { !isPrototypedAvailable ? (
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Endpoints.production'
                                                defaultMessage='Production'
                                            />
                                        </Typography>
                                    ) : (
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Endpoints.prototype'
                                                defaultMessage='Prototype'
                                            />
                                        </Typography>
                                    )}
                                    {showEndpoint(api, 'prod')
                                && (
                                    <Tooltip
                                        title={showEndpoint(api, 'prod')}
                                        interactive
                                    >
                                        <Typography component='p' variant='body1' className={classes.textTrim}>
                                            <>
                                                {showEndpoint(api, 'prod')}
                                            </>
                                        </Typography>
                                    </Tooltip>
                                )}
                                    <Typography component='p' variant='body1' className={classes.notConfigured}>
                                        {!showEndpoint(api, 'prod') && (
                                            <>
                                                <FormattedMessage
                                                    id={'Apis.Details.Configuration.'
                                                    + 'components.Endpoints.not.set'}
                                                    defaultMessage='-'
                                                />
                                            </>
                                        )}
                                    </Typography>
                                </Box>
                                {!isPrototypedAvailable && (
                                    <Box pb={2}>
                                        {/* Sandbox Endpoint (TODO) fix the endpoint info when
                                                it's available with the api object */}
                                        <Typography component='p' variant='subtitle2' className={classes.subtitle}>
                                            <FormattedMessage
                                                id='Apis.Details.Configuration.components.Endpoints.sandbox'
                                                defaultMessage='Sandbox'
                                            />
                                        </Typography>
                                        {showEndpoint(api, 'sand')
                                    && (
                                        <Tooltip
                                            title={showEndpoint(api, 'sand')}
                                            interactive
                                        >
                                            <Typography component='p' variant='body1' className={classes.textTrim}>
                                                <>
                                                    {showEndpoint(api, 'sand')}
                                                </>
                                            </Typography>
                                        </Tooltip>
                                    )}
                                        <Typography component='p' variant='body1' className={classes.notConfigured}>
                                            {!showEndpoint(api, 'sand') && (
                                                <>
                                                    <FormattedMessage
                                                        id={'Apis.Details.Configuration.components.Endpoints.sandbox.'
                                                        + 'not.set'}
                                                        defaultMessage='-'
                                                    />
                                                </>
                                            )}
                                        </Typography>
                                    </Box>
                                )}
                            </>
                        )}
                    <Box width='100%' textAlign='right' m={1}>
                        <Link to={'/apis/' + api.id + '/endpoints'}>
                            <Typography style={{ marginLeft: '10px' }} color='primary' variant='caption'>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.Endpoints.edit.api.endpoints'
                                    defaultMessage='Edit API Endpoints'
                                />
                                <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                            </Typography>
                        </Link>
                    </Box>
                </ExpansionPanelDetails>
            </WrappedExpansionPanel>
        </>
    );
}

Endpoints.propTypes = {
    api: PropTypes.shape({}).isRequired,
};

export default withAPI(Endpoints);
