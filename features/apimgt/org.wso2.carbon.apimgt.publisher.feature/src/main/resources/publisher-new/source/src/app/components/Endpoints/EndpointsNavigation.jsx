import React from 'react';
import ServiceDiscoveryIcon from '@material-ui/icons/Search';
import PageNav from 'AppComponents/Base/container/navigation/PageNav';
import NavItem from 'AppComponents/Base/container/navigation/NavItem';
import EndpointsIcon from '@material-ui/icons/ZoomOutMapOutlined';

/**
 * Compose the Left side fixed navigation bar for APIs page (/publisher-new/apis)
 * @returns {React.Component} @inheritdoc
 */
const EndpointsNavigation = () => {
    const items = [{ name: 'Service Discovery', linkTo: '/endpoints/discover', NavIcon: <ServiceDiscoveryIcon /> }];
    const navItems = items.map(item => <NavItem key={item.name} {...item} />);
    const section = <NavItem name='Endpoints' linkTo='/endpoints' NavIcon={<EndpointsIcon />} />;
    return <PageNav section={section} navItems={navItems} />;
};

export default EndpointsNavigation;
