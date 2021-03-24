
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';

const StreamingAPIMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;
    return (
        <Component
            openList={dense}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.api'
                    defaultMessage='Streaming API'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-streaming-api-ws'
                linkTo='/apis/create/streamingapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.design.new.ws.content'
                        defaultMessage='Create a Web Socket API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.design.new.title'
                    defaultMessage='Web Socket API'
                />
            </LandingMenuItem>
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-streaming-api-web-hook'
                linkTo='/apis/create/streamingapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.websub.content'
                        defaultMessage='Create a Webhook/WebSub API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.websub.title'
                    defaultMessage='Webhook API'
                />
            </LandingMenuItem>
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-streaming-api-sse'
                linkTo='/apis/create/streamingapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.sse.content'
                        defaultMessage='Create a Server-Sent Events API'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.sse.title'
                    defaultMessage='SSE API'
                />
            </LandingMenuItem>
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-streaming-api-import'
                linkTo='/apis/create/asyncapi'
                helperText={(
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPI.streaming.import.content'
                        defaultMessage='Upload a file or provide an Async API URL'
                    />
                )}
            >
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.streaming.import.title'
                    defaultMessage='Import an AsyncAPI'
                />
            </LandingMenuItem>
        </Component>
    );
};

export default StreamingAPIMenu;
