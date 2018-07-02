/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';

import EndpointForm from '../Create/EndpointForm';
import Endpoint from '../../../data/Endpoint';
import Progress from '../../Shared/Progress';

/**
 * Render global endpoint details.
 * @class EndpointDetails
 * @extends {Component}
 */
class EndpointDetails extends Component {
    /**
     * Creates an instance of EndpointDetails.
     * @param {any} props @inheritDoc
     * @memberof EndpointDetails
     */
    constructor(props) {
        super(props);
        this.state = {
            endpoint: false,
        };
    }

    /**
     * @inheritDoc
     * @memberof EndpointDetails
     */
    componentDidMount() {
        const { endpointUUID } = this.props.match.params;
        const promisedEndpoint = Endpoint.get(endpointUUID);
        promisedEndpoint.then(endpoint => this.setState({ endpoint }));
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Global endpoint
     * @memberof EndpointDetails
     */
    render() {
        const { endpoint } = this.state;
        if (!endpoint) {
            return <Progress />;
        }
        return (
            <div>
                <Grid container spacing={0} justify='center'>
                    <Grid item md={10}>
                        <Typography variant='headline'> {endpoint.name} </Typography>
                        <EndpointForm endpoint={endpoint} />
                    </Grid>
                </Grid>
            </div>
        );
    }
}

EndpointDetails.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            endpointUUID: PropTypes.string,
        }),
    }).isRequired,
};

export default EndpointDetails;
