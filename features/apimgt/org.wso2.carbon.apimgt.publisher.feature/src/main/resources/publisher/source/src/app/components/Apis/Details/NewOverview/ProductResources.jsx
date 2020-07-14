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
import { withStyles, withTheme } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

/**
 * Render method base.
 * @param {*} props
 */
function RenderMethodBase(props) {
    const { theme, method } = props;
    const methodLower = method.toLowerCase();
    let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[methodLower] : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[methodLower]);
    }
    return (
        <Chip
            label={method.toUpperCase()}
            style={{ backgroundColor: chipColor, color: chipTextColor, height: 20 }}
        />
    );
}

RenderMethodBase.propTypes = {
    method: PropTypes.string.isRequired,
    theme: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

const RenderMethod = withTheme(RenderMethodBase);

const styles = {
    root: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'left',
        marginBottom: 10,
        padding: 5,
    },
    heading: {
        marginRight: 20,
        paddingBottom: 10,
    },
    resourceRow: {
        display: 'flex',
        flexDirection: 'row',
        marginLeft: 10,
    },
};

/**
 * Component to show api product resources in overview tab
 */
class ProductResources extends React.PureComponent {
    /**
     * @inheritDoc
     */
    render() {
        const { classes, parentClasses, api } = this.props;
        const apiResources = api.apis;
        return (
            <>
                <div className={parentClasses.titleWrapper} style={{ margin: '20px 0 0' }}>
                    <Typography variant='h5' component='h3' className={parentClasses.title}>
                        <FormattedMessage
                            id='Apis.Details.Overview.ProductResources.resources'
                            defaultMessage='Resources'
                        />
                    </Typography>
                </div>
                <div className={classes.root}>
                    <div className={classes.contentWrapper}>
                        {Object.keys(apiResources).map((key) => {
                            const resource = apiResources[key];
                            return (
                                <div className={classes.root}>
                                    <Typography className={classes.heading} variant='body1'>
                                        {resource.name + ' : ' + resource.version}
                                    </Typography>
                                    {Object.keys(resource.operations).map((innerKey) => {
                                        const operation = (resource.operations)[innerKey];
                                        return (
                                            <div className={classes.resourceRow}>
                                                <Typography className={classes.heading} variant='body1'>
                                                    {operation.target}
                                                </Typography>
                                                <RenderMethod method={operation.verb} />

                                            </div>
                                        );
                                    })}

                                </div>
                            );
                        })}
                    </div>
                </div>
            </>
        );
    }
}
ProductResources.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({ id: PropTypes.string }).isRequired,
};

export default withStyles(styles)(ProductResources);
