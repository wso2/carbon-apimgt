import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage, injectIntl } from 'react-intl';
import { app } from 'Settings';
import Loading from 'AppComponents/Base/Loading/Loading';
import API from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import {
    Grid, List, ListItem, MenuItem, Paper, TextField,
} from '@material-ui/core';


const useStyles = makeStyles((theme) => ({
    root: {
        padding: theme.spacing(3, 2),
        '& td, & th': {
            color: theme.palette.getContrastText(theme.custom.infoBar.background),
        },
        background: theme.custom.infoBar.background,
    },
    table: {
        minWidth: '100%',
    },
    leftCol: {
        width: 200,
    },
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    iconEven: {
        color: theme.custom.infoBar.iconOddColor,
        width: theme.spacing(3),
    },
    iconOdd: {
        color: theme.custom.infoBar.iconOddColor,
        width: theme.spacing(3),
    },
    noKeysRoot: {
        backgroundImage: `url(${app.context + theme.custom.overviewPage.keysBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 192,
        display: 'flex',
        alignItems: 'center',
    },
    heading: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
        paddingLeft: theme.spacing(1),
    },
    emptyBox: {
        background: '#ffffff55',
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px #fff',
        padding: theme.spacing(2),
        width: '100%',
    },
    summaryRoot: {
        display: 'flex',
        alignItems: 'center',
    },
    actionPanel: {
        justifyContent: 'flex-start',
    },
    Paper: {
        marginTop: theme.spacing(2),
        padding: theme.spacing(2),
    },
    Paper2: {
        marginTop: theme.spacing(2),
        padding: theme.spacing(2),
        height: '80%',
    },
    list: {
        width: '100%',
        maxWidth: 800,
        backgroundColor: theme.palette.background.paper,
        position: 'relative',
        overflow: 'auto',
        maxHeight: 175,
    },
}));

/**
 * Render application overview page.
 * @param {JSON} props Props passed down from parent.
 * @returns {JSX} jsx output from render.
 */
function Overview(props) {
    const classes = useStyles();
    const [application, setApplication] = useState(null);
    const [tierDescription, setTierDescription] = useState(null);
    const [notFound, setNotFound] = useState(false);
    const { match: { params: { applicationId } } } = props;
    const [environment, setEnvironment] = useState(null);
    useEffect(() => {
        const client = new API();
        // Get application
        const promisedApplication = client.getApplication(applicationId);
        promisedApplication
            .then((response) => {
                const promisedTier = client.getTierByName(response.obj.throttlingPolicy, 'application');
                const appInner = response.obj;
                promisedTier.then((tierResponse) => {
                    setTierDescription(tierResponse.obj.description);
                    setApplication(appInner);
                    if (appInner.solaceDeployedEnvironments !== null) {
                        setEnvironment(appInner.solaceDeployedEnvironments[0]);
                    }
                });
            }).catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                } else {
                    setNotFound(false);
                }
            });
    }, []);
    if (notFound) {
        return <ResourceNotFound />;
    }
    if (!application) {
        return <Loading />;
    }
    if (environment) {
        console.log(environment);
    }
    const handleChange = (event) => {
        setEnvironment(event.target.value);
        console.log(event.target.value);
    };
    return (
        <>
            <div className={classes.root}>
                <Table className={classes.table}>
                    <TableBody>
                        <TableRow>
                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                <div className={classes.iconAligner}>
                                    <Icon className={classes.iconEven}>description</Icon>
                                    <span className={classes.iconTextWrapper}>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Applications.Details.Overview.description'
                                                defaultMessage='Description'
                                            />
                                        </Typography>
                                    </span>
                                </div>
                            </TableCell>
                            <TableCell>
                                {application.description}
                            </TableCell>
                        </TableRow>
                        {tierDescription
                            && (
                                <TableRow>
                                    <TableCell component='th' scope='row' className={classes.leftCol}>
                                        <div className={classes.iconAligner}>
                                            <Icon className={classes.iconOdd}>settings_input_component</Icon>
                                            <span className={classes.iconTextWrapper}>
                                                <Typography variant='caption' gutterBottom align='left'>
                                                    <FormattedMessage
                                                        id='Applications.Details.InfoBar.business.plan'
                                                        defaultMessage='Business Plan'
                                                    />
                                                </Typography>
                                            </span>
                                        </div>
                                    </TableCell>
                                    {application
                                        && (
                                            <TableCell>
                                                {application.throttlingPolicy}
                                                {' '}
                                                {`(${tierDescription})`}
                                            </TableCell>
                                        )}
                                </TableRow>
                            )}
                        <TableRow>
                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                <div className={classes.iconAligner}>
                                    <Icon className={classes.iconOdd}>assignment_turned_in</Icon>
                                    <span className={classes.iconTextWrapper}>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Applications.Details.Overview.workflow.status'
                                                defaultMessage='Workflow Status'
                                            />
                                        </Typography>
                                    </span>
                                </div>
                            </TableCell>
                            <TableCell>
                                {application.status}
                            </TableCell>
                        </TableRow>
                        <TableRow>
                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                <div className={classes.iconAligner}>
                                    <Icon className={classes.iconEven}>account_box</Icon>
                                    <span className={classes.iconTextWrapper}>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Applications.Details.Overview.application.owner'
                                                defaultMessage='Application Owner'
                                            />
                                        </Typography>
                                    </span>
                                </div>
                            </TableCell>
                            <TableCell>
                                {application.owner}
                            </TableCell>
                        </TableRow>
                        {application.attributes
                            && (
                                Object.keys(application.attributes).map((attr, index) => {
                                    const attrValue = application.attributes[attr];
                                    return (
                                        <TableRow key={attr}>
                                            <TableCell component='th' scope='row' className={classes.leftCol}>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classNames(
                                                        { [classes.iconEven]: index % 2 !== 0 },
                                                        { [classes.iconOdd]: index % 2 === 0 },
                                                    )}
                                                    >
                                                        web_asset
                                                    </Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <Typography variant='caption' gutterBottom align='left'>
                                                            {attr}
                                                        </Typography>
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {attrValue}
                                            </TableCell>
                                        </TableRow>
                                    );
                                }))}

                    </TableBody>
                </Table>
                {application.containsSolaceApis === true && environment && (
                    <div className={classes.root}>
                        <Typography id='itest-api-details-bushiness-plans-head' variant='h5'>
                            <FormattedMessage
                                id='solace.application.available.topics.heading'
                                defaultMessage='Available Topics'
                            />
                        </Typography>
                        <Typography variant='caption' gutterBottom>
                            <FormattedMessage
                                id='solace.application.available.topics.subheading'
                                defaultMessage='Topics permitted to access from solace applications'
                            />
                        </Typography>
                        <Paper className={classes.Paper}>
                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <TextField
                                        select
                                        onChange={handleChange}
                                        value={environment.environmentDisplayName}
                                        style={{ maxWidth: '50%' }}
                                        variant='outlined'
                                    >
                                        {application.solaceDeployedEnvironments.map((e) => (
                                            <MenuItem key={e} value={e.environmentDisplayName}>
                                                {e.environmentDisplayName}
                                            </MenuItem>
                                        ))}
                                    </TextField>
                                </Grid>
                                <Grid item xs={6}>
                                    <Paper className={classes.Paper2}>
                                        <Typography id='itest-api-details-bushiness-plans-head' variant='h6'>
                                            <FormattedMessage
                                                id='solace.application.topics.publish'
                                                defaultMessage='Publish Topics'
                                            />
                                        </Typography>
                                        <List className={classes.list}>
                                            {environment.publishTopics.map((t) => (
                                                <ListItem>
                                                    <Typography gutterBottom align='left'>
                                                        {t}
                                                    </Typography>
                                                </ListItem>
                                            ))}
                                        </List>
                                    </Paper>
                                </Grid>
                                <Grid item xs={6}>
                                    <Paper className={classes.Paper2}>
                                        <Typography id='itest-api-details-bushiness-plans-head' variant='h6'>
                                            <FormattedMessage
                                                id='solace.application.topics.subscribe'
                                                defaultMessage='Subscribe Topics'
                                            />
                                        </Typography>
                                        <List className={classes.list}>
                                            {environment.subscribeTopics.map((t) => (
                                                <ListItem>
                                                    <Typography gutterBottom align='left'>
                                                        {t}
                                                    </Typography>
                                                </ListItem>
                                            ))}
                                        </List>
                                    </Paper>
                                </Grid>
                            </Grid>
                        </Paper>
                    </div>
                )}
            </div>
        </>
    );
}
export default injectIntl(Overview);
