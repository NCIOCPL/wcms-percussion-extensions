package gov.cancer.wcm.images;

import java.util.*;

public class ImageValidatorTestHarness {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello world!");
		
		// Get CT Validator
		ImageCTValidator ctValidator = getImageCTValidator();
		
		// Get data - getTestData()
		HashMap<String, String> fieldData = getTestData();
		
		// CT Validator - validate item
		ArrayList<ImageValidationError> validationErrors = ctValidator.validateItems(fieldData);
		for(ImageValidationError item : validationErrors) {
			System.out.println(item.getErrorMessage());
		}
	}

	private static HashMap<String, String> getTestData() {
		HashMap<String, String> testData = new HashMap<String, String>();
		
		testData.put("img1_width", "650");
		testData.put("img1_height", "650");
		testData.put("img2_width", "230");
		testData.put("img2_height", "173");
		testData.put("img4_width", "400");
		testData.put("img4_height", "300");
		
		return testData;
	}
	
	private static ImageCTValidator getImageCTValidator() {
		ImageCTValidator testCTValidator = new ImageCTValidator("gloImage", getImageFieldValidators());
		
		return testCTValidator;
	}
	
	private static ArrayList<ImageFieldValidator> getImageFieldValidators() {
		ImageFieldValidator img1Validator = new ImageFieldValidator("img1", "Article Image", "", getImg1Constraints());
		ImageFieldValidator img2Validator = new ImageFieldValidator("img2", "Thumbnail Image", "", getImg2Constraints());
		ImageFieldValidator img4Validator = new ImageFieldValidator("img4", "Feature Card Image", "", getImg4Constraints());
		
		ArrayList<ImageFieldValidator> testImageFieldValidators = new ArrayList<ImageFieldValidator>();
		testImageFieldValidators.add(img1Validator);
		testImageFieldValidators.add(img2Validator);
		testImageFieldValidators.add(img4Validator);
		
		return testImageFieldValidators;
	}
	
	private static ArrayList<Constraint> getImg1Constraints() {
		BetweenConstraint btwnConstraint = new BetweenConstraint("width", "600", "900");
		
		ArrayList<Constraint> testConstraints = new ArrayList<Constraint>();
		testConstraints.add(btwnConstraint);
		testConstraints.add(btwnConstraint);
		
		return testConstraints;
	}
	
	private static ArrayList<Constraint> getImg2Constraints() {
		ExactConstraint eqlConstraint = new ExactConstraint("width", "230");
		ExactConstraint eqlConstraint1 = new ExactConstraint("height", "173");
		
		ArrayList<Constraint> testConstraints = new ArrayList<Constraint>();
		testConstraints.add(eqlConstraint);
		testConstraints.add(eqlConstraint1);
		
		return testConstraints;
	}
	
	private static ArrayList<Constraint> getImg4Constraints() {
		ExactConstraint eqlConstraint = new ExactConstraint("width", "425");
		ExactConstraint eqlConstraint1 = new ExactConstraint("height", "319");
		
		ArrayList<Constraint> testConstraints = new ArrayList<Constraint>();
		testConstraints.add(eqlConstraint);
		testConstraints.add(eqlConstraint1);
		
		return testConstraints;
	}
}
