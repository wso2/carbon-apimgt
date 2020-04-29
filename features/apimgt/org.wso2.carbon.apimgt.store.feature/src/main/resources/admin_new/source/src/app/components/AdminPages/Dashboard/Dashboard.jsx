import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';

const useStyles = makeStyles((theme) => ({
    rootGrid: {
        flexGrow: 1,
    },
    paper: {
        padding: theme.spacing(2),
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
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
}));

export default function Dashboard() {
    const classes = useStyles();
    const bull = <span className={classes.bullet}>•</span>;

    return (
        <ContentBase title='Dashboard' pageStyle='no-paper'>

            <div className={classes.rootGrid}>
                <Grid container spacing={3}>
                    <Grid item xs={12}>
                        <Card className={classes.root}>
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
                    </Grid>
                    <Grid item xs={6}>
                        <Card className={classes.root}>
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
                    </Grid>
                    <Grid item xs={6}>
                        <Card className={classes.root}>
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
                    </Grid>

                </Grid>
            </div>
        </ContentBase>
    );
}
