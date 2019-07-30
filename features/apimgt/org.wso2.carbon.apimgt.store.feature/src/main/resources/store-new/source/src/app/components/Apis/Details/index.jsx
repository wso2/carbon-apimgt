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
    Route, Switch, Redirect, Link,
} from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Loadable from 'react-loadable';
import { FormattedMessage, injectIntl } from 'react-intl';
import APIProduct from 'AppData/APIProduct';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import { PageNotFound } from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import RightPanel from './RightPanel';
import { ApiContext } from './ApiContext';
import Api from '../../../data/api';
import Progress from '../../Shared/Progress';
import AuthManager from '../../../data/AuthManager';

const LoadableSwitch = Loadable.Map({
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
        const { api_uuid } = props;
        const ApiConsole = loaded.ApiConsole.default;
        const Overview = loaded.Overview.default;
        const Documentation = loaded.Documentation.default;
        const Credentials = loaded.Credentials.default;
        const Comments = loaded.Comments.default;
        const Sdk = loaded.Sdk.default;
        const redirectURL = '/apis/' + api_uuid + '/overview';

        return (
            <Switch>
                <Redirect exact from='/apis/:api_uuid' to={redirectURL} />
                <Route path='/apis/:api_uuid/overview' component={Overview} />
                <Route path='/apis/:api_uuid/credentials' component={Credentials} />
                <Route path='/apis/:api_uuid/comments' component={Comments} />
                <Route path='/apis/:api_uuid/test' component={ApiConsole} />
                <Route path='/apis/:api_uuid/docs' component={Documentation} />
                <Route path='/apis/:api_uuid/sdk' component={Sdk} />
                <Route component={PageNotFound} />
            </Switch>
        );
    },
    loading() {
        return <Progress />;
    },
});

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
        this.updateSubscriptionData = () => {
            const user = AuthManager.getUser();

            const { path } = this.props;

            let promisedAPI = null;
            let existingSubscriptions = null;
            let promisedApplications = null;

            if (path === '/apis') {
                const api = new Api();
                promisedAPI = api.getAPIById(this.api_uuid);
                existingSubscriptions = api.getSubscriptions(this.api_uuid, null);
                promisedApplications = api.getAllApplications();
            } else if (path === '/api-products') {
                const apiProducts = new APIProduct();
                promisedAPI = apiProducts.getAPIProductById(this.api_uuid);
                existingSubscriptions = apiProducts.getSubscriptions(this.api_uuid, null);
                promisedApplications = apiProducts.getAllApplications();
            }

            if (!user) {
                promisedAPI.then((response) => {
                    this.setState({ api: response.body });
                }).catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
                return;
            }


            Promise.all([promisedAPI, existingSubscriptions, promisedApplications])
                .then((response) => {
                    const [api, subscriptions, applications] = response.map(data => data.obj);
                    // Getting the policies from api details
                    this.setState({ api });
                    if (api && api.tiers) {
                        const apiTiers = api.tiers;
                        const tiers = [];
                        for (let i = 0; i < apiTiers.length; i++) {
                            const tierName = apiTiers[i];
                            tiers.push({ value: tierName, label: tierName });
                        }
                        this.setState({ tiers });
                        if (tiers.length > 0) {
                            this.setState({ policyName: tiers[0].value });
                        }
                    }

                    const subscribedApplications = [];
                    // get the application IDs of existing subscriptions
                    subscriptions.list.map(element => subscribedApplications.push({
                        value: element.applicationId,
                        policy: element.throttlingPolicy,
                        status: element.status,
                        subscriptionId: element.subscriptionId,
                    }));
                    this.setState({ subscribedApplications });

                    // Removing subscribed applications from all the applications and get the available applications to subscribe
                    const applicationsAvailable = [];
                    for (let i = 0; i < applications.list.length; i++) {
                        const applicationId = applications.list[i].applicationId;
                        const applicationName = applications.list[i].name;
                        const applicationStatus = applications.list[i].status;
                        // include the application only if it does not has an existing subscriptions
                        let applicationSubscribed = false;
                        for (let j = 0; j < subscribedApplications.length; j++) {
                            if (subscribedApplications[j].value === applicationId) {
                                applicationSubscribed = true;
                                subscribedApplications[j].label = applicationName;
                            }
                        }
                        if (!applicationSubscribed && applicationStatus === 'APPROVED') {
                            applicationsAvailable.push({ value: applicationId, label: applicationName });
                        }
                    }
                    this.setState({ applicationsAvailable });
                    if (applicationsAvailable && applicationsAvailable.length > 0) {
                        this.setState({ applicationId: applicationsAvailable[0].value });
                    }
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
        };
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

        const { classes, theme, intl } = this.props;
        const { active, api } = this.state;
        const redirect_url = '/apis/' + this.props.match.params.api_uuid + '/overview';
        const leftMenuIconMainSize = theme.custom.leftMenuIconMainSize;
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
                    <InfoBar apiId={this.props.match.params.api_uuid} innerRef={node => (this.infoBar = node)} intl={intl} />
                    <LoadableSwitch api_uuid={this.props.match.params.api_uuid} />
                </div>
                {theme.custom.showApiHelp && <RightPanel />}
            </ApiContext.Provider>
        ) : <div className='apim-dual-ring' />
        );
    }
}

Details.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Details));
