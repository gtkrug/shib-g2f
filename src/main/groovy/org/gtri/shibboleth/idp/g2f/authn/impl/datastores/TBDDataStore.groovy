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
import groovy.util.logging.Slf4j
import net.shibboleth.idp.authn.AuthnEventIds
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.auth.DigestScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HttpContext
import org.springframework.http.*
import org.springframework.http.client.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Slf4j
class TBDDataStore implements DeviceDataStore {
    private String endPoint
    private HttpHeaders headers = new HttpHeaders()
    private RestTemplate restTemplate = new RestTemplate()

    /** Constructor */
    TBDDataStore(String endPoint, String username = null, String password = null) {
        log.debug("TBD Data Store constructor adding endpoint (${endPoint})")
        if (endPoint[-1] != '/') {
            endPoint += '/'
        }
        this.endPoint = endPoint
    }

    @Override
    def beginAuthentication(G2fUserContext g2fUserContext) {
        def username = g2fUserContext.username
        log.debug("Begin G2F authentication for user {}", username)
        try {
            log.debug("beginAuthentication() - Check user 2nd factor against DB (TBD)")
        } catch (Exception e) {
            log.error("DB Authentiation error: {} {}", e.statusCode, e.responseBodyAsString)
            return false
        }
        return true
    }

    @Override
    boolean finishAuthentication(G2fUserContext g2fUserContext) {
        def username = g2fUserContext.username
        log.debug("Begin finishAuthentication() ")

        return true
    }

    /**
     * HTTP basic authentication interceptor for RestTemplate
     */
    private static class BasicAuthInterceptor implements ClientHttpRequestInterceptor {
        def username
        def password

        @Override
        ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
            log.debug("Did not implement....  ${realm}")
            return execution.execute(request, data)
        }
    }

}
