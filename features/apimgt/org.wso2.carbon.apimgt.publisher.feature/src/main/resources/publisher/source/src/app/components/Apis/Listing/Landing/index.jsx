import React from 'react';
import {
    useTheme,
} from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
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
    dividerCls: {
        height: '180px',
        position: 'absolute',
        top: '50%',
        '-ms-transform': 'translateY(-50%)',
        transform: 'translateY(-50%)',
        margin: 'auto',
    },
});

const APILanding = () => {
    const theme = useTheme();
    const matches = useMediaQuery(theme.breakpoints.down('xs'));
    const { dividerCls, root } = useStyles();
    const {
        graphqlIcon,
        restApiIcon,
        soapApiIcon,
        streamingApiIcon,
    } = theme.custom.landingPage.icons;

    return (
        <div className={root}>
            <Grid
                container
                direction='column'
                justify='center'
                spacing={5}
            >
                <Grid item xs={12}>
                    <Box pt={matches ? 2 : 7} />
                </Grid>
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
                            <Box display={{ xs: 'none', lg: 'block' }} mx={5}>
                                <Divider className={dividerCls} light orientation='vertical' variant='inset' />
                            </Box>
                            <ServiceCatalogMenu icon={streamingApiIcon} />
                        </Grid>
                    </Box>
                </Grid>
            </Grid>
        </div>

    );
};

export default APILanding;
