/*
 * Copyright 2016 Stefan Wold <ratler@stderr.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gtri.shibboleth.idp.g2f.authn.impl.datastores

import org.gtri.shibboleth.idp.g2f.authn.api.DeviceDataStore
import org.gtri.shibboleth.idp.g2f.authn.impl.G2fUserContext
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import sun.reflect.generics.reflectiveObjects.NotImplementedException


@Slf4j
public class DummyDataStore implements DeviceDataStore {
    private def keyHandle
    private def publicKey
    private def counter = 0
    private def compromised = false

    @Autowired
    DummyDataStore(@Value('${g2f.dummyStore.keyHandle}') String keyHandle,
                   @Value('${g2f.dummyStore.publicKey}') String publicKey) {
        log.debug("G2F DummyDataStore constructor")
        this.publicKey = publicKey
        this.keyHandle = keyHandle
    }

    @Override
    def beginAuthentication(G2fUserContext g2fUserContext) {
        log.debug("G2F DummyDataStore beginAuthentication...")
    }

    @Override
    boolean finishAuthentication(G2fUserContext g2fUserContext) {
        def username = g2fUserContext.username
        log.debug("G2F DummyDataStore finishAuthentication...")
    }
}

