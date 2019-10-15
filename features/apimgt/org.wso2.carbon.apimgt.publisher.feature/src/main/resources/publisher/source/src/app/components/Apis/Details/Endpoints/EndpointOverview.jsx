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
    Collapse,
    RadioGroup, Checkbox, Dialog, DialogTitle, DialogContent,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import LaunchIcon from '@material-ui/icons/Launch';
import { Link } from 'react-router-dom';

import cloneDeep from 'lodash.clonedeep';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import InlineEndpoints from 'AppComponents/Apis/Details/Endpoints/Prototype/InlineEndpoints';
import {
    getEndpointTypeProperty,
    createEndpointConfig,
    getEndpointTemplate,
} from './endpointUtils';
import GeneralConfiguration from './GeneralConfiguration';
import LoadbalanceFailoverConfig from './LoadbalanceFailoverConfig';
import GenericEndpoint from './GenericEndpoint';
import AdvanceEndpointConfig from './AdvancedConfig/AdvanceEndpointConfig';
import Credentials from './AWSLambda/Credentials.jsx';
import Mappings from './AWSLambda/Mappings.jsx';

const styles = theme => ({
    overviewWrapper: {
        marginTop: theme.spacing(2),
    },
    listing: {
        margin: theme.spacing(),
        padding: theme.spacing(),
    },
    endpointContainer: {
        paddingLeft: theme.spacing(2),
        padding: theme.spacing(),
        marginTop: theme.spacing(),
    },
    endpointName: {
        paddingLeft: theme.spacing(),
        fontSize: '1rem',
        paddingTop: theme.spacing(),
        paddingBottom: theme.spacing(),
    },
    endpointTypesWrapper: {
        padding: theme.spacing(3),
        marginTop: theme.spacing(2),
    },
    sandboxHeading: {
        display: 'flex',
        alignItems: 'center',
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        paddingTop: theme.spacing(),
    },
    endpointsWrapperLeft: {
        padding: theme.spacing(),
        borderRight: '#c4c4c4',
        borderRightStyle: 'solid',
        borderRightWidth: 'thin',
    },
    endpointsWrapperRight: {
        padding: theme.spacing(),
    },
    endpointsTypeSelectWrapper: {
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2),
        padding: theme.spacing(),
        display: 'flex',
        justifyContent: 'space-between',
    },
    endpointTypesSelectWrapper: {
        display: 'flex',
    },
    defaultEndpointWrapper: {
        paddingLeft: theme.spacing(),
        paddingRight: theme.spacing(),
        marginRight: theme.spacing(),
    },
    configDialogHeader: {
        fontWeight: '600',
    },
    addLabel: {
        padding: theme.spacing(2),
    },
});

const endpointTypes = [
    { key: 'http', value: 'HTTP/REST Endpoint' },
    { key: 'address', value: 'HTTP/SOAP Endpoint' },
    { key: 'default', value: 'Dynamic Endpoints' },
    { key: 'prototyped', value: 'Prototyped' },
    { key: 'INLINE', value: 'Mocked' },
    { key: 'awslambda', value: 'AWS Lambda' },
];

