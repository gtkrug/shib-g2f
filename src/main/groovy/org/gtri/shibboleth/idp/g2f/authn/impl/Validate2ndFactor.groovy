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

package org.gtri.shibboleth.idp.g2f.authn.impl

import org.gtri.shibboleth.idp.g2f.authn.api.DeviceDataStore
import org.gtri.shibboleth.idp.g2f.authn.api.G2fPrincipal
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import net.shibboleth.idp.authn.AbstractValidationAction
import net.shibboleth.idp.authn.AuthnEventIds
import net.shibboleth.idp.authn.context.AuthenticationContext
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext
import net.shibboleth.idp.authn.principal.UsernamePrincipal
import net.shibboleth.idp.profile.ActionSupport
import net.shibboleth.utilities.java.support.primitive.StringSupport
import org.opensaml.profile.action.EventIds
import org.opensaml.profile.context.ProfileRequestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.annotation.Nonnull
import javax.security.auth.Subject
import javax.servlet.ServletRequest

@Slf4j
class Validate2ndFactor extends AbstractValidationAction {

    /** Spring injected data store */
    @Autowired
    @Qualifier('deviceDataStore')
    private DeviceDataStore dataStore

    /** User context */
    private G2fUserContext g2fUserContext

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
                                   @Nonnull final AuthenticationContext authenticationContext) {
        final ServletRequest servletRequest = getHttpServletRequest()
        if (!servletRequest) {
            log.error("{} no ServletRequest is available", getLogPrefix())
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX)
            recordFailure()
            return false
        }

        g2fUserContext = authenticationContext.getSubcontext(G2fUserContext.class, true)
        if (!g2fUserContext) {
            log.error("{} no g2f user context exists", getLogPrefix())
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX)
            recordFailure()
            return false
        }

        def tokenResponse = StringSupport.trimOrNull(servletRequest.getParameter("code"))
        if (!tokenResponse) {
            log.warn("{} no g2F response in the request", getLogPrefix())
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS)
            recordFailure()
            return false
        }
        g2fUserContext.tokenResponse = tokenResponse
        log.debug("{} got g2F response: {}", getLogPrefix(), tokenResponse)
        return true
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
                             @Nonnull final AuthenticationContext authenticationContext) {
        g2fUserContext = authenticationContext.getSubcontext(G2fUserContext.class, true)

        log.debug("{} validating g2f response for user {}", logPrefix, g2fUserContext.username)

        def tokenResponse = g2fUserContext.tokenResponse
        /** Check for errorCode in response */
        def result = dataStore.finishAuthentication(g2fUserContext)
        if (result) {
             log.info("{} G2F login successful", logPrefix)
             recordSuccess()
             buildAuthenticationResult(profileRequestContext, authenticationContext)
        } else {
             handleError(profileRequestContext, authenticationContext, 'InvalidCredentials', AuthnEventIds.INVALID_CREDENTIALS)
             recordFailure()
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Subject populateSubject(@Nonnull final Subject subject) {
        log.debug("{} subjects {}", getLogPrefix(), subject.getPrincipals())
        subject.getPrincipals().add(new G2fPrincipal(g2fUserContext.username))
        return subject
    }

    /** {@inheritDoc} */
    @Override
    protected void buildAuthenticationResult(@Nonnull final ProfileRequestContext profileRequestContext,
                                             @Nonnull final AuthenticationContext authenticationContext) {
        super.buildAuthenticationResult(profileRequestContext, authenticationContext)
        log.debug("{} hmm username is: {}", getLogPrefix(), g2fUserContext.username)
        profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class, true)
                .setPrincipalName((String) g2fUserContext.username)
    }
}
