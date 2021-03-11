
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from '../components/LandingMenuItem';
import LandingMenu from '../components/LandingMenu';

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
                id='itest-id-create-default'
                linkTo='/apis/create/rest'
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
