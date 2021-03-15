
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';

const ServiceCatalogMenu = (props) => {
    const { icon, openList } = props;
    return (
        <LandingMenu
            openList={openList}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.service.catalog.api'
                    defaultMessage='Service Catalog'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={openList}
                id='itest-id-create-from-service-catalog'
                linkTo='/service-catalog'
            >
                <FormattedMessage
                    id='Apis.Listing.import.from.service.catalog.title'
                    defaultMessage='Import From Service Catalog'
                />
            </LandingMenuItem>
        </LandingMenu>
    );
};

export default ServiceCatalogMenu;
