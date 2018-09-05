import React from 'react';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import CssBaseline from '@material-ui/core/CssBaseline';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { Menu as MenuIcon } from "@material-ui/icons";
import IconButton from "@material-ui/core/IconButton";
import GlobalNavBar from "./GlobalNavbar";
import GenericSearch from './GenericSearch';
import UserMenu from './UserMenu';
import LeftMenu from './LeftMenu';
import InfoBar from './InfoBar';
import APIDetail from './APIDetail';
import RightPanel from './RightPanel';

const styles = theme => ({
  appBar: {
    position: 'relative',
    background: theme.palette.background.appBar,
  },
  icon: {
    marginRight: theme.spacing.unit * 2,
  },
  footer: {
    backgroundColor: theme.palette.grey['A100'],
    padding: theme.spacing.unit * 3,
  },
  toolbar: { 
    minHeight: 56, 
    [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: { 
      minHeight: 48, 
    }, 
    [theme.breakpoints.up('sm')]: { 
      minHeight: 64, 
    }, 
  }, 
  drawer: {
    top: 64,
  },
  menuIcon: {
    color: theme.palette.getContrastText(theme.palette.background.appBar),
    fontSize: 35
  },
  mainContent: {
    display: 'flex',
  },
  content: {
    flexGrow: 1,
  }
});

class Album extends React.Component {
  constructor(props){
    super(props);
    this.toggleGlobalNavBar = this.toggleGlobalNavBar.bind(this);
    this.state = {
      left: false,
      openNavBar: false,
  
    };
  }
  
  toggleGlobalNavBar(event) {
    this.setState({ openNavBar: !this.state.openNavBar });
  }


  render(){
    const { classes } = this.props;


    return (
      <React.Fragment>
        <CssBaseline />
        <AppBar position="fixed" className={classes.appBar}>
          <Toolbar className={classes.toolbar}>
          <IconButton onClick={this.toggleGlobalNavBar} color="inherit">
              <MenuIcon className={classes.menuIcon} />
            </IconButton>
            <div className="main-logo"></div>
            <div className="vertical-divider"></div>
            <GenericSearch  />
            <div className="vertical-divider"></div>
            <UserMenu />
          </Toolbar>
        </AppBar>
       <GlobalNavBar
          toggleGlobalNavBar={this.toggleGlobalNavBar}
          open={this.state.openNavBar}
        />
        <div className={classes.mainContent}>
          <LeftMenu />
          <main className={classes.content}>
            <InfoBar />
            <APIDetail />
          </main>
          <RightPanel />
        </div>
        {/* Footer */}
        <footer className={classes.footer}>
        <Typography noWrap>{'WSO2 APIM v3.0.0 | © 2018 WSO2 Inc'}</Typography>
          
        </footer>
        {/* End footer */}
      </React.Fragment>
    );
  }
}

Album.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Album);