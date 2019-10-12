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

/*
 `max-len` es-lint rule has been disabled for the entire file due to the generated svg elements in the file.
 it's impossible to get them under 120 columns rule
  */
/* eslint-disable max-len */
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = () => ({
    root: {
        marginTop: 30,
    },
});

/**
 *
 *
 * @class LifeCycleImage
 * @extends {React.Component}
 */
class LifeCycleImage extends React.Component {
    /**
     *
     *
     * @param {*} element
     * @returns
     * @memberof LifeCycleImage
     */
    highLightMe(element) {
        const style = {};
        if (!element.startsWith(this.props.lifeCycleStatus.toLowerCase())) {
            style.opacity = 0.2;
        }
        return style;
    }

    /**
     *
     *
     * @returns
     * @memberof LifeCycleImage
     */
    render() {
        const { classes } = this.props;

        return (
            <svg
                xmlns='http://www.w3.org/2000/svg'
                width='633'
                height='244'
                viewBox='0 0 743.09858 287.34519'
                id='svg5953'
                className={classes.root}
            >
                <defs id='defs5955'>
                    <path
                        transform='scale(.26458)'
                        id='path5588'
                        d='m 1738.3694,290.45336 a 290.37688,290.37688 0 0 1 268.3994,-179.55952 290.37688,290.37688 0 0 1 5.8758,0.0595'
                        style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                        color='#000'
                        overflow='visible'
                        fill='none'
                        strokeWidth='6.633'
                        strokeLinejoin='round'
                    />
                    <path
                        transform='scale(.26458)'
                        id='path5578'
                        d='m 2103.6746,127.54094 a 290.37688,290.37688 0 0 1 193.3409,265.03704'
                        style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                        color='#000'
                        overflow='visible'
                        fill='none'
                        strokeWidth='6.633'
                        strokeLinejoin='round'
                    />
                    <path
                        transform='scale(.26458)'
                        id='path5547'
                        d='m 1748.9295,534.82816 a 290.37688,290.37688 0 0 0 257.8393,156.81941 290.37688,290.37688 0 0 0 5.8709,-0.0593'
                        style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                        color='#000'
                        overflow='visible'
                        fill='none'
                        strokeWidth='6.633'
                        strokeLinejoin='round'
                    />
                    <path
                        transform='scale(.26458)'
                        id='path4189'
                        d='m 1748.9295,534.82816 a 290.37688,290.37688 0 0 0 257.8393,156.81941 290.37688,290.37688 0 0 0 5.8709,-0.0593'
                        style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                        color='#000'
                        overflow='visible'
                        fill='none'
                        strokeWidth='6.633'
                        strokeLinejoin='round'
                    />
                </defs>
                <g id='layer1' transform='translate(51.512 4.77)'>
                    <path d='m 99,13.3622 24,0 0,24 -24,0 z' id='path6746' fill='none' />
                    <g id='prototyped' style={this.highLightMe('prototyped')}>
                        <text
                            id='text6729'
                            y='6.362'
                            x='61'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                            fill='#2b2b2b'
                        >
                            <tspan y='6.362' x='61' id='tspan6731'>
                                PROTOTYPED
                            </tspan>
                        </text>
                        <path
                            id='path6748'
                            d='m 118,16.3622 -4.18,0 c -0.42,-1.16 -1.52,-2 -2.82,-2 -1.3,0 -2.4,0.84 -2.82,2 l -4.18,0 c -1.1,0 -2,0.9 -2,2 l 0,14 c 0,1.1 0.9,2 2,2 l 14,0 c 1.1,0 2,-0.9 2,-2 l 0,-14 c 0,-1.1 -0.9,-2 -2,-2 z m -7,0 c 0.55,0 1,0.45 1,1 0,0.55 -0.45,1 -1,1 -0.55,0 -1,-0.45 -1,-1 0,-0.55 0.45,-1 1,-1 z m -2,14 -4,-4 1.41,-1.41 2.59,2.58 6.59,-6.59 1.41,1.42 -8,8 z'
                        />
                    </g>
                    <path d='m 224.5,133.8622 24,0 0,24 -24,0 z' id='path6762' fill='none' />
                    <g id='published' style={this.highLightMe('published')}>
                        <text
                            id='text6524'
                            y='125.476'
                            x='207'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                        >
                            <tspan y='125.476' x='207' id='tspan6526'>
                                PUBLISHED
                            </tspan>
                        </text>
                        <path
                            id='path6764'
                            d='m 240.5,144.8622 c 1.66,0 2.99,-1.34 2.99,-3 0,-1.66 -1.33,-3 -2.99,-3 -1.66,0 -3,1.34 -3,3 0,1.66 1.34,3 3,3 z m -8,0 c 1.66,0 2.99,-1.34 2.99,-3 0,-1.66 -1.33,-3 -2.99,-3 -1.66,0 -3,1.34 -3,3 0,1.66 1.34,3 3,3 z m 0,2 c -2.33,0 -7,1.17 -7,3.5 l 0,2.5 14,0 0,-2.5 c 0,-2.33 -4.67,-3.5 -7,-3.5 z m 8,0 c -0.29,0 -0.62,0.02 -0.97,0.05 1.16,0.84 1.97,1.97 1.97,3.45 l 0,2.5 6,0 0,-2.5 c 0,-2.33 -4.67,-3.5 -7,-3.5 z'
                        />
                    </g>
                    <g id='created' style={this.highLightMe('created')}>
                        <text
                            id='text6520'
                            y='282.362'
                            x='68'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                        >
                            <tspan y='282.362' x='68' id='tspan6522'>
                                CREATED
                            </tspan>
                        </text>
                        <path
                            id='path6778'
                            d='m 97,238.8622 c -1.1,0 -1.99,0.9 -1.99,2 l -0.01,16 c 0,1.1 0.89,2 1.99,2 l 12.01,0 c 1.1,0 2,-0.9 2,-2 l 0,-12 -6,-6 -8,0 z m 7,7 0,-5.5 5.5,5.5 -5.5,0 z'
                        />
                    </g>
                    <path d='m 91,236.8622 24,0 0,24 -24,0 z' id='path6780' fill='none' />
                    <g id='deprecated' style={this.highLightMe('deprecated')}>
                        <text
                            id='text6536'
                            y='125.476'
                            x='397.858'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                        >
                            <tspan y='125.476' x='397.858' id='tspan6538'>
                                DEPRECATED
                            </tspan>
                        </text>
                        <path
                            id='path6794'
                            d='m 433.52944,97.811947 c 0,1.1 0.9,2 2,2 l 8,0 c 1.1,0 2,-0.9 2,-2 l 0,-12 -12,0 0,12 z m 13,-15 -3.5,0 -1,-1 -5,0 -1,1 -3.5,0 0,2 14,0 0,-2 z'
                        />
                        <path id='path6796' d='m 427.52944,78.811947 24,0 0,24.000003 -24,0 z' fill='none' />
                    </g>
                    <path d='m 427.52944,236.8622 24,0 0,24 -24,0 z' id='path6810' clipRule='evenodd' fill='none' />
                    <g id='blocked' style={this.highLightMe('blocked')}>
                        <text
                            id='text6532'
                            y='282.362'
                            x='386.858'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                        >
                            <tspan y='282.362' x='386.858' id='tspan6534'>
                                BLOCKED
                            </tspan>
                        </text>
                        <path
                            id='path6812'
                            d='m 450.22944,255.8622 -9.1,-9.1 c 0.9,-2.3 0.4,-5 -1.5,-6.9 -2,-2 -5,-2.4 -7.4,-1.3 l 4.3,4.3 -3,3 -4.4,-4.3 c -1.2,2.4 -0.7,5.4 1.3,7.4 1.9,1.9 4.6,2.4 6.9,1.5 l 9.1,9.1 c 0.4,0.4 1,0.4 1.4,0 l 2.3,-2.3 c 0.5,-0.4 0.5,-1.1 0.1,-1.4 z'
                        />
                    </g>
                    <g id='retired' style={this.highLightMe('retired')}>
                        <text
                            id='text6540'
                            y='125.483'
                            x='627.485'
                            style={{ lineHeight: '125%' }}
                            fontWeight='400'
                            fontSize='15'
                            fontFamily='sans-serif'
                            letterSpacing='0'
                            wordSpacing='0'
                        >
                            <tspan y='125.483' x='627.485' id='tspan6542'>
                                RETIRED
                            </tspan>
                        </text>
                        <path
                            id='path6826'
                            d='m 663.36667,89.811947 0,2 -13,0 0,-6 9,0 c 2.21,0 4,1.79 4,4 z m -20,3 0,2 6,0 0,2 8,0 0,-2 6,0 0,-2 -20,0 z m 5.14,-1.9 c 1.16,-1.19 1.14,-3.08 -0.04,-4.24 -1.19,-1.16 -3.08,-1.14 -4.24,0.04 -1.16,1.19 -1.14,3.08 0.04,4.24 1.19,1.16 3.08,1.14 4.24,-0.04 z'
                        />
                        <path id='path6828' d='m 641.36667,78.811947 24,0 0,24.000003 -24,0 z' fill='none' />
                    </g>
                    <g id='published_to_created' style={this.highLightMe('published_to_created')}>
                        <path
                            id='path6872'
                            d='m 235.84375,184.86133 c -11.86654,31.99459 -35.12697,58.48216 -65.32031,74.38281 l -5.4624,15.71344 14.85695,3.63617 c 35.16928,-17.95415 62.42125,-48.3249 76.47461,-85.22656 l -15.28708,7.18109 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#065381'
                        />
                        <path
                            id='path6985'
                            d='m 178.61585,255.22337 -4.875,15.375 14.5,3.625'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            d='m 187.11585,249.22337 -4.875,15.375 14.5,3.625'
                            id='path6987'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='created_to_published' style={this.highLightMe('created_to_published')}>
                        <path
                            id='path6850'
                            d='m 202.80469,171.18359 c -8.60566,23.79669 -25.78994,43.52064 -48.1836,55.30469 l 15.33148,4.4495 -5.74163,15.30245 c 27.19915,-14.16065 48.15744,-37.93366 58.79687,-66.69335 l -4.08764,-14.98782 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#34b2e4'
                        />
                        <path
                            d='m 200.07828,178.47337 15.66836,-6.25318 3.78013,13.89858'
                            id='path6979'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            id='path6991'
                            d='m 195.3937,187.48898 15.66836,-6.25318 3.78013,13.89858'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='prototyped_to_published' style={this.highLightMe('prototyped_to_published')}>
                        <path
                            id='path6874'
                            d='m 181.42383,-4.0683594 5.18392,15.3553234 -15.14681,3.480614 c 26.97521,14.497097 48.38501,37.525806 60.88086,65.484375 l 16.41911,5.795641 4.09261,-14.231188 C 238.22018,39.231643 213.06477,12.507292 181.42383,-4.0683594 Z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#fe912a'
                        />
                        <path
                            id='path6993'
                            d='m 228.26421,71.982698 16.39604,5.833631 3.93328,-14.097941'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            d='m 224.76421,65.357698 16.39604,5.833631 3.93328,-14.097941'
                            id='path6995'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='published_to_prototyped' style={this.highLightMe('published_to_prototyped')}>
                        <path
                            id='path6855'
                            d='m 164.61328,27.712891 -16.76273,3.798407 6.57718,15.455499 c 19.64736,10.327117 35.3679,26.809879 44.75391,46.923828 l 6.94786,-12.105746 13.33339,3.763949 C 208.07457,60.73778 188.78631,40.399355 164.61328,27.712891 Z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#ffc95c'
                        />
                        <path
                            id='path6997'
                            d='m 163.43006,52.336437 -6.45235,-15.202795 16.61701,-3.535534'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            d='M 171.82695,57.993291 165.3746,42.790496 181.99161,39.254962'
                            id='path6999'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='created_to_prototyped' style={this.highLightMe('created_to_prototyped')}>
                        <path
                            id='path6870'
                            d='M 35.433594,-4.7226562 C -17.800688,22.43674 -51.366944,77.095386 -51.511719,136.85742 c 0.08726,60.42038 34.333636,115.59607 88.439453,142.48828 l -5.963174,-14.43529 14.818643,-3.6858 C -1.8626488,238.10436 -32.152993,189.83798 -32.251953,136.87891 -32.191581,84.271252 -2.3316563,36.239603 44.818359,12.90625 l 7.576833,-14.3582512 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#8b103e'
                        />
                        <path
                            id='path7001'
                            d='m 25.625,1.1122031 16.25,3 -7.125,13.7499999'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            d='m 16.375,7.1122031 16.25,2.9999999 -7.125,13.75'
                            id='path7003'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='prototyped_to_created' style={this.highLightMe('prototyped_to_created')}>
                        <path
                            id='path6840'
                            d='M 52.171875,26.720703 C 10.473112,47.61586 -15.897048,90.22032 -16,136.86133 c 0.06835,46.84095 26.630186,89.61173 68.585938,110.43945 l 18.169686,-5.17409 -8.489999,-14.63841 C 27.868397,210.36444 6.0894455,175.28511 6,136.86133 6.0597413,98.328986 27.932967,63.153267 62.464844,46.056641 l -14.466177,-4.2932 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#e34856'
                        />
                        <path
                            id='path7005'
                            d='m 39.421204,240.07329 17.677669,-4.94975 -7.778174,-14.49569'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                        <path
                            d='m 27.930719,230.8809 17.677669,-4.94975 -7.778174,-14.49569'
                            id='path7007'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                        />
                    </g>
                    <g id='deprecated_to_retired' style={this.highLightMe('deprecated_to_retired')}>
                        <path
                            id='rect7009'
                            d='m 530.25049,110.51652 68.24164,0 9.89949,9.52539 -9.89949,9.40863 -68.24164,0 9.54594,-9.76218 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#5c5c5c'
                            strokeWidth='6.016'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                        />
                        <path
                            id='path7016'
                            d='m 589.97554,111.03345 9.31051,8.95242 -9.42988,8.89273'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                        <path
                            d='m 581.28804,111.03345 9.31051,8.95242 -9.42988,8.89273'
                            id='path7018'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                    </g>
                    <g id='published_to_deprecated' style={this.highLightMe('published_to_deprecated')}>
                        <path
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            d='m 306.25049,110.51652 68.24164,0 9.89949,9.52539 -9.89949,9.40863 -68.24164,0 9.54594,-9.76218 z'
                            id='path7020'
                            color='#000'
                            overflow='visible'
                            fill='#5c5c5c'
                            strokeWidth='6.016'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                        />
                        <path
                            d='m 365.97554,111.03345 9.31051,8.95242 -9.42988,8.89273'
                            id='path7022'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                        <path
                            id='path7024'
                            d='m 357.28804,111.03345 9.31051,8.95242 -9.42988,8.89273'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                    </g>
                    <g id='published_to_blocked' style={this.highLightMe('published_to_blocked')}>
                        <path
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            d='m 306.92162,149.0749 92.7941,75.49823 1.41692,13.66474 -13.63383,0.80053 -92.79411,-75.49822 13.59185,-1.29876 z'
                            id='path7026'
                            color='#000'
                            overflow='visible'
                            fill='#65daad'
                            strokeWidth='6.016'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                        />
                        <path
                            d='m 392.87565,219.47284 1.33665,12.84698 -12.94218,0.70939'
                            id='path7028'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                        <path
                            id='path7030'
                            d='m 386.23855,213.86735 1.33664,12.84698 -12.94218,0.70939'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                    </g>
                    <g
                        id='blocked_to_published'
                        transform='rotate(180 338.919 207.057)'
                        style={this.highLightMe('blocked_to_published')}
                    >
                        <path
                            id='path7039'
                            d='m 306.92162,149.0749 92.7941,75.49823 1.41692,13.66474 -13.63383,0.80053 -92.79411,-75.49822 13.59185,-1.29876 z'
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            color='#000'
                            overflow='visible'
                            fill='#64d0da'
                            strokeWidth='6.016'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                        />
                        <path
                            id='path7041'
                            d='m 392.87565,219.47284 1.33665,12.84698 -12.94218,0.70939'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                        <path
                            d='m 386.23855,213.86735 1.33664,12.84698 -12.94218,0.70939'
                            id='path7043'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                    </g>
                    <g id='blocked_to_deprecated' style={this.highLightMe('blocked_to_deprecated')}>
                        <path
                            style={{ isolation: 'auto', mixBlendMode: 'normal' }}
                            d='m 427.85405,225.05409 0,-68.24164 9.52539,-9.89949 9.40863,9.89949 0,68.24164 -9.76218,-9.54594 z'
                            id='path7045'
                            color='#000'
                            overflow='visible'
                            fill='#5c5c5c'
                            strokeWidth='6.016'
                            strokeLinecap='round'
                            strokeLinejoin='round'
                        />
                        <path
                            d='m 428.37098,165.32904 8.95242,-9.31051 8.89273,9.42988'
                            id='path7047'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                        <path
                            id='path7049'
                            d='m 428.37098,174.01654 8.95242,-9.31051 8.89273,9.42988'
                            fill='none'
                            fillRule='evenodd'
                            stroke='#fff'
                            strokeWidth='.955'
                        />
                    </g>
                </g>
            </svg>
        );
    }
}
LifeCycleImage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    lifeCycleStatus: PropTypes.string.isRequired,
};

export default withStyles(styles)(LifeCycleImage);
