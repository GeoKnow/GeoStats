'use strict';

var map;
var entities;
var currentEntityLayer;
var entityLayer;

angular.module('geostatsAngularApp')
  .controller('MapCtrl', [ "$scope", "$filter", function($scope, $filter) {

    entityLayer         = L.layerGroup();
    currentEntityLayer  = L.layerGroup();

    var tileLayer = L.tileLayer('http://{s}.tile.cloudmade.com/50a9040722d340a79cd8bcf277194b99/96931/256/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="http://openstreetmap.org">OSM</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>'
    });

    map = L.map('map', {
        center: [51.399206, 10.349121],
        zoom: 6,
        maxZoom: 6,
        minZoom: 6,
        dragging : true,
        zoomControl : true,
        scrollWheelZoom : false,
        maxBounds : [[58.8609857506449, 0],[42.19752230305685, 20.81201171875]],
        layers: [tileLayer , currentEntityLayer, entityLayer]
    });

    entities = getEntityGeometries();
    console.log(entities);

    // _.each(_.values(entities['federalStates']), function (entity) { 
    _.each(_.values(entities['districts']), function (entity) { 

    // _.each(_.values(entities['administrativeDistricts']), function (entity) { 

        addPolygon(entity, "black", entityLayer, false);       
    });
}]);

function getEntityLayer(){

    return entityLayer;
}

function getCurrentEntityLayer(){

    return currentEntityLayer;
}

function getMap(){

    return map;
}

function getEntities() {

    return entities;
}

function getEntityGeometries() {

    var data = undefined;
    $.ajax({
        dataType: "json",
        url: 'data/geometries.json',
        async: false,
        success: function(ent){

            data = ent;
        }
    });
    return data;
}

function addPolygon(entity, color, layer, clearLayer) {

    if (clearLayer) layer.clearLayers();
    var polygon = L.multiPolygon(entity.sgeo, { stroke: true, color : "red", weight : 1, fillColor: color, fillOpacity: 1.0 });
    polygon.addTo(layer);
    polygon.district = entity;
    polygon.bindPopup(getPopupHtml(entity));
}

function getPopupHtml(entity){

    var html =
    "<div>" +
        "<h1><a href='#entity/"+entity.uri+"'>" + entity.label + "</a></h1>" +
        "<div>" +
            "<img class='' style='float:left; padding-right: 10px;' width='50px' src='" +entity.img + "'/>" +
            "<p>" + truncate(entity.comment, 200) + "</p>" +
            "<div style='clear: both;'></div> \
            <a href='#entity/"+entity.uri+"'> \
                <button type='button' class='btn btn-default' id='start'>Expose</button> \
            </a> \
        </div> \
    </div>";

    return html;
}

function truncate(input, chars, breakOnWord) {
    if (isNaN(chars)) return input;
    if (chars <= 0) return '';
    if (input && input.length > chars) {
        input = input.substring(0, chars);

        if (!breakOnWord) {
            var lastspace = input.lastIndexOf(' ');
            //get last space
            if (lastspace !== -1) {
                input = input.substr(0, lastspace);
            }
        }else{
            while(input.charAt(input.length-1) === ' '){
                input = input.substr(0, input.length -1);
            }
        }
        return input + '...';
    }
    return input;
}