var path = require('path');

const config = {
    entry: {
        index: './source/index.js'
    },
    output: {
        path: path.resolve(__dirname, 'public/dist'),
        filename: "[name].bundle.js",
        chunkFilename: "[name].bundle.js",
        publicPath: 'public/app/dist/'
    },
    watch: false,
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                    }
                ]
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
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
            },
            {
                test: /\.(woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=100000'
            }
        ]
    },
    plugins: []
};


if (process.env.NODE_ENV === "development") {
    config.watch = true;
    config.devtool = "source-map";
}
if (process.env.NODE_ENV === 'production') {

}

module.exports = function (env) {
    if (env && env.analysis) {
        var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
        config.plugins.push(new BundleAnalyzerPlugin())
    }
    return config;
}
