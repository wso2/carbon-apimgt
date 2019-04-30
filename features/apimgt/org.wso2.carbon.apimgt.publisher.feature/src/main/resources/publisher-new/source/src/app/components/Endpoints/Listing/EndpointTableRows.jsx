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

import React from 'react';
import { Link } from 'react-router-dom';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Dialog from '@material-ui/core/Dialog';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogActions from '@material-ui/core/DialogActions';
import Button from '@material-ui/core/Button';
import Delete from '@material-ui/icons/Delete';
import Slide from '@material-ui/core/Slide';
import IconButton from '@material-ui/core/IconButton';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';

const styles = () => ({});

/**
 *
 *
 * @class EndpointTableRows
 * @extends {React.Component}
 */
class EndpointTableRows extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            openMenu: false,
        };
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
    }

    handleRequestClose() {
        this.setState({ openMenu: false });
    }

    handleRequestOpen() {
        this.setState({ openMenu: true });
    }

    /**
     *
     *
     * @returns
     * @memberof EndpointTableRows
     */
    render() {
        const {
            name, type, maxTps, endpointConfig, id,
        } = this.props.endpoint;
        return (
            <TableRow hover>
                <TableCell>
                    <Link to={'/endpoints/' + id}>{name}</Link>
                </TableCell>
                <TableCell>{type}</TableCell>
                <TableCell>{endpointConfig.list[0].url}</TableCell>
                <TableCell>{maxTps}</TableCell>
                <TableCell>
                    <div>
                        <ScopeValidation resourceMethod={resourceMethod.PUT} resourcePath={resourcePath.SINGLE_API}>
                            <IconButton aria-label='Delete' onClick={this.handleRequestOpen}>
                                <Delete />
                            </IconButton>
                        </ScopeValidation>
                        <Dialog open={this.state.openMenu} transition={Slide}>
                            <DialogTitle>Confirm</DialogTitle>
                            <DialogContent>
                                <DialogContentText>Are you sure you want to delete the Endpoint?</DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button dense color='primary' onClick={this.handleRequestClose}>
                                    Cancel
                                </Button>
                                <Button dense color='primary' onClick={() => this.props.handleEndpointDelete(id, name)}>
                                    Delete
                                </Button>
                            </DialogActions>
                        </Dialog>
                    </div>
                </TableCell>
            </TableRow>
        );
    }
}
EndpointTableRows.propTypes = {
    endpoint: PropTypes.shape({
        id: PropTypes.string,
        maxTps: PropTypes.number,
        name: PropTypes.string,
        type: PropTypes.string,
        endpointConfig: PropTypes.shape({}),
    }).isRequired,
    handleEndpointDelete: PropTypes.func.isRequired,
};

export default withStyles(styles)(EndpointTableRows);
