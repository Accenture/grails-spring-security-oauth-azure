grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits('global') {

    }
    log 'warn'
    repositories {
        mavenLocal()
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo 'https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo'
    }
    dependencies {
    }

    plugins {
        compile ':spring-security-oauth:2.1.0-RC4'

        // TODO we need this to compile in Grails 2.4...
        compile ':hibernate:3.6.10.14', {
            export = false
        }

        build(':release:3.0.1', ':rest-client-builder:2.0.1') {
            export = false
        }
    }
}
