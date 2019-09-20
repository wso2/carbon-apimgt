/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useState, useContext } from 'react';
import {
    Collapse,
    ExpansionPanel,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    FormControl,
    FormControlLabel,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Switch,
    Typography,
    withStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import EndpointSecurity from './GeneralConfiguration/EndpointSecurity';
import Certificates from './GeneralConfiguration/Certificates';
import API from '../../../../data/api'; // TODO: Use webpack aliases instead of relative paths ~tmkb
import Alert from '../../../Shared/Alert';
import { endpointsToList } from './endpointUtils';

const styles = theme => ({
    endpointTypeSelect: {
        width: '50%',
    },
    configHeaderContainer: {
        display: 'flex',
        justifyContent: 'space-between',
    },
    generalConfigContent: {
        boxShadow: 'inset -1px 2px 3px 0px #c3c3c3',
    },
    secondaryHeading: {
        fontSize: theme.typography.pxToRem(15),
        color: theme.palette.text.secondary,
        display: 'flex',
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        flexBasis: '33.33%',
        flexShrink: 0,
        fontWeight: '900',
    },
    endpointConfigSection: {
        padding: '10px',
    },
    generalConfigPanel: {
        width: '100%',
    },
});

const endpointTypes = [
    { key: 'http', value: 'HTTP/REST Endpoint' },
    { key: 'address', value: 'HTTP/SOAP Endpoint' },
    { key: 'default', value: 'Dynamic Endpoints' },
    { key: 'awslambda', value: 'AWS Lambda Endpoint' },
];

/**
 * The component which holds the general configurations of the endpoints.
 *
 * @param {any} props The input properties to the component
 * @returns {any} The HTML representation of the component.
 * */
function GeneralConfiguration(props) {
    const {
        intl,
        epConfig,
        endpointSecurityInfo,
        handleToggleEndpointSecurity,
        handleEndpointSecurityChange,
        handleEndpointTypeSelect,
        endpointType,
        classes,
        apiType,
    } = props;
    const [isConfigExpanded, setConfigExpand] = useState(true);
    const [endpointCertificates, setEndpointCertificates] = useState([]);
    const [epTypeSubHeading, setEpTypeSubHeading] = useState('Single HTTP/ REST');
    const { api } = useContext(APIContext);

    /**
     * Method to upload the certificate content by calling the rest api.
     * */
    const saveCertificate = (certificate, endpoint, alias) => {
        return API.addCertificate(certificate, endpoint, alias)
            .then((resp) => {
                if (resp.status === 201) {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificate.add.success',
                        defaultMessage: 'Certificate added successfully',
                    }));
                    const tmpCertificates = [...endpointCertificates];
                    tmpCertificates.push({
                        alias: resp.obj.alias,
                        endpoint: resp.obj.endpoint,
                    });
                    setEndpointCertificates(tmpCertificates);
                }
            })
            .catch((err) => {
                console.error(err.message);
                if (err.message === 'Conflict') {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificate.alias.exist',
                        defaultMessage: 'Adding Certificate Failed. Certificate alias exists.',
                    }));
                }
            });
    };

    /**
     * Method to get the endpoint type heading.
     * Ex: Load Balance REST/ HTTP, Fail Over SOAP/ HTTP
     *
     * @return {string} The endpoint type string.
     * */
    const getEndpointTypeSubHeading = () => {
        let type = '';
        const epType = epConfig.endpoint_type;
        const endpointTypeKey = endpointType.key;

        if (endpointTypeKey === 'default') {
            return 'Dynamic Endpoints';
        }

        if (endpointTypeKey === 'awslambda') {
            return 'AWS Lambda Endpoint';
        }

        switch (epType) {
            case 'load_balance':
                type = 'Load Balance';
                break;
            case 'failover':
                type = 'Fail Over';
                break;
            default:
                type = 'Single';
                break;
        }
        if (endpointTypeKey === 'address') {
            type = type.concat(' HTTP/ SOAP');
        } else {
            type = type.concat(' HTTP/ REST');
        }
        return type;
    };

    /**
     * Method to delete the selected certificate.
     *
     * @param {string} alias The alias of the certificate to be deleted.
     * */
    const deleteCertificate = (alias) => {
        return API.deleteEndpointCertificate(alias)
            .then((resp) => {
                setEndpointCertificates(() => {
                    if (resp.status === 200) {
                        return endpointCertificates.filter((cert) => {
                            return cert.alias !== alias;
                        });
                    } else {
                        return -1;
                    }
                });
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificate.delete.success',
                    defaultMessage: 'Certificate Deleted Successfully',
                }));
            })
            .catch((err) => {
                console.log(err);
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Endpoints.GeneralConfiguration.Certificates.certificate.delete.error',
                    defaultMessage: 'Error Deleting Certificate',
                }));
            });
    };

    useEffect(() => {
        const heading = getEndpointTypeSubHeading();
        setEpTypeSubHeading(heading);
    }, [props]);

    // Get the certificates from backend.
    useEffect(() => {
        API.getEndpointCertificates()
            .then((resp) => {
                const { certificates } = resp.obj;
                const endpoints = endpointsToList(epConfig);
                const filteredCertificates = certificates.filter((cert) => {
                    for (const endpoint of endpoints) {
                        if (endpoint.url.indexOf(cert.endpoint) !== -1) {
                            return true;
                        }
                    }
                    return false;
                });
                setEndpointCertificates(filteredCertificates);
            })
            .catch((err) => {
                console.error(err);
                setEndpointCertificates([]);
            });
    }, []);

    return (
        <React.Fragment>
            <ExpansionPanel
                expanded={isConfigExpanded}
                onChange={() => setConfigExpand(!isConfigExpanded)}
                className={classes.generalConfigPanel}
            >
                <ExpansionPanelSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls='panel1bh-content'
                    id='panel1bh-header'
                    className={classes.configHeaderContainer}
                >
                    <Typography className={classes.heading}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.GeneralConfiguration.general.configuration.heading'
                            defaultMessage='General Configuration'
                        />
                    </Typography>
                    {apiType !== 'HTTP' ? (
                        <div />
                    ) : (
                        <Typography className={classes.secondaryHeading}>
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.endpoint.type.sub.heading'
                                defaultMessage='Endpoint Type'
                            />{' '}
                            : {epTypeSubHeading}
                            {' | '}
                        </Typography>
                    )}
                    {apiType !== 'HTTP' || endpointType.key === 'awslambda' ? (
                        <div />
                    ) : (
                        <Typography
                            className={classes.secondaryHeading}
                            hidden={
                                apiType !== 'HTTP' || endpointType.key === 'default' || endpointType.key === 'awslambda'
                            }
                        >
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.endpoint.security.sub.heading'
                                defaultMessage='Endpoint Security'
                            />
                            : {endpointSecurityInfo !== null ? endpointSecurityInfo.type : 'None'}
                            {' | '}
                        </Typography>
                    )}
                    {apiType !== 'HTTP' || endpointType.key === 'default' || endpointType.key === 'awslambda' ? (
                        <div />
                    ) : (
                        <Typography
                            className={classes.secondaryHeading}
                            hidden={
                                apiType !== 'HTTP' || endpointType.key === 'default' || endpointType.key === 'awslambda'
                            }
                        >
                            <FormattedMessage
                                id='Apis.Details.Endpoints.GeneralConfiguration.certificates.sub.heading'
                                defaultMessage='Certificates'
                            />
                            : {endpointCertificates.length}
                        </Typography>
                    )}
                </ExpansionPanelSummary>
                <ExpansionPanelDetails className={classes.generalConfigContent}>
                    <Grid container direction='row' xs={12}>
                        {apiType !== 'HTTP' ? (
                            <div />
                        ) : (
                            <Grid container item xs={8}>
                                <Grid item xs className={classes.endpointConfigSection}>
                                    <FormControl className={classes.endpointTypeSelect}>
                                        <InputLabel htmlFor='endpoint-type-select'>
                                            <FormattedMessage
                                                id='Apis.Details.Endpoints.EndpointOverview.endpointType'
                                                defaultMessage='Endpoint Type'
                                            />
                                        </InputLabel>
                                        <Select
                                            disabled={isRestricted(['apim:api_create'], api)}
                                            value={endpointType.key}
                                            onChange={handleEndpointTypeSelect}
                                            inputProps={{
                                                name: 'key',
                                                id: 'endpoint-type-select',
                                            }}
                                        >
                                            {endpointTypes.map((type) => {
                                                return <MenuItem value={type.key}>{type.value}</MenuItem>;
                                            })}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                {apiType !== 'HTTP' ? (
                                    <div />
                                ) : (
                                    <Grid
                                        item
                                        xs
                                        className={classes.endpointConfigSection}
                                        hidden={endpointType.key === 'awslambda'}
                                    >
                                        <FormControlLabel
                                            value='start'
                                            checked={endpointSecurityInfo !== null}
                                            control={
                                                <Switch
                                                    color='primary'
                                                    disabled={isRestricted(['apim:api_create'], api)}
                                                />
                                            }
                                            label={
                                                <FormattedMessage
                                                    id={
                                                        'Apis.Details.Endpoints.EndpointOverview.' +
                                                        'endpoint.security.enable.switch'
                                                    }
                                                    defaultMessage='Endpoint Security'
                                                />
                                            }
                                            labelPlacement='start'
                                            onChange={handleToggleEndpointSecurity}
                                        />
                                        <Collapse in={endpointSecurityInfo !== null}>
                                            <EndpointSecurity
                                                securityInfo={endpointSecurityInfo}
                                                onChangeEndpointAuth={handleEndpointSecurityChange}
                                            />
                                        </Collapse>
                                    </Grid>
                                )}
                            </Grid>
                        )}
                        <Grid
                            item
                            xs
                            className={classes.endpointConfigSection}
                            hidden={endpointType.key === 'default' || endpointType.key === 'awslambda'}
                        >
                            <Certificates
                                certificates={endpointCertificates}
                                uploadCertificate={saveCertificate}
                                deleteCertificate={deleteCertificate}
                            />
                        </Grid>
                    </Grid>
                </ExpansionPanelDetails>
            </ExpansionPanel>
        </React.Fragment>
    );
}

GeneralConfiguration.propTypes = {
    epConfig: PropTypes.shape({}).isRequired,
    endpointSecurityInfo: PropTypes.shape({}).isRequired,
    handleToggleEndpointSecurity: PropTypes.func.isRequired,
    handleEndpointSecurityChange: PropTypes.func.isRequired,
    handleEndpointTypeSelect: PropTypes.func.isRequired,
    endpointType: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    apiType: PropTypes.string.isRequired,
};

export default injectIntl(withStyles(styles)(GeneralConfiguration));
