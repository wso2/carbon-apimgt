import React from 'react';
import Drawer from 'material-ui/Drawer';
import Divider from 'material-ui/Divider';
import ChevronLeftIcon from 'material-ui-icons/ChevronLeft';
import ChevronRightIcon from 'material-ui-icons/ChevronRight';
import FolderOpen from 'material-ui-icons/FolderOpen';
import Dns from 'material-ui-icons/Dns';
import Footer from './Footer/Footer'
import {Link} from "react-router-dom";
import AuthManager from '../../data/AuthManager.js';
import Input from 'material-ui/Input';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Button from 'material-ui/Button';
import Menu from 'material-ui/Menu';
import InfoIcon from 'material-ui-icons/Info';
import Info from 'material-ui-icons/Info';
import Avatar from 'material-ui/Avatar';
import List, {ListItem, ListItemIcon, ListItemText,} from 'material-ui/List';
import ConfigManager from "../../data/ConfigManager";
import EnvironmentMenu from "./Header/EnvironmentMenu";
import Utils from "../../data/Utils";
import MenuIcon from 'material-ui-icons/Menu';
import Card, { CardActions, CardContent } from 'material-ui/Card';
import Person from 'material-ui-icons/Person';
import Popover from 'material-ui/Popover';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import {findDOMNode} from 'react-dom';
import { FormControlLabel } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import Typography from 'material-ui/Typography';
import classNames from 'classnames';


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


const drawerWidth = 240;

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
        marginLeft: drawerWidth,
        width: `calc(100% - ${drawerWidth}px)`,
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
        width: drawerWidth,
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
    content: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.default,
        padding: theme.spacing.unit * 3,
        marginTop: 64,
        paddingBottom: 50,
        overflowY: 'auto'
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
});

class Layout extends React.Component {
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

    render() {
        const { classes, theme } = this.props;
        let user = AuthManager.getUser();

        return (
            <div className={classes.root}>
                <AppBar
                    position="absolute"
                    className={classNames(classes.appBar, this.state.open && classes.appBarShift)}
                >
                    <Toolbar disableGutters={!this.state.open}>
                        <IconButton
                            color="inherit"
                            aria-label="open drawer"
                            onClick={this.handleDrawerOpen}
                            className={classNames(classes.menuButton, this.state.open && classes.hide)}
                        >
                            <MenuIcon />
                        </IconButton>

                        <Link to="/"  className={classes.brandWrapper}>
                                <Typography variant="title"  noWrap className={classes.brand}>
                                <img className={classes.siteLogo} src="/store/public/app/images/logo.png"
                                     alt="wso2-logo"/> <span>API STORE</span>
                            </Typography>
                        </Link>

                        <Input
                            placeholder="Search APIs"
                            className={classes.input}
                            inputProps={{
                                'aria-label': 'Apis',
                            }}
                        />
                        <IconButton aria-label="Search Info" color="default">
                            <Info onClick={this.handleClickTips}/>
                        </IconButton>
                        <Menu
                            id="tip-menu"
                            anchorEl={this.state.anchorElTips}
                            open={this.state.openTips}
                            onClose={this.handleRequestCloseTips}
                        >
                            <List dense={true}>
                                {helpTips.map((tip) => {
                                    return <ListItem button key={tip}>
                                        <ListItemIcon><InfoIcon/></ListItemIcon>
                                        <ListItemText primary={tip}/>
                                    </ListItem>
                                })}
                            </List>
                        </Menu>
                        {/* Environment menu */}
                        <EnvironmentMenu environments={this.state.environments}
                                         environmentLabel={Utils.getEnvironment().label}
                                         handleEnvironmentChange={this.handleEnvironmentChange}/>
                        {/* User menu */}
                        <Button color="inherit"
                                ref={node => {
                                    this.button = node;
                                }}
                                onClick={() => this.handleClickButton("openPopB")}
                        >{user.name}</Button>
                        <Popover
                            open={this.state.openPopB}
                            anchorEl={this.state.anchorEl}
                            onClose={() => this.handleRequestClose("openPopB")}
                            anchorOrigin={{
                                vertical: 'bottom',
                                horizontal: 'center',
                            }}
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'center',
                            }}
                        >
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
                                    <ListItem button className={classes.listItem}>
                                        <Avatar>
                                            <Person />
                                        </Avatar>
                                        <ListItemText secondary="Chanage Password" primary={this.state.user}/>
                                    </ListItem>
                                    <Link to={"/profile"} className={classes.textDisplayLink}>
                                        <ListItem button className={classes.listItem}>

                                            <ListItemText
                                                className={classes.textDisplay}
                                                primary="Profile"
                                                secondary="Go here"/>

                                        </ListItem>
                                    </Link>

                                </CardContent>
                                <CardActions>
                                    <Link to="/logout">
                                        <Button color="default" onClick={this.handleRequestCloseUserMenu}>
                                            Logout
                                        </Button>
                                    </Link>

                                </CardActions>
                            </Card>
                        </Popover>
                    </Toolbar>
                </AppBar>

                <Drawer
                    variant="permanent"
                    classes={{
                        paper: classNames(classes.drawerPaper, !this.state.open && classes.drawerPaperClose),
                    }}
                    open={this.state.open}
                >
                    <div className={classes.toolbar}>
                        <IconButton onClick={this.handleDrawerClose}>
                            {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                        </IconButton>
                    </div>
                    <Divider />
                        {user &&
                            <List>
                                <Link to="/">
                                    <ListItem button>
                                        <ListItemIcon>
                                            <FolderOpen />
                                        </ListItemIcon>
                                        <ListItemText primary="APIs" />
                                    </ListItem>
                                </Link>
                                <Link to="/applications">
                                    <ListItem button>
                                        <ListItemIcon>
                                            <Dns />
                                        </ListItemIcon>
                                        <ListItemText primary="Applications" />
                                    </ListItem>
                                </Link>
                            </List>
                            }

                    <Divider />
                </Drawer>
                <main className={classes.content}>
                    {this.props.children}

                </main>
                <Footer />

            </div>
        );
    }
}

Layout.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Layout);
