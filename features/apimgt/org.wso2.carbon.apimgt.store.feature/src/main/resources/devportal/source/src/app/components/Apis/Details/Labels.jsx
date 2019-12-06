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
import Typography from '@material-ui/core/Typography';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import CopyToClipboard from 'react-copy-to-clipboard';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import Icon from '@material-ui/core/Icon';
import API from 'AppData/api';
import Utils from 'AppData/Utils';
import { FormattedMessage, injectIntl } from 'react-intl';
import { ApiContext } from './ApiContext';

const styles = (theme) => ({
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconEven: {
        color: theme.palette.secondary.light,
        width: theme.spacing(3),
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
            marginTop: theme.spacing(3),
        },
    },
    bootstrapInput: {
        borderRadius: 4,
        backgroundColor: theme.palette.common.white,
        border: '1px solid #ced4da',
        padding: '5px 12px',
        width: 350,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        '&:focus': {
            borderColor: '#80bdff',
            boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
        },
    },
    buttonIcon: {
        marginRight: 10,
    },
    iconStyle: {
        cursor: 'grab',
    },
});

class Labels extends React.Component {
    constructor(props) {
        super(props);
        this.apiClient = new API();
        this.state = {
            urlCopied: false,
        };
    }

    onCopy = (name) => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const caller = function () {
            that.setState({ urlCopied: false });
        };
        setTimeout(caller, 2000);
    }

    /**
     * Downloads the Swagger of the api for the provided label
     *
     * @param {string} apiId uuid of the API
     * @param {string} label name of the environment
     */
    downloadSwagger(apiId, label) {
        const promiseSwagger = this.apiClient.getSwaggerByAPIIdAndLabel(apiId, label);
        promiseSwagger
            .then((done) => {
                Utils.downloadFile(done);
            })
            .catch((error) => {
                console.log(error);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Environments.download.wsdl.error',
                    defaultMessage: 'Error downloading the Swagger',
                }));
            });
    }

    render() {
        const { api } = this.context;
        const { classes, intl } = this.props;
        const { urlCopied } = this.state;

        return (
            <Grid container spacing={2} item xs={12}>
                {api.labels.map((label) => {
                    return (
                        <Grid key={label} item xs={12}>
                            <ExpansionPanel>
                                <ExpansionPanelSummary
                                    expandIcon={<Icon>expand_more</Icon>}
                                    aria-controls='panel1a-content'
                                    id='panel1a-header'
                                >
                                    <div className={classes.iconAligner}>
                                        <Icon className={classes.iconEven}>label</Icon>
                                        <span className={classes.iconTextWrapper}>
                                            <Typography className={classes.heading}>
                                                {label.name}
                                            </Typography>
                                        </span>
                                    </div>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container item xs={12} spacing={2}>
                                        <Typography className={classes.heading}>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.microgateway.urls'
                                                defaultMessage='Microgateway URLs'
                                            />
                                        </Typography>
                                        {label.accessUrls.map((row) => (
                                            <Grid item xs={12} key={row}>
                                                <TextField
                                                    defaultValue={row}
                                                    id='bootstrap-input'
                                                    InputProps={{
                                                        disableUnderline: true,
                                                        readOnly: true,
                                                        classes: {
                                                            root: classes.bootstrapRoot,
                                                            input: classes.bootstrapInput,
                                                        },
                                                    }}
                                                    InputLabelProps={{
                                                        shrink: true,
                                                        className: classes.bootstrapFormLabel,
                                                    }}
                                                />
                                                <Tooltip
                                                    title={
                                                        urlCopied
                                                            ? intl.formatMessage({
                                                                defaultMessage: 'Copied',
                                                                id: 'Apis.Details.Labels.copied',
                                                            })
                                                            : intl.formatMessage({
                                                                defaultMessage: 'Copy to clipboard',
                                                                id: 'Apis.Details.Labels.copy.to.clipboard',
                                                            })
                                                    }
                                                    placement='right'
                                                    className={classes.iconStyle}
                                                >
                                                    <CopyToClipboard
                                                        text={row}
                                                        onCopy={() => this.onCopy('urlCopied')}
                                                    >
                                                        <Icon color='secondary'>insert_drive_file</Icon>
                                                    </CopyToClipboard>
                                                </Tooltip>
                                            </Grid>
                                        ))}
                                        {(api.type === 'HTTP' || api.type === 'SOAPTOREST') && (
                                            <Button
                                                size='small'
                                                onClick={
                                                    () => this.downloadSwagger(api.id, label.name)
                                                }
                                            >
                                                <CloudDownloadRounded className={classes.buttonIcon} />
                                                <FormattedMessage
                                                    id='Apis.Details.Environments.download.swagger'
                                                    defaultMessage='Swagger'
                                                />
                                            </Button>
                                        )}
                                    </Grid>
                                </ExpansionPanelDetails>
                            </ExpansionPanel>
                        </Grid>
                    );
                })}
            </Grid>
        );
    }
}

Labels.propTypes = {
    classes: PropTypes.object.isRequired,
};
Labels.contextType = ApiContext;

export default injectIntl(withStyles(styles)(Labels));
