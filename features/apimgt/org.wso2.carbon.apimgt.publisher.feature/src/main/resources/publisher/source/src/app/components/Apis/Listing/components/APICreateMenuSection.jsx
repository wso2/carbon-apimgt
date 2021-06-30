import React from 'react';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';

const APICreateMenuSection = (props) => {
    const {
        title, children,
    } = props;


    return (
        <Grid
            item
            xs={11}
            sm={5}
            md={2}
        >
            <Box mb={2}>
                <Typography
                    variant='h6'
                    align='left'
                >
                    {title}
                </Typography>
            </Box>
            <Grid
                container
                direction='row'
                justify='flex-start'
                alignItems='center'
                spacing={4}
            >
                {/* Menu links or buttons */}
                {children}
            </Grid>
        </Grid>
    );
};

export default APICreateMenuSection;
