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
    ListItemAvatar,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import KeyboardArrowRightRounded from '@material-ui/icons/KeyboardArrowRight';

import RemoveCircle from '@material-ui/icons/RemoveCircle';
import EndpointAdd from './EndpointAdd';
import LoadBalanceConfig from './LoadBalanceConfig';

const styles = theme => ({
    endpointInputWrapper: {
        display: 'flex',
    },
    epInput: {
        width: '100%',
    },
    listingWrapper: {
        paddingLeft: theme.spacing.unit,
        paddingRight: theme.spacing.unit,
        marginRight: theme.spacing.unit,
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
    const {
        classes,
        category,
        apiEndpoints,
        epType,
        failOvers,
        getSelectedEndpoint,
        selectedEpIndex,
    } = props;
    const [endpointType, setEndpointType] = useState('http');
    const [endpoints, setEndpoints] = useState([{ url: 'http(s)://appserver/service' }]);
    const [isLBConfigOpen, setOpenLBConfigDialog] = useState(false);

    const addEndpoint = (type) => {
        setEndpointType(type);
        setEndpoints(endpoints.concat([{ url: 'http(s)://appserver/service' }]));
    };

    const removeEndpoint = (index) => {
        const currentEndpoints = endpoints.filter((ep, id) => { return id !== index; });
        setEndpoints(currentEndpoints);
        if (currentEndpoints.length === 1) {
            setEndpointType('http');
        }
    };

    const handleEpSelect = (event, index) => {
        getSelectedEndpoint(index, epType, category);
    };

    const getEndpointTypeSeparator = () => {
        if (endpointType === 'failover') {
            return (
                <div className={classes.epTypeName}>
                    <Typography>FailOver</Typography>
                </div>
            );
        }
        if (endpointType === 'load_balance') {
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
            <div />
        );
    };

    useEffect(() => {
        setEndpointType(epType);
        if (apiEndpoints !== null && epType === 'failover') {
            setEndpoints([apiEndpoints].concat(failOvers));
        } else {
            setEndpoints(apiEndpoints);
        }
    }, [apiEndpoints, epType, failOvers]);

    return (
        <div className={classes.listingWrapper} >
            <Grid container direction='column' xs={12}>
                <List>
                    <ListItem id={category + '_0'} button onClick={event => handleEpSelect(event, 0)}>
                        <ListItemText primary={endpoints[0].url} />
                        <ListItemSecondaryAction >
                            {(selectedEpIndex[0] === 0 && selectedEpIndex[1] === category) ?
                                <KeyboardArrowRightRounded /> : <div />}
                        </ListItemSecondaryAction>
                    </ListItem>
                </List>
                <Grid xs={12}>
                    <Divider variant='middle' />
                    <EndpointAdd onAddEndpointClick={addEndpoint} endpointType={endpointType} />
                    {getEndpointTypeSeparator()}
                    <List>
                        {
                            (endpoints.map((ep, index) => {
                                if (index > 0) {
                                    return (
                                        <ListItem
                                            button
                                            id={category + '_' + index}
                                            onClick={event => handleEpSelect(event, index)}
                                        >
                                            <ListItemAvatar>
                                                <Button onClick={() => removeEndpoint(index)}>
                                                    <RemoveCircle />
                                                </Button>
                                            </ListItemAvatar>
                                            <ListItemText primary={endpoints[index].url} />
                                            <ListItemSecondaryAction >
                                                {(selectedEpIndex[0] === index && selectedEpIndex[1] === category) ?
                                                    <KeyboardArrowRightRounded /> : <div />}
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
        </div>
    );
}

EndpointListing.defaultProps = {
    selectedEpIndex: 0,
};

EndpointListing.propTypes = {
    classes: PropTypes.shape({
        epTypeWrapper: PropTypes.shape({}),
        epTypeName: PropTypes.shape({}),
        listingWrapper: PropTypes.shape({}),
        epConfig: PropTypes.shape({}),
    }).isRequired,
    epType: PropTypes.string.isRequired,
    category: PropTypes.string.isRequired,
    apiEndpoints: PropTypes.shape({}).isRequired,
    failOvers: PropTypes.shape({}).isRequired,
    getSelectedEndpoint: PropTypes.func.isRequired,
    selectedEpIndex: PropTypes.number,
};

export default injectIntl(withStyles(styles)(EndpointListing));
