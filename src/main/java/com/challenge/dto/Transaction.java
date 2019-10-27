package com.challenge.dto;

public class Transaction {

	private String instrument;
	private double price;
	private long timeStamp;

	public Transaction(String instrument, double price, long timeStamp) {
		super();
		this.instrument = instrument;
		this.price = price;
		this.timeStamp = timeStamp;
	}

	public Transaction() {
		super();
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "Transaction [instrumnet=" + instrument + ", price=" + price + ", timeStamp=" + timeStamp + "]";
	}

}
