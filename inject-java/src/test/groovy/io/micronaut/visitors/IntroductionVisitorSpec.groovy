package io.micronaut.visitors

import io.micronaut.inject.AbstractTypeElementSpec
import io.micronaut.inject.ExecutableMethod
import io.micronaut.inject.writer.BeanDefinitionVisitor

class IntroductionVisitorSpec extends AbstractTypeElementSpec {

    void "test that it is possible to visit introduction advice that extend from existing interfaces"() {
        given:
        def definition = buildBeanDefinition('test.MyInterface' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test;

import io.micronaut.aop.introduction.Stub;
import io.micronaut.visitors.InterfaceWithGenerics;

@Stub
interface MyInterface extends InterfaceWithGenerics<Foo, Long>  {
    String myMethod();
}

class Foo {}
''')
        expect:
        IntroductionVisitor.VISITED_CLASS_ELEMENTS.size() == 1
        IntroductionVisitor.VISITED_METHOD_ELEMENTS.size() == 4
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].name == 'save'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].genericReturnType.name == 'test.Foo'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].parameters[0].genericType.name == 'test.Foo'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[2].parameters[0].genericType.name == Iterable.name
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[2].genericReturnType.getFirstTypeArgument().get().name == 'test.Foo'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[2].parameters[0].genericType.getFirstTypeArgument().isPresent()
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[2].parameters[0].genericType.getFirstTypeArgument().get().name == 'test.Foo'
        def saveMethod = definition.findPossibleMethods("save").findFirst().get()
        saveMethod.getReturnType().type.name == 'test.Foo'
        saveMethod.getArguments()[0].type.name == 'test.Foo'
        def saveAllMethod = definition.findPossibleMethods("saveAll").findFirst().get()
        saveAllMethod.getReturnType().getFirstTypeVariable().get().type.name == 'test.Foo'
        saveAllMethod.getArguments()[0].getFirstTypeVariable().get().type.name == 'test.Foo'
        def findMethod = definition.findPossibleMethods("find").findFirst().get()
        findMethod.getReturnType().getFirstTypeVariable().get().type.name == 'test.Foo'
        findMethod.getArguments()[0].type == Long
    }

}
