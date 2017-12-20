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
import Grid from 'material-ui/Grid';
import {withStyles} from 'material-ui/styles';
import grey from 'material-ui/colors/grey';
import green from 'material-ui/colors/green';
import Alert from "../../../Shared/Alert";
import {CircularProgress} from 'material-ui/Progress';

const styles = theme => ({
        root: {
            flexGrow: 1,
            marginTop: 30,
        },
        paper: {
            padding: 20,
            textAlign: 'left',
        },
        subHeadings: {
            fontWeight: "300",
            padding: "10px 0 10px 30px",
            margin: "0px"
        },
        inputLabel: {
            color: grey[800],
        },
        buttonProgress: {
            color: green[500],
            position: 'relative',
            marginTop: -20,
            marginLeft: -50,
        }
    }
);

class ApiCreateEndpoint extends React.Component {
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            inputLabel: "inputLabel",
            api: this.api,
            apiFields: {
                apiVersion: "1.0.0"
            },
            validate: false,
            messageOpen: false,
            message: '',
            loading: false
        };
        this.updateData = this.updateData.bind(this);
        this.inputChange = this.inputChange.bind(this);
        this.handlePolicies = this.handlePolicies.bind(this)
    }

    componentDidMount() {
        this.updateData();
    }

    inputChange(e) {
        const field = e.target.name;
        const apiFields = this.state.apiFields;
        apiFields[field] = e.target.value;
        this.setState({apiFields: apiFields});
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
        this.setState({apiFields: apiFields});
    }

    createAPICallback = (response) => {
        let uuid = JSON.parse(response.data).id;
        let redirect_url = "/apis/" + uuid + "/overview";
        this.props.history.push(redirect_url);
    };

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit = (e) => {
        e.preventDefault();
        const values = this.state.apiFields;
        //Check for form errors manually
        if (!values.apiName || !values.apiVersion || !values.apiContext) {
            this.setState({message: 'Please fill all required fields', messageOpen: true, validate: true});
            return;
        }
        this.setState({loading: true});
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
                this.setState({message: 'API Created with UUID :' + uuid});
                promisedApi.then(response => {
                    this.setState({message: 'Updating API policies . . .'});
                    let api_data = JSON.parse(response.data);
                    api_data.policies = this.state.apiFields.selectedPolicies;
                    console.info("Adding policies to the api", this.state.apiFields.selectedPolicies);
                    let promised_update = this.api.update(api_data);
                    promised_update.then(response => {
                        this.setState({message: 'Policies updated successfully!'});
                        this.createAPICallback(response);
                    })
                })
            })
            .catch(error => {
                console.error(error);
                this.setState({loading: false, message: "Error occurred while creating the API"});
                if (error.response && error.response.obj) {
                    let data = error.response.obj;
                    let message = "[" + data.code + "]" + ": " + data.description;
                    this.setState({message: message});
                }
            });

        console.log('Send this in a POST request:', api_data);
    };

    render() {
        const {classes} = this.props;
        const {inputLabel, policies, apiFields, message, loading} = this.state;
        const SuperScriptAsterisk = () => (<sup style={{color: "red"}}>*</sup>);
        const inputLabelClass = {classes: {root: classes[inputLabel]}};
        const props = {
            classes: inputLabelClass.classes,
            policies: policies,
            handlePolicies: this.handlePolicies,
            selectedPolicies: apiFields && apiFields.selectedPolicies
        };
        return (
            <div className={classes.root}>
                <Alert message={message}/>
                <Grid container spacing={0} justify="center">
                    <Grid item md={10} className="page-content">
                        <Paper className={classes.paper}>
                            <Typography type="headline" gutterBottom>
                                Create New API
                            </Typography>
                            <Typography type="subheading" gutterBottom align="left" className={classes.subHeadings}>
                                Fill the mandatory fields (Name, Version, Context) and create the API. Configure
                                advanced
                                configurations later.
                            </Typography>
                            <form onSubmit={this.handleSubmit}>
                                <TextField
                                    InputLabelProps={inputLabelClass}
                                    error={!this.state.apiFields.apiName && this.state.validate}
                                    id="apiName"
                                    label={<div><span>Name </span><SuperScriptAsterisk/></div>}
                                    type="text"
                                    name="apiName"
                                    margin="normal"
                                    style={{width: "100%"}}
                                    value={this.state.apiFields.apiName}
                                    onChange={this.inputChange}
                                />
                                <TextField
                                    // InputLabelProps={inputLabelClass}
                                    label={<div><span>Version </span><SuperScriptAsterisk/></div>}
                                    id="apiVersion"
                                    helperText="**Version input not support in this release"
                                    type="text"
                                    name="apiVersion"
                                    margin="normal"
                                    style={{width: "100%"}}
                                    disabled
                                    // value={this.state.apiFields.apiVersion}
                                />
                                <TextField
                                    InputLabelProps={inputLabelClass}
                                    error={!this.state.apiFields.apiContext && this.state.validate}
                                    id="apiContext"
                                    label={<div><span>Context </span><SuperScriptAsterisk/></div>}
                                    type="text"
                                    name="apiContext"
                                    margin="normal"
                                    style={{width: "100%"}}
                                    value={this.state.apiFields.apiContext}
                                    onChange={this.inputChange}
                                />
                                <TextField
                                    InputLabelProps={inputLabelClass}
                                    id="apiEndpoint"
                                    label="Endpoint"
                                    type="text"
                                    name="apiEndpoint"
                                    margin="normal"
                                    style={{width: "100%"}}
                                    value={this.state.apiFields.apiEndpoint}
                                    onChange={this.inputChange}
                                />
                                <ScopeValidation resourcePath={resourcePath.API_CHANGE_LC}
                                                 resourceMethod={resourceMethod.POST}>

                                    {this.state.policies ? <Policies {...props}/> : ''}
                                </ScopeValidation>
                                <Grid container direction="row" justify="flex-end" alignItems="flex-end" spacing={16}>
                                    <Grid item>
                                        <Button raised onClick={() => this.props.history.push("/api/create/home")}>
                                            Cancel
                                        </Button>
                                    </Grid>
                                    <Grid item>
                                        <ScopeValidation resourcePath={resourcePath.APIS}
                                                         resourceMethod={resourceMethod.POST}>
                                            <div>
                                                <Button disabled={loading} raised color="primary" id="action-create"
                                                        type="primary">
                                                    Create
                                                </Button>
                                                {loading &&
                                                <CircularProgress size={24} className={classes.buttonProgress}/>}
                                            </div>
                                        </ScopeValidation>
                                    </Grid>
                                </Grid>
                            </form>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

export default withStyles(styles)(ApiCreateEndpoint);
