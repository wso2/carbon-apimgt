import * as React from 'react';
import Typography from '@material-ui/core/Typography';
import { makeStyles, useTheme } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
        maxWidth: theme.custom.contentAreaWidth,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    historyHead: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    contentWrapper: {
        display: 'flex',
        justifyContent: 'left',
        padding: 50,
    },
    nodeWrapper: {
        display: 'flex',
        position: 'relative',
    },
    nodeTitle: {
        color: '#065361',
    },
    nodeTextLeft: {
        position: 'absolute',
        left: 0,
        top: 12,
        textAlign: 'right',
        width: 214,
        fontSize: 11,
    },
    nodeTextRight: {
        position: 'absolute',
        left: 425,
        top: 12,
        textAlign: 'left',
        width: 214,
        fontSize: 11,
    },
}));

/**
 * Renders a single node.
 * @param {JSON} props props passed to the component.
 * @returns {JSX} Rendered jsx output.
 */
function Node(props) {
    const classes = useStyles();
    const {
        title, description, isLeft, isRevisionNode,
    } = props;
    let label = '';
    if (description.indexOf('PUT') !== -1) {
        label = 'PUT';
    } else if (description.indexOf('POST') !== -1) {
        label = 'POST';
    }

    const theme = useTheme();

    let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[label.toString().toLowerCase()] : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properlly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[label.toString().toLowerCase()]);
    }
    return (
        <div className={classes.nodeWrapper}>
            <svg
                xmlns='http://www.w3.org/2000/svg'
                xmlnsXlink='http://www.w3.org/1999/xlink'
                width='170mm'
                height='78.693'
                version='1.1'
                viewBox='0 0 170 20.821'
            >
                <defs>
                    <linearGradient id='linearGradient1230-5'>
                        <stop offset='0' stopColor='#dfdfdf' stopOpacity='1' />
                        <stop offset='1' stopColor='#eaeaea' stopOpacity='1' />
                    </linearGradient>
                    <linearGradient
                        id='linearGradient1582'
                        x1='-66.699'
                        x2='-48.205'
                        y1='51.693'
                        y2='51.693'
                        gradientUnits='userSpaceOnUse'
                        xlinkHref='#linearGradient1230-5'
                    />
                    <linearGradient
                        id='linearGradient2160'
                        x1='-66.699'
                        x2='-48.205'
                        y1='51.693'
                        y2='51.693'
                        gradientUnits='userSpaceOnUse'
                        xlinkHref='#linearGradient1230-5'
                    />
                </defs>
                <g stroke='none' transform='translate(131.668 147.17)'>
                    {isLeft && (
                        <>
                            <path
                                fill='#010b10'
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                strokeWidth='0.337'
                                d='M-53.88 126.349H-52.802V134.29H-53.88z'
                                opacity='1'
                                paintOrder='stroke markers fill'
                                transform='scale(1 -1)'
                            />
                            <g
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                paintOrder='stroke markers fill'
                                transform='matrix(-.72197 0 0 .72197 -94.82 -177.791)'
                            >
                                <circle
                                    cx='-57.452'
                                    cy='51.693'
                                    r='9.247'
                                    fill='url(#linearGradient1582)'
                                    strokeWidth='0.694'
                                    opacity='1'
                                />
                                <circle
                                    cx='-57.452'
                                    cy='51.693'
                                    r='7.182'
                                    fill={chipColor}
                                    strokeWidth='0.539'
                                    opacity='1'
                                />
                            </g>
                            <path
                                fill={chipColor}
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                strokeWidth='0.056'
                                d='M-71.521-140.7a.747.747 0 00-.747.748.747.747 0 00.747.746.747.747 0 00.617-.326h8.385a.747.747 0 00.617.326.747.747 0 00.747-.746.747.747 0 00-.747-.747.747.747 0 00-.695.475h-8.229a.747.747 0 00-.695-.475z'
                                opacity='1'
                                paintOrder='stroke markers fill'
                            />
                            <text
                                x='-53.2'
                                y='-139.263'
                                style={{
                                    lineHeight: '1.25',
                                    InkscapeFontSpecification: "'Open Sans, Normal'",
                                    fontVariantLigatures: 'normal',
                                    fontVariantCaps: 'normal',
                                    fontVariantNumeric: 'normal',
                                    fontFeatureSettings: 'normal',
                                    WebkitTextAlign: 'start',
                                    textAlign: 'start',
                                    textAnchor: 'middle',
                                }}
                                fill='#eee'
                                fillOpacity='1'
                                strokeWidth='0.115'
                                fontFamily='Open Sans'
                                fontSize='3.375'
                                fontStretch='normal'
                                fontStyle='normal'
                                fontVariant='normal'
                                fontWeight='normal'
                                letterSpacing='0'
                                textAnchor='start'
                                wordSpacing='0'
                                writingMode='lr-tb'
                                xmlSpace='preserve'
                            >
                                <tspan
                                    x='-53.2'
                                    y='-139.263'
                                    fill='#eee'
                                    fillOpacity='1'
                                    strokeWidth='0.115'
                                >
                                    {label}
                                </tspan>
                            </text>
                        </>
                    )}
                    {!isLeft && (
                        <>
                            <path
                                fill={chipColor}
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                strokeWidth='0.056'
                                d='M-31.445-141.008a.747.747 0 00-.746.747.747.747 0 00.746.747.747.747 0 00.617-.327h8.387a.747.747 0 00.616.327.747.747 0 00.747-.747.747.747 0 00-.747-.747.747.747 0 00-.695.475h-8.229a.747.747 0 00-.696-.475z'
                                opacity='1'
                                paintOrder='stroke markers fill'
                            />
                            <path
                                fill='#010b10'
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                strokeWidth='0.337'
                                d='M-40.509 126.349H-39.431V134.29H-40.509z'
                                opacity='1'
                                paintOrder='stroke markers fill'
                                transform='scale(1 -1)'
                            />
                            <g
                                fillOpacity='1'
                                fillRule='nonzero'
                                strokeDasharray='none'
                                strokeDashoffset='0'
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeMiterlimit='4'
                                strokeOpacity='1'
                                paintOrder='stroke markers fill'
                                transform='matrix(-.72197 0 0 .72197 -81.449 -177.791)'
                            >
                                <circle
                                    cx='-57.452'
                                    cy='51.693'
                                    r='9.247'
                                    fill='url(#linearGradient2160)'
                                    strokeWidth='0.694'
                                    opacity='1'
                                />
                                <circle
                                    cx='-57.452'
                                    cy='51.693'
                                    r='7.182'
                                    fill={chipColor}
                                    strokeWidth='0.539'
                                    opacity='1'
                                />
                            </g>
                            <text
                                xmlSpace='preserve'
                                style={{
                                    lineHeight: '1.25',
                                    InkscapeFontSpecification: "'Open Sans, Normal'",
                                    fontVariantLigatures: 'normal',
                                    fontVariantCaps: 'normal',
                                    fontVariantNumeric: 'normal',
                                    fontFeatureSettings: 'normal',
                                    WebkitTextAlign: 'start',
                                    textAlign: 'start',
                                    textAnchor: 'middle',
                                }}
                                x='-44.31'
                                y='-139.263'
                                fill='#eee'
                                fillOpacity='1'
                                strokeWidth='0.115'
                                fontFamily='Open Sans'
                                fontSize='3.375'
                                fontStretch='normal'
                                fontStyle='normal'
                                fontVariant='normal'
                                fontWeight='normal'
                                letterSpacing='0'
                                textAnchor='start'
                                wordSpacing='0'
                                writingMode='lr-tb'
                            >
                                <tspan
                                    x='-40'
                                    y='-139.263'
                                    fill='#eee'
                                    fillOpacity='1'
                                    strokeWidth='0.115'
                                >
                                    {label}
                                </tspan>
                            </text>
                        </>
                    )}
                    {(isLeft && isRevisionNode) && (
                        <path
                            fill='#010b10'
                            fillOpacity='1'
                            fillRule='nonzero'
                            strokeDasharray='none'
                            strokeDashoffset='0'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                            strokeMiterlimit='4'
                            strokeOpacity='1'
                            strokeWidth='0.321'
                            d='M-40.508-147.17v6.161h-6.118v1.078c2.361-.003 4.911.003 7.195.002v-7.241z'
                            opacity='1'
                            paintOrder='stroke markers fill'
                        />
                    )}
                    {(!isLeft && isRevisionNode) && (
                        <path
                            fill='#010b10'
                            fillOpacity='1'
                            fillRule='nonzero'
                            strokeDasharray='none'
                            strokeDashoffset='0'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                            strokeMiterlimit='4'
                            strokeOpacity='1'
                            strokeWidth='0.321'
                            d='M-52.803-147.17v6.161h6.118v1.078c-2.361-.003-4.911.003-7.195.002v-7.241z'
                            opacity='1'
                            paintOrder='stroke markers fill'
                        />
                    )}
                </g>
            </svg>
            <div className={isLeft ? classes.nodeTextLeft : classes.nodeTextRight}>
                <Typography variant='body1'>{title}</Typography>
                <Typography variant='caption'>{description}</Typography>
            </div>
        </div>

    );
}

export default Node;
