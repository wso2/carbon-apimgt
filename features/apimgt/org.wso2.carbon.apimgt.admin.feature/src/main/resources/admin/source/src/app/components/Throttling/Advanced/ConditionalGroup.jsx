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
import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { useIntl, FormattedMessage } from 'react-intl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import AddEditExecution from 'AppComponents/Throttling/Advanced/AddEditExecution';
import AddEditConditionPolicy from 'AppComponents/Throttling/Advanced/AddEditConditionPolicy';
import AddEditConditionPolicyIp from 'AppComponents/Throttling/Advanced/AddEditConditionPolicyIP';
import CON_CONSTS from 'AppComponents/Throttling/Advanced/CON_CONSTS';
import DeleteCondition from 'AppComponents/Throttling/Advanced/DeleteCondition';
import DeleteConditionGroup from 'AppComponents/Throttling/Advanced/DeleteConditionGroup';
import Alert from '@material-ui/lab/Alert';
import AlertTitle from '@material-ui/lab/AlertTitle';

/**
 * Create UUID
 * @returns {string} random uuid string.
 */
function getUUID() {
    return Math.random().toString(36).substring(2) + Date.now().toString(36);
}

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
        marginBottom: 20,
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        flexBasis: '33.33%',
        flexShrink: 0,
        flex: 1,
        alignItems: 'center',
    },
    secondaryHeading: {
        fontSize: theme.typography.pxToRem(15),
        color: theme.palette.text.secondary,
        display: 'flex',
        alignItems: 'center',
    },
    table: {
        marginBottom: 40,
        background: '#efefef',
        '& th': {
            background: '#ccc',
        },
    },
    expandContentRoot: {
        flexDirection: 'column',
    },
    subsubTitle: {
        fontSize: '0.81rem',
    },
    alert: {
        flex: 1,
    },
    hr: {
        border: 'solid 1px #efefef',
        width: '100%',
    },
    descriptionBox: {
        marginLeft: theme.spacing(1),
    },
}));

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 * @param {JSON} props Provides props from parent
 */
