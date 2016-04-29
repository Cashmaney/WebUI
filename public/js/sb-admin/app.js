'use strict';
/**
 * @ngdoc overview
 * @name sbAdminApp
 * @description
 * # sbAdminApp
 *
 * Main module of the application.
 */
angular
  .module('sbAdminApp', [
    'oc.lazyLoad',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
  ])
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider',function ($stateProvider,$urlRouterProvider,$ocLazyLoadProvider) {
    
    $ocLazyLoadProvider.config({
      debug:false,
      events:true
    });

    $urlRouterProvider.otherwise('/dashboard/home');

    $stateProvider
      .state('dashboard', {
        url:'/dashboard',
        templateUrl: '/assets/views/dashboard/main.html',
        resolve: {
            loadMyDirectives:function($ocLazyLoad){
                return $ocLazyLoad.load(
                {
                    name:'sbAdminApp',
                    files:[
                    '/assets/js/sb-admin/directives/header/header.js',
                    '/assets/js/sb-admin/directives/header/header-notification/header-notification.js',
                    '/assets/js/sb-admin/directives/sidebar/sidebar.js',
                    '/assets/js/sb-admin/directives/sidebar/sidebar-search/sidebar-search.js'
                    ]
                }),
                $ocLazyLoad.load(
                {
                   name:'toggle-switch',
                   files:["/assets/js/depends/angular-toggle-switch/angular-toggle-switch.min.js",
                          "/assets/js/depends/angular-toggle-switch/angular-toggle-switch.css"
                      ]
                }),
                $ocLazyLoad.load(
                {
                  name:'ngAnimate',
                  files:['/assets/js/depends/angular/angular-animate.min.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngCookies',
                  files:['/assets/js/depends/angular/angular-cookies.min.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngResource',
                  files:['/assets/js/depends/angular/angular-resource.min.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngSanitize',
                  files:['/assets/js/depends/angular/angular-sanitize.min.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngTouch',
                  files:['/assets/js/depends/angular/angular-touch.min.js']
                })
            }
        }
    })
      .state('dashboard.home',{
        url:'/home',
        controller: 'MainCtrl',
        templateUrl:'/assets/views/dashboard/home.html',
        resolve: {
          loadMyFiles:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'sbAdminApp',
              files:[
              '/assets/js/sb-admin/controllers/main.js',
              '/assets/js/sb-admin/directives/timeline/timeline.js',
              '/assets/js/sb-admin/directives/notifications/notifications.js',
              '/assets/js/sb-admin/directives/chat/chat.js',
              '/assets/js/sb-admin/directives/dashboard/stats/stats.js'
              ]
            })
          }
        }
      })
      .state('dashboard.form',{
        templateUrl:'/assets/views/form.html',
        url:'/form'
    })
      .state('dashboard.blank',{
        templateUrl:'/assets/views/pages/blank.html',
        url:'/blank'
    })
      .state('login',{
        templateUrl:'/assets/views/pages/login.html',
        url:'/login'
    })
      .state('dashboard.chart',{
        templateUrl:'/assets/views/chart.html',
        url:'/chart',
        controller:'ChartCtrl',
        resolve: {
          loadMyFile:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'chart.js',
              files:[
                '/assets/js/depends/angular-chart/angular-chart.min.js',
                '/assets/js/depends/angular-chart/angular-chart.css'
              ]
            }),
            $ocLazyLoad.load({
                name:'sbAdminApp',
                files:['/assets/js/sb-admin/controllers/chartContoller.js']
            })
          }
        }
    })
      .state('dashboard.table',{
        templateUrl:'/assets/views/table.html',
        url:'/table'
    })
      .state('dashboard.panels-wells',{
          templateUrl:'/assets/views/ui-elements/panels-wells.html',
          url:'/panels-wells'
      })
      .state('dashboard.buttons',{
        templateUrl:'/assets/views/ui-elements/buttons.html',
        url:'/buttons'
    })
      .state('dashboard.notifications',{
        templateUrl:'/assets/views/ui-elements/notifications.html',
        url:'/notifications'
    })
      .state('dashboard.typography',{
       templateUrl:'/assets/views/ui-elements/typography.html',
       url:'/typography'
   })
      .state('dashboard.icons',{
       templateUrl:'/assets/views/ui-elements/icons.html',
       url:'/icons'
   })
      .state('dashboard.grid',{
       templateUrl:'/assets/views/ui-elements/grid.html',
       url:'/grid'
   })
  }]);

    
