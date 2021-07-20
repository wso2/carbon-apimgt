/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { isRestricted } from 'AppData/AuthManager';
import LifeCycleIcon from '@material-ui/icons/Autorenew';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import PersonPinCircleOutlinedIcon from '@material-ui/icons/PersonPinCircleOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import ScopesIcon from '@material-ui/icons/VpnKey';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
import BusinessIcon from '@material-ui/icons/Business';
import CodeIcon from '@material-ui/icons/Code';
import ConfigurationIcon from '@material-ui/icons/Build';
import RuntimeConfigurationIcon from '@material-ui/icons/Settings';
import PropertiesIcon from '@material-ui/icons/List';
import SubscriptionsIcon from '@material-ui/icons/RssFeed';
import MonetizationIcon from '@material-ui/icons/LocalAtm';
import StoreIcon from '@material-ui/icons/Store';
import DashboardIcon from '@material-ui/icons/Dashboard';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl, defineMessages } from 'react-intl';
import {
    Redirect, Route, Switch, Link, matchPath,
} from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import Utils from 'AppData/Utils';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import AppContext from 'AppComponents/Shared/AppContext';
import LastUpdatedTime from 'AppComponents/Apis/Details/components/LastUpdatedTime';
import Overview from './NewOverview/Overview';
import DesignConfigurations from './Configuration/DesignConfigurations';
import RuntimeConfiguration from './Configuration/RuntimeConfiguration';
import RuntimeConfigurationWebSocket from './Configuration/RuntimeConfigurationWebSocket';
import LifeCycle from './LifeCycle/LifeCycle';
import Documents from './Documents';
import Operations from './Operations/Operations';
import APIOperations from './Resources/APIOperations';
import APIProductOperations from './ProductResources/APIProductOperations';
import ProductResourcesEdit from './ProductResources/ProductResourcesEdit';
import Endpoints from './Endpoints/Endpoints';
import Environments from './Environments/Environments';
import Subscriptions from './Subscriptions/Subscriptions';
import Scope from './Scopes';
import Security from './Security';
import APIDefinition from './APIDefinition/APIDefinition';
import WSDL from './APIDefinition/WSDL';
import APIDetailsTopMenu from './components/APIDetailsTopMenu';
import BusinessInformation from './BusinessInformation/BusinessInformation';
import Properties from './Properties/Properties';
import Monetization from './Monetization';
import ExternalStores from './ExternalStores/ExternalStores';
import { APIProvider } from './components/ApiContext';
import CreateNewVersion from './NewVersion/NewVersion';
import TestConsole from './TestConsole/TestConsole';

const styles = (theme) => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
        overflowY: 'auto',
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing(1),
        paddingTop: theme.spacing(1),
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        color: theme.palette.getContrastText(theme.palette.background.leftMenuActive),
        textDecoration: 'none',
    },
    detailsContent: {
        display: 'flex',
        flex: 1,
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom: theme.spacing(3),
        overflow: 'auto',
    },
    contentInside: {
        width: 'calc(100% - 56px)',
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(3),
        paddingTop: theme.spacing(2),
    },
});

/**
 * Base component for API specific Details page,
 * What this component do is, Handle all the request coming under `/apis/:api_uuid` path, If the :api_uuid or
 *  the later part of the URL is not valid , This will return a `PageNotFound` component.
 * For valid API request , This component will fetch the API and pass the API response data to below components in `api`
 * prop name.
 * Note: If you want to add new route or new page under APIs detail, add the desired path to `PATHS` constant mapping.
 * This mapping will be used in parent component to directly return `PageNotFound` component, If user making a request
 * to an undefined path segment.
 */
class Details extends Component {
    /**
     * Return boolean , whether provided URL has a valid Route under the Details page.
     * Check https://github.com/ReactTraining/react-router/blob/master/packages/react-router-dom/modules/NavLink.js
     * code for the usage of public matchPath method
     * @static
     * @param {String} pathname location URL of an incoming request
     * @memberof Details
     * @returns {Boolean} whether URL matched with defined sub paths or not
     */
    static isValidURL(pathname) {
        for (const [subPathKey, subPath] of Object.entries(Details.subPaths)) {
            // Skip the BASE path , because it will match for any `/apis/:apiUUID/*` values
            if ((subPathKey !== 'BASE') && (subPathKey !== 'BASE_PRODUCT')) {
                const matched = matchPath(pathname, subPath);
                if (matched) {
                    return matched;
                }
            }
        }
        return false;
    }

