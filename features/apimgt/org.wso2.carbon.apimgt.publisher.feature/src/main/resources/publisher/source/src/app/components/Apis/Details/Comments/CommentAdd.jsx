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
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';

const styles = theme => ({
    commentIcon: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    commentText: {
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(2),
        paddingTop: theme.spacing.unig,
        marginTop: theme.spacing(2),
    },
    textField: {
        marginTop: 0,
        width: '88%',
    },
    category: {
        width: '12%',
    },
});

/**
   * Display a component to add a new comment
   * @class CommmentAdd
   * @extends {React.Component}
   */
class CommentAdd extends React.Component {
    /**
     * Creates an instance of CommentAdd
     * @param {*} props properies passed by the parent element
     * @memberof CommentAdd
     */
    constructor(props) {
        super(props);
        this.state = {
            commentText: '',
            category: 'General',
            currentLength: 0,
        };
        this.inputChange = this.inputChange.bind(this);
        this.handleClickAddComment = this.handleClickAddComment.bind(this);
        this.handleClickCancel = this.handleClickCancel.bind(this);
        this.handleCategoryChange = this.handleCategoryChange.bind(this);
        this.filterCommentToAddReply = this.filterCommentToAddReply.bind(this);
    }

    /**
     * Handles the comment text when input changes
     * @param {Object} {target} target element
     * @memberof CommentAdd
     */
    inputChange({ target }) {
        this.setState({ commentText: target.value, currentLength: target.value.length });
    }

    /**
     * Hides the component to add a new comment when cancelled
     * @memberof CommentAdd
     */
    handleClickCancel() {
        const { toggleShowReply } = this.props;
        toggleShowReply();
    }

    /**
     * Handles category when the category is changed
     * @param {any} event Drop down select event
     * @memberof CommentAdd
     */
    handleCategoryChange(event) {
        this.setState({ category: event.target.value });
    }

    /**
     * Filters the comment to add the reply
     * @memberof CommentAdd
     */
    filterCommentToAddReply(commentToFilter) {
        const { parentCommentId } = this.props;
        return commentToFilter.commentId === parentCommentId;
    }

    /**
     * Handles adding a new comment
     * @memberof CommentAdd
     */
    handleClickAddComment() {
        const {
            api, parentCommentId, allComments, toggleShowReply, commentsUpdate,
        } = this.props;
        const { category, commentText } = this.state;
        const Api = new API();
        const apiId = api.id;
        const comment = {
            apiId: api.id,
            category,
            commentText: commentText.trim(),
            parentCommentId,
        };

        // to check whether a string does not contain only white spaces
        if (comment.commentText.replace(/\s/g, '').length) {
            Api.addComment(apiId, comment)
                .then((newComment) => {
                    this.setState({ commentText: '', category: 'General' });
                    const addedComment = newComment.body;
                    if (parentCommentId === null) {
                        allComments.push(addedComment);
                    } else {
                        const index = allComments.findIndex(this.filterCommentToAddReply);
                        allComments[index].replies.push(addedComment);
                        toggleShowReply();
                    }
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
        this.setState({ currentLength: 0 });
    }

    /**
     * Render method of the component
     * @returns {React.Component} Comment html component
     * @memberof CommentAdd
     */
    render() {
        const { classes, cancelButton, theme } = this.props;
        const { category, commentText, currentLength } = this.state;
        return (
            <Grid container spacing={7} className={classes.contentWrapper}>

                <Grid item xs zeroMinWidth>
                    <FormControl className={classes.category}>
                        <Select
                            value={category}
                            onChange={this.handleCategoryChange}
                        >
                            <MenuItem value='General'>General</MenuItem>
                            <MenuItem value='Feature Request'>Feature Request</MenuItem>
                            <MenuItem value='Bug Report'>Bug Report</MenuItem>
                        </Select>
                    </FormControl>
                    <TextField
                        id='standard-multiline-flexible'
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
                        {currentLength + '/' + theme.custom.maxCommentLength }
                    </Typography>
                    <Grid container spacing={2}>
                        <Grid item>
                            <Button variant='contained' color='primary' onClick={() => this.handleClickAddComment()}>
                  Add Comment
                            </Button>
                        </Grid>
                        {cancelButton
                && (
                    <Grid item>
                        <Button onClick={() => this.handleClickCancel()} className={classes.button}>Cancel</Button>
                    </Grid>
                )
                        }
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

CommentAdd.defaultProps = {
    api: null,
    parentCommentId: null,
    toggleShowReply: null,
    commentsUpdate: null,
};

CommentAdd.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    cancelButton: PropTypes.bool.isRequired,
    api: PropTypes.instanceOf(Object),
    parentCommentId: PropTypes.string,
    toggleShowReply: PropTypes.func,
    commentsUpdate: PropTypes.func,
    allComments: PropTypes.instanceOf(Array).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(CommentAdd);
