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

            // var sparqlService = new Jassa.service.SparqlServiceHttp('http://dbpedia.org/sparql');
            var sparqlService = new Jassa.service.SparqlServiceHttp('http://geostats-angular/sparql');

            sparqlService = new Jassa.service.SparqlServiceCache(sparqlService);
            sparqlService = new Jassa.service.SparqlServicePaginate(sparqlService, 1000);

            var store = new Jassa.sponate.StoreFacade(sparqlService, {
                dbo: "http://dbpedia.org/ontology/", 
                foaf: "http://xmlns.com/foaf/0.1/",
                geostats:  "http://geostats.aksw.org/"
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
                    vehicleCode      : "?vehicleCode",
                    leader           : "?leader",
                    leaderThumbnail  : "?leaderThumbnail",
                    leaderLabel      : "?leaderLabel",
                    birthDate        : "?birthDate",
                    birthPlace       : "?bp",
                    birthPlaceLabel  : "?bpl",
                    party            : "?party",
                    pt               : "?pt",
                    partyLabel       : "?partyLabel",
                    elevation        : "?elevation",
                    destatisID       : "?destatisID",
                    nuts             : "?nuts"
                }],
                from : "OPTIONAL { ?s dbo:thumbnail ?thumbnail . } \
                        OPTIONAL { ?s dbo:abstract ?abstract . FILTER( lang(?abstract) = '"+$translate.preferredLanguage()+"')} \
                        OPTIONAL { ?s dbo:populationTotal ?population . } \
                        OPTIONAL { ?s dbo:areaTotal ?area . } \
                        OPTIONAL { ?s dbo:vehicleCode ?vehicleCode  . } \
                        OPTIONAL { ?s dbo:elevation ?elevation . } \
                        OPTIONAL { ?nuts owl:sameAs ?s . FILTER( regex(?nuts, '^http://nuts.geovocab.org/id/', 'i')) } \
                        OPTIONAL { ?s geostats:regionalStatistikId ?destatisID . } \
                        OPTIONAL { \
                            ?s dbo:leader ?leader .  \
                            FILTER(regex(str(?s), '^http://de.dbpedia.org/resource/', 'i')) . \
                            ?leader rdfs:label ?leaderLabel . FILTER( lang(?leaderLabel) = '"+$translate.preferredLanguage()+"') \
                            OPTIONAL { ?leader dbo:thumbnail ?leaderThumbnail . } \
                            OPTIONAL { ?leader dbo:birthDate ?birthDate . } \
                            OPTIONAL { ?leader dbo:birthPlace ?bp . ?bp rdfs:label ?bpl . FILTER( LANGMATCHES(lang(?bpl), '"+$translate.preferredLanguage()+"')) } \
                            OPTIONAL { \
                                ?leader dbo:party ?party . \
                                OPTIONAL { ?party rdfs:label ?partyLabel .  FILTER( LANGMATCHES(LANG(?partyLabel), 'de')) } . \
                                OPTIONAL { ?party dbo:thumbnail ?pt } . \
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
            var entityConcept = new Jassa.facete.Concept(Jassa.sparql.ElementString.create("?s ?p ?o . FILTER ( ?s = <" + $routeParams.uri/*.replace("http://de.", "http://")*/ + "> )"), Jassa.rdf.NodeFactory.createVar("s"));
            var promise1 = store.entity.find().concept(entityConcept).asList();
            var promise2 = store.labels.find().concept(entityConcept).asList();
            var promise = $.when.apply(window, [promise1, promise2]);

            // Jassa.sponate.angular.bridgePromise(promise, $q.defer(), $rootScope).then(function(docs, labels){ 
            promise.done(function(docs, labels){ 

                var entity = docs[0];

                console.log(entity);

                $scope.entity = {
                    uri : entity.id.slice(1,-1),
                    wikipedia : entity.id.slice(1,-1).replace("http://de.dbpedia.org/resource/", "http://de.wikipedia.org/wiki/"),
                    homepage : entity.homepage ? entity.homepage.slice(1,-1) : "Keine Angabe",
                    image : entity.thumbnail ? entity.thumbnail.slice(1,-1) : "http://fribi.de/img/uploads/groups/782836_1288967599.jpg",
                    label : entity.label,
                    abstract : entity.abstract,
                    vehicleCode : entity.vehicleCode ? entity.vehicleCode : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.vehicleCode = translation; }),
                    area : entity.area ? entity.area / 1000000 + " km²" : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.area = translation; }),
                    population : entity.population ? entity.population : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.population = translation; }),
                    elevation : entity.elevation ? entity.elevation + " m" : $translate('NOT_AVAILABLE').then(function (translation) { $scope.entity.elevation = translation; }),
                    leader : {
                        uri : entity.leader ? entity.leader.slice(1,-1) : "",
                        image : entity.leaderThumbnail ? entity.leaderThumbnail.slice(1,-1) : "",
                        birthDate : entity.birthDate,
                        birthPlace : {
                            uri : entity.birthPlace ? entity.birthPlace.slice(1,-1) : "",
                            label : entity.birthPlaceLabel ? entity.birthPlaceLabel : ""
                        },
                        label : entity.leaderLabel,
                        party : {
                            uri : entity.party,
                            label : entity.partyLabel,
                            image : entity.pt ? entity.pt.slice(1,-1) : ""
                        }
                    },
                    destatisID : entity.destatisID,
                    nuts : {
                        uri : entity.nuts ? entity.nuts.slice(1,-1) : "",
                        label : entity.nuts ? entity.nuts.slice(1,-1).replace("http://nuts.geovocab.org/id/", "") : ""
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
