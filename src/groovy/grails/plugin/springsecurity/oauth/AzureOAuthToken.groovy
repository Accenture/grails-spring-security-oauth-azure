/*
 * Copyright 2012 the original author or authors
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
package grails.plugin.springsecurity.oauth

import org.scribe.model.Token
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * OAuth authentication token for Azure AD users. It's a standard {@link OAuthToken}
 * that returns the email address as the principal.
 *
 * @author <a href='mailto:cazacugmihai@gmail.com'>Mihai Cazacu</a>
 * @author Thierry Nicola
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class AzureOAuthToken extends OAuthToken {

    public static final String PROVIDER_NAME = 'azure'

    String[] groups

    String email

    AzureOAuthToken(Token scribeToken, email, groups) {
        super(scribeToken)
        this.email = email
        this.principal = email
        this.groups = groups
        groups.each { group ->
            if (group) {
                if(!this.authorities) {
                    this.authorities = new ArrayList<>();
                }
                this.authorities.add(new SimpleGrantedAuthority(group))
            }
        }
    }

    String getSocialId() {
        email
    }

    String getScreenName() {
        email
    }

    String getRefreshToken() {
        accessToken.secret
    }

    Date getExpiry() {
        accessToken.expiry
    }

    String getProviderName() {
        PROVIDER_NAME
    }

    String[] getGroups() {
        return groups
    }


}
