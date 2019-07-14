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
import PropTypes from 'prop-types';
import { Paper, FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import Switch from '@material-ui/core/Switch';
import Collapse from '@material-ui/core/Collapse';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import ReactDOM from 'react-dom';

import EndpointListing from './EndpointListing';
import EndpointConfig from './EndpointConfig';

const styles = theme => ({
    overviewWrapper: {
        marginTop: theme.spacing.unit * 2,
    },
    listing: {
        margin: theme.spacing.unit,
        padding: theme.spacing.unit,
    },
    endpointContainer: {
        padding: '10px',
    },
    endpointName: {
        fontSize: '1.2rem',
        paddingTop: '5px',
        paddingBottom: '5px',
    },
    endpointTypesWrapper: {
        padding: theme.spacing.unit * 3,
        marginTop: theme.spacing.unt * 2,
    },
    sandboxHeading: {
        display: 'flex',
        alignItems: 'center',
    },
});
/**
 * The endpoint overview component. This component holds the views of endpoint creation and configuration.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the endpoints overview.
 */
function EndpointOverview(props) {
    const { classes, api } = props;
    const { endpointConfig, endpointSecurity } = api;
    const {
        production_endpoints,
        production_failovers,
        sandbox_endpoints,
        sandbox_failovers,
        sessionManagement,
        sessionTimeOut,
        endpoint_type,
        algoClassName,
        algoCombo,
    } = endpointConfig;

    console.log(api);

    const [endpointType, setEndpointType] = useState(endpoint_type);
    const [epConfig, setEpConfig] = useState(endpointConfig);
    const [selectedEndpointInfo, setSelectedEndpointInfo] = useState({});
    const [selectedEpIndex, setSelectedEpIndex] = useState([]);
    const [selectedEp, setSelectedEPRef] = useState(0);
    const [isSandboxChecked, setSandboxChecked] = useState(false);

    const getFailOverCategory = (category) => {
        return (category === 'production_endpoints' ? 'production_failovers' : 'sandbox_failovers');
    };

    const getSelectedEndpoint = (index, epType, category, ref) => {
        setSelectedEpIndex([index, category]);
        setSelectedEPRef(ref);
        setSelectedEPRef(ReactDOM.findDOMNode(ref).getBoundingClientRect().top);
        setSelectedEndpointInfo(() => {
            let selected = {};
            switch (epType) {
                case 'load_balance':
                    selected = epConfig[category][index];
                    break;
                case 'failover':
                    if (index === 0) {
                        selected = (epConfig[category]);
                        break;
                    } else {
                        const failOverCategory = getFailOverCategory(category);
                        selected = epConfig[failOverCategory][index - 1];
                    }
                    break;
                default:
                    selected = epConfig[category];
                    break;
            }
            console.log('selected', selected);
            if (!selected) {
                selected = { url: 'asddssfsd' };
            }
            return selected;
        });
    };

    // const addEndpoint = (category, epType) => {
    //     let tmpEndpointConfig = epConfig;
    //     let oldEndpoints = {};
    //     let newEndpoint = {};
    //     console.log(category, epType);
    //     if (epType === 'load_balance') {
    //         oldEndpoints = epConfig[category];
    //         if (Array.isArray(oldEndpoints)) {
    //             newEndpoint = oldEndpoints[0].endpoint_type !== undefined ?
    //                 { endpoint_type: 'address', url: '', template_not_supported: false } :
    //                 { url: '' };
    //         } else {
    //             oldEndpoints = [oldEndpoints].concat(oldEndpoints.endpoint_type !== undefined ?
    //                 { endpoint_type: 'address', url: '', template_not_supported: false } :
    //                 { url: '' });
    //         }
    //         oldEndpoints.concat(newEndpoint);
    //         tmpEndpointConfig[category] = oldEndpoints;
    //     } else {
    //         const failOverCategory = getFailOverCategory(category);
    //         oldEndpoints = epConfig[failOverCategory];
    //         if (Array.isArray(oldEndpoints)) {
    //             newEndpoint = oldEndpoints[0].endpoint_type !== undefined ?
    //                 { endpoint_type: 'address', url: '', template_not_supported: false } :
    //                 { url: '' };
    //         } else {
    //             oldEndpoints = [oldEndpoints].concat(oldEndpoints.endpoint_type !== undefined ?
    //                 { endpoint_type: 'address', url: '', template_not_supported: false } :
    //                 { url: '' });
    //         }
    //         oldEndpoints.concat(newEndpoint);
    //         tmpEndpointConfig[category] = oldEndpoints;
    //     }
    //     setEpConfig(tmpEndpointConfig);
    //     console.log(tmpEndpointConfig);
    // };

    useEffect(() => {
        setEpConfig(endpointConfig);
        setEndpointType(endpointConfig.endpoint_type);
    }, [endpointConfig]);


    const editEndpoint = (url) => {
        console.log(selectedEpIndex, url, endpointType);
    };

    const configStyle = {
        position: 'relative',
        top: selectedEp - 200,
    };

    const handleEndpointTypeSelect = (event) => {
        setEndpointType(event.target.value);
    };

    const endpointTypes = [{ key: 'http', value: 'HTTP/REST Endpoint' },
        { key: 'address', value: 'HTTP/SOAP Endpoint' }];
    return (
        <div className={classes.overviewWrapper}>
            <Grid container>
                <Grid item xs={6}>
                    <Paper>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor='age-simple'>
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.EndpointOverview.endpointType'
                                    defaultMessage='Endpoint Type'
                                />
                            </InputLabel>
                            <Select
                                value={endpointType.value}
                                onChange={handleEndpointTypeSelect}
                                inputProps={{
                                    name: 'age',
                                    id: 'age-simple',
                                }}
                            >
                                {endpointTypes.map((key, index) => {
                                    console.log(key);
                                    return (<MenuItem value={key}>{endpointTypes[index].value}</MenuItem>);
                                })}
                            </Select>
                        </FormControl>
                    </Paper>
                    <Paper className={classes.endpointContainer}>
                        <Typography className={classes.endpointName}>
                            <FormattedMessage
                                id='Apis.Details.EndpointsNew.EndpointOverview.production'
                                defaultMessage='Production'
                            />
                        </Typography>
                        <EndpointListing
                            getSelectedEndpoint={getSelectedEndpoint}
                            apiEndpoints={epConfig.production_endpoints}
                            failOvers={epConfig.production_failovers}
                            selectedEpIndex={selectedEpIndex}
                            epType={epConfig.endpoint_type}
                            //addNewEndpoint={addEndpoint}
                            category='production_endpoints'
                        />
                        <div className={classes.sandboxHeading}>
                            <Typography className={classes.endpointName}>
                                <FormattedMessage
                                    id='Apis.Details.EndpointsNew.EndpointOverview.sandbox'
                                    defaultMessage='Sandbox'
                                />
                            </Typography>
                            <Switch checked={isSandboxChecked} onChange={() => setSandboxChecked(!isSandboxChecked)} />
                        </div>
                        <Collapse in={isSandboxChecked}>
                            <EndpointListing
                                getSelectedEndpoint={getSelectedEndpoint}
                                apiEndpoints={epConfig.sandbox_endpoints}
                                selectedEpIndex={selectedEpIndex}
                                failOvers={epConfig.sandbox_failovers}
                                epType={epConfig.endpoint_type}
                                //addNewEndpoint={addEndpoint}
                                category='sandbox_endpoints'
                            />

                        </Collapse>
                    </Paper>
                </Grid>
                <Grid item xs={6}>
                    <div style={configStyle} >
                        <EndpointConfig epInfo={selectedEndpointInfo} changeEndpointURL={editEndpoint} />
                    </div>
                </Grid>
            </Grid>
        </div>
    );
}

EndpointOverview.propTypes = {
    classes: PropTypes.shape({
        overviewWrapper: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        endpointName: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(EndpointOverview));
