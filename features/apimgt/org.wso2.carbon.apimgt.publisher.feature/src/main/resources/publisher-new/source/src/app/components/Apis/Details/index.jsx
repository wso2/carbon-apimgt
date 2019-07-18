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

import LifeCycleIcon from '@material-ui/icons/Autorenew';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import ScopesIcon from '@material-ui/icons/VpnKey';
// import SecurityIcon from '@material-ui/icons/Security';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
// import CommentsIcon from '@material-ui/icons/CommentRounded';
import BusinessIcon from '@material-ui/icons/Business';
import CodeIcon from '@material-ui/icons/Code';
// import SubscriptionsIcon from '@material-ui/icons/Bookmarks';
import ConfigurationIcon from '@material-ui/icons/Build';
import PropertiesIcon from '@material-ui/icons/List';
import { withStyles } from '@material-ui/core/styles';
import { Redirect, Route, Switch, Link, matchPath } from 'react-router-dom';
import Utils from 'AppData/Utils';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import { PageNotFound } from 'AppComponents/Base/Errors';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import { injectIntl } from 'react-intl';
import Overview from './NewOverview/Overview';
import Configuration from './Configuration/Configuration';
import LifeCycle from './LifeCycle/LifeCycle';
import Documents from './Documents';
import Resources from './Resources/Resources';
import Endpoints from './Endpoints/Endpoints';
import Subscriptions from './Subscriptions/Subscriptions';
import Comments from './Comments/Comments';
import Scope from './Scopes';
import Security from './Security';
import APIDefinition from './APIDefinition/APIDefinition';
import APIDetailsTopMenu from './components/APIDetailsTopMenu';
import BusinessInformation from './BusinessInformation/BusinessInformation';
import Properties from './Properties/Properties';
import ApiContext from './components/ApiContext';
import CreateNewVersion from './NewVersion/NewVersion';

