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
import StoreIcon from '@material-ui/icons/Store';
import DashboardIcon from '@material-ui/icons/Dashboard';
import CodeIcon from '@material-ui/icons/Code';
import PersonPinCircleOutlinedIcon from '@material-ui/icons/PersonPinCircleOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl, defineMessages } from 'react-intl';
import {
    Redirect, Route, Switch, Link, matchPath,
} from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import Utils from 'AppData/Utils';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import AuthorizedError from 'AppComponents/Base/Errors/AuthorizedError';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import AppContext from 'AppComponents/Shared/AppContext';
import LastUpdatedTime from 'AppComponents/Apis/Details/components/LastUpdatedTime';
import Divider from '@material-ui/core/Divider';
import { RevisionContextProvider } from 'AppComponents/Shared/RevisionContext';
import DevelopSectionMenu from 'AppComponents/Apis/Details/components/leftMenu/DevelopSectionMenu';
import { PROPERTIES as UserProperties } from 'AppData/User';
import Overview from './NewOverview/Overview';
import DesignConfigurations from './Configuration/DesignConfigurations';
import RuntimeConfiguration from './Configuration/RuntimeConfiguration';
import Topics from './Configuration/Topics';
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
import Comments from './Comments/Comments';
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
import TryOutConsole from './TryOut/TryOutConsole';

