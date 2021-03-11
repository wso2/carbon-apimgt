import React from 'react';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Grid from '@material-ui/core/Grid';
import Configurations from 'Config';

const LandingMenu = (props) => {
    const {
        icon, children, title,
    } = props;
    return (
        <Grid item xs={12} sm={5} md={3} lg={2}>
            <Box bgcolor='white' borderRadius={8} borderColor='grey.300' border={1} boxShadow={1} minHeight={380} p={1}>

                {/* Menu title */}
                <Typography align='left' gutterBottom variant='h6'>
                    {title}
                </Typography>
                <Box mx={-1}>
                    <Divider light variant='full' />
                </Box>

                {/* Menu icon */}
                <Box alignItems='center' mt={2} justifyContent='center' display={{ xs: 'none', sm: 'flex' }}>
                    <img
                        width='90px'
                        src={Configurations.app.context
                            + icon}
                        alt='Rest API'
                    />

                </Box>
                {/* Menu links or buttons */}
                {children}

            </Box>
        </Grid>
    );
};

export default LandingMenu;
