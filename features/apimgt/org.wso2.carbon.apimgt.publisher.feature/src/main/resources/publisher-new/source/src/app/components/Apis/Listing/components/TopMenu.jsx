import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {},
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
    content: {
        flexGrow: 1,
    },
    createButton: {
        color: '#000000',
        background: '#15b8cf',
    },
});

/**
 *
 *
 * @class TopMenu
 * @extends {React.Component}
 */
class TopMenu extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            listType: this.props.theme.custom.defaultApiView,
            isAPIProduct: this.props.isAPIProduct,
        };
        this.changeAPIProductProperty = this.changeAPIProductProperty.bind(this);
    }
    /**
     * @inheritDoc
     * @param {@} prevProps
     */
    componentDidUpdate(prevProps) {
        if (this.props.isAPIProduct !== prevProps.isAPIProduct) {
            this.changeAPIProductProperty(this.props.isAPIProduct);
        }
    }
    /**
     * Change state for product
     * @param {*} isProduct
     */
    changeAPIProductProperty(isProduct) {
        this.setState({ isAPIProduct: isProduct });
    }


    /**
     *
     *
     * @returns
     * @memberof TopMenu
     */
    render() {
        const {
            classes, apis, setListType, theme,
        } = this.props;
        const { listType, isAPIProduct } = this.state;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
        return (
            <div className={classes.root}>
                <div className={classes.mainIconWrapper}>
                    <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                </div>
                <div className={classes.mainTitleWrapper}>
                    <Typography variant='display1' className={classes.mainTitle}>
                        {isAPIProduct ? 'API Products' : 'APIs'}
                        <FormattedMessage
                            id='Apis.Listing.components.TopMenu.apis'
                            defaultMessage='APIs'
                        />
                    </Typography>
                    {apis && (
                        <Typography variant='caption' gutterBottom align='left'>
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.displaying'
                                defaultMessage='Displaying'
                            />{' '}
                            {apis.count} {isAPIProduct ? ' API Product(s)' : ' API(s)'}
                            {' '} {apis.count} {' '}
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.api'
                                defaultMessage='API'
                            />
                        </Typography>
                    )}
                </div>
                <VerticalDivider height={70} />
                <div className={classes.APICreateMenu}>
                    {isAPIProduct ? (
                        <Link to='/api-products/create'>
                            <Button variant='contained' className={classes.createButton}>
                                <FormattedMessage id='create.an.api.product' defaultMessage='Create an API Product' />
                            </Button>
                        </Link>
                    ) : (
                        <APICreateMenu
                            buttonProps={{ variant: 'contained', color: 'primary', className: classes.button }}
                        >
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.create.api'
                                defaultMessage='Create API'
                            />
                        </APICreateMenu>
                    )}
                </div>
                <div className={classes.buttonRight}>
                    <IconButton className={classes.button} onClick={() => setListType('list')}>
                        <List color={listType === 'list' ? 'primary' : 'default'} />
                    </IconButton>
                    <IconButton className={classes.button} onClick={() => setListType('grid')}>
                        <GridOn color={listType === 'grid' ? 'primary' : 'default'} />
                    </IconButton>
                </div>
            </div>
        );
    }
}

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    setListType: PropTypes.func.isRequired,
    apis: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
    isAPIProduct: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(TopMenu);
