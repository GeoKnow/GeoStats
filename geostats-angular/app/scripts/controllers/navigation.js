'use strict';

angular.module('geostatsAngularApp')
    .controller('NavigationCtrl', ['$scope', '$location', '$translate', function ($scope, $location, $translate) {

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

        $scope.loadFaq = function(){
            $location.url('/faq');
        };

        $scope.changeLanguage = function (key) {

            $translate.use(key);
        };
    }]);