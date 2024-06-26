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

package com.android.server.wifi.aware;

import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.WifiAwareDataPathSecurityConfig;
import android.util.SparseIntArray;

/**
 * Utility methods for Wi-Fi Aware unit tests.
 */
public class TestUtils {
    public static class MonitoredWifiAwareNativeApi extends WifiAwareNativeApi {
        private final SparseIntArray mTransactionIds = new SparseIntArray();

        MonitoredWifiAwareNativeApi() {
            super(null); // doesn't matter - mocking parent
        }

        private void addTransactionId(int transactionId) {
            if (transactionId == 0) {
                return; // transaction ID == 0 is used as a placeholder ID in several command
            }
            mTransactionIds.append(transactionId, mTransactionIds.get(transactionId) + 1);
        }

        public void validateUniqueTransactionIds() {
            for (int i = 0; i < mTransactionIds.size(); ++i) {
                if (mTransactionIds.valueAt(i) != 1) {
                    throw new RuntimeException("Duplicate transaction IDs -- " + mTransactionIds);
                }
            }
        }

        public boolean enableAndConfigure(short transactionId, ConfigRequest configRequest,
                boolean notifyIdentityChange, boolean initialConfiguration, boolean isInteractive,
                boolean isIdle, boolean rangingEnabled, boolean isInstantCommunicationEnabled,
                int instantModeChannel, int clusterId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean disable(short transactionId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean publish(short transactionId, byte publishId, PublishConfig publishConfig,
                byte[] nik) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean subscribe(short transactionId, byte subscribeId,
                SubscribeConfig subscribeConfig, byte[] nik) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean sendMessage(short transactionId, byte pubSubId, int requestorInstanceId,
                byte[] dest, byte[] message, int messageId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean stopPublish(short transactionId, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean stopSubscribe(short transactionId, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean getCapabilities(short transactionId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean createAwareNetworkInterface(short transactionId, String interfaceName) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean deleteAwareNetworkInterface(short transactionId, String interfaceName) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean initiateDataPath(short transactionId, int peerId, int channelRequestType,
                int channel, byte[] peer, String interfaceName,
                boolean isOutOfBand, byte[] appInfo, Capabilities capabilities,
                WifiAwareDataPathSecurityConfig securityConfig, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean respondToDataPathRequest(short transactionId, boolean accept, int ndpId,
                String interfaceName, byte[] appInfo,
                boolean isOutOfBand, Capabilities capabilities,
                WifiAwareDataPathSecurityConfig securityConfig, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean endDataPath(short transactionId, int ndpId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean respondToPairingRequest(short transactionId, int pairingId, boolean accept,
                byte[] pairingIdentityKey, boolean enablePairingCache, int requestType, byte[] pmk,
                String password, int akm, int cipherSuite) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean initiatePairing(short transactionId, int peerId, byte[] peer,
                byte[] pairingIdentityKey, boolean enablePairingCache, int requestType, byte[] pmk,
                String password, int akm, int cipherSuite) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean respondToBootstrappingRequest(short transactionId, int bootstrappingId,
                boolean accept, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean initiateBootstrapping(short transactionId, int peerId, byte[] peer,
                int method, byte[] cookie, byte pubSubId, boolean isComeBack) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean suspendRequest(short transactionId, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }

        public boolean resumeRequest(short transactionId, byte pubSubId) {
            addTransactionId(transactionId);
            return true;
        }
    }
}
