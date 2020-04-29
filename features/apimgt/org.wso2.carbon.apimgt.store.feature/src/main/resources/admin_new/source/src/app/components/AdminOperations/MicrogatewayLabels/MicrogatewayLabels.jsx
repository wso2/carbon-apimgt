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

const deleteMgLabel = (id, name, setUpdated, intl) => {
    const restApi = new API();
    const promisedDelete = restApi.deleteMicrogatewayLabel(id);
    setUpdated(false);
    promisedDelete
        .then((response) => {
            if (response.status !== 200) {
                Alert.info(
                    intl.formatMessage({
                        id: 'microgateway.labels.delete.label.unsuccesful',
                        defaultMessage:
                            'Something went wrong while deleting the label!',
                    }),
                );
                return;
            }
            Alert.info(
                intl.formatMessage({
                    id: 'microgateway.labels.delete.label.succesful',
                    defaultMessage: 'Microgateway label deleted successfully.',
                }),
            );
            setUpdated(true);
        })
        .catch(() => {
            Alert.error(
                intl.formatMessage({
                    id: 'microgateway.labels.delete.label.unsuccesful',
                    defaultMessage:
                        'Something went wrong while deleting the label!',
                }),
            );
        });
};

/**
 * Renders MicrogatewayLabels
 */
function MicrogatewayLabels(props) {
    const { classes, intl } = props;
    const restApi = new API();
    const [mgLabels, setMgLabels] = useState([]);
    const [isUpdated, setUpdated] = useState(true);

    useEffect(() => {
        if (isUpdated) {
            restApi.getMicrogatewayLabelList().then((result) => {
                setMgLabels(result.body.list);
            });
        }
    }, [isUpdated]);

    const title = (
        <FormattedMessage
            id='create.banner.title.create.microgateway.labels'
            defaultMessage='Create Microgateway Labels'
        />
    );
    const description = (
        <FormattedMessage
            id='create.banner.description.microgateway.labels'
            // todo: Write the Microgateway label description here
            defaultMessage='Microgateway description'
        />
    );
    const buttonText = (
        <FormattedMessage
            id='create.banner.button.text.microgateway.labels'
            defaultMessage='Create Label'
        />
    );

    const columns = [
        { name: 'id', options: { display: false } },
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'microgateway.labels.table.header.label',
                defaultMessage: 'Label',
            }),
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'microgateway.labels.table.header.description',
                defaultMessage: 'Description',
            }),
        },
        {
            name: 'accessUrls',
            label: intl.formatMessage({
                id: 'microgateway.labels.table.header.hosts',
                defaultMessage: 'Gateway Host(s)',
            }),
            options: {
                customBodyRender: (value) => {
                    return (
                        <td>
                            {value.map((host) => (
                                <tr>{host}</tr>
                            ))}
                        </td>
                    );
                },
            },
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
                                                    '/microgateway/labels/edit-microgateway-label/' +
                                                    row[0],
                                                name: row[1],
                                                description: row[2],
                                                hosts: row[3],
                                            }}
                                        >
                                            <Button>
                                                <Icon>edit</Icon>
                                                <FormattedMessage
                                                    id='microgateway.labels.label.edit'
                                                    defaultMessage='Edit'
                                                />
                                            </Button>
                                        </Link>
                                    </td>
                                    <td>
                                        <Button
                                            onClick={() =>
                                                deleteMgLabel(
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
                                                id='microgateway.labels.label.delete'
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
                        id='microgateway.labels.table.header.actions'
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
                        id='contents.main.title.microgateway.labels'
                        defaultMessage='Microgateway Labels'
                    />
                </Typography>
                <Link
                    to={
                        settings.app.context +
                        '/microgateway/labels/create-microgateway-label'
                    }
                >
                    <Button size='small' className={classes.button}>
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='contents.main.heading.microgateway.labels.create.label'
                            defaultMessage='Create Label'
                        />
                    </Button>
                </Link>
            </div>
            {mgLabels.length > 0 ? (
                <MUIDataTable
                    title={false}
                    data={mgLabels}
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
                        '/microgateway/labels/create-microgateway-label'
                    }
                />
            )}
        </div>
    );
}

export default injectIntl(withStyles(styles)(MicrogatewayLabels));
