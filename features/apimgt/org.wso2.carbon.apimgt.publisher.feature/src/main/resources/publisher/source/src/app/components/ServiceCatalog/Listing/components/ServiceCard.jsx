import React, { useMemo } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { useTheme } from '@material-ui/core';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import CardMedia from '@material-ui/core/CardMedia';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';
import Configurations from 'Config';
import { FormattedMessage } from 'react-intl';
import Avatar from '@material-ui/core/Avatar';
import CardActions from '@material-ui/core/CardActions';
import IconButton from '@material-ui/core/IconButton';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import DeleteIcon from '@material-ui/icons/Delete';

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
        // boxShadow: theme.shadows[1], // Lowest shadow
    },
    typeIcon: {
        width: theme.spacing(3),
        height: theme.spacing(3),
    },
    deleteAction: {
        padding: 0,
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
    const handelDelete = (event) => {
        const { id } = event.currentTarget;
        event.preventDefault();
        event.stopPropagation();
        onDelete(id);
    };
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
        <Link color='inherit' underline='none' component={RouterLink} to={`/service-catalog/${service.id}/overview`}>
            <Paper
                elevation={raised ? 3 : 1}
                classes={{ root: classes.root }}
                onMouseOver={() => setRaised(true)}
                onMouseOut={() => setRaised(false)}
            >
                <CardMedia
                    className={classes.media}
                    image={`${Configurations.app.context}/site/public/images/service_catalog/`
                        + 'wso2micro-integrator-active_bright.png'}
                    title='Paella dish'
                />
                <Box p={1} pb={0}>
                    <Typography variant='h5' component='h2'>
                        {service.name}
                        <Box pt={1} pb={3} fontSize='body2.fontSize' color='text.secondary'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.components.ServiceCard.version'
                                defaultMessage='Version'
                            />
                            {' '}
                            {service.version}
                        </Box>
                    </Typography>
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
                                alignItems='flex-end'
                            >
                                <Grid item>
                                    <Box pb={1.2} pr={0.5}>
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
                                    <Box pb={1} pt={0.5} fontFamily='fontFamily'>

                                        {service.definitionType}
                                    </Box>
                                </Grid>

                            </Grid>
                        </Grid>
                    </Grid>
                </Box>
                <Box display='flex' className={classes.deleteAction} pr={1} flexDirection='row-reverse'>
                    <IconButton id={service.id} onClick={handelDelete} aria-label='add to favorites'>
                        <DeleteIcon />
                    </IconButton>
                </Box>
            </Paper>
        </Link>
    );
}
