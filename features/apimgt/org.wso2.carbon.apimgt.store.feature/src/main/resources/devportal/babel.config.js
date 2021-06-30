/**
 * Add targets -> node -> current as per this comment https://github.com/babel/babel/issues/5085#issuecomment-363242788
* Add minimum browser compatibility for production builds as per https://babeljs.io/docs/en/babel-preset-env#targets
 *      Related github issue: https://github.com/wso2/product-apim/issues/2661
 *   We have set the development browser compatibility to last 2 versions of each browser.
 *   This makes the dev build process fast
 *   Production build will consider Edge 20 and Chrome 58 as the minimum browser compatible versions.
 *   ** IE 11 is not supported (Require more polyfills etc to support it more PITA)
 *   For more information about browser compatibility list refer: https://github.com/browserslist/browserslist
*/
module.exports = {
    env: {
        test: {
            presets: [
                [
                    '@babel/preset-env',
                    {
                        targets: {
                            node: 'current',
                        },
                    },
                ],
                '@babel/preset-react',
            ],
            plugins: [
                '@babel/plugin-syntax-dynamic-import',
                '@babel/plugin-proposal-class-properties',
                'dynamic-import-node',
            ],
        },
        production: {
            presets: [
                [
                    '@babel/preset-env',
                    {
                        targets: {
                            chrome: '58',
                            edge: '16',
                        },
                    },
                ],
                '@babel/preset-react',
            ],
            plugins: ['@babel/plugin-syntax-dynamic-import', '@babel/plugin-proposal-class-properties'],
        },
        development: {
            presets: [
                [
                    '@babel/preset-env',
                    {
                        targets: 'last 2 versions',
                    },
                ],
                '@babel/preset-react',
            ],
            plugins: ['@babel/plugin-syntax-dynamic-import', '@babel/plugin-proposal-class-properties','react-hot-loader/babel'],
        },
    },
};
