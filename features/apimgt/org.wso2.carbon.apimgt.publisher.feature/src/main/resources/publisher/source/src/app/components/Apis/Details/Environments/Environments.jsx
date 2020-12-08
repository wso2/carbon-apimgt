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
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableContainer from '@material-ui/core/TableContainer';
import Switch from '@material-ui/core/Switch';
import clsx from 'clsx';
import TableRow from '@material-ui/core/TableRow';
import Divider from '@material-ui/core/Divider';
import Checkbox from '@material-ui/core/Checkbox';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Chip from '@material-ui/core/Chip';
import { isRestricted } from 'AppData/AuthManager';
import { makeStyles } from '@material-ui/core/styles';
import MicroGateway from 'AppComponents/Apis/Details/Environments/MicroGateway';
import Kubernetes from 'AppComponents/Apis/Details/Environments/Kubernetes';
import HelpOutlineIcon from '@material-ui/icons/HelpOutline';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import RestoreIcon from '@material-ui/icons/Restore';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    saveButton: {
        marginTop: theme.spacing(3),
    },


    changeCard: {
        boxShadow: 15,
        borderRadius: '10px',
        borderWidth: '2px',
        borderColor: 'cyan',
    },
    noChangeCard: {
        boxShadow: 15,
        borderRadius: '10px',


    },
    cardHeight: {
        boxShadow: 1,
        height: '100%',
    },
    cardContentHeight: {
        boxShadow: 1,
        height: '75%',
    },
    cardActionHeight: {
        boxShadow: 1,
        height: '25%%',
    },
    textOverlay: {


        overflow: 'hidden',
        maxHeight: '100%',
        maxWidth: '100%',
        cursor: 'pointer',
        '&:hover': { overflow: 'visible' },
    }
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
    // const [gatewayEnvironments, setGatewayEnvironments] = useState([...api.gatewayEnvironments]);
    const [selectedMgLabel, setSelectedMgLabel] = useState([...api.labels]);
    const [isUpdating, setUpdating] = useState(false);
    const [selectedDeployments, setSelectedDeployments] = useState([...api.deploymentEnvironments]);

    const restApi = new API();
    const [allDeployments, setAllDeployments] = useState([]);
    const [allRevisions, setRevisions] = useState([]);
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };
    const [open1, setOpen1] = React.useState(false);

    const handleClickOpen1 = () => {
        setOpen1(true);
    };

    const handleClose1 = () => {
        setOpen1(false);
    };

    const [checked, setChecked] = React.useState([]);
    const handleChange = (event) => {
        setChecked(event.target.checked);
    };

    useEffect(() => {
        restApi.getDeployments()
            .then((result) => {
                setAllDeployments(result.body.list);
            });
        restApi.getRevisions().then((result) => {
            setRevisions(result.list);
        });
    }, []);

    /**
     *
     * Handle the Environments save button action
     */
    // function addEnvironments() {
    //     setUpdating(true);
    //     updateAPI({
    //         gatewayEnvironments,
    //         labels: selectedMgLabel,
    //         deploymentEnvironments: selectedDeployments,
    //     })
    //         .then(() => Alert.info('API Update Successfully'))
    //         .catch((error) => {
    //             if (error.response) {
    //                 Alert.error(error.response.body.description);
    //             } else {
    //                 Alert.error('Something went wrong while updating the environments');
    //             }
    //             console.error(error);
    //         })
    //         .finally(() => setUpdating(false));
    // }

    return (
        <>
            <Grid 
                container
                direction='row'
                alignItems='flex-start'
                spacing={1}
            >
                <Grid item>
                    <Typography variant='h4' gutterBottom>
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.Deployments'
                            defaultMessage='Deployments'
                        />
                    </Typography>
                </Grid>
                <Grid item>
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.Create.Revision.Deploy'
                                defaultMessage='Create new revision and deploy'
                            />
                        )}
                        placement='top-end'
                        aria-label='New Deployment'
                    >
                        <IconButton size='small' aria-label='delete'>
                            <HelpOutlineIcon fontSize='small' />
                        </IconButton>
                    </Tooltip>
                </Grid>

            </Grid>

            <Box ml={4}>
                {'xxx' === 'xxx' 
                    ?
                    <Grid container>

                        <Button
                            className={classes.saveButton}
                            disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || isUpdating}
                            type='submit'
                            variant='contained'
                            color='primary'

                            onClick={handleClickOpen}
                        >
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.New.Deployment'
                                defaultMessage='New Deployment'
                            />
                            {isUpdating && <CircularProgress size={20} />}
                        </Button>
                        <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title" fullWidth>
                            <DialogTitle id="form-dialog-title">Deploy</DialogTitle>
                            <DialogContent>
                                <DialogContentText>
                                    <Typography variant='h6' gutterBottom >
                                        Revision</Typography>
                                </DialogContentText>

                                <Box mb={5}>
                                    <TextField
                                        autoFocus
                                        margin="dense"
                                        id="name"
                                        label={'Revision ' + (allRevisions.length + 1)}
                                        type="email"
                                        disabled
                                        variant='outlined'
                                        helperText={(
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.Revision.Name'
                                                defaultMessage='Name of the revision'
                                            />
                                        )}
                                        fullWidth
                                    />
                                    <TextField
                                        margin="dense"
                                        id="name"
                                        label="Description"
                                        type="email"
                                        variant='outlined'
                                        helperText={(
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.Revision.Description'
                                                defaultMessage='Description of the revision'
                                            />
                                        )}
                                        fullWidth
                                        multiline
                                        rows={3}
                                        rowsMax={4}
                                    />
                                </Box>
                                <Box mt={3}>
                                    <DialogContentText>
                                        <Typography variant='h6' gutterBottom >
                                            Environments</Typography>
                                    </DialogContentText>


                                    <Grid
                                        container
                                        spacing={3}
                                    >
                                        {settings.environment.map((row) =>
                                            <Grid item xs={4}>

                                                <Card className={clsx(checked ? (classes.changeCard) : (classes.noChangeCard), classes.cardHeight)} variant="outlined">
                                                    <Box height="100%" width="100%">
                                                        <Box height="70%">
                                                            <CardContent className={classes.cardContentHeight}>
                                                                <Grid
                                                                    container
                                                                    direction='column'
                                                                    spacing={2}
                                                                >  <Grid item>
                                                                        <Typography className={classes.title} color="textSecondary" gutterBottom>
                                                                            {row.type}
                                                                        </Typography>
                                                                    </Grid>
                                                                    <Grid item>
                                                                        <Chip

                                                                            label="Revision_1"

                                                                            style={{ backgroundColor: 'lightgreen' }}


                                                                        /></Grid>  <Grid item>
                                                                        <Typography variant="h6" component="h4">
                                                                            {row.name}
                                                                        </Typography>


                                                                    </Grid></Grid>
                                                            </CardContent>
                                                        </Box>
                                                        <Box height="30%">
                                                            <CardActions className={classes.cardActionHeight}>

                                                                <Checkbox
                                                                    id={row.name.split(" ").join("")}
                                                                    checked={checked}
                                                                    onChange={handleChange}
                                                                    color="primary"
                                                                    inputProps={{ "aria-label": "secondary checkbox" }}
                                                                />

                                                            </CardActions>
                                                        </Box>
                                                    </Box>
                                                </Card>

                                            </Grid>

                                        )}
                                    </Grid></Box>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleClose} >
                                    Cancel
        </Button>
                                <Button onClick={handleClose} type='submit'
                                    variant='contained'
                                    color='primary'>
                                    Deploy
        </Button>
                            </DialogActions>
                        </Dialog>
                    </Grid>
                    : ''}
                <Box mx="auto" mt={3}>
                    <Typography variant='h6' gutterBottom >
                        <FormattedMessage
                            id='Apis.Details.Environments.Environments.APIGateways'
                            defaultMessage='API Gateways'
                        />
                    </Typography>

                    <TableContainer component={Paper}>
                        <Table >
                            <TableHead>
                                <TableRow>
                                    <TableCell align='left'>Name</TableCell>
                                    <TableCell align='left'>Type</TableCell>
                                    <TableCell align='left'>Server URL</TableCell>
                                    {api.isWebSocket() ? (
                                        <>
                                            <TableCell align='left'>Endpoints</TableCell>
                                            {/* <TableCell align='left'>WSS Endpoint</TableCell> */}
                                        </>
                                    ) : (
                                            <>
                                                <TableCell align='left'>Endpoints</TableCell>
                                                {/* <TableCell align='left'>HTTPS Endpoint</TableCell> */}
                                            </>
                                        )}
                                    {'xxx' === 'xxx' ?
                                        <TableCell align='left'>Deployed Revision</TableCell>
                                        : <TableCell align='left'>Action</TableCell>
                                    }
                                    <TableCell align='left'>Display in devportal</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {settings.environment.map((row) => (
                                    <TableRow key={row.name}>
                                        {/* <TableCell padding='checkbox'>
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
                                </TableCell> */}
                                        <TableCell component='th' scope='row'>
                                            {row.name}

                                        </TableCell>
                                        <TableCell align='left'>{row.type}</TableCell>
                                        <TableCell align='left'>{row.serverUrl}</TableCell>

                                        {api.isWebSocket() ? (
                                            <>
                                                <TableCell align='left'>{row.endpoints.ws} <div>{row.endpoints.wss}</div></TableCell>
                                                {/* <TableCell align='left'>{row.endpoints.wss}</TableCell> */}
                                            </>
                                        ) : (
                                                <>
                                                    <TableCell align='left'>{row.endpoints.http}<div>{row.endpoints.https}</div></TableCell>
                                                    {/* <TableCell align='left'>{row.endpoints.https}</TableCell> */}
                                                </>
                                            )}

                                        {'xxx' === 'xxx' ?
                                            <TableCell align='left'>
                                                <Chip

                                                    label="Revision_1"

                                                    style={{ backgroundColor: 'lightgreen' }}


                                                /> </TableCell> :
                                            <TableCell align='left'>
                                                <Button

                                                    className={classes.button}
                                                    startIcon={<RestoreIcon />}
                                                >
                                                    Deploy
</Button>
                                            </TableCell>
                                        }

                                        <TableCell align='left'>
                                            <Switch

                                                name="checkedA"

                                            />

                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    {!api.isWebSocket()
                        && (
                            <MicroGateway
                                selectedMgLabel={selectedMgLabel}
                                setSelectedMgLabel={setSelectedMgLabel}
                                api={api}
                            />
                        )}
                    {
                        allDeployments
                        && (
                            allDeployments.map((clusters) => (clusters.name.toLowerCase() === 'kubernetes' && (
                                <Kubernetes
                                    clusters={clusters}
                                    selectedDeployments={selectedDeployments}
                                    setSelectedDeployments={setSelectedDeployments}
                                    api={api}
                                />
                            )))
                        )
                    }
                </Box>
            </Box>
            {/* <Grid
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
            )} */}


            <div className={classes.root}>
                <Box component="span" mt={5} xs={12}>
                    <div>
                        <Typography variant='h5'>
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.Revisions'
                                defaultMessage='Revisions'
                            />
                        </Typography>
                        <Typography variant='caption' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Environments.Environments.Manage.Revision'
                                defaultMessage='Manage Revision of the API'
                            />
                        </Typography>
                    </div>

                    {'xxx' === 'xxx' ? (
                        <Box ml={4}>
                            <Grid container
                                direction='column'
                                alignItems='flex-start'
                                spacing={3} >
                                <Grid item>
                                    <Button
                                        className={classes.saveButton}
                                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || isUpdating}
                                        type='submit'
                                        variant="outlined" color="primary"
                                        onClick={handleClickOpen1}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Environments.Environments.Create.Revision'
                                            defaultMessage='Create new Revision'
                                        />
                                        {isUpdating && <CircularProgress size={20} />}
                                    </Button>
                                    <Dialog open={open1} onClose={handleClose1} aria-labelledby="form-dialog-title" fullWidth>
                                        <DialogTitle id="form-dialog-title">Create Revision</DialogTitle>
                                        <DialogContent>
                                            <DialogContentText>
                                                <Typography variant='h6' gutterBottom >
                                                    Revision</Typography>
                                            </DialogContentText>

                                            <Box mb={5}>
                                                <TextField
                                                    autoFocus
                                                    margin="dense"
                                                    id="name"
                                                    label={'Revision ' + (allRevisions.length + 1)}
                                                    type="email"
                                                    disabled
                                                    variant='outlined'
                                                    helperText={(
                                                        <FormattedMessage
                                                            id='Apis.Details.Environments.Environments.Create.Revision.Name'
                                                            defaultMessage='Name of the revision'
                                                        />
                                                    )}
                                                    fullWidth
                                                />
                                                <TextField
                                                    margin="dense"
                                                    id="name"
                                                    label="Description"
                                                    type="email"
                                                    variant='outlined'
                                                    helperText={(
                                                        <FormattedMessage
                                                            id='Apis.Details.Environments.Environments.Create.Revision.Description'
                                                            defaultMessage='Description of the revision'
                                                        />
                                                    )}
                                                    fullWidth
                                                    multiline
                                                    rows={3}
                                                    rowsMax={4}
                                                />
                                            </Box>



                                        </DialogContent>
                                        <DialogActions>
                                            <Button onClick={handleClose1} >
                                                Cancel
        </Button>
                                            <Button onClick={handleClose1} type='submit'
                                                variant='contained'
                                                color='primary'>
                                                Create
        </Button>
                                        </DialogActions>
                                    </Dialog>


                                </Grid>


                                <Grid item>


                                    <Grid
                                        container
                                        spacing={3}
                                    >
                                        {allRevisions.map((row) => (

                                            <Grid item xs={2}>

                                                <Card className={clsx(classes.noChangeCard, classes.cardHeight)} variant="outlined">
                                                    <Box height="100%" width="100%">
                                                        <Box height="80%">
                                                            <CardContent className={classes.cardContentHeight}>



                                                                <Typography variant="h6" component="h4">
                                                                    Revision {row.id}
                                                                </Typography>
                                                                <Divider />
                                                                <Typography className={classes.textOverlay} color="textSecondary" >
                                                                    {row.description}
                                                                </Typography>




                                                            </CardContent>
                                                        </Box>
                                                        <Box height="20%">
                                                            <CardActions >
                                                                <Grid
                                                                    container
                                                                    spacing={6}
                                                                >
                                                                    <Grid item>
                                                                        <Button

                                                                            className={classes.button}
                                                                            startIcon={<RestoreIcon />}
                                                                        >
                                                                            Restore
      </Button>

                                                                    </Grid>

                                                                    <Grid item>
                                                                        <Button

                                                                            className={classes.button}
                                                                            startIcon={<DeleteForeverIcon />}
                                                                        >
                                                                            Delete
      </Button>
                                                                    </Grid></Grid>
                                                            </CardActions>
                                                        </Box>
                                                    </Box>
                                                </Card>
                                            </Grid>
                                        ))} </Grid> </Grid> </Grid>
                        </Box>) : (

                            <Box ml={4}>
                                <Grid container
                                    direction='row'
                                    alignItems='flex-start'
                                    spacing={3} >
                                    <Grid item>
                                        <Button
                                            className={classes.saveButton}
                                            disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || isUpdating}
                                            type='submit'
                                            variant="outlined" color="primary"
                                            onClick={handleClickOpen1}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.Restore'
                                                defaultMessage='Restore'
                                            />
                                            {isUpdating && <CircularProgress size={20} />}
                                        </Button>
                                    </Grid>
                                    <Grid item>
                                        <Button
                                            className={classes.saveButton}
                                            disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || isUpdating}
                                            type='submit'
                                            variant="outlined" color="primary"
                                            onClick={handleClickOpen1}s
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.Environments.Environments.Delete'
                                                defaultMessage='Delete'
                                            />
                                            {isUpdating && <CircularProgress size={20} />}
                                        </Button>
                                    </Grid>
                                </Grid></Box>

                        )}
                </Box>
            </div>

        </>
    );
}
