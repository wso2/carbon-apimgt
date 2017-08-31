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
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import {Link} from 'react-router-dom'
import {Table, Popconfirm, Menu, Dropdown, Row, Col, message} from 'antd';

import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import GridIcon from 'material-ui-icons/GridOn';
import ListIcon from 'material-ui-icons/List';


const menu = (
    <Menu>
        <Menu.Item>
            <Link to="/api/create/swagger">Create new API with Swagger</Link>
        </Menu.Item>
        <Menu.Item>
            <Link to="/api/create/rest">Create new API</Link>
        </Menu.Item>
    </Menu>
);
const ButtonGroup = Button.Group;
class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {listType: 'grid', apis: null};
        this.handleApiDelete = this.handleApiDelete.bind(this);
    }

    componentDidMount() {
        let api = new API();
        let promised_apis = api.getAll();
        promised_apis.then((response) => {
            this.setState({apis: response.obj});
        }).catch(error => {
            if (process.env.NODE_ENV !== "production")
                console.log(error);
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

    setListType = (value) => {
        this.setState({listType: value});
    }

    handleApiDelete(api_uuid, name) {
        const hideMessage = message.loading("Deleting the API ...",0);
        const api = new API();
        let promised_delete = api.deleteAPI(api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    message.error("Something went wrong while deleting the " + name + " API!");
                    hideMessage();
                    return;
                }
                message.success(name + " API deleted successfully!");
                let api = this.state.apis;
                for (let apiIndex in api.list) {
                    if (api.list.hasOwnProperty(apiIndex) && api.list[apiIndex].id === api_uuid) {
                        api.list.splice(apiIndex, 1);
                        break;
                    }
                }
                this.setState({active: false, apis: api});
                hideMessage();
            }
        );
    }

    render() {
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        const columns = [{
            title: 'Name',
            dataIndex: 'name',
            key: 'name',
            render: (text, record) => <Link to={"/apis/" + record.id}>{text}</Link>,
        }, {
            title: 'Context',
            dataIndex: 'context',
            key: 'context',
        }, {

            title: 'Version',
            dataIndex: 'version',
            key: 'version',
        }, {
            title: 'Action',
            key: 'action',
            render: text => {
                return null;
                //todo: Delete button should be enabled here after M6 based on permission model
            },
        }];
        return (
                <Grid container>
                    <Grid item xs={12}>
                        <Paper>
                            <Typography type="display2" gutterBottom
                                        style={{fontWeight:"300",padding:"10px 0 0 30px",margin:"0px"
                                            ,position:"relative"}}>
                                All Apis
                                <div style={{alignSelf:"flex-end", fontSize:"11px", margin:"auto", width: "200px",
                                    display:"block", float:"right"}}>
                                    <Button style={{padding:"0px", margin: "0px"}} color="primary" aria-label="add"  onClick={() => this.setListType('list')}
                                            >
                                        <ListIcon style={{width:"30px",height:"30px"}} />
                                    </Button>
                                    <Button style={{padding:"0px", margin: "0px"}} color="accent" aria-label="edit" onClick={() => this.setListType('grid')}
                                            >
                                        <GridIcon style={{width:"30px",height:"30px"}} />
                                    </Button>
                                </div>
                            </Typography>
                            <Typography type="caption" gutterBottom align="left"
                                        style={{fontWeight:"300",padding:"10px 0 10px 30px",margin:"0px"}}>
                                Listing all apis
                            </Typography>

                        </Paper>
                    </Grid>
                    <Grid item xs={12} style={{marginLeft:"40px"}}>
                            <div className="flex-container">

                            </div>
                            {
                                this.state.apis ?
                                    this.state.listType === "list" ?
                                        <Row type="flex" justify="start">
                                            <Col span={24}>
                                                <Table columns={columns} dataSource={this.state.apis.list} bordered
                                                       style={{margin: '10px'}}/>
                                            </Col>
                                        </Row>
                                        : <Grid container>
                                        {this.state.apis.list.map((api, i) => {
                                            return <ApiThumb key={api.id} listType={this.state.listType} api={api}/>
                                        })}
                                    </Grid>
                                    : <Loading/>
                            }

                    </Grid>
                </Grid>

        );
    }
}

export default Listing