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
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { Typography } from '@material-ui/core';
import Grid from '@material-ui/core/Grid/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import classNames from 'classnames';
import CircularProgress from '@material-ui/core/CircularProgress';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Comment from './Comment';
import CommentAdd from './CommentAdd';
import API from '../../../../data/api';
import { ApiContext } from '../ApiContext';
import AuthManager from '../../../../data/AuthManager';

const styles = theme => ({
    root: {
        display: 'flex',
        alignItems: 'center',
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    paper: {
        marginRight: theme.spacing(3),
        paddingBottom: theme.spacing(3),
        paddingRight: theme.spacing(2),
        '& span, & h5, & label, & td, & li, & div, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    contentWrapper: {
        paddingLeft: theme.spacing(3),
        marginTop: theme.spacing(1),
        '& span, & h5, & label, & td, & li, & div, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    contentWrapperOverview: {
        padding: 0,
        width: '100%',
        boxShadow: 'none',
    },
    titleSub: {
        cursor: 'pointer',
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    link: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        cursor: 'pointer',
    },
    verticalSpace: {
        marginTop: theme.spacing(0.2),
    },
    loadMoreLink: {
        textDecoration: 'underline',
    },
    genericMessageWrapper: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
        marginRight: theme.spacing(3),
    },
    paperProgress: {
        padding: theme.spacing(3),
        marginTop: theme.spacing(2),
    },
});

/**
 * Display a comment list
 * @class Comments
 * @extends {React.Component}
 */
class Comments extends Component {
    static contextType = ApiContext;

    /**
     * Creates an instance of Comments
     * @param {*} props properies passed by the parent element
     * @memberof Comments
     */
    constructor(props) {
        super(props);
        this.state = {
            expanded: true,
            allComments: null,
            comments: [],
            totalComments: 0,
            startCommentsToDisplay: 0,
        };
        this.updateCommentList = this.updateCommentList.bind(this);
        this.handleExpandClick = this.handleExpandClick.bind(this);
        this.handleLoadMoreComments = this.handleLoadMoreComments.bind(this);
    }

