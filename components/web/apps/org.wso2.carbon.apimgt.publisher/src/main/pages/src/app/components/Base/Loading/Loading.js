import React from 'react'

const LoadingAnimation = (props) => {
    return (
        <div>
            <div style={{height: 200}} data-toggle="loading" data-loading-inverse="true" className="loading">
                <div className="loading-animation">
                    <div className="logo">
                        <svg x="0px" y="0px" viewBox="0 0 14 14">
                            <path className="circle"
                                  d="M6.534,0.748C7.546,0.683,8.578,0.836,9.508,1.25 c1.903,0.807,3.339,2.615,3.685,4.654c0.244,1.363,0.028,2.807-0.624,4.031c-0.851,1.635-2.458,2.852-4.266,3.222 c-1.189,0.25-2.45,0.152-3.583-0.289c-1.095-0.423-2.066-1.16-2.765-2.101C1.213,9.78,0.774,8.568,0.718,7.335 C0.634,5.866,1.094,4.372,1.993,3.207C3.064,1.788,4.76,0.867,6.534,0.748z"/>
                            <path className="pulse-line"
                                  d="M12.602,7.006c-0.582-0.001-1.368-0.001-1.95,0 c-0.491,0.883-0.782,1.4-1.278,2.28C8.572,7.347,7.755,5.337,6.951,3.399c-0.586,1.29-1.338,3.017-1.923,4.307 c-1.235,0-2.38-0.002-3.615,0"/>
                        </svg>
                        <div className="signal"/>
                    </div>
                    <p>LOADING</p></div>
                <div className="loading-bg"/>
            </div>

        </div>
    );
};

export default LoadingAnimation