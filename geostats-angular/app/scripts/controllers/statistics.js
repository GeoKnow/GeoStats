'use strict';

angular.module('geostatsAngularApp')
    .controller('StatisticsCtrl', ['$scope', '$routeParams', '$location', function ($scope, $routeParams, $location) {
    
        $scope.sparqlEndpoint = $routeParams.spe ? $routeParams.spe : "http://139.18.2.142:8890/sparql" ;
        $scope.graph          = $routeParams.g ? $routeParams.g : "http://geostats.aksw.org";
        $scope.layers         = [{ id: 'federalStates', label : "Bundesländer" }, 
                                 { id: 'administrativeDistricts', label : "Regierungsbezirke" }, 
                                 { id: 'districts', label : "Kreise und kreisfreie Städte" }];

        $scope.layer          = $routeParams.l ? _.where($scope.layers, {id:$routeParams.l})[0] : $scope.layers[2];
        $scope.intervals      = ['1','2','3','4','5','6','7','8','10','keine Gruppierung'];
        $scope.interval       = $routeParams.i ? $routeParams.i : "keine Gruppierung";


        if ( $routeParams.c ) {
            $scope.color = $routeParams.c;
            $("#colorpicker").css({"background-color": "#" + $scope.color});
        }
        else {
            $scope.color = "#00FF4C";
            $("#colorpicker").css({"background-color": "#" + $scope.color});
        }

        $scope.$watch('color', function(){
            $scope.color = $scope.color.replace("#", "");
            $("#colorpicker").css({"background-color": "#" + $scope.color});
        })
        
        $scope.$watch('primaryDataCube', function(){ 

            if ( $routeParams.pm ) {
                _.each($scope.datacubes, function(datacube, index){
                    if ( $scope.primaryDataCube.id == datacube.id ) {
                        $scope.primaryMeasure = _.findWhere($scope.datacubes[index].measures, { id : $routeParams.pm  });
                    }
                });
            }
        });

        $scope.$watch('secondaryDataCube', function(){ 

            if ( typeof $scope.secondaryDataCube === 'undefined' ) {
                $scope.secondaryMeasure = null;
            }

            if ( $routeParams.pm ) {
                _.each($scope.datacubes, function(datacube, index){
                    if ( typeof $scope.secondaryDataCube !== 'undefined' && $scope.secondaryDataCube.id == datacube.id ) {
                        $scope.secondaryMeasure = _.findWhere($scope.datacubes[index].measures, { id : $routeParams.sm  });
                    }
                });
            }
        });

        $scope.generateVisualization = function(){

            $scope.error = null;

            if ( !$scope.primaryDataCube ) $scope.error = "Bitte wähle eine Primäre Statistik.";
            if ( !$scope.primaryMeasure ) $scope.error = "Bitte wähle das Maß für die Primäre Statistik aus.";
            
            if ( !$scope.error ) {
                var options = {};
                options.i   = $scope.interval;
                options.l   = $scope.layer.id;
                options.c   = $scope.color;
                options.sep = $scope.sparqlEndpoint;
                options.g   = $scope.graph;
                options.pdq = $scope.primaryDataCube.id;
                options.pm  = $scope.primaryMeasure.id;
                options.pml = $scope.primaryMeasure.label;

                if ($scope.secondaryDataCube) options.sdq = $scope.secondaryDataCube.id;
                if ($scope.secondaryMeasure) options.sm   = $scope.secondaryMeasure.id;
                if ($scope.secondaryMeasure) options.sml  = $scope.secondaryMeasure.label;

                $location.path('/result').search(options);
            }
        }

        $scope.getDataCubes = function() {

            var sparqlService = new Jassa.service.SparqlServiceHttp('http://139.18.2.142:8890/sparql', ['http://geostats.aksw.org']);
            sparqlService = new Jassa.service.SparqlServiceCache(sparqlService);
            sparqlService = new Jassa.service.SparqlServicePaginate(sparqlService, 1000);

            var store = new Jassa.sponate.StoreFacade(sparqlService, {
                dbo: "http://dbpedia.org/ontology/", 
                foaf: "http://xmlns.com/foaf/0.1/",
                qb: "http://purl.org/linked-data/cube#",
                rdfs: "http://www.w3.org/2000/01/rdf-schema#"
            });

            store.addMap({
                name : "datacube",
                template : [{
                    id : "?dataset",
                    dataset    : [{
                        id : "?dataset",
                        label : "?dsLabel",
                        structure: [{
                            id : "?structure",
                            spec: [{
                                id : "?spec",
                                measure : [{
                                    id : "?measure",
                                    label : "?label"
                                }]
                            }]
                        }]
                    }]
                }],
                from : "?dataset a qb:DataSet . \
                        ?dataset qb:structure ?structure . \
                        ?dataset rdfs:label ?dsLabel . \
                        ?structure qb:component ?spec . \
                        ?spec qb:measure ?measure . \
                        ?measure rdfs:label ?label "
            });

            var results = [];
            
            store.datacube.find().asList().done(function(docs){ 

                _.each(docs, function(doc){

                    var dataset      = {};
                    dataset.id       = doc.dataset[0].id;
                    dataset.label    = doc.dataset[0].label;   
                    dataset.measures = [];      

                    var measures     = doc.dataset[0].structure[0].spec;
                    _.each(measures, function(measure){

                        dataset.measures.push(measure.measure[0]);
                    })

                    results.push(dataset);
                });

                $scope.primaryDataCube   = $routeParams.pdq ? _.findWhere($scope.datacubes, { id : $routeParams.pdq }) : null;
                $scope.secondaryDataCube = $routeParams.sdq ? _.findWhere($scope.datacubes, { id : $routeParams.sdq }) : null;

                if ( !$scope.$$phase ) $scope.$apply();
            });

            return results;
        }

        $scope.datacubes      = $scope.getDataCubes($scope, $routeParams);
    }]);