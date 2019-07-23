/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useState } from 'react';
import {
    Button,
    Collapse,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    ExpansionPanel,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    FormControl,
    FormControlLabel,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Switch,
    Typography,
    withStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl } from 'react-intl';
import EndpointSecurity from './GeneralConfiguration/EndpointSecurity';
import Certificates from './GeneralConfiguration/Certificates';
import LoadBalanceConfig from './LoadBalanceConfig';

const styles = theme => ({
    endpointTypeSelect: {
        width: '50%',
    },
    configHeaderContainer: {
        display: 'flex',
        justifyContent: 'space-between',
    },
    secondaryHeading: {
        fontSize: theme.typography.pxToRem(15),
        color: theme.palette.text.secondary,
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        flexBasis: '33.33%',
        flexShrink: 0,
        fontWeight: '900',
    },
    endpointConfigSection: {
        padding: '10px',
    },
});

const endpointTypes = [{ key: 'http', value: 'HTTP/REST Endpoint' },
    { key: 'address', value: 'HTTP/SOAP Endpoint' }];

function GeneralConfiguration(props) {
    const {
        epConfig,
        endpointSecurityInfo,
        handleToggleEndpointSecurity,
        handleEndpointSecurityChange,
        handleEndpointTypeSelect,
        endpointType,
        classes,
    } = props;
    const [isConfigExpanded, setConfigExpand] = useState(true);
    const [isLBConfigOpen, setLBConfigOpen] = useState(false);

    useEffect(() => {
    });

    return (
        <div>
            <ExpansionPanel expanded={isConfigExpanded} onChange={() => setConfigExpand(!isConfigExpanded)}>
                <ExpansionPanelSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls='panel1bh-content'
                    id='panel1bh-header'
                    className={classes.configHeaderContainer}
                >
                    <Typography className={classes.heading}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.GeneralConfiguration.general.configuration.heading'
                            defaultMessage='GeneralConfiguration'
                        />
                    </Typography>
                    <Typography className={classes.secondaryHeading}>
                        Endpoint Type: {epConfig.endpoint_type}, Endpoint Security: , Certificates: None
                    </Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                    <Grid container direction='row'>
                        <Grid item xs container className={classes.endpointConfigSection} direction='column'>
                            <FormControl className={classes.endpointTypeSelect}>
                                <InputLabel htmlFor='endpoint-type-select'>
                                    <FormattedMessage
                                        id='Apis.Details.EndpointsNew.EndpointOverview.endpointType'
                                        defaultMessage='Endpoint Type'
                                    />
                                </InputLabel>
                                <Select
                                    value={endpointType.key}
                                    onChange={handleEndpointTypeSelect}
                                    inputProps={{
                                        name: 'key',
                                        id: 'endpoint-type-select',
                                    }}
                                >
                                    {endpointTypes.map((type) => {
                                        return (<MenuItem value={type.key}>{type.value}</MenuItem>);
                                    })}
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs className={classes.endpointConfigSection}>
                            <FormControlLabel
                                value='start'
                                checked={endpointSecurityInfo !== null}
                                control={<Switch color='primary' />}
                                label={<FormattedMessage
                                    id='Apis.Details.EndpointsNew.EndpointOverview.endpoint.security.enable.switch'
                                    defaultMessage='Endpoint Security'
                                />}
                                labelPlacement='start'
                                onChange={handleToggleEndpointSecurity}
                            />
                            <Collapse in={endpointSecurityInfo !== null}>
                                <EndpointSecurity
                                    securityInfo={endpointSecurityInfo}
                                    onChangeEndpointAuth={handleEndpointSecurityChange}
                                />
                            </Collapse>
                        </Grid>
                        <Grid item xs className={classes.endpointConfigSection}>
                            <Certificates />
                        </Grid>
                    </Grid>
                </ExpansionPanelDetails>
            </ExpansionPanel>
            <Dialog open={isLBConfigOpen}>
                <DialogTitle>
                    <Typography className={classes.dialogHeader}>
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.EndpointListing.loadbalance.endpoint.configuration'
                            defaultMessage='Load Balance Endpoint Configuration'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <LoadBalanceConfig />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setLBConfigOpen(false)} color='primary'>
                        <FormattedMessage id='Apis.Details.EndpointsNew.EndpointListing.close' defaultMessage='Close' />
                    </Button>
                    <Button onClick={() => setLBConfigOpen(false)} color='primary' autoFocus>
                        <FormattedMessage id='Apis.Details.EndpointsNew.EndpointListing.save' defaultMessage='Save' />
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}

GeneralConfiguration.propTypes = {
    epConfig: PropTypes.shape({}).isRequired,
    endpointSecurityInfo: PropTypes.shape({}).isRequired,
    handleToggleEndpointSecurity: PropTypes.func.isRequired,
    handleEndpointSecurityChange: PropTypes.func.isRequired,
    handleEndpointTypeSelect: PropTypes.func.isRequired,
    endpointType: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(GeneralConfiguration));
