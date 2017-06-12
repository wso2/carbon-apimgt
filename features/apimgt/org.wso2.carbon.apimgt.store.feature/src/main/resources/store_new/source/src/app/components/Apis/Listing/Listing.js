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
import qs from 'qs'

import ApiThumb from './ApiThumb'
import '../Apis.css'
import API from '../../../data/api.js'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import {Link} from 'react-router-dom'
import Grid from 'material-ui/Grid';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import BottomNavigation, { BottomNavigationButton } from 'material-ui/BottomNavigation';
import ListIcon from 'material-ui-icons/List';
import GridOnIcon from 'material-ui-icons/GridOn';


class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {listType: 'grid', apis: null, value: 1};
    }

    handleChange = (event, value) => {
        this.setState({ value });
        if(value === 0 ){
            this.setState({listType:"list"});
        } else if(value === 1 ){
            this.setState({listType:"grid"});
        }
        // this.setState({listType: value});
    };
    componentDidMount() {
        let api = new API();
        let promised_apis = api.getAll();
        promised_apis.then((response) => {
            this.setState({apis: response.obj});
        }).catch(error => {
            let status = error.status;
            if (status === 404) {
                this.setState({notFound: true});
            } else if (status === 401) {
                this.setState({isAuthorize: false});
                let params = qs.stringify({reference: this.props.location.pathname});
                this.props.history.push({pathname: "/login", search: params});
            }
        });
    }



    render() {
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }

        const { value } = this.state;
        return (
            <div style={{padding:"20"}}>
                <BottomNavigation value={value} onChange={this.handleChange} style={{float:"right"}}>
                    <BottomNavigationButton label="List" icon={<ListIcon />} />
                    <BottomNavigationButton label="Grid" icon={<GridOnIcon />} />
                </BottomNavigation>

                {
                    this.state.apis ?
                        this.state.listType === "list" ?
                            <Grid container gutter={24} >
                                <Grid item xl={2} lg={3} md={4} sm={6} xs={12}>
                                    <Card>
                                        <CardMedia>
                                            <img alt="example" width="100%" src="/publisher/public/images/api/api-default.png"/>
                                        </CardMedia>
                                        <CardContent>
                                            <Typography type="headline" component="h2">
                                                Lizard
                                            </Typography>
                                            <Typography component="p">
                                                Lizards are a widespread group of squamate reptiles, with over 6,000 species, ranging
                                                across all continents except Antarctica
                                            </Typography>
                                        </CardContent>
                                        <CardActions>
                                            <Button dense color="primary">
                                                Share
                                            </Button>
                                            <Button dense color="primary">
                                                Learn More
                                            </Button>
                                        </CardActions>
                                    </Card>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                </Grid>
                            </Grid>

                            :
                            <Grid container gutter={24} >
                                {this.state.apis.list.map((api, i) => {
                                    return <ApiThumb api={api}/>
                                })}


                            </Grid>
                        : <div>Loading... </div>
                }

            </div>
        );
    }
}

export default Listing