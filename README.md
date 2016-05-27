grails-spring-security-oauth-google [![Build Status](https://api.travis-ci.org/donbeave/grails-spring-security-oauth-google.png?branch=master)](https://travis-ci.org/donbeave/grails-spring-security-oauth-google)
====================================

Azure extension for [Grails Spring Security OAuth][spring-security-oauth-plugin] plugin

Installation
------------

Add the following plugin definition to your BuildConfig:
```groovy
// ...
plugins {
  // ...
  compile ':spring-security-oauth:2.0.2'
  compile ':spring-security-oauth-azure:0.3.1'
  // ...
}
```

Usage
-----

Add to your Config.groovy:


```groovy
def appName = grails.util.Metadata.current.'app.name'
def baseURL = grails.serverURL ?: "http://127.0.0.1:${System.getProperty('server.port', '8080')}/${appName}"
oauth {
  // ...
  providers {
    // ...

    azure {
      api = org.grails.plugin.springsecurity.oauth.GoogleApi20
      key = 'azure_ad_clientID'
      secret = 'azure_ad_secretKey'
      successUri = '/oauth/azure/success'
      failureUri = '/oauth/azure/error'
      callback = "${baseURL}/oauth/azure/callback"
      scope = 'User.Read'
    }
    // ...
  }
}
```

Add the following to Config.groovy if you want to use Secured annotations together with Spring security. All references to InterceptUrlMap should be removed.

```groovy
grails.plugin.springsecurity.securityConfigType = 'Annotation'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        '/':                              ['permitAll'],
        '/index':                         ['permitAll'],
        '/index.gsp':                     ['permitAll'],
        '/login/**':                      ['permitAll'],
        '/assets/**':                     ['permitAll'],
        '/**/js/**':                      ['permitAll'],
        '/**/css/**':                     ['permitAll'],
        '/**/images/**':                  ['permitAll'],
        '/**/favicon.ico':                ['permitAll'],
        '/oauth/**':                      ['permitAll'],
        '/springSecurityOAuth/**':        ['permitAll']
]
```

In your view you can use the taglib exposed from this plugin and from OAuth plugin to create links and to know if the user is authenticated with a given provider:
```xml
<oauth:connect provider="azure" id="azure-connect-link">Azure</oauth:connect>

Logged in with azure?
<s2o:ifLoggedInWith provider="azure">yes</s2o:ifLoggedInWith>
<s2o:ifNotLoggedInWith provider="azure">no</s2o:ifNotLoggedInWith>
```

You can look at [bagage's sample app][sample-app].

Copyright and license
---------------------

Copyright 2012-2014 Mihai Cazacu, Enrico Comiti and Alexey Zhokhov under the [Apache License, Version 2.0](LICENSE). Supported by [AZ][zhokhov].

[zhokhov]: http://www.zhokhov.com
[spring-security-oauth-plugin]: https://github.com/enr/grails-spring-security-oauth
[sample-app]: https://github.com/bagage/grails-google-authentification-example
