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

import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import Paper from '@material-ui/core/Paper';
import { isRestricted } from 'AppData/AuthManager';
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
}));

/**
 * Renders Microgateway labels
 * @class MicroGateway
 * @param {*} props
 * @extends {React.Component}
 */
export default function MicroGateway(props) {
    const classes = useStyles();
    const { selectedMgLabel, setSelectedMgLabel, api } = props;
    const restApi = new API();
    const [mgLabels, setMgLabels] = useState([]);
    useEffect(() => {
        restApi.microgatewayLabelsGet()
            .then((result) => {
                setMgLabels(result.body.list);
            });
    }, []);

    return (
        <>
            <Typography variant='h4' align='left' className={classes.mainTitle}>
                <FormattedMessage
                    id='Apis.Details.Environments.Environments.Microgateways'
                    defaultMessage='Microgateways'
                />
            </Typography>
            {mgLabels.length > 0 ? (
                <Paper className={classes.gatewayPaper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell />
                                <TableCell align='left'>Label</TableCell>
                                <TableCell align='left'>Description</TableCell>
                                <TableCell align='left'>Access URL</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {mgLabels.map((row) => (
                                <TableRow key={row.name}>
                                    <TableCell padding='checkbox'>
                                        <Checkbox
                                            disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                            checked={selectedMgLabel.includes(row.name)}
                                            onChange={
                                                (event) => {
                                                    const { checked, name } = event.target;
                                                    if (checked) {
                                                        setSelectedMgLabel([...selectedMgLabel, name]);
                                                    } else {
                                                        setSelectedMgLabel(
                                                            selectedMgLabel.filter((env) => env !== name),
                                                        );
                                                    }
                                                }
                                            }
                                            name={row.name}
                                            color='primary'
                                        />
                                    </TableCell>
                                    <TableCell component='th' scope='row' align='left'>
                                        {row.name}
                                    </TableCell>
                                    <TableCell align='left'>{row.description}</TableCell>
                                    <TableCell align='left'>{row.access_urls.join(', ')}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
            )
                : (
                    <InlineMessage type='info' height={100} className={classes.emptyBox}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Environments.microgateway.labels.emptym1'
                                    defaultMessage='Microgateway labels are not available.'
                                />
                                <FormattedMessage
                                    id='Apis.Details.Environments.microgateway.labels.emptym2'
                                    defaultMessage=' You can request the administrator to add labels.'
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                )}
        </>
    );
}
MicroGateway.defaultProps = {
    api: {},
};
MicroGateway.propTypes = {
    selectedMgLabel: PropTypes.arrayOf(PropTypes.string).isRequired,
    setSelectedMgLabel: PropTypes.func.isRequired,
    api: PropTypes.shape({}),
};
