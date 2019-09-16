/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useContext, useState, useEffect } from 'react';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';

import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import { Link } from 'react-router-dom';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import API from '../../../../data/api';

const useStyles = makeStyles(theme => ({
    root: {
        width: theme.custom.contentAreaWidth,
        marginTop: theme.spacing(3),
        overflowX: 'auto',
    },
    table: {
        minWidth: 650,
    },
}));

const StyledTableCell = withStyles(() => ({
    head: {
        fontSize: 13,
    },
    body: {
        fontSize: 14,
    },
}))(TableCell);

/**
 * Renders an External Store list
 * @class ExternalStores
 * @extends {React.Component}
 */
export default function ExternalStores() {
    const { api } = useContext(APIContext);
    const { settings } = useAppContext();
    const [allExternalStores, setAllExternalStores] = useState([]);
    const [publishedExternalStores, setPublishedExternalStores] = useState([]);
    const [isUpdating, setUpdating] = useState(false);
    const classes = useStyles();
    if (!settings.externalStoresEnabled) {
        return (
            <div className='message message-warning'>
                <h4>
                    <FormattedMessage
                        id='ExternalStores.Errors.StoresNotFound.header'
                        defaultMessage='External Stores not found for api: '
                    />
                    <span style={{ color: 'green' }}> {api.id} </span>
                </h4>
            </div>
        );
    }
    /**
     * Gets published external stores
     */
    function getPublishedExternalStores() {
        API.getPublishedExternalStores(api.id)
            .then((response) => {
                const publishedStoreIds = response.body.list.map(store => store.id);
                setPublishedExternalStores(publishedStoreIds);
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while getting published external stores');
                }
            });
    }

    useEffect(() => {
        API.getAllExternalStores().then((response) => {
            setAllExternalStores([...response.body.list]);
        });
        getPublishedExternalStores();
    }, []);

    /**
     * Handle publish to external store button action
     */
    function updateStores() {
        setUpdating(true);
        API.publishAPIToExternalStores(api.id, publishedExternalStores)
            .then((response) => {
                Alert.success('Successfully Published to external stores: '
                    + response.body.list.map(store => store.id));
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while updating the external stores');
                }
            })
            .finally(() => {
                setUpdating(false);
                getPublishedExternalStores();
            });
    }

    return (
        <div>
            <div>
                <Typography variant='h4' align='left' >
                    <FormattedMessage
                        id='Apis.Details.Environments.Environments.External-Stores'
                        defaultMessage='External Stores'
                    />
                </Typography>
                <Paper className={classes.root}>
                    <Table className={classes.table}>
                        <TableHead>
                            <TableRow>
                                <StyledTableCell />
                                <StyledTableCell>Name</StyledTableCell>
                                <StyledTableCell align='right'>Type</StyledTableCell>
                                <StyledTableCell align='right'>Endpoint</StyledTableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {allExternalStores.map(row => (
                                <TableRow key={row.id}>
                                    <StyledTableCell padding='checkbox'>
                                        <Checkbox
                                            checked={publishedExternalStores.includes(row.id)}
                                            disabled={api.lifeCycleStatus !== 'PUBLISHED'}
                                            onChange={
                                                (event) => {
                                                    const { checked, name } = event.target;
                                                    if (checked) {
                                                        if (!publishedExternalStores.includes(name)) {
                                                            setPublishedExternalStores([
                                                                ...publishedExternalStores, name]);
                                                        }
                                                    } else {
                                                        setPublishedExternalStores(publishedExternalStores
                                                            .filter(store => store !== name));
                                                    }
                                                }
                                            }
                                            name={row.id}
                                        />
                                    </StyledTableCell>
                                    <StyledTableCell component='th' scope='row'>
                                        {row.displayName}
                                    </StyledTableCell>
                                    <StyledTableCell align='right'>{row.type}</StyledTableCell>
                                    <StyledTableCell align='right'>
                                        <a
                                            target='_blank'
                                            rel='noopener noreferrer'
                                            href={row.endpoint}
                                        >{row.endpoint}
                                        </a>
                                    </StyledTableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
                <Grid container>
                    <Grid
                        container
                        direction='row'
                        alignItems='center'
                        spacing={4}
                        style={{ marginTop: 20 }}
                    >
                        <Grid item>
                            <Button
                                type='submit'
                                variant='contained'
                                color='primary'
                                disabled={isUpdating || api.lifeCycleStatus !== 'PUBLISHED'}
                                onClick={updateStores}
                            >
                                <FormattedMessage
                                    id='Apis.Details.Environments.ExternalStores.save'
                                    defaultMessage='Save'
                                />
                                {isUpdating && <CircularProgress size={20} />}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Link to={'/apis/' + api.id + '/overview'}>
                                <Button>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.ExternalStores.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        </div>
    );
}
