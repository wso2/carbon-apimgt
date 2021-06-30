import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Carousel from './Carousel';
import ApisWithTag from './ApisWithTag';
import ParallaxScroll from './ParallaxScroll';
import Contact from './Contact';

const styles = () => ({
    root: {
        flexGrow: 1,
        margin: '0 100px',
        alignItem: 'center',
    },
    fullWidthBack: {},
    superRoot: {
        display: 'flex',
        flexDirection: 'column',
    },
});
/**
 * Renders landing view..
 * @param {JSON} props Parent pros.
 * @returns {JSX} renders landing view.
 */
function Landing(props) {
    const { classes, theme } = props;
    const {
        custom: {
            landingPage:
            {
                carousel: { active: carouselActive },
                listByTag: { active: listByTagActive, content: listByTagContent },
                parallax: { active: parallaxActive },
                contact: { active: contactActive },
            },
        },
    } = theme;
    return (
        <div className={classes.superRoot}>
            <div className={classes.root}>
                <Grid container spacing={3}>
                    {carouselActive && (
                        <Grid item xs={12}>
                            <Carousel />
                        </Grid>
                    )}
                    {listByTagActive && listByTagContent.length > 0 && (
                        <Grid item xs={12}>
                            <Typography variant='h2' gutterBottom>
                                {listByTagContent[0].title}
                            </Typography>
                            {listByTagContent[0].description && (
                                <Typography variant='body1' gutterBottom>
                                    {listByTagContent[0].description}
                                </Typography>
                            )}
                            <ApisWithTag tag={listByTagContent[0].tag} maxCount={listByTagContent[1].maxCount} />
                        </Grid>
                    )}
                </Grid>
            </div>
            {parallaxActive && (
                <div className={classes.fullWidthBack}>
                    <ParallaxScroll index={0} />
                </div>
            )}
            <div className={classes.root}>
                <Grid container spacing={3}>
                    {listByTagActive && listByTagContent.length > 1 && (
                        <Grid item xs={12}>
                            <Typography variant='h2' gutterBottom>
                                {listByTagContent[1].title}
                            </Typography>
                            {listByTagContent[1].description && (
                                <Typography variant='body1' gutterBottom>
                                    {listByTagContent[1].description}
                                </Typography>
                            )}
                            <ApisWithTag tag={listByTagContent[1].tag} maxCount={listByTagContent[1].maxCount} />
                        </Grid>
                    )}
                </Grid>
            </div>
            {parallaxActive && (
                <div className={classes.fullWidthBack}>
                    <ParallaxScroll index={1} />
                </div>
            )}
            {contactActive && (
                <div className={classes.root}>
                    <Typography variant='h2' gutterBottom>Contact Us</Typography>
                    <Contact />
                </div>
            )}

        </div>
    );
}

Landing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(Landing);
