import React, {Component} from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Card from '@material-ui/core/Card';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import {FormattedMessage} from 'react-intl';

const styles = theme => ({
    grid:{
        marginTop:theme.spacing.unit*10
    },
    button:{
        margin:theme.spacing.unit*2
    },
    typography:{
        margin:theme.spacing.unit*2,
        //height:theme.spacing.unit*4
    }

});

class EmptyPage extends Component{
    
    constructor(props){
        super(props);
    }
    
    render(){
        const { classes } = this.props;
        return(
            <div>
                <Grid container justify="center" spacing={24} className={classes.grid}>
                    <Grid item sm={4} >
                        <Card>
                            <Typography gutterBottom variant='headline' component='h4' className={classes.typography}>
                                Add New Document from Inline
                            </Typography>
                            <Typography className={classes.typography}>
                                <FormattedMessage id='You can create a new Document from Inline by clicking the create button below'/>
                            </Typography>
                            <Button className={classes.button} variant="contained">
                                Create
                            </Button>   
                        </Card>
                    </Grid>
                
                    <Grid item sm={4}>
                        <Card>
                            <Typography gutterBottom variant='headline' component='h4' className={classes.typography}>
                                Create New Document from File or URL
                            </Typography>
                            <Typography className={classes.typography}>
                                <FormattedMessage id='You can create a new Document from File or URL by clicking the create button below'/>
                            </Typography>
                            <Button className={classes.button} variant="contained">
                                Create
                            </Button>    
                        </Card>
                    </Grid>
                </Grid>   
                
            </div>
        );   
    }
}
EmptyPage.propTypes = {
    classes: PropTypes.object.isRequired,
};
export default withStyles(styles)(EmptyPage);