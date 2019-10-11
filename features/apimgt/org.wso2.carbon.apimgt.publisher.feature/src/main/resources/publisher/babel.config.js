/**
 * WSO2 NOTES:
 * Add targets -> node -> current for test config
 *      as per this comment https://github.com/babel/babel/issues/5085#issuecomment-363242788
 * Add minimum browser compatibility for production builds as per https://babeljs.io/docs/en/babel-preset-env#targets
 *      Related github issue: https://github.com/wso2/product-apim/issues/2661
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
                            ie: '11',
                        },
                    },
                ],
                '@babel/preset-react',
            ],
            plugins: ['@babel/plugin-proposal-class-properties', '@babel/plugin-syntax-dynamic-import'],
        },
        development: {
            presets: ['@babel/preset-env', '@babel/preset-react'],
            plugins: ['@babel/plugin-proposal-class-properties', '@babel/plugin-syntax-dynamic-import'],
        },
    },
};
