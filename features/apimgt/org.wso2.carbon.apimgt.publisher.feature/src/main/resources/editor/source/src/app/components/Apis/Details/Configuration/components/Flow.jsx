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
import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { Grid, withStyles } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import EditMediationPolicy from 'AppComponents/Apis/Details/MediationPolicies/EditMediationPolicy';
import EditRounded from '@material-ui/icons/EditRounded';

const styles = {
    content: {
        flexGrow: 1,
    },
    itemWrapper: {
        width: 'auto',
        display: 'flex',
    },
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    subTitle: {
        marginTop: 20,
    },
    subTitleDescription: {
        marginBottom: 10,
    },
    flowWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
    heading: {
        margin: 'auto',
        color: 'rgba(0, 0, 0, 0.40)',
        wordBreak: 'break-all',
        whiteSpace: 'normal',
    },
    paper: {
        padding: '10px 24px',
        width: 'auto',
    },
};

/**
 * The base component of the IN mediation policy.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function InFlow(props) {
    const {
        classes, updateMediationPolicy, selectedMediationPolicy, type, isRestricted,
    } = props;
    const [editing, setEditing] = useState(false);

    function startEditing() {
        setEditing(true);
    }
    return (
        <>
            <Paper className={classes.paper}>
                <Grid container spacing={2} alignItems='flex-start'>
                    <Grid item md={12} style={{ position: 'relative', display: 'inline-flex' }}>
                        <Typography className={classes.subHeading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.MediationPolicies.Mediation'
                                defaultMessage='Message Mediation'
                            />
                        </Typography>
                        <Typography className={classes.heading}>
                            {selectedMediationPolicy && selectedMediationPolicy.name ? (
                                <span>{selectedMediationPolicy.name}</span>
                            ) : (
                                <span>none</span>
                            )}
                        </Typography>
                        <Button
                            size='small'
                            onClick={startEditing}
                            disabled={isRestricted}
                        >
                            <EditRounded />
                        </Button>
                    </Grid>
                </Grid>
            </Paper>
            <EditMediationPolicy
                setEditing={setEditing}
                editing={editing}
                updateMediationPolicy={updateMediationPolicy}
                selectedMediationPolicy={selectedMediationPolicy}
                type={type}
            />
        </>
    );
}

InFlow.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    updateMediationPolicy: PropTypes.func.isRequired,
    selectedMediationPolicy: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(InFlow);
