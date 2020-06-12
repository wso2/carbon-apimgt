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

import React, { useContext, useState } from 'react';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import { Link } from 'react-router-dom';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import { isRestricted } from 'AppData/AuthManager';
import { makeStyles } from '@material-ui/core/styles';
import MicroGateway from 'AppComponents/Apis/Details/Environments/MicroGateway';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    saveButton: {
        marginTop: theme.spacing(3),
    },
}));

/**
 * Renders an Environments list
 * @class Environments
 * @extends {React.Component}
 */
export default function Environments() {
    const classes = useStyles();
    const { api, updateAPI } = useContext(APIContext);
    const { settings } = useAppContext();
    const [gatewayEnvironments, setGatewayEnvironments] = useState([...api.gatewayEnvironments]);
    const [selectedMgLabel, setSelectedMgLabel] = useState([...api.labels]);

    const [isUpdating, setUpdating] = useState(false);

    /**
     *
     * Handle the Environments save button action
     */
    function addEnvironments() {
        setUpdating(true);
        updateAPI({
            gatewayEnvironments,
            labels: selectedMgLabel,
        })
            .then(() => Alert.info('API Update Successfully'))
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while updating the environments');
                }
                console.error(error);
            })
            .finally(() => setUpdating(false));
    }

    return (
        <>
            <Typography variant='h4' gutterBottom>
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.APIGateways'
                    defaultMessage='API Gateways'
                />
            </Typography>
            <Paper className={classes.saveButton}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell />
                            <TableCell align='left'>Name</TableCell>
                            <TableCell align='left'>Type</TableCell>
                            <TableCell align='left'>Server URL</TableCell>
                            {api.isWebSocket() ? (
                                <>
                                    <TableCell align='left'>WS Endpoint</TableCell>
                                    <TableCell align='left'>WSS Endpoint</TableCell>
                                </>
                            ) : (
                                <>
                                    <TableCell align='left'>HTTP Endpoint</TableCell>
                                    <TableCell align='left'>HTTPS Endpoint</TableCell>
                                </>
                            )}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {settings.environment.map((row) => (
                            <TableRow key={row.name}>
                                <TableCell padding='checkbox'>
                                    <Checkbox
                                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                        checked={gatewayEnvironments.includes(row.name)}
                                        onChange={
                                            (event) => {
                                                const { checked, name } = event.target;
                                                if (checked) {
                                                    setGatewayEnvironments([...gatewayEnvironments, name]);
                                                } else {
                                                    setGatewayEnvironments(
                                                        gatewayEnvironments.filter((env) => env !== name),
                                                    );
                                                }
                                            }
                                        }
                                        name={row.name}
                                        color='primary'
                                    />
                                </TableCell>
                                <TableCell component='th' scope='row'>
                                    {row.name}
                                </TableCell>
                                <TableCell align='left'>{row.type}</TableCell>
                                <TableCell align='left'>{row.serverUrl}</TableCell>

                                {api.isWebSocket() ? (
                                    <>
                                        <TableCell align='left'>{row.endpoints.ws}</TableCell>
                                        <TableCell align='left'>{row.endpoints.wss}</TableCell>
                                    </>
                                ) : (
                                    <>
                                        <TableCell align='left'>{row.endpoints.http}</TableCell>
                                        <TableCell align='left'>{row.endpoints.https}</TableCell>
                                    </>
                                )}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Paper>

            {!api.isWebSocket()
                && (
                    <MicroGateway
                        selectedMgLabel={selectedMgLabel}
                        setSelectedMgLabel={setSelectedMgLabel}
                        api={api}
                    />
                )}

            <Grid
                container
                direction='row'
                alignItems='flex-start'
                spacing={1}
            >
                <Grid item>
                    <Button
                        className={classes.saveButton}
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || isUpdating}
                        type='submit'
                        variant='contained'
                        color='primary'
                        onClick={addEnvironments}
                    >
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.save'
                            defaultMessage='Save'
                        />
                        {isUpdating && <CircularProgress size={20} />}
                    </Button>
                </Grid>
                <Grid item>
                    <Link to={'/apis/' + api.id + '/overview'}>
                        <Button className={classes.saveButton}>
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </Link>
                </Grid>
            </Grid>
            {isRestricted(['apim:api_create'], api) && (
                <Grid item>
                    <Typography variant='body2' color='primary'>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.update.not.allowed'
                            defaultMessage={
                                '* You are not authorized to update particular fields of'
                                + ' the API due to insufficient permissions'
                            }
                        />
                    </Typography>
                </Grid>
            )}
        </>
    );
}
