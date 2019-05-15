import React from 'react';
import PropTypes from 'prop-types';
import { List, ListItem, ListItemText, Divider } from '@material-ui/core/';
import MenuList from '@material-ui/core/MenuList';
import { Link } from 'react-router-dom';

import MenuButton from 'AppComponents/Shared/MenuButton';
import { FormattedMessage } from 'react-intl';

const DocCreateMenu = (props) => {
    const createTypes = (
        <MenuList>
            <List>
                <ListItem>
                    <Link to=''>
                        <ListItemText
                            primary={<FormattedMessage
                                id='create.an.inline.document'
                                defaultMessage='Create an inline document'
                            />}
                            secondary={<FormattedMessage
                                id='create.a.new.inline.document.for.api'
                                defaultMessage='Create a new inline document for this api.'
                            />}
                        />
                    </Link>
                </ListItem>
                <Divider />
                <ListItem>
                    <Link to=''>
                        <ListItemText
                            primary={<FormattedMessage
                                id='create.a.document.from.file.or.url'
                                defaultMessage='Create a document from file or URL'
                            />}
                            secondary={<FormattedMessage
                                id='create.a.document.from.file.or.url.decription'
                                defaultMessage='Create a new document using a file or URL for this api.'
                            />}
                        />
                    </Link>
                </ListItem>
            </List>
        </MenuList>
    );
    return <MenuButton {...props} menuList={createTypes} />;
};

DocCreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]).isRequired,
};
export default DocCreateMenu;
