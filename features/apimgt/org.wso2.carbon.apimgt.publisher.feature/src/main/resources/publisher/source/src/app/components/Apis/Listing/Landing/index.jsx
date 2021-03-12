import React from 'react';
import {
    useTheme,
} from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import useMediaQuery from '@material-ui/core/useMediaQuery';

import RestAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/RestAPIMenu';
import SoapAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/SoapAPIMenu';
import GraphqlAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/GraphqlAPIMenu';
import StreamingAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/StreamingAPIMenu';
import ServiceCatalogMenu from 'AppComponents/Apis/Listing/Landing/Menus/ServiceCatalogMenu';

const useStyles = makeStyles({
    root: {
        flexGrow: 1,
    },
});

const APILanding = (props) => {
    const { deploying, handleDeploySample } = props;
    const theme = useTheme();
    const matches = useMediaQuery(theme.breakpoints.down('xs'));
    const classes = useStyles();
    const {
        graphqlIcon,
        restApiIcon,
        soapApiIcon,
        streamingApiIcon,
    } = theme.custom.landingPage.icons;

    return (
        <div className={classes.root}>
            <Grid
                container
                direction='column'
                justify='center'
                spacing={5}
            >
                <Grid item xs={12}>
                    <Box pt={matches ? 2 : 7} />
                </Grid>
                {(deploying !== null && handleDeploySample !== null) && (
                    <Grid item md={12}>
                        <Typography display='block' gutterBottom align='center' variant='h4'>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPI.create.new'
                                defaultMessage='Let’s get started !'
                            />
                            <Box color='text.secondary' pt={2}>
                                <Typography display='block' gutterBottom align='center' variant='body1'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.create.new.description'
                                        defaultMessage={'Monitor the API’s lifecycle, documentation, '
                                            + 'security, community, and subscriptions.'}
                                    />
                                </Typography>
                            </Box>

                        </Typography>

                    </Grid>
                )}

                <Grid item xs={12}>
                    <Box pt={matches ? 2 : 7} pb={5} mx={matches ? 12 : 3}>
                        <Grid
                            container
                            direction='row'
                            justify='center'
                            alignItems='flex-start'
                            spacing={3}
                        >
                            <RestAPIMenu icon={restApiIcon} />
                            <SoapAPIMenu icon={soapApiIcon} />
                            <GraphqlAPIMenu icon={graphqlIcon} />
                            <StreamingAPIMenu icon={streamingApiIcon} />
                            {/* <Divider light orientation='vertical' variant='inset' /> */}
                            <ServiceCatalogMenu icon={streamingApiIcon} />
                        </Grid>
                    </Box>
                </Grid>
            </Grid>
        </div>

    );
};

export default APILanding;
