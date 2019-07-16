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
    Typography,
    withStyles,
    ExpansionPanel,
    ExpansionPanelSummary,
    ExpansionPanelDetails,
} from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';

import SuspendTimeoutConfig from './SuspendTimeoutConfig';

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
    heading: {
        flexBasis: '33.33%',
        flexShrink: 0,
    },
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} HTML representation of the component.
 */
function AdvancedEndpointConfig(props) {
    const { classes } = props;
    const [isAdvacedOpen, setAdvancedOpen] = useState(false);

    const openAdvancedConfig = () => {
        setAdvancedOpen(!isAdvacedOpen);
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
                        <FormattedMessage
                            id='Apis.Details.EndpointsNew.AdvancedConfig.AdvanceEndpointConfig.advanced.configuration'
                            defaultMessage='Advanced Configuration'
                        />
                    </Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                    <SuspendTimeoutConfig />
                </ExpansionPanelDetails>
            </ExpansionPanel>
        </div>
    );
}

AdvancedEndpointConfig.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(AdvancedEndpointConfig));
