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
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import ApplicationCreate from '../../Shared/AppsAndKeys/ApplicationCreate';
import Alert from '../../Shared/Alert';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    appBar: {
        position: 'relative',
        backgroundColor: theme.palette.background.appBar,
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    flex: {
        flex: 1,
    },
    button: {
        marginRight: theme.spacing.unit * 2,
    },
    buttonWrapper: {
        paddingLeft: theme.spacing.unit * 7,
    },
    createFormWrapper: {
        paddingLeft: theme.spacing.unit * 5,
    },
});
/**
 *
 *
 * @param {*} props
 * @returns
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 *
 *
 * @class NewApp
 * @extends {React.Component}
 */
class NewApp extends React.Component {
    state = {
        open: false,
    };

    /**
     *
     *
     * @memberof NewApp
     */
    handleClickOpen = () => {
        this.setState({ open: true });
    };

    /**
     *
     *
     * @memberof NewApp
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     *
     *
     * @memberof NewApp
     */
    saveApplication = () => {
        this.applicationCreate.handleSubmit()
            .then((response) => {
                const appCreated = JSON.parse(response.data);
                // Once application loading fixed this need to pass application ID and load app
                console.log('Application created successfully.');
                this.setState({ open: false });
                this.props.updateApps();
            })
            .catch((error) => {
                const { response } = error;
                if (response && response.body) {
                    const message = response.body.description || 'Error while creating the application';
                    Alert.error(message);
                } else {
                    Alert.error(error.message);
                }
                console.error('Error while creating the application');
            });
    };

    /**
     *
     *
     * @returns
     * @memberof NewApp
     */
    render() {
        const { classes } = this.props;
        return (
            <React.Fragment>
                <Button variant='contained' color='primary' className={classes.button} onClick={this.handleClickOpen}>
                    ADD NEW APPLICATION
                </Button>
                <Dialog fullScreen open={this.state.open} onClose={this.handleClose} TransitionComponent={Transition}>
                    <AppBar className={classes.appBar}>
                        <Toolbar>
                            <IconButton color='inherit' onClick={this.handleClose} aria-label='Close'>
                                <CloseIcon />
                            </IconButton>
                            <Typography variant='title' color='inherit' className={classes.flex}>
                                Create New Application
                            </Typography>
                            <Button color='inherit' onClick={this.handleClose}>
                                save
                            </Button>
                        </Toolbar>
                    </AppBar>
                    <div className={classes.createFormWrapper}>
                        <ApplicationCreate innerRef={node => (this.applicationCreate = node)} />
                    </div>
                    <div className={classes.buttonWrapper}>
                        <Button variant='outlined' className={classes.button} onClick={this.handleClose}>
                            Cancel
                        </Button>
                        <Button variant='contained' color='primary' className={classes.button} onClick={this.saveApplication}>
                            ADD NEW APPLICATION
                        </Button>
                    </div>
                </Dialog>
            </React.Fragment>
        );
    }
}

NewApp.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(NewApp);
