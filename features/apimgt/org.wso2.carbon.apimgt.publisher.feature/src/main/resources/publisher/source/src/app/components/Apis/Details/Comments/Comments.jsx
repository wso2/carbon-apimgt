/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Typography } from '@material-ui/core';
import CircularProgress from '@material-ui/core/CircularProgress';
import Grid from '@material-ui/core/Grid/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import CommentsAPI from 'AppData/Comments';
import Comment from './Comment';
import CommentAdd from './CommentAdd';

const styles = (theme) => ({
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
        textAlign: 'center',
    },
    dialogContainer: {
        width: 1000,
        padding: theme.spacing(2),
    },
});

/**
 * Display a comment list
 * @class Comments
 * @extends {React.Component}
 */
class Comments extends Component {
    /**
     * Creates an instance of Comments
     * @param {*} props properies passed by the parent element
     * @memberof Comments
     */
    constructor(props) {
        super(props);
        this.state = {
            expanded: true,
            allComments: [],
            comments: [],
            totalComments: 0,
            apiId: null,
            loading: true,
        };
        this.handleExpandClick = this.handleExpandClick.bind(this);
        this.handleLoadMoreComments = this.handleLoadMoreComments.bind(this);
        this.addComment = this.addComment.bind(this);
        this.updateComment = this.updateComment.bind(this);
        this.onDeleteComment = this.onDeleteComment.bind(this);
    }

    /**
     * Gets all the comments for a particular API, when component mounts
     * @memberof Comments
     */
    componentDidMount() {
        const { api, theme } = this.props;
        this.setState({ apiId: api.id });
        const limit = theme.custom.commentsLimit;
        const offset = 0;

        CommentsAPI.all(api.id, limit, offset)
            .then((result) => {
                const commentList = result.body.list;
                this.setState({
                    allComments: commentList,
                    comments: commentList,
                    totalComments: result.body.pagination.total,
                    loading: false,
                });
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error('Something went wrong while retrieving comments');
                }
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
            CommentsAPI
                .all(apiId, 1, remainingComments.length)
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
     * Add comment to the comment list
     * @param {any} comment added comment
     * @memberof Comments
     */
    addComment(comment) {
        const { totalComments, allComments } = this.state;
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
     * Handles loading the previous comments
     * @memberof Comments
     */
    handleLoadMoreComments() {
        const { allComments, comments } = this.state;
        const { theme, api: { id: apiId } } = this.props;
        const limit = theme.custom.commentsLimit;
        const offset = comments.length;

        CommentsAPI.all(apiId, limit, offset)
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
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof Comments
     */
    render() {
        const { classes, api } = this.props;
        const {
            comments, allComments, totalComments, loading,
        } = this.state;
        return (
            <div className={classes.contentWrapper}>
                <div className={classes.root}>
                    <Typography id='itest-api-details-comments-head' variant='h4' className={classes.titleSub}>
                        {totalComments > 0 ? totalComments + (' ') : ''}
                        <FormattedMessage id='Apis.Details.Comments.title' defaultMessage='Comments' />
                    </Typography>
                </div>

                <div className={classes.paper}>
                    <CommentAdd
                        api={api}
                        commentsUpdate={this.addComment}
                        addComment={this.addComment}
                        allComments={allComments}
                        replyTo={null}
                        cancelButton
                    />
                </div>

                {loading && (
                    <div className={classes.paperProgress}>
                        <CircularProgress size={24} />
                    </div>
                )}
                {!loading && totalComments === 0
                    && (
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
                    )}
                <Comment
                    comments={comments}
                    api={api}
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
                                        onKeyDown={this.handleLoadMoreComments}
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
        );
    }
}

Comments.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    api: PropTypes.instanceOf(Object).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Comments));
