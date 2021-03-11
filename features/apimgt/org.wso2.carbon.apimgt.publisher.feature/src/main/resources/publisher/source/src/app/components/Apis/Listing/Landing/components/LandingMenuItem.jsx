import React from 'react';
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    lintRoot: {
        color: theme.custom.landingPage.menu.primary,
    },
}));

const LandingMenuItem = (props) => {
    const {
        helperText, children, id, linkTo,
    } = props;
    const { lintRoot } = useStyles();
    return (
        <Box mt={2}>
            <Typography color='primary' variant='subtitle1'>
                <Link
                    className={lintRoot}
                    id={id}
                    to={linkTo}
                >
                    {children}
                </Link>
            </Typography>
            <Box color='text.secondary' fontFamily='fontFamily' fontSize='body2.fontSize'>
                {helperText}
            </Box>
        </Box>
    );
};

export default LandingMenuItem;
