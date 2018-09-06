import React from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core/';
import DeleteIcon from '@material-ui/icons/Delete';
import Slide from '@material-ui/core/Slide';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import { resourceMethod, resourcePath, ScopeValidation } from '../../../../data/ScopeValidation';
import ApiPermissionValidation from '../../../../data/ApiPermissionValidation';
import Alert from '../../../Shared/Alert';

/**
 * Handle Delete an API from API Overview/Details page
 *
 * @class DeleteApiButton
 * @extends {React.Component}
 */
class DeleteApiButton extends React.Component {
    /**
     *Creates an instance of DeleteApiButton.
     * @param {*} props @inheritDoc
     * @memberof DeleteApiButton
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
     * @memberof DeleteApiButton
     */
    handleRequestClose() {
        this.setState({ openMenu: false });
    }

    /**
     * Handle Delete button onClick event
     *
     * @memberof DeleteApiButton
     */
    handleRequestOpen() {
        this.setState({ openMenu: true });
    }

    /**
     *
     * Send API delete REST API request
     * @param {*} e
     * @memberof DeleteApiButton
     */
    handleApiDelete() {
        const { api, history } = this.props;
        api.delete().then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.error('Something went wrong while deleting the API!');
                return;
            }
            const redirectURL = '/apis';
            Alert.success('API ' + api.name + ' was deleted successfully!');
            history.push(redirectURL);
        });
    }

    /**
     *
     * @inheritDoc
     * @returns {React.Component} inherit docs
     * @memberof DeleteApiButton
     */
    render() {
        const { api, buttonClass } = this.props;
        return (
            <React.Fragment>
                {/* allowing delete based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={resourcePath.SINGLE_API}>
                    <ApiPermissionValidation
                        checkingPermissionType={ApiPermissionValidation.permissionType.DELETE}
                        userPermissions={api.userPermissionsForApi}
                    >
                        <Button
                            onClick={this.handleRequestOpen}
                            size='small'
                            aria-haspopup='true'
                            className={buttonClass}
                        >
                            <DeleteIcon color='secondary' />
                            Delete
                        </Button>
                    </ApiPermissionValidation>
                </ScopeValidation>
                <Dialog open={this.state.openMenu} transition={Slide}>
                    <DialogTitle>Confirm</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Are you sure you want to delete the API ({api.name} - {api.version}
                            )?
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button dense variant='outlined' color='secondary' onClick={this.handleApiDelete}>
                            Delete
                        </Button>
                        <Button dense onClick={this.handleRequestClose}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </React.Fragment>
        );
    }
}

DeleteApiButton.defaultProps = {
    buttonClass: '',
};

DeleteApiButton.propTypes = {
    api: PropTypes.shape({
        delete: PropTypes.func,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    buttonClass: PropTypes.string,
};

export default withRouter(DeleteApiButton);
