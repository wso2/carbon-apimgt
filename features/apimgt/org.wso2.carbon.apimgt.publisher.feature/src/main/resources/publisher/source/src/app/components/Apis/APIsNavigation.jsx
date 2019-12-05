import React from 'react';
import PropTypes from 'prop-types';
import APIsIcon from '@material-ui/icons/SettingsInputHdmi';
import AlertsIcon from '@material-ui/icons/AddAlert';

import PageNav from '../Base/container/navigation/PageNav';
import NavItem from '../Base/container/navigation/NavItem';

/**
 * Compose the Left side fixed navigation bar for APIs page (APP_CONTEXT/apis)
 * @returns {React.Component} @inheritdoc
 */
const NavBar = (props) => {
    const { intl } = props;
    const items = [
        {
            name: intl.formatMessage({
                id: 'Apis.APIsNavigation.alerts',
                defaultMessage: 'Alerts',
            }),
            linkTo: '/alerts',
            NavIcon: <AlertsIcon />,
        },
    ];
    const navItems = items.map((item) => <NavItem key={item.name} {...item} />);
    const section = <NavItem name='APIs' linkTo='/apis' NavIcon={<APIsIcon />} />;
    return <PageNav section={section} navItems={navItems} />;
};
NavBar.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default NavBar;
