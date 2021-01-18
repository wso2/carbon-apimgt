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
// import Box from '@material-ui/core/Box';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Switch from '@material-ui/core/Switch';
// import Checkbox from '@material-ui/core/Checkbox';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import InputLabel from '@material-ui/core/InputLabel';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Chip from '@material-ui/core/Chip';
// import { isRestricted } from 'AppData/AuthManager';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    mainTitle: {
        paddingTop: theme.spacing(3),
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
        // selectedMgLabel,
        // setSelectedMgLabel,
        api,
        updateAPI,
        mgLabels,
        allRevisions,
        allEnvRevision,
    } = props;
    const restApi = new API();
    // const [mgLabels, setMgLabels] = useState(null);
    // const [allRevisions, setRevisions] = useState(null);
    // const [allEnvRevision, setEnvRevision] = useState(null);
    const [selectedRevision, setRevision] = useState(null);

    const handleSelect = (event) => {
        setRevision(event.target.value);
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
                    id='Apis.Details.Environments.Environments.GatewayLabels'
                    defaultMessage='Gateway Labels'
                />
            </Typography>
            {mgLabels.length > 0 ? (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                {/* <TableCell /> */}
                                <TableCell align='left'>Label</TableCell>
                                <TableCell align='left'>Description</TableCell>
                                <TableCell align='left'>Access URL</TableCell>
                                <TableCell align='left'>Deployed Revision</TableCell>
                                <TableCell align='left'>Display in devportal</TableCell>
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
                                        {allEnvRevision && (allEnvRevision.filter(
                                            (o1) => o1.deploymentInfo.filter((o2) => o2.name === row.name),
                                        ).length) !== 0 ? (
                                                allEnvRevision && allEnvRevision.filter(
                                                    (o1) => o1.deploymentInfo.filter((o2) => o2.name === row.name),
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
                                                            Undepoly
                                                        </Button>
                                                    </div>
                                                ))) : (
                                                // eslint-disable-next-line react/jsx-indent
                                                <div>
                                                    <FormControl
                                                        className={classes.formControl}
                                                        variant='outlined'
                                                        margin='dense'
                                                        size='small'
                                                    >
                                                        <InputLabel
                                                            disabled={allRevisions}
                                                            id='demo-simple-select-label'
                                                        >
                                                            Select Revision
                                                        </InputLabel>
                                                        <Select
                                                            labelId='demo-simple-select-helper-label'
                                                            id='demo-simple-select-helper'
                                                            disabled={api.isRevision}
                                                            onChange={handleSelect}
                                                        >
                                                            {allRevisions && allRevisions.map(
                                                                (number) => (
                                                                    <MenuItem value={number.id}>
                                                                        {number.displayName}
                                                                    </MenuItem>
                                                                ),
                                                            )}
                                                        </Select>
                                                    </FormControl>
                                                    <Button
                                                        className={classes.button2}
                                                        disabled={api.isRevision || !selectedRevision}
                                                        variant='outlined'
                                                        onClick={() => deployRevision(selectedRevision, row.name)}

                                                    >
                                                        Depoly
                                                    </Button>
                                                </div>
                                            )}
                                    </TableCell>

                                    <TableCell align='left'>
                                        <Switch
                                            checked={row.showInApiConsole}
                                            disabled={api.isRevision}
                                            name='checkedA'
                                        />

                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

            )
                : (

                    <InlineMessage type='info' height={100} className={classes.emptyBox}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Environments.Gateway.labels.emptym1'
                                    defaultMessage='Gateway labels are not available.'
                                />
                                <FormattedMessage
                                    id='Apis.Details.Environments.Gateway.labels.emptym2'
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
    // selectedMgLabel: PropTypes.arrayOf(PropTypes.string).isRequired,
    // setSelectedMgLabel: PropTypes.func.isRequired,
    api: PropTypes.shape({}).isRequired,
    updateAPI: PropTypes.func.isRequired,
    mgLabels: PropTypes.shape({}).isRequired,
    allRevisions: PropTypes.shape({}).isRequired,
    allEnvRevision: PropTypes.shape({}).isRequired,
};
