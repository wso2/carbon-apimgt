import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import LeftMenuItemPrimary from 'AppComponents/Shared/LeftMenuItemPrimary';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl } from 'react-intl';

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
    },
    contentInside: {
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(3),
        paddingTop: theme.spacing(2),
    },
});

const subPaths = {
    TASKS_USER_CREATION: '/admin/tasks/user_creation',
    TASKS_APPLICATION_CREATION: '/admin/tasks/application_creation',
    TASKS_SUBSCRIPTION_CREATION: '/admin/tasks/subscription_creation',
    TASKS_APPLICATION_REGISTRATION: '/admin/tasks/applicationregistration',
    TASKS_API_STATE_CHANGE: '/admin/tasks/apistatechange',
    SETTINGS_APPLICATIONS: '/admin/settings/applications',
    SETTINGS_SCOPE_MAPPING: '/admin/settings/scopemapping',
    MICROGATEWAY_LABELS: '/admin/microgateway/labels',
    CATEGORIES_API_CATEGORIES: '/admin/categories/apicategories',
    THROTTLINGPOLICIES_ADVANCED_POLICIES: '/admin/throttlingpolicies/advancedpolicies',
    THROTTLINGPOLICIES_APPLICATION_POLICIES: '/admin/throttlingpolicies/applicationpolicies',
    THROTTLINGPOLICIES_SUBSCRIPTION_POLICIES: '/admin/throttlingpolicies/subscriptionpolicies',
    THROTTLINGPOLICIES_CUSTOM_POLICIES: '/admin/throttlingpolicies/custompolicies',
    THROTTLINGPOLICIES_BLACKLIST_POLICIES: '/admin/throttlingpolicies/blacklistpolicies',
    ANALYTICS_API_AVAILABILITY: '/admin/analytics/apiavailability',
};

const tasksSubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.tasks.usercreation',
            defaultMessage: 'USER CREATION',
        }),
        to: subPaths.TASKS_USER_CREATION,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.tasks.applicationcreation',
            defaultMessage: 'APPLICATION CREATION',
        }),
        to: subPaths.TASKS_APPLICATION_CREATION,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.tasks.subscriptioncreation',
            defaultMessage: 'SUBSCRIPTION CREATION',
        }),
        to: subPaths.TASKS_SUBSCRIPTION_CREATION,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.tasks.applicationregistration',
            defaultMessage: 'APPLICATION REGISTRATION',
        }),
        to: subPaths.TASKS_APPLICATION_REGISTRATION,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.tasks.apistatechange',
            defaultMessage: 'API STATE CHANGE',
        }),
        to: subPaths.TASKS_API_STATE_CHANGE,
    },
];

const settingsSubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.settings.applications',
            defaultMessage: 'APPLICATIONS',
        }),
        to: subPaths.SETTINGS_APPLICATIONS,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.settings.scopemapping',
            defaultMessage: 'SCOPE MAPPING',
        }),
        to: subPaths.SETTINGS_SCOPE_MAPPING,
    },
];

const microgatewaySubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.microgateway.labels',
            defaultMessage: 'LABELS',
        }),
        to: subPaths.MICROGATEWAY_LABELS,
    },
];

const categoriesSubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.categories.apicategories',
            defaultMessage: 'API CATEGORIES',
        }),
        to: subPaths.CATEGORIES_API_CATEGORIES,
    },
];

const throttlingPoliciesSubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.advancedpolicies',
            defaultMessage: 'ADVANCED POLICIES',
        }),
        to: subPaths.THROTTLINGPOLICIES_ADVANCED_POLICIES,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.applicationpolicies',
            defaultMessage: 'APPLICATION POLICIES',
        }),
        to: subPaths.THROTTLINGPOLICIES_APPLICATION_POLICIES,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.subscriptionpolicies',
            defaultMessage: 'SUBSCRIPTION POLICIES',
        }),
        to: subPaths.THROTTLINGPOLICIES_SUBSCRIPTION_POLICIES,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.custompolicies',
            defaultMessage: 'CUSTOM POLICIES',
        }),
        to: subPaths.THROTTLINGPOLICIES_CUSTOM_POLICIES,
    },
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.blacklistpolicies',
            defaultMessage: 'BLACKLIST POLICIES',
        }),
        to: subPaths.THROTTLINGPOLICIES_BLACKLIST_POLICIES,
    },
];

const analyticsSubMenuDetails = (intl) => [
    {
        name: intl.formatMessage({
            id: 'leftmenu.secondary.analytics.apiavailability',
            defaultMessage: 'API AVAILABILITY',
        }),
        to: subPaths.ANALYTICS_API_AVAILABILITY,
    },
];
const botDetectionSubMenuDetails = () => [];

