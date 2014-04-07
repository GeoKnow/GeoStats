'use strict';

angular.module('geostatsAngularApp')
    .controller('ResultCtrl', ['$scope', '$routeParams', '$location',  function ($scope, $routeParams, $location) {

        getCurrentEntityLayer().clearLayers();
        getEntityLayer().clearLayers();

        $scope.sparqlEndpoint          = $routeParams.sep;
        $scope.graph                   = $routeParams.g;
        $scope.color                   = $routeParams.c;
        $scope.layer                   = $routeParams.l ? $routeParams.l : "districts";
        $scope.interval                = $routeParams.i ? $routeParams.i : "keine Gruppierung";
        $scope.primaryDataCube         = $routeParams.pdq;
        $scope.primaryMeasure          = $routeParams.pm;
        $scope.primaryStatisticLabel   = $routeParams.pml ? $routeParams.pml : "Primäre Statistik";
        $scope.secondaryDataCube       = $routeParams.sdq;
        $scope.secondaryMeasure        = $routeParams.sm;
        $scope.secondaryStatisticLabel = $routeParams.sml ? $routeParams.sml : null;

        if ( /result$/.test($location.absUrl()) ) $scope.info = "info";

        $scope.entities = [];

        $scope.queryData = function(sparqlEndpoint, graph, layer, measure, language){

            // initialize the sparql endpoint service
            var sparqlService = new Jassa.service.SparqlServiceHttp(sparqlEndpoint, [graph]);
            sparqlService = new Jassa.service.SparqlServiceCache(sparqlService);
            sparqlService = new Jassa.service.SparqlServicePaginate(sparqlService, 1000);

            // register the namespace
            var store = new Jassa.sponate.StoreFacade(sparqlService, {
                dbo: "http://dbpedia.org/ontology/", 
                foaf: "http://xmlns.com/foaf/0.1/",
                qb: "http://purl.org/linked-data/cube#",
                rdfs: "http://www.w3.org/2000/01/rdf-schema#",
                gs: "http://geostats.aksw.org/qb/"
            });

            // create a pattern for a given hierachy level
            var layerPattern = 
            layer == 'districts' ? '?layer a dbo:District .' : layer == 'federalStates' ? '?layer a dbo:FederalState .' : '?layer a dbo:AdministrativeDistrict .';

            // rdf to json mapping
            var template = {
                id : "?observation", 
                primary : "?primary", 
                layer : [{ 
                    id : "?layer", 
                    label : "?label",
                    thumbnail : "?thumbnail"
                }], 
                area : [{
                    id : "?area" 
                }]};

            // the actual query patterns
            var from = "?observation a qb:Observation . \
                    ?observation gs:refArea ?area . \
                    ?layer owl:sameAs ?area . \
                    ?layer rdfs:label ?label . \
                    OPTIONAL { ?layer dbo:thumbnail ?thumbnail . } \
                    FILTER(lang(?label) = '"+language+"') . " + 
                    layerPattern + 
                    " ?observation <" + measure + "> ?primary . ";

            store.addMap({
                name : "observation",
                template : [template],
                from : from
            });

            return store.observation.find().asList();
        }

        $scope.merge = function(primaryObservations, secondaryObservations){

            // we can only merge them if they exist
            if ( secondaryObservations ) {

                _.each(secondaryObservations, function(secondaryObservation){

                    var primaryObservation = 
                        _.find(primaryObservations,function(primObs){ 
                            if ( primObs.area[0].id == secondaryObservation.area[0].id ) return primObs;
                        });

                    primaryObservation.secondary = secondaryObservation.primary;
                });
            }
        }

        $scope.getColorLuminance = function(hex, lum) {

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

        $scope.split = function(a, n) {
            var len = a.length,out = [], i = 0;
            while (i < len) {
                var size = Math.ceil((len - i) / n--);
                out.push(a.slice(i, i + size));
                i += size;
            }
            return out;
        }

        $scope.processObservations = function(promise) {

            var minMax = { min: 100000000, max : -100000000 };
            var templateEntities = _.values(getEntities()[$scope.layer]);

            promise.done(function(primaryObservations, secondaryObservations){

                $scope.merge(primaryObservations, secondaryObservations);

                console.log("primary");
                console.log(primaryObservations);
                
                // we need to normalize the values in the first step
                _.each(primaryObservations, function(observation){



                    var dbpediaUri   = observation.layer[0].id.slice(1,-1);
                    var entity       = angular.copy(_.find(templateEntities, function(obj) { return obj.uri == dbpediaUri }));
                    entity.id        = dbpediaUri; 
                    entity.combined  = "";

                    if ( observation.primary == "." || observation.primary == "-" || observation.secondary == "." || observation.secondary == "-") {
                        
                        entity.primary = -1;
                        entity.secondary = -1;
                        entity.disqualified = true;
                    }
                    if ( !entity.disqualified ) {

                        entity.primary   = observation.primary;
                        entity.secondary = observation.secondary;
                        entity.label     = observation.layer[0].label;
                        entity.img       = observation.layer[0].thumbnail ? observation.layer[0].thumbnail.slice(1,-1) : "";

                        if ( !observation.secondary ) entity.combined = entity.primary;
                        else entity.combined  = entity.primary / entity.secondary;

                        minMax.min  = Math.min(minMax.min, entity.combined);
                        minMax.max  = Math.max(minMax.max, entity.combined);
                    }                
                    $scope.entities.push(entity);
                });
                
                _.each($scope.entities, function(entity){

                    entity.combined = (entity.combined - minMax.min) / (minMax.max - minMax.min);
                    var color = $scope.getColorLuminance($scope.color, 0 - (1 - (entity.combined)));

                    // if a entity has no value, then we display it in a different color
                    entity.disqualified ? addPolygon(entity, "yellow", getEntityLayer(), false) : addPolygon(entity, color, getEntityLayer(), false); 
                });

                $scope.entities = _.sortBy($scope.entities, function(entity){ return entity.combined; });

                if ($scope.interval <= 10 && $scope.interval >= 1) {

                    var chunks = $scope.split($scope.entities, $scope.interval);
                    var intensity = 0;
                    _.each(chunks, function(chunk){

                        _.each(chunk, function(entity){
                            
                            var color = $scope.getColorLuminance($scope.color, 0 - (1 - (intensity)));
                            // if a entity has no value, then we display it in a different color
                            entity.disqualified ? addPolygon(entity, "yellow", getEntityLayer(), false) : addPolygon(entity, color, getEntityLayer(), false); 
                        })
                        intensity += (1.0 / $scope.interval);
                    });                    
                }

                $scope.$apply();
            });
        }

        // // start to download the observations
        if ( $scope.sparqlEndpoint && $scope.graph && $scope.layer && $scope.primaryMeasure ) {

            // we need to combine the promises for the first and second measure
            var promises = [];
            promises.push($scope.queryData($scope.sparqlEndpoint, $scope.graph, $scope.layer, $scope.primaryMeasure.slice(1,-1), "de"));
            if ($scope.secondaryMeasure) promises.push($scope.queryData($scope.sparqlEndpoint, $scope.graph, $scope.layer, $scope.secondaryMeasure.slice(1,-1), "de"));

            // if all promises have succeded we can merge them and display them
            $scope.processObservations($.when.apply(window, promises));
        }
        else $scope.error = "Fehler";

        // on hover we want to highlight the polygon for the district on the map
        $scope.select = function(entity) {
            
            addPolygon(_.where($scope.entities, {uri : entity.uri})[0], "red", getCurrentEntityLayer(), true);
        };
    }]);