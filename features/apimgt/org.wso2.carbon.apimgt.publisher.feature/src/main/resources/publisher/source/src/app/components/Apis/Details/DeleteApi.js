import React from 'react'
import {resourceMethod, resourcePath, ScopeValidation} from '../../../data/ScopeValidation'
import ApiPermissionValidation from '../../../data/ApiPermissionValidation'

import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle} from 'material-ui/Dialog';
import Button from 'material-ui/Button';
import { Delete } from '@material-ui/icons/';
import Slide from "material-ui/transitions/Slide";
import Alert from '../../Shared/Alert'


class DeleteApi extends React.Component{
    constructor(props){
        super(props);
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
        this.state = {openMenu: false};
    }
    handleRequestClose() {
        this.setState({openMenu: false});
    };

    handleRequestOpen() {
        this.setState({openMenu: true});
    };

    handleApiDelete(e) {
        this.setState({loading: true});
        const api = new Api();
        let promised_delete = api.deleteAPI(this.api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    Alert.info("Lifecycle state updated successfully");

                    Alert.error("Something went wrong while deleting the API!");
                    this.setState({loading: false});
                    return;
                }
                let redirect_url = "/apis/";
                this.props.history.push(redirect_url);
                Alert.success("API " + this.state.api.name + " was deleted successfully!");
                this.setState({active: false, loading: false});
            }
        );
    }
    render(){
        const api = this.props.api;
        return(
            <div>
                {/* allowing delet based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.DELETE}
                                 resourcePath={resourcePath.SINGLE_API}>
                    <ApiPermissionValidation
                        checkingPermissionType={ApiPermissionValidation.permissionType.DELETE}
                        userPermissions={api.userPermissionsForApi}>
                        <Button onClick={this.handleRequestOpen}
                                variant="raised" size="small"
                                aria-haspopup="true">
                            <Delete/>
                        </Button>
                    </ApiPermissionValidation>
                </ScopeValidation>
                <Dialog open={this.state.openMenu} transition={Slide}>
                    <DialogTitle>
                        {"Confirm"}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Are you sure you want to delete the API ({api.name} - {api.version})?
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button dense color="primary" onClick={this.handleApiDelete}>
                            Delete
                        </Button>
                        <Button dense color="primary" onClick={this.handleRequestClose}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        )
    }
}
export default DeleteApi;