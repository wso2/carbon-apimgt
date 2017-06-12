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

import React from 'react'
import {Link} from 'react-router-dom'
import Grid from 'material-ui/Grid';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';


class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        console.info(props.api);
    }


    render() {
        let details_link = "/apis/" + this.props.api.id;
        return (


            <Grid item xl={2} lg={3} md={4} sm={6} xs={12}>
                <Card>
                    <CardMedia>
                        <img alt="example" width="100%" src="/publisher/public/images/api/api-default.png"/>
                    </CardMedia>
                    <CardContent>
                        <Typography type="caption" gutterBottom align="left">
                            {this.props.api.version}
                        </Typography>
                        <Typography type="headline" component="h2">
                            <Link to={details_link}>{this.props.api.name}</Link>
                        </Typography>
                        <Typography type="headline" component="h5">
                            {this.props.api.context}
                        </Typography>
                        <Typography component="p">
                            {this.props.api.description}
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Button dense color="primary">
                            Subscribe
                        </Button>
                        <Button dense color="primary">
                            Learn More
                        </Button>
                    </CardActions>
                </Card>
            </Grid>
        );
    }
}
export default ApiThumb
