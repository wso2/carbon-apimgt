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
import PropTypes from 'prop-types';
import {
    List, ListItem, ListItemText, Divider,
} from '@material-ui/core/';
import { makeStyles } from '@material-ui/styles';
import { Link } from 'react-router-dom';

import MenuButton from 'AppComponents/Shared/MenuButton';
import { FormattedMessage } from 'react-intl';
import AuthManager from 'AppData/AuthManager';

const useStyles = makeStyles((theme) => ({
    links: {
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
}));

const APICreateMenu = (props) => {
    const classes = useStyles();

    const createTypes = (
        <List>
            <ListItem>
                <Link id='itest-id-createdefault' to='/apis/create/rest' className={classes.links}>
                    <ListItemText
                        primary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.primary.rest'
                                defaultMessage='Design a New REST API'
                            />
                        )}
                        secondary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.secondary.rest'
                                defaultMessage='Design and prototype a new REST API'
                            />
                        )}
                    />
                </Link>
            </ListItem>
            <Divider />
            <ListItem>
                <Link to='/apis/create/openapi' className={classes.links}>
                    <ListItemText
                        primary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.primary.swagger'
                                defaultMessage='I Have an Existing REST API'
                            />
                        )}
                        secondary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.secondary.openapi'
                                defaultMessage='Use an existing OpenAPI definition file or URL'
                            />
                        )}
                    />
                </Link>
            </ListItem>
            <Divider />
            <ListItem>
                <Link to='/apis/create/wsdl' className={classes.links}>
                    <ListItemText
                        primary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.primary.soap'
                                defaultMessage='I Have a SOAP Endpoint'
                            />
                        )}
                        secondary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.secondary.soap'
                                defaultMessage='Use an existing SOAP or import the WSDL'
                            />
                        )}
                    />
                </Link>
            </ListItem>
            <Divider />
            <ListItem>
                <Link to='/apis/create/graphQL' className={classes.links}>
                    <ListItemText
                        primary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.primary.graphql'
                                defaultMessage='I Have a GraphQL SDL schema'
                            />
                        )}
                        secondary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.secondary.graphql'
                                defaultMessage='Import a GraphQL SDL schema'
                            />
                        )}
                    />
                </Link>
            </ListItem>
            <Divider />
            <ListItem>
                <Link to='/apis/create/ws' className={classes.links}>
                    <ListItemText
                        primary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.primary.ws'
                                defaultMessage='Design New WebSocket API'
                            />
                        )}
                        secondary={(
                            <FormattedMessage
                                id='Apis.Listing.components.APICreateMenu.secondary.ws'
                                defaultMessage='Design and prototype a new WebSocket API'
                            />
                        )}
                    />
                </Link>
            </ListItem>
        </List>
    );
    return !AuthManager.isNotCreator() && <MenuButton {...props} menuList={createTypes} />;
};

APICreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]).isRequired,
};
export default APICreateMenu;
