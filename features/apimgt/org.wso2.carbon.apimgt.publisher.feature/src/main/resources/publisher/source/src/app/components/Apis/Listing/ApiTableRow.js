/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import {Link} from 'react-router-dom'

import {TableCell, TableRow} from 'material-ui/Table';
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle} from 'material-ui/Dialog';
import Button from 'material-ui/Button';
import Delete from 'material-ui-icons/Delete';
import Slide from "material-ui/transitions/Slide";


class ApiTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {active: true, loading: false, open: false, openMenu: false};
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
    }

    handleRequestClose() {
        this.setState({openMenu: false});
    };

    handleRequestOpen() {
        this.setState({openMenu: true});
    };

    render() {
        const {name, context, version, id} = this.props.apis;
        return (
            <TableRow hover>
                <TableCell>
                    <Link to={"/apis/" + id}>{name}</Link></TableCell>
                <TableCell>{context}</TableCell>
                <TableCell>{version}</TableCell>
                <TableCell>
                    <div>
                        <ScopeValidation resourceMethod={resourceMethod.PUT} resourcePath={resourcePath.SINGLE_API}>
                            <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleRequestOpen}>
                                <Delete/> Delete
                            </Button>
                        </ScopeValidation>
                        <Dialog open={this.state.openMenu} transition={Slide}>
                            <DialogTitle>
                                {"Confirm"}
                            </DialogTitle>
                            <DialogContent>
                                <DialogContentText>
                                    Are you sure you want to delete the API?
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button dense color="primary" onClick={this.handleRequestClose}>
                                    Cancel
                                </Button>
                                <Button dense color="primary" onClick={() => this.props.handleApiDelete(id, name)}>
                                    Delete
                                </Button>
                            </DialogActions>
                        </Dialog></div>
                </TableCell>
            </TableRow>
        );
    }
}

export default ApiTableRow;
