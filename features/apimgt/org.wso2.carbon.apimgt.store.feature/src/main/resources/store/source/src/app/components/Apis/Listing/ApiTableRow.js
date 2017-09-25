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
import {Link} from 'react-router-dom'
import API from '../../../data/api'

import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import Typography from 'material-ui/Typography';


class APiTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {active: true, loading: false, open: false};
        this.handleApiDelete = this.handleApiDelete.bind(this);
    }

    handleRequestClose = () => {
        this.setState({ openUserMenu: false });
    };

    handleApiDelete(e) {
        this.setState({loading: true});
        const api = new API();
        const api_uuid = this.props.api.id;
        const name = this.props.api.name;
        let promised_delete = api.deleteAPI(api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    message.error("Something went wrong while deleting the " + name + " API!");
                    this.setState({open: false});
                    return;
                }
                message.success(name + " API deleted successfully!");
                this.setState({active: false, loading: false});
            }
        );
    }

    render() {
        let details_link = "/apis/" + this.props.api.id;
        const {name, version, context, description} = this.props.api;
        if (!this.state.active) { // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }
        return (
            <TableRow hover>
                <TableCell>{name}</TableCell>
                <TableCell numeric>{version}</TableCell>
                <TableCell>{context}</TableCell>
                <TableCell style={{ whiteSpace: "normal", wordWrap: "break-word"}}>
                    <Typography>
                        {description}
                    </Typography>
                </TableCell>
                <TableCell>
                    <Link to={details_link}>More...</Link>
                </TableCell>
            </TableRow>
        );
    }
}
export default APiTableRow
