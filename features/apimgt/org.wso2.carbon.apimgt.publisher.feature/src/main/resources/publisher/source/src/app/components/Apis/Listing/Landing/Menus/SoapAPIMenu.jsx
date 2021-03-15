
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';

const SoapAPIMenu = (props) => {
    const { icon, openList } = props;
    return (
        <LandingMenu
            openList={openList}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.soap.api'
                    defaultMessage='SOAP API'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={openList}
                id='itest-id-create-soap-api'
                linkTo='/apis/create/wsdl'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.content'
                        defaultMessage='Generate REST or create a pass-through API'
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
