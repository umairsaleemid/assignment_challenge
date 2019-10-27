package com.challenge.servicemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.challenge.constant.AssignmentConstant;
import com.challenge.dto.Statistics;
import com.challenge.dto.Transaction;

public class StatisticsServiceManager {

	private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceManager.class);

	private StatisticsServiceManager() {
		super();
	}

	/**
	 * Retrieve overall stats. If first time than create new statistic object if
	 * expired. And use this map for recalculation.
	 * 
	 */
	public static Statistics retriveOverAllStatistics(Map<String, Statistics> statsMap) {

		Statistics statistics = null;

		if (statsMap.containsKey(AssignmentConstant.OVER_ALL_STATS)) {
			statistics = statsMap.get(AssignmentConstant.OVER_ALL_STATS);
		} else {
			statistics = new Statistics(0, 0, 0, 0, 0);
		}

		return statistics;
	}

	/**
	 * Retrieve instrument stats. If first time than create new statistic object
	 * if expired. And use this map for recalculation.
	 * 
	 */
	public static Statistics retriveInstrumentStatistics(Map<String, Statistics> latestStatsByInstrument,
			String instrumentName) {

		Statistics statistics = null;

		if (latestStatsByInstrument.containsKey(instrumentName)) {
			statistics = latestStatsByInstrument.get(instrumentName);
		} else {
			statistics = new Statistics(0, 0, 0, 0, 0);
		}

		return statistics;
	}

	/**
	 * Store All transactions in map, periodic job will delete the transaction
	 * if expired. And use this map for recalculation.
	 * 
	 */
	public static void recordAllTransactions(Transaction transaction,
			Map<Long, Map<String, List<Transaction>>> transactionsMap) {
		Map<String, List<Transaction>> statsByTimeStamp = transactionsMap.get(transaction.getTimeStamp());
		if (statsByTimeStamp != null) {
			List<Transaction> transactionByInstrument = statsByTimeStamp.get(transaction.getInstrument());

			// Create new list of transaction
			if (transactionByInstrument == null) {
				transactionByInstrument = new ArrayList<>();
			}
			transactionByInstrument.add(transaction);
			statsByTimeStamp.put(transaction.getInstrument(), transactionByInstrument);
		} else {
			statsByTimeStamp = new HashMap<>();
			List<Transaction> transactionByInstrument = new ArrayList<>();
			transactionByInstrument.add(transaction);
			statsByTimeStamp.put(transaction.getInstrument(), transactionByInstrument);
			transactionsMap.put(transaction.getTimeStamp(), statsByTimeStamp);
		}

		LOG.debug("New Added: Total size: {} - {}", transactionsMap.size(), transactionsMap);
		LOG.info("New transaction added. Total Size: {}", transactionsMap.size());
	}

	/**
	 * Calculate overall statistics and statistics by instrument.
	 * 
	 */
	public static void calculateStatistics(Transaction transaction, Map<String, Statistics> latestStatsByInstrument,
			Map<String, Statistics> overAllStatsMap) {
		Statistics statByInstrument = latestStatsByInstrument.get(transaction.getInstrument());
		if (statByInstrument == null || statByInstrument.getCount() == 0) {
			// New instrument came
			statByInstrument = new Statistics(transaction.getPrice(), transaction.getPrice(), transaction.getPrice(),
					transaction.getPrice(), 1);
			latestStatsByInstrument.put(transaction.getInstrument(), statByInstrument);
		} else {
			// Already stored instrument came
			statByInstrument.setCount(statByInstrument.getCount() + 1);
			statByInstrument.setSum(statByInstrument.getSum() + transaction.getPrice());
			statByInstrument.setMin(Math.min(statByInstrument.getMin(), transaction.getPrice()));
			statByInstrument.setMax(Math.max(statByInstrument.getMax(), transaction.getPrice()));
			statByInstrument.setAvg(statByInstrument.getSum() / statByInstrument.getCount());
		}

		Statistics overAllStats = overAllStatsMap.get(AssignmentConstant.OVER_ALL_STATS);

		if (overAllStats == null || overAllStats.getCount() == 0) {
			// First Time, new instrument came.
			overAllStats = new Statistics(transaction.getPrice(), transaction.getPrice(), transaction.getPrice(),
					transaction.getPrice(), 1);
			overAllStatsMap.put(AssignmentConstant.OVER_ALL_STATS, overAllStats);
		} else {
			overAllStats.setCount(overAllStats.getCount() + 1);
			overAllStats.setSum(overAllStats.getSum() + transaction.getPrice());
			overAllStats.setMin(Math.min(overAllStats.getMin(), transaction.getPrice()));
			overAllStats.setMax(Math.max(overAllStats.getMax(), transaction.getPrice()));
			overAllStats.setAvg(overAllStats.getSum() / overAllStats.getCount());
		}

	}

	/**
	 * Recalculate overall statistics and statistics by instrument, method is
	 * called by periodic job.
	 * 
	 */
	public static void recalculateAllStats(Map<String, List<Transaction>> removedTransEntry,
			Map<String, Statistics> latestStatsByInstrument, Map<String, Statistics> overAllStatsMap,
			Map<Long, Map<String, List<Transaction>>> alltransactionsMap) {

		Statistics overAllStats = overAllStatsMap.get(AssignmentConstant.OVER_ALL_STATS);
		removedTransEntry.forEach((key, oldTransList) -> {
			
			// Recalculate OverAll Statistics
			overAllStatsMap.put(AssignmentConstant.OVER_ALL_STATS,
					recalculateStats(overAllStats, oldTransList, alltransactionsMap, null));

			// Recalculate Instruments Statistics
			latestStatsByInstrument.put(key,
					recalculateStats(latestStatsByInstrument.get(key), oldTransList, alltransactionsMap, key));
		});
	}

	/**
	 * Recalculate statistics on the basis of overall statistics or instrument
	 * key, method is called by periodic job.
	 * 
	 */
	public static Statistics recalculateStats(Statistics statsMap, List<Transaction> oldTransList,
			Map<Long, Map<String, List<Transaction>>> alltransactionsMap, String removedKey) {

		statsMap.setCount(statsMap.getCount() - oldTransList.size());
		statsMap.setSum(statsMap.getSum() - oldTransList.stream().mapToDouble(o -> o.getPrice()).sum());

		if (statsMap.getCount() > 0) {
			statsMap.setAvg(statsMap.getSum() / statsMap.getCount());

			List<Transaction> allTransactionList = new ArrayList<>();

			if (removedKey == null) {
				// recalculate overall stats
				alltransactionsMap.values().forEach(obj -> obj
						.forEach((key, storeTransactionList) -> allTransactionList.addAll(storeTransactionList)));

			} else {

				// recalculate for instruments
				alltransactionsMap.values().forEach(obj -> {
					if (obj.containsKey(removedKey)) {
						allTransactionList.addAll(obj.get(removedKey));
					}
				});

			}

			if (!allTransactionList.isEmpty()) {
				// calculating min and max
				Supplier<DoubleStream> doubleStreamSupplier = () -> allTransactionList.parallelStream()
						.mapToDouble(d -> d.getPrice());

				statsMap.setMax(doubleStreamSupplier.get().max().getAsDouble());
				statsMap.setMin(doubleStreamSupplier.get().min().getAsDouble());
			}

		} else {
			statsMap.setAvg(0.0);
			statsMap.setMax(0.0);
			statsMap.setMin(0.0);
		}

		LOG.debug("Recalulate overAllStats: {}", statsMap);
		return statsMap;
	}

}
