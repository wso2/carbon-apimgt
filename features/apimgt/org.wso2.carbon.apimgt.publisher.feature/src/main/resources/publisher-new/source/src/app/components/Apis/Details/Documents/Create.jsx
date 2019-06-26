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
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
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
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonMain: {
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 3,
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
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        width: 300,
    },
    addResource: {
        width: 600,
        marginTop: 0,
    },
    buttonIcon: {
        marginRight: 10,
    },
    expansionPanel: {
        marginBottom: theme.spacing.unit,
    },
    expansionPanelDetails: {
        flexDirection: 'column',
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

function Create(props) {
    const { classes, toggleAddDocs, intl } = props;
    const [type, setType] = useState('HOWTO');
    const [source, setSource] = useState('INLINE');
    const [name, setName] = useState('');
    const [summary, setSummary] = useState('');
    const [url, setURL] = useState('');
    const [file, setFile] = useState(null);

    const changeType = (e) => {
        const { value } = e.target;
        if (value === 'PUBLIC_FORUM' || value === 'SUPPORT_FORUM') {
            setSource('URL');
        }
        setType(value);
    };
    const changeSource = (e) => {
        const { value } = e.target;
        setSource(value);
    };
    const setDisable = (item) => {
        if (item === 'INLINE' || item === 'MARKDOWN' || item === 'FILE') {
            if (type === 'PUBLIC_FORUM' || type === 'SUPPORT_FORUM') {
                return true;
            }
        }
    };
    const handleChange = name => (e) => {
        const { value } = e.target;
        if (name === 'name') {
            setName(value);
        } else if (name === 'summary') {
            setSummary(value);
        } else if (name === 'URL') {
            setURL(value);
        }
    };

    const onDrop = (acceptedFile) => {
        setFile(acceptedFile);
    };

    const addDocument = (apiId, updateAPI) => {
        const restAPI = new Api();

        const docPromise = restAPI.addDocument(apiId, {
            name,
            type,
            summary,
            sourceType: source,
            visibility: 'API_LEVEL',
            sourceUrl: url,
            otherTypeName: null,
            inlineContent: '',
        });
        docPromise
            .then((doc) => {
                Alert.info(`${doc.name} ${intl.formatMessage({
                    id: 'documents.markdown.editor.create.updated.successfully',
                    defaultMessage: 'updated successfully.',
                })}`);
                props.getDocumentsList();
                toggleAddDocs();
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
    };

    return (
        <div className={classes.addNewWrapper}>
            <Typography className={classes.addNewHeader}>
                <FormattedMessage id='documents.markdown.editor.create.title' defaultMessage='Add New Document' />}
            </Typography>
            <Divider className={classes.divider} />
            <div className={classes.addNewOther}>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        label='Name *'
                        helperText={
                            <FormattedMessage
                                id='documents.markdown.editor.create.name.description'
                                defaultMessage='Provide the name for the document'
                            />
                        }
                        type='text'
                        name='name'
                        margin='normal'
                        value={name}
                        onChange={handleChange('name')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        autoFocus
                    />
                </FormControl>
                <FormControl margin='normal' className={classes.FormControlOdd}>
                    <TextField
                        fullWidth
                        multiline
                        margin='normal'
                        variant='outlined'
                        label='Summary *'
                        helperText={
                            <FormattedMessage
                                id='documents.markdown.editor.create.summary.description'
                                defaultMessage='Provide a brief description for the document'
                            />
                        }
                        type='text'
                        name='summary'
                        margin='normal'
                        value={summary}
                        onChange={handleChange('summary')}
                        InputLabelProps={{
                            shrink: true,
                        }}
                    />
                </FormControl>
                <FormControl component='fieldset' className={classes.formControlFirst}>
                    <FormLabel component='legend'>
                        <FormattedMessage id='documents.markdown.editor.create.type' defaultMessage='Type' />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Type'
                        name='type'
                        className={classes.group}
                        value={type}
                        onChange={changeType}
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
                                            id='documents.markdown.editor.create.how.to'
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
                                            id='documents.markdown.editor.create.sample.and.sdk'
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
                                            id='documents.markdown.editor.create.public.forum'
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
                                            id='documents.markdown.editor.create.support.forum'
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
                                            id='documents.markdown.editor.create.other'
                                            defaultMessage='Other'
                                        />
                                    </div>
                                </div>
                            }
                        />
                    </RadioGroup>
                </FormControl>
                <FormControl component='fieldset' className={classes.formControl}>
                    <FormLabel component='legend'>
                        <FormattedMessage id='documents.markdown.editor.create.source' defaultMessage='Source' />
                    </FormLabel>
                    <RadioGroup
                        aria-label='Source'
                        name='source'
                        className={classes.group}
                        value={source}
                        onChange={changeSource}
                    >
                        <FormControlLabel
                            disabled={setDisable('INLINE')}
                            value='INLINE'
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='documents.markdown.editor.create.inline'
                                    defaultMessage='Inline'
                                />
                            }
                        />
                        <FormControlLabel
                            disabled={setDisable('MARKDOWN')}
                            value='MARKDOWN'
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='documents.markdown.editor.create.markdown'
                                    defaultMessage='Markdown'
                                />
                            }
                        />
                        <FormControlLabel
                            disabled={setDisable('URL')}
                            value='URL'
                            control={<Radio />}
                            label={<FormattedMessage id='documents.markdown.editor.create.url' defaultMessage='URL' />}
                        />
                        <FormControlLabel
                            disabled={setDisable('FILE')}
                            value='FILE'
                            control={<Radio />}
                            label={
                                <FormattedMessage id='documents.markdown.editor.create.file' defaultMessage='File' />
                            }
                        />
                    </RadioGroup>
                </FormControl>
                {source === 'URL' && (
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <TextField
                            fullWidth
                            margin='normal'
                            label={<FormattedMessage id='documents.markdown.editor.create.url' defaultMessage='URL' />}
                            helperText={
                                <FormattedMessage
                                    id='documents.markdown.editor.create.description.url'
                                    defaultMessage='Provide the URL to the source'
                                />
                            }
                            type='text'
                            name='URL'
                            margin='normal'
                            value={url}
                            onChange={handleChange('URL')}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </FormControl>
                )}
                {source === 'FILE' && (
                    <Dropzone
                        multiple={false}
                        className={classes.dropzone}
                        activeClassName={classes.acceptDrop}
                        rejectClassName={classes.rejectDrop}
                        onDrop={(dropFile) => {
                            onDrop(dropFile);
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
                {(source === 'INLINE' || source === 'MARKDOWN') && (
                    <InlineMessage type='info' height={60}>
                        <div className={classes.contentWrapper}>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='document.gotonext.content'
                                    defaultMessage='Please save the document. The content can be edited in the next step.'
                                />
                            </Typography>
                        </div>
                    </InlineMessage>
                )}
            </div>
            <Divider className={classes.divider} />
            <ApiContext.Consumer>
                {({ api, updateAPI }) => (
                    <div className={classes.addNewOther}>
                        <Button variant='contained' color='primary' onClick={() => addDocument(api.id, updateAPI)}>
                            <FormattedMessage
                                id='documents.markdown.editor.create.add.document'
                                defaultMessage='Add Document'
                            />
                        </Button>
                        <Button className={classes.button} onClick={toggleAddDocs}>
                            <FormattedMessage id='documents.markdown.editor.create.cancel' defaultMessage='Cancel' />
                        </Button>
                    </div>
                )}
            </ApiContext.Consumer>
        </div>
    );
}

Create.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Create));
