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
import qs from 'qs';

import { Link } from 'react-router-dom';
import { Col, Menu, Row, Table } from 'antd';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import IconButton from 'material-ui/IconButton';
import List from '@material-ui/icons/List';
import GridIcon from '@material-ui/icons/GridOn';
import AddNewMenu from './AddNewMenu';
import Alert from '../../Shared/Alert';

import { ScopeValidation, resourceMethod, resourcePath } from '../../../data/ScopeValidation';
import ApiThumb from './ApiThumb';
import '../Apis.css';
import API from '../../../data/api.js';
import { Progress } from '../../Shared';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import SampleAPI from './SampleAPI';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 20,
    },
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginRight: 50,
    },
    addButton: {
        display: 'inline-block',
        marginBottom: 20,
        zIndex: 1,
    },
    popperClose: {
        pointerEvents: 'none',
    },
});
const menu = (
    <Menu>
        <Link to='/api/create/swagger'>
            <Menu.Item>Create new API with Swagger</Menu.Item>
        </Link>
        <Link to='/api/create/rest'>
            <Menu.Item>Create new API</Menu.Item>
        </Link>
    </Menu>
);
class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = { listType: 'grid', apis: null };
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.updateApi = this.updateApi.bind(this);
    }

    componentDidMount() {
        const api = new API();
        const promisedApis = api.getAll();
        promisedApis
            .then((response) => {
                this.setState({ apis: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    const params = qs.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    setListType = (value) => {
        this.setState({ listType: value });
    };

    updateApi(api_uuid) {
        const api = this.state.apis;
        for (const apiIndex in api.list) {
            if (api.list.hasOwnProperty(apiIndex) && api.list[apiIndex].id === api_uuid) {
                api.list.splice(apiIndex, 1);
                break;
            }
        }
        this.setState({ apis: api });
    }

    handleApiDelete(apiUUID, name) {
        Alert.info('Deleting the API ...');
        const api = new API();
        const promisedDelete = api.deleteAPI(apiUUID);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info('Something went wrong while deleting the ' + name + ' API!');
                return;
            }
            Alert.info(name + ' API deleted Successfully');
            const { api } = this.state;
            for (const apiIndex in api.list) {
                if (api.list.hasOwnProperty(apiIndex) && api.list[apiIndex].id === apiUUID) {
                    api.list.splice(apiIndex, 1);
                    break;
                }
            }
            this.setState({ active: false, apis: api });
        });
    }
    handleClickNew = () => {
        this.setState({ newMenuOpen: true });
    };
    handleCloseNew = () => {
        this.setState({ newMenuOpen: false });
    };
    render() {
        const classes = this.props.classes;
        const { apis } = this.state;
        if (this.state.notFound) {
            return <ResourceNotFound />;
        }
        const columns = [
            {
                title: 'Name',
                dataIndex: 'name',
                key: 'name',
                render: (text, record) => <Link to={'/apis/' + record.id}>{text}</Link>,
            },
            {
                title: 'Context',
                dataIndex: 'context',
                key: 'context',
            },
            {
                title: 'Version',
                dataIndex: 'version',
                key: 'version',
            },
            {
                title: 'Action',
                key: 'action',
                render: record => (
                    <ScopeValidation resourcePath={resourcePath.SINGLE_API} resourceMethod={resourceMethod.DELETE}>
                        <Button
                            style={{ fontSize: 10, padding: '0px', margin: '0px' }}
                            color='primary'
                            onClick={() => this.handleApiDelete(record.id, record.name)}
                        >
                            Delete
                        </Button>
                    </ScopeValidation>
                ),
            },
        ];
        if (!apis) {
            return <Progress />;
        } else if (apis.count === 0) {
            return <SampleAPI />;
        } else {
            return (
                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12} className={classes.titleBar}>
                        <div className={classes.buttonLeft}>
                            <div className={classes.title}>
                                <Typography variant='display2' gutterBottom>
                                    APIs
                                </Typography>
                            </div>
                            <AddNewMenu />
                        </div>
                        <div className={classes.buttonRight}>
                            <IconButton
                                className={classes.button}
                                aria-label='Delete'
                                onClick={() => this.setListType('list')}
                            >
                                <List />
                            </IconButton>
                            <IconButton
                                className={classes.button}
                                aria-label='Delete'
                                onClick={() => this.setListType('grid')}
                            >
                                <GridIcon />
                            </IconButton>
                        </div>
                    </Grid>

                    <Grid item xs={12}>
                        {this.state.listType === 'list' ? (
                            <Row type='flex' justify='start'>
                                <Col span={24}>
                                    <Table
                                        columns={columns}
                                        dataSource={this.state.apis.list}
                                        bordered
                                        locale={{ emptyText: 'There is no data to display' }}
                                    />
                                </Col>
                            </Row>
                        ) : (
                            <Grid container spacing={8}>
                                {this.state.apis.list.map((api, i) => {
                                    return (
                                        <ApiThumb
                                            key={api.id}
                                            listType={this.state.listType}
                                            api={api}
                                            updateApi={this.updateApi}
                                        />
                                    );
                                })}
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            );
        }
    }
}

Listing.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
