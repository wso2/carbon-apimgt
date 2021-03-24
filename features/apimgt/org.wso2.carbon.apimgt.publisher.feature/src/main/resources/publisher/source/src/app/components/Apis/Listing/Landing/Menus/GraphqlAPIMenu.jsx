
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';

const GraphqlAPIMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;

    return (
        <Component
            openList={dense}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.graphql.api'
                    defaultMessage='GraphQL'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-graphql-api'
                linkTo='/apis/create/graphQL'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.content'
                        defaultMessage='Use an existing definition'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.title'
                    defaultMessage='Import GraphQL SDL'
                />
            </LandingMenuItem>
        </Component>
    );
};

export default GraphqlAPIMenu;
