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
import { FormattedMessage } from 'react-intl';
import CustomIcon from '../../Shared/CustomIcon';
import { ApiContext } from './ApiContext';
import Resources from './Resources';
import Comments from './Comments/Comments';
import Sdk from './Sdk';
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
        height: 192,
        overflow: 'auto',
    },
    actionPanel: {
        justifyContent: 'flex-start',
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

class Overview extends React.Component {
    state = {
        value: 0,
    };

    /**
     *
     *
     * @memberof Overview
     */
    handleExpandClick = () => {
        this.setState(state => ({ expanded: !state.expanded }));
    };

    /**
     *
     *
     * @returns
     * @memberof Overview
     */
    render() {
        const { classes, theme } = this.props;
        return (
            <ApiContext.Consumer>
                {({ api, applicationsAvailable, subscribedApplications }) => (
                    <Grid container className={classes.root} spacing={16}>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
                                        icon='credentials'
                                    />

                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.api.credentials'
                                            defaultMessage='API Credentials'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>2</div>
                                                <Link to='/' className={classes.linkStyle}>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.subscriptions'
                                                        defaultMessage='Subscriptions'
                                                    />
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.subscribe.to.application'
                                                    defaultMessage='Subscribe to an Application'
                                                />
                                            </Typography>
                                            <div className={classes.subscriptionBox}>
                                                <Link to='/' className={classes.linkStyle}>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.with.an.existing'
                                                        defaultMessage='With an Existing Application'
                                                    />
                                                </Link>
                                                <Typography variant='caption'>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.subscribe.to.an.application'
                                                        defaultMessage='Subscribe to an Application'
                                                    />
                                                </Typography>
                                                <Link to='/' className={classes.linkStyle}>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.with.a.new.application'
                                                        defaultMessage='With a New Application'
                                                    />
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.with.a.new.application.help'
                                                    defaultMessage='API Credentials are grouped in to applications. An application is primarily used to decouple the consumer from the APIs. It allows you to Generate and use a single key for multiple APIs and subscribe multiple times to a single API with different SLA levels.'
                                                />
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
                                        icon='credentials'
                                    />

                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.resources.title'
                                            defaultMessage='Resources'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.resourceWrapper}>
                                    {api && <Resources api={api} />}
                                </ExpansionPanelDetails>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Button size='small' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.resources.show.more'
                                            defaultMessage='Show More >>'
                                        />
                                    </Button>
                                </ExpansionPanelActions>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
                                        icon='comments'
                                    />

                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.comments.title'
                                            defaultMessage='Comments'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails className={classes.resourceWrapper}>
                                    {api && <Comments apiId={api.id} showLatest />}
                                </ExpansionPanelDetails>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Button size='small' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.comments.show.more'
                                            defaultMessage='Show More >>'
                                        />
                                    </Button>
                                </ExpansionPanelActions>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
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
                                    <Grid container className={classes.root} spacing={16}>
                                        {api && <Sdk apiId={api.id} onlyIcons />}
                                        <Grid item xs={12}>
                                            <Typography>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.sdk.generation.description'
                                                    defaultMessage='If you wants to create a software application to consume the subscribed APIs, you can generate client side SDK for a supported language/framework and use it as a start point to write the software application.'
                                                />
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                                <Divider />
                                <ExpansionPanelActions className={classes.actionPanel}>
                                    <Button size='small' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.sdk.generation.show.more'
                                            defaultMessage='Show More >>'
                                        />
                                    </Button>
                                </ExpansionPanelActions>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
                                        icon='docs'
                                    />

                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.documents.title'
                                            defaultMessage='Documents'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>3</div>
                                                <Link to='/' className={classes.linkStyle}>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.documents.count.sufix'
                                                        defaultMessage='Documents'
                                                    />
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.documents.last.updated'
                                                    defaultMessage='Last Updated'
                                                />
                                            </Typography>
                                            <div className={classes.subscriptionBox}>
                                                <Link to='/' className={classes.linkStyle}>
                                                    AboutmyApi.pdf
                                                </Link>
                                                <Typography variant='caption'>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.documents.last.updated'
                                                        defaultMessage='Last Updated'
                                                    />
                                                    21 minutes ago
                                                </Typography>
                                            </div>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </Grid>
                        <Grid item xs={6}>
                            <ExpansionPanel defaultExpanded>
                                <ExpansionPanelSummary>
                                    <CustomIcon
                                        strokeColor={theme.palette.secondary.main}
                                        className={classes.iconClass}
                                        width={24}
                                        height={24}
                                        icon='docs'
                                    />

                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.forum.title'
                                            defaultMessage='Forum'
                                        />
                                    </Typography>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container className={classes.root} spacing={16}>
                                        <Grid item xs={12}>
                                            <div className={classes.subscriptionTop}>
                                                <div className={classes.boxBadge}>1</div>
                                                <Link to='/' className={classes.linkStyle}>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.forum.number.count'
                                                        defaultMessage='Topic'
                                                    />
                                                </Link>
                                            </div>
                                        </Grid>
                                        <Grid item xs={12}>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Overview.forum.last.updated'
                                                    defaultMessage='Last Updated'
                                                />
                                            </Typography>
                                            <div className={classes.subscriptionBox}>
                                                <Link to='/' className={classes.linkStyle}>
                                                    Find pets fast
                                                </Link>
                                                <Typography variant='caption'>
                                                    <FormattedMessage
                                                        id='Apis.Details.Overview.forum.last.updated'
                                                        defaultMessage='Last Updated'
                                                    />
                                                    2 days ago
                                                </Typography>
                                            </div>
                                        </Grid>
                                    </Grid>
                                </ExpansionPanelDetails>
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
