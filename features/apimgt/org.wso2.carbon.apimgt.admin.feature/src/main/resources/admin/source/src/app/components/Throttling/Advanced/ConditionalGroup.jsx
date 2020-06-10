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
import { makeStyles } from '@material-ui/core/styles';
import { useIntl } from 'react-intl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import DoneIcon from '@material-ui/icons/Done';
import TextField from '@material-ui/core/TextField';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Button from '@material-ui/core/Button';
import Checkbox from '@material-ui/core/Checkbox';

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
    },
    expandContentRoot: {
        flexDirection: 'column',
    },
}));

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 * @param {JSON} props Provides props from parent
 */
export default function ConditionalGroup(props) {
    const intl = useIntl();
    const classes = useStyles();
    const [expanded, setExpanded] = React.useState(false);
    const { executionFlow, setExecutionFlow } = props;

    const handleChange = (panel) => (event, isExpanded) => {
        setExpanded(isExpanded ? panel : false);
    };
    const onChange = (e) => {
        const field = e.target.name;
        const { value } = e.target;
        if (field === 'description') {
            executionFlow[field] = value;
        }
        setExecutionFlow(executionFlow);
    };
    const rows = [
        {
            name: 'IP Condition Policy',
            description: 'This configuration is used to throttle by IP address.',
        },
        {
            name: 'Header Condition Policy',
            description: 'This configuration is used to throttle based on Headers.',
        },
        {
            name: 'Query Param Condition Policy',
            description: 'This configuration is used to throttle based on query parameters.',
        },
        {
            name: 'JWT Condition Policy',
            description: 'This configuration is used to define JWT claims conditions',
        },
    ];
    return (
        <div className={classes.root}>
            <ExpansionPanel expanded={expanded === 'panel1'} onChange={handleChange('panel1')}>
                <ExpansionPanelSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls='panel1bh-content'
                    id='panel1bh-header'
                >
                    <div className={classes.heading} variant='body2'>
                        <Box display='flex'>
                            <DoneIcon />
                            <span>IP</span>
                            <DoneIcon />
                            <span>Header</span>
                            <DoneIcon />
                            <span>Query Param</span>
                            <DoneIcon />
                            <span>JWT Claim</span>
                        </Box>
                        <Typography variant='caption'>
                            Condition Policies active for Group
                            { ` ${executionFlow.id}` }
                        </Typography>
                    </div>
                    {!expanded && (
                        <Typography className={classes.secondaryHeading}>
                            Expand to edit
                        </Typography>
                    )}
                </ExpansionPanelSummary>
                <ExpansionPanelDetails classes={{ root: classes.expandContentRoot }}>


                    <Table className={classes.table} aria-label='simple table'>
                        <TableHead>
                            <TableRow>
                                <TableCell>Condition Policy</TableCell>
                                <TableCell align='right'>Action</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {rows.map((row) => (
                                <TableRow key={row.name}>
                                    <TableCell component='th' scope='row'>
                                        <Typography variant='body2'>
                                            <Checkbox
                                                checked={false}
                                                onChange={onChange}
                                                color='primary'
                                                inputProps={{ 'aria-label': 'secondary checkbox' }}
                                            />
                                            {row.name}
                                        </Typography>
                                        <Typography variant='caption'>
                                            {row.description}
                                        </Typography>
                                    </TableCell>
                                    <TableCell align='right'>
                                        <Button variant='contained' size='small' disabled>Configure</Button>

                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>

                    <TextField
                        margin='dense'
                        name='description'
                        value={executionFlow.description}
                        onChange={onChange}
                        label={intl.formatMessage({
                            id: 'Throttling.Advanced.ConditionalGroups.form.description',
                            defaultMessage: 'Description',
                        })}
                        fullWidth
                        multiline
                        helperText={intl.formatMessage({
                            id: 'Throttling.Advanced.AddEdit.ConditionalGroups.form.description.help',
                            defaultMessage: 'Description of this group',
                        })}
                        variant='outlined'
                    />
                </ExpansionPanelDetails>
            </ExpansionPanel>

        </div>
    );
}
