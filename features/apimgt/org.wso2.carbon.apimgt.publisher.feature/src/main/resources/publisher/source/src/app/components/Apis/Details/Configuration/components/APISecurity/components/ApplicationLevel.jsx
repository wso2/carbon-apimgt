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
import Checkbox from '@material-ui/core/Checkbox';
import FormGroup from '@material-ui/core/FormGroup';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import WrappedExpansionPanel from 'AppComponents/Shared/WrappedExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import AuthorizationHeader from 'AppComponents/Apis/Details/Configuration/components/AuthorizationHeader.jsx';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormHelperText from '@material-ui/core/FormHelperText';
import { FormattedMessage } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import KeyManager from 'AppComponents/Apis/Details/Configuration/components/KeyManager';
import API from 'AppData/api';

import {
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_API_KEY,
    API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY,
    API_SECURITY_MUTUAL_SSL,
} from './apiSecurityConstants';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(1),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    iconSpace: {
        marginLeft: theme.spacing(0.5),
    },
    bottomSpace: {
        marginBottom: theme.spacing(4),
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
}));

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ApplicationLevel(props) {
    const {
        haveMultiLevelSecurity, securityScheme, configDispatcher, api,
    } = props;
    const [apiFromContext] = useAPI();
    const classes = useStyles();
    let mandatoryValue = null;
    let hasResourceWithSecurity;
    if (apiFromContext.apiType === API.CONSTS.APIProduct) {
        const apiList = apiFromContext.apis;
        for (const apiInProduct in apiList) {
            if (Object.prototype.hasOwnProperty.call(apiList, apiInProduct)) {
                hasResourceWithSecurity = apiList[apiInProduct].operations.findIndex(
                    (op) => op.authType !== 'None',
                ) > -1;
                if (hasResourceWithSecurity) {
                    break;
                }
            }
        }
    } else {
        hasResourceWithSecurity = apiFromContext.operations.findIndex((op) => op.authType !== 'None') > -1;
    }

    mandatoryValue = 'optional';
    // If not Oauth2, Basic auth or ApiKey security is selected, no mandatory values should be pre-selected
    if (!(securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2) || securityScheme.includes(API_SECURITY_BASIC_AUTH)
        || securityScheme.includes(API_SECURITY_API_KEY))) {
        mandatoryValue = null;
    } else if (!securityScheme.includes(API_SECURITY_MUTUAL_SSL)) {
        mandatoryValue = API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY;
    } else if (securityScheme.includes(API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY)) {
        mandatoryValue = API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY;
    }

    return (
        <>
            <Grid item xs={12}>
                <WrappedExpansionPanel className={classes.expansionPanel} id='applicationLevel'>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography className={classes.subHeading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Components.APISecurity.Components.
                                    ApplicationLevel.http'
                                defaultMessage='Application Level Security'
                            />
                            <Tooltip
                                title={(
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.APISecurity.tooltip'
                                        defaultMessage={
                                            'This option determines the type of security'
                                            + ' that will be used to secure this API. An API can be secured '
                                            + 'with either OAuth2/Basic/ApiKey or it can be secured with all of them. '
                                            + 'If OAuth2 option is selected, relevant API will require a valid '
                                            + 'OAuth2 token for successful invocation.'
                                        }
                                    />
                                )}
                                aria-label='APISecurity'
                                placement='right-end'
                                interactive
                            >
                                <HelpOutline className={classes.iconSpace} />
                            </Tooltip>
                        </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                        <FormGroup style={{ display: 'flow-root' }}>
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                        checked={securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2)}
                                        onChange={({ target: { checked, value } }) => configDispatcher({
                                            action: 'securityScheme',
                                            event: { checked, value },
                                        })}
                                        value={DEFAULT_API_SECURITY_OAUTH2}
                                        color='primary'
                                    />
                                )}
                                label='OAuth2'
                            />
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                        checked={securityScheme.includes(API_SECURITY_BASIC_AUTH)}
                                        onChange={({ target: { checked, value } }) => configDispatcher({
                                            action: 'securityScheme',
                                            event: { checked, value },
                                        })}
                                        value={API_SECURITY_BASIC_AUTH}
                                        color='primary'
                                    />
                                )}
                                label='Basic'
                            />
                            <FormControlLabel
                                control={(
                                    <Checkbox
                                        checked={securityScheme.includes(API_SECURITY_API_KEY)}
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                        onChange={({ target: { checked, value } }) => configDispatcher({
                                            action: 'securityScheme',
                                            event: { checked, value },
                                        })}
                                        value={API_SECURITY_API_KEY}
                                        color='primary'
                                    />
                                )}
                                label='Api Key'
                            />
                        </FormGroup>
                        <FormControl className={classes.bottomSpace} component='fieldset'>
                            <RadioGroup
                                aria-label='HTTP security HTTP mandatory selection'
                                name={API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY}
                                value={mandatoryValue}
                                onChange={({ target: { name, value } }) => configDispatcher({
                                    action: 'securityScheme',
                                    event: { name, value },
                                })}
                                row
                            >
                                <FormControlLabel
                                    value={API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY}
                                    control={(
                                        <Radio
                                            disabled={!haveMultiLevelSecurity
                                                || isRestricted(['apim:api_create'], apiFromContext)}
                                            color='primary'
                                        />
                                    )}
                                    label='Mandatory'
                                    labelPlacement='end'
                                />
                                <FormControlLabel
                                    value='optional'
                                    control={(
                                        <Radio
                                            disabled={!haveMultiLevelSecurity
                                                || isRestricted(['apim:api_create'], apiFromContext)}
                                            color='primary'
                                        />
                                    )}
                                    label='Optional'
                                    labelPlacement='end'
                                />
                            </RadioGroup>
                            <FormHelperText>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.APISecurity.application.mandatory'
                                    defaultMessage='Choose whether Application level security is mandatory or optional'
                                />
                            </FormHelperText>
                        </FormControl>
                        {(apiFromContext.apiType === API.CONSTS.API) && (
                            <KeyManager
                                api={api}
                                configDispatcher={configDispatcher}
                            />
                        )}
                        <AuthorizationHeader api={api} configDispatcher={configDispatcher} />
                        <FormControl>
                            {!hasResourceWithSecurity
                            && (
                                <FormHelperText>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.APISecurity.api.unsecured'
                                        defaultMessage='Application level security is not required since API
                                        has no secured resources'
                                    />
                                </FormHelperText>
                            )}
                        </FormControl>
                    </ExpansionPanelDetails>
                </WrappedExpansionPanel>
            </Grid>
        </>
    );
}

ApplicationLevel.propTypes = {
    configDispatcher: PropTypes.func.isRequired,
    haveMultiLevelSecurity: PropTypes.bool.isRequired,
    securityScheme: PropTypes.arrayOf(PropTypes.string).isRequired,
    api: PropTypes.shape({}).isRequired,
};
