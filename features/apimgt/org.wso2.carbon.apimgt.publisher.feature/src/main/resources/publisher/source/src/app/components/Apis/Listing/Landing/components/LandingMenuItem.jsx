import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    lintRoot: {
        color: theme.custom.landingPage.menu.primary,
        '&:hover': {
            backgroundColor: '#0B78F014',
            padding: '2px',
            textDecoration: 'none',
        },
    },
}));

const LandingMenuItem = (props) => {
    const {
        helperText, children, id, linkTo, component = 'Link', onClick,
    } = props;
    const { lintRoot } = useStyles();
    return (
        <Grid
            item
            xs={12}
        >
            <Typography
                color='primary'
                variant='h6'
            >
                {/* Using React Router Links with Material-UI Links
                Pattern as suggested in https://material-ui.com/guides/composition/#link */}
                {component.toLowerCase() === 'link' && (
                    <Link
                        className={lintRoot}
                        id={id}
                        component={RouterLink}
                        to={linkTo}
                    >
                        {children}
                    </Link>
                )}
                {component.toLowerCase() === 'button' && (
                    <Button onClick={onClick} color='primary' variant='outlined'>{children}</Button>
                )}

            </Typography>
            <Box color='text.secondary' fontFamily='fontFamily' fontSize='body2.fontSize'>
                {helperText}
            </Box>
        </Grid>
    );
};

export default LandingMenuItem;
