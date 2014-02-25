'use strict';

angular.module('geostatsAngularApp')
    .controller('EntityCtrl', ['$scope', '$routeParams', '$q', '$rootScope', '$translate', 
        function ($scope, $routeParams, $q, $rootScope, $translate) {

        getMap().closePopup();
        getCurrentEntityLayer().clearLayers();

        if ( $routeParams.uri ) {

            var entities       = getEntities();
            var districts      = _.values(entities['districts']);
            var adminDistricts = _.values(entities['adminstrativeDistricts']);
            var federalStates  = _.values(entities['federalStates']);

            // find the 
            var entity = _.findWhere(districts, { uri : $routeParams.uri});
            if (!entity) entity = _.findWhere(adminDistricts, { uri : $routeParams.uri});
            if (!entity) entity = _.findWhere(federalStates, { uri : $routeParams.uri});

            addPolygon(entity, "red", getCurrentEntityLayer(), true);

            var sparqlService = new Jassa.service.SparqlServiceHttp('http://dbpedia.org/sparql');
            // var sparqlService = new Jassa.service.SparqlServiceHttp('http://geostats-angular/sparql');

            sparqlService = new Jassa.service.SparqlServiceCache(sparqlService);
            sparqlService = new Jassa.service.SparqlServicePaginate(sparqlService, 1000);

            var store = new Jassa.sponate.StoreFacade(sparqlService, {
                dbo: "http://dbpedia.org/ontology/", 
                foaf: "http://xmlns.com/foaf/0.1/"
            });

            store.addMap({
                name : "entity",
                template : [{
                    id : "?s",
                    thumbnail        : "?thumbnail",
                    homepage         : "?homepage",
                    abstract         : "?abstract",
                    area             : "?area",
                    population       : "?population",
                    leader           : "?leader",
                    leaderThumbnail  : "?leaderThumbnail",
                    birthDate        : "?birthDate",
                    birthPlace       : "?birthPlace",
                    party            : "?party",
                    partyThumbnail   : "?partyThumbnail",
                    elevation        : "?elevation"
                }],
                from : "OPTIONAL { ?s dbo:thumbnail ?thumbnail . } \
                        OPTIONAL { ?s dbo:abstract ?abstract . FILTER( lang(?abstract) = '"+$translate.preferredLanguage()+"')} \
                        OPTIONAL { ?s dbo:populationTotal ?population . } \
                        OPTIONAL { ?s dbo:areaTotal ?area . } \
                        OPTIONAL { ?s dbo:elevation ?elevation . } \
                        OPTIONAL { \
                            ?s dbo:leader ?leader .  \
                            OPTIONAL { ?leader dbo:thumbnail ?leaderThumbnail . } \
                            OPTIONAL { ?leader dbo:birthDate ?birthDate . } \
                            OPTIONAL { ?leader dbo:birthPlace ?birthPlace . } \
                            OPTIONAL { \
                                ?leader dbo:party ?party . \
                                OPTIONAL { ?leader dbo:thumbnail ?partyThumbnail } . \
                            }  \
                        } \
                        OPTIONAL { ?s foaf:homepage ?homepage . } "
            });

            // The label util factory can be preconfigured with prefered properties and langs
            var labelUtilFactory = new Jassa.sponate.LabelUtilFactory(['http://www.w3.org/2000/01/rdf-schema#label'], [$translate.preferredLanguage()]);
        
            // A label util can be created based on var names and holds an element and an aggregator factory.
            var labelUtil = labelUtilFactory.createLabelUtil('o', 's', 'p');

            store.addMap({
                name: 'labels',
                template: [{
                    id: '?s',
                    displayLabel: labelUtil.getAggFactory(),
                    hiddenLabels: [{id: '?o'}]
                }],
                from:  labelUtil.getElement()
            });

            // var concept = new Jassa.facete.Concept(Jassa.sparql.ElementString.create("?s a <http://dbpedia.org/ontology/Place> ."), Jassa.rdf.NodeFactory.createVar("s"));
            // var promise = store.entity.find().concept(concept).limit(10).asList();
            var entityConcept = new Jassa.facete.Concept(Jassa.sparql.ElementString.create("?s ?p ?o . FILTER ( ?s = <" + $routeParams.uri.replace("http://de.", "http://") + "> )"), Jassa.rdf.NodeFactory.createVar("s"));
            var promise1 = store.entity.find().concept(entityConcept).asList();
            var promise2 = store.labels.find().concept(entityConcept).asList();
            var promise = $.when.apply(window, [promise1, promise2]);

            // Jassa.sponate.angular.bridgePromise(promise, $q.defer(), $rootScope).then(function(docs, labels){ 
            promise.done(function(docs, labels){ 

                var entity = docs[0];

                $scope.entity = {
                    uri : entity.id.slice(1,-1),
                    homepage : entity.homepage.slice(1,-1),
                    image : entity.thumbnail.slice(1,-1),
                    label : {
                        de : labels[0].displayLabel,
                        en : labels[0].displayLabel
                    },
                    abstract : {
                        de : entity.abstract,
                        en : entity.abstract
                    },
                    area : entity.area ? entity.area + " km²" : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.area = translation; }),
                    population : entity.population ? entity.population : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.population = translation; }),
                    elevation : entity.elevation ? entity.elevation + " m" : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.elevation = translation; }),
                    leader : {
                        uri : entity.leader.slice(1,-1),
                        image : entity.leaderThumbnail.slice(1,-1),
                        birthDate : entity.birthDate,
                        birthPlace : entity.birthPlace,
                        label : {
                            de : "Klaus Wowereit",
                            en : "Klaus Wowereit"
                        },
                        party : {
                            uri : entity.party,
                            label : {
                                de : "SPD",
                                en : "SPD"
                            },
                            image : entity.party.thumbnail.slice(1,-1)
                        }
                    }
                };

                if ( !$scope.$$phase ) $scope.$apply();
            });
        }
        else {

            // hack to show the info in the view
            $scope.info = "info";
        }
  }]);
