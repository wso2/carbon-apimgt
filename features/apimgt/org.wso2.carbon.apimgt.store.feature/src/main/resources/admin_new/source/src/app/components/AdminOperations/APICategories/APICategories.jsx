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
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import { Link } from 'react-router-dom';
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
import Icon from '@material-ui/core/Icon';
import CreateBanner from '../CreateBanner';
import Alert from 'AppComponents/Shared/Alert';
import settings from '../../../../../../site/public/conf/settings';

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

let allCategories = [];

const deleteAPICategory = (id, name, setUpdated) => {
    const restApi = new API();
    const promisedDelete = restApi.deleteAPICategory(id);
    promisedDelete
        .then((response) => {
            if (response.status !== 200) {
                Alert.info('Something went wrong while deleting the API!');
                return;
            }
            setUpdated(false);
            Alert.info(`API category: ${name} deleted Successfully`);
        })
        .catch((error) => {
            if (error.status === 409) {
                Alert.error(
                    '[ ' + name + ' ] : ' + error.response.body.description,
                );
            } else {
                Alert.error('Something went wrong while deleting the API!');
            }
        });
};

/**
 * Renders APICategories
 */
export default function APICategories() {
    const classes = useStyles();
    const restApi = new API();
    const [mgLabels, setMgLabels] = useState([]);
    const [isUpdated, setUpdated] = useState(false);

    useEffect(() => {
        restApi.apiCategoriesListGet().then((result) => {
            if (!isUpdated) {
                allCategories = result.body.list;
                setMgLabels(result.body.list);
                setUpdated(true);
            }
        });
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
                <Typography
                    variant='h4'
                    align='left'
                    className={classes.mainTitle}
                >
                    <FormattedMessage
                        id='contents.main.title.api.categories'
                        defaultMessage='API categories'
                    />
                </Typography>
                <Link
                    to={
                        settings.app.context +
                        '/categories/api-categories/create-api-category'
                    }
                >
                    <Button size='small' className={classes.button}>
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='contents.main.heading.api.categories.create.api.category'
                            defaultMessage='Create API Category'
                        />
                    </Button>
                </Link>
            </div>
            {mgLabels.length > 0 ? (
                <Paper className={classes.gatewayPaper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell align='left'>
                                    Category Name
                                </TableCell>
                                <TableCell align='left'>Description</TableCell>
                                <TableCell align='left'>
                                    Number of APIs
                                </TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {mgLabels.map((row) => (
                                <TableRow key={row.name}>
                                    <TableCell
                                        component='th'
                                        scope='row'
                                        align='left'
                                    >
                                        {row.name}
                                    </TableCell>
                                    <TableCell align='left'>
                                        {row.description}
                                    </TableCell>
                                    {/* todo: Fill following table cell with Number_of_APIs per api category after API is modified */}
                                    <TableCell align='left'>-</TableCell>
                                    <TableCell>
                                        <table className={classes.actionTable}>
                                            <tr>
                                                <td>
                                                    <Link
                                                        to={{
                                                            pathname:
                                                                settings.app
                                                                    .context +
                                                                '/categories/api-categories/edit-api-category/' +
                                                                row.id,
                                                            state: {
                                                                fromNotifications: true,
                                                            },
                                                            name: row.name,
                                                            description:
                                                                row.description,
                                                        }}
                                                    >
                                                        <Button>
                                                            <Icon>edit</Icon>
                                                            <FormattedMessage
                                                                id='api.categories.category.edit'
                                                                defaultMessage='Edit'
                                                            />
                                                        </Button>
                                                    </Link>
                                                </td>
                                                <td>
                                                    <Button
                                                        // todo: when the rest api is completed, send the number_of_apis
                                                        // to this function and validate before deleting it.
                                                        // also, disabling the delete button can be done
                                                        onClick={() =>
                                                            deleteAPICategory(
                                                                row.id,
                                                                row.name,
                                                                setUpdated,
                                                            )
                                                        }
                                                        disabled={!isUpdated}
                                                    >
                                                        <Icon>
                                                            delete_forever
                                                        </Icon>
                                                        <FormattedMessage
                                                            id='api.categories.category.delete'
                                                            defaultMessage='Delete'
                                                        />
                                                    </Button>
                                                </td>
                                            </tr>
                                        </table>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
            ) : (
                <CreateBanner
                    title={title}
                    description={description}
                    buttonText={buttonText}
                    to={
                        settings.app.context +
                        '/categories/api-categories/create-api-category'
                    }
                />
            )}
        </div>
    );
}
