import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Button from 'material-ui/Button';
import Dialog from 'material-ui/Dialog';
import List, { ListItem, ListItemText } from 'material-ui/List';
import Divider from 'material-ui/Divider';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Typography from 'material-ui/Typography';
import CloseIcon from 'material-ui-icons/Close';
import Slide from 'material-ui/transitions/Slide';
import Avatar from 'material-ui/Avatar';
import InsertDriveFile from 'material-ui-icons/InsertDriveFile'

const styles = theme => ({
  appBar: {
    position: 'relative',
  },
  flex: {
    flex: 1,
  },
  docContent: {
      padding: 20,
  },
  caption: {
      color: theme.palette.text.primary,
  },
  headline: {
    color: theme.palette.text.primary,
  },
  summary:{
      textDecoration: 'none',
      display: 'flex',
      paddingLeft: 0,
      cursor: 'pointer',
  }
});

function Transition(props) {
  return <Slide direction="up" {...props} />;
}

class DocumentView extends React.Component {
  state = {
    open: false,
  };

  handleClickOpen = () => {
    this.setState({ open: true });
  };

  handleClose = () => {
    this.setState({ open: false });
  };

  render() {
    const { classes, doc, truncateSummary } = this.props;
    return (
      <div>
        
        <a onClick={this.handleClickOpen} className={classes.summary}>
            <Avatar>
                <InsertDriveFile />
            </Avatar>
            <ListItemText primary={doc.name} secondary={truncateSummary}/>
        </a>
        <Dialog
          fullScreen
          open={this.state.open}
          onClose={this.handleClose}
          transition={Transition}
        >
          <AppBar className={classes.appBar}>
            <Toolbar>
              <IconButton color="inherit" onClick={this.handleClose} aria-label="Close">
                <CloseIcon />
              </IconButton>
              <div className={classes.titleWrapper}>
                <Typography variant="headline" gutterBottom className={classes.headline}>
                    {doc.name}
                </Typography>
                <Typography variant="caption" gutterBottom align="left" className={classes.caption}>
                    {doc.type}
                </Typography>
                </div>
            </Toolbar>
          </AppBar>
            <Typography gutterBottom noWrap className={classes.docContent}>
                {doc.summary}
            </Typography>
     
        </Dialog>
      </div>
    );
  }
}

DocumentView.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(DocumentView);