'use strict';

describe('Controller: TranslatorCtrl', function () {

  // load the controller's module
  beforeEach(module('geostatsAngularApp'));

  var TranslatorCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    TranslatorCtrl = $controller('TranslatorCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
