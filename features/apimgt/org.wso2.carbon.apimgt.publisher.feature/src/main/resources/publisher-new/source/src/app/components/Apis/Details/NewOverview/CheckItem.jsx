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
import classNames from 'classnames';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Fab from '@material-ui/core/Fab';
import Grid from '@material-ui/core/Grid';
import CheckIcon from '@material-ui/icons/Check';
import WarningIcon from '@material-ui/icons/Warning';

const styles = theme => ({
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    buttonSuccess: {
        backgroundColor: green[500],
        '&:hover': {
            backgroundColor: green[700],
        },
    },
    checkItem: {
        textAlign: 'center',
    },
});

function CheckItem(props) {
    const { classes, itemLabel, itemSuccess } = props;
    const buttonClassname = classNames({
        [classes.buttonSuccess]: itemSuccess,
    });
    return (
        <Grid item lg={1} md={2} xs={4} className={classes.checkItem}>
            <Fab color='default' className={buttonClassname}>
                {itemSuccess ? <CheckIcon /> : <WarningIcon />}
            </Fab>
            <Typography variant='overline'>{itemLabel}</Typography>
        </Grid>
    );
}

CheckItem.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    itemSuccess: PropTypes.bool.isRequired,
    itemLabel: PropTypes.string.isRequired,
};

export default withStyles(styles)(CheckItem);
