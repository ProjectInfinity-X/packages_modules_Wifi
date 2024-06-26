/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wifi;

import static com.android.server.wifi.scanner.WifiScanningServiceImpl.getVendorIesBytesFromVendorIesList;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.mockito.Mockito.*;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiSsid;

import com.android.modules.utils.build.SdkLevel;
import com.android.net.module.util.MacAddressUtils;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilities for testing Wifi Scanning
 */
public class ScanTestUtil {

    public static void setupMockChannels(WifiNative wifiNative, int[] channels24, int[] channels5,
            int[] channelsDfs, int[] channels6, int[] channels60) throws Exception {
        when(wifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_24_GHZ))
                .thenReturn(channels24);
        when(wifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_5_GHZ))
                .thenReturn(channels5);
        when(wifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_5_GHZ_DFS_ONLY))
                .thenReturn(channelsDfs);
        when(wifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_6_GHZ))
                .thenReturn(channels6);
        when(wifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_60_GHZ))
                .thenReturn(channels60);
    }

    public static WifiScanner.ScanSettings createRequest(WifiScanner.ChannelSpec[] channels,
            int period, int batch, int bssidsPerScan, int reportEvents) {
        WifiScanner.ScanSettings request = new WifiScanner.ScanSettings();
        request.band = WifiScanner.WIFI_BAND_UNSPECIFIED;
        request.channels = channels;
        request.periodInMs = period;
        request.numBssidsPerScan = bssidsPerScan;
        request.maxScansToCache = batch;
        request.reportEvents = reportEvents;
        return request;
    }

    public static WifiScanner.ScanSettings createRequest(int type, int band, int period, int batch,
            int bssidsPerScan, int reportEvents) {
        return createRequest(WifiScanner.SCAN_TYPE_HIGH_ACCURACY, band, period, 0, 0,
                batch, bssidsPerScan, reportEvents);
    }

    public static WifiScanner.ScanSettings createRequest(int band, int period, int batch,
            int bssidsPerScan, int reportEvents) {
        return createRequest(WifiScanner.SCAN_TYPE_HIGH_ACCURACY, band, period, 0, 0, batch,
                bssidsPerScan, reportEvents);
    }

    /**
     * Create an exponential back off scan request if maxPeriod != period && maxPeriod != 0.
     */
    public static WifiScanner.ScanSettings createRequest(int type, int band, int period,
            int maxPeriod, int stepCount, int batch, int bssidsPerScan, int reportEvents) {
        WifiScanner.ScanSettings request = new WifiScanner.ScanSettings();
        request.type = type;
        request.band = band;
        request.channels = null;
        request.periodInMs = period;
        request.maxPeriodInMs = maxPeriod;
        request.stepCount = stepCount;
        request.numBssidsPerScan = bssidsPerScan;
        request.maxScansToCache = batch;
        request.reportEvents = reportEvents;
        return request;
    }

    /**
     * Builder to create WifiNative.ScanSettings objects for testing
     */
    public static class NativeScanSettingsBuilder {
        private final WifiNative.ScanSettings mSettings = new WifiNative.ScanSettings();
        public NativeScanSettingsBuilder() {
            mSettings.scanType = WifiScanner.SCAN_TYPE_LOW_LATENCY;
            mSettings.buckets = new WifiNative.BucketSettings[0];
            mSettings.num_buckets = 0;
            mSettings.report_threshold_percent = 100;
        }

        public NativeScanSettingsBuilder withType(int type) {
            mSettings.scanType = type;
            return this;
        }
        public NativeScanSettingsBuilder withBasePeriod(int basePeriod) {
            mSettings.base_period_ms = basePeriod;
            return this;
        }
        public NativeScanSettingsBuilder withMaxApPerScan(int maxAp) {
            mSettings.max_ap_per_scan = maxAp;
            return this;
        }
        public NativeScanSettingsBuilder withMaxScansToCache(int maxScans) {
            mSettings.report_threshold_num_scans = maxScans;
            return this;
        }
        public NativeScanSettingsBuilder withMaxPercentToCache(int percent) {
            mSettings.report_threshold_percent = percent;
            return this;
        }
        public NativeScanSettingsBuilder withEnable6GhzRnr(boolean enable) {
            mSettings.enable6GhzRnr = enable;
            return this;
        }
        public NativeScanSettingsBuilder withVendorIes(byte[] vendorIes) {
            if (vendorIes == null) {
                mSettings.vendorIes = null;
            } else {
                mSettings.vendorIes = Arrays.copyOf(vendorIes, vendorIes.length);
            }
            return this;
        }

        /**
         * Add the provided hidden network SSIDs to scan request.
         * @param networkSSIDs List of hidden network SSIDs
         * @return builder object
         */
        public NativeScanSettingsBuilder withHiddenNetworkSSIDs(String[] networkSSIDs) {
            mSettings.hiddenNetworks = new WifiNative.HiddenNetwork[networkSSIDs.length];
            for (int i = 0; i < networkSSIDs.length; i++) {
                mSettings.hiddenNetworks[i] = new WifiNative.HiddenNetwork();
                mSettings.hiddenNetworks[i].ssid = networkSSIDs[i];
            }
            return this;
        }

        public NativeScanSettingsBuilder addBucketWithChannelCollection(
                int period, int reportEvents, ChannelCollection channelCollection) {
            WifiNative.BucketSettings bucket = new WifiNative.BucketSettings();
            bucket.bucket = mSettings.num_buckets;
            bucket.period_ms = period;
            bucket.report_events = reportEvents;
            channelCollection.fillBucketSettings(bucket, Integer.MAX_VALUE);
            return addBucket(bucket);
        }

        public NativeScanSettingsBuilder addBucketWithBand(
                int period, int reportEvents, int band) {
            WifiNative.BucketSettings bucket = new WifiNative.BucketSettings();
            bucket.bucket = mSettings.num_buckets;
            bucket.band = band;
            bucket.period_ms = period;
            bucket.report_events = reportEvents;
            return addBucket(bucket);
        }

        public NativeScanSettingsBuilder addBucketWithChannels(
                int period, int reportEvents, WifiScanner.ChannelSpec... channels) {
            int[] channelFreqs = new int[channels.length];
            for (int i = 0; i < channels.length; ++i) {
                channelFreqs[i] = channels[i].frequency;
            }
            return addBucketWithChannels(period, reportEvents, channelFreqs);
        }

        public NativeScanSettingsBuilder addBucketWithChannels(
                int period, int reportEvents, int... channels) {
            WifiNative.BucketSettings bucket = new WifiNative.BucketSettings();
            bucket.bucket = mSettings.num_buckets;
            bucket.band = WifiScanner.WIFI_BAND_UNSPECIFIED;
            bucket.num_channels = channels.length;
            bucket.channels = channelsToNativeSettings(channels);
            bucket.period_ms = period;
            bucket.report_events = reportEvents;
            return addBucket(bucket);
        }

        public NativeScanSettingsBuilder addBucket(WifiNative.BucketSettings bucket) {
            mSettings.buckets = Arrays.copyOf(mSettings.buckets, mSettings.num_buckets + 1);
            mSettings.buckets[mSettings.num_buckets] = bucket;
            mSettings.num_buckets = mSettings.num_buckets + 1;
            return this;
        }

        public WifiNative.ScanSettings build() {
            return mSettings;
        }

    }

    /**
     * Compute the expected native scan settings that are expected for the given
     * WifiScanner.ScanSettings using the given ChannelHelper.
     * This method is created to test 6Ghz PSC scanning.
     */
    public static WifiNative.ScanSettings computeSingleScanNativeSettingsWithChannelHelper(
            WifiScanner.ScanSettings requestSettings, ChannelHelper channelHelper) {
        int reportEvents = requestSettings.reportEvents | WifiScanner.REPORT_EVENT_AFTER_EACH_SCAN;
        NativeScanSettingsBuilder builder = new NativeScanSettingsBuilder()
                .withBasePeriod(0)
                .withMaxApPerScan(0)
                .withMaxPercentToCache(0)
                .withMaxScansToCache(0)
                .withType(requestSettings.type);
        if (SdkLevel.isAtLeastS()) {
            builder.withEnable6GhzRnr(requestSettings.getRnrSetting()
                    == WifiScanner.WIFI_RNR_ENABLED
                    || (requestSettings.getRnrSetting()
                    == WifiScanner.WIFI_RNR_ENABLED_IF_WIFI_BAND_6_GHZ_SCANNED
                    && ChannelHelper.is6GhzBandIncluded(requestSettings.band)));
        }
        ChannelCollection channelCollection = channelHelper.createChannelCollection();
        channelCollection.addChannels(requestSettings);
        builder.addBucketWithChannelCollection(0, reportEvents, channelCollection);
        return builder.build();
    }

    /**
     * Compute the expected native scan settings that are expected for the given
     * WifiScanner.ScanSettings.
     */
    public static WifiNative.ScanSettings computeSingleScanNativeSettings(
            WifiScanner.ScanSettings requestSettings) {
        int reportEvents = requestSettings.reportEvents | WifiScanner.REPORT_EVENT_AFTER_EACH_SCAN;
        NativeScanSettingsBuilder builder = new NativeScanSettingsBuilder()
                .withBasePeriod(0)
                .withMaxApPerScan(0)
                .withMaxPercentToCache(0)
                .withMaxScansToCache(0)
                .withType(requestSettings.type);
        if (SdkLevel.isAtLeastS()) {
            builder.withEnable6GhzRnr(requestSettings.getRnrSetting()
                    == WifiScanner.WIFI_RNR_ENABLED
                    || (requestSettings.getRnrSetting()
                    == WifiScanner.WIFI_RNR_ENABLED_IF_WIFI_BAND_6_GHZ_SCANNED
                    && ChannelHelper.is6GhzBandIncluded(requestSettings.band)));
        }
        if (requestSettings.band == WifiScanner.WIFI_BAND_UNSPECIFIED) {
            builder.addBucketWithChannels(0, reportEvents, requestSettings.channels);
        } else {
            builder.addBucketWithBand(0, reportEvents, requestSettings.band);
        }
        if (SdkLevel.isAtLeastU()) {
            List<ScanResult.InformationElement> vendorIesList = requestSettings.getVendorIes();
            byte[] nativeSettingsVendorIes = getVendorIesBytesFromVendorIesList(vendorIesList);
            builder.withVendorIes(nativeSettingsVendorIes);
        }

        return builder.build();
    }

    /**
     * Compute the expected native scan settings that are expected for the given channels.
     */
    public static WifiNative.ScanSettings createSingleScanNativeSettingsForChannels(
            int reportEvents, WifiScanner.ChannelSpec... channels) {
        return createSingleScanNativeSettingsForChannels(
            WifiScanner.SCAN_TYPE_LOW_LATENCY, reportEvents, channels);
    }

    /**
     * Compute the expected native scan settings that are expected for the given channels & type.
     */
    public static WifiNative.ScanSettings createSingleScanNativeSettingsForChannels(
            int nativeScanType, int reportEvents, WifiScanner.ChannelSpec... channels) {
        int actualReportEvents = reportEvents | WifiScanner.REPORT_EVENT_AFTER_EACH_SCAN;
        return new NativeScanSettingsBuilder()
                .withBasePeriod(0)
                .withMaxApPerScan(0)
                .withMaxPercentToCache(0)
                .withMaxScansToCache(0)
                .addBucketWithChannels(0, actualReportEvents, channels)
                .withType(nativeScanType)
                .build();
    }

    public static Set<Integer> createFreqSet(int... elements) {
        Set<Integer> set = new HashSet<>();
        for (int e : elements) {
            set.add(e);
        }
        return set;
    }

    public static ScanResult createScanResult(int freq) {
        return new ScanResult.Builder(WifiSsid.fromUtf8Text("AN SSID"),
                MacAddressUtils.createRandomUnicastAddress().toString())
                .setCaps("")
                .setFrequency(freq)
                .build();
    }

    private static ScanData createScanData(int[] freqs, int bucketsScanned, int bandScanned) {
        ScanResult[] results = new ScanResult[freqs.length];
        for (int i = 0; i < freqs.length; ++i) {
            results[i] = createScanResult(freqs[i]);
        }
        return new ScanData(0, 0, bucketsScanned, bandScanned, results);
    }

    private static ScanData createScanData(int[] freqs, int bucketsScanned) {
        return createScanData(freqs, bucketsScanned, WifiScanner.WIFI_BAND_UNSPECIFIED);
    }

    public static ScanData[] createScanDatas(
            int[][] freqs, int[] bucketsScanned, int[] bandsScanned) {
        assumeTrue(freqs.length == bucketsScanned.length);
        assumeTrue(freqs.length == bandsScanned.length);
        ScanData[] data = new ScanData[freqs.length];
        for (int i = 0; i < freqs.length; ++i) {
            data[i] = createScanData(freqs[i], bucketsScanned[i], bandsScanned[i]);
        }
        return data;
    }

    public static ScanData[] createScanDatas(int[][] freqs, int[] bucketsScanned) {
        assumeTrue(freqs.length == bucketsScanned.length);
        ScanData[] data = new ScanData[freqs.length];
        for (int i = 0; i < freqs.length; ++i) {
            data[i] = createScanData(freqs[i], bucketsScanned[i]);
        }
        return data;
    }

    public static ScanData[] createScanDatas(int[][] freqs) {
        return createScanDatas(freqs, new int[freqs.length] /* defaults all 0 */);
    }

    private static void assertScanResultEquals(
            String prefix, ScanResult expected, ScanResult actual) {
        assertEquals(prefix + "SSID", expected.SSID, actual.SSID);
        assertEquals(prefix + "wifiSsid", expected.wifiSsid.toString(), actual.wifiSsid.toString());
        assertEquals(prefix + "BSSID", expected.BSSID, actual.BSSID);
        assertEquals(prefix + "capabilities", expected.capabilities, actual.capabilities);
        assertEquals(prefix + "level", expected.level, actual.level);
        assertEquals(prefix + "frequency", expected.frequency, actual.frequency);
        assertEquals(prefix + "timestamp", expected.timestamp, actual.timestamp);
        assertEquals(prefix + "seen", expected.seen, actual.seen);
    }

    private static void assertScanResultsEquals(String prefix, ScanResult[] expected,
            ScanResult[] actual) {
        assertNotNull(prefix + "expected ScanResults was null", expected);
        assertNotNull(prefix + "actual ScanResults was null", actual);
        assertEquals(prefix + "results.length", expected.length, actual.length);
        for (int j = 0; j < expected.length; ++j) {
            ScanResult expectedResult = expected[j];
            ScanResult actualResult = actual[j];
            assertScanResultEquals(prefix + "results[" + j + "]", actualResult, expectedResult);
        }
    }

    private static void assertScanResultsEqualsAnyOrder(String prefix, ScanResult[] expected,
            ScanResult[] actual) {
        assertNotNull(prefix + "expected ScanResults was null", expected);
        assertNotNull(prefix + "actual ScanResults was null", actual);
        assertEquals(prefix + "results.length", expected.length, actual.length);

        // Sort using the bssids.
        ScanResult[] sortedExpected = Arrays
                .stream(expected)
                .sorted(Comparator.comparing(s -> s.BSSID))
                .toArray(ScanResult[]::new);
        ScanResult[] sortedActual = Arrays
                .stream(actual)
                .sorted(Comparator.comparing(s -> s.BSSID))
                .toArray(ScanResult[]::new);
        assertScanResultsEquals(prefix, sortedExpected, sortedActual);
    }

    /**
     * Asserts if the provided scan results are the same.
     */
    public static void assertScanResultEquals(ScanResult expected, ScanResult actual) {
        assertScanResultEquals("", expected, actual);
    }

    /**
     * Asserts if the provided scan result arrays are the same.
     */
    public static void assertScanResultsEquals(ScanResult[] expected, ScanResult[] actual) {
        assertScanResultsEquals("", expected, actual);
    }

    /**
     * Asserts if the provided scan result arrays are the same.
     */
    public static void assertScanResultsEqualsAnyOrder(ScanResult[] expected, ScanResult[] actual) {
        assertScanResultsEqualsAnyOrder("", expected, actual);
    }

    private static void assertScanDataEquals(String prefix, ScanData expected, ScanData actual) {
        assertNotNull(prefix + "expected ScanData was null", expected);
        assertNotNull(prefix + "actual ScanData was null", actual);
        assertEquals(prefix + "id", expected.getId(), actual.getId());
        assertEquals(prefix + "flags", expected.getFlags(), actual.getFlags());
        assertEquals(prefix + "band", expected.getScannedBandsInternal(),
                actual.getScannedBandsInternal());
        assertScanResultsEquals(prefix, expected.getResults(), actual.getResults());
    }

    public static void assertScanDataEquals(ScanData expected, ScanData actual) {
        assertScanDataEquals("", expected, actual);
    }

    public static void assertScanDatasEquals(String prefix, ScanData[] expected, ScanData[] actual) {
        assertNotNull("expected " + prefix + "ScanData[] was null", expected);
        assertNotNull("actaul " + prefix + "ScanData[] was null", actual);
        assertEquals(prefix + "ScanData.length", expected.length, actual.length);
        for (int i = 0; i < expected.length; ++i) {
            assertScanDataEquals(prefix + "ScanData[" + i + "].", expected[i], actual[i]);
        }
    }

    public static void assertScanDatasEquals(ScanData[] expected, ScanData[] actual) {
        assertScanDatasEquals("", expected, actual);
    }

    public static WifiScanner.ChannelSpec[] channelsToSpec(int... channels) {
        WifiScanner.ChannelSpec[] channelSpecs = new WifiScanner.ChannelSpec[channels.length];
        for (int i = 0; i < channels.length; ++i) {
            channelSpecs[i] = new WifiScanner.ChannelSpec(channels[i]);
        }
        return channelSpecs;
    }

    public static void assertNativeScanSettingsEquals(WifiNative.ScanSettings expected,
            WifiNative.ScanSettings actual) {
        assertEquals("scan type", expected.scanType, actual.scanType);
        assertEquals("bssids per scan", expected.max_ap_per_scan, actual.max_ap_per_scan);
        assertEquals("scans to cache", expected.report_threshold_num_scans,
                actual.report_threshold_num_scans);
        assertEquals("percent to cache", expected.report_threshold_percent,
                actual.report_threshold_percent);
        assertEquals("base period", expected.base_period_ms, actual.base_period_ms);
        assertEquals("enable 6Ghz RNR", expected.enable6GhzRnr, actual.enable6GhzRnr);
        assertArrayEquals("vendor IEs", expected.vendorIes, actual.vendorIes);

        assertEquals("number of buckets", expected.num_buckets, actual.num_buckets);
        assertNotNull("buckets was null", actual.buckets);
        for (int i = 0; i < expected.buckets.length; ++i) {
            assertNotNull("buckets[" + i + "] was null", actual.buckets[i]);
            assertEquals("buckets[" + i + "].period",
                    expected.buckets[i].period_ms, actual.buckets[i].period_ms);
            assertEquals("buckets[" + i + "].reportEvents",
                    expected.buckets[i].report_events, actual.buckets[i].report_events);

            assertEquals("buckets[" + i + "].band",
                    expected.buckets[i].band, actual.buckets[i].band);
            if (expected.buckets[i].band == WifiScanner.WIFI_BAND_UNSPECIFIED) {
                Set<Integer> expectedChannels = new HashSet<>();
                for (WifiNative.ChannelSettings channel : expected.buckets[i].channels) {
                    expectedChannels.add(channel.frequency);
                }
                Set<Integer> actualChannels = new HashSet<>();
                for (WifiNative.ChannelSettings channel : actual.buckets[i].channels) {
                    actualChannels.add(channel.frequency);
                }
                assertEquals("channels", expectedChannels, actualChannels);
            } else {
                // since num_channels and channels are ignored when band is not
                // WifiScanner.WIFI_BAND_UNSPECIFIED just assert that there are no channels
                // the band equality was already checked above
                assertEquals("buckets[" + i + "].num_channels not 0", 0,
                        actual.buckets[i].num_channels);
                assertTrue("buckets[" + i + "].channels not null or empty",
                        actual.buckets[i].channels == null
                        || actual.buckets[i].channels.length == 0);
            }
        }
    }

    /**
     * Asserts if the provided pno settings are the same.
     */
    public static void assertNativePnoSettingsEquals(WifiNative.PnoSettings expected,
            WifiNative.PnoSettings actual) {
        assertNotNull("expected was null", expected);
        assertNotNull("actaul was null", actual);
        assertEquals("min5GHzRssi", expected.min5GHzRssi, actual.min5GHzRssi);
        assertEquals("min24GHzRssi", expected.min24GHzRssi, actual.min24GHzRssi);
        assertEquals("min6GHzRssi", expected.min6GHzRssi, actual.min6GHzRssi);
        assertEquals("isConnected", expected.isConnected, actual.isConnected);
        assertNotNull("expected networkList was null", expected.networkList);
        assertNotNull("actual networkList was null", actual.networkList);
        assertEquals("networkList.length", expected.networkList.length, actual.networkList.length);
        for (int i = 0; i < expected.networkList.length; i++) {
            assertEquals("networkList[" + i + "].ssid",
                    expected.networkList[i].ssid, actual.networkList[i].ssid);
            assertEquals("networkList[" + i + "].flags",
                    expected.networkList[i].flags, actual.networkList[i].flags);
            assertEquals("networkList[" + i + "].auth_bit_field",
                    expected.networkList[i].auth_bit_field, actual.networkList[i].auth_bit_field);
        }
    }

    /**
     * Convert a list of channel frequencies to an array of equivalent WifiNative.ChannelSettings
     */
    public static WifiNative.ChannelSettings[] channelsToNativeSettings(int... channels) {
        WifiNative.ChannelSettings[] channelSpecs = new WifiNative.ChannelSettings[channels.length];
        for (int i = 0; i < channels.length; ++i) {
            channelSpecs[i] = new WifiNative.ChannelSettings();
            channelSpecs[i].frequency = channels[i];
        }
        return channelSpecs;
    }

    /**
     * Matcher to check that a BucketSettings has the given band
     */
    public static Matcher<WifiNative.BucketSettings> bandIs(final int expectedBand) {
        return new TypeSafeDiagnosingMatcher<WifiNative.BucketSettings>() {
            @Override
            public boolean matchesSafely(WifiNative.BucketSettings bucketSettings,
                    Description mismatchDescription) {
                if (bucketSettings.band != expectedBand) {
                    mismatchDescription
                            .appendText("did not have expected band ").appendValue(expectedBand)
                            .appendText(", was ").appendValue(bucketSettings.band);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("bucket band is ").appendValue(expectedBand);
            }
        };
    }

    /**
     * Matcher to check that a BucketSettings has exactly the given channels
     */
    public static Matcher<WifiNative.BucketSettings> channelsAre(final int... expectedChannels) {
        return new TypeSafeDiagnosingMatcher<WifiNative.BucketSettings>() {
            @Override
            public boolean matchesSafely(WifiNative.BucketSettings bucketSettings,
                    Description mismatchDescription) {
                if (bucketSettings.band != WifiScanner.WIFI_BAND_UNSPECIFIED) {
                    mismatchDescription.appendText("did not have expected unspecified band, was ")
                            .appendValue(bucketSettings.band);
                    return false;
                } else if (bucketSettings.num_channels != expectedChannels.length) {
                    mismatchDescription
                            .appendText("did not have expected num_channels ")
                            .appendValue(expectedChannels.length)
                            .appendText(", was ").appendValue(bucketSettings.num_channels);
                    return false;
                } else if (bucketSettings.channels == null) {
                    mismatchDescription.appendText("had null channels array");
                    return false;
                } else if (bucketSettings.channels.length != expectedChannels.length) {
                    mismatchDescription
                            .appendText("did not have channels array length matching excepted ")
                            .appendValue(expectedChannels.length)
                            .appendText(", was ").appendValue(bucketSettings.channels.length);
                    return false;
                } else {
                    Set<Integer> foundChannelsSet = new HashSet<>();
                    for (int i = 0; i < bucketSettings.channels.length; ++i) {
                        foundChannelsSet.add(bucketSettings.channels[i].frequency);
                    }
                    Set<Integer> expectedChannelsSet = new HashSet<>();
                    for (int i = 0; i < expectedChannels.length; ++i) {
                        expectedChannelsSet.add(expectedChannels[i]);
                    }

                    if (!foundChannelsSet.containsAll(expectedChannelsSet)
                            || foundChannelsSet.size() != expectedChannelsSet.size()) {
                        Set<Integer> extraChannelsSet = new HashSet<>(foundChannelsSet);
                        extraChannelsSet.removeAll(expectedChannelsSet);
                        expectedChannelsSet.removeAll(foundChannelsSet);
                        mismatchDescription
                                .appendText("does not contain expected channels ")
                                .appendValue(expectedChannelsSet);
                        if (extraChannelsSet.size() > 0) {
                            mismatchDescription
                                    .appendText(", but contains extra channels ")
                                    .appendValue(extraChannelsSet);
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("bucket channels are ").appendValue(expectedChannels);
            }
        };
    }
}
