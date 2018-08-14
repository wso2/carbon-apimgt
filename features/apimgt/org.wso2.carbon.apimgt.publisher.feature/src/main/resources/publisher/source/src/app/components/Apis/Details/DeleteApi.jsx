import React from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core/';
import { Delete } from '@material-ui/icons/';
import Slide from '@material-ui/core/Slide';
import PropTypes from 'prop-types';

import { resourceMethod, resourcePath, ScopeValidation } from '../../../data/ScopeValidation';
import ApiPermissionValidation from '../../../data/ApiPermissionValidation';
import Alert from '../../Shared/Alert';
import Api from '../../../data/APIClient.js';

/**
 * Handle Delete an API from API Overview/Details page
 *
 * @class DeleteApi
 * @extends {React.Component}
 */
class DeleteApi extends React.Component {
    /**
     *Creates an instance of DeleteApi.
     * @param {*} props @inheritDoc
     * @memberof DeleteApi
     */
    constructor(props) {
        super(props);
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
        this.state = { openMenu: false };
    }

    /**
     * Handle Delete button close event
     *
     * @memberof DeleteApi
     */
    handleRequestClose() {
        this.setState({ openMenu: false });
    }

    /**
     * Handle Delete button onClick event
     *
     * @memberof DeleteApi
     */
    handleRequestOpen() {
        this.setState({ openMenu: true });
    }

    /**
     *
     * Send API delete REST API request
     * @param {*} e
     * @memberof DeleteApi
     */
    handleApiDelete() {
        const api = new Api();
        const promisedDelete = api.deleteAPI(this.api_uuid);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info('Lifecycle state updated successfully');

                Alert.error('Something went wrong while deleting the API!');
                return;
            }
            const redirectURL = '/apis/';
            this.props.history.push(redirectURL);
            Alert.success('API ' + this.state.api.name + ' was deleted successfully!');
        });
    }

    /**
     *
     * @inheritDoc
     * @returns {React.Component} inherit docs
     * @memberof DeleteApi
     */
    render() {
        const { api } = this.props;
        return (
            <div>
                {/* allowing delet based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={resourcePath.SINGLE_API}>
                    <ApiPermissionValidation
                        checkingPermissionType={ApiPermissionValidation.permissionType.DELETE}
                        userPermissions={api.userPermissionsForApi}
                    >
                        <Button onClick={this.handleRequestOpen} variant='raised' size='small' aria-haspopup='true'>
                            <Delete />
                        </Button>
                    </ApiPermissionValidation>
                </ScopeValidation>
                <Dialog open={this.state.openMenu} transition={Slide}>
                    <DialogTitle>Confirm</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Are you sure you want to delete the API ({api.name} - {api.version})?
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button dense color='primary' onClick={this.handleApiDelete}>
                            Delete
                        </Button>
                        <Button dense color='primary' onClick={this.handleRequestClose}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }
}

DeleteApi.propTypes = {
    api: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

export default DeleteApi;
