package com.vardaan.example.metrics;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import com.vardaan.example.metrics.AttendanceRatioGauge;

public class MyMeter {

	public static void main(String args[]) {
		MetricRegistry metricRegistry = new MetricRegistry();

		Meter meter1 = new Meter();
		metricRegistry.register("meter1", meter1);

		Meter meter2 = metricRegistry.meter("meter2");

		String name1 = MetricRegistry.name(java.util.logging.Filter.class, "request", "count");
		String name2 = MetricRegistry.name("CustomFilter", "response", "count");

		System.out.println("meter1  " + meter1.getCount());
		System.out.println("meter2  " + meter2.toString());

		System.out.println("name1  " + name1);
		System.out.println("name2  " + name2);

		SharedMetricRegistries.add("default", metricRegistry);
		MetricRegistry retrievedMetricRegistry = SharedMetricRegistries.getOrCreate("default");
		retrievedMetricRegistry.getMeters().forEach((k, v) -> System.out.println("" + k + v));
		SharedMetricRegistries.remove("default");

		///////////////////////////////////////////

		Meter meter = new Meter();
		long initCount = meter.getCount();
		System.out.println(initCount);

		meter.mark();
		System.out.println(meter.getCount());

		meter.mark(20);
		System.out.println(meter.getCount());

		Random r = new Random(10);
		meter.mark(r.nextInt());
		double meanRate = meter.getMeanRate();
		double oneMinRate = meter.getOneMinuteRate();
		double fiveMinRate = meter.getFiveMinuteRate();
		double fifteenMinRate = meter.getFifteenMinuteRate();

		System.out.println("\n" + "MeterCount-" + meter.getCount() + " " + meanRate + " " + oneMinRate + " "
				+ fiveMinRate + " " + fifteenMinRate);

		//////////////////////////////////////////////////////////
		
		RatioGauge ratioGauge = new AttendanceRatioGauge(15, 20);
		 
		System.out.println("xyz " + ratioGauge.getValue());
		
		
		///////////////////////////////////////////////
		Histogram histogram = metricRegistry.histogram(MetricRegistry.name(MyMeter.class, "rate-Historgram"));
		
		histogram.update(5);
		long count1 = histogram.getCount();
		assertThat(count1, equalTo(1L));
		 
		Snapshot snapshot1 = histogram.getSnapshot();
		assertThat(snapshot1.getValues().length, equalTo(1));
		assertThat(snapshot1.getValues()[0], equalTo(5L));
		 
		histogram.update(20);
		histogram.update(15);
		
	//	histogram.update();
		long count2 = histogram.getCount();
		assertThat(count2, equalTo(2L));
		 
		Snapshot snapshot2 = histogram.getSnapshot();
		assertThat(snapshot2.getValues().length, equalTo(2));
		assertThat(snapshot2.getValues()[1], equalTo(20L));
		assertThat(snapshot2.getMax(), equalTo(20L));
		assertThat(snapshot2.getMean(), equalTo(12.5));
	//	assertEquals(10.6, snapshot2.getStdDev(), 0.1);
		assertThat(snapshot2.get75thPercentile(), equalTo(20.0));
		assertThat(snapshot2.get999thPercentile(), equalTo(20.0));
		
		////Console Reporter
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).build();
		reporter.start(5, TimeUnit.SECONDS);
		reporter.report();
		
		
		
		
		
		
		
		
		///CSV Reporter
		CsvReporter.Builder csvBuilder =  CsvReporter.forRegistry(metricRegistry);
		File file = new File("file");
		CsvReporter csvReporter = csvBuilder.build(file);
		reporter.start(10, TimeUnit.MILLISECONDS);
		csvReporter.report();
		
		
		// ------------------ Slidingwindow reservoir example-----------------
		//https://www.javatips.net/api/AIR-master/airpal-master/src/main/java/com/airbnb/airpal/core/store/usage/LocalUsageStore.java#
		
	}
}
