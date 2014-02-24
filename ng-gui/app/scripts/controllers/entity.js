'use strict';

angular.module('geostatsAngularApp')
  .controller('EntityCtrl', function ($scope, $routeParams) {

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

        $scope.entity = {
            uri : "http://de.dbpedia.org/resource/Berlin",
            homepage : "http://www.berlin.de/",
            image : "http://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Flag_of_Berlin.svg/200px-Flag_of_Berlin.svg.png",
            label : {
                de : "Berlin",
                en : "Berlin"
            },
            abstract : {
                de : "Berlin ist die Hauptstadt der Bundesrepublik Deutschland. Als eigenes Land ist der Stadtstaat Berlin mit etwa 3,4 Millionen Einwohnern die bevölkerungsreichste und mit 892 Quadratkilometern die flächengrößte Kommune Deutschlands sowie nach Einwohnern die zweitgrößte der Europäischen Union. Berlin bildet das Zentrum der Metropolregion Berlin/Brandenburg (6 Millionen Einwohner) und der Agglomeration Berlin (4,4 Millionen Einwohner). Der Stadtstaat unterteilt sich in zwölf Bezirke.",
                en : "Berlin ist Hauptstadt und Regierungssitz der Bundesrepublik Deutschland. Als eigenes Land bildet es das Zentrum der Metropolregion Berlin/Brandenburg. Der Stadtstaat unterteilt sich in zwölf Bezirke und ist mit über 3,5 Millionen Einwohnern die bevölkerungsreichste und mit rund 892 Quadratkilometern die flächengrößte Stadt Deutschlands und Mitteleuropas sowie nach Einwohnern die zweitgrößte der Europäischen Union. Im Ballungsraum Berlin leben fast 4,5 Millionen Menschen. Neben den Flüssen Spree und Havel befinden sich im Stadtgebiet kleinere Fließgewässer sowie zahlreiche Seen und Wälder. Erstmals 1237 urkundlich erwähnt, war Berlin im Verlauf der Geschichte und in verschiedenen Staatsformen Hauptstadt Brandenburgs, Preußens und des Deutschen Reichs. Faktisch war der Ostteil der Stadt Hauptstadt der Deutschen Demokratischen Republik. Seit der Deutschen Wiedervereinigung im Jahr 1990 ist Berlin gesamtdeutsche Hauptstadt mit Sitz des Bundespräsidenten seit 1994, des Deutschen Bundestags seit 1999 sowie des Bundesrats seit 2000. Berlin gilt als Weltstadt Es ist ein wichtiger europäischer Verkehrsknotenpunkt und eine der meistbesuchten Städte des Kontinents. Die Sportereignisse, Universitäten, Forschungseinrichtungen und Museen der Stadt genießen internationalen Ruf. Berlins jüngere Geschichte, Nachtleben, Architektur und vielfältige Lebensbedingungen sind weltbekannt."
            },
            area : "891.85 km²",
            population : "3.401.147",
            elevation : "34 m",
            leader : {
                uri : "http://de.dbpedia.org/resource/Klaus_Wowereit",
                image : "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Klaus_Wowereit_2012-02-24.jpg/200px-Klaus_Wowereit_2012-02-24.jpg",
                birthDate : "1953-10-01",
                birthPlace : "Berlin",
                label : {
                    de : "Klaus Wowereit",
                    en : "Klaus Wowereit"
                },
                party : {
                    uri : "http://de.dbpedia.org/resource/Social_Democratic_Party_of_Germany",
                    label : {
                        de : "SPD",
                        en : "SPD"
                    },
                    image : "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/SPD_logo.svg/200px-SPD_logo.svg.png"
                }
            }
        };
    }
    else {

        // hack to show the info in the view
        $scope.info = "info";
    }
  });
