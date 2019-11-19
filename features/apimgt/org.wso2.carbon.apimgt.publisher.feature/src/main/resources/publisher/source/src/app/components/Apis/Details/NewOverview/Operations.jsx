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
import LaunchIcon from '@material-ui/icons/Launch';
import Box from '@material-ui/core/Box';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import ApiContext from '../components/ApiContext';

const styles = (theme) => ({
    contentWrapper: {
        marginTop: theme.spacing(2),
        maxHeight: '250px',
        overflowY: 'auto',
    },
});

/**
 *
 * @param {*} props
 */
function RenderMethodBase(props) {
    const { theme, method } = props;
    const methodLower = method.toLowerCase();
    let chipColor = theme.custom.operationChipColor
        ? theme.custom.operationChipColor[methodLower]
        : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properlly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.operationChipColor[methodLower]);
    }
    return (
        <Chip
            label={method}
            style={{
                backgroundColor: chipColor, color: chipTextColor, height: 20, fontSize: 9, width: 95,
            }}
        />
    );
}

RenderMethodBase.propTypes = {
    method: PropTypes.string.isRequired,
    theme: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

const RenderMethod = withTheme(RenderMethodBase);
/**
 *
 * @param {*} props
 */
function Operations(props) {
    const { classes, parentClasses } = props;
    return (
        <ApiContext.Consumer>
            {({ api }) => (
                <>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.Operations.operation'
                                defaultMessage='Operations'
                            />
                        </Typography>
                    </div>
                    <div>
                        <div className={classes.contentWrapper}>
                            <Table style={{ padding: 20 }}>
                                {api.operations
                            && api.operations.length !== 0
                            && api.operations.map((item) => (
                                <TableRow style={{ borderStyle: 'hidden' }}>
                                    <TableCell style={{ padding: 8 }}>
                                        <Typography className={parentClasses.heading} component='p' variant='body1'>
                                            {item.target}
                                        </Typography>
                                    </TableCell>
                                    <TableCell style={{ padding: 8 }}>
                                        <RenderMethod method={item.verb} />
                                    </TableCell>
                                </TableRow>
                            ))}
                            </Table>
                        </div>
                        <Box py={2}>
                            <Link to={'/apis/' + api.id + '/operations'}>
                                <Typography
                                    className={classes.subHeading}
                                    color='primary'
                                    display='inline'
                                    variant='caption'
                                >
                                    <FormattedMessage
                                        id='Apis.Details.NewOverview.Operations.ShowMore'
                                        defaultMessage='Show More'
                                    />
                                    <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                                </Typography>
                            </Link>
                        </Box>
                    </div>
                </>
            )}
        </ApiContext.Consumer>
    );
}

Operations.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Operations);
