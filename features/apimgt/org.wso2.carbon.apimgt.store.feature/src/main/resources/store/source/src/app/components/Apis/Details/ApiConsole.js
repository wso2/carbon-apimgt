import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import PropTypes from 'prop-types';

import SwaggerUI from "swagger-ui";
import StandalonePreset from 'swagger-ui/dist/swagger-ui-standalone-preset';
import Utils from "../../../data/Utils";
import Api from '../../../data/api'
import AuthManager from '../../../data/AuthManager'
import 'swagger-ui/dist/swagger-ui.css';


class ApiConsole extends React.Component {
    constructor(props){
        super(props);
        this.api_uuid = this.props.match.params.api_uuid;
    }
    componentDidMount() {
        const api = new Api();
        let promised_swagger = api.getSwaggerByAPIId(this.api_uuid);
        
        promised_swagger.then(
            response => {
                console.info(response);
                let url = response.url;
                const swaggerUI = SwaggerUI({
                    dom_id: "#ui",
                    url: response.url,
                    requestInterceptor: (req) => {
                        // Only set Authorization header if the request matches the spec URL
                        if (req.url === url) {
                          req.headers.Authorization =  "Bearer " + AuthManager.getUser().getPartialToken();
                        }
                        return req;
                      },
                    presets: [
                            SwaggerUI.presets.apis,
                            StandalonePreset

                    ],
                    plugins: [
                        SwaggerUI.plugins.DownloadUrl,
                    ],
                    layout: "StandaloneLayout"
                })
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
            <div id="ui" />
        );
    }
}

ApiConsole.propTypes = {
	optionalArray: PropTypes.array
}

export default ApiConsole;