package com.windforce.common.ramcache.persist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.windforce.common.ramcache.orm.Accessor;
import com.windforce.common.ramcache.persist.Element;
import com.windforce.common.ramcache.persist.TimingConsumerState;
import com.windforce.common.ramcache.persist.TimingPersister;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TimingPerformanceTest implements ApplicationContextAware {

	private TimingPersister persister;

	private int total = 1000000;

	@Test
	public void test() throws InterruptedException {
		Accessor accessor = applicationContext.getBean(Accessor.class);
		persister = new TimingPersister();
		persister.initialize("test", accessor, "1");

		for (int i = 0; i < total; i++) {
			persister.put(Element.saveOf(new Person(i, "name:" + i)));
		}
		long start = System.currentTimeMillis();
		persister.flush();
		while (true) {
			if (persister.getConsumer().getState() == TimingConsumerState.RUNNING) {
				Thread.yield();
			} else {
				break;
			}
		}
		System.out.println("插入完成时间:" + (System.currentTimeMillis() - start));

		for (int i = 0; i < total; i++) {
			persister.put(Element.updateOf(new Person(i, "new:name:" + i)));
		}
		start = System.currentTimeMillis();
		persister.flush();
		while (true) {
			if (persister.getConsumer().getState() == TimingConsumerState.RUNNING) {
				Thread.yield();
			} else {
				break;
			}
		}

		System.out.println("更新完成时间:" + (System.currentTimeMillis() - start));

		for (int i = 0; i < total; i++) {
			persister.put(Element.removeOf(i, Person.class));
		}
		start = System.currentTimeMillis();
		persister.flush();
		while (true) {
			if (persister.getConsumer().getState() == TimingConsumerState.RUNNING) {
				Thread.yield();
			} else {
				break;
			}
		}
		TimingConsumer.shutdownExecutor();
		System.out.println("删除完成时间:" + (System.currentTimeMillis() - start));
	}

	private ConfigurableApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}
}
