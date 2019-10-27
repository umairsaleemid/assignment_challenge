package com.challenge.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.challenge.constant.AssignmentConstant;
import com.challenge.dto.Statistics;
import com.challenge.dto.Transaction;
import com.challenge.servicemanager.StatisticsServiceManager;

/**
 * 
 * This service is responsible for statistics calculation and retrieval.
 * 
 * @author
 *
 */
@Service
public class StatisticsService implements IStatisticsService {

	private static final Logger LOG = LoggerFactory.getLogger(StatisticsService.class);
	private static final Map<Long, Map<String, List<Transaction>>> alltransactionsMap = new ConcurrentHashMap<>();
	private static final Map<Long, Map<String, List<Transaction>>> futureTransStatisticMap = new ConcurrentHashMap<>();
	private static Map<String, Statistics> latestStatsByInstrument = new ConcurrentHashMap<>();
	private static Map<String, Statistics> overAllStatsMap = new ConcurrentHashMap<>();

	/**
	 * 
	 * Every time new tick arrives, the method store it in alltransactionsMap,
	 * update overAllStatsMap and latestStatsByInstrument maps.
	 * 
	 * @param transaction
	 */
	@Override
	public void addTransaction(Transaction transaction) {

		StatisticsServiceManager.calculateStatistics(transaction, latestStatsByInstrument, overAllStatsMap);
		StatisticsServiceManager.recordAllTransactions(transaction, alltransactionsMap);
	}

	/**
	 * Store all future transactions in map, periodic job will take care to add
	 * the transaction in the current time stamp map and recalculate it.
	 * 
	 * @param transaction
	 */
	@Override
	public void addFutureTransaction(Transaction transaction) {

		StatisticsServiceManager.recordAllTransactions(transaction, futureTransStatisticMap);

		LOG.debug("New future transaction added: Total size: {} - {}", futureTransStatisticMap.size(),
				futureTransStatisticMap);
		LOG.info("New future transaction added: Total Size: {}", futureTransStatisticMap.size());
	}

	/**
	 * returns aggregated statistics for all ticks across all instrument
	 * 
	 * @return Statistics
	 */
	@Override
	public Statistics getStatistics() {
		LOG.debug("overAllStatsMap: {}", overAllStatsMap);
		LOG.debug("alltransactionsMap: {}", alltransactionsMap);
		return StatisticsServiceManager.retriveOverAllStatistics(overAllStatsMap);
	}

	/**
	 * returns aggregated statistics for all ticks across single instrument
	 * 
	 * @param instrumentName
	 * @return Statistics
	 */
	@Override
	public Statistics getStatisticsByInstrument(String instrumentName) {
		LOG.debug("Instrument stats: {}", latestStatsByInstrument);
		return StatisticsServiceManager.retriveInstrumentStatistics(latestStatsByInstrument, instrumentName);
	}

	/**
	 * The periodic job to remove old ticks and update the statistics maps. 1-
	 * Job is running each second and calculate currentTS - 60seconds and remove
	 * that entry from the map. 2-Recalculate the overall statistics. 3-
	 * Recalculate the statistics by instrument
	 */

	@Scheduled(cron = "* * * * * ?")
	private void removeExpiredTransactions() {

		long currentTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis()).atOffset(ZoneOffset.UTC)
				.toEpochSecond();
		long beforeThresholdseconds = currentTimeStamp - AssignmentConstant.TRANSACTION_EXPIRED_DURATION;

		LOG.info("Job Executed at {} for {}", currentTimeStamp, beforeThresholdseconds);

		extractFutureTransactions(currentTimeStamp);

		Map<String, List<Transaction>> removedTransEntry = alltransactionsMap.remove(beforeThresholdseconds);

		if (removedTransEntry != null) {

			// If the transaction removed at that period/second.

			LOG.debug("Instrument Expired: {}", removedTransEntry.entrySet());
			LOG.info("Instrument Expired:");

			StatisticsServiceManager.recalculateAllStats(removedTransEntry, latestStatsByInstrument, overAllStatsMap,
					alltransactionsMap);

			LOG.info("Statistics recalculated");
		}

	}

	/**
	 * Extract the future transaction on the basis of current time stamp and
	 * push it on the current map.
	 * 
	 */
	private void extractFutureTransactions(long currentTimeStamp) {

		// add current time transaction If exist in futureMap.
		Map<String, List<Transaction>> futureTransactions = futureTransStatisticMap.remove(currentTimeStamp);

		if (futureTransactions != null) {

			LOG.debug("Future Transaction found: {}", futureTransactions);

			futureTransactions.forEach(
					(key, newTransList) -> newTransList.forEach(newTransaction -> addTransaction(newTransaction)));

		}
	}

}
