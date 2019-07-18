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

import React, { useState, useEffect, useRef } from 'react';
import {
    Grid,
    Button,
    Typography,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    withStyles,
    ListItemText,
    ListItemAvatar,
    Icon,
    TextField,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import KeyboardArrowRightRounded from '@material-ui/icons/KeyboardArrowRight';

import LoadBalanceConfig from './LoadBalanceConfig';
import GenericEndpoint from './GenericEndpoint';
import GenericEndpointAdd from "./GenericEndpointAdd";

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
        padding: '5px',
    },
    epTypeName: {
        paddingTop: '10px',
        fontWeight: 600,
    },
    epConfig: {
        justifyContent: 'flex-end',
    },
    leftArrow: {
        paddingTop: '6px',
    },
    leftArrowLight: {
        color: '#c5c5c5',
        paddingTop: '6px',
    },
    dialogHeader: {
        fontWeight: 600,
    },
    listItemOdd: {
        background: '#ececec',
        borderRadius: '5px',
        '&:hover': {
            backgroundColor: theme.palette.grey[300],
        },
        '&:focus': {
            backgroundColor: theme.palette.grey[400],
        },
    },
    listItem: {
        borderRadius: '5px',
        '&:hover': {
            backgroundColor: theme.palette.grey[300],
        },
        '&:focus': {
            backgroundColor: theme.palette.grey[400],
        },
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
        editEndpoint,
        selectedEpIndex,
        addNewEndpoint,
        removeEndpoint,
    } = props;
    const [endpointType, setEndpointType] = useState(epType);
    const [endpoints, setEndpoints] = useState([{ url: 'http://myservice/' }]);
    const selectedRef = useRef(null);

    const addEndpoint = (url) => {
        console.log('Add endpoint url');
        addNewEndpoint(category, epType, url);
    };

    useEffect(() => {

    }, [endpoints]);
    // TODO: Fix continuous rendering.
    useEffect(() => {
        console.log('Endpoint Listing: ', apiEndpoints, failOvers);
        setEndpointType(epType);
        setEndpoints(() => {
            if (apiEndpoints !== null && epType === 'failover') {
                return ([apiEndpoints].concat(failOvers));
            } else {
                if (apiEndpoints !== undefined) {
                    return Array.isArray(apiEndpoints) ? apiEndpoints : [apiEndpoints];
                }
                return [{ url: 'http://myservice/' }];
            }
        });
    }, [props]);

    return (
        <div className={classes.listingWrapper} ref={selectedRef}>
            <Grid container direction='column' xs={12}>
                <Grid xs={12}>
                    {(endpointType === 'failover' || endpointType === 'load_balance') ?
                        <GenericEndpointAdd addEndpoint={addEndpoint} /> : <div />}
                </Grid>
                <Grid xs={12}>
                    {
                        (endpoints.map((ep, index) => {
                            if (index > 0) {
                                return (
                                    <GenericEndpoint
                                        endpointURL={endpoints[index] ? endpoints[index].url : ''}
                                        type={endpointType}
                                        index={index}
                                        category={category}
                                        editEndpoint={editEndpoint}
                                    />
                                );
                            }
                            return (<div />);
                        }))
                    }
                </Grid>
            </Grid>
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
    addNewEndpoint: PropTypes.func.isRequired,
    removeEndpoint: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(EndpointListing));
