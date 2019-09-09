import React, { useEffect, useState, useContext } from 'react';
import Tenants from 'AppData/Tenants';
import { Link } from 'react-router-dom';
import Settings from 'AppComponents/Shared/SettingsContext';
import { withStyles, withTheme } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import { classes } from 'istanbul-lib-coverage';
import Typography from '@material-ui/core/Typography';


const styles = theme => ({
    root: {
        flexGrow: 1,
        display: 'flex',
        background: theme.palette.background.default,
        height: '100%',

    },
    paper: {
        padding: theme.spacing.unit * 2,
        textAlign: 'center',
        color: theme.palette.text.secondary,
        margin: 'auto',
        '-webkit-box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
        '-moz-box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
        'box-shadow': '0px 0px 2px 0px rgba(0,0,0,0.5)',
    },
    list: {
        background: theme.palette.background.paper,
        display: 'block',
        // height: theme.spacing.unit * 12,
    },
    listItem: {
        margin: 'auto',
    },
});


const tenantListing = (props) => {
    // const [tenantList, setTenantList] = useState([]);
    const settingContext = useContext(Settings);
    const { tenantList, classes, theme } = props;
    console.info('inside funciotn', theme);
    useEffect(() => {
        // const tenantApi = new Tenants();
        // tenantApi.getTenantsByState().then((response) => {
        //     setTenantList(response.body);
        // }).catch((error) => {
        //     console.log('error when getting tenants ' + error);
        // });
    }, []);
    console.log(tenantList);
    console.log(settingContext);
    return (
        <div className={classes.root}>
            {/* <Grid container md={6} justify='center' spacing={0} className={classes.list}>

                <Grid item xs={12} md={5} className={classes.listItem}>
                    <Paper elevation={0} square className={classes.paper}>
                        <Typography noWrap>carbon1</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={5} className={classes.listItem}>
                    <Paper elevation={0} square className={classes.paper}>
                        <Typography noWrap>carbon2</Typography>
                    </Paper>
                </Grid>


            </Grid> */}
            <Grid container md={6} justify='center' spacing={0} className={classes.list}>
                {tenantList.map(({ domain }) => {
                    return (
                    // <Link to={`/apis?tenant=${domain}`} onClick={() => settingContext.setTenantDomain(domain)}>
                    // {domain}
                    // {' '}
                    // </Link>
                        <Grid item xs={12} md={5} className={classes.listItem}>
                            <Paper elevation={0} square className={classes.paper}>
                                <Typography noWrap>{domain}</Typography>
                            </Paper>
                        </Grid>
                    );
                })}
            </Grid>
        </div>
    );
};

export default withStyles(styles, { withTheme: true })(tenantListing);
