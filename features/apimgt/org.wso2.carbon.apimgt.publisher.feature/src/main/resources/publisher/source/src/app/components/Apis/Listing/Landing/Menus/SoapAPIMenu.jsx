
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';

const SoapAPIMenu = (props) => {
    const { icon } = props;
    return (
        <LandingMenu
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.soap.api'
                    defaultMessage='SOAP API'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                id='itest-id-create-soap-api'
                linkTo='/apis/create/wsdl'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.content'
                        defaultMessage='Use an existing WSDL'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.title'
                    defaultMessage='Import WSDL'
                />
            </LandingMenuItem>


        </LandingMenu>
    );
};

export default SoapAPIMenu;
