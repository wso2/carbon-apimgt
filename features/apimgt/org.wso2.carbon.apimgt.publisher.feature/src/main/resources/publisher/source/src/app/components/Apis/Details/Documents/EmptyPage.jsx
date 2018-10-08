import React from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Card from '@material-ui/core/Card';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    grid:{
        marginTop:theme.spacing.unit*10
    },
    button:{
        margin:theme.spacing.unit*2
    },
    typography:{
        margin:theme.spacing.unit*2
    }
});

function EmptyPage(props) {
        const { classes } = props;
        return(
            <Fragment>
                <Grid container justify="center" spacing={24} className={classes.grid}>
                    <Grid item sm={4} >
                        <Card>
                            <Typography gutterBottom variant='headline' component='h4' className={classes.typography}>
                                <FormattedMessage id='create.doc.inline.title'
                                    DefaultMessage='Create New Document from Inline'/>
                            </Typography>
                            <Typography className={classes.typography}>
                                <FormattedMessage id='create.document.inline'
                                   DefaultMessage='Documenting your API makes your consumers experience even better. Create a new document from Inline'/>
                            </Typography>
                            <Button className={classes.button} variant="contained">
                                <FormattedMessage id='create.btn'
                                   DefaultMessage='Create'/>
                            </Button>
                        </Card>
                    </Grid>

                    <Grid item sm={4}>
                        <Card>
                            <Typography gutterBottom variant='headline' component='h4' className={classes.typography}>
                                <FormattedMessage id='create.doc.fileurl.title'
                                   DefaultMessage='Create New Document from File or URL'/>
                            </Typography>
                            <Typography className={classes.typography}>
                                <FormattedMessage id='create.document.fileUrl'
                                 DefaultMeassage="Documenting your API makes your consumers experience even better. Create a new document by File or URL"/>
                            </Typography>
                            <Button className={classes.button} variant="contained">
                                <FormattedMessage id='create.btn'
                                DefaultMessage='Create'/>
                            </Button>
                        </Card>
                    </Grid>
                </Grid>
            </Fragment>
        );
}
EmptyPage.propTypes = {
    classes: PropTypes.object.isRequired,
};
export default withStyles(styles)(EmptyPage);