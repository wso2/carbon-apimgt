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
                            primary={<FormattedMessage
                                id='design.a.new.rest.api'
                                defaultMessage='Design a New REST API'
                            />}
                            secondary={<FormattedMessage
                                id='design.and.prototype.a.new.rest.api'
                                defaultMessage='Design and prototype a new REST API'
                            />}
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/swagger'>
                        <ListItemText
                            primary={<FormattedMessage
                                id='i.have.an.existing.api'
                                defaultMessage='I Have an Existing REST API'
                            />}
                            secondary={<FormattedMessage
                                id='use.an.existing.rest.endpoint.or.Swagger.definition'
                                defaultMessage='Use an existing REST endpoint or Swagger definition'
                            />}
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/wsdl'>
                        <ListItemText
                            primary={<FormattedMessage
                                id='i.have.a.soap.endpoint'
                                defaultMessage='I Have a SOAP Endpoint'
                            />}
                            secondary={<FormattedMessage
                                id='use.an.existing.soap.or.import.the.wsdl'
                                defaultMessage='Use an existing SOAP or Import the WSDL'
                            />}
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/websocket'>
                        <ListItemText
                            primary={<FormattedMessage
                                id='design.new.websocket.api'
                                defaultMessage='Design New WebSocket API'
                            />}
                            secondary={<FormattedMessage
                                id='design.and.prototype.a.new.websocket.api'
                                defaultMessage='Design and prototype a new WebSocket API'
                            />}
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
