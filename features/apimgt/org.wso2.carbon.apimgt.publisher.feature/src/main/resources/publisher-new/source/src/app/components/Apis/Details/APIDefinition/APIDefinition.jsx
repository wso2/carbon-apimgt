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
import EditRounded from '@material-ui/icons/EditRounded';
import CloudUploadRounded from '@material-ui/icons/CloudUploadRounded';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import MonacoEditor from 'react-monaco-editor';
import yaml from 'js-yaml';
import Alert from 'AppComponents/Shared/Alert';
import Dropzone from 'react-dropzone';
import qs from 'qs';

import SwaggerEditorDrawer from './SwaggerEditorDrawer';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

const styles = theme => ({
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    swaggerEditorWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    buttonIcon: {
        marginRight: 10,
    },
    dropzone: {
        border: 'none',
        cursor: 'pointer',
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
    },
});
/**
 * This component holds the functionality of viewing the api definition content of an api. The initial view is a
 * read-only representation of the api definition file.
 * Users can either edit the content by clicking the 'Edit' button or upload a new api definition file by clicking
 * 'Import API Definition'.
 * */
class APIDefinition extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            openEditor: false,
        };
        this.api = props.api;
        this.openEditor = this.openEditor.bind(this);
        this.closeEditor = this.closeEditor.bind(this);
        this.transition = this.transition.bind(this);
        this.updateSwaggerContent = this.updateSwaggerContent.bind(this);
        this.updateSwaggerDefinition = this.updateSwaggerDefinition.bind(this);
        this.hasJsonStructure = this.hasJsonStructure.bind(this);
        this.onDrop = this.onDrop.bind(this);
        this.handleNo = this.handleNo.bind(this);
        this.handleOk = this.handleOk.bind(this);
    }

    componentDidMount() {
        const promisedApi = this.api.getSwagger(this.api.id);
        const { location } = this.props;
        promisedApi
            .then((response) => {
                this.setState({ swagger: JSON.stringify(response.obj, null, 1) });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    const params = qs.stringify({ reference: location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }


    /**
     * Handles the file upload.
     * */
    onDrop(file) {
        if (file[0]) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const content = e.target.result;
                this.setState({ swagger: content }, () => {
                    this.updateSwaggerDefinition();
                });
            };
            reader.readAsText(file[0]);
        } else {
            Alert.error('Unsupported file type.');
        }
    }

    /**
     * Checks whether the swagger content is json type.
     * */
    hasJsonStructure() {
        const { swagger } = this.state.swagger;
        if (typeof swagger !== 'string') return false;
        try {
            const result = JSON.parse(swagger);
            return Object.prototype.toString.call(result) === '[object Object]'
                || Array.isArray(result);
        } catch (err) {
            return false;
        }
    }

    /**
     * Handles the yes button action of the save api definition confirmation dialog box.
    */
    handleOk() {
        const updatedContent = window.localStorage.getItem('swagger-editor-content');
        this.setState({ swagger: updatedContent, openDialog: false }, () => this.updateSwaggerDefinition());
    }

    /**
     * Handles the No button action of the save api definition confirmation dialog box.
    */
    handleNo() {
        this.setState({ openDialog: false });
    }

    /**
     * Method to set the state for opening the swagger editor drawer.
     * Swagger editor loads the definition content from the local storage. Hence we set the swagger content to the
     * local storage.
     * */
    openEditor() {
        window.localStorage.setItem('swagger-editor-content', this.state.swagger);
        this.setState({ openEditor: true });
    }

    /**
     * Sets the state to close the swagger-editor drawer.
     * */
    closeEditor() {
        window.localStorage.setItem('swagger-editor-content', '');
        this.setState({ openEditor: false });
    }

    /**
     * Handles the transition of the drawer.
     * */
    transition(props) {
        return <Slide direction='up' {...props} />;
    }

    /**
     * Updates swagger content in the local storage.
     * */
    updateSwaggerContent() {
        this.setState({ openDialog: true });
    }

    /**
     * Updates swagger definition of the api.
     * */
    updateSwaggerDefinition() {
        let parsedContent = {};
        if (this.hasJsonStructure()) {
            parsedContent = JSON.parse(this.state.swagger);
        } else {
            try {
                parsedContent = yaml.load(this.state.swagger);
            } catch (err) {
                Alert.error('Error while updating the API Definition');
                return;
            }
        }
        const promise = this.api.updateSwagger(parsedContent);
        promise.then((response) => {
            if (response) Alert.success('API Definition Updated Successfully');
        }).catch((err) => {
            console.debug(err);
            Alert.error('Error while updating the API Definition');
        });
    }

    render() {
        const { swagger, openEditor, openDialog } = this.state;
        const { classes } = this.props;

        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
        };
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!swagger) {
            return <Progress />;
        }

        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        API Definition
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.openEditor}>
                        <EditRounded className={classes.buttonIcon} />
                        Edit
                    </Button>
                    <Dropzone
                        multiple={false}
                        className={classes.dropzone}
                        accept={['application/json', 'application/x-yaml']}
                        onDrop={(dropFile) => {
                            this.onDrop(dropFile);
                        }}
                    >
                        <Button size='small' className={classes.button}>
                            <CloudUploadRounded className={classes.buttonIcon} />
                            Import API Definition
                        </Button>
                    </Dropzone>
                </div>
                <div>
                    <MonacoEditor
                        width='100%'
                        height='calc(100vh - 51px)'
                        theme='vs-dark'
                        value={swagger}
                        options={editorOptions}
                    />
                </div>
                <Dialog
                    fullScreen
                    open={openEditor}
                    onClose={this.closeEditor}
                    TransitionComponent={this.transition}
                >
                    <Paper square className={classes.popupHeader}>
                        <IconButton className={classes.button} color='inherit' onClick={this.closeEditor} aria-label='Close'>
                            <Icon>close</Icon>
                        </IconButton>

                        <Button
                            className={classes.button}
                            variant='contained'
                            color='primary'
                            onClick={this.updateSwaggerContent}
                        >
                            <FormattedMessage
                                id='documents.swagger.editor.update.content'
                                defaultMessage='Update Content'
                            />
                        </Button>
                    </Paper>
                    <SwaggerEditorDrawer />
                </Dialog>
                <Dialog
                    open={openDialog}
                    onClose={this.handleNo}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="alert-dialog-title">
                        <Typography align='left'>
                            Save API Definition
                        </Typography>
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            Do you want to save the API Definition?
                            This will affect the existing resources.
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.handleNo} color="secondary">
                            No
                        </Button>
                        <Button onClick={this.handleOk} color="primary" autoFocus>
                            Yes
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }
}
APIDefinition.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.object,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.object,
    }).isRequired,
    resourceNotFountMessage: PropTypes.shape({}).isRequired,
};
export default withStyles(styles)(APIDefinition);
