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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';

const GraphqlAPIMenu = (props) => {
    const { icon, isCreateMenu, isDisabled } = props;
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
            {!isDisabled ? (
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
            ) : (
                <Grid
                    item
                    xs={12}
                >
                    <Typography align='center' variant='body1'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPI.graphql.no permission'
                            defaultMessage='You do not have enough permission to create an API'
                        />
                    </Typography>
                </Grid>
            )}
        </Component>
    );
};

export default GraphqlAPIMenu;
