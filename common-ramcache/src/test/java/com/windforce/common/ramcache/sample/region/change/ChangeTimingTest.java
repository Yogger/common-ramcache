package com.windforce.common.ramcache.sample.region.change;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.windforce.common.ramcache.anno.Inject;
import com.windforce.common.ramcache.orm.Accessor;
import com.windforce.common.ramcache.persist.TimingConsumer;
import com.windforce.common.ramcache.persist.TimingConsumerState;
import com.windforce.common.ramcache.persist.TimingPersister;
import com.windforce.common.ramcache.service.IndexValue;
import com.windforce.common.ramcache.service.RegionCacheService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChangeTimingTest {

	@Inject
	private RegionCacheService<Integer, BasicItem> itemService;
	
	@Autowired
	private Accessor accessor;
	
	private IndexValue idx = IndexValue.valueOf("owner", 1);
	private Integer id = 1;
	
	@Before
	public void before() {
		itemService.create(BasicItem.valueOf(id, idx.getValue(int.class), 10));
	}
	
	@Test
	public void test_change() throws InterruptedException {
		System.out.println("[test_change]");
		BasicItem item = itemService.get(idx, id);
		item.setOwner(0);
		
		Collection<BasicItem> items = itemService.load(idx);
		assertThat(items.size(), is(0));
		
		items = itemService.load(IndexValue.valueOf("owner", 0));
		assertThat(items.size(), is(1));
		assertThat(items.contains(item), is(true));
		
		wait4queueEmpty();
		System.out.println("[test_change]:数据库加载");
		BasicItem entity = accessor.load(BasicItem.class, id);
		assertThat(entity, notNullValue());
		assertThat(entity.getOwner(), is(0));
	}
	
	/** 等待更新队列清空 */
	private void wait4queueEmpty() throws InterruptedException {
		TimingPersister persister = (TimingPersister) itemService.getPersister();
		persister.flush();
		TimingConsumer consumer = persister.getConsumer();
		while (consumer.getState() == TimingConsumerState.RUNNING) {
			Thread.yield();
		}
	}
}
