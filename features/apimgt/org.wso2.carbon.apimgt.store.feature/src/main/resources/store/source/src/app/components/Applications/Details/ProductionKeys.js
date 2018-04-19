import React from 'react'

import Application from '../../../data/Application';

import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Grid from 'material-ui/Grid';
import { withStyles } from 'material-ui/styles';
import Loading from '../../Base/Loading/Loading'
import TextField from 'material-ui/TextField';

import IconButton from 'material-ui/IconButton';
import Input, { InputLabel, InputAdornment } from 'material-ui/Input';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Visibility from 'material-ui-icons/Visibility';
import VisibilityOff from 'material-ui-icons/VisibilityOff';
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import { Checkbox, FormControlLabel, FormGroup } from "material-ui";
import Divider from 'material-ui/Divider';
import Typography from 'material-ui/Typography';


// Styles for Grid and Paper elements
const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    paper: {
        padding: 16,
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
});

class ProductionKeys extends React.Component {
    constructor(props) {
        super(props);
        this.key_type = props.type;
        this.state = {
            application: null,
            showCS: false, // Show Consumer Secret flag
            showAT: false,// Show Access Token flag
        };
        this.appId = this.props.match.params.applicationId;
        this.handleShowToken = this.handleShowToken.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleUpdateToken = this.handleUpdateToken.bind(this);
    }

    handleClickToken() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"]
            }
        if (!keys.callbackUrl) {
            keys.callbackUrl = "https://wso2.am.com";
        }
        application.generateKeys(this.key_type, keys.supportedGrantTypes, keys.callbackUrl).then(
            () => application.generateToken(this.key_type).then(() => this.setState({ application: application }))
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

    handleUpdateToken() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type);
        application.updateKeys(this.key_type, keys.supportedGrantTypes, keys.callbackUrl, keys.consumerKey, 
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
        promised_tokens.then((token) => this.setState({ showAT: true }))
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
            <div className={classes.root}>
                <Paper>
                    <Grid container className="tab-grid" spacing={20} >
                        <Grid item xs={12}>
                            {!consumerKey &&
                                <Button variant="raised" color="primary" onClick={() => this.handleClickToken()}>
                                    Generate Keys</Button>}
                        </Grid>
                        <Grid item xs={7}>
                            <TextField
                                inputProps={{ readonly: true }}
                                label="Consumer Key"
                                id="consumerKey"
                                value={consumerKey || "Keys are not generated yet. Click the Generate token button to generate the keys."}
                                className={"textField"}
                                helperText="Consumer Key of the application"
                                margin="normal"
                                fullWidth={true}
                            />
                        </Grid>
                        <Grid item xs={7}>
                            <FormControl fullWidth={true} margin="normal">
                                <InputLabel htmlFor="consumerSecret">Consumer Token</InputLabel>
                                <Input
                                    inputProps={{ readonly: true }}
                                    id="consumerSecret"
                                    type={(showCS || !consumerSecret) ? 'text' : 'password'}
                                    value={consumerSecret || "Keys are not generated yet. Click the Generate token button to generate the keys."}
                                    endAdornment={
                                        <InputAdornment position="end">
                                            <IconButton classes="" onClick={this.handleShowCS}
                                                onMouseDown={this.handleMouseDownGeneric}>
                                                {showCS ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    }
                                />
                                <FormHelperText>Consumer Secret of the application</FormHelperText>
                            </FormControl>
                        </Grid>
                        <Divider inset={true} />
                        <Grid item xs={12}>
                            <Typography variant="subheading">
                                Grant Types
                            </Typography>
                            <Typography>
                                The application can use the following grant types to generate Access Tokens.
                                Based on the application requirement, you can enable or disable grant types
                                for this application.
                            </Typography>
                        </Grid>
                        <Grid item xs={7}>
                            <FormGroup row>
                                <FormControlLabel control={<Checkbox
                                    id="refresh_token"
                                    checked={supportedGrantTypes && supportedGrantTypes.includes("refresh_token")}
                                    onChange={this.handleCheckboxChange}
                                    value="refresh_token"
                                />} label="Refresh Token" />
                                <FormControlLabel control={<Checkbox
                                    id="password"
                                    checked={supportedGrantTypes && supportedGrantTypes.includes("password")}
                                    value="password"
                                    onChange={this.handleCheckboxChange}
                                />} label="Password" />
                                <FormControlLabel control={<Checkbox
                                    id="implicit"
                                    checked={supportedGrantTypes && supportedGrantTypes.includes("implicit")}
                                    value="implicit"
                                    onChange={this.handleCheckboxChange}
                                />} label="Implicit" />
                                <FormControlLabel control={<Checkbox
                                    id="code"
                                    checked={supportedGrantTypes && supportedGrantTypes.includes("code")}
                                    value="code"
                                    onChange={this.handleCheckboxChange}
                                />} label="Code" />
                                <FormControlLabel control={<Checkbox
                                    id='client_credentials'
                                    checked={true}
                                    disabled
                                    value="client_credentials"
                                />} label="Client Credential"
                                />
                            </FormGroup>
                        </Grid>
                        {supportedGrantTypes && (supportedGrantTypes.includes("implicit") ||
                            supportedGrantTypes.includes("code")) &&
                            <Grid item xs={7}>
                                <Typography variant="body2">
                                    Callback URL
                                </Typography>
                                <TextField
                                    id="callbackURL"
                                    fullWidth={true}
                                    onChange={this.handleTextChange}
                                    value={callbackUrl}
                                />
                            </Grid>
                        }
                        {consumerSecret &&
                            <Grid item xs={12}>
                                <Grid item xs={7}>
                                    <Button variant="raised" color="default"
                                        onClick={this.handleUpdateToken}
                                    >Update</Button>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography variant="subheading">
                                        Generate a Test Access Token
                                    </Typography>
                                    <Button disabled={this.state.showAT || accessToken}
                                        onMouseDown={this.handleMouseDownGeneric}
                                        onClick={this.handleShowToken} variant="raised" color="default"
                                        className="form-buttons">Generate Token</Button>
                                </Grid>
                                <Grid item xs={7}>
                                    <TextField
                                        inputProps={{ readonly: true }}
                                        label="Access Token"
                                        id="accessToken"
                                        value={accessToken || "Click on Generate Token button to generate Access Token."}
                                        // type={(this.state.showAT || !accessToken) ? 'text' : 'password'} TODO: add visibility icon ~tmkb
                                        className={"textField"}
                                        helperText="Access Token for the Application"
                                        margin="normal"
                                        fullWidth={true}
                                    />
                                </Grid>
                            </Grid>}
                    </Grid>
                </Paper>
            </div>
        );
    }
}

export default withStyles(styles)(ProductionKeys);
