import  React from 'react'

import Application from '../../../data/Application';

import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Grid from 'material-ui/Grid';
import {withStyles} from 'material-ui/styles';
import Loading from '../../Base/Loading/Loading'
import TextField from 'material-ui/TextField';

import IconButton from 'material-ui/IconButton';
import Input, {InputLabel, InputAdornment} from 'material-ui/Input';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Visibility from 'material-ui-icons/Visibility';
import VisibilityOff from 'material-ui-icons/VisibilityOff';

// Styles for Grid and Paper elements
const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 30,
        width: "95%"
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
        this.state = {
            application: null,
            showCS: false, // Show Consumer Secret flag
            showAT: false,// Show Access Token flag
        };
        this.appId = this.props.match.params.applicationId;
        this.handleShowToken = this.handleShowToken.bind(this);
    }

    handleClickToken() {
        let application = this.state.application;
        const type = Application.KEY_TYPES.PRODUCTION;
        application.generateKeys(type).then(
            () => application.generateToken(type).then(() => this.setState({}))
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
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
        let promised_tokens = this.state.application.generateToken(Application.KEY_TYPES.PRODUCTION);
        promised_tokens.then((token) => this.setState({showAT: true}))
    }

    handleShowCS = () => {
        this.setState({showCS: !this.state.showCS});
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
            application.getKeys().then(() => this.setState({application: application}))
        });
    }

    render() {
        if (!this.state.application) {
            return <Loading/>
        }
        const {classes} = this.props;
        const type = Application.KEY_TYPES.PRODUCTION; /* TODO: Re-use this component to work with sand-box key UI as well ~tmkb*/
        let cs_ck_keys = this.state.application.keys.get(type);
        let consumerKey = (cs_ck_keys && cs_ck_keys.consumerKey);
        let consumerSecret = (cs_ck_keys && cs_ck_keys.consumerSecret);
        let accessToken = this.state.application.tokens.has(type) && this.state.application.tokens.get(type).accessToken;
        return (
            <div className={classes.root}>
                <Grid container spacing={24}>
                    <Grid alignContent="stretch" alignItems="baseline" container justify="center" item xs={12}>
                        { !consumerKey &&
                        <Button raised color="accent" onClick={() => this.handleClickToken()}>Generate Token</Button>}
                    </Grid>
                    <Grid item xs={6}>
                        <Paper className={classes.paper}>
                            <TextField
                                inputProps={{readonly: true}}
                                label="Consumer Key"
                                id="consumerKey"
                                value={consumerKey || "Keys are not generated yet. Click the Generate token button to generate the keys."}
                                className={"textField"}
                                helperText="Consumer Key of the application"
                                margin="none"
                                fullWidth={true}
                            />
                        </Paper>
                    </Grid>
                    <Grid item xs={6}>
                        <Paper className={classes.paper}>
                            <FormControl fullWidth={true} margin="none">
                                <InputLabel htmlFor="consumerSecret">Consumer Secret</InputLabel>
                                <Input
                                    inputProps={{readonly: true}}
                                    id="consumerSecret"
                                    type={(this.state.showCS || !consumerSecret) ? 'text' : 'password'}
                                    value={consumerSecret || "Keys are not generated yet. Click the Generate token button to generate the keys."}
                                    endAdornment={
                                        <InputAdornment position="end">
                                            <IconButton classes="" onClick={this.handleShowCS}
                                                        onMouseDown={this.handleMouseDownGeneric}>
                                                {this.state.showCS ? <VisibilityOff/> : <Visibility/>}
                                            </IconButton>
                                        </InputAdornment>
                                    }
                                />
                                <FormHelperText>Consumer Secret of the application</FormHelperText>
                            </FormControl>
                        </Paper>
                    </Grid>
                    <Grid item xs={8}>
                        <Button disabled={this.state.showAT || accessToken} onMouseDown={this.handleMouseDownGeneric}
                                onClick={this.handleShowToken} raised color="primary"
                                className={classes.button}>Generate Token</Button>
                        <Paper className={classes.paper}>
                            <TextField
                                inputProps={{readonly: true}}
                                label="Access Token"
                                id="accessToken"
                                value={accessToken || "Click on Generate Token button to generate Access Token."}
                                // type={(this.state.showAT || !accessToken) ? 'text' : 'password'} TODO: add visibility icon ~tmkb
                                className={"textField"}
                                helperText="Access Token for the Application"
                                margin="none"
                                fullWidth={true}
                            />
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

export default withStyles(styles)(ProductionKeys);
