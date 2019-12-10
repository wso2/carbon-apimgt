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
import { withRouter } from 'react-router';
import { Link } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Avatar from '@material-ui/core/Avatar';
import Icon from '@material-ui/core/Icon';
import classNames from 'classnames';
import Box from '@material-ui/core/Box';
import { PropTypes } from 'prop-types';
import LinearProgress from '@material-ui/core/LinearProgress';
import { FormattedMessage } from 'react-intl';

const useStyles = makeStyles((theme) => ({
    chipHeadWrapperDefault: {
        background: theme.palette.grey[100],
        color: theme.palette.getContrastText(theme.palette.grey[100]),
    },
    chipHeadWrapperDone: {
        background: theme.custom.productSampleProgess.backgroundMain,
        color: theme.palette.getContrastText(theme.custom.productSampleProgess.backgroundMain),
    },
    chipHeadWrapper: {
        padding: 5,
        borderRadius: 10,
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
        marginTop: 10,
    },
    chipAvatarDone: {
        background: theme.custom.productSampleProgess.backgroundChip,
        color: theme.palette.getContrastText(theme.custom.productSampleProgess.backgroundChip),
        marginRight: 10,
    },
    chipAvatarDefault: {
        background: theme.palette.grey[50],
        color: theme.palette.getContrastText(theme.palette.grey[50]),
        marginRight: 10,
    },
    post: {
        background: theme.custom.resourceChipColors.post,
        color: theme.palette.getContrastText(theme.custom.resourceChipColors.post),
        display: 'inline-block',
        marginLeft: 5,
        marginRight: 15,
        borderRadius: 5,
        padding: 5,
        textTransform: 'lowercase',
    },
    get: {
        background: theme.custom.resourceChipColors.get,
        color: theme.palette.getContrastText(theme.custom.resourceChipColors.get),
        display: 'inline-block',
        marginLeft: 10,
        borderRadius: 5,
        padding: 5,
        textTransform: 'lowercase',
    },
    actions: {
        justifyContent: 'flex-start',
        paddingLeft: 20,
        paddingTop: 20,
    },
    progress: {
        width: '100%',
        marginLeft: 20,
    },
    chipHeadTitle: {
        whiteSpace: 'nowrap',
    },
}));

