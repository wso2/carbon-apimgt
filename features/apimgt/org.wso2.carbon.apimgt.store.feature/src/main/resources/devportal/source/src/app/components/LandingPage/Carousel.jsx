import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Slide from '@material-ui/core/Slide';
import Icon from '@material-ui/core/Icon';
import classNames from 'classnames';
import ReactSafeHtml from 'react-safe-html';
import { app } from 'Settings';

const styles = theme => ({
    root: {
        position: 'relative',
        display: 'flex',
    },
    imageBox: {
        width: '100%',
        height: 'auto',
    },
    arrows: {
        position: 'absolute',
        zIndex: 2,
        display: 'flex',
        flex: 1,
        height: '100%',
        background: '#00000044',
        justifyContent: 'center',
        alignItems: 'center',
        cursor: 'pointer',
        '& span': {
            fontSize: 60,
            color: theme.palette.getContrastText('#000000'),
        },
    },
    arrowLeft: {
        left: 0,
    },
    arrowRight: {
        right: 0,
    },
    slideContainer: {
        width: '100%',
        zIndex: 1,
        display: 'flex',
        flex: 1,
        justifyContent: 'center',
    },
    slideContentWrapper: {
        position: 'absolute',
        background: '#00000044',
        color: theme.palette.getContrastText('#000000'),
        bottom: 0,
        padding: theme.spacing(2),
    },
    slideContentTitle: {
        fontWeight: theme.typography.fontWeightLight,
        fontSize: theme.typography.h3.fontSize,
    },
    slideContentContent: {
        fontWeight: theme.typography.fontWeightLight,
        fontSize: theme.typography.body1.fontSize,
    },
});

function Carousel(props) {
    const { theme } = props;
    const [counter, setCounter] = useState(0);
    const [slideDirection, setSlideDirection] = useState('left');
    const content = theme.custom.landingPage.carousel.slides;
    const handleLeftArrow = () => {
        setSlideDirection('right');
        if (counter === 0) {
            setCounter(content.length - 1);
        } else {
            setCounter(counter - 1);
        }
    };
    const handleRightArrow = () => {
        setSlideDirection('left');
        if (counter === content.length - 1) {
            setCounter(0);
        } else {
            setCounter(counter + 1);
        }
    };
    const { classes } = props;

    return (
        <div className={classes.root}>
            <div className={classNames(classes.arrowLeft, classes.arrows)} onClick={handleLeftArrow}>
                <Icon>chevron_left</Icon>
            </div>
            <div className={classNames(classes.arrowRight, classes.arrows)} onClick={handleRightArrow}>
                <Icon>chevron_right</Icon>
            </div>
            {content.map((slide, index) => (
                <Slide
                    direction={slideDirection}
                    in={counter === index}
                    timeout={{ enter: 500, exit: 0 }}
                    key={index}
                    mountOnEnter
                    unmountOnExit
                >
                    <div className={classes.slideContainer}>
                        <div className={classNames(classes.slideContentWrapper, 'slideContentWrapper')}>
                            <div className={classNames(classes.slideContentTitle, 'slideContentTitle')}><ReactSafeHtml html={slide.title} /></div>
                            <div className={classes.slideContentContent}><ReactSafeHtml html={slide.content} /></div>
                        </div>
                        <img className={classes.imageBox} src={app.context + slide.src} />
                    </div>
                </Slide>
            ))}
        </div>
    );
}

Carousel.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Carousel);
