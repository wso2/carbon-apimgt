/* eslint-disable react/jsx-props-no-spreading */
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
import React, { lazy, Suspense } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import ArrowBackIosIcon from '@material-ui/icons/ArrowBackIos';
import ArrowForwardIosIcon from '@material-ui/icons/ArrowForwardIos';
import {
    Route, Switch, Redirect, Link, withRouter,
} from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import Api from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Alert from 'AppComponents/Shared/Alert';
import classNames from 'classnames';
import { Helmet } from 'react-helmet';
import { app } from 'Settings';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import { ResourceNotFound } from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import { ApiContext } from './ApiContext';
import Progress from '../../Shared/Progress';
import Wizard from './Credentials/Wizard/Wizard';
import User from '../../../data/User';


const ApiConsole = lazy(() => import('./ApiConsole/ApiConsole' /* webpackChunkName: "APIConsole" */));
const GraphQLConsole = lazy(() => import('./GraphQLConsole/GraphQLConsole' /* webpackChunkName: "GraphQLConsole" */));
const Overview = lazy(() => import('./Overview' /* webpackChunkName: "APIOverview" */));
const Documents = lazy(() => import('./Documents/Documents' /* webpackChunkName: "APIDocuments" */));
const Credentials = lazy(() => import('./Credentials/Credentials' /* webpackChunkName: "APICredentials" */));
const Comments = lazy(() => import('./Comments/Comments' /* webpackChunkName: "APIComments" */));
const Sdk = lazy(() => import('./Sdk' /* webpackChunkName: "APISdk" */));

const LoadableSwitch = withRouter((props) => {
    const { match, api } = props;
    const apiUuid = match.params.apiUuid;
    const path = '/apis/';
    const { advertised } = api.advertiseInfo;
    const redirectURL = path + apiUuid + '/overview';

    let tryoutRoute;
    if (api.type === 'GRAPHQL') {
        tryoutRoute = <Route path='/apis/:apiUuid/test' component={GraphQLConsole} />
    }else {
        tryoutRoute = <Route path='/apis/:apiUuid/test' component={ApiConsole} />
    }

    return (
        <Suspense fallback={<Progress />}>
            <Switch>
                <Redirect exact from='/apis/:apiUuid' to={redirectURL} />
                <Route path='/apis/:apiUuid/overview' render={() => <Overview {...props} />} />
                <Route path='/apis/:apiUuid/documents' component={Documents} />
                <Route exact path='/apis/:apiUuid/credentials/wizard' component={Wizard} />
                {!advertised && <Route path='/apis/:apiUuid/comments' component={Comments} />}
                {!advertised && <Route path='/apis/:apiUuid/credentials' component={Credentials} />}
                {!advertised && tryoutRoute}
                {!advertised && <Route path='/apis/:apiUuid/sdk' component={Sdk} />}
                <Route component={ResourceNotFound} />
            </Switch>
        </Suspense>
    );
});

/**
 *
 * @returns style object
 * @param {*} theme
 */
