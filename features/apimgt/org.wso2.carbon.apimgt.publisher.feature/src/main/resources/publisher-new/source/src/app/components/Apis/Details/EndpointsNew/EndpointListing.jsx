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

import React, { useState, useEffect } from 'react';
import {
    Grid,
    Button,
    Divider,
    Typography,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    withStyles,
    ListItemText,
    Popper,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';

import RemoveCircle from '@material-ui/icons/RemoveCircle';
import EndpointAdd from './EndpointAdd';
import EndpointConfig from './EndpointConfig';
import LoadBalanceConfig from './LoadBalanceConfig';

const styles = theme => ({
    endpointInputWrapper: {
        display: 'flex',
    },
    epInput: {
        width: '100%',
    },
    listingWrapper: {
        paddingLeft: '10px',
        paddingRight: '10px',
        marginRight: '5px',
    },
    epTypeWrapper: {
        display: 'flex',
    },
    epTypeName: {
        justifyContent: 'flex-start',
        width: '50%',
    },
    epConfig: {
        justifyContent: 'flex-end',
        paddingLeft: '30%',
        width: '50%',
    },
});

/**
 * The Component for endpoint listing.
 * @param {*} props The props that are being passed to the component.
 * @returns {any} The HTML view of the ep listing component.
 */
function EndpointListing(props) {
    const { classes, epType } = props;
    const [endpointType, setEndpointType] = useState(0);
    const [endpoints, setEndpoints] = useState([{ value: 'http(s)://appserver/service' }]);
    const [isLBConfigOpen, setOpenLBConfigDialog] = useState(false); //Loadbalance Config
    const [openEndpointConfig, setOpenEndpointConfig] = useState(false);
    const [selectedEP, setselectedEP] = useState(undefined);

    const addEndpoint = (type) => {
        setEndpointType(type);
        setEndpoints(endpoints.concat([{ value: 'http(s)://appserver/service' }]));
    };

    const removeEndpoint = (index) => {
        const currentEndpoints = endpoints.filter((ep, id) => { return id !== index; });
        setEndpoints(currentEndpoints);
        if (currentEndpoints.length === 1) {
            console.log('Count 0');
            setEndpointType(0);
        }
    };

    const openDialog = () => {
        setOpenLBConfigDialog(!isLBConfigOpen);
    };

    const handleEpChange = (epIndex, event) => {
        console.log(epIndex, event.target.value);
        const modifiedEndpoints = endpoints.map((ep, index) => {
            if (epIndex !== index) return ep;
            return { value: event.target.value };
        });
        setEndpoints(modifiedEndpoints);
    };

    const handleEpSelect = (event) => {
        // getSelected(index);
        const newSelection = event.currentTarget;
        setOpenEndpointConfig(prev => selectedEP !== newSelection || !prev);
        setselectedEP(newSelection);
        console.log('EP selected', event.currentTarget);
    };

    const getEndpointTypeSeparator = () => {
        if (endpointType === 0) {
            return (<div />);
        }
        if (endpointType === 1) {
            return (
                <div className={classes.epTypeWrapper}>
                    <div className={classes.epTypeName}>
                        <Typography>
                            <FormattedMessage id='Loadbalance' defaultMessage='Loadbalance' />
                        </Typography>
                        <Divider />
                    </div>
                    <div className={classes.epConfig}>
                        <Button onClick={() => setOpenLBConfigDialog(true)}>
                            <Typography>
                                <FormattedMessage id='Configure' defaultMessage='Configure' />
                            </Typography>
                        </Button>
                    </div>
                </div>);
        }
        return (
            <div className={classes.epTypeName}>
                <Typography>FailOver</Typography>
            </div>
        );
    };
    return (
        <div className={classes.listingWrapper}>
            <Grid container direction='column' xs={12}>
                <List>
                    <ListItem id={epType + '-0'} button onClick={handleEpSelect}>
                        <ListItemText primary={endpoints[0].value} />
                    </ListItem>
                </List>
                <Grid xs={12}>
                    <Divider variant='middle' />
                    <EndpointAdd onAddEndpointClick={addEndpoint} type={endpointType} />
                    {getEndpointTypeSeparator()}
                    <List>
                        {
                            (endpoints.map((ep, index) => {
                                if (index > 0) {
                                    return (
                                        <ListItem button id={epType + '-' + index} onClick={handleEpSelect}>
                                            <ListItemText primary={endpoints[index].value} />
                                            <ListItemSecondaryAction >
                                                <Button onClick={() => removeEndpoint(index)}>
                                                    <RemoveCircle />
                                                </Button>
                                            </ListItemSecondaryAction>
                                        </ListItem>
                                    );
                                }
                                return (<div />);
                            }))
                        }
                    </List>
                </Grid>
            </Grid>
            <Dialog open={isLBConfigOpen}>
                <DialogTitle>
                    <Typography varient='h4'>
                        <FormattedMessage
                            id='Loadbalance.Endpoint.Configuration'
                            defaultMessage='Load Balance Endpoint Configuration'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <LoadBalanceConfig />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenLBConfigDialog(false)} color='primary'>
                        Close
                    </Button>
                    <Button onClick={() => setOpenLBConfigDialog(false)} color='primary' autoFocus>
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
            <Popper
                open={openEndpointConfig}
                anchorEl={selectedEP}
                placement='right-start'
                modifiers={{
                    flip: {
                        enabled: true,
                    },
                    preventOverflow: {
                        enabled: true,
                        boundariesElement: 'scrollParent',
                    },
                    arrow: {
                        enabled: true,
                    },
                }}
            >
                <EndpointConfig />
            </Popper>
        </div>
    );
}

EndpointListing.propTypes = {
    classes: PropTypes.shape({
        epTypeWrapper: PropTypes.shape({}),
        epTypeName: PropTypes.shape({}),
        listingWrapper: PropTypes.shape({}),
        epConfig: PropTypes.shape({}),
    }).isRequired,
    epType: PropTypes.string.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointListing));
