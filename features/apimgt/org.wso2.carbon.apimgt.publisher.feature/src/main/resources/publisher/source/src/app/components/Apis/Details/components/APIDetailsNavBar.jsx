import React from 'react';
import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import { withRouter } from 'react-router-dom';

import PageNav from '../../../Base/container/navigation/PageNav';
import NavItem from '../../../Base/container/navigation/NavItem';

const isCurrentPage = (pathName, locationPath, apiID) => {
    const pattern = new RegExp(`^(/${pathName}.*$).*`);
    const pathAfterAPIID = locationPath.split(apiID)[1];
    return pattern.test(pathAfterAPIID);
};

const APIDetailsNavBar = (props) => {
    const { apiDetailPages, location, match } = props;
    const locationPath = location.pathname;
    const apiID = match.params.apiUUID;
    const section = <NavItem {...APIDetailsNavBar.section} />;
    const navItems = apiDetailPages.map(item => (
        <NavItem
            key={item.pathName}
            selected={isCurrentPage(item.pathName, locationPath, apiID)}
            linkTo={item.pathName}
            {...item}
        />
    ));
    return <PageNav section={section} navItems={navItems} />;
};

APIDetailsNavBar.section = { name: 'API', NavIcon: <APIsIcon /> };
export default withRouter(APIDetailsNavBar);
