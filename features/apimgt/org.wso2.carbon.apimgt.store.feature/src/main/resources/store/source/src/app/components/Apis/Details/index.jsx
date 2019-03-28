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
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import Overview from './Overview';
import ApiConsole from './ApiConsole/ApiConsole';
import Documentation from './Documents/Documentation';
import Sdk from './Sdk';
import { PageNotFound } from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import RightPanel from './RightPanel';
import { ApiContext } from './ApiContext';
import Credentials from './Credentials/Credentials';
import Api from '../../../data/api';
import Comments from './Comments/Comments';
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

        this.state = {
            active: 'overview',
            overviewHiden: false,
            handleMenuSelect: this.handleMenuSelect,
            api: null,
            applications: null,
            subscribedApplications: [],
            applicationsAvailable: [],
            item: 1,
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
    updateSubscriptionData() {
        const api = new Api();
        const promised_api = api.getAPIById(this.api_uuid);
        const existing_subscriptions = api.getSubscriptions(this.api_uuid, null);
        const promised_applications = api.getAllApplications();

        Promise.all([promised_api, existing_subscriptions, promised_applications])
            .then((response) => {
                const [api, subscriptions, applications] = response.map(data => data.obj);
                // Getting the policies from api details
                this.setState({ api });
                if (api && api.policies) {
                    const apiTiers = api.policies;
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
                subscriptions.list.map(element => subscribedApplications.push({ value: element.applicationId, policy: element.policy }));
                this.setState({ subscribedApplications });

                // Removing subscribed applications from all the applications and get the available applications to subscribe
                const applicationsAvailable = [];
                for (let i = 0; i < applications.list.length; i++) {
                    const applicationId = applications.list[i].applicationId;
                    const applicationName = applications.list[i].name;
                    // include the application only if it does not has an existing subscriptions
                    let applicationSubscribed = false;
                    for (let j = 0; j < subscribedApplications.length; j++) {
                        if (subscribedApplications[j].value === applicationId) {
                            applicationSubscribed = true;
                            subscribedApplications[j].label = applicationName;
                        }
                    }
                    if (!applicationSubscribed) {
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
    }

    /**
     *
     *
     * @memberof Details
     */
    componentDidMount() {
        const currentLink = this.props.location.pathname.match(/[^\/]+(?=\/$|$)/g);
        if (currentLink && currentLink.length > 0) {
            this.setState({ active: currentLink[0] });
        }
        this.updateSubscriptionData();
    }

    /**
     *
     *
     * @returns
     * @memberof Details
     */
    render() {
        const { classes, theme } = this.props;
        const { api } = this.state;
        const redirect_url = '/apis/' + this.props.match.params.api_uuid + '/overview';
        const leftMenuIconMainSize = theme.custom.leftMenuIconMainSize;
        const globalStyle = 'body{ font-family: ' + theme.typography.fontFamily + '}';
        return (
            <ApiContext.Provider value={this.state}>
                <style>{globalStyle}</style>
                <div className={classes.LeftMenu}>
                    <Link to='/apis' className={classes.leftLInkMainWrapper}>
                        <div className={classes.leftLInkMain}>
                            <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon='api' />
                            <Typography className={classes.leftLInkMainText}>ALL APIs</Typography>
                        </div>
                    </Link>
                    <LeftMenuItem text='overview' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                    <LeftMenuItem text='credentials' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                    <LeftMenuItem text='comments' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                    <LeftMenuItem text='test' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                    <LeftMenuItem text='docs' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                    <LeftMenuItem text='sdk' handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                </div>
                <div className={classes.content}>
                    <InfoBar api_uuid={this.props.match.params.api_uuid} innerRef={node => (this.infoBar = node)} />
                    <Switch>
                        <Redirect exact from='/apis/:api_uuid' to={redirect_url} />
                        <Route path='/apis/:api_uuid/overview' component={Overview} />
                        <Route path='/apis/:api_uuid/credentials' component={Credentials} />
                        <Route path='/apis/:api_uuid/comments' component={() => <Comments api={api} apiId={this.props.match.params.api_uuid} />} />
                        <Route path='/apis/:api_uuid/test' component={ApiConsole} />
                        <Route path='/apis/:api_uuid/docs' component={Documentation} />
                        <Route path='/apis/:api_uuid/sdk' component={Sdk} />
                        <Route component={PageNotFound} />
                    </Switch>
                </div>
                {theme.custom.showApiHelp && <RightPanel />}
            </ApiContext.Provider>
        );
    }
}

Details.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Details);