const styles = theme => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
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
        paddingBottom: theme.spacing.unit * 3,
    },
    contentInside: {
        paddingLeft: theme.spacing.unit * 3,
        paddingRight: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 2,
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
            // Skip the BASE path , because it's will match for any `/apis/:apiUUID/*` values
            if (subPathKey !== 'BASE') {
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
        this.handleMenuSelect = this.handleMenuSelect.bind(this);
        const { location, isAPIProduct } = this.props;
        const currentLink = location.pathname.match(/[^/]+(?=\/$|$)/g);
        let active = null;
        if (currentLink && currentLink.length > 0) {
            [active] = currentLink;
        }
        this.state = {
            api: null,
            apiNotFound: false,
            active: active || 'overview',
            updateAPI: this.updateAPI, // eslint-disable-line react/no-unused-state
            isAPIProduct,
        };
        this.setAPI = this.setAPI.bind(this);
        this.setAPIProduct = this.setAPIProduct.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Details
     */
    componentDidMount() {
        const {
            location: { pathname }, isAPIProduct,
        } = this.props;
        // Load API data iff request page is valid
        if (Details.isValidURL(pathname)) {
            if (isAPIProduct) {
                this.setAPIProduct();
            } else {
                this.setAPI();
            }
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
        const { apiUUID } = this.props.match.params;
        const { isAPIProduct } = this.props.isAPIProduct;
        if (!api || api.id === apiUUID) {
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
     *
     * @memberof Details
     */
    setAPI() {
        const { apiUUID } = this.props.match.params;
        const promisedApi = Api.get(apiUUID);
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
    /**
     *
     *
     * @memberof Details
     */
    setAPIProduct() {
        const { apiProdUUID } = this.props.match.params;
        const promisedApi = Api.getProduct(apiProdUUID);
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
                }
            });
    }

    /**
     *
     *
     * @param {*} newAPI
     * @memberof Details
     */
    updateAPI(newAPI, isAPIProduct) {
        const restAPI = new Api();
        /* eslint no-underscore-dangle: ["error", { "allow": ["_data"] }] */
        /* eslint no-param-reassign: ["error", { "props": false }] */
        if (newAPI._data) delete newAPI._data;
        if (newAPI.client) delete newAPI.client;
        if (isAPIProduct) {
            const promisedApi = restAPI.updateProduct(JSON.parse(JSON.stringify(newAPI)));
            promisedApi
                .then((api) => {
                    Alert.info(`${api.name} updated successfully.`);
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
        } else {
            const promisedApi = restAPI.update(JSON.parse(JSON.stringify(newAPI)));
            promisedApi
                .then((api) => {
                    Alert.info(`${api.name} updated successfully.`);
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
    }

    /**
     *
     *
     * @param {*} menuLink
     * @memberof Details
     */
    handleMenuSelect(menuLink) {
        const { isAPIProduct } = this.state;
        const path = isAPIProduct ? '/api-products/'
         + this.props.match.params.apiProdUUID + '/' + menuLink :
            '/apis/' + this.props.match.params.apiUUID + '/' + menuLink;
        this.props.history.push({
            pathname: path,
        });
        this.setState({ active: menuLink });
    }

    /**
     * Renders Grid container layout with NavBar place static in LHS, Components which coming as children for
     * Details page
     * should wrap it's content with <Grid item > element
     * @returns {Component} Render API Details page
     */
    render() {
        const {
            api, apiNotFound, active, isAPIProduct,
        } = this.state;
        const {
            classes,
            theme,
            match,
            location: pageLocation,
            location: { pathname }, // nested destructuring
        } = this.props;
        const { intl } = this.props;
        // pageLocation renaming is to prevent es-lint errors saying can't use global name location
        if (!Details.isValidURL(pathname)) {
            return <PageNotFound location={pageLocation} />;
        }

        const redirectUrl = (isAPIProduct ? '/api-products/' : '/apis/') + match.params.api_uuid + '/' + active;
        if (apiNotFound) {
            const { apiUUID } = match.params;
            const resourceNotFountMessage = {
                title: `API is Not Found in the "${Utils.getCurrentEnvironment().label}" Environment`,
                body: `Can't find the API with the id "${apiUUID}"`,
            };
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!api) {
            return <Progress />;
        }
        const { leftMenuIconMainSize } = theme.custom;

        return (
            <React.Fragment>
                <ApiContext.Provider value={this.state}>
                    <div className={classes.LeftMenu}>
                        <Link to={isAPIProduct ? '/api-products' : '/apis'}>
                            <div className={classes.leftLInkMain}>
                                <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon='api' />
                            </div>
                        </Link>
                        <LeftMenuItem text='overview' handleMenuSelect={this.handleMenuSelect} active={active} />
                        <LeftMenuItem
                            text='configuration'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ConfigurationIcon />}
                        />
                        {isAPIProduct ? null : (
                            <LeftMenuItem
                                text='endpoints'
                                handleMenuSelect={this.handleMenuSelect}
                                active={active}
                                Icon={<EndpointIcon />}
                            />
                        )}
                        <LeftMenuItem
                            text='api definition'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<CodeIcon />}
                        />
                        <LeftMenuItem
                            text='resources'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ResourcesIcon />}
                        />
                        <LeftMenuItem
                            text='lifecycle'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<LifeCycleIcon />}
                        />
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.left.menu.scope',
                                defaultMessage: 'scopes',
                            })}
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<ScopesIcon />}
                        />
                        <LeftMenuItem
                            text='documents'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<DocumentsIcon />}
                        />
                        {/* TODO: uncomment when component run without errors */}
                        {/* <LeftMenuItem
                         text='subscriptions'
                         handleMenuSelect={this.handleMenuSelect}
                         active={active}
                         Icon={<SubscriptionsIcon />}
                         />
                         <LeftMenuItem
                         text='security'
                         handleMenuSelect={this.handleMenuSelect}
                         active={active}
                         Icon={<SecurityIcon />}
                         />
                         <LeftMenuItem
                         text='comments'
                         handleMenuSelect={this.handleMenuSelect}
                         active={active}
                         Icon={<CommentsIcon />}
                         /> */}
                        <LeftMenuItem
                            text='business info'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<BusinessIcon />}
                        />
                        <LeftMenuItem
                            text='properties'
                            handleMenuSelect={this.handleMenuSelect}
                            active={active}
                            Icon={<PropertiesIcon />}
                        />
                    </div>
                    <div className={classes.content}>
                        <APIDetailsTopMenu api={api} />
                        <div className={classes.contentInside}>
                            <Switch>
                                <Redirect exact from={Details.subPaths.BASE} to={redirectUrl} />
                                <Route
                                    path={isAPIProduct ? Details.subPaths.OVERVIEW_PRODUCT : Details.subPaths.OVERVIEW}
                                    component={() => <Overview />}
                                />
                                <Route
                                    path={Details.subPaths.API_DEFINITION}
                                    component={() => <APIDefinition api={api} />}
                                />
                                <Route path={Details.subPaths.LIFE_CYCLE} component={() => <LifeCycle api={api} />} />
                                <Route
                                    path={isAPIProduct ?
                                        Details.subPaths.CONFIGURATION_PRODUCT : Details.subPaths.CONFIGURATION}
                                    component={() => <Configuration api={api} />}
                                />
                                <Route path={Details.subPaths.ENDPOINTS} component={() => <Endpoints api={api} />} />
                                <Route path={Details.subPaths.RESOURCES} component={() => <Resources api={api} />} />
                                <Route path={Details.subPaths.SCOPES} component={() => <Scope api={api} />} />
                                <Route path={Details.subPaths.DOCUMENTS} component={() => <Documents api={api} />} />
                                <Route
                                    path={Details.subPaths.SUBSCRIPTIONS}
                                    component={() => <Subscriptions api={api} />}
                                />
                                <Route path={Details.subPaths.SECURITY} component={() => <Security api={api} />} />
                                <Route path={Details.subPaths.COMMENTS} component={() => <Comments api={api} />} />
                                <Route
                                    path={Details.subPaths.BUSINESS_INFO}
                                    component={() => <BusinessInformation />}
                                />
                                <Route path={Details.subPaths.PROPERTIES} component={() => <Properties />} />
                                <Route path={Details.subPaths.NEW_VERSION} component={() => <CreateNewVersion />} />
                            </Switch>
                        </div>
                    </div>
                </ApiContext.Provider>
            </React.Fragment>
        );
    }
}

