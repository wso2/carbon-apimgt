import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Icon from '@material-ui/core/Icon';
import MaterialIcons from 'MaterialIcons';
import Background from './Background';

const styles = {
    icon: {},
    iconWrapper: {
        position: 'relative',
        '& span': {
            position: 'absolute',
            left: '50%',
        },
    },
};

/**
 * Generate dynamic API thumbnail image (SVG), Use PureComponent to avoid unnessasary re-rendering when hover ect
 *
 * @class ImageGenerator
 * @extends {PureComponent}
 */
class ImageGenerator extends PureComponent {
    /**
     *
     * @inheritdoc
     * @returns {React.PureComponent} @inheritdoc
     * @memberof ImageGenerator
     */
    render() {
        const {
            classes, api, width, height, theme, fixedIcon,
        } = this.props;

        const {
            category, key, color, backgroundIndex,
        } = fixedIcon;

        const str = api.name;
        let count;
        let colorPair;
        let randomBackgroundIndex;
        let IconElement;
        const colorPairs = theme.custom.thumbnail.backgrounds;

        // Creating the icon
        if (key && category) {
            IconElement = key;
        } else {
            count = MaterialIcons.categories[0].icons.length;
            const randomIconIndex = (str.charCodeAt(0) + str.charCodeAt(str.length - 1)) % count;
            IconElement = MaterialIcons.categories[0].icons[randomIconIndex].id;
        }

        // Obtain or generate background color pair
        if (backgroundIndex && colorPairs.length > backgroundIndex) {
            colorPair = colorPairs[backgroundIndex];
        } else {
            randomBackgroundIndex = (str.charCodeAt(0) + str.charCodeAt(str.length - 1)) % colorPairs.length;
            colorPair = colorPairs[randomBackgroundIndex];
        }

        return (
            <div className={classes.iconWrapper} style={{ width }}>
                <Icon className={classes.icon} style={{ fontSize: height, marginLeft: -height / 2, color }}>
                    {IconElement}
                </Icon>
                <Background width={width} height={height} colorPair={colorPair} />
            </div>
        );
    }
}

ImageGenerator.defaultProps = {
    height: 190,
    width: 250,
    fixedIcon: {
        category: null,
        key: null,
        color: '',
        backgroundIndex: null,
    },
};

ImageGenerator.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    width: PropTypes.number,
    fixedIcon: PropTypes.shape({}),
    api: PropTypes.shape({}).isRequired,
    iconSettings: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(ImageGenerator);
