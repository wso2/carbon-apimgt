import React, { useState } from 'react';
import 'react-tagsinput/react-tagsinput.css';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import clsx from 'clsx';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Tooltip from '@material-ui/core/Tooltip';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import AddIcon from '@material-ui/icons/Add';
import CardHeader from '@material-ui/core/CardHeader';
import PropTypes from 'prop-types';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import { useTheme } from '@material-ui/core';
import Checkbox from '@material-ui/core/Checkbox';
import RadioButtonUncheckedIcon from '@material-ui/icons/RadioButtonUnchecked';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';

const useStyles = makeStyles(() => ({
    root: {
        minHeight: '480px',
    },
    textOptional: {
        fontFamily: 'sans-serif',
        fontSize: 'small',
        color: '#707070',
        fontWeight: '100',
    },
    textRevision: {
        fontFamily: 'sans-serif',
        fontSize: '16px',
        color: '#707070',
        fontWeight: '800',
    },
    textDeploy: {
        fontFamily: 'sans-serif',
        fontSize: '26px',
        color: '#1B3A57',
        fontWeight: '800',
    },
    textDescription: {
        fontFamily: 'sans-serif',
        fontSize: '16px',
        color: '#707070',
        fontWeight: '400',
    },
    descriptionWidth: {
        minWidth: '550px',
    },
    textAlign: {
        textAlign: 'center',
    },
}));
/**
 * Renders an Deployment Onboarding
 * @class Environments
 * @extends {React.Component}
 */
