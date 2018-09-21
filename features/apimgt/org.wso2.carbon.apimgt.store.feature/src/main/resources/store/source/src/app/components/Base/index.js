import React from 'react';
import {Link} from "react-router-dom";
import AuthManager from '../../data/AuthManager.js';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import ListItem from '@material-ui/core/ListItem';

import ConfigManager from "../../data/ConfigManager";
import EnvironmentMenu from "./Header/EnvironmentMenu";
import Utils from "../../data/Utils";
import { Menu as MenuIcon } from "@material-ui/icons";
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';

import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {findDOMNode} from 'react-dom';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import Typography from '@material-ui/core/Typography';

import CssBaseline from '@material-ui/core/CssBaseline';
import GlobalNavBar from "../new/GlobalNavbar";
import GenericSearch from '../new/GenericSearch';
import Person from '@material-ui/icons/Person';
import Popper from '@material-ui/core/Popper';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';



const helpTips = [
    "By API Name [Default]",
    "By API Provider [ Syntax - provider:xxxx ] or",
    "By API Version [ Syntax - version:xxxx ] or",
    "By Context [ Syntax - context:xxxx ] or",
    "By Description [ Syntax - description:xxxx ] or",
    "By Tags [ Syntax - tags:xxxx ] or",
    "By Sub-Context [ Syntax - subcontext:xxxx ] or",
    "By Documentation Content [ Syntax - doc:xxxx ]"
];
  

const styles = theme => ({
    root: {
        flexGrow: 1,
        height: '100vh',
        zIndex: 1,
        overflow: 'hidden',
        position: 'relative',
        display: 'flex',
    },
    appBar: {
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        display: 'flex',
    },
    appBarShift: {
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    menuButton: {
        marginLeft: 12,
        marginRight: 36,
    },
    hide: {
        display: 'none',
    },
    drawerPaper: {
        position: 'relative',
        transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    drawerPaperClose: {
        width: 60,
        overflowX: 'hidden',
        transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    toolbar: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        padding: '0 8px',
    },
    brandText: {
        marginRight: 10,
        fontWeight: 200,
        textDecoration: 'none',
        color: theme.palette.text.primary
    },
    siteLogo: {
        width: 60,
        display: 'inline-block',
        marginRight: 10,
    },
    brand: {
        textDecoration: 'none',
        color: theme.palette.text.banner,
        marginRight: 20,
        display: 'flex',
        alignItems: 'flex-start',
    },
    brandWrapper: {
        textDecoration: 'none',
    },
    input: {
        margin: '0 20 0 20',
        flex: 1,
    },
    // New styles ..........
    ///////////////////////////////////
    ///////////////////////////////////
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
        height: 50,
        display: 'none',
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
        height: '100vh',
      },
      userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
      },
      wrapper: {
        minHeight: '100%',
        marginBottom: -50,
      },
      push : {
        height: 50,
      },
});

class Layout extends React.Component {
    constructor(props){
        super(props);
        this.toggleGlobalNavBar = this.toggleGlobalNavBar.bind(this);
    }
    state = {
        open: false,
        anchorElUserMenu: undefined,
        anchorElAddMenu: undefined,
        anchorElMainMenu: undefined,
        anchorElTips: undefined,
        openUserMenu: false,
        openAddMenu: false,
        openMainMenu: false,
        searchVisible: false,
        openTips: false,
        environments: {},
        environmentId: 0,
        openPopB: false,
        nightMode: false,
        themeIndex: 0,
        left: false,
        openNavBar: false,
        openUserMenu: false,
    };

    handleDrawerOpen = () => {
        this.setState({ open: true });
    };

    handleDrawerClose = () => {
        this.setState({ open: false });
    };
    componentDidMount(){
        //Get Environments
        let promised_environments = ConfigManager.getConfigs().environments.then(response => {
            this.setState({
                environments: response.data.environments
            });
        }).catch(error => {
            console.error('Error while receiving environment configurations : ', error);
        });

        let storedThemeIndex = localStorage.getItem("themeIndex");
        if (storedThemeIndex) {
            this.setState({themeIndex: parseInt(storedThemeIndex)});
            let nightMode = false;
            if(parseInt(storedThemeIndex) === 1 ){
                nightMode = true;
            }
            this.setState({nightMode: nightMode})
        }
    }


    handleRequestCloseUserMenu = () => {
        this.setState({openUserMenu: false});
    };

    handleEnvironmentChange = event => {
        this.setState({ openEnvironmentMenu: false });
        //TODO: [rnk] Optimize Rendering.
        let environmentId = parseInt(event.target.id);
        Utils.setEnvironment(this.state.environments[environmentId]);
        this.setState({environmentId});
    };

    handleRequestCloseAddMenu = () => {
        this.setState({openAddMenu: false});
    };
    handleClickTips = event => {
        this.setState({openTips: true, anchorElTips: event.currentTarget});
    };
    handleRequestCloseTips = () => {
        this.setState({openTips: false});
    };

    handleClickButton = (key) => {
        this.setState({
            [key]: true,
            anchorEl: findDOMNode(this.button),
        });
    }
    handleRequestClose = (key) => {
        this.setState({
            [key]: false,
        });
    }
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
            <CssBaseline />
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
            <div className={classes.mainContent}>
              {/* <LeftMenu /> */}
              {this.props.children}

              {/* <main className={classes.content}> */}
                {/* <InfoBar /> */}
                {/* <APIDetail /> */}
              {/* </main> */}
              {/* <RightPanel /> */}
            </div>
            {/* Footer */}

    <div className={classes.push}></div>
  </div>
    <footer className={classes.footer}>
            <Typography noWrap>{'WSO2 APIM v3.0.0 | © 2018 WSO2 Inc'}</Typography>
    </footer>


            
           
            {/* End footer */}
          </React.Fragment>



        );
    }
}

Layout.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Layout);
