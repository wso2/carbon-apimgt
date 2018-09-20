import React from "react";
import PropTypes from "prop-types";
import classnames from "classnames";
import { withStyles } from "@material-ui/core/styles";
import Collapse from "@material-ui/core/Collapse";
import ArrowDropDownCircleOutlined from "@material-ui/icons/ArrowDropDownCircleOutlined";
import { Typography } from "@material-ui/core";
import { ApiContext } from "../ApiContext";
import AccountBox from "@material-ui/icons/AccountBox";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import VerticalDivider from "../../../Shared/VerticalDivider";
import Comment from "./Comment";
import CommentAdd from "./CommentAdd";
const dummyComments = [
    {
      user: "Erickandall",
      content:
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus venenatis sem quis nunc ultricies vestibulum. Nullam ut massa non libero laoreet facilisis eget sed lorem. Vivamus sed",
      time: "March 4 at 5.42 a.m",
      reply: [
        {
          user: "Donglas Purdy",
          content:
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus venenatis sem quis nunc ultricies vestibulum. Nullam ut massa non libero laoreet facilisis eget sed lorem. Vivamus sed",
          time: "March 5 at 5.42 a.m"
        }
      ]
    },
    {
      user: "Erickandall",
      content:
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus venenatis sem quis nunc ultricies vestibulum. Nullam ut massa non libero laoreet facilisis eget sed lorem. Vivamus sed",
      time: "March 4 at 5.42 a.m",
      reply: []
    }
  ];
const styles = theme => ({
  root: {
    display: "flex",
    alignItems: "center",
    paddingTop: 8,
    paddingBottom: 8
  },
  contentWrapper: {
    maxWidth: theme.palette.custom.contentAreaWidth,
    paddingLeft: theme.spacing.unit * 2,
    paddingTop: theme.spacing.unig
  },
  titleSub: {
    cursor: "pointer"
  },
});

class Comments extends React.Component {
  state = {
    value: 0,
    expanded: true
  };

  handleExpandClick = () => {
    this.setState(state => ({ expanded: !state.expanded }));
  };

  render() {
    const { classes } = this.props;
    const { value } = this.state;
    
    return (
      <ApiContext.Consumer>
        {({ active, api, handleMenuSelect }) => (
          <div className={classes.contentWrapper}>
            <div className={classes.root}>
              <ArrowDropDownCircleOutlined
                onClick={this.handleExpandClick}
                aria-expanded={this.state.expanded}
              />

              <Typography
                onClick={this.handleExpandClick}
                variant="display1"
                className={classes.titleSub}
              >
                Comments
              </Typography>
            </div>
            <Collapse in={this.state.expanded} timeout="auto" unmountOnExit>
                <Comment comments={dummyComments} />
                <CommentAdd />
            </Collapse>
          </div>
        )}
      </ApiContext.Consumer>
    );
  }
}

Comments.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Comments);