    /**
     * Creates an instance of Details.
     * @param {any} props @inheritDoc
     * @memberof Details
     */
    constructor(props) {
        super(props);
        const isAPIProduct = null;
        this.state = {
            api: null,
            apiNotFound: false,
            // updateAPI: this.updateAPI,
            isAPIProduct,
            imageUpdate: 0,
        };
        this.setAPI = this.setAPI.bind(this);
        this.setAPIProduct = this.setAPIProduct.bind(this);
        this.updateAPI = this.updateAPI.bind(this);
        this.setImageUpdate = this.setImageUpdate.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Details
     */
    componentDidMount() {
        const {
            location: { pathname },
            isAPIProduct,
        } = this.props;
        // Load API data iff request page is valid
        if (Details.isValidURL(pathname)) {
            if (isAPIProduct) {
                this.setAPIProduct();
            } else {
                this.setAPI();
            }
            const api = new API();
            api.getTenantsByState('active')
                .then((response) => {
                    const { list } = response.body;
                    this.setState({ tenantList: list });
                }).catch((error) => {
                    console.error('error when getting tenants ' + error);
                });
        }
    }

    /**
     *
     *
     * @returns
     * @memberof Details
     */
    componentDidUpdate() {
        const { api } = this.state;
        const { match, isAPIProduct } = this.props;
        const { apiUUID } = match.params;
        const { apiProdUUID } = match.params;
        if (!api || (api.id === apiUUID || api.id === apiProdUUID)) {
            return;
        }
        if (isAPIProduct) {
            this.setAPIProduct();
        } else {
            this.setAPI();
        }
    }

    /**
     *
     * This method is a hack to update the image in the toolbar when a new image is uploaded
     * @memberof Details
     */
    setImageUpdate() {
        this.setState((previousState) => ({
            imageUpdate: previousState.imageUpdate + 1,
        }));
    }

    /**
     *
     *
     * @memberof Details
     */
    setAPI(newAPI) {
        if (newAPI) {
            this.setState({ api: newAPI });
        } else {
            const { match } = this.props;
            const { apiUUID } = match.params;
            const promisedApi = API.get(apiUUID);
            promisedApi
                .then((api) => {
                    this.setState({ api });
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        this.setState({ apiNotFound: true });
                    } else if (status === 401) {
                        doRedirectToLogin();
                    }
                });
        }
    }

