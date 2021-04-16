/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
                linkTo='/apis/create/streamingapi/ws'
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
                linkTo='/apis/create/streamingapi/websub'
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
                linkTo='/apis/create/streamingapi/sse'
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
