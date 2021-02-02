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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import MuiExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import { Link } from 'react-router-dom';
import Divider from '@material-ui/core/Divider';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import View from 'AppComponents/Apis/Details/Documents/View';
import Box from '@material-ui/core/Box';
import { app } from 'Settings';
import { ApiContext } from './ApiContext';
import Resources from './Resources';
import Operations from './Operations';
import Comments from './Comments/Comments';
import Sdk from './Sdk';
import OverviewDocuments from './OverviewDocuments';
import Environments from './Environments';

/**
 *
 *
 * @param {*} theme
 */
const styles = (theme) => ({
    root: {
        padding: theme.spacing(3),
        color: theme.palette.getContrastText(theme.palette.background.paper),
        margin: -1 * theme.spacing(0, 2),
    },
    iconClass: {
        marginRight: 10,
    },
    boxBadge: {
        background: theme.palette.grey.A400,
        fontSize: theme.typography.h5.fontSize,
        padding: theme.spacing(1),
        width: 30,
        height: 30,
        marginRight: 20,
        textAlign: 'center',
    },
    subscriptionBox: {
        paddingLeft: theme.spacing(2),
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
        '& button': {
            textTransform: 'capitalize !important',
        }
    },
    verticalSpace: {
        marginLeft: theme.spacing(60),
    },
    subheading: {
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    marginTop: {
        marginTop: theme.spacing(8),
    },
    subsToApp: {
        marginTop: theme.spacing(2),
    },
    expansionRoot: {
        minHeight: 238,
    },
    noCommentRoot: {
        backgroundImage: `url(${app.context + theme.custom.overviewPage.commentsBackground})`,
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
        backgroundImage: `url(${app.context + theme.custom.overviewPage.documentsBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 192,
    },
    noCredentialsRoot: {
        backgroundImage: `url(${app.context + theme.custom.overviewPage.credentialsBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 236,
    },
    emptyBox: {
        background: theme.custom.overview.noContentBackground,
        color: theme.palette.getContrastText(theme.custom.overview.noContentBackground),
        border: 'solid 1px #fff',
        padding: theme.spacing(2),
        '& span': {
            color: theme.palette.getContrastText(theme.custom.overview.noContentBackground),
        }
    },
    paper: {
        margin: theme.spacing(2),
        padding: theme.spacing(2),
    },
    paperWithDoc: {
        margin: theme.spacing(2),
        padding: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    heading: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    mutualsslMessage: {
        marginTop: theme.spacing(2),
    },
    sectionTitle: {
        padding: 5,
        paddingLeft: 16,
    },
    overviewPaper: {
        border: 'solid 1px #ccc',
        borderRadius: 5,
    },
    overviewContainerBox: {
        minHeight: 200,
        overflowY: 'auto',
    },
    overviewContainerBoxAction: {
        minHeight: 172,
    }
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
})((props) => <MuiExpansionPanelSummary {...props} />);

ExpansionPanelSummary.muiName = 'ExpansionPanelSummary';

/**
 * Handles the Overview page for APIs and API Products.
 * @param {*} props properties passed by parent element
 * @memberof Overview
 */
function Overview(props) {
    const { classes, theme } = props;
    const {
        custom: {
            apiDetailPages: {
                showCredentials, showComments, showTryout, showDocuments, showSdks,
            },
        },
    } = theme;
    const { api, subscribedApplications } = useContext(ApiContext);
    const [totalComments, setCount] = useState(0);
    const [totalDocuments, setDocsCount] = useState(0);
    const [overviewDocOverride, setOverviewDocOverride] = useState(null);
    const isOnlyMutualSSL = api.securityScheme.includes('mutualssl') && !api.securityScheme.includes('oauth2') &&
        !api.securityScheme.includes('api_key') && !api.securityScheme.includes('basic_auth');
    const isOnlyBasicAuth = api.securityScheme.includes('basic_auth') && !api.securityScheme.includes('oauth2') &&
        !api.securityScheme.includes('api_key');
    useEffect(() => {
        const restApi = new API();
        const promisedApi = restApi.getDocumentsByAPIId(api.id);
        promisedApi
            .then((response) => {
                const overviewDoc = response.body.list.filter((item) => item.otherTypeName === '_overview');
                if (overviewDoc.length > 0) {
                    // We can override the UI with this content
                    setOverviewDocOverride(overviewDoc[0]); // Only one doc we can render
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    Alert.error('Error occurred');
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
            <>
                <Paper className={classes.paperWithDoc}>
                    <View doc={overviewDocOverride} apiId={api.id} fullScreen />
                </Paper>
            </>
        );
    }
    const user = AuthManager.getUser();
    return (
        <Box m={2}>
            <Grid container spacing={3}>
                <Grid item lg={4} md={6} xs={12}>
                    <Paper elevation={0} className={classes.overviewPaper}>
                        <Typography variant='subtitle2' className={classes.sectionTitle}>
                            <FormattedMessage
                                id='Apis.Details.Overview.endpoints.title'
                                defaultMessage='Endpoints'
                            />
                        </Typography>
                        <Divider />
                        <Box p={2} className={classes.overviewContainerBox}>
                            <Environments />
                        </Box>
                    </Paper>
                </Grid>
                {/* Resources */}
                {api.type !== 'WS' && showTryout && (
                    <Grid item lg={4} md={6} xs={12}>
                        <Paper elevation={0} className={classes.overviewPaper}>
                            <Typography variant='subtitle2' className={classes.sectionTitle}>
                                {getTitleForAPIOperationType(api.type)}

                            </Typography>
                            <Divider />
                            <Box p={2} className={classes.overviewContainerBoxAction}>
                                {getResourcesForAPIs(api.type, api)}
                            </Box>
                            <Box>
                                {!api.advertiseInfo.advertised && (
                                    <>
                                        <Divider />
                                        <Link to={'/apis/' + api.id + '/test'} className={classes.button}>
                                            <Button
                                                id='test'
                                                size='small'
                                                color='primary'
                                                aria-labelledby='test APIOperationTitle'
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.resources.show.more'
                                                    defaultMessage='Try Out'
                                                />
                                            </Button>
                                        </Link>
                                    </>
                                )}
                            </Box>
                        </Paper>
                    </Grid>)}
                {(!api.advertiseInfo.advertised && showComments) && (<Grid item lg={4} md={6} xs={12}>
                    <Paper elevation={0} className={classes.overviewPaper}>
                        <Typography variant='subtitle2' className={classes.sectionTitle}>
                            <FormattedMessage
                                id='Apis.Details.Overview.comments.title'
                                defaultMessage='Comments'
                            />
                        </Typography>
                        <Divider />
                        <Box p={2} className={classes.overviewContainerBox}>
                            {api && (
                                <Comments apiId={api.id} showLatest isOverview setCount={setCount} />
                            )}
                            {totalComments === 0 && (
                                <div className={classes.emptyBox}>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.comments.no.content'
                                            defaultMessage='No Comments Yet'
                                        />
                                    </Typography>
                                </div>
                            )}
                        </Box>
                    </Paper>
                </Grid>)}

                {showDocuments && (<Grid item lg={4} md={6} xs={12}>
                    <Paper elevation={0} className={classes.overviewPaper}>
                        <Typography variant='subtitle2' className={classes.sectionTitle}>
                            <FormattedMessage
                                id='Apis.Details.Overview.documents.title'
                                defaultMessage='Documents'
                            />
                        </Typography>
                        <Divider />
                        <Box p={2} className={classes.overviewContainerBoxAction}>
                            <OverviewDocuments apiId={api.id} setDocsCount={setDocsCount} />
                        </Box>
                        <Box>
                            <Divider />
                            <Link to={'/apis/' + api.id + '/documents'} className={classes.button}>
                                <Button id='DMore' size='small' color='primary' aria-labelledby='DMore Documents'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.comments.show.more'
                                        defaultMessage='Show All Documents'
                                    />
                                </Button>
                            </Link>
                        </Box>
                    </Paper>
                </Grid>)}
                {api.type !== 'WS' && showSdks && (<Grid item lg={4} md={6} xs={12}>
                    <Paper elevation={0} className={classes.overviewPaper}>
                        <Typography variant='subtitle2' className={classes.sectionTitle}>
                            <FormattedMessage
                                id='Apis.Details.Overview.sdk.generation.title'
                                defaultMessage='Software Development Kits'
                            />
                        </Typography>
                        <Divider />
                        <Box p={2} className={classes.overviewContainerBoxAction} display='flex'>
                            {api && <Sdk apiId={api.id} onlyIcons />}
                        </Box>
                        <Box>
                            <Divider />
                            <Link to={'/apis/' + api.id + '/documents'} className={classes.button}>
                                <Button id='DMore' size='small' color='primary' aria-labelledby='DMore Documents'>
                                    <FormattedMessage
                                        id='Apis.Details.Overview.sdk.generation.show.more'
                                        defaultMessage='Show All SDKs'
                                    />
                                </Button>
                            </Link>
                        </Box>
                    </Paper>
                </Grid>)}
                {!api.advertiseInfo.advertised && showCredentials && (
                    <Grid item lg={4} md={6} xs={12}>
                        <Paper elevation={0} className={classes.overviewPaper}>
                            <Typography variant='subtitle2' className={classes.sectionTitle}>
                                <FormattedMessage
                                    id='Apis.Details.Overview.api.subscriptions'
                                    defaultMessage='Subscriptions'
                                />
                            </Typography>
                            <Divider />
                            <Box p={2} className={classes.overviewContainerBoxAction} display='flex'>

                                {api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped' ? (

                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.no.subscription.message'
                                            defaultMessage='Subscriptions Are Not Allowed'
                                        />
                                    </Typography>

                                ) : (
                                        <Box display='flex' flexDirection='column'>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.subscribe.info'
                                                    defaultMessage={
                                                        'Subscription enables you to receive access'
                                                        + ' tokens and be authenticated to invoke this API.'
                                                    }
                                                />
                                            </Typography>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id={'Apis.Details.Overview' +
                                                        '.subscribe.available'}
                                                    defaultMessage='Subscription tiers available '
                                                />
                                                {api.tiers.map((tier, index) => (<>
                                                    {tier.tierName}{index !== (api.tiers.length - 1)
                                                        ? (', ') : ' '
                                                    }
                                                </>))}
                                            </Typography>
                                            <Box display='block' mt={2}>
                                                <Grid item xs={12}>
                                                    {user ? (
                                                        <Box display='flex' flexDirection='column' mr={2}>
                                                            <Link
                                                                to={'/apis/' + api.id + '/credentials'}
                                                                style={
                                                                    !api.isSubscriptionAvailable ?
                                                                        { pointerEvents: 'none' } : null
                                                                }
                                                            >
                                                                <Button
                                                                    variant='contained'
                                                                    color='primary'
                                                                    size='large'
                                                                    disabled={!api.isSubscriptionAvailable || isOnlyMutualSSL ||
                                                                        isOnlyBasicAuth}
                                                                >
                                                                    <FormattedMessage
                                                                        id={'Apis.Details.Overview.subscribe' +
                                                                            'btn.link'}
                                                                        defaultMessage='Subscribe'
                                                                    />
                                                                </Button>
                                                            </Link>
                                                            {subscribedApplications && (<Typography variant='caption' component='div'>
                                                                {subscribedApplications.length === 0 ? (<FormattedMessage
                                                                    id='Apis.Details.Overview.subscribe.count.zero'
                                                                    defaultMessage={
                                                                        'No application subscriptions.'
                                                                    }
                                                                />) : (
                                                                        subscribedApplications.length
                                                                    )}
                                                                {(isOnlyMutualSSL || isOnlyBasicAuth) && (
                                                                    <Grid className={classes.mutualsslMessage}>
                                                                        <Typography variant='body2'>
                                                                            <FormattedMessage
                                                                                id='Apis.Details.Overview.mutualssl.basicauth'
                                                                                defaultMessage={'Subscription is not required for Mutual SSL APIs' +
                                                                                    ' or APIs with only Basic Authentication.'}
                                                                            />
                                                                        </Typography>
                                                                    </Grid>
                                                                )}
                                                                {' '}
                                                                {subscribedApplications.length === 1 && (<>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.Overview.subscribe.count.singular'
                                                                        defaultMessage={
                                                                            'Application subscribed.'
                                                                        }
                                                                    /></>)}
                                                                {subscribedApplications.length > 1 && (<>
                                                                    <FormattedMessage
                                                                        id='Apis.Details.Overview.subscribe.count.plural'
                                                                        defaultMessage={
                                                                            'Applications subscribed.'
                                                                        }
                                                                    /></>)}
                                                            </Typography>)}
                                                        </Box>
                                                    ) : (
                                                            <Box display='inline' mr={2}>
                                                                <a href={app.context + '/services/configs'}>
                                                                    <Button
                                                                        variant='contained'
                                                                        color='primary'
                                                                        size='large'
                                                                        disabled={!api.isSubscriptionAvailable}
                                                                    >
                                                                        <FormattedMessage
                                                                            id={'Apis.Details.Overview.signin' +
                                                                                '.subscribe.btn.link'}
                                                                            defaultMessage='Sign in to Subscribe'
                                                                        />
                                                                    </Button>
                                                                </a>
                                                            </Box>
                                                        )}
                                                </Grid>
                                            </Box>
                                        </Box>
                                    )}
                            </Box>
                            <Box>
                                <Divider />
                                <Link to={'/apis/' + api.id + '/credentials'} className={classes.button}>
                                    <Button id='DMore' size='small' color='primary' aria-labelledby='DMore Documents'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.sdk.generation.show.subscriptions'
                                            defaultMessage='Subscriptions'
                                        />
                                    </Button>
                                </Link>
                            </Box>
                        </Paper>
                    </Grid>)}


            </Grid>
        </Box>
    )
}

Overview.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    theme: PropTypes.instanceOf(Object).isRequired,
};

export default withStyles(styles, { withTheme: true })(Overview);
