/**
 * Build config for electron renderer process
 */

import path from 'path';
import webpack from 'webpack';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import { BundleAnalyzerPlugin } from 'webpack-bundle-analyzer';
import CssMinimizerPlugin from 'css-minimizer-webpack-plugin';
import { merge } from 'webpack-merge';
import TerserPlugin from 'terser-webpack-plugin';
import baseConfig from './webpack.config.base';
import webpackPaths from './webpack.paths';
import checkNodeEnv from '../scripts/check-node-env';
import deleteSourceMaps from '../scripts/delete-source-maps';
const glob = require('glob');

checkNodeEnv('production');
deleteSourceMaps();

const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
import CopyWebpackPlugin from 'copy-webpack-plugin';

const configuration: webpack.Configuration = {
  devtool: 'source-map',

  mode: 'production',

  target: ['web', 'electron-renderer'],

  entry: [
    path.join(webpackPaths.srcRendererPath, 'index.tsx'),
    // ... other entries
    // ...glob.sync(path.join(webpackPaths.srcDocPath, '**/*.html').replace(/\\/g, "/")),
  ],

  output: {
    path: webpackPaths.distRendererPath,
    publicPath: './',
    filename: 'renderer.js',
    library: {
      type: 'umd',
    },
  },

  module: {
    rules: [
      {
        test: /\.s?(a|c)ss$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            options: {
              modules: true,
              sourceMap: true,
              importLoaders: 1,
            },
          },
          'sass-loader',
        ],
        include: /\.module\.s?(c|a)ss$/,
      },
      {
        test: /\.s?(a|c)ss$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader',
          'sass-loader',
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: [require('tailwindcss'), require('autoprefixer')],
              },
            },
          },
        ],
        exclude: /\.module\.s?(c|a)ss$/,
      },
      // Fonts
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
      },
      // Images
      {
        test: /\.(png|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      },

      // {
      //   test: /\.html$/,
      //   include: webpackPaths.srcDocPath,
      //   use: [
      //     {
      //       loader: 'file-loader',
      //       options: {
      //       name: '[path][name].[ext]',
      //       context: webpackPaths.srcDocPath,
      //       },
      //   },
      //   {
      //       loader: 'html-loader',
      //       options: {
      //       preprocessor: (content, loaderContext) => {
      //           let processedContent = content;
      //           const regex = new RegExp('href="/docs', 'g');
      //           const regex2 = new RegExp('src="/docs', 'g');
      //           processedContent = processedContent.replace(regex, 'href="./docs');
      //           processedContent = processedContent.replace(regex2, 'href="./docs');
      //           return processedContent;
      //       },
      //       },
      //   },
      //   // {
      //   //     loader: 'extract-loader',
      //   // },  
      //   ],
      // },

      // SVG
      {
        test: /\.svg$/,
        use: [
          {
            loader: '@svgr/webpack',
            options: {
              prettier: false,
              svgo: false,
              svgoConfig: {
                plugins: [{ removeViewBox: false }],
              },
              titleProp: true,
              ref: true,
            },
          },
          'file-loader',
        ],
      },
    ],
  },

  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        parallel: true,
      }),
      new CssMinimizerPlugin(),
    ],
  },

  plugins: [
    /**
     * Create global constants which can be configured at compile time.
     *
     * Useful for allowing different behaviour between development builds and
     * release builds
     *
     * NODE_ENV should be production so that modules do not perform certain
     * development checks
     */
    new webpack.EnvironmentPlugin({
      NODE_ENV: 'production',
      DEBUG_PROD: false,
    }),

    new MiniCssExtractPlugin({
      filename: 'style.css',
    }),

    new BundleAnalyzerPlugin({
      analyzerMode: process.env.ANALYZE === 'true' ? 'server' : 'disabled',
      analyzerPort: 8889,
    }),

    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: path.join(webpackPaths.srcRendererPath, 'index.ejs'),
      minify: {
        collapseWhitespace: true,
        removeAttributeQuotes: true,
        removeComments: true,
      },
      isBrowser: false,
      isDevelopment: process.env.NODE_ENV !== 'production',
    }),

    new webpack.DefinePlugin({
      'process.type': '"renderer"',
    }),
    new CopyWebpackPlugin({
      patterns: [
        {
          from: path.join(webpackPaths.srcDocPath, "assets"),
          to: "docs/assets",
          transform(content, filename) {
            const isJsRegex = new RegExp(`\.js$`);
            const jsonRegex = new RegExp('\.json$');

            let processedContent = content.toString();

            if (isJsRegex.test(filename)) {
              const regex = new RegExp('(\/docs)', 'g');
              processedContent = processedContent.replace(regex, '.');
            } else if (jsonRegex.test(filename)) {
              const emptyLinkUrl = new RegExp(`"url": "\/docs\/"`, 'g')
              const emptyLinkReplacement = `"url": "./index.html"`

              const emptyLinkHeaderLinkRex = new RegExp(`"url": "\/docs\/(((?!\.html)[^"])+)"`, 'g');
              const emptyLinkHeaderReplacmenet = `"url": "./index.html$1"`

              const normalLinkUrlReg = new RegExp(`"url": "\/docs\/([^\.]*\.html[^"]*)"`, 'g');

            
              const normalLinkUrlReplacement = `"url": "./$1"`
              
              processedContent = processedContent.replace(emptyLinkUrl, emptyLinkReplacement);
              processedContent = processedContent.replace(normalLinkUrlReg, normalLinkUrlReplacement)
              processedContent = processedContent.replace(emptyLinkHeaderLinkRex, emptyLinkHeaderReplacmenet);
            }

            return processedContent;
          }
        },
        {
          from: path.join(webpackPaths.srcDocPath, '**/*.html').replace(/\\/g, "/"),
          to: 'docs/[name][ext]',
          
          transform(content) {
            let processedContent = content.toString();
            
            const emptyLinkUrl = new RegExp(`"\/docs\/"`, 'g')
            const emptyLinkReplacement = `"./index.html"`
            const regex = new RegExp('href="\/docs', 'g');
            const regex2 = new RegExp('src="\/docs', 'g');
            processedContent = processedContent.replace(emptyLinkUrl, emptyLinkReplacement);
            processedContent = processedContent.replace(regex, 'href=".');
            processedContent = processedContent.replace(regex2, 'src=".');
            console.log(processedContent);
            return processedContent;
          }  
        },
      ]
    }),
    new MonacoWebpackPlugin({

    }),
  ],
};

export default merge(baseConfig, configuration);
