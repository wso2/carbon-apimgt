import React, { useEffect, useTheme } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import Api from 'AppData/api';

const useStyles = makeStyles({
    root: {
        minWidth: 275,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
});

export default function APIThumbPlain(props) {
    const classes = useStyles();
    const bull = <span className={classes.bullet}>•</span>;
    useEffect(() => {
        const { api } =props;
        const restApi = new Api();

        const promisedThumbnail = restApi.getAPIThumbnail(api.id);

        promisedThumbnail.then((response) => {
            if (response && response.data) {
                if (response.headers['content-type'] === 'application/json') {
                    const iconJson = JSON.parse(response.data);
                    this.setState({
                        selectedIcon: iconJson.key,
                        category: iconJson.category,
                        color: iconJson.color,
                        backgroundIndex: iconJson.backgroundIndex,
                    });
                } else if (response && response.data.size > 0) {
                    const url = windowURL.createObjectURL(response.data);
                    this.setState({ imageObj: url });
                }
            }
        }).finally(() => {
            this.setState({ imageLoaded: true })
        })
    }, []);
    return (
        <Card className={classes.root} variant="outlined">
            <CardContent>
                <Typography className={classes.title} color="textSecondary" gutterBottom>
                    Word of the Day
        </Typography>
                <Typography variant="h5" component="h2">
                    be{bull}nev{bull}o{bull}lent
        </Typography>
                <Typography className={classes.pos} color="textSecondary">
                    adjective
        </Typography>
                <Typography variant="body2" component="p">
                    well meaning and kindly.
          <br />
                    {'"a benevolent smile"'}
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small">Learn More</Button>
            </CardActions>
        </Card>
    );
}
