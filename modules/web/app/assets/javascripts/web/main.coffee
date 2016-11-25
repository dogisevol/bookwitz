'use strict'

define(['angular'], (angular) ->

  web = angular.module('web', ['ngResource', 'ngRoute', 'ngSanitize', 'ui.bootstrap', 'ui.bootstrap.tpls', 'ui.grid.selection', 'ui.grid.exporter', 'ui.grid.edit'])

  web.config [
    '$routeProvider',
    ($routeProvider) ->
      $routeProvider.when '/books',
        templateUrl: 'web/vassets/partials/books.tpl.html'
      $routeProvider.when '/userWords',
        templateUrl: 'web/vassets/partials/userWords.tpl.html'
      $routeProvider.otherwise
        redirectTo: '/books'
  ]

  web.factory 'navigation',
    ($resource) ->
      $resource('web/navigation')

  web.factory 'userWordsList',
    ($resource) ->
        $resource('web/userWords')

  web.factory 'userBook',
    ($resource) ->
      $resource('web/book')

  web.factory 'wordDefinitions',
    ($resource) ->
      $resource('web/wordDefinitions', {word:'@word'})

  web.controller 'UserWordsController',
    class UserWordsController
      constructor: ($scope, $http, $location, userWordsList) ->
        $scope.myData = []
        userWordsList.query((result)->
          result.forEach((item)->
              $scope.myData.push(item)
            )
          )

        $scope.gridOptions =
            "enableGridMenu": true,
            "data":  'myData',
            onRegisterApi : (gridApi)->
                $scope.gridApi = gridApi
                gridApi.edit.on.afterCellEdit($scope,(rowEntity, colDef, newValue, oldValue)->
                    $http.post('web/updateUserWord', {'word': rowEntity.word, 'note': newValue})
                    .success (response) ->
                        if response.status == 'failure'
                            $scope.errorMsg = response.status
                    .error (response) ->
                        $scope.errorMsg = response.status
              )
            "enableFiltering": true,
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
              { "name":"word", "field": "word", enableCellEdit: false },
              { "name":"note", "field": "note" }
            ]

  web.controller 'BooksController',
    class BooksController
      constructor: ($scope, $http, $location, userBook) ->
        userBook.get((result)->
          $scope.gridOptions.data = []
          result.data.forEach((item)->
              $scope.gridOptions.data.push(item)
            )
          )

        $scope.showWordInfo = (row) ->
            $scope.$broadcast("openWordInfo", row.entity.word);

        $scope.gridOptions =
            "enableGridMenu": true,
            onRegisterApi : (gridApi)->
                $scope.gridApi = gridApi
            "enableFiltering": true,
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
            exporterPdfMaxGridWidth: 500,
            exporterCsvLinkElement: angular.element(document.querySelectorAll(".custom-csv-link-location")),
            "enableSorting": true,
            "multiSelect": true,
            "enableRowSelection": true,
            "selectionRowHeaderWidth": 35,
            "rowHeight": 35,
            "rowTemplate": "<div ng-dblclick=\"grid.appScope.showWordInfo(row)\" ng-repeat=\"(colRenderIndex, col) in colContainer.renderedColumns track by col.uid\" ui-grid-one-bind-id-grid=\"rowRenderIndex + '-' + col.uid + '-cell'\" class=\"ui-grid-cell ng-scope ui-grid-coluiGrid-0007\" ng-class=\"{ 'ui-grid-row-header-cell': col.isRowHeader }\" role=\"gridcell\" ui-grid-cell=\"\"></div>",
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
                        ), 5000
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
                            ), 5000
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
                    ), 5000
                ), ((response) ->
                  if response.status > 0
                    $scope.errorMsg = response.status
                  else
                ), (evt) ->
                  file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total))
                  return

  web.controller 'ModalWordController',
    class ModalWordController
          constructor: ($scope, $uibModal, wordDefinitions) ->

            $scope.$on("openWordInfo", (event, word) -> $scope.openWordInfo(word))

            $scope.examples = [{
              "name": "<a href='http://www.google.ca'>Nexus S</a>",
              "snippet": "this one has an anchor tag"
            }, {
              "name": "<img src='http://www.w3schools.com/tags/smiley.gif' title='image tag example' />",
              "snippet": "this one has an image tag"
            }, {
              "name": "Just regular text"
            }]

            $scope.openModal = () ->
                selectedRows = $scope.gridApi.selection.getSelectedRows()
                if selectedRows && selectedRows.length == 1
                    $scope.openWordInfo(selectedRows[0].word)

            $scope.openWordInfo = (word) ->
                wordDefinitions.get(word: word, (data)->
                    $scope.modalData = data
                    text = ''
                    $scope.modalInstance = $uibModal.open({
                      templateUrl: '/web/vassets/partials/modal.tpl.html',
                      controller: ($uibModalInstance ,$scope, modalData) ->
                        $scope.modalData = modalData
                        $scope.closeModal = () ->
                          $uibModalInstance.dismiss('cancel');
                        $scope.format = (text) ->
                            result = text.replace(new RegExp(word, 'g'), '<b>'+word+'</b>')
                            console.log(result)
                            result
                      resolve:
                        modalData: -> $scope.modalData
                        examples: -> $scope.examples
                    }).result
                (err)->
                    alert('Cannot get definitions wor the word' + word)
                )

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