import React from 'react';
import PropTypes from 'prop-types';
/**
 *
 * @class CustomIcon
 * @extends {React.Component}
 */
export default function CustomIcon(props) {
    const {
        className, icon, height, width, strokeColor,
    } = props;
    // todo: create icons for each icon case and return
    // todo: remove default return statement
    // if (icon === 'overview') {
    //     return null;
    // }
    return (
        <svg
            xmlns='http://www.w3.org/2000/svg'
            width={width}
            height={height}
            viewBox='0 0 8.5272856 8.5114363'
            id='svg8'
            className={className}
            style={{ padding: 0 }}
        >
            <g id='layer2' transform='translate(79.857 -62.367)'>
                <g
                    id='g5726'
                    transform='matrix(.9999 0 0 1.00321 -86.091 38.578)'
                    fill='none'
                    stroke={strokeColor}
                    strokeLinejoin='round'
                >
                    <g transform='scale(.88683 .9159) rotate(-45 -45.67 5.272)' id='g5724' strokeWidth='0.539'>
                        <circle id='circle5720' cx='-22.921' cy='63.11' r='2.603' />
                    </g>
                </g>
            </g>
        </svg>
    );
}

CustomIcon.defaultProps = {
    strokeColor: '#8b8e95',
    width: 32,
    height: 32,
    icon: 'api',
    className: '',
};

CustomIcon.propTypes = {
    strokeColor: PropTypes.string,
    width: PropTypes.number,
    height: PropTypes.number,
    icon: PropTypes.oneOf(['overview', 'api']),
    className: '',
};
