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
import ArrowDropDownCircleOutlined from '@material-ui/icons/ArrowDropDownCircleOutlined';
import ArrowDropDown from '@material-ui/icons/ArrowDropDown';
import { Typography } from '@material-ui/core';
import Grid from '@material-ui/core/Grid/Grid';
import Alert from '../../../Shared/Alert';
import Comment from './Comment';
import CommentAdd from './CommentAdd';
import API from '../../../../data/api';
import { ApiContext } from '../ApiContext';

const styles = theme => ({
    root: {
        display: 'flex',
        alignItems: 'center',
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing.unit * 2,
        paddingTop: theme.spacing.unig,
    },
    titleSub: {
        cursor: 'pointer',
    },
    link: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        cursor: 'pointer',
    },
    verticalSpace: {
        marginTop: theme.spacing.unit * 0.2,
    },
    loadMoreLink: {
        textDecoration: 'underline',
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
            allComments: [],
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
        let { apiId, theme, match } = this.props;
        if( match ) apiId = match.params.api_uuid;
        const restApi = new API();
        restApi.getAllComments(apiId)
            .then((result) => {
                const commentList = result.body.list;
                this.setState({ allComments: commentList, totalComments: commentList.length });
                if (commentList.length < theme.custom.commentsLimit) {
                    this.setState({ startCommentsToDisplay: 0, comments: commentList.slice(0, commentList.length) });
                } else {
                    this.setState({ startCommentsToDisplay: commentList.length - theme.custom.commentsLimit, comments: commentList.slice(commentList.length - theme.custom.commentsLimit, commentList.length) });
                }
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
     * Handles loading the previous comments
     * @memberof Comments
     */
    handleLoadMoreComments() {
        const { totalComments, startCommentsToDisplay, allComments } = this.state;
        const { theme } = this.props;
        if (startCommentsToDisplay - theme.custom.commentsLimit <= 0) {
            this.setState({ startCommentsToDisplay: 0, comments: allComments.slice(0, totalComments) });
        } else {
            this.setState({ startCommentsToDisplay: startCommentsToDisplay - theme.custom.commentsLimit, comments: allComments.slice(startCommentsToDisplay - theme.custom.commentsLimit, totalComments) });
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
        let newStart; let difference; let
            newTotal;
        this.setState({ allComments: comments });
        if (totalComments < theme.custom.commentsLimit) {
            newTotal = comments.length;
            this.setState({ startCommentsToDisplay: 0, totalComments: newTotal, comments });
        } else if (totalComments <= comments.length) {
            difference = comments.length - totalComments;
            newStart = startCommentsToDisplay + difference;
            newTotal = comments.length;
            this.setState({ startCommentsToDisplay: newStart, totalComments: newTotal, comments: comments.slice(newStart, newTotal) });
        } else {
            difference = totalComments - comments.length;
            if (startCommentsToDisplay === 0) {
                newStart = startCommentsToDisplay;
            } else {
                newStart = startCommentsToDisplay - difference;
            }
            newTotal = comments.length;
            this.setState({ startCommentsToDisplay: newStart, totalComments: newTotal, comments: comments.slice(newStart, newTotal) });
        }
    }

    /**
   * Render method of the component
   * @returns {React.Component} Comment html component
   * @memberof Comments
   */
    render() {
        const { classes, showLatest } = this.props;
        const {
            comments, expanded, allComments, startCommentsToDisplay, totalComments,
        } = this.state;
        return (
            <ApiContext.Consumer>
            {({ api }) => (
            <div className={classes.contentWrapper}>
                {!showLatest && <div className={classes.root}>
                    <ArrowDropDownCircleOutlined
                        onClick={this.handleExpandClick}
                        aria-expanded={expanded}
                    />
                    <Typography
                        onClick={this.handleExpandClick}
                        variant='display1'
                        className={classes.titleSub}
                    >
                    Comments
                    </Typography>
                </div>}
                {!showLatest && <CommentAdd apiId={api.id} commentsUpdate={this.updateCommentList} allComments={allComments} parentCommentId={null} cancelButton={false} />}
                <Comment comments={comments} apiId={api.id} commentsUpdate={this.updateCommentList} allComments={allComments} />
                { startCommentsToDisplay !== 0
                        && (
                            <div className={classes.contentWrapper}>
                                <Grid container spacing={32} className={classes.root}>
                                    <Grid item>
                                        <Typography className={classes.verticalSpace} variant='body2'>
                                            <a className={classes.link + ' ' + classes.loadMoreLink} onClick={this.handleLoadMoreComments}>
                                                Load Previous Comments
                                            </a>
                                        </Typography>
                                    </Grid>
                                    <Grid>
                                        <ArrowDropDown
                                            onClick={this.handleLoadMoreComments}
                                            className={classes.link + ' ' + classes.verticalSpace}
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography className={classes.verticalSpace} variant='body2'>
                                            Showing comments
                                            {' '}
                                            { totalComments - startCommentsToDisplay + ' of ' + totalComments }
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </div>
                        )
                }
            </div>
            )}
            </ApiContext.Consumer>
        );
    }
}

Comments.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
};

export default withStyles(styles, { withTheme: true })(Comments);
