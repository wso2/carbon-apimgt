
import React, { useState } from 'react';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { makeStyles } from '@material-ui/core/styles';
import Configurations from 'Config';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';

const useStyles = makeStyles((theme) => ({
    boxTransition: {
        transition: 'box-shadow 0.9s cubic-bezier(.25,.8,.25,1)',
        cursor: 'pointer',
    },
    overlayBox: {
        cursor: 'auto',
        outline: 'none',
        'border-color': '#f9f9f9', // TODO: take from theme ~tmkb
        'box-shadow': '0 0 6px 4px #f9f9f9',
        'border-radius': '5px',
    },
    overlayCloseButton: {
        float: 'right',
    },
    actionStyle: {
        paddingLeft: theme.spacing(4),
        paddingRight: theme.spacing(4),
    },
}));


const RestAPIMenu = (props) => {
    const {
        to, iconName, name, disabled, id,
    } = props;
    const { boxTransition } = useStyles();
    const [isHover, setIsHover] = useState(false);
    const onMouseOver = () => {
        setIsHover(true && !disabled);
    };
    const onMouseOut = () => {
        setIsHover(false);
    };
    const span = ({ children }) => <>{children}</>;
    const Component = disabled ? span : Link;

    return (

        <Grid
            item
            xs={12}
            sm={5}
            md={3}
        >
            <Component
                id={id}
                color='inherit'
                underline='none'
                component={RouterLink}
                to={to}
            >
                <Box
                    className={boxTransition}
                    onMouseOver={onMouseOver}
                    onMouseOut={onMouseOut}
                    bgcolor='background.paper'
                    justifyContent='center'
                    alignItems='center'
                    borderRadius={8}
                    borderColor='grey.300'
                    display='flex'
                    border={1}
                    boxShadow={isHover ? 4 : 1}
                    minHeight={440}
                    p={1}
                    fontSize='h4.fontSize'
                    fontFamily='fontFamily'
                    flexDirection='row'
                    position='relative'
                >
                    <Grid
                        container
                        direction='column'
                        justify='space-between'
                        alignItems='center'
                    >
                        <Grid item xs={12}>
                            <Box
                                alignItems='center'
                                justifyContent='center'
                            >
                                <img
                                    width='190px'
                                    src={Configurations.app.context
                                        + iconName}
                                    alt={name}
                                    aria-hidden='true'
                                />
                            </Box>
                        </Grid>
                        <Grid item xs={12} />
                        <Grid item xs={11}>
                            <Box
                                alignItems='center'
                                pt={15}
                                justifyContent='center'
                                color={disabled ? 'disabled' : 'primary.main'}
                            >
                                {name}
                            </Box>
                        </Grid>
                        <Grid item xs={11}>
                            {disabled && (
                                <Box>
                                    <Typography variant='body2' color='primary'>
                                        *You are not authorized to create or update
                                        {' '}
                                        {name.toLowerCase()}
                                        {' '}
                                        due to insufficient permissions
                                    </Typography>
                                </Box>
                            )}
                        </Grid>
                    </Grid>
                </Box>
            </Component>
        </Grid>
    );
};

RestAPIMenu.defaultProps = {
    iconName: 'add_circle',
    disabled: false,
};

export default RestAPIMenu;