const styles = (theme) => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        minHeight: `calc(100vh - ${64 + theme.custom.footer.height}px)`,
    },
    leftLInkMain: {
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        textAlign: 'center',
        height: theme.custom.apis.topMenu.height,
    },
    content: {
        display: 'flex',
        flexGrow: 1,
        flexDirection: 'column',
        paddingBottom: theme.spacing(3),
    },
    contentInside: {
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(3),
        paddingTop: theme.spacing(2),
    },
    footeremaillink: {
        marginLeft: theme.custom.leftMenuWidth, /* 4px */
    },
    root: {
        backgroundColor: theme.palette.background.leftMenu,
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
        paddingTop: '0',
        paddingBottom: '0',
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightRegular,
    },
    expanded: {
        '&$expanded': {
            margin: 0,
            backgroundColor: theme.palette.background.leftMenu,
            minHeight: 40,
            paddingBottom: 0,
            paddingLeft: 0,
            paddingRight: 0,
            paddingTop: 0,
        },
    },
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        textTransform: theme.custom.leftMenuTextStyle,
        width: '100%',
        textAlign: 'left',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        fontSize: theme.typography.body1.fontSize,
        fontWeight: 250,
        whiteSpace: 'nowrap',
    },
    expandIconColor: {
        color: '#ffffff',
    },
    headingText: {
        marginTop: '10px',
        fontWeight: 800,
        color: '#ffffff',
        textAlign: 'left',
        marginLeft: '8px',
    },
    customIcon: {
        marginTop: (theme.custom.apis.topMenu.height - theme.custom.leftMenuIconMainSize) / 2,
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
            allRevisions: null,
            allEnvRevision: null,
            authorizedAPI: false,
            openPageSearch: false,
        };
        this.setAPI = this.setAPI.bind(this);
        this.setOpenPageSearch = this.setOpenPageSearch.bind(this);
        this.setAPIProduct = this.setAPIProduct.bind(this);
        this.updateAPI = this.updateAPI.bind(this);
        this.setImageUpdate = this.setImageUpdate.bind(this);
        this.getRevision = this.getRevision.bind(this);
        this.getDeployedEnv = this.getDeployedEnv.bind(this);
        this.handleAccordionState = this.handleAccordionState.bind(this);
        this.getLeftMenuItemForResourcesByType = this.getLeftMenuItemForResourcesByType.bind(this);
        this.getLeftMenuItemForDefinitionByType = this.getLeftMenuItemForDefinitionByType.bind(this);
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
        console.info(this.state.imageUpdate);
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
                    this.getRevision();
                    this.getDeployedEnv();
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        this.setState({ apiNotFound: true });
                    } else if (status === 403) {
                        this.setState({ authorizedAPI: true });
                    } else if (status === 401) {
                        doRedirectToLogin();
                    }
                });
        }
    }

    /**
     * Set open state for page search
     * @param {*} status
     */
    setOpenPageSearch(status) {
        const { openPageSearch } = this.state;
        if (status !== openPageSearch) {
            this.setState({ openPageSearch: status });
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
                this.getRevision();
                this.getDeployedEnv();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                } else if (status === 403) {
                    this.setState({ authorizedAPI: true });
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
            case 'WEBSUB':
            case 'SSE':
                return (
                    <>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.asyncApi.definition',
                                defaultMessage: 'AsyncAPI Definition',
                            })}
                            route='asyncApi definition'
                            to={pathPrefix + 'asyncApi definition'}
                            Icon={<CodeIcon />}
                        />
                    </>
                );
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
            case 'WEBSUB':
            case 'SSE':
                return (
                    <>
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.topics',
                                defaultMessage: 'topics',
                            })}
                            to={pathPrefix + 'topics'}
                            Icon={<ResourcesIcon />}
                        />
                    </>
                );
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
     * Get Revisions
     */
    getRevision() {
        const { api } = this.state;
        const restApi = new API();
        const restApiProduct = new APIProduct();
        let isAPIProduct = false;
        if (api.apiType === API.CONSTS.APIProduct) {
            isAPIProduct = true;
        }

        let promisedUpdate;
        let apiId = null;
        if (!isAPIProduct) {
            apiId = api.isRevision ? api.revisionedApiId : api.id;
            promisedUpdate = restApi.getRevisions(apiId);
        } else if (isAPIProduct) {
            apiId = api.isRevision ? api.revisionedApiProductId : api.id;
            promisedUpdate = restApiProduct.getProductRevisions(apiId);
        }
        return promisedUpdate
            .then((result) => {
                this.setState({ allRevisions: result.body.list });
            })
            .catch(() => {
                Alert.error('Something went wrong while getting the revisions!');
            });
    }

    /**
     * Get Depolyed environment
     */
    getDeployedEnv() {
        const { api } = this.state;
        const restApi = new API();
        const restApiProduct = new APIProduct();
        let isAPIProduct = false;
        if (api.apiType === API.CONSTS.APIProduct) {
            isAPIProduct = true;
        }

        let promisedUpdate;
        if (!isAPIProduct) {
            promisedUpdate = restApi.getRevisionsWithEnv(api.isRevision ? api.revisionedApiId : api.id);
        } else if (isAPIProduct) {
            promisedUpdate = restApiProduct.getProductRevisionsWithEnv(api.isRevision
                ? api.revisionedApiProductId : api.id);
        }
        return promisedUpdate
            .then((result) => {
                this.setState({ allEnvRevision: result.body.list });
            })
            .catch(() => {
                Alert.error('Something went wrong while getting the revisions!');
            });
    }

    /**
     * update ls
     * @param {String} name event triggered
     * @param {Boolean} isExpanded state
     */
    handleAccordionState(name, isExpanded) {
        const { user } = this.context;
        this.setState({ [name]: isExpanded });
        if (name === 'portalConfigsExpanded') {
            user.setProperty(UserProperties.PORTAL_CONFIG_OPEN, isExpanded);
        } else {
            user.setProperty(UserProperties.API_CONFIG_OPEN, isExpanded);
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
        if (api.apiType === API.CONSTS.APIProduct) {
            isAPIProduct = true;
        }

        const updatedProperties = _updatedProperties instanceof API ? _updatedProperties.toJson() : _updatedProperties;
        let promisedUpdate;
        // TODO: Ideally, The state should hold the corresponding API object
        // which we could call it's `update` method safely ~tmkb
        if (!isEmpty(updatedProperties)) {
            // newApi object has to be provided as the updatedProperties. Then api will be updated.
            promisedUpdate = api.update(updatedProperties);
        } else if (!isAPIProduct) {
            // Just like calling noArg `setState()` will just trigger a re-render without modifying the state,
            // Calling `updateAPI()` without args wil return the API without any update.
            // Just sync-up the api state with backend
            promisedUpdate = API.get(api.id);
        } else if (isAPIProduct) {
            promisedUpdate = APIProduct.get(api.id);
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
            api, apiNotFound, isAPIProduct, imageUpdate, tenantList, allRevisions, allEnvRevision, openPageSearch,
            authorizedAPI,
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
        const isAsyncAPI = api && (api.type === 'WS' || api.type === 'WEBSUB' || api.type === 'SSE');
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
        if (authorizedAPI) {
            return (
                <>
                    <AuthorizedError />
                </>
            );
        }

        if (!api) {
            return <Progress per={70} message='Loading API data ...' />;
        }
        const { leftMenuIconMainSize } = theme.custom;
        return (
            <Box display='flex' alignItems='stretch' flexDirection='row'>
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
                    <Box className={classes.LeftMenu}>
                        <Link to={'/' + (isAPIProduct ? 'api-products' : 'apis') + '/'}>
                            <div className={classes.leftLInkMain}>
                                <CustomIcon
                                    className={classes.customIcon}
                                    width={leftMenuIconMainSize}
                                    height={leftMenuIconMainSize}
                                    icon={isAPIProduct ? 'api-product' : 'apis'}
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
                            head='valueOnly'
                        />
                        <Typography className={classes.headingText}>
                            Develop
                        </Typography>
                        <DevelopSectionMenu
                            pathPrefix={pathPrefix}
                            isAPIProduct={isAPIProduct}
                            api={api}
                            getLeftMenuItemForResourcesByType={this.getLeftMenuItemForResourcesByType}
                            getLeftMenuItemForDefinitionByType={this.getLeftMenuItemForDefinitionByType}
                        />
                        <Divider />
                        {!isAPIProduct && api.advertiseInfo && !api.advertiseInfo.advertised && (
                            <>
                                <Typography className={classes.headingText}>Deploy</Typography>
                                <LeftMenuItem
                                    text={intl.formatMessage({
                                        id: 'Apis.Details.index.environments',
                                        defaultMessage: 'Deployments',
                                    })}
                                    route='deployments'
                                    to={pathPrefix + 'deployments'}
                                    Icon={<PersonPinCircleOutlinedIcon />}
                                />
                            </>
                        )}
                        {isAPIProduct && (
                            <>
                                <Typography className={classes.headingText}>Deploy</Typography>
                                <LeftMenuItem
                                    text={intl.formatMessage({
                                        id: 'Apis.Details.index.environments',
                                        defaultMessage: 'Deployments',
                                    })}
                                    route='deployments'
                                    to={pathPrefix + 'deployments'}
                                    Icon={<PersonPinCircleOutlinedIcon />}
                                />
                            </>
                        )}
                        {!isAPIProduct && api.advertiseInfo && !api.advertiseInfo.advertised && !api.isWebSocket()
                            && !api.isGraphql() && !isAsyncAPI && (
                            <div>
                                <Divider />
                                <Typography className={classes.headingText}>Test</Typography>
                                <LeftMenuItem
                                    route='test-console'
                                    text={intl.formatMessage({
                                        id: 'Apis.Details.index.Tryout.menu.name',
                                        defaultMessage: 'Try Out',
                                    })}
                                    to={pathPrefix + 'test-console'}
                                    iconText='test'
                                />
                            </div>
                        )}
                        {!isAPIProduct && !isRestricted(['apim:api_publish'], api) && (
                            <div>
                                <Divider />
                                <Typography className={classes.headingText}>Publish</Typography>
                                <LeftMenuItem
                                    text={intl.formatMessage({
                                        id: 'Apis.Details.index.lifecycle',
                                        defaultMessage: 'lifecycle',
                                    })}
                                    to={pathPrefix + 'lifecycle'}
                                    Icon={<LifeCycleIcon />}
                                />
                            </div>
                        )}
                        {!isAPIProduct && settingsContext.externalStoresEnabled && (
                            <>
                                <Divider />
                                <LeftMenuItem
                                    text={intl.formatMessage({
                                        id: 'Apis.Details.index.external-stores',
                                        defaultMessage: 'external dev portals',
                                    })}
                                    to={pathPrefix + 'external-devportals'}
                                    Icon={<StoreIcon />}
                                />
                            </>
                        )}
                        <Divider />
                    </Box>
                    <Box className={classes.content}>
                        <RevisionContextProvider
                            value={{
                                allRevisions,
                                getRevision: this.getRevision,
                                allEnvRevision,
                                getDeployedEnv: this.getDeployedEnv,
                            }}
                        >
                            <APIDetailsTopMenu
                                setOpenPageSearch={this.setOpenPageSearch}
                                openPageSearch={openPageSearch}
                                api={api}
                                isAPIProduct={isAPIProduct}
                                imageUpdate={imageUpdate}
                            />
                            <div className={classes.contentInside}>
                                <LastUpdatedTime lastUpdatedTime={api.lastUpdatedTime} />
                                <Switch>
                                    <Redirect exact from={Details.subPaths.BASE} to={redirectUrl} />
                                    <Route
                                        path={Details.subPaths.OVERVIEW_PRODUCT}
                                        key={Details.subPaths.OVERVIEW_PRODUCT}
                                        component={() => (
                                            <Overview
                                                setOpenPageSearch={this.setOpenPageSearch}
                                                api={api}
                                            />
                                        )}
                                    />
                                    <Route
                                        path={Details.subPaths.OVERVIEW}
                                        component={() => (
                                            <Overview
                                                setOpenPageSearch={this.setOpenPageSearch}
                                                api={api}
                                            />
                                        )}
                                    />
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
                                    <Route
                                        path={Details.subPaths.ASYNCAPI_DEFINITION}
                                        component={() => <APIDefinition api={api} updateAPI={this.updateAPI} />}
                                    />
                                    <Route
                                        path={Details.subPaths.LIFE_CYCLE}
                                        component={() => <LifeCycle api={api} />}
                                    />
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
                                        path={Details.subPaths.TOPICS}
                                        component={() => <Topics api={api} updateAPI={this.updateAPI} />}
                                    />
                                    <Route
                                        path={Details.subPaths.CONFIGURATION_PRODUCT}
                                        component={() => <DesignConfigurations api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.RUNTIME_CONFIGURATION_PRODUCT}
                                        component={() => <RuntimeConfiguration api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.ENDPOINTS}
                                        component={() => <Endpoints api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.ENVIRONMENTS}
                                        component={() => <Environments api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.ENVIRONMENTS_PRODUCT}
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
                                    <Route
                                        path={Details.subPaths.DOCUMENTS}
                                        component={() => <Documents api={api} />}
                                    />
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
                                    <Route path={Details.subPaths.COMMENTS} component={() => <Comments api={api} />} />
                                    <Route
                                        path={Details.subPaths.BUSINESS_INFO}
                                        component={() => <BusinessInformation api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.BUSINESS_INFO_PRODUCT}
                                        component={() => <BusinessInformation api={api} />}
                                    />
                                    <Route
                                        path={Details.subPaths.PROPERTIES}
                                        component={() => <Properties api={api} />}
                                    />
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
                                        component={() => <TryOutConsole apiObj={api} />}
                                    />
                                    <Route path={Details.subPaths.EXTERNAL_STORES} component={ExternalStores} />
                                    <Route
                                        path={Details.subPaths.COMMENTS}
                                        component={() => <Comments apiObj={api} />}
                                    />
                                </Switch>
                            </div>
                        </RevisionContextProvider>
                    </Box>
                </APIProvider>
            </Box>
        );
    }
}

Details.contextType = AppContext;
// Add your path here and refer it in above <Route /> component,
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
    ENVIRONMENTS: '/apis/:api_uuid/deployments',
    ENVIRONMENTS_PRODUCT: '/api-products/:apiprod_uuid/deployments',
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
    COMMENTS: '/apis/:api_uuid/comments',
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
    TOPICS: '/apis/:api_uuid/topics',
    ASYNCAPI_DEFINITION: '/apis/:api_uuid/asyncApi definition',
};

// To make sure that paths will not change by outsiders, Basically an enum
Object.freeze(Details.paths);

Details.propTypes = {
    classes: PropTypes.shape({
        LeftMenu: PropTypes.string,
        content: PropTypes.string,
        leftLInkMain: PropTypes.string,
        contentInside: PropTypes.string,
        footeremaillink: PropTypes.string,
    }).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
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
