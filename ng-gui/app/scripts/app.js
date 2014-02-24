'use strict';

var geostats = angular.module('geostatsAngularApp', [
	  'ngRoute',
	  'pascalprecht.translate',
	  'ui.bootstrap',
	  'leaflet-directive',
      'tableSort',
      'ui.multiselect'
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
		  	.when('/statistic/:sparqlEndpoint/:graph/:layer/:primaryDataCube/:primaryStatistic/:secondaryDataCube/:secondaryStatistic', {
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
		  	.otherwise({
				redirectTo: '/'
		  	});
  	})
  	.config(['$translateProvider', function ($translateProvider) {
		$translateProvider.translations('en', {
			'TAB_HEADER_HOME'		: 'Home',
			'TAB_HEADER_ENTITY'		: 'Info',
			'TAB_HEADER_STATISTICS'	: 'Statistics',
			'TAB_HEADER_RESULTS'	: 'Results',
            'TAB_HEADER_DOCUMENTATION'    : 'Documentation'
		});
	   
		$translateProvider.translations('de', {
		    'TAB_HEADER_HOME'       : 'Home',
            'TAB_HEADER_ENTITY'     : 'Info',
            'TAB_HEADER_STATISTICS' : 'Statistiken',
            'TAB_HEADER_RESULTS'    : 'Ergebnisse',
            'TAB_HEADER_DOCUMENTATION'    : 'Dokumentation'
		});
	   
		$translateProvider.preferredLanguage('de');
  	}]);






