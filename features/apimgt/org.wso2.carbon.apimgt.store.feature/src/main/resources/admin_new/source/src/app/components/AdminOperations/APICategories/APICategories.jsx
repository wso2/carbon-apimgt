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

import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
import CreateBanner from '../CreateBanner';
import Alert from 'AppComponents/Shared/Alert';
import settings from '../../../../../../site/public/conf/settings';

const styles = (theme) => ({
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
});

const deleteAPICategory = (id, name, setUpdated, intl) => {
    const restApi = new API();
    const promisedDelete = restApi.deleteAPICategory(id);
    setUpdated(false);
    promisedDelete
        .then((response) => {
            if (response.status !== 200) {
                Alert.info(
                    intl.formatMessage({
                        id: 'api.categories.delete.api.category.unsuccesful',
                        defaultMessage:
                            'Something went wrong while deleting the API!',
                    }),
                );
                return;
            }
            Alert.info(
                intl.formatMessage({
                    id: 'api.categories.delete.api.category.succesful',
                    defaultMessage: 'API Category deleted successfully.',
                }),
            );
            setUpdated(true);
        })
        .catch((error) => {
            if (error.status === 409) {
                Alert.error(
                    '[ ' + name + ' ] : ' + error.response.body.description,
                );
            } else {
                Alert.error(
                    intl.formatMessage({
                        id: 'api.categories.delete.api.category.unsuccesful',
                        defaultMessage:
                            'Something went wrong while deleting the API!',
                    }),
                );
            }
        });
};

/**
 * Renders APICategories
 */
function APICategories(props) {
    const { classes, intl } = props;
    const restApi = new API();
    const [apiCategories, setApiCategories] = useState([]);
    const [isUpdated, setUpdated] = useState(true);

    useEffect(() => {
        if (isUpdated) {
            restApi.apiCategoriesListGet().then((result) => {
                setApiCategories(result.body.list);
            });
        }
    }, [isUpdated]);

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

    const columns = [
        { name: 'id', options: { display: false } },
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'api.categories.table.header.category.name',
                defaultMessage: 'Category Name',
            }),
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'api.categories.table.header.category.description',
                defaultMessage: 'Description',
            }),
        },
        {
            name: 'noOfApis',
            label: intl.formatMessage({
                id: 'api.categories.table.header.category.number.of.apis',
                defaultMessage: 'Number of APIs',
            }),
        },
        {
            name: 'actions',
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const row = tableMeta.rowData;
                        return (
                            <table className={classes.actionTable}>
                                <tr>
                                    <td>
                                        <Link
                                            to={{
                                                pathname:
                                                    settings.app.context +
                                                    '/categories/api-categories/edit-api-category/' +
                                                    row[0],
                                                name: row[1],
                                                description: row[2],
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
                                                    row[0],
                                                    row[1],
                                                    setUpdated,
                                                    intl,
                                                )
                                            }
                                            disabled={!isUpdated}
                                        >
                                            <Icon>delete_forever</Icon>
                                            <FormattedMessage
                                                id='api.categories.category.delete'
                                                defaultMessage='Delete'
                                            />
                                        </Button>
                                    </td>
                                </tr>
                            </table>
                        );
                    }
                    return false;
                },
                filter: false,
                sort: false,
                label: (
                    <FormattedMessage
                        id='api.categories.table.header.actions'
                        defaultMessage='Actions'
                    />
                ),
            },
        },
    ];

    const options = {
        filterType: 'multiselect',
        selectableRows: 'none',
        title: false,
        filter: false,
        sort: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: false,
    };

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
            {apiCategories.length > 0 ? (
                <MUIDataTable
                    title={false}
                    data={apiCategories}
                    columns={columns}
                    options={options}
                />
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

export default injectIntl(withStyles(styles)(APICategories));
