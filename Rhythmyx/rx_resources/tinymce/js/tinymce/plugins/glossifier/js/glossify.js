/**
 * glossify.js
 * Custom utility functions used by the NCI Glossifier plugin.js
 * 
 * plugin.js draws a custom modal over the Content Explorer content editor window. This is the
 * Javascript that is linked into the modal HTML, rather than mucking up the drawn HTML with 
 * a bunch of string literals.
 * 
 * Last modified 5/1/2018
 */
 
/**
 * Draw progress bar on loading screen
 */
var prg_width = 200;
function progress() {
	var node = document.getElementById("progress");
	var w = node.style.width.match(/\\d+/);
	if (w == prg_width) {
		w = 0;
	}
	node.style.width = parseInt(w) + 5 + "px";
}
setInterval(progress, 250);


// jQuery functions
$(function() {

	/**
	 * Build and array of 'value' IDs and push them into our hidden data element as a data attribute
	 */	
	function returnChecks() {
		var myCheckArr = [];
		var checkedItem = $('#Glossify').contents().find('input[name="terms"]' + ':checked');
		checkedItem.each(function() {
			$this = $(this);
			myCheckArr.push($this.attr("value"));
		});
		$("#massaged-data").attr("data-checked-array", myCheckArr);					
	}

	/**
	 * Update the data attribute each time a checkbox is checked/unchecked
	 */
	$('input[name="terms"]').change(function() {
		returnChecks()
	});

});