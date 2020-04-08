import React from 'react';
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

const tasksSubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.tasks.usercreation',
            defaultMessage: 'USER CREATION',
        }),
        to: 'user_creation',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.tasks.applicationcreation',
            defaultMessage: 'APPLICATION CREATION',
        }),
        to: 'application_creation',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.tasks.subscriptioncreation',
            defaultMessage: 'SUBSCRIPTION CREATION',
        }),
        to: 'subscription_creation',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.tasks.applicationregistration',
            defaultMessage: 'APPLICATION REGISTRATION',
        }),
        to: 'application_registration',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.tasks.apistatechange',
            defaultMessage: 'API STATE CHANGE',
        }),
        to: 'api_state_change',
    },
];

const settingsSubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.settings.applications',
            defaultMessage: 'APPLICATIONS',
        }),
        to: 'applications',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.settings.scopemapping',
            defaultMessage: 'SCOPE MAPPING',
        }),
        to: 'scope_mapping',
    },
];

const microgatewaySubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.microgateway.labels',
            defaultMessage: 'LABELS',
        }),
        to: 'labels',
    },
];

const categoriesSubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.categories.apicategories',
            defaultMessage: 'API CATEGORIES',
        }),
        to: 'api_categories',
    },
];

const throttlingPoliciesSubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.advancedpolicies',
            defaultMessage: 'ADVANCED POLICIES',
        }),
        to: 'advanced_policies',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.applicationpolicies',
            defaultMessage: 'APPLICATION POLICIES',
        }),
        to: 'application_policies',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.subscriptionpolicies',
            defaultMessage: 'SUBSCRIPTION POLICIES',
        }),
        to: 'subcription_policies',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.custompolicies',
            defaultMessage: 'CUSTOM POLICIES',
        }),
        to: 'custom_policies',
    },
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.throttlingpolicies.blacklistpolicies',
            defaultMessage: 'BLACKLIST POLICIES',
        }),
        to: 'blacklist_policies',
    },
];

const analyticsSubMenuDetails = (props) => [
    {
        name: props.intl.formatMessage({
            id: 'leftmenu.secondary.analytics.apiavailability',
            defaultMessage: 'API AVAILABILITY',
        }),
        to: 'api_availability',
    },
];
const botDetectionSubMenuDetails = () => [];

const LeftMenu = (props) => {
    const { classes, intl } = props;
    return (
        <div className={classes.LeftMenu}>
            <LeftMenuItemPrimary
                text={intl.formatMessage({
                    id: 'leftmenu.primary.tasks',
                    defaultMessage: 'TASKS',
                })}
                secondaryMenuDetails={tasksSubMenuDetails(props)}
            />
            <LeftMenuItemPrimary
                text={intl.formatMessage({
                    id: 'leftmenu.primary.settings',
                    defaultMessage: 'SETTINGS',
                })}
                secondaryMenuDetails={settingsSubMenuDetails(props)}
            />
            <LeftMenuItemPrimary
                text={intl.formatMessage({
                    id: 'leftmenu.primary.microgateway',
                    defaultMessage: 'MICROGATEWAY',
                })}
                secondaryMenuDetails={microgatewaySubMenuDetails(props)}
            />
            <LeftMenuItemPrimary
                text={intl.formatMessage({
                    id: 'leftmenu.primary.categories',
                    defaultMessage: 'CATEGORIES',
                })}
                secondaryMenuDetails={categoriesSubMenuDetails(props)}
            />
            <LeftMenuItemPrimary
                text={intl.formatMessage({
                    id: 'leftmenu.primary.throttlingpolicies',
                    defaultMessage: 'THROTTLING POLICIES',
                })}
                secondaryMenuDetails={throttlingPoliciesSubMenuDetails(props)}
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
                secondaryMenuDetails={analyticsSubMenuDetails(props)}
            />
        </div>

    );
};

export default injectIntl(withStyles(styles)(LeftMenu));
