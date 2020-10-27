## Settings in resources/beans.xml

### Xml-based injection - Constructor
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>

    <bean id="game" class="academy.learnprogramming.GameImpl">
        <constructor-arg ref="numberGenerator"/>
    </bean>

</beans>
```

### Xml-based life cycle - Default init method
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd"
        default-init-method="reset">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>
</beans>
```

### Xml-based life cycle - Default destory method
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd"
        default-destroy-method ="dispose">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>
</beans>
```

### Xml-based life cycle - Init method
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>

    <bean id="game" class="academy.learnprogramming.GameImpl" init-method="reset">
        <property name="numberGenerator" ref="numberGenerator"/>
    </bean>
</beans>
```

### Xml-based life cycle - Destroy method
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>

    <bean id="game" class="academy.learnprogramming.GameImpl" destroy-method="dispose">
        <property name="numberGenerator" ref="numberGenerator"/>
    </bean>
</beans>
```

### CommonAnnotationBeanPostProcessor
- This post-processor includes support for the PostConstruct and PreDestroy 
annotations - as init annotation and destroy annotation, respectively - 
through inheriting from InitDestroyAnnotationBeanPostProcessor with pre-configured annotation types.
- NOTE: A default CommonAnnotationBeanPostProcessor will be registered 
by the "context:annotation-config" and "context:component-scan" XML tags. 
Remove or turn off the default annotation configuration there 
if you intend to specify a custom CommonAnnotationBeanPostProcessor bean definition!
- NOTE: **Annotation** injection will be performed **before XML injection**; 
thus the latter configuration will override the former for properties wired through both approaches.
- [Spring.io doc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/CommonAnnotationBeanPostProcessor.html)
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>

    <bean id="game" class="academy.learnprogramming.GameImpl">
        <property name="numberGenerator" ref="numberGenerator"/>
    </bean>

    <bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/> 
</beans>
```

### Autowired Annotation

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean id="numberGenerator" class="academy.learnprogramming.NumberGeneratorImpl"/>

    <bean id="game" class="academy.learnprogramming.GameImpl">
    </bean>

</beans>
```