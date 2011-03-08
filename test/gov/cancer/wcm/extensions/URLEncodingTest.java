package test.gov.cancer.wcm.extensions;

import java.lang.reflect.Method;
import gov.cancer.wcm.extensions.CGV_URLEncoding;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class URLEncodingTest {

	@Test
	public void testValidatePrettyUrl() throws Exception {
		Method p = CGV_URLEncoding.class.getDeclaredMethod("validatePrettyUrl",
				new Class[]{String.class});
		p.setAccessible(true);
		String testUrl1 = "MyUrl";
		String testUrl2 = "09876";
		String testUrl3 = "My_098-good.url";
		String testUrl4 = "my.bad*url";
		String testUrl5 = "";
		boolean b = (Boolean)p.invoke(null, testUrl1);
		assertTrue(b);
		b = (Boolean)p.invoke(null, testUrl2);
		assertTrue(b);
		b = (Boolean)p.invoke(null, testUrl3);
		assertTrue(b);
		b = (Boolean)p.invoke(null, testUrl4);
		assertTrue(!b);
		b = (Boolean)p.invoke(null, testUrl5);
		assertTrue(b);
	}
}
