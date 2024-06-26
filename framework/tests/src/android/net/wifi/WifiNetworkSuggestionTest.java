/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.net.MacAddress;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.PasspointTestUtils;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.telephony.SubscriptionManager;

import androidx.test.filters.SmallTest;

import com.android.modules.utils.build.SdkLevel;

import org.junit.Test;

import java.nio.charset.Charset;
import java.security.cert.X509Certificate;

/**
 * Unit tests for {@link android.net.wifi.WifiNetworkSuggestion}.
 */
@SmallTest
public class WifiNetworkSuggestionTest {
    private static final String TEST_SSID = "\"Test123\"";
    private static final String TEST_BSSID = "12:12:12:12:12:12";
    private static final String TEST_SSID_1 = "\"Test1234\"";
    private static final String TEST_PRESHARED_KEY = "Test123";
    private static final String TEST_FQDN = "fqdn";
    private static final String TEST_WAPI_CERT_SUITE = "suite";
    private static final String TEST_DOMAIN_SUFFIX_MATCH = "domainSuffixMatch";
    private static final int DEFAULT_PRIORITY_GROUP = 0;
    private static final int TEST_PRIORITY_GROUP = 1;
    private static final int TEST_CARRIER_ID = 1998;
    private static final ParcelUuid GROUP_UUID = ParcelUuid
            .fromString("0000110B-0000-1000-8000-00805F9B34FB");

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for Open network which requires
     * app interaction.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForOpenNetworkWithReqAppInteraction() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setIsAppInteractionRequired(true)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.NONE));
        assertTrue(suggestion.isAppInteractionRequired);
        assertFalse(suggestion.isUserInteractionRequired);
        assertEquals(WifiConfiguration.METERED_OVERRIDE_NONE,
                suggestion.wifiConfiguration.meteredOverride);
        assertEquals(-1, suggestion.wifiConfiguration.priority);
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA_EAP network which requires
     * app interaction, not share credential and has a priority of zero set.
     */
    @Test
    public void
            testWifiNetworkSuggestionBuilderForWpa2EapNetworkWithPriorityAndReqAppInteraction() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setIsAppInteractionRequired(true)
                .setCredentialSharedWithUser(false)
                .setPriority(0)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK));
        assertEquals("\"" + TEST_PRESHARED_KEY + "\"",
                suggestion.wifiConfiguration.preSharedKey);
        assertTrue(suggestion.isAppInteractionRequired);
        assertFalse(suggestion.isUserInteractionRequired);
        assertEquals(WifiConfiguration.METERED_OVERRIDE_NONE,
                suggestion.wifiConfiguration.meteredOverride);
        assertEquals(0, suggestion.wifiConfiguration.priority);
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA_PSK network which requires
     * user interaction and is metered.
     */
    @Test
    public void
            testWifiNetworkSuggestionBuilderForWpa2PskNetworkWithMeteredAndReqUserInteraction() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setIsUserInteractionRequired(true)
                .setIsInitialAutojoinEnabled(false)
                .setIsMetered(true)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK));
        assertEquals("\"" + TEST_PRESHARED_KEY + "\"",
                suggestion.wifiConfiguration.preSharedKey);
        assertFalse(suggestion.isAppInteractionRequired);
        assertTrue(suggestion.isUserInteractionRequired);
        assertEquals(WifiConfiguration.METERED_OVERRIDE_METERED,
                suggestion.wifiConfiguration.meteredOverride);
        assertEquals(-1, suggestion.wifiConfiguration.priority);
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertFalse(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA_PSK network which requires
     * user interaction and is not metered.
     */
    @Test
    public void
            testWifiNetworkSuggestionBuilderForWpa2PskNetworkWithNotMeteredAndReqUserInteraction() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setIsUserInteractionRequired(true)
                .setIsInitialAutojoinEnabled(false)
                .setIsMetered(false)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_PSK));
        assertEquals("\"" + TEST_PRESHARED_KEY + "\"",
                suggestion.wifiConfiguration.preSharedKey);
        assertFalse(suggestion.isAppInteractionRequired);
        assertTrue(suggestion.isUserInteractionRequired);
        assertEquals(WifiConfiguration.METERED_OVERRIDE_NOT_METERED,
                suggestion.wifiConfiguration.meteredOverride);
        assertEquals(-1, suggestion.wifiConfiguration.priority);
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertFalse(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for OWE network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForEnhancedOpenNetworkWithBssid() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setBssid(MacAddress.fromString(TEST_BSSID))
                .setIsEnhancedOpen(true)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertEquals(TEST_BSSID, suggestion.wifiConfiguration.BSSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.OWE));
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for OWE network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForOemPaidEnhancedOpenNetworkWithBssid() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setBssid(MacAddress.fromString(TEST_BSSID))
                .setOemPaid(true)
                .setIsEnhancedOpen(true)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertEquals(TEST_BSSID, suggestion.wifiConfiguration.BSSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.OWE));
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertTrue(suggestion.wifiConfiguration.oemPaid);
        assertTrue(suggestion.isOemPaid());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for OWE network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForOemPrivateEnhancedOpenNetworkWithBssid() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setBssid(MacAddress.fromString(TEST_BSSID))
                .setOemPrivate(true)
                .setIsEnhancedOpen(true)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertEquals(TEST_BSSID, suggestion.wifiConfiguration.BSSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.OWE));
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertTrue(suggestion.wifiConfiguration.oemPrivate);
        assertTrue(suggestion.isOemPrivate());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for SAE network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3PskNetwork() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .setCredentialSharedWithUser(true)
                .setIsInitialAutojoinEnabled(false)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SAE));
        assertEquals("\"" + TEST_PRESHARED_KEY + "\"",
                suggestion.wifiConfiguration.preSharedKey);
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertFalse(suggestion.isInitialAutoJoinEnabled);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3EapNetwork() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X));
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_EAP));
        assertFalse(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise standard network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3EapNetworkWithStandardApi() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseStandardModeConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X));
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_EAP));
        assertFalse(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise network
     * with 192-bit RSA SuiteB certificates.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3EapNetworkWithSuiteBRsaCerts() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_RSA3072_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_RSA3072_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_RSA3072_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseStandardModeConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X));
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_EAP));
        assertFalse(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise network
     * with 192-bit ECC certificates.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3EapNetworkWithSuiteBEccCerts() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_ECDSA_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_ECC_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_ECDSA_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseStandardModeConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X));
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WPA_EAP));
        assertFalse(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.CCMP));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise 192-bit RSA SuiteB network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3SuiteBRsaEapNetwork() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_RSA3072_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_RSA3072_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_RSA3072_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.GCMP_256));
        assertTrue(suggestion.wifiConfiguration.allowedGroupManagementCiphers
                .get(WifiConfiguration.GroupMgmtCipher.BIP_GMAC_256));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise 192-bit RSA SuiteB network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3SuiteBRsaEapNetworWith192BitApi() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_RSA3072_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_RSA3072_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_RSA3072_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Enterprise192BitModeConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.GCMP_256));
        assertTrue(suggestion.wifiConfiguration.allowedGroupManagementCiphers
                .get(WifiConfiguration.GroupMgmtCipher.BIP_GMAC_256));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise 192-bit ECC SuiteB network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3SuiteBEccEapNetwork() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_ECDSA_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_ECC_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_ECDSA_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.GCMP_256));
        assertTrue(suggestion.wifiConfiguration.allowedGroupManagementCiphers
                .get(WifiConfiguration.GroupMgmtCipher.BIP_GMAC_256));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WPA3-Enterprise 192-bit ECC SuiteB network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWpa3SuiteBEccEapNetworkWith192BitApi() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_SUITE_B_ECDSA_CERT);
        enterpriseConfig.setClientKeyEntryWithCertificateChain(FakeKeys.CLIENT_SUITE_B_ECC_KEY,
                new X509Certificate[] {FakeKeys.CLIENT_SUITE_B_ECDSA_CERT});

        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Enterprise192BitModeConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.SUITE_B_192));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.GCMP_256));
        assertTrue(suggestion.wifiConfiguration.allowedGroupManagementCiphers
                .get(WifiConfiguration.GroupMgmtCipher.BIP_GMAC_256));
        assertTrue(suggestion.wifiConfiguration.requirePmf);
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        // allowedSuiteBCiphers are set according to the loaded certificate and cannot be tested
        // here.
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.isInitialAutoJoinEnabled);
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Ensure create enterprise suggestion requires CA, when CA certificate is missing, will throw
     * an exception.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderForEapNetworkWithoutCa() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .build();
    }

    /**
     * Ensure create enterprise suggestion requires CA, when both domain suffix and alt subject
     * match are missing, will throw an exception.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderForEapNetworkWithoutMatch() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3EnterpriseStandardModeConfig(enterpriseConfig)
                .build();
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WAPI-PSK network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWapiPskNetwork() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWapiPassphrase(TEST_PRESHARED_KEY)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WAPI_PSK));
        assertTrue(suggestion.wifiConfiguration.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.SMS4));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.SMS4));
        assertEquals("\"" + TEST_PRESHARED_KEY + "\"",
                suggestion.wifiConfiguration.preSharedKey);
        assertNull(suggestion.getEnterpriseConfig());
    }


    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WAPI-CERT network.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWapiCertNetwork() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.WAPI_CERT);
        enterpriseConfig.setWapiCertSuite(TEST_WAPI_CERT_SUITE);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWapiEnterpriseConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WAPI_CERT));
        assertTrue(suggestion.wifiConfiguration.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.SMS4));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.SMS4));
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        assertNotNull(suggestion.wifiConfiguration.enterpriseConfig);
        assertEquals(WifiEnterpriseConfig.Eap.WAPI_CERT,
                suggestion.wifiConfiguration.enterpriseConfig.getEapMethod());
        assertEquals(TEST_WAPI_CERT_SUITE,
                suggestion.wifiConfiguration.enterpriseConfig.getWapiCertSuite());
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for WAPI-CERT network
     * which selects the certificate suite automatically.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForWapiCertAutoNetwork() {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.WAPI_CERT);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWapiEnterpriseConfig(enterpriseConfig)
                .build();

        assertEquals("\"" + TEST_SSID + "\"", suggestion.wifiConfiguration.SSID);
        assertTrue(suggestion.wifiConfiguration.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.WAPI_CERT));
        assertTrue(suggestion.wifiConfiguration.allowedPairwiseCiphers
                .get(WifiConfiguration.PairwiseCipher.SMS4));
        assertTrue(suggestion.wifiConfiguration.allowedGroupCiphers
                .get(WifiConfiguration.GroupCipher.SMS4));
        assertNull(suggestion.wifiConfiguration.preSharedKey);
        assertNotNull(suggestion.wifiConfiguration.enterpriseConfig);
        assertEquals(WifiEnterpriseConfig.Eap.WAPI_CERT,
                suggestion.wifiConfiguration.enterpriseConfig.getEapMethod());
        assertEquals("",
                suggestion.wifiConfiguration.enterpriseConfig.getWapiCertSuite());
        assertNotNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Validate correctness of WifiNetworkSuggestion object created by
     * {@link WifiNetworkSuggestion.Builder#build()} for Passpoint network which requires
     *  app interaction and metered.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderForPasspointNetworkWithReqAppInteractionMetered() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setIsAppInteractionRequired(true)
                .setIsMetered(true)
                .build();
        assertEquals(TEST_FQDN, suggestion.wifiConfiguration.FQDN);
        assertTrue(suggestion.isAppInteractionRequired);
        assertEquals(suggestion.wifiConfiguration.meteredOverride,
                WifiConfiguration.METERED_OVERRIDE_METERED);
        assertEquals(suggestion.getPasspointConfig().getMeteredOverride(),
                WifiConfiguration.METERED_OVERRIDE_METERED);
        assertTrue(suggestion.isUserAllowedToManuallyConnect);
        assertNull(suggestion.getEnterpriseConfig());
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#setSsid(String)} throws an exception
     * when the string is not Unicode.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderSetSsidWithNonUnicodeString() {
        new WifiNetworkSuggestion.Builder()
                .setSsid("\ud800")
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#setWpa2Passphrase(String)} throws an exception
     * when the string is not ASCII encodable.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderSetWpa2PasphraseWithNonAsciiString() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase("salvē")
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)}}
     * throws an exception when the PasspointConfiguration is not valid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderSetPasspointConfigWithNonValid() {
        PasspointConfiguration passpointConfiguration = new PasspointConfiguration();
        new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setSsid(String)} is not set.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithNoSsid() {
        new WifiNetworkSuggestion.Builder()
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setSsid(String)} is invoked with an invalid value.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithInvalidSsid() {
        new WifiNetworkSuggestion.Builder()
                .setSsid("")
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setBssid(MacAddress)} is invoked with an invalid
     * value.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithInvalidBroadcastBssid() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setBssid(MacAddress.BROADCAST_ADDRESS)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setBssid(MacAddress)} is invoked with an invalid
     * value.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithInvalidAllZeroBssid() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setBssid(WifiManager.ALL_ZEROS_MAC_ADDRESS)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#setPriority(int)} throws an exception
     * when the value is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderWithInvalidPriority() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setPriority(-2)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setWpa2Passphrase(String)} and
     * {@link WifiNetworkSuggestion.Builder#setWpa3Passphrase(String)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothWpa2PasphraseAndWpa3Passphrase() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setWpa3Passphrase(String)} and
     * {@link WifiNetworkSuggestion.Builder
     * #setWpa3EnterpriseStandardModeConfig(WifiEnterpriseConfig)}
     * are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothWpa3PasphraseAndEnterprise() {
        assumeTrue(SdkLevel.isAtLeastS());
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .setWpa3EnterpriseStandardModeConfig(new WifiEnterpriseConfig())
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setWpa3Passphrase(String)} and
     * {@link WifiNetworkSuggestion.Builder#setIsEnhancedOpen(boolean)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothWpa3PasphraseAndEnhancedOpen() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .setIsEnhancedOpen(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setSsid(String)} and
     * {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothSsidAndPasspointConfig() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setWpa2Passphrase(String)} and
     * {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothWpa2PassphraseAndPasspointConfig() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setWpa3Passphrase(String)} and
     * {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothWpa3PassphraseAndPasspointConfig() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both
     * {@link WifiNetworkSuggestion.Builder
     * #setWpa3EnterpriseStandardModeConfig(WifiEnterpriseConfig)}
     * and {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are
     * invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothEnterpriseAndPasspointConfig() {
        assumeTrue(SdkLevel.isAtLeastS());
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setWpa3EnterpriseStandardModeConfig(new WifiEnterpriseConfig())
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setIsEnhancedOpen(boolean)} and
     * {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothEnhancedOpenAndPasspointConfig() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setIsEnhancedOpen(true)
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when both {@link WifiNetworkSuggestion.Builder#setIsHiddenSsid(boolean)} and
     * {@link WifiNetworkSuggestion.Builder#setPasspointConfig(PasspointConfiguration)} are invoked.
     */
    @Test(expected = IllegalStateException.class)
    public void testWifiNetworkSuggestionBuilderWithBothHiddenSsidAndPasspointConfig() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setIsHiddenSsid(true)
                .setPasspointConfig(passpointConfiguration)
                .build();
    }

    /**
     * Verify that the macRandomizationSetting defaults to RANDOMIZATION_PERSISTENT and could be set
     * to RANDOMIZATION_NON_PERSISTENT.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderSetMacRandomization() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .build();
        assertEquals(WifiConfiguration.RANDOMIZATION_PERSISTENT,
                suggestion.wifiConfiguration.macRandomizationSetting);

        suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setMacRandomizationSetting(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT)
                .build();
        assertEquals(WifiConfiguration.RANDOMIZATION_PERSISTENT,
                suggestion.wifiConfiguration.macRandomizationSetting);
        assertEquals(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT,
                suggestion.getMacRandomizationSetting());

        suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setMacRandomizationSetting(
                        WifiNetworkSuggestion.RANDOMIZATION_NON_PERSISTENT)
                .build();
        assertEquals(WifiConfiguration.RANDOMIZATION_NON_PERSISTENT,
                suggestion.wifiConfiguration.macRandomizationSetting);
        assertEquals(WifiNetworkSuggestion.RANDOMIZATION_NON_PERSISTENT,
                suggestion.getMacRandomizationSetting());
    }

    /**
     * Verify that the builder creates the appropriate PasspointConfiguration according to the
     * enhanced MAC randomization setting.
     */
    @Test
    public void testWifiNetworkSuggestionBuilderSetMacRandomizationPasspoint() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        passpointConfiguration.setMacRandomizationEnabled(false);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .build();
        assertEquals(false,
                suggestion.passpointConfiguration.isNonPersistentMacRandomizationEnabled());
        assertTrue(suggestion.passpointConfiguration.isMacRandomizationEnabled());

        suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setMacRandomizationSetting(
                        WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT)
                .build();
        assertEquals(false,
                suggestion.passpointConfiguration.isNonPersistentMacRandomizationEnabled());
        assertTrue(suggestion.passpointConfiguration.isMacRandomizationEnabled());
        assertEquals(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT,
                suggestion.getMacRandomizationSetting());

        suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setMacRandomizationSetting(
                        WifiNetworkSuggestion.RANDOMIZATION_NON_PERSISTENT)
                .build();
        assertEquals(true,
                suggestion.passpointConfiguration.isNonPersistentMacRandomizationEnabled());
        assertTrue(suggestion.passpointConfiguration.isMacRandomizationEnabled());
        assertEquals(WifiNetworkSuggestion.RANDOMIZATION_NON_PERSISTENT,
                suggestion.getMacRandomizationSetting());
    }

    /**
     * Verify calling setMacRandomizationSetting with an invalid argument throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWifiNetworkSuggestionBuilderSetMacRandomizationInvalidParam() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setMacRandomizationSetting(-1234)
                .build();
    }

    /**
     * Verify that the builder creates the appropriate SIM credential suggestion with SubId, also
     * verify {@link WifiNetworkSuggestion#equals(Object)} consider suggestion with different SubId
     * as different suggestions.
     */
    @Test
    public void testSimCredentialNetworkWithSubId() {
        assumeTrue(SdkLevel.isAtLeastS());
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.SIM);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
        WifiNetworkSuggestion suggestion1 = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setSubscriptionId(1)
                .build();
        assertEquals(1, suggestion1.getSubscriptionId());
        WifiNetworkSuggestion suggestion2 = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setSubscriptionId(2)
                .build();
        assertEquals(2, suggestion2.getSubscriptionId());
        assertNotEquals(suggestion1, suggestion2);
    }

    /**
     * Check that parcel marshalling/unmarshalling works
     */
    @Test
    public void testWifiNetworkSuggestionParcel() {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = TEST_SSID;
        configuration.BSSID = TEST_BSSID;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion(
                configuration, null, false, true, true, true, TEST_PRIORITY_GROUP);

        Parcel parcelW = Parcel.obtain();
        suggestion.writeToParcel(parcelW, 0);
        byte[] bytes = parcelW.marshall();
        parcelW.recycle();

        Parcel parcelR = Parcel.obtain();
        parcelR.unmarshall(bytes, 0, bytes.length);
        parcelR.setDataPosition(0);
        WifiNetworkSuggestion parcelSuggestion =
                WifiNetworkSuggestion.CREATOR.createFromParcel(parcelR);

        // Two suggestion objects are considered equal if they point to the same network (i.e same
        // SSID + keyMgmt + same UID). |isAppInteractionRequired| & |isUserInteractionRequired| are
        // not considered for equality and hence needs to be checked for explicitly below.
        assertEquals(suggestion, parcelSuggestion);
        assertEquals(suggestion.hashCode(), parcelSuggestion.hashCode());
        assertEquals(suggestion.isAppInteractionRequired,
                parcelSuggestion.isAppInteractionRequired);
        assertEquals(suggestion.isUserInteractionRequired,
                parcelSuggestion.isUserInteractionRequired);
        assertEquals(suggestion.isInitialAutoJoinEnabled,
                parcelSuggestion.isInitialAutoJoinEnabled);
    }

    /**
     * Check that parcel marshalling/unmarshalling works
     */
    @Test
    public void testPasspointNetworkSuggestionParcel() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .build();

        Parcel parcelW = Parcel.obtain();
        suggestion.writeToParcel(parcelW, 0);
        byte[] bytes = parcelW.marshall();
        parcelW.recycle();

        Parcel parcelR = Parcel.obtain();
        parcelR.unmarshall(bytes, 0, bytes.length);
        parcelR.setDataPosition(0);
        WifiNetworkSuggestion parcelSuggestion =
                WifiNetworkSuggestion.CREATOR.createFromParcel(parcelR);

        // Two suggestion objects are considered equal if they point to the same network (i.e same
        // SSID + keyMgmt + same UID). |isAppInteractionRequired| & |isUserInteractionRequired| are
        // not considered for equality and hence needs to be checked for explicitly below.
        assertEquals(suggestion, parcelSuggestion);
        assertEquals(suggestion.hashCode(), parcelSuggestion.hashCode());
        assertEquals(suggestion.isAppInteractionRequired,
                parcelSuggestion.isAppInteractionRequired);
        assertEquals(suggestion.isUserInteractionRequired,
                parcelSuggestion.isUserInteractionRequired);
        assertEquals(suggestion.isInitialAutoJoinEnabled,
                parcelSuggestion.isInitialAutoJoinEnabled);
    }

    /**
     * Check NetworkSuggestion equals returns {@code true} for 2 network suggestions with the same
     * SSID, BSSID, key mgmt and UID.
     */
    @Test
    public void testWifiNetworkSuggestionEqualsSame() {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = TEST_SSID;
        configuration.BSSID = TEST_BSSID;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        WifiNetworkSuggestion suggestion =
                new WifiNetworkSuggestion(configuration, null, true, false, true, true,
                        TEST_PRIORITY_GROUP);

        WifiConfiguration configuration1 = new WifiConfiguration();
        configuration1.SSID = TEST_SSID;
        configuration1.BSSID = TEST_BSSID;
        configuration1.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion(configuration1, null, false, true, true, true,
                        DEFAULT_PRIORITY_GROUP);

        assertEquals(suggestion, suggestion1);
        assertEquals(suggestion.hashCode(), suggestion1.hashCode());
    }

    /**
     * Check NetworkSuggestion equals returns {@code false} for 2 network suggestions with the same
     * BSSID, key mgmt and UID, but different SSID.
     */
    @Test
    public void testWifiNetworkSuggestionEqualsFailsWhenSsidIsDifferent() {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = TEST_SSID;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion =
                new WifiNetworkSuggestion(configuration, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        WifiConfiguration configuration1 = new WifiConfiguration();
        configuration1.SSID = TEST_SSID_1;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion(configuration1, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        assertNotEquals(suggestion, suggestion1);
    }

    /**
     * Check NetworkSuggestion equals returns {@code false} for 2 network suggestions with the same
     * SSID, key mgmt and UID, but different BSSID.
     */
    @Test
    public void testWifiNetworkSuggestionEqualsFailsWhenBssidIsDifferent() {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = TEST_SSID;
        configuration.BSSID = TEST_BSSID;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion =
                new WifiNetworkSuggestion(configuration, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        WifiConfiguration configuration1 = new WifiConfiguration();
        configuration1.SSID = TEST_SSID;
        configuration1.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion(configuration1, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        assertNotEquals(suggestion, suggestion1);
    }

    /**
     * Check NetworkSuggestion equals returns {@code false} for 2 network suggestions with the same
     * SSID, BSSID and UID, but different key mgmt.
     */
    @Test
    public void testWifiNetworkSuggestionEqualsFailsWhenKeyMgmtIsDifferent() {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = TEST_SSID;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiNetworkSuggestion suggestion =
                new WifiNetworkSuggestion(configuration, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        WifiConfiguration configuration1 = new WifiConfiguration();
        configuration1.SSID = TEST_SSID;
        configuration1.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion(configuration1, null, false, false, true, true,
                        DEFAULT_PRIORITY_GROUP);

        assertNotEquals(suggestion, suggestion1);
    }

    /**
     * Check NetworkSuggestion equals returns {@code true} for 2 Passpoint network suggestions with
     * same FQDN.
     */
    @Test
    public void testPasspointNetworkSuggestionEqualsSameWithSameFQDN() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        PasspointConfiguration passpointConfiguration1 = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .build();
        WifiNetworkSuggestion suggestion1 = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration1)
                .build();
        assertEquals(suggestion, suggestion1);
        assertEquals(suggestion.hashCode(), suggestion1.hashCode());
    }

    /**
     * Check NetworkSuggestion equals returns {@code false} for 2 Passpoint network suggestions with
     * different FQDN.
     */
    @Test
    public void testPasspointNetworkSuggestionNotEqualsSameWithDifferentFQDN() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        PasspointConfiguration passpointConfiguration1 = PasspointTestUtils.createConfig();
        passpointConfiguration1.getHomeSp().setFqdn(TEST_FQDN + 1);

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .build();
        WifiNetworkSuggestion suggestion1 = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration1)
                .build();
        assertNotEquals(suggestion, suggestion1);
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} to
     * true on a open network suggestion.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCredentialSharedWithUserWithOpenNetwork() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setCredentialSharedWithUser(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when {@link WifiNetworkSuggestion.Builder#setIsInitialAutojoinEnabled(boolean)} to
     * false on a open network suggestion.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetIsAutoJoinDisabledWithOpenNetwork() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setIsInitialAutojoinEnabled(false)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setIsInitialAutojoinEnabled(boolean)}
     * and {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} (boolean)}
     * to false on a network suggestion.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetIsAutoJoinDisabledWithSecureNetworkNotSharedWithUser() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCredentialSharedWithUser(false)
                .setIsInitialAutojoinEnabled(false)
                .build();
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     */
    @Test
    public void testSetIsNetworkAsUntrusted() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setUntrusted(true)
                .build();
        assertTrue(suggestion.isUntrusted());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     * Also the {@link WifiNetworkSuggestion#isUserAllowedToManuallyConnect} should be false;
     */
    @Test
    public void testSetIsNetworkAsUntrustedOnPasspointNetwork() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setUntrusted(true)
                .build();
        assertTrue(suggestion.isUntrusted());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     */
    @Test
    public void testSetIsNetworkAsOemPaid() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setOemPaid(true)
                .build();
        assertTrue(suggestion.isOemPaid());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     * Also the {@link WifiNetworkSuggestion#isUserAllowedToManuallyConnect} should be false;
     */
    @Test
    public void testSetIsNetworkAsOemPaidOnPasspointNetwork() {
        assumeTrue(SdkLevel.isAtLeastS());

        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setOemPaid(true)
                .build();
        assertTrue(suggestion.isOemPaid());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.getPasspointConfig().isOemPaid());
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     */
    @Test
    public void testSetIsNetworkAsOemPrivate() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setOemPrivate(true)
                .build();
        assertTrue(suggestion.isOemPrivate());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} set the
     * correct value to the WifiConfiguration.
     * Also the {@link WifiNetworkSuggestion#isUserAllowedToManuallyConnect} should be false;
     */
    @Test
    public void testSetIsNetworkAsOemPrivateOnPasspointNetwork() {
        assumeTrue(SdkLevel.isAtLeastS());

        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setOemPrivate(true)
                .build();
        assertTrue(suggestion.isOemPrivate());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
        assertTrue(suggestion.getPasspointConfig().isOemPrivate());
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set {@link WifiNetworkSuggestion.Builder#setUntrusted(boolean)} to true and
     * set {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} to true
     * together.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCredentialSharedWithUserWithSetIsNetworkAsUntrusted() {
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCredentialSharedWithUser(true)
                .setUntrusted(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set {@link WifiNetworkSuggestion.Builder#setOemPaid(boolean)} to true and
     * set {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} to true
     * together.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCredentialSharedWithUserWithSetIsNetworkAsOemPaid() {
        assumeTrue(SdkLevel.isAtLeastS());

        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCredentialSharedWithUser(true)
                .setOemPaid(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set {@link WifiNetworkSuggestion.Builder#setOemPrivate(boolean)} to true and
     * set {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} to true
     * together.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCredentialSharedWithUserWithSetIsNetworkAsOemPrivate() {
        assumeTrue(SdkLevel.isAtLeastS());

        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCredentialSharedWithUser(true)
                .setOemPrivate(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setIsInitialAutojoinEnabled(boolean)}
     * and {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} (boolean)}
     * to false on a passpoint suggestion.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetIsAutoJoinDisabledWithSecureNetworkNotSharedWithUserForPasspoint() {
        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setCredentialSharedWithUser(false)
                .setIsInitialAutojoinEnabled(false)
                .build();
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCarrierMerged(boolean)} (boolean)} set the
     * correct value to the WifiConfiguration.
     */
    @Test
    public void testSetCarrierMergedNetwork() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setSubscriptionId(1)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setCarrierMerged(true)
                .setIsMetered(true)
                .build();
        assertTrue(suggestion.isCarrierMerged());
        assertTrue(suggestion.wifiConfiguration.carrierMerged);
    }

    /**
     * Validate {@link WifiNetworkSuggestion.Builder#setCarrierMerged(boolean)} (boolean)} set the
     * correct value to the passpoint network.
     */
    @Test
    public void testSetCarrierMergedNetworkOnPasspointNetwork() {
        assumeTrue(SdkLevel.isAtLeastS());

        PasspointConfiguration passpointConfiguration = PasspointTestUtils.createConfig();
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(passpointConfiguration)
                .setSubscriptionId(1)
                .setCarrierMerged(true)
                .setIsMetered(true)
                .build();
        assertTrue(suggestion.isCarrierMerged());
        assertTrue(suggestion.getPasspointConfig().isCarrierMerged());
        assertEquals(1, suggestion.getPasspointConfig().getSubscriptionId());

        passpointConfiguration.setSubscriptionId(4);
        assertEquals(1, suggestion.getPasspointConfig().getSubscriptionId());
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setCarrierMerged(boolean)} (boolean)}
     * to true on a network is not metered.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCarrierMergedNetworkOnUnmeteredNetworkFail() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);
        new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setSubscriptionId(1)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setCarrierMerged(true)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setCarrierMerged(boolean)} (boolean)}
     * to true on a network which {@link WifiNetworkSuggestion.Builder#setSubscriptionId(int)}
     * is not set with a valid sub id.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCarrierMergedNetworkWithoutValidSubscriptionIdFail() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
        enterpriseConfig.setCaCertificate(FakeKeys.CA_CERT0);
        enterpriseConfig.setDomainSuffixMatch(TEST_DOMAIN_SUFFIX_MATCH);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2EnterpriseConfig(enterpriseConfig)
                .setCarrierMerged(true)
                .setIsMetered(true)
                .build();
        assertTrue(suggestion.isCarrierMerged());
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setCarrierMerged(boolean)} (boolean)}
     * to true on a non enterprise network.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetCarrierMergedNetworkWithNonEnterpriseNetworkFail() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setSubscriptionId(1)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCarrierMerged(true)
                .setIsMetered(true)
                .build();
        assertTrue(suggestion.isCarrierMerged());
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#setSubscriptionId(int)} throws an exception when
     * Subscription ID is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidSubscriptionId() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSubscriptionId(SubscriptionManager.INVALID_SUBSCRIPTION_ID)
                .build();
    }

    /**
     * Test set and get carrier Id
     */
    @Test
    public void testSetCarrierId() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setCarrierId(TEST_CARRIER_ID)
                .build();

        assertEquals(TEST_CARRIER_ID, suggestion.getCarrierId());
    }

    /**
     * Test set and get SAE Hash-to-Element only mode for WPA3 SAE network.
     */
    @Test
    public void testSetSaeH2eOnlyModeForWpa3Sae() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa3Passphrase(TEST_PRESHARED_KEY)
                .setIsWpa3SaeH2eOnlyModeEnabled(true)
                .build();
        assertTrue(suggestion.getWifiConfiguration().getSecurityParamsList()
                .stream().anyMatch(param -> param.isSaeH2eOnlyMode()));
    }

    /**
     * Test set and get SAE Hash-to-Element only mode for WPA2 PSK network.
     * For non-WPA3 SAE network, enabling H2E only mode should raise
     * IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetSaeH2eOnlyModeForWpa2Psk() {
        assumeTrue(SdkLevel.isAtLeastS());

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setIsWpa3SaeH2eOnlyModeEnabled(true)
                .build();
    }

    /**
     * Test set a network suggestion with restricted
     */
    @Test
    public void testSetRestrictedNetwork() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setRestricted(true)
                .build();
        assertTrue(suggestion.isRestricted());
        assertFalse(suggestion.isUserAllowedToManuallyConnect);
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set {@link WifiNetworkSuggestion.Builder#setRestricted(boolean)} to true and
     * set {@link WifiNetworkSuggestion.Builder#setCredentialSharedWithUser(boolean)} to true
     * together.
     */
    @Test(expected = IllegalStateException.class)
    public void testCredentialSharedWithUserWithSetRestrictedNetwork() {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setRestricted(true)
                .setCredentialSharedWithUser(true)
                .build();
    }

    /**
     * Test set a network suggestion with Subscription Group
     */
    @Test
    public void testSetSubscriptionGroup() {
        assumeTrue(SdkLevel.isAtLeastT());
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setSubscriptionGroup(GROUP_UUID)
                .build();
        assertEquals(suggestion.getSubscriptionGroup(), GROUP_UUID);
    }

    /**
     * Test set a passpoint network suggestion with Subscription Group
     */
    @Test
    public void testSetSubscriptionGroupOnPasspoint() {
        assumeTrue(SdkLevel.isAtLeastT());
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setPasspointConfig(PasspointTestUtils.createConfig())
                .setSubscriptionGroup(GROUP_UUID)
                .build();
        assertEquals(suggestion.getSubscriptionGroup(), GROUP_UUID);
        assertEquals(suggestion.getPasspointConfig().getSubscriptionGroup(), GROUP_UUID);
    }


    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set {@link WifiNetworkSuggestion.Builder#setSubscriptionGroup(ParcelUuid)} to null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetSubscriptionGroupWithNull() {
        assumeTrue(SdkLevel.isAtLeastT());
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setSubscriptionGroup(null)
                .build();
    }

    /**
     * Ensure {@link WifiNetworkSuggestion.Builder#build()} throws an exception
     * when set both {@link WifiNetworkSuggestion.Builder#setSubscriptionGroup(ParcelUuid)} and
     * {@link WifiNetworkSuggestion.Builder#setSubscriptionId(int)}
     */
    @Test(expected = IllegalStateException.class)
    public void testSetBothSubscriptionGroupAdnSubscriptionId() {
        assumeTrue(SdkLevel.isAtLeastT());
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(TEST_SSID)
                .setWpa2Passphrase(TEST_PRESHARED_KEY)
                .setSubscriptionGroup(GROUP_UUID)
                .setSubscriptionId(1)
                .build();
    }

    @Test
    public void testSetWifiSsid() {
        WifiSsid ssid = WifiSsid.fromBytes("服務集識別碼".getBytes(Charset.forName("GBK")));
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setWifiSsid(ssid)
                .build();
        assertArrayEquals(ssid.getBytes(), suggestion.getWifiSsid().getBytes());
        assertNull(suggestion.getSsid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyWifiSsid() {
        WifiSsid ssid = WifiSsid.fromBytes(null);
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setWifiSsid(ssid)
                .build();
    }

    /**
     * Test set a network suggestion with Wi-Fi 7
     */
    @Test
    public void testNetworkSuggestionForWifi7() {
        // Validate default behavior
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder().setSsid(
                TEST_SSID).setWpa2Passphrase(TEST_PRESHARED_KEY).build();
        assertTrue(suggestion.isWifi7Enabled());
        assertTrue(suggestion.wifiConfiguration.isWifi7Enabled());

        // Validate disable Wi-Fi 7
        suggestion = new WifiNetworkSuggestion.Builder().setSsid(TEST_SSID).setWpa2Passphrase(
                TEST_PRESHARED_KEY).setWifi7Enabled(false).build();
        assertFalse(suggestion.isWifi7Enabled());
        assertFalse(suggestion.wifiConfiguration.isWifi7Enabled());
    }
}
