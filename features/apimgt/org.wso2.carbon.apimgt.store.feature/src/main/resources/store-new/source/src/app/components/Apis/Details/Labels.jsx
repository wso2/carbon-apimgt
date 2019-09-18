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
import Grid from '@material-ui/core/Grid';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import LabelIcon from '@material-ui/icons/Label';
import { ApiContext } from './ApiContext';

const styles = theme => ({
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconEven: {
        color: theme.palette.secondary.light,
        width: theme.spacing.unit * 3,
    },
    iconOdd: {
        color: theme.palette.secondary.main,
        width: theme.spacing.unit * 3,
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
            marginTop: theme.spacing.unit * 3,
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
});

class Labels extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            prodUrlCopied: false,
            epUrl: '',
        };
    }

    onCopy = name => () => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = function () {
            that.setState({
                [elementName]: false,
            });
        };
        setTimeout(caller, 4000);
    };

    render() {
        const { api } = this.context;
        const { classes } = this.props;
        const { prodUrlCopied, epUrl } = this.state;

        return (
            <Grid container spacing={16} item xs={12}>
                {api.labels.map((endpoint) => {
                    return (
                        <Grid key={endpoint} item xs={12}>
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
                                                {endpoint.name}
                                            </Typography>
                                        </span>
                                    </div>
                                </ExpansionPanelSummary>
                                <ExpansionPanelDetails>
                                    <Grid container item xs={12} spacing={16}>
                                        {/* {(endpoint.accessUrls == null) && ( */}
                                        <Typography className={classes.heading}>
                                            <FormattedMessage
                                                id='Apis.Details.InfoBar.gateway.urls'
                                                defaultMessage='Microgateway URLs'
                                            />
                                        </Typography>
                                        {endpoint.accessUrls.map(row => (
                                            <Grid item xs={12}>
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
                                            </Grid>
                                        ))} 
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

export default withStyles(styles)(Labels);
