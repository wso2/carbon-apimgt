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
import 'react-toastify/dist/ReactToastify.min.css';
import Policies from '../../Details/LifeCycle/Policies.js'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../../data/ScopeValidation';

import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';
import Grid from 'material-ui/Grid';
import ArrowDropDown from 'material-ui-icons/ArrowDropDown';
import ArrowDropUp from 'material-ui-icons/ArrowDropUp';

class ApiCreateEndpoint extends React.Component {
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            api: this.api,
            apiFields: {
	    apiVersion: "1.0.0"
	    },
            validate: false,
            messageOpen: false,
            message:'',
            showMore: false
        };
        this.updateData = this.updateData.bind(this);
        this.inputChange = this.inputChange.bind(this);
        this.handleMore = this.handleMore.bind(this);
    }

    componentWillMount() {
        this.updateData();
    }
    inputChange(e){
        const field = e.target.name;
        const apiFields = this.state.apiFields;
        apiFields[field] = e.target.value;
        this.setState({apiFields:apiFields});
    }
    updateData() {
        let promised_tier = this.api.policies('api');
        promised_tier.then(response => {
            let tiers = response.obj;
            this.setState({policies: tiers});
        })
    }

    handlePolicies(policies) {
        console.info("updating policies in the api create page");
        const apiFields = this.state.apiFields;
        apiFields["selectedPolicies"] = policies;
        this.setState({apiFields:apiFields});
    }

    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
        message.success("Api Created Successfully. Now you can add resources, define endpoints etc..");
    };
    handleMore(){
        this.setState({showMore:!this.state.showMore});
    }
    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit = (e) => {
        e.preventDefault();
        const values = this.state.apiFields;
        //Check for form errors manually
        if( !values.apiName || !values.apiVersion || !values.apiContext ){
            this.setState({ messageOpen: true });
            this.setState({message: 'Please fill all required fields'});
            this.setState({validate: true});
            return;
        }

        let production = {
            type: "production",
            inline: {
                name: values.apiName + values.apiVersion.replace(/\./g, "_"), // TODO: It's better to add this name property from the REST api itself, making sure no name conflicts with other inline endpoint definitions ~tmkb
                endpointConfig: JSON.stringify({serviceUrl: values.apiEndpoint}),
                endpointSecurity: {enabled: false},
                type: "http"
            }
        };
        let api_data = {
            name: values.apiName,
            context: values.apiContext,
            version: values.apiVersion
        };
        if (values.apiEndpoint) {
            let sandbox = JSON.parse(JSON.stringify(production)); // deep coping the object
            sandbox.type = "sandbox";
            sandbox.inline.name += "_sandbox";
            api_data['endpoint'] = [production, sandbox];
        }
        let new_api = new API();
        let promised_create = new_api.create(api_data);
        promised_create
            .then(response => {
                let uuid = JSON.parse(response.data).id;
                let promisedApi = this.api.get(uuid);
                promisedApi.then(response => {
                    let api_data = JSON.parse(response.data);
                    api_data.policies = this.state.apiFields.selectedPolicies;
                    console.info("Adding policies to the api", this.state.apiFields.selectedPolicies);
                    let promised_update = this.api.update(api_data);
                    promised_update.then(response => {
                        this.createAPICallback(response);
                    })
                });
            })
            .catch(
                error_response => {
                    let error_data = JSON.parse(error_response.data);
                    let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                    this.setState({ messageOpen: true });
                    this.setState({message: messageTxt});
                });

        console.log('Send this in a POST request:', api_data);
    };


    render() {
        const props = {
            policies: this.state.policies,
            handlePolicies: this.handlePolicies.bind(this),
            selectedPolicies: this.state.apiFields.selectedPolicies
        }
        return (
            <Grid container>
                <Grid item xs={12}>
                    <Snackbar
                        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
                        open={this.state.messageOpen}
                        onRequestClose={this.handleRequestClose}
                        SnackbarContentProps={{
                            'aria-describedby': 'message-id',
                        }}
                        message={<span id="message-id">{this.state.message}</span>}
                    />
                    <Paper>
                        <Typography className="page-title" type="display2" gutterBottom>
                            Create New API
                        </Typography>
                        <Typography type="caption" gutterBottom align="left"
                                    style={{fontWeight:"300",padding:"10px 0 10px 30px",margin:"0px"}}>
                            Fill the mandatory fields (Name, Version, Context) and create the API. Configure advanced
                            configurations later.
                        </Typography>

                    </Paper>
                </Grid>
                <Grid item xs={12}  className="page-content">
                    <form onSubmit={this.handleSubmit}>
                        <TextField
                            error={!this.state.apiFields.apiName && this.state.validate}
                            id="apiName"
                            label="Name"
                            type="text"
                            name="apiName"
                            margin="normal"
                            style={{width:"100%"}}
                            value={this.state.apiFields.apiName}
                            onChange={this.inputChange}
                        />
                        <TextField
                            id="apiVersion"
                            label="Version(Version input not support in this release)"
                            type="text"
                            name="apiVersion"
                            margin="normal"			
                            style={{width:"100%"}}
			    disabled="true" 
                            value={this.state.apiFields.apiVersion}
                        />
                        <TextField
                            error={!this.state.apiFields.apiContext && this.state.validate}
                            id="apiContext"
                            label="Context"
                            type="text"
                            name="apiContext"
                            margin="normal"
                            style={{width:"100%"}}
                            value={this.state.apiFields.apiContext}
                            onChange={this.inputChange}
                        />
                          <div className="toggle-section-title">
                              <Button raised color="default" onClick={this.handleMore}>
                                  { !this.state.showMore ? <span><ArrowDropDown className="more-arrow-shift" /> More</span> :
                                      <span><ArrowDropUp /> Less</span> }
                              </Button>
                          </div>
                        {this.state.showMore &&
                            <div className="toggle-section-content">

                                <TextField
                                    id="apiEndpoint"
                                    label="Endpoint"
                                    type="text"
                                    name="apiEndpoint"
                                    margin="normal"
                                    style={{width:"100%"}}
                                    value={this.state.apiFields.apiEndpoint}
                                    onChange={this.inputChange}
                                />
                                <Typography type="subheading" gutterBottom>
                                    Business Plans
                                </Typography>
                                <Typography type="caption" className="help-text">
                                    Business Plans allows you to limit the number of successful hits to an API during a
                                    given period of time. Select a plan for the API and enable API level throttling.
                                </Typography>
                                <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC} resourceMethod={resourceMethod.POST}>

                                    {this.state.policies ? <Policies {...props}/> : ''}
                                </ScopeValidation>
                            </div>
                        }
                        <ScopeValidation resourcePath={resourcePath.APIS} resourceMethod={resourceMethod.POST}>
                            <Button raised color="primary" id="action-create" type="primary">
                                Create
                            </Button>
                        </ScopeValidation>
                        <Button onClick={() => this.props.history.push("/api/create/home")}>
                            Cancel
                        </Button>
                    </form>
                </Grid>
            </Grid>
        );
    }
}




export default ApiCreateEndpoint;
