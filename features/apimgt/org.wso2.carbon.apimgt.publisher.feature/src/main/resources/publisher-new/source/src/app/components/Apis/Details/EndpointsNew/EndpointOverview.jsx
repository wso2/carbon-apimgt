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

import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import {
    Grid,
    Paper,
    Switch,
    Collapse,
    Typography,
    withStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';

import EndpointListing from './EndpointListing';
import EndpointConfig from './EndpointConfig';
import EndpointAdd from './EndpointAdd';
import { getEndpointTypeProperty, getEndpointTemplateByType } from './endpointUtils';
import GeneralConfiguration from './GeneralConfiguration';


const styles = theme => ({
    overviewWrapper: {
        marginTop: theme.spacing.unit * 2,
    },
    listing: {
        margin: theme.spacing.unit,
        padding: theme.spacing.unit,
    },
    endpointContainer: {
        padding: '10px',
    },
    endpointName: {
        fontSize: '1rem',
        paddingTop: '5px',
        paddingBottom: '5px',
    },
    endpointTypesWrapper: {
        padding: theme.spacing.unit * 3,
        marginTop: theme.spacing.unt * 2,
    },
    sandboxHeading: {
        display: 'flex',
        alignItems: 'center',
    },
});

/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const { classes, api } = props;
    const { endpointConfig, endpointSecurity } = api;
    const [isSOAPEndpoint, setSOAPEndpoint] = useState({ key: 'http', value: 'HTTP/REST Endpoint' });
    const [epConfig, setEpConfig] = useState(endpointConfig);
    const [selectedEndpointInfo, setSelectedEndpointInfo] = useState({});
    const [selectedEpIndex, setSelectedEpIndex] = useState([]);
    const [selectedEp, setSelectedEPRef] = useState(0);
    const [isSandboxChecked, setSandboxChecked] = useState(false);
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState({});

    console.log(api);
    const endpointTypes = [{ key: 'http', value: 'HTTP/REST Endpoint' },
        { key: 'address', value: 'HTTP/SOAP Endpoint' }];

    const getFailOverCategory = (category) => {
        return (category === 'production_endpoints' ? 'production_failovers' : 'sandbox_failovers');
    };
    const configStyle = {
        position: 'relative',
        top: selectedEp,
    };

    const getSelectedEndpoint = (index, epType, category, ref) => {
        setSelectedEpIndex([index, category]);
        setSelectedEPRef(ref);
        setSelectedEPRef(ReactDOM.findDOMNode(ref).getBoundingClientRect().top); // TODO Remove
        setSelectedEndpointInfo(() => {
            let selected = {};
            switch (epType) {
                case 'load_balance':
                    selected = epConfig[category][index];
                    break;
                case 'failover':
                    if (index === 0) {
                        selected = (epConfig[category]);
                        break;
                    } else {
                        const failOverCategory = getFailOverCategory(category);
                        selected = epConfig[failOverCategory][index - 1];
                    }
                    break;
                default:
                    selected = epConfig[category];
                    break;
            }
            if (!selected) {
                selected = { url: 'asddssfsd' };
            }
            return selected;
        });
    };
    const getEndpointType = (type) => {
        if (type === 'http') {
            return endpointTypes[0];
        } else if (type === 'address') {
            return endpointTypes[1];
        } else {
            const prodEndpoints = endpointConfig.production_endpoints;
            if (Array.isArray(prodEndpoints)) {
                return prodEndpoints[0].endpoint_type !== undefined ? endpointTypes[1] : endpointTypes[0];
            }
            return prodEndpoints.endpoint_type !== undefined ? endpointTypes[1] : endpointTypes[0];
        }
    };
    useEffect(() => {
        setEpConfig(endpointConfig);
    }, []);
    useEffect(() => {
        const type = endpointConfig.endpoint_type;
        setSOAPEndpoint(getEndpointType(type));
    }, []);

    useEffect(() => {
        setEndpointSecurityInfo(endpointSecurity);
    }, []);

    useEffect(() => {
    }, [endpointSecurityInfo]);

    useEffect(() => {
        console.log('newConfig', epConfig);
    }, [epConfig]);

    useEffect(() => {
        let endpointConfiguration = {};
        if (isSOAPEndpoint.key === 'address') {
            endpointConfiguration = {
                ...epConfig,
                production_endpoints: {
                    endpoint_type: 'address',
                    template_not_supported: false,
                    url: 'http://myservice/resource',
                },
                sandbox_endpoints: {endpoint_type: 'address',
                    template_not_supported: false,
                    url: 'http://myservice/resource',
                },
                production_failovers: [],
                sandbox_failovers: [],
            };
        } else {
            endpointConfiguration = { ...epConfig };
        }
        setEpConfig(endpointConfiguration);
    }, [isSOAPEndpoint]);

    const editEndpoint = (url) => {
        let modifiedEndpoint = null;
        let endpointTypeProperty = null;
        if (selectedEpIndex[0] > 0) {
            endpointTypeProperty = getEndpointTypeProperty(epConfig.endpoint_type, selectedEpIndex[1]);
            modifiedEndpoint = epConfig[endpointTypeProperty];
            if (epConfig.endpoint_type === 'failover') {
                modifiedEndpoint[selectedEpIndex[0] - 1].url = url;
            } else {
                modifiedEndpoint[selectedEpIndex[0]].url = url;
            }
        } else {
            modifiedEndpoint = epConfig[selectedEpIndex[1]];
            if (Array.isArray(modifiedEndpoint)) {
                modifiedEndpoint[0].url = url;
            } else {
                modifiedEndpoint.url = url;
            }
        }
        setEpConfig({ ...epConfig, [endpointTypeProperty]: modifiedEndpoint });
    };

    const addEndpoint = (category, type) => {
        let endpointTemplate = {};
        if (isSOAPEndpoint.key === 'address' || type === 'failover') {
            endpointTemplate = {
                endpoint_type: isSOAPEndpoint.key,
                template_not_supported: false,
                url: 'http://appserver/resource',
            };
        } else {
            endpointTemplate = {
                url: 'http://appserver/resource',
            };
        }
        const epConfigProperty = getEndpointTypeProperty(type, category);
        let endpointList = epConfig[epConfigProperty];
        /**
         * Check whether we have existing endpoints added.
         * */
        if (endpointList) {
            if (!Array.isArray(endpointList)) {
                endpointList = [endpointList].concat(endpointTemplate);
            } else {
                endpointList = endpointList.concat(endpointTemplate);
            }
        } else {
            endpointList = [endpointTemplate];
        }
        setEpConfig({ ...epConfig, [epConfigProperty]: endpointList });
    };

    const onChangeEndpointCategory = (event) => {
        const tmpEndpointConfig = getEndpointTemplateByType(
            event.target.value,
            isSOAPEndpoint.key === 'address',
            epConfig,
        );
        setEpConfig({ ...tmpEndpointConfig });
    };

    const handleEndpointTypeSelect = (event) => {
        const selectedKey = event.target.value;
        const selectedType = endpointTypes.filter((type) => {
            return type.key === selectedKey;
        })[0];
        setSOAPEndpoint(selectedType);
    };

    const handleToggleEndpointSecurity = () => {
        setEndpointSecurityInfo(() => {
            if (endpointSecurityInfo === null) {
                return { type: 'BASIC', username: '', password: '' };
            }
            return null;
        });
    };

    const removeEndpoint = (index, epType, category) => {
        const endpointConfigProperty = getEndpointTypeProperty(epType, category);
        const indexToRemove = epType === 'failover' ? index - 1 : index;
        const tmpEndpoints = epConfig[endpointConfigProperty];
        tmpEndpoints.splice(indexToRemove, 1);
        setEpConfig({ ...epConfig, [endpointConfigProperty]: tmpEndpoints });
    };

    const handleEndpointSecurityChange = (event, field) => {
        setEndpointSecurityInfo({ ...endpointSecurityInfo, [field]: event.target.value });
    };

    return (
        <div className={classes.overviewWrapper}>
            <GeneralConfiguration
                epConfig={epConfig}
                endpointSecurityInfo={endpointSecurityInfo}
                onChangeEndpointCategory={onChangeEndpointCategory}
                handleToggleEndpointSecurity={handleToggleEndpointSecurity}
                handleEndpointSecurityChange={handleEndpointSecurityChange}
                handleEndpointTypeSelect={handleEndpointTypeSelect}
                isSOAPEndpoint={isSOAPEndpoint}
            />
            <Grid container xs={12}>
                <Grid item xs={6}>
                    <Paper className={classes.endpointContainer}>
                        <Typography className={classes.endpointName}>
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.EndpointOverview.production'
                                defaultMessage='Production'
                            />
                        </Typography>
                        <EndpointListing
                            getSelectedEndpoint={getSelectedEndpoint}
                            apiEndpoints={epConfig.production_endpoints}
                            failOvers={epConfig.production_failovers}
                            selectedEpIndex={selectedEpIndex}
                            epType={epConfig.endpoint_type}
                            endpointAddComponent={<EndpointAdd />}
                            addNewEndpoint={addEndpoint}
                            removeEndpoint={removeEndpoint}
                            category='production_endpoints'
                        />
                        <div className={classes.sandboxHeading}>
                            <Typography className={classes.endpointName}>
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.EndpointOverview.sandbox'
                                    defaultMessage='Sandbox'
                                />
                            </Typography>
                            <Switch checked={isSandboxChecked} onChange={() => setSandboxChecked(!isSandboxChecked)} />
                        </div>
                        <Collapse in={isSandboxChecked}>
                            <EndpointListing
                                getSelectedEndpoint={getSelectedEndpoint}
                                apiEndpoints={epConfig.sandbox_endpoints}
                                selectedEpIndex={selectedEpIndex}
                                failOvers={epConfig.sandbox_failovers}
                                epType={epConfig.endpoint_type}
                                endpointAddComponent={<EndpointAdd />}
                                addNewEndpoint={addEndpoint}
                                removeEndpoint={removeEndpoint}
                                category='sandbox_endpoints'
                            />

                        </Collapse>
                    </Paper>
                </Grid>
                <Grid item xs={6}>
                    <div style={configStyle}>
                        <EndpointConfig epInfo={selectedEndpointInfo} changeEndpointURL={editEndpoint} />
                    </div>
                </Grid>
            </Grid>
        </div>
    );
}

EndpointOverview.propTypes = {
    classes: PropTypes.shape({
        overviewWrapper: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        endpointName: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(EndpointOverview));
