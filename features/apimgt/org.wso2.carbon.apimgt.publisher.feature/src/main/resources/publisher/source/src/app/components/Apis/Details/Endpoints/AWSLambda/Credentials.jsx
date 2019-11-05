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
import PropTypes from 'prop-types';
import {
    Grid,
    TextField,
    makeStyles,
    Typography,
    Tooltip,
    RadioGroup,
    FormControlLabel,
    Radio,
} from '@material-ui/core';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';

const useStyles = makeStyles(theme => ({
    typography: {
        display: 'inline-block',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 300,
    },
    helpIcon: {
        fontSize: 20,
    },
}));

/**
 * The credentials component. This component holds the access and secret key pair.
 * @param {any} props The parameters to credentials component.
 * @returns {any} HTML view of the credentials component.
 */
export default function Credentials(props) {
    const {
        epConfig,
        setEpConfig,
        endpointsDispatcher,
        awsAccessMethod,
        setAwsAccessMethod,
    } = props;
    const classes = useStyles();
    const [isChanged, setIsChanged] = useState(false);
    const handleChange = (event) => {
        const newEpConfig = { ...epConfig };
        newEpConfig.amznAccessKey = '';
        newEpConfig.amznSecretKey = '';
        setEpConfig(newEpConfig);
        endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
        setAwsAccessMethod(event.target.value);
    };
    useEffect(() => {
        if (epConfig.amznAccessKey !== '' && epConfig.amznSecretKey !== '') {
            setAwsAccessMethod('stored');
        }
    }, []);
    return (
        <div>
            <Typography className={classes.typography}>
                <FormattedMessage
                    id={'Apis.Details.Endpoints.EndpointOverview.awslambda' +
                    '.endpoint.accessMethod'}
                    defaultMessage='Access Method'
                />
            </Typography>
            <RadioGroup aria-label='accessMethod' name='accessMethod' value={awsAccessMethod} onChange={handleChange}>
                <div>
                    <FormControlLabel
                        value='role-supplied'
                        control={<Radio color='primary' />}
                        label='Using IAM role-supplied temporary AWS credentials'
                    />
                    <Tooltip
                        title={'You can and should use an IAM role to manage temporary credentials for ' +
                            'applications that run on an EC2 instance'
                        }
                    >
                        <Icon className={classes.helpIcon}>help_outline</Icon>
                    </Tooltip>
                </div>
                <div>
                    <FormControlLabel
                        value='stored'
                        control={<Radio color='primary' />}
                        label='Using stored AWS credentials'
                    />
                </div>
            </RadioGroup>
            <Grid item>
                <TextField
                    required
                    disabled={awsAccessMethod === 'role-supplied'}
                    id='outlined-required'
                    label='Access Key'
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    value={epConfig.amznAccessKey}
                    onChange={(event) => {
                        const newEpConfig = { ...epConfig };
                        newEpConfig.amznAccessKey = event.target.value;
                        setEpConfig(newEpConfig);
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                    }}
                />
                <TextField
                    required
                    disabled={awsAccessMethod === 'role-supplied'}
                    id='outlined-password-input-required'
                    label='Secret Key'
                    type='password'
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    // eslint-disable-next-line no-nested-ternary
                    value={isChanged ? epConfig.amznSecretKey : (epConfig.amznSecretKey === '' ? '' : 'AWS_SECRET_KEY')}
                    onChange={(event) => {
                        const newEpConfig = { ...epConfig };
                        newEpConfig.amznSecretKey = event.target.value;
                        setEpConfig(newEpConfig);
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                        setIsChanged(true);
                    }}
                />
            </Grid>
        </div>
    );
}

Credentials.propTypes = {
    epConfig: PropTypes.shape({}).isRequired,
    setEpConfig: PropTypes.func.isRequired,
    endpointsDispatcher: PropTypes.func.isRequired,
    awsAccessMethod: PropTypes.shape('').isRequired,
    setAwsAccessMethod: PropTypes.func.isRequired,
};
