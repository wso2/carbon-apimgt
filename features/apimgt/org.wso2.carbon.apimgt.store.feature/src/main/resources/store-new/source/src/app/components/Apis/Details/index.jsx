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
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {
    Route, Switch, Redirect, Link, withRouter,
} from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Loadable from 'react-loadable';
import { FormattedMessage, injectIntl } from 'react-intl';
import APIProduct from 'AppData/APIProduct';
import Api from 'AppData/api';
import CONSTS from 'AppData/Constants';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import { PageNotFound } from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import RightPanel from './RightPanel';
import { ApiContext } from './ApiContext';
import Progress from '../../Shared/Progress';


const LoadableSwitch = withRouter(Loadable.Map({
    loader: {
        ApiConsole: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "ApiConsole" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './ApiConsole/ApiConsole'
        ),
        Overview: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "Overview" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './Overview'
        ),
        Documentation: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "Documentation" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './Documents/Documentation'
        ),
        Credentials: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "Credentials" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './Credentials/Credentials'
        ),
        Comments: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "Comments" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './Comments/Comments'
        ),
        Sdk: () => import(
            // eslint-disable-line function-paren-newline
            /* webpackChunkName: "Sdk" */
            /* webpackPrefetch: true */
            // eslint-disable-next-line comma-dangle
            './Sdk'
        ),
    },
    render(loaded, props) {
        const { apiType, match } = props;
        const ApiConsole = loaded.ApiConsole.default;
        const Overview = loaded.Overview.default;
        const Documentation = loaded.Documentation.default;
        const Credentials = loaded.Credentials.default;
        const Comments = loaded.Comments.default;
        const Sdk = loaded.Sdk.default;
        const api_uuid = match.params.api_uuid;
        let path = '/apis/';
        if (apiType === CONSTS.API_PRODUCT_TYPE) {
            path = '/api-products/';
        }
        const redirectURL = path + api_uuid + '/overview';

        return (
            <Switch>
                <Redirect exact from='/apis/:api_uuid' to={redirectURL} />
                <Route
                    path='/apis/:api_uuid/overview'
                    render={props => (
                        <Overview {...props} />)}
                />
                <Route path='/apis/:api_uuid/credentials' component={Credentials} />
                <Route path='/apis/:api_uuid/comments' component={Comments} />
                <Route path='/apis/:api_uuid/test' component={ApiConsole} />
                <Route path='/apis/:api_uuid/docs' component={Documentation} />
                <Route path='/apis/:api_uuid/sdk' component={Sdk} />
                <Redirect exact from='/api-products/:api_uuid' to={redirectURL} />
                <Route
                    path='/api-products/:api_uuid/overview'
                    render={props => (
                        <Overview {...props} />)}
                />
                <Route path='/api-products/:api_uuid/credentials' component={Credentials} />
                <Route path='/api-products/:api_uuid/comments' component={Comments} />
                <Route path='/api-products/:api_uuid/test' component={ApiConsole} />
                <Route path='/api-products/:api_uuid/docs' component={Documentation} />
                <Route path='/api-products/:api_uuid/sdk' component={Sdk} />
                <Route component={PageNotFound} />
            </Switch>
        );
    },
    loading() {
        return <Progress />;
    },
}));

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'left',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
        boxShadow: '11px -1px 15px -8px rgba(115,115,115,1)',
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        color: theme.palette.getContrastText(theme.palette.background.leftMenuActive),
        textDecoration: 'none',
        alignItems: 'center',
        paddingLeft: theme.spacing.unit * 2,
        display: 'flex',
    },
    leftLInkMainText: {
        fontSize: 18,
        color: theme.palette.grey[500],
        textDecoration: 'none',
        paddingLeft: theme.spacing.unit * 2,
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
    leftLInkMainWrapper: {
        textDecoration: 'none',
    },
});
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
            const { apiType } = this.props;
            this.setState({ apiType });

            let promisedAPI = null;
            let existingSubscriptions = null;
            let promisedApplications = null;
            let restApi = null;

            if (apiType === CONSTS.API_TYPE) {
                restApi = new Api();
            } else if (apiType === CONSTS.API_PRODUCT_TYPE) {
                restApi = new APIProduct();
            }

            promisedAPI = restApi.getAPIById(this.api_uuid);
            existingSubscriptions = restApi.getSubscriptions(this.api_uuid, null);
            promisedApplications = restApi.getAllApplications();

            promisedAPI.then((api) => {
                this.setState({ api: api.body });
            }).catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });

            Promise.all([existingSubscriptions, promisedApplications])
                .then((response) => {
                    const [subscriptions, applications] = response.map(data => data.obj);
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
                            label: appIdToNameMapping[element.applicationId],
                        };
                    });

                    // Removing subscribed applications from all the applications and get
                    // the available applications to subscribe
                    const subscribedAppIds = subscribedApplications.map(sub => sub.value);
                    const applicationsAvailable = applications.list
                        .filter(app => !subscribedAppIds.includes(app.applicationId)
                        && app.status === 'APPROVED')
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
        };
        const { apiType } = this.props;
        this.state = {
            active: 'overview',
            overviewHiden: false,
            handleMenuSelect: this.handleMenuSelect,
            updateSubscriptionData: this.updateSubscriptionData,
            api: null,
            applications: null,
            subscribedApplications: [],
            applicationsAvailable: [],
            item: 1,
            xo: null,
            apiType,
        };
        this.setDetailsAPI = this.setDetailsAPI.bind(this);
        this.api_uuid = this.props.match.params.api_uuid;
    }

    /**
     *
     *
     * @memberof Details
     */
    handleMenuSelect = (menuLink) => {
        this.props.history.push({ pathname: '/apis/' + this.props.match.params.api_uuid + '/' + menuLink });
        menuLink === 'overview' ? this.infoBar.toggleOverview(true) : this.infoBar.toggleOverview(false);
        this.setState({ active: menuLink });
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
     * @memberof Details
     */
    componentDidMount() {
        this.updateActiveLink();
        this.updateSubscriptionData();
    }

    /**
     *
     * Selects the active link for the side panel based on the URL
     * @memberof Details
     */
    updateActiveLink() {
        const { active } = this.state;
        const currentLink = this.props.location.pathname.match(/[^\/]+(?=\/$|$)/g);

        if (currentLink && currentLink.length > 0 && active !== currentLink[0]) {
            this.setState({ active: currentLink[0] });
        }
    }

    /**
     *
     *
     * @returns
     * @memberof Details
     */
    render() {
        this.updateActiveLink();

        const {
            classes, theme, intl, apiType, match,
        } = this.props;
        const { apiUuid } = match.params;
        const { active, api } = this.state;
        const { leftMenuIconMainSize } = theme.custom;
        const globalStyle = 'body{ font-family: ' + theme.typography.fontFamily + '}';
        return (api ? (
            <ApiContext.Provider value={this.state}>
                <style>{globalStyle}</style>
                <div className={classes.LeftMenu}>
                    <Link to='/apis' className={classes.leftLInkMainWrapper}>
                        <div className={classes.leftLInkMain}>
                            <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon='api' />
                            <Typography className={classes.leftLInkMainText}>
                                <FormattedMessage id='Apis.Details.index.all.apis' defaultMessage='ALL APIs' />
                            </Typography>
                        </div>
                    </Link>
                    <LeftMenuItem text='overview' handleMenuSelect={this.handleMenuSelect} active={active} />
                    <LeftMenuItem text='credentials' handleMenuSelect={this.handleMenuSelect} active={active} />
                    {/* TODO: uncomment when the feature is working */}
                    {/* <LeftMenuItem text='comments' handleMenuSelect={this.handleMenuSelect} active={active} /> */}
                    <LeftMenuItem text='test' handleMenuSelect={this.handleMenuSelect} active={active} />
                    <LeftMenuItem text='docs' handleMenuSelect={this.handleMenuSelect} active={active} />
                    <LeftMenuItem text='sdk' handleMenuSelect={this.handleMenuSelect} active={active} />
                </div>
                <div className={classes.content}>
                    <InfoBar apiId={apiUuid} innerRef={node => (this.infoBar = node)} intl={intl} />
                    <LoadableSwitch api_uuid={apiUuid} apiType={apiType} />
                </div>
                {theme.custom.showApiHelp && <RightPanel />}
            </ApiContext.Provider>
        ) : <div className='apim-dual-ring' />
        );
    }
}

Details.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({}).isRequired,
    params: PropTypes.shape({}).isRequired,
    apiType: PropTypes.string.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Details));
