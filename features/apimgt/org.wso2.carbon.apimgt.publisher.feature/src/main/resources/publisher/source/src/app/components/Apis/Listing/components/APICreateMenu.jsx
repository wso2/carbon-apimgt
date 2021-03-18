/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { makeStyles } from '@material-ui/core/styles';
import {
    useTheme,
} from '@material-ui/core';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Grid from '@material-ui/core/Grid';

import RestAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/RestAPIMenu';
import SoapAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/SoapAPIMenu';
import GraphqlAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/GraphqlAPIMenu';
import StreamingAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/StreamingAPIMenu';
import ServiceCatalogMenu from 'AppComponents/Apis/Listing/Landing/Menus/ServiceCatalogMenu';
import MenuButton from 'AppComponents/Shared/MenuButton';

const useStyles = makeStyles({
    dividerCls: {
        height: '180px',
        position: 'absolute',
        top: '50%',
        '-ms-transform': 'translateY(-50%)',
        transform: 'translateY(-50%)',
        margin: 'auto',
    },
});

const APICreateMenu = () => {
    const theme = useTheme();
    const { dividerCls } = useStyles();
    const {
        graphqlIcon,
        restApiIcon,
        soapApiIcon,
        streamingApiIcon,
    } = theme.custom.landingPage.icons;
    return (
        <MenuButton
            buttonProps={{
                color: 'primary',
                variant: 'contained',
            }}
            menuList={(
                <Grid
                    container
                    direction='row'
                    justify='center'
                    alignItems='flex-start'
                    spacing={3}
                >
                    <RestAPIMenu openList icon={restApiIcon} />
                    <SoapAPIMenu openList icon={soapApiIcon} />
                    <GraphqlAPIMenu openList icon={graphqlIcon} />
                    <StreamingAPIMenu openList icon={streamingApiIcon} />
                    <Box display={{ xs: 'none', lg: 'block' }} mx={2}>
                        <Divider className={dividerCls} light orientation='vertical' variant='inset' />
                    </Box>
                    <ServiceCatalogMenu openList icon={streamingApiIcon} />
                </Grid>
            )}
        >
            Create API
        </MenuButton>
    );
};
export default APICreateMenu;
