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
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
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

/**
 * Renders Applications
 */
function Applications(props) {
    const { classes, intl } = props;
    const restApi = new API();
    const [applicationList, setApplicationList] = useState([]);
    const [isUpdated, setUpdated] = useState(true);

    useEffect(() => {
        if (isUpdated) {
            restApi.getApplicationList().then((result) => {
                setApplicationList(result.body.list);
            });
        }
    }, [isUpdated]);

    const title = (
        <FormattedMessage
            id='info.banner.application.settings'
            defaultMessage='Change Application Owner'
        />
    );
    const description = (
        <FormattedMessage
            id='info.banner.description.applications'
            // todo: Write the description here
            defaultMessage='You can change application owner here...'
        />
    );

    const columns = [
        { name: 'applicationId', options: { display: false } },
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'applications.table.header.name',
                defaultMessage: 'Name',
            }),
        },
        {
            name: 'owner',
            label: intl.formatMessage({
                id: 'applications.table.header.owner',
                defaultMessage: 'Owner',
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
                                                    '/settings/applications/edit/' +
                                                    row[0],
                                                name: row[1],
                                                owner: row[2],
                                                applicationList: applicationList,
                                            }}
                                        >
                                            <Button>
                                                <Icon>edit</Icon>
                                                <FormattedMessage
                                                    id='applications.edit.owner'
                                                    defaultMessage='Edit'
                                                />
                                            </Button>
                                        </Link>
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
                        id='applications.table.header.actions'
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
                        id='contents.main.title.applications'
                        defaultMessage='Applications'
                    />
                </Typography>
            </div>
            {applicationList.length > 0 ? (
                <MUIDataTable
                    title={false}
                    data={applicationList}
                    columns={columns}
                    options={options}
                />
            ) : (
                <InlineMessage type='info' height={140}>
                    <div className={classes.contentWrapper}>
                        <Typography
                            variant='h5'
                            component='h3'
                            className={classes.head}
                        >
                            {title}
                        </Typography>
                        <Typography component='p' className={classes.content}>
                            {description}
                        </Typography>
                    </div>
                </InlineMessage>
            )}
        </div>
    );
}

export default injectIntl(withStyles(styles)(Applications));
