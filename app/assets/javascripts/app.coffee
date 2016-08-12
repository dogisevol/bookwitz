'use strict'

requirejs.config(
  paths:
    'angular': ['../lib/angularjs/angular2'],
    'angular-resource': ['../lib/angularjs/angular-resource'],
    'angular-route': ['../lib/angularjs/angular-route'],
    'angular-file-upload': ['../lib/angular-file-upload/ng-file-upload.min'],
    'angular-ui-grid': ['../lib/angular-ui-grid/ui-grid']
  shim:
    'angular':
      exports: 'angular'
    'angular-route': ['angular'],
    'angular-resource': ['angular'],
    'angular-file-upload': ['angular'],
    'angular-ui-grid': ['angular']
)

require([
    'angular',
    'angular-resource',
    'angular-route',
    '/vassets/javascripts/web/main.js',
    '/vassets/javascripts/users/main.js',
    'angular-ui-grid',
    'angular-file-upload'
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