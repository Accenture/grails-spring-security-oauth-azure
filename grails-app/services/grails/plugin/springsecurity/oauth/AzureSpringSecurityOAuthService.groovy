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

import grails.converters.JSON

/**
 * @author <a href='mailto:cazacugmihai@gmail.com'>Mihai Cazacu</a>
 */
class AzureSpringSecurityOAuthService {

    def oauthService
    def grailsApplication

    /*
     * Requires scope of "https://www.googleapis.com/auth/userinfo.email"
     * Expected response:
     *   { "email": "username@gmail.com", "verified_email": true }
     */
    def createAuthToken(accessToken) {
        def response = oauthService.getAzureResource(accessToken, 'https://graph.windows.net/me?api-version=1.6')
        def user
        try {
            user = JSON.parse(response.body)
        } catch (Exception e) {
            log.error "Error parsing response from Azure. Response:\n${response.body}\n${response.headers}"
            throw new OAuthLoginException('Error parsing response from Azure', e)
        }
        if (!user?.userPrincipalName) {
            log.error "No userPrincipalName from Azure. Response:\n${response.body}"
            throw new OAuthLoginException('No userPrincipalName from Azure')
        }
        def userMemberOf = oauthService.getAzureResource(accessToken,
                "https://graph.windows.net/myorganization/users/${user.objectId}/memberOf?api-version=1.6");
        def groupList
        try {
            groupList = JSON.parse(userMemberOf.body)
        } catch (Exception e) {
            log.error "Error parsing response from Azure. Response:\n${response.body}\n${response.headers}"
            throw new OAuthLoginException('Error parsing response from Azure', e)
        }
        def groups = []
        def groupMap = grailsApplication.config.oauth.providers.azure.groupMap
        if (groupList?.value) {
            groups = groupList.value.collect { groupItem ->
                groupMap ? groupMap[groupItem.displayName]:groupItem.displayName
            }
        }
        return new AzureOAuthToken(accessToken, user.userPrincipalName, groups)
    }

}

