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

import React from "react";
import PropTypes from "prop-types";
import {withStyles} from "@material-ui/core/styles";
import Button from "@material-ui/core/Button";
import EditRounded from "@material-ui/icons/EditRounded";
import CloudUploadRounded from "@material-ui/icons/CloudUploadRounded";
import Dialog from "@material-ui/core/Dialog";
import IconButton from "@material-ui/core/IconButton";
import Icon from "@material-ui/core/Icon";
import Paper from "@material-ui/core/Paper";
import {FormattedMessage, injectIntl} from "react-intl";
import ResourceNotFound from "../../../Base/Errors/ResourceNotFound";
import Api from "AppData/api";
import {Progress} from "AppComponents/Shared";
import Typography from "@material-ui/core/Typography";
import Slide from "@material-ui/core/Slide";
import MonacoEditor from "react-monaco-editor";
import SwaggerEditorDrawer from "./SwaggerEditorDrawer";
import yaml from "js-yaml";
import Alert from 'AppComponents/Shared/Alert';

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
});
class APIDefinition extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            openEditor: false,

        };
        this.api = props.api;
        this.api_uuid = props.api.id;
        this.openEditor = this.openEditor.bind(this);
        this.closeEditor = this.closeEditor.bind(this);
        this.Transition = this.Transition.bind(this);
        this.updateSwaggerContent = this.updateSwaggerContent.bind(this);
        this.updateSwaggerDefinition = this.updateSwaggerDefinition.bind(this);
        this.hasJsonStructure = this.hasJsonStructure.bind(this);
    }

    componentDidMount() {
        const promisedApi = this.api.getSwagger(this.api_uuid);
        promisedApi
            .then((response) => {
                this.setState({swagger: JSON.stringify(response.obj, null, 1)});
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                } else if (status === 401) {
                    this.setState({isAuthorize: false});
                    const params = qs.stringify({reference: this.props.location.pathname});
                    this.props.history.push({pathname: '/login', search: params});
                }
            });
    }

    updateSwaggerContent() {
        const updatedContent = window.localStorage.getItem('swagger-editor-content');
        this.closeEditor();
        this.setState({swagger: updatedContent});
    }

    openEditor() {
        window.localStorage.setItem('swagger-editor-content', this.state.swagger);
        this.setState({openEditor: true});
    }

    closeEditor() {
        window.localStorage.setItem('swagger-editor-content', '');
        this.setState({openEditor: false});
    }

    Transition(props) {
        return <Slide direction='up' {...props} />;
    }
    updateSwaggerContent() {
        const updatedContent = window.localStorage.getItem('swagger-editor-content');
        this.setState({swagger: updatedContent}, () => this.updateSwaggerDefinition());
    }

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
        promise.then(
            (response) => {
                Alert.success('API Deninition Updated Successfully');
                this.closeEditor();
            }
        ).catch((err) => {
            console.debug(err);
            Alert.error('Error while updating the API Definition');
        });
    }

    hasJsonStructure() {
        const swagger = this.state.swagger;
        if (typeof swagger !== 'string') return false;
        try {
            const result = JSON.parse(swagger);
            return Object.prototype.toString.call(result) === '[object Object]'
                || Array.isArray(result);
        } catch (err) {
            return false;
        }
    }

    render() {

        const {swagger} = this.state;
        const {classes} = this.props;

        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
            handleMouseWheel: true,
            scrollbar: {
                handleMouseWheel: true
            }
        };
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage}/>;
        }
        if (!swagger) {
            <Progress />
        }

        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        API Definition
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.openEditor}>
                        <EditRounded className={classes.buttonIcon}/>
                        Edit
                    </Button>
                    <Button size='small' className={classes.button}>
                        <CloudUploadRounded className={classes.buttonIcon}/>
                        Import API Definition
                    </Button>
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
                <Dialog fullScreen open={this.state.openEditor} onClose={this.closeEditor}
                        TransitionComponent={this.Transition}>
                    <Paper square className={classes.popupHeader}>
                        <IconButton color='inherit' onClick={this.closeEditor} aria-label='Close'>
                            <Icon>close</Icon>
                        </IconButton>
                        <Button className={classes.button} variant='contained' color='primary'
                                onClick={this.updateSwaggerContent}>
                            <FormattedMessage
                                id='documents.swagger.editor.update.content'
                                defaultMessage='Update Content'
                            />
                        </Button>
                    </Paper>
                    <SwaggerEditorDrawer />
                </Dialog>
            </div>
        );
    }
}
APIDefinition.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    state: PropTypes.shape({}).isRequired,
};
export default withStyles(styles)(APIDefinition);
