
/* eslint-disable no-unreachable */
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

import React, { useContext } from 'react';
import { Link as MUILink } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import Tooltip from '@material-ui/core/Tooltip';
import API from 'AppData/api';
import Utils from 'AppData/Utils';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, useIntl } from 'react-intl';
import { ApiContext } from './ApiContext';


const useStyles = makeStyles((theme) => ({
    iconStyle: {
        cursor: 'pointer',
        margin: '-10px 0',
        padding: '0 0 0 5px',
        '& .material-icons': {
            fontSize: 18,
            color: theme.palette.secondary.main,
        },
    },
    buttonIcon: {
        marginRight: 10,
    },
    downloadLink: {
        fontSize: 14,
        color: theme.palette.primary.main,
        display: 'flex',
    },
}));

/**
 * Renders the download links.
 * @returns {JSX} rendered output
 */
function SourceDownload(props) {
    const { selectedEndpoint } = props;
    const { api } = useContext(ApiContext);
    const apiClient = new API();
    const classes = useStyles();
    const intl = useIntl();

    /**
     * Downloads the WSDL of the api for the provided environment
     *
     * @param {string} apiId uuid of the API
     * @param {string} environmentName name of the environment
     */
    const downloadWSDL = (apiId, environmentName) => {
        const wsdlClient = apiClient.getWsdlClient();
        const promisedGet = wsdlClient.downloadWSDLForEnvironment(apiId, environmentName);
        promisedGet
            .then((done) => {
                Utils.downloadFile(done);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Environments.download.wsdl.error',
                        defaultMessage: 'Error downloading the WSDL',
                    }));
                }
            });
    };

    /**
     * Downloads the swagger of the api for the provided environment
     *
     * @param {string} apiId uuid of the API
     * @param {string} environment name of the environment
     */
    const downloadSwagger = (apiId, environment) => {
        const promiseSwagger = apiClient.getSwaggerByAPIIdAndEnvironment(apiId, environment);
        promiseSwagger
            .then((done) => {
                Utils.downloadFile(done);
            })
            .catch((error) => {
                console.log(error);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Environments.download.swagger.error',
                    defaultMessage: 'Error downloading the Swagger',
                }));
            });
    };
    if (
        api.type === 'SOAP') {
        return (
            <Tooltip
                title={(
                    <FormattedMessage
                        id='Apis.Details.Environments.download.wsdl'
                        defaultMessage='WSDL'
                    />
                )}
                placement='right'
                className={classes.iconStyle}
            >
                <MUILink
                    onClick={() => downloadWSDL(api.id, selectedEndpoint.environmentName)}
                >
                    <CloudDownloadRounded className={classes.buttonIcon} />
                    <FormattedMessage
                        id='Apis.Details.Environments.download.wsdl.text'
                        defaultMessage='Download WSDL'
                    />
                </MUILink>
            </Tooltip>
        );
    }
    if (api.type === 'HTTP' || api.type === 'SOAPTOREST') {
        return (
            <Tooltip
                title={(
                    <FormattedMessage
                        id='Apis.Details.Environments.download.swagger'
                        defaultMessage='Swagger'
                    />
                )}
                placement='right'
                className={classes.iconStyle}
            >
                <MUILink
                    href='#'
                    onClick={() => downloadSwagger(api.id, selectedEndpoint.environmentName)}
                    className={classes.downloadLink}
                >
                    <CloudDownloadRounded className={classes.buttonIcon} />
                    <FormattedMessage
                        id='Apis.Details.Environments.download.swagger.text'
                        defaultMessage='Download Swagger'
                    />
                </MUILink>
            </Tooltip>
        );
    }
}

export default SourceDownload;
