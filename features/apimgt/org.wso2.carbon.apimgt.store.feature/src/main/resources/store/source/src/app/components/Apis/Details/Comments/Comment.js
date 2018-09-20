import React from "react";
import PropTypes from "prop-types";
import classnames from "classnames";
import { withStyles } from "@material-ui/core/styles";
import Collapse from "@material-ui/core/Collapse";
import DeleteOutlined from "@material-ui/icons/DeleteOutlined";
import { Typography } from "@material-ui/core";
import { ApiContext } from "../ApiContext";
import AccountBox from "@material-ui/icons/AccountBox";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import VerticalDivider from "../../../Shared/VerticalDivider";
import CommentAdd from './CommentAdd';

const styles = theme => ({
  link: {
    color: theme.palette.getContrastText(theme.palette.background.default),
    cursor: "pointer"
  },
  time: {
    color: theme.palette.getContrastText(theme.palette.background.default)
  },
  commentIcon: {
    color: theme.palette.getContrastText(theme.palette.background.default)
  },
  commentText: {
    color: theme.palette.getContrastText(theme.palette.background.default)
  },
  deleteIcon: {
    cursor: 'pointer',
    color: theme.palette.getContrastText(theme.palette.background.default),
    width: 20,
  },
  root: {
    marginTop: 20,
  }
});

class Comment extends React.Component {
  state = {
    value: 0,
    showReply: false,
    replyIndex: 0,
  };

  showAddComment (index) {
    if(index === this.state.replyIndex){
      this.setState(state => ({showReply: !state.showReply, replyIndex: index}));  
    } else {
      this.setState(state => ({showReply: !state.showReply, replyIndex: index}));  
    }
    
  }
  render() {
    const { classes, comments } = this.props;
    return (
      comments &&
      comments.map((comment, index) => (
        <Grid container spacing={24} className={classes.root}>
          <Grid item>
            <AccountBox className={classes.commentIcon} />
          </Grid>
          <Grid item xs zeroMinWidth>
            <Typography noWrap className={classes.commentText} variant="body2">
              {comment.name}
            </Typography>

            <Typography noWrap className={classes.commentText}>
              {comment.content}
            </Typography>
            <Grid container spacing={8}>
              <Grid item>
                <Typography component="a" className={classes.link} onClick={() => this.showAddComment(index)}>
                  REPLY
                </Typography>
              </Grid>
              <Grid item>
                <VerticalDivider height={15} />
              </Grid>
              <Grid item className={classes.time}>
                <Typography component="a" variant="caption" >
                    {comment.time}
                </Typography>
              </Grid>
              <Grid item className={classes.time}>
                {comment.user === "Erickandall" && <DeleteOutlined className={classes.deleteIcon} /> } 
                {/* TODO get the real loged in user and replace above. */}
              </Grid>
            </Grid>
            {(this.state.showReply && index === this.state.replyIndex) && <CommentAdd />}
            {comment.reply && <Comment {...this.props} comments={comment.reply} />}
          </Grid>
        </Grid>
      ))
    );
  }
}

Comment.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Comment);
