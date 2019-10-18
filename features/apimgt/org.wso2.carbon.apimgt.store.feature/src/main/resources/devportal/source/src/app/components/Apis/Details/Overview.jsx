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
import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import MuiExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import { Link } from 'react-router-dom';
import Divider from '@material-ui/core/Divider';
import Paper from '@material-ui/core/Paper';
import ExpansionPanelActions from '@material-ui/core/ExpansionPanelActions';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import View from 'AppComponents/Apis/Details/Documents/View';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import { ApiContext } from './ApiContext';
import Resources from './Resources';
import Operations from './Operations';
import Comments from './Comments/Comments';
import Sdk from './Sdk';
import OverviewDocuments from './OverviewDocuments';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    iconClass: {
        marginRight: 10,
    },
    boxBadge: {
        background: theme.palette.grey.A400,
        fontSize: theme.typography.h5.fontSize,
        padding: theme.spacing.unit,
        width: 30,
        height: 30,
        marginRight: 20,
        textAlign: 'center',
    },
    subscriptionBox: {
        paddingLeft: theme.spacing.unit * 2,
    },
    linkStyle: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        fontSize: theme.typography.fontSize,
    },
    subscriptionTop: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    resourceWrapper: {
        height: 192,
        overflow: 'auto',
    },
    actionPanel: {
        justifyContent: 'flex-start',
    },
    linkToTest: {
        textDecoration: 'none',
    },
    button: {
        textDecoration: 'none',
    },
    verticalSpace: {
        marginLeft: theme.spacing.unit * 60,
    },
    subheading: {
        marginLeft: theme.spacing.unit * 2,
    },
    marginTop: {
        marginTop: theme.spacing(2),
    },
    subsToApp: {
        marginTop: theme.spacing(2),
    },
    subscribeButton: {
        marginLeft: theme.spacing(2),
    },
    expansionRoot: {
        minHeight: 238,
    },
    noCommentRoot: {
        backgroundImage: `url(${theme.custom.overviewPage.commentsBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 192,
    },
    commentRoot: {
        height: '100%',
        minHeight: 192,
    },
    noDocumentRoot: {
        backgroundImage: `url(${theme.custom.overviewPage.documentsBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 192,
    },
    noCredentialsRoot: {
        backgroundImage: `url(${theme.custom.overviewPage.credentialsBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 236,
    },
    emptyBox: {
        background: '#ffffff55',
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px #fff',
        padding: theme.spacing(2),
    },
    paper: {
        margin: theme.spacing(2),
        padding: theme.spacing(2),
    },
    heading: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
});
const ExpansionPanelSummary = withStyles({
    root: {
        borderBottom: '1px solid rgba(0,0,0,.125)',
        marginBottom: -1,
        minHeight: 56,
        '&$expanded': {
            minHeight: 56,
        },
    },
    content: {
        '&$expanded': {
            margin: '12px 0',
        },
        alignItems: 'center',
    },
    expanded: {},
})(props => <MuiExpansionPanelSummary {...props} />);

ExpansionPanelSummary.muiName = 'ExpansionPanelSummary';

/**
 * Handles the Overview page for APIs and API Products.
 * @param {*} props properties passed by parent element
 * @memberof Overview
 */
function Overview(props) {
    const { classes, theme } = props;
    const { api, applicationsAvailable, subscribedApplications } = useContext(ApiContext);
    const [totalComments, setCount] = useState(0);
    const [totalDocuments, setDocsCount] = useState(0);
    const [overviewDocOverride, setOverviewDocOverride] = useState(null);
    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(api.id);
        promisedApi
            .then((response) => {
                const overviewDoc = response.body.list.filter(item => item.otherTypeName === '_overview');
                if (overviewDoc.length > 0) {
                    // We can override the UI with this content
                    setOverviewDocOverride(overviewDoc[0]); // Only one doc we can render
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    Alert.error('Error occured');
                }
            });
    }, []);
    const getResourcesForAPIs = (apiType, apiObject) => {
        switch (apiType) {
            case 'GRAPHQL':
                return <Operations api={apiObject} />;
            case 'WS':
                return '';
            default:
                return <Resources api={apiObject} />;
        }
    };

    const getTitleForAPIOperationType = (apiType) => {
        switch (apiType) {
            case 'GRAPHQL':
                return <FormattedMessage id='Apis.Details.Overview.operations.title' defaultMessage='Operations' />;
            default:
                return <FormattedMessage id='Apis.Details.Overview.resources.title' defaultMessage='Resources' />;
        }
    };
    if (overviewDocOverride) {
        return (
            <React.Fragment>
                <Paper className={classes.paper}>
                    <View doc={overviewDocOverride} apiId={api.id} fullScreen />
                </Paper>
            </React.Fragment>
        );
    }
    const titleIconColor = theme.custom.overview.titleIconColor;
    const titleIconSize = theme.custom.overview.titleIconSize;
    const user = AuthManager.getUser();
    return (
        <Grid container className={classes.root} spacing={2}>
            {!api.advertiseInfo.advertised && (
                <Grid item xs={12} lg={6}>
                    <ExpansionPanel defaultExpanded>
                        <ExpansionPanelSummary>
                            <CustomIcon
                                strokeColor={titleIconColor}
                                className={classes.iconClass}
                                width={titleIconSize}
                                height={titleIconSize}
                                icon='credentials'
                            />
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='Apis.Details.Overview.api.credentials'
                                    defaultMessage='API Credentials'
                                />
                            </Typography>
                        </ExpansionPanelSummary>
                        {api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped' ? (
                            <ExpansionPanelDetails
                                classes={{
                                    root: classes.noCredentialsRoot,
                                }}
                            >
                                <Grid item xs={12}>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.subscribe.to.application'
                                            defaultMessage='Generate Credentials'
                                        />
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.credential.wizard.info.body'
                                            defaultMessage={
                                                'Use the Key Generation Wizard. '
                                                + 'Create a new application -> '
                                                + 'Subscribe -> Generate keys and '
                                                + 'Access Token to invoke this API.'
                                            }
                                        />
                                    </Typography>
                                    <Link to={`/apis/${api.id}/credentials/wizard`}>
                                        <Button variant='contained' color='primary' size='large'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.no.subscription.message'
                                                defaultMessage='No Subscriptions Allowed'
                                            />
                                        </Button>
                                    </Link>
                                </Grid>
                            </ExpansionPanelDetails>
                        ) : (
                            <ExpansionPanelDetails classes={{ root: classes.expansionRoot }}>
                                <Grid container className={classes.root} spacing={2}>
                                    <Grid item xs={12}>
                                        <Typography variant='subtitle2'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.subscribe.to.application'
                                                defaultMessage='Generate Credentials'
                                            />
                                        </Typography>
                                        <Typography variant='body2'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.credential.wizard.info'
                                                defaultMessage={
                                                    'Use the Key Generation Wizard. Create a new application '
                                                    + '-> Subscribe -> ' +
                                                    ' Generate keys and Access Token to invoke this API.'
                                                }
                                            />
                                        </Typography>
                                        {user && (
                                            <Link
                                                to={{
                                                    pathname: '/apis/' + api.id + '/credentials/wizard',
                                                }}
                                                style={!api.isSubscriptionAvailable ? { pointerEvents: 'none' } : null}
                                            >
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    size='large'
                                                    disabled={!api.isSubscriptionAvailable}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.credential.wizard.title'
                                                        defaultMessage='Key Generation Wizard'
                                                    />
                                                </Button>
                                            </Link>
                                        )}
                                        {applicationsAvailable && applicationsAvailable.length > 0 && (
                                            <React.Fragment>
                                                <Link
                                                    to={'/apis/' + api.id + '/credentials'}
                                                    style={!api.isSubscriptionAvailable ?
                                                        { pointerEvents: 'none' } : null}
                                                >
                                                    <Button
                                                        variant='contained'
                                                        color='primary'
                                                        size='large'
                                                        className={classes.subscribeButton}
                                                        disabled={!api.isSubscriptionAvailable}
                                                    >
                                                        <FormattedMessage
                                                            id='Apis.Details.Overview.subscribe.to.application.btn'
                                                            defaultMessage='Subscribe to an Application'
                                                        />
                                                    </Button>
                                                </Link>
                                            </React.Fragment>
                                        )}
                                    </Grid>
                                    <Grid item xs={12}>
                                        <Typography variant='subtitle2'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.view.credentials'
                                                defaultMessage='View Credentials'
                                            />
                                        </Typography>
                                        <Link to={'/apis/' + api.id + '/credentials'} className={classes.linkStyle}>
                                            <Typography variant='body2'>
                                                {subscribedApplications.length}{' '}
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.subscriptions'
                                                    defaultMessage='Subscriptions >>'
                                                />
                                            </Typography>
                                        </Link>
                                    </Grid>
                                </Grid>
                            </ExpansionPanelDetails>
                        )}
                    </ExpansionPanel>
                </Grid>
            )}
            {api.type !== 'WS' && (
                <Grid item xs={12} lg={6}>
                    <ExpansionPanel defaultExpanded>
                        <ExpansionPanelSummary>
                            <CustomIcon
                                strokeColor={titleIconColor}
                                className={classes.iconClass}
                                width={titleIconSize}
                                height={titleIconSize}
                                icon='credentials'
                            />
                            <Typography className={classes.heading} variant='h6'>
                                {getTitleForAPIOperationType(api.type)}
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails className={classes.resourceWrapper}>
                            {getResourcesForAPIs(api.type, api)}
                        </ExpansionPanelDetails>
                        {!api.advertiseInfo.advertised && (
                            <React.Fragment>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Link to={'/apis/' + api.id + '/test'} className={classes.linkToTest}>
                                        <Button size='small' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.resources.show.more'
                                                defaultMessage='Test >>'
                                            />
                                        </Button>
                                    </Link>
                                </ExpansionPanelActions>
                            </React.Fragment>
                        )}
                    </ExpansionPanel>
                </Grid>
            )}
            {!api.advertiseInfo.advertised && (
                <React.Fragment>
                    <Grid item xs={12} lg={6}>
                        <ExpansionPanel defaultExpanded>
                            <ExpansionPanelSummary>
                                <CustomIcon
                                    strokeColor={titleIconColor}
                                    className={classes.iconClass}
                                    width={titleIconSize}
                                    height={titleIconSize}
                                    icon='comments'
                                />
                                <Typography className={classes.heading} variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.comments.title'
                                        defaultMessage='Comments'
                                    />
                                </Typography>
                                <Typography className={classes.subheading}>
                                    {' ' + (totalComments > 3 ? 3 : totalComments) + ' of ' + totalComments}
                                </Typography>
                            </ExpansionPanelSummary>
                            <ExpansionPanelDetails
                                classes={{
                                    root: classNames(
                                        { [classes.noCommentRoot]: totalComments === 0 },
                                        { [classes.commentRoot]: totalComments !== 0 },
                                    ),
                                }}
                            >
                                <Grid container className={classes.root} spacing={2}>
                                    {api &&
                                        <Grid item xs={12}>
                                            <Comments apiId={api.id} showLatest isOverview setCount={setCount} />
                                        </Grid>
                                    }
                                    {totalComments === 0 &&
                                        <Grid item xs={12}>
                                            <div className={classes.emptyBox}>
                                                <Typography variant='body2'>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.comments.no.content'
                                                        defaultMessage='No Comments Yet'
                                                    />
                                                </Typography>
                                            </div>
                                        </Grid>
                                    }
                                </Grid>
                            </ExpansionPanelDetails>
                            <Divider />
                            <ExpansionPanelActions className={classes.actionPanel}>
                                <Link to={'/apis/' + api.id + '/comments'} className={classes.button}>
                                    <Button size='small' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.comments.show.more'
                                            defaultMessage='Show More >>'
                                        />
                                    </Button>
                                </Link>
                            </ExpansionPanelActions>
                        </ExpansionPanel>
                    </Grid>
                    {api.type !== 'WS' && (
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={titleIconColor}
                                        className={classes.iconClass}
                                        width={titleIconSize}
                                        height={titleIconSize}
                                        icon='sdk'
                                    />
                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.sdk.generation.title'
                                            defaultMessage='SDK Generation'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.resourceWrapper}>
                                    <Grid container className={classes.root} spacing={2}>
                                        {api && <Sdk apiId={api.id} onlyIcons />}
                                        <Grid item xs={12}>
                                            <Typography>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.sdk.generation.description'
                                                    defaultMessage={`If you want to create a software application
                                                     to consume the subscribed APIs, you can generate client side
                                                      SDK for a supported language/framework and use it as a start
                                                       point to write the software application.`}
                                                />
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Link to={'/apis/' + api.id + '/sdk'} className={classes.linkToTest}>
                                        <Button size='small' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Details.Overview.sdk.generation.show.more'
                                                defaultMessage='Show More >>'
                                            />
                                        </Button>
                                    </Link>
                                </ExpansionPanelActions>
                            </ExpansionPanel>
                        </Grid>
                    )}
                </React.Fragment>
            )}
            <Grid item xs={12} lg={6}>
                <ExpansionPanel defaultExpanded>
                    <ExpansionPanelSummary>
                        <CustomIcon
                            strokeColor={titleIconColor}
                            className={classes.iconClass}
                            width={titleIconSize}
                            height={titleIconSize}
                            icon='docs'
                        />

                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage id='Apis.Details.Overview.documents.title' defaultMessage='Documents' />
                        </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails
                        classes={{ root: classNames({ [classes.noDocumentRoot]: totalDocuments === 0 }) }}
                    >
                        <Grid container className={classes.root} spacing={2}>
                            <OverviewDocuments apiId={api.id} setDocsCount={setDocsCount} />
                        </Grid>
                    </ExpansionPanelDetails>
                    <Divider />
                    <ExpansionPanelActions className={classes.actionPanel}>
                        <Link to={'/apis/' + api.id + '/docs'} className={classes.button}>
                            <Button size='small' color='primary'>
                                <FormattedMessage
                                    id='Apis.Details.Overview.comments.show.more'
                                    defaultMessage='Show More >>'
                                />
                            </Button>
                        </Link>
                    </ExpansionPanelActions>
                </ExpansionPanel>
            </Grid>
        </Grid>
    );
}

Overview.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    theme: PropTypes.instanceOf(Object).isRequired,
};

export default withStyles(styles, { withTheme: true })(Overview);
