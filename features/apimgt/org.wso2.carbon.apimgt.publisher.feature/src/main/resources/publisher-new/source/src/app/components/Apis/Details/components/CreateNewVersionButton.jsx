import React from 'react';

import LibraryAdd from '@material-ui/icons/LibraryAdd';
import PropTypes from 'prop-types';
import { Link, withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';

import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';

const styles = theme => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
    },
    backLink: {
        alignItems: 'center',
        textDecoration: 'none',
        display: 'flex',
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    backText: {
        color: theme.palette.primary.main,
        cursor: 'pointer',
        fontFamily: theme.typography.fontFamily,
    },
    createNewVersionWrapper: {
        flex: 1,
        display: 'flex',
        justifyContent: 'flex-end',
        paddingRight: theme.spacing.unit * 2,
    },
    createNewVersion: {
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        color: theme.custom.createNewVersionButtonColor || 'inherit',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
});

/**
 * Handle Creation a new version of an API from API Overview/Details page
 *
 * @class CreateNewVersionButton
 * @extends {React.Component}
 */
class CreateNewVersionButton extends React.Component {
    /**
     *Creates an instance of CreateNewVersionButton.
     * @param {*} props @inheritDoc
     * @memberof CreateNewVersionButton
     */
    constructor(props) {
        super(props);
    }

    /**
     *
     * @inheritDoc
     * @returns {React.Component} inherit docs
     * @memberof CreateNewVersionButton
     */
    render() {
        const { api, classes } = this.props;
        return (
            <React.Fragment>
                {/* allowing create new version based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.API_COPY}>
                    <div className={classes.createNewVersionWrapper}>
                        <VerticalDivider height={70} />
                        <Link
                            className={classes.createNewVersion}
                            to={'/apis/' + api.id + '/new_version'}
                        >

                            <div>
                                <LibraryAdd />
                            </div>
                            <div className={classes.linkText}>Create New Version</div>
                        </Link>
                    </div>
                </ScopeValidation>
            </React.Fragment>
        );
    }
}

CreateNewVersionButton.propTypes = {
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withRouter(withStyles(styles, { withTheme: true })(CreateNewVersionButton));
