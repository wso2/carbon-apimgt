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
import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid/Grid';
import { Typography } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import AuthManager from 'AppData/AuthManager';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';

const styles = theme => ({
    link: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        cursor: 'pointer',
    },
    time: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        marginTop: theme.spacing(0.3),
    },
    verticalSpace: {
        marginTop: theme.spacing(0.2),
    },
    disable: {
        color: theme.custom.disableColor,
    },
    commentIcon: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    commentText: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        marginTop: theme.spacing.unig,
        width: '100%',
        whiteSpace: 'pre-wrap',
        overflowWrap: 'break-word',
    },
    root: {
        marginTop: theme.spacing(2.5),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(2),
        paddingTop: theme.spacing.unig,
    },
});

/**
 * Component to display options of the comment
 * @class CommentOptions
 * @extends {React.Component}
 */
class CommentOptions extends React.Component {
    /**
     * Creates an instance of CommentAdd
     * @param {*} props properies passed by the parent element
     * @memberof CommentAdd
     */
    constructor(props) {
        super(props);
        this.state = {};
        this.showAddComment = this.showAddComment.bind(this);
        this.showEditComment = this.showEditComment.bind(this);
        this.handleClickOpen = this.handleClickOpen.bind(this);
    }

    /**
     * Shows the component to add a new comment
     * @param {any} index Index of comment in the array
     * @memberof CommentOptions
     */
    showAddComment(index) {
        const { editIndex, showAddComment } = this.props;
        if (editIndex === -1) {
            showAddComment(index);
        }
    }

    /**
     * Shows the component to edit a comment
     * @param {any} index Index of comment in the array
     * @memberof Comment
     */
    showEditComment(index) {
        const { editIndex, showEditComment } = this.props;
        if (editIndex === -1) {
            showEditComment(index);
        }
    }

    /**
     * Shows the confimation dialog to delete a comment
     * @param {Object} comment Comment that has to be deleted
     * @memberof Comment
     */
    handleClickOpen(comment) {
        const { editIndex, handleClickOpen } = this.props;
        if (editIndex === -1) {
            handleClickOpen(comment);
        }
    }

    /**
     * Returns the date and time in a particular format
     * @param {any} timestamp Timestamp that has to be formatted
     * @memberof CommentOptions
     */
    displayDate(timestamp) {
        const localDate = new Date(timestamp);
        const localDateString = localDate.toLocaleDateString(undefined, {
            day: 'numeric',
            month: 'short',
            year: 'numeric',
        });
        const localTimeString = localDate.toLocaleTimeString(undefined, {
            hour: '2-digit',
            minute: '2-digit',
        });

        return localDateString + ' ' + localTimeString;
    }

    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof CommentOptions
     */
    render() {
        const {
            classes, comment, editIndex, index, theme,
        } = this.props;
        const canDelete =
            comment.createdBy === AuthManager.getUser().name || AuthManager.getUser().name === theme.custom.adminRole;
        const canModify = comment.createdBy === AuthManager.getUser().name && comment.entryPoint === 'APIPublisher';
        return (
            <Grid container spacing={2} className={classes.verticalSpace} key={comment.commentId}>
                {comment.parentCommentId == null && [
                    <Grid item key='key-reply'>
                        <Typography
                            component='a'
                            className={editIndex === -1 ? classes.link : classes.disable}
                            onClick={() => this.showAddComment(index)}
                        >
                            Reply
                        </Typography>
                    </Grid>,
                    <Grid item key='key-reply-vertical-divider'>
                        <VerticalDivider height={15} />
                    </Grid>,
                ]}

                {/* only the comment owner or admin can delete a comment */}
                {canDelete && [
                    <Grid item key='key-delete'>
                        <Typography
                            component='a'
                            className={editIndex === -1 ? classes.link : classes.disable}
                            onClick={() => this.handleClickOpen(comment)}
                        >
                            Delete
                        </Typography>
                    </Grid>,
                    <Grid item key='key-delete-vertical-divider'>
                        <VerticalDivider height={15} />
                    </Grid>,
                ]}

                {/* only the comment owner can modify the comment from the exact entry point */}
                {canModify && [
                    <Grid item key='key-edit'>
                        <Typography
                            component='a'
                            className={editIndex === -1 ? classes.link : classes.disable}
                            onClick={() => this.showEditComment(index)}
                        >
                            Edit
                        </Typography>
                    </Grid>,
                    <Grid item key='key-edit-verical-divider'>
                        <VerticalDivider height={15} />
                    </Grid>,
                ]}
                <Grid item className={classes.time}>
                    <Typography component='a' variant='caption'>
                        {this.displayDate(comment.createdTime)}
                    </Typography>
                </Grid>

                {editIndex === index
                    ? null
                    : [
                        <Grid item key='key-category-vertical-divider'>
                            <VerticalDivider height={15} />
                        </Grid>,
                        <Grid item className={classes.time} key='key-category'>
                            <Typography component='a' variant='caption'>
                                {comment.category}
                            </Typography>
                        </Grid>,
                    ]}
            </Grid>
        );
    }
}

CommentOptions.defaultProps = {
    showAddComment: null,
};

CommentOptions.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    editIndex: PropTypes.number.isRequired,
    index: PropTypes.number.isRequired,
    comment: PropTypes.instanceOf(Object).isRequired,
    handleClickOpen: PropTypes.func.isRequired,
    showEditComment: PropTypes.func.isRequired,
    showAddComment: PropTypes.func,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(CommentOptions);