const styles = (theme) => {
    const {
        custom: {
            leftMenu: { width, position },
        },
    } = theme;
    const shiftToLeft = position === 'vertical-left' ? width : 0;
    const shiftToRight = position === 'vertical-right' ? width : 0;
    const shiftToLeftMinView = position === 'vertical-left' ? 45 : 0;
    const shiftToRightMinView = position === 'vertical-right' ? 45 : 0;
    const leftMenuPaddingLeft = position === 'horizontal' ? theme.spacing(3) : 0;

    return {
        leftMenu: {
            backgroundColor: theme.custom.leftMenu.background,
            backgroundImage: `url(${app.context}${theme.custom.leftMenu.backgroundImage})`,
            textAlign: 'left',
            fontFamily: theme.typography.fontFamily,
            position: 'absolute',
            bottom: 0,
            paddingLeft: leftMenuPaddingLeft,
        },
        leftMenuHorizontal: {
            top: theme.custom.infoBar.height,
            width: '100%',
            overflowX: 'auto',
            height: 60,
            display: 'flex',
            left: 0,
        },
        leftMenuVerticalLeft: {
            width: theme.custom.leftMenu.width,
            top: 0,
            left: 0,
            overflowY: 'auto', 
        },
        leftMenuVerticalLeftMinView: {
            width: 45,
            top: 0,
            left: 0,
            overflowY: 'auto',  
        },
        leftMenuVerticalRight: {
            width: theme.custom.leftMenu.width,
            top: 0,
            right: 0,
            overflowY: 'auto',
        },
        leftLInkMain: {
            borderRight: 'solid 1px ' + theme.custom.leftMenu.background,
            cursor: 'pointer',
            background: theme.custom.leftMenu.rootBackground,
            color: theme.palette.getContrastText(theme.custom.leftMenu.rootBackground),
            textDecoration: 'none',
            alignItems: 'center',
            justifyContent: 'center',
            display: 'flex',
            height: theme.custom.infoBar.height,
            textDecoration: 'none',
        },
        leftLInkMainText: {
            fontSize: 18,
            color: theme.palette.grey[500],
            textDecoration: 'none',
            paddingLeft: theme.spacing(2),
        },
        detailsContent: {
            display: 'flex',
            flex: 1,
        },
        content: {
            display: 'flex',
            flex: 1,
            flexGrow: 1,
            flexDirection: 'column',
            marginLeft: shiftToLeft,
            marginRight: shiftToRight,
            paddingBottom: theme.spacing(3),
            overflowX: 'hidden',
        },
        contentExpandView: {
            display: 'flex',
            flex: 1,
            flexGrow: 1,
            flexDirection: 'column',
            marginLeft: shiftToLeftMinView,
            marginRight: shiftToRightMinView,
            paddingBottom: theme.spacing(3),
            overflowX: 'hidden', 
            minHeight: 'calc(100vh - 114px)',
        },
        shiftLeft: {
            marginLeft: 0,
        },
        contentLoader: {
            paddingTop: theme.spacing(3),
        },
        contentLoaderRightMenu: {
            paddingRight: theme.custom.leftMenu.width,
        },
    };
};
/**
 *
 *
 * @class Details
 * @extends {React.Component}
 */
class Details extends React.Component {
    /**
     *Creates an instance of Details.
     * @param {*} props
     * @memberof Details
     */
    constructor(props) {
        super(props);
        /**
         *
         *
         * @memberof Details
         */
        this.updateSubscriptionData = (callback) => {
            let existingSubscriptions = null;
            let promisedApplications = null;

            const restApi = new Api();

            // const subscriptionClient = new Subscription();
            const promisedAPI = restApi.getAPIById(this.api_uuid);

            promisedAPI
                .then((api) => {
                    this.setState({ api: api.body });
                })
                .catch((error) => {
                    const { status, response } = error;
                    const { setTenantDomain, intl } = this.props;

                    const message = intl.formatMessage({
                        defaultMessage: 'Invalid tenant domain',
                        id: 'Apis.Details.index.invalid.tenant.domain',
                    });
                    if (response && response.body.code === 901300) {
                        setTenantDomain('INVALID');
                        Alert.error(message);
                    }
                    console.error('Error when getting apis', error);
                    if (status === 404 || status === 403) {
                        this.setState({ notFound: true });
                    }
                });
            const user = AuthManager.getUser();
            if(user === null){
                const user1 = new User();
                this.setState({open:user1.isSideBarOpen});
            }
            if (user != null) {
                this.setState({open:user.isSideBarOpen});
                existingSubscriptions = restApi.getSubscriptions(this.api_uuid, null);
                const subscriptionLimit = Settings.app.subscribeApplicationLimit || 5000;
                promisedApplications = restApi.getAllApplications(null, subscriptionLimit);

                Promise.all([existingSubscriptions, promisedApplications])
                    .then((response) => {
                        const [subscriptions, applications] = response.map((data) => data.obj);
                        const appIdToNameMapping = applications.list.reduce((acc, cur) => {
                            acc[cur.applicationId] = cur.name;
                            return acc;
                        }, {});
                        // get the application IDs of existing subscriptions
                        const subscribedApplications = subscriptions.list.map((element) => {
                            return {
                                value: element.applicationId,
                                policy: element.throttlingPolicy,
                                status: element.status,
                                subscriptionId: element.subscriptionId,
                                label: element.applicationInfo.name,
                            };
                        });

                        // Removing subscribed applications from all the applications and get
                        // the available applications to subscribe
                        const subscribedAppIds = subscribedApplications.map((sub) => sub.value);
                        const applicationsAvailable = applications.list
                            .filter((app) => !subscribedAppIds.includes(app.applicationId) && app.status === 'APPROVED')
                            .map((filteredApp) => {
                                return {
                                    value: filteredApp.applicationId,
                                    label: filteredApp.name,
                                };
                            });
                        this.setState({ subscribedApplications, applicationsAvailable }, () => {
                            if (callback) {
                                callback();
                            }
                        });
                    })
                    .catch((error) => {
                        if (process.env.NODE_ENV !== 'production') {
                            console.log(error);
                        }
                        const { status } = error;
                        if (status === 404) {
                            this.setState({ notFound: true });
                        }
                    });
            }
        };


        this.state = {
            active: 'overview',
            overviewHiden: false,
            updateSubscriptionData: this.updateSubscriptionData,
            api: null,
            applications: null,
            subscribedApplications: [],
            applicationsAvailable: [],
            item: 1,
            xo: null,
        };
        this.setDetailsAPI = this.setDetailsAPI.bind(this);
        this.api_uuid = this.props.match.params.apiUuid;
        this.handleDrawerClose = this.handleDrawerClose.bind(this);
        this.handleDrawerOpen = this.handleDrawerOpen.bind(this);
    }

