'use strict';

var map;
var entities;
var currentEntityLayer;
var entityLayer;

angular.module('geostatsAngularApp')
  .controller('MapCtrl', [ "$scope", function($scope) {

    entityLayer         = L.layerGroup();
    currentEntityLayer  = L.layerGroup();

    var tileLayer = L.tileLayer('http://{s}.tile.cloudmade.com/50a9040722d340a79cd8bcf277194b99/96931/256/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="http://openstreetmap.org">OSM</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>'
    });

    map = L.map('map', {
        center: [51.399206, 10.349121],
        zoom: 6,
        maxZoom: 18,
        minZoom: 1,
        dragging : true,
        zoomControl : true,
        layers: [tileLayer , currentEntityLayer, entityLayer]
    });

    entities = getEntityGeometries();

    _.each(_.values(entities['districts']), function (entity) { 
        addPolygon(entity, "black", entityLayer); 
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

function getPopupHtml(entity) {

    var html =
        "<div>" +
            "<h1><a href='#entity/"+entity.uri+"'>" + entity.label + "</a></h1>" +
            "<div>" +
                "<img class='' style='float:left; padding-right: 10px;' width='50px' src='" +entity.img + "'/>" +
                "<p>" + entity.comment + "</p>" +
                "<div style='clear: both;'/>" +
            "</div>" +    
        "</div>";

    return html;
}