    /**
     * Gets all the comments for a particular API, when component mounts
     * @memberof Comments
     */
    componentDidMount() {
        let {
            apiId, theme, match, intl, isOverview, setCount,
        } = this.props;
        if (match) apiId = match.params.apiUuid;

        const restApi = new API();

        restApi
            .getAllComments(apiId)
            .then((result) => {
                let commentList = result.body.list;
                if (isOverview) {
                    setCount(commentList.length);
                    if (commentList.length > 2) {
                        commentList = commentList.slice(commentList.length - 3, commentList.length);
                    }
                }
                this.setState({ allComments: commentList, totalComments: commentList.length });
                if (commentList.length < theme.custom.commentsLimit) {
                    this.setState({
                        startCommentsToDisplay: 0,
                        comments: commentList.slice(0, commentList.length),
                    });
                } else {
                    this.setState({
                        startCommentsToDisplay: commentList.length - theme.custom.commentsLimit,
                        comments: commentList.slice(
                            commentList.length - theme.custom.commentsLimit,
                            commentList.length,
                        ),
                    });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     * Handles loading the previous comments
     * @memberof Comments
     */
    handleLoadMoreComments() {
        const { totalComments, startCommentsToDisplay, allComments } = this.state;
        const { theme } = this.props;
        if (startCommentsToDisplay - theme.custom.commentsLimit <= 0) {
            this.setState({ startCommentsToDisplay: 0, comments: allComments.slice(0, totalComments) });
        } else {
            this.setState({
                startCommentsToDisplay: startCommentsToDisplay - theme.custom.commentsLimit,
                comments: allComments.slice(startCommentsToDisplay - theme.custom.commentsLimit, totalComments),
            });
        }
    }

    /**
     * Handles expanding the comment section
     * @memberof Comments
     */
    handleExpandClick() {
        const { expanded } = this.state;
        this.setState({ expanded: !expanded });
    }

    /**
     * Updates the comment list, This is passed through props to child component
     * @param {any} comments Updated comment list
     * @memberof Comments
     */
    updateCommentList(comments) {
        const { startCommentsToDisplay, totalComments } = this.state;
        const { theme } = this.props;
        let newStart;
        let difference;
        let newTotal;
        this.setState({ allComments: comments });
        if (totalComments < theme.custom.commentsLimit) {
            newTotal = comments.length;
            this.setState({ startCommentsToDisplay: 0, totalComments: newTotal, comments });
        } else if (totalComments <= comments.length) {
            difference = comments.length - totalComments;
            newStart = startCommentsToDisplay + difference;
            newTotal = comments.length;
            this.setState({
                startCommentsToDisplay: newStart,
                totalComments: newTotal,
                comments: comments.slice(newStart, newTotal),
            });
        } else {
            difference = totalComments - comments.length;
            if (startCommentsToDisplay === 0) {
                newStart = startCommentsToDisplay;
            } else {
                newStart = startCommentsToDisplay - difference;
            }
            newTotal = comments.length;
            this.setState({
                startCommentsToDisplay: newStart,
                totalComments: newTotal,
                comments: comments.slice(newStart, newTotal),
            });
        }
    }

    /**
     * Method to compare the tenant domains
     * @param {*} advertiseInfo advertiseInfo object for the API
     * @param {*} currentUser current logged in user
     * @returns {boolean} true or false
     */
    isCrossTenant(currentUser) {
        const { tenantDomain } = this.props;
        if(!tenantDomain) {
            return false;
        }
        let loggedInUserDomain = null;
        const loggedInUser = currentUser.name;

        if (loggedInUser.includes('@')) {
            const splitLoggedInUser = loggedInUser.split('@');
            loggedInUserDomain = splitLoggedInUser[splitLoggedInUser.length - 1];
        } else {
            loggedInUserDomain = 'carbon.super';
        }

        if (tenantDomain !== loggedInUserDomain) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof Comments
     */
    render() {
        const { classes, showLatest, isOverview } = this.props;
        const {
            comments, expanded, allComments, startCommentsToDisplay, totalComments, commentsUpdate,
        } = this.state;
        return (
            <ApiContext.Consumer>
                {({ api }) => (
                    <div
                        className={classNames(
                            { [classes.contentWrapper]: !isOverview },
                            { [classes.contentWrapperOverview]: isOverview },
                        )}
                    >
                        {!showLatest && (
                            <div className={classes.root}>
                                <Typography variant='h4' className={classes.titleSub}>
                                    <FormattedMessage id='Apis.Details.Comments.title' defaultMessage='Comments' />
                                </Typography>
                            </div>
                        )}
                        {!showLatest && AuthManager.getUser() &&
                        !this.isCrossTenant(AuthManager.getUser()) && (
                            <Paper className={classes.paper}>
                                <CommentAdd
                                    apiId={api.id}
                                    commentsUpdate={this.updateCommentList}
                                    allComments={allComments}
                                    parentCommentId={null}
                                    cancelButton
                                />
                            </Paper>
                        )}
                        {!allComments && (
                            <Paper className={classes.paperProgress}>
                                <CircularProgress size={24} />
                            </Paper>
                        )}
                        {allComments && totalComments === 0 && !isOverview &&
                            <div className={classes.genericMessageWrapper}>
                                <InlineMessage type='info' className={classes.dialogContainer}>
                                    <Typography variant='h5' component='h3'>
                                        <FormattedMessage
                                            id='Apis.Details.Comments.no.comments'
                                            defaultMessage='No Comments Yet'
                                        />
                                    </Typography>
                                    <Typography component='p'>
                                        <FormattedMessage
                                            id='Apis.Details.Comments.no.comments.content'
                                            defaultMessage='No comments available for this API yet'
                                        />
                                    </Typography>
                                </InlineMessage>
                            </div>
                        }
                        <Comment
                            comments={comments}
                            apiId={api.id}
                            commentsUpdate={this.updateCommentList}
                            allComments={allComments}
                            isOverview={isOverview}
                        />
                        {startCommentsToDisplay !== 0 && (
                            <div className={classes.contentWrapper}>
                                <Grid container spacing={4} className={classes.root}>
                                    <Grid item>
                                        <Typography className={classes.verticalSpace} variant='body1'>
                                            <a
                                                className={classes.link + ' ' + classes.loadMoreLink}
                                                onClick={this.handleLoadMoreComments}
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.Comments.load.previous.comments'
                                                    defaultMessage='Load Previous Comments'
                                                />
                                            </a>
                                        </Typography>
                                    </Grid>
                                    <Grid>
                                        <Icon
                                            onClick={this.handleLoadMoreComments}
                                            className={classes.link + ' ' + classes.verticalSpace}
                                        >
                                            arrow_drop_down
                                        </Icon>
                                    </Grid>
                                    <Grid item>
                                        <Typography className={classes.verticalSpace} variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.Comments.showing.comments'
                                                defaultMessage='Showing comments '
                                            />

                                            {totalComments - startCommentsToDisplay + ' of ' + totalComments}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </div>
                        )}
                    </div>
                )}
            </ApiContext.Consumer>
        );
    }
}

Comments.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
};

export default withSettings(injectIntl(withStyles(styles, { withTheme: true })(Comments)));
