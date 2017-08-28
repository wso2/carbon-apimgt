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
        console.info(JSON.stringify(props.api));
    }


    render() {
        let details_link = "/apis/" + this.props.api.id;
        return (
          <Grid item xl={1} lg={2} md={3} sm={4} xs={6}>
              <Card>
                  <CardMedia>
                      <img alt="default-img" width="100%" src="/store/public/images/api/api-default.png"/>
                      <Typography type="headline" component="h2" style={{position:'absolute', bottom:'30px', left:'15px'}}>{this.props.api.name}</Typography>
                      <Typography component="p" style={{position:'absolute', bottom:'15px', left:'15px'}}>{this.props.api.version}</Typography>
                  </CardMedia>
                  <CardContent>
                      <Typography component="p">
                          {this.props.api.provider}
                      </Typography>
                      <Typography component="p">
                          {this.props.api.context}
                      </Typography>
                      <Typography component="p">
                          {this.props.api.description}
                      </Typography>
                  </CardContent>
                  <CardActions>
                    <Link to={details_link}>Details</Link>
                  </CardActions>
              </Card>
          </Grid>
        );
    }
}
export default ApiThumb
