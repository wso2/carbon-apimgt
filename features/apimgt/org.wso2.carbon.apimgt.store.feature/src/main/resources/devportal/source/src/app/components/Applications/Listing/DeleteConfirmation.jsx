import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core/';
import Slide from '@material-ui/core/Slide';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

const DeleteConfirmation = (props) => {
    const { handleAppDelete, isDeleteOpen, toggleDeleteConfirmation } = props;
    return (
        <Dialog open={isDeleteOpen} transition={Slide}>
            <DialogTitle>
                <FormattedMessage
                    id='Applications.Listing.DeleteConfirmation.dialog.title'
                    defaultMessage='Delete Application'
                />
            </DialogTitle>
            <DialogContent>
                <DialogContentText>
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog.text.description'
                        defaultMessage='The application will be removed'
                    />
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button dense onClick={toggleDeleteConfirmation}>
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog.cancel'
                        defaultMessage='Cancel'
                    />
                </Button>
                <Button
                    size='small'
                    variant='outlined'
                    color='primary'
                    onClick={handleAppDelete}
                >
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog,delete'
                        defaultMessage='Delete'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
};
DeleteConfirmation.propTypes = {
    handleAppDelete: PropTypes.func.isRequired,
    isDeleteOpen: PropTypes.bool.isRequired,
    toggleDeleteConfirmation: PropTypes.func.isRequired,
};
export default DeleteConfirmation;
