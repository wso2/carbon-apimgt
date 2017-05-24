let path = require('path');

const config = {
    entry: {
        index: './pages/index.js'
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
                            presets: ['es2015', 'react']
                        }
                    }
                ]
            }
        ]
    }
};

if (process.env.NODE_ENV === "development") {
    config.watch = true;
}

module.exports = config;