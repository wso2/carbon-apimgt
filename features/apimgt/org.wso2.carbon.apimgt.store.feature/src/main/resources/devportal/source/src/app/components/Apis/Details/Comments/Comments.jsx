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
import Paper from '@material-ui/core/Paper';
import { Typography } from '@material-ui/core';
import Grid from '@material-ui/core/Grid/Grid';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import { FormattedMessage, injectIntl } from 'react-intl';
import classNames from 'classnames';
import CircularProgress from '@material-ui/core/CircularProgress';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import AddCircleOutlineIcon from '@material-ui/icons/AddCircleOutline';
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
        textDecoration: 'none',
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
    button: {
        textTransform: 'capitalize',
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
            apiId: null,
            showCommentAdd: false,
        };
        this.handleExpandClick = this.handleExpandClick.bind(this);
        this.handleLoadMoreComments = this.handleLoadMoreComments.bind(this);
        this.toggleCommentAdd = this.toggleCommentAdd.bind(this);
        this.addComment = this.addComment.bind(this);
        this.updateComment = this.updateComment.bind(this);
        this.onDeleteComment = this.onDeleteComment.bind(this);
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
        this.setState({ apiId: apiId });

        const restApi = new API();
        const limit = theme.custom.commentsLimit;
        const offset = 0;

        restApi
            .getAllComments(apiId, limit, offset)
            .then((result) => {
                let commentList = result.body.list;
                if (isOverview) {
                    setCount(commentList.length);
                    if (commentList.length > 2) {
                        commentList = commentList.slice(commentList.length - 3, commentList.length);
                    }
                }
                this.setState({
                    allComments: commentList,
                    comments: commentList,
                    totalComments: result.body.pagination.total
                 });
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
        const { allComments, apiId, comments } = this.state;
        const { theme } = this.props;
        const restApi = new API();
        const limit = theme.custom.commentsLimit;
        const offset = comments.length;

        restApi
            .getAllComments(apiId, limit, offset)
            .then((result) => {
                const newAllCommentList = allComments.concat(result.body.list);
                this.setState({ allComments: newAllCommentList, comments: newAllCommentList });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
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
     * Add comment to the comment list
     * @param {any} comment added comment
     * @memberof Comments
     */
    addComment(comment) {
        const { totalComments, allComments } = this.state;
        const { theme: { custom: { commentsLimit } } } = this.props;
        const newTotal = totalComments + 1;

        this.setState({
            allComments: [comment, ...allComments],
            totalComments: newTotal,
            comments: [comment, ...allComments],
        });
    }

    /**
     * Update a specific comment in the comment list
     * @param {any} comment updated comment 
     * @memberof Comments
     */
    updateComment(comment) {
        const { comments } = this.state;

        const newComments = comments.reduce((acc, cur) => {
            let temp = cur;
            if (cur.id === comment.id) {
                temp = comment;
            }
            return [...acc, temp];
        }, []);
        this.setState({
            allComments: newComments,
            comments: newComments,
        });
    }

    /**
     * Delete a comment
     * @param {string} commentIdOfCommentToDelete id of deleted commetn
     * @memberof Comments
     */
    onDeleteComment(commentIdOfCommentToDelete) {
        const {
            apiId, comments, totalComments,
        } = this.state;

        const remainingComments = comments.filter((item) => item.id !== commentIdOfCommentToDelete);
        const newTotal = totalComments - 1;

        if (newTotal > remainingComments.length) {
            const restApi = new API();

            restApi
                .getAllComments(apiId, 1, remainingComments.length)
                .then((result) => {
                    if (result.body) {
                        this.setState({
                            totalComments: newTotal,
                            comments: [...remainingComments, ...result.body.list],
                            allComments: [...remainingComments, ...result.body.list],
                        });
                    }
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                });
        } else {
            this.setState({
                totalComments: newTotal,
                comments: remainingComments,
                allComments: remainingComments,
            });
        }
    }

    /**
     * Method to compare the tenant domains
     * @param {*} advertiseInfo advertiseInfo object for the API
     * @param {*} currentUser current logged in user
     * @returns {boolean} true or false
     */
    isCrossTenant(apiProvider, currentUser) {
        let tenantDomain = null;
        let loggedInUserDomain = null;
        const loggedInUser = currentUser.name;

        if (apiProvider.includes('@')) {
            const splitDomain = apiProvider.split('@');
            tenantDomain = splitDomain[splitDomain.length - 1];
        } else {
            tenantDomain = 'carbon.super';
        }

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

    toggleCommentAdd() {
        this.setState((prevState) => ({ showCommentAdd: !prevState.showCommentAdd }));
    }
    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof Comments
     */
    render() {
        const { classes, isOverview, } = this.props;
        const {
            comments, allComments, totalComments, showCommentAdd,
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
                        {!isOverview && (<div className={classes.root}>
                            <Typography variant='h4' component='h2' className={classes.titleSub}>
                                {totalComments + (' ')}
                                <FormattedMessage id='Apis.Details.Comments.title' defaultMessage='Comments' />
                            </Typography>
                        </div>)}

                        {AuthManager.getUser() &&
                            !this.isCrossTenant(api.provider, AuthManager.getUser()) && (
                                <Box mt={2} ml={1}>
                                    {!showCommentAdd && (<Button
                                        color="primary"
                                        size="small"
                                        className={classes.button}
                                        startIcon={<AddCircleOutlineIcon />}
                                        onClick={this.toggleCommentAdd}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Comments.write.a.new.comment'
                                            defaultMessage='Write a New Comment'
                                        />
                                    </Button>)}
                                    {showCommentAdd && (<CommentAdd
                                        apiId={api.id}
                                        commentsUpdate={this.addComment}
                                        addComment={this.addComment}
                                        allComments={allComments}
                                        replyTo={null}
                                        cancelCallback={this.toggleCommentAdd}
                                        cancelButton
                                    />)}
                                </Box>
                            )}
                        {!allComments && (
                            <Paper className={classes.paperProgress}>
                                <CircularProgress size={24} />
                            </Paper>
                        )}
                        {allComments && totalComments === 0 &&
                            <Box mt={2} mb={2} ml={1}>
                                <InlineMessage
                                    type='info'
                                    title={
                                        <FormattedMessage
                                            id='Apis.Details.Comments.no.comments'
                                            defaultMessage='No Comments Yet'
                                        />
                                    }
                                >
                                    <Typography component='p'>
                                        <FormattedMessage
                                            id='Apis.Details.Comments.no.comments.content'
                                            defaultMessage='No comments available for this API yet'
                                        />
                                    </Typography>
                                </InlineMessage>
                            </Box>
                        }
                        <Comment
                            comments={comments}
                            crossTenentUser={AuthManager.getUser() ? 
                                this.isCrossTenant(api.provider, AuthManager.getUser()) : null}
                            apiId={api.id}
                            allComments={allComments}
                            onDeleteComment={this.onDeleteComment}
                            updateComment={this.updateComment}
                        />
                        {totalComments > comments.length && (
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
                                                    defaultMessage='Show More'
                                                />
                                            </a>
                                        </Typography>
                                    </Grid>
                                    <Grid item>
                                        <Typography className={classes.verticalSpace} variant='body1'>
                                            {'(' + comments.length + ' of ' + totalComments + ')'}
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

Comments.defaultProps = {
    setCount: () => { },
};
Comments.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    setCount: PropTypes.func,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Comments));
