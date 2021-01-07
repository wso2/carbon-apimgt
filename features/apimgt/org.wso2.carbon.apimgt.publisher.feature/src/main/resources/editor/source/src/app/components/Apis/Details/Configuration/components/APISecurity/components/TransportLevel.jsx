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

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Checkbox from '@material-ui/core/Checkbox';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormHelperText from '@material-ui/core/FormHelperText';
import { FormattedMessage, injectIntl } from 'react-intl';
import Certificates from 'AppComponents/Apis/Details/Endpoints/GeneralConfiguration/Certificates';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import WrappedExpansionPanel from 'AppComponents/Shared/WrappedExpansionPanel';
import Transports from 'AppComponents/Apis/Details/Configuration/components/Transports.jsx';

import {
    API_SECURITY_MUTUAL_SSL,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_API_KEY,
} from './apiSecurityConstants';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(1),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
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
function TransportLevel(props) {
    const {
        haveMultiLevelSecurity, securityScheme, configDispatcher, intl, id, api,
    } = props;
    const isMutualSSLEnabled = securityScheme.includes(API_SECURITY_MUTUAL_SSL);
    const [apiFromContext] = useAPI();
    const [clientCertificates, setClientCertificates] = useState([]);
    const classes = useStyles();

    /**
     * Method to upload the certificate content by calling the rest api.
     *
     * @param {string} certificate The certificate needs to be associated with the API
     * @param {string} policy The tier to be used for the certificate.
     * @param {string} alias The alias of the certificate to be deleted.
     *
     * */
    const saveClientCertificate = (certificate, policy, alias) => {
        return API.addClientCertificate(id, certificate, policy, alias).then((resp) => {
            if (resp.status === 201) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Configuration.components.APISecurity.TranportLevel.certificate.add.success',
                    defaultMessage: 'Certificate added successfully',
                }));
                const tmpCertificates = [...clientCertificates];
                tmpCertificates.push({
                    apiId: resp.obj.apiId,
                    alias: resp.obj.alias,
                    tier: resp.obj.tier,
                });
                setClientCertificates(tmpCertificates);
            }
        }).catch((error) => {
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Configuration.components.APISecurity.TranportLevel.certificate.alias.error',
                    defaultMessage: 'Something went wrong while adding the API certificate',
                }));
            }
        });
    };

    /**
     * Method to delete the selected certificate.
     *
     * @param {string} alias The alias of the certificate to be deleted.
     * */
    const deleteClientCertificate = (alias) => {
        return API.deleteClientCertificate(alias, id).then((resp) => {
            setClientCertificates(() => {
                if (resp.status === 200) {
                    return clientCertificates.filter((cert) => {
                        return cert.alias !== alias;
                    });
                } else {
                    return -1;
                }
            });
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.Configuration.components.APISecurity.TranportLevel.certificate.delete.success',
                defaultMessage: 'Certificate Deleted Successfully',
            }));
        }).catch((error) => {
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Configuration.components.APISecurity.TranportLevel.certificate.delete.error',
                    defaultMessage: 'Error while deleting certificate',
                }));
            }
        });
    };

    // Get the client certificates from backend.
    useEffect(() => {
        API.getAllClientCertificates(id).then((resp) => {
            const { certificates } = resp.obj;
            setClientCertificates(certificates);
        }).catch((err) => {
            console.error(err);
            setClientCertificates([]);
        });
    }, []);

    let mandatoryValue = 'optional';
    // If not mutual ssl security is selected, no mandatory values should be pre-selected
    if (!isMutualSSLEnabled) {
        mandatoryValue = 'null';
    } else if (
        !(securityScheme.includes(DEFAULT_API_SECURITY_OAUTH2) || securityScheme.includes(API_SECURITY_BASIC_AUTH)
            || securityScheme.includes(API_SECURITY_API_KEY))
    ) {
        mandatoryValue = API_SECURITY_MUTUAL_SSL_MANDATORY;
    } else if (securityScheme.includes(API_SECURITY_MUTUAL_SSL_MANDATORY)) {
        mandatoryValue = API_SECURITY_MUTUAL_SSL_MANDATORY;
    }
    return (
        <>
            <Grid item xs={12}>
                <WrappedExpansionPanel className={classes.expansionPanel} id='transportLevel'>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography className={classes.subHeading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Components.APISecurity.Components.
                                    TransportLevel.transport.level.security'
                                defaultMessage='Transport Level Security'
                            />
                        </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                        <Transports api={api} configDispatcher={configDispatcher} securityScheme={securityScheme} />
                        <FormControlLabel
                            control={(
                                <Checkbox
                                    disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                    checked={isMutualSSLEnabled}
                                    onChange={({ target: { checked, value } }) => configDispatcher({
                                        action: 'securityScheme',
                                        event: { checked, value },
                                    })}
                                    value={API_SECURITY_MUTUAL_SSL}
                                    color='primary'
                                />
                            )}
                            label='Mutual SSL'
                        />
                        {isMutualSSLEnabled && (
                            <FormControl component='fieldset'>
                                <RadioGroup
                                    aria-label='HTTP security SSL mandatory selection'
                                    name={API_SECURITY_MUTUAL_SSL_MANDATORY}
                                    value={mandatoryValue}
                                    onChange={({ target: { name, value } }) => configDispatcher({
                                        action: 'securityScheme',
                                        event: { name, value },
                                    })}
                                    row
                                >
                                    <FormControlLabel
                                        value={API_SECURITY_MUTUAL_SSL_MANDATORY}
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
                                        id='Apis.Details.Configuration.components.APISecurity.http.mandatory'
                                        defaultMessage='Choose whether Transport level security is mandatory or
                                        optional'
                                    />
                                </FormHelperText>
                            </FormControl>
                        )}
                        {isMutualSSLEnabled && (
                            // TODO:
                            // This is half baked!!!
                            // Refactor the Certificate component to share its capabilities in here and
                            // endpoints page ~tmkb
                            <Certificates
                                isMutualSSLEnabled={isMutualSSLEnabled}
                                certificates={clientCertificates}
                                uploadCertificate={saveClientCertificate}
                                deleteCertificate={deleteClientCertificate}
                                apiId={id}
                            />
                        )}
                    </ExpansionPanelDetails>
                </WrappedExpansionPanel>
            </Grid>
        </>
    );
}

TransportLevel.propTypes = {
    configDispatcher: PropTypes.func.isRequired,
    haveMultiLevelSecurity: PropTypes.bool.isRequired,
    securityScheme: PropTypes.arrayOf(PropTypes.string).isRequired,
    intl: PropTypes.shape({}).isRequired,
    id: PropTypes.string.isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl((TransportLevel));
