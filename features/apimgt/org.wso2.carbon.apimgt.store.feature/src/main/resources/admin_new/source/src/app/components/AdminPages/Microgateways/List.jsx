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
import AddEdit from 'AppComponents/AdminPages/Microgateways/AddEdit';
import EditIcon from '@material-ui/icons/Edit';

/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    return new Promise(((resolve) => {
        setTimeout(() => {
            resolve([
                { id: '1', label: 'West Wing', description: "It's somewhat hot" },
                { id: '2', label: 'East Wing', description: "It's cool" },
                { id: '3', label: 'South Wing', description: "It's red zone" },
                { id: '4', label: 'Noth Wing', description: "It's blue zone" },
            ]);
        }, 1000);
    }));
}

const columProps = [
    {
        name: 'label',
        label: 'Label',
        options: {
            filter: true,
            sort: true,
        },
    },
    {
        name: 'description',
        label: 'Description',
        options: {
            filter: true,
            sort: false,
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
            id: 'AdminPages.Microgateways.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Microgateway',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'AdminPages.Microgateways.List.addButtonProps.title',
            defaultMessage: 'Add Microgateway',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.Microgateways.List.search.default',
            defaultMessage: 'Search by Microgateway label',
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
                                    id='AdminPages.Microgateways.List.help.link.one'
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
                                    id='AdminPages.Microgateways.List.help.link.two'
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
                                    id='AdminPages.Microgateways.List.help.link.three'
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
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'AdminPages.Microgateways.List.title.microgateways',
            defaultMessage: 'Microgateways',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.Microgateways.List.empty.content.microgateways'
                    defaultMessage={'It is possible to create a Microgateway distribution '
                    + 'for a group of APIs. In order to group APIs, a label needs to be created'
                    + ' and attached to the APIs that need to be in a single group.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.Microgateways.List.empty.title.microgateways'
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
                    id='AdminPages.Microgateways.List.help.link.one'
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
            EditComponent={AddEdit}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Edit Microgateway',
            }}
            DeleteComponent={Delete}
        />
    );
}
