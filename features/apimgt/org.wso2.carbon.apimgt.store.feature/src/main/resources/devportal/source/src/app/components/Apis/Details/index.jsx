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
import { Route, Switch, Redirect, Link, withRouter } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Loadable from 'react-loadable';
import { FormattedMessage, injectIntl } from 'react-intl';
import Api from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Alert from 'AppComponents/Shared/Alert';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import { PageNotFound } from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import { ApiContext } from './ApiContext';
import Progress from '../../Shared/Progress';
import classNames from 'classnames';

const LoadableSwitch = withRouter(Loadable.Map({
    loader: {
        ApiConsole: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "ApiConsole" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './ApiConsole/ApiConsole'),
        Overview: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "Overview" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './Overview'),
        Documentation: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "Documentation" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './Documents/Documentation'),
        Credentials: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "Credentials" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './Credentials/Credentials'),
        Comments: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "Comments" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './Comments/Comments'),
        Sdk: () =>
                import(
                    // eslint-disable-line function-paren-newline
                    /* webpackChunkName: "Sdk" */
                    /* webpackPrefetch: true */
                    // eslint-disable-next-line comma-dangle
                    './Sdk'),
    },
    render(loaded, props) {
        const { match, advertised } = props;
        const ApiConsole = loaded.ApiConsole.default;
        const Overview = loaded.Overview.default;
        const Documentation = loaded.Documentation.default;
        const Credentials = loaded.Credentials.default;
        const Comments = loaded.Comments.default;
        const Sdk = loaded.Sdk.default;
        const apiUuid = match.params.api_uuid;
        const path = '/apis/';

        const redirectURL = path + apiUuid + '/overview';

        return (
            <Switch>
                <Redirect exact from='/apis/:apiUuid' to={redirectURL} />
                <Route path='/apis/:apiUuid/overview' render={props => <Overview {...props} />} />
                <Route path='/apis/:apiUuid/docs' component={Documentation} />
                {!advertised && <Route path='/apis/:apiUuid/comments' component={Comments} />}
                {!advertised && <Route path='/apis/:apiUuid/credentials' component={Credentials} />}
                {!advertised && <Route path='/apis/:apiUuid/test' component={ApiConsole} />}
                {!advertised && <Route path='/apis/:apiUuid/sdk' component={Sdk} />}
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
const styles = (theme) => {
    const {
        custom: {
            leftMenu: { width, position },
        },
    } = theme;
    const shiftToLeft = position === 'vertical-left' ? width : 0;
    const shiftToRight = position === 'vertical-right' ? width : 0;
    const leftMenuPaddingLeft = position === 'horizontal' ? theme.spacing(3) : 0;

    return {
        LeftMenu: {
            backgroundColor: theme.custom.leftMenu.background,
            textAlign: 'left',
            fontFamily: theme.typography.fontFamily,
            position: 'absolute',
            bottom: 0,
            boxShadow: '11px -1px 15px -8px rgba(115,115,115,1)',
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
            marginLeft: shiftToLeft,
            marginRight: shiftToRight,
            paddingBottom: theme.spacing.unit * 3,
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
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
            const user = AuthManager.getUser();
            if (user != null) {
                existingSubscriptions = restApi.getSubscriptions(this.api_uuid, null);
                promisedApplications = restApi.getAllApplications();

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
                            .filter(app => !subscribedAppIds.includes(app.applicationId) && app.status === 'APPROVED')
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
        this.api_uuid = this.props.match.params.api_uuid;
    }

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
        this.updateSubscriptionData();
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
        const { apiUuid } = match.params;
        const { api } = this.state;
        const {
            custom: {
                leftMenu: {
                    rootIconSize, rootIconTextVisible, rootIconVisible, position,
                },
            },
        } = theme;
        const globalStyle = 'body{ font-family: ' + theme.typography.fontFamily + '}';
        const pathPrefix = '/apis/' + this.api_uuid + '/';

        return api ? (
            <ApiContext.Provider value={this.state}>
                <style>{globalStyle}</style>
                <div
                    className={classNames(
                        classes.LeftMenu,
                        {
                            [classes.leftMenuHorizontal]: position === 'horizontal',
                        },
                        {
                            [classes.leftMenuVerticalLeft]: position === 'vertical-left',
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
                    <LeftMenuItem text='overview' route='overview' to={pathPrefix + 'overview'} />
                    {!api.advertiseInfo.advertised && (
                        <React.Fragment>
                            <LeftMenuItem text='credentials' route='credentials' to={pathPrefix + 'credentials'} />
                            <LeftMenuItem text='comments' route='comments' to={pathPrefix + 'comments'} />
                            {api.type !== 'WS' && <LeftMenuItem text='test' route='test' to={pathPrefix + 'test'} />}
                        </React.Fragment>
                    )}
                    <LeftMenuItem text='Documentation' route='docs' to={pathPrefix + 'docs'} />
                    {!api.advertiseInfo.advertised && api.type !== 'WS' && (
                        <LeftMenuItem text='SDK' route='sdk' to={pathPrefix + 'sdk'} />
                    )}
                </div>
                <div className={classes.content}>
                    <InfoBar apiId={apiUuid} innerRef={node => (this.infoBar = node)} intl={intl} />
                    <div
                        className={classNames(
                            { [classes.contentLoader]: position === 'horizontal' },
                            { [classes.contentLoaderRightMenu]: position === 'vertical-right' },
                        )}
                    >
                        <LoadableSwitch api_uuid={apiUuid} advertised={api.advertiseInfo.advertised} />
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
    params: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default withSettings(injectIntl(withStyles(styles, { withTheme: true })(Details)));
