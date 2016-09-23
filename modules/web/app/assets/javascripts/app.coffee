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
    'ui-bootstrap',
    'ui-bootstrap-tpls',
    'ng-file-upload',
    'ngSanitize'
  ], (angular) ->

  bookwitz = angular.module('bookwitz', ['ngResource', 'ngRoute', 'web', 'users', 'ui.grid', 'ngFileUpload'])

  bookwitz.factory 'navigation',
  ($resource) ->
    $resource('navigation')

  bookwitz.config [
    '$routeProvider',
    ($routeProvider) ->
      $routeProvider.when '/',
        templateUrl: 'vassets/partials/welcome.tpl.html'
      $routeProvider.otherwise
        redirectTo: '/'
  ]

  bookwitz.directive 'bookwitzHeader', ->
    restrict: 'E'
    scope:
      h1: '@'
      h2: '@'
      lead: '@'
      subtext: '@'
      navModule: '@'
      navService: '@'
    templateUrl: 'vassets/partials/header.tpl.html'
    transclude: true

  bookwitz.directive 'bookwitzNav', ->
    restrict: 'E'
    scope:
      module: '@'
      service: '@'
    templateUrl: 'vassets/partials/navigation.tpl.html'
    link: ($scope) ->
      $scope.data = angular.injector([$scope.module]).get($scope.service).get(() -> $scope.$apply())

  angular.bootstrap(document, ['bookwitz']);
)