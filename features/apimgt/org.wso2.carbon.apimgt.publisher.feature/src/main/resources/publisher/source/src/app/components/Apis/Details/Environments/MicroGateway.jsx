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

import React, { useState } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Alert from 'AppComponents/Shared/Alert';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Switch from '@material-ui/core/Switch';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Chip from '@material-ui/core/Chip';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    mainTitle: {
        paddingTop: theme.spacing(3),
        marginBottom: theme.spacing(2),
    },
    gatewayPaper: {
        marginTop: theme.spacing(2),
    },
    content: {
        marginTop: theme.spacing(2),
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
    },
    progressWrapper: {
        padding: theme.spacing(),
        textAlign: 'center',
    },
    formControl: {
        margin: theme.spacing(1),
        minWidth: 130,
    },
    button1: {
        color: '#1B3A57',
        marginLeft: 7,
    },
    button2: {
        color: '#1B3A57',
        marginLeft: 7,
        marginTop: 10,
    },
}));

/**
 * Renders Microgateway labels
 * @class MicroGateway
 * @param {*} props
 * @extends {React.Component}
 */
export default function MicroGateway(props) {
    const classes = useStyles();
    const {
        api,
        updateAPI,
        mgLabels,
        allRevisions,
        allEnvRevision,
    } = props;
    const restApi = new API();
    const [selectedRevision, setRevision] = useState(null);

    const handleSelect = (event) => {
        setRevision(event.target.value);
    };
    const handleChange = () => {
        // display in devportal check
    };

    /**
      * Handles undeploy a revision
      * @memberof Revisions
      */
    function undeployRevision(revisionId, envName) {
        const body = [{
            name: envName,
            displayOnDevportal: false,
        }];
        restApi.undeployRevision(api.id, revisionId, body)
            .then(() => {
                Alert.info('Undeploy revision Successfully');
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while undeploy the revision');
                }
                console.error(error);
            }).finally(() => {
                updateAPI();
            });
    }

    /**
      * Handles deploy a revision
      * @memberof Revisions
      */
    function deployRevision(revisionId, envName) {
        const body = [{
            name: envName,
            displayOnDevportal: true,
        }];
        restApi.deployRevision(api.id, revisionId, body)
            .then(() => {
                Alert.info('Deploy revision Successfully');
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while deploy the revision');
                }
                console.error(error);
            }).finally(() => {
                updateAPI();
            });
    }
    if (!mgLabels) {
        return (
            <div className={classes.progressWrapper}>
                <CircularProgress size={20} />
            </div>
        );
    }
    return (
        <>
            <Typography variant='h6' align='left' className={classes.mainTitle}>
                <FormattedMessage
                    id='Apis.Details.Environments.MicroGateway.GatewayLabels'
                    defaultMessage='Gateway Labels'
                />
            </Typography>
            {mgLabels.length > 0 ? (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell align='left'>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.MicroGateway.label'
                                        defaultMessage='Label'
                                    />
                                </TableCell>
                                <TableCell align='left'>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.MicroGateway.description'
                                        defaultMessage='Description'
                                    />
                                </TableCell>
                                <TableCell align='left'>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.MicroGateway.access.url'
                                        defaultMessage='Access URL'
                                    />
                                </TableCell>
                                <TableCell align='left'>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.MicroGateway.deployed.revision'
                                        defaultMessage='Deployed Revision'
                                    />
                                </TableCell>
                                <TableCell align='left'>
                                    <FormattedMessage
                                        id='Apis.Details.Environments.MicroGateway.devportal.display'
                                        defaultMessage='Display in devportal'
                                    />
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {mgLabels.map((row) => (
                                <TableRow key={row.name}>
                                    <TableCell component='th' scope='row' align='left'>
                                        {row.name}
                                    </TableCell>
                                    <TableCell align='left'>{row.description}</TableCell>
                                    <TableCell align='left'>
                                        {row.accessUrls.map((host) => (
                                            <div>{host}</div>
                                        ))}

                                    </TableCell>
                                    <TableCell align='left'>
                                        {allEnvRevision && allEnvRevision.filter(
                                            (o1) => {
                                                if (o1.deploymentInfo.filter(
                                                    (o2) => o2.name === row.name,
                                                ).length > 0) {
                                                    return o1;
                                                }
                                                return null;
                                            },
                                        ).length !== 0 ? (
                                                allEnvRevision && allEnvRevision.filter(
                                                    (o1) => {
                                                        if (o1.deploymentInfo.filter(
                                                            (o2) => o2.name === row.name,
                                                        ).length > 0) {
                                                            return o1;
                                                        }
                                                        return null;
                                                    },
                                                ).map((o3) => (
                                                    <div>
                                                        <Chip
                                                            label={o3.displayName}
                                                            style={{ backgroundColor: '#15B8CF' }}
                                                        />
                                                        <Button

                                                            className={classes.button1}
                                                            variant='outlined'
                                                            disabled={api.isRevision}
                                                            onClick={() => undeployRevision(o3.id, row.name)}
                                                            size='small'
                                                        >
                                                            <FormattedMessage
                                                                id='Apis.Details.Environments.MicroGateway.undeploy.btn'
                                                                defaultMessage='Undeploy'
                                                            />
                                                        </Button>
                                                    </div>
                                                ))) : (
                                                // eslint-disable-next-line react/jsx-indent
                                                <div>
                                                    <TextField
                                                        id='revision-selector'
                                                        select
                                                        label={(
                                                            <FormattedMessage
                                                                id='Apis.Details.Environments.MicroGateway.select.table'
                                                                defaultMessage='Select Revision'
                                                            />
                                                        )}
                                                        SelectProps={{
                                                            MenuProps: {
                                                                anchorOrigin: {
                                                                    vertical: 'bottom',
                                                                    horizontal: 'left',
                                                                },
                                                                getContentAnchorEl: null,
                                                            },
                                                        }}
                                                        name='selectRevision'
                                                        onChange={handleSelect}
                                                        margin='dense'
                                                        variant='outlined'
                                                        style={{ width: '50%' }}
                                                        disabled={api.isRevision
                                                            || !allRevisions || allRevisions.length === 0}
                                                    >
                                                        {allRevisions && allRevisions.length !== 0 && allRevisions.map(
                                                            (number) => (
                                                                <MenuItem value={number.id}>
                                                                    {number.displayName}
                                                                </MenuItem>
                                                            ),
                                                        )}
                                                    </TextField>
                                                    <Button
                                                        className={classes.button2}
                                                        disabled={api.isRevision || !selectedRevision}
                                                        variant='outlined'
                                                        onClick={() => deployRevision(selectedRevision, row.name)}

                                                    >
                                                        <FormattedMessage
                                                            id='Apis.Details.Environments.MicroGateway.deploy.button'
                                                            defaultMessage='Deploy'
                                                        />
                                                    </Button>
                                                </div>
                                            )}
                                    </TableCell>
                                    <TableCell align='left'>
                                        <Switch
                                            // checked={row.showInApiConsole}
                                            checked={false}
                                            onChange={handleChange}
                                            disabled={api.isRevision}
                                            name='checkedA'
                                        />
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            ) : (
                <InlineMessage type='info' height={100} className={classes.emptyBox}>
                    <div className={classes.contentWrapper}>
                        <Typography component='p' className={classes.content}>
                            <FormattedMessage
                                id='Apis.Details.Environments.MicroGateway.labels.emptym1'
                                defaultMessage='Gateway labels are not available.'
                            />
                            <FormattedMessage
                                id='Apis.Details.Environments.MicroGateway.labels.emptym2'
                                defaultMessage=' You can request the administrator to add labels.'
                            />
                        </Typography>
                    </div>
                </InlineMessage>
            )}
        </>
    );
}

MicroGateway.propTypes = {
    api: PropTypes.shape({}).isRequired,
    updateAPI: PropTypes.func.isRequired,
    mgLabels: PropTypes.shape({ length: PropTypes.func, map: PropTypes.func }).isRequired,
    allRevisions: PropTypes.shape({ length: PropTypes.func, map: PropTypes.func }).isRequired,
    allEnvRevision: PropTypes.shape({ filter: PropTypes.func }).isRequired,
};
