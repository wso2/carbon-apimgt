/* eslint-disable react/prop-types */
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
import { injectIntl, FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import { Typography, Tooltip } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
dayjs.extend(relativeTime);

import classNames from 'classnames';
import Alert from 'AppComponents/Shared/Alert';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import API from 'AppData/api';
import CommentEdit from './CommentEdit';
import CommentOptions from './CommentOptions';
import CommentAdd from './CommentAdd';

const styles = (theme) => ({
    link: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        cursor: 'pointer',
    },
    commentIcon: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    commentText: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        marginTop: 0,
        width: '99%',
        whiteSpace: 'pre-wrap',
        overflowWrap: 'break-word',
        wordBreak: 'break-all',
    },
    root: {
        marginTop: theme.spacing(1),
    },
    contentWrapper: {
        paddingLeft: theme.spacing(2),
        paddingTop: theme.spacing(1),
    },
    contentWrapperOverview: {
        background: 'transparent',
        width: '100%',
    },
    divider: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
        width: '60%',
    },
    paper: {
        margin: 0,
        marginTop: theme.spacing(1),
        marginRight: theme.spacing(3),
        paddingBottom: theme.spacing(3),
    },
    cleanBack: {
        background: 'transparent',
        width: '100%',
        boxShadow: 'none',
    },
});

/**
 * Display a particular comment and details
 * @class Comment
 * @extends {React.Component}
 */
class Comment extends React.Component {
    /**
     * Creates an instance of Comment
     * @param {*} props properies passed by the parent element
     * @memberof Comment
     */
    constructor(props) {
        super(props);
        this.state = {
            openDialog: false,
            replyId: -1,
            editIndex: -1,
            deleteComment: null,
        };
        this.handleClickDeleteComment = this.handleClickDeleteComment.bind(this);
        this.handleShowEdit = this.handleShowEdit.bind(this);
        this.handleShowReply = this.handleShowReply.bind(this);
        this.handleClickOpen = this.handleClickOpen.bind(this);
        this.showAddComment = this.showAddComment.bind(this);
        this.showEditComment = this.showEditComment.bind(this);
        this.handleConfirmDialog = this.handleConfirmDialog.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.filterRemainingComments = this.filterRemainingComments.bind(this);
        this.filterCommentToDelete = this.filterCommentToDelete.bind(this);
        this.handleLoadMoreReplies = this.handleLoadMoreReplies.bind(this);
        this.handleAddReply = this.handleAddReply.bind(this);
        this.handleDeleteReply = this.handleDeleteReply.bind(this);
    }

    /**
     * Add two numbers.
     * @param {string} commentToFilter comment to filter.
     * @returns {boolean} filtering needed or not.
     */
    filterRemainingComments(commentToFilter) {
        const { deleteComment } = this.state;
        return commentToFilter.id !== deleteComment.id;
    }

    /**
     * Add two numbers.
     * @param {JSON} commentToFilter comment to filter.
     * @returns {string} id of the comment.
     */
    filterCommentToDelete(commentToFilter) {
        const { deleteComment } = this.state;
        return commentToFilter.id === deleteComment.parentCommentId;
    }

    /**
     * Shows the component to add a new comment
     * @param {any} id of comment
     * @memberof Comment
     */
    showAddComment(id) {
        this.setState({ replyId: id });
    }

    /**
     * Shows the component to edit a comment
     * @param {any} index Index of comment in the array
     * @memberof Comment
     */
    showEditComment(index) {
        const { editIndex } = this.state;
        if (editIndex === -1) {
            this.setState({ editIndex: index });
        }
    }

    /**
     * Hides the component to edit a comment
     * @param {any} index Index of comment in the array
     * @memberof Comment
     */
    handleShowEdit() {
        this.setState({ editIndex: -1 });
    }

