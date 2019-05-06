import React from 'react';
import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import AlertsIcon from '@material-ui/icons/AddAlert';

import PageNav from '../Base/container/navigation/PageNav';
import NavItem from '../Base/container/navigation/NavItem';

/**
 * Compose the Left side fixed navigation bar for APIs page (/publisher-new/apis)
 * @returns {React.Component} @inheritdoc
 */
const NavBar = () => {
    const items = [{ name: 'Alerts', linkTo: '/alerts', NavIcon: <AlertsIcon /> }];
    const navItems = items.map(item => <NavItem key={item.name} {...item} />);
    const section = <NavItem name='APIs' linkTo='/apis' NavIcon={<APIsIcon />} />;
    return <PageNav section={section} navItems={navItems} />;
};

export default NavBar;
