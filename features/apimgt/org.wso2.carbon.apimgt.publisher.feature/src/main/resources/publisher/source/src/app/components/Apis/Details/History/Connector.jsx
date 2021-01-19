import * as React from 'react';

function Connector(props) {
    return (
        <svg
            xmlns='http://www.w3.org/2000/svg'
            width={50.375}
            height={14.828}
            viewBox='0 0 13.328 3.923'
            {...props}
        >
            <path
                d='M12.251 0v1.423H0v2.5h1.076V2.5h12.252V0z'
                fill='#010b10'
                paintOrder='stroke markers fill'
            />
        </svg>
    );
}

export default Connector;
