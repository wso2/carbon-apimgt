
import React, { useEffect, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import queryString from 'query-string';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';
import SampleAPI from 'AppComponents/Apis/Listing/SampleAPI/SampleAPI';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import Configurations from 'Config';
import API from 'AppData/api';

const RestAPIMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;
    const { alwaysShowDeploySampleButton } = Configurations.apis;
    const [showSampleDeploy, setShowSampleDeploy] = useState(false);

    useEffect(() => {
        const composeQuery = '?query=name:PizzaShackAPI version:1.0 context:pizzashack';
        const composeQueryJSON = queryString.parse(composeQuery);
        composeQueryJSON.limit = 1;
        composeQueryJSON.offset = 0;
        API.search(composeQueryJSON).then((resp) => {
            const data = JSON.parse(resp.data);
            setShowSampleDeploy(data.count === 0);
        });
    }, []);

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
            {alwaysShowDeploySampleButton && showSampleDeploy && (
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
