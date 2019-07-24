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
import {
    FormControl,
    Grid,
    Paper,
    Typography,
    withStyles,
    Radio,
    FormControlLabel,
    RadioGroup, Icon, Dialog, DialogTitle, DialogContent, IconButton,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';

import EndpointListing from './EndpointListing';
import { getEndpointTemplateByType, getEndpointTypeProperty } from './endpointUtils';
import GeneralConfiguration from './GeneralConfiguration';
import GenericEndpoint from './GenericEndpoint';
import LoadBalanceConfig from './LoadBalanceConfig';
import AdvanceEndpointConfig from './AdvancedConfig/AdvanceEndpointConfig';


const styles = theme => ({
    overviewWrapper: {
        marginTop: theme.spacing.unit * 2,
    },
    listing: {
        margin: theme.spacing.unit,
        padding: theme.spacing.unit,
    },
    endpointContainer: {
        paddingBottom: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        width: '100%',
        marginTop: theme.spacing.unit,
    },
    endpointName: {
        paddingLeft: theme.spacing.unit,
        fontSize: '1rem',
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
    },
    endpointTypesWrapper: {
        padding: theme.spacing.unit * 3,
        marginTop: theme.spacing.unit * 2,
    },
    sandboxHeading: {
        display: 'flex',
        alignItems: 'center',
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
    },
    endpointsWrapperLeft: {
        padding: theme.spacing.unit,
        borderRight: '#c4c4c4',
        borderRightStyle: 'solid',
        borderRightWidth: 'thin',
    },
    endpointsWrapperRight: {
        padding: theme.spacing.unit,
    },
    endpointsTypeSelectWrapper: {
        marginLeft: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit * 2,
        padding: theme.spacing.unit,
        display: 'flex',
        justifyContent: 'space-between',
    },
    endpointTypesSelectWrapper: {
        display: 'flex',
    },
    defaultEndpointWrapper: {
        paddingLeft: theme.spacing.unit,
        paddingRight: theme.spacing.unit,
        marginRight: theme.spacing.unit,
    },
    configDialogHeader: {
        fontWeight: '600',
    },
});

/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const { classes, api, onChangeAPI } = props;
    const { endpointConfig, endpointSecurity } = api;
    const [endpointType, setEndpointType] = useState({ key: 'http', value: 'HTTP/REST Endpoint' });
    const [epConfig, setEpConfig] = useState(endpointConfig);
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState({});
    const [isLBConfigOpen, setLBConfigOpen] = useState(false);
    const [advanceConfigOptions, setAdvancedConfigOptions] = useState({
        open: false,
        index: 0,
        type: '',
        category: '',
        config: undefined,
    });

    const endpointTypes = [{ key: 'http', value: 'HTTP/REST Endpoint' },
        { key: 'address', value: 'HTTP/SOAP Endpoint' }];

    /**
     * Method to get the type of the endpoint. (HTTP/REST or HTTP/SOAP)
     * In failover/ loadbalance cases, the endpoint type is presented in the endpoints list. Therefore that property
     * needs to be extracted separately.
     *
     * @param {string} type  The representative type of the endpoint.
     * @return {string} The type of the endpoint.
     * */
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
        setEndpointType(getEndpointType(endpointConfig.endpoint_type));
        setEndpointSecurityInfo(endpointSecurity);
    }, []);

    useEffect(() => {
        onChangeAPI({ ...api, endpointSecurity: endpointSecurityInfo });
    }, [endpointSecurityInfo]);

    useEffect(() => {
        onChangeAPI({ ...api, endpointConfig: epConfig });
    }, [epConfig]);

    useEffect(() => {
    }, [endpointType]);

    /**
     * Method to modify the endpoint represented by the given parameters.
     *
     * @param {number} index The index of the endpoint in the listing.
     * @param {string} category The endpoint category. (production/ sand box)
     * @param {string} url The new endpoint url.
     * */
    const editEndpoint = (index, category, url) => {
        let modifiedEndpoint = null;
        const endpointTypeProperty = getEndpointTypeProperty(epConfig.endpoint_type, category);
        if (index > 0) {
            modifiedEndpoint = epConfig[endpointTypeProperty];
            if (epConfig.endpoint_type === 'failover') {
                modifiedEndpoint[index - 1].url = url;
            } else {
                modifiedEndpoint[index].url = url;
            }
        } else {
            modifiedEndpoint = epConfig[category];
            if (Array.isArray(modifiedEndpoint)) {
                modifiedEndpoint[0].url = url;
            } else {
                modifiedEndpoint.url = url;
            }
        }
        setEpConfig({ ...epConfig, [endpointTypeProperty]: modifiedEndpoint });
    };

    /**
     * Method to add new loadbalance/ failover endpoint to the existing endpoints.
     *
     * @param {string} category The endpoint category (production/ sandbox)
     * @param {string} type The endpoint type. (load_balance/ failover)
     * @param {string} newURL The url of the new endpoint.
     * */
    const addEndpoint = (category, type, newURL) => {
        let endpointTemplate = {};
        if (endpointType.key === 'address' || type === 'failover') {
            endpointTemplate = {
                endpoint_type: endpointType.key,
                template_not_supported: false,
                url: newURL,
            };
        } else {
            endpointTemplate = {
                url: newURL,
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

    /**
     * Method to capture the endpoint category change.
     * @param {any} event The html element change event.
     * */
    const onChangeEndpointCategory = (event) => {
        const tmpEndpointConfig = getEndpointTemplateByType(
            event.target.value,
            endpointType.key === 'address',
            epConfig,
        );
        setEpConfig({ ...tmpEndpointConfig });
    };

    /**
     * Handles the endpoint type select event.
     * @param {any} event The select event.
     * */
    const handleEndpointTypeSelect = (event) => {
        const selectedKey = event.target.value;
        const selectedType = endpointTypes.filter((type) => {
            return type.key === selectedKey;
        })[0];
        let endpointConfiguration = {};
        if (selectedKey === 'address') {
            endpointConfiguration = {
                ...epConfig,
                endpoint_type: 'address',
                production_endpoints: {
                    endpoint_type: 'address',
                    template_not_supported: false,
                    url: 'http://myservice/resource',
                },
                sandbox_endpoints: {
                    endpoint_type: 'address',
                    template_not_supported: false,
                    url: 'http://myservice/resource',
                },
            };
        } else {
            endpointConfiguration = {
                ...epConfig,
                production_endpoints: {
                    url: 'http://myservice/resource',
                },
                sandbox_endpoints: {
                    url: 'http://myservice/resource',
                },
                endpoint_type: 'http',
            };
        }
        setEpConfig(endpointConfiguration);
        setEndpointType(selectedType);
    };

    /**
     * Handles the endpoint security toggle action.
     * */
    const handleToggleEndpointSecurity = () => {
        setEndpointSecurityInfo(() => {
            if (endpointSecurityInfo === null) {
                return { type: 'BASIC', username: '', password: '' };
            }
            return null;
        });
    };

    /**
     * Method to get the advance configuration from the selected endpoint.
     *
     * @param {number} index The selected endpoint index
     * @param {string} epType The type of the endpoint. (loadbalance/ failover)
     * @param {string} category The endpoint category (Production/ sandbox)
     * @return {object} The advance config object of the endpoint.
     * */
    const getAdvanceConfig = (index, epType, category) => {
        const endpointTypeProperty = getEndpointTypeProperty(epType, category);
        let advanceConfig = {};
        if (index > 0) {
            if (epConfig.endpoint_type === 'failover') {
                advanceConfig = epConfig[endpointTypeProperty][index - 1].config;
            } else {
                advanceConfig = epConfig[endpointTypeProperty][index].config;
            }
        } else {
            const endpointInfo = epConfig[endpointTypeProperty];
            if (Array.isArray(endpointInfo)) {
                advanceConfig = endpointInfo[0].config;
            } else {
                advanceConfig = endpointInfo.config;
            }
        }
        return advanceConfig;
    };

    /**
     * Method to open/ close the advance configuration dialog. This method also sets some information about the
     * seleted endpoint type/ category and index.
     *
     * @param {number} index The index of the selected endpoint.
     * @param {string} type The endpoint type
     * @param {string} category The endpoint category.
     * */
    const toggleAdvanceConfig = (index, type, category) => {
        const advanceEPConfig = getAdvanceConfig(index, type, category);
        setAdvancedConfigOptions(() => {
            return ({
                open: !advanceConfigOptions.open,
                index,
                type,
                category,
                config: advanceEPConfig === undefined ? {} : advanceEPConfig,
            });
        });
    };

    /**
     * Method to handle the loadbalance configuration changes.
     *
     * @param {object} lbConfig The modified loadbalance configuration.
     * */
    const handleLBConfigChange = (lbConfig) => {
        setEpConfig({ ...epConfig, ...lbConfig });
        setLBConfigOpen(false);
    };

    /**
     * Method to remove the selected endpoint from the endpoints list.
     *
     * @param {number} index The selected endpoint index
     * @param {string} epType The type of the endpoint. (loadbalance/ failover)
     * @param {string} category The endpoint category (production/ sandbox)
     * */
    const removeEndpoint = (index, epType, category) => {
        const endpointConfigProperty = getEndpointTypeProperty(epType, category);
        const indexToRemove = epType === 'failover' ? index - 1 : index;
        const tmpEndpoints = epConfig[endpointConfigProperty];
        tmpEndpoints.splice(indexToRemove, 1);
        setEpConfig({ ...epConfig, [endpointConfigProperty]: tmpEndpoints });
    };

    /**
     * Method to handle the endpoint security changes.
     * @param {any} event The html event
     * @param {string} field The security propety that is being modified.
     * */
    const handleEndpointSecurityChange = (event, field) => {
        setEndpointSecurityInfo({ ...endpointSecurityInfo, [field]: event.target.value });
    };

    /**
     * Method to save the advance configurations.
     *
     * @param {object} advanceConfig The advance configuration object.
     * */
    const saveAdvanceConfig = (advanceConfig) => {
        const endpointConfigProperty =
            getEndpointTypeProperty(advanceConfigOptions.type, advanceConfigOptions.category);
        const endpoints = epConfig[endpointConfigProperty];
        if (Array.isArray(endpoints)) {
            if (advanceConfigOptions.type === 'failover') {
                endpoints[advanceConfigOptions.index - 1].config = advanceConfig;
            } else {
                endpoints[advanceConfigOptions.index].config = advanceConfig;
            }
        } else {
            endpoints.config = advanceConfig;
        }
        setAdvancedConfigOptions({ open: false });
        setEpConfig({ ...epConfig, [endpointConfigProperty]: endpoints });
    };

    /**
     * Method to close the advance configuration dialog box.
     * */
    const closeAdvanceConfig = () => {
        setAdvancedConfigOptions({ open: false });
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
                endpointType={endpointType}
            />
            <Paper className={classes.endpointContainer}>
                <Grid container xs spacing={2}>
                    <Grid xs className={classes.endpointsWrapperLeft}>
                        <Typography className={classes.endpointName}>
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.EndpointOverview.production'
                                defaultMessage='Production'
                            />
                        </Typography>
                        <GenericEndpoint
                            className={classes.defaultEndpointWrapper}
                            endpointURL={epConfig.production_endpoints.length > 0 ?
                                epConfig.production_endpoints[0].url : epConfig.production_endpoints.url}
                            type=''
                            index={0}
                            category='production_endpoints'
                            editEndpoint={editEndpoint}
                            setAdvancedConfigOpen={toggleAdvanceConfig}
                        />
                    </Grid>
                    <Grid xs className={classes.endpointsWrapperRight}>
                        <div className={classes.sandboxHeading}>
                            <Typography className={classes.endpointName}>
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.EndpointOverview.sandbox'
                                    defaultMessage='Sandbox'
                                />
                            </Typography>
                        </div>
                        <GenericEndpoint
                            className={classes.defaultEndpointWrapper}
                            endpointURL={epConfig.sandbox_endpoints.length > 0 ?
                                epConfig.sandbox_endpoints[0].url : epConfig.sandbox_endpoints.url}
                            type=''
                            index={0}
                            category='sandbox_endpoints'
                            editEndpoint={editEndpoint}
                            setAdvancedConfigOpen={toggleAdvanceConfig}
                        />
                    </Grid>
                </Grid>
                <Grid xs className={classes.endpointsTypeSelectWrapper}>
                    <div />
                    <div className={classes.endpointTypesSelectWrapper}>
                        <FormControl component='fieldset' className={classes.formControl}>
                            <RadioGroup
                                aria-label='EndpointType'
                                name='endpointType'
                                className={classes.radioGroup}
                                value={epConfig.endpoint_type}
                                onChange={onChangeEndpointCategory}
                            >
                                <FormControlLabel
                                    value='failover'
                                    control={<Radio />}
                                    label='Failover'
                                />
                                <FormControlLabel
                                    value='load_balance'
                                    control={<Radio />}
                                    label='Load balance'
                                />
                            </RadioGroup>
                        </FormControl>
                        <div>
                            <IconButton
                                disabled={epConfig.endpoint_type !== 'load_balance'}
                                aria-label='Delete'
                                onClick={() => setLBConfigOpen(true)}
                            >
                                <Icon>
                                    settings
                                </Icon>
                            </IconButton>
                        </div>
                    </div>
                    <div />
                </Grid>
                <Grid xs container>
                    <Grid xs className={classes.endpointsWrapperLeft}>
                        <EndpointListing
                            apiEndpoints={epConfig.production_endpoints}
                            failOvers={epConfig.production_failovers}
                            epType={epConfig.endpoint_type}
                            addNewEndpoint={addEndpoint}
                            removeEndpoint={removeEndpoint}
                            editEndpoint={editEndpoint}
                            setAdvancedConfigOpen={toggleAdvanceConfig}
                            category='production_endpoints'
                        />
                    </Grid>
                    <Grid xs className={classes.endpointsWrapperRight}>
                        <EndpointListing
                            apiEndpoints={epConfig.sandbox_endpoints}
                            failOvers={epConfig.sandbox_failovers}
                            epType={epConfig.endpoint_type}
                            addNewEndpoint={addEndpoint}
                            removeEndpoint={removeEndpoint}
                            editEndpoint={editEndpoint}
                            setAdvancedConfigOpen={toggleAdvanceConfig}
                            category='sandbox_endpoints'
                        />
                    </Grid>
                </Grid>
            </Paper>
            <Dialog open={isLBConfigOpen}>
                <DialogTitle>
                    <Typography className={classes.configDialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.EndpointOverview.load.balance.configuration.title'
                            defaultMessage='Load Balance Configuration'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <LoadBalanceConfig
                        handleLBConfigChange={handleLBConfigChange}
                        closeLBConfigDialog={() => setLBConfigOpen(false)}
                        algoCombo={epConfig.algoCombo}
                        algoClassName={epConfig.algoClassName}
                        sessionTimeOut={epConfig.sessionTimeOut}
                        sessionManagement={epConfig.sessionManagement}
                    />
                </DialogContent>
            </Dialog>
            <Dialog open={advanceConfigOptions.open}>
                <DialogTitle>
                    <Typography className={classes.configDialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.EndpointOverview.advance.endpoint.configuration'
                            defaultMessage='Advance Configuration'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <AdvanceEndpointConfig
                        isSOAPEndpoint={endpointType.key === 'address'}
                        advanceConfig={advanceConfigOptions.config}
                        onSaveAdvanceConfig={saveAdvanceConfig}
                        onCancel={closeAdvanceConfig}
                    />
                </DialogContent>
            </Dialog>
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
    onChangeAPI: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointOverview));
