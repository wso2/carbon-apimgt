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
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

import SwaggerUI from "swagger-ui";
import StandalonePreset from 'swagger-ui/dist/swagger-ui-standalone-preset';
import Utils from "../../../../data/Utils";
import Api from '../../../../data/api'
import AuthManager from '../../../../data/AuthManager'
import 'swagger-ui/dist/swagger-ui.css';

import Input, {InputLabel} from '@material-ui/core/Input';
import {MenuItem} from '@material-ui/core/Menu';
import Select from '@material-ui/core/Select';
import Progress from '../../../Shared/Progress';
import TextField from '@material-ui/core/TextField';
import CommonColors from '@material-ui/core/colors/common';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
    inputText: {
        marginLeft: '145px',
        minWidth: '400px'
    },
    appSelect: {
        marginLeft: '300px',
        minWidth: '400px'
    },
    disabled: {
        color: CommonColors.black
    },
    keySelect: {
        marginLeft: '275px',
        minWidth: '360px',
        marginRight: '10px'
    },
    grid: {
        spacing: 2,
        justifyContent: 'center',
        marginTop: '30px',
        marginBottom: '30px'
    }

});

class ApiConsole extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            subscribedApplications: null,
            applicationId: '',
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
        let promised_swagger = api.getSwaggerByAPIId(this.api_uuid);
        let promised_applications = api.getAllApplications();
        let promised_subscriptions = api.getSubscriptions(this.api_uuid, null);

        Promise.all([promised_swagger, promised_applications, promised_subscriptions]).then(responses => {

          let swagger = responses[0];
          let applications = responses[1].obj.list;
          let subscriptions = responses[2].obj.list;
          let url = swagger.url;

          let subscribedApplications = [];
          //get the application IDs of existing subscriptions
          subscriptions.map(element => subscribedApplications.push({applicationId: element.applicationId}));

          //Get application names of the subscribed applications
          for (let i = 0; i < applications.length; i++) {
              let applicationId = applications[i].applicationId;
              let applicationName = applications[i].name;

              for ( var j =0; j < subscribedApplications.length; j++ ) {
                  if(subscribedApplications[j].applicationId === applicationId) {
                    subscribedApplications[j].name = applicationName;
                  }
              }
          }

          if (subscribedApplications && subscribedApplications.length > 0) {
            this.setState({applicationId: subscribedApplications[0].applicationId});
          }
          this.setState({subscribedApplications: subscribedApplications});

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

        const { classes, endpoint } = this.props;
        let { handleInputs } = this.props;
        const isReadOnly = !handleInputs; // Showing the endpoint details
        handleInputs = handleInputs || null;
        if (this.state.subscribedApplications == null) {
              return <Progress />;
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    <Grid item md={6}>
                        <div>
                            <Typography variant='subheading'>
                               Try
                               <Select
                                   value={this.state.applicationId}
                                   onChange={this.handleChange('applicationId')}
                                   className={classes.appSelect}
                               >
                                   {this.state.subscribedApplications.map((app) => <option value={app.applicationId} key={app.applicationId}>
                                       {app.name}
                                   </option>)}
                               </Select>
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                Using
                                Key
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                Set Request Header
                                <TextField
                                    className={classes.inputText}
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    name='maxTps'
                                    defaultValue=''
                                    onChange={handleInputs}
                                    InputProps={{ classes: { disabled: classes.disabled } }}
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
	optionalArray: PropTypes.array,
	classes: PropTypes.object.isRequired
}

export default withStyles(styles)(ApiConsole);