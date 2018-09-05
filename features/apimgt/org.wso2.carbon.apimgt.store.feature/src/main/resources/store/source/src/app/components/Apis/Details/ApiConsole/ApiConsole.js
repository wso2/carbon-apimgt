  /*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

import SwaggerUI from "swagger-ui";
import StandalonePreset from 'swagger-ui/dist/swagger-ui-standalone-preset';
import Api from '../../../../data/api'
import AuthManager from '../../../../data/AuthManager'
import 'swagger-ui/dist/swagger-ui.css';

import Select from '@material-ui/core/Select';
import Progress from '../../../Shared/Progress';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';
import Application from '../../../../data/Application'

const styles = theme => ({
    authHeader: {
        marginLeft: '105px',
        color: '#555555',
        backgroundColor: '#eeeeee',
        border: '1px solid #ccc'
    },
    inputText: {
        marginLeft: '40px',
        minWidth: '400px'
    },
    gatewaySelect: {
        marginLeft: '173px',
        minWidth: '400px'
    },
    credentialSelect: {
        marginLeft: '150px',
        minWidth: '400px',
        marginRight: '10px'
    },
    grid: {
        spacing: 20,
        marginTop: '30px',
        marginBottom: '30px',
        paddingLeft: '90px'
    }
});

class ApiConsole extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            apiGateways: null,
            apiGateway: '',
            credentialTypes: [],
            credentialType: '',
            accessToken: '',
            setSwagger: null
        };
        this.api_uuid = this.props.match.params.api_uuid;
    }

    componentDidMount() {
        const api = new Api();
        const disableAuthorizeAndInfoPlugin = function() {
          return {
            wrapComponents: {
              authorizeBtn: () => () => null,
              info: () => () => null
            }
          };
        };

        let credentialTypes = [];
        let apiGateways = [];
        credentialTypes.push('OAuth2');
        apiGateways.push('Default');

        this.setState({apiGateways: apiGateways, apiGateway: apiGateways[0], credentialTypes: credentialTypes,
         credentialType: credentialTypes[0]});

        let promised_swagger = api.getSwaggerByAPIId(this.api_uuid);

        Promise.all([promised_swagger]).then(responses => {

          let swagger = responses[0];
          let url = swagger.url;
          this.setState({setSwagger: true});

          const swaggerUI = SwaggerUI({
              dom_id: "#ui",
              url: swagger.url,
              requestInterceptor: (req) => {
                  // Only set Authorization header if the request matches the spec URL
                  if (req.url === url) {
                    req.headers.Authorization =  "Bearer " + AuthManager.getUser().getPartialToken();
                  }
                  return req;
                },
              presets: [
                      SwaggerUI.presets.apis,
                      disableAuthorizeAndInfoPlugin
              ],
              plugins: [
                  SwaggerUI.plugins.DownloadUrl
              ]
          })

        }).catch(
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

    handleChange = name => event => {
        this.setState({[name]: event.target.value});
    };

    render() {

        const { classes } = this.props;
        let { handleInputs } = this.props;

        if (this.state.setSwagger == null) {
              return <Progress />;
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    <Grid item md={12}>
                        <div>
                            <Typography variant='subheading'>
                               API Gateway
                               <Select
                                   value={this.state.apiGateway}
                                   onChange={this.handleChange('apiGateway')}
                                   className={classes.gatewaySelect}
                               >
                                   {this.state.apiGateways.map((apiGateway) => <option value={apiGateway} key={apiGateway}>
                                       {apiGateway}
                                   </option>)}
                               </Select>
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                API Credentials
                                <Select
                                   value={this.state.credentialType}
                                   onChange={this.handleChange('credentialType')}
                                   className={classes.credentialSelect}
                                >
                                   {this.state.credentialTypes.map((type) => <option value={type} key={type}>
                                       {type}
                                   </option>)}
                               </Select>
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                Set Request Header
                                  <span className={classes.authHeader} >Authorization : Bearer</span>
                                    <TextField
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        className={classes.inputText}
                                        label="Access Token"
                                        name='accessToken'
                                        defaultValue=''
                                        onChange={this.handleChange('accessToken')}
                                    />
                            </Typography>
                        </div>
                    </Grid>
                </Grid>
                <div id="ui" />
            </React.Fragment>
        );
    }

}

ApiConsole.defaultProps = {
    handleInputs: false,
};

ApiConsole.propTypes = {
    handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
	classes: PropTypes.object.isRequired
}

export default withStyles(styles)(ApiConsole);