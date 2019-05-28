/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Application from '../../../data/Application';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Loading from '../../Base/Loading/Loading'
import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Typography from '@material-ui/core/Typography';
import Chip from '@material-ui/core/Chip';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';


// Styles for Grid and Paper elements
const styles = theme => ({
    FormControl: {
        padding: theme.spacing.unit*2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit*2,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position:'relative',
    },
    chips: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        margin: theme.spacing.unit / 4,
    },
});
const MenuProps = {
    PaperProps: {
      style: {
        maxHeight: 224,
        width: 250,
      },
    },
  };
  const allScopes = [
      "device_ipad",
      "device_mac"
  ];
class Tokens extends React.Component {
    constructor(props) {
        super(props);
        this.key_type = props.type;
        this.state = {
            application: null,
            showCS: false, // Show Consumer Secret flag
            showAT: false,// Show Access Token flag
            scopesSelected: [],
            timout: 3600, // Timeout for token in miliseconds
        };
        this.appId = this.props.selectedApp.appId || this.props.selectedApp.value;
        this.key_type = this.props.keyType;
        this.handleShowToken = this.handleShowToken.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleUpdateToken = this.handleUpdateToken.bind(this);
    }
    handleChangeScopes = event => {
        this.setState({ scopesSelected: event.target.value });
    };
    handleChangeInput = name => event => {
        this.setState({ [name]: event.target.value });
    };

    /**
     * Generate an access token for the application instance
     * @returns {promise} Set the generated token into current
     * instance and return tokenObject received as Promise object
     * @memberof Tokens
     */
    generateToken() {
        if (!this.state.application) {
            console.warn("No Application found!");
            return false;
        }
        let promised_tokens = this.state.application.generateToken(this.key_type, this.state.timout,
            this.state.scopesSelected);
        return promised_tokens;
    }

    handleUpdateToken() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type);
        application.updateKeys(keys.tokenType, this.key_type, keys.supportedGrantTypes, keys.callbackUrl, keys.consumerKey,
            keys.consumerSecret).
            then(() => this.setState({ application: application })
            ).catch(
                error => {
                    if (process.env.NODE_ENV !== "production") {
                        console.log(error);
                    }
                    let status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                }
            );
    }
    
    /**
     * Because application access tokens are not coming with /keys or /application API calls,
     * Fetch access token value upon user request
     * @returns {boolean} If no application object found in state object
     */
    handleShowToken() {
        if (!this.state.application) {
            console.warn("No Application found!");
            return false;
        }
        let promised_tokens = this.state.application.generateToken(this.key_type);
        return promised_tokens;
        //promised_tokens.then((token) => this.setState({ showAT: true }))
    }

    handleTextChange(event) {
        const { application, key } = this.state;
        const { currentTarget } = event;
        let keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"],
                "keyType": this.key_type,
            }
        keys.callbackUrl = currentTarget.value;
        application.keys.set(this.key_type, keys);
        this.setState({ application });
    }

    handleCheckboxChange(event) {
        const { application } = this.state;
        const { currentTarget } = event;
        const keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"],
                "keyType": this.key_type,
            }
        let index;

        if (currentTarget.checked) {
            keys.supportedGrantTypes.push(currentTarget.id)
        } else {
            index = keys.supportedGrantTypes.indexOf(currentTarget.id)
            keys.supportedGrantTypes.splice(index, 1);
        }
        application.keys.set(this.key_type, keys);
        // update the state with the new array of options
        this.setState({ application });
    };

    handleShowCS = () => {
        this.setState({ showCS: !this.state.showCS });
    };

    /**
     * Avoid conflict with `onClick`
     * @param event
     */
    handleMouseDownGeneric = event => {
        event.preventDefault();
    };


    /**
     * Fetch Application object by ID coming from URL path params and fetch related keys to display
     */
    componentDidMount() {
        let promised_app = Application.get(this.appId);
        promised_app.then(application => {
            application.getKeys().then(() => this.setState({ application: application }))
        }).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            }
        );
    }

    render() {
        const { notFound, showCS } = this.state;
        if (notFound) {
            return <ResourceNotFound />
        }
        if (!this.state.application) {
            return <Loading />
        }
        const { classes } = this.props;
        let cs_ck_keys = this.state.application.keys.get(this.key_type);
        let consumerKey = (cs_ck_keys && cs_ck_keys.consumerKey);
        let consumerSecret = (cs_ck_keys && cs_ck_keys.consumerSecret);
        let supportedGrantTypes = (cs_ck_keys && cs_ck_keys.supportedGrantTypes);
        let callbackUrl = (cs_ck_keys && cs_ck_keys.callbackUrl);
        supportedGrantTypes = supportedGrantTypes || false;
        let accessToken = this.state.application.tokens.has(this.key_type) &&
            this.state.application.tokens.get(this.key_type).accessToken;
        return (
            <React.Fragment>
                    <FormControl margin="normal" className={classes.FormControl}>
                        <TextField
                                    required
                                    label="Access token validity period"
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    helperText="You can set an expiration period to determine the validity period of the token after generation. 
                                    Set this to a negative value to ensure that the token never expires. "
                                    fullWidth
                                    name="timout"
                                    onChange={this.handleChangeInput('timout')}
                                    placeholder="Enter time in miliseconds"
                                    value={this.state.timout}
                                    autoFocus={true}
                                    className={classes.inputText}
                                />
                    </FormControl>
                    <FormControl margin="normal" className={classes.FormControlOdd}>
                            <InputLabel htmlFor="quota-helper" className={classes.quotaHelp}>Scopes</InputLabel>
                            <Select
                                multiple
                                value={this.state.scopesSelected}
                                onChange={this.handleChangeScopes}
                                input={<Input id="select-multiple-chip" />}
                                renderValue={selected => (
                                <div className={classes.chips}>
                                    {selected.map(value => (
                                    <Chip key={value} label={value} className={classes.chip} />
                                    ))}
                                </div>
                                )}
                                MenuProps={MenuProps}
                            >
                                {allScopes.map(scope => (
                                <MenuItem
                                    key={scope}
                                    value={scope}
                                >
                                    {scope}
                                </MenuItem>
                                ))}
                            </Select>
                            <Typography variant="caption">When you generate access tokens to APIs protected by scope/s, you can select the scope/s and then generate the token for it. Scopes enable fine-grained access control to API resources based on user roles. You define scopes to an API's resources. When a user invokes the API, his/her OAuth 2 bearer token cannot grant access to any API resource beyond its associated scopes.</Typography>
                        </FormControl>
            </React.Fragment>
        );
    }
}

export default withStyles(styles)(Tokens);
