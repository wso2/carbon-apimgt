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
        if (isCreateMenu) {
            const composeQuery = '?query=name:PizzaShackAPI version:1.0 context:pizzashack';
            const composeQueryJSON = queryString.parse(composeQuery);
            composeQueryJSON.limit = 1;
            composeQueryJSON.offset = 0;
            API.search(composeQueryJSON).then((resp) => {
                const data = JSON.parse(resp.data);
                setShowSampleDeploy(data.count === 0);
            });
        } else {
            setShowSampleDeploy(true);
        }
    }, []);

    return (
        <Component
            id='itest-rest-api-create-menu'
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
            {(!isCreateMenu || (isCreateMenu && alwaysShowDeploySampleButton)) && showSampleDeploy && (
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
