import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import green from '@material-ui/core/colors/green';
import red from '@material-ui/core/colors/red';
import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import ButtonBase from '@material-ui/core/ButtonBase';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Grid from '@material-ui/core/Grid';
import GridList from '@material-ui/core/GridList';
import GridListTile from '@material-ui/core/GridListTile';
import GridListTileBar from '@material-ui/core/GridListTileBar';
import Slide from '@material-ui/core/Slide';
import Typography from '@material-ui/core/Typography';
import EditIcon from '@material-ui/icons/Edit';
import CancelIcon from '@material-ui/icons/Cancel';
import UploadIcon from '@material-ui/icons/Send';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

import ImageGenerator from './ImageGenerator';

const windowURL = window.URL || window.webkitURL;
const styles = theme => ({
    acceptDrop: {
        backgroundColor: green[50],
    },
    dropzone: {
        border: '2px dashed rgb(102, 102, 102)',
        borderRadius: '5px',
        cursor: 'pointer',
        height: theme.spacing.unit * 20,
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
        width: '100%',
    },
    media: {
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
    preview: {
        height: theme.spacing.unit * 25,
    },
    rejectDrop: {
        backgroundColor: red[50],
    },
    suppressLinkStyles: {
        textDecoration: 'none',
        color: theme.palette.text.disabled,
    },
    thumb: {
        '&:hover': {
            zIndex: 1,
            '& $thumbBackdrop': {
                opacity: 0.2,
            },
        },
    },
    thumbBackdrop: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        backgroundColor: theme.palette.common.black,
        opacity: 0.4,
    },
    thumbButton: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: theme.palette.common.white,
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
        this.state = { open: false, file: null, thumbnail: null };
        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    /**
     * Load required data for showing the thubnail view
     */
    componentDidMount() {
        const thumbApi = new Api();
        const { api } = this.props;
        thumbApi.getAPIThumbnail(api.id).then((response) => {
            if (response && response.data && response.data.size > 0) {
                const url = windowURL.createObjectURL(response.data);
                this.setState({ thumbnail: url });
            }
        });
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

    /**
     * @param {SyntheticEvent} e React event object
     */
    handleClick(e) {
        if (e.target.id === 'btnEditAPIThumb') {
            this.setState({ open: true });
        } else if (e.target.id === 'btnUploadAPIThumb') {
            const { api } = this.props;
            this.uploadThumbnail(api.id, this.state.file[0]);
        }
    }

    /**
     * Add new thumbnail image to an API
     *
     * @param {String} apiId ID of the API to be updated
     * @param {File} file new thumbnail image file
     */
    uploadThumbnail(apiId, file) {
        const { intl } = this.props;
        if (!apiId || !file) {
            Alert.error(intl.formatMessage({ id: 'thumbnail.validation.error' }));
            return;
        }
        const api = new Api();
        const thumbnailPromise = api.addAPIThumbnail(apiId, file);
        thumbnailPromise
            .then(() => {
                Alert.info(intl.formatMessage({ id: 'thumbnail.upload.success' }));
                this.setState({ open: false, thumbnail: file.preview });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.error(intl.formatMessage({ id: 'thumbnail.upload.error' }));
            });
    }

    /**
     * Handle modal close event
     */
    handleClose() {
        if (this.state.file) {
            windowURL.revokeObjectURL(this.state.file.preview);
        }
        this.setState({ open: false, file: null });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            api, classes, width, height, isEditable,
        } = this.props;
        const { file, thumbnail } = this.state;
        const overviewPath = `/apis/${api.id}/overview`;
        let view;

        if (thumbnail) {
            view = <img height={height} width={width} src={thumbnail} alt='API Thumbnail' className={classes.media} />;
        } else {
            view = <ImageGenerator width={width} height={height} api={api} />;
        }

        return (
            <React.Fragment>
                {isEditable ? (
                    <ButtonBase focusRipple className={classes.thumb} onClick={this.handleClick} id='btnEditAPIThumb'>
                        {view}
                        <span className={classes.thumbBackdrop} />
                        <span className={classes.thumbButton}>
                            <Typography component='span' variant='subheading' color='inherit'>
                                <EditIcon />
                            </Typography>
                        </span>
                    </ButtonBase>
                ) : (
                    <Link className={classes.suppressLinkStyles} to={overviewPath}>
                        {view}
                    </Link>
                )}

                <Dialog
                    TransitionComponent={Transition}
                    aria-labelledby='thumb-dialog-title'
                    disableBackdropClick
                    open={this.state.open}
                    onClose={this.handleClose}
                    fullWidth='true'
                    maxWidth='lg'
                >
                    <DialogTitle id='thumb-dialog-title'>Upload Thumbnail</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={16}>
                            <Grid item xs={9}>
                                <Dropzone
                                    multiple={false}
                                    accept='image/*'
                                    className={classes.dropzone}
                                    activeClassName={classes.acceptDrop}
                                    rejectClassName={classes.rejectDrop}
                                    onDrop={(dropFile) => {
                                        this.onDrop(dropFile);
                                    }}
                                >
                                    <Typography component='span' variant='title' color='inherit'>
                                        <FormattedMessage
                                            id='drop.image.message'
                                            defaultMessage='Drop your image or click the box to '
                                        />
                                    </Typography>
                                </Dropzone>
                            </Grid>
                            <Grid item xs={3}>
                                <GridList cellHeight='auto' className={classes.gridList} cols='1'>
                                    <GridListTile key='tileImage' cols='1'>
                                        <img
                                            className={classes.preview}
                                            src={
                                                file && file.length > 0
                                                    ? file[0].preview
                                                    : '/publisher-new/site/public/images/api/api-default.png'
                                            }
                                            alt='Thumbnail Preview'
                                        />
                                        <GridListTileBar title='Preview' />
                                    </GridListTile>
                                </GridList>
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button color='primary' onClick={this.handleClick} id='btnUploadAPIThumb'>
                            <UploadIcon />
                            <FormattedMessage id='upload.btn' defaultMessage='UPLOAD' />
                        </Button>
                        <Button onClick={this.handleClose} color='secondary'>
                            <CancelIcon />
                            <FormattedMessage id='cancel.btn' defaultMessage='CANCEL' />
                        </Button>
                    </DialogActions>
                </Dialog>
            </React.Fragment>
        );
    }
}

ThumbnailView.defaultProps = {
    height: 190,
    width: 250,
    isEditable: false,
};

ThumbnailView.propTypes = {
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    isEditable: PropTypes.bool,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(ThumbnailView));
