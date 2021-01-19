import React from 'react';

/**
 * Renders a single node.
 * @returns {JSX} Rendered jsx output.
 */
function TimeAxis() {
    return (
        <svg
            xmlns='http://www.w3.org/2000/svg'
            xmlnsXlink='http://www.w3.org/1999/xlink'
            width='270mm'
            height='78.693'
            version='1.1'
            viewBox='0 0 270 20.821'
        >
            <defs>
                <linearGradient id='linearGradient1834'>
                    <stop offset='0' stopColor='#cecece' stopOpacity='1' />
                    <stop offset='1' stopColor='#cecece' stopOpacity='0' />
                </linearGradient>
                <linearGradient
                    id='linearGradient1836'
                    x1='-129.492'
                    x2='-98.827'
                    y1='-139.878'
                    y2='-139.878'
                    gradientUnits='userSpaceOnUse'
                    xlinkHref='#linearGradient1834'
                />
            </defs>
            <g
                fillRule='nonzero'
                stroke='none'
                strokeDasharray='none'
                strokeDashoffset='0'
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeMiterlimit='4'
                strokeOpacity='1'
                paintOrder='stroke markers fill'
                transform='translate(131.668 147.17)'
            >
                <path
                    fill='#010b10'
                    fillOpacity='0'
                    strokeWidth='0.794'
                    d='M-80.085 -92.139H-48.81399999999999V-76.103H-80.085z'
                    opacity='1'
                />
                <circle
                    cx='-130.227'
                    cy='-139.98'
                    r='0.78'
                    fill='#cecece'
                    fillOpacity='1'
                    strokeWidth='0.265'
                    opacity='1'
                />
                <path
                    fill='url(#linearGradient1836)'
                    fillOpacity='1'
                    strokeWidth='0.508'
                    d='M-129.492 -140.224H-98.82799999999999V-139.53199999999998H-129.492z'
                    opacity='1'
                />
            </g>
        </svg>
    );
}

export default TimeAxis;
