package com.challenge.service;

import com.challenge.dto.Statistics;
import com.challenge.dto.Transaction;

/**
 * 
 * This service interface is responsible for statistics calculation and
 * retrieval.
 * 
 * @author
 *
 */
public interface IStatisticsService {

	/**
	 * 
	 * Every time new tick arrives, it store it in allStatisticMap, update
	 * overAllStatsMap and latestStatsByInstrument
	 * 
	 */
	public void addTransaction(Transaction transaction);

	/**
	 * Store All future transactions in map, periodic job will take care to add
	 * the transaction in the actual map. And use this map for recalculation.
	 * 
	 */
	public void addFutureTransaction(Transaction transaction);

	/**
	 * returns aggregated statistics for all ticks across all instrument
	 * 
	 * @return
	 */
	public Statistics getStatistics();

	/**
	 * returns aggregated statistics for all ticks across single instrument
	 * 
	 * @return
	 */
	public Statistics getStatisticsByInstrument(String instrumentName);

}
