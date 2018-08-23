import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Chip from '@material-ui/core/Chip';
import { FormattedMessage } from 'react-intl';

import ImageGenerator from './ImageGenerator';

const styles = {
    card: {
        margin: '10px',
        maxWidth: '250px',
        transition: 'box-shadow 0.3s ease-in-out',
    },
};

/**
 *
 * Render API Card component in API listing card view,containing essential API information like name , version ect
 * @class APIThumb
 * @extends {Component}
 */
class APIThumb extends Component {
    /**
     *Creates an instance of ImgMediaCard.
     * @param {*} props
     * @memberof ImgMediaCard
     */
    constructor(props) {
        super(props);
        this.state = { isHover: false };
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
    }

    /**
     *
     *
     * @memberof ImgMediaCard
     */
    toggleMouseOver() {
        this.setState({ isHover: !this.state.isHover });
    }
    /**
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof ImgMediaCard
     */
    render() {
        const { classes, api, deleteButton } = this.props;
        const { isHover } = this.state;

        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classes.card}
            >
                <CardMedia
                    src='None'
                    component={ImageGenerator}
                    height='140'
                    title='Contemplative Reptile'
                    name={api.name}
                    id={api.id}
                />
                <CardContent style={{ padding: '10px' }}>
                    <Typography gutterBottom variant='headline' component='h2'>
                        {api.name}
                    </Typography>
                    <Grid container>
                        <Grid item md={6}>
                            <FormattedMessage id='by' defaultMessage='By' />:
                            <Typography style={{ textTransform: 'capitalize' }} variant='body2' gutterBottom>
                                {api.provider}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage id='context' defaultMessage='Context' />:
                            <Typography variant='body2' gutterBottom>
                                {api.context}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage id='version' defaultMessage='Version' />:
                            <Typography variant='body2'>{api.version}</Typography>
                        </Grid>
                    </Grid>
                </CardContent>
                <CardActions style={{ justifyContent: 'space-between', padding: 0 }}>
                    <Chip label={api.lifeCycleStatus} color='default' />
                    {deleteButton}
                </CardActions>
            </Card>
        );
    }
}

APIThumb.defaultProps = {
    deleteButton: null,
};

APIThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    deleteButton: PropTypes.element,
};

export default withStyles(styles)(APIThumb);
