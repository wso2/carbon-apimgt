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

import React from 'react';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import Checkbox from '@material-ui/core/Checkbox';
import { isRestricted } from 'AppData/AuthManager';
import cloneDeep from 'lodash.clonedeep';

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
}));

/**
 * Renders deployments List
 * @class Kubernetes
 * @param {*} props
 * @extends {React.Component}
 */
export default function Kubernetes(props) {
    const classes = useStyles();
    const {
        clusters, selectedDeployments, setSelectedDeployments, api,
    } = props;

    /**
     * Handle the Deployment Environments onChange event
     * @param {event} event onCheck event
     * @param {clusterId} clusterId seletected clsuter Id
     */
    function handleEnvironmentsSelect(event, clusterId) {
        const { checked } = event.target;
        const selectedDeploymentCopy = cloneDeep(selectedDeployments);
        if (selectedDeploymentCopy && selectedDeploymentCopy.length > 0) {
            for (let dep = 0; dep < selectedDeploymentCopy.length; dep++) {
                const deployment = selectedDeploymentCopy[dep];
                if (deployment && deployment.type === 'Kubernetes') {
                    if (deployment.clusterName && deployment.clusterName.length > 0) {
                        const clusterNameFound = [];
                        for (let cluster = 0; cluster < deployment.clusterName.length; cluster++) {
                            const clusterName = deployment.clusterName[cluster];
                            if (clusterName === clusterId) {
                                clusterNameFound.push(clusterId);
                            }
                        }
                        if (checked && !clusterNameFound.includes(clusterId)) {
                            deployment.clusterName.push(clusterId);
                        } else if (!checked && clusterNameFound.includes(clusterId)) {
                            const index = deployment.clusterName.indexOf(clusterId);
                            if (index > -1) {
                                deployment.clusterName.splice(index, 1);
                            }
                        }
                    } else if (deployment.clusterName && deployment.clusterName.length === 0) {
                        deployment.clusterName.push(clusterId);
                    }
                }
            }
        } else {
            selectedDeploymentCopy.push({
                type: 'Kubernetes',
                clusterName: [clusterId],
            });
        }
        setSelectedDeployments(selectedDeploymentCopy);
    }

    /**
     *
     * Handle the Deployment Environments onCheck
     * @param {*} clusterId selected cluster Id
     * @returns {include} selected cluster alredy include in the response
     */
    function handleOnChecked(clusterId) {
        let include = false;
        if (selectedDeployments) {
            const deploymentTypes = selectedDeployments.filter((deployment) => deployment.type === 'Kubernetes');
            if (deploymentTypes && deploymentTypes.length > 0) {
                const selectedClusters = deploymentTypes[0].clusterName.filter((cluster) => cluster === clusterId);
                include = selectedClusters && selectedClusters.length > 0;
            }
        }
        return include;
    }
    return (
        <>
            <Typography variant='h4' gutterBottom align='left' className={classes.mainTitle}>
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.Kubernetes'
                    defaultMessage='Kubernetes'
                />
            </Typography>

            <Paper className={classes.gatewayPaper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell />
                            <TableCell align='left'>Name</TableCell>
                            <TableCell align='left'>Namespace</TableCell>
                            <TableCell align='left'>Master URL</TableCell>
                            <TableCell align='left'>Ingress URL</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {clusters.clusters.map((row) => (
                            <TableRow key={row.clusterId}>
                                <TableCell padding='checkbox'>
                                    <Checkbox
                                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                        checked={handleOnChecked(row.clusterId)}
                                        onChange={(e) => handleEnvironmentsSelect(e, row.clusterId)}
                                        color='primary'
                                    />
                                </TableCell>
                                <TableCell component='th' scope='row'>
                                    {row.clusterName}
                                </TableCell>
                                <TableCell align='left'>{row.namespace}</TableCell>
                                <TableCell align='left'>{row.masterURL}</TableCell>
                                <TableCell align='left'>{row.ingressURL}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Paper>
        </>
    );
}
