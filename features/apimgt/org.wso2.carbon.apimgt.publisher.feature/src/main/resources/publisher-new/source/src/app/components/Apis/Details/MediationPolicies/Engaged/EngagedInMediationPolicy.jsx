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
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';

const styles = theme => ({
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    paperRoot: {
        padding: 20,
        marginTop: 20,
    },
    formControl: {
        display: 'flex',
        flexDirection: 'row',
        padding: `${theme.spacing.unit * 2}px 2px`,
    },
    itemWrapper: {
        width: 500,
    },
});
/**
 * The base component of the engaged mediation policy  view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function EngagedInMediationPolicy(props) {
    const { handleInputChange, classes, engagedPolicyFile } = props;

    return (
        <div className={classes.itemWrapper}>
            <TextField
                style={{
                    width: '100%',
                }}
                id='inFlow'
                name='In Flow'
                helperText={<FormattedMessage
                    id='Apis.Details.MediationPolicies.MediationPolicies.in.flow.helper.text'
                    defaultMessage='mediation policy that is engaged in Request Flow'
                />}
                margin='normal'
                type='text'
                onChange={handleInputChange}
                value={engagedPolicyFile.name}
            />
        </div>
    );
}
EngagedInMediationPolicy.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    engagedPlicyFile: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
    engagedPolicyFile: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(EngagedInMediationPolicy);
