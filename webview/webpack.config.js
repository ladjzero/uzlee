var path = require('path')
var webpack = require('webpack')

module.exports = {
  entry: {
    posts: './src/posts.app.js'
  },
  output: {
    publicPath: '/dist/',
    path: path.join(__dirname, 'dist'),
    filename: '[name].bundle.js'
  },
  module: {
    loaders: [
      {test: /\.vue$/, loader: 'vue'},
      {test: /\.js$/, exclude: /node_modules/, loader: 'babel', query: {presets: ['es2015']}}
    ]
  },
  devtool: '#source-map',
  plugins: [
    new webpack.optimize.UglifyJsPlugin()
  ]
}
