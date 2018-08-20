import React from 'react';
import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import AlertsIcon from '@material-ui/icons/AddAlert';

import { withStyles } from '@material-ui/core';
import { withRouter } from 'react-router-dom';

import PageNav from '../Base/container/navigation/PageNav';
import NavItem from '../Base/container/navigation/NavItem';

const styles = () => ({
    pageIcon: {
        fontSize: '40px',
    },
    navIcon: {
        fontSize: '35px',
    },
});

const NavBar = () => {
    const section = <NavItem {...NavBar.section} />;
    const navItems = NavBar.items.map(item => <NavItem {...item} />);
    return <PageNav section={section} navItems={navItems} />;
};

NavBar.section = { name: 'APIs', NavIcon: <APIsIcon /> };
NavBar.items = [
    { name: 'Alerts', linkTo: '/alerts', NavIcon: <AlertsIcon /> },
];
export default withStyles(styles)(withRouter(NavBar));
