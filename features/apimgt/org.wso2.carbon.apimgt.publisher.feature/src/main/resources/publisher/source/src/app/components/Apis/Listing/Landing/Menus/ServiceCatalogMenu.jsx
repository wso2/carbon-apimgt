
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';

const ServiceCatalogMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;
    return (
        <Component
            openList={dense}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.service.catalog.api'
                    defaultMessage='Service Catalog'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-from-service-catalog'
                linkTo='/service-catalog'
            >
                <FormattedMessage
                    id='Apis.Listing.import.from.service.catalog.title'
                    defaultMessage='Import From Service Catalog'
                />
            </LandingMenuItem>
        </Component>
    );
};

export default ServiceCatalogMenu;
