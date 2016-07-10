/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 Sample Demo module. This contains the "business logic" for the topology
 overlay that we are implementing.
 */

(function () {
    'use strict';

    // injected refs
    var $log, fs, flash, wss, tds, ds;
    var tunnelNameDataQuery, tunnelNameDataQueryRem, tunnelNameDataQueryUpd,
        tunnelNameDataQueryUpdConstr;
    // constants
    var vnQuerymsg = 'vnQuerymsg',
        vnQuerymsgHandle = 'vnQuerymsgHandle',
        vnRemovemsgHandle = 'vnRemovemsgHandle',
        vnClear = 'vnClear',
        showVnInfoMsg = 'showVnInfoMsg',
        showVnInfoMsgRem = 'showVnInfoMsgRem',
        srcMessage = 'pceTopovSetSrc',
        dstMessage = 'pceTopovSetDst',
        vnSetup = 'vnSetup',
        showVnInfoMsgUpdate = 'showVnInfoMsgUpdate',
        vnUpdatemsgHandle = 'vnUpdatemsgHandle',
        vnUpdatemsgHandleConstr = 'vnUpdatemsgHandleConstr',
        showVnInfoMsgUpdateCnstrs = 'showVnInfoMsgUpdateCnstrs',
        vnDeviceHighlight = 'vnDeviceHighlight';

    var handlerMap = {}, handlerMapRem = {}, handlerMapUpd = {},
        handlerMapUpdConstr = {};
    // === ---------------------------

    //===Helper function

    function dClose() {
        $log.debug('Dialog Close button clicked (or Esc pressed)');
    }

    function isChecked(cboxId) {
        return d3.select('#' + cboxId).property('checked');
    }

    function getCheckedValue(cboxId) {
        return d3.select('#' + cboxId).property('value');
    }

    function dOkQuery() {

        tunnelNameDataQuery.a.forEach( function (val, idx) {
            var vnIdVal = isChecked('vn-id-query-'+idx);
            if (vnIdVal) {
                wss.sendEvent(vnQuerymsgHandle, {
                    vnid: val
                });
            }
        } );

        flash.flash('query VN path message');
    }

    function dOkRem() {

        tunnelNameDataQueryRem.a.forEach( function (val, idx) {
            var vnIdVal = isChecked('vn-id-remove-'+idx);
            if (vnIdVal) {
                wss.sendEvent(vnRemovemsgHandle, {
                    vnid: val
                });
            }
        } );

        flash.flash('remove VN path message');
    }

    function dOkUpdate() {
        tunnelNameDataQueryUpd.a.forEach( function (val, idx) {
            var vnIdVal = isChecked('vn-id-update-'+idx);
            if (vnIdVal) {
                handlerMapUpdConstr[showVnInfoMsgUpdateCnstrs] = showVnQueryInfoUpdateConstrts;
                wss.bindHandlers(handlerMapUpdConstr);

                wss.sendEvent(vnUpdatemsgHandle, {
                    vnid: val
                });
            }
        } );

        flash.flash('update VN path message');
    }

    function dOkUpdateConstr() {
        var checkType;
        tunnelNameDataQueryUpdConstr.a.forEach( function (val, idx) {
            if (val == 'SRC') {
                checkType = 'src';
                return;
            }

            if (val == 'DST') {
                checkType = 'dst';
                return;
            } 

            if (checkType == 'src') {
                var srcDev = isChecked('vn-id-update-src-'+idx);
                if (srcDev) {
                    wss.sendEvent(srcMessage, {
                        id: val
                    });
                }
            }

            if (checkType == 'dst') {
                var dstDev = isChecked('vn-id-update-dst-'+idx);
                if (dstDev) {
                    wss.sendEvent(dstMessage, {
                        id: val
                    });
                }
            }
        } );

        var bandWidth = isChecked('vn-id-update-bw'),
        bandValue = null,
        bandType = null;

        if (bandWidth) {
            bandValue = getCheckedValue('vn-id-update-bw-val');

            if (isChecked('vn-id-update-bw-kbps-val')) {
                bandType = 'kbps';
            } else if (isChecked('vn-id-update-bw-mbps-val')) {
                bandType = 'mbps';
            }
        }

        var costType = isChecked('vn-id-update-cost'),
            costTypeVal = null;

        if (costType) {
            if (isChecked('vn-id-update-cost-igp-val')) {
                costTypeVal = 'igp';
            } else if (isChecked('vn-id-update-cost-te-val')) {
                costTypeVal = 'te';
            }
        }

        wss.sendEvent(vnUpdatemsgHandleConstr, {
            bw: bandValue,
            bwtype: bandType,
            ctype: costTypeVal
        });

        flash.flash('update VN path message');
    }

    function createUserTextQuery(data) {

        var content = ds.createDiv(),
            form = content.append('form'),
            p = form.append('p');

        p.append('span').text('VN Name');
        p.append('br');

        data.a.forEach( function (val, idx) {
            p.append('input').attr({
                id: 'vn-id-query-'+idx,
                type: 'radio',
                name: 'vn-id-name-query',
                value: val
            });

            p.append('span').text(val);
            p.append('br');
        } );

        return content;
    }

    function createUserTextQueryRem(data) {

        var content = ds.createDiv(),
            form = content.append('form'),
            p = form.append('p');

        p.append('span').text('VN Name');
        p.append('br');

        data.a.forEach( function (val, idx) {
            p.append('input').attr({
                id: 'vn-id-remove-'+idx,
                type: 'radio',
                name: 'vn-id-remove',
                value: val
            });

            p.append('span').text(val);
            p.append('br');
        } );

        return content;
    }

    function createUserTextQueryUpd(data) {

        var content = ds.createDiv(),
            form = content.append('form'),
            p = form.append('p');

        p.append('span').text('VN Name');
        p.append('br');

        data.a.forEach( function (val, idx) {
            p.append('input').attr({
                id: 'vn-id-update-'+idx,
                type: 'radio',
                name: 'vn-id-update',
                value: val
            });

            p.append('span').text(val);
            p.append('br');
        } );

        return content;
    }

    function createUserTextQueryUpdConstr(data) {

        var content = ds.createDiv(),
            form = content.append('form'),
            p = form.append('p');
        var constType = 'none';

        function addAttribute(name, id, nameField, type) {
            p.append('input').attr({
                type: type,
                name: name,
                id: id,
                checked: 'checked'
            });

            p.append('span').text(nameField);
            p.append('br');
        }

        data.a.forEach( function (val, idx) {
            if (val == 'VnName') {
                constType = 'VN';
                return;
            }

            if (val == 'BandWidth') {
                constType = 'BW';
                return;
            }

            if (val == 'CostType') {
                constType = 'CT';
                return;
            }

            if (val == 'SRC') {
                constType = 'SRC';
                p.append('span').text('SRC:');
                p.append('br');
                return;
            }

            if (val == 'DST') {
                constType = 'DST';
                p.append('span').text('DST:');
                p.append('br');
                return;
            }

            if (constType == 'VN') {
                p.append('span').text('VN Name: ');
                p.append('span').text(val);
                p.append('br');
            }

            if (constType == 'BW') {
                addAttribute('vn-id-update-bwbox-name', 'vn-id-update-bw', 'Band Width', 'checkbox');
                p.append('input').attr({
                    id: 'vn-id-update-bw-val',
                    type: 'number',
                    name: 'vn-id-update-bwv-name',
                    value: val
                });
                p.append('br');
                p.append('input').attr({
                    id: 'vn-id-update-bw-kbps-val',
                    type: 'radio',
                    name: 'vn-id-update-bw-val-name',
                    checked: 'checked',
                    class: 'radioButtonSpace'
                });
                p.append('span').text('kbps');
                p.append('input').attr({
                    id: 'vn-id-update-bw-mbps-val',
                    type: 'radio',
                    name: 'vn-id-update-bw-val-name',
                    class: 'radioButtonSpace'
                });
                p.append('span').text('kbps');
                p.append('br');
            }

            if (constType == 'CT') {
                addAttribute('vn-id-update-cost-name', 'vn-id-update-cost', 'Cost Type', 'checkbox');
                p.append('input').attr({
                    id: 'vn-id-update-cost-igp-val',
                    type: 'radio',
                    name: 'vn-id-update-cost-val',
                    class: 'radioButtonSpace'
                });
                p.append('span').text('IGP');
                p.append('br');
                p.append('input').attr({
                    id: 'vn-id-update-cost-te-val',
                    type: 'radio',
                    name: 'vn-id-update-cost-val',
                    checked: 'checked',
                    class: 'radioButtonSpace'
                });
                p.append('span').text('TE');
                p.append('br');
            }

            if (constType == 'SRC') {
                addAttribute('vn-id-update-src-name'+idx, 'vn-id-update-src-'+idx, val, 'checkbox');
            }

            if (constType == 'DST') {
                addAttribute('vn-id-update-dst-name'+idx, 'vn-id-update-dst-'+idx, val, 'checkbox');
            }

        } );

        return content;
    }

    function showVnQueryInformation(data) {

        wss.unbindHandlers(handlerMap);
        tunnelNameDataQuery = data;
        tds.openDialog()
            .setTitle('Available VNs for query')
            .addContent(createUserTextQuery(data))
            .addOk(dOkQuery, 'OK')
            .addCancel(dClose, 'Close')
            .bindKeys();
    }

    function showVnQueryInfoRemove(data) {

        wss.unbindHandlers(handlerMapRem);
        tunnelNameDataQueryRem = data;
        tds.openDialog()
            .setTitle('Available VNs for remove')
            .addContent(createUserTextQueryRem(data))
            .addOk(dOkRem, 'OK')
            .addCancel(dClose, 'Close')
            .bindKeys();
    }

    function showVnQueryInfoUpdate(data) {

        wss.unbindHandlers(handlerMapUpd);
        tunnelNameDataQueryUpd = data;
        tds.openDialog()
            .setTitle('Available VNs for update')
            .addContent(createUserTextQueryUpd(data))
            .addOk(dOkUpdate, 'OK')
            .addCancel(dClose, 'Close')
            .bindKeys();
    }

    function showVnQueryInfoUpdateConstrts(data) {

        wss.unbindHandlers(handlerMapUpdConstr);
        tunnelNameDataQueryUpdConstr = data;
        tds.openDialog()
            .setTitle('User inputs for VN path update')
            .addContent(createUserTextQueryUpdConstr(data))
            .addOk(dOkUpdateConstr, 'OK')
            .addCancel(dClose, 'Close')
            .bindKeys();
    }

    function createUserText() {
        var content = ds.createDiv('constraints-input'),
            form = content.append('form'),
            p = form.append('p');

        function addAttribute(name, id, nameField, type) {
            if (type == 'radio') {
                p.append('input').attr({
                    type: type,
                    name: name,
                    id: id,
                    class: 'radioButtonSpace'
                });
            } else {
                p.append('input').attr({
                    type: type,
                    name: name,
                    id: id
                });
            }

            p.append('span').text(nameField);
            p.append('br');
        }

        //Add the bandwidth related inputs.
        addAttribute('vn-band-width-name', 'vn-band-width-box', 'Band Width', 'checkbox');
        addAttribute('vn-band-width-value-name', 'vn-band-width-value', null, 'number');
        addAttribute('vn-band-type', 'vn-band-kpbs-val', 'kbps', 'radio');
        addAttribute('vn-band-type', 'vn-band-mpbs-val', 'mbps', 'radio');
        //Add the cost type related inputs.
        addAttribute('vn-cost-type-name', 'vn-cost-type', 'Cost Type', 'checkbox');
        addAttribute('vn-cost-type-valname', 'vn-cost-type-igp', 'IGP', 'radio');
        addAttribute('vn-cost-type-valname', 'vn-cost-type-te', 'TE', 'radio');
        //Add the VN name
        addAttribute('vn-tunnel-name', 'vn-tunnel-name-id', 'VN Name', 'text');

        return content;
    }

    // === Main API functions

    function setSrc(node, type) {
        if (type == 'single') {
            wss.sendEvent(srcMessage, {
                id: node.id
            });

            flash.flash('Source node: ' + node.id);

        } else {
            node.forEach( function (val, idx) {
                wss.sendEvent(srcMessage, {
                    id: val
                });

                flash.flash('Source node: ' + val);
            } );
        }
    }

    function setDst(node, type) {
         if (type == 'single') {
            wss.sendEvent(dstMessage, {
                id: node.id
            });

            flash.flash('Destination node: ' + node.id);

        } else {
            node.forEach( function (val, idx) {
                wss.sendEvent(dstMessage, {
                    id: val
                });

                flash.flash('Destination node: ' + val);
            } );
        }
    }

    function setPath() {

        function dOk() {
            var bandWidth = isChecked('vn-band-width-box'),
                bandValue = null,
                bandType = null;

            if (bandWidth) {
                bandValue = getCheckedValue('vn-band-width-value');

                if (isChecked('vn-band-kpbs-val')) {
                    bandType = 'kbps';
                } else if (isChecked('vn-band-mpbs-val')) {
                    bandType = 'mbps';
                }
            }

            var costType = isChecked('vn-cost-type'),
                costTypeVal = null;

            if (costType) {
                if (isChecked('vn-cost-type-igp')) {
                    costTypeVal = 'igp';
                } else if (isChecked('vn-cost-type-te')) {
                   costTypeVal = 'te';
                }
            }

            wss.sendEvent(vnSetup, {
                bw: bandValue,
                bwtype: bandType,
                ctype: costTypeVal,
                vnName: getCheckedValue('vn-tunnel-name-id')
            });

            flash.flash('create path message');
            $log.debug('Dialog OK button clicked');
        }

        tds.openDialog()
        .setTitle('User inputs for setup path')
        .addContent(createUserText())
        .addOk(dOk, 'OK')
        .addCancel(dClose, 'Close')
        .bindKeys();

        flash.flash('setup path message');
    }

    function remPath() {
        wss.sendEvent(vnQuerymsg, {
            query: 'remove'
        });

        handlerMapRem[showVnInfoMsgRem] = showVnQueryInfoRemove;
        wss.bindHandlers(handlerMapRem);

        flash.flash('remove path message query');
    }

    function query() {
        wss.sendEvent(vnQuerymsg, {
            query: 'show'
        });
        handlerMap[showVnInfoMsg] = showVnQueryInformation;
        wss.bindHandlers(handlerMap);

        flash.flash('VN query message');
    }

    function updatePath() {
        wss.sendEvent(vnQuerymsg, {
            query: 'update'
        });
        handlerMapUpd[showVnInfoMsgUpdate] = showVnQueryInfoUpdate;
        wss.bindHandlers(handlerMapUpd);

        flash.flash('VN update query message');
    }

    function vnClearAll(node) {
        wss.sendEvent(vnClear);

        flash.flash('VN clear message');
    }

    function vnHighlightDev(node) {
        wss.sendEvent(vnDeviceHighlight);

        flash.flash('VN device highlight message');
    }

    // === ---------------------------
    // === Module Factory Definition

    angular.module('ovVnwebTopov', [])
        .factory('VnwebTopovDemoService',
        ['$log', 'FnService', 'FlashService', 'WebSocketService',
         'TopoDialogService', 'DialogService',

        function (_$log_, _fs_, _flash_, _wss_, _tds_, _ds_) {
            $log = _$log_;
            fs = _fs_;
            flash = _flash_;
            wss = _wss_;
            tds = _tds_;
            ds = _ds_;

            return {
                setSrc: setSrc,
                setDst: setDst,
                setPath: setPath,
                remPath: remPath,
                query: query,
                vnClearAll: vnClearAll,
                updatePath: updatePath,
                vnHighlightDev: vnHighlightDev
            };
        }]);
}());
