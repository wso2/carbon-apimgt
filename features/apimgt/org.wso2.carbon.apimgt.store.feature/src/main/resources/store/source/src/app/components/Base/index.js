import React from "react";
import { Link } from "react-router-dom";
import AuthManager from "../../data/AuthManager.js";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import Button from "@material-ui/core/Button";
import ListItem from "@material-ui/core/ListItem";

import ConfigManager from "../../data/ConfigManager";
import EnvironmentMenu from "./Header/EnvironmentMenu";
import Utils from "../../data/Utils";
import { Menu as MenuIcon } from "@material-ui/icons";
import Card from "@material-ui/core/Card";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";

import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core/styles";
import { findDOMNode } from "react-dom";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Switch from "@material-ui/core/Switch";
import Typography from "@material-ui/core/Typography";

import GlobalNavBar from "../new/GlobalNavbar";
import GenericSearch from "../new/GenericSearch";
import Person from "@material-ui/icons/Person";
import Popper from "@material-ui/core/Popper";
import Grow from "@material-ui/core/Grow";
import Paper from "@material-ui/core/Paper";
import ClickAwayListener from "@material-ui/core/ClickAwayListener";

const styles = theme => ({
  // New styles ..........
  ///////////////////////////////////
  ///////////////////////////////////
  appBar: {
    position: "relative",
    background: theme.palette.background.appBar
  },
  icon: {
    marginRight: theme.spacing.unit * 2
  },
  menuIcon: {
    color: theme.palette.getContrastText(theme.palette.background.appBar),
    fontSize: 35
  },
  userLink: {
    color: theme.palette.getContrastText(theme.palette.background.appBar)
  },
// Page layout styles
  drawer: {
    top: 64
  },
  wrapper: {
    minHeight: "100%",
    marginBottom: -50
  },
  contentWrapper: {
    display: 'flex',
    flexDirection: 'row',
    overflow: 'auto',
    position: 'relative',
    minHeight: 'calc(100vh - 114px)',
  },
  push: {
    height: 50
  },
  footer: {
    backgroundColor: theme.palette.grey["A100"],
    paddingLeft: theme.spacing.unit * 3,
    height: 50,
    alignItems: 'center',
    display: 'flex',
  },
  toolbar: {
    minHeight: 56,
    [`${theme.breakpoints.up("xs")} and (orientation: landscape)`]: {
      minHeight: 48
    },
    [theme.breakpoints.up("sm")]: {
      minHeight: 64
    }
  },
});

class Layout extends React.Component {
  constructor(props) {
    super(props);
    this.toggleGlobalNavBar = this.toggleGlobalNavBar.bind(this);
  }
  state = {
    environments: {},
    environmentId: 0,
    nightMode: false,
    themeIndex: 0,
    left: false,
    openNavBar: false,
    openUserMenu: false,
  };


  componentDidMount() {
    //Get Environments
    let promised_environments = ConfigManager.getConfigs()
      .environments.then(response => {
        this.setState({
          environments: response.data.environments
        });
      })
      .catch(error => {
        console.error(
          "Error while receiving environment configurations : ",
          error
        );
      });

    let storedThemeIndex = localStorage.getItem("themeIndex");
    if (storedThemeIndex) {
      this.setState({ themeIndex: parseInt(storedThemeIndex) });
      let nightMode = false;
      if (parseInt(storedThemeIndex) === 1) {
        nightMode = true;
      }
      this.setState({ nightMode: nightMode });
    }
  }

  handleRequestCloseUserMenu = () => {
    this.setState({ openUserMenu: false });
  };

  handleEnvironmentChange = event => {
    this.setState({ openEnvironmentMenu: false });
    //TODO: [rnk] Optimize Rendering.
    let environmentId = parseInt(event.target.id);
    Utils.setEnvironment(this.state.environments[environmentId]);
    this.setState({ environmentId });
  };

  handleClickButton = key => {
    this.setState({
      [key]: true,
      anchorEl: findDOMNode(this.button)
    });
  };
  handleRequestClose = key => {
    this.setState({
      [key]: false
    });
  };
  handleSwitch = name => event => {
    this.setState({ [name]: event.target.checked });
    this.props.setTheme();
  };
  toggleGlobalNavBar(event) {
    this.setState({ openNavBar: !this.state.openNavBar });
  }
  handleToggleUserMenu = () => {
    this.setState(state => ({ openUserMenu: !state.openUserMenu }));
  };
  handleCloseUserMenu = event => {
    if (this.anchorEl.contains(event.target)) {
      return;
    }

    this.setState({ openUserMenu: false });
  };

  render() {
    const { classes, theme } = this.props;
    let user = AuthManager.getUser();

    return (
      <React.Fragment>
        <div className={classes.wrapper}>
            
          <AppBar position="fixed" className={classes.appBar}>
              <Toolbar className={classes.toolbar}>
              <IconButton onClick={this.toggleGlobalNavBar} color="inherit">
                  <MenuIcon className={classes.menuIcon} />
                </IconButton>
                <Link to="/"><div className="main-logo"></div></Link>
                <div className="vertical-divider"></div>
                <GenericSearch  />
                <div className="vertical-divider"></div>
                {/* Environment menu */}
                <EnvironmentMenu environments={this.state.environments}
                                         environmentLabel={Utils.getEnvironment().label}
                                         handleEnvironmentChange={this.handleEnvironmentChange}/>
                <Button
                    buttonRef={node => {
                    this.anchorEl = node;
                    }}
                    aria-owns={open ? 'menu-list-grow' : null}
                    aria-haspopup="true"
                    onClick={this.handleToggleUserMenu}
                    className={classes.userLink}
                >
                    <Person  /> {user.name}
                </Button>
                <Popper open={this.state.openUserMenu} anchorEl={this.anchorEl} transition disablePortal 
                            anchorOrigin={{
                                vertical: 'bottom',
                                horizontal: 'center',
                            }}
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'center',
                            }}
>
                    {({ TransitionProps, placement }) => (
                    <Grow
                        {...TransitionProps}
                        id="menu-list-grow"
                        style={{ transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom' }}
                    >
                        <Paper>
                        <ClickAwayListener onClickAway={this.handleCloseUserMenu}>
                        <Card>
                                <CardContent>

                                    <ListItem button className={classes.listItem}>
                                        <FormControlLabel
                                            control={
                                                <Switch
                                                    checked={this.state.nightMode}
                                                    onChange={this.handleSwitch('nightMode')}
                                                    value="checkedB"
                                                    color="primary"
                                                />
                                            }
                                            label="Night Mode"
                                        />
                                    </ListItem>
                                    <Link to={"/user"}>
                                        Change Password
                                    </Link>
                                    <Link to={"/profile"} className={classes.textDisplayLink}>
                                        Profile
                                    </Link>

                                </CardContent>
                                <CardActions>
                                    <Link to="/logout">
                                        Logout
                                    </Link>

                                </CardActions>
                            </Card>
                        </ClickAwayListener>
                        </Paper>
                    </Grow>
                    )}
                </Popper>
              </Toolbar>
            </AppBar>
          <GlobalNavBar
              toggleGlobalNavBar={this.toggleGlobalNavBar}
              open={this.state.openNavBar}
            />    
            <div className={classes.contentWrapper}>
            {this.props.children}
            </div>
           
          <div className={classes.push} />
        </div>
        <footer className={classes.footer}>
          <Typography noWrap>{'WSO2 APIM v3.0.0 | © 2018 WSO2 Inc'}</Typography>
        </footer>
      </React.Fragment>
    );
  }
}

Layout.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired
};

export default withStyles(styles, { withTheme: true })(Layout);
