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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import ApiContext from '../components/ApiContext';

const showEndpoint = function (api, type) {
    if (api.endpoint.length > 0) {
        for (let i = 0; i < api.endpoint.length; i++) {
            if (type === 'prod' && api.endpoint[i].type === 'http') {
                return api.endpoint[i].inline.endpointConfig.list[0].url;
            } else if (type === 'sand' && api.endpoint[i].type === 'sandbox_endpoints') {
                return api.endpoint[i].inline.endpointConfig.list[0].url;
            }
        }
    } else {
        return null;
    }
};
function Endpoints(props) {
    const { parentClasses } = props;

    return (
        <ApiContext.Consumer>
            {({ api }) => (
                <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            Endpoints
                        </Typography>
                        <Link to={'/apis/' + api.id + '/endpoints'}>
                            <Button variant='contained' color='default'>
                                Edit
                            </Button>
                        </Link>
                    </div>

                    {/* Production Endpoint (TODO) fix the endpoint
                                        info when it's available with the api object */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Production Endpoint
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {showEndpoint(api, 'prod') && <React.Fragment>{showEndpoint(api, 'prod')}</React.Fragment>}
                        {!showEndpoint(api, 'prod') && <React.Fragment>&lt;Not Configured&gt;</React.Fragment>}
                    </Typography>
                    {/* Sandbox Endpoint (TODO) fix the endpoint info when
                                        it's available with the api object */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Sandbox Endpoint
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {showEndpoint(api, 'sand') && <React.Fragment>{showEndpoint(api, 'sand')}</React.Fragment>}
                        {!showEndpoint(api, 'sand') && <React.Fragment>&lt;Not Configured&gt;</React.Fragment>}
                    </Typography>
                    {/* Sandbox Endpoint (TODO) fix the endpoint info when
                                        it's available with the api object */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Endpoint Security
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.endpointSecurity && <React.Fragment>{api.endpoint}</React.Fragment>}
                        {!api.endpointSecurity && <React.Fragment>&lt;Not configured&gt;</React.Fragment>}
                    </Typography>
                </Paper>
            )}
        </ApiContext.Consumer>
    );
}

Endpoints.propTypes = {
    parentClasses: PropTypes.object.isRequired,
};

export default Endpoints;
