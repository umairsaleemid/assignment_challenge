package com.challenge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.challenge.dto.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssignmentChallengeApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	public String TRANS_PATH = "/ticks";
	public String STATS_PATH = "/statistics";

	@Test
	public void testBadTransaction() throws Exception {

		Transaction transaction = new Transaction("", 6, System.currentTimeMillis());

		// add ticks
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_BAD_REQUEST));

	}

	@Test
	public void testOldTransaction() throws Exception {

		Transaction transaction = new Transaction("IBM", 100, System.currentTimeMillis() - 70000);

		// add ticks
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_NO_CONTENT));

	}

	@Test
	public void testAddTransaction() throws Exception {

		Transaction transaction = new Transaction("Google", 8, System.currentTimeMillis());

		// add ticks - 1
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1)).andExpect(jsonPath("$.sum").value(8L))
				.andExpect(jsonPath("$.avg").value(8.0)).andExpect(jsonPath("$.min").value(8.0))
				.andExpect(jsonPath("$.max").value(8.0));

		TimeUnit.SECONDS.sleep(10);

		transaction = new Transaction("Google", 6, System.currentTimeMillis());

		// add ticks - 2
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2)).andExpect(jsonPath("$.sum").value(14L))
				.andExpect(jsonPath("$.min").value(6.0)).andExpect(jsonPath("$.max").value(8.0));

		TimeUnit.SECONDS.sleep(10);

		transaction = new Transaction("Google", 4, System.currentTimeMillis());

		// add ticks - 3
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(3)).andExpect(jsonPath("$.sum").value(18L))
				.andExpect(jsonPath("$.min").value(4.0)).andExpect(jsonPath("$.max").value(8.0));

		TimeUnit.SECONDS.sleep(20);

		transaction = new Transaction("IBM", 3, System.currentTimeMillis());

		// add ticks - 4
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(4)).andExpect(jsonPath("$.sum").value(21L));

		TimeUnit.SECONDS.sleep(21);

		transaction = new Transaction("IBM", 7, System.currentTimeMillis());

		// add ticks - 5
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(4)).andExpect(jsonPath("$.sum").value(20L))
				.andExpect(jsonPath("$.min").value(3.0)).andExpect(jsonPath("$.max").value(7.0));

		TimeUnit.SECONDS.sleep(61);

		mvc.perform(get(STATS_PATH).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0)).andExpect(jsonPath("$.sum").value(0L));

	}

	@Test
	public void testAddTransactionByInstrument() throws Exception {

		Transaction transaction = new Transaction("Google", 8, System.currentTimeMillis());

		// add ticks - 1
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH + "/Google").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1)).andExpect(jsonPath("$.sum").value(8L))
				.andExpect(jsonPath("$.avg").value(8.0)).andExpect(jsonPath("$.min").value(8.0))
				.andExpect(jsonPath("$.max").value(8.0));

		TimeUnit.SECONDS.sleep(5);

		transaction = new Transaction("Microsoft", 6, System.currentTimeMillis());

		// add ticks - 2
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		// validate new statistics
		mvc.perform(get(STATS_PATH + "/Microsoft").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1)).andExpect(jsonPath("$.sum").value(6L));

		TimeUnit.SECONDS.sleep(61);

		// validate new statistics
		mvc.perform(get(STATS_PATH + "/Microsoft").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0)).andExpect(jsonPath("$.sum").value(0L));

	}

	@Test
	public void testAddFutureTransactionByInstrument() throws Exception {

		int futureSecond = 10000;
		Transaction transaction = new Transaction("Google", 12, System.currentTimeMillis() + futureSecond);

		// add ticks - 1
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		TimeUnit.SECONDS.sleep(5);

		transaction = new Transaction("Google", 10, System.currentTimeMillis() + futureSecond);

		// add ticks - 2
		mvc.perform(post(TRANS_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(transaction)))
				.andExpect(status().is(HttpServletResponse.SC_CREATED));

		TimeUnit.SECONDS.sleep(15);

		// validate new statistics
		mvc.perform(get(STATS_PATH + "/Google").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2)).andExpect(jsonPath("$.sum").value(22L))
				.andExpect(jsonPath("$.avg").value(11.0)).andExpect(jsonPath("$.min").value(10.0))
				.andExpect(jsonPath("$.max").value(12.0));

		TimeUnit.SECONDS.sleep(61);
	}
}
