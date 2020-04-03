import React from 'react';
// import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import { Route } from 'react-router-dom';
// import PropTypes from 'prop-types';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
// import PageNav from '../../Base/container/navigation/PageNav';
// import NavItem from '../../Base/container/navigation/NavItem';


// const isCurrentPage = (pathName, locationPath, apiID) => {
//     const pattern = new RegExp(`^(/${pathName}.*$).*`);
//     const pathAfterAPIID = locationPath.split(apiID)[1];
//     return pattern.test(pathAfterAPIID);
// };

// const SideNavBar = (props) => {
//     const { apiDetailPages, location, match } = props;
//     const locationPath = location.pathname;
//     const apiID = match.params.apiUUID;
//     const section = <NavItem name='APIs' NavIcon={<APIsIcon />} />;
//     const navItems = apiDetailPages.map((item) => (
//         <NavItem
//             key={item.pathName}
//             selected={isCurrentPage(item.pathName, locationPath, apiID)}
//             linkTo={item.pathName}
//             {...item}
//         />
//     ));
//     return <PageNav section={section} navItems={navItems} />;
// };

// SideNavBar.propTypes = {
//     apiDetailPages: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
//     location: PropTypes.shape({}).isRequired,
//     match: PropTypes.shape({}).isRequired,
// };
// export default withRouter(SideNavBar);

const SideNavBar = (props) => {
    const { classes, intl } = props;
    console.log('props obj from side nav bar: ', props);
    return (
        <div className='Details-LeftMenu-649'>
            <LeftMenuItem
                // text={intl.formatMessage({
                //     id: 'Apis.Details.index.overview',
                //     defaultMessage: 'overview',
                // })}
                text='overview'
                to='/overview'
            />
        </div>
    );
};

export default SideNavBar;