    /**
     *
     *
     * @memberof Details
     */
    setAPIProduct() {
        const { match } = this.props;
        const { apiProdUUID } = match.params;
        const { isAPIProduct } = this.props;
        const promisedApi = APIProduct.get(apiProdUUID);
        promisedApi
            .then((api) => {
                this.setState({ isAPIProduct });
                this.setState({ api });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    }

    /**
     *
     * @returns
     * @memberof Details
     */
    getLeftMenuItemForDefinitionByType(apiType) {
        const { isAPIProduct } = this.state;
        const { intl, match } = this.props;
        const uuid = match.params.apiUUID || match.params.api_uuid || match.params.apiProdUUID;
        const pathPrefix = '/' + (isAPIProduct ? 'api-products' : 'apis') + '/' + uuid + '/';
        const apiDefinitionMenuItem = (
            <LeftMenuItem
                text={intl.formatMessage({
                    id: 'Apis.Details.index.api.definition2',
                    defaultMessage: 'API definition',
                })}
                route='api definition'
                to={pathPrefix + 'api definition'}
                Icon={<CodeIcon />}
            />
        );

        switch (apiType) {
            case 'GRAPHQL':
                return (
                    <>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.schema.definition',
                                defaultMessage: 'Schema Definition',
                            })}
                            route='schema definition'
                            to={pathPrefix + 'schema definition'}
                            Icon={<CodeIcon />}
                        />
                    </>
                );
            case 'WS':
                return '';
            case 'SOAP':
                return (
                    <>
                        {apiDefinitionMenuItem}
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.wsdl.definition',
                                defaultMessage: 'WSDL Definition',
                            })}
                            route='wsdl'
                            to={pathPrefix + 'wsdl'}
                            Icon={<CodeIcon />}
                        />
                    </>
                );
            default:
                return (
                    <>
                        {apiDefinitionMenuItem}
                    </>
                );
        }
    }

    getLeftMenuItemForResourcesByType(apiType) {
        const { isAPIProduct } = this.state;
        const { intl, match } = this.props;
        const uuid = match.params.apiUUID || match.params.api_uuid || match.params.apiProdUUID;
        const pathPrefix = '/' + (isAPIProduct ? 'api-products' : 'apis') + '/' + uuid + '/';

        switch (apiType) {
            case 'GRAPHQL':
                return (
                    <>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.operations',
                                defaultMessage: 'operations',
                            })}
                            to={pathPrefix + 'operations'}
                            Icon={<ResourcesIcon />}
                        />
                    </>
                );
            case 'WS':
                return '';
            default:
                return (
                    <>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.resources',
                                defaultMessage: 'resources',
                            })}
                            to={pathPrefix + 'resources'}
                            Icon={<ResourcesIcon />}
                        />
                    </>
                );
        }
    }

    /**
     * This method is similar to ReactJS `setState` method, In this `updateAPI()` method, we accept partially updated
     * API object or comple API object. When updating , the provided updatedAPI object will be merged with the existing
     * API object in the state and use it as the payload in the /apis PUT operation.
     *
     * Partially updated API object means: {description: "Here is my new description.."} kind of object. It should have
     * a key in API object and value contains the updated value of that property
     * @param {Object} [_updatedProperties={}] Partially updated API object or complete API object
     * (even an instance of API class is accepted here)
     * @param {Boolean} isAPIProduct Whether the update operation should execute on an API or API Product
     * @returns {Promise} promise object that resolve to update (/apis PUT operation) response
     */
    updateAPI(_updatedProperties = {}) {
        const { api } = this.state;
        let isAPIProduct = false;
        if (api.apiType === 'APIProduct') {
            isAPIProduct = true;
        }
        const updatedProperties = _updatedProperties instanceof API ? _updatedProperties.toJson() : _updatedProperties;
        let promisedUpdate;
        // TODO: Ideally, The state should hold the corresponding API object
        // which we could call it's `update` method safely ~tmkb
        if (!isEmpty(updatedProperties)) {
            // newApi object has to be provided as the updatedProperties. Then api will be updated.
            promisedUpdate = api.update(updatedProperties);
        } else {
            // Just like calling noArg `setState()` will just trigger a re-render without modifying the state,
            // Calling `updateAPI()` without args wil return the API without any update.
            // Just sync-up the api state with backend
            promisedUpdate = API.get(api.id);
        }
        return promisedUpdate
            .then((updatedAPI) => {
                if (isAPIProduct) {
                    Alert.info(`${updatedAPI.name} API updated successfully`);
                    this.setState({ api: updatedAPI });
                    return updatedAPI;
                } else {
                    Alert.info(`${updatedAPI.name} API updated successfully`);
                    this.setState({ api: updatedAPI });
                    return updatedAPI;
                }
            })
            .catch((error) => {
                // TODO: Should log and handle the error case by the original callee ~tmkb
                console.error(error);
                Alert.error(`Something went wrong while updating the ${api.name} API!!`);
                // Kinda force render,Resting API object to old one
                this.setState({ api });
                throw error;
            });
    }

    /**
     * Renders Grid container layout with NavBar place static in LHS, Components which coming as children for
     * Details page
     * should wrap it's content with <Grid item > element
     * @returns {Component} Render API Details page
     */
    render() {
        const {
            api, apiNotFound, isAPIProduct, imageUpdate, tenantList,
        } = this.state;
        const {
            classes,
            theme,
            match,
            intl,
            location: pageLocation,
            location: { pathname }, // nested destructuring
        } = this.props;

        const { settings: settingsContext } = this.context;

        // pageLocation renaming is to prevent es-lint errors saying can't use global name location
        if (!Details.isValidURL(pathname)) {
            return <ResourceNotFound location={pageLocation} />;
        }
        const uuid = match.params.apiUUID || match.params.api_uuid || match.params.apiProdUUID;
        const pathPrefix = '/' + (isAPIProduct ? 'api-products' : 'apis') + '/' + uuid + '/';
        const redirectUrl = pathPrefix;
        if (apiNotFound) {
            const { apiUUID } = match.params;
            const resourceNotFoundMessageText = defineMessages({
                titleMessage: {
                    id: 'Apis.Details.index.api.not.found.title',
                    defaultMessage: 'API is not found in the {environmentLabel} Environment',
                },
                bodyMessage: {
                    id: 'Apis.Details.index.api.not.found.body',
                    defaultMessage: 'Cannot find the API with the given id',
                },
            });
            const resourceNotFountMessage = {
                title: intl.formatMessage(resourceNotFoundMessageText.titleMessage, {
                    environmentLabel: `${Utils.getCurrentEnvironment().label}`,
                }),
                body: intl.formatMessage(resourceNotFoundMessageText.bodyMessage, { apiUUID: `${apiUUID}` }),
            };
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!api) {
            return <Progress />;
        }
        const { leftMenuIconMainSize } = theme.custom;

        return (
            <>
                <APIProvider
                    value={{
                        api,
                        updateAPI: this.updateAPI,
                        isAPIProduct,
                        setAPI: this.setAPI,
                        setImageUpdate: this.setImageUpdate,
                        imageUpdate,
                        tenantList,
                    }}
                >
                    <div className={classes.LeftMenu}>
                        <Link to={'/' + (isAPIProduct ? 'api-products' : 'apis') + '/'}>
                            <div className={classes.leftLInkMain}>
                                <CustomIcon
                                    width={leftMenuIconMainSize}
                                    height={leftMenuIconMainSize}
                                    icon={isAPIProduct ? 'api-product' : 'api'}
                                />
                            </div>
                        </Link>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.overview',
                                defaultMessage: 'overview',
                            })}
                            to={pathPrefix + 'overview'}
                            Icon={<DashboardIcon />}
                        />
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.design.configs',
                                defaultMessage: 'Design Configurations',
                            })}
                            route='configuration'
                            to={pathPrefix + 'configuration'}
                            Icon={<ConfigurationIcon />}
                        />
                        {!api.isWebSocket() && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.runtime.configs',
                                    defaultMessage: 'Runtime Configurations',
                                })}
                                route='runtime-configuration'
                                to={pathPrefix + 'runtime-configuration'}
                                Icon={<RuntimeConfigurationIcon />}
                            />
                        )}
                        {api.isWebSocket() && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.runtime.configs',
                                    defaultMessage: 'Runtime Configurations',
                                })}
                                route='runtime-configuration'
                                to={pathPrefix + 'runtime-configuration-websocket'}
                                Icon={<RuntimeConfigurationIcon />}
                            />
                        )}
                        {this.getLeftMenuItemForResourcesByType(api.type)}
                        {!isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.endpoints',
                                    defaultMessage: 'endpoints',
                                })}
                                to={pathPrefix + 'endpoints'}
                                Icon={<EndpointIcon />}
                            />
                        )}
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.subscriptions',
                                defaultMessage: 'subscriptions',
                            })}
                            to={pathPrefix + 'subscriptions'}
                            Icon={<SubscriptionsIcon />}
                        />
                        {!isAPIProduct && !isRestricted(['apim:api_publish'], api) && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.lifecycle',
                                    defaultMessage: 'lifecycle',
                                })}
                                to={pathPrefix + 'lifecycle'}
                                Icon={<LifeCycleIcon />}
                            />
                        )}
                        {this.getLeftMenuItemForDefinitionByType(api.type)}
                        {!isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.environments',
                                    defaultMessage: 'environments',
                                })}
                                route='environments'
                                to={pathPrefix + 'environments'}
                                Icon={<PersonPinCircleOutlinedIcon />}
                            />
                        )}
                        {!api.isWebSocket() && !isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.left.menu.scope',
                                    defaultMessage: 'Local Scopes',
                                })}
                                to={pathPrefix + 'scopes'}
                                Icon={<ScopesIcon />}
                            />
                        )}
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.business.info',
                                defaultMessage: 'business info',
                            })}
                            to={pathPrefix + 'business info'}
                            Icon={<BusinessIcon />}
                        />
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.properties',
                                defaultMessage: 'properties',
                            })}
                            to={pathPrefix + 'properties'}
                            Icon={<PropertiesIcon />}
                        />
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.documents',
                                defaultMessage: 'documents',
                            })}
                            to={pathPrefix + 'documents'}
                            Icon={<DocumentsIcon />}
                        />
                        {!api.isWebSocket() && !isAPIProduct && !api.isGraphql() && !isRestricted(['apim:api_publish'],
                            api) && api.lifeCycleStatus !== 'PUBLISHED' && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.Tryout',
                                    defaultMessage: 'test console',
                                })}
                                to={pathPrefix + 'test-console'}
                                iconText='test'
                            />
                        )}
                        {!api.isWebSocket() && !isRestricted(['apim:api_publish'], api) && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.monetization',
                                    defaultMessage: 'monetization',
                                })}
                                to={pathPrefix + 'monetization'}
                                Icon={<MonetizationIcon />}
                            />
                        )}
                        {!isAPIProduct && settingsContext.externalStoresEnabled && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.external-stores',
                                    defaultMessage: 'external dev portals',
                                })}
                                to={pathPrefix + 'external-devportals'}
                                Icon={<StoreIcon />}
                            />
                        )}
                    </div>
                    <div className={classes.content}>
                        <APIDetailsTopMenu api={api} isAPIProduct={isAPIProduct} imageUpdate={imageUpdate} />
                        <div className={classes.contentInside}>
                            <LastUpdatedTime lastUpdatedTime={api.lastUpdatedTime} />
                            <Switch>
                                <Redirect exact from={Details.subPaths.BASE} to={redirectUrl} />
                                <Route
                                    path={Details.subPaths.OVERVIEW_PRODUCT}
                                    key={Details.subPaths.OVERVIEW_PRODUCT}
                                    component={() => <Overview api={api} />}
                                />
                                <Route path={Details.subPaths.OVERVIEW} component={() => <Overview api={api} />} />
                                <Route
                                    path={Details.subPaths.API_DEFINITION}
                                    component={() => <APIDefinition api={api} updateAPI={this.updateAPI} />}
                                />
                                <Route
                                    path={Details.subPaths.WSDL}
                                    component={() => <WSDL api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.API_DEFINITION_PRODUCT}
                                    component={() => <APIDefinition api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.SCHEMA_DEFINITION}
                                    component={() => <APIDefinition api={api} />}
                                />
                                <Route path={Details.subPaths.LIFE_CYCLE} component={() => <LifeCycle api={api} />} />
                                <Route
                                    path={Details.subPaths.CONFIGURATION}
                                    component={() => <DesignConfigurations api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.RUNTIME_CONFIGURATION}
                                    component={() => <RuntimeConfiguration api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.RUNTIME_CONFIGURATION_WEBSOCKET}
                                    component={() => <RuntimeConfigurationWebSocket api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.CONFIGURATION_PRODUCT}
                                    component={() => <DesignConfigurations api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.RUNTIME_CONFIGURATION_PRODUCT}
                                    component={() => <RuntimeConfiguration api={api} />}
                                />
                                <Route path={Details.subPaths.ENDPOINTS} component={() => <Endpoints api={api} />} />
                                <Route
                                    path={Details.subPaths.ENVIRONMENTS}
                                    component={() => <Environments api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.OPERATIONS}
                                    component={() => <Operations api={api} updateAPI={this.updateAPI} />}
                                />
                                <Route
                                    exact
                                    path={Details.subPaths.RESOURCES_PRODUCT}
                                    component={APIProductOperations}
                                />
                                <Route
                                    path={Details.subPaths.RESOURCES_PRODUCT_EDIT}
                                    component={ProductResourcesEdit}
                                />

                                <Route
                                    path={Details.subPaths.RESOURCES}
                                    key={Details.subPaths.RESOURCES}
                                    component={APIOperations}
                                />

                                <Route path={Details.subPaths.SCOPES} component={() => <Scope api={api} />} />
                                <Route path={Details.subPaths.DOCUMENTS} component={() => <Documents api={api} />} />
                                <Route
                                    path={Details.subPaths.DOCUMENTS_PRODUCT}
                                    component={() => <Documents api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.SUBSCRIPTIONS}
                                    component={() => <Subscriptions api={api} updateAPI={this.updateAPI} />}
                                />
                                <Route
                                    path={Details.subPaths.SUBSCRIPTIONS_PRODUCT}
                                    component={() => <Subscriptions api={api} updateAPI={this.updateAPI} />}
                                />
                                <Route path={Details.subPaths.SECURITY} component={() => <Security api={api} />} />
                                <Route
                                    path={Details.subPaths.BUSINESS_INFO}
                                    component={() => <BusinessInformation api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.BUSINESS_INFO_PRODUCT}
                                    component={() => <BusinessInformation api={api} />}
                                />
                                <Route path={Details.subPaths.PROPERTIES} component={() => <Properties api={api} />} />
                                <Route
                                    path={Details.subPaths.PROPERTIES_PRODUCT}
                                    component={() => <Properties api={api} />}
                                />
                                <Route path={Details.subPaths.NEW_VERSION} component={() => <CreateNewVersion />} />
                                <Route path={Details.subPaths.SUBSCRIPTIONS} component={() => <Subscriptions />} />
                                <Route
                                    path={Details.subPaths.MONETIZATION}
                                    component={() => <Monetization api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.MONETIZATION_PRODUCT}
                                    component={() => <Monetization api={api} />}
                                />
                                <Route
                                    path={Details.subPaths.TRYOUT}
                                    component={() => <TestConsole apiObj={api} />}
                                />
                                <Route path={Details.subPaths.EXTERNAL_STORES} component={ExternalStores} />
                            </Switch>
                        </div>
                    </div>
                </APIProvider>
            </>
        );
    }
}

