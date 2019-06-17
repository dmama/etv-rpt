package ch.vd.unireg.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.vd.unireg.scheduler.BatchScheduler;
import ch.vd.unireg.scheduler.BatchSchedulerImpl;

public class BatchRptRunnerAppTest {

	@Test
	public void testRegisterShutdownHook() {
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"unireg-all-module.xml");
		Assert.assertTrue(context.isActive());
		final BatchScheduler batchScheduler = context.getBean("batchScheduler", BatchSchedulerImpl.class);
		Assert.assertNotNull(batchScheduler);
		context.registerShutdownHook();
	}

}
