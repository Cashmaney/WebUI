'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('timeline',function() {
    return {
        templateUrl:'/assets/js/sb-admin/directives/timeline/timeline.html',
        restrict: 'E',
        replace: true,
    }
  });
