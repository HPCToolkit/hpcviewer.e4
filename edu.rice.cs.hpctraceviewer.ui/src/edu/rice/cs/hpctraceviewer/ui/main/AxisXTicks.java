// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;

public class AxisXTicks 
{
	private static final int TICK_X_PIXEL = 110;
	private static final int MINIMUM_PIXEL_BETWEEN_TICKS = 10;
	
	/**
	 * text format for labels of x-axis ticks.
	 * We use the US format at the moment. To be customized in the future.
	 */
	private static final DecimalFormat formatTime = new DecimalFormat("###,###,###,###,###.##");

	/**
	 * The width of x-axs canvas
	 */
	private final int width;
	
	/**
	 * The original time unit of the traces in the database
	 */
	private final TimeUnit dbTimeUnit;
	
	/**
	 * A record of tick's label and its index in the ticks:
	 * <ul>
	 *   <li> {@code tickIndex}: the index of the label based on the list of ticks
	 *   <li> {@code tickLabel} the suggested tick's label.
	 * </ul>
	 */
	public record RecordAxisXTickLabel(int tickIndex, double tickLabel) {}
	
	/**
	 * A record of information needed to draw an x-axis:
	 * <ul>
	 *  <li> {@code dataTimeUnit}: the time unit of the tick
	 * 
	 *  <li> {@code displayTimeUnit}: the time unit to display the time, which can differ to dataTimeUnit
	 *  
	 *  <li> {@code conversionFactor}: the conversion factor from data time to display time unit
	 *  
	 *  <li> {@code ticks}: the pixel position of the ticks
	 *  
	 *  <li> {@code tick_labels}: the series of the ticks and its position based on the ticks' indexes
	 * </ul>
	 */
	public record RecordAxisXTicks(TimeUnit dataTimeUnit, TimeUnit displayTimeUnit, double conversionFactor, int []ticks, List<RecordAxisXTickLabel> tick_labels) {}
	

	private record DeltaTimeUnit (long delta, TimeUnit timeUnit) {}
	
	private record UserDisplayTimeUnit(TimeUnit userDisplayTimeUnit, double multiplier, long roundedGap, int suggestedNumAxisLabels) {}

	/***
	 * Create an X-axis configuration of a given screen width.
	 * Typically users call {@link computeTicks()} method to compute the ticks position
	 *  
	 * @param width
	 * 			The width of x-axis canvas
	 * @param dbTimeUnit
	 * 			The time unit of traces in the database
	 */
	public AxisXTicks(int width, TimeUnit dbTimeUnit) {
		this.width = width;
		this.dbTimeUnit = dbTimeUnit;
	}
	
	/**
	 * Compute the position of ticks along x-axis, and its labels

	 * @param attribute
	 * 			The trace display attribute
	 * @param getTextLen
	 * 			A functor to calculate the width (in pixels) of a text string
	 * 
	 * @return {@code RecordAxisXTicks}
	 * 			All information needed to draw ticks and its labels
	 */
	public RecordAxisXTicks computeTicks(TraceDisplayAttribute attribute, Function<String, Integer> getTextLen) {
		
		// --------------------------------------------------------------------------
		// find the right unit time (s, ms, us, ns) 
		// we want to display ticks to something like:
		//  10s .... 20s ... 30s ... 40s
		// --------------------------------------------------------------------------
		
		var deltaTU = computeHintTimeUnit(attribute, dbTimeUnit);
		
		TimeUnit displayTimeUnit = deltaTU.timeUnit;

		// --------------------------------------------------------------------------
		// find rounded delta_time to log 10:
		// if delta_time is 12 --> rounded to 10
		// 					3  --> rounded to 1
		// 					32 --> rounded to 10
		// 					312 --> rounded to 100
		// --------------------------------------------------------------------------

		int logdt 	 = (int) Math.log10(deltaTU.delta);
		long dtRound = (int) Math.pow(10, logdt);
		
		int maxTicks = width/MINIMUM_PIXEL_BETWEEN_TICKS; 
		int numAxisLabel;
		do {
			numAxisLabel = (int) (displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit) / dtRound);
			if (numAxisLabel > maxTicks) {
				dtRound *= 10;
			}
		} while (numAxisLabel > maxTicks);
		
