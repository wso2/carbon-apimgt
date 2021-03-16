
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';

const GraphqlAPIMenu = (props) => {
    const { icon, openList } = props;
    return (
        <LandingMenu
            openList={openList}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.graphql.api'
                    defaultMessage='GraphQL'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={openList}
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


        </LandingMenu>
    );
};

export default GraphqlAPIMenu;
