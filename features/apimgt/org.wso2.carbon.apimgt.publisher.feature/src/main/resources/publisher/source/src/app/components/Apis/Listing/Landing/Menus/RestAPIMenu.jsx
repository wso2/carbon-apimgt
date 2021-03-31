
import React, { useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';
import SampleAPI from 'AppComponents/Apis/Listing/SampleAPI/SampleAPI';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import Configurations from 'Config';
import { useApiListContext } from 'AppComponents/Shared/ApiListContext';

/**
 * Verify whether the sample API has already been deployed.
 * @param {(Array | null)} apisAndApiProducts list of APIs
 * @returns {boolean} true if API already deployed/exists
 */
const verifyIfSampleDeployed = (apisAndApiProducts) => {
    if (apisAndApiProducts && Array.isArray(apisAndApiProducts)) {
        return apisAndApiProducts.some((api) => api.name === 'PizzaShackAPI' && api.version === '1.0.0'
            && api.context === '/pizzashack');
    } else {
        return false;
    }
};

const RestAPIMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;
    const { alwaysShowDeploySampleButton = true } = Configurations.apis || {};
    const { apisAndApiProducts } = useApiListContext();
    const isSampleDeployed = useMemo(() => verifyIfSampleDeployed(apisAndApiProducts), [apisAndApiProducts]);

    return (
        <Component
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api'
                    defaultMessage='REST API'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={dense}
                id='itest-id-landing-rest-create-default'
                linkTo='/apis/create/rest'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.content'
                        defaultMessage='Design and prototype a new REST API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.title'
                    defaultMessage='Start From Scratch'
                />
            </LandingMenuItem>

            <LandingMenuItem
                dense={dense}
                id='itest-id-landing-upload-oas'
                linkTo='/apis/create/openapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.content'
                        defaultMessage='Import OAS 3 or Swagger 2.0 definition'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.title'
                    defaultMessage='Import Open API'
                />
            </LandingMenuItem>
            {alwaysShowDeploySampleButton && !isSampleDeployed && (
                <>
                    <Box width={1}>
                        <Divider light variant='middle' />
                    </Box>
                    <SampleAPI dense={dense} />
                </>
            )}
        </Component>
    );
};

export default RestAPIMenu;
