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

import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import {
    Grid,
    TextField,
    makeStyles,
    Typography,
    Tooltip,
    RadioGroup,
    FormControl,
    FormControlLabel,
    MenuItem,
    InputLabel,
    Select,
    Radio,
} from '@material-ui/core';
import Icon from '@material-ui/core/Icon';
import LaunchIcon from '@material-ui/icons/Launch';
import { FormattedMessage } from 'react-intl';
import { Link } from 'react-router-dom';
import API from 'AppData/api';
import Banner from 'AppComponents/Shared/Banner';

const useStyles = makeStyles((theme) => ({
    typography: {
        display: 'inline-block',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 300,
    },
    selectField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 300,
    },
    helpIcon: {
        fontSize: 20,
    },
    banner: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
}));

/**
 * The credentials component. This component holds the access and secret key pair.
 * @param {any} props The parameters to credentials component.
 * @returns {any} HTML view of the credentials component.
 */
export default function Credentials(props) {
    const {
        apiId,
        endpointConfig,
        endpointsDispatcher,
    } = props;
    const classes = useStyles();
    const [pageError, setPageError] = useState(null);
    const inputLabel = useRef(null);
    const handleChange = (event) => {
        const newEndpointConfig = { ...endpointConfig };
        newEndpointConfig.access_method = event.target.value;
        newEndpointConfig.amznAccessKey = '';
        newEndpointConfig.amznSecretKey = '';
        newEndpointConfig.amznRegion = '';
        endpointsDispatcher({ action: 'set_awsCredentials', value: newEndpointConfig });
        setPageError(null);
    };
    const regionsList = [
        {
            key: 'us-east-1',
            value: 'us-east-1: US East (N. Virginia)',
        },
        {
            key: 'us-east-2',
            value: 'us-east-2: US East (Ohio)',
        },
        {
            key: 'us-west-1',
            value: 'us-west-1: US West (N. California)',
        },
        {
            key: 'us-west-2',
            value: 'us-west-2: US West (Oregon)',
        },
        {
            key: 'ap-east-1',
            value: 'ap-east-1: Asia Pacific (Hong Kong)',
        },
        {
            key: 'ap-south-1',
            value: 'ap-south-1: Asia Pacific (Mumbai)',
        },
        {
            key: 'ap-northeast-1',
            value: 'ap-northeast-1: Asia Pacific (Tokyo)',
        },
        {
            key: 'ap-northeast-2',
            value: 'ap-northeast-2: Asia Pacific (Seoul)',
        },
        {
            key: 'ap-northeast-3',
            value: 'ap-northeast-3: Asia Pacific (Osaka-Local)',
        },
        {
            key: 'ap-southeast-1',
            value: 'ap-southeast-1: Asia Pacific (Singapore)',
        },
        {
            key: 'ap-southeast-2',
            value: 'ap-southeast-2: Asia Pacific (Sydney)',
        },
        {
            key: 'ca-central-1',
            value: 'ca-central-1: Canada (Central)',
        },
        {
            key: 'eu-central-1',
            value: 'eu-central-1: Europe (Frankfurt)',
        },
        {
            key: 'eu-west-1',
            value: 'eu-west-1: Europe (Ireland)',
        },
        {
            key: 'eu-west-2',
            value: 'eu-west-2: Europe (London)',
        },
        {
            key: 'eu-west-3',
            value: 'eu-west-3: Europe (Paris)',
        },
        {
            key: 'eu-north-1',
            value: 'eu-north-1: Europe (Stockholm)',
        },
        {
            key: 'me-south-1',
            value: 'me-south-1: Middle East (Bahrain)',
        },
        {
            key: 'sa-east-1',
            value: 'sa-east-1: South America (São Paulo)',
        },
    ];
    useEffect(() => {
        API.getAmznResourceNames(apiId)
            .catch((error) => {
                setPageError(error.response.body.error);
            });
    }, []);
    return (
        <>
            <Typography className={classes.typography}>
                <FormattedMessage
                    id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                    + '.endpoint.accessMethod'}
                    defaultMessage='Access Method'
                />
            </Typography>
            <RadioGroup
                aria-label='accessMethod'
                name='accessMethod'
                value={endpointConfig.access_method}
                onChange={handleChange}
            >
                <div>
                    <FormControlLabel
                        value='role-supplied'
                        control={<Radio color='primary' />}
                        label={
                            (
                                <FormattedMessage
                                    id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                                    + '.endpoint.accessMethod.roleSupplied'}
                                    defaultMessage='Using IAM role-supplied temporary AWS credentials'
                                />
                            )
                        }
                    />
                    <Tooltip
                        title={
                            (
                                <FormattedMessage
                                    id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                                    + '.endpoint.tooltip'}
                                    defaultMessage={'You can and should use an IAM role to manage temporary '
                                    + 'credentials for applications that run on an EC2 instance'}
                                />
                            )
                        }
                    >
                        <Icon className={classes.helpIcon}>help_outline</Icon>
                    </Tooltip>
                </div>
                <div>
                    <FormControlLabel
                        value='stored'
                        control={<Radio color='primary' />}
                        label={
                            (
                                <FormattedMessage
                                    id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                                    + '.endpoint.accessMethod.stored'}
                                    defaultMessage='Using stored AWS credentials'
                                />
                            )
                        }
                    />
                </div>
            </RadioGroup>
            <Grid item>
                <TextField
                    required
                    disabled={endpointConfig.access_method === 'role-supplied'}
                    id='outlined-required'
                    label={
                        (
                            <FormattedMessage
                                id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                                + '.endpoint.accessKey'}
                                defaultMessage='Access Key'
                            />
                        )
                    }
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    value={endpointConfig.amznAccessKey}
                    onChange={(event) => {
                        const newEndpointConfig = { ...endpointConfig };
                        newEndpointConfig.amznAccessKey = event.target.value;
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEndpointConfig });
                    }}
                />
                <TextField
                    required
                    disabled={endpointConfig.access_method === 'role-supplied'}
                    id='outlined-password-input-required'
                    label={
                        (
                            <FormattedMessage
                                id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                                + '.endpoint.secretKey'}
                                defaultMessage='Secret Key'
                            />
                        )
                    }
                    type='password'
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    value={endpointConfig.amznSecretKey}
                    onChange={(event) => {
                        const newEndpointConfig = { ...endpointConfig };
                        newEndpointConfig.amznSecretKey = event.target.value;
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEndpointConfig });
                    }}
                />
                <FormControl
                    required
                    margin='normal'
                    variant='outlined'
                    disabled={endpointConfig.access_method === 'role-supplied'}
                >
                    <InputLabel ref={inputLabel} id='region-label'>
                        <FormattedMessage
                            id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                            + '.endpoint.region'}
                            defaultMessage='Region'
                        />
                    </InputLabel>
                    <Select
                        labelId='region-label'
                        autoWidth={false}
                        className={classes.selectField}
                        onChange={(event) => {
                            const newEndpointConfig = { ...endpointConfig };
                            newEndpointConfig.amznRegion = event.target.value;
                            endpointsDispatcher({ action: 'set_awsCredentials', value: newEndpointConfig });
                        }}
                        value={endpointConfig.amznRegion}
                    >
                        {regionsList.map((region) => ((
                            <MenuItem key={region.key} value={region.key}>
                                {region.value}
                            </MenuItem>
                        )))}
                    </Select>
                </FormControl>
            </Grid>
            <Grid item>
                <Link to={`/apis/${apiId}/resources`}>
                    <Typography style={{ marginLeft: '10px' }} color='primary' display='inline' variant='caption'>
                        <FormattedMessage
                            id={'Apis.Details.Endpoints.EndpointOverview.awslambda'
                            + '.endpoint.linkToResources'}
                            defaultMessage='Go to Resources to map ARNs'
                        />
                        <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                    </Typography>
                </Link>
            </Grid>
            {pageError
                && (
                    <Grid item className={classes.banner}>
                        <Banner
                            disableActions
                            dense
                            paperProps={{ elevation: 1 }}
                            type='warning'
                            message={pageError}
                        />
                    </Grid>
                )}
        </>
    );
}

Credentials.propTypes = {
    apiId: PropTypes.shape('').isRequired,
    endpointConfig: PropTypes.shape({}).isRequired,
    endpointsDispatcher: PropTypes.func.isRequired,
};
