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
import Grid from '@material-ui/core/Grid';
import Switch from '@material-ui/core/Switch';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import { FormattedMessage } from 'react-intl';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import AuthManager from 'AppData/AuthManager';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default class SchemaValidation extends React.Component {
    /**
     *Creates an instance of SchemaValidation.
     * @param {*} props
     * @memberof SchemaValidation
     */
    constructor(props) {
        super(props);
        this.state = { isOpen: false };
        this.setIsOpen = this.setIsOpen.bind(this);
        this.isNotCreator = AuthManager.isNotCreator();
    }

    /**
     *
     * @inheritdoc
     * @param {Object} prevProps
     * @param {Object} prevState
     * @memberof SchemaValidation
     */
    componentDidUpdate(prevProps) {
        const { api } = this.props;
        /*
        This could have been done using hooks too, But at the moment it requires `useRef` hook to get the previous props
        Hence using class component and its `componentDidUpdate` life cycle method to open the caution dialog box
        For more info: https://reactjs.org/docs/hooks-faq.html#how-to-get-the-previous-props-or-state
         */
        if (!prevProps.api.enableSchemaValidation && api.enableSchemaValidation) {
            this.setIsOpen(true);
        }
    }
    /**
     *
     * Set isOpen state of the dialog box which shows the caution message when enabling schema validation
     * @param {Boolean} isOpen Should dialog box is open or not
     * @memberof SchemaValidation
     */
    setIsOpen(isOpen) {
        this.setState({ isOpen });
    }

    /**
     *
     * @inheritdoc
     * @param {*} props
     * @returns
     * @memberof SchemaValidation
     */
    render() {
        const { api, configDispatcher } = this.props;
        const { isOpen } = this.state;

        return (
            <Grid container spacing={1} alignItems='flex-start'>
                <Grid item>
                    <FormControl component='fieldset' style={{ marginTop: 20 }}>
                        <FormLabel component='legend'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.schema.validation'
                                defaultMessage='Schema validation'
                            />
                        </FormLabel>
                        <FormControlLabel
                            control={
                                <Switch
                                    disabled={this.isNotCreator}
                                    checked={
                                        api.enableSchemaValidation === undefined ? false : api.enableSchemaValidation
                                    }
                                    onChange={({ target: { checked } }) =>
                                        configDispatcher({
                                            action: 'enableSchemaValidation',
                                            value: checked,
                                        })
                                    }
                                    color='primary'
                                />
                            }
                        />
                    </FormControl>
                </Grid>
                <Grid item>
                    <Tooltip
                        title={
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.schema.validation.tooltip'
                                defaultMessage={'Enable the request and response ' +
                                'validation against the OpenAPI definition'}
                            />
                        }
                        aria-label='Schema Validation'
                        placement='right-end'
                        interactive
                        style={{ marginTop: 20 }}
                    >
                        <HelpOutline />
                    </Tooltip>
                </Grid>
                <Grid item>
                    <Dialog
                        open={isOpen}
                        onClose={() => this.setIsOpen(false)}
                        aria-labelledby='alert-dialog-title'
                        aria-describedby='alert-dialog-description'
                    >
                        <DialogTitle id='alert-dialog-title'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.SchemaValidation.title'
                                defaultMessage='Caution!'
                            />
                        </DialogTitle>
                        <DialogContent>
                            <DialogContentText id='alert-dialog-description'>
                                <Typography variant='subtitle1' display='block' gutterBottom>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.SchemaValidation.description'
                                        defaultMessage={
                                            'Enabling JSON schema validation will cause to build the' +
                                        ' payload in every requests and responses. This will have an impact ' +
                                        'on the round trip time of an API request!'
                                        }
                                    />
                                </Typography>
                                <Typography variant='subtitle2' display='block' gutterBottom>
                                    <b>
                                        <FormattedMessage
                                            id={'Apis.Details.Configuration.components.SchemaValidation' +
                                            '.description.question'}
                                            defaultMessage='Do you want  to enable schema validation?'
                                        />
                                    </b>
                                </Typography>
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            <Button color='primary' onClick={() => this.setIsOpen(false)}>
                                Yes
                            </Button>
                            <Button
                                onClick={() => {
                                    this.setIsOpen(false);
                                    configDispatcher({
                                        action: 'enableSchemaValidation',
                                        value: false,
                                    });
                                }}
                            >
                                No
                            </Button>
                        </DialogActions>
                    </Dialog>
                </Grid>
            </Grid>
        );
    }
}

SchemaValidation.propTypes = {
    api: PropTypes.shape({ enableSchemaValidation: PropTypes.bool }).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
