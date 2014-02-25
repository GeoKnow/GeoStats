'use strict';

angular.module('geostatsAngularApp')
  .controller('StatisticsCtrl', function ($scope, $routeParams) {
    
    $scope.sparqlEndpoint = $routeParams.spe ? $routeParams.spe : "http://geostats.aksw.org/sparql" ;
    $scope.graph          = $routeParams.g ? $routeParams.g : "http://geostats.aksw.org";
    $scope.layer          = $routeParams.l ? $routeParams.l : "districts";

    $scope.datacubes = getDataCubes();

    $scope.primaryDataCube   = $routeParams.pdq ? _.findWhere($scope.datacubes, { uri : $routeParams.pdq }) : null;
    $scope.primaryMeasure    = $routeParams.pm ? _.findWhere($scope.datacubes, { uri : $routeParams.pm }) : null;
    $scope.secondaryDataCube = $routeParams.sdq ? _.findWhere($scope.datacubes, { uri : $routeParams.sdq }) : null;
    $scope.secondaryMeasure  = $routeParams.sm ? _.findWhere($scope.datacubes, { uri : $routeParams.sm }) : null;
  });

function getDataCubes() {

    return [
        { uri : "http://geostats.aksw.org/qb/Insolvenzen", label : "Insolvenzen", measures : 
            [
                { uri : "http://geostats.aksw.org/measure/1", label : "Alle" },
                { uri : "http://geostats.aksw.org/measure/2", label : "Einige" },
                { uri : "http://geostats.aksw.org/measure/3", label : "Keine" }
            ] },
        { uri : "http://geostats.aksw.org/qb/Miete", label : "Miete", measures : 
            [
                { uri : "http://geostats.aksw.org/measure/1", label : "Alle" },
                { uri : "http://geostats.aksw.org/measure/2", label : "Einige" },
                { uri : "http://geostats.aksw.org/measure/3", label : "Keine" }
            ] },
        { uri : "http://geostats.aksw.org/qb/Katzen", label : "Katzen", measures : 
            [
                { uri : "http://geostats.aksw.org/measure/1", label : "Alle" },
                { uri : "http://geostats.aksw.org/measure/2", label : "Einige" },
                { uri : "http://geostats.aksw.org/measure/3", label : "Keine" }
            ]  },
        { uri : "http://geostats.aksw.org/qb/Bevoelkerung", label : "Bevölkerung", measures : 
            [
                { uri : "http://geostats.aksw.org/measure/1", label : "Alle" },
                { uri : "http://geostats.aksw.org/measure/2", label : "Einige" },
                { uri : "http://geostats.aksw.org/measure/3", label : "Keine" }
            ]  }
    ];
}
