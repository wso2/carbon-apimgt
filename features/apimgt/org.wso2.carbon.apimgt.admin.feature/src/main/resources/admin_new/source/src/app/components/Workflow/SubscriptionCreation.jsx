/* eslint-disable react/jsx-props-no-spreading */
/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import { makeStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import MUIDataTable from 'mui-datatables';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';
import Alert from 'AppComponents/Shared/Alert';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import API from 'AppData/api';
import NativeSelect from '@material-ui/core/NativeSelect';
import Button from '@material-ui/core/Button';

const useStyles = makeStyles((theme) => ({
    searchBar: {
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
    },
    searchInput: {
        fontSize: theme.typography.fontSize,
    },
    block: {
        display: 'block',
    },
    contentWrapper: {
        margin: '40px 16px',
    },
    button: {
        borderColor: 'rgba(255, 255, 255, 0.7)',
    },
}));

/**
 * Render a list
 * @param {JSON} props props passed from parent
 * @returns {JSX} Header AppBar components.
 */
function ListLabels() {
    const intl = useIntl();
    const [data, setData] = useState(null);
    const [lists, setlists] = useState([]);

    /**
    * Mock API call
    * @returns {Promise}.
    */
    function apiCall() {
        return new Promise((resolve, reject) => {
            const restApi = new API();
            restApi
                .workflowsGet('AM_SUBSCRIPTION_CREATION')
                .then((result) => {
                    const workflowlist = result.body.list;
                    const array = [];
                    workflowlist.map((respond) => {
                        const obj = {};
                        obj.referenceId = respond.referenceId;
                        obj.status = 'APPROVED';
                        array.push(obj);
                        return array;
                    });
                    setlists(array);
                    resolve(workflowlist);
                })
                .catch((error) => {
                    reject(error);
                });
        });
    }

    const fetchData = () => {
    // Fetch data from backend
        setData(null);
        const promiseAPICall = apiCall();
        promiseAPICall.then((LocalData) => {
            setData(LocalData);
        })
            .catch((e) => {
                Alert.error(e);
            });
    };

    useEffect(() => {
        fetchData();
    }, []);

    const onChange = (referenceId, e) => {
        lists.map((res) => {
            if (res.referenceId === referenceId) {
                res.status = e.target.value;
            }
            return res.status;
        });
    };

    const onClick = (referenceId) => {
        const body = {};
        let status;
        lists.map((res) => {
            if (res.referenceId === referenceId) {
                status = res.status;
            }
            return status;
        });
        body.status = status;
        body.attributes = {};
        body.description = 'Approve workflow request.';

        const restApi = new API();
        let promisedupdateWorkflow = '';
        promisedupdateWorkflow = restApi.updateWorkflow(referenceId, body);
        return promisedupdateWorkflow
            .then(() => {
                return (
                    <FormattedMessage
                        id='Workflow.SubscriptionCreation.update.success'
                        defaultMessage='workflow status is updated successfully'
                    />
                );
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    throw (response.body.description);
                }
                return null;
            })
            .finally(() => {
                fetchData();
            });
    };

    const timeAgo = (prevDate) => {
        const diff = Number(new Date()) - prevDate;
        const minute = 60 * 1000;
        const hour = minute * 60;
        const day = hour * 24;
        const month = day * 30;
        const year = day * 365;
        switch (true) {
            case diff < minute: {
                const seconds = Math.round(diff / 1000);
                return `${seconds} ${seconds > 1 ? 'seconds' : 'second'} ago`;
            }
            case diff < hour: {
                return Math.round(diff / minute) + ' minutes ago';
            }
            case diff < day: {
                return Math.round(diff / hour) + ' hours ago';
            }
            case diff < month: {
                return Math.round(diff / day) + ' days ago';
            }
            case diff < year: {
                return Math.round(diff / month) + ' months ago';
            }
            case diff > year: {
                return Math.round(diff / year) + ' years ago';
            }
            default: {
                return '';
            }
        }
    };

    const pageProps = {
        help: (
            <HelpBase>
                <List component='nav' aria-label='main mailbox folders'>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.one'
                                    defaultMessage='Create a Microgateway label'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.two'
                                    defaultMessage='Assign the Microgateway label to an API'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.three'
                                    defaultMessage='View the Microgateway labels'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        /*
        pageStyle='half' center part of the screen.
        pageStyle='full' = Take the full content area.
        pageStyle='paperLess' = Avoid from displaying background paper. ( For dashbord we need this )
        */
        pageStyle: 'full',
        title: intl.formatMessage({
            id: 'Workflow.SubscriptionCreation.title.subscriptioncreation',
            defaultMessage: 'Subscription Creation - Approval Tasks',
        }),
    };

    const columProps = [
        {
            name: 'description',
            label: 'Description',
            options: {
                filter: false,
                sort: false,
            },
        },
        {
            name: 'workflowStatus',
            label: 'Status',
            options: {
                filter: false,
                sort: false,
            },
        },
        {
            name: 'elapsed time',
            label: 'Elapsed time',
            options: {
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const dataRow = data[tableMeta.rowIndex];
                    const { createdTime } = dataRow;
                    const prevDate = new Date(createdTime).getTime();

                    return (
                        <div>
                            {timeAgo(prevDate)}
                        </div>
                    );
                },
            },
        },
        {
            name: 'createdTime',
            label: 'Created On',
            options: {
                filter: false,
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const dataRow = data[tableMeta.rowIndex];
                    const { createdTime } = dataRow;
                    const date = new Date(createdTime);
                    const datestring = date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-'
                    + ('0' + date.getDate()).slice(-2) + ' ' + ('0' + date.getHours()).slice(-2) + ':'
                    + ('0' + date.getMinutes()).slice(-2) + ':' + ('0' + date.getSeconds()).slice(-2);

                    return (
                        <div>
                            {datestring}
                        </div>
                    );
                },
            },
        },
        {
            name: 'UpdateStatus',
            label: 'Update Status',
            options: {
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const options = ['APPROVED', 'REJECTED'];
                    const dataRow = data[tableMeta.rowIndex];
                    const { referenceId } = dataRow;
                    return (
                        <div>
                            <NativeSelect
                                value={[lists.find((x) => x.referenceId === referenceId)].status}
                                onChange={(e) => onChange(referenceId, e)}
                            >
                                {options.map((option) => <option key={option} value={option}>{option}</option>)}
                            </NativeSelect>
                        </div>
                    );
                },
            },
        },
        {
            name: 'action',
            label: 'Actions',
            options: {
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const dataRow = data[tableMeta.rowIndex];
                    const { referenceId } = dataRow;
                    return (
                        <div>
                            <Button
                                variant='contained'
                                color='primary'
                                onClick={() => onClick(referenceId)}
                            >
                            Complete
                            </Button>
                        </div>
                    );
                },
            },
        },
    ];

    const addButtonProps = {};
    const addButtonOverride = null;
    const noDataMessage = (
        <FormattedMessage
            id='AdminPages.Addons.ListBase.nodata.message'
            defaultMessage='No items yet'
        />
    );

    const EditComponent = (() => <span />);

    const searchActive = true;
    const searchPlaceholder = intl.formatMessage({
        id: 'Workflow.SubscriptionCreation.search.default',
        defaultMessage: 'Search by workflow request description',
    });


    const classes = useStyles();
    const [searchText, setSearchText] = useState('');

    const filterData = (event) => {
        setSearchText(event.target.value);
    };


    const columns = [
        ...columProps,
    ];

    const options = {
        filterType: 'checkbox',
        selectableRows: 'none',
        filter: false,
        search: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: null,
        responsive: 'stacked',
        searchText,
    };
    if (data && data.length === 0) {
        return (
            <ContentBase
                {...pageProps}
                pageStyle='small'
            >
                <Card className={classes.root}>
                    <CardActionArea>
                        <CardContent>
                            <Typography gutterBottom variant='h5' component='h2'>
                                <FormattedMessage
                                    id='Workflow.SubscriptionCreation.List.empty.title.subscriptioncreations'
                                    defaultMessage='Subscription Creation'
                                />

                            </Typography>
                            <Typography variant='body2' color='textSecondary' component='p'>
                                <FormattedMessage
                                    id='Workflow.SubscriptionCreation.List.empty.content.subscriptioncreations'
                                    defaultMessage={'There are no workflow pending requests for subscription creation.'
                                    + 'It is possible to approve or reject workflow pending requests of subscription '
                                    + ' creation. Workflow Approval Executor need to be enabled to introduce this '
                                    + 'approve reject process into system'}
                                />
                            </Typography>
                        </CardContent>
                    </CardActionArea>
                    <CardActions>
                        {addButtonOverride || (
                            <EditComponent updateList={fetchData} {...addButtonProps} />
                        )}
                    </CardActions>
                </Card>
            </ContentBase>
        );
    }
    if (!data) {
        return (
            <ContentBase pageStyle='paperLess'>
                <InlineProgress />
            </ContentBase>

        );
    }
    return (

        <>
            <ContentBase {...pageProps}>
                {(searchActive || addButtonProps) && (
                    <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                        <Toolbar>
                            <Grid container spacing={2} alignItems='center'>

                                <Grid item>
                                    {searchActive && (<SearchIcon className={classes.block} color='inherit' />)}
                                </Grid>
                                <Grid item xs>
                                    {searchActive && (
                                        <TextField
                                            fullWidth
                                            placeholder={searchPlaceholder}
                                            InputProps={{
                                                disableUnderline: true,
                                                className: classes.searchInput,
                                            }}
                                            onChange={filterData}
                                        />
                                    )}
                                </Grid>
                                <Grid item>
                                    {addButtonOverride || (
                                        <EditComponent
                                            updateList={fetchData}
                                            {...addButtonProps}
                                        />
                                    )}
                                    <Tooltip title={(
                                        <FormattedMessage
                                            id='AdminPages.Addons.ListBase.reload'
                                            defaultMessage='Reload'
                                        />
                                    )}
                                    >
                                        <IconButton onClick={fetchData}>
                                            <RefreshIcon className={classes.block} color='inherit' />
                                        </IconButton>
                                    </Tooltip>
                                </Grid>
                            </Grid>
                        </Toolbar>
                    </AppBar>
                )}

                {data && data.length > 0 && (
                    <MUIDataTable
                        title={null}
                        data={data}
                        columns={columns}
                        options={options}
                    />
                )}
                {data && data.length === 0 && (
                    <div className={classes.contentWrapper}>
                        <Typography color='textSecondary' align='center'>
                            {noDataMessage}
                        </Typography>
                    </div>
                )}
            </ContentBase>
        </>
    );
}

export default ListLabels;
