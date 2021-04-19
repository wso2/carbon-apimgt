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
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import green from '@material-ui/core/colors/green';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import Icon from '@material-ui/core/Icon';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Dropzone from 'react-dropzone';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import APIValidation from 'AppData/APIValidation';
import AppContext from 'AppComponents/Shared/AppContext';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    button: {
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewOther: {
        padding: theme.spacing(2),
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        width: 300,
    },
    expansionPanel: {
        marginBottom: theme.spacing(1),
    },
    group: {
        display: 'flex',
        flexDirection: 'row',
    },
    formControlFirst: {
        display: 'block',
    },
    formControl: {
        display: 'block',
        marginTop: 20,
    },
    formControlLabel: {
        background: '#efefef',
        borderRadius: 5,
        paddingRight: 10,
        marginLeft: 0,
        marginTop: 10,
    },
    typeTextWrapper: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    acceptDrop: {
        backgroundColor: green[50],
    },
    dropzone: {
        border: '1px dashed ' + theme.palette.primary.main,
        borderRadius: '5px',
        cursor: 'pointer',
        height: 75,
        padding: `${theme.spacing(2)}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
        margin: '10px 0',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
        cursor: 'pointer',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        '& span.material-icons': {
            color: theme.palette.primary.main,
        },
    },
    uploadedFile: {
        fontSize: 11,
    },
});

class CreateEditForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            type: 'HOWTO',
            sourceType: 'INLINE',
            name: '',
            summary: '',
            sourceUrl: '',
            file: null,
            disableName: false,
            otherTypeName: null,
            nameNotDuplicate: true,
            nameMaxLengthExceeds: false,
            invalidUrl: false,
            nameEmpty: false,
            summeryEmpty: false,
            urlEmpty: false,
            invalidDocName: false,
            visibility: 'API_LEVEL'
        };
    }

    changeType = (e) => {
        const { value } = e.target;
        if (value === 'PUBLIC_FORUM' || value === 'SUPPORT_FORUM') {
            this.setState({ sourceType: 'URL' });
        }
        this.setState({ type: value });
    };
    changeSource = (e) => {
        const { value } = e.target;
        this.validate();
        this.setState({ sourceType: value });
    };
    setDisable = (item) => {
        const { type } = this.state;
        if (item === 'INLINE' || item === 'MARKDOWN' || item === 'FILE') {
            if (type === 'PUBLIC_FORUM' || type === 'SUPPORT_FORUM') {
                return true;
            }
        }
    };
    handleChange = name => (e) => {
        const { value } = e.target;
        if (name === 'name') {
            this.setState({ name: value });
        } else if (name === 'summary') {
            this.setState({ summary: value });
        } else if (name === 'sourceUrl') {
            this.setState({ sourceUrl: value });
        } else if (name === 'otherTypeName') {
            this.setState({ otherTypeName: value });
        }else if (name === 'visibility') {
            this.setState({ visibility: value });
        }
    };

    onDrop = (acceptedFile) => {
        const { intl } = this.props;
        var specialChars = /[`!@#%^*()+\={};'"\\|,<>\/~]/;
        if (specialChars.test(acceptedFile[0].name)) {
            this.setState({ file: null });
            Alert.error(intl.formatMessage({
                id: 'Apis.Details.Documents.CreateEditForm.source.file.name.error.invalid',
                defaultMessage: 'Document source file name cannot contain spaces or special characters',
            }));
        } else {
            this.setState({ file: acceptedFile });
        }
    };

    addDocument = (apiId) => {
        const { apiType } = this.props;
        const restAPI = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();
        const {
            name, type, summary, sourceType, sourceUrl, file, otherTypeName, visibility,
        } = this.state;
        const docPromise = restAPI.addDocument(apiId, {
            name,
            type,
            summary,
            sourceType,
            visibility,
            sourceUrl,
            otherTypeName,
            inlineContent: '',
        });
        return { docPromise, file };
    };
    updateDocument = (apiId) => {
        const { apiType } = this.props;
        const restAPI = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();
        const {
            name, type, summary, sourceType, sourceUrl, file, otherTypeName, visibility,
        } = this.state;
        const { docId } = this.props;
        const docPromise = restAPI.updateDocument(apiId, docId, {
            name,
            type,
            summary,
            sourceType,
            visibility,
            sourceUrl,
            otherTypeName,
            inlineContent: '',
        });
        return { docPromise, file };
    };
    getDocument() {
        const { apiId, docId, apiType } = this.props;
        const restAPI = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();
        if (docId && apiId) {
            const docPromise = restAPI.getDocument(apiId, docId);
            docPromise
                .then((doc) => {
                    const {
                        name, type, summary, sourceType, sourceUrl, otherTypeName, visibility,
                    } = doc.body;
                    this.setState({
                        name,
                        type,
                        summary,
                        sourceType,
                        sourceUrl,
                        otherTypeName,
                        visibility,
                    });
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const { status } = error;
                    if (status === 404) {
                        this.setState({ apiNotFound: true });
                    }
                });
        }
    }
    validate(field=null, value=null) {
        let invalidUrl = false;
        if (field === 'url') {
            invalidUrl = value ? APIValidation.url.validate(value).error : false;
            this.setState({ invalidUrl });
            if (value === '') {
                this.setState({ urlEmpty: true });
            } else {
                this.setState({ urlEmpty: false });
            }
        } else if (field === 'name') {
            if (value) {
                const nameValidity = APIValidation.documentName.required().validate(value, { abortEarly: false }).error;
                if (nameValidity === null) {
                    this.setState({ invalidDocName: false });
                    const promise = APIValidation.apiDocument.validate({ id: this.props.apiId, name: value });
                        promise
                            .then((isDocumentPresent) => {
                                this.setState({ nameNotDuplicate: !isDocumentPresent });
                            })
                            .catch((error) => {
                                if (error.status === 404) {
                                    this.setState({ nameNotDuplicate: true });
                                } else {
                                    Alert.error('Error when validating document name');
                                }
                            });
                } else {
                    this.setState({ invalidDocName: true });
                }
            } else {
                this.setState({ nameNotDuplicate: true });
            }

            if (value === '') {
                this.setState({ nameEmpty: true, nameMaxLengthExceeds: false });
            } else if (value && value.length > 60) {
                this.setState({ nameEmpty: false, nameMaxLengthExceeds: true });
            } else {
                this.setState({ nameEmpty: false, nameMaxLengthExceeds: false });
            }
        } else if (field === 'summary') {
            if (value === '') {
                this.setState({ summeryEmpty: true });
            } else {
                this.setState({ summeryEmpty: false });
            }
        }
    }
    componentDidMount() {
        this.getDocument();
        const { apiId, docId } = this.props;
        if (apiId && docId) {
            this.setState({ disableName: true });
        }
    }
    showNameHelper() {
        const { nameEmpty, nameNotDuplicate, nameMaxLengthExceeds, invalidDocName } = this.state;
        if (nameMaxLengthExceeds) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.exceeds.document.name.length.helper.text'
                    defaultMessage='Document name exceeds the maximum length of 60 characters'
                />
            );
        } else if (nameNotDuplicate && !nameEmpty && !invalidDocName) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.document.name.helper.text'
                    defaultMessage='Provide the name for the document'
                />
            );
        } else if (nameEmpty) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.empty.document.name.helper.text'
                    defaultMessage='Document name cannot be empty'
                />
            );
        } else if (invalidDocName) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.invalid.document.name.helper.text'
                    defaultMessage='Document name cannot contain spaces or special characters'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.duplicate.document.name.helper.text'
                    defaultMessage='Duplicate document name'
                />
            );
        }
    }
    getUrlHelperText() {
        const { invalidUrl, urlEmpty} = this.state;

        if (invalidUrl) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.source.url.helper.text.error.invalid'
                    defaultMessage='Enter a valid URL to the source'
                />
            );
        } else if (urlEmpty) {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.source.url.helper.text.error.empty'
                    defaultMessage='URL Field cannot be empty'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='Apis.Details.Documents.CreateEditForm.source.url.helper.text'
                    defaultMessage='Provide the URL to the source'
                />
            );
        }
    }
    render() {
        const {
            name,
            type,
            summary,
            sourceType,
            sourceUrl,
            file,
            disableName,
            otherTypeName,
            invalidUrl,
            nameNotDuplicate,
            nameMaxLengthExceeds,
            nameEmpty,
            invalidDocName,
            summeryEmpty,
            urlEmpty,
            visibility
        } = this.state;
        const { classes, setSaveDisabled } = this.props;
        const { settings: settingsContext } = this.context;
        if (
            name !== '' &&
            summary !== '' &&
            nameNotDuplicate &&
            !nameMaxLengthExceeds &&
            !invalidDocName &&
            ((!invalidUrl && sourceUrl !== '') || sourceType !== 'URL')
        ) {
            setSaveDisabled(false);
        } else {
            setSaveDisabled(true);
        }
        return (
            <div className={classes.addNewOther}>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        InputProps={{
                            onBlur: ({ target: { value } }) => {
                                this.validate('name', value);
                            },
                        }}
                        label={
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.name'
                                defaultMessage='Name *'
                            />
                        }
                        helperText={this.showNameHelper()}
                        type='text'
                        variant='outlined'
                        name='name'
                        margin='normal'
                        value={name}
                        onChange={this.handleChange('name')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        autoFocus
                        disabled={disableName}
                        error={!nameNotDuplicate || nameEmpty || nameMaxLengthExceeds || invalidDocName}
                    />
                </FormControl>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        multiline
                        InputProps={{
                            onBlur: ({ target: { value } }) => {
                                this.validate('summary', value);
                            },
                            onKeyUp: ({ target: { value } }) => {
                                this.validate('summary', value);
                            },
                        }}
                        margin='normal'
                        variant='outlined'
                        label={
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.summary'
                                defaultMessage='Summary *'
                            />
                        }
                        helperText={
                            summeryEmpty ? (
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.document.summary.error.empty'
                                    defaultMessage='Document summary can not be empty'
                                />
                            ) : (
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.document.summary.helper.text'
                                    defaultMessage='Provide a brief description for the document'
                                />
                            )
                        }
                        type='text'
                        name='summary'
                        margin='normal'
                        value={summary}
                        onChange={this.handleChange('summary')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        error={summeryEmpty}
                    />
                </FormControl>
                {settingsContext.docVisibilityEnabled && 
                <FormControl margin='normal' className={classes.FormControlOdd}>
                <TextField
                    fullWidth
                    id='docVisibility-selector'
                    select
                    variant='outlined'
                    label={
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.docVisibility'
                            defaultMessage='Document Visibility'
                        />
                    }
                    helperText={
                        summeryEmpty ? (
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.summary.error.empty'
                                defaultMessage='Document summary can not be empty'
                            />
                        ) : (
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.summary.helper.text'
                                defaultMessage='Provide a brief description for the document'
                            />
                        )
                    }
                    type='text'
                    name='visibility'
                    margin='normal'
                    value={visibility}
                    onChange={this.handleChange('visibility')}
                    error={summeryEmpty}
                >
                    <MenuItem value='API_LEVEL'>
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.docVisibility.dropdown.public'
                            defaultMessage='Same as API Visibility'
                        />
                    </MenuItem>
                    <MenuItem value='PRIVATE'>
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.docVisibility.dropdown.private'
                            defaultMessage='Private'
                        />
                    </MenuItem>
                    <MenuItem value='OWNER_ONLY'>
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.docVisibility.dropdown.ownerOnly'
                            defaultMessage='Owner Only'
                        />
                    </MenuItem>
                </TextField>
            </FormControl>}
                <FormControl component='fieldset' className={classes.formControlFirst}>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.create.type'
                            defaultMessage='Type'
                        />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Type'
                        name='type'
                        className={classes.group}
                        value={type}
                        onChange={this.changeType}
                    >
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='HOWTO'
                            control={<Radio color='primary'/>}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>help_outline</Icon>
                                    <div>
                                        <FormattedMessage
                                            id='Apis.Details.Documents.CreateEditForm.document.create.type.how.to'
                                            defaultMessage='How To'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='SAMPLES'
                            control={<Radio color='primary'/>}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>code</Icon>
                                    <div>
                                        <FormattedMessage
                                            id='Apis.Details.Documents.CreateEditForm.document.create.type.sample'
                                            defaultMessage='Sample and SDK'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='PUBLIC_FORUM'
                            control={<Radio color='primary'/>}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>forum</Icon>
                                    <div>
                                        <FormattedMessage
                                            id='Apis.Details.Documents.CreateEditForm.document.create.type.public.forum'
                                            defaultMessage='Public Forum'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='SUPPORT_FORUM'
                            control={<Radio color='primary'/>}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>forum</Icon>
                                    <div>
                                        <FormattedMessage
                                            id={
                                                'Apis.Details.Documents.CreateEditForm.document.create.type.support.' +
                                                'forum'
                                            }
                                            defaultMessage='Support Forum'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='OTHER'
                            control={<Radio color='primary'/>}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>video_label</Icon>
                                    <div>
                                        <FormattedMessage
                                            id='Apis.Details.Documents.CreateEditForm.document.create.type.other'
                                            defaultMessage='Other'
                                        />
                                    </div>
                                </div>
                            }
                        />
                    </RadioGroup>
                </FormControl>
                {type === 'OTHER' && (
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <TextField
                            fullWidth
                            label={
                                <FormattedMessage
                                    id={
                                        'Apis.Details.Documents.CreateEditForm.document.create.type.other.document.' +
                                        'category'
                                    }
                                    defaultMessage='Other Document Type *'
                                />
                            }
                            helperText={
                                <FormattedMessage
                                    id={
                                        'Apis.Details.Documents.CreateEditForm.document.create.type.other.document.' +
                                        'category.helper.text'
                                    }
                                    defaultMessage='Provide the document type'
                                />
                            }
                            type='text'
                            name='otherTypeName'
                            margin='normal'
                            value={otherTypeName}
                            variant='outlined'
                            onChange={this.handleChange('otherTypeName')}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </FormControl>
                )}
                <FormControl component='fieldset' className={classes.formControl}>
                    <FormLabel component='legend'>
                        <FormattedMessage id='Apis.Details.Documents.CreateEditForm.source' defaultMessage='Source' />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Source'
                        name='sourceType'
                        className={classes.group}
                        value={sourceType}
                        onChange={this.changeSource}
                    >
                        <FormControlLabel
                            disabled={this.setDisable('INLINE')}
                            value='INLINE'
                            control={<Radio color='primary'/>}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.inline'
                                    defaultMessage='Inline'
                                />
                            }
                        />
                        <FormControlLabel
                            disabled={this.setDisable('MARKDOWN')}
                            value='MARKDOWN'
                            control={<Radio color='primary'/>}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.markdown'
                                    defaultMessage='Markdown'
                                />
                            }
                        />
                        <FormControlLabel
                            disabled={this.setDisable('URL')}
                            value='URL'
                            control={<Radio color='primary'/>}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.url'
                                    defaultMessage='URL'
                                />
                            }
                        />
                        <FormControlLabel
                            disabled={this.setDisable('FILE')}
                            value='FILE'
                            control={<Radio color='primary'/>}
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.file'
                                    defaultMessage='File'
                                />
                            }
                        />
                    </RadioGroup>
                </FormControl>
                {sourceType === 'URL' && (
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <TextField
                            fullWidth
                            InputProps={{
                                onBlur: ({ target: { value } }) => {
                                    this.validate('url', value);
                                },
                                onKeyUp: ({ target: { value } }) => {
                                    this.validate('url', value);
                                },
                            }}
                            margin='normal'
                            label={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.url.url'
                                    defaultMessage='URL'
                                />
                            }
                            helperText={this.getUrlHelperText()}
                            type='text'
                            name='sourceUrl'
                            margin='normal'
                            value={sourceUrl}
                            onChange={this.handleChange('sourceUrl')}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            error={invalidUrl || urlEmpty}
                            variant='outlined'
                        />
                    </FormControl>
                )}
                {sourceType === 'FILE' && (
                    <Dropzone
                        multiple={false}
                        accept='application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document, application/pdf, text/plain'
                        className={classes.dropzone}
                        activeClassName={classes.acceptDrop}
                        rejectClassName={classes.rejectDrop}
                        onDrop={(dropFile) => {
                            this.onDrop(dropFile);
                        }}
                    >
                        {({ getRootProps, getInputProps }) => (
                            <div {...getRootProps()}>
                                <input {...getInputProps()} />
                                <div className={classes.dropZoneWrapper}>
                                    <Icon className={classes.dropIcon} style={{ fontSize: 56 }}>
                                        cloud_upload
                                    </Icon>
                                    {file && file.length > 0 && (
                                        <div className={classes.uploadedFile}>
                                            <Icon>file</Icon> {file[0].name}
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </Dropzone>
                )}
                {(sourceType === 'INLINE' || sourceType === 'MARKDOWN') && (
                    <InlineMessage type='info' height={60}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.document.content.info'
                                    defaultMessage={
                                        'Please save the document. The content can be edited in the next ' + 'step.'
                                    }
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                )}
            </div>
        );
    }
}
CreateEditForm.contextType = AppContext;
CreateEditForm.defaultProps = {
    apiId: null,
    docId: null,
};
CreateEditForm.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    docId: PropTypes.shape({}),
    apiId: PropTypes.shape({}),
    saveDisabled: PropTypes.bool.isRequired,
    setSaveDisabled: PropTypes.func.isRequired,
    apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]).isRequired,
};

export default injectIntl(withStyles(styles)(CreateEditForm));
