'use strict'

define(['angular'], (angular) ->

  web = angular.module('web', ['ngResource', 'ngRoute', 'ui.grid.selection', 'ui.grid.exporter'])

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
        $scope.gridOptions =
            "enableGridMenu": true,
            onRegisterApi : (gridApi)->
                $scope.gridApi = gridApi
            "enableSelectAll": true,
            "exporterCsvFilename": "myFile.csv",
            "exporterPdfDefaultStyle": {"fontSize": 9},
            "exporterPdfTableStyle": {"margin": [30, 30, 30, 30]},
            "exporterPdfTableHeaderStyle": {"fontSize": 10, "bold": true, "italics": true, "color": "red"},
            "exporterPdfHeader": { "text": "My Header", "style": "headerStyle" },
            "exporterPdfFooter" : (currentPage, pageCount) ->
              { text: currentPage.toString() + ' of ' + pageCount.toString(), style: 'footerStyle' }
            ,
            "exporterPdfCustomFormatter" : ( docDefinition ) ->
              docDefinition.styles.headerStyle = { fontSize: 22, bold: true }
              docDefinition.styles.footerStyle = { fontSize: 10, bold: true }
              docDefinition
            ,
            exporterPdfOrientation: 'portrait',
            exporterPdfPageSize: 'LETTER',
            exporterPdfMaxGridWidth: 500,
            exporterCsvLinkElement: angular.element(document.querySelectorAll(".custom-csv-link-location")),
            "enableSorting": true,
            "multiSelect": true,
            "enableRowSelection": true,
            "selectionRowHeaderWidth": 35,
            "rowHeight": 35,
            "showGridFooter":true
            "columnDefs": [
              { "name":"word", "field": "word" },
              { "name":"tag", "field": "tag" },
              { "name":"freq", "field": "freq", "type": "number" },
        ]


  web.controller 'BookUploadController',
    class BookUploadController
          constructor: ($scope, Upload, $timeout, $http) ->
            $scope.file = {} if $scope.file is undefined

            $scope.sendSelected = () ->
                 $http.post('web/addUserWords', {params: {'words': $scope.gridApi.selection.getSelectedRows()}})
                    .success (response) ->
                        if response.status == 'failure'
                            $scope.errorMsg = response.status
                        else
                          angular.forEach($scope.gridApi.selection.getSelectedRows(), (data, index) ->
                            $scope.gridOptions.data.splice($scope.gridOptions.data.lastIndexOf(data), 1);
                          );
                    .error (response) ->
                        $scope.errorMsg = response.status

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
                            $scope.gridOptions.data = response.data
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