import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Icon from '@material-ui/core/Icon';
import {FormattedMessage, injectIntl, useIntl} from 'react-intl';
import { app } from 'Settings';
import Loading from 'AppComponents/Base/Loading/Loading';
import API from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Application from 'AppData/Application';
import axios from 'axios';
import InputAdornment from "@material-ui/core/InputAdornment";
import Tooltip from "@material-ui/core/Tooltip";
import CopyToClipboard from "react-copy-to-clipboard";
import IconButton from "@material-ui/core/IconButton";
import TextField from "@material-ui/core/TextField";
import Grid from "@material-ui/core/Grid";
import Chip from "@material-ui/core/Chip";


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
    solaceDiv: {
        padding: theme.spacing(3, 2),
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
    const [deployedToSolace, setDeployedToSolace] = useState(true);
    const [solaceAppDetails, setSolaceAppDetails] = useState(null);
    const [contentCopied, setContentCopied] = useState(false);
    const [copiedContent, setCopiedContent] = useState(null);
    const intl = useIntl();
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
        /*console.log(application);
        const url = "http://ec2-18-157-186-227.eu-central-1.compute.amazonaws.com:3000/v1/WSO2/apps/ElevatorApp";
        const userName = "wso2";
        const password = "hzxVWwFQs2EEK5kK";
        const base64 = require('base-64');
        const headers = new Headers();
        headers.set('Authorization', 'Basic' + base64.encode(userName + ":" + password));
        fetch(url, {method:'GET', headers: headers}).
            then(response => console.log(response));*/
        /*axios.get(url, {headers: headers})
            .then(r => console.log(r))*/
    }, []);
    if (notFound) {
        return <ResourceNotFound />;
    }
    if (!application) {
        return <Loading />;
    }
    const onCopy = (copiedContent) => {
        setContentCopied(true);
        setCopiedContent(copiedContent);

        const caller = function () {
            setContentCopied(false);
        };
        setTimeout(caller, 2000);
    }
    const pathPrefix = '/applications/' + applicationId;
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
                {deployedToSolace && (
                    <div className={classes.solaceDiv}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <Typography variant='h5' className={classes.keyTitle}>
                                    <FormattedMessage
                                        id='solace.application.keys'
                                        defaultMessage='Solace Application Details'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={5}>
                                <Typography>
                                    <FormattedMessage
                                        id='solace.application.keys'
                                        defaultMessage='Access credentials'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={7}>
                                <Typography>
                                    <FormattedMessage
                                        id='solace.application.keys'
                                        defaultMessage='Available endpoints'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={5}>
                                <TextField
                                    disabled
                                    id='consumer-key'
                                    value='key'
                                    margin='dense'
                                    label={(
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.key'
                                            defaultMessage='Consumer Key'
                                        />
                                    )}
                                    fullWidth
                                    variant='outlined'
                                    /*InputProps={{
                                        readOnly: true,
                                        endAdornment: (
                                            <InputAdornment position='end'>
                                                <Tooltip
                                                    title={
                                                        keyCopied
                                                            ? intl.formatMessage({
                                                                defaultMessage: 'Copied',
                                                                id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                            })
                                                            : intl.formatMessage({
                                                                defaultMessage: 'Copy to clipboard',
                                                                id: 'Shared.AppsAndKeys.ViewKeys.copy.to',
                                                            })
                                                    }
                                                    placement='right'
                                                >
                                                    <CopyToClipboard
                                                        text='key'
                                                        onCopy={() => this.onCopy('keyCopied')}
                                                        classes={{ root: classes.iconButton }}
                                                    >
                                                        <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                            <Icon color='secondary'>
                                                                file_copy
                                                            </Icon>
                                                        </IconButton>
                                                    </CopyToClipboard>
                                                </Tooltip>
                                            </InputAdornment>
                                        ),
                                    }}*/
                                />
                                <TextField
                                    disabled
                                    id='consumer-secret'
                                    value='secret'
                                    margin='dense'
                                    label={(
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.secret'
                                            defaultMessage='Consumer secret'
                                        />
                                    )}
                                    fullWidth
                                    variant='outlined'
                                    /*InputProps={{
                                        readOnly: true,
                                        endAdornment: (
                                            <InputAdornment position='end'>
                                                <Tooltip
                                                    title={
                                                        keyCopied
                                                            ? intl.formatMessage({
                                                                defaultMessage: 'Copied',
                                                                id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                            })
                                                            : intl.formatMessage({
                                                                defaultMessage: 'Copy to clipboard',
                                                                id: 'Shared.AppsAndKeys.ViewKeys.copy.to',
                                                            })
                                                    }
                                                    placement='right'
                                                >
                                                    <CopyToClipboard
                                                        text='key'
                                                        onCopy={() => this.onCopy('keyCopied')}
                                                        classes={{ root: classes.iconButton }}
                                                    >
                                                        <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                            <Icon color='secondary'>
                                                                file_copy
                                                            </Icon>
                                                        </IconButton>
                                                    </CopyToClipboard>
                                                </Tooltip>
                                            </InputAdornment>
                                        ),
                                    }}*/
                                />
                            </Grid>
                            <Grid item xs={7}>
                                <Grid container spacing={2} alignItems="center" justify="center">
                                    <Grid item xs={1}>
                                        <Chip label="MQTT" color="primary" size="medium" variant="outlined"/>
                                    </Grid>
                                    <Grid item xs={11}>
                                        <FormattedMessage
                                            id='Shared.AppsAndKeys.ViewKeys.consumer.key'
                                            defaultMessage='Consumer Key'
                                        />
                                        {/*<Tooltip
                                            title={
                                                contentCopied
                                                    ? intl.formatMessage({
                                                        defaultMessage: 'Copied',
                                                        id: 'Shared.AppsAndKeys.KeyConfiguration.copied',
                                                    })
                                                    : intl.formatMessage({
                                                        defaultMessage: 'Copy to clipboard',
                                                        id: 'Shared.AppsAndKeys.KeyConfiguration.copy.to.clipboard',
                                                    })
                                            }
                                            placement='right'
                                            className={classes.iconStyle}
                                        >
                                            <CopyToClipboard
                                                text={copiedContent}
                                                onCopy={onCopy}
                                            >
                                                <IconButton aria-label='Copy to clipboard'
                                                            classes={{ root: classes.iconButton }}>
                                                    <Icon color='secondary'>file_copy</Icon>
                                                </IconButton>
                                            </CopyToClipboard>
                                        </Tooltip>*/}
                                    </Grid>
                                    <Grid item xs={1}>
                                        <Chip label="HTTP" color="primary" size="medium" variant="outlined"/>
                                    </Grid>
                                    <Grid item xs={11}>
                                        <TextField
                                            id='consumer-secret'
                                            value='secret'
                                            margin='dense'
                                            fullWidth
                                            /*InputProps={{
                                                readOnly: true,
                                                endAdornment: (
                                                    <InputAdornment position='end'>
                                                        <Tooltip
                                                            title={
                                                                keyCopied
                                                                    ? intl.formatMessage({
                                                                        defaultMessage: 'Copied',
                                                                        id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                                    })
                                                                    : intl.formatMessage({
                                                                        defaultMessage: 'Copy to clipboard',
                                                                        id: 'Shared.AppsAndKeys.ViewKeys.copy.to',
                                                                    })
                                                            }
                                                            placement='right'
                                                        >
                                                            <CopyToClipboard
                                                                text='key'
                                                                onCopy={() => this.onCopy('keyCopied')}
                                                                classes={{ root: classes.iconButton }}
                                                            >
                                                                <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                                    <Icon color='secondary'>
                                                                        file_copy
                                                                    </Icon>
                                                                </IconButton>
                                                            </CopyToClipboard>
                                                        </Tooltip>
                                                    </InputAdornment>
                                                ),
                                            }}*/
                                        />
                                    </Grid>
                                    <Grid item xs={1}>
                                        <Chip label="AMQP" color="primary" size="medium" variant="outlined"/>
                                    </Grid>
                                    <Grid item xs={11}>
                                        <TextField
                                            id='consumer-secret'
                                            value='secret'
                                            margin='dense'
                                            fullWidth
                                            /*InputProps={{
                                                readOnly: true,
                                                endAdornment: (
                                                    <InputAdornment position='end'>
                                                        <Tooltip
                                                            title={
                                                                keyCopied
                                                                    ? intl.formatMessage({
                                                                        defaultMessage: 'Copied',
                                                                        id: 'Shared.AppsAndKeys.ViewKeys.copied',
                                                                    })
                                                                    : intl.formatMessage({
                                                                        defaultMessage: 'Copy to clipboard',
                                                                        id: 'Shared.AppsAndKeys.ViewKeys.copy.to',
                                                                    })
                                                            }
                                                            placement='right'
                                                        >
                                                            <CopyToClipboard
                                                                text='key'
                                                                onCopy={() => this.onCopy('keyCopied')}
                                                                classes={{ root: classes.iconButton }}
                                                            >
                                                                <IconButton aria-label='Copy to clipboard' classes={{ root: classes.iconButton }}>
                                                                    <Icon color='secondary'>
                                                                        file_copy
                                                                    </Icon>
                                                                </IconButton>
                                                            </CopyToClipboard>
                                                        </Tooltip>
                                                    </InputAdornment>
                                                ),
                                            }}*/
                                        />
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </div>
                )}
            </div>
        </>
    );
}
export default injectIntl(Overview);
