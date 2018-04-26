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
					text: 'Submit changes', 
					onclick: function() {
						this.parent().parent().close();
                        editor.setContent('<b>i hate your face</b>');                        						
				}},
				{
					text: 'Close',
					onclick: function() {
                        this.parent().parent().close();
				}}
			]
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