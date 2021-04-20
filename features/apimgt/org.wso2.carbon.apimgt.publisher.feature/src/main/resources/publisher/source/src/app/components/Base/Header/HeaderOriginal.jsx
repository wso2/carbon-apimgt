import React from 'react';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import SearchIcon from '@material-ui/icons/SearchOutlined';
import Hidden from '@material-ui/core/Hidden';
import Box from '@material-ui/core/Box';
import { Link } from 'react-router-dom';
import Avatar from 'AppComponents/Base/Header/avatar/Avatar';
import CloseIcon from '@material-ui/icons/Close';
import Configurations from 'Config';
import HeaderSearch from 'AppComponents/Base/Header/headersearch/HeaderSearch';
import GlobalNavBar from 'AppComponents/Base/Header/navbar/GlobalNavBar';
import { GlobalDrawerProvider } from 'AppComponents/Base/Header/navbar/useNavBar';


const useStyles = makeStyles((theme) => ({
    appBar: {
        background: theme.palette.background.appBar,
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    menuIcon: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    drawerToggleIcon: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    toolbarStyles: {
        minHeight: theme.spacing(8),
    },
}));

/**
 *
 */
export default function HeaderOriginal(props) {
    const { avatar, menuItems, user } = props;
    const classes = useStyles();
    const theme = useTheme();
    const [open, setOpen] = React.useState(false);
    const [openMiniSearch, setOpenMiniSearch] = React.useState(false);
    const toggleMiniSearch = () => { setOpenMiniSearch(!openMiniSearch); };
    const handleDrawerToggle = () => {
        setOpen(!open);
    };
    const Icon = open ? CloseIcon : MenuIcon;
    return (
        <GlobalDrawerProvider value={{ open, setOpen }}>
            <AppBar
                position='fixed'
                className={classes.appBar}
            >
                <Toolbar className={classes.toolbarStyles}>
                    <IconButton
                        aria-label='Expand publisher landing page drawer'
                        onClick={handleDrawerToggle}
                        edge='start'
                    >
                        <Icon
                            fontSize='large'
                            className={classes.drawerToggleIcon}
                            titleAccess='Expand page drawer'
                        />
                    </IconButton>
                    <Box display='flex' justifyContent='space-between' flexDirection='row' width={1}>
                        <Box display='flex'>
                            <Link to='/'>
                                <img
                                    src={Configurations.app.context + theme.custom.logo}
                                    alt={`${theme.custom.title.prefix} ${theme.custom.title.suffix}`}
                                    style={{ height: theme.custom.logoHeight, width: theme.custom.logoWidth }}
                                />
                            </Link>
                        </Box>
                        <Box display='flex'>
                            <Hidden smDown>
                                <HeaderSearch />
                            </Hidden>
                            <Hidden mdUp>
                                <IconButton onClick={toggleMiniSearch} color='inherit'>
                                    <SearchIcon className={classes.menuIcon} />
                                </IconButton>
                                {openMiniSearch
                                    && (
                                        <HeaderSearch toggleSmSearch={toggleMiniSearch} smSearch={openMiniSearch} />
                                    )}
                            </Hidden>
                            {menuItems}
                            {avatar || <Avatar user={user} />}
                        </Box>
                    </Box>
                </Toolbar>
            </AppBar>
            <GlobalNavBar setOpen={setOpen} open={open} />
        </GlobalDrawerProvider>
    );
}
