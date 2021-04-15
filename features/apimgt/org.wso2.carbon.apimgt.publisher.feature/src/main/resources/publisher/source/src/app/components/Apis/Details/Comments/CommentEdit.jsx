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
import { withStyles } from '@material-ui/core/styles';
import { TextField, Button, Typography } from '@material-ui/core';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';

const styles = (theme) => ({
    textField: {
        marginTop: 0,
        width: '87.5%',
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(2),
        paddingTop: theme.spacing(1),
        marginTop: theme.spacing(2),
    },
    category: {
        width: '12%',
        marginRight: '0.5%',
    },
});

/**
 * Display a component to edit a comment
 * @class CommmentEdit
 * @extends {React.Component}
 */
class CommentEdit extends React.Component {
    /**
     * Creates an instance of CommentEdit
     * @param {*} props properies passed by the parent element
     * @memberof CommentEdit
     */
    constructor(props) {
        super(props);
        this.state = {
            commentText: '',
            category: '',
            currentLength: 0,
        };
        this.inputChange = this.inputChange.bind(this);
        this.handleCategoryChange = this.handleCategoryChange.bind(this);
        this.handleClickUpdateComment = this.handleClickUpdateComment.bind(this);
        this.handleClickCancel = this.handleClickCancel.bind(this);
        this.filterCommentToUpdate = this.filterCommentToUpdate.bind(this);
        this.filterCommentToUpdateReply = this.filterCommentToUpdateReply.bind(this);
    }

    /**
     * @memberof Comments
     */
    componentDidMount() {
        const { comment } = this.props;
        this.setState({
            commentText: comment.commentText,
            category: comment.category,
            currentLength: comment.commentText.length,
        });
    }

    /**
     * Hides the component to edit a comment when cancelled
     * @memberof CommentEdit
     */
    handleClickCancel() {
        const { toggleShowEdit, commentsUpdate, allComments } = this.props;
        toggleShowEdit();
        commentsUpdate(allComments);
    }

    /**
     * Handles category when the category is changed
     * @param {any} event Drop down select event
     * @memberof CommentEdit
     */
    handleCategoryChange(event) {
        this.setState({ category: event.target.value });
    }

    /**
     * Handles updating a comment
     * @memberof CommentEdit
     */
    handleClickUpdateComment() {
        const {
            api, comment, allComments, toggleShowEdit, commentsUpdate,
        } = this.props;
        const { category, commentText } = this.state;
        const Api = new API();
        const apiId = api.id;
        const commentToEdit = comment;
        commentToEdit.commentText = commentText.trim();
        commentToEdit.category = category;

        // to check whether a string does not contain only whitehis spaces
        if (comment.commentText.replace(/\s/g, '').length) {
            Api.updateComment(apiId, commentToEdit.commentId, commentToEdit)
                .then((result) => {
                    const updatedComment = result.body;
                    if (commentToEdit.parentCommentId === undefined) {
                        const index = allComments.findIndex(this.filterCommentToUpdate);
                        allComments[index].category = updatedComment.category;
                        allComments[index].commentText = updatedComment.commentText;
                    } else {
                        const index = allComments.findIndex(this.filterCommentToUpdateReply);
                        const replyIndex = allComments[index].replies.findIndex(this.filterCommentToUpdate);
                        allComments[index].replies[replyIndex] = updatedComment;
                    }
                    toggleShowEdit();
                    commentsUpdate(allComments);
                })
                .catch((error) => {
                    console.error(error);
                    if (error.response) {
                        Alert.error(error.response.body.message);
                    } else {
                        Alert.error('Something went wrong while adding the comment');
                    }
                });
        } else {
            Alert.error('You cannot enter a blank comment');
        }
    }

    /**
     * Filters the comment to update
     * @memberof CommentAdd
     */
    filterCommentToUpdate(commentToFilter) {
        const { comment } = this.props;
        return commentToFilter.commentId === comment.commentId;
    }

    /**
     * Filters the comment to update
     * @memberof CommentAdd
     */
    filterCommentToUpdateReply(commentToFilter) {
        const { comment } = this.props;
        return commentToFilter.commentId === comment.parentCommentId;
    }

    /**
     * Handles the comment text when input changes
     * @param {Object} {target} target element
     * @memberof CommentEdit
     */
    inputChange({ target }) {
        this.setState({ commentText: target.value, currentLength: target.value.length });
    }

    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof CommentEdit
     */
    render() {
        const { classes, theme } = this.props;
        const { category, commentText, currentLength } = this.state;
        return (
            <div>
                <FormControl className={classes.category}>
                    <Select value={category} onChange={this.handleCategoryChange}>
                        <MenuItem value='General'>General</MenuItem>
                        <MenuItem value='Feature Request'>Feature Request</MenuItem>
                        <MenuItem value='Bug Report'>Bug Report</MenuItem>
                    </Select>
                </FormControl>
                <TextField
                    id='multiline-static'
                    autoFocus
                    multiline
                    className={classes.textField}
                    margin='normal'
                    placeholder='Write a comment'
                    inputProps={{ maxLength: theme.custom.maxCommentLength }}
                    value={commentText}
                    onChange={this.inputChange}
                />
                <Typography className={classes.commentText} align='right'>
                    {currentLength + '/' + theme.custom.maxCommentLength}
                </Typography>
                <Grid container spacing={2}>
                    <Grid item>
                        <Button variant='contained' color='primary' onClick={() => this.handleClickUpdateComment()}>
                            Save
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button onClick={() => this.handleClickCancel()} className={classes.button}>
                            Cancel
                        </Button>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

CommentEdit.defaultProps = {
    commentsUpdate: null,
};

CommentEdit.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    api: PropTypes.instanceOf(Object).isRequired,
    allComments: PropTypes.instanceOf(Array).isRequired,
    // todo make commentsUpdate required once comment edit feature is supported
    commentsUpdate: PropTypes.func,
    toggleShowEdit: PropTypes.func.isRequired,
    comment: PropTypes.instanceOf(Object).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(CommentEdit);
