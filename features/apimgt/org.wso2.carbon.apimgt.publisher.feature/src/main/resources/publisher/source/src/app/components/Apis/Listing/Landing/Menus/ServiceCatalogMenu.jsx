
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from '../components/LandingMenuItem';
import LandingMenu from '../components/LandingMenu';

const ServiceCatalogMenu = (props) => {
    const { icon } = props;
    return (
        <LandingMenu
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.api'
                    defaultMessage='Service Catalog'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                id='itest-id-create-default'
                linkTo='/apis/create/rest'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.design.new.content'
                        defaultMessage='Design and prototype a new Streaming API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.design.new.title'
                    defaultMessage='Design New Streaming API'
                />
            </LandingMenuItem>
            <LandingMenuItem
                id='itest-id-create-default'
                linkTo='/apis/create/rest'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.import.content'
                        defaultMessage='Upload the definition or provide the URL'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.import.title'
                    defaultMessage='Import AsyncAPI Definition'
                />
            </LandingMenuItem>
        </LandingMenu>
    );
};

export default ServiceCatalogMenu;
