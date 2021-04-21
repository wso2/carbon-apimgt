import React from 'react';

import {
    Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
} from '@material-ui/core/';
import DeleteIcon from '@material-ui/icons/Delete';
import Box from '@material-ui/core/Box';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import API from 'AppData/api';
import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import { FormattedMessage } from 'react-intl';
import classNames from 'classnames';
import { isRestricted } from 'AppData/AuthManager';

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
        color: theme.custom.apis.listing.deleteButtonColor,
        cursor: 'pointer',
        padding: theme.spacing(0.4),
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'center',
        justifyContent: 'center',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
    inlineBlock: {
        display: 'inline-block',
        paddingRight: 10,
    },
    flexBox: {
        display: 'flex',
        paddingRight: 10,
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
        const {
            api: { id, name }, setLoading, updateData, isAPIProduct, history,
        } = this.props;
        if (isAPIProduct) {
            const promisedDelete = API.deleteProduct(id);
            promisedDelete
                .then((response) => {
                    if (response.status !== 200) {
                        Alert.info('Something went wrong while deleting the API Product!');
                        return;
                    }
                    Alert.info(`API Product ${name} deleted Successfully`);
                    if (updateData) {
                        updateData(id);
                        setLoading(false);
                    } else {
                        history.push('/api-products');
                    }
                })
                .catch((error) => {
                    if (error.status === 409) {
                        Alert.error('[ ' + name + ' ] : ' + error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while deleting the API Product!');
                    }
                    setLoading(false);
                });
        } else {
            const promisedDelete = API.delete(id);
            promisedDelete
                .then((response) => {
                    if (response.status !== 200) {
                        Alert.info('Something went wrong while deleting the API!');
                        return;
                    }
                    Alert.info(`API ${name} deleted Successfully`);
                    if (updateData) {
                        updateData(id);
                        setLoading(false);
                    } else {
                        history.push('/apis');
                    }
                })
                .catch((error) => {
                    if (error.status === 409) {
                        Alert.error('[ ' + name + ' ] : ' + error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while deleting the API!');
                    }
                    setLoading(false);
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
            api, onClick, classes, updateData,
        } = this.props;
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
                    <Box
                        className={classNames({ [classes.inlineBlock]: updateData, [classes.flexBox]: !updateData })}
                    >
                        {!updateData && (<VerticalDivider height={70} />)}
                        <Box className={classes.delete}>
                            <IconButton
                                id='itest-id-deleteapi-icon-button'
                                onClick={this.handleRequestOpen}
                                onKeyDown={this.handleRequestOpen}
                                className={classes.delete}
                                disabled={isRestricted(['apim:api_delete'], api)}
                                aria-label='delete'
                                disableFocusRipple
                                disableRipple
                            >
                                <DeleteIcon />
                            </IconButton>
                            <Box
                                fontFamily='fontFamily'
                                fontSize='caption.fontSize'
                                onClick={this.handleRequestOpen}
                                onKeyDown={this.handleRequestOpen}
                            >

                                <FormattedMessage
                                    id='Apis.Details.components.DeleteApiButton.delete'
                                    defaultMessage='Delete'
                                />
                            </Box>
                        </Box>
                    </Box>
                </ScopeValidation>
                <Dialog open={this.state.openMenu}>
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
    setLoading: () => {},
};

DeleteApiButton.propTypes = {
    api: PropTypes.shape({
        delete: PropTypes.func,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    setLoading: PropTypes.func,
    updateData: PropTypes.func.isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default withRouter(withStyles(styles, { withTheme: true })(DeleteApiButton));
