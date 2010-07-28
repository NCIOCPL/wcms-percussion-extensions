package test.gov.cancer.wcm.extensions;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Method;

import gov.cancer.wcm.extensions.CGov_TitlePopulate;

public class TitlePopulateTest {

	@Test
	public void testModifyTitle() throws Exception {
		Method p = CGov_TitlePopulate.class.getDeclaredMethod("modifyTitle",
				new Class[]{String.class});
		p.setAccessible(true);
		String title = "Test Title";
		String sysTitle =(String)p.invoke(null, new Object[]{title});
		assertEquals(sysTitle.length(), title.length() + 7);
	}
}
