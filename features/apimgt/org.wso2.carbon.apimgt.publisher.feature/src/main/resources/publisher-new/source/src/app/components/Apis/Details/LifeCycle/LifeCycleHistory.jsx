/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import List from '@material-ui/core/List';
import Avatar from '@material-ui/core/Avatar';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Person from '@material-ui/icons/Person';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import moment from 'moment';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const styles = theme => ({
    firstCol: {
        width: 100,
    },
    personIcon: {
        fontSize: theme.typography.fontSize,
    },
    avatar: {
        width: 25,
        height: 25,
    },
});
const LifeCycleHistory = props => {
    const { classes } = props;
    return (
        <Paper>
            <Table className={classes.table}>
                <TableHead>
                    <TableRow>
                        <TableCell className={classes.firstCol}>User</TableCell>
                        <TableCell>Action</TableCell>
                        <TableCell>Time</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {props.lcHistory.map(
                        entry =>
                            entry.previousState && (
                                <TableRow key={entry.postState}>
                                    <TableCell component="th" scope="row">
                                        <Avatar className={classes.avatar}>
                                            <Person className={classes.personIcon} />
                                        </Avatar>
                                        <div>{entry.user}</div>
                                    </TableCell>
                                    <TableCell>
                                        {'LC has changed from ' + entry.previousState + ' to ' + entry.postState}
                                    </TableCell>
                                    <TableCell>{moment(entry.updatedTime).fromNow()}</TableCell>
                                </TableRow>
                            ),
                    )}
                </TableBody>
            </Table>
        </Paper>
    );
};
LifeCycleHistory.propTypes = {
    classes: PropTypes.object.isRequired,
};
export default withStyles(styles)(LifeCycleHistory);
