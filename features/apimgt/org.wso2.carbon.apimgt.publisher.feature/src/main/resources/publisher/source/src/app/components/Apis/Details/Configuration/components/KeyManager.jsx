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

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { makeStyles } from '@material-ui/core/styles';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Chip from '@material-ui/core/Chip';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import API from 'AppData/api';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(3),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    iconSpace: {
        marginLeft: theme.spacing(0.5),
    },
    actionSpace: {
        margin: '-7px auto',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
    keyManagerSelect: {
        minWidth: 180,
    },
}));

/**
 *
 * KeyManager configuration
 * @param {*} props
 * @returns
 */
export default function KeyManager(props) {
    const [apiFromContext] = useAPI();
    const [keyManagersConfigured, setKeyManagersConfigured] = useState([]);
    const {
        configDispatcher,
        api: { keyManagers },
    } = props;
    const classes = useStyles();
    const handleChange = (event) => {
        configDispatcher({
            action: 'keymanagers',
            value: event.target.value,
        });
    };
    useEffect(() => {
        API.keyManagers().then((response) => setKeyManagersConfigured(response.body.list));
    }, []);

    return (
        <ExpansionPanel className={classes.expansionPanel}>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                <Typography className={classes.subHeading} variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.KeyManager.configuration'
                        defaultMessage='Keymanager Configuration'
                    />
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Keymanager.components.Configuration.tooltip'
                                defaultMessage='If enabled, the Key Managers for the API will be enabled.'
                            />
                        )}
                        aria-label='Key managers'
                        placement='right-end'
                        interactive
                    >
                        <HelpOutline className={classes.iconSpace} />
                    </Tooltip>
                </Typography>
                <FormControlLabel
                    className={classes.actionSpace}
                    control={(
                        <Switch
                            disabled={isRestricted(['apim:api_create'], apiFromContext)}
                            checked={!keyManagers.includes('all')}
                            onChange={({ target: { checked } }) => configDispatcher({
                                action: 'allKeyManagersEnabled',
                                value: checked,
                            })}
                            color='primary'
                        />
                    )}
                />
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                <Grid container>
                    <Grid item md={12}>
                        {!keyManagers.includes('all') && (
                            <Grid container>
                                <Grid item md={12}>
                                    <Select
                                        multiple
                                        value={keyManagers}
                                        className={classes.keyManagerSelect}
                                        onChange={handleChange}
                                        renderValue={(selected) => (
                                            <div className={classes.chips}>
                                                {selected.map((value) => (
                                                    <Chip key={value} label={value} className={classes.chip} />
                                                ))}
                                            </div>
                                        )}
                                    >
                                        {keyManagersConfigured.map((key) => (
                                            <MenuItem key={key.name} value={key.name} disabled={!key.enabled}>
                                                <Checkbox color='primary' checked={keyManagers.includes(key.name)} />
                                                <ListItemText primary={key.name} secondary={key.description} />
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </Grid>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </ExpansionPanelDetails>
        </ExpansionPanel>
    );
}

KeyManager.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
