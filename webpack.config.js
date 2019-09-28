const path = require('path');

module.exports = {
  mode: 'development',
  entry: './frontend/index.js',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'src/main/resources/static'),
  },
};