    /**
     *
     *
     * @memberof Details
     */
    componentDidMount() {
        this.updateSubscriptionData();
    }

    componentDidUpdate(prevProps) {
        const { match: { params: {apiUuid: prevApiUuid}} } = prevProps;
        const { match: { params: {apiUuid: newApiUuid}} } = this.props;
        if ( prevApiUuid !== newApiUuid ) {
            this.api_uuid = newApiUuid;
            this.updateSubscriptionData();
        } 
    }

    handleDrawerOpen() {
        this.setState({ open: true });  
        const user = AuthManager.getUser();
        if(user != null){
            user.isSideBarOpen = true;
            AuthManager.setUser(user);
        }
    };

    handleDrawerClose() {
        this.setState({ open: false });
        const user = AuthManager.getUser();
        if(user != null){
            user.isSideBarOpen = false;
            AuthManager.setUser(user);
        }
    };

    /**
     *
     *
     * @param {*} api
     * @memberof Details
     */
    setDetailsAPI(api) {
        this.setState({ api });
    }

    /**
     *
     *
     * @returns
     * @memberof Details
     */
    render() {
        const {
            classes, theme, intl, match,
        } = this.props;
        const user = AuthManager.getUser();
        const { apiUuid } = match.params;
        const { api, notFound , open} = this.state;
        const {
            custom: {
                leftMenu: {
                    rootIconSize, rootIconTextVisible, rootIconVisible, position,
                },
                apiDetailPages: {
                    showCredentials, showComments, showTryout, showDocuments, showSdks,
                },
                title: {
                    prefix, sufix,
                }
            },
        } = theme;
        const globalStyle = 'body{ font-family: ' + theme.typography.fontFamily + '}';
        const pathPrefix = '/apis/' + this.api_uuid + '/';
        if (!api && notFound) {
            return <ResourceNotFound />;
        }
        // check for widget=true in the query params. If it's present we render without <Base> component.
        const pageUrl = new URL(window.location);
        const isWidget = pageUrl.searchParams.get('widget');

        return api ? (
            <ApiContext.Provider value={this.state}>
                <Helmet>
                    <title>{`${prefix} ${api.name}${sufix}`}</title>
                </Helmet>
                <style>{globalStyle}</style>
                  {!isWidget && (
                <div
                    className={classNames(
                        classes.leftMenu,
                        {
                            [classes.leftMenuHorizontal]: position === 'horizontal'
                        },
                        {
                            [classes.leftMenuVerticalLeft]: position === 'vertical-left' && open,
                            [classes.leftMenuVerticalLeftMinView]: position === 'vertical-left' && !open,

                        },
                        {
                            [classes.leftMenuVerticalRight]: position === 'vertical-right',
                        },
                        'left-menu',

                    )}
                >
                    {rootIconVisible && (
                        <Link to='/apis' className={classes.leftLInkMain}>
                            <CustomIcon width={rootIconSize} height={rootIconSize} icon='api' />
                            {rootIconTextVisible && (
                                <Typography className={classes.leftLInkMainText}>
                                    <FormattedMessage id='Apis.Details.index.all.apis' defaultMessage='ALL APIs' />
                                </Typography>
                            )}
                        </Link>
                    )}
                    <LeftMenuItem
                        text={<FormattedMessage id='Apis.Details.index.overview' defaultMessage='Overview' />}
                        route='overview'
                        iconText='overview'
                        to={pathPrefix + 'overview'}
                        open={open}
                    />
                    {!api.advertiseInfo.advertised && (
                        <>
                            {user && showCredentials && (
                                <>
                                   
                                        <LeftMenuItem
                                            text={
                                                <FormattedMessage
                                                    id='Apis.Details.index.subscriptions'
                                                    defaultMessage='Subscriptions'
                                                />
                                            }
                                            route='credentials'
                                            iconText='credentials'
                                            to={pathPrefix + 'credentials'}
                                            open={open}
                                        />
                                    
                                </>
                            )}
                            {api.type !== 'WS' && showTryout && (
                               
                                    <LeftMenuItem
                                        text={<FormattedMessage id='Apis.Details.index.try.out'
                                            defaultMessage='Try out' />}
                                        route='test'
                                        iconText='test'
                                        to={pathPrefix + 'test'}
                                        open={open}
                                    />
                                
                            )}
                            {showComments && (
                                
                                    <LeftMenuItem
                                        text={
                                            <FormattedMessage id='Apis.Details.index.comments'
                                                defaultMessage='Comments' />
                                        }
                                        route='comments'
                                        iconText='comments'
                                        to={pathPrefix + 'comments'}
                                        open={open}
                                    />
                               
                            )}
                        </>
                    )}
                    {showDocuments && (
                       
                            <LeftMenuItem
                                text={<FormattedMessage id='Apis.Details.index.documentation'
                                    defaultMessage='Documentation' />}
                                route='documents'
                                iconText='docs'
                                to={pathPrefix + 'documents'}
                                open={open}
                            />
                       
                    )}
                    {!api.advertiseInfo.advertised && api.type !== 'WS' && showSdks && (
                        
                            <LeftMenuItem
                                text={<FormattedMessage id='Apis.Details.index.sdk' defaultMessage='SDKs' />}
                                route='sdk'
                                iconText='sdk'
                                to={pathPrefix + 'sdk'}
                                open={open}
                            />
                       
                    )}
                    {open ? (
                        <div onClick={this.handleDrawerClose}
                            style={{ width:100, paddingLeft: '15px', position: 'absolute',bottom: 0, cursor: 'pointer',}}
                        >
                            <ArrowBackIosIcon fontSize='medium' style={{ color: 'white' }} />
                        </div>
                    ) : (
                        <div onClick={this.handleDrawerOpen}
                            style={{ paddingLeft: '15px', position: 'absolute', bottom: 0, cursor: 'pointer',}}
                        >
                            <ArrowForwardIosIcon fontSize='medium' style={{ color: 'white' }} />
                        </div>

                    )}

                </div>
                )}

                <div
                    className={classNames(
                        { [classes.content]: open },
                        { [classes.contentExpandView]: !open },
                    )}
                >
                    <InfoBar apiId={apiUuid} innerRef={(node) => (this.infoBar = node)} intl={intl} {...this.props} />
                    <div
                        className={classNames(
                            { [classes.contentLoader]: position === 'horizontal' },
                            { [classes.contentLoaderRightMenu]: position === 'vertical-right' },
                        )}
                    >
                        <LoadableSwitch 
                            api={api} 
                            updateSubscriptionData={this.updateSubscriptionData}
                        />
                    </div>
                </div>
            </ApiContext.Provider>
        ) : (
                <div className='apim-dual-ring' />
            );
    }
}

Details.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default withSettings(injectIntl(withStyles(styles, { withTheme: true })(Details)));
