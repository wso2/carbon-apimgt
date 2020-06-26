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
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightRegular,
    },
    normalText: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightRegular,
        marginRight: 30,
        width: 200,
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
    listHeaderResource: {
        fontWeight: '600',
        fontSize: theme.typography.pxToRem(17),
    },
}));

/**
*
* @param {any} props Props for view usage in resources function.
* @returns {any} Returns the rendered UI for view scope usages.
*/
export default function UsageViewResource(props) {
    const classes = useStyles();
    const { usedResourceList } = props;
    if (!usedResourceList) {
        return <CircularProgress />;
    } else {
        return (
            <Table className={classes.table}>
                <TableRow>
                    <TableCell>
                        <Typography className={classes.listHeaderResource}>
                            <FormattedMessage
                                id='Scopes.Usage.UsageView.resource.usage'
                                defaultMessage='List of Resources'
                            />
                        </Typography>
                    </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell>
                        <Table className={classes.table}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        <Typography className={classes.heading}>
                                            <FormattedMessage
                                                id='Scopes.Usage.UsageView.resource.target'
                                                defaultMessage='Target'
                                            />
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography className={classes.heading}>
                                            <FormattedMessage
                                                id='Scopes.Usage.UsageView.resource.verb'
                                                defaultMessage='Verb'
                                            />
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            {usedResourceList.map((resource) => (
                                <TableRow>
                                    <TableCell>
                                        <Typography className={classes.normalText}>
                                            <Typography>
                                                {resource.target}
                                            </Typography>
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography className={classes.normalText}>
                                            <Typography>
                                                {resource.verb}
                                            </Typography>
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </Table>
                    </TableCell>
                </TableRow>
            </Table>
        );
    }
}

UsageViewResource.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    usedResourceList: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};
