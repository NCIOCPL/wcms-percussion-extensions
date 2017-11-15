package gov.cancer.wcm.images;

public class ImageValidationError {
	private String fieldName;
	private String errorMessage;
	
	public String getFieldName(){
		return fieldName;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	public ImageValidationError(String field, String errorMsg){
		fieldName = field;
		errorMessage = errorMsg;
	}
}
