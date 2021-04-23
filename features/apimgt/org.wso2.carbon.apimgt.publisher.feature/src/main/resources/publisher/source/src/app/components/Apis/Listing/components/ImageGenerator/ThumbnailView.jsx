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
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import green from '@material-ui/core/colors/green';
import red from '@material-ui/core/colors/red';
import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import Grid from '@material-ui/core/Grid';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Paper from '@material-ui/core/Paper';
import Slide from '@material-ui/core/Slide';
import Typography from '@material-ui/core/Typography';
import Icon from '@material-ui/core/Icon';
import IconButton from '@material-ui/core/IconButton';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import MaterialIcons from 'MaterialIcons';
import Alert from 'AppComponents/Shared/Alert';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Configurations from 'Config';

import BaseThumbnail from './BaseThumbnail';

const windowURL = window.URL || window.webkitURL;
const dropzoneStyles = {
    border: '1px dashed ',
    borderRadius: '5px',
    cursor: 'pointer',
    height: 75,
    padding: '8px 0px',
    position: 'relative',
    textAlign: 'center',
    width: '100%',
    margin: '10px 0',
};
const styles = (theme) => ({
    acceptDrop: {
        backgroundColor: green[50],
    },
    dropzone: {
        border: '1px dashed ' + theme.palette.primary.main,
        borderRadius: '5px',
        cursor: 'pointer',
        height: 'calc(100vh - 10em)',
        padding: `${theme.spacing(2)}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
        margin: '10px 0',
    },
    dropZoneWrapper: {
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        '& span': {
            fontSize: 100,
            color: theme.palette.primary.main,
        },
    },
    preview: {
        height: theme.spacing(25),
    },
    rejectDrop: {
        backgroundColor: red[50],
    },
    group: {
        display: 'flex',
        flexDirection: 'row',
        padding: '0 0 0 20px',
    },
    popupHeader: {
        display: 'flex',
        flexDirection: 'row',
    },
    iconView: {
        width: 30,
        margin: 10,
        cursor: 'pointer',
    },
    subtitle: {
        marginRight: 20,
    },
    subtitleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 20,
        marginBottom: 10,
    },
    body: {
        marginBottom: 10,
    },
    imageGenWrapper: {
        '& > div': {
            position: 'fixed',
            marginTop: 20,
        },
    },
    backgroundSelection: {
        cursor: 'pointer',
        marginRight: 10,
        display: 'inline-block',
        border: 'solid 1px #ccc',
    },
    actionBox: {
        boxShadow:
            '0px 1px 5px 0px rgba(0,0,0,0.2), 0px 2px 2px 0px rgba(0,0,0,0.14), 0px 3px 1px -2px rgba(0,0,0,0.12)',
        justifyContent: 'flex-start',
        padding: '10px 0 10px 10px',
    },
    imageContainer: {
        paddingTop: 10,
    },
});

/**
 * Slide up transition for modal
 *
 * @param {any} props Properties
 * @returns {Slide} Slide up transition
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 * Provides a view for the API Thumbnail image.
 * This can be either user defined image uploaded earlier or a generated Image.
 */
class ThumbnailView extends Component {
    /**
     * Initializes the ThumbnailView Component
     *
     * @param {any} props Component properties
     */
    constructor(props) {
        super(props);
        this.state = {
            open: false,
            file: null,
            thumbnail: null,
            selectedTab: 'upload',
            category: MaterialIcons.categories[0].name,
            selectedIcon: null,
            selectedIconUpdate: null,
            color: null,
            colorUpdate: null,
            backgroundIndex: null,
            backgroundIndexUpdate: null,
            uploading: false,
        };
        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    /**
     * Clean up resource
     */
    componentWillUnmount() {
        if (this.state.thumbnail) {
            windowURL.revokeObjectURL(this.state.thumbnail);
        }
    }

    /**
     * Event listener for file drop on the dropzone
     *
     * @param {File} acceptedFile dropped file
     */
    onDrop(acceptedFile) {
        this.setState({ file: acceptedFile });
    }

    selectIcon = (selectedIconUpdate) => {
        this.setState({ selectedIconUpdate });
    };

    selectBackground = (backgroundIndexUpdate) => {
        this.setState({ backgroundIndexUpdate });
    };

    /**
     * @param {SyntheticEvent} e React event object
     */
    handleClick = (action, intl) => () => {
        if (action === 'btnEditAPIThumb') {
            this.setState({ open: true });
        } else if (action === 'btnUploadAPIThumb') {
            const { api } = this.props;
            const {
                selectedTab, selectedIconUpdate, category, colorUpdate, backgroundIndexUpdate, file,
            } = this.state;
            let fileObj;
            if (selectedTab === 'upload') {
                if (!api.id && !file) {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Listing.components.ImageGenerator.ThumbnailView.thumbnail.validation.error',
                        defaultMessage: 'Invalid file or API information is not set correctly.',
                    }));
                    return;
                }
                /* eslint prefer-destructuring: ["error", {VariableDeclarator: {object: true}}] */
                fileObj = file[0];
            } else if (selectedTab === 'remove') {
                fileObj = new File([], 'FileName.jpg', { type: 'application/json' });
            } else {
                if (!selectedIconUpdate && !colorUpdate && !backgroundIndexUpdate) {
                    Alert.error(intl.formatMessage({
                        id: 'the.icon.is.not.modified',
                        defaultMessage: 'The icon is not modified',
                    }));
                    return;
                }
                const newIconJson = {
                    key: selectedIconUpdate,
                    category,
                    color: colorUpdate,
                    backgroundIndex: backgroundIndexUpdate,
                };
                const blob = new Blob([JSON.stringify(newIconJson)], { type: 'application/json' });
                fileObj = new File([blob], 'FileName.json', { type: 'application/json' });
            }

            this.uploadThumbnail(selectedTab, api.id, fileObj, intl);
        }
    };

    handleChange = (event, selectedTab) => {
        this.setState({ selectedTab });
    };

    handleSelectionChange = (name) => (event) => {
        this.setState({ [name]: event.target.value });
    };

    handleChangeComplete = (colorUpdate) => {
        this.setState({ colorUpdate: colorUpdate.hex });
    };

    /**
     * Handle modal close event
     */
    handleClose() {
        const { file, preview } = this.state;
        if (file) {
            windowURL.revokeObjectURL(preview);
        }
        this.setState((cState) => ({
            open: false,
            file: null,
            colorUpdate: cState.color,
            backgroundIndexUpdate: cState.background,
            selectedIconUpdate: cState.selectedIcon,
        }));
    }

    /**
     * Add new thumbnail image to an API
     *
     * @param {String} apiId ID of the API to be updated
     * @param {File} file new thumbnail image file
     */
    uploadThumbnail(selectedTab, apiId, file, intl) {
        this.setState({ uploading: true });
        const {
            api: { apiType, id },
            setImageUpdate,
        } = this.props;
        const promisedThumbnail = apiType === Api.CONSTS.APIProduct
            ? new APIProduct().addAPIProductThumbnail(id, file)
            : new Api().addAPIThumbnail(id, file);

        promisedThumbnail
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Listing.components.ImageGenerator.ThumbnailView.thumbnail.upload.success',
                    defaultMessage: 'Thumbnail uploaded successfully',
                }));
                if (selectedTab === 'upload') {
                    this.setState({ open: false, thumbnail: windowURL.createObjectURL(file) });
                } else {
                    this.setState((cState) => ({
                        open: false,
                        thumbnail: file.preview,
                        selectedIcon: cState.selectedIconUpdate,
                        color: cState.colorUpdate,
                        backgroundIndex: cState.backgroundIndexUpdate,
                    }));
                }
                setImageUpdate();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.error(intl.formatMessage({
                    id: 'Apis.Listing.components.ImageGenerator.ThumbnailView.thumbnail.upload.error',
                    defaultMessage: 'Error occurred while uploading new thumbnail. Please try again.',
                }));
            })
            .finally(() => {
                this.setState({ uploading: false });
            });
    }

    saveDisableEnable() {
        const {
            file, selectedTab, selectedIconUpdate, colorUpdate, backgroundIndexUpdate, uploading,
        } = this.state;
        if (selectedTab === 'upload') {
            return !(file && file[0]) || uploading; // If no files is uploaded retrun true
        } else if (selectedTab === 'remove') {
            return false;
        } else {
            // If one of them is selected we return false
            return !(selectedIconUpdate || backgroundIndexUpdate || colorUpdate) || uploading;
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            api, classes, width, height, isEditable, intl, imageUpdate,
        } = this.props;
        const maxSize = 1000000;
        const {
            file,
            thumbnail,
            selectedTab,
            selectedIcon,
            color,
            backgroundIndex,
            uploading,
        } = this.state;
        let { category } = this.state;
        if (!category) category = MaterialIcons.categories[0].name;

        return (
            <>
                <BaseThumbnail
                    isEditable={isEditable}
                    onClick={this.handleClick('btnEditAPIThumb', intl)}
                    thumbnail={thumbnail}
                    selectedIcon={selectedIcon}
                    color={color}
                    backgroundIndex={backgroundIndex}
                    category={category}
                    api={api}
                    width={width}
                    height={height}
                    imageUpdate={imageUpdate}
                />

                <Dialog
                    TransitionComponent={Transition}
                    aria-labelledby='thumb-dialog-title'
                    disableBackdropClick
                    open={this.state.open}
                    onClose={this.handleClose}
                    fullWidth='true'
                    maxWidth='lg'
                    fullScreen
                >
                    <Paper square className={classes.popupHeader}>
                        <IconButton color='inherit' onClick={this.handleClose} aria-label='Close'>
                            <Icon>close</Icon>
                        </IconButton>
                        <RadioGroup
                            aria-label='APIThumbnail'
                            name='apiThumbnail'
                            className={classes.group}
                            value={this.state.selectedTab}
                            onChange={this.handleChange}
                        >
                            <FormControlLabel
                                value='upload'
                                control={<Radio color='primary' />}
                                label={(
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.ThumbnailView.upload'
                                        defaultMessage='Upload'
                                    />
                                )}
                            />
                            <FormControlLabel
                                value='remove'
                                control={<Radio color='primary' />}
                                label={(
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.ThumbnailView.remove'
                                        defaultMessage='Remove'
                                    />
                                )}
                            />
                        </RadioGroup>
                    </Paper>

                    <DialogContent>
                        {selectedTab === 'upload' && (
                            <Grid container spacing={4}>
                                <Grid item xs={3}>
                                    <div className={classes.imageContainer}>
                                        <img
                                            className={classes.preview}
                                            src={
                                                file && file.length > 0
                                                    ? windowURL.createObjectURL(file[0])
                                                    : Configurations.app.context
                                                      + '/site/public/images/api/api-default.png'
                                            }
                                            alt='Thumbnail Preview'
                                        />
                                    </div>
                                </Grid>
                                <Grid item xs={9}>
                                    <Dropzone
                                        multiple={false}
                                        accept='image/*'
                                        maxSize={maxSize}
                                        className={classes.dropzone}
                                        activeClassName={classes.acceptDrop}
                                        rejectClassName={classes.rejectDrop}
                                        onDrop={(dropFile) => {
                                            this.onDrop(dropFile);
                                        }}
                                    >
                                        {({ getRootProps, getInputProps, rejectedFiles }) => {
                                            const isFileTooLarge = rejectedFiles.length > 0
                                                && rejectedFiles[0].size > maxSize;
                                            return (
                                                <div {...getRootProps({ style: dropzoneStyles })}>
                                                    <input {...getInputProps()} />
                                                    {isFileTooLarge && (
                                                        <Typography color='error'>
                                                            <FormattedMessage
                                                                id='upload.image.size.error'
                                                                defaultMessage='Uploaded File is too large.
                                                                Maximum file size limit to 1MB'
                                                            />
                                                        </Typography>
                                                    )}
                                                    <div className={classes.dropZoneWrapper}>
                                                        <Icon className={classes.dropIcon}>cloud_upload</Icon>
                                                        <Typography>
                                                            <FormattedMessage
                                                                id='upload.image'
                                                                defaultMessage='Click or drag the image to upload.'
                                                            />
                                                        </Typography>
                                                    </div>
                                                </div>
                                            );
                                        }}
                                    </Dropzone>
                                    <Typography>
                                        <FormattedMessage
                                            id='upload.image.size.info'
                                            defaultMessage='Maximum file size limit to 1MB'
                                        />
                                    </Typography>
                                </Grid>
                            </Grid>
                        )}
                    </DialogContent>
                    <DialogActions className={classes.actionBox}>
                        <Button
                            disabled={this.saveDisableEnable()}
                            variant='contained'
                            color='primary'
                            size='small'
                            onClick={this.handleClick('btnUploadAPIThumb', intl)}
                        >
                            {selectedTab === 'design' && uploading && (
                                <>
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.ThumbnailView.saving.btn'
                                        defaultMessage='Saving'
                                    />
                                    <CircularProgress size={16} />
                                </>
                            )}
                            {selectedTab === 'design' && !uploading && (
                                <FormattedMessage
                                    id='Apis.Listing.components.ImageGenerator.ThumbnailView.save.btn'
                                    defaultMessage='Save'
                                />
                            )}

                            {selectedTab !== 'design' && uploading && (
                                <>
                                    <FormattedMessage
                                        id='Apis.Listing.components.ImageGenerator.ThumbnailView.uploading.btn'
                                        defaultMessage='Uploading'
                                    />
                                    <CircularProgress size={16} />
                                </>
                            )}
                            {selectedTab === 'upload' && !uploading && (
                                <FormattedMessage
                                    id='Apis.Listing.components.ImageGenerator.ThumbnailView.upload.btn'
                                    defaultMessage='Upload'
                                />
                            )}
                            {selectedTab === 'remove' && !uploading && (
                                <FormattedMessage
                                    id='Apis.Listing.components.ImageGenerator.ThumbnailView.remove.btn'
                                    defaultMessage='Remove'
                                />
                            )}
                        </Button>

                        <Button variant='contained' size='small' onClick={this.handleClose}>
                            <FormattedMessage
                                id='Apis.Listing.components.ImageGenerator.ThumbnailView.cancel.btn'
                                defaultMessage='CANCEL'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </>
        );
    }
}

ThumbnailView.defaultProps = {
    height: 190,
    width: 250,
    isEditable: false,
    setImageUpdate: () => {},
};

ThumbnailView.propTypes = {
    api: PropTypes.shape({}).isRequired,
    setImageUpdate: PropTypes.shape({}),
    imageUpdate: PropTypes.number.isRequired,
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    isEditable: PropTypes.bool,
    intl: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default injectIntl(withAPI(withStyles(styles, { withTheme: true })(ThumbnailView)));