// todo: decide the Naming and directory for this.
//       Since Page contents are also rendered from this component.
const LeftMenu = (props) => {
    const { classes, intl } = props;
    return (
        <Router>
            <div className={classes.LeftMenu}>
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.tasks',
                        defaultMessage: 'TASKS',
                    })}
                    secondaryMenuDetails={tasksSubMenuDetails(intl)}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.settings',
                        defaultMessage: 'SETTINGS',
                    })}
                    secondaryMenuDetails={settingsSubMenuDetails(intl)}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.microgateway',
                        defaultMessage: 'MICROGATEWAY',
                    })}
                    secondaryMenuDetails={microgatewaySubMenuDetails(intl)}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.categories',
                        defaultMessage: 'CATEGORIES',
                    })}
                    secondaryMenuDetails={categoriesSubMenuDetails(intl)}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.throttlingpolicies',
                        defaultMessage: 'THROTTLING POLICIES',
                    })}
                    secondaryMenuDetails={throttlingPoliciesSubMenuDetails(intl)}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.botdetection',
                        defaultMessage: 'BOT DETECTION',
                    })}
                    secondaryMenuDetails={botDetectionSubMenuDetails()}
                />
                <LeftMenuItemPrimary
                    text={intl.formatMessage({
                        id: 'leftmenu.primary.analytics',
                        defaultMessage: 'ANALYTICS',
                    })}
                    secondaryMenuDetails={analyticsSubMenuDetails(intl)}
                />
            </div>
            <div className={classes.content}>
                <div className={classes.contentInside}>
                    {/* todo: Create components for each submenu item and replace h1's */}
                    {/* todo: Setup server sider routing to support refreshes and link based navigation */}
                    <Switch>
                        <Route path={subPaths.TASKS_USER_CREATION}>
                            <h1>Hello user creation</h1>
                        </Route>
                        <Route path={subPaths.TASKS_APPLICATION_CREATION}>
                            <h1>Hello application creation</h1>
                        </Route>
                        <Route path={subPaths.TASKS_SUBSCRIPTION_CREATION}>
                            <h1>Hello subscription creation</h1>
                        </Route>
                        <Route path={subPaths.TASKS_APPLICATION_REGISTRATION}>
                            <h1>Hello TASKS_APPLICATION_REGISTRATION</h1>
                        </Route>
                        <Route path={subPaths.TASKS_API_STATE_CHANGE}>
                            <h1>Hello TASKS_API_STATE_CHANGE</h1>
                        </Route>
                        <Route path={subPaths.SETTINGS_APPLICATIONS}>
                            <h1>Hello SETTINGS_APPLICATIONS</h1>
                        </Route>
                        <Route path={subPaths.SETTINGS_SCOPE_MAPPING}>
                            <h1>Hello SETTINGS_SCOPE_MAPPING</h1>
                        </Route>
                        <Route path={subPaths.MICROGATEWAY_LABELS}>
                            <h1>Hello MICROGATEWAY_LABELS</h1>
                        </Route>
                        <Route path={subPaths.CATEGORIES_API_CATEGORIES}>
                            <h1>Hello CATEGORIES_API_CATEGORIES</h1>
                        </Route>
                        <Route path={subPaths.THROTTLINGPOLICIES_ADVANCED_POLICIES}>
                            <h1>Hello THROTTLINGPOLICIES_ADVANCED_POLICIES</h1>
                        </Route>
                        <Route path={subPaths.THROTTLINGPOLICIES_APPLICATION_POLICIES}>
                            <h1>Hello THROTTLINGPOLICIES_APPLICATION_POLICIES</h1>
                        </Route>
                        <Route path={subPaths.THROTTLINGPOLICIES_SUBSCRIPTION_POLICIES}>
                            <h1>Hello THROTTLINGPOLICIES_SUBSCRIPTION_POLICIES</h1>
                        </Route>
                        <Route path={subPaths.THROTTLINGPOLICIES_CUSTOM_POLICIES}>
                            <h1>Hello THROTTLINGPOLICIES_CUSTOM_POLICIES</h1>
                        </Route>
                        <Route path={subPaths.THROTTLINGPOLICIES_BLACKLIST_POLICIES}>
                            <h1>Hello THROTTLINGPOLICIES_BLACKLIST_POLICIES</h1>
                        </Route>
                        <Route path={subPaths.ANALYTICS_API_AVAILABILITY}>
                            <h1>Hello ANALYTICS_API_AVAILABILITY</h1>
                        </Route>
                        {/* todo: determine the component for /admin/ */}
                        <Route path='/admin/'>
                            <h1>Select submenu</h1>
                        </Route>
                    </Switch>
                </div>
            </div>
        </Router>
    );
};

export default injectIntl(withStyles(styles)(LeftMenu));
