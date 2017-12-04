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

import React, {Component} from 'react';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';

const ResourceNotFound = (props) => {
    return (
        <Paper elevation={4}>
            <Typography type="headline" component="h3">
                404 Resource Not Found!
            </Typography>
            <Typography type="body1" component="p">
                Can't find the resource you are looking for
                <span style={{color: 'green'}}> {props.response ? props.response.statusText : ""} </span>
            </Typography>
        </Paper>
    );
};

export default ResourceNotFound