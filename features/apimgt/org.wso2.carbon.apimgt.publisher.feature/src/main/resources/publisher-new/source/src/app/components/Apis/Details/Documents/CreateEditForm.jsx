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

import React, { useState } from 'react';
import intl, { FormattedMessage, injectIntl } from 'react-intl';
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
import Dropzone from 'react-dropzone';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';

const styles = theme => ({
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        width: 300,
    },
    expansionPanel: {
        marginBottom: theme.spacing.unit,
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
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
        margin: '10px 0',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
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
        };
    }

    changeType = (e) => {
        const { value } = e.target;
        if (value === 'PUBLIC_FORUM' || value === 'SUPPORT_FORUM') {
            this.setState({ type: 'URL' });
        }
        this.setState({ type: value });
    };
    changeSource = (e) => {
        const { value } = e.target;
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
        }
    };

    onDrop = (acceptedFile) => {
        this.setState({ file: acceptedFile });
    };

    addDocument = (apiId) => {
        const { apiType } = this.props;
        const restAPI = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();
        const {
            name, type, summary, sourceType, sourceUrl, file, otherTypeName,
        } = this.state;
        const docPromise = restAPI.addDocument(apiId, {
            name,
            type,
            summary,
            sourceType,
            visibility: 'API_LEVEL',
            sourceUrl,
            otherTypeName,
            inlineContent: '',
        });
        return { docPromise, file };
    };
    updateDocument = (apiId) => {
        const restAPI = new Api();
        const {
            name, type, summary, sourceType, sourceUrl, file, otherTypeName,
        } = this.state;
        const { docId } = this.props;
        const docPromise = restAPI.updateDocument(apiId, docId, {
            name,
            type,
            summary,
            sourceType,
            visibility: 'API_LEVEL',
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
                        name, type, summary, sourceType, sourceUrl, otherTypeName,
                    } = doc.body;
                    this.setState({
                        name,
                        type,
                        summary,
                        sourceType,
                        sourceUrl,
                        otherTypeName,
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
    componentDidMount() {
        this.getDocument();
        const { apiId, docId } = this.props;
        if (apiId && docId) {
            this.setState({ disableName: true });
        }
    }
    render() {
        const {
            name, type, summary, sourceType, sourceUrl, file, disableName, otherTypeName,
        } = this.state;
        const { classes } = this.props;
        return (
            <div className={classes.addNewOther}>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        label={<FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.name'
                            defaultMessage='Name *'
                        />}
                        helperText={
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.name.helper.text'
                                defaultMessage='Provide the name for the document'
                            />
                        }
                        type='text'
                        name='name'
                        margin='normal'
                        value={name}
                        onChange={this.handleChange('name')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        autoFocus
                        disabled={disableName}
                    />
                </FormControl>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        multiline
                        margin='normal'
                        variant='outlined'
                        label={<FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.document.summary'
                            defaultMessage='Summary *'
                        />}
                        helperText={
                            <FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.document.summary.helper.text'
                                defaultMessage='Provide a brief description for the document'
                            />
                        }
                        type='text'
                        name='summary'
                        margin='normal'
                        value={summary}
                        onChange={this.handleChange('summary')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                </FormControl>
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
                            control={<Radio />}
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
                            control={<Radio />}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>code</Icon>
                                    <div>
                                        <FormattedMessage
                                            id='Apis.Details.Documents.CreateEditForm.document.create.type.sample'
                                            defaultMessage='Sample &amp; SDK'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='PUBLIC_FORUM'
                            control={<Radio />}
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
                            control={<Radio />}
                            label={
                                <div className={classes.typeTextWrapper}>
                                    <Icon>forum</Icon>
                                    <div>
                                        <FormattedMessage
                                            id={'Apis.Details.Documents.CreateEditForm.document.create.type.support.' +
                                            'forum'}
                                            defaultMessage='Support Forum'
                                        />
                                    </div>
                                </div>
                            }
                        />
                        <FormControlLabel
                            className={classes.formControlLabel}
                            value='OTHER'
                            control={<Radio />}
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
                                    id={'Apis.Details.Documents.CreateEditForm.document.create.type.other.document.' +
                                    'category'}
                                    defaultMessage='Other Document Category *'
                                />
                            }
                            helperText={
                                <FormattedMessage
                                    id={'Apis.Details.Documents.CreateEditForm.document.create.type.other.document.' +
                                    'category.helper.text'}
                                    defaultMessage='Provide the document category'
                                />
                            }
                            type='text'
                            name='otherTypeName'
                            margin='normal'
                            value={otherTypeName}
                            onChange={this.handleChange('otherTypeName')}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </FormControl>
                )}
                <FormControl component='fieldset' className={classes.formControl}>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Apis.Details.Documents.CreateEditForm.source'
                            defaultMessage='Source'
                        />
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
                            control={<Radio />}
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
                            control={<Radio />}
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
                            control={<Radio />}
                            label={<FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.source.url'
                                defaultMessage='URL'
                            />}
                        />
                        <FormControlLabel
                            disabled={this.setDisable('FILE')}
                            value='FILE'
                            control={<Radio />}
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
                            margin='normal'
                            label={<FormattedMessage
                                id='Apis.Details.Documents.CreateEditForm.source.url.url'
                                defaultMessage='URL'
                            />}
                            helperText={
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.source.url.helper.text'
                                    defaultMessage='Provide the URL to the source'
                                />
                            }
                            type='text'
                            name='sourceUrl'
                            margin='normal'
                            value={sourceUrl}
                            onChange={this.handleChange('sourceUrl')}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </FormControl>
                )}
                {sourceType === 'FILE' && (
                    <Dropzone
                        multiple={false}
                        className={classes.dropzone}
                        activeClassName={classes.acceptDrop}
                        rejectClassName={classes.rejectDrop}
                        onDrop={(dropFile) => {
                            this.onDrop(dropFile);
                        }}
                    >
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
                    </Dropzone>
                )}
                {(sourceType === 'INLINE' || sourceType === 'MARKDOWN') && (
                    <InlineMessage type='info' height={60}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Documents.CreateEditForm.document.content.info'
                                    defaultMessage={'Please save the document. The content can be edited in the next ' +
                                    'step.'}
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                )}
            </div>
        );
    }
}
CreateEditForm.defaultProps = {
    apiId: null,
    docId: null,
};
CreateEditForm.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    docId: PropTypes.shape({}),
    apiId: PropTypes.shape({}),
    apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]).isRequired,
};

export default injectIntl(withStyles(styles)(CreateEditForm));
