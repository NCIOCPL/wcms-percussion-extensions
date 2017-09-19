package gov.cancer.wcm.images;

import java.util.List;

public class ImageFieldValidator {
	private String imageField;
	private List<Constraint> constraints;
	
	/*
	 * Retrieves the property name.
	 */
	public String getImageField(){
		return imageField;
	}
	
	/*
	 * Retrieves the constraints
	 */
	public List<Constraint> getConstraints(){
		return constraints;
	}
	
	public ImageFieldValidator(String fieldName, List<Constraint> imageConstraints){
		this.imageField = fieldName;
		this.constraints = imageConstraints;
	}
}
