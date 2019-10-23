import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
    Grid,
    TextField,
    makeStyles,
    Typography,
    Tooltip,
} from '@material-ui/core';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';

const useStyles = makeStyles(theme => ({
    typography: {
        display: 'inline-block',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 300,
    },
}));

/**
 * The credentials component. This component holds the access and secret key pair.
 * @param {any} props The parameters to credentials component.
 * @returns {any} HTML view of the credentials component.
 */
export default function Credentials(props) {
    const {
        apiId,
        epConfig,
        setEpConfig,
        endpointsDispatcher,
    } = props;
    const classes = useStyles();
    const [isValid, setIsValid] = useState(null);
    const [isChanged, setIsChanged] = useState(false);
    useEffect(() => {
        API.getAmznResourceNames(apiId)
            .then((response) => {
                setIsValid(response.body);
            });
    }, []);
    return (
        <div>
            <Grid item>
                <Typography className={classes.typography}>
                    <FormattedMessage
                        id={'Apis.Details.Endpoints.EndpointOverview.awslambda' +
                        '.endpoint.credentials'}
                        defaultMessage='AWS Credentials'
                    />
                </Typography>
                { isValid ?
                    <Tooltip title='AWS Credentials are valid'><Icon color='primary'>check_circle</Icon></Tooltip> :
                    <div />
                }
            </Grid>
            <Grid item>
                <TextField
                    required
                    id='outlined-required'
                    label='Access Key'
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    value={epConfig.amznAccessKey}
                    onChange={(event) => {
                        const newEpConfig = { ...epConfig };
                        newEpConfig.amznAccessKey = event.target.value;
                        setEpConfig(newEpConfig);
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                    }}
                />
                <TextField
                    required
                    id='outlined-password-input-required'
                    label='Secret Key'
                    type='password'
                    margin='normal'
                    variant='outlined'
                    className={classes.textField}
                    value={isChanged ? epConfig.amznSecretKey : 'AWS__SECRET__KEY'}
                    onChange={(event) => {
                        const newEpConfig = { ...epConfig };
                        newEpConfig.amznSecretKey = event.target.value;
                        setEpConfig(newEpConfig);
                        endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                        setIsChanged(true);
                    }}
                />
            </Grid>
        </div>
    );
}

Credentials.propTypes = {
    apiId: PropTypes.shape('').isRequired,
    epConfig: PropTypes.shape({}).isRequired,
    setEpConfig: PropTypes.func.isRequired,
    endpointsDispatcher: PropTypes.func.isRequired,
};
