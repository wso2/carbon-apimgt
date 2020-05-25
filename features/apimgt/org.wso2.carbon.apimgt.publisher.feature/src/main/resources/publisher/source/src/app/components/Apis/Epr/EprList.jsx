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
import { useIntl, FormattedMessage } from 'react-intl';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/Apis/Epr/Addons/HelpBase';
import ListBase from 'AppComponents/Apis/Epr/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import EprAPI from 'AppData/Epr';
import CreateApi from 'AppComponents/Apis/Epr/CreateApi';
/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    const eprAPI = new EprAPI();
    return eprAPI.getRegistries({ limit: 10, offset: 0 });
}

const columProps = [
    {
        name: 'name',
        label: 'Name',
        options: {
            filter: true,
            sort: true,
        },
    },
    {
        name: 'type',
        label: 'Type',
        options: {
            filter: true,
            sort: false,
        },
    },
    {
        name: 'owner',
        label: 'Owner',
        options: {
            filter: true,
            sort: false,
        },
    },
    {
        name: '',
        label: 'Actions',
        options: {
            filter: false,
            sort: false,
            customBodyRender: (value, tableMeta) => {
                return (
                    tableMeta && tableMeta.rowData && <CreateApi id={tableMeta.rowData[tableMeta.rowData.length - 1]} dataRow={tableMeta.rowData} />
                    // <Button variant='outlined' onClick={() => console.info(tableMeta, )}>Default</Button>
                );
            },
            setCellProps: () => {
                return {
                    style: { width: 200 },
                };
            },
        },
    },
    {
        name: 'id',
        label: 'Owner',
        options: {
            display: false,
        },
    },
];


/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListMG() {
    const intl = useIntl();

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Apis.Epr.EprList.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Endpoint',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Apis.Epr.EprList.addButtonProps.title',
            defaultMessage: 'Add Endpoint',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Apis.Epr.EprList.search.default',
            defaultMessage: 'Search by Endpoint label',
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
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Apis.Epr.EprList.help.link.one'
                                    defaultMessage='Create a Microgateway label'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Apis.Epr.EprList.help.link.two'
                                    defaultMessage='Assign the Microgateway label to an API'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Apis.Epr.EprList.help.link.three'
                                    defaultMessage='View the Microgateway labels'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        /*
        pageStyle='half' center part of the screen.
        pageStyle='full' = Take the full content area.
        pageStyle='paperLess' = Avoid from displaying background paper. ( For dashbord we need this )
        */
        pageStyle: 'full',
        title: intl.formatMessage({
            id: 'Apis.Epr.EprList.title.microgateways',
            defaultMessage: 'Microgateways',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Apis.Epr.EprList.empty.content.microgateways'
                    defaultMessage={'It is possible to create a Microgateway distribution '
                    + 'for a group of APIs. In order to group APIs, a label needs to be created'
                    + ' and attached to the APIs that need to be in a single group.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Apis.Epr.EprList.empty.title.microgateways'
                    defaultMessage='Microgateways'
                />

            </Typography>),
    };
    /*
    If the add button wants to route to a new page, we need to override the Button component completely.
    Send the following prop to ListBase component.
    import { Link as RouterLink } from 'react-router-dom';
    import Button from '@material-ui/core/Button';

    const addButtonOverride = (
        <RouterLink to='/'>
            <Button variant='contained' color='primary'>
                <FormattedMessage
                    id='Apis.Epr.EprList.help.link.one'
                    defaultMessage='Create a Microgateway label'
                />
            </Button>
        </RouterLink>
    );
    */
    /* *************************************************************** */
    /* To override the no data message send the following with the props to ListBase
    const noDataMessage = (
        <FormattedMessage
            id='AdminPages.Addons.ListBase.nodata.message'
            defaultMessage='No items yet'
        />
    )
    /* **************************************************************** */
    /*
    Send the following props to ListBase to override the action column.


    To disable the Edit button pass an empty component. Ex EditComponent={() => <span />}
    To disable the Delete button pass an empty component. Ex DeleteComponent={() => <span />}
    To make the edit link go to a new page send a react-router-dom as the EditComponent.
    Ex:
    import { Link as RouterLink } from 'react-router-dom';
    import EditIcon from '@material-ui/icons/Edit';
    .....
    .....
    EditComponent={() => <RouterLink to='/'>
                <EditIcon />
            </RouterLink> }
    .....

    */
    return (
        <Box display='flex' width='100%' flexDirection='column'>
            <ListBase
                columProps={columProps}
                pageProps={pageProps}
                addButtonProps={addButtonProps}
                searchProps={searchProps}
                emptyBoxProps={emptyBoxProps}
                apiCall={apiCall}
                EditComponent={() => <span />}
                showActionColumn={false}
            />
        </Box>

    );
}
