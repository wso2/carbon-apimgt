import React from 'react';
import PropTypes from 'prop-types';
import { List, ListItem, ListItemText, Divider } from '@material-ui/core/';
import MenuList from '@material-ui/core/MenuList';
import { Link } from 'react-router-dom';

import MenuButton from 'AppComponents/Shared/MenuButton';

const APICreateMenu = (props) => {
    const createTypes = (
        <MenuList>
            <List>
                <ListItem>
                    <Link to='/apis/create/rest'>
                        <ListItemText primary='Design a New REST API' secondary='Design and prototype a new REST API' />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/swagger'>
                        <ListItemText
                            primary='I Have an Existing REST API'
                            secondary='Use an existing REST endpoint or Swagger definition'
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/wsdl'>
                        <ListItemText
                            primary='I Have a SOAP Endpoint'
                            secondary='Use an existing SOAP or Import the WSDL'
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to='/apis/create/rest'>
                        <ListItemText
                            primary='Design New Websocket API'
                            secondary='Design and prototype a new WebSocket API'
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
