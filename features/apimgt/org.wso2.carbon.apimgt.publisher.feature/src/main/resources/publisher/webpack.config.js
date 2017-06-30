let path = require('path');

const config = {
    entry: {
        index: './source/index.js'
    },
    output: {
        path: path.resolve(__dirname, 'public/dist'),
        filename: '[name].js'
    },
    devtool: "source-map",
    plugins: [],
    watch: false,
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: {
                            presets: ['es2015', 'react'],
                            plugins: ['transform-class-properties']
                        }
                    }
                ]
            },
            {
              test: /\.css$/,
              use: [ 'style-loader', 'css-loader' ]
            },
            {
                test: /\.less$/,
                use: [{
                    loader: "style-loader" // creates style nodes from JS strings
                }, {
                    loader: "css-loader" // translates CSS into CommonJS
                }, {
                    loader: "less-loader" // compiles Less to CSS
                }]
            }
        ]
    }
};

if (process.env.NODE_ENV === "development") {
    config.watch = true;
}

module.exports = config;
