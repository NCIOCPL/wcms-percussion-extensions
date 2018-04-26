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
	function showDialog() {
		var win = editor.windowManager.open({
			title: 'Glossifier tool',
			width: parseInt(editor.getParam("plugin_preview_width", "650"), 10),
			height: parseInt(editor.getParam("plugin_preview_height", "500"), 10),
			html: '<iframe src="javascript:\'\'" frameborder="0"></iframe>',
			buttons: [{
					text: 'Close',
					onclick: function() {
						this.parent().parent().close();
			}},
					{text: 'Close2', onclick: 'close'}
			],
			onSubmit: function(e) {
				// We get a lovely "Wrong document" error in IE 11 if we
				// don't move the focus to the editor before creating an undo
				// transation since it tries to make a bookmark for the current selection
				editor.focus();

				editor.undoManager.transact(function() {
					editor.setContent(e.data.code);
				});

				editor.selection.setCursorLocation();
				editor.nodeChanged();
			}
		});

		// Gecko has a major performance issue with textarea
		// contents so we need to set it when all reflows are done
		win.find('#code').value(editor.getContent({source_view: false}));
	}

	editor.addCommand("openGlossifier", showDialog);

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