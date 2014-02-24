'use strict';

angular.module('geostatsAngularApp')
    .controller('ResultCtrl', function ($scope, $routeParams) {

        getCurrentEntityLayer().clearLayers();
        getEntityLayer().clearLayers();

        var sparqlEndpoint    = $routeParams.spe;
        var graph             = $routeParams.g;
        var layer             = $routeParams.l ? $routeParams.l : "districts";
        var primaryDataCube   = $routeParams.pdq;
        var primaryMeasure    = $routeParams.pm;
        var secondaryDataCube = $routeParams.sdq;
        var secondaryMeasure  = $routeParams.sm;

        $scope.primaryStatisticLabel = "Primäre Statistik";
        $scope.secondaryStatisticLabel = "Sekundäre Statistik";

        console.log(layer);

        $scope.entities = queryData(sparqlEndpoint, graph, layer, primaryDataCube, primaryMeasure, secondaryDataCube, secondaryMeasure);
        
        var minMax = getMinMax($scope.entities);

        _.each($scope.entities, function(entity){

            entity.combined = (entity.combined - minMax.min) / (minMax.max - minMax.min);
            var color = getColorLuminance('00FF4C', 0 - (1 - (entity.combined)));

            // if a entity has no value, then we display it in a different color
            entity.disqualified ? 
                addPolygon(entity, "yellow", getEntityLayer(), false) : addPolygon(entity, color, getEntityLayer(), false);     
        });

        // on hove we want to highlight the polygon for the district on the map
        $scope.select = function(entity) {
            
            addPolygon(_.where($scope.entities, {uri : entity.uri})[0], "red", getCurrentEntityLayer(), true);
        };
    });
    
    // get the date from the sparql endpoint
    function queryData(sparqlEndpoint, graph, layer, primaryDataCube, primaryMeasure, secondaryDataCube, secondaryMeasure) {

        var templateEntities = getEntities();
        var entities = [];
        _.each(_.values(templateEntities[layer]), function(entity){

            var e       = angular.copy(entity);
            e.primary   = Math.random(); 
            e.secondary = Math.random();
            e.combined  = e.primary / e.secondary;
            entities.push(e);
        });

        return entities;
    }

    function getMinMax(entities) {

        var min = 100000000;
        var max = -2;

        _.each(entities, function(entity){
                       
            min = Math.min(min, entity.combined);
            max = Math.max(max, entity.combined);
        });

        return { min : min, max : max };
    }

    function getColorLuminance(hex, lum) {

        // validate hex string
        hex = String(hex).replace(/[^0-9a-f]/gi, '');
        if (hex.length < 6) {
            hex = hex[0]+hex[0]+hex[1]+hex[1]+hex[2]+hex[2];
        }
        lum = lum || 0;

        // convert to decimal and change luminosity
        var rgb = "#", c, i;
        for (i = 0; i < 3; i++) {
            c = parseInt(hex.substr(i*2,2), 16);
            c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
            rgb += ("00"+c).substr(c.length);
        }

        return rgb;
    }