    /**
     * Hides the component to add a new comment
     * @param {any} index Index of comment in the array
     * @memberof Comment
     */
    handleShowReply() {
        this.setState({ replyId: -1 });
    }

    /**
     * Shows the confimation dialog to delete a comment
     * @param {Object} comment Comment that has to be deleted
     * @memberof Comment
     */
    handleClickOpen(comment) {
        const { editIndex } = this.state;
        if (editIndex === -1) {
            this.setState({ deleteComment: comment, openDialog: true });
        }
    }

    /**
     * Hides the confimation dialog to delete a comment
     * @memberof Comment
     */
    handleClose() {
        this.setState({ openDialog: false });
    }

    /**
     * Handles the Confirm Dialog
     * @param {*} message properies passed by the Confirm Dialog
     * @memberof Comment
     */
    handleConfirmDialog(message) {
        if (message) {
            this.handleClickDeleteComment();
        } else {
            this.handleClose();
        }
    }

    /**
     * Handles deleting a comment
     * @memberof Comment
     */
    handleClickDeleteComment() {
        const apiClient = new API();

        const { deleteComment } = this.state;
        const {
            apiId, intl, onDeleteComment,
        } = this.props;
        const commentIdOfCommentToDelete = deleteComment.id;
        const parentCommentIdOfCommentToDelete = deleteComment.parentCommentId;
        this.handleClose();

        apiClient
            .deleteComment(apiId, commentIdOfCommentToDelete)
            .then(() => {
                if (parentCommentIdOfCommentToDelete === null) {
                    if (onDeleteComment) {
                        onDeleteComment(commentIdOfCommentToDelete);
                    }
                    Alert.info('Comment has been successfully deleted');
                } else {
                    this.handleDeleteReply(parentCommentIdOfCommentToDelete, commentIdOfCommentToDelete);
                    Alert.info('Reply comment has been successfully deleted');
                }
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                }
                else {
                    Alert.error(
                        intl.formatMessage({
                            defaultMessage: 'Something went wrong while deleting comment',
                            id: 'Apis.Details.Comments.Comment.something.went.wrong',
                        })
                        + ' - '
                        + commentIdOfCommentToDelete,
                    );
                }
            });
    }

    /**
     * Handles loading more comment replies
     * @param {Object} comment comment for which replies should be loaded
     * @memberof Comments
     */
    handleLoadMoreReplies(comment) {
        const { apiId, comments, updateComment } = this.props;
        const { id, replies: { count, list } } = comment;
        const restApi = new API();

        restApi
            .getAllCommentReplies(apiId, id, 3, count)
            .then((result) => {
                if (result.body) {
                    const { list: replyList, count: replyCount } = result.body;
                    const existingComment = comments.find((entry) => entry.id === id);

                    const newRepliesList = list.concat(replyList);
                    const newCount = count + replyCount;
                    const newLimit = newCount <= 3 ? 3 : newCount;

                    const updatedComment = {
                        ...existingComment,
                        replies: {
                            count: newCount,
                            list: newRepliesList,
                            pagination: { ...existingComment.replies.pagination, limit: newLimit },
                        },
                    };
                    if (updateComment) {
                        updateComment(updatedComment);
                    }
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     * Delete reply
     * @param {string} parentCommentId parent comment of reply
     * @param {string} replyCommentId deleted reply comment
     * @memberof Comments
     */
    handleDeleteReply(parentCommentId, replyCommentId) {
        const { comments, updateComment, apiId } = this.props;
        const existingComment = comments.find((item) => item.id === parentCommentId);
        const { replies } = existingComment;
        // updated values
        const updatedRepliesList = replies.list.filter((reply) => reply.id !== replyCommentId);
        const newTotal = replies.pagination.total - 1;
        const newLimit = replies.pagination.limit > newTotal ? newTotal : replies.pagination.limit;
        const newCount = replies.count - 1;

        if (newTotal > newCount) {
            const restApi = new API();
            restApi
                .getAllCommentReplies(apiId, parentCommentId, 1, newLimit - 1)
                .then((result) => {
                    if (result.body) {
                        const updatedComment = {
                            ...existingComment,
                            replies: {
                                ...replies,
                                list: [...updatedRepliesList, ...result.body.list],
                                pagination: {
                                    ...replies.pagination,
                                    total: newTotal,
                                },
                            },
                        };
                        if (updateComment) {
                            updateComment(updatedComment);
                        }
                    }
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                });
        } else {
            const updatedComment = {
                ...existingComment,
                replies: {
                    ...replies,
                    count: newCount,
                    list: updatedRepliesList,
                    pagination: {
                        ...replies.pagination,
                        limit: newLimit,
                        total: newTotal,
                    },
                },
            };
            if (updateComment) {
                updateComment(updatedComment);
            }
        }
    }

    /**
     * Add new reply
     * @param {Object} comment added reply comment
     * @memberof Comments
     */
    handleAddReply(comment) {
        const { comments, updateComment } = this.props;
        const { parentCommentId } = comment;
        const existingComment = comments.find((item) => item.id === parentCommentId);
        const { replies } = existingComment;
        const newCount = (replies.count || 0) + 1;
        const newLimit = newCount <= 3 ? 3 : newCount;
        const updatedComment = {
            ...existingComment,
            replies: {
                ...replies,
                count: newCount,
                list: [...replies.list, comment],
                pagination: {
                    ...replies.pagination,
                    limit: newLimit,
                    offset: replies.pagination.offset || 0,
                    total: replies.pagination.total + 1,
                },
            },
        };
        if (updateComment) {
            updateComment(updatedComment);
        }
    }

    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof Comment
     */
    render() {
        const {
            classes, comments, apiId, allComments, isOverview, crossTenentUser,
        } = this.props;

        const { editIndex, openDialog, replyId } = this.state;
        return (
            <>
                <div className={classNames({ [classes.paper]: !isOverview && comments.length > 0 }, { [classes.cleanBack]: isOverview })}>
                    {comments
                        && comments
                            .slice(0)
                            .map((comment, index) => (
                                <div
                                    // eslint-disable-next-line react/no-array-index-key
                                    key={comment.id + '-' + index}
                                    className={classNames(
                                        { [classes.contentWrapper]: !isOverview },
                                        { [classes.contentWrapperOverview]: isOverview },
                                    )}
                                >
                                    {index !== 0 && <Divider className={classes.divider} />}
                                    <Grid md={8} container spacing={1} className={classNames({ [classes.root]: !isOverview })}>
                                        <Grid item>
                                            <Icon className={classes.commentIcon}>account_circle</Icon>
                                        </Grid>
                                        <Grid item xs zeroMinWidth>
                                            <Typography noWrap className={classes.commentText}>
                                                {(comment.commenterInfo && comment.commenterInfo.firstName) ?
                                                    (comment.commenterInfo.firstName + comment.commenterInfo.lastName) :
                                                    comment.createdBy}
                                            </Typography>
                                            <Tooltip title={comment.createdTime} aria-label={comment.createdTime}>
                                                <Typography noWrap className={classes.commentText} variant='caption'>
                                                    {dayjs(comment.createdTime).fromNow()}
                                                </Typography>
                                            </Tooltip>

                                            <Typography className={classes.commentText}>{comment.content}</Typography>

                                            {!crossTenentUser && (
                                                <CommentOptions
                                                    comment={comment}
                                                    editIndex={editIndex}
                                                    index={index}
                                                    showAddComment={this.showAddComment}
                                                    handleClickOpen={this.handleClickOpen}
                                                    showEditComment={this.showEditComment}
                                                />
                                            )}

                                            {comment.id === replyId && (
                                                <Box ml={6} mb={2}>
                                                    <CommentAdd
                                                        apiId={apiId}
                                                        replyTo={comment.id}
                                                        allComments={allComments}
                                                        handleShowReply={this.handleShowReply}
                                                        cancelButton
                                                        addReply={this.handleAddReply}
                                                    />
                                                </Box>
                                            )}

                                            {comment.replies && comment.replies.list.map((reply, index) => (
                                                <>
                                                    <Box ml={8}>
                                                        {index !== 0 && <Divider className={classes.divider} />}
                                                        <Grid container spacing={1} className={classes.root}>
                                                            <Grid item>
                                                                <Icon className={classes.commentIcon}>account_circle</Icon>
                                                            </Grid>
                                                            <Grid item xs zeroMinWidth>
                                                                <Typography noWrap className={classes.commentText}>
                                                                    {(reply.commenterInfo && reply.commenterInfo.fullName)
                                                                        ? reply.commenterInfo.fullName : reply.createdBy}
                                                                </Typography>
                                                                <Tooltip title={comment.createdTime} aria-label={comment.createdTime}>
                                                                    <Typography noWrap className={classes.commentText} variant='caption'>
                                                                        {dayjs(reply.createdTime).fromNow()}
                                                                    </Typography>
                                                                </Tooltip>

                                                                {index !== editIndex && (
                                                                    <Typography className={classes.commentText}>
                                                                        {reply.content}</Typography>
                                                                )}

                                                                {index === editIndex && (
                                                                    <CommentEdit
                                                                        apiId={apiId}
                                                                        allComments={reply}
                                                                        comment={reply}
                                                                        toggleShowEdit={this.handleShowEdit}
                                                                    />
                                                                )}

                                                                {!crossTenentUser && (
                                                                    <CommentOptions
                                                                        comment={reply}
                                                                        editIndex={editIndex}
                                                                        index={index}
                                                                        showAddComment={this.showAddComment}
                                                                        handleClickOpen={this.handleClickOpen}
                                                                        showEditComment={this.showEditComment}
                                                                    />
                                                                )}
                                                            </Grid>
                                                        </Grid>
                                                    </Box>
                                                </>
                                            ))}
                                            {comment.replies && comment.replies.count < comment.replies.pagination.total
                                                && (
                                                    <div className={classes.contentWrapper}>
                                                        <Grid container spacing={4} className={classes.root}>
                                                            <Grid item>
                                                                <Typography className={classes.verticalSpace} variant='body1'>
                                                                    <a
                                                                        className={classes.link + ' ' + classes.loadMoreLink}
                                                                        onClick={() => this.handleLoadMoreReplies(comment)}
                                                                        onKeyDown={() => this.handleLoadMoreReplies(comment)}
                                                                    >
                                                                        <FormattedMessage
                                                                            id='Apis.Details.Comments.Comment.load.more.replies'
                                                                            defaultMessage='Show More Replies'
                                                                        />
                                                                    </a>
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item>
                                                                <Typography className={classes.verticalSpace} zvariant='body1'>
                                                                    {'(' + (comment.replies.count) + ' of '
                                                                        + comment.replies.pagination.total + ')'}
                                                                </Typography>
                                                            </Grid>
                                                        </Grid>
                                                    </div>
                                                )}
                                        </Grid>
                                    </Grid>
                                </div>
                            ))}
                </div>
                <ConfirmDialog
                    key='key-dialog'
                    labelCancel='Cancel'
                    title='Confirm Delete'
                    message='Are you sure you want to delete this comment?'
                    labelOk='Yes'
                    callback={this.handleConfirmDialog}
                    open={openDialog}
                />
            </>
        );
    }
}
Comment.defaultProps = {
    isOverview: false,
};
Comment.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    allComments: PropTypes.instanceOf(Array).isRequired,
    comments: PropTypes.instanceOf(Array).isRequired,
    isOverview: PropTypes.bool,
    crossTenentUser: PropTypes.bool.isRequired,
    updateComment: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(Comment));
