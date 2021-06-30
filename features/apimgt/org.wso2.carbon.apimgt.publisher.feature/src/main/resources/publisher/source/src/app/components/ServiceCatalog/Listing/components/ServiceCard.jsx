import React, { useMemo } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useTheme } from '@material-ui/core';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import Configurations from 'Config';
import { FormattedMessage } from 'react-intl';
import Avatar from '@material-ui/core/Avatar';
import { Link as RouterLink } from 'react-router-dom';
import { isRestricted } from 'AppData/AuthManager';
import Link from '@material-ui/core/Link';
import Tooltip from '@material-ui/core/Tooltip';
import Chip from '@material-ui/core/Chip';

import CreateAPIButton from 'AppComponents/ServiceCatalog/CreateApi';
import DeleteServiceButton from 'AppComponents/ServiceCatalog/Listing/Delete';
import LetterGenerator from 'AppComponents/Apis/Listing/components/ImageGenerator/LetterGenerator';


const useStyles = makeStyles((theme) => ({
    root: {
        width: theme.spacing(25),
        height: theme.spacing(35),
        backgroundColor: theme.palette.background.paper,
        '&:hover': {
            cursor: 'pointer',
        },
    },
    media: {
        height: 0,
        paddingTop: '56.25%', // 16:9
        backgroundColor: '#F6F7F9',
        backgroundSize: 'contain',
        boxShadow: '0px 1px 3px #00000033',
    },
    typeIcon: {
        width: theme.spacing(3),
        height: theme.spacing(3),
    },
    usageChip: {
        width: theme.spacing(5),
        // boxShadow: theme.shadows[1], // Lowest shadow
    },
}));

/**
 *
 * @returns
 */
export default function ServiceCard(props) {
    const { service, onDelete } = props;
    const classes = useStyles();
    const theme = useTheme();
    const [raised, setRaised] = React.useState(false);
    /**
     * enum:
          - OAS2
          - OAS3
          - WSDL1
          - WSDL2
          - GRAPHQL_SDL
          - ASYNC_API
     */
    const {
        graphql,
        asyncapi,
        oas3,
        swagger,
    } = theme.custom.serviceCatalog.icons;
    const iconsMapping = useMemo(() => ({
        OAS2: swagger,
        OAS3: oas3,
        GRAPHQL_SDL: graphql,
        ASYNC_API: asyncapi,
    }), []);
    return (
        <Paper
            elevation={raised ? 3 : 1}
            classes={{ root: classes.root }}
            onMouseOver={() => setRaised(true)}
            onMouseOut={() => setRaised(false)}
        >
            <Link
                color='inherit'
                underline='none'
                component={RouterLink}
                to={`/service-catalog/${service.id}/overview`}
            >
                <CardMedia
                    className={classes.media}
                    component={LetterGenerator}
                    width={theme.spacing(25)}
                    height={theme.spacing(15)}
                    artifact={service}
                />
                <Box p={1} pb={0}>
                    <Tooltip placement='top-start' interactive title={service.name}>
                        <Typography display='block' noWrap variant='h5' component='h2'>
                            {service.name}
                        </Typography>
                    </Tooltip>
                    <Box pt={1} pb={3} fontFamily='fontFamily' fontSize='body2.fontSize' color='text.secondary'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.components.ServiceCard.version'
                            defaultMessage='Version'
                        />
                        {' '}
                        <b>{service.version}</b>
                    </Box>
                    <Grid
                        container
                        direction='row'
                        justify='space-between'
                        alignItems='stretch'
                    >
                        <Grid item xs={12}>
                            <Grid
                                container
                                direction='row'
                                justify='flex-start'
                                alignItems='center'
                            >
                                <Grid item>
                                    <Box
                                        fontWeight='fontWeightLight'
                                        fontSize='subtitle1.fontSize'
                                        fontFamily='fontFamily'
                                        color='text.secondary'
                                    >
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.components.ServiceCard.type'
                                            defaultMessage='Type'
                                        />
                                    </Box>
                                </Grid>
                                <Grid item>
                                    <Box pl={1} pr={0.5}>
                                        {iconsMapping[service.definitionType] && (
                                            <Avatar
                                                alt={service.definitionType}
                                                src={`${Configurations.app.context}`
                                                + `${iconsMapping[service.definitionType]}`}
                                                className={classes.typeIcon}
                                            />
                                        )}
                                    </Box>
                                </Grid>
                                <Grid item xs={6}>
                                    <Box fontWeight='fontWeightMedium' color='text.secondary' fontFamily='fontFamily'>
                                        {service.definitionType}
                                    </Box>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Box>
                <Box mt={2}>
                    <Grid
                        container
                        direction='row'
                        justify='space-between'
                        alignItems='center'
                    >
                        <Box pl={1}>
                            <Grid item>
                                <Chip
                                    className={classes.usageChip}
                                    color='primary'
                                    label={(
                                        <Box
                                            fontFamily='fontFamily'
                                            color='text.primary'
                                        >
                                            {service.usage}
                                        </Box>
                                    )}
                                    size='small'
                                    variant='outlined'
                                />
                                <Box
                                    fontFamily='fontFamily'
                                    color='text.primary'
                                    fontSize='body2.fontSize'
                                    display='inline'
                                    ml={1}
                                >
                                    APIs
                                </Box>
                            </Grid>
                        </Box>
                        {!isRestricted(['apim:api_create']) && (
                            <Grid item>
                                <CreateAPIButton
                                    isIconButton
                                    serviceDisplayName={service.name}
                                    serviceKey={service.serviceKey}
                                    definitionType={service.definitionType}
                                    serviceVersion={service.version}
                                    serviceUrl={service.serviceUrl}
                                    usage={service.usage}
                                />
                                <DeleteServiceButton
                                    id='itest-service-card-delete'
                                    serviceDisplayName={service.name}
                                    serviceId={service.id}
                                    onDelete={onDelete}
                                    isIconButton
                                />
                            </Grid>
                        )}
                    </Grid>
                </Box>
            </Link>
        </Paper>
    );
}
