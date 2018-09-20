import React from "react";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core/styles";
import { Typography, TextField, Button } from "@material-ui/core";
import AccountBox from "@material-ui/icons/AccountBox";
import Grid from "@material-ui/core/Grid";

const styles = theme => ({
  commentIcon: {
    color: theme.palette.getContrastText(theme.palette.background.default)
  },
  commentText: {
    color: theme.palette.getContrastText(theme.palette.background.default)
  },
  textField: {
    width: '100%',
  }
});

class CommentAdd extends React.Component {
  state = {
    value: 0,
  };


  render() {
    const { classes } = this.props;
    return (
      <Grid container spacing={24}>

          <Grid item xs zeroMinWidth>
            <TextField
              id="multiline-static"
              multiline
              rows="1"
              className={classes.textField}
              margin="normal"
              placeholder="Write a comment"
            />
            <Grid container spacing={8}>
              <Grid item>
                <Button variant="contained" color="primary">
                  Add Comment
                </Button>
              </Grid>
              <Grid item>
                <Button className={classes.button}>Cancel</Button>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
    );
  }
}

CommentAdd.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(CommentAdd);
