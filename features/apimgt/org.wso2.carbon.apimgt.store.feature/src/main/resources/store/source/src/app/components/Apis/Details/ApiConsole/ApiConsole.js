import React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import PropTypes from 'prop-types';

import SwaggerUI from "swagger-ui";
import StandalonePreset from 'swagger-ui/dist/swagger-ui-standalone-preset';
import Utils from "../../../../data/Utils";
import Api from '../../../../data/api'
import AuthManager from '../../../../data/AuthManager'
import 'swagger-ui/dist/swagger-ui.css';

import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import {FormControl} from 'material-ui/Form';
import Select from 'material-ui/Select';
import Progress from '../../../Shared/Progress';
import TextField from 'material-ui/TextField';
import CommonColors from 'material-ui/colors/common';
import { withStyles } from 'material-ui/styles';

const styles = () => ({
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
    }


});

class ApiConsole extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            applications: null,
            selectedApplication: null,
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

        Promise.all([promised_swagger, promised_applications]).then(responses => {

          let swaggerResponse = responses[0];
          let url = swaggerResponse.url;
          this.setState({applications: responses[1].obj.list});
          const swaggerUI = SwaggerUI({
              dom_id: "#ui",
              url: swaggerResponse.url,
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

    handleChange(e) {
        this.setState({selectedApplication: e.target.value});
        if (this.props.handlePolicies) {
            this.props.handlePolicies(e.target.value);
        }
    }

    render() {

        const { classes, endpoint } = this.props;
        let { handleInputs } = this.props;
        const isReadOnly = !handleInputs; // Showing the endpoint details
        handleInputs = handleInputs || null;
        console.info("printing in render");
        console.info(this.state.applications);
        console.info(this.state.setSwagger);
        if (this.state.applications == null) {
              return <Progress />;
        }

        console.info("apps are null");

        return (
            <React.Fragment>
                <Grid container spacing={2} justify='center'>
                    <Grid item md={6}>
                        <div>
                            <Typography variant='subheading'>
                               Try
                            <Select
                                margin="none"
                                className={classes.appSelect}
                                value={this.state.selectedApplication}
                                onChange={this.handleChange}
                                input={<Input id="name-multiple"/>}
                                MenuProps={{
                                    PaperProps: {
                                        style: {
                                            width: 200,
                                        },
                                    },
                                }}
                            >
                                {this.state.applications.map(app => (
                                    <MenuItem
                                        key={app.applicationId}
                                        value={app.name}
                                        style={{
                                            fontWeight: this.state.applications.indexOf(app.name) !== -1 ? '500' : '400',
                                        }}
                                    >
                                        {app.name}
                                    </MenuItem>
                                ))}
                            </Select></Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterLeft>
                                Using
                                <Select
                                    margin="none"
                                    className={classes.keySelect}
                                    value={this.state.selectedApplication}
                                    onChange={this.handleChange}
                                    input={<Input id="name-multiple"/>}
                                    MenuProps={{
                                        PaperProps: {
                                            style: {
                                                width: 200,
                                            },
                                        },
                                    }}
                                >
                                    {this.state.applications.map(app => (
                                        <MenuItem
                                            key={app.applicationId}
                                            value={app.name}
                                            style={{
                                                fontWeight: this.state.applications.indexOf(app.name) !== -1 ? '500' : '400',
                                            }}
                                        >
                                            {app.name}
                                        </MenuItem>
                                    ))}
                                </Select>Key
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterLeft>
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
    classes: PropTypes.shape({}).isRequired,
	optionalArray: PropTypes.array
}

export default withStyles(styles)(ApiConsole);