'use strict';

angular.module('geostatsAngularApp')
    .controller('NavigationCtrl', ['$scope', '$location', function ($scope, $location) {

        $scope.navClass = function(page){

            var currentRoute = $location.path().substring(1) || 'home';
            return page === currentRoute ? 'active' : '';
        };
      
        $scope.loadHome = function(){
            $location.url('/home');
        };
        
        $scope.loadEntity = function(){
            $location.url('/entity');
        };
        
        $scope.loadStatistic = function(){
            $location.url('/statistic');
        };

        $scope.loadResult = function(){
            $location.url('/result');
        };

        $scope.loadDocumentation = function(){
            $location.url('/documentation');
        };
}]);