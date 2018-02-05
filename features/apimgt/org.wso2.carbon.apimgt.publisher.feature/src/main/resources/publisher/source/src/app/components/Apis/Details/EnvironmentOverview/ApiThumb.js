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

import React, {Component} from 'react'
import Typography from 'material-ui/Typography';
import ExpansionPanel, {ExpansionPanelDetails, ExpansionPanelSummary} from 'material-ui/ExpansionPanel';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
import Input, {InputLabel} from 'material-ui/Input';
import {FormControl} from 'material-ui/Form';
import Grid from 'material-ui/Grid';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Avatar from 'material-ui/Avatar';

class ApiThumb extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const {name, version, context, lifecycle} = this.props;

        return (
            <Grid item xs={6} sm={4} md={3} lg={2} xl={2}>
                <Card>
                    <CardMedia image="/publisher/public/app/images/api/api-default.png">
                        <img src="/publisher/public/app/images/api/api-default.png" style={{width:"100%"}}/>
                    </CardMedia>
                    <CardContent>
                        <Typography type="headline" component="h2">
                            {name}
                        </Typography>
                        <Typography component="div">
                            <Avatar style={{backgroundColor: "green", float: "right"}}>P</Avatar>
                            <p>{version}</p>
                            <p>{context}</p>
                            <p>{lifecycle}</p>
                        </Typography>
                    </CardContent>
                </Card>
            </Grid>
        );
    }
}

export default ApiThumb;
