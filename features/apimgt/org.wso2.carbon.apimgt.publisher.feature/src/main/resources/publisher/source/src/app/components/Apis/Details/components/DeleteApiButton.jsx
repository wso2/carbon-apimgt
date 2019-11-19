import React from 'react';

import {
    Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
} from '@material-ui/core/';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete';
import Slide from '@material-ui/core/Slide';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api';
import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import { FormattedMessage } from 'react-intl';

const styles = (theme) => ({
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
        paddingRight: theme.spacing(2),
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
        const { api, history } = this.props;
        if (api.apiType === API.CONSTS.APIProduct) {
            API.deleteProduct(api.id).then((response) => {
                if (response.status !== 200) {
                    console.log(response);
                    Alert.error('Something went wrong while deleting the API Product!');
                    return;
                }
                const redirectURL = '/api-products';
                Alert.success('API Product ' + api.name + ' was deleted successfully!');
                history.push(redirectURL);
            }).catch((error) => {
                if (error.status === 409) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while deleting the API Product!');
                }
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
            }).catch((error) => {
                if (error.status === 409) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while deleting the API!');
                }
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
        const { api, onClick, classes } = this.props;
        const type = api.apiType === API.CONSTS.APIProduct ? 'API Product ' : 'API ';
        const version = api.apiType === API.CONSTS.APIProduct ? null : '-' + api.version;
        const deleteHandler = onClick || this.handleApiDelete;

        let path = resourcePath.SINGLE_API;

        if (api.apiType === API.CONSTS.APIProduct) {
            path = resourcePath.SINGLE_API_PRODUCT;
        }

        return (
            <>
                {/* allowing delete based on scopes */}
                <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={path}>
                    <div className={classes.deleteWrapper}>
                        <VerticalDivider height={70} />
                        <a
                            id='itest-id-deleteapi-icon-button'
                            onClick={this.handleRequestOpen}
                            onKeyDown={this.handleRequestOpen}
                            className={classes.delete}
                        >
                            <div>
                                <DeleteIcon />
                            </div>
                            <Typography variant='caption'>
                                <FormattedMessage
                                    id='Apis.Details.components.DeleteApiButton.delete'
                                    defaultMessage='Delete'
                                />
                            </Typography>
                        </a>
                    </div>
                </ScopeValidation>
                <Dialog open={this.state.openMenu} transition={Slide}>
                    <DialogTitle>
                        <FormattedMessage
                            id='Apis.Details.components.DeleteApiButton.title'
                            defaultMessage='Delete {type}'
                            values={{ type }}
                        />
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            <FormattedMessage
                                id='Apis.Details.components.DeleteApiButton.text.description'
                                defaultMessage='{type} <b> {name} {version} </b> will be deleted permanently.'
                                values={{
                                    b: (msg) => <b>{msg}</b>,
                                    type,
                                    name: api.name,
                                    version,
                                }}
                            />
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button dense onClick={this.handleRequestClose}>
                            <FormattedMessage
                                id='Apis.Details.components.DeleteApiButton.button.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                        <Button
                            id='itest-id-deleteconf'
                            onClick={() => {
                                deleteHandler();
                                this.handleRequestClose();
                            }}
                        >
                            <FormattedMessage
                                id='Apis.Details.components.DeleteApiButton.button.delete'
                                defaultMessage='Delete'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </>
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
};

export default withRouter(withStyles(styles, { withTheme: true })(DeleteApiButton));
