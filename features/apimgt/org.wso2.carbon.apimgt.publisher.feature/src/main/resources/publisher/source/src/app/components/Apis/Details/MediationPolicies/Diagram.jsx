/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/* eslint space-infix-ops: 0 */
/* eslint max-len: 0 */

import React from 'react';
import PropTypes from 'prop-types';

function Diagram(props) {
    const { inPolicy, outPolicy, faultPolicy } = props;
    return (
        <svg width={507.08} height={251.087} viewBox='0 0 134.165 66.433' {...props}>
            <path
                d='M75.324 4.557h29.754l4.08 5.34-4.08 5.312H75.324l3.74-5.09z'
                fill='#01518e'
                paintOrder='stroke markers fill'
            />
            <text
                y={120.155}
                x={153.787}
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                fontWeight={400}
                fontSize={5.599}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                fill='#fff'
                strokeWidth={0.382}
                transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'
            >
                <tspan y={120.155} x={153.787}>
                    IN FLOW
                </tspan>
            </text>
            <g transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'>
                <ellipse
                    ry={16.889}
                    rx={16.718}
                    cy={133.037}
                    cx={121.291}
                    fill='#1495d9'
                    paintOrder='stroke markers fill'
                />
                <path
                    d={
                        'M128.011 128.038l.656-.656a5.776 5.776 0 00-4.098-1.72c-1.475 0-2.95.573-4.097 '
                        + '1.72l.655.656c.984-.901 2.213-1.393 3.442-1.393 1.23 0 2.459.492 3.442 1.393zm-.738.'
                        + '656c-.737-.738-1.72-1.148-2.704-1.148-.983 0-1.966.41-2.704 1.148l.656.655c.573-.574 '
                        + '1.31-.82 2.048-.82s1.475.246 2.049.82zm-.245 5.162h-1.64v-3.278h-1.638v3.278h-8.195c-.'
                        + '901 0-1.639.738-1.639 1.64v3.277c0 .901.738 1.639 1.64 1.639h11.472c.901 0 1.639-.738 '
                        + '1.639-1.639v-3.278c0-.901-.738-1.639-1.64-1.639zm-9.015 4.098h-1.638v-1.64h1.638zm2.869'
                        + ' 0h-1.64v-1.64h1.64zm2.868 0h-1.64v-1.64h1.64z'
                    }
                    fill='#fff'
                />
                <text
                    y={154.402}
                    x={109.23}
                    style={{
                        lineHeight: 1.25,
                        InkscapeFontSpecification: "'Open Sans, Normal'",
                        fontVariantLigatures: 'normal',
                        fontVariantCaps: 'normal',
                        fontVariantNumeric: 'normal',
                        fontFeatureSettings: 'normal',
                        textAlign: 'start',
                    }}
                    fontWeight={400}
                    fontSize={5.149}
                    fontFamily='Open Sans'
                    letterSpacing={0}
                    wordSpacing={0}
                    fill='#1495d9'
                    strokeWidth={0.351}
                >
                    <tspan y={154.402} x={109.23}>
                        GATEWAY
                    </tspan>
                </text>
            </g>
            <g transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'>
                <ellipse
                    cx={202.624}
                    cy={133.037}
                    rx={16.718}
                    ry={16.889}
                    fill='#1495d9'
                    paintOrder='stroke markers fill'
                />
                <path
                    d={
                        'M197.936 124.162c-.677 0-1.221.64-1.221 1.433v10.417c0 .794.544 1.433 1.22 1.433h4.773c.676'
                        + ' 0 1.22-.64 1.22-1.433v-10.417c0-.794-.544-1.433-1.22-1.433zm.388 2.572h3.829c.17 0 .305.16.305.'
                        + '358v.098c0 .198-.136.358-.305.358h-3.829c-.169 0-.305-.16-.305-.358v-.098c0-.198.136-.358.305-'
                        + '.358zm0 1.693h3.829c.17 0 .305.16.305.358v.098c0 .198-.136.358-.305.358h-3.829c-.169 0-.305-.16-'
                        + '.305-.358v-.098c0-.198.136-.358.305-.358zm2.04 3.841a.513.602 0 01.513.603.513.602 0 01-.514.602.'
                        + '513.602 0 01-.513-.602.513.602 0 01.513-.603zm0 1.889a.513.602 0 01.513.602.513.602 0 01-.514.602.'
                        + '513.602 0 01-.513-.602.513.602 0 01.513-.602z'
                    }
                    fill='#fff'
                    paintOrder='stroke markers fill'
                />
                <path
                    d={
                        'M203.513 127.416c-.676 0-1.22.639-1.22 1.432v10.418c0 .793.544 1.432 1.22 1.432h4.772c.677 0 1.221-.639'
                        + ' 1.221-1.432v-10.418c0-.793-.544-1.432-1.22-1.432zm.389 2.572h3.829c.169 0 .305.16.305.358v.097c0 .199-.136'
                        + '.359-.305.359h-3.83c-.168 0-.305-.16-.305-.359v-.097c0-.199.137-.358.306-.358zm0 1.693h3.829c.169 0 .305.16.'
                        + '305.358v.097c0 .199-.136.359-.305.359h-3.83c-.168 0-.305-.16-.305-.359v-.097c0-.199.137-.358.306-.358zm2.039'
                        + ' 3.84a.513.602 0 01.513.603.513.602 0 01-.513.602.513.602 0 01-.513-.602.513.602 0 01.513-.602zm0 1.89a.513.602'
                        + ' 0 01.513.601.513.602 0 01-.513.603.513.602 0 01-.513-.603.513.602 0 01.513-.602z'
                    }
                    fill='#fff'
                    paintOrder='stroke markers fill'
                />
                <text
                    style={{
                        lineHeight: 1.25,
                        InkscapeFontSpecification: "'Open Sans, Normal'",
                        fontVariantLigatures: 'normal',
                        fontVariantCaps: 'normal',
                        fontVariantNumeric: 'normal',
                        fontFeatureSettings: 'normal',
                        textAlign: 'start',
                    }}
                    x={189.861}
                    y={154.402}
                    fontWeight={400}
                    fontSize={5.149}
                    fontFamily='Open Sans'
                    letterSpacing={0}
                    wordSpacing={0}
                    fill='#1495d9'
                    strokeWidth={0.351}
                >
                    <tspan x={189.861} y={154.402}>
                        BACKEND
                    </tspan>
                </text>
            </g>
            <path
                d='M0 4.75h39.279l4.08 5.34-4.08 5.312H0l3.74-5.09z'
                fill='#01518e'
                paintOrder='stroke markers fill'
            />
            <text
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                x={64.901}
                y={120.401}
                fontWeight={400}
                fontSize={5.599}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                fill='#fff'
                strokeWidth={0.382}
                transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'
            >
                <tspan x={64.901} y={120.401}>
                    IN FLOW
                </tspan>
            </text>
            <text
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                x={134.287}
                y={111.833}
                transform='matrix(.9948 0 0 1.00524 -56.867 -109.725)'
                fontWeight={400}
                fontSize={3.528}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                strokeWidth={0.228}
            >
                {inPolicy && inPolicy.name ? (
                    <tspan x={134.287} y={111.833} fontWeight={700}>
                        {inPolicy.name}
                    </tspan>
                ) : (
                    <tspan x={134.287} y={111.833} fontWeight={700}>
                        NONE
                    </tspan>
                )}
            </text>
            <path
                d='M109.158 28.61H79.404l-4.08 5.34 4.08 5.312h29.754l-3.74-5.09z'
                fill='#1495d9'
                paintOrder='stroke markers fill'
            />
            <text
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                x={151.158}
                y={150.83}
                fontWeight={400}
                fontSize={5.599}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                fill='#fff'
                strokeWidth={0.382}
                transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'
            >
                <tspan x={151.158} y={150.83}>
                    OUT FLOW
                </tspan>
            </text>
            <path
                d='M43.36 28.803H4.08L0 34.143l4.08 5.312h39.28l-3.741-5.09z'
                fill='#1495d9'
                paintOrder='stroke markers fill'
            />
            <text
                y={151.076}
                x={61.834}
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                fontWeight={400}
                fontSize={5.599}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                fill='#fff'
                strokeWidth={0.382}
                transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'
            >
                <tspan y={151.076} x={61.834}>
                    OUT FLOW
                </tspan>
            </text>
            <path
                d={
                    'M46.577 40.057v10.05H4.081L0 55.447l4.08 5.312c14.511.01 38.535-.007'
                    + ' 53.038-.007V40.057l-5.038 3.78z'
                }
                fill='#ff6333'
                paintOrder='stroke markers fill'
            />
            <text
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                x={61.834}
                y={178.245}
                fontWeight={400}
                fontSize={5.599}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                fill='#fff'
                strokeWidth={0.382}
                transform='matrix(.77598 0 0 .78413 -36.04 -82.763)'
            >
                <tspan x={61.834} y={178.245}>
                    FAULT FLOW
                </tspan>
            </text>
            <text
                y={136.375}
                x={59.402}
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                transform='matrix(.9948 0 0 1.00524 -56.867 -109.725)'
                fontWeight={400}
                fontSize={3.528}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                strokeWidth={0.228}
            >
                {outPolicy && outPolicy.name ? (
                    <tspan y={136.375} x={59.402} fontWeight={700}>
                        {outPolicy.name}
                    </tspan>
                ) : (
                    <tspan y={136.375} x={59.402} fontWeight={700}>
                        NONE
                    </tspan>
                )}
            </text>
            <text
                style={{
                    lineHeight: 1.25,
                    InkscapeFontSpecification: "'Open Sans, Normal'",
                    fontVariantLigatures: 'normal',
                    fontVariantCaps: 'normal',
                    fontVariantNumeric: 'normal',
                    fontFeatureSettings: 'normal',
                    textAlign: 'start',
                }}
                x={62.402}
                y={171.163}
                transform='matrix(.97184 0 0 1.02898 -56.867 -109.725)'
                fontWeight={400}
                fontSize={3.528}
                fontFamily='Open Sans'
                letterSpacing={0}
                wordSpacing={0}
                strokeWidth={0.358}
            >
                {faultPolicy && faultPolicy.name ? (
                    <tspan x={62.402} y={171.163} fontWeight={700}>
                        {faultPolicy.name}
                    </tspan>
                ) : (
                    <tspan x={62.402} y={171.163} fontWeight={700}>
                        NONE
                    </tspan>
                )}
            </text>
            <path
                d={
                    'M5.912 38.54l-3.379-4.4 3.38-4.42M5.912 59.64l-3.379-4.4 3.38-4.42M81.122 '
                    + '38.54l-3.38-4.4 3.38-4.42M103.784 14.264l3.379-4.4-3.38-4.42M37.335 14.489l3.379-4.4-3.38-4.42'
                }
                fill='none'
                stroke='#fff'
                strokeWidth={0.657}
            />
        </svg>
    );
}
Diagram.propTypes = {
    inPolicy: PropTypes.shape({}).isRequired,
    outPolicy: PropTypes.shape({}).isRequired,

    faultPolicy: PropTypes.shape({}).isRequired,
};

export default Diagram;
