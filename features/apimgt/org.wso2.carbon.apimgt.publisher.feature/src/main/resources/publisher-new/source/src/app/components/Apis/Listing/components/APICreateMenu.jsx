import React from 'react';
import PropTypes from 'prop-types';
import { List, ListItem, ListItemText, Divider } from '@material-ui/core/';
import MenuList from '@material-ui/core/MenuList';
import { Link } from 'react-router-dom';

import MenuButton from 'AppComponents/Shared/MenuButton';
import { FormattedMessage } from 'react-intl';

const APICreateMenu = (props) => {
    const createTypes = (
        <MenuList>
            <List>
                <ListItem>
                    <Link to='/apis/create/rest'>
                        <ListItemText
                            primary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.primary.rest'
                                    defaultMessage='Design a New REST API'
                                />
                            }
                            secondary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.secondary.rest'
                                    defaultMessage='Design and prototype a new REST API'
                                />
                            }
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/openapi'>
                        <ListItemText
                            primary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.primary.swagger'
                                    defaultMessage='I Have an Existing REST API'
                                />
                            }
                            secondary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.secondary.swagger'
                                    defaultMessage='Use an existing REST endpoint or Swagger definition'
                                />
                            }
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/wsdl'>
                        <ListItemText
                            primary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.primary.soap'
                                    defaultMessage='I Have a SOAP Endpoint'
                                />
                            }
                            secondary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.secondary.soap'
                                    defaultMessage='Use an existing SOAP or Import the WSDL'
                                />
                            }
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/graphQL'>
                        <ListItemText
                            primary='I Have a GraphQL SDL schema'
                            secondary='Import a GraphQL SDL schema'
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/ws'>
                        <ListItemText
                            primary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.primary.ws'
                                    defaultMessage='Design New Websocket API'
                                />
                            }
                            secondary={
                                <FormattedMessage
                                    id='Apis.Listing.components.APICreateMenu.secondary.ws'
                                    defaultMessage='Design and prototype a new WebSocket API'
                                />
                            }
                        />
                    </Link>
                </ListItem>
            </List>
        </MenuList>
    );
    return <MenuButton {...props} menuList={createTypes} />;
};

APICreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]).isRequired,
};
export default APICreateMenu;