/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const {
        classes,
        api,
        endpointsDispatcher,
        swaggerDef,
        updateSwagger,
    } = props;
    const { endpointConfig, endpointSecurity } = api;
    const [endpointType, setEndpointType] = useState(endpointTypes[0]);
    const [epConfig, setEpConfig] = useState(endpointConfig);
    const [endpointSecurityInfo, setEndpointSecurityInfo] = useState(null);
    const [advanceConfigOptions, setAdvancedConfigOptions] = useState({
        open: false,
        index: 0,
        type: '',
        category: '',
        config: undefined,
    });
    const [endpointCategory, setEndpointCategory] = useState({ sandbox: false, prod: false });

    /**
     * Method to get the type of the endpoint. (HTTP/REST or HTTP/SOAP)
     * In failover/ loadbalance cases, the endpoint type is presented in the endpoints list. Therefore that property
     * needs to be extracted separately.
     *
     * @param {Object} apiObject  The representative type of the endpoint.
     * @return {string} The type of the endpoint.
     * */
    const getEndpointType = (apiObject) => {
        const type = apiObject.endpointConfig && apiObject.endpointConfig.endpoint_type;
        if (apiObject.endpointImplementationType === 'INLINE') {
            return endpointTypes[4];
        } else if (apiObject.endpointImplementationType === 'ENDPOINT' &&
            apiObject.endpointConfig.implementation_status === 'prototyped') {
            return endpointTypes[3];
        } else if (type === 'http') {
            return endpointTypes[0];
        } else if (type === 'address') {
            return endpointTypes[1];
        } else if (type === 'default') {
            return endpointTypes[2];
        } else if (type === 'awslambda') {
            return endpointTypes[5];
        } else {
            const availableEndpoints = (endpointConfig.production_endpoints && endpointConfig.production_endpoints) ||
                (endpointConfig.sandbox_endpoints && endpointConfig.sandbox_endpoints);
            // Handle the all endpoints de-select condition... Rollback to http.
            if (!availableEndpoints) {
                return endpointTypes[0];
            }
            if (Array.isArray(availableEndpoints)) {
                return availableEndpoints[0].endpoint_type !== undefined ? endpointTypes[1] : endpointTypes[0];
            }
            return availableEndpoints.endpoint_type !== undefined ? endpointTypes[1] : endpointTypes[0];
        }
    };

    useEffect(() => {
        const epType = getEndpointType(api);
        if (epType.key !== 'INLINE') {
            setEndpointCategory({
                prod: !!endpointConfig.production_endpoints,
                sandbox: !!endpointConfig.sandbox_endpoints,
            });
        }
        setEpConfig(endpointConfig);
        setEndpointType(epType);
        setEndpointSecurityInfo(endpointSecurity);
    }, [props]);

    const getEndpoints = (type) => {
        if (epConfig[type]) {
            return epConfig[type].length > 0 ?
                epConfig[type][0].url : epConfig[type].url;
        }
        return '';
    };

    const handleOnChangeEndpointCategoryChange = (category) => {
        let endpointConfigCopy = cloneDeep(endpointConfig);
        if (category === 'prod') {
            const endpointProp = 'production_endpoints';
            if (endpointCategory[category]) {
                delete endpointConfigCopy[endpointProp];
                if (endpointConfigCopy.endpointType === 'failover') {
                    delete endpointConfigCopy.production_failovers;
                }
            } else if (endpointConfigCopy.endpointType === 'load_balance') {
                endpointConfigCopy[endpointProp] = [getEndpointTemplate(endpointType.key)];
            } else if (endpointConfigCopy.endpointType === 'failover') {
                endpointConfigCopy[endpointProp] = getEndpointTemplate(endpointType.key);
                endpointConfigCopy.production_failovers = [];
            } else {
                endpointConfigCopy[endpointProp] = getEndpointTemplate(endpointType.key);
            }
        } else {
            const endpointProp = 'sandbox_endpoints';
            if (endpointCategory[category]) {
                delete endpointConfigCopy[endpointProp];
                if (endpointConfigCopy.endpointType === 'failover') {
                    delete endpointConfigCopy.sandbox_failovers;
                }
            } else if (endpointConfigCopy.endpointType === 'load_balance') {
                endpointConfigCopy[endpointProp] = [getEndpointTemplate(endpointType.key)];
            } else if (endpointConfigCopy.endpointType === 'failover') {
                endpointConfigCopy[endpointProp] = getEndpointTemplate(endpointType.key);
                endpointConfigCopy.sandbox_failovers = [];
            } else {
                endpointConfigCopy[endpointProp] = getEndpointTemplate(endpointType.key);
            }
        }
        // Check whether, config has either prod/ sandbox endpoints. If not, reSet the endpoint type to http
        if (!endpointConfigCopy.production_endpoints && !endpointConfigCopy.sandbox_endpoints) {
            endpointConfigCopy = createEndpointConfig('http');
        }
        endpointsDispatcher({ action: 'select_endpoint_category', value: endpointConfigCopy });
    };

    /**
     * Method to modify the endpoint represented by the given parameters.
     *
     * If url is null, remove the endpoint from the endpoint config.
     *
     * @param {number} index The index of the endpoint in the listing.
     * @param {string} category The endpoint category. (production/ sand box)
     * @param {string} url The new endpoint url.
     * */
    const editEndpoint = (index, category, url) => {
        let modifiedEndpoint = null;
        // Make a copy of the endpoint config.
        const endpointConfigCopy = cloneDeep(epConfig);
        /*
        * If the index > 0, it means that the endpoint is load balance or fail over.
        * Otherwise it is the default endpoint. (index = 0)
        * */
        if (index > 0) {
            const endpointTypeProperty = getEndpointTypeProperty(endpointConfigCopy.endpoint_type, category);
            modifiedEndpoint = endpointConfigCopy[endpointTypeProperty];
            /*
            * In failover case, the failover endpoints are a separate object. But in endpoint listing, since we
            *  consider all the endpoints as a single list, to get the real index of the failover endpoint we use
            *  index - 1.
            * */
            if (endpointConfigCopy.endpoint_type === 'failover') {
                modifiedEndpoint[index - 1].url = url.trim();
            } else {
                modifiedEndpoint[index].url = url.trim();
            }
            endpointConfigCopy[endpointTypeProperty] = modifiedEndpoint;
        } else if (url !== '') {
            modifiedEndpoint = endpointConfigCopy[category];

            /*
            * In this case, we are editing the default endpoint.
            * If the endpoint type is load balance, the production_endpoints or the sandbox_endpoint object is an
            *  array. Otherwise, in failover mode, the default endpoint is an object.
            *
            * So, we check whether the endpoints is an array or an object.
            *
            * If This is the first time a user creating an endpoint endpoint config object does not have
            *  production_endpoints or sandbox_endpoints object.
            * Therefore create new object and add to the endpoint config.
            * */
            if (!modifiedEndpoint) {
                modifiedEndpoint = getEndpointTemplate(endpointConfigCopy.endpoint_type);
                modifiedEndpoint.url = url.trim();
            } else if (Array.isArray(modifiedEndpoint)) {
                if (url === '') {
                    modifiedEndpoint.splice(0, 1);
                } else {
                    modifiedEndpoint[0].url = url.trim();
                }
            } else {
                modifiedEndpoint.url = url.trim();
            }
            endpointConfigCopy[category] = modifiedEndpoint;
        } else {
            /*
            * If the url is empty, delete the respective endpoint object.
            * */
            delete endpointConfigCopy[category];
        }
        endpointsDispatcher({ action: category, value: modifiedEndpoint });
    };

    /**
     * Handles the endpoint type select event.
     * @param {any} event The select event.
     * */
    const handleEndpointTypeSelect = (event) => {
        const selectedKey = event.target.value;
        if (selectedKey === 'INLINE') {
            const tmpConfig = createEndpointConfig('prototyped');
            endpointsDispatcher({
                action: 'set_inline',
                value: {
                    endpointConfig: tmpConfig,
                    endpointImplementationType: 'INLINE',
                },
            });
        } else if (selectedKey === 'prototyped') {
            const tmpConfig = createEndpointConfig(selectedKey);
            endpointsDispatcher({
                action: 'set_prototyped',
                value: {
                    endpointImplementationType: 'ENDPOINT',
                    endpointConfig: tmpConfig,
                },
            });
        } else if (selectedKey === 'awslambda') {
            const generatedEndpointConfig = createEndpointConfig(selectedKey);
            endpointsDispatcher({
                action: 'select_endpoint_type',
                value: {
                    endpointImplementationType: 'ENDPOINT',
                    endpointConfig: { ...generatedEndpointConfig },
                },
            });
        } else {
            const generatedEndpointConfig = createEndpointConfig(selectedKey);
            endpointsDispatcher({
                action: 'select_endpoint_type',
                value: {
                    endpointImplementationType: 'ENDPOINT',
                    endpointConfig: { ...generatedEndpointConfig },
                },
            });
        }
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
     * Method to handle the endpoint security changes.
     * @param {any} event The html event
     * @param {string} field The security propety that is being modified.
     * */
    const handleEndpointSecurityChange = (event, field) => {
        endpointsDispatcher({
            action: 'endpointSecurity',
            value: { ...endpointSecurityInfo, [field]: event.target.value },
        });
    };

    /**
     * Method to save the advance configurations.
     *
     * @param {object} advanceConfig The advance configuration object.
     * */
    const saveAdvanceConfig = (advanceConfig) => {
        const config = cloneDeep(epConfig);
        const endpointConfigProperty =
            getEndpointTypeProperty(advanceConfigOptions.type, advanceConfigOptions.category);
        const selectedEndpoints = config[endpointConfigProperty];
        if (Array.isArray(selectedEndpoints)) {
            if (advanceConfigOptions.type === 'failover') {
                selectedEndpoints[advanceConfigOptions.index - 1].config = advanceConfig;
            } else {
                selectedEndpoints[advanceConfigOptions.index].config = advanceConfig;
            }
        } else {
            selectedEndpoints.config = advanceConfig;
        }
        setAdvancedConfigOptions({ open: false });
        endpointsDispatcher({
            action: 'set_advance_config',
            value: { ...config, [endpointConfigProperty]: selectedEndpoints },
        });
    };

    /**
     * Method to close the advance configuration dialog box.
     * */
    const closeAdvanceConfig = () => {
        setAdvancedConfigOptions({ open: false });
    };

    /**
     * Method to update the resource paths object in the swagger.
     * @param {any} paths The updated paths object.
     * */
    const updatePaths = (paths) => {
        updateSwagger({ ...swaggerDef, paths });
    };

    return (
        <div className={classes.overviewWrapper}>
            {api.type === 'WS' ?
                <React.Fragment>
                    <Typography>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.EndpointOverview.websoket.endpoint'
                            defaultMessage='Websocket Endpoint'
                        />
                    </Typography>
                    <GenericEndpoint
                        autoFocus
                        name='Websocket Endpoint'
                        className={classes.defaultEndpointWrapper}
                        endpointURL={getEndpoints('production_endpoints')}
                        type=''
                        index={0}
                        category='production_endpoints'
                        editEndpoint={editEndpoint}
                        setAdvancedConfigOpen={toggleAdvanceConfig}
                    />
                </React.Fragment>
                :
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <FormControl component='fieldset' className={classes.formControl}>
                            <RadioGroup
                                aria-label='EndpointType'
                                name='endpointType'
                                className={classes.radioGroup}
                                value={endpointType.key}
                                onChange={handleEndpointTypeSelect}
                            >
                                {endpointTypes.map((endpoint) => {
                                    if (api.lifeCycleStatus === 'CREATED') {
                                        return (
                                            <FormControlLabel
                                                value={endpoint.key}
                                                control={
                                                    <Radio
                                                        disabled={(isRestricted(['apim:api_create'], api))}
                                                        color='primary'
                                                    />
                                                }
                                                label={endpoint.value}
                                            />);
                                    } else if (api.lifeCycleStatus === 'PROTOTYPED') {
                                        if (endpoint.key === 'prototyped' || endpoint.key === 'INLINE') {
                                            return (
                                                <FormControlLabel
                                                    value={endpoint.key}
                                                    control={
                                                        <Radio
                                                            disabled={(isRestricted(['apim:api_create'], api))}
                                                            color='primary'
                                                        />
                                                    }
                                                    label={endpoint.value}
                                                />);
                                        }
                                    } else if (endpoint.key !== 'prototyped' && endpoint.key !== 'INLINE') {
                                        return (
                                            <FormControlLabel
                                                value={endpoint.key}
                                                control={
                                                    <Radio
                                                        disabled={(isRestricted(['apim:api_create'], api))}
                                                        color='primary'
                                                    />
                                                }
                                                label={endpoint.value}
                                            />);
                                    }
                                    return <div />;
                                })}
                            </RadioGroup>
                        </FormControl>
                    </Grid>
                    <Grid item xs={12}>
                        {endpointType.key === 'INLINE' ?
                            <InlineEndpoints paths={swaggerDef.paths} updatePaths={updatePaths} /> :
                            <Paper className={classes.endpointContainer}>
                                {endpointType.key === 'awslambda' ?
                                    <div>
                                        <Typography>
                                            <FormattedMessage
                                                id={'Apis.Details.Endpoints.EndpointOverview.awslambda' +
                                                '.endpoint.credentials'}
                                                defaultMessage='AWS Credentials'
                                            />
                                        </Typography>
                                        <Credentials
                                            saveAPI={getEndpoints}
                                            epConfig={epConfig}
                                            setEpConfig={setEpConfig}
                                        />
                                        <Typography>
                                            <FormattedMessage
                                                id={'Apis.Details.Endpoints.EndpointOverview.awslambda' +
                                                '.endpoint.mappings'}
                                                defaultMessage='Resources Mapping'
                                            />
                                        </Typography>
                                        <Mappings api={api} />
                                    </div> :
                                    <div>
                                        {endpointType.key === 'default' ?
                                            <InlineMessage>
                                                <div className={classes.contentWrapper}>
                                                    <Typography component='p' className={classes.content}>
                                                        <FormattedMessage
                                                            id={'Apis.Details.Endpoints.EndpointOverview.upload' +
                                                            '.mediation.message'}
                                                            defaultMessage={'Please upload a mediation sequence file ' +
                                                            'to Message Mediation Policies, which sets the endpoints.'}
                                                        />
                                                        <Link to={'/apis/' + api.id + '/runtime-configuration'}>
                                                            <LaunchIcon
                                                                style={{ marginLeft: '2px' }}
                                                                fontSize='small'
                                                                color='primary'
                                                            />
                                                        </Link>
                                                    </Typography>
                                                </div>
                                            </InlineMessage> :
                                            <React.Fragment>
                                                {endpointType.key === 'prototyped' ?
                                                    <Typography>
                                                        <FormattedMessage
                                                            id={'Apis.Details.Endpoints.EndpointOverview.prototype' +
                                                            '.endpoint.label'}
                                                            defaultMessage='Prototype Endpoint'
                                                        />
                                                    </Typography> :
                                                    <FormControlLabel
                                                        control={
                                                            <Checkbox
                                                                checked={endpointCategory.prod}
                                                                value='prod'
                                                                color='primary'
                                                                onChange={event =>
                                                                    handleOnChangeEndpointCategoryChange('prod', event)}
                                                            />
                                                        }
                                                        label={
                                                            <Typography>
                                                                <FormattedMessage
                                                                    id={'Apis.Details.Endpoints.EndpointOverview' +
                                                                    '.production.endpoint.label'}
                                                                    defaultMessage='Production Endpoint'
                                                                />
                                                            </Typography>}
                                                    />
                                                }
                                                <Collapse in={endpointCategory.prod && endpointType.key !== 'default'}>
                                                    <GenericEndpoint
                                                        autoFocus
                                                        name={endpointType.key === 'prototyped' ?
                                                            <FormattedMessage
                                                                id={'Apis.Details.Endpoints.EndpointOverview' +
                                                                '.prototype.endpoint.header'}
                                                                defaultMessage='Prototype Endpoint'
                                                            /> : <FormattedMessage
                                                                id={'Apis.Details.Endpoints.EndpointOverview' +
                                                                '.production.endpoint.header'}
                                                                defaultMessage='Production Endpoint'
                                                            />}
                                                        className={classes.defaultEndpointWrapper}
                                                        endpointURL={getEndpoints('production_endpoints')}
                                                        type=''
                                                        index={0}
                                                        category='production_endpoints'
                                                        editEndpoint={editEndpoint}
                                                        setAdvancedConfigOpen={toggleAdvanceConfig}
                                                    />
                                                </Collapse>
                                            </React.Fragment>
                                        }
                                        {endpointType.key === 'prototyped' || endpointType.key === 'default' ?
                                            <div /> :
                                            <React.Fragment>
                                                <FormControlLabel
                                                    control={
                                                        <Checkbox
                                                            disabled={endpointType.key === 'default'}
                                                            checked={endpointCategory.sandbox}
                                                            value='sandbox'
                                                            color='primary'
                                                            onChange={event =>
                                                                handleOnChangeEndpointCategoryChange('sandbox', event)}
                                                        />
                                                    }
                                                    label={
                                                        <FormattedMessage
                                                            id={'Apis.Details.Endpoints.EndpointOverview.sandbox' +
                                                            '.endpoint'}
                                                            defaultMessage='Sandbox Endpoint'
                                                        />
                                                    }
                                                />
                                                <Collapse
                                                    in={
                                                        endpointCategory.sandbox &&
                                                        endpointType.key !== 'default'
                                                    }
                                                >
                                                    <GenericEndpoint
                                                        autoFocus
                                                        name='Sandbox Endpoint'
                                                        className={classes.defaultEndpointWrapper}
                                                        endpointURL={getEndpoints('sandbox_endpoints')}
                                                        type=''
                                                        index={0}
                                                        category='sandbox_endpoints'
                                                        editEndpoint={editEndpoint}
                                                        setAdvancedConfigOpen={toggleAdvanceConfig}
                                                    />
                                                </Collapse>
                                            </React.Fragment>
                                        }
                                    </div>
                                }
                            </Paper>
                        }
                    </Grid>
                    {
                        endpointType.key === 'INLINE' ||
                        endpointType.key === 'prototyped' ||
                        endpointType.key === 'awslambda' ?
                            <div /> :
                            <Grid item xs={12}>
                                <Typography variant='h4' align='left' className={classes.titleWrapper} gutterBottom>
                                    <FormattedMessage
                                        id='Apis.Details.Endpoints.EndpointOverview.general.config.header'
                                        defaultMessage='General Endpoint Configurations'
                                    />
                                </Typography>
                                <GeneralConfiguration
                                    epConfig={(cloneDeep(epConfig))}
                                    endpointSecurityInfo={endpointSecurityInfo}
                                    handleToggleEndpointSecurity={handleToggleEndpointSecurity}
                                    handleEndpointSecurityChange={handleEndpointSecurityChange}
                                    endpointType={endpointType}
                                    apiType={api.type}
                                />
                            </Grid>
                    }
                    {
                        endpointType.key === 'INLINE' ||
                        endpointType.key === 'default' ||
                        endpointType.key === 'prototyped' ||
                        endpointType.key === 'awslambda' ?
                            <div />
                            :
                            <Grid item xs={12}>
                                <Typography variant='h4' align='left' className={classes.titleWrapper} gutterBottom>
                                    <FormattedMessage
                                        id='Apis.Details.Endpoints.EndpointOverview.lb.failover.endpoints.header'
                                        defaultMessage='Load balance and Failover Configurations'
                                    />
                                </Typography>
                                <LoadbalanceFailoverConfig
                                    toggleAdvanceConfig={toggleAdvanceConfig}
                                    endpointsDispatcher={endpointsDispatcher}
                                    epConfig={(cloneDeep(epConfig))}
                                    endpointSecurityInfo={endpointSecurityInfo}
                                    handleEndpointTypeSelect={handleEndpointTypeSelect}
                                    globalEpType={endpointType}
                                    apiType={api.type}
                                />
                            </Grid>
                    }
                </Grid>
            }
            <Dialog open={advanceConfigOptions.open}>
                <DialogTitle>
                    <Typography className={classes.configDialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.Endpoints.EndpointOverview.advance.endpoint.configuration'
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
    endpointsDispatcher: PropTypes.func.isRequired,
    swaggerDef: PropTypes.shape({}).isRequired,
    updateSwagger: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointOverview));
