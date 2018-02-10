import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';

class Documentation extends React.Component{
    constructor(props){
        super(props);
    }
    render(){
        return (
            <Paper>
                <Grid container className="tab-grid" spacing={0} justify="center">
                    <Grid item xs={12}>
                        <Typography type="display1" gutterBottom>
                            <span style={{fontSize: "50%"}}>Api Documentation page</span>
                        </Typography>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Documentation