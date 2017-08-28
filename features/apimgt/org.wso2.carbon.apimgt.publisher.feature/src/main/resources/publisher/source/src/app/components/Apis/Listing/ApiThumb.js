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

import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from 'material-ui/Dialog';
import Slide from 'material-ui/transitions/Slide';
import Grid from 'material-ui/Grid';
import DeleteIcon from 'material-ui-icons/Delete';


class ApiThumb extends React.Component {
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
        const {name, version, context} = this.props.api;
        if (!this.state.active) { // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }
        return (
            <Grid item xs={12} sm={6} md={3} lg={2} xl={2}>
                <Card>
                    <CardMedia image="/publisher/public/images/api/api-default.png">
                        <img src="/publisher/public/images/api/api-default.png" style={{width:"100%"}}/>
                    </CardMedia>
                    <CardContent>
                        <Typography type="headline" component="h2">
                            {name}
                        </Typography>
                        <Typography component="div">
                            <p>{version}</p>
                            <p>{context}</p>
                            <p className="description">{this.props.api.description}</p>
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Link to={details_link}>
                            <Button dense color="primary">
                                More...
                            </Button>
                        </Link>
                        <Dialog open={this.state.openUserMenu} transition={Slide} onRequestClose={this.handleRequestClose}>
                            <DialogTitle>
                                {"Use Google's location service?"}
                            </DialogTitle>
                            <DialogContent>
                                <DialogContentText>
                                    Are you sure you want to delete the API ({name} - {version})?
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={this.handleRequestClose} color="primary">
                                    Cancel
                                </Button>
                                //todo:Delete button should be enabled after M6 based on permission model
                            </DialogActions>
                        </Dialog>


                    </CardActions>
                </Card>
            </Grid>
        );
    }
}
export default ApiThumb
