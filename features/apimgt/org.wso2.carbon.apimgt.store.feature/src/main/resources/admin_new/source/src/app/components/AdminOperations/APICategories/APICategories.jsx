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
import { Link, Router } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import AddCircle from '@material-ui/icons/AddCircle';
import CreateBanner from '../CreateBanner';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    mainTitle: {
        paddingLeft: 0,
    },
    gatewayPaper: {
        marginTop: theme.spacing(2),
    },
    content: {
        marginTop: theme.spacing(3),
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
}));

const handleCreateClick = () => {
    const restApi = new API();
    // following hardcoded line is only for dev purposes.
    restApi.createAPICategory('new category', 'description goes here');
};

/**
 * Renders APICategories
 * @class APICategories
 * @extends {React.Component}
 */
export default function APICategories() {
    const classes = useStyles();
    const restApi = new API();
    const [mgLabels, setMgLabels] = useState([]);

    restApi.apiCategoriesListGet()
        .then((result) => {
            setMgLabels(result.body.list);
        });
    const title = (
        <FormattedMessage
            id='create.banner.title.create.api.categories'
            defaultMessage='Create API Categories'
        />
    );
    const description = (
        <FormattedMessage
            id='create.banner.description.api.category'
            // todo: Write the API category's description here
            defaultMessage='API category description'
        />
    );
    const buttonText = (
        <FormattedMessage
            id='create.banner.button.text.api.category'
            defaultMessage='Create API Category'
        />
    );

    return (
        <div className={classes.heading}>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='contents.main.title.api.categories'
                        defaultMessage='API categories'
                    />
                </Typography>
                <Link to='/admin/categories/api categories/create api category'>
                    <Button
                        size='small'
                        className={classes.button}
                    >
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='contents.main.heading.api.categories.add_new'
                            defaultMessage='Add New API Category'
                        />
                    </Button>
                </Link>
            </div>
            {mgLabels.length > 0 ? (
                <Paper className={classes.gatewayPaper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell align='left'>Category Name</TableCell>
                                <TableCell align='left'>Description</TableCell>
                                <TableCell align='left'>Number of APIs</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {mgLabels.map((row) => (
                                <TableRow key={row.name}>
                                    <TableCell component='th' scope='row' align='left'>
                                        {row.name}
                                    </TableCell>
                                    <TableCell align='left'>{row.description}</TableCell>
                                    {/* todo: Fill following table cell with Number_of_APIs per api category after API is modified */}
                                    <TableCell align='left'>-</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
            )
                : (
                    <CreateBanner
                        title={title}
                        description={description}
                        buttonText={buttonText}
                        onClick={handleCreateClick}
                    />
                )}
        </div>
    );
}
APICategories.defaultProps = {
    testprop: 'testpropva;',
};
// APICategories.propTypes = {
//     testprop: PropTypes.shape(),
// };
