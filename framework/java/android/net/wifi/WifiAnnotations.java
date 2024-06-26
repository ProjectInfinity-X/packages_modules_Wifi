/*
 * Copyright (C) 2019 The Android Open Source Project
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

package android.net.wifi;

import android.annotation.IntDef;
import android.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Wifi annotations meant to be statically linked into client modules, since they cannot be
 * exposed as @SystemApi.
 *
 * e.g. {@link IntDef}, {@link StringDef}
 *
 * @hide
 */
public final class WifiAnnotations {
    private WifiAnnotations() {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"SCAN_TYPE_"}, value = {
            WifiScanner.SCAN_TYPE_LOW_LATENCY,
            WifiScanner.SCAN_TYPE_LOW_POWER,
            WifiScanner.SCAN_TYPE_HIGH_ACCURACY})
    public @interface ScanType {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"WIFI_BAND_"}, value = {
            WifiScanner.WIFI_BAND_UNSPECIFIED,
            WifiScanner.WIFI_BAND_24_GHZ,
            WifiScanner.WIFI_BAND_5_GHZ,
            WifiScanner.WIFI_BAND_5_GHZ_DFS_ONLY,
            WifiScanner.WIFI_BAND_6_GHZ})
    public @interface WifiBandBasic {}

    @IntDef(prefix = { "CHANNEL_WIDTH_" }, value = {
            SoftApInfo.CHANNEL_WIDTH_INVALID,
            SoftApInfo.CHANNEL_WIDTH_20MHZ_NOHT,
            SoftApInfo.CHANNEL_WIDTH_20MHZ,
            SoftApInfo.CHANNEL_WIDTH_40MHZ,
            SoftApInfo.CHANNEL_WIDTH_80MHZ,
            SoftApInfo.CHANNEL_WIDTH_80MHZ_PLUS_MHZ,
            SoftApInfo.CHANNEL_WIDTH_160MHZ,
            SoftApInfo.CHANNEL_WIDTH_320MHZ,
            SoftApInfo.CHANNEL_WIDTH_2160MHZ,
            SoftApInfo.CHANNEL_WIDTH_4320MHZ,
            SoftApInfo.CHANNEL_WIDTH_6480MHZ,
            SoftApInfo.CHANNEL_WIDTH_8640MHZ,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Bandwidth {}

    @IntDef(prefix = { "CHANNEL_WIDTH_" }, value = {
            ScanResult.CHANNEL_WIDTH_20MHZ,
            ScanResult.CHANNEL_WIDTH_40MHZ,
            ScanResult.CHANNEL_WIDTH_80MHZ,
            ScanResult.CHANNEL_WIDTH_160MHZ,
            ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ,
            ScanResult.CHANNEL_WIDTH_320MHZ,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChannelWidth{}

    @IntDef(prefix = { "PREAMBLE_" }, value = {
            ScanResult.PREAMBLE_LEGACY,
            ScanResult.PREAMBLE_HT,
            ScanResult.PREAMBLE_VHT,
            ScanResult.PREAMBLE_HE,
            ScanResult.PREAMBLE_EHT,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PreambleType {}

    @IntDef(prefix = { "WIFI_STANDARD_" }, value = {
            ScanResult.WIFI_STANDARD_UNKNOWN,
            ScanResult.WIFI_STANDARD_LEGACY,
            ScanResult.WIFI_STANDARD_11N,
            ScanResult.WIFI_STANDARD_11AC,
            ScanResult.WIFI_STANDARD_11AX,
            ScanResult.WIFI_STANDARD_11AD,
            ScanResult.WIFI_STANDARD_11BE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface WifiStandard{}

    @IntDef(prefix = { "PROTOCOL_" }, value = {
            ScanResult.PROTOCOL_NONE,
            ScanResult.PROTOCOL_WPA,
            ScanResult.PROTOCOL_RSN,
            ScanResult.PROTOCOL_OSEN,
            ScanResult.PROTOCOL_WAPI
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Protocol {}

    @IntDef(prefix = { "KEY_MGMT_" }, value = {
        ScanResult.KEY_MGMT_NONE,
        ScanResult.KEY_MGMT_PSK,
        ScanResult.KEY_MGMT_EAP,
        ScanResult.KEY_MGMT_FT_PSK,
        ScanResult.KEY_MGMT_FT_EAP,
        ScanResult.KEY_MGMT_PSK_SHA256,
        ScanResult.KEY_MGMT_EAP_SHA256,
        ScanResult.KEY_MGMT_OSEN,
        ScanResult.KEY_MGMT_SAE,
        ScanResult.KEY_MGMT_OWE,
        ScanResult.KEY_MGMT_EAP_SUITE_B_192,
        ScanResult.KEY_MGMT_FT_SAE,
        ScanResult.KEY_MGMT_OWE_TRANSITION,
        ScanResult.KEY_MGMT_WAPI_PSK,
        ScanResult.KEY_MGMT_WAPI_CERT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface KeyMgmt {}

    @IntDef(prefix = { "CIPHER_" }, value = {
        ScanResult.CIPHER_NONE,
        ScanResult.CIPHER_NO_GROUP_ADDRESSED,
        ScanResult.CIPHER_TKIP,
        ScanResult.CIPHER_CCMP,
        ScanResult.CIPHER_GCMP_256,
        ScanResult.CIPHER_SMS4,
        ScanResult.CIPHER_GCMP_128,
        ScanResult.CIPHER_BIP_GMAC_128,
        ScanResult.CIPHER_BIP_GMAC_256,
        ScanResult.CIPHER_BIP_CMAC_256,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Cipher {}

    /**
     * Security type of current connection.
     * @hide
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = { "SECURITY_TYPE_" }, value = {
            WifiInfo.SECURITY_TYPE_UNKNOWN,
            WifiInfo.SECURITY_TYPE_OPEN,
            WifiInfo.SECURITY_TYPE_WEP,
            WifiInfo.SECURITY_TYPE_PSK,
            WifiInfo.SECURITY_TYPE_EAP,
            WifiInfo.SECURITY_TYPE_SAE,
            WifiInfo.SECURITY_TYPE_OWE,
            WifiInfo.SECURITY_TYPE_WAPI_PSK,
            WifiInfo.SECURITY_TYPE_WAPI_CERT,
            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE,
            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT,
            WifiInfo.SECURITY_TYPE_PASSPOINT_R1_R2,
            WifiInfo.SECURITY_TYPE_PASSPOINT_R3,
            WifiInfo.SECURITY_TYPE_DPP,
    })
    public @interface SecurityType {}

    /**
     * The type of wifi uri scheme.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"URI_SCHEME_"}, value = {
            UriParserResults.URI_SCHEME_ZXING_WIFI_NETWORK_CONFIG,
            UriParserResults.URI_SCHEME_DPP,
    })
    public @interface UriScheme {}
}