		// --------------------------------------------------------------------------
		// the time unit displayed for users
		// sometime the time unit is millisecond, but user can see it as second like:
		// 2.1s, 2.2s instead of 2100ms and 2200ms
		// --------------------------------------------------------------------------
		
		var infoDisplayTimeUnit = calculateUserDisplayTimeUnit(attribute, displayTimeUnit, dtRound, numAxisLabel);
		
		TimeUnit userDisplayTimeUnit = infoDisplayTimeUnit.userDisplayTimeUnit();
		double multiplier = infoDisplayTimeUnit.multiplier();
		numAxisLabel = infoDisplayTimeUnit.suggestedNumAxisLabels();
		dtRound = infoDisplayTimeUnit.roundedGap();

		// --------------------------------------------------------------------------
		// Calculate the time start: try to find the best rounded time start:
		// - if the gap between ticks is big enough to put the label, the start time doesn't change
		// - otherwise, we have to find the best rounded time start
		// --------------------------------------------------------------------------
		
		var timeStartInDisplayTimeUnit = displayTimeUnit.convert(attribute.getTimeBegin(), dbTimeUnit);
		
		// The original time start according to the database
		var timeStartInDatabaseTimeUnit = attribute.getTimeBegin();
		String userUnitTime = attribute.getTimeUnitName(userDisplayTimeUnit);
		
		// find the length (in possible) of a possible tick's label
		// we add "XX" to make sure it's big enough for all labels :-(
		final String maxText = formatTime.format(timeStartInDisplayTimeUnit * multiplier) + userUnitTime + "XX";
		final var textWidth = getTextLen.apply(maxText);

		double deltaXPixels = (double) width / attribute.getTimeInterval();

		// The gap between tick in the original time unit according to the database
		var dtRoundInDatabaseTimeUnit = dbTimeUnit.convert(dtRound, displayTimeUnit);

		final var deltaTick = dtRoundInDatabaseTimeUnit * deltaXPixels;
		final boolean isFitEnough = deltaTick > textWidth;
		
		timeStartInDatabaseTimeUnit = computeHintTimeStart(attribute, dtRound, displayTimeUnit);
		
		if (!isFitEnough) {
			// In case the ticks' gap is small, find a begin time to the next upper bound
			// e.g. if the time is 22, find the next nice round number like 30
			timeStartInDatabaseTimeUnit = computeNiceRoundedStartLabel(
					attribute, 
					timeStartInDatabaseTimeUnit, 
					dtRound, 
					displayTimeUnit, 
					userDisplayTimeUnit, 
					multiplier, 
					numAxisLabel);
		}

		// --------------------------------------------------------------------------
		// hack: add ticks between 0 to timeStart
		// if timeStart begins at pixel 90, and the gap between ticks is 20, then we need to add ticks:
		// 10, 30, 50, 80
		// --------------------------------------------------------------------------

		var originalTimeStartInDatabaseTimeUnit = attribute.getTimeBegin();
		var distance = timeStartInDatabaseTimeUnit - originalTimeStartInDatabaseTimeUnit;
		
		int numTicksBeforeTimeStart = (int) (distance/ dtRoundInDatabaseTimeUnit);
		var startTick = timeStartInDatabaseTimeUnit - (numTicksBeforeTimeStart * dtRoundInDatabaseTimeUnit);
		
		int tickIndex = 0;
		int []ticks = new int[numAxisLabel + numTicksBeforeTimeStart + 1];

		for (tickIndex=0; tickIndex<numTicksBeforeTimeStart; tickIndex++) {
			var time = startTick + tickIndex * dtRoundInDatabaseTimeUnit;
			int axis_x_pos = (int) convertTimeToPixel(attribute.getTimeBegin(), time, deltaXPixels);
			ticks[tickIndex] = axis_x_pos;
		}
		
