import  React from 'react'

import Api from '../../../data/api'
import Application from '../../../data/Application';

import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Grid from 'material-ui/Grid';
import {withStyles} from 'material-ui/styles';
import Typography from 'material-ui/Typography';
import Loading from '../../Base/Loading/Loading'

import IconButton from 'material-ui/IconButton';
import Input, { InputLabel, InputAdornment } from 'material-ui/Input';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Visibility from 'material-ui-icons/Visibility';
import VisibilityOff from 'material-ui-icons/VisibilityOff';
import classNames from 'classnames';

class ProductionKeys extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            accessToken: "",
            consumerKey: "",
            consumerSecret: "",
            application: null,
            showCS: false,
        };
        this.appId = this.props.match.params.applicationId;
    }

    handleClickToken() {
        var api = new Api();
        var data = "{\"keyType\": \"PRODUCTION\", \"grantTypesToBeSupported\": [\"password\",\"client_credentials\"], \"callbackUrl\": \"\"}";

        api.generateKeys(this.appId, data).then(
            response => {
                this.state.consumerKey = response.obj.consumerKey;
                this.state.consumerSecret = response.obj.consumerSecret;

                var tokenData = {};
                tokenData.consumerKey = response.obj.consumerKey;
                tokenData.consumerSecret = response.obj.consumerSecret;
                tokenData.validityPeriod = 3600;
                tokenData.scopes = "";

                api.generateToken(this.appId, tokenData).then(
                    response => {
                        this.setState({accessToken: response.obj.accessToken});
                    }
                );
            }
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

    handleClickShowPasssword = () => {
        this.setState({ showCS: !this.state.showCS });
    };

    handleMouseDownPassword = event => {
        event.preventDefault();
    };

    componentDidMount() {
        let promised_app = Application.get(this.appId);
        promised_app.then(application => {
            application.getKeys().then(_ => this.setState({application: application}))
        });
    }

    render() {
        if (!this.state.application) {
            return <Loading/>
        }
        let consumerKey = this.state.consumerKey || this.state.application.keys[0].consumerKey;
        let consumerSecret = this.state.consumerSecret || this.state.application.keys[0].consumerSecret;
        return (
            <Grid container className="tab-content">
                <Grid item xs={12}>
                    <Button color="accent" onClick={() => this.handleClickToken()}>Generate Token</Button>
                    <Paper elevation={4} className="key-container">
                        <Typography type="headline" component="h3">
                            Consumer Key
                        </Typography>
                        {consumerKey ?
                            <Typography type="body1" component="p">
                                {consumerKey}
                            </Typography>
                            :
                            <Typography type="body1" component="p">
                                <i>Keys are not genrated yet. Click the Generate token button to generate the keys.</i>
                            </Typography>
                        }
                        <FormControl>
                            <InputLabel htmlFor="consumerSecret">Consumer Secret</InputLabel>
                            <Input
                                id="consumerSecret"
                                type={this.state.showCS ? 'text' : 'password'}
                                value={consumerSecret || "Keys are not genrated yet. Click the Generate token button to generate the keys."}
                                endAdornment={
                                    <InputAdornment position="end">
                                        <IconButton classes="" onClick={this.handleClickShowPasssword}
                                            onMouseDown={this.handleMouseDownPassword}>
                                            {this.state.showCS ? <VisibilityOff/> : <Visibility/>}
                                        </IconButton>
                                    </InputAdornment>
                                }
                            />
                        </FormControl>

                        <Typography type="headline" component="h3">
                            Access Token
                        </Typography>
                        {this.state.accessToken ?
                            <Typography type="body1" component="p">
                                {this.state.accessToken}
                            </Typography>
                            :
                            <Typography type="body1" component="p">
                                <i>Token is not genrated yet. Click the Generate token button to get the token.</i>
                            </Typography>
                        }
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

export default ProductionKeys
