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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import MuiExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import { Link } from 'react-router-dom';
import Divider from '@material-ui/core/Divider';
import ExpansionPanelActions from '@material-ui/core/ExpansionPanelActions';
import Button from '@material-ui/core/Button';
import CustomIcon from '../../Shared/CustomIcon';
import { ApiContext } from './ApiContext';
import Resources from './Resources';
import Comments from './Comments/Comments';
import Sdk from './Sdk';
import API from '../../../data/api';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
        width: theme.custom.contentAreaWidth,
    },
    iconClass: {
        marginRight: 10,
        color: theme.palette.secondary.main,
    },
    boxBadge: {
        background: theme.palette.grey.A400,
        color: theme.palette.getContrastText(theme.palette.grey.A400),
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
        height: 158,
        overflow: 'auto',
    },
    sdkWrapper: {
        height: 192,
        overflow: 'auto',
    },
    actionPanel: {
        justifyContent: 'flex-start',
    },
    noCommentsBack: {
        background: 'url(' + theme.custom.overviewImage.comments + ') no-repeat left top',
        width: 'auto',
        height:192,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
    },
    noDocsBack: {
        background: 'url(' + theme.custom.overviewImage.docs + ') no-repeat left top',
        width: 'auto',
        height:192,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
    },
    noForumBack: {
        background: 'url(' + theme.custom.overviewImage.forum + ') no-repeat left top',
        width: 'auto',
        height:192,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
    },
    noContentText: {
        fontSize: '0.9rem'
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
})(props => <MuiExpansionPanelSummary {...props} />);

ExpansionPanelSummary.muiName = 'ExpansionPanelSummary';

class Overview extends React.Component {
    state = {
        value: 0,
        hasComments: false,
        hasDocs: false,
        hasForum: false,
    };

    /**
     *
     *
     * @memberof Overview
     */
    handleExpandClick = () => {
        //this.setState(state => ({ expanded: !state.expanded }));
    };

    /**
     *
     *
     * @memberof Overview
     */
    componentDidMount() {
        const apiId = this.props.match.params.api_uuid;
        const restApi = new API();
        restApi.getAllComments(apiId)
            .then((result) => {
                if( result.body.list > 0 ) this.setState({hasComments: true});
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error('Something went wrong while retrieving comments');
                }
            });
    };

    /**
     *
     *
     * @returns
     * @memberof Overview
     */
    render() {
        const { classes, theme } = this.props;
        const { hasComments, hasDocs, hasForum } = this.state;
        const apiId = this.props.match.params.api_uuid;
        return (
            <ApiContext.Consumer>
                {({ api, applicationsAvailable, subscribedApplications }) => (
                    <Grid container className={classes.root} spacing={16}>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='credentials' />

                                    <Typography className={classes.heading} variant='h6'>
                                        API Credentials
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>{subscribedApplications.length}</div>
                                                <Link to={'/apis/'+ apiId + '/credentials'} className={classes.linkStyle}>
                                                    {subscribedApplications.length === 1 ? 'Subscription' : 'Subscriptions'}
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>Subscribe to an Application</Typography>
                                            <div className={classes.subscriptionBox}>
                                                {applicationsAvailable.length > 0 && (
                                                <React.Fragment>
                                                    <Link to={{ pathname: '/apis/'+ apiId + '/credentials', state: {popup: 'openAvailable'}}} className={classes.linkStyle}>
                                                        With an Existing Application
                                                    </Link>
                                                    <Typography variant='caption'>{subscribedApplications.length} {subscribedApplications.length === 1 ? 'subscription' : 'subscriptions'}</Typography>
                                                </React.Fragment>
                                                )}
                                                <Link to={{ pathname: '/apis/'+ apiId + '/credentials', state: {popup: 'openNew'}}} className={classes.linkStyle}>
                                                    With a New Application
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography>API Credentials are grouped in to applications. An application is primarily used to decouple the consumer from the APIs. It allows you to Generate and use a single key for multiple APIs and subscribe multiple times to a single API with different SLA levels.</Typography>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='credentials' />

                                    <Typography className={classes.heading} variant='h6'>
                                        Resources
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.resourceWrapper}>{api && <Resources api={api} />}</ExpansionPanelDetails>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Link to={'/apis/'+ apiId + '/test'} className={classes.linkStyle}>
                                        <Button size='small' color='primary'>
                                            Test Resources >>
                                        </Button>
                                    </Link>
                                </ExpansionPanelActions>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='comments' />

                                    <Typography className={classes.heading} variant='h6'>
                                        Comments
                                    </Typography>
                                </ExpansionPanelSummary>
                                { hasComments && <ExpansionPanelDetails className={classes.resourceWrapper}>{ api &&  <Comments innerRef={node => (this.comments = node)} apiId={api.id} showLatest /> }</ExpansionPanelDetails> }
                                { !hasComments && <ExpansionPanelDetails className={classes.noCommentsBack}>
                                    <Typography variant="caption" className={classes.noContentText}>
                                            Use the comments feature to initiate conversations and share your opinions with other users.
                                    </Typography>
                                </ExpansionPanelDetails> }
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='sdk' />

                                    <Typography className={classes.heading} variant='h6'>
                                        SDK Generation
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.sdkWrapper}>
                                    <Grid container className={classes.root} spacing={16}>
                                        {api && <Sdk apiId={api.id} onlyIcons />}
                                        <Grid item xs={12}>
                                            <Typography>If you wants to create a software application to consume the subscribed APIs, you can generate client side SDK for a supported language/framework and use it as a start point to write the software application.</Typography>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='docs' />

                                    <Typography className={classes.heading} variant='h6'>
                                        Documents
                                    </Typography>
                                </ExpansionPanelSummary>
                                { hasDocs && <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>3</div>
                                                <Link to='/' className={classes.linkStyle}>
                                                    Documents
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>Last Updated</Typography>
                                            <div className={classes.subscriptionBox}>
                                                <Link to='/' className={classes.linkStyle}>
                                                    AboutmyApi.pdf
                                                </Link>
                                                <Typography variant='caption'>Last updated 21 minutes ago</Typography>
                                            </div>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails> }
                                { !hasDocs && <ExpansionPanelDetails className={classes.noDocsBack}>
                                    <Typography variant="caption"  className={classes.noContentText}>
                                            Documents are not available for this API.
                                    </Typography>
                                </ExpansionPanelDetails> }
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon strokeColor={theme.palette.secondary.main} className={classes.iconClass} width={24} height={24} icon='docs' />

                                    <Typography className={classes.heading} variant='h6'>
                                        Forum
                                    </Typography>
                                </ExpansionPanelSummary>
                                { hasForum && <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>1</div>
                                                <Link to='/' className={classes.linkStyle}>
                                                    Topic
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>Last Updated</Typography>
                                            <div className={classes.subscriptionBox}>
                                                <Link to='/' className={classes.linkStyle}>
                                                    Find pets fast
                                                </Link>
                                                <Typography variant='caption'>Last updated 2 days ago</Typography>
                                            </div>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails> }
                                { !hasForum && <ExpansionPanelDetails className={classes.noForumBack}>
                                    <Typography variant="caption"  className={classes.noContentText}>
                                        Use the forum feature to initiate conversations and share your opinions with other users.
                                    </Typography>
                                </ExpansionPanelDetails> }
                            </ExpansionPanel>
                        </Grid>
                    </Grid>
                )}
            </ApiContext.Consumer>
        );
    }
}

Overview.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Overview);
