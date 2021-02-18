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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import WarningIcon from '@material-ui/icons/Warning';
import ErrorIcon from '@material-ui/icons/Error';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import InfoIcon from '@material-ui/icons/Info';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router';
import Fade from '@material-ui/core/Fade';
import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';

// Icon size reference https://github.com/mui-org/material-ui/blob/master/packages/material-ui/src/Icon/Icon.js#L48
const useStyles = makeStyles((theme) => ({
    xLarge: {
        fontSize: theme.typography.pxToRem(64),
    },
}));
/**
 *
 *
 * @export
 * @returns
 */
function Banner(props) {
    const {
        type, message, dense, history, paperProps, disableActions, open, onClose, disableClose, errors,
    } = props;
    const classes = useStyles();
    const [isOpen, setIsOpen] = useState(open);
    const [show, setShow] = useState(false);
    const iconProps = {};
    if (dense) {
        iconProps.fontSize = 'large';
    } else {
        iconProps.className = classes.xLarge;
    }

    const handleShowClick = () => {
        // eslint-disable-next-line no-unused-expressions
        (show === true) ? setShow(false) : setShow(true);
    };

    let bannerIcon = null;
    let { description } = message;

    let title;
    // TODO Check for an instance of FormattedMessage as well ~tmkb
    if (typeof message === 'string' || message instanceof String) {
        description = message;
        const [first, ...rest] = type;
        title = `${first.toUpperCase()}${rest.join('')}`; // Capitalize the first letter
    } else {
        // If `message` is an object, we expect it to be a REST API error response object
        title = `[${message.code}]: ${message.message}`;
    }

    // IF add,remove or modify cases update the proptypes as well
    switch (type) {
        case 'error':
            bannerIcon = <ErrorIcon color='error' {...iconProps} />;
            break;
        case 'warning':
            bannerIcon = <WarningIcon color='error' {...iconProps} />;
            break;
        case 'successes':
            bannerIcon = <CheckCircleIcon color='error' {...iconProps} />;
            break;
        case 'info':
            bannerIcon = <InfoIcon color='error' {...iconProps} />;
            break;
        default:
            bannerIcon = <InfoIcon color='error' {...iconProps} />;
            break;
    }

    return (
        <Fade in={isOpen} unmountOnExit>
            <Box clone pt={dense ? 1 : 2} pr={dense ? 1 : 2} pb={dense ? 0 : 1} pl={dense ? 1 : 2}>
                <Paper {...paperProps}>
                    <Accordion expanded={show}>
                        <AccordionSummary>
                            <Grid container spacing={2} alignItems='center' wrap='nowrap'>
                                <Grid item>{bannerIcon}</Grid>
                                <Grid item>
                                    <Typography variant='subtitle2' display='block' gutterBottom>
                                        {title}
                                        <Typography variant='body1'>{description}</Typography>
                                    </Typography>
                                </Grid>
                            </Grid>
                        </AccordionSummary>
                        <AccordionDetails>
                            <Grid container spacing={2} alignItems='center' wrap='nowrap'>
                                <Grid item style={{ paddingLeft: '8.5%' }}>
                                    <Typography variant='subtitle2' display='block' gutterBottom>
                                        Identified Errors
                                        {/* eslint-disable-next-line max-len */}
                                        {errors.map((error) => <Typography variant='body1'>{error.message}</Typography>)}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </AccordionDetails>
                    </Accordion>
                    <Grid container justify='flex-end' spacing={1}>
                        <Grid item>
                            {!disableActions && (
                                <>
                                    <Button onClick={() => history.goBack()} color='primary'>
                                        <FormattedMessage
                                            id='app.components.Shared.Banner.back'
                                            defaultMessage='Back'
                                        />
                                    </Button>
                                    <Button onClick={() => window.location.reload()} color='primary'>
                                        Refresh
                                    </Button>
                                </>
                            )}
                            {!disableClose && (
                                <Button onClick={onClose || (() => setIsOpen(false))} color='primary'>
                                    CLOSE
                                </Button>
                            )}
                            <Button color='primary' onClick={handleShowClick}>
                                {show ? 'HIDE ERRORS' : 'SHOW ERRORS'}
                            </Button>
                        </Grid>
                    </Grid>
                </Paper>
            </Box>
        </Fade>
    );
}
Banner.defaultProps = {
    dense: false,
    type: 'info',
    disableActions: false,
    paperProps: { elevation: 1 },
    open: true,
    onClose: null,
    disableClose: false,
};

Banner.propTypes = {
    type: PropTypes.oneOf(['error', 'warning', 'info', 'successes']),
    message: PropTypes.PropTypes.oneOfType([PropTypes.string, PropTypes.shape({})]).isRequired,
    dense: PropTypes.bool,
    open: PropTypes.bool,
    disableClose: PropTypes.bool,
    onClose: PropTypes.func,
    disableActions: PropTypes.bool,
    paperProps: PropTypes.shape({ elevation: PropTypes.number }),
    history: PropTypes.shape({ goBack: PropTypes.func }).isRequired,
};

export default withRouter(Banner);
