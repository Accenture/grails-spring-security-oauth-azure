package org.grails.plugin.springsecurity.oauth;

import grails.converters.JSON;
import grails.util.Holders;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Azure OAuth2.0
 * Released under the same license as scribe (MIT License)
 *
 * @author houman001
 *         This code borrows from and modifies changes made by @yincrash and @donbeave
 * @author yincrash
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
public class AzureApi extends DefaultApi20 {

    private static final String AUTHORIZE_PARAMS = "response_type=code&client_id=%s&client_secret=%s&redirect_uri=%s&resource=%s";
    private static final String SCOPED_AUTHORIZE_PARAMS = AUTHORIZE_PARAMS + "&scope=%s";
    private static final String SUFFIX_OFFLINE = "&access_type=offline&approval_prompt=force";

    private boolean offline = false;

    protected static final String tenant = (String)Holders.getFlatConfig().get("oauth.providers.azure.tenant");

    @Override
    public String getAccessTokenEndpoint() {
        StringBuilder url = new StringBuilder();
        url.append("https://login.microsoftonline.com/");
        if ((tenant != null)&&(!tenant.isEmpty())) {
            url.append(tenant);
        } else {
            url.append("common");
        }
        url.append("/oauth2/token");
        return url.toString();
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new AccessTokenExtractor() {
            @Override
            public Token extract(String response) {
                Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");

                JSONObject tokenResponse = (JSONObject)JSON.parse(response);
                if (tokenResponse.containsKey("access_token")) {
                    String token = OAuthEncoder.decode(tokenResponse.getString("access_token"));
                    String refreshToken = "";
                    if (tokenResponse.containsKey("refresh_token"))
                        refreshToken = OAuthEncoder.decode(tokenResponse.getString("refresh_token"));
                    Date expiry = null;
                    if (tokenResponse.containsKey("expires_in")) {
                        int lifeTime = Integer.parseInt(OAuthEncoder.decode(tokenResponse.getString("expires_in")));
                        expiry = new Date(System.currentTimeMillis() + lifeTime * 1000);
                    }
                    return new AzureApiToken(token, refreshToken, expiry, response);
                } else {
                    throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
                }
            }
        };

    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        StringBuilder url = new StringBuilder();
        url.append("https://login.microsoftonline.com/");
        if ((tenant != null)&&(!tenant.isEmpty())) {
            url.append(tenant);
        } else {
            url.append("common");
        }
        url.append("/oauth2/authorize");
        // Append scope if present
        if (config.hasScope()) {
            return String.format(offline ?
                            url.toString() +
                                    "?" +
                                    SCOPED_AUTHORIZE_PARAMS + SUFFIX_OFFLINE
                            :
                            url.toString() +
                                    "?" +
                                    SCOPED_AUTHORIZE_PARAMS, OAuthEncoder.encode(config.getApiKey()),
                    OAuthEncoder.encode(config.getApiSecret()),
                    OAuthEncoder.encode(config.getCallback()),
                    OAuthEncoder.encode("https://graph.windows.net"),
                    OAuthEncoder.encode(config.getScope())
            );
        } else {
            return String.format(offline ?
                            url.toString() +
                                    "?" +
                                    AUTHORIZE_PARAMS + SUFFIX_OFFLINE
                            :
                            url.toString() +
                                    "?" +
                                    AUTHORIZE_PARAMS, OAuthEncoder.encode(config.getApiKey()),
                    OAuthEncoder.encode(config.getApiSecret()),
                    OAuthEncoder.encode(config.getCallback()),
                    OAuthEncoder.encode("https://graph.windows.net")
            );
        }
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new Service(this, config);
    }

    public class Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;
        private OAuthConfig config;

        public Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        public void setOffline(boolean offline) {
            AzureApi.this.offline = offline;
        }

        public boolean isOffline() {
            return offline;
        }

        @Override
        public void signRequest(Token accessToken, OAuthRequest request) {
            request.addHeader("Authorization","Bearer "+accessToken.getToken());
            request.addHeader("Accept", "application/json");
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
                case POST:
                    request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                    request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                    request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
                    break;
                case GET:
                default:
                    request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
                    request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                    if (config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }

}