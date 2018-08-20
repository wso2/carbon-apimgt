import React, { Component } from 'react';
import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import OverviewIcon from '@material-ui/icons/SettingsOverscan';
import LifeCycleIcon from '@material-ui/icons/Autorenew';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import ResourcesIcon from '@material-ui/icons/VerticalSplit';
import ScopesIcon from '@material-ui/icons/VpnKey';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
import SubscriptionsIcon from '@material-ui/icons/Bookmarks';
import { withRouter } from 'react-router-dom';

import PageNav from '../Base/container/navigation/PageNav';
import NavItem from '../Base/container/navigation/NavItem';

const APIDetailsNavBar = () => {
    const section = <NavItem {...APIDetailsNavBar.section} />;
    const navItems = APIDetailsNavBar.items.map(item => <NavItem {...item} />);
    return <PageNav section={section} navItems={navItems} />;
};

APIDetailsNavBar.section = { name: 'API', NavIcon: <APIsIcon /> };
APIDetailsNavBar.items = [
    { name: 'Overview', linkTo: 'overview', NavIcon: <OverviewIcon /> },
    { name: 'LifeCycle', linkTo: 'lifecycle', NavIcon: <LifeCycleIcon /> },
    { name: 'Endpoints', linkTo: 'endpoints', NavIcon: <EndpointIcon /> },
    { name: 'Resources', linkTo: 'resources', NavIcon: <ResourcesIcon /> },
    { name: 'Scopes', linkTo: 'scopes', NavIcon: <ScopesIcon /> },
    { name: 'Documents', linkTo: 'documents', NavIcon: <DocumentsIcon /> },
    { name: 'Subscription', linkTo: 'subscription', NavIcon: <SubscriptionsIcon /> },
];
export default withRouter(APIDetailsNavBar);