function ConditionalGroup(props) {
    const intl = useIntl();
    const classes = useStyles();
    const [expanded, setExpanded] = React.useState(false);
    const {
        group, updateGroup, hasErrors, index, deleteGroup,
    } = props;

    const handleChange = (panel) => (event, isExpanded) => {
        setExpanded(isExpanded ? panel : false);
    };
    const filterRows = (type) => {
        const filteredConditions = [];
        for (let i = 0; i < group.conditions.length; i++) {
            if (group.conditions[i].type === type) {
                group.conditions[i].id = getUUID();
                filteredConditions.push(group.conditions[i]);
            }
        }
        return filteredConditions;
        // return group.conditions.filter((condition) => {
        //     if (condition.type === type) {
        //         condition.id = getUUID();
        //     }
        //     return condition.type === type;
        // });
    };
    const onChange = (e) => {
        group[e.target.name] = e.target.value;
        updateGroup();
    };

    const deleteThisGroup = () => {
        deleteGroup(index);
        setExpanded(false);
    };

    const rows = [
        {
            name: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.ip',
                defaultMessage: 'IP Condition Policy',
            }),
            description: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.ip.help',
                defaultMessage: 'This configuration is used to throttle by IP address.',
            }),
            items: filterRows(CON_CONSTS.IPCONDITION),
            type: CON_CONSTS.IPCONDITION,
            labelPrefix: '',
        },
        {
            name: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.header',
                defaultMessage: 'Header Condition Policy',
            }),
            description: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.header.help',
                defaultMessage: 'This configuration is used to throttle based on Headers.',
            }),
            items: filterRows(CON_CONSTS.HEADERCONDITION),
            type: CON_CONSTS.HEADERCONDITION,
            labelPrefix: 'Header ',
        },
        {
            name: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.query.param',
                defaultMessage: 'Query Param Condition Policy',
            }),
            description: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.query.param.help',
                defaultMessage: 'This configuration is used to throttle based on query parameters.',
            }),
            items: filterRows(CON_CONSTS.QUERYPARAMETERCONDITION),
            type: CON_CONSTS.QUERYPARAMETERCONDITION,
            labelPrefix: 'Param ',
        },
        {
            name: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.jwt',
                defaultMessage: 'JWT Condition Policy',
            }),
            description: intl.formatMessage({
                id: 'Throttling.Advanced.ConditionalGroup.jwt.help',
                defaultMessage: 'This configuration is used to define JWT claims conditions',
            }),
            items: filterRows(CON_CONSTS.JWTCLAIMSCONDITION),
            type: CON_CONSTS.JWTCLAIMSCONDITION,
            labelPrefix: 'Claim ',
        },
    ];
    const getNewItem = (type) => {
        if (type === CON_CONSTS.IPCONDITION) {
            return (
                {
                    type: CON_CONSTS.IPCONDITION,
                    invertCondition: false,
                    headerCondition: null,
                    ipCondition: {
                        ipConditionType: 'IPSPECIFIC',
                        specificIP: '',
                        startingIP: null,
                        endingIP: null,
                    },
                    jwtClaimsCondition: null,
                    queryParameterCondition: null,
                }
            );
        } else if (type === CON_CONSTS.HEADERCONDITION) {
            return ({
                type: CON_CONSTS.HEADERCONDITION,
                invertCondition: false,
                headerCondition: {
                    headerName: '',
                    headerValue: '',
                },
                ipCondition: null,
                jwtClaimsCondition: null,
                queryParameterCondition: null,
            });
        } else if (type === CON_CONSTS.QUERYPARAMETERCONDITION) {
            return ({
                type: CON_CONSTS.QUERYPARAMETERCONDITION,
                invertCondition: false,
                headerCondition: null,
                ipCondition: null,
                jwtClaimsCondition: null,
                queryParameterCondition: {
                    parameterName: '',
                    parameterValue: '',
                },
            });
        } else if (type === CON_CONSTS.JWTCLAIMSCONDITION) {
            return ({
                type: CON_CONSTS.JWTCLAIMSCONDITION,
                invertCondition: false,
                headerCondition: null,
                ipCondition: null,
                jwtClaimsCondition: {
                    claimUrl: '',
                    attribute: '',
                },
                queryParameterCondition: null,
            });
        } else {
            return ({});
        }
    };
    const addItem = (rowRef, item) => {
        const { name, value } = item;
        const { type: rowType } = rowRef;
        const newItem = getNewItem(rowType);
        if (rowType === CON_CONSTS.HEADERCONDITION) {
            newItem.headerCondition.headerName = name;
            newItem.headerCondition.headerValue = value;
        } else if (rowType === CON_CONSTS.QUERYPARAMETERCONDITION) {
            newItem.queryParameterCondition.parameterName = name;
            newItem.queryParameterCondition.parameterValue = value;
        } else if (rowType === CON_CONSTS.JWTCLAIMSCONDITION) {
            newItem.jwtClaimsCondition.claimUrl = name;
            newItem.jwtClaimsCondition.attribute = value;
        }
        group.conditions.push(newItem);
        updateGroup();
    };
    const addItemIP = (item) => {
        const {
            ipConditionType, specificIP, startingIP, endingIP,
        } = item;
        const newItem = getNewItem(CON_CONSTS.IPCONDITION);
        if (ipConditionType === CON_CONSTS.IPCONDITION_IPRANGE) {
            newItem.ipCondition.specificIP = null;
            newItem.ipCondition.startingIP = startingIP;
            newItem.ipCondition.endingIP = endingIP;
            newItem.ipCondition.ipConditionType = ipConditionType;
        } else {
            newItem.ipCondition.specificIP = specificIP;
            newItem.ipCondition.startingIP = null;
            newItem.ipCondition.endingIP = null;
            newItem.ipCondition.ipConditionType = ipConditionType;
        }
        group.conditions.push(newItem);
        updateGroup();
    };
    const deleteItem = (item) => {
        for (let i = 0; i < group.conditions.length; i++) {
            if (group.conditions[i].id === item.id) {
                group.conditions.splice(i, 1);
            }
        }
        updateGroup();
    };
    const updateItem = (rowRef, item, originalItem) => {
        const { type: rowType } = rowRef;
        const { name, value } = item;
        for (let i = 0; i < group.conditions.length; i++) {
            if (group.conditions[i].id === originalItem.id) {
                if (rowType === CON_CONSTS.HEADERCONDITION) {
                    group.conditions[i].headerCondition.headerName = name;
                    group.conditions[i].headerCondition.headerValue = value;
                } else if (rowType === CON_CONSTS.QUERYPARAMETERCONDITION) {
                    group.conditions[i].queryParameterCondition.parameterName = name;
                    group.conditions[i].queryParameterCondition.parameterValue = value;
                } else if (rowType === CON_CONSTS.JWTCLAIMSCONDITION) {
                    group.conditions[i].jwtClaimsCondition.claimUrl = name;
                    group.conditions[i].jwtClaimsCondition.attribute = value;
                }
            }
        }
        updateGroup();
    };
    const updateItemIP = (item, originalItem) => {
        const {
            ipConditionType, specificIP, startingIP, endingIP,
        } = item;
        for (let i = 0; i < group.conditions.length; i++) {
            if (group.conditions[i].id === originalItem.id) {
                group.conditions[i].ipCondition.ipConditionType = ipConditionType;
                group.conditions[i].ipCondition.specificIP = specificIP;
                group.conditions[i].ipCondition.startingIP = startingIP;
                group.conditions[i].ipCondition.endingIP = endingIP;
            }
        }
        updateGroup();
    };
    const setInvertCondition = (e, rowType) => {
        const { checked } = e.target;
        for (let i = 0; i < group.conditions.length; i++) {
            if (group.conditions[i].type === rowType) {
                group.conditions[i].invertCondition = checked;
            }
        }
        updateGroup();
    };
    const isInvertConditionChecked = (row) => {
        let checked = false;
        const { items } = row;
        if (items && items.length > 0) {
            checked = items[0].invertCondition;
        }
        return checked;
    };
    return (
        <div className={classes.root}>
            <ExpansionPanel expanded={expanded === 'panel1'} onChange={handleChange('panel1')}>
                <ExpansionPanelSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls='panel1bh-content'
                    id='panel1bh-header'
                >
                    <div className={classes.heading} variant='body2'>
                        {!expanded && (
                            <Typography variant='caption'>
                                {group.description}
                            </Typography>
                        )}
                    </div>
                    {!expanded && (
                        <Typography className={classes.secondaryHeading}>
                            Expand to edit
                        </Typography>
                    )}
                    {expanded && (
                        <Typography className={classes.secondaryHeading}>
                            Hide group
                        </Typography>
                    )}
                </ExpansionPanelSummary>
                <ExpansionPanelDetails classes={{ root: classes.expandContentRoot }}>

                    <Box marginBottom={2}>
                        <Alert severity='warning'>
                            <AlertTitle>Warning</AlertTitle>
                            <FormattedMessage
                                id='Throttling.Advanced.ConditionalGroup.alert'
                                defaultMessage='Publishing Query Params, Header Data and JWT token isn&apos; t
                                configured. If a policy configured with any of these conditions, it won&apos; t be
                                applied.'
                            />
                        </Alert>
                    </Box>

                    <Box flex='1'>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Advanced.ConditionalGroup.condition.policies'
                                defaultMessage='Condition Policies'
                            />

                        </Typography>
                    </Box>
                    {rows.map((row) => (
                        <>
                            <hr className={classes.hr} />
                            <Box component='div' marginLeft={1} display='flex' alignItems='center'>
                                <Box flex={1}>
                                    <Typography
                                        color='inherit'
                                        variant='subtitle2'
                                        component='div'
                                        className={classes.subsubTitle}
                                    >
                                        {row.name}
                                    </Typography>
                                    <Typography variant='caption'>
                                        {row.description}
                                    </Typography>
                                </Box>
                                <Box component='span' m={1}>
                                    {row.items.length > 0 && (
                                        <FormControlLabel
                                            control={(
                                                <Checkbox
                                                    checked={isInvertConditionChecked(row)}
                                                    onChange={(e) => setInvertCondition(e, row.type)}
                                                    name='invertCondition'
                                                />
                                            )}
                                            label={intl.formatMessage({
                                                id: 'Throttling.Advanced.ConditionalGroup.invert.condition',
                                                defaultMessage: 'Invert Condition',
                                            })}
                                        />
                                    )}
                                </Box>
                                <Box component='span' m={1}>
                                    {row.type === CON_CONSTS.IPCONDITION ? (
                                        <AddEditConditionPolicyIp
                                            row={row}
                                            callBack={addItemIP}
                                        />
                                    ) : (
                                        <AddEditConditionPolicy
                                            row={row}
                                            callBack={addItem}
                                        />
                                    )}

                                </Box>
                            </Box>

                            {row.items.length > 0 && (
                                <Box component='div' marginLeft={1}>
                                    <Table className={classes.table} size='small' aria-label='a dense table'>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>
                                                    {row.type === CON_CONSTS.IPCONDITION ? (
                                                        <FormattedMessage
                                                            id='Throttling.Advanced.ConditionalGroup.ip.header.name'
                                                            defaultMessage='IP Condition Type'
                                                        />
                                                    ) : (
                                                        <>
                                                            {row.labelPrefix}
                                                            {' '}
                                                            <FormattedMessage
                                                                id='Throttling.Advanced.ConditionalGroup.header.name'
                                                                defaultMessage='Name'
                                                            />
                                                        </>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    {row.type === CON_CONSTS.IPCONDITION ? (
                                                        <FormattedMessage
                                                            id='Throttling.Advanced.ConditionalGroup.ip.header.value'
                                                            defaultMessage='IP Address'
                                                        />
                                                    ) : (
                                                        <>
                                                            {row.labelPrefix}
                                                            {' '}
                                                            <FormattedMessage
                                                                id='Throttling.Advanced.ConditionalGroup.header.value'
                                                                defaultMessage='Value'
                                                            />
                                                        </>
                                                    )}
                                                </TableCell>
                                                <TableCell />
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {row.items.map((item) => (
                                                <TableRow key={item.headerName}>
                                                    <TableCell component='td' scope='row'>
                                                        {item.type === CON_CONSTS.IPCONDITION
                                                        && item.ipCondition.ipConditionType
                                                        === CON_CONSTS.IPCONDITION_IPRANGE && (
                                                            <FormattedMessage
                                                                id='Throttling.Advanced.ConditionalGroup.ip.iprange'
                                                                defaultMessage='IP Range'
                                                            />
                                                        ) }
                                                        {item.type === CON_CONSTS.IPCONDITION
                                                        && item.ipCondition.ipConditionType
                                                        === CON_CONSTS.IPCONDITION_IPSPECIFIC && (
                                                            <FormattedMessage
                                                                id='Throttling.Advanced.ConditionalGroup.ip.specific'
                                                                defaultMessage='Specific IP'
                                                            />
                                                        ) }
                                                        {item.type === CON_CONSTS.HEADERCONDITION
                                                        && item.headerCondition.headerName }
                                                        {item.type === CON_CONSTS.QUERYPARAMETERCONDITION
                                                        && item.queryParameterCondition.parameterName }
                                                        {item.type === CON_CONSTS.JWTCLAIMSCONDITION
                                                        && item.jwtClaimsCondition.claimUrl }
                                                    </TableCell>
                                                    <TableCell component='td' scope='row'>
                                                        {item.type === CON_CONSTS.IPCONDITION
                                                        && item.ipCondition.ipConditionType
                                                        === CON_CONSTS.IPCONDITION_IPRANGE && (
                                                            <>
                                                                <strong>
                                                                    <FormattedMessage
                                                                        id='Throttling.Advanced.ConditionalGroup.from'
                                                                        defaultMessage='From:'
                                                                    />
                                                                </strong>
                                                                {item.ipCondition.startingIP}
                                                                {' '}
                                                                <strong>
                                                                    <FormattedMessage
                                                                        id='Throttling.Advanced.ConditionalGroup.to'
                                                                        defaultMessage='To:'
                                                                    />
                                                                </strong>
                                                                {item.ipCondition.endingIP}
                                                            </>
                                                        ) }
                                                        {item.type === CON_CONSTS.IPCONDITION
                                                        && item.ipCondition.ipConditionType
                                                        === CON_CONSTS.IPCONDITION_IPSPECIFIC
                                                        && (
                                                            item.ipCondition.specificIP
                                                        ) }
                                                        {item.type === CON_CONSTS.HEADERCONDITION
                                                        && item.headerCondition.headerValue }
                                                        {item.type === CON_CONSTS.QUERYPARAMETERCONDITION
                                                        && item.queryParameterCondition.parameterValue }
                                                        {item.type === CON_CONSTS.JWTCLAIMSCONDITION
                                                        && item.jwtClaimsCondition.attribute }
                                                    </TableCell>
                                                    <TableCell width={100} className={classes.actionColumn}>
                                                        <Box display='flex'>
                                                            {row.type === CON_CONSTS.IPCONDITION ? (
                                                                <AddEditConditionPolicyIp
                                                                    row={row}
                                                                    item={item}
                                                                    callBack={updateItemIP}
                                                                />
                                                            ) : (
                                                                <AddEditConditionPolicy
                                                                    row={row}
                                                                    item={item}
                                                                    callBack={updateItem}
                                                                />
                                                            )}
                                                            <DeleteCondition
                                                                item={item}
                                                                row={row}
                                                                callBack={deleteItem}
                                                            />
                                                        </Box>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </Box>
                            )}
                        </>
                    ))}
                    <hr className={classes.hr} />
                    <AddEditExecution
                        updateGroup={updateGroup}
                        limit={group.limit}
                        hasErrors={hasErrors}
                        title={(
                            <FormattedMessage
                                id='Throttling.Advanced.ConditionalGroup.execution.policy'
                                defaultMessage='Execution Policy'
                            />
                        )}
                    />
                    <TextField
                        margin='dense'
                        name='description'
                        value={group.description}
                        onChange={onChange}
                        label={intl.formatMessage({
                            id: 'Throttling.Advanced.ConditionalGroups.form.description',
                            defaultMessage: 'Description',
                        })}
                        className={classes.descriptionBox}
                        fullWidth
                        multiline
                        helperText={intl.formatMessage({
                            id: 'Throttling.Advanced.AddEdit.ConditionalGroups.form.description.help',
                            defaultMessage: 'Description of this group',
                        })}
                        variant='outlined'
                    />
                    <DeleteConditionGroup deleteThisGroup={deleteThisGroup} />
                </ExpansionPanelDetails>
            </ExpansionPanel>

        </div>
    );
}
ConditionalGroup.propTypes = {
    dataRow: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default ConditionalGroup;
