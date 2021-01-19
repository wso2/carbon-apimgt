import * as React from 'react';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';

const useStyles = makeStyles(() => ({
    nodeWrapper: {
        display: 'flex',
        position: 'relative',
    },
    nodeTitle: {
        color: '#065361',
    },
    nodeTextLeft: {
        position: 'absolute',
        left: 90,
        textAlign: 'right',
        width: 214,
        fontSize: 11,
    },
    nodeTextRight: {
        position: 'absolute',
        width: 214,
        left: 339,
        textAlign: 'left',
        fontSize: 11,
    },
    nodeTextTop: {
        top: -27,
    },
    nodeTextBottom: {
        top: 24,
    },
}));
/**
 * Renders a single node.
 * @param {JSON} props props passed to the component.
 * @returns {JSX} Rendered jsx output.
 */
function NodeStartEnd(props) {
    const { isTop, isLeft } = props;
    const classes = useStyles();

    return (
        <div className={classes.nodeWrapper}>
            <svg
                xmlns='http://www.w3.org/2000/svg'
                width='170mm'
                height='10mm'
                viewBox='0 0 170 10'
                {...props}
            >
                <g transform='translate(131.668 136.35)' paintOrder='stroke markers fill'>
                    {isLeft && (
                        <>
                            {/* Line left */}
                            {isTop && (<path fill='#010b10' d='M-53.88-126.7h1.078v-7.59h-1.078z' />)}

                            {/* Left top */}
                            <circle cx={-53.344} cy={-134.288} r={2.057} fill='#010b10' />


                        </>
                    )}
                    {!isLeft && (
                        <>
                            {/* Line right */}
                            {isTop && (<path fill='#010b10' d='M-40.509-126.879h1.078v-8.828h-1.078z' />)}

                            {/* Right top */}
                            <circle r={2.057} cy={-134.288} cx={-39.97} fill='#010b10' />


                        </>
                    )}

                </g>
            </svg>

            <div className={clsx(isLeft ? classes.nodeTextLeft : classes.nodeTextRight,
                isTop ? classes.nodeTextTop : classes.nodeTextBottom)}
            >
                <Typography variant='body1'>
                    {isTop ? 'Working Copy' : 'Start'}
                </Typography>
                <Typography variant='caption' />
            </div>
        </div>
    );
}

export default NodeStartEnd;
