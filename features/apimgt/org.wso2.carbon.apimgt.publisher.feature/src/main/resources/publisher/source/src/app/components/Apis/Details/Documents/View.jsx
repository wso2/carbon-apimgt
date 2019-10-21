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

import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Icon from '@material-ui/core/Icon';
import Button from '@material-ui/core/Button';
import ReactMarkdown from 'react-markdown';
import ReactSafeHtml from 'react-safe-html';
import { FormattedMessage, injectIntl } from 'react-intl';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    titleLink: {
        color: theme.palette.primary.main,
    },
    docTitle: {
        fontWeight: 100,
        fontSize: 50,
        color: theme.palette.grey[500],
    },
    docBadge: {
        padding: theme.spacing.unit,
        background: theme.palette.primary.main,
        position: 'absolute',
        top: 0,
        marginTop: -22,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    button: {
        padding: theme.spacing.unit * 2,
        marginTop: theme.spacing.unit * 2,
    },
    displayURL: {
        padding: theme.spacing.unit * 2,
        marginTop: theme.spacing.unit * 2,
        background: theme.palette.grey[200],
        color: theme.palette.getContrastText(theme.palette.grey[200]),
        display: 'flex',
    },
    displayURLLink: {
        paddingLeft: theme.spacing.unit * 2,
    },
    paper: {
        marginTop: 20,
        padding: theme.spacing.unit * 2,
        height: '100%',
    },
    leftCell: {
        width: 150,
    },
});
/**
 *
 *
 * @param {*} props
 * @returns
 */
function View(props) {
    const {
        classes,
        fullScreen,
        intl,
        match: {
            params: { documentId },
        },
    } = props;
    const { api, isAPIProduct } = useContext(APIContext);

    const [code, setCode] = useState('');
    const [doc, setDoc] = useState(null);
    const restAPI = isAPIProduct ? new APIProduct() : new API();

    useEffect(() => {
        const docPromise = restAPI.getDocument(api.id, documentId);
        docPromise
            .then((doc) => {
                const { body } = doc;
                setDoc(body);
                if (body.sourceType === 'MARKDOWN' || body.sourceType === 'INLINE') loadContentForDoc();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }, [documentId]);

    const loadContentForDoc = () => {
        const docPromise = restAPI.getInlineContentOfDocument(api.id, documentId);
        docPromise
            .then((doc) => {
                setCode(doc.text);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    };
    /**
     * Download the document related file
     * @param {any} response Response of download file
     */
    const downloadFile = (response, doc) => {
        let fileName = '';
        const contentDisposition = response.headers['content-disposition'];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers['content-type'];
        const blob = new Blob([response.data], {
            type: contentType,
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement('a');
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    };
    const handleDownload = () => {
        const promised_get_content = restAPI.getFileForDocument(api.id, documentId);
        promised_get_content
            .then((done) => {
                downloadFile(done, document);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Documents.View.error.downloading',
                        defaultMessage: 'Error downloading the file',
                    }));
                }
            });
    };
    const urlPrefix = isAPIProduct ? 'api-products' : 'apis';
    const listingPath = `/${urlPrefix}/${api.id}/documents`;
    return (
        doc && (
            <React.Fragment>
                <div className={classes.root}>
                    <div className={classes.titleWrapper}>
                        <Link to={listingPath} className={classes.titleLink}>
                            <Typography variant='h5' align='left' className={classes.mainTitle}>
                                <FormattedMessage id='Apis.Details.Documents.View.heading' defaultMessage='Documents' />
                            </Typography>
                        </Link>
                        <Icon>keyboard_arrow_right</Icon>
                        <Typography variant='h5'>{doc.name}</Typography>
                    </div>
                    <Paper className={classes.paper}>
                        <Table className={classes.table}>
                            <TableBody>
                                <TableRow>
                                    <TableCell className={classes.leftCell}>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.Documents.View.meta.name'
                                                defaultMessage='Name'
                                            />
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant='body1'>{doc.name}</Typography>
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.Documents.View.meta.summary'
                                                defaultMessage='Summary'
                                            />
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant='body1'>{doc.summary}</Typography>
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.Documents.View.meta.catogery'
                                                defaultMessage='Categorized as'
                                            />
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            {doc.type === 'OTHER' ? doc.otherTypeName : doc.type}
                                        </Typography>{' '}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            <FormattedMessage
                                                id='Apis.Details.Documents.View.meta.source'
                                                defaultMessage='Source Type'
                                            />
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant='body1'>{doc.sourceType}</Typography>{' '}
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Paper>

                    <Paper className={classes.paper}>
                        {doc.sourceType === 'MARKDOWN' && <ReactMarkdown source={code} />}
                        {doc.sourceType === 'INLINE' && <ReactSafeHtml html={code} />}
                        {doc.sourceType === 'URL' && (
                            <a className={classes.displayURL} href={doc.sourceUrl} target='_blank'>
                                {doc.sourceUrl}
                                <Icon className={classes.displayURLLink}>open_in_new</Icon>
                            </a>
                        )}
                        {doc.sourceType === 'FILE' && (
                            <Button
                                variant='contained'
                                color='default'
                                className={classes.button}
                                onClick={handleDownload}
                            >
                                <FormattedMessage
                                    id='Apis.Details.Documents.View.btn.download'
                                    defaultMessage='Download'
                                />

                                <Icon>arrow_downward</Icon>
                            </Button>
                        )}
                    </Paper>
                </div>
            </React.Fragment>
        )
    );
}

View.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    doc: PropTypes.shape({}).isRequired,
    apiId: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    fullScreen: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(View));
