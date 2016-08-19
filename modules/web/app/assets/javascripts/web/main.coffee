'use strict'

define(['angular'], (angular) ->

  web = angular.module('web', ['ngResource', 'ngRoute', 'ui.grid.selection'])

  web.config [
    '$routeProvider',
    ($routeProvider) ->
      $routeProvider.when '/books',
        templateUrl: 'web/vassets/partials/books.tpl.html'
      $routeProvider.otherwise
        redirectTo: '/books'
  ]

  web.factory 'navigation',
    ($resource) ->
      $resource('web/navigation')

  web.controller 'BooksController',
    class BooksController
      constructor: ($scope, $http, $location) ->
        $http.get('/web/books')
                     .success (response) ->
                       $scope.gridOptions.data = response

        $scope.gridOptions =
            "enableSorting": true
            "multiSelect": false
            "modifierKeysToMultiSelect": false
            "enableRowHeaderSelection": false
            "noUnselect": true
            "enableRowSelection": true
            "enableSelectAll": false
            "rowHeight": 35
            "showGridFooter":true
            "columnDefs": [
              { "name":"book", "field": "title" },
              { "name":"id", "field": "id" },
              { "name":"userId", "field": "userId" }
            ]

        $scope.wordsGridOptions =
            "enableSorting": true,
            "multiSelect": false,
            "enableRowSelection": true,
            "enableSelectAll": true,
            "selectionRowHeaderWidth": 35,
            "rowHeight": 35,
            "showGridFooter":true
            "columnDefs": [
              { "name":"word", "field": "word" },
              { "name":"tag", "field": "tag" },
              { "name":"freq", "field": "freq" },
        ]


  web.controller 'BookUploadController',
    class BookUploadController
          constructor: ($scope, Upload, $timeout, $http) ->
            $scope.file = {} if $scope.file is undefined

            $scope.uploadText = (file) ->
                $http.post('web/contentUpload', {'content': $scope.content})
                .success (response) ->
                    if response.status == 'failure'
                        $scope.errorMsg = response.status
                        return
                    else
                        $timeout (->
                          $scope.progressFile($scope.file, response)
                          return
                        ), 1000
                .error (response) ->
                    $scope.errorMsg = response.status


            $scope.progressFile = (file, uuid) ->
                $http.get('web/bookUpload', {params: {'uuid': uuid}})
                .success (response) ->
                    if response.status == 'failure'
                        $scope.errorMsg = response.status
                        return
                    else
                        if response.status == 'done'
                            file.progress = 100
                            $scope.wordsGridOptions.data = response.data
                            $scope.f = null
                            return
                        else
                            file.progress = response.progress
                            file.operation = response.status
                            $timeout (->
                              $scope.progressFile(file, uuid)
                              return
                            ), 1000
                .error (response) ->
                    $scope.errorMsg = response.status
            $scope.uploadFiles = (file, errFiles) ->
              $scope.f = file
              $scope.errFile = errFiles and errFiles[0]
              if file
                file.upload = Upload.upload(
                  url: 'web/bookUpload'
                  data: file: file)
                file.upload.then ((response) ->
                    $timeout (->
                    $scope.progressFile(file, response.data)
                    ), 3000
                ), ((response) ->
                  if response.status > 0
                    $scope.errorMsg = response.status
                  else
                ), (evt) ->
                  file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total))
                  return

  web.directive 'bookwitzInput', ->
    restrict: 'E'
    replace: 'true'
    scope:
      name: '@'
      model: '='
      label: '@'
      placeholder: '@'
      size: '@'
      type: '@'
      value: '@'
      required: '@'
      errors: "="
    templateUrl: 'web/vassets/partials/input.tpl.html'

  web.directive 'bookwitzForm', ->
    restrict: 'E'
    scope:
      controller: '='
      submit: '='

  web.directive 'bookwitzSubmit', ->
    restrict: 'E'
    scope:
      class: '@'
      value: '@'
    templateUrl: 'web/vassets/partials/submit.tpl.html'

  web.directive 'bookwitzButton', ->
    restrict: 'E'
    scope:
      click: '@'
      value: '@'
    templateUrl: 'web/vassets/partials/button.tpl.html'

  web.directive 'bookHeader', ->
    restrict: 'E'
    scope:
      h1: '@'
      h2: '@'
      lead: '@'
      subtext: '@'
      navModule: '@'
      navService: '@'
    templateUrl: 'web/vassets/partials/header.tpl.html'
    transclude: true

  web.directive 'bookNav', ->
    restrict: 'E'
    scope:
      module: '@'
      service: '@'
    templateUrl: 'web/vassets/partials/navigation.tpl.html'
    link: ($scope) ->
      $scope.data = angular.injector([$scope.module]).get($scope.service).get(() -> $scope.$apply())
)