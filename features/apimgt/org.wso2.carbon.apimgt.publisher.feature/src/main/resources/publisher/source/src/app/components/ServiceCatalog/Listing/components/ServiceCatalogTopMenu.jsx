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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import IconButton from '@material-ui/core/IconButton';
import ButtonGroup from '@material-ui/core/ButtonGroup';

/**
 * Service catalog page top menu
 *
 * @function ServiceCatalogTopMenu
 * @returns {any} ServiceCatalogTopMenu Page for Services
 */
function ServiceCatalogTopMenu(props) {
    const { isGridView, setIsGridView, showServiceToggle } = props;
    return (
        <Box
            borderBottom={1}
            bgcolor='background.paper'
            px={8}
            display='flex'
            alignItems='center'
            height={72}
            borderColor='grey.500'
        >
            <Grid
                container
                direction='row'
                justify='space-between'
                alignItems='center'
            >
                <Grid item>
                    <Typography variant='h4'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Listing.heading'
                            defaultMessage='Service Catalog'
                        />
                    </Typography>
                </Grid>
                <Grid item>
                    {showServiceToggle && (
                        <ButtonGroup color='primary' aria-label='outlined primary button group'>
                            <IconButton onClick={() => setIsGridView(true)} aria-label='delete'>
                                <GridOn color={isGridView ? 'primary' : 'disabled'} />
                            </IconButton>
                            <IconButton onClick={() => setIsGridView(false)} aria-label='delete'>
                                <List color={!isGridView ? 'primary' : 'disabled'} />
                            </IconButton>
                        </ButtonGroup>
                    )}
                </Grid>
            </Grid>
        </Box>
    );
}

export default ServiceCatalogTopMenu;
