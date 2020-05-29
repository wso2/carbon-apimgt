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
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import Delete from 'AppComponents/AdminPages/Microgateways/Delete';
import EditIcon from '@material-ui/icons/Edit';
import { Link as RouterLink } from 'react-router-dom';
import IconButton from '@material-ui/core/IconButton';

/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    return new Promise(((resolve) => {
        setTimeout(() => {
            resolve([
                {
                    id: '1', name: '10KPerMin', quotaPolicy: 'requestCount', quota: '10000',
                },
                {
                    id: '1', name: '20KPerMin', quotaPolicy: 'requestCount', quota: '20000',
                },
                {
                    id: '1', name: '50KPerMin', quotaPolicy: 'requestCount', quota: '50000',
                },

            ]);
        }, 1000);
    }));
}

const columProps = [
    // {
    //     name: 'data',
    //     options: {
    //         display: 'excluded',
    //         filter: false,
    //     },
    // },
    {
        name: 'name',
        options: {
            customBodyRender: (value, tableMeta) => {
                if (tableMeta.rowData) {
                    // const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                    return <RouterLink to='/throttling/advanced/polcyid'>{value}</RouterLink>;
                } else {
                    return <div />;
                }
            },
            filter: false,
            sort: true,
        },
    },
    {
        name: 'quotaPolicy',
        label: 'Quota Policy',
    },
    {
        name: 'quota',
        label: 'Quota',
    },
    {
        name: 'quota',
        label: 'Quota',
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
            id: 'Throttling.Advanced.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Advanced.List.addButtonProps.title',
            defaultMessage: 'Add Policy',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Advanced.List.search.default',
            defaultMessage: 'Search by policy name',
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
                                + `learn/rate-limiting/
                                introducing-throttling-use-cases/`}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Advanced.List.help.link.one'
                                    defaultMessage='Introducing Throttling Use-Cases'
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
                                + `learn/rate-limiting/adding-new-throttling-policies/
                                #adding-a-new-advanced-throttling-policy`}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Advanced.List.help.link.two'
                                    defaultMessage='Adding a new advanced throttling policy'
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
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Advanced.List.title.main',
            defaultMessage: 'Advanced Throttling Policies',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Throttling.Advanced.List.empty.content'
                    defaultMessage={'It is possible to create a Microgateway distribution '
                        + 'for a group of APIs. In order to group APIs, a label needs to be created'
                        + ' and attached to the APIs that need to be in a single group.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Advanced.List.empty.title'
                    defaultMessage='Advanced Throttling Policies'
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
                    id='Throttling.Advanced.List.help.link.one'
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
        <ListBase
            columProps={columProps}
            pageProps={pageProps}
            addButtonProps={addButtonProps}
            searchProps={searchProps}
            emptyBoxProps={emptyBoxProps}
            apiCall={apiCall}
            EditComponent={() => (
                <RouterLink to='/throttling/advanced/polcyid'>
                    <IconButton color='primary' component='span'>
                        <EditIcon />
                    </IconButton>
                </RouterLink>
            )}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Edit Policy',
            }}
            DeleteComponent={Delete}
        />
    );
}
