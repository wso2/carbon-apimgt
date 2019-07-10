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

import React, { useState } from 'react';
import {
    Dialog,
    Button,
    DialogContent,
    DialogActions,
    Typography,
    withStyles,
    IconButton,
    Paper,
    Icon,
    ExpansionPanel,
    ExpansionPanelSummary,
    ExpansionPanelDetails,
    List,
    ListItem,
    ListItemText,
    Divider,
} from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Certificates from './AdvancedConfig/Certificates';
import EndpointSecurity from './AdvancedConfig/EndpointSecurity';
import SuspendTimeoutConfig from './AdvancedConfig/SuspendTimeoutConfig';


const configTitles = [
    {
        name: 'certificate',
        title: 'Endpoint Certificates',
    },
    {
        name: 'security',
        title: 'Endpoint Security',
    },
    {
        name: 'suspend',
        title: 'Endpoint Suspension and Timeouts',
    },
];

const styles = theme => ({
    advancedConfigDialog: {
        width: '30%',
    },
    advancedConfigList: {
        width: '100%',
    },
    advancedConfigPanel: {
        boxShadow: 'none',
        padding: '0px',
    },
    popupHeader: {
        display: 'flex',
    },
    dialogHeader: {
        padding: theme.spacing.unit,
        fontSize: '100%',
    },
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} HTML representation of the component.
 */
function AdvancedEndpointConfig(props) {
    const { classes } = props;
    const [option, setOption] = useState('');
    const [isAdvacedOpen, setAdvancedOpen] = useState(false);
    const [isOpenAdvanceConfigDialog, setOpenAdvanceConfigDialog] = useState(false);
    const [dialogTitle, setTitle] = useState('');

    const getTitle = (configType) => {
        return configTitles.filter((op) => { return (op.name === configType); });
    };

    const closeConfig = () => {
        setOpenAdvanceConfigDialog(false);
    };

    const openAdvancedConfig = () => {
        setAdvancedOpen(!isAdvacedOpen);
    };

    const openAdvancedConfigDialog = (configOption) => {
        setOpenAdvanceConfigDialog(true);
        setOption(configOption);
        setTitle(getTitle(configOption)[0].title);
    };

    const handleClose = () => {
        setOpenAdvanceConfigDialog(false);
    };

    const getConfigurationContent = (configType) => {
        if (configType === 'security') {
            return (<EndpointSecurity />);
        }
        if (configType === 'certificate') {
            return (<Certificates />);
        }
        if (configType === 'suspend') {
            return (<SuspendTimeoutConfig />);
        }
        return (<div />);
    };

    return (
        <div>
            <ExpansionPanel
                expanded={isAdvacedOpen}
                onChange={openAdvancedConfig}
                className={classes.advancedConfigPanel}
            >
                <ExpansionPanelSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls='panel1bh-content'
                    id='panel1bh-header'
                >
                    <Typography className={classes.heading}>
                        <FormattedMessage id='Ádvanced.Configuration' defaultMessage='Advanced Configuration' />
                    </Typography>
                    <Typography className={classes.secondaryHeading}>
                        <FormattedMessage
                            id='Security.Certificate.Suspend'
                            defaultMessage='Security Certificates Suspend'
                        />
                    </Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                    <List className={classes.advancedConfigList}>
                        <ListItem button onClick={() => openAdvancedConfigDialog('security')}>
                            <ListItemText primary='Security' secondary='None' />
                        </ListItem>
                        <Divider />
                        <ListItem button onClick={() => openAdvancedConfigDialog('certificate')}>
                            <ListItemText primary='Certificates' secondary='None' />
                        </ListItem>
                        <Divider />
                        <ListItem button onClick={() => openAdvancedConfigDialog('suspend')}>
                            <ListItemText primary='Suspends and Timeouts' secondary='None' />
                        </ListItem>
                        <Divider />
                    </List>
                </ExpansionPanelDetails>
            </ExpansionPanel>
            <Dialog open={isOpenAdvanceConfigDialog}>
                <Paper square className={classes.popupHeader}>
                    <IconButton
                        className={classes.button}
                        color='inherit'
                        onClick={closeConfig}
                        aria-label='Close'
                    >
                        <Icon>close</Icon>
                    </IconButton>
                    <Typography className={classes.dialogHeader}>{dialogTitle}</Typography>
                </Paper>
                <DialogContent>
                    <div>
                        {getConfigurationContent(option)}
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color='primary'>
                        <FormattedMessage id='Cancel' defaultMessage='Cancel' />
                    </Button>
                    <Button onClick={handleClose} color='primary'>
                        <FormattedMessage id='Ok' defaultMessage='Ok' />
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}

AdvancedEndpointConfig.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(AdvancedEndpointConfig));
