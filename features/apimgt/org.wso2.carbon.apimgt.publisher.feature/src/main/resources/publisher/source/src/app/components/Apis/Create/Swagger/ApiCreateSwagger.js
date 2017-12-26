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
import React from 'react';
import API from '../../../../data/api.js'
import './ApiCreateSwagger.css'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../../data/ScopeValidation'
import Dropzone from 'react-dropzone'


import Radio, { RadioGroup } from 'material-ui/Radio';
import { FormLabel, FormControl, FormControlLabel } from 'material-ui/Form';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import ArrowDropDown from 'material-ui-icons/ArrowDropDown';
import ArrowDropUp from 'material-ui-icons/ArrowDropUp';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Snackbar from 'material-ui/Snackbar';
import IconButton from 'material-ui/IconButton';
import CloseIcon from 'material-ui-icons/Close';



class ApiCreateSwagger extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            uploadMethod:'file',
            files:[],swaggerUrl:'',
            open:false,
            message:''
        };
        this.onDrop = this.onDrop.bind(this);
        this.swaggerUrlChange = this.swaggerUrlChange.bind(this);
    }
    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
    };
    swaggerUrlChange(e){
        this.setState({swaggerUrl:e.target.value});
    }
    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit = (e) => {
        e.preventDefault();
        let input_type = this.state.uploadMethod;
        if (input_type === "url") {
            let url = this.state.swaggerUrl;
            if(url === ""){
                console.debug("Swagger Url is empty.");
                this.setState({ message: "Swagger Url is empty." });
                this.setState({ open: true });
                return;
            }
            let data = {};
            data.url = url;
            data.type = 'swagger-url';
            let new_api = new API('');
            new_api.create(data)
                .then(this.createAPICallback)
                .catch(
                    function (error_response) {
                        let error_data = JSON.parse(error_response.data);
                        let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                        this.setState({ message: messageTxt });
                        this.setState({ open: true });
                        console.debug(error_response);
                    });
        } else if (input_type === "file") {
            if(this.state.files.length === 0){
                this.setState({ message: "Select a swagger file to upload." });
                this.setState({ open: true });
                console.log("Select a swagger file to upload.");
                return;
            }
            let swagger = this.state.files[0];
            let new_api = new API('');
            new_api.create(swagger)
                .then(this.createAPICallback)
                .catch(
                    error_response => {
                        let error_data;
                        let messageTxt;
                        if (error_response.obj) {
                            error_data = error_response.obj;
                            messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                        } else {
                            error_data = error_response.data;
                            messageTxt = "Error: " + error_data + ".";

                        }
                        this.setState({ message: messageTxt });
                        this.setState({ open: true });
                        console.debug(error_response);
                    });
        }

    };

    handleUploadMethodChange = (e, value) => {
        this.setState({uploadMethod:value});
    };

    onDrop(files) {
        this.setState({
            files
        });
    }
    handleRequestClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }

        this.setState({ open: false });
    };

    render(){


        return(
            <Grid container>
                <Grid item xs={12}>

                    <Paper>
                        <Typography className="page-title" type="display2" gutterBottom>
                            Create New API - { this.state.uploadMethod === "file" ? <span>Swagger file upload</span> :
                                <span>By swagger url</span> }
                        </Typography>
                        <Typography type="caption" gutterBottom align="left" className="page-title-help">
                            Fill the mandatory fields (Name, Version, Context) and create the API. Configure advanced
                            configurations later.
                        </Typography>

                    </Paper>
                </Grid>
                <Grid item xs={12}  className="page-content">
                    <form onSubmit={this.handleSubmit} className="login-form">
                        <AppBar position="static" color="default">
                            <Toolbar>
                                <RadioGroup
                                    aria-label="inputType"
                                    name="inputType"
                                    value={this.state.uploadMethod}
                                    onChange={this.handleUploadMethodChange}
                                    className="horizontal"
                                >
                                    <FormControlLabel value="file" control={<Radio />} label="File" />
                                    <FormControlLabel value="url" control={<Radio />} label="Url" />
                                </RadioGroup>
                            </Toolbar>
                        </AppBar>

                        { this.state.uploadMethod === "file" &&
                        <FormControl className="horizontal dropzone-wrapper">
                            <div className="dropzone">
                                <Dropzone onDrop={this.onDrop} multiple={false}>
                                    <p>Try dropping some files here, or click to select files to upload.</p>
                                </Dropzone>
                            </div>
                            <aside>
                                <h2>Uploaded files</h2>
                                <ul>
                                    {
                                        this.state.files.map(f => <li key={f.name}>{f.name} - {f.size} bytes</li>)
                                    }
                                </ul>
                            </aside>
                        </FormControl> }
                        { this.state.uploadMethod === "url" &&
                        <FormControl className="horizontal full-width">
                            <TextField
                                id="swaggerUrl"
                                label="Swagger Url"
                                type="text"
                                name="swaggerUrl"
                                margin="normal"
                                style={{width:"100%"}}
                                value={this.state.swaggerUrl}
                                onChange={this.swaggerUrlChange}
                            />
                        </FormControl> }
                        <FormControl className="horizontal">
                          {/* Allowing to create an API from swagger definition, based on scopes */}
                            <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.APIS}>
                                <Button raised color="primary" type="submit" className="button-left">
                                    Create
                                </Button>
                            </ScopeValidation>
                            <Button raised onClick={() => this.props.history.push("/api/create/home")}>
                                Cancel
                            </Button>

                        </FormControl>

                    </form>
                    <Snackbar
                        anchorOrigin={{
                            vertical: 'top',
                            horizontal: 'center',
                        }}
                        open={this.state.open}
                        autoHideDuration={6e3}
                        onClose={this.handleRequestClose}
                        SnackbarContentProps={{
                            'aria-describedby': 'message-id',
                        }}
                        message={<span id="message-id">{this.state.message}</span>}
                        action={[
                            <IconButton
                                key="close"
                                aria-label="Close"
                                color="inherit"
                                onClick={this.handleRequestClose}
                            >
                                <CloseIcon />
                            </IconButton>,
                        ]}
                    />
                </Grid>
            </Grid>
        );
    }
}


export default ApiCreateSwagger;
