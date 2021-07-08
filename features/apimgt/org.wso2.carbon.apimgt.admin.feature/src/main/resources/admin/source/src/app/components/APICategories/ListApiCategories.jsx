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

import React, {useState} from 'react';
import API from 'AppData/api';
import { useIntl, FormattedMessage } from 'react-intl';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import Delete from 'AppComponents/APICategories/DeleteAPICategory';
import AddEdit from 'AppComponents/APICategories/AddEditAPICategory';
import EditIcon from '@material-ui/icons/Edit';
import WarningBase from "AppComponents/AdminPages/Addons/WarningBase";

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListApiCategories() {
    const intl = useIntl();
    const [hasListApiCategoriesPermission, setHasListApiCategoriesPermission] = useState(true);

    /**
     * API call to get api category list
     * @returns {Promise}.
     */
    function apiCall() {
        const restApi = new API();
        return restApi
            .apiCategoriesListGet()
            .then((result) => {
                return result.body.list;
            })
            .catch((error) => {
                if (error.statusCode === 401) {
                    setHasListApiCategoriesPermission(false);
                } else {
                    setHasListApiCategoriesPermission(true);
                    throw error;
                }
            });
    }

    const columProps = [
        { name: 'id', options: { display: false } },
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'AdminPages.ApiCategories.table.header.category.name',
                defaultMessage: 'Category Name',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'AdminPages.ApiCategories.table.header.category.description',
                defaultMessage: 'Description',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'numberOfAPIs',
            label: intl.formatMessage({
                id: 'AdminPages.ApiCategories.table.header.category.number.of.apis',
                defaultMessage: 'Number of APIs',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
    ];
    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'AdminPages.ApiCategories.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add API Category',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'AdminPages.ApiCategories.List.addButtonProps.title',
            defaultMessage: 'Add API Category',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.ApiCategories.List.search.default',
            defaultMessage: 'Search by API Category name',
        }),
        active: true,
    };
    const pageProps = {
        help: (
            <HelpBase>
                <List component='nav' aria-label='main mailbox folders'>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={
                                Configurations.app.docUrl
                                + 'develop/customizations/customizing-the-developer-portal/customize-api-listing/'
                                + 'categorizing-and-grouping-apis/api-category-based-grouping/'
                            }
                        >
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        id='AdminPages.ApiCategories.List.help.link.one'
                                        defaultMessage='API Category based Grouping'
                                    />
                                )}
                            />
                        </Link>
                    </ListItem>
                </List>
            </HelpBase>
        ),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'AdminPages.ApiCategories.List.title.apicategories',
            defaultMessage: 'API Categories',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.ApiCategories.List.empty.content.apicategories'
                    defaultMessage={
                        'You can use API categories to group APIs. In previous versions of WSO2 API Manager, '
                        + 'the process of grouping APIs was carried out by using tag-wise groups.'
                        + ' Unlike tag-wise grouping, API categories do not use a naming convention.'
                        + ' Therefore, the admin does not need to take into consideration any naming'
                        + ' conventions when using API category-based grouping.'
                    }
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.ApiCategories.List.empty.title.apicategories'
                    defaultMessage='API Categories'
                />
            </Typography>
        ),
    };

    if (!hasListApiCategoriesPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'ApiCategories.List.title.apiCategories',
                        defaultMessage: 'API Categories',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='ApiCategories.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='ApiCategories.List.permission.denied.content'
                        defaultMessage={'You dont have enough permission to view Api Categories.'
                        + ' Please contact the site administrator.'}
                    />
                )}
            />
        );
    } else {
        return (
            <ListBase
                columProps={columProps}
                pageProps={pageProps}
                addButtonProps={addButtonProps}
                searchProps={searchProps}
                emptyBoxProps={emptyBoxProps}
                apiCall={apiCall}
                EditComponent={AddEdit}
                editComponentProps={{
                    icon: <EditIcon/>,
                    title: 'Edit API Category',
                }}
                DeleteComponent={Delete}
            />
        );
    }
}