// Add your path here and refer it in above <Route/> component,
// Paths that are not defined here will be returned with Not Found error
// key name doesn't matter here, Use an appropriate name as the key
Details.subPaths = {
    BASE: '/apis/:api_uuid',
    BASE_PRODUCT: '/api-products/:apiprod_uuid',
    OVERVIEW: '/apis/:api_uuid/overview',
    OVERVIEW_PRODUCT: '/api-products/:apiprod_uuid/overview',
    API_DEFINITION: '/apis/:api_uuid/api definition',
    LIFE_CYCLE: '/apis/:api_uuid/lifecycle',
    CONFIGURATION: '/apis/:api_uuid/configuration',
    CONFIGURATION_PRODUCT: '/api-products/:apiprod_uuid/configuration',
    ENDPOINTS: '/apis/:api_uuid/endpoints',
    RESOURCES: '/apis/:api_uuid/resources',
    RESOURCES_PRODUCT: '/api_products/:apiprod_uuid/resources',
    SCOPES: '/apis/:api_uuid/scopes',
    DOCUMENTS: '/apis/:api_uuid/documents',
    SUBSCRIPTIONS: '/apis/:api_uuid/subscriptions',
    SECURITY: '/apis/:api_uuid/security',
    COMMENTS: '/apis/:api_uuid/comments',
    BUSINESS_INFO: '/apis/:api_uuid/business info',
    PROPERTIES: '/apis/:api_uuid/properties',
    NEW_VERSION: '/apis/:api_uuid/new_version',
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
