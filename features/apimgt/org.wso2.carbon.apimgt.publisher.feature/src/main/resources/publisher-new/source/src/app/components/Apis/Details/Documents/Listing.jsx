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
import intl, { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import API from 'AppData/api.js';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import AddCircle from '@material-ui/icons/AddCircle';
import Alert from 'AppComponents/Shared/Alert';
import Create from './Create';
import MarkdownEditor from './MarkdownEditor';
import TextEditor from './TextEditor';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewHeader: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing.unit,
    },
    mainTitle: {
        paddingRight: 10,
    },
});
function LinkGenerator(props) {
    return <Link to={'/apis/' + props.apiId + '/documents/' + props.docId + '/details'}>{props.docName}</Link>;
}
class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            docs: null,
            showAddDocs: false,
        };
        this.apiId = props.api.id;
        this.toggleAddDocs = this.toggleAddDocs.bind(this);
        this.getDocumentsList = this.getDocumentsList.bind(this);
    }
    /**
     * @inheritDoc
     * @memberof Listing
     */
    componentDidMount() {
        this.getDocumentsList();
    }

    /*
     Get the document list attached to current API and set it to the state
     */
    getDocumentsList() {
        const api = new API();
        const { intl } = this.props;
        const docs = api.getDocuments(this.props.api.id);
        docs.then((response) => {
            this.setState({ docs: response.obj.list });
        }).catch((errorResponse) => {
            const errorData = JSON.parse(errorResponse.message);
            const messageTxt =
                'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
            console.error(messageTxt);
            Alert.error(intl.formatMessage({
                id: 'documents.listing.error.in.fetching',
                defaultMessage: 'Error in fetching documents list of the API',
            }));
        });
    }
    toggleAddDocs() {
        this.setState((oldState) => {
            return { showAddDocs: !oldState.showAddDocs };
        });
    }
    render() {
        const { classes } = this.props;
        const { docs, showAddDocs } = this.state;
        const columns = [
            {
                name: 'documentId',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'name',
                options: {
                    customBodyRender: (value, tableMeta, api) => {
                        if (tableMeta.rowData) {
                            const docName = tableMeta.rowData[1];
                            const docId = tableMeta.rowData[0];
                            return <LinkGenerator docName={docName} docId={docId} apiId={this.apiId} />;
                        }
                    },
                    filter: false,
                },
            },
            'sourceType',
            {
                name: 'action',
                options: {
                    customBodyRender: (value, tableMeta, api) => {
                        if (tableMeta.rowData) {
                            const docName = tableMeta.rowData[1];
                            const docId = tableMeta.rowData[0];
                            const sourceType = tableMeta.rowData[2];
                            if (sourceType === 'MARKDOWN') {
                                return <MarkdownEditor docName={docName} docId={docId} apiId={this.apiId} />;
                            } else if (sourceType === 'INLINE') {
                                return <TextEditor docName={docName} docId={docId} apiId={this.apiId} />;
                            } else {
                                return <span />;
                            }
                        }
                    },
                    filter: false,
                },
            },
        ];
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage id='documents.listing.documents' defaultMessage='Documents' />
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.toggleAddDocs}>
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage id='documents.listing.add.new.document' defaultMessage='Add New Document' />
                    </Button>
                </div>
                <div className={classes.contentWrapper}>
                    {showAddDocs && (
                        <Create toggleAddDocs={this.toggleAddDocs} getDocumentsList={this.getDocumentsList} />
                    )}

                    {docs && (
                        <MUIDataTable title='' data={docs} columns={columns} options={{ selectableRows: false }} />
                    )}
                </div>
            </div>
        );
    }
}

Listing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Listing));
