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
		boolean validated = ctValidator.validateItems(fieldData);
		System.out.println("Test items validated: " + validated);
	}

	private static HashMap<String, String> getTestData() {
		HashMap<String, String> testData = new HashMap<String, String>();
		
		testData.put("img1_width", "650");
		testData.put("img1_height", "650");
		testData.put("img2_width", "425");
		testData.put("img2_height", "319");
		
		return testData;
	}
	
	private static ImageCTValidator getImageCTValidator() {
		ImageCTValidator testCTValidator = new ImageCTValidator("gloImage", getImageFieldValidators());
		
		return testCTValidator;
	}
	
	private static ArrayList<ImageFieldValidator> getImageFieldValidators() {
		ImageFieldValidator img1Validator = new ImageFieldValidator("img1", getImg1Constraints());
		ImageFieldValidator img2Validator = new ImageFieldValidator("img2", getImg2Constraints());
		
		ArrayList<ImageFieldValidator> testImageFieldValidators = new ArrayList<ImageFieldValidator>();
		testImageFieldValidators.add(img1Validator);
		testImageFieldValidators.add(img2Validator);
		
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
		EqualConstraint eqlConstraint = new EqualConstraint("width", "425");
		EqualConstraint eqlConstraint1 = new EqualConstraint("height", "319");
		
		ArrayList<Constraint> testConstraints = new ArrayList<Constraint>();
		testConstraints.add(eqlConstraint);
		testConstraints.add(eqlConstraint1);
		
		return testConstraints;
	}
}
