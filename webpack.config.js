const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');

const resources = path.resolve(__dirname, 'src/main/resources')

module.exports = {
  mode: 'development',
  entry: './frontend/index.js',
  output: {
    publicPath: '/',
    filename: 'bundle.js',
    path: path.join(resources, 'static'),
  },
  plugins: [
    new CleanWebpackPlugin(),
    new HtmlWebpackPlugin({
      hash: true,
      template: './frontend/index.html',
      filename: path.join(resources, 'templates', 'index.html')
    })
  ]
};