		// --------------------------------------------------------------------------
		// if the distance of between ticks is not big enough to display the label, find a multiple of ticks
		// so that it fits the label.
		// to make a nice rounded ticks, we want the multiple of 2 or 5 or 10 or something like that
		// --------------------------------------------------------------------------
		
		int numTicksPerLabel = 1;

		if (!isFitEnough) {
			final int []allowedGapTicksBetweenLabels;
			var time = displayTimeUnit.convert(timeStartInDatabaseTimeUnit, dbTimeUnit);
			
			if (time % (5 * dtRound) == 0) {
				allowedGapTicksBetweenLabels = new int[]{5, 10, 15, 20, 50, 100, 250, 500, 1000};
			} else {
				allowedGapTicksBetweenLabels = new int[]{2, 4, 10, 20, 100, 200, 1000};
			}
			
			numTicksPerLabel = (int) Math.ceil( (double)textWidth / deltaTick );
			for (var gapLabels: allowedGapTicksBetweenLabels) {
				if (gapLabels >= numTicksPerLabel) {
					numTicksPerLabel = gapLabels;
					break;
				}
			}
		}
		
		// --------------------------------------------------------------------------
		// calculate the tick's position and its time label
		// --------------------------------------------------------------------------

		List<RecordAxisXTickLabel> tickLabels = new ArrayList<>(numAxisLabel);
		