function SampleAPIProductWizard(props) {
    const classes = useStyles();
    const {
        step, setStep, productPath, history,
    } = props;

    function handleClose() {
        if (step === 4) {
            setStep(0);
            history.push(productPath);
        }
        setStep(0);
    }

    return (
        <>
            <Dialog
                fullWidth
                maxWidth='md'
                open={step !== 0}
                onClose={handleClose}
                aria-labelledby='max-width-dialog-title'
            >
                <DialogTitle id='max-width-dialog-title'>
                    <FormattedMessage
                        id='Apis.Listing.SampleAPI.SampleAPIProductWizard.title'
                        defaultMessage='Auto Generating API Product Progress..'
                    />
                </DialogTitle>
                <DialogContent>
                    {/* Calculator api creation */}
                    <div
                        className={classNames(classes.chipHeadWrapper, {
                            [classes.chipHeadWrapperDefault]: step < 2,
                            [classes.chipHeadWrapperDone]: step >= 2,
                        })}
                    >
                        <Avatar
                            className={classNames({
                                [classes.chipAvatarDefault]: step < 2,
                                [classes.chipAvatarDone]: step >= 2,
                            })}
                        >
                            {step < 2 ? <Icon>border_inner</Icon> : <Icon>done</Icon>}
                        </Avatar>
                        <div className={classes.chipHeadTitle}>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPIProductWizard.calculator.text'
                                defaultMessage='Creating API Calculator'
                            />
                        </div>
                        {step < 2 && <LinearProgress variant='query' className={classes.progress} />}
                    </div>
                    <Box fontWeight='fontWeightBold' m={1} component='span'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPIProductWizard.resource'
                            defaultMessage='Resources'
                        />
                    </Box>
                    <Box m={1} component='span'>
                        <span>/add</span>
                        {' '}
                        <span className={classes.post}>post</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/divide</span>
                        {' '}
                        <span className={classes.post}>post</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/multiply</span>
                        {' '}
                        <span className={classes.post}>post</span>
                    </Box>

                    {/* Math Api creation */}
                    <div
                        className={classNames(classes.chipHeadWrapper, {
                            [classes.chipHeadWrapperDefault]: step < 3,
                            [classes.chipHeadWrapperDone]: step >= 3,
                        })}
                    >
                        <Avatar
                            className={classNames({
                                [classes.chipAvatarDefault]: step < 3,
                                [classes.chipAvatarDone]: step >= 3,
                            })}
                        >
                            {step < 3 ? <Icon>border_inner</Icon> : <Icon>done</Icon>}
                        </Avatar>
                        <div className={classes.chipHeadTitle}>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPIProductWizard.mathapi'
                                defaultMessage='Creating Math API'
                            />
                        </div>
                        {step < 3 && <LinearProgress variant='query' className={classes.progress} />}
                    </div>
                    <Box fontWeight='fontWeightBold' m={1} component='span'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPIProductWizard.resource'
                            defaultMessage='Resources'
                        />
                    </Box>
                    <Box m={1} component='span'>
                        <span>/area</span>
                        {' '}
                        <span className={classes.get}>get</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/volume</span>
                        {' '}
                        <span className={classes.get}>get</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/multiply</span>
                        {' '}
                        <span className={classes.get}>get</span>
                    </Box>
                    {/* API Product creation */}
                    <div
                        className={classNames(classes.chipHeadWrapper, {
                            [classes.chipHeadWrapperDefault]: step < 4,
                            [classes.chipHeadWrapperDone]: step >= 4,
                        })}
                    >
                        <Avatar
                            className={classNames({
                                [classes.chipAvatarDefault]: step < 4,
                                [classes.chipAvatarDone]: step >= 4,
                            })}
                        >
                            {step < 4 ? <Icon>border_inner</Icon> : <Icon>done</Icon>}
                        </Avatar>
                        <div className={classes.chipHeadTitle}>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPIProductWizard.product.title'
                                defaultMessage={
                                    'Creating CalculatorAPIProduct from the resources'
                                    + 'of Math API and Calculator API'
                                }
                            />
                        </div>
                        {step < 4 && <LinearProgress variant='query' className={classes.progress} />}
                    </div>
                    <Box fontWeight='fontWeightBold' m={1} component='span'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPIProductWizard.resource'
                            defaultMessage='Resources'
                        />
                    </Box>
                    <Box m={1} component='span'>
                        <span>/add</span>
                        {' '}
                        <span className={classes.post}>post</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/divide</span>
                        {' '}
                        <span className={classes.post}>post</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/area</span>
                        {' '}
                        <span className={classes.get}>get</span>
                    </Box>
                    <Box m={1} component='span'>
                        <span>/volume</span>
                        {' '}
                        <span className={classes.get}>get</span>
                    </Box>
                </DialogContent>
                <DialogActions className={classes.actions}>
                    {step >= 4 && (
                        <Link to={productPath}>
                            <Button variant='contained' color='primary'>
                                <FormattedMessage
                                    id='Apis.Listing.SampleAPI.SampleAPIProductWizard.product.button'
                                    defaultMessage='Go to CalculatorAPIProduct'
                                />
                            </Button>
                        </Link>
                    )}
                    <Button onClick={handleClose} color='primary'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPIProductWizard.product.close.button'
                            defaultMessage='Close'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}
SampleAPIProductWizard.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    step: PropTypes.number.isRequired,
    setStep: PropTypes.func.isRequired,
    productPath: PropTypes.string.isRequired,
    history: PropTypes.shape({}).isRequired,
};
export default withRouter(SampleAPIProductWizard);
