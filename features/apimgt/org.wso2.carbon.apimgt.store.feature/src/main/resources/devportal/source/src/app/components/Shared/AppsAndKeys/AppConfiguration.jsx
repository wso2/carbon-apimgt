/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState } from 'react';
import Box from '@material-ui/core/Box';
import Chip from '@material-ui/core/Chip';
import MenuItem from '@material-ui/core/MenuItem';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import Select from '@material-ui/core/Select';
import Input from '@material-ui/core/Input';

const styles = theme => ({
    FormControl: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        paddingLeft: 0,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing(2),
        width: '100%',
    },
    button: {
        marginLeft: theme.spacing(1),
    },
    quotaHelp: {
        position: 'relative',
    },
    checkboxWrapper: {
        display: 'flex',
    },
    checkboxWrapperColumn: {
        display: 'flex',
        flexDirection: 'row',
    },
    group: {
        flexDirection: 'row',
    },
    removeHelperPadding: {
        '& p': {
            margin: '8px 0px',
        },
    },
});

const MenuProps = {
    PaperProps: {
        style: {
            paddingTop: 2,
            paddingBottom: 2,
            paddingLeft: 0,
            maxHeight: 224,
            width: 500,
        },
    },
};

/**
 *
 *
 * @class AppConfiguration
 * @extends {React.Component}
 */
const AppConfiguration = (props) => {

    const {
        classes, config, isUserOwner, defaultValue, handleChange,
    } = props;

    const [selectedValue, setSelectedValue] = useState(defaultValue);
    const [multipleSelectedValue, setMultipleSelectedValue] = useState([]);
    
    /**
     * This method is used to handle the updating of key generation
     * request object.
     * @param {*} field field that should be updated in key request
     * @param {*} event event fired
     */
    const handleAppRequestChange = (event) => {
        const { target: currentTarget } = event;
        if(config.multiple){
            setMultipleSelectedValue(currentTarget.value);
        }else {
            setSelectedValue(currentTarget.value);
        }
        
        handleChange('additionalProperties', event);
    }

    return ( 
        <Box display='flex'>
        <Grid item xs={10} md={5}>
        {config.type === 'select'  && config.multiple === false ? (
            <TextField
                classes={{
                    root: classes.removeHelperPadding,
                }}
                fullWidth
                id={config.name}
                select
                label={config.label}
                value={selectedValue}
                name={config.name}
                onChange={e => handleAppRequestChange(e)}
                helperText={
                    <Typography variant='caption'>
                        {config.tooltip}                 
                    </Typography>
                }
                margin='normal'
                variant='outlined'
                disabled={!isUserOwner}
            >
            {config.values.map( key => (
                <MenuItem key={key} value={key}>
                    {key}
                </MenuItem>
            ))}
            </TextField>
        ) : (config.type === 'select'  && config.multiple === true) ? (
            <React.Fragment>
                <Typography variant='caption'>
                    {config.label}                 
                </Typography>
                <br/>
                <Select
                    displayEmpty
                    name={config.name}
                    multiple
                    value={multipleSelectedValue}
                    onChange={e => handleAppRequestChange(e)}
                    input={<Input id='select-multiple-chip' />}
                    renderValue={selected => (
                        <div className={classes.chips}>
                            {selected.map(value => (
                                <Chip key={value} label={value} className={classes.chip} />
                            ))}
                        </div>
                    )}
                    MenuProps={MenuProps}
                    helperText={
                        <Typography variant='caption'>
                            {config.tooltip}                 
                        </Typography>
                    }
                    label={config.label}
                >
                {config.values.map( key => (
                    <MenuItem key={key} value={key}>
                        {key}
                    </MenuItem>
                ))}
                </Select>
                <br/>
                <Typography variant='caption'>
                        {config.tooltip}                 
                </Typography>
            </React.Fragment>
        ) : (config.type === 'input') ? (
            <TextField
                classes={{
                    root: classes.removeHelperPadding,
                }}
                fullWidth
                id={config.name}
                label={config.label}
                value={selectedValue}
                name={config.name}
                onChange={e => handleAppRequestChange(e)}
                helperText={
                        <Typography variant='caption'>
                            {config.tooltip}                 
                        </Typography>
                }
                margin='normal'
                variant='outlined'
                disabled={!isUserOwner}
            />
        ) : (
            <TextField
                classes={{
                    root: classes.removeHelperPadding,
                }}
                fullWidth
                id={config.name}
                label={config.label}
                value={selectedValue}
                name={config.name}
                onChange={e => handleAppRequestChange(e)}
                helperText={
                        <Typography variant='caption'>                          
                                {config.tooltip}
                        </Typography>
                }
                margin='normal'
                variant='outlined'
                disabled={!isUserOwner}
            />
        )}
        </Grid>
    </Box>
    );
};

AppConfiguration.defaultProps = {
    notFound: false,
};

AppConfiguration.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    defaultValue: PropTypes.any.isRequired,
    isUserOwner: PropTypes.bool.isRequired,
    handleChange: PropTypes.func.isRequired,
    config: PropTypes.any.isRequired,
    notFound: PropTypes.bool,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(AppConfiguration));
