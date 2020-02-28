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
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';


const useStyles = makeStyles((theme) => ({
    paper: {
        marginTop: 20,
        padding: theme.spacing(2),
        height: '100%',
    },
    table: {
        width: '100%',
        background: theme.palette.background.paper,
    },
    leftCell: {
        width: '150px',
        paddingRight: theme.spacing(10),
    },
    key: {
        color: theme.palette.primary.main,
        paddingRight: '10px',
    },
    value: {
        color: theme.palette.grey[500],
        paddingLeft: '10px',
    },
    add: {
        color: 'green',
        fontSize: '15px',
        fontWeight: 'bold',
    },
    remove: {
        color: 'red',
        fontSize: '15px',
        fontWeight: 'bold',
    },
}));

function MessageTrace(props) {
    const {
        name, eventAddedArray, componentName, diffViewActive,
    } = props;
    const classes = useStyles();
    const [transportHeaders, setTransportHeaders] = useState(null);
    const [addedTransportHeaders, setAddedTransportHeaders] = useState(null);
    const [removedTransportHeaders, setRemovedTransportHeaders] = useState(null);
    const [synMsgContext, setSynMsgContext] = useState(null);
    const [addedSynMsgContext, setAddedSynMsgContext] = useState(null);
    const [removedSynMsgContext, setRemovedSynMsgContext] = useState(null);
    const [axis2MsgContext, setAxis2MsgContext] = useState(null);
    const [addedAxis2MsgContext, setAddedAxis2MsgContext] = useState(null);
    const [removedAxis2MsgContext, setRemovedAxis2MsgContext] = useState(null);
    const [payload, setPayload] = useState(null);

    function setProperties() {
        eventAddedArray.forEach((event) => {
            if (event.componentName === componentName) {
                setTransportHeaders(event.transportHeaders);
                setAddedTransportHeaders(event.addedTransportHeaders);
                setRemovedTransportHeaders(event.removedTransportHeaders);
                setSynMsgContext(event.synapseCtxProperties);
                setAddedSynMsgContext(event.addedSynapseCtxProperties);
                setRemovedSynMsgContext(event.removedSynapseCtxProperties);
                setAxis2MsgContext(event.axis2MessageContext);
                setAddedAxis2MsgContext(event.addedAxis2CtxProperties);
                setRemovedAxis2MsgContext(event.removedAxis2CtxProperties);
                setPayload(event.payload);
            }
        });
    }

    useEffect(() => {
        setProperties();
        console.log(eventAddedArray);
    }, [eventAddedArray, componentName]);

    return (
        <>
            <div>
                <Paper className={classes.paper}>
                    <Table className={classes.table}>
                        <TableHead>
                            <TableRow>
                                <TableCell className={classes.leftCell}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MessageTrace.properties'
                                            defaultMessage='Properties'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='body1'>
                                        {name}
                                        {' '}
                                        Mediator
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow key='transport headers'>
                                <TableCell className={classes.leftCell}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MessageTrace.transport.headers'
                                            defaultMessage='Transport Headers'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    {diffViewActive && (
                                        <Typography variant='body1'>
                                            {addedTransportHeaders === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(addedTransportHeaders).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.add}> ( + ) </span>
                                                                    {key}
                                                                </span>
                                                        :
                                                                <span className={classes.value}>
                                                                    {addedTransportHeaders[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                            {removedTransportHeaders === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(removedTransportHeaders).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.remove}> ( - ) </span>
                                                                    {key}
                                                                </span>
                                                                   :
                                                                <span className={classes.value}>
                                                                    {removedTransportHeaders[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                        </Typography>
                                    )}
                                    {!diffViewActive && (
                                        <Typography variant='body1'>
                                            {transportHeaders === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(transportHeaders).map((key) => (
                                                            <>
                                                                <span className={classes.key}>{key}</span>
                                                        :
                                                                <span className={classes.value}>
                                                                    {transportHeaders[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                        </Typography>
                                    )}
                                </TableCell>
                            </TableRow>
                            <TableRow key='synapse message context'>
                                <TableCell className={classes.leftCell}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MessageTrace.synapse.message.context'
                                            defaultMessage='Synapse Message Context'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    {diffViewActive && (
                                        <Typography variant='body1'>
                                            {addedSynMsgContext === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(addedSynMsgContext).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.add}> ( + ) </span>
                                                                    {key}
                                                                </span>
                                                                       :
                                                                <span className={classes.value}>
                                                                    {addedSynMsgContext[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                            {removedSynMsgContext === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(removedSynMsgContext).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.remove}> ( - ) </span>
                                                                    {key}
                                                                </span>
                                                                   :
                                                                <span className={classes.value}>
                                                                    {removedSynMsgContext[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                        </Typography>
                                    )}
                                    {!diffViewActive && (
                                        <Typography variant='body1'>
                                            {synMsgContext === null ? <span className={classes.value}> null </span> : (
                                                <span>
                                                    {Object.keys(synMsgContext).map((key) => (
                                                        <>
                                                            <span className={classes.key}>{key}</span>
                                                        :
                                                            <span className={classes.value}>{synMsgContext[key]}</span>
                                                            <br />
                                                        </>
                                                    ))}
                                                </span>
                                            )}
                                        </Typography>
                                    )}
                                </TableCell>
                            </TableRow>
                            <TableRow key='axis2 message context'>
                                <TableCell className={classes.leftCell}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MessageTrace.axis2.message.context'
                                            defaultMessage='AXIS2 Message Context'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    {diffViewActive && (
                                        <Typography variant='body1'>
                                            {addedAxis2MsgContext === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(addedAxis2MsgContext).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.add}> ( + ) </span>
                                                                    {key}
                                                                </span>
                                                                       :
                                                                <span className={classes.value}>
                                                                    {addedAxis2MsgContext[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                            {removedAxis2MsgContext === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(removedAxis2MsgContext).map((key) => (
                                                            <>
                                                                <span className={classes.key}>
                                                                    <span className={classes.remove}> ( - ) </span>
                                                                    {key}
                                                                </span>
                                                                   :
                                                                <span className={classes.value}>
                                                                    {removedAxis2MsgContext[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                        </Typography>
                                    )}
                                    {!diffViewActive && (
                                        <Typography variant='body1'>
                                            {axis2MsgContext === null
                                                ? <span className={classes.value}> null </span> : (
                                                    <span>
                                                        {Object.keys(axis2MsgContext).map((key) => (
                                                            <>
                                                                <span className={classes.key}>{key}</span>
                                                            :
                                                                <span className={classes.value}>
                                                                    {axis2MsgContext[key]}
                                                                </span>
                                                                <br />
                                                            </>
                                                        ))}
                                                    </span>
                                                )}
                                        </Typography>
                                    )}
                                </TableCell>
                            </TableRow>
                            <TableRow key='payload'>
                                <TableCell className={classes.leftCell}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.MediationPolicies.MessageTrace.payload'
                                            defaultMessage='Payload'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='body1'>
                                        {payload === null ? <span className={classes.value}> null </span> : (
                                            <span className={classes.value}>
                                                {payload}
                                            </span>
                                        )}
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </Paper>
            </div>
        </>
    );
}

MessageTrace.propTypes = {
    name: PropTypes.string.isRequired,
    componentName: PropTypes.string.isRequired,
    diffViewActive: PropTypes.bool.isRequired,
};

export default MessageTrace;
