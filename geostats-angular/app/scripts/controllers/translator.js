'use strict';

angular.module('geostatsAngularApp')
  .config(['$translateProvider', function ($translateProvider) {
        $translateProvider.translations('en', {
            'NOT_AVAILABLE'                     : "Not available",
            'TAB_HEADER_HOME'                   : 'Home',
            'TAB_HEADER_ENTITY'                 : 'Info',
            'TAB_HEADER_STATISTICS'             : 'Statistics',
            'TAB_HEADER_RESULTS'                : 'Results',
            'TAB_HEADER_DOCUMENTATION'          : 'Documentation',
            'TAB_HEADER_FAQ'                    : 'FAQ',
            'TAB_HEADER_LANGUAGE'               : 'Language',
            'TAB_FAQ_QUESTION_TIME'             : 'Is it possible to see the results for different years?',
            'TAB_FAQ_ANSWER_TIME'               : 'Currently not.',
            'TAB_FAQ_QUESTION_GRANULARITY'      : 'Is it possible to change the polygon layer granularity to "municipal"?',
            'TAB_FAQ_ANSWER_GRANULARITY'        : 'Technically this is possible, but you should consider that for example in Germany there are over 13000 municipals which is probably very hard for Leaflet to render.',
            'TAB_FAQ_QUESTION_IMAGES_NOT_SHOWING' : "TAB_FAQ_QUESTION_IMAGES_NOT_SHOWING",
            'TAB_FAQ_ANSWER_IMAGES_NOT_SHOWING'   : "TAB_FAQ_ANSWER_IMAGES_NOT_SHOWING",
            'TAB_FAQ_QUESTION_LANGUAGES'        : "TAB_FAQ_QUESTION_LANGUAGES",
            'TAB_FAQ_ANSWER_LANGUAGES'          : "TAB_FAQ_ANSWER_LANGUAGES"
        });
       
        $translateProvider.translations('de', {
            'NOT_AVAILABLE'                     : "Keine Angabe",
            'TAB_HEADER_HOME'                   : 'Home',
            'TAB_HEADER_ENTITY'                 : 'Info',
            'TAB_HEADER_STATISTICS'             : 'Statistiken',
            'TAB_HEADER_RESULTS'                : 'Ergebnisse',
            'TAB_HEADER_DOCUMENTATION'          : 'Dokumentation',
            'TAB_HEADER_FAQ'                    : 'FAQ',
            'TAB_HEADER_LANGUAGE'               : 'Sprache',
            'TAB_FAQ_QUESTION_TIME'             : 'Kann man die Statistiken über einen Jahresverlauf ansehen?',
            'TAB_FAQ_ANSWER_TIME'               : 'Momentan nicht.',
            'TAB_FAQ_QUESTION_GRANULARITY'      : 'Ist es möglich die Granularität des Polygonlayers auf "Gemeinden" zu erweitern?',
            'TAB_FAQ_ANSWER_GRANULARITY'        : 'Technisch sollte es durchaus möglich sein, es sollte aber auch bedacht werden, dass es zum Beispiel in Deutschland über 13000 Gemeinden gibt und Leaflet mit einer solchen Anzahl eventuell Probleme haben könnte.',
            'TAB_FAQ_QUESTION_IMAGES_NOT_SHOWING' : "Warum werden manche Wappen nicht korrekt geladen?",
            'TAB_FAQ_ANSWER_IMAGES_NOT_SHOWING'   : "Das ist leider ein Problem des DBPedia-Extraction Frameworks, bzw. Wikipedias. Die Daten im deutschen DBpedia Endpunkt sind sehr alt. Außerdem steht auch an Bildern nicht direkt eine Information ob es sich um ein Wappen oder Bild handelt.",
            'TAB_FAQ_QUESTION_LANGUAGES'        : "Wie kann man die Sprachen erweitern?",
            'TAB_FAQ_ANSWER_LANGUAGES'          : "TAB_FAQ_ANSWER_LANGUAGES"
        });
       
        $translateProvider.preferredLanguage('de');
    }]);
