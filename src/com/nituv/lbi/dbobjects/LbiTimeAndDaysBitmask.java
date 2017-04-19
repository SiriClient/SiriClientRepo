package com.nituv.lbi.dbobjects;

import java.sql.Time;

public class LbiTimeAndDaysBitmask {
	private Time time;
	private int daysBitmask;
	
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	public int getDaysBitmask() {
		return daysBitmask;
	}
	public void setDays_Bitmask(int daysBitmask) {
		this.daysBitmask = daysBitmask;
	}
}
