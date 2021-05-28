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

import React, { useState, useEffect } from 'react';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';
import Chip from '@material-ui/core/Chip';
import MenuItem from '@material-ui/core/MenuItem';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import Select from '@material-ui/core/Select';
import Input from '@material-ui/core/Input';
import Box from '@material-ui/core/Box';


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

/**
 *
 *
 * @class AppConfiguration
 * @extends {React.Component}
 */
const AppConfiguration = (props) => {

    const {
        classes, config, isUserOwner, previousValue, handleChange,
    } = props;

    const [selectedValue, setSelectedValue] = useState(previousValue);

    /**
     * This method is used to handle the updating of key generation
     * request object.
     * @param {*} field field that should be updated in key request
     * @param {*} event event fired
     */
    const handleAppRequestChange = (event) => {
        const { target: currentTarget } = event;
        setSelectedValue(currentTarget.value);
        handleChange('additionalProperties', event);
    }
    /**
     * Update the state when new props are available
     */
    useEffect(() => {
        setSelectedValue(previousValue);
    }, [previousValue])
    return (
        <>
            <TableRow>
                <TableCell component='th' scope='row' className={classes.leftCol}>
                    {config.label}
                </TableCell>
                <TableCell>
                    <Box maxWidth={600}>
                    {config.type === 'select' && config.multiple === false ? (
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
                            margin='dense'
                            variant='outlined'
                            disabled={!isUserOwner}
                        >
                            {config.values.map(key => (
                                <MenuItem key={key} value={key}>
                                    {key}
                                </MenuItem>
                            ))}
                        </TextField>
                    ) : (config.type === 'select' && config.multiple === true && Array.isArray(selectedValue)) ? (
                        <>
                            <FormControl variant="outlined" className={classes.formControl} fullWidth>
                                <InputLabel id="multi-select-label">{config.label}</InputLabel>
                                <Select
                                    labelId="multi-select-label"
                                    id="multi-select-outlined"
                                    margin='dense'
                                    displayEmpty
                                    name={config.name}
                                    multiple
                                    value={selectedValue}
                                    onChange={e => handleAppRequestChange(e)}
                                    input={<Input id='multi-select-outlined' />}
                                    renderValue={selected => (
                                        <div className={classes.chips}>
                                            {selected.map(value => (
                                                <Chip key={value} label={value} className={classes.chip} />
                                            ))}
                                        </div>
                                    )}
                                    helperText={
                                        <Typography variant='caption'>
                                            {config.tooltip}
                                        </Typography>
                                    }
                                    label={config.label}
                                >
                                    {config.values.map(key => (
                                        <MenuItem key={key} value={key}>
                                            <Checkbox checked={selectedValue.indexOf(key) > -1} />
                                            <ListItemText primary={key} />
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>


                            <Typography variant='caption'>
                                {config.tooltip}
                            </Typography>
                        </>
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
                            margin='dense'
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
                                        margin='dense'
                                        variant='outlined'
                                        disabled={!isUserOwner}
                                    />
                                )}
                            </Box>
                </TableCell>
            </TableRow>
        </>
    );
};

AppConfiguration.defaultProps = {
    notFound: false,
};

AppConfiguration.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    previousValue: PropTypes.any.isRequired,
    isUserOwner: PropTypes.bool.isRequired,
    handleChange: PropTypes.func.isRequired,
    config: PropTypes.any.isRequired,
    notFound: PropTypes.bool,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(AppConfiguration));
