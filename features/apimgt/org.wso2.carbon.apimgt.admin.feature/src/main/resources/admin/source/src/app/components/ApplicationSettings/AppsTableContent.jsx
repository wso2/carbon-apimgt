/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import EditApplication from 'AppComponents/ApplicationSettings/EditApplication';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    fullHeight: {
        height: '100%',
    },
    tableRow: {
        height: theme.spacing(5),
        '& td': {
            padding: theme.spacing(0.5),
        },
    },
    appOwner: {
        pointerEvents: 'none',
    },
    appName: {
        '& a': {
            color: '#1b9ec7 !important',
        },
    },
    appTablePaper: {
        '& table tr td': {
            paddingLeft: theme.spacing(1),
        },
        '& table tr td:first-child, & table tr th:first-child': {
            paddingLeft: theme.spacing(2),
        },
        '& table tr td button.Mui-disabled span.material-icons': {
            color: theme.palette.action.disabled,
        },
    },
    tableCellWrapper: {
        '& td': {
            'word-break': 'break-all',
            'white-space': 'normal',
        },
    },
});
const StyledTableCell = withStyles((theme) => ({
    head: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    body: {
        fontSize: 14,
    },
    root: {
        '&:first-child': {
            paddingLeft: theme.spacing(2),
        },
    },
}))(TableCell);

const StyledTableRow = withStyles((theme) => ({
    root: {
        '&:nth-of-type(odd)': {
            backgroundColor: theme.palette.background.default,
        },
    },
}))(TableRow);

/**
 *
 *
 * @class AppsTableContent
 * @extends {Component}
 */
class AppsTableContent extends Component {
    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            notFound: false,
        };
        this.handleAppEdit = this.handleAppEdit.bind(this);
    }

    handleAppEdit = (app) => {
        const { EditComponent, editComponentProps, apiCall } = this.props;
        return (
            <>
                {EditComponent && (
                    <EditComponent
                        dataRow={app}
                        updateList={apiCall}
                        {...editComponentProps}
                    />
                )}
            </>
        );
    };

    /**
     * @inheritdoc
     * @memberof AppsTableContent
     */
    render() {
        const {
            apps, classes, editComponentProps, apiCall,
        } = this.props;
        const { notFound } = this.state;

        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <TableBody className={classes.fullHeight}>
                {apps && apps.map((app) => {
                    return (
                        <StyledTableRow className={classes.tableRow} key={app.applicationId}>
                            <StyledTableCell align='left'>
                                {app.name}
                            </StyledTableCell>
                            <StyledTableCell align='left'>{app.owner}</StyledTableCell>
                            <StyledTableCell align='left'>
                                <EditApplication
                                    dataRow={app}
                                    updateList={apiCall}
                                    {...editComponentProps}
                                />
                            </StyledTableCell>
                        </StyledTableRow>
                    );
                })}
            </TableBody>
        );
    }
}
AppsTableContent.propTypes = {
    toggleDeleteConfirmation: PropTypes.func.isRequired,
    apps: PropTypes.instanceOf(Map).isRequired,
};
export default withStyles(styles)(AppsTableContent);
