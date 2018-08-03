import React from 'react'
import Grid from 'material-ui/Grid';
import TextField from 'material-ui/TextField';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import PropTypes from 'prop-types';
import {withStyles} from 'material-ui/styles';
import {FormControl, FormControlLabel} from 'material-ui/Form';
import Paper from 'material-ui/Paper';
import { InputAdornment} from 'material-ui/Input';
import User from '@material-ui/icons/AccountCircle';
import Lock from '@material-ui/icons/Lock';
import Person from '@material-ui/icons/Person';
import Mail from '@material-ui/icons/Mail';
import { Link } from 'react-router-dom';
import AuthManager from "../../data/AuthManager";
import Utils from "../../data/Utils";
import ConfigManager from "../../data/ConfigManager";
import LoadingAnimation from "../Base/Loading/Loading";
import API from "../../data/api";
import Snackbar from 'material-ui/Snackbar';
import Checkbox from 'material-ui/Checkbox';
import Alert from '../Shared/Alert'

const styles = {
    buttonsWrapper: {
        marginTop: 10,
        marginLeft:10
    },
    buttonAlignment: {
        marginLeft: 20,
    },
    buttonRight: {
        textDecoration: 'none',
    }
};

class SignUp extends React.Component{

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            environments: [],
            environmentId: 0,
            username: "",
            password: "",
            firstName: "",
            lastName: "",
            email: "",
            errorMessage: "",
            error: false,
            alert: false,
            policy: false
        };
    }

    componentDidMount(){
        ConfigManager.getConfigs().environments.then(response => {
            const environments = response.data.environments;
            let environmentId = Utils.getEnvironmentID(environments);
            if (environmentId === -1) {
                environmentId = 0;
            }
            this.setState({environments, environmentId});

            const environment = environments[environmentId];
            Utils.setEnvironment(environment);

        }).catch(() => {
            console.error('Error while receiving environment configurations');
        });
    };

    handleClick = () => {
        this.handleAuthentication().then(
            () => this.handleSignUp()
        ).catch(
            () => console.log("Error occurred during authentication")
        );
    };

    handleSignUp = () => {
        let { username, password, firstName, lastName, email } = this.state;
        if (!username || !password || !firstName || !lastName || !email){
            this.setState({ alert: true })
        } else {
            this.setState({ alert: false});
            let user_data = {
                username: this.state.username,
                password: this.state.password,
                firstName: this.state.firstName,
                lastName: this.state.lastName,
                email: this.state.email
            };
            let api = new API();
            let promise = api.createUser(user_data);
            promise.then(() => {
                console.log("User created successfully.");
                this.authManager.logout();
                Alert.info("User added successfully. You can now sign into the API store.");
                let redirect_url = "/login";
                this.props.history.push(redirect_url);
            }).catch((error) => {
                console.log(JSON.stringify(error));
            })
        }
    };

    handleAuthentication = () => {
        return this.authManager.registerUser(this.state.environments[0]);
    };

    handleChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    handlePasswordChange = () => event => {
        if (event.target.value != this.state.password) {
            this.setState({
                error: true,
                errorMessage: "Password does not match"
            })
        } else {
            this.setState({
                error:false,
                errorMessage: ""
            })
        }
      console.log( JSON.stringify(event.target.value) + "password")
    };

    handlePolicyChange  = (event) => {
        if (event.target.checked) {
            this.setState({ policy: true });
        } else {
            this.setState({ policy: false });
        }
    }
    ;

    render(){
        const { classes } = this.props;
        if (!this.state.environments[0]) {
            return <LoadingAnimation/>
        }
        return(
            <div className="login-flex-container">
                <Snackbar
                    anchorOrigin={{vertical: 'top', horizontal: 'center'}}
                    open={this.state.alert}
                    message={ 'Please fill all required fields' }
                />
                <Grid container justify={"center"} alignItems={"center"} spacing={0} style={{height: "100vh"}}>
                    <Grid item lg={6} md={8} xs={10}>
                        <Grid container>
                            <Grid item sm={3} xs={12}>
                                <Grid container direction={"column"}>
                                    <Grid item>
                                        <img className="brand"
                                             src={`/store/public/app/images/logo.svg`}
                                             alt="wso2-logo"/>
                                    </Grid>
                                    <Grid item>
                                        <Typography type="subheading" align="right" gutterBottom>
                                            {`API STORE`}
                                        </Typography>
                                    </Grid>

                                </Grid>
                            </Grid>

                            {/*Sign-up Form*/}
                            <Grid item sm={9} xs={12}>
                                <div className="login-main-content">
                                    <Paper elevation={1} square={true} className="login-paper">
                                        <form className="login-form">
                                            <Typography type="body1" gutterBottom>
                                                Create your account
                                            </Typography>
                                            <span>
                                                <FormControl style={{width: "100%"}}>
                                                    <TextField
                                                        required
                                                        id="username"
                                                        label="Username"
                                                        type="text"
                                                        autoComplete="username"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handleChange('username')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <User />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id="password"
                                                        label="Password"
                                                        type="password"
                                                        autoComplete="current-password"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handleChange('password')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <Lock />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        error={this.state.error}
                                                        id="rePassword"
                                                        label="Re-type Password"
                                                        type="password"
                                                        autoComplete="current-password"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handlePasswordChange('rePassword')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <Lock />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                        helperText={this.state.errorMessage}
                                                    />
                                                    <TextField
                                                        required
                                                        id="firstName"
                                                        label="First Name"
                                                        type="text"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handleChange('firstName')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <Person />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id="lastName"
                                                        label="Last Name"
                                                        type="text"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handleChange('lastName')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <Person />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id="email"
                                                        label="E mail"
                                                        type="mail"
                                                        margin="normal"
                                                        style={{width: "100%"}}
                                                        onChange={this.handleChange('email')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position="start">
                                                                    <Mail />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <FormControl>
                                                    <Typography>
                                                        After successfully signing in, a cookie is placed in your browser to track your session. See our {' '}
                                                        <Link to={"/policy/cookie-policy"}>
                                                              Cookie Policy
                                                        </Link>
                                                        {' '} for more details.
                                                    </Typography>
                                                    </FormControl>
                                                    <FormControlLabel
                                                        control={
                                                            <Checkbox onChange={this.handlePolicyChange} />
                                                        }
                                                        label={
                                                            <p>
                                                                <strong>
                                                                    I hereby confirm that I have read and understood the {''}
                                                                    <Link to={"/policy/privacy-policy"} target="_blank">
                                                                        Privacy Policy.
                                                                    </Link>
                                                                </strong>
                                                            </p>
                                                        }
                                                    />
                                                </FormControl>
                                            </span>
                                            <div className={classes.buttonsWrapper}>
                                                <Button
                                                    variant="raised"
                                                    color="primary"
                                                    onClick={this.handleClick.bind(this)}
                                                    disabled={!this.state.policy}
                                                >
                                                    Sign up
                                                </Button>
                                                <Link to={"/"} style={{ textDecoration: 'none' }}>
                                                    <Button variant="raised" className={classes.buttonAlignment}>
                                                        Back to Store
                                                    </Button>
                                                </Link>
                                            </div>
                                        </form>
                                    </Paper>
                                </div>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

SignUp.propTypes = {
    classes: PropTypes.object.isRequired,
};


export default withStyles(styles)(SignUp);