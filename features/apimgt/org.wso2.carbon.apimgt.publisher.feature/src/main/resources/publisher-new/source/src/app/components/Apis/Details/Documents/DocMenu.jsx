import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/icons/List';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import { ScopeValidation, resourcePath, resourceMethod } from 'AppData/ScopeValidation';

import DocCreateMenu from './DocCreateMenu';


const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
    btnColor: {
        color: theme.palette.getContrastText(theme.palette.primary.main),
    }
});

const DocMenu = ({ classes, api }) => {
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item>
                <Grid container spacing={16}>
                    <Grid item>
                        <Typography variant='display1' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='documents'
                                defaultMessage='Documents'
                            />
                        </Typography>
                    </Grid>
                    <Grid item>
                        {/* Allowing adding doc to an API based on scopes */}
                        <ScopeValidation resourcePath={resourcePath.API_DOCS} resourceMethod={resourceMethod.POST}>
                            <ApiPermissionValidation userPermissions={api.userPermissionsForApi}>
                                <DocCreateMenu buttonProps={{ size: 'medium', color: 'primary', className: classes.btnColor, variant: 'contained' }}>
                                    <FormattedMessage id='create.btn' defaultMessage='Create' />
                                </DocCreateMenu>
                            </ApiPermissionValidation>
                        </ScopeValidation>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};

DocMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.object.isRequired,
};

export default withStyles(styles)(DocMenu);