export default function DeploymentOnboarding(props) {
    const {
        classes,
        getVhostHelperText,
        createDeployRevision,
        description,
        setDescription,
    } = props;
    const classes1 = useStyles();
    const theme = useTheme();
    const { maxCommentLength } = theme.custom;
    const { settings } = useAppContext();
    const defaultVhosts = settings.environment.map(
        (e) => (e.vhosts && e.vhosts.length > 0 ? { env: e.name, vhost: e.vhosts[0].host } : undefined),
    );
    const [DescriptionOpen, setDescriptionOpen] = useState(false);
    const [SelectedEnvironment, setSelectedEnvironment] = useState([]);
    // const [currentLength, setCurrentLength] = useState(0);
    const [selectedVhostDeploy, setVhostsDeploy] = useState(defaultVhosts);

    const handleDescriptionOpen = () => {
        setDescriptionOpen(!DescriptionOpen);
    };

    const handleVhostDeploySelect = (event) => {
        const vhosts = selectedVhostDeploy.filter((v) => v.env !== event.target.name);
        vhosts.push({ env: event.target.name, vhost: event.target.value });
        setVhostsDeploy(vhosts);
    };

    const handleChange = (event) => {
        if (event.target.checked) {
            setSelectedEnvironment([...SelectedEnvironment, event.target.value]);
        } else {
            setSelectedEnvironment(
                SelectedEnvironment.filter((env) => env !== event.target.value),
            );
        }
        if (event.target.name === 'description') {
            setDescription(event.target.value);
            // setCurrentLength(event.target.value.length);
        }
    };

    return (
        <>
            <Grid container spacing={2}>
                <Grid item xs={2} />
                <Grid item xs={8}>
                    <Grid container spacing={2}>
                        <Grid item xs={2} />
                        <Grid item xs={8} className={classes1.textAlign}>
                            <Typography variant='h6' className={classes1.textDeploy}>
                                Deploy the API
                            </Typography>
                        </Grid>
                        <Grid item xs={2} />
                    </Grid>
                    <Box pb={2}>
                        <Grid container>
                            <Grid item xs={2} />
                            <Grid item xs={8} className={classes1.textAlign}>
                                <Typography variant='h6' className={classes1.textDescription}>
                                    Deploy API to the Gateway Environment
                                </Typography>
                            </Grid>
                            <Grid item xs={2} />
                        </Grid>
                    </Box>
                    <Paper fullWidth className={classes1.root}>
                        <Box p={5}>
                            <Typography className={classes1.textRevision}>
                                API Gateways
                            </Typography>
                            <Box mt={4}>
                                <Grid
                                    container
                                    spacing={3}
                                >
                                    {settings.environment.map((row) => (
                                        <Grid item xs={3}>
                                            <Card
                                                className={clsx(SelectedEnvironment
                                                    && SelectedEnvironment.includes(row.name)
                                                    ? (classes.changeCard) : (classes.noChangeCard),
                                                classes.cardHeight)}
                                                variant='outlined'
                                            >
                                                <Box height='100%'>
                                                    <CardHeader
                                                        action={(
                                                            <Checkbox
                                                                id={row.name.split(' ').join('')}
                                                                value={row.name}
                                                                checked={SelectedEnvironment.includes(row.name)}
                                                                onChange={handleChange}
                                                                color='primary'
                                                                icon={<RadioButtonUncheckedIcon />}
                                                                checkedIcon={<CheckCircleIcon color='primary' />}
                                                                inputProps={{ 'aria-label': 'secondary checkbox' }}
                                                            />
                                                        )}
                                                        title={(
                                                            <Typography variant='subtitle2'>
                                                                {row.displayName}
                                                            </Typography>
                                                        )}
                                                        subheader={(
                                                            <Typography
                                                                variant='body2'
                                                                color='textSecondary'
                                                                gutterBottom
                                                            >
                                                                {row.type}
                                                            </Typography>
                                                        )}
                                                    />
                                                    <CardContent className={classes.cardContentHeight}>
                                                        <Grid
                                                            container
                                                            direction='column'
                                                            spacing={2}
                                                        >
                                                            <Grid item xs={12}>
                                                                <Tooltip
                                                                    title={getVhostHelperText(row.name,
                                                                        selectedVhostDeploy)}
                                                                    placement='bottom'
                                                                >
                                                                    <TextField
                                                                        id='vhost-selector'
                                                                        select
                                                                        label={(
                                                                            <FormattedMessage
                                                                                id='Apis.Details.Environments
                                                                                .deploy.vhost'
                                                                                defaultMessage='VHost'
                                                                            />
                                                                        )}
                                                                        SelectProps={{
                                                                            MenuProps: {
                                                                                anchorOrigin: {
                                                                                    vertical: 'bottom',
                                                                                    horizontal: 'left',
                                                                                },
                                                                                getContentAnchorEl: null,
                                                                            },
                                                                        }}
                                                                        name={row.name}
                                                                        value={selectedVhostDeploy.find(
                                                                            (v) => v.env === row.name,
                                                                        ).vhost}
                                                                        onChange={handleVhostDeploySelect}
                                                                        margin='dense'
                                                                        variant='outlined'
                                                                        fullWidth
                                                                        helperText={getVhostHelperText(row.name,
                                                                            selectedVhostDeploy, true)}
                                                                    >
                                                                        {row.vhosts.map(
                                                                            (vhost) => (
                                                                                <MenuItem value={vhost.host}>
                                                                                    {vhost.host}
                                                                                </MenuItem>
                                                                            ),
                                                                        )}
                                                                    </TextField>
                                                                </Tooltip>
                                                            </Grid>
                                                        </Grid>
                                                    </CardContent>
                                                </Box>
                                            </Card>
                                        </Grid>
                                    ))}
                                </Grid>
                            </Box>
                            <Box mt={2}>
                                <Button
                                    color='primary'
                                    className={classes.button}
                                    display='inline'
                                    startIcon={<AddIcon />}
                                    onClick={handleDescriptionOpen}
                                >
                                    Add a description
                                </Button>
                                <Typography display='inline' className={classes1.textOptional}>
                                    (optional)
                                </Typography>
                                <br />
                                {DescriptionOpen && (
                                    <>
                                        <TextField
                                            className={classes1.descriptionWidth}
                                            name='description'
                                            margin='dense'
                                            variant='outlined'
                                            label='Description'
                                            inputProps={{ maxLength: maxCommentLength }}
                                            helperText={(
                                                <FormattedMessage
                                                    id='Apis.Details.Environments.Environments.revision
                                                    .description.deploy'
                                                    defaultMessage='Brief description of the new revision'
                                                />
                                            )}
                                            multiline
                                            rows={3}
                                            defaultValue={description === true ? '' : description}
                                            onBlur={handleChange}
                                        />
                                        {/* <Typography className={classes.textCount} align='right'>
                                            {currentLength + '/' + maxCommentLength}
                                        </Typography> */}
                                    </>
                                )}
                            </Box>
                            <Box mt={3}>
                                <Button
                                    type='submit'
                                    variant='contained'
                                    onClick={
                                        () => createDeployRevision(SelectedEnvironment, selectedVhostDeploy)
                                    }
                                    color='primary'
                                    disabled={SelectedEnvironment.length === 0}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Environments.Environments.deploy.deploy'
                                        defaultMessage='Deploy'
                                    />
                                </Button>
                            </Box>
                        </Box>
                    </Paper>
                </Grid>
                <Grid item xs={2} />
            </Grid>
        </>
    );
}
DeploymentOnboarding.propTypes = {
    getVhostHelperText: PropTypes.shape({}).isRequired,
    createDeployRevision: PropTypes.shape({}).isRequired,
    setDescription: PropTypes.shape({}).isRequired,
    description: PropTypes.string.isRequired,
};
