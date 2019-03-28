import React from 'react';
import { Link } from 'react-router-dom';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import ListItem from '@material-ui/core/ListItem';

import { Menu as MenuIcon } from '@material-ui/icons';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';

import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { findDOMNode } from 'react-dom';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import Typography from '@material-ui/core/Typography';

import Person from '@material-ui/icons/Person';
import Popper from '@material-ui/core/Popper';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import HowToReg from '@material-ui/icons/HowToReg';
import GenericSearch from './Generic/GenericSearch';
import GlobalNavBar from './Generic/GlobalNavbar';
import Utils from '../../data/Utils';
import EnvironmentMenu from './Header/EnvironmentMenu';
import ConfigManager from '../../data/ConfigManager';
import AuthManager from '../../data/AuthManager';
import VerticalDivider from '../Shared/VerticalDivider';
import Footer from './Footer/Footer';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    appBar: {
        position: 'relative',
        background: theme.palette.background.appBar,
    },
    icon: {
        marginRight: theme.spacing.unit * 2,
    },
    menuIcon: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: 35,
    },
    userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    // Page layout styles
    drawer: {
        top: 64,
    },
    wrapper: {
        minHeight: '100%',
        marginBottom: -50,
        background: 'transparent url(' + theme.custom.backgroundImage + ') repeat left top',
    },
    contentWrapper: {
        display: 'flex',
        flexDirection: 'row',
        overflow: 'auto',
        position: 'relative',
        minHeight: 'calc(100vh - 114px)',
    },
    push: {
        height: 50,
    },
    footer: {
        backgroundColor: theme.palette.grey.A100,
        paddingLeft: theme.spacing.unit * 3,
        height: 50,
        alignItems: 'center',
        display: 'flex',
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
});
/**
 *
 *
 * @class Layout
 * @extends {React.Component}
 */
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

    /**
     *
     *
     * @memberof Layout
     */
    componentDidMount() {
        // Get Environments
        const promised_environments = ConfigManager.getConfigs()
            .environments.then((response) => {
                this.setState({
                    environments: response.data.environments,
                });
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });

        const storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex) });
            let nightMode = false;
            if (parseInt(storedThemeIndex) === 1) {
                nightMode = true;
            }
            this.setState({ nightMode });
        }
    }

    /**
     *
     *
     * @memberof Layout
     */
    handleRequestCloseUserMenu = () => {
        this.setState({ openUserMenu: false });
    };

    /**
     *
     *
     * @memberof Layout
     */
    handleEnvironmentChange = (event) => {
        this.setState({ openEnvironmentMenu: false });
        // TODO: [rnk] Optimize Rendering.
        const environmentId = parseInt(event.target.id);
        Utils.setEnvironment(this.state.environments[environmentId]);
        this.setState({ environmentId });
    };

    /**
     *
     *
     * @memberof Layout
     */
    handleClickButton = (key) => {
        this.setState({
            [key]: true,
            anchorEl: findDOMNode(this.button),
        });
    };

    /**
     *
     *
     * @memberof Layout
     */
    handleRequestClose = (key) => {
        this.setState({
            [key]: false,
        });
    };

    /**
     *
     *
     * @memberof Layout
     */
    handleSwitch = name => (event) => {
        this.setState({ [name]: event.target.checked });
        this.props.setTheme();
    };

    /**
     *
     *
     * @param {*} event
     * @memberof Layout
     */
    toggleGlobalNavBar(event) {
        this.setState({ openNavBar: !this.state.openNavBar });
    }

    /**
     *
     *
     * @memberof Layout
     */
    handleToggleUserMenu = () => {
        this.setState(state => ({ openUserMenu: !state.openUserMenu }));
    };

    /**
     *
     *
     * @memberof Layout
     */
    handleCloseUserMenu = (event) => {
        if (this.anchorEl.contains(event.target)) {
            return;
        }

        this.setState({ openUserMenu: false });
    };

    /**
     *
     *
     * @memberof Layout
     */
    componentWillMount() {
        document.body.style.height = '100%';
        document.body.style.margin = '0';
    }

    /**
     *
     *
     * @memberof Layout
     */
    componentWillUnmount() {
        document.body.style.height = null;
        document.body.style.margin = null;
    }

    /**
     *
     *
     * @returns
     * @memberof Layout
     */
    render() {
        const { classes, theme } = this.props;
        const user = AuthManager.getUser();

        return (
            <React.Fragment>
                <div className={classes.wrapper}>
                    <AppBar position='fixed' className={classes.appBar}>
                        <Toolbar className={classes.toolbar}>
                            <IconButton onClick={this.toggleGlobalNavBar} color='inherit'>
                                <MenuIcon className={classes.menuIcon} />
                            </IconButton>
                            <Link to='/'>
                                <img src={theme.custom.logo} />
                            </Link>
                            <VerticalDivider height={32} />
                            <GenericSearch />
                            <VerticalDivider height={72} />
                            {/* Environment menu */}
                            <EnvironmentMenu environments={this.state.environments} environmentLabel={Utils.getEnvironment().label} handleEnvironmentChange={this.handleEnvironmentChange} />
                            {user ? (
                                <React.Fragment>
                                    <Button
                                        buttonRef={(node) => {
                                            this.anchorEl = node;
                                        }}
                                        aria-owns={open ? 'menu-list-grow' : null}
                                        aria-haspopup='true'
                                        onClick={this.handleToggleUserMenu}
                                        className={classes.userLink}
                                    >
                                        <Person />
                                        {' '}
                                        {user.name}
                                    </Button>
                                    <Popper
                                        open={this.state.openUserMenu}
                                        anchorEl={this.anchorEl}
                                        transition
                                        disablePortal
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
                                            <Grow {...TransitionProps} id='menu-list-grow' style={{ transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom' }}>
                                                <Paper>
                                                    <ClickAwayListener onClickAway={this.handleCloseUserMenu}>
                                                        <Card>
                                                            <CardContent>
                                                                <ListItem button className={classes.listItem}>
                                                                    <FormControlLabel control={<Switch checked={this.state.nightMode} onChange={this.handleSwitch('nightMode')} value='checkedB' color='primary' />} label='Night Mode' />
                                                                </ListItem>
                                                                <Link to='/user'>Change Password</Link>
                                                                <Link to='/profile' className={classes.textDisplayLink}>
                                                                    Profile
                                                                </Link>
                                                            </CardContent>
                                                            <CardActions>
                                                                <Link to='/logout'>Logout</Link>
                                                            </CardActions>
                                                        </Card>
                                                    </ClickAwayListener>
                                                </Paper>
                                            </Grow>
                                        )}
                                    </Popper>
                                </React.Fragment>
                            ) : (
                                <React.Fragment>
                                    <Link to='/sign-up'>
                                        <Button className={classes.userLink}>
                                            <HowToReg />
                                            {' '}
sign-up
                                        </Button>
                                    </Link>
                                    <Link to='/login'>
                                        <Button className={classes.userLink}>
                                            <Person />
                                            {' '}
Sign-in
                                        </Button>
                                    </Link>
                                </React.Fragment>
                            )}
                        </Toolbar>
                    </AppBar>
                    <GlobalNavBar toggleGlobalNavBar={this.toggleGlobalNavBar} open={this.state.openNavBar} />
                    <div className={classes.contentWrapper}>{this.props.children}</div>

                    <div className={classes.push} />
                </div>
                <Footer />
            </React.Fragment>
        );
    }
}

Layout.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Layout);
