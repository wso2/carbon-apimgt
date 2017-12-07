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

import React, {Component} from 'react'

import Api from '../../../../data/api'

import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

class Security extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: {
                name: ''
            }
        };
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.updateData();
    }

    updateData() {
        let api = new Api();
        console.log(api);
        let promised_api = api.get(this.props.match.params.api_uuid);
        promised_api.then(response => {
            this.setState({api: response.obj});
            console.log(response.obj);
        });
    }

    render() {
        return (
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Typography className="page-title" type="display2">
                            {this.state.api.name} - <span>Threat Protection Policies</span>
                        </Typography>
                        <Typography type="caption" gutterBottom align="left" className="page-title-help">
                           Add or Remove Threat Protection Policies from APIs
                        </Typography>

                    </Paper>
                </Grid>
                <Grid item xs={12}  className="page-content">

                </Grid>
            </Grid>
        );
    }
}

export default Security