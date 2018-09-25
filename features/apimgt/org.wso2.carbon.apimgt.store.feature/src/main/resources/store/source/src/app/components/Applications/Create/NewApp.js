import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import ListItemText from '@material-ui/core/ListItemText';
import ListItem from '@material-ui/core/ListItem';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import ApplicationCreate from '../../Shared/AppsAndKeys/ApplicationCreate';

const styles = {
  appBar: {
    position: 'relative',
  },
  flex: {
    flex: 1,
  },
  button: {
    marginRight: 16,
  }
};

function Transition(props) {
  return <Slide direction="up" {...props} />;
}

class NewApp extends React.Component {
  state = {
    open: false,
  };

  handleClickOpen = () => {
    this.setState({ open: true });
  };

  handleClose = () => {
    this.setState({ open: false });
  };
  saveApplication = () => {
    let promised_create = this.applicationCreate.handleSubmit();
    if(promised_create) {
      promised_create.then(response => {
        let appCreated = JSON.parse(response.data);
        that.newApp = {value: appCreated.applicationId, label: appCreated.name}
        //Once application loading fixed this need to pass application ID and load app
        console.log("Application created successfully.");
        this.setState({ open: false });
      }).catch(
        function (error_response) {
            Alert.error('Application already exists.');
            console.log("Error while creating the application");
      });
    }
  }
  render() {
    const { classes } = this.props;
    return (
      <React.Fragment>
          <Button variant="contained" color="primary" className={classes.button} onClick={this.handleClickOpen}>
                                                ADD NEW APPLICATION
        </Button>
        <Dialog
          fullScreen
          open={this.state.open}
          onClose={this.handleClose}
          TransitionComponent={Transition}
        >
          <AppBar className={classes.appBar}>
            <Toolbar>
              <IconButton color="inherit" onClick={this.handleClose} aria-label="Close">
                <CloseIcon />
              </IconButton>
              <Typography variant="title" color="inherit" className={classes.flex}>
                Create New Application
              </Typography>
              <Button color="inherit" onClick={this.handleClose}>
                save
              </Button>
            </Toolbar>
          </AppBar>
          <ApplicationCreate innerRef={node => this.applicationCreate = node} />
          <div>
            <Button variant="outlined" className={classes.button} onClick={this.handleClose}>
                Cancel
            </Button>
            <Button variant="contained" color="primary" className={classes.button} onClick={this.saveApplication}>
                                              ADD NEW APPLICATION
            </Button>
           
            </div>
        </Dialog>
        </React.Fragment>
    );
  }
}

NewApp.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(NewApp);
