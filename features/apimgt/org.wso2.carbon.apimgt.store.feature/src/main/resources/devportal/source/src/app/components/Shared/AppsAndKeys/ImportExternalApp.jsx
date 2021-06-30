/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import Typography from '@material-ui/core/Typography';
import DialogTitle from '@material-ui/core/DialogTitle';
import PlayForWorkIcon from '@material-ui/icons/PlayForWork';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import { FormattedMessage, useIntl } from 'react-intl';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';

function ImportExternalApp(props) {
    const {
        consumerKey, consumerSecret, onChange, isUserOwner, key, provideOAuthKeySecret, importDisabled,
    } = props;
    const intl = useIntl();

    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };
    /**
     * Handle onChange of provided consumer key and secret
     *
     * @param event
     */
    function handleChange(event) {
        if (onChange) {
            onChange(event);
        }
    }
    return (
        <>
            <Button
                variant="outlined"
                color="secondary"
                startIcon={<PlayForWorkIcon />}
                onClick={handleClickOpen}
                disabled={importDisabled}
            >
                <FormattedMessage
                    defaultMessage='Provide Existing OAuth Keys'
                    id='Shared.AppsAndKeys.ImportExternalApp.provide.oauth'
                />
            </Button>
            <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
                <DialogTitle id="form-dialog-title"><FormattedMessage
                    defaultMessage='Provide Existing OAuth Keys'
                    id='Shared.AppsAndKeys.ImportExternalApp.provide.oauth'
                /></DialogTitle>
                <DialogContent>

                    <Grid container spacing={3} direction='column'>
                        <Grid item xs={12}>
                            <TextField
                                id='provided-consumer-key'
                                name='providedConsumerKey'
                                label={intl.formatMessage({
                                    defaultMessage: 'Consumer Key',
                                    id: 'Shared.AppsAndKeys.ImportExternalApp.consumer.key',
                                })}
                                value={consumerKey}
                                onChange={e => handleChange(e)}
                                margin='normal'
                                fullWidth
                                disabled={!isUserOwner}
                                variant='outlined'
                            />
                            <FormControl>
                                <FormHelperText id='consumer-key-helper-text'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ImportExternalApp.consumer.key.title'
                                        defaultMessage='Consumer Key of the OAuth application'
                                    />
                                </FormHelperText>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                id='provided-consumer-secret'
                                name='providedConsumerSecret'
                                label={intl.formatMessage({
                                    defaultMessage: 'Consumer Secret',
                                    id: 'Shared.AppsAndKeys.ImportExternalApp.consumer.secret',
                                })}
                                value={consumerSecret}
                                onChange={e => handleChange(e)}
                                margin='normal'
                                fullWidth
                                disabled={!isUserOwner}
                                variant='outlined'
                            />
                            <FormControl>
                                <FormHelperText id='consumer-secret-helper-text'>
                                    <FormattedMessage
                                        id='Shared.AppsAndKeys.ImportExternalApp.consumer.secret.of.application'
                                        defaultMessage='Consumer Secret of the OAuth application'
                                    />
                                </FormHelperText>
                            </FormControl>
                        </Grid>
                    </Grid>

                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="primary">
                        <FormattedMessage
                            defaultMessage='Cancel'
                            id='Shared.AppsAndKeys.ImportExternalApp.cancel'
                        />
                    </Button>
                    <ScopeValidation
                        resourcePath={resourcePaths.APPLICATION_GENERATE_KEYS}
                        resourceMethod={resourceMethods.POST}
                    >
                        {!isUserOwner ? (
                            <>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() => provideOAuthKeySecret()}
                                    disabled={!isUserOwner}
                                >
                                    {
                                        key
                                            ? (
                                                <FormattedMessage
                                                    defaultMessage='Update'
                                                    id='Shared.AppsAndKeys.ImportExternalApp.provide.
                                                    oauth.button.update'
                                                />
                                            )
                                            : (
                                                <FormattedMessage
                                                    defaultMessage='Provide'
                                                    id='Shared.AppsAndKeys.ImportExternalApp.provide.oauth.button.provide'
                                                />
                                            )
                                    }
                                </Button>
                                <Typography variant='caption'>
                                    <FormattedMessage
                                        defaultMessage='Only owner can provide keys'
                                        id='Shared.AppsAndKeys.ImportExternalApp.key.provide.user.owner'
                                    />
                                </Typography>
                            </>
                        ) : (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() => provideOAuthKeySecret()}
                                >
                                    {
                                        key
                                            ? (
                                                <FormattedMessage
                                                    defaultMessage='Update'
                                                    id='Shared.AppsAndKeys.ImportExternalApp.provide.oauth.button.update'
                                                />
                                            )
                                            : (
                                                <FormattedMessage
                                                    defaultMessage='Provide'
                                                    id='Shared.AppsAndKeys.ImportExternalApp.provide.oauth.button.provide'
                                                />
                                            )
                                    }
                                </Button>
                            )}
                    </ScopeValidation>
                </DialogActions>
            </Dialog>
        </>
    );
}
ImportExternalApp.propTypes = {
    intl: PropTypes.shape({}).isRequired,
    onChange: PropTypes.func.isRequired,
    consumerKey: PropTypes.string,
    consumerSecret: PropTypes.string,
    isUserOwner: PropTypes.string,
    key: PropTypes.shape({}).isRequired,
    provideOAuthKeySecret: PropTypes.func.isRequired,
    importDisabled: PropTypes.bool,
};

ImportExternalApp.defaultProps = {
    consumerKey: '',
    consumerSecret: '',
    isUserOwner: false,
    importDisabled: false,
};

export default ImportExternalApp;




