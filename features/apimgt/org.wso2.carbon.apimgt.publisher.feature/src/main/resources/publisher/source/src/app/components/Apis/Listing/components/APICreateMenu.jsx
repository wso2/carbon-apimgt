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
    List, ListItem, ListItemText, Divider, Collapse,
} from '@material-ui/core/';
import { makeStyles } from '@material-ui/styles';
import { Link } from 'react-router-dom';

import MenuButton from 'AppComponents/Shared/MenuButton';
import { FormattedMessage } from 'react-intl';
import AuthManager from 'AppData/AuthManager';
import { ExpandLess, ExpandMore } from '@material-ui/icons';

const useStyles = makeStyles((theme) => ({
    links: {
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    root: {
        backgroundColor: theme.palette.background.paper,
    },
    nested: {
        paddingLeft: theme.spacing(4),
    },
}));

const APICreateMenu = (props) => {
    const classes = useStyles();
    const [openRest, setOpenRest] = React.useState(false);
    const [openWS, setOpenWS] = React.useState(false);
    const handleClickRest = () => {
        setOpenRest(!openRest);
    };
    const handleClickWS = () => {
        setOpenWS(!openWS);
    };

    const createTypes = (
        <List style={{ minWidth: '325px' }} className={classes.root}>
            <ListItem button onClick={handleClickRest}>
                <ListItemText
                    primary={(
                        <FormattedMessage
                            id='Apis.Listing.components.APICreateMenu.primary.restMain'
                            defaultMessage='Create a REST API'
                        />
                    )}
                    secondary={(
                        <FormattedMessage
                            id='Apis.Listing.components.APICreateMenu.secondary.restMain'
                            defaultMessage='Design or Import a REST API'
                        />
                    )}
                />
                {openRest ? <ExpandLess /> : <ExpandMore /> }
            </ListItem>
            <Collapse in={openRest} timeout='auto' unmountOnExit>
                <List>
                    <ListItem button className={classes.nested}>
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
                    <ListItem button className={classes.nested}>
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
                </List>
            </Collapse>
            <Divider />
            <ListItem button onClick={handleClickWS}>
                <ListItemText
                    primary={(
                        <FormattedMessage
                            id='Apis.Listing.components.APICreateMenu.primary.wsMain'
                            defaultMessage='Create a WebSocket API'
                        />
                    )}
                    secondary={(
                        <FormattedMessage
                            id='Apis.Listing.components.APICreateMenu.secondary.wsMain'
                            defaultMessage='Design or Import a WebSocket API'
                        />
                    )}
                />
                {openWS ? <ExpandLess /> : <ExpandMore /> }
            </ListItem>
            <Collapse in={openWS} timeout='auto' unmountOnExit>
                <ListItem button className={classes.nested}>
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
                <Divider />
                <ListItem button className={classes.nested}>
                    <Link to='/apis/create/asyncApi' className={classes.links}>
                        <ListItemText
                            primary={(
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.primary.asyncApi'
                                    defaultMessage='I Have an Existing WebSocket API'
                                />
                            )}
                            secondary={(
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.secondary.asyncApi'
                                    defaultMessage='Use an existing AsyncAPI 2.0 definition file or URL'
                                />
                            )}
                        />
                    </Link>
                </ListItem>
            </Collapse>
            <Divider />
            <ListItem button>
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
            <ListItem button>
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
        </List>
    );
    return !AuthManager.isNotCreator() && <MenuButton {...props} menuList={createTypes} />;
};

APICreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]).isRequired,
};
export default APICreateMenu;
