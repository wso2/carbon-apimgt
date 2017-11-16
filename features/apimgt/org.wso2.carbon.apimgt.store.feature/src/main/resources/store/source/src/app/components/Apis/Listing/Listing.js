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
import APiTableRow from './ApiTableRow'
import '../Apis.css'
import API from '../../../data/api.js'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import {Link} from 'react-router-dom'
import Grid from 'material-ui/Grid';
import Card, {CardActions, CardContent, CardMedia} from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import BottomNavigation, {BottomNavigationButton} from 'material-ui/BottomNavigation';
import ListIcon from 'material-ui-icons/List';
import GridOnIcon from 'material-ui-icons/GridOn';

import Table, {TableBody, TableCell, TableHead, TableRow} from 'material-ui/Table';
import Paper from 'material-ui/Paper';


class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {listType: 'grid', apis: null, value: 1};
    }

    handleChange = (event, value) => {
        this.setState({value});
        if (value === 0) {
            this.setState({listType: "list"});
        } else if (value === 1) {
            this.setState({listType: "grid"});
        }
        // this.setState({listType: value});
    };

    componentDidMount() {
        let api = new API();


        let promised_tires = api.getAllTiers("application");
        promised_tires.then(
            response => {
                let tires = [];
                for (let i = 0; i < response.obj.count; i++) {
                    let tier = {};
                    tier.name = response.obj.list[i].name;
                    tier.description = response.obj.list[i].description;
                    tires.push(tier);
                }
                this.setState({tiers: tires});
                debugger;
            }
        ).catch(error => {
            let status = error.status;
            if (status === 404) {
                this.setState({notFound: true});
            } else if (status === 401) {
                this.setState({isAuthorize: false});
                let params = qs.stringify({reference: this.props.location.pathname});
                this.props.history.push({pathname: "/login", search: params});
            }
        });


        let promised_apis = api.getAllAPIs();
        promised_apis.then((response) => {
            //debugger
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
        let forum_link = "/forum";
        let create_app_link = "/application/create";
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }

        const {value} = this.state;


        return (
            <div style={{padding: "20px"}}>
                <BottomNavigation value={value} onChange={this.handleChange} style={{float: "right"}}>
                    <BottomNavigationButton label="List" icon={<ListIcon/>}/>
                    <BottomNavigationButton label="Grid" icon={<GridOnIcon/>}/>
                </BottomNavigation>
                {
                    this.state.apis ?
                        this.state.listType === "grid" ?

                            <Grid container>
                                {this.state.apis.list.map(api => {
                                    return <ApiThumb api={api} key={api.id}/>
                                })}
                            </Grid>
                            :

                            <Grid container>
                                <Grid item xs>
                                    <Paper>
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell>Name</TableCell>
                                                    <TableCell>Version</TableCell>
                                                    <TableCell>Context</TableCell>
                                                    <TableCell>Description</TableCell>
                                                    <TableCell></TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {this.state.apis.list.map(api => {
                                                    return <APiTableRow api={api} key={api.id}/>
                                                })}
                                            </TableBody>
                                        </Table>
                                    </Paper>
                                </Grid>
                            </Grid>
                        :
                        <Loading/>
                }
            </div>
        );
    }
}

export default Listing
