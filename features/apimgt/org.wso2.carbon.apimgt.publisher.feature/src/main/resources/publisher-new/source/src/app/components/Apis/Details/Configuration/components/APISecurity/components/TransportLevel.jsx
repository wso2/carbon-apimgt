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
import FormLabel from '@material-ui/core/FormLabel';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormHelperText from '@material-ui/core/FormHelperText';
import { FormattedMessage } from 'react-intl';
import Certificates from 'AppComponents/Apis/Details/Endpoints/GeneralConfiguration/Certificates';

import {
    API_SECURITY_MUTUAL_SSL,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
} from '../APISecurity';
import AuthManager from '../../../../../../../data/AuthManager';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function TransportLevel(props) {
    const { haveMultiLevelSecurity, securityScheme, configDispatcher } = props;
    const isMutualSSLEnabled = securityScheme.includes(API_SECURITY_MUTUAL_SSL);
    const isNotCreator = AuthManager.isNotCreator();

    let mandatoryValue = 'optional';
    // If not mutual ssl security is selected, no mandatory values should be pre-selected
    if (!isMutualSSLEnabled) {
        mandatoryValue = 'null';
    } else if (
        !(securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2) || securityScheme.includes(API_SECURITY_BASIC_AUTH))
    ) {
        mandatoryValue = API_SECURITY_MUTUAL_SSL_MANDATORY;
    } else if (securityScheme.includes(API_SECURITY_MUTUAL_SSL_MANDATORY)) {
        mandatoryValue = API_SECURITY_MUTUAL_SSL_MANDATORY;
    }
    return (
        <React.Fragment>
            <Grid item>
                <FormControl component='fieldset'>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.Configuration.APISecurity.ssl'
                            defaultMessage='Transport Level (TLS)'
                        />
                    </FormLabel>
                    <FormGroup>
                        <FormControlLabel
                            control={(
                                <Checkbox
                                    disabled={isNotCreator}
                                    checked={isMutualSSLEnabled}
                                    onChange={({ target: { checked, value } }) => configDispatcher({
                                        action: 'securityScheme',
                                        event: { checked, value },
                                    })
                                    }
                                    value={API_SECURITY_MUTUAL_SSL}
                                />
                            )}
                            label='Mutual SSL'
                        />
                    </FormGroup>
                </FormControl>
                <Grid item>
                    <FormControl component='fieldset'>
                        <RadioGroup
                            aria-label='HTTP security SSL mandatory selection'
                            name={API_SECURITY_MUTUAL_SSL_MANDATORY}
                            value={mandatoryValue}
                            onChange={({ target: { name, value } }) => configDispatcher({
                                action: 'securityScheme',
                                event: { name, value },
                            })
                            }
                            row
                        >
                            <FormControlLabel
                                value={API_SECURITY_MUTUAL_SSL_MANDATORY}
                                control={<Radio disabled={!haveMultiLevelSecurity} color='default' />}
                                label='Mandatory'
                                labelPlacement='end'
                            />
                            <FormControlLabel
                                value='optional'
                                control={<Radio disabled={!haveMultiLevelSecurity} color='default' />}
                                label='Optional'
                                labelPlacement='end'
                            />
                        </RadioGroup>
                        <FormHelperText>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.APISecurity.http.mandatory'
                                defaultMessage='Choose whether Transport level security is mandatory or optional'
                            />
                        </FormHelperText>
                    </FormControl>
                </Grid>
                <Grid item>
                    {isMutualSSLEnabled && (
                        // TODO:
                        // This is half baked!!!
                        // Refactor the Certificate component to share its capabilities in here and endpoints page ~tmkb
                        <Certificates
                            certificates={{}}
                            uploadCertificate={() => {}}
                            deleteCertificate={() => {}}
                        />
                    )}

                </Grid>
            </Grid>
        </React.Fragment>
    );
}

TransportLevel.propTypes = {
    configDispatcher: PropTypes.func.isRequired,
    haveMultiLevelSecurity: PropTypes.bool.isRequired,
    securityScheme: PropTypes.arrayOf(PropTypes.string).isRequired,
};
