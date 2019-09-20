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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';

import ApplicationLevel from './components/ApplicationLevel';
import TransportLevel from './components/TransportLevel';

// Check this file for more info  <CARBON_APIMGT>/components/apimgt/org.wso2.carbon.apimgt.impl
// /src/main/java/org/wso2/carbon/apimgt/impl/APIConstants.java

const DEFAULT_API_SECURITY_OAUTH2 = 'oauth2';
const API_SECURITY_BASIC_AUTH = 'basic_auth';
const API_SECURITY_API_KEY = 'api_key';
const API_SECURITY_MUTUAL_SSL = 'mutualssl';
const API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY = 'oauth_basic_auth_api_key_mandatory';
const API_SECURITY_MUTUAL_SSL_MANDATORY = 'mutualssl_mandatory';

const useStyles = makeStyles(theme => ({
    error: {
        color: theme.palette.error.main,
    },
    bottomSpace: {
        marginBottom: theme.spacing(4),
    },
}));

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function APISecurity(props) {
    const {
        api: { securityScheme, id },
        configDispatcher,
        api,
    } = props;
    const haveMultiLevelSecurity =
        securityScheme.includes(API_SECURITY_MUTUAL_SSL) &&
        (securityScheme.includes(API_SECURITY_BASIC_AUTH) ||
        securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2) || securityScheme.includes(API_SECURITY_API_KEY));
    const classes = useStyles();

    // Check the validation conditions and return an error message
    const Validate = () => {
        if (
            !securityScheme.includes(API_SECURITY_MUTUAL_SSL) &&
            !securityScheme.includes(API_SECURITY_BASIC_AUTH) &&
            !securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2) &&
            !securityScheme.includes(API_SECURITY_API_KEY)
        ) {
            return (
                <Typography className={classes.bottomSpace}>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.APISecurity.emptySchemas'
                        defaultMessage='Please select at least one API security method!'
                    />
                </Typography>
            );
        } else if (
            // User has enabled both security levels and set both levels as optional
            haveMultiLevelSecurity &&
            !(
                securityScheme.includes(API_SECURITY_MUTUAL_SSL_MANDATORY) ||
                securityScheme.includes(API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY)
            )
        ) {
            return (
                <Typography className={classes.bottomSpace}>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.APISecurity.allOptional'
                        defaultMessage='Please select at least one API security level mandatory!'
                    />
                </Typography>
            );
        }
        return null; // No errors :-)
    };
    return (
        <React.Fragment>
            <Grid container spacing={2} alignItems='flex-start'>
                <TransportLevel
                    haveMultiLevelSecurity={haveMultiLevelSecurity}
                    securityScheme={securityScheme}
                    configDispatcher={configDispatcher}
                    api={api}
                    id={id}
                />
                <ApplicationLevel
                    haveMultiLevelSecurity={haveMultiLevelSecurity}
                    securityScheme={securityScheme}
                    api={api}
                    configDispatcher={configDispatcher}
                    id={id}
                />
                <Grid item>
                    <span className={classes.error}>
                        <Validate />
                    </span>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}

APISecurity.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};

export {
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_API_KEY,
    API_SECURITY_MUTUAL_SSL,
    API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
};
