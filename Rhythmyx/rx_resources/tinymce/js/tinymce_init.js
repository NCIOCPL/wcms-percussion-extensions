function perc_tinymce_init(options) 
{
	var css_path = "../sys_resources/css/tinymce/tinymce.css";
	var mergedBaseOptions = $.extend({}, {	
	"mode" : "textareas",
	"content_css" : css_path,
	"theme" : "modern",
	"editor_selector" : "tinymce_callout",
	"valid_elements" : "*[*]",
	"noneditable_leave_contenteditable": true,
	"image_advtab" : true,

     	plugins: [
        "advlist autolink lists link charmap print preview hr anchor pagebreak",
        "searchreplace wordcount visualblocks visualchars code fullscreen",
        "insertdatetime media nonbreaking save table contextmenu directionality",
        "emoticons template paste textcolor",
	"noneditable rxinline"
   	],

	"skin" : "lightgray",
	"skin_variant" : "black",
	"height" : options.height,
	"width" :  options.width,
	// Theme options
	"theme_advanced_toolbar_location" : "top",
	"theme_advanced_toolbar_align" : "left",
	"theme_advanced_statusbar_location" : "bottom",
	"theme_advanced_resizing" : true,
	"browser_spellcheck" : true,
	"gecko_spellcheck" : true,
	"toolbar_items_size" : "normal",

	"toolbar": "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify |  bullist numlist outdent indent | link | print preview media | forecolor backcolor emoticons | rxinlinelink rxinlinetemplate rxinlineimage glossifier"

        }, options );

	$.ajax({
		url: options.perc_config,
		dataType : 'json',
		success :  function (data) {
		var config = {};
		var mergedOptions = mergedBaseOptions;
		if($.isArray(data))
		{
			$.each(data, function(i, item) {
				if (!item.hasOwnProperty('roles') || $(options.userRoles).not(item.roles).length < $(options.userRoles).length)
				{
					if(item.hasOwnProperty('config_css') && item.config_css != '')
					{
						css_path = css_path + "," + item.config_css;
					}
					
					mergedOptions = $.extend({}, mergedBaseOptions, item);
				}
			});
		}
		else
		{
			mergedOptions = $.extend({}, mergedBaseOptions, data);
		}
		if (mergedOptions.hasOwnProperty('langmap'))
		{
			var langmap = mergedOptions.langmap;
			var perc_locale = mergedOptions.perc_locale;
			
			if(langmap.hasOwnProperty(perc_locale)) {
				var language = langmap[perc_locale];
				mergedOptions.language = language;
			}
		}
		if(mergedOptions.hasOwnProperty('control_css') && mergedOptions.control_css != '')
		{
			css_path = css_path + "," + mergedOptions.control_css;
			mergedOptions.content_css = css_path;
		}

		tinyMCE.init(mergedOptions);
	},
	error: function(data) {
		if (data.status=="200"){
			alert("Error in TinyMCE config "+options.perc_config);
		} else if (data.status="404") {
			alert("Cannot find TinyMCE config file "+options.perc_config);
		} else {
			alert("Error loading TinyMCE config file "+JSON.stringify(data));
		}
	}
	});
}

