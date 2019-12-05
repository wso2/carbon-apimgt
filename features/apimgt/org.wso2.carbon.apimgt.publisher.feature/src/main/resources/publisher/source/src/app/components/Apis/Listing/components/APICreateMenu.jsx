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
                                id='Apis.Listing.components.APICreateMenu.secondary.swagger'
                                defaultMessage='Use an existing OpenAPI definition (swagger) file or URL'
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
