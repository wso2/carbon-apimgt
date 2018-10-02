import React, { Component } from 'react';
import { ButtonBase, Button, Dialog, DialogActions, DialogTitle, Typography, Slide } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import CancelIcon from '@material-ui/icons/Cancel';
import Card from '@material-ui/core/Card';
import CardMedia from '@material-ui/core/CardMedia';
import EditIcon from '@material-ui/icons/Edit';
import PropTypes from 'prop-types';
import UploadIcon from '@material-ui/icons/Send';
import green from '@material-ui/core/colors/green';
import red from '@material-ui/core/colors/red';

import ImageGenerator from './ImageGenerator';

const styles = theme => ({
    card: {
        maxWidth: 80,
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
    media: {
        // ⚠️ object-fit is not supported by IE11.
        objectFit: 'cover',
    },
    paper: {
        position: 'absolute',
        width: theme.spacing.unit * 100,
        backgroundColor: theme.palette.background.paper,
        boxShadow: theme.shadows[5],
        padding: theme.spacing.unit * 4,
        top: '25%',
        left: `calc(50% - ${(theme.spacing.unit * 100) / 2}px)`,
    },
});

/**
 * Slide up transition for modal
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
     * @param {any} props Component properties
     */
    constructor(props) {
        super(props);
        this.state = { open: false };
        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }
    /**
     * @param {SyntheticEvent} e React event object
     */
    handleClick(e) {
        if (e.target.id === 'btnEditAPIThumb') {
            this.setState({ open: true });
        }
    }
    /**
     * Handle modal close event
     */
    handleClose() {
        this.setState({ open: false });
    }
    /**
     * @inheritdoc
     */
    render() {
        const { api, classes } = this.props;
        let view;

        if (api.thumb) {
            view = (
                <Card className={classes.card}>
                    <CardMedia
                        component='img'
                        className={classes.media}
                        height={80}
                        image={api.thumb}
                        title='API Thumbnail'
                    />
                </Card>
            );
        } else {
            view = <ImageGenerator width={80} height={80} api={api} />;
        }

        return (
            <div>
                <ButtonBase
                    focusRipple
                    className={classes.thumb}
                    onClick={this.handleClick}
                    id='btnEditAPIThumb'
                >
                    {view}
                    <span className={classes.thumbBackdrop} />
                    <span className={classes.thumbButton}>
                        <Typography
                            component='span'
                            variant='subheading'
                            color='inherit'
                        >
                            <EditIcon />
                        </Typography>
                    </span>
                </ButtonBase>

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
                    <DialogActions>
                        <Button variant='outlined' color='primary' onClick={this.handleClick} id='btnUploadAPIThumb' >
                            <UploadIcon />
                            Upload
                        </Button>
                        <Button variant='outlined' onClick={this.handleClose} color='secondary'>
                            <CancelIcon />
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }
}

ThumbnailView.propTypes = {
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ThumbnailView);
