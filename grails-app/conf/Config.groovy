// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

// just for test, to avoid oauthService BeanCreationException "No oauth configuration found"
environments {
  test {
    oauth {
      providers {
        google {
            api = org.grails.plugin.springsecurity.oauth.AzureApi
            clientUri = ''
            authorizationEndpointUri = ''
            tokenEndpointUri = ''
            graphEndpointUri = ''
            secret = 'oauth_google_secret'
            successUri = '/oauth/azure/success'
            failureUri = '/oauth/azure/failure'
            callback = "${baseURL}/oauth/azure/callback"
            scope = 'https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email'
        }
      }
    }
  }
}
