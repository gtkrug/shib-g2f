> This project is work in progress.

# Shibboleth G2F Authentication Plugin
This is a generic second factor authentication flow for Shibboleth Identity Provider v3.3.x. 
It was derived from a [FIDO U2F Flow](https://github.com/Ratler/shibboleth-mfa-u2f-auth).

## Notes
Tested with Shibboleth Identity Provider 3.3.x and Google Chrome 57.x and Firefox 53

## Requirements
* [Shibboleth Identity Provider 3.3.x](http://shibboleth.net/downloads/identity-provider/latest/)
* Java 8

## Installation

1. Clone and build with `./gradlew clean installDist`

2. Copy `conf`, `edit-webapp` and `views` to $IDP_HOME, usually /opt/shibboleth-idp.
  ```
  $ cp -r build/install/shib-g2f-auth/* $IDP_HOME/
  ```

3. Copy `$IDP_HOME/conf/g2f.properties.dist` to `$IDP_HOME/conf/g2f.properties` then edit `$IDP_HOME/conf/g2f.properties`
   (Full edits would be required for a real second factor)

4. Edit `$IDP_HOME/conf/idp.properties` and change the following properties:
  * Append `/conf/g2f.properties` to the property `idp.additionalProperties=`, eg `idp.additionalProperties= /conf/ldap.properties, /conf/saml-nameid.properties, /conf/services.properties, /conf/g2f.properties`
  * Change the property `idp.authn.flows=` to `idp.authn.flows=MFA`

5. Edit `$IDP_HOME/conf/authn/general-authn.xml`, add `authn/G2f` bean to the element `<util:list id="shibboleth.AvailableAuthenticationFlows">`
```
    <bean id="authn/G2f" parent="shibboleth.AuthenticationFlow"
        p:passiveAuthenticationSupported="true"
        p:forcedAuthenticationSupported="true">
        <property name="supportedPrincipals">
            <util:list>
                <bean parent="shibboleth.SAML2AuthnContextClassRef"
                    c:classRef="http://something-descriptive.org/g2f" />
                <bean parent="shibboleth.SAML1AuthenticationMethod"
                    c:method="http://something-descriptive.org/g2f" />
            </util:list>
        </property>
    </bean>
```

Modify the supportedPrincipals list in the bean `<bean id="authn/MFA"...` to something like this:

```
    <property name="supportedPrincipals">
        <list>
            <bean parent="shibboleth.SAML2AuthnContextClassRef"
                c:classRef="http://something-descriptive.org/g2f" />
            <bean parent="shibboleth.SAML1AuthenticationMethod"
                c:method="http://something-descriptive.org/g2f" />
            <bean parent="shibboleth.SAML2AuthnContextClassRef"
                c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport" />
            <bean parent="shibboleth.SAML2AuthnContextClassRef"
                c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:Password" />
            <bean parent="shibboleth.SAML1AuthenticationMethod"
                c:method="urn:oasis:names:tc:SAML:1.0:am:password" />
        </list>
    </property>
```

6. Edit `$IDP_HOME/conf/authn/mfa-authn-config.xml` and change the element `<util:map id="shibboleth.authn.MFA.TransitionMap">`
to something like this:

```
    <util:map id="shibboleth.authn.MFA.TransitionMap">
        <!-- First rule runs the UsernamePassword login flow. -->
        <entry key="">
            <bean parent="shibboleth.authn.MFA.Transition" p:nextFlow="authn/Password" />
        </entry>

        <entry key="authn/Password">
            <bean parent="shibboleth.authn.MFA.Transition" p:nextFlow="authn/G2f" />
        </entry>
        <!-- An implicit final rule will return whatever the final flow returns. -->
    </util:map>
```

The MFA flow above is the simplest form. The MFA login flow provides a scriptable (or programmable) way to combine one
or more login flows, see
[https://wiki.shibboleth.net/confluence/display/IDP30/MultiFactorAuthnConfiguration](https://wiki.shibboleth.net/confluence/display/IDP30/MultiFactorAuthnConfiguration)
for more information.

7. Rebuild the IdP war file
  ```
  $ $IDP_HOME/bin/build.sh
  ```
