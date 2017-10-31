import  React from 'react'

import Api from '../../../data/api'

import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Grid from 'material-ui/Grid';
import { withStyles } from 'material-ui/styles';
import Typography from 'material-ui/Typography';

class ProductionKeys extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            accessToken: "",
            consumerKey: "",
            consumerSecret: ""
        }

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

    render() {
        return (
            <Grid container className="tab-content">
                <Grid item xs={12} >
                    <Button color="accent" onClick={() => this.handleClickToken()}>Generate Token</Button>
                    <Paper elevation={4} className="key-container">
                        <Typography type="headline" component="h3">
                            Consumer Key
                        </Typography>
                        {this.state.consumerKey ?
                            <Typography type="body1" component="p">
                            {this.state.consumerKey}
                            </Typography>
                        :
                            <Typography type="body1" component="p">
                                <i>Keys are not genrated yet. Click the Generate token button to generate the keys.</i>
                            </Typography>
                        }

                        <Typography type="headline" component="h3">
                            Consumer Secret
                        </Typography>
                        {this.state.consumerSecret ?
                            <Typography type="body1" component="p">
                            {this.state.consumerSecret}
                            </Typography>
                        :
                            <Typography type="body1" component="p">
                                <i>Keys are not genrated yet. Click the Generate token button to generate the keys.</i>
                            </Typography>
                        }

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
