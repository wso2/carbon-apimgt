import React from "react";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core/styles";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import ExpansionPanel from "@material-ui/core/ExpansionPanel";
import ExpansionPanelSummary from "@material-ui/core/ExpansionPanelSummary";
import ExpansionPanelDetails from "@material-ui/core/ExpansionPanelDetails";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";

import KeyConfiguration from "./KeyConfiguration";
import ViewKeys from "./ViewKeys";

const styles = theme => ({
  root: {
     padding: theme.spacing.unit*3,
  },
  button: {
    marginLeft: theme.spacing.unit*2
  },
  tokenSection: {
    marginTop: theme.spacing.unit*2,
    marginBottom: theme.spacing.unit*2,
  },
  margin: {
      marginRight: theme.spacing.unit*2,
  },
  keyTitle: {
    textTransform: 'uppercase',
  },
  keyConfigWrapper: {
      flexDirection: 'column',
      marginBottom: 0,
  },
  generateWrapper:{
    padding: 10,
    borderLeft: 'solid 1px #fff',
    borderRight: 'solid 1px #fff',
    borderBottom: 'solid 1px #fff',
  }
});

class TokenManager extends React.Component {
  state = {
    value: 0,
    response: null
  };
  generateKeys = () => {
    let that = this;
    let promiseGenerate = this.keys.keygenWrapper();
    promiseGenerate
      .then(response => {
        console.log("Keys generated successfully with ID : " + response);
        that.setState({
          response
        });
      })
      .catch(error => {
        if (process.env.NODE_ENV !== "production") {
          console.log(error);
        }
        let status = error.status;
        if (status === 404) {
          this.setState({ notFound: true });
        }
      });
  };
  handleChange = (event, value) => {
    this.setState({ value });
  };
  render() {
    const { classes, selectedApp, keyType } = this.props;
    const { value, response } = this.state;
    return (
      <div className={classes.root}>
        <Typography variant="headline" className={classes.keyTitle}>{keyType} Key and Secret</Typography>

        <ViewKeys selectedApp={selectedApp} keyType={keyType} />
       
        <ExpansionPanel>
          <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
            <Typography className={classes.heading} variant="subtitle1">
              Key Configuration
            </Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails className={classes.keyConfigWrapper}>
            <KeyConfiguration
              innerRef={node => (this.keys = node)}
              selectedApp={selectedApp}
              keyType={keyType}
            />
          </ExpansionPanelDetails>
        </ExpansionPanel>
        <div class={classes.generateWrapper}>
          <Button
                variant="contained"
                color="primary"
                className={classes.button}
                onClick={this.generateKeys}
              >
                Generate Keys
          </Button>
        </div>
        
        {/* <Tokens
          innerRef={node => (this.tokens = node)}
          selectedApp={selectedApp}
          keyType={keyType}
        /> */}
      </div>
    );
  }
}

TokenManager.propTypes = {
  classes: PropTypes.object
};

export default withStyles(styles)(TokenManager);
