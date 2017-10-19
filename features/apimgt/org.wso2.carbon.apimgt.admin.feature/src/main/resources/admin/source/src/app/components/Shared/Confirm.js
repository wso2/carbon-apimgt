/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 
import React from 'react'
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';

class Confirm extends React.Component{
    constructor(props){
        super(props);
        this.state(
            {
                open: false
            }
        )
    }
    handleRequestClose(action){
        this.setState({ open: false });
        action === "ok" ? this.props.callback(true) : this.props.callback(false);
    }
    render(props){
        return(
            <Dialog open={this.state.open} onRequestClose={this.handleRequestClose}>
                <DialogTitle>
                    { props.title ? props.title : 'Please Confirm' }
                </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        { props.message ? props.message : 'Are you sure?' }
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => this.handleRequestClose("cancel")} color="primary">
                        { props.labelCancel ? props.labelCancel : 'Cancel'}
                    </Button>
                    <Button onClick={() => this.handleRequestClose("ok")} color="primary">
                        { props.labelOk ? props.labelOk : 'OK'}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }
}

export default Confirm;