Details.contextType = AppContext;
// Add your path here and refer it in above <Route/> component,
// Paths that are not defined here will be returned with Not Found error
// key name doesn't matter here, Use an appropriate name as the key
Details.subPaths = {
    BASE: '/apis/:api_uuid',
    BASE_PRODUCT: '/api-products/:apiprod_uuid',
    OVERVIEW: '/apis/:api_uuid/overview',
    OVERVIEW_PRODUCT: '/api-products/:apiprod_uuid/overview',
    API_DEFINITION: '/apis/:api_uuid/api definition',
    WSDL: '/apis/:api_uuid/wsdl',
    API_DEFINITION_PRODUCT: '/api-products/:apiprod_uuid/api definition',
    SCHEMA_DEFINITION: '/apis/:api_uuid/schema definition',
    LIFE_CYCLE: '/apis/:api_uuid/lifecycle',
    CONFIGURATION: '/apis/:api_uuid/configuration',
    RUNTIME_CONFIGURATION: '/apis/:api_uuid/runtime-configuration',
    CONFIGURATION_PRODUCT: '/api-products/:apiprod_uuid/configuration',
    RUNTIME_CONFIGURATION_PRODUCT: '/api-products/:apiprod_uuid/runtime-configuration',
    RUNTIME_CONFIGURATION_WEBSOCKET: '/apis/:api_uuid/runtime-configuration-websocket',
    ENDPOINTS: '/apis/:api_uuid/endpoints',
    ENVIRONMENTS: '/apis/:api_uuid/environments',
    OPERATIONS: '/apis/:api_uuid/operations',
    RESOURCES: '/apis/:api_uuid/resources',
    RESOURCES_PRODUCT: '/api-products/:apiprod_uuid/resources',
    RESOURCES_PRODUCT_EDIT: '/api-products/:apiprod_uuid/resources/edit',
    SCOPES: '/apis/:api_uuid/scopes',
    DOCUMENTS: '/apis/:api_uuid/documents',
    DOCUMENTS_PRODUCT: '/api-products/:apiprod_uuid/documents',
    SUBSCRIPTIONS_PRODUCT: '/api-products/:apiprod_uuid/subscriptions',
    SUBSCRIPTIONS: '/apis/:api_uuid/subscriptions',
    SECURITY: '/apis/:api_uuid/security',
    BUSINESS_INFO: '/apis/:api_uuid/business info',
    BUSINESS_INFO_PRODUCT: '/api-products/:apiprod_uuid/business info',
    PROPERTIES: '/apis/:api_uuid/properties',
    PROPERTIES_PRODUCT: '/api-products/:apiprod_uuid/properties',
    NEW_VERSION: '/apis/:api_uuid/new_version',
    MONETIZATION: '/apis/:api_uuid/monetization',
    MONETIZATION_PRODUCT: '/api-products/:apiprod_uuid/monetization',
    EXTERNAL_STORES: '/apis/:api_uuid/external-devportals',
    TRYOUT: '/apis/:api_uuid/test-console',
    QUERYANALYSIS: '/apis/:api_uuid/queryanalysis',
};

// To make sure that paths will not change by outsiders, Basically an enum
Object.freeze(Details.paths);

Details.propTypes = {
    classes: PropTypes.shape({
        LeftMenu: PropTypes.string,
        content: PropTypes.string,
        leftLInkMain: PropTypes.string,
        contentInside: PropTypes.string,
    }).isRequired,
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.object,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.object,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.shape({
            leftMenuIconMainSize: PropTypes.number,
        }),
    }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Details));
