'use strict';

angular.module('geostatsAngularApp')
    .controller('HomeCtrl', function ($scope) {
    
        getCurrentEntityLayer().clearLayers();
    });