		for(int i=0; i <= numAxisLabel; i++) {
			var origDbTime = timeStartInDatabaseTimeUnit + dtRoundInDatabaseTimeUnit * i;
			int axis_x_pos = (int) convertTimeToPixel(attribute.getTimeBegin(), origDbTime, deltaXPixels);
			
			ticks[i + tickIndex] = axis_x_pos;
			
			// draw the tick's label if it's the time
			if (i % numTicksPerLabel == 0) {

				// convert tick time from database unit time to the current unit time
				var time = displayTimeUnit.convert(origDbTime, dbTimeUnit);
				
				// in case we can simplify the unit time, convert the time to the display time
				// multiplier is the constant to convert to the display unit time.
				// for instance if the unit time is seconds and display time is minutes,
				// then the multiplier is 1/60
				double timeToAppear = time * multiplier;
				tickLabels.add(new RecordAxisXTickLabel(i + tickIndex, timeToAppear));
			}
		}
		return new RecordAxisXTicks(displayTimeUnit, userDisplayTimeUnit, multiplier, ticks, tickLabels);
	}
	
	
	/***
	 * Get the coarsest granularity of displayed time unit.
	 * 
	 * @param attribute
	 * 			The configuration of the trace view
	 * @param displayTimeUnit
	 * 			A hint of display time unit result from {@link computeHintTimeUnit} 
	 * @param dtRound
	 * 			The rounded gap between ticks 
	 * @param hintNumAxisLabel
	 * 			A hint of the number of labels
	 * 
	 * @return {@code UserDisplayTimeUnit}
	 */
	private UserDisplayTimeUnit calculateUserDisplayTimeUnit(
			TraceDisplayAttribute attribute, 
			TimeUnit displayTimeUnit, 
			long dtRound, 
			int hintNumAxisLabel) {

		TimeUnit userDisplayTimeUnit = displayTimeUnit;
		double multiplier = 1.0f;
		int numAxisLabel = hintNumAxisLabel;
		
		// Issue #20 : avoid displaying big numbers.
		// if the time is 1200ms we should display it as 1.2s
		// this doesn't change the real unit time. It's just for the display
		if (dtRound >= 100 && attribute.canIncrement(displayTimeUnit)) {
			
			final TimeUnit tu = attribute.increment(displayTimeUnit);
			
			// temporary fix for issue #304
			// need to use Java time unit conversion from to coarser grain time unit.
			// who know we need to convert to minutes or hours 
			var conversion =  userDisplayTimeUnit.convert(1, tu);
			multiplier = (double) 1.0 / conversion;
			
			// if the delta time is not multiple of 1000 (like from micro to mili),
			// we need to adjust the distance between ticks so that the label is 
			// nicely even (not a multiple of .67, ...)
			if (1000 % conversion != 0) {
				dtRound = conversion;
				var suggestedNumLabels = (int) (displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit) / dtRound);
				numAxisLabel = suggestedNumLabels;
			}
			userDisplayTimeUnit = tu;
		}
		return new UserDisplayTimeUnit(userDisplayTimeUnit, multiplier, dtRound, numAxisLabel);
	}
	
	
	/****
	 * Compute a hint of possible display time unit
	 * 
	 * @param attribute
	 * 			The configuration of the trace view
	 * @param dbTimeUnit
	 * 			The original time unit in the database
	 * @return
	 */
	private DeltaTimeUnit computeHintTimeUnit(TraceDisplayAttribute attribute, TimeUnit dbTimeUnit) {
		
		// --------------------------------------------------------------------------
		// finding some HINTs of number of ticks, and distance between ticks 	
		// --------------------------------------------------------------------------
		
		double numTicks  = (double)width / TICK_X_PIXEL;
		double fraction  = attribute.getTimeInterval() / numTicks;

		TimeUnit displayTimeUnit = attribute.computeDisplayTimeUnit(dbTimeUnit);

		// --------------------------------------------------------------------------
		// find the nice rounded number
		// if dt < 10:  1, 2, 3, 4...
		// if dt < 100: 10, 20, 30, 40, ..
		// ...
		// --------------------------------------------------------------------------
		
		long t1 = attribute.getTimeBegin();
		long t2 = (long) (t1 + fraction);
		long dt = displayTimeUnit.convert(t2 - t1, dbTimeUnit);
		
		// there is nothing we can do if the difference between the ticks is less than 1 ns.
		if (dt<1) {
			int ordinal = attribute.getTimeUnitOrdinal(displayTimeUnit);
			if (ordinal>0 && (t1 != t2)) {
				ordinal--;
				displayTimeUnit = attribute.getTimeUnit(ordinal);
				
				// recompute dt with the new time unit
				dt = displayTimeUnit.convert(t2-t1, dbTimeUnit);
			} else {
				dt = 1;
			}
		}
		return new DeltaTimeUnit(dt, displayTimeUnit);
	}
	
	
	private long computeHintTimeStart(TraceDisplayAttribute attribute, long dtRound, TimeUnit displayTimeUnit) {
		
		// The original time start according to the database
		var timeStartInDatabaseTimeUnit = attribute.getTimeBegin();
		
		// The time start according to the display unit time
		var timeStartInDisplayTimeUnit = displayTimeUnit.convert(attribute.getTimeBegin(), dbTimeUnit);

		// try to round up the start time based on multiple of ticks (in unit time)
		long remainder = timeStartInDisplayTimeUnit % dtRound;
		if (remainder > 0) {
			timeStartInDisplayTimeUnit = timeStartInDisplayTimeUnit + (dtRound - remainder);
			timeStartInDatabaseTimeUnit = dbTimeUnit.convert(timeStartInDisplayTimeUnit, displayTimeUnit);
		}
		
		// if the original time begin is 1000900ns, but the conversion is 1000ms,
		// find the time begin as the next round time, which is 1001ms
		var timeStartProjectedToDatabaseTimeUnit = dbTimeUnit.convert(timeStartInDisplayTimeUnit, displayTimeUnit);
		var distance = timeStartInDatabaseTimeUnit - timeStartProjectedToDatabaseTimeUnit;
		
		if (distance > 0) {
			// the new time begin is the next "tick"
			timeStartInDisplayTimeUnit = timeStartInDisplayTimeUnit + dtRound;
			timeStartInDatabaseTimeUnit = dbTimeUnit.convert(timeStartInDisplayTimeUnit, displayTimeUnit);
		}
		return timeStartInDatabaseTimeUnit;
	}
	
	/****
	 * Get a rounded number for the start of the x-axis label depending on the ticks 
	 * and the original start time from {@code TraceDisplayAttribute}
	 * 
	 * @param attribute
	 * 			The configuration of the trace view
	 * @param dtRound
	 * 			The computed gap between ticks in {@code displayTimeUnit}
	 * @param displayTimeUnit
	 * 			The time unit to compute the tick
	 * @param userDisplayTimeUnit
	 * 			The time unit to display the tick. Sometimes the displayed time unit
	 * 			is different than the computed time unit for tick 
	 * @param multiplier
	 * 			The conversion from computed time unit to user display time unit
	 * @param numAxisLabel
	 * 			The number of labels
	 * 
	 * @return {@code long} the start time in the original database time unit 
	 */
	private long computeNiceRoundedStartLabel(
			TraceDisplayAttribute attribute,
			long timeStartInDatabaseTimeUnit,
			long dtRound, 
			TimeUnit displayTimeUnit, 
			TimeUnit userDisplayTimeUnit,
			double multiplier,
			int numAxisLabel) {

		// this is an ugly approach to find a nice round:
		// find the nice start from t0, t1, ... tn/4 as we don't need through all the t 
		// the distance of dt = t1 - t0 should be the multiple of dtRound but we like to complicate it 
		// to enable to convert to higher unit time by multiplying with `multiplier` as:
		//
		//	 dt = t1 - t0 = dtRound x multiplier
		//
		// so dt can be:
		//  10000, 20000, 30000, ...
		//	10, 20, 30 ...
		//  1, 2, 3, ...
		//  0.1, 0.2, 0.3, ...
		var interval = userDisplayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit);
		// The gap between tick in the original time unit according to the database
		var dtRoundInDatabaseTimeUnit = dbTimeUnit.convert(dtRound, displayTimeUnit);

		for (int i=0; i<numAxisLabel/4; i++) {
			var origDbTime = timeStartInDatabaseTimeUnit + dtRoundInDatabaseTimeUnit * i;

			// convert tick time from database unit time to the current unit time
			var time = userDisplayTimeUnit.convert(origDbTime, dbTimeUnit);
			
			// in case we can simplify the unit time, convert the time to the display time
			// multiplier is the constant to convert to the display unit time.
			// for instance if the unit time is seconds and display time is minutes,
			// then the multiplier is 1/60
			if (multiplier < 1) {
				time = displayTimeUnit.convert(origDbTime, dbTimeUnit);
				interval = displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit);
				
				if (interval > 10 * dtRound) {
					var rem = time % (5 * dtRound);
					if (rem > 0) {
						time += (5* dtRound - rem);
						timeStartInDatabaseTimeUnit = dbTimeUnit.convert(time, displayTimeUnit);
					}
					break;
				}
				// in case multiplier < 1 or the time is 3.1, 3.2, .... find the next rounding number 
				if (time % 10 == 0 || time % dtRound == 0) {
					timeStartInDatabaseTimeUnit = origDbTime;
					break;
				}
			} else {
				if (time % (5 * dtRound) == 0) {
					timeStartInDatabaseTimeUnit = origDbTime;
					break;
				}
			}
		}
		return timeStartInDatabaseTimeUnit;
	}
	
	
	/***
	 * Retrieve the suggested label format.
	 * 
	 * @return {@code DecimalFormat}
	 */
	public static DecimalFormat getLabelFormat() {
		return formatTime;
	}
	
	
	/*****
	 * convert from time to pixel
	 * 
	 * @param displayTimeBegin current attribute time configuration
	 * @param time the time to convert
	 * @param deltaXPixels the distance for two different time (in pixel)
	 * 
	 * @return pixel (x-axis)
	 */
	private int convertTimeToPixel(long displayTimeBegin,
								   long time, 
								   double deltaXPixels)
	{
		// define pixel : (time - TimeBegin) x number_of_pixel_per_time 
		//				  (time - TimeBegin) x (numPixelsH/timeInterval)
		long dTime = time-displayTimeBegin;
		return(int) (deltaXPixels * dTime);
	}

}
