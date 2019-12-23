import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import { FormattedMessage, injectIntl } from 'react-intl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import { Link } from 'react-router-dom';
import Divider from '@material-ui/core/Divider';
import ExpansionPanelActions from '@material-ui/core/ExpansionPanelActions';
import { app } from 'Settings';
import Loading from 'AppComponents/Base/Loading/Loading';
import API from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import TokenManager from 'AppComponents/Shared/AppsAndKeys/TokenManager';


const useStyles = makeStyles(theme => ({
    root: {
        padding: theme.spacing(3, 2),
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
        color: theme.custom.infoBar.iconEvenColor,
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
}));

/**
 * Render application overview page.
 * @param {JSON} props Props passed down from parent.
 * @returns {JSX} jsx output from render.
 */
function Overview(props) {
    const classes = useStyles();
    const theme = useTheme();
    const [application, setApplication] = useState(null);
    const [tierDescription, setTierDescription] = useState(null);
    const [notFound, setNotFound] = useState(false);
    const { match: { params: { applicationId } } } = props;
    useEffect(() => {
        const client = new API();
        // Get application
        const promisedApplication = client.getApplication(applicationId);
        promisedApplication
            .then((response) => {
                const promisedTier = client.getTierByName(response.obj.throttlingPolicy, 'application');
                const app = response.obj;
                promisedTier.then((tierResponse) => {
                    setTierDescription(tierResponse.obj.description);
                    setApplication(app);
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
    const { titleIconColor } = theme.custom.overview;
    const { titleIconSize } = theme.custom.overview;
    const pathPrefix = '/applications/' + applicationId;
    console.info(application);
    return (
        <>
            <Paper className={classes.root}>
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
                                                        id='Applications.Details.InfoBar.throttling.tier'
                                                        defaultMessage='Throttling Tier'
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
                                    <Icon className={classes.iconEven}>vpn_key</Icon>
                                    <span className={classes.iconTextWrapper}>
                                        <Typography variant='caption' gutterBottom align='left'>
                                            <FormattedMessage
                                                id='Applications.Details.Overview.token.type'
                                                defaultMessage='Token Type'
                                            />
                                        </Typography>
                                    </span>
                                </div>
                            </TableCell>
                            <TableCell>
                                {application.tokenType}
                            </TableCell>
                        </TableRow>
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
                    </TableBody>
                </Table>
            </Paper>
            <Grid container className={classes.root} spacing={2}>
                <Grid item xs={12} lg={6}>
                    <ExpansionPanel defaultExpanded>
                        <ExpansionPanelSummary classes={{ content: classes.summaryRoot }}>
                            <Icon className={classes.iconEven}>vpn_key</Icon>
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='Applications.Details.Overview.prod.keys.title'
                                    defaultMessage='Production Keys'
                                />
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails
                            classes={{ root: classNames({ [classes.noKeysRoot]: true }) }}
                        >
                            <TokenManager
                                keyType='PRODUCTION'
                                selectedApp={{
                                    appId: application.applicationId,
                                    label: application.name,
                                    tokenType: application.tokenType,
                                    owner: application.owner,
                                    hashEnabled: application.hashEnabled,
                                }}
                                summary
                            />
                        </ExpansionPanelDetails>
                        <Divider />
                        <ExpansionPanelActions className={classes.actionPanel}>
                            <Link to={pathPrefix + '/productionkeys'} className={classes.button}>
                                <Button size='small' color='primary'>
                                    <FormattedMessage
                                        id='Applications.Details.Overview.show.more'
                                        defaultMessage='Manage >>'
                                    />
                                </Button>
                            </Link>
                        </ExpansionPanelActions>
                    </ExpansionPanel>
                </Grid>
                <Grid item xs={12} lg={6}>
                    <ExpansionPanel defaultExpanded>
                        <ExpansionPanelSummary classes={{ content: classes.summaryRoot }}>
                            <Icon className={classes.iconEven}>vpn_key</Icon>
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='Applications.Details.Overview.sand.keys.title'
                                    defaultMessage='Sandbox Keys'
                                />
                            </Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails
                            classes={{ root: classNames({ [classes.noKeysRoot]: true }) }}
                        >
                            <TokenManager
                                keyType='SANDBOX'
                                selectedApp={{
                                    appId: application.applicationId,
                                    label: application.name,
                                    tokenType: application.tokenType,
                                    owner: application.owner,
                                    hashEnabled: application.hashEnabled,
                                }}
                                summary
                            />
                        </ExpansionPanelDetails>
                        <Divider />
                        <ExpansionPanelActions className={classes.actionPanel}>
                            <Link to={pathPrefix + '/sandboxkeys'} className={classes.button}>
                                <Button size='small' color='primary'>
                                    <FormattedMessage
                                        id='Applications.Details.Overview.show.more'
                                        defaultMessage='Manage >>'
                                    />
                                </Button>
                            </Link>
                        </ExpansionPanelActions>
                    </ExpansionPanel>
                </Grid>
            </Grid>
        </>
    );
}
export default injectIntl(Overview);
