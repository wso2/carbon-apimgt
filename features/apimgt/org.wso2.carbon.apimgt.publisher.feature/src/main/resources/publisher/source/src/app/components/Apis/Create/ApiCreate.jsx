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
import React from 'react';

import { Route, Switch } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

import ApiCreateEndpoint from './Endpoint/ApiCreateEndpoint';
import ApiCreateSwagger from './Swagger/ApiCreateSwagger';
import ApiCreateWSDL from './WSDL/ApiCreateWSDL';
import APICreateTopMenu from './Components/APICreateTopMenu';

const styles = {
    content: {
        flexGrow: 1,
    },
};

function ApiCreate(props) {
    const { classes } = props;
    return (
        <main className={classes.content}>
            <APICreateTopMenu />
            <Switch>
                <Route path='/apis/create/rest' component={ApiCreateEndpoint} />
                <Route path='/apis/create/swagger' component={ApiCreateSwagger} />
                <Route path='/apis/create/wsdl' component={ApiCreateWSDL} />
            </Switch>
        </main>
    );
}

ApiCreate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiCreate);
