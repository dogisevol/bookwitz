'use strict'

requirejs.config(
  paths:
    'ng-file-upload': ['/vassets/javascripts/web/ng-file-upload.min']
  shim:
    'angular':
      exports: 'angular'
    'ng-file-upload': ['angular']
)

require([
    'angular',
    'angular-resource',
    'angular-route',
    '/vassets/javascripts/web/main.js',
    '/vassets/javascripts/users/main.js',
    'angular-ui',
    'ui-grid',
    'ng-file-upload'
  ], (angular) ->

  bookwitz = angular.module('bookwitz', ['ngResource', 'ngRoute', 'web', 'users', 'ui.grid', 'ngFileUpload'])

  bookwitz.factory 'navigation',
  ($resource) ->
    $resource('navigation')

  bookwitz.config [
    '$routeProvider',
    ($routeProvider) ->
      $routeProvider.when '/',
        templateUrl: 'users/vassets/partials/home.tpl.html'
      $routeProvider.otherwise
        redirectTo: '/'
  ]

  angular.bootstrap(document, ['bookwitz']);
)