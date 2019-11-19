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

import React from 'react';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import FileIcon from '@material-ui/icons/Description';
import List from '@material-ui/core/List';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';

class Documents extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            documentsList: null,
        };
    }

    componentDidMount() {
        const { api: { apiType, id }, intl } = this.props;
        const API = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();

        const docs = API.getDocuments(id);
        docs.then((response) => {
            this.setState({ documentsList: response.obj.list });
        }).catch((errorResponse) => {
            const errorData = JSON.parse(errorResponse.message);
            const messageTxt = 'Error[' + errorData.code + ']: '
            + errorData.description
            + ' | ' + errorData.message
            + '.';
            console.error(messageTxt);
            Alert.error(intl.formatMessage({
                id: 'Apis.Details.NewOverview.Documents.error',
                defaultMessage: 'Error in fetching documents list of the API',
            }));
        });
    }

    render() {
        const { parentClasses, api } = this.props;
        const { documentsList } = this.state;
        return (
            <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
                <div className={parentClasses.titleWrapper}>
                    <Typography variant='h5' component='h3' className={parentClasses.title}>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.Documents.documents'
                            defaultMessage='Documents'
                        />
                    </Typography>
                    <Link to={'/apis/' + api.id + '/documents'}>
                        <Button variant='contained' color='default'>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.Documents.edit'
                                defaultMessage='Edit'
                            />
                        </Button>
                    </Link>
                </div>

                {documentsList && documentsList.length !== 0 && (
                    <List className={parentClasses.ListRoot}>
                        {documentsList.map((item) => (
                            <ListItemAvatar key={item.id}>
                                <Avatar>
                                    <FileIcon />
                                </Avatar>
                                <ListItemText primary={item.name} secondary={item.summary} />
                            </ListItemAvatar>
                        ))}
                    </List>
                )}
                {documentsList && documentsList.length === 0 && (
                    <Typography component='p' variant='body1' className={parentClasses.subtitle}>
                        &lt;
                        <FormattedMessage
                            id='Apis.Details.NewOverview.Documents.not.created'
                            defaultMessage='Not Created'
                        />
&gt;
                    </Typography>
                )}
            </Paper>
        );
    }
}

Documents.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
        apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]),
    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default Documents;
