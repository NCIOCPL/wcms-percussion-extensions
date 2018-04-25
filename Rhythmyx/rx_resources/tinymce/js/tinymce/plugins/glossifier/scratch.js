/**
 * plugin.js
 *
 * Released under LGPL License.
 * Copyright (c) 1999-2015 Ephox Corp. All rights reserved
 *
 * License: http://www.tinymce.com/license
 * Contributing: http://www.tinymce.com/contributing
 */

/*global tinymce:true */

tinymce.PluginManager.add('glossifier', function(editor) {
	var settings = editor.settings;

	editor.addCommand('openGlossifier', function() {
		editor.windowManager.open({
			title: 'glossifier',
			width: parseInt(editor.getParam("plugin_preview_width", "650"), 10),
			height: parseInt(editor.getParam("plugin_preview_height", "500"), 10),
			html: '<iframe src="javascript:\'\'" frameborder="0"></iframe>',
			buttons: {
				text: 'Close',
				onclick: function() {
					this.parent().parent().close();
				}
			},
			onPostRender: function() {
				var previewHtml, loadingHtml, headHtml = '';

				headHtml += '<base href="' + editor.documentBaseURI.getURI() + '">';

				tinymce.each(editor.contentCSS, function(url) {
					headHtml += '<link type="text/css" rel="stylesheet" href="' + editor.documentBaseURI.toAbsolute(url) + '">';
				});

				var bodyId = settings.body_id || 'tinymce';
				if (bodyId.indexOf('=') != -1) {
					bodyId = editor.getParam('body_id', '', 'hash');
					bodyId = bodyId[editor.id] || bodyId;
				}

				var bodyClass = settings.body_class || '';
				if (bodyClass.indexOf('=') != -1) {
					bodyClass = editor.getParam('body_class', '', 'hash');
					bodyClass = bodyClass[editor.id] || '';
				}

				var preventClicksOnLinksScript = (
					'<script>' +
						'document.addEventListener && document.addEventListener("click", function(e) {' +
							'for (var elm = e.target; elm; elm = elm.parentNode) {' +
								'if (elm.nodeName === "A") {' +
									'e.preventDefault();' +
								'}' +
							'}' +
						'}, false);' +
					'</script> '
				);

				var dirAttr = editor.settings.directionality ? ' dir="' + editor.settings.directionality + '"' : '';

				previewHtml = (
					'<!DOCTYPE html>' +
					'<html>' +
					'<head>' +
						headHtml +
					'</head>' +
					'<body id="' + bodyId + '" class="mce-content-body ' + bodyClass + '"' + dirAttr + '>' +
						editor.getContent() +
						preventClicksOnLinksScript +
					'</body>' +
					'</html>'
				);


				// Custom HTML 1
				loadingHtml = (
					'<!DOCTYPE html>' +
					'<html>' +
					  '<head>' +
						'<title>GlossifyDocumentPrep</title>' +
						'<style type="text/css">H2 {COLOR: #333366; FONT-FAMILY: Trebuchet MS, Tahoma, Verdana, Arial, sans-serif; FONT-SIZE: 12px; FONT-WEIGHT: bold; LINE-HEIGHT: 14px}</style>' +
						'<script language="javascript" type="text/javascript">' +
						  'var prg_width = 200;' +
						  'function progress() {' +
							'var node = document.getElementById("progress");' +
							'var w = node.style.width.match(/\\d+/);' +
							'if (w == prg_width) {' +
								'w = 0;' +
							'}' +
							'node.style.width = parseInt(w) + 5 + "px";' +
						  '}' +
						  'setInterval(progress, 250);' +
						'</script>' +
					  '</head>' +
					  '<body>' +
						'<div>' +
						'<div style="border: 1px solid black; width:200px; height:10px;">' +
						  '<div id="progress" style="height:10px; width:0px; background-color:red;"/></div>' +
						'</div>' +
						'<h2>Processing document, please wait.........</h2>' +
					  '</body>' +
					'</html>'
				);
				
				/** draw html into body **/
				//this.getEl('body').firstChild.src = 'data:text/html;charset=utf-8,' + encodeURIComponent(previewHtml);
				this.getEl('body').firstChild.src = 'data:text/html;charset=utf-8,' + encodeURIComponent(loadingHtml);

			}
		});
	});

	editor.addButton('glossifier', {
		icon: 'glossifier" style="background-image:url(\'../rx_resources/tinymce/images/glossify.gif\');"',
		title : 'Glossify',
		cmd : 'openGlossifier'
	});

	editor.addMenuItem('glossifier', {
		icon: 'glossifier" style="background-image:url(\'../rx_resources/tinymce/images/glossify.gif\');"',
		text : 'Glossify',
		cmd : 'openGlossifier',
		context: 'tools'
	});
});
