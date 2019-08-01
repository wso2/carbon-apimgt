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
import { PageNotFound } from 'AppComponents/Base/Errors/index';

import APICreateWrapper from './Default/APICreateWrapper';
import APIProductCreateWrapper from './Default/APIProductCreateWrapper';
import ApiCreateSwagger from './OpenAPI/ApiCreateOpenAPI';
import ApiCreateWSDL from './WSDL/ApiCreateWSDL';
import ApiCreateWebSocket from './WebSocket/ApiCreateWebSocket';

const styles = {
    content: {
        flexGrow: 1,
    },
};


/**
 *
 * Handle routing for APIs create
 * @param {*} props
 * @returns @inheritdoc
 */
function ApiCreate(props) {
    const { classes } = props;
    return (
        <main className={classes.content}>
            <Switch>
                <Route path='/apis/create/rest' component={APICreateWrapper} />
                <Route path='/api-products/create' component={APIProductCreateWrapper} />
                <Route path='/apis/create/openapi' component={ApiCreateSwagger} />
                <Route path='/apis/create/wsdl' component={ApiCreateWSDL} />
                <Route path='/apis/create/ws' component={ApiCreateWebSocket} />
                <Route component={PageNotFound} />
            </Switch>
        </main>
    );
}

ApiCreate.propTypes = {
    classes: PropTypes.shape({ content: PropTypes.string }).isRequired,
};

export default withStyles(styles)(ApiCreate);
