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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Dropzone from 'react-dropzone';
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import EditButton from '@material-ui/icons/Edit';
import SaveIcon from '@material-ui/icons/SaveAlt';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared/';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Input from '@material-ui/core/Input';
import { Link } from 'react-router-dom';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import PageContainer from 'AppComponents/Base/container/';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import PageNavigation from 'AppComponents/Apis/APIsNavigation';

/**
 * API Details Document page component
 * @class Details
 * @extends {Component}
 */

const styles = theme => ({
    root: {
        width: '100%',
        maxWidth: 360,
        backgroundColor: theme.palette.background.paper,
    },
    textField: {
        width: '50%',
    },
    caption: {
        marginTop: theme.spacing.unit * 2,
    },
    button: {
        margin: theme.spacing.unit,
        cursor: 'pointer',
        textDecoration: 'none',
        color: theme.palette.primary.main,
    },
    downloadWrapper: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
    },
    mainTitle: {
        paddingRight: theme.spacing.unit,
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonMain: {
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonSave: {
        marginTop: theme.spacing.unit * 5,
    },
    buttonCancel: {
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 2,
    },
});

/**
 *
 *
 * @class Details
 * @extends {Component}
 */
class Details extends React.Component {
    /**
     *Creates an instance of Details.
     * @param {*} props properies passed by the parent element
     * @memberof Details
     */
    constructor(props) {
        super(props);
        this.state = {
            doc: null,
            isEditable: false,
            files: [],
            uploadMethod: 'file',
        };
        this.onDrop = this.onDrop.bind(this);
        this.editDocument = this.editDocument.bind(this);
    }

    /**
     *
     *
     * @memberof Details
     */
    componentDidMount() {
        const {
            api,
            match: {
                params: { apiProductUUID, documentId, apiUUID },
            },
        } = this.props;
        let promisedDocument;
        switch (api.apiType) {
            case Api.CONSTS.APIProduct: {
                const apiProduct = new APIProduct();
                promisedDocument = apiProduct.getDocument(apiProductUUID, documentId);
                break;
            }
            default: {
                const newApi = new Api();
                promisedDocument = newApi.getDocument(apiUUID, documentId);
            }
        }
        promisedDocument
            .then((response) => {
                if (response.obj) {
                    this.setState({ doc: response.obj });
                }
            })
            .catch((error) => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }

    handleTextChange = prop => (event) => {
        this.state.doc[prop] = event.target.value;
        this.setState({ doc: this.state.doc });
    };

    /*
     On click listener for 'View' link on each document related row in the documents table.
     1- If the document type is 'URL' open it in new tab
     2- If the document type is 'INLINE' open the content with an inline editor
     3- If the document type is 'FILE' download the file
     */
    viewDocContentHandler(document) {
        const { apiUUID } = this.props.match.params;
        if (document.sourceType === 'URL') {
            window.open(document.sourceUrl, '_blank');
        } else if (document.sourceType === 'INLINE') {
            this.setState({
                documentId: document.documentId,
                showInlineEditor: true,
                selectedDocName: document.name,
            });
        } else if (document.sourceType === 'FILE') {
            const api = new Api();
            const promised_get_content = api.getFileForDocument(apiUUID, document.documentId);
            promised_get_content
                .then((done) => {
                    this.downloadFile(done, document);
                })
                .catch((error_response) => {
                    const error_data = JSON.parse(error_response.data);
                    const messageTxt =
                        'Error[' + error_data.code + ']: ' + error_data.description + ' | ' + error_data.message + '.';
                    console.error(messageTxt);
                });
        }
    }

    /**
     * Make the fields editable
     */
    editDocument() {
        this.setState({
            isEditable: true,
        });
    }

    /**
     * Download the document related file
     * @param {any} response Response of download file
     */
    downloadFile(response, doc) {
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
    }

    /**
     * Handle Document file ondrop action when user drag and drop file to dopzone, This is passed through props
     * to child component
     * @param {Object} files File object passed from DropZone library
     * @memberof ApiCreateSwagger
     */
    onDrop(files) {
        this.setState({
            files,
        });
    }

    handleUploadMethodChange = (e, value) => {
        this.setState({ uploadMethod: value });
    };

    /**
     *
     *  Render method of the component
     * @returns {React.Component} endpoint detail html component
     * @memberof Details
     */
    render() {
        const { classes, api } = this.props;
        const {
            notFound, doc, isEditable, files,
        } = this.state;
        const docListingPath =
            api.apiType === Api.CONSTS.APIProduct
                ? '/api-products/'
                : '/apis/' + this.props.match.params.apiUUID + '/documents';
        if (notFound) {
            return (
                <PageContainer pageNav={<PageNavigation />}>
                    <ResourceNotFound />
                </PageContainer>
            );
        }

        if (!doc) {
            return <Progress />;
        }

        const overviewPath = `${docListingPath}/${doc.documentId}/details`;

        return (
            <Grid container spacing={0} direction='row' justify='flex-start' alignItems='stretch'>
                <Grid item xs={12}>
                    <Grid container>
                        <Grid item>
                            <Typography variant='h4' align='left' className={classes.mainTitle}>
                                <Link to={docListingPath}>
                                    <FormattedMessage id='documents' defaultMessage='Documents' />
                                </Link>
                            </Typography>
                        </Grid>
                        <Grid item>
                            <Typography variant='h4' align='left' className={classes.mainTitle}>
                                >
                            </Typography>
                        </Grid>
                        <Grid item>
                            <Typography variant='h4' align='left' className={classes.mainTitle}>
                                {doc.name}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <Button
                                size='small'
                                className={classes.button}
                                onClick={this.editDocument}
                                disabled={isEditable}
                            >
                                <EditButton className={classes.buttonIcon} />
                                Edit Document
                            </Button>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={12}>
                    <Grid container direction='column'>
                        <Grid item>
                            <Typography variant='caption' className={classes.caption}>
                                <FormattedMessage id='type' defaultMessage='Type' />
                            </Typography>
                            <Select
                                value={doc.type}
                                onChange={this.handleTextChange('type')}
                                label={<FormattedMessage id='type' defaultMessage='Type' />}
                                input={
                                    !isEditable ? (
                                        <Input name='name' id='name-readonly' readOnly />
                                    ) : (
                                        <Input name='name' id='name-readonly' />
                                    )
                                }
                                className={classes.textField}
                            >
                                <MenuItem value=''>
                                    <em>None</em>
                                </MenuItem>
                                <MenuItem value='HOWTO'>HOW TO</MenuItem>
                                <MenuItem value='SAMPLES'>SAMPLES</MenuItem>
                                <MenuItem value='PUBLIC_FORUM'>PUBLIC FORUM</MenuItem>
                                <MenuItem value='SUPPORT_FORUM'>SUPPORT FORUM</MenuItem>
                                <MenuItem value='API_MESSAGE_FORMAT'>API MESSAGE FORMAT</MenuItem>
                                <MenuItem value='SWAGGER_DOC'>SWAGGER DOC</MenuItem>
                                <MenuItem value='OTHER'>OTHER</MenuItem>
                            </Select>
                        </Grid>
                        <Grid item>
                            <TextField
                                id='doc-summary'
                                label={<FormattedMessage id='summary' defaultMessage='Summary' />}
                                value={doc.summary}
                                placeholder='No Value!'
                                margin='normal'
                                className={classes.textField}
                                multiline
                                rowsMax='4'
                                InputProps={{
                                    readOnly: !isEditable,
                                }}
                                variant='outlined'
                                onChange={this.handleTextChange('summary')}
                            />
                        </Grid>
                        <Grid item>
                            {doc.sourceType === 'URL' && (
                                <React.Fragment>
                                    {isEditable ? (
                                        <TextField
                                            id='doc-source'
                                            label={<FormattedMessage id='document' defaultMessage='Document' />}
                                            value={doc.sourceUrl}
                                            placeholder='No Value!'
                                            margin='normal'
                                            className={classes.textField}
                                            variant='outlined'
                                            onChange={this.handleTextChange('sourceUrl')}
                                        />
                                    ) : (
                                        <React.Fragment>
                                            <Typography variant='caption' className={classes.caption}>
                                                <FormattedMessage id='document' defaultMessage='Document' />
                                            </Typography>
                                            <a href={doc.sourceUrl} target='_blank'>
                                                {doc.sourceUrl}
                                            </a>
                                        </React.Fragment>
                                    )}
                                </React.Fragment>
                            )}
                            {doc.sourceType === 'FILE' && (
                                <React.Fragment>
                                    {isEditable ? (
                                        <FormControl className='horizontal dropzone-wrapper'>
                                            <div className='dropzone'>
                                                <Dropzone
                                                    onDrop={this.onDrop}
                                                    multiple={false}
                                                    accept='application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document, application/pdf'
                                                >
                                                    <p>
                                                        <FormattedMessage
                                                            id='try.dropping.some.files.here.or.click.to.select.files.to.upload'
                                                            defaultMessage={
                                                                'Try dropping some files here, or click to select' +
                                                                ' files to upload.'
                                                            }
                                                        />
                                                    </p>
                                                </Dropzone>
                                            </div>
                                            <aside>
                                                <h2>
                                                    <FormattedMessage
                                                        id='uploaded.files'
                                                        defaultMessage='Uploaded files'
                                                    />
                                                </h2>
                                                <ul>
                                                    {files.map(f => (
                                                        <li key={f.name}>
                                                            {f.name} - {f.size} bytes
                                                        </li>
                                                    ))}
                                                </ul>
                                            </aside>
                                        </FormControl>
                                    ) : (
                                        <React.Fragment>
                                            <Typography variant='caption' className={classes.caption}>
                                                <FormattedMessage id='document' defaultMessage='Document' />
                                            </Typography>
                                            <div className={classes.downloadWrapper}>
                                                <Typography variant='subtitle1' gutterBottom>
                                                    {doc.name}.pdf
                                                </Typography>
                                                <a
                                                    className={classes.button}
                                                    color='primary'
                                                    onClick={() => this.viewDocContentHandler(doc)}
                                                >
                                                    <SaveIcon />
                                                </a>
                                            </div>
                                        </React.Fragment>
                                    )}
                                </React.Fragment>
                            )}
                            {doc.sourceType === 'INLINE' && (
                                <React.Fragment>
                                    {isEditable ? (
                                        <TextField
                                            id='doc-source'
                                            label={<FormattedMessage id='document' defaultMessage='Document' />}
                                            value={doc.sourceUrl}
                                            placeholder='No Value!'
                                            margin='normal'
                                            className={classes.textField}
                                            variant='outlined'
                                            onChange={this.handleTextChange('sourceUrl')}
                                        />
                                    ) : (
                                        <React.Fragment>
                                            <Typography variant='caption' className={classes.caption}>
                                                <FormattedMessage id='document' defaultMessage='Document' />
                                            </Typography>
                                            <a href={doc.sourceUrl} target='_blank'>
                                                {doc.sourceUrl}
                                            </a>
                                        </React.Fragment>
                                    )}
                                </React.Fragment>
                            )}
                        </Grid>
                        {isEditable && (
                            <Grid item>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={this.addScope}
                                    className={classes.buttonSave}
                                >
                                    <FormattedMessage id='save' defaultMessage='Save' />
                                </Button>
                                <Link to={overviewPath}>
                                    <Button variant='contained' color='default' className={classes.buttonCancel}>
                                        <FormattedMessage id='cancel.btn' defaultMessage='Cancel' />
                                    </Button>
                                </Link>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

Details.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({ apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]) }).isRequired,
};

export default withStyles(styles)(Details);
