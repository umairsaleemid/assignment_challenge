package com.solactive;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import com.solactive.dto.Statistics;
import com.solactive.dto.Transaction;
import com.solactive.service.StatisticsService;

@RunWith(SpringRunner.class)
public class TransactionServiceTest {

	@Mock
	private ScheduledExecutorService scheduledExecutor;

	@InjectMocks
	private StatisticsService statisticsService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAddStatistics() throws InterruptedException {

		Transaction transaction = new Transaction("Google", 8, Instant.now(Clock.systemUTC()).getEpochSecond());
		statisticsService.addTransaction(transaction);

		Statistics newStat = statisticsService.getStatistics();
		Assert.assertEquals("Count matched", 1, newStat.getCount());
	}

	@Test
	public void testFutureTransaction() throws InterruptedException {

		Transaction transaction = new Transaction("Google", 8, Instant.now(Clock.systemUTC()).getEpochSecond() + 1);
		statisticsService.addFutureTransaction(transaction);

		Statistics newStat = statisticsService.getStatistics();
		Assert.assertEquals("Null", null, newStat);
	}
}
