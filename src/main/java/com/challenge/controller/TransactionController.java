package com.challenge.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.constant.AssignmentConstant;
import com.challenge.dto.Statistics;
import com.challenge.dto.Transaction;
import com.challenge.service.IStatisticsService;

/**
 * All operations related to indexes this controller.
 * 
 */
@RestController
public class TransactionController {

	@Autowired
	IStatisticsService statisticsService;

	private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

	/**
	 * 
	 * Every time new tick arrives, it store it in allStatisticMap, update
	 * overAllStatsMap and latestStatsByInstrument
	 * 
	 * @return
	 */
	@PostMapping("/ticks")
	public void createTransaction(@Valid @RequestBody Transaction transaction, HttpServletResponse response) {

		// Validate Transaction
		if (transaction == null || transaction.getPrice() < 0.0 || transaction.getInstrument() == null
				|| transaction.getInstrument().length() <= 0 || transaction.getTimeStamp() <= 0) {

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		long currentTimeStamp = Instant.now(Clock.systemUTC()).getEpochSecond();
		long transactionTimestamp = Instant.ofEpochMilli(transaction.getTimeStamp()).atOffset(ZoneOffset.UTC)
				.toEpochSecond();
		long diff = currentTimeStamp - transactionTimestamp;

		// Discard ticks older than 60 seconds
		if (diff > AssignmentConstant.TRANSACTION_EXPIRED_DURATION) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}

		transaction.setTimeStamp(transactionTimestamp);

		if (currentTimeStamp >= transactionTimestamp) {
			// Ticks which are in range (60 seconds)
			statisticsService.addTransaction(transaction);
			LOG.debug("Transaction added.");
		} else {
			// All Future ticks will be store in separate map and period job
			// will put it in actual map.
			statisticsService.addFutureTransaction(transaction);
			LOG.debug("Future Transaction added.");
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	/**
	 * returns aggregated statistics for all ticks across all instrument
	 * 
	 * @return
	 */
	@GetMapping("/statistics")
	public Statistics getStatistics() {
		return statisticsService.getStatistics();
	}

	/**
	 * returns aggregated statistics for all ticks across single instrument
	 * 
	 * @return
	 */
	@GetMapping("/statistics/{instrument}")
	public Statistics getStatisticsByInstrument(@Valid @PathVariable String instrument) {
		return statisticsService.getStatisticsByInstrument(instrument);
	}
}
