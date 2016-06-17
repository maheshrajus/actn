// sample topology overlay - client side
//
// This is the glue that binds our business logic (in sampleTopovDemo.js)
// to the overlay framework.

(function () {
    'use strict';

    // injected refs
    var $log, tov, stds;
    var selection, selectionType;
    // internal state should be kept in the service module (not here)

    // our overlay definition
    var overlay = {
        // NOTE: this must match the ID defined in AppUiTopovOverlay
        overlayId: 'vn-web-overlay',
        glyphId: 'topo',
        tooltip: 'VN Web Topo Overlay',

        // These glyphs get installed using the overlayId as a prefix.
        // e.g. 'src' is installed as 'PCE-web-overlay-src'
        // They can be referenced (from this overlay) as '*src'
        // That is, the '*' prefix stands in for 'PCE-web-overlay-'
        glyphs: {
            src: {
                vb: '0 0 110 110',
                d: 'M28.7,59.3 M14.9,53 M8.7,39 M32.4,92.5H25l-0.2-3.6' +
                'c-0.5-9-5.4-23.9-12.9-33.5c-5.2-6.6-7-12.8-7-16.3c0-13.3,10.7-24,23.8-24c13.1,0,23.8,10.8,23.8,24c0,3.5-1.8,9.7-7,16.3' +
                'C38,65,33.1,80,32.6,88.9L32.4,92.5z M27.9,89.5h1.7l0-0.7c0.5-9.4,5.7-25.2,13.5-35.2c4.7-6,6.4-11.4,6.4-14.5' +
                'c0-11.6-9.3-21-20.8-21C17.3,18,7.9,27.5,7.9,39c0,3,1.7,8.4,6.4,14.5c7.9,10.1,13.1,25.8,13.5,35.2L27.9,89.5z M28.7,83.2' +
                'M28.6,29.8c-4.7,0-8.5,3.8-8.5,8.5c0,4.7,3.8,8.5,8.5,8.5s8.5-3.8,8.5-8.5C37.1,33.6,33.3,29.8,28.6,29.8z M89.6,47 M89.6,29.5' +
                'c-0.1,3.1-0.1,12.8,0,17c0.1,4.2,14.1-5.5,13.9-8.5C103.4,35.1,89.6,25.6,89.6,29.5z M51,38.1L89.5,38 M89.5,39.5l0-3L51,36.5l0,3' +
                'L89.5,39.5z'
            },
            dst: {
                vb: '0 0 110 110',
                d: 'M80.3,59.8 M85.8,92.5h-7.2L78.4,89c-0.4-8.8-5.2-23.6-12.3-33' +
                'c-4.9-6.5-6.7-12.5-6.7-16c0-13,10.2-23.7,22.7-23.7c12.5,0,22.7,10.6,22.7,23.7c0,3.5-1.8,9.5-6.7,16C91.2,65.4,86.4,80.1,86,89' +
                'L85.8,92.5z M81.4,89.5H83l0-0.7c0.5-9.3,5.4-24.8,12.9-34.7c4.5-5.9,6.1-11.2,6.1-14.2c0-11.4-8.9-20.7-19.8-20.7' +
                'c-10.9,0-19.8,9.3-19.8,20.7c0,3,1.6,8.3,6.1,14.2C76,64,80.9,79.5,81.4,88.8L81.4,89.5z M82.1,30.8c-4.5,0-8.1,3.7-8.1,8.4' +
                's3.6,8.4,8.1,8.4c4.5,0,8.1-3.7,8.1-8.4S86.6,30.8,82.1,30.8z M47.2,47.5 M45.2,30.8c-0.1,3.1-0.1,12.6,0,16.7' +
                'c0.1,4.1,13.4-5.4,13.3-8.4C58.4,36.2,45.2,26.9,45.2,30.8z M45.2,39.1L6.7,39.2 M45.2,40.6l0-3L6.7,37.7l0,3L45.2,40.6z'
            },
            jp: {
                vb: '0 0 110 110',
                d: 'M84.3,89.3L58.9,64.2l-1.4,1.4L83,90.7L84.3,89.3z M27,7.6H7.4v19.2H27V7.6z' +
                'M59.3,47.1H39.8v19.2h19.5V47.1z M102.1,79.5H82.6v19.2h19.5V79.5z M41.7,47.6L19,25.1l-1.2,1.2l22.7,22.5L41.7,47.6z'
            },
        },

       // detail panel button definitions
        buttons: {
            src: {
                gid: '*src',
                tt: 'Set source node',
                cb: function (data) {
                    $log.debug('Set src action invoked with data:', data);
                    stds.setSrc(selection, selectionType);
                }
            },
            dst: {
                gid: '*dst',
                tt: 'Set destination node',
                cb: function (data) {
                    $log.debug('Set dst action invoked with data:', data);
                    stds.setDst(selection, selectionType);
                }
            }
        },
        activate: function () {
            stds.vnHighlightDev();
            $log.debug("VN Web topology overlay ACTIVATED");
        },
        deactivate: function () {
            stds.vnClearAll();
            $log.debug("VN Web topology overlay DEACTIVATED");
        },

        // Key bindings for traffic overlay buttons
        // NOTE: fully qual. button ID is derived from overlay-id and key-name
        keyBindings: {
            0: {
                cb: function () { stds.query(); },
                tt: 'Query the path',
                gid: 'checkMark'
            },
            1: {
                cb: function () { stds.setSrc(selection, selectionType); },
                tt: 'Set sources',
                gid: '*src'
            },
            2: {
                cb: function () { stds.setDst(selection, selectionType); },
                tt: 'Set destinations',
                gid: '*dst'
            },
            3: {
                cb: function () { stds.setPath(); },
                tt: 'Set path',
                gid: 'plus'
            },
            4: {
                cb: function () { stds.remPath(); },
                tt: 'remove path',
                gid: 'minus'
            },
            5: {
                cb: function () { stds.updatePath(); },
                tt: 'update path',
                gid: '*jp'
            },
            _keyOrder: [
                '0', '1', '2', '3', '4', '5'
            ]
        },

        hooks: {
            // hook for handling escape key
            // Must return true to consume ESC, false otherwise.
            escape: function () {
                // Must return true to consume ESC, false otherwise.
                return false;
            },

            // hooks for when the selection changes...
            empty: function () {
                selectionCallback('empty');
            },
            single: function (data) {
                selectionCallback('single', data);
            },
            multi: function (selectOrder) {
                selectionCallback('multi', selectOrder);
            }
        }
    };

    function selectionCallback(x, d) {
        $log.debug('Selection callback', x, d);
        selection = d;
        selectionType = x;
    }

    // invoke code to register with the overlay service
    angular.module('ovVnwebTopov')
        .run(['$log', 'TopoOverlayService', 'VnwebTopovDemoService',

        function (_$log_, _tov_, _stds_) {
            $log = _$log_;
            tov = _tov_;
            stds = _stds_;
            tov.register(overlay);
        }]);

}());
