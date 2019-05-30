/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import 'swagger-ui/dist/swagger-ui.css';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';

import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import SwaggerUI from './SwaggerUI';

/**
 *
 *
 * @param {*} theme
 */
const styles = {
    authHeader: {
        marginLeft: '105px',
        color: '#555555',
        backgroundColor: '#eeeeee',
        border: '1px solid #ccc',
    },
    inputText: {
        marginLeft: '40px',
        minWidth: '400px',
    },
    gatewaySelect: {
        marginLeft: '173px',
        minWidth: '400px',
    },
    credentialSelect: {
        marginLeft: '150px',
        minWidth: '400px',
        marginRight: '10px',
    },
    grid: {
        spacing: 20,
        marginTop: '30px',
        marginBottom: '30px',
        paddingLeft: '90px',
    },
};
/**
 *
 *
 * @class ApiConsole
 * @extends {React.Component}
 */
class ApiConsole extends React.Component {
    /**
     *Creates an instance of ApiConsole.
     * @param {*} props
     * @memberof ApiConsole
     */
    constructor(props) {
        super(props);
        this.state = {};
        this.handleChanges = this.handleChanges.bind(this);
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    componentDidMount() {
        const api = new Api();
        const { match } = this.props;
        const apiID = match.params.api_uuid;
        const promisedSwagger = api.getSwaggerByAPIId(apiID);
        const promisedAPI = api.getAPIById(apiID);

        Promise.all([promisedAPI, promisedSwagger])
            .then((responses) => {
                const data = responses.map(response => response.body);
                const swagger = data[1];
                const apiObj = data[0];
                swagger.basePath = apiObj.context;
                swagger.host = 'localhost:8243';
                this.setState({ api: apiObj, swagger });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    handleChanges(event) {
        const target = event.currentTarget;
        const { name, value } = target;
        this.setState({ [name]: value });
    }

    /**
     *
     *
     * @returns
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const { api, notFound, swagger } = this.state;

        if (api == null || swagger == null) {
            return <Progress />;
        }
        if (notFound) {
            return 'API Not found !';
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    <Grid item md={12}>
                        <Typography variant='subheading' gutterleft>
                            Set Request Header
                            <span className={classes.authHeader}>Authorization : Bearer</span>
                            <TextField
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                className={classes.inputText}
                                label='Access Token'
                                name='accessToken'
                                defaultValue=''
                                onChange={this.handleChanges}
                            />
                        </Typography>
                    </Grid>
                </Grid>
                <SwaggerUI spec={swagger} />
            </React.Fragment>
        );
    }
}

ApiConsole.defaultProps = {
    // handleInputs: false,
};

ApiConsole.propTypes = {
    // handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiConsole);
