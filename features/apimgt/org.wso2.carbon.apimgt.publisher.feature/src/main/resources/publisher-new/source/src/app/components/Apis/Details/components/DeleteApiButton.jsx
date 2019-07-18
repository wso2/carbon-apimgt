import React from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core/';
import DeleteIcon from '@material-ui/icons/Delete';
import Slide from '@material-ui/core/Slide';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';

import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
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
    deleteWrapper: {
        flex: 0,
        display: 'flex',
        justifyContent: 'flex-end',
        paddingRight: theme.spacing.unit * 2,
    },
    delete: {
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'center',
        color: theme.custom.deleteButtonColor,
        justifyContent: 'center',
        cursor: 'pointer',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
});

/**
 * Handle Delete an API from API Overview/Details page
 *
 * @class DeleteApiButton
 * @extends {React.Component}
 */
class DeleteApiButton extends React.Component {
    /**
     *Creates an instance of DeleteApiButton.
     * @param {*} props @inheritDoc
     * @memberof DeleteApiButton
     */
    constructor(props) {
        super(props);
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
        this.state = { openMenu: false };
    }

    /**
     * Handle Delete button close event
     *
     * @memberof DeleteApiButton
     */
    handleRequestClose() {
        this.setState({ openMenu: false });
    }

    /**
     * Handle Delete button onClick event
     *
     * @memberof DeleteApiButton
     */
    handleRequestOpen() {
        this.setState({ openMenu: true });
    }

    /**
     *
     * Send API delete REST API request
     * @param {*} e
     * @memberof DeleteApiButton
     */
    handleApiDelete() {
        const { api, history, isAPIProduct } = this.props;
        if (isAPIProduct) {
            api.deleteProduct().then((response) => {
                if (response.status !== 200) {
                    console.log(response);
                    Alert.error('Something went wrong while deleting the API Product!');
                    return;
                }
                const redirectURL = '/api-products';
                Alert.success('API Product ' + api.name + ' was deleted successfully!');
                history.push(redirectURL);
            });
        } else {
            api.delete().then((response) => {
                if (response.status !== 200) {
                    console.log(response);
                    Alert.error('Something went wrong while deleting the API!');
                    return;
                }
                const redirectURL = '/apis';
                Alert.success('API ' + api.name + ' was deleted successfully!');
                history.push(redirectURL);
            });
        }
    }

    /**
     *
     * @inheritDoc
     * @returns {React.Component} inherit docs
     * @memberof DeleteApiButton
     */
    render() {
        const {
            api, onClick, classes, isAPIProduct,
        } = this.props;
        const deleteHandler = onClick || this.handleApiDelete;
        return (
            <React.Fragment>
                {/* allowing delete based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={resourcePath.SINGLE_API}>
                    <div className={classes.deleteWrapper}>
                        <VerticalDivider height={70} />
                        <a
                            onClick={this.handleRequestOpen}
                            onKeyDown={this.handleRequestOpen}
                            className={classes.delete}
                        >
                            <div>
                                <DeleteIcon />
                            </div>
                            <div className={classes.linkText}>Delete</div>
                        </a>
                    </div>
                </ScopeValidation>
                <Dialog open={this.state.openMenu} transition={Slide}>
                    <DialogTitle>Confirm</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Are you sure you want to delete the {isAPIProduct ? 'API Product ' : 'API '}
                            ({api.name} {isAPIProduct ? null : '-' + api.version}
                            )?
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button dense variant='outlined' color='secondary' onClick={deleteHandler}>
                            Delete
                        </Button>
                        <Button dense onClick={this.handleRequestClose}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </React.Fragment>
        );
    }
}

DeleteApiButton.defaultProps = {
    onClick: false,
};

DeleteApiButton.propTypes = {
    api: PropTypes.shape({
        delete: PropTypes.func,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    onClick: PropTypes.func,
    classes: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default withRouter(withStyles(styles, { withTheme: true })(DeleteApiButton));
