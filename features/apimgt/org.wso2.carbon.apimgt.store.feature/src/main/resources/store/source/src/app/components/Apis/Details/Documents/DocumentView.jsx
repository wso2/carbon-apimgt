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
import Dialog from '@material-ui/core/Dialog';
import { ListItemText } from '@material-ui/core/List';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import Avatar from '@material-ui/core/Avatar';
import InsertDriveFile from '@material-ui/icons/InsertDriveFile';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    appBar: {
        position: 'relative',
    },
    flex: {
        flex: 1,
    },
    docContent: {
        padding: 20,
    },
    caption: {
        color: theme.palette.text.primary,
    },
    headline: {
        color: theme.palette.text.primary,
    },
    summary: {
        textDecoration: 'none',
        display: 'flex',
        paddingLeft: 0,
        cursor: 'pointer',
    },
});

function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 *
 *
 * @class DocumentView
 * @extends {React.Component}
 */
class DocumentView extends React.Component {
    state = {
        open: false,
    };

    /**
     *
     *
     * @memberof DocumentView
     */
    handleClickOpen = () => {
        this.setState({ open: true });
    };

    /**
     *
     *
     * @memberof DocumentView
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     *
     *
     * @returns
     * @memberof DocumentView
     */
    render() {
        const { classes, doc, truncateSummary } = this.props;
        return (
            <div>
                <a onClick={this.handleClickOpen} className={classes.summary}>
                    <Avatar>
                        <InsertDriveFile />
                    </Avatar>
                    <ListItemText primary={doc.name} secondary={truncateSummary} />
                </a>
                <Dialog fullScreen open={this.state.open} onClose={this.handleClose} transition={Transition}>
                    <AppBar className={classes.appBar}>
                        <Toolbar>
                            <IconButton color='inherit' onClick={this.handleClose} aria-label='Close'>
                                <CloseIcon />
                            </IconButton>
                            <div className={classes.titleWrapper}>
                                <Typography variant='headline' gutterBottom className={classes.headline}>
                                    {doc.name}
                                </Typography>
                                <Typography variant='caption' gutterBottom align='left' className={classes.caption}>
                                    {doc.type}
                                </Typography>
                            </div>
                        </Toolbar>
                    </AppBar>
                    <Typography gutterBottom noWrap className={classes.docContent}>
                        {doc.summary}
                    </Typography>
                </Dialog>
            </div>
        );
    }
}

DocumentView.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(DocumentView);
