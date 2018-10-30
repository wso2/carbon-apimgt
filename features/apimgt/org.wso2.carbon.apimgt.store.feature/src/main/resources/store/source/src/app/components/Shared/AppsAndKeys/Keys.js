import React from 'react'

import Application from '../../../data/Application';

import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Loading from '../../Base/Loading/Loading'
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormLabel from '@material-ui/core/FormLabel';
import { FormattedMessage } from 'react-intl';

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
    checkboxWrapper: {
        display: 'flex',
    },
    checkboxWrapperColumn: {
        display: 'flex',
        flexDirection: 'column',
    }
});

class Keys extends React.Component {
    constructor(props) {
        super(props);
        this.key_type = props.type;
        this.state = {
            application: null,
            tokenType: "OAUTH",
        };
        this.appId = this.props.selectedApp.value;
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleTokenTypeChange = this.handleTokenTypeChange.bind(this);
        this.key_type = this.props.keyType;
    }
    handleTextChange(event) {
        const { application } = this.state;
        const { currentTarget } = event;
        let keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"],
                "keyType": this.key_type,
                "tokenType": this.state.tokenType,
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
                "tokenType": this.state.tokenType,
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

    handleTokenTypeChange(event) {
        const {application} = this.state;
        const keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"],
                "keyType": this.key_type,
                "tokenType": this.state.tokenType,
            }
        keys.tokenType = event.target.value;
        application.keys.set(this.key_type, keys);
        // update the state with the new array of options
        this.setState({application, tokenType:event.target.value});
    };
    //We have to wrap the two update and generate methods in a single mehtod.
    keygenWrapper() {
        if(this.hasKeys) {
            return this.updateKeys();
        } else { 
            return this.generateKeys();
        }
    }
    generateKeys() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type) ||
            {
                "supportedGrantTypes":
                    ["client_credentials"]
            }
        if (!keys.callbackUrl) {
            keys.callbackUrl = "https://wso2.am.com";
        }
        let keyPromiss = application.generateKeys(this.key_type, keys.supportedGrantTypes, keys.callbackUrl,keys.tokenType);
        return keyPromiss;
    }

    updateKeys() {
        const { application } = this.state;
        const keys = application.keys.get(this.key_type);
        let updatePromiss = application.updateKeys(keys.tokenType, this.key_type, keys.supportedGrantTypes, keys.callbackUrl, keys.consumerKey, keys.consumerSecret);
        return updatePromiss;
    }
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
        const { notFound } = this.state;
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
        if( consumerKey ) {this.hasKeys = true } else { this.hasKeys = false }
        return (
            <div className={classes.root}>
                <Grid container spacing={24} className={classes.root}>
                    <Grid item xs={12} md={6}>
                        <FormControl component="fieldset" className={classes.formControl}>
                            <FormLabel component="legend">
                                <FormattedMessage
                                    id='token.type'
                                    defaultMessage='Token Type'/></FormLabel>
                            <RadioGroup
                                aria-label="Token Type"
                                name="tokenType"
                                className={classes.group}
                                value={this.state.tokenType}
                                onChange={this.handleTokenTypeChange}>
                                <FormControlLabel value="OAUTH" control={<Radio/>} label="OAUTH"/>
                                <FormControlLabel value="JWT" control={<Radio/>} label="JWT"/>
                            </RadioGroup>
                        </FormControl>
                    <FormControl className={classes.FormControl} component="fieldset">
                        <InputLabel shrink htmlFor="age-label-placeholder" className={classes.quotaHelp}>
                            <FormattedMessage
                                id='grant.types'
                                defaultMessage='Grant Types'
                            />
                        </InputLabel>
                            <div className={classes.checkboxWrapper}>
                                <div className={classes.checkboxWrapperColumn}>
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
                                </div>
                                <div className={classes.checkboxWrapperColumn}>
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
                                </div>
                                
                            </div>
                        <FormHelperText>The application can use the following grant types to generate Access Tokens.
                                Based on the application requirement, you can enable or disable grant types
                                for this application.</FormHelperText>
                    </FormControl>
                                            

                    { //(supportedGrantTypes && (supportedGrantTypes.includes("implicit") || supportedGrantTypes.includes("code")))  &&
                        <FormControl className={classes.FormControlOdd}>
                            <InputLabel shrink htmlFor="age-label-placeholder" className={classes.quotaHelp}>
                                Callback URL
                            </InputLabel>
                            <TextField
                                id="callbackURL"
                                fullWidth={true}
                                onChange={this.handleTextChange}
                                label="Enter the Callback URL"
                                placeholder="http://url-to-webapp"
                                className={classes.textField}
                                margin="normal"
                                value={callbackUrl}
                            />
                            }
                        </FormControl>
                    }
                    </Grid>
                </Grid>
                </div>
        );
    }
}

export default withStyles(styles)(Keys);
