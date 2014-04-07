'use strict';

var geostats = angular.module('geostatsAngularApp', [
	  'ngRoute',
	  'pascalprecht.translate',
	  'ui.bootstrap',
	  'leaflet-directive',
      'tableSort',
      'ui.multiselect',
      'truncate',
      'colorpicker.module'
	])
  	.config(function ($routeProvider) {
		$routeProvider
			.when('/', {
				templateUrl: 'views/home.html',
				controller: 'HomeCtrl'
		  	})
            .when('/entity/:uri*', {
                templateUrl: 'views/entity.html',
                controller: 'EntityCtrl'
            })
			.when('/entity', {
				templateUrl: 'views/entity.html',
				controller: 'EntityCtrl'
		  	})
            .when('/statistic', {
                templateUrl: 'views/statistics.html',
                controller: 'StatisticsCtrl'
            })
		  	.when('/statistic/', {
				templateUrl: 'views/statistics.html',
				controller: 'StatisticsCtrl'
		  	})
		  	.when('/result', {
				templateUrl: 'views/result.html',
				controller: 'ResultCtrl'
		  	})
            .when('/documentation', {
              templateUrl: 'views/documentation.html',
              controller: 'DocumentationCtrl'
            })
            .when('/faq', {
              templateUrl: 'views/faq.html',
              controller: 'FaqCtrl'
            })
		  	.otherwise({
				redirectTo: '/'
		  	});
  	});






