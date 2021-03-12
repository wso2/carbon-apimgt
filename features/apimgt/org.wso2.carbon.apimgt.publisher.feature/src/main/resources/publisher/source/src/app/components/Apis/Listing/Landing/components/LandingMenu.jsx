import React, { useState } from 'react';
import blue from '@material-ui/core/colors/blue';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { makeStyles } from '@material-ui/core/styles';
import Configurations from 'Config';
import Fade from '@material-ui/core/Fade';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';

const useStyles = makeStyles({
    boxTransition: {
        transition: 'box-shadow 0.9s cubic-bezier(.25,.8,.25,1)',
        cursor: 'pointer',
    },
    overlayBox: {
        cursor: 'auto',
        opacity: 0.9,
        'backdrop-filter': 'blur(10px)',
        outline: 'none',
        'border-color': '#f9f9f9',
        'box-shadow': '0 0 6px 4px #f9f9f9',
        'border-radius': '5px',
    },
    overlayCloseButton: {
        float: 'right',
    },
});

const LandingMenu = (props) => {
    const [isHover, setIsHover] = useState(false);
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [isFadeOut, setIsFadeOut] = useState(true);
    const { boxTransition, overlayBox, overlayCloseButton } = useStyles();
    const onMouseOver = () => {
        setIsHover(true);
    };
    const onMouseOut = () => {
        setIsHover(false);
    };

    const { title, icon, children } = props;
    return (
        <Grid
            item
            xs={12}
            sm={5}
            md={3}
            lg={2}
        >
            <Box
                className={boxTransition}
                onMouseOver={onMouseOver}
                onMouseOut={onMouseOut}
                bgcolor='white'
                justifyContent='center'
                alignItems='center'
                borderRadius={8}
                borderColor='grey.300'
                display='flex'
                border={1}
                boxShadow={isHover ? 4 : 1}
                minHeight={340}
                p={1}
                color={blue[900]}
                fontSize='h4.fontSize'
                fontFamily='fontFamily'
                flexDirection='row'
                onClick={(e) => { setIsCollapsed(true); setIsFadeOut(false); e.preventDefault(); e.stopPropagation(); }}
                position='relative'
            >
                <Grid
                    container
                    direction='row'
                    justify='center'
                    alignItems='center'
                >
                    {title}
                    <Grid item xs={12}>
                        <Box
                            alignItems='center'
                            mt={2}
                            justifyContent='center'
                            display={{ xs: 'none', sm: 'flex' }}
                        >
                            <img
                                width='90px'
                                src={Configurations.app.context
                                    + icon}
                                alt='Rest API'
                            />
                        </Box>
                    </Grid>

                </Grid>

                <Box
                    position='absolute'
                    top={5}
                    left={5}
                    height='97%'
                    bgcolor='#f8f8fb'
                    textAlign='center'
                    width='97%'
                    className={overlayBox}
                    visibility={isFadeOut && 'hidden'}
                >
                    <ClickAwayListener onClickAway={() => {

                    }}
                    >
                        <Fade onExited={() => setIsFadeOut(true)} timeout={{ enter: 500, exit: 150 }} in={isCollapsed}>
                            <Box>
                                <IconButton
                                    className={overlayCloseButton}
                                    onClick={(e) => {
                                        setIsCollapsed(false);
                                        e.preventDefault(); e.stopPropagation();
                                    }}
                                >
                                    <CloseIcon />
                                </IconButton>
                                <Grid
                                    container
                                    direction='row'
                                    justify='center'
                                    alignItems='center'
                                    spacing={6}
                                >
                                    {/* Menu links or buttons */}
                                    {children}
                                </Grid>
                            </Box>
                        </Fade>
                    </ClickAwayListener>
                </Box>
            </Box>
        </Grid>
    );
};

export default LandingMenu;
