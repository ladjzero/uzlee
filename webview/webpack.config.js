var path = require('path')
var webpack = require('webpack')

module.exports = {
  entry: {
    posts: './src/posts.app.js'
  },
  output: {
    publicPath: '/dist/',
    path: path.join(__dirname, 'src', 'dist'),
    filename: '[name].bundle.js'
  },
  module: {
    loaders: [
      {test: /\.vue$/, loader: 'vue'},
      {test: /\.js$/, exclude: /node_modules/, loader: 'babel', query: {presets: ['es2015']}}
    ]
  },
  vue: {
    autoprefixer: {browsers: ['Android 4.0']}
  },
  plugins: [
    new webpack.optimize.UglifyJsPlugin()
  ]
}
