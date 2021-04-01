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
import PropTypes from 'prop-types';
import { Typography, Tooltip } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { injectIntl } from 'react-intl';
import classNames from 'classnames';
import Alert from 'AppComponents/Shared/Alert';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import CommentsAPI from 'AppData/Comments';
import CommentEdit from './CommentEdit';
import CommentOptions from './CommentOptions';
import CommentAdd from './CommentAdd';

dayjs.extend(relativeTime);

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
        marginTop: theme.spacing(4),
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
    }

    /**
     * Genereates unique keys for comments
     * @memberof Comment
     */
    getKey() {
        return this.keyCount++;
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
        const apiClient = new CommentsAPI();

        const { deleteComment } = this.state;
        const {
            apiId, allComments, commentsUpdate, intl,
        } = this.props;
        const commentIdOfCommentToDelete = deleteComment.id;
        const parentCommentIdOfCommentToDelete = deleteComment.parentCommentId;
        this.handleClose();

        apiClient
            .deleteComment(apiId, commentIdOfCommentToDelete)
            .then(() => {
                if (parentCommentIdOfCommentToDelete === null) {
                    const remainingComments = allComments.filter(this.filterRemainingComments);
                    commentsUpdate(remainingComments);
                    Alert.info('Comment has been successfully deleted');
                } else {
                    const index = allComments.findIndex(this.filterCommentToDelete);
                    const remainingReplies = allComments[index].replies.list.filter(this.filterRemainingComments);
                    allComments[index].replies.list = remainingReplies;
                    commentsUpdate(allComments);
                    Alert.info('Reply comment has been successfully deleted');
                }
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
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
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof Comment
     */
    render() {
        const {
            classes, comments, apiId, allComments, commentsUpdate, isOverview,
        } = this.props;

        const { editIndex, openDialog, replyId } = this.state;
        return (
            <>
                <div className={classes.paper}>
                    {comments
                        && comments
                            .slice(0)
                            .map((comment, index) => (
                                <div key={comment.id} className={classes.contentWrapper}>
                                    {index !== 0 && <Divider light className={classes.divider} />}
                                    <Grid
                                        md={8}
                                        container
                                        spacing={1}
                                        className={classNames({ [classes.root]: !isOverview })}
                                    >
                                        <Grid item>
                                            <Icon className={classes.commentIcon}>account_circle</Icon>
                                        </Grid>
                                        <Grid item xs zeroMinWidth>
                                            <Typography noWrap className={classes.commentText}>
                                                {(comment.commenterInfo && comment.commenterInfo.firstName)
                                                    ? (comment.commenterInfo.firstName + comment.commenterInfo.lastName)
                                                    : comment.createdBy}
                                            </Typography>
                                            <Tooltip title={comment.createdTime} aria-label={comment.createdTime}>
                                                <Typography noWrap className={classes.commentText} variant='caption'>
                                                    {dayjs(comment.createdTime).fromNow()}
                                                </Typography>
                                            </Tooltip>

                                            <Typography className={classes.commentText}>{comment.content}</Typography>

                                            <CommentOptions
                                                comment={comment}
                                                editIndex={editIndex}
                                                index={index}
                                                showAddComment={this.showAddComment}
                                                handleClickOpen={this.handleClickOpen}
                                                showEditComment={this.showEditComment}
                                            />

                                            {comment.id === replyId && (
                                                <Box ml={6} mb={2}>
                                                    <CommentAdd
                                                        apiId={apiId}
                                                        replyTo={comment.id}
                                                        allComments={allComments}
                                                        commentsUpdate={commentsUpdate}
                                                        handleShowReply={this.handleShowReply}
                                                        cancelButton
                                                    />
                                                </Box>
                                            )}

                                            {comment.replies && comment.replies.list.map((reply, commentIndex) => (
                                                <>
                                                    <Box ml={8}>
                                                        {commentIndex !== 0
                                                        && <Divider light className={classes.divider} />}
                                                        <Grid container spacing={1} className={classes.root}>
                                                            <Grid item>
                                                                <Icon className={classes.commentIcon}>
                                                                    account_circle
                                                                </Icon>
                                                            </Grid>
                                                            <Grid item xs zeroMinWidth>
                                                                <Typography noWrap className={classes.commentText}>
                                                                    {(reply.commenterInfo
                                                                    && reply.commenterInfo.fullName)
                                                                        ? reply.commenterInfo.fullName
                                                                        : reply.createdBy}
                                                                </Typography>
                                                                <Tooltip
                                                                    title={comment.createdTime}
                                                                    aria-label={comment.createdTime}
                                                                >
                                                                    <Typography
                                                                        noWrap
                                                                        className={classes.commentText}
                                                                        variant='caption'
                                                                    >
                                                                        {dayjs(reply.createdTime).fromNow()}
                                                                    </Typography>
                                                                </Tooltip>

                                                                {commentIndex !== editIndex && (
                                                                    <Typography className={classes.commentText}>
                                                                        {reply.content}
                                                                    </Typography>
                                                                )}

                                                                {commentIndex === editIndex && (
                                                                    <CommentEdit
                                                                        apiId={apiId}
                                                                        allComments={reply}
                                                                        commentsUpdate={commentsUpdate}
                                                                        comment={reply}
                                                                        toggleShowEdit={this.handleShowEdit}
                                                                    />
                                                                )}

                                                                <CommentOptions
                                                                    comment={reply}
                                                                    editIndex={editIndex}
                                                                    index={commentIndex}
                                                                    showAddComment={this.showAddComment}
                                                                    handleClickOpen={this.handleClickOpen}
                                                                    showEditComment={this.showEditComment}
                                                                />
                                                            </Grid>
                                                        </Grid>
                                                    </Box>
                                                </>
                                            ))}
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
    commentsUpdate: PropTypes.func.isRequired,
    comments: PropTypes.instanceOf(Array).isRequired,
    isOverview: PropTypes.bool,
};

export default injectIntl(withStyles(styles)(Comment));
