package com.greachconf

import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class DemoHandlerSpec extends Specification {

    @AutoCleanup
    @Shared
    GroovyRatpackMainApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()


    def 'Should render "Hi Groovy Person, Greach is awesome!" without name parameter'() {
        when:
        def getText = aut.httpClient.getText('greeter')

        then:
        getText == 'Hi Groovy Person, Greach is awesome!'
    }


    def 'Should render "Hi Spain, Greach is awesome!" with name parameter'() {
        when:

        def getText = aut.httpClient.params { paramBuilder ->
            paramBuilder.put 'name', 'Spain'
        }.getText('greeter')

        then:
        getText == 'Hi Spain, Greach is awesome!'
    }
}
