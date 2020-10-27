##  Annotation-based Container Configuration
- Annotation injection is performed before XML injection. Thus, the XML configuration overrides the annotations for properties wired through both approaches.
- As always, you can register them as individual bean definitions, but they can also be implicitly registered by including the following tag in an XML-based Spring configuration (notice the inclusion of the context namespace):
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

</beans>
```

### @Required
- This annotation indicates that the affected bean property must be populated at configuration time, through an explicit property value in a bean definition or through autowiring
- The @Required annotation is formally deprecated as of Spring Framework 5.1, in favor of using constructor injection for required settings
- Example
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Required
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

### @Autowired
- JSR 330’s @Inject annotation can be used in place of Spring’s @Autowired annotation in the examples included in this section. See here for more details.
- Example
```
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```
- As of Spring Framework 4.3, an @Autowired annotation on such a constructor is no longer necessary if the target bean defines only one constructor to begin with. However, if several constructors are available and there is no primary/default constructor, at least one of the constructors must be annotated with @Autowired in order to instruct the container which one to use. See the discussion on constructor resolution for details.
- You can also apply the @Autowired annotation to traditional setter methods, as the following example shows:
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```
- You can also apply the annotation to methods with arbitrary names and multiple arguments, as the following example shows:
```
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```
- You can apply @Autowired to fields as well and even mix it with constructors, as the following example shows:
```
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    private MovieCatalog movieCatalog;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```
- Make sure that your target components (for example, MovieCatalog or CustomerPreferenceDao) are consistently declared by the type that you use for your @Autowired-annotated injection points. Otherwise, injection may fail due to a "no type match found" error at runtime.
- You can also instruct Spring to provide all beans of a particular type from the ApplicationContext by adding the @Autowired annotation to a field or method that expects an array of that type, as the following example shows:
```
public class MovieRecommender {

    @Autowired
    private MovieCatalog[] movieCatalogs;

    // ...
}
```
- The same applies for typed collections, as the following example shows:
```
public class MovieRecommender {

    private Set<MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Set<MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```
- Your target beans can implement the org.springframework.core.Ordered interface or use the @Order or standard @Priority annotation if you want items in the array or list to be sorted in a specific order. Otherwise, their order follows the registration order of the corresponding target bean definitions in the container.

You can declare the @Order annotation at the target class level and on @Bean methods, potentially for individual bean definitions (in case of multiple definitions that use the same bean class). @Order values may influence priorities at injection points, but be aware that they do not influence singleton startup order, which is an orthogonal concern determined by dependency relationships and @DependsOn declarations.

Note that the standard javax.annotation.Priority annotation is not available at the @Bean level, since it cannot be declared on methods. Its semantics can be modeled through @Order values in combination with @Primary on a single bean for each type.

- Even typed Map instances can be autowired **as long as the expected key type is String**. The map values contain all beans of the expected type, and **the keys contain the corresponding bean names**, as the following example shows:
```
public class MovieRecommender {

    private Map<String, MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Map<String, MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```
- By default, autowiring fails when no matching candidate beans are available for a given injection point. In the case of a declared array, collection, or map, at least one matching element is expected.
- The default behavior is to treat annotated methods and fields as indicating required dependencies. You can change this behavior as demonstrated in the following example, enabling the framework to skip a non-satisfiable injection point through marking it as non-required (i.e., by setting the required attribute in @Autowired to false)
- A non-required method will not be called at all if its dependency (or one of its dependencies, in case of multiple arguments) is not available. A non-required field will not get populated at all in such cases, leaving its default value in place.

```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired(required = false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```
- Injected constructor and factory method arguments are a special case since the required attribute in @Autowired has a somewhat different meaning due to Spring’s constructor resolution algorithm that may potentially deal with multiple constructors. Constructor and factory method arguments are effectively required by default but with a few special rules in a single-constructor scenario, such as multi-element injection points (arrays, collections, maps) resolving to empty instances if no matching beans are available. This allows for a common implementation pattern where all dependencies can be declared in a unique multi-argument constructor — for example, declared as a single public constructor without an @Autowired annotation.

- Only one constructor of any given bean class may declare @Autowired with the required attribute set to true, indicating the constructor to autowire when used as a Spring bean. As a consequence, if the required attribute is left at its default value true, only a single constructor may be annotated with @Autowired. If multiple constructors declare the annotation, they will all have to declare required=false in order to be considered as candidates for autowiring (analogous to autowire=constructor in XML). The constructor with the greatest number of dependencies that can be satisfied by matching beans in the Spring container will be chosen. If none of the candidates can be satisfied, then a primary/default constructor (if present) will be used. Similarly, if a class declares multiple constructors but none of them is annotated with @Autowired, then a primary/default constructor (if present) will be used. If a class only declares a single constructor to begin with, it will always be used, even if not annotated. Note that an annotated constructor does not have to be public.

- The required attribute of @Autowired is recommended over the deprecated @Required annotation on setter methods. Setting the required attribute to false indicates that the property is not required for autowiring purposes, and the property is ignored if it cannot be autowired. @Required, on the other hand, is stronger in that it enforces the property to be set by any means supported by the container, and if no value is defined, a corresponding exception is raised.

- Alternatively, you can express the non-required nature of a particular dependency through Java 8’s java.util.Optional, as the following example shows:
```
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {
        ...
    }
}
```
- As of Spring Framework 5.0, you can also use a @Nullable annotation (of any kind in any package — for example, javax.annotation.Nullable from JSR-305) or just leverage Kotlin builtin null-safety support:
```
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {
        ...
    }
}
```
- You can also use @Autowired for interfaces that are well-known resolvable dependencies: BeanFactory, ApplicationContext, Environment, ResourceLoader, ApplicationEventPublisher, and MessageSource. These interfaces and their extended interfaces, such as ConfigurableApplicationContext or ResourcePatternResolver, are automatically resolved, with no special setup necessary. The following example autowires an ApplicationContext object:
```
public class MovieRecommender {

    @Autowired
    private ApplicationContext context;

    public MovieRecommender() {
    }

    // ...
}
```
- The @Autowired, @Inject, @Value, and @Resource annotations are handled by Spring BeanPostProcessor implementations. This means that you cannot apply these annotations within your own BeanPostProcessor or BeanFactoryPostProcessor types (if any). These types must be 'wired up' explicitly by using XML or a Spring @Bean method.

### Fine-tuning Annotation-based Autowiring with @Primary
- Because autowiring by type may lead to multiple candidates, it is often necessary to have more control over the selection process. One way to accomplish this is with Spring’s @Primary annotation. @Primary indicates that a particular bean should be given preference when multiple beans are candidates to be autowired to a single-valued dependency. If exactly one primary bean exists among the candidates, it becomes the autowired value.

- Consider the following configuration that defines firstMovieCatalog as the primary MovieCatalog:
```
@Configuration
public class MovieConfiguration {

    @Bean
    @Primary
    public MovieCatalog firstMovieCatalog() { ... }

    @Bean
    public MovieCatalog secondMovieCatalog() { ... }

    // ...
}
```
- With the preceding configuration, the following MovieRecommender is autowired with the firstMovieCatalog:
```
public class MovieRecommender {

    @Autowired
    private MovieCatalog movieCatalog;

    // ...
}

The following example shows corresponding bean definitions.
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog" primary="true">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

### Fine-tuning Annotation-based Autowiring with Qualifiers
- @Primary is an effective way to use autowiring by type with several instances when one primary candidate can be determined. When you need more control over the selection process, you can use Spring’s @Qualifier annotation. You can associate qualifier values with specific arguments, narrowing the set of type matches so that a specific bean is chosen for each argument. In the simplest case, this can be a plain descriptive value, as shown in the following example:
```
public class MovieRecommender {

    @Autowired
    @Qualifier("main")
    private MovieCatalog movieCatalog;

    // ...
}
```
- You can also specify the @Qualifier annotation on individual constructor arguments or method parameters, as shown in the following example:
```
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(@Qualifier("main") MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}

The following example shows corresponding bean definitions.
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="main"/> 
        // The bean with the main qualifier value is wired with 
        // the constructor argument that is qualified with the same value.
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="action"/> 
        // The bean with the action qualifier value is wired with the constructor argument 
        // that is qualified with the same value.
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```
- For a fallback match, the bean name is considered a default qualifier value. 
    - Thus, you can define the bean with an id of main instead of the nested qualifier element, leading to the same matching result. 
    - However, although you can use this convention to refer to specific beans by name
    - @Autowired is fundamentally about **type-driven injection with optional semantic qualifiers**.
    - This means that **qualifier values**, even with the bean name fallback, always have **narrowing semantics within the set of type matches**. 
    - They do not semantically express a reference to a unique bean id. Good qualifier values are main or EMEA or persistent, expressing characteristics of a specific component that are independent from the bean id, which may be auto-generated in case of an anonymous bean definition such as the one in the preceding example.

- **Qualifiers also apply to typed collections**, as discussed earlier — for example, to Set<MovieCatalog>. In this case, all matching beans, according to the declared qualifiers, are injected as a collection. 
    - This implies that qualifiers do not have to be unique. 
    - Rather, they constitute filtering criteria. 
    - For example, you can define multiple MovieCatalog beans with the same qualifier value “action”, all of which are injected into a Set<MovieCatalog> annotated with @Qualifier("action").

- Letting qualifier values select against target bean names, within the type-matching candidates, does not require a @Qualifier annotation at the injection point. 

- If there is no other resolution indicator (such as a qualifier or a primary marker), **for a non-unique dependency situation, Spring matches the injection point name (that is, the field name or parameter name)** against the target bean names and choose the same-named candidate, if any.

- For beans that are themselves **defined as a collection, Map, or array type**, @Resource is a fine solution, referring to the specific collection or array bean by unique name. That said, as of 4.3, collection, you can match Map, and array types through Spring’s @Autowired type matching algorithm as well, as long as the element type information is preserved in @Bean return type signatures or collection inheritance hierarchies. In this case, you can use qualifier values to select among same-typed collections, as outlined in the previous paragraph.

- As of 4.3, @Autowired also considers **self references** for injection (that is, references back to the bean that is currently injected). Note that self injection is a fallback. Regular dependencies on other components always have precedence. In that sense, self references do not participate in regular candidate selection and are therefore in particular never primary. On the contrary, they always end up as lowest precedence. In practice, you should use self references as a last resort only (for example, for calling other methods on the same instance through the bean’s transactional proxy). Consider factoring out the affected methods to a separate delegate bean in such a scenario. Alternatively, you can use @Resource, which may obtain a proxy back to the current bean by its unique name.

- Trying to inject the results from @Bean methods on the same configuration class is effectively a self-reference scenario as well. Either lazily resolve such references in the method signature where it is actually needed (as opposed to an autowired field in the configuration class) or declare the affected @Bean methods as static, decoupling them from the containing configuration class instance and its lifecycle. Otherwise, such beans are only considered in the fallback phase, with matching beans on other configuration classes selected as primary candidates instead (if available).

- @Autowired applies to fields, constructors, and multi-argument methods, allowing for narrowing through qualifier annotations at the parameter level. In contrast, @Resource is supported only for fields and bean property setter methods with a single argument. As a consequence, you should stick with qualifiers if your injection target is a constructor or a multi-argument method.

- You can create your own custom qualifier annotations. To do so, define an annotation and provide the @Qualifier annotation within your definition, as the following example shows and Then you can provide the custom qualifier on autowired fields and parameters, as the following example shows:
```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Genre {

    String value();
}

public class MovieRecommender {

    @Autowired
    @Genre("Action")
    private MovieCatalog actionCatalog;

    private MovieCatalog comedyCatalog;

    @Autowired
    public void setComedyCatalog(@Genre("Comedy") MovieCatalog comedyCatalog) {
        this.comedyCatalog = comedyCatalog;
    }

    // ...
}
```
- Next, you can provide the information for the candidate bean definitions. You can add <qualifier/> tags as sub-elements of the <bean/> tag and then specify the type and value to match your custom qualifier annotations. The type is matched against the fully-qualified class name of the annotation. Alternately, as a convenience if no risk of conflicting names exists, you can use the short class name. The following example demonstrates both approaches:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="Genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="example.Genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```
- In some cases, using an annotation without a value may suffice. This can be useful when the annotation serves a more generic purpose and can be applied across several different types of dependencies. For example, you may provide an offline catalog that can be searched when no Internet connection is available. First, define the simple annotation, as the following example shows:
```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Offline {

}

public class MovieRecommender {

    @Autowired
    @Offline 
    private MovieCatalog offlineCatalog;

    // ...
}

<bean class="example.SimpleMovieCatalog">
    <qualifier type="Offline"/> 
    <!-- inject any dependencies required by this bean -->
</bean>
```
- You can also define custom qualifier annotations that accept named attributes in addition to or instead of the simple value attribute. If multiple attribute values are then specified on a field or parameter to be autowired, a bean definition must match all such attribute values to be considered an autowire candidate. As an example, consider the following annotation definition:
```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MovieQualifier {

    String genre();

    Format format();
}
```
- In this case Format is an enum, defined as follows:
```
public enum Format {
    VHS, DVD, BLURAY
}
```
- The fields to be autowired are annotated with the custom qualifier and include values for both attributes: genre and format, as the following example shows:
```
public class MovieRecommender {

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Action")
    private MovieCatalog actionVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Comedy")
    private MovieCatalog comedyVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.DVD, genre="Action")
    private MovieCatalog actionDvdCatalog;

    @Autowired
    @MovieQualifier(format=Format.BLURAY, genre="Comedy")
    private MovieCatalog comedyBluRayCatalog;

    // ...
}
```
- Finally, the bean definitions should contain matching qualifier values. This example also demonstrates that you can use bean meta attributes instead of the <qualifier/> elements. If available, the <qualifier/> element and its attributes take precedence, but the autowiring mechanism falls back on the values provided within the <meta/> tags if no such qualifier is present, as in the last two bean definitions in the following example:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Action"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Comedy"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="DVD"/>
        <meta key="genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="BLURAY"/>
        <meta key="genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

</beans>
```

### Using Generics as Autowiring Qualifiers
- In addition to the @Qualifier annotation, you can use Java generic types as an implicit form of qualification. For example, suppose you have the following configuration:
```
@Configuration
public class MyConfiguration {

    @Bean
    public StringStore stringStore() {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore() {
        return new IntegerStore();
    }
}
```
- Assuming that the preceding beans implement a generic interface, (that is, Store<String> and Store<Integer>), you can @Autowire the Store interface and the generic is used as a qualifier, as the following example shows:
```
@Autowired
private Store<String> s1; // <String> qualifier, injects the stringStore bean

@Autowired
private Store<Integer> s2; // <Integer> qualifier, injects the integerStore bean
```
- Generic qualifiers also apply when autowiring lists, Map instances and arrays. The following example autowires a generic List:
```
// Inject all Store beans as long as they have an <Integer> generic
// Store<String> beans will not appear in this list
@Autowired
private List<Store<Integer>> s;
```

### Using CustomAutowireConfigurer
- CustomAutowireConfigurer is a BeanFactoryPostProcessor that lets you register your own custom qualifier annotation types, even if they are not annotated with Spring’s @Qualifier annotation. The following example shows how to use CustomAutowireConfigurer:
```
<bean id="customAutowireConfigurer"
        class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer">
    <property name="customQualifierTypes">
        <set>
            <value>example.CustomQualifier</value>
        </set>
    </property>
</bean>
```
- The AutowireCandidateResolver determines autowire candidates by:
    - The autowire-candidate value of each bean definition
    - Any default-autowire-candidates patterns available on the <beans/> element
    - The presence of @Qualifier annotations and any custom annotations registered with the CustomAutowireConfigurer
- When multiple beans qualify as autowire candidates, the determination of a “primary” is as follows: If exactly one bean definition among the candidates has a primary attribute set to true, it is selected.

### Injection with @Resource
- Spring also supports injection by using the JSR-250 @Resource annotation (javax.annotation.Resource) on fields or bean property setter methods. This is a common pattern in Java EE: for example, in JSF-managed beans and JAX-WS endpoints. Spring supports this pattern for Spring-managed objects as well.

- @Resource takes a name attribute. By default, Spring interprets that value as the bean name to be injected. In other words, it follows by-name semantics, as demonstrated in the following example:
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource(name="myMovieFinder") 
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```
- If no name is explicitly specified, the default name is derived from the field name or setter method. In case of a field, it takes the field name. In case of a setter method, it takes the bean property name. The following example is going to have the bean named movieFinder injected into its setter method:
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```
- The name provided with the annotation is resolved as a bean name by the ApplicationContext of which the CommonAnnotationBeanPostProcessor is aware. The names can be resolved through JNDI if you configure Spring’s SimpleJndiBeanFactory explicitly. However, we recommend that you rely on the default behavior and use Spring’s JNDI lookup capabilities to preserve the level of indirection.
- In the exclusive case of @Resource usage with no explicit name specified, and similar to @Autowired, @Resource finds a primary type match instead of a specific named bean and resolves well known resolvable dependencies: the BeanFactory, ApplicationContext, ResourceLoader, ApplicationEventPublisher, and MessageSource interfaces.
- Thus, in the following example, the customerPreferenceDao field first looks for a bean named "customerPreferenceDao" and then falls back to a primary type match 
for the type CustomerPreferenceDao:
```
public class MovieRecommender {

    @Resource
    private CustomerPreferenceDao customerPreferenceDao;

    @Resource
    private ApplicationContext context; 
    // The context field is injected based on the known resolvable dependency type: ApplicationContext.

    public MovieRecommender() {
    }

    // ...
}
```

### Using @Value
- @Value is typically used to inject externalized properties:
```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name}") String catalog) {
        this.catalog = catalog;
    }
}
```
- With the following configuration:
```
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig { }
```
- And the following application.properties file:
```
catalog.name=MovieCatalog
```
- In that case, the catalog parameter and field will be equal to the MovieCatalog value.

- A default lenient embedded value resolver is provided by Spring. It will try to resolve the property value and if it cannot be resolved, the property name (for example ${catalog.name}) will be injected as the value. If you want to maintain strict control over nonexistent values, you should declare a PropertySourcesPlaceholderConfigurer bean, as the following example shows:
```
@Configuration
public class AppConfig {

     @Bean
     public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
           return new PropertySourcesPlaceholderConfigurer();
     }
}
```
- When configuring a PropertySourcesPlaceholderConfigurer using JavaConfig, the @Bean method must be static.
- Using the above configuration ensures Spring initialization failure if any ${} placeholder could not be resolved. It is also possible to use methods like setPlaceholderPrefix, setPlaceholderSuffix, or setValueSeparator to customize placeholders.
- Spring Boot configures by default a PropertySourcesPlaceholderConfigurer bean that will get properties from application.properties and application.yml files.
- Built-in converter support provided by Spring allows simple type conversion (to Integer or int for example) to be automatically handled. Multiple comma-separated values can be automatically converted to String array without extra effort.

- It is possible to provide a default value as following:
```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name:defaultCatalog}") String catalog) {
        this.catalog = catalog;
    }
}
```
- A Spring BeanPostProcessor uses a ConversionService behind the scene to handle the process for converting the String value in @Value to the target type. If you want to provide conversion support for your own custom type, you can provide your own ConversionService bean instance as the following example shows:
```
@Configuration
public class AppConfig {

    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new MyCustomConverter());
        return conversionService;
    }
}
```
- When @Value contains a [SpEL](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#expressions) expression the value will be dynamically computed at runtime as the following example shows:
```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("#{systemProperties['user.catalog'] + 'Catalog' }") String catalog) {
        this.catalog = catalog;
    }
}
```
- SpEL also enables the use of more complex data structures:
```
@Component
public class MovieRecommender {

    private final Map<String, Integer> countOfMoviesPerCatalog;

    public MovieRecommender(
            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;
    }
}
```

### Using @PostConstruct and @PreDestroy
- The CommonAnnotationBeanPostProcessor not only recognizes the @Resource annotation but also the JSR-250 lifecycle annotations: javax.annotation.PostConstruct and javax.annotation.PreDestroy. Introduced in Spring 2.5, the support for these annotations offers an alternative to the lifecycle callback mechanism described in initialization callbacks and destruction callbacks. Provided that the CommonAnnotationBeanPostProcessor is registered within the Spring ApplicationContext, a method carrying one of these annotations is invoked at the same point in the lifecycle as the corresponding Spring lifecycle interface method or explicitly declared callback method. In the following example, the cache is pre-populated upon initialization and cleared upon destruction:
```
public class CachingMovieLister {

    @PostConstruct
    public void populateMovieCache() {
        // populates the movie cache upon initialization...
    }

    @PreDestroy
    public void clearMovieCache() {
        // clears the movie cache upon destruction...
    }
}
```
- For details about the effects of combining various lifecycle mechanisms, see [Combining Lifecycle Mechanisms](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-factory-lifecycle-combined-effects).

- Like @Resource, the @PostConstruct and @PreDestroy annotation types were a part of the standard Java libraries from JDK 6 to 8. However, the entire javax.annotation package got separated from the core Java modules in JDK 9 and eventually removed in JDK 11. If needed, the javax.annotation-api artifact needs to be obtained via Maven Central now, simply to be added to the application’s classpath like any other library.

### Classpath Scanning and Managed Components
- Most examples in this chapter use XML to specify the configuration metadata that produces each BeanDefinition within the Spring container. The previous section (Annotation-based Container Configuration) demonstrates how to provide a lot of the configuration metadata through source-level annotations. Even in those examples, however, the “base” bean definitions are explicitly defined in the XML file, while the annotations drive only the dependency injection. This section describes an option for implicitly detecting the candidate components by scanning the classpath. **Candidate components are classes that match against a filter criteria and have a corresponding bean definition registered with the container**. This removes the need to use XML to perform bean registration. Instead, you can use annotations (for example, @Component), AspectJ type expressions, or your own custom filter criteria to select which classes have bean definitions registered with the container.

- @Component and Further Stereotype Annotations
    - The @Repository annotation is a marker for any class that fulfills the role or stereotype of a repository (also known as Data Access Object or DAO). Among the uses of this marker is the automatic translation of exceptions, as described in Exception Translation.
    - Spring provides further stereotype annotations: @Component, @Service, and @Controller. **@Component is a generic stereotype for any Spring-managed component. @Repository, @Service, and @Controller are specializations of @Component for more specific use cases (in the persistence, service, and presentation layers, respectively)**. Therefore, you can annotate your component classes with @Component, but, by annotating them with @Repository, @Service, or @Controller instead, your classes are more properly suited for processing by tools or associating with aspects. For example, these stereotype annotations make ideal targets for pointcuts. @Repository, @Service, and @Controller can also carry additional semantics in future releases of the Spring Framework. Thus, if you are choosing between using @Component or @Service for your service layer, @Service is clearly the better choice. Similarly, as stated earlier, @Repository is already supported as a marker for automatic exception translation in your persistence layer.

- Using Meta-annotations and Composed Annotations
    - Many of the annotations provided by Spring can be used as meta-annotations in your own code. A meta-annotation is an annotation that can be applied to another annotation. For example, the @Service annotation mentioned earlier is meta-annotated with @Component, as the following example shows:
    ```
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component // The Component causes @Service to be treated in the same way as @Component.
    public @interface Service {

        // ...
    }
    ```
    - You can also combine meta-annotations to create “composed annotations”. For example, the @RestController annotation from Spring MVC is composed of @Controller and @ResponseBody.

    - In addition, composed annotations can optionally redeclare attributes from meta-annotations to allow customization. This can be particularly useful when you want to only expose a subset of the meta-annotation’s attributes. For example, Spring’s @SessionScope annotation hardcodes the scope name to session but still allows customization of the proxyMode. The following listing shows the definition of the SessionScope annotation:
    ```
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Scope(WebApplicationContext.SCOPE_SESSION)
    public @interface SessionScope {

        /**
        * Alias for {@link Scope#proxyMode}.
        * <p>Defaults to {@link ScopedProxyMode#TARGET_CLASS}.
        */
        @AliasFor(annotation = Scope.class)
        ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

    }
    ```
    - You can then use @SessionScope without declaring the proxyMode as follows:
    ```
    @Service
    @SessionScope
    public class SessionScopedService {
        // ...
    }
    ```
    - You can also override the value for the proxyMode, as the following example shows:
    ```
    @Service
    @SessionScope(proxyMode = ScopedProxyMode.INTERFACES)
    public class SessionScopedUserService implements UserService {
        // ...
    }
    ```
    - For further details, see the [Spring Annotation Programming Model](https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model) wiki page.

- Automatically Detecting Classes and Registering Bean Definitions
    - Spring can **automatically detect stereotyped classes** and register corresponding BeanDefinition instances with the ApplicationContext. For example, the following two classes are eligible for such autodetection:
    ```
    @Service
    public class SimpleMovieLister {

        private MovieFinder movieFinder;

        public SimpleMovieLister(MovieFinder movieFinder) {
            this.movieFinder = movieFinder;
        }
    }
    @Repository
    public class JpaMovieFinder implements MovieFinder {
        // implementation elided for clarity
    }
    ```
    - To **autodetect** these classes and register the corresponding beans, you **need to add @ComponentScan to your @Configuration class**, where the **basePackages attribute is a common parent package for the two classes**. (Alternatively, you can specify a comma- or semicolon- or space-separated list that includes the parent package of each class.)
    ```
    @Configuration
    @ComponentScan(basePackages = "org.example") // could have used the value attribute - @ComponentScan("org.example"))
    public class AppConfig  {
        // ...
    }
    ```
    - The following alternative uses XML:
    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
            https://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/context
            https://www.springframework.org/schema/context/spring-context.xsd">

        <context:component-scan base-package="org.example"/>

    </beans>
    ```
    - The use of <context:component-scan> implicitly enables the functionality of <context:annotation-config>. There is usually no need to include the <context:annotation-config> element when using <context:component-scan>.
    - Furthermore, the AutowiredAnnotationBeanPostProcessor and CommonAnnotationBeanPostProcessor are both implicitly included when you use the component-scan element. That means that the two components are autodetected and wired together — all without any bean configuration metadata provided in XML.
    - You can disable the registration of AutowiredAnnotationBeanPostProcessor and CommonAnnotationBeanPostProcessor by including the annotation-config attribute with a value of false.

- Using Filters to Customize Scanning
    - By default, classes annotated with @Component, @Repository, @Service, @Controller, @Configuration, or a custom annotation that itself is annotated with @Component are the only detected candidate components. However, you can modify and extend this behavior by applying custom filters. Add them as includeFilters or excludeFilters attributes of the @ComponentScan annotation (or as <context:include-filter /> or <context:exclude-filter /> child elements of the <context:component-scan> element in XML configuration). Each filter element requires the type and expression attributes. The following table describes the filtering options:

    |Filter Type	| Example Expression	| Description|
    |----           |----                   |----        |
    |annotation (default)|org.example.SomeAnnotation|An annotation to be present or meta-present at the type level in target components.|
    |assignable|org.example.SomeClass|A class (or interface) that the target components are assignable to (extend or implement).|
    |aspectj|org.example..*Service+|An AspectJ type expression to be matched by the target components.|
    |regex|org\.example\.Default.*|A regex expression to be matched by the target components' class names.|
    |custom|org.example.MyTypeFilter|A custom implementation of the org.springframework.core.type.TypeFilter interface.|

    - The following example shows the configuration ignoring all @Repository annotations and using “stub” repositories instead:
    ```
    @Configuration
    @ComponentScan(basePackages = "org.example",
            includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Stub.*Repository"),
            excludeFilters = @Filter(Repository.class))
    public class AppConfig {
        ...
    }
    ```
    - The following listing shows the equivalent XML:
    ```
    <beans>
    <context:component-scan base-package="org.example">
        <context:include-filter type="regex"
                expression=".*Stub.*Repository"/>
        <context:exclude-filter type="annotation"
                expression="org.springframework.stereotype.Repository"/>
    </context:component-scan>
    </beans>
    ```
    - You can also disable the default filters by setting useDefaultFilters=false on the annotation or by providing use-default-filters="false" as an attribute of the <component-scan/> element. This effectively disables automatic detection of classes annotated or meta-annotated with @Component, @Repository, @Service, @Controller, @RestController, or @Configuration.

- Defining Bean Metadata within Components
    - Spring components can also contribute bean definition metadata to the container. You can do this with the same @Bean annotation used to define bean metadata within @Configuration annotated classes. The following example shows how to do so:
    ```
    @Component
    public class FactoryMethodComponent {

        @Bean
        @Qualifier("public")
        public TestBean publicInstance() {
            return new TestBean("publicInstance");
        }

        public void doWork() {
            // Component method implementation omitted
        }
    }
    ```
    - The preceding class is a Spring component that has application-specific code in its doWork() method. However, it also contributes a bean definition that has a factory method referring to the method publicInstance(). **The @Bean annotation identifies the factory method and other bean definition properties, such as a qualifier value through the @Qualifier annotation.** Other method-level annotations that can be specified are @Scope, @Lazy, and custom qualifier annotations.
    - In addition to its role for component initialization, you can also place the @Lazy annotation on injection points marked with @Autowired or @Inject. In this context, it leads to the injection of a lazy-resolution proxy.
    - Autowired fields and methods are supported, as previously discussed, with additional support for autowiring of @Bean methods. The following example shows how to do so:
    ```
    @Component
    public class FactoryMethodComponent {

        private static int i;

        @Bean
        @Qualifier("public")
        public TestBean publicInstance() {
            return new TestBean("publicInstance");
        }

        // use of a custom qualifier and autowiring of method parameters
        @Bean
        protected TestBean protectedInstance(
                @Qualifier("public") TestBean spouse,
                @Value("#{privateInstance.age}") String country) {
            TestBean tb = new TestBean("protectedInstance", 1);
            tb.setSpouse(spouse);
            tb.setCountry(country);
            return tb;
        }

        @Bean
        private TestBean privateInstance() {
            return new TestBean("privateInstance", i++);
        }

        @Bean
        @RequestScope
        public TestBean requestScopedInstance() {
            return new TestBean("requestScopedInstance", 3);
        }
    }
    ```
    - The example autowires the String method parameter country to the value of the age property on another bean named privateInstance. A Spring Expression Language element defines the value of the property through the notation #{ <expression> }. For @Value annotations, an expression resolver is preconfigured to look for bean names when resolving expression text.

    - As of Spring Framework 4.3, you may also declare a **factory method parameter of type InjectionPoint** (or its more specific subclass: DependencyDescriptor) to access the requesting injection point that triggers **the creation of the current bean**. Note that this applies only to the actual creation of bean instances, not to the injection of existing instances. As a consequence, this feature makes most sense for beans of prototype scope. For other scopes, the factory method only ever sees the injection point that triggered the creation of a new bean instance in the given scope (for example, the dependency that triggered the creation of a lazy singleton bean). You can use the provided injection point metadata with semantic care in such scenarios. The following example shows how to use InjectionPoint:
    ```
    @Component
    public class FactoryMethodComponent {

        @Bean @Scope("prototype")
        public TestBean prototypeInstance(InjectionPoint injectionPoint) {
            return new TestBean("prototypeInstance for " + injectionPoint.getMember());
        }
    }
    ```
    - The @Bean methods in a regular Spring component are processed differently than their counterparts inside a Spring @Configuration class. The difference is that @Component classes are not enhanced with CGLIB to intercept the invocation of methods and fields. CGLIB proxying is the means by which invoking methods or fields within @Bean methods in @Configuration classes creates bean metadata references to collaborating objects. Such methods are not invoked with normal Java semantics but rather go through the container in order to provide the usual lifecycle management and proxying of Spring beans, even when referring to other beans through programmatic calls to @Bean methods. In contrast, invoking a method or field in a @Bean method within a plain @Component class has standard Java semantics, with no special CGLIB processing or other constraints applying.

### Naming Autodetected Components
- When a component is autodetected as part of the scanning process, its bean name is generated by the BeanNameGenerator strategy known to that scanner. By default, any Spring stereotype annotation (@Component, @Repository, @Service, and @Controller) that contains a name value thereby provides that name to the corresponding bean definition.

- If such an annotation contains no name value or for any other detected component (such as those discovered by custom filters), the default bean name generator returns the **uncapitalized** non-qualified class name. For example, if the following component classes were detected, the names would be myMovieLister and movieFinderImpl:
```
@Service("myMovieLister")
public class SimpleMovieLister {
    // ...
}

@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

- If you do not want to rely on the default bean-naming strategy, you can provide a custom bean-naming strategy. First, implement the BeanNameGenerator interface, and be sure to include a default no-arg constructor. Then, provide the fully qualified class name when configuring the scanner, as the following example annotation and bean definition show.
```
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
    // ...
}

<beans>
    <context:component-scan base-package="org.example"
        name-generator="org.example.MyNameGenerator" />
</beans>
```
- If you run into naming conflicts due to multiple autodetected components having the same non-qualified class name (i.e., classes with identical names but residing in different packages), you may need to configure a BeanNameGenerator that defaults to the fully qualified class name for the generated bean name. As of Spring Framework 5.2.3, the FullyQualifiedAnnotationBeanNameGenerator located in package org.springframework.context.annotation can be used for such purposes.

### Providing a Scope for Autodetected Components
- As with Spring-managed components in general, the **default** and most common scope for autodetected components is **singleton**. However, sometimes you need a different scope that can be specified by the @Scope annotation. You can provide the name of the scope within the annotation, as the following example shows:
```
@Scope("prototype")
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```
- @Scope annotations are only introspected on the concrete bean class (for annotated components) or the factory method (for @Bean methods). In contrast to XML bean definitions, there is no notion of bean definition inheritance, and inheritance hierarchies at the class level are irrelevant for metadata purposes.

- For details on web-specific scopes such as “request” or “session” in a Spring context, see Request, Session, Application, and WebSocket Scopes. As with the pre-built annotations for those scopes, you may also compose your own scoping annotations by using Spring’s meta-annotation approach: for example, a custom annotation meta-annotated with @Scope("prototype"), possibly also declaring a custom scoped-proxy mode.

- To provide a custom strategy for scope resolution rather than relying on the annotation-based approach, you can implement the ScopeMetadataResolver interface. Be sure to include a default no-arg constructor. Then you can provide the fully qualified class name when configuring the scanner, as the following example of both an annotation and a bean definition shows:
```
@Configuration
@ComponentScan(basePackages = "org.example", scopeResolver = MyScopeResolver.class)
public class AppConfig {
    // ...
}

<beans>
    <context:component-scan base-package="org.example" scope-resolver="org.example.MyScopeResolver"/>
</beans>
```
- When using certain non-singleton scopes, it may be necessary to generate proxies for the scoped objects. The reasoning is described in [Scoped Beans as Dependencies](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-factory-scopes-other-injection). For this purpose, a scoped-proxy attribute is available on the component-scan element. The three possible values are: no, interfaces, and targetClass. For example, the following configuration results in standard JDK dynamic proxies:
```
@Configuration
@ComponentScan(basePackages = "org.example", scopedProxy = ScopedProxyMode.INTERFACES)
public class AppConfig {
    // ...
}

<beans>
    <context:component-scan base-package="org.example" scoped-proxy="interfaces"/>
</beans>
```

### Providing Qualifier Metadata with Annotations
- The @Qualifier annotation is discussed in Fine-tuning Annotation-based Autowiring with Qualifiers. The examples in that section demonstrate the use of the @Qualifier annotation and custom qualifier annotations to provide fine-grained control when you resolve autowire candidates. Because those examples were based on XML bean definitions, the qualifier metadata was provided on the candidate bean definitions by using the qualifier or meta child elements of the bean element in the XML. When relying upon classpath scanning for auto-detection of components, you can provide the qualifier metadata with type-level annotations on the candidate class. The following three examples demonstrate this technique:
```
@Component
@Qualifier("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}

@Component
@Genre("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}

@Component
@Offline
public class CachingMovieCatalog implements MovieCatalog {
    // ...
}
```
- As with most annotation-based alternatives, keep in mind that the annotation metadata is bound to the class definition itself, while the use of XML allows for multiple beans of the same type to provide variations in their qualifier metadata, because that metadata is provided per-instance rather than per-class.

### Generating an Index of Candidate Components

- While classpath scanning is very fast, it is possible to improve the startup performance of large applications by creating a static list of candidates at compilation time. In this mode, all modules that are target of component scan must use this mechanism.
```
Your existing @ComponentScan or <context:component-scan directives must stay as is to request the context to scan candidates in certain packages. When the ApplicationContext detects such an index, it automatically uses it rather than scanning the classpath.
```
- To generate the index, add an additional dependency to each module that contains components that are targets for component scan directives. The following example shows how to do so with Maven:
```
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context-indexer</artifactId>
        <version>5.2.9.RELEASE</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```
- With Gradle 4.5 and earlier, the dependency should be declared in the compileOnly configuration, as shown in the following example:
```
dependencies {
    annotationProcessor "org.springframework:spring-context-indexer:{spring-version}"
}
```
- That process generates a META-INF/spring.components file that is included in the jar file.
- When working with this mode in your IDE, the spring-context-indexer must be registered as an annotation processor to make sure the index is up-to-date when candidate components are updated.
- The index is enabled automatically when a META-INF/spring.components is found on the classpath. If an index is partially available for some libraries (or use cases) but could not be built for the whole application, you can fallback to a regular classpath arrangement (as though no index was present at all) by setting spring.index.ignore to true, either as a system property or in a spring.properties file at the root of the classpath.

### Java-based Container Configuration

- This section covers how to use annotations in your Java code to configure the Spring container. It includes the following topics:
    - Basic Concepts: @Bean and @Configuration
    - Instantiating the Spring Container by Using AnnotationConfigApplicationContext
    - Using the @Bean Annotation
    - Using the @Configuration annotation
    - Composing Java-based Configurations
    - Bean Definition Profiles
    - PropertySource Abstraction
    - Using @PropertySource
    - Placeholder Resolution in Statements

- Basic Concepts: @Bean and @Configuration
    - The central artifacts in Spring’s new Java-configuration support are **@Configuration-annotated classes** and **@Bean-annotated methods**.

    - The @Bean annotation is used to indicate that a method instantiates, configures, and initializes a new object to be managed by the Spring IoC container. For those familiar with Spring’s <beans/> XML configuration, the @Bean annotation plays the same role as the <bean/> element. You can use @Bean-annotated methods with any Spring @Component. However, they are most often used with @Configuration beans.

    - Annotating a class with **@Configuration indicates that its primary purpose is as a source of bean definitions**. Furthermore, @Configuration classes let inter-bean dependencies be defined by calling other @Bean methods in the same class. The simplest possible @Configuration class reads as follows:
    ```
    @Configuration
    public class AppConfig {

        @Bean
        public MyService myService() {
            return new MyServiceImpl();
        }
    }
    ```

    - The preceding AppConfig class is equivalent to the following Spring <beans/> XML:
    ```
    <beans>
    <bean id="myService" class="com.acme.services.MyServiceImpl"/>
    </beans>
    ```

    - Full @Configuration vs “lite” @Bean mode?
        - When @Bean methods are declared within classes that are **not annotated with @Configuration**, they are referred to as being processed in a “lite” mode. **Bean methods declared in a @Component or even in a plain old class are considered to be “lite”**, with a different primary purpose of the containing class and a @Bean method being a sort of bonus there. For example, service components may expose management views to the container through an additional @Bean method on each applicable component class. In such scenarios, @Bean methods are a general-purpose factory method mechanism.

        - Unlike full @Configuration, **lite @Bean methods cannot declare inter-bean dependencies**. Instead, they operate on their containing component’s internal state and, optionally, on arguments that they may declare. Such a @Bean method should therefore not invoke other @Bean methods. Each such method is literally only a factory method for a particular bean reference, without any special runtime semantics. The positive side-effect here is that no CGLIB subclassing has to be applied at runtime, so there are no limitations in terms of class design (that is, the containing class may be final and so forth).

        - **In common scenarios, @Bean methods are to be declared within @Configuration classes**, ensuring that “full” mode is always used and that cross-method references therefore get redirected to the container’s lifecycle management. This prevents the same @Bean method from accidentally being invoked through a regular Java call, which helps to reduce subtle bugs that can be hard to track down when operating in “lite” mode.

### Instantiating the Spring Container by Using AnnotationConfigApplicationContext
- The following sections document Spring’s AnnotationConfigApplicationContext, introduced in Spring 3.0. This versatile ApplicationContext implementation is capable of accepting not only @Configuration classes as input but also plain @Component classes and classes annotated with JSR-330 metadata.

- When @Configuration classes are provided as input, the @Configuration class itself is registered as a bean definition and all declared @Bean methods within the class are also registered as bean definitions.

- When @Component and JSR-330 classes are provided, they are registered as bean definitions, and it is assumed that DI metadata such as @Autowired or @Inject are used within those classes where necessary.

- Simple Construction
    - In much the same way that Spring XML files are used as input when instantiating a ClassPathXmlApplicationContext, you can use @Configuration classes as input when instantiating an AnnotationConfigApplicationContext. This allows for completely XML-free usage of the Spring container, as the following example shows:
    ```
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        MyService myService = ctx.getBean(MyService.class);
        myService.doStuff();
    }
    ```
    - As mentioned earlier, AnnotationConfigApplicationContext is not limited to working only with @Configuration classes. Any @Component or JSR-330 annotated class may be supplied as input to the constructor, as the following example shows:
    ```
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(MyServiceImpl.class, Dependency1.class, Dependency2.class);
        MyService myService = ctx.getBean(MyService.class);
        myService.doStuff();
    }
    ```
    - The preceding example assumes that MyServiceImpl, Dependency1, and Dependency2 use Spring dependency injection annotations such as @Autowired.

- Building the Container Programmatically by Using register(Class<?>…​)
    - You can instantiate an AnnotationConfigApplicationContext by using a no-arg constructor and then configure it by using the register() method. This approach is particularly useful when programmatically building an AnnotationConfigApplicationContext. The following example shows how to do so:
    ```
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class, OtherConfig.class);
        ctx.register(AdditionalConfig.class);
        ctx.refresh();
        MyService myService = ctx.getBean(MyService.class);
        myService.doStuff();
    }
    ```
- Enabling Component Scanning with scan(String…​)
    - To enable component scanning, you can annotate your @Configuration class as follows:
    ```
    @Configuration
    @ComponentScan(basePackages = "com.acme") // This annotation enables component scanning.

    public class AppConfig  {
        ...
    }
    ```
    - Experienced Spring users may be familiar with the XML declaration equivalent from Spring’s context: namespace, shown in the following example:
    ```
    <beans>
        <context:component-scan base-package="com.acme"/>
    </beans>
    ```
    - In the preceding example, the com.acme package is scanned to look for any @Component-annotated classes, and those classes are registered as Spring bean definitions within the container. 
    
    - AnnotationConfigApplicationContext exposes the scan(String…​) method to allow for the same component-scanning functionality, as the following example shows:
    ```
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("com.acme");
        ctx.refresh();
        MyService myService = ctx.getBean(MyService.class);
    }
    ```
    - Remember that @Configuration classes are meta-annotated with @Component, so they are candidates for component-scanning. In the preceding example, assuming that AppConfig is declared within the com.acme package (or any package underneath), it is picked up during the call to scan(). Upon refresh(), all its @Bean methods are processed and registered as bean definitions within the container.

- Support for Web Applications with AnnotationConfigWebApplicationContext
    - A WebApplicationContext variant of AnnotationConfigApplicationContext is available with AnnotationConfigWebApplicationContext. You can use this implementation when configuring the Spring ContextLoaderListener servlet listener, Spring MVC DispatcherServlet, and so forth. The following web.xml snippet configures a typical Spring MVC web application (note the use of the contextClass context-param and init-param):
    ```
    <web-app>
        <!-- Configure ContextLoaderListener to use AnnotationConfigWebApplicationContext
            instead of the default XmlWebApplicationContext -->
        <context-param>
            <param-name>contextClass</param-name>
            <param-value>
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
        </context-param>

        <!-- Configuration locations must consist of one or more comma- or space-delimited
            fully-qualified @Configuration classes. Fully-qualified packages may also be
            specified for component-scanning -->
        <context-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>com.acme.AppConfig</param-value>
        </context-param>

        <!-- Bootstrap the root application context as usual using ContextLoaderListener -->
        <listener>
            <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
        </listener>

        <!-- Declare a Spring MVC DispatcherServlet as usual -->
        <servlet>
            <servlet-name>dispatcher</servlet-name>
            <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
            <!-- Configure DispatcherServlet to use AnnotationConfigWebApplicationContext
                instead of the default XmlWebApplicationContext -->
            <init-param>
                <param-name>contextClass</param-name>
                <param-value>
                    org.springframework.web.context.support.AnnotationConfigWebApplicationContext
                </param-value>
            </init-param>
            <!-- Again, config locations must consist of one or more comma- or space-delimited
                and fully-qualified @Configuration classes -->
            <init-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>com.acme.web.MvcConfig</param-value>
            </init-param>
        </servlet>

        <!-- map all requests for /app/* to the dispatcher servlet -->
        <servlet-mapping>
            <servlet-name>dispatcher</servlet-name>
            <url-pattern>/app/*</url-pattern>
        </servlet-mapping>
    </web-app>
    ```

### Using the @Bean Annotation

- @Bean is a method-level annotation and a direct analog of the XML <bean/> element. The annotation supports some of the attributes offered by <bean/>, such as: * init-method * destroy-method * autowiring * name.

- You can use the @Bean annotation in a @Configuration-annotated or in a @Component-annotated class.

- Declaring a Bean
    - To declare a bean, you can annotate a method with the @Bean annotation. You use this method to register a bean definition within an ApplicationContext of the type specified as the method’s return value. By default, the bean name is the same as the method name. The following example shows a @Bean method declaration:
    ```
    @Configuration
    public class AppConfig {

        @Bean
        public TransferServiceImpl transferService() {
            return new TransferServiceImpl();
        }
    }
    ```
    - The preceding configuration is exactly equivalent to the following Spring XML:
    ```
    <beans>
        <bean id="transferService" class="com.acme.TransferServiceImpl"/>
    </beans>
    ```
    - Both declarations make a bean named transferService available in the ApplicationContext, bound to an object instance of type TransferServiceImpl, as the following text image shows:
    ```
    transferService -> com.acme.TransferServiceImpl
    ```
    - You can also declare your @Bean method with an interface (or base class) return type, as the following example shows:
    ```
    @Configuration
    public class AppConfig {

        @Bean
        public TransferService transferService() {
            return new TransferServiceImpl();
        }
    }
    ```
    - However, this limits the visibility for advance type prediction to the specified interface type (TransferService). Then, with the full type (TransferServiceImpl) known to the container only once, the affected singleton bean has been instantiated. Non-lazy singleton beans get instantiated according to their declaration order, so you may see different type matching results depending on when another component tries to match by a non-declared type (such as @Autowired TransferServiceImpl, which resolves only once the transferService bean has been instantiated).
    
    - If you consistently refer to your types by a declared service interface, your @Bean return types may safely join that design decision. However, for components that implement several interfaces or for components potentially referred to by their implementation type, it is safer to declare the most specific return type possible (at least as specific as required by the injection points that refer to your bean).

- Bean Dependencies
    - A @Bean-annotated method can have an arbitrary number of parameters that describe the dependencies required to build that bean. For instance, if our TransferService requires an AccountRepository, we can materialize that dependency with a method parameter, as the following example shows:
    ```
    @Configuration
    public class AppConfig {

        @Bean
        public TransferService transferService(AccountRepository accountRepository) {
            return new TransferServiceImpl(accountRepository);
        }
    }
    ```
    - The resolution mechanism is pretty much identical to constructor-based dependency injection. See the relevant section for more details.

- Receiving Lifecycle Callbacks
    - Any classes defined with the @Bean annotation support the regular lifecycle callbacks and can use the @PostConstruct and @PreDestroy annotations from JSR-250. See JSR-250 annotations for further details.

    - The regular Spring lifecycle callbacks are fully supported as well. If a bean implements InitializingBean, DisposableBean, or Lifecycle, their respective methods are called by the container.

    - The standard set of *Aware interfaces (such as BeanFactoryAware, BeanNameAware, MessageSourceAware, ApplicationContextAware, and so on) are also fully supported.

    - The @Bean annotation supports specifying arbitrary initialization and destruction callback methods, much like Spring XML’s init-method and destroy-method attributes on the bean element, as the following example shows:
    ```
    public class BeanOne {

    public void init() {
        // initialization logic
        }
    }

    public class BeanTwo {

        public void cleanup() {
            // destruction logic
        }
    }

    @Configuration
    public class AppConfig {

        @Bean(initMethod = "init")
        public BeanOne beanOne() {
            return new BeanOne();
        }

        @Bean(destroyMethod = "cleanup")
        public BeanTwo beanTwo() {
            return new BeanTwo();
        }
    }
    ```
    - By default, beans defined with Java configuration that have a public close or shutdown method are automatically enlisted with a destruction callback. If you have a public close or shutdown method and you do not wish for it to be called when the container shuts down, you can add @Bean(destroyMethod="") to your bean definition to disable the default (inferred) mode.

    - You may want to do that by default for a resource that you acquire with JNDI, as its lifecycle is managed outside the application. In particular, make sure to always do it for a DataSource, as it is known to be problematic on Java EE application servers.

    - The following example shows how to prevent an automatic destruction callback for a DataSource:
    ```
    @Bean(destroyMethod="")
        public DataSource dataSource() throws NamingException {
        return (DataSource) jndiTemplate.lookup("MyDS");
    }
    ```

    - Also, with @Bean methods, you typically use programmatic JNDI lookups, either by using Spring’s JndiTemplate or JndiLocatorDelegate helpers or straight JNDI InitialContext usage but not the JndiObjectFactoryBean variant (which would force you to declare the return type as the FactoryBean type instead of the actual target type, making it harder to use for cross-reference calls in other @Bean methods that intend to refer to the provided resource here).

    - In the case of BeanOne from the example above the preceding note, it would be equally valid to call the init() method directly during construction, as the following example shows:
    ```
    @Configuration
    public class AppConfig {

        @Bean
        public BeanOne beanOne() {
            BeanOne beanOne = new BeanOne();
            beanOne.init();
            return beanOne;
        }

        // ...
    }
    ```

    - When you work directly in Java, you can do anything you like with your objects and do not always need to rely on the container lifecycle.

- Specifying Bean Scope
    - Spring includes the @Scope annotation so that you can specify the scope of a bean.

- Using the @Scope Annotation
    - You can specify that your beans defined with the @Bean annotation should have a specific scope. You can use any of the standard scopes specified in the Bean Scopes section.

    - The default scope is singleton, but you can override this with the @Scope annotation, as the following example shows:
    ```
    @Configuration
    public class MyConfiguration {

        @Bean
        @Scope("prototype")
        public Encryptor encryptor() {
            // ...
        }
    }
    ```

    - @Scope and scoped-proxy
        - Spring offers a convenient way of working with scoped dependencies through scoped proxies. The easiest way to create such a proxy when using the XML configuration is the <aop:scoped-proxy/> element. Configuring your beans in Java with a @Scope annotation offers equivalent support with the proxyMode attribute. The default is no proxy (ScopedProxyMode.NO), but you can specify ScopedProxyMode.TARGET_CLASS or ScopedProxyMode.INTERFACES.

        - If you port the scoped proxy example from the XML reference documentation (see scoped proxies) to our @Bean using Java, it resembles the following:
        ```
        // an HTTP Session-scoped bean exposed as a proxy
        @Bean
        @SessionScope
        public UserPreferences userPreferences() {
            return new UserPreferences();
        }

        @Bean
        public Service userService() {
            UserService service = new SimpleUserService();
            // a reference to the proxied userPreferences bean
            service.setUserPreferences(userPreferences());
            return service;
        }
        ```
    - Customizing Bean Naming
        - By default, configuration classes use a @Bean method’s name as the name of the resulting bean. This functionality can be overridden, however, with the name attribute, as the following example shows:
        ```
        @Configuration
        public class AppConfig {

            @Bean(name = "myThing")
            public Thing thing() {
                return new Thing();
            }
        }
        ```
    - Bean Aliasing
        - As discussed in Naming Beans, it is sometimes desirable to give a single bean **multiple names**, otherwise known as bean aliasing. The name attribute of the @Bean annotation accepts a String array for this purpose. The following example shows how to set a number of aliases for a bean:
        ```
        @Configuration
        public class AppConfig {

            @Bean({"dataSource", "subsystemA-dataSource", "subsystemB-dataSource"})
            public DataSource dataSource() {
                // instantiate, configure and return DataSource bean...
            }
        }
        ```
    - Bean Description
        - Sometimes, it is helpful to provide a more detailed textual description of a bean. This can be particularly useful when beans are exposed (perhaps through JMX) for monitoring purposes.

        - To add a description to a @Bean, you can use the @Description annotation, as the following example shows:
        ```
        @Configuration
        public class AppConfig {

            @Bean
            @Description("Provides a basic example of a bean")
            public Thing thing() {
                return new Thing();
            }
        }
        ```
- Using the @Configuration annotation
    - @Configuration is a class-level annotation indicating that an object is a source of bean definitions. @Configuration classes declare beans through public @Bean annotated methods. Calls to @Bean methods on @Configuration classes can also be used to define inter-bean dependencies. See Basic Concepts: [@Bean and @Configuration](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-java-basic-concepts) for a general introduction.

    - Injecting Inter-bean Dependencies
        - When beans have dependencies on one another, expressing that dependency is as simple as having one bean method call another, as the following example shows:
        ```
        @Configuration
        public class AppConfig {

            @Bean
            public BeanOne beanOne() {
                return new BeanOne(beanTwo());
            }

            @Bean
            public BeanTwo beanTwo() {
                return new BeanTwo();
            }
        }
        ```
        - In the preceding example, beanOne receives a reference to beanTwo through constructor injection.
        - This method of declaring inter-bean dependencies **works only when the @Bean method is declared within a @Configuration class**. You **cannot declare inter-bean dependencies by using plain @Component classes**.
    - Lookup Method Injection
        - As noted earlier, lookup method injection is an advanced feature that you should use rarely. It is useful in cases where a singleton-scoped bean has a dependency on a prototype-scoped bean. Using Java for this type of configuration provides a natural means for implementing this pattern. The following example shows how to use lookup method injection:
        ```
        public abstract class CommandManager {
            public Object process(Object commandState) {
                // grab a new instance of the appropriate Command interface
                Command command = createCommand();
                // set the state on the (hopefully brand new) Command instance
                command.setState(commandState);
                return command.execute();
            }

            // okay... but where is the implementation of this method?
            protected abstract Command createCommand();
        }
        ```
        - By using Java configuration, you can create a subclass of CommandManager where the abstract createCommand() method is overridden in such a way that it looks up a new (prototype) command object. The following example shows how to do so:
        ```
        @Bean
        @Scope("prototype")
        public AsyncCommand asyncCommand() {
            AsyncCommand command = new AsyncCommand();
            // inject dependencies here as required
            return command;
        }

        @Bean
        public CommandManager commandManager() {
            // return new anonymous implementation of CommandManager with createCommand()
            // overridden to return a new prototype Command object
            return new CommandManager() {
                protected Command createCommand() {
                    return asyncCommand();
                }
            }
        }
        ```
        - [The prototype scope](https://docs.spring.io/spring-framework/docs/3.0.0.M3/reference/html/ch04s04.html)
            - The non-singleton, prototype scope of bean deployment results in the creation of a new bean instance every time a request for that specific bean is made
    - Further Information About How Java-based Configuration Works Internally
        - Consider the following example, which shows a @Bean annotated method being called twice:
        ```
        @Configuration
        public class AppConfig {

            @Bean
            public ClientService clientService1() {
                ClientServiceImpl clientService = new ClientServiceImpl();
                clientService.setClientDao(clientDao());
                return clientService;
            }

            @Bean
            public ClientService clientService2() {
                ClientServiceImpl clientService = new ClientServiceImpl();
                clientService.setClientDao(clientDao());
                return clientService;
            }

            @Bean
            public ClientDao clientDao() {
                return new ClientDaoImpl();
            }
        }
        ```
        - clientDao() has been called once in clientService1() and once in clientService2(). Since this method creates a new instance of ClientDaoImpl and returns it, you would normally expect to have two instances (one for each service). That definitely would be problematic: In Spring, instantiated beans have a singleton scope by default. This is where the magic comes in: All @Configuration classes are subclassed at startup-time with CGLIB. In the subclass, the child method checks the container first for any cached (scoped) beans before it calls the parent method and creates a new instance.
        - There are a few restrictions due to the fact that CGLIB dynamically adds features at startup-time. In particular, configuration classes must not be final. However, as of 4.3, any constructors are allowed on configuration classes, including the use of @Autowired or a single non-default constructor declaration for default injection.
        - If you prefer to avoid any CGLIB-imposed limitations, consider declaring your @Bean methods on non-@Configuration classes (for example, on plain @Component classes instead). Cross-method calls between @Bean methods are not then intercepted, so you have to exclusively rely on dependency injection at the constructor or method level there.

- Composing Java-based Configurations
    - Spring’s Java-based configuration feature lets you compose annotations, which can reduce the complexity of your configuration.
    - Using the @Import Annotation
        - Much as the <import/> element is used within Spring XML files to aid in modularizing configurations, the @Import annotation allows for loading @Bean definitions from another configuration class, as the following example shows:
        ```
        @Configuration
        public class ConfigA {

            @Bean
            public A a() {
                return new A();
            }
        }

        @Configuration
        @Import(ConfigA.class)
        public class ConfigB {

            @Bean
            public B b() {
                return new B();
            }
        }
        ```
        - Now, rather than needing to specify both ConfigA.class and ConfigB.class when instantiating the context, only ConfigB needs to be supplied explicitly, as the following example shows:
        ```
        public static void main(String[] args) {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigB.class);

            // now both beans A and B will be available...
            A a = ctx.getBean(A.class);
            B b = ctx.getBean(B.class);
        }
        ```
        - This approach simplifies container instantiation, as only one class needs to be dealt with, rather than requiring you to remember a potentially large number of @Configuration classes during construction.
        - As of Spring Framework 4.2, @Import also supports references to regular component classes, analogous to the AnnotationConfigApplicationContext.register method. This is particularly useful if you want to avoid component scanning, by using a few configuration classes as entry points to explicitly define all your components.

- Injecting Dependencies on Imported @Bean Definitions
    - The preceding example works but is simplistic. In most practical scenarios, beans have dependencies on one another across configuration classes. When using XML, this is not an issue, because no compiler is involved, and you can declare ref="someBean" and trust Spring to work it out during container initialization. When using @Configuration classes, the Java compiler places constraints on the configuration model, in that references to other beans must be valid Java syntax.

    - Fortunately, solving this problem is simple. As we already discussed, a @Bean method can have an arbitrary number of parameters that describe the bean dependencies. Consider the following more real-world scenario with several @Configuration classes, each depending on beans declared in the others:
    ```
    @Configuration
    public class ServiceConfig {

        @Bean
        public TransferService transferService(AccountRepository accountRepository) {
            return new TransferServiceImpl(accountRepository);
        }
    }

    @Configuration
    public class RepositoryConfig {

        @Bean
        public AccountRepository accountRepository(DataSource dataSource) {
            return new JdbcAccountRepository(dataSource);
        }
    }

    @Configuration
    @Import({ServiceConfig.class, RepositoryConfig.class})
    public class SystemTestConfig {

        @Bean
        public DataSource dataSource() {
            // return new DataSource
        }
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
        // everything wires up across configuration classes...
        TransferService transferService = ctx.getBean(TransferService.class);
        transferService.transfer(100.00, "A123", "C456");
    }
    ```
    - There is another way to achieve the same result. Remember that @Configuration classes are ultimately only another bean in the container: This means that they can take advantage of @Autowired and @Value injection and other features the same as any other bean.
    - The following example shows how one bean can be autowired to another bean:
    ```
    @Configuration
    public class ServiceConfig {

        @Autowired
        private AccountRepository accountRepository;

        @Bean
        public TransferService transferService() {
            return new TransferServiceImpl(accountRepository);
        }
    }

    @Configuration
    public class RepositoryConfig {

        private final DataSource dataSource;

        public RepositoryConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        public AccountRepository accountRepository() {
            return new JdbcAccountRepository(dataSource);
        }
    }

    @Configuration
    @Import({ServiceConfig.class, RepositoryConfig.class})
    public class SystemTestConfig {

        @Bean
        public DataSource dataSource() {
            // return new DataSource
        }
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
        // everything wires up across configuration classes...
        TransferService transferService = ctx.getBean(TransferService.class);
        transferService.transfer(100.00, "A123", "C456");
    }
    ```
    - Constructor injection in @Configuration classes is only supported as of Spring Framework 4.3. Note also that there is no need to specify @Autowired if the target bean defines only one constructor.

- Fully-qualifying imported beans for ease of navigation
    - In the preceding scenario, using @Autowired works well and provides the desired modularity, but determining exactly where the autowired bean definitions are declared is still somewhat ambiguous. For example, as a developer looking at ServiceConfig, how do you know exactly where the @Autowired AccountRepository bean is declared? It is not explicit in the code, and this may be just fine. Remember that the Spring Tools for Eclipse provides tooling that can render graphs showing how everything is wired, which may be all you need. Also, your Java IDE can easily find all declarations and uses of the AccountRepository type and quickly show you the location of @Bean methods that return that type.

    - In cases where this ambiguity is not acceptable and you wish to have direct navigation from within your IDE from one @Configuration class to another, consider autowiring the configuration classes themselves. The following example shows how to do so:
    ```
    @Configuration
    public class ServiceConfig {

        @Autowired
        private RepositoryConfig repositoryConfig;

        @Bean
        public TransferService transferService() {
            // navigate 'through' the config class to the @Bean method!
            return new TransferServiceImpl(repositoryConfig.accountRepository());
        }
    }
    ```
    - In the preceding situation, where AccountRepository is defined is completely explicit. However, ServiceConfig is now tightly coupled to RepositoryConfig. That is the tradeoff. This tight coupling can be somewhat mitigated by using interface-based or abstract class-based @Configuration classes. Consider the following example:
    ```
    @Configuration
    public class ServiceConfig {

        @Autowired
        private RepositoryConfig repositoryConfig;

        @Bean
        public TransferService transferService() {
            return new TransferServiceImpl(repositoryConfig.accountRepository());
        }
    }

    @Configuration
    public interface RepositoryConfig {

        @Bean
        AccountRepository accountRepository();
    }

    @Configuration
    public class DefaultRepositoryConfig implements RepositoryConfig {

        @Bean
        public AccountRepository accountRepository() {
            return new JdbcAccountRepository(...);
        }
    }

    @Configuration
    @Import({ServiceConfig.class, DefaultRepositoryConfig.class})  // import the concrete config!
    public class SystemTestConfig {

        @Bean
        public DataSource dataSource() {
            // return DataSource
        }

    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
        TransferService transferService = ctx.getBean(TransferService.class);
        transferService.transfer(100.00, "A123", "C456");
    }
    ```
    - Now ServiceConfig is loosely coupled with respect to the concrete DefaultRepositoryConfig, and built-in IDE tooling is still useful: You can easily get a type hierarchy of RepositoryConfig implementations. In this way, navigating @Configuration classes and their dependencies becomes no different than the usual process of navigating interface-based code.


- Conditionally Include @Configuration Classes or @Bean Methods
    - It is often useful to conditionally enable or disable a complete @Configuration class or even individual @Bean methods, based on some arbitrary system state. One common example of this is to **use the @Profile annotation to activate beans only when a specific profile has been enabled** in the Spring Environment (see Bean Definition Profiles for details).

    - The @Profile annotation is actually implemented by using a much more flexible annotation called @Conditional. The @Conditional annotation indicates specific org.springframework.context.annotation.Condition implementations that should be consulted before a @Bean is registered.

    - Implementations of the Condition interface provide a matches(…​) method that returns true or false. For example, the following listing shows the actual Condition implementation used for @Profile:
    ```
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Read the @Profile annotation attributes
        MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
        if (attrs != null) {
            for (Object value : attrs.get("value")) {
                if (context.getEnvironment().acceptsProfiles(((String[]) value))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    ```
    - See the [@Conditional javadoc](https://docs.spring.io/spring-framework/docs/5.2.9.RELEASE/javadoc-api/org/springframework/context/annotation/Conditional.html) for more detail.

- Combining Java and XML Configuration
    - Spring’s @Configuration class support does not aim to be a 100% complete replacement for Spring XML. Some facilities, such as Spring XML namespaces, remain an ideal way to configure the container. In cases where XML is convenient or necessary, you have a choice: either instantiate the container in an “XML-centric” way by using, for example, ClassPathXmlApplicationContext, or instantiate it in a “Java-centric” way by using AnnotationConfigApplicationContext and the @ImportResource annotation to import XML as needed.

    - XML-centric Use of @Configuration Classes
        - It may be preferable to bootstrap the Spring container from XML and include @Configuration classes in an ad-hoc fashion. For example, in a large existing codebase that uses Spring XML, it is easier to create @Configuration classes on an as-needed basis and include them from the existing XML files. Later in this section, we cover the options for using @Configuration classes in this kind of “XML-centric” situation.
        - Declaring @Configuration classes as plain Spring <bean/> elements
            - Remember that @Configuration classes are ultimately bean definitions in the container. In this series examples, we create a @Configuration class named AppConfig and include it within system-test-config.xml as a <bean/> definition. Because <context:annotation-config/> is switched on, the container recognizes the @Configuration annotation and processes the @Bean methods declared in AppConfig properly.
            - The following example shows an ordinary configuration class in Java:
            ```
            @Configuration
            public class AppConfig {

                @Autowired
                private DataSource dataSource;

                @Bean
                public AccountRepository accountRepository() {
                    return new JdbcAccountRepository(dataSource);
                }

                @Bean
                public TransferService transferService() {
                    return new TransferService(accountRepository());
                }
            }
            ```
            - The following example shows part of a sample system-test-config.xml file:
            ```
            <beans>
                <!-- enable processing of annotations such as @Autowired and @Configuration -->
                <context:annotation-config/>
                <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

                <bean class="com.acme.AppConfig"/>

                <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
                    <property name="url" value="${jdbc.url}"/>
                    <property name="username" value="${jdbc.username}"/>
                    <property name="password" value="${jdbc.password}"/>
                </bean>
            </beans>
            ```
            - The following example shows a possible jdbc.properties file:
            ```
            jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
            jdbc.username=sa
            jdbc.password=
            ```
            - main
            ```
            public static void main(String[] args) {
                ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/com/acme/system-test-config.xml");
                TransferService transferService = ctx.getBean(TransferService.class);
                // ...
            }
            ```
        - Using <context:component-scan/> to pick up @Configuration classes
            - Because @Configuration is meta-annotated with @Component, @Configuration-annotated classes are automatically candidates for component scanning. Using the same scenario as describe in the previous example, we can redefine system-test-config.xml to take advantage of component-scanning. Note that, in this case, we need not explicitly declare <context:annotation-config/>, because <context:component-scan/> enables the same functionality.

            - The following example shows the modified system-test-config.xml file:
            ```
            <beans>
                <!-- picks up and registers AppConfig as a bean definition -->
                <context:component-scan base-package="com.acme"/>
                <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

                <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
                    <property name="url" value="${jdbc.url}"/>
                    <property name="username" value="${jdbc.username}"/>
                    <property name="password" value="${jdbc.password}"/>
                </bean>
            </beans>
            ```
    - @Configuration Class-centric Use of XML with @ImportResource
        - In applications where @Configuration classes are the primary mechanism for configuring the container, it is still likely necessary to use at least some XML. In these scenarios, you can use @ImportResource and define only as much XML as you need. Doing so achieves a “Java-centric” approach to configuring the container and keeps XML to a bare minimum. The following example (which includes a configuration class, an XML file that defines a bean, a properties file, and the main class) shows how to use the @ImportResource annotation to achieve “Java-centric” configuration that uses XML as needed:
        ```
        @Configuration
        @ImportResource("classpath:/com/acme/properties-config.xml")
        public class AppConfig {

            @Value("${jdbc.url}")
            private String url;

            @Value("${jdbc.username}")
            private String username;

            @Value("${jdbc.password}")
            private String password;

            @Bean
            public DataSource dataSource() {
                return new DriverManagerDataSource(url, username, password);
            }
        }

        // properties-config.xml
        <beans>
            <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>
        </beans>

        // jdbc.properties
        jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
        jdbc.username=sa
        jdbc.password=

        // main
        public static void main(String[] args) {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
            TransferService transferService = ctx.getBean(TransferService.class);
            // ...
        }
        ```

### Environment Abstraction
- The Environment interface is an abstraction integrated in the container that models two key aspects of the application environment: profiles and properties.

- A profile is a named, logical group of bean definitions to be registered with the container only if the given profile is active. Beans may be assigned to a profile whether defined in XML or with annotations. The role of the Environment object with relation to profiles is in determining which profiles (if any) are currently active, and which profiles (if any) should be active by default.

- Properties play an important role in almost all applications and may originate from a variety of sources: properties files, JVM system properties, system environment variables, JNDI, servlet context parameters, ad-hoc Properties objects, Map objects, and so on. The role of the Environment object with relation to properties is to provide the user with a convenient service interface for configuring property sources and resolving properties from them.

- Bean Definition Profiles
    - Bean definition profiles provide a mechanism in the core container that allows for registration of different beans in different environments. The word, “environment,” can mean different things to different users, and this feature can help with many use cases, including:
        - Working against an in-memory datasource in development versus looking up that same datasource from JNDI when in QA or production.
        - Registering monitoring infrastructure only when deploying an application into a performance environment.
        - Registering customized implementations of beans for customer A versus customer B deployments.
    - Consider the first use case in a practical application that requires a DataSource. In a test environment, the configuration might resemble the following:
    ```
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("my-schema.sql")
            .addScript("my-test-data.sql")
            .build();
    }
    ```
    - Now consider how this application can be deployed into a QA or production environment, assuming that the datasource for the application is registered with the production application server’s JNDI directory. Our dataSource bean now looks like the following listing:
    ```
    @Bean(destroyMethod="")
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
    ```
    - The problem is how to switch between using these two variations based on the current environment. Over time, Spring users have devised a number of ways to get this done, usually relying on a combination of system environment variables and XML <import/> statements containing ${placeholder} tokens that resolve to the correct configuration file path depending on the value of an environment variable. Bean definition profiles is a core container feature that provides a solution to this problem.

    - If we generalize the use case shown in the preceding example of environment-specific bean definitions, we end up with the need to register certain bean definitions in certain contexts but not in others. You could say that you want to register a certain profile of bean definitions in situation A and a different profile in situation B. We start by updating our configuration to reflect this need.

- Using @Profile
    - The @Profile annotation lets you indicate that a component is eligible for registration when one or more specified profiles are active. Using our preceding example, we can rewrite the dataSource configuration as follows:
    ```
    @Configuration
    @Profile("development")
    public class StandaloneDataConfig {

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:com/bank/config/sql/schema.sql")
                .addScript("classpath:com/bank/config/sql/test-data.sql")
                .build();
        }
    }

    @Configuration
    @Profile("production")
    public class JndiDataConfig {

        @Bean(destroyMethod="")
        public DataSource dataSource() throws Exception {
            Context ctx = new InitialContext();
            return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
        }
    }
    ```
    - As mentioned earlier, with @Bean methods, you typically choose to use programmatic JNDI lookups, by using either Spring’s JndiTemplate/JndiLocatorDelegate helpers or the straight JNDI InitialContext usage shown earlier but not the JndiObjectFactoryBean variant, which would force you to declare the return type as the FactoryBean type.
    - The profile string may contain a simple profile name (for example, production) or a profile expression. A profile expression allows for more complicated profile logic to be expressed (for example, production & us-east). The following operators are supported in profile expressions:
        - !: A logical “not” of the profile
        - &: A logical “and” of the profiles
        - |: A logical “or” of the profiles
    - You cannot mix the & and | operators without using parentheses. For example, production & us-east | eu-central is not a valid expression. It must be expressed as production & (us-east | eu-central).
    - You can use @Profile as a meta-annotation for the purpose of creating a custom composed annotation. The following example defines a custom @Production annotation that you can use as a drop-in replacement for @Profile("production"):
    ```
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Profile("production")
    public @interface Production {
    }
    ```
    - If a @Configuration class is marked with @Profile, all of the @Bean methods and @Import annotations associated with that class are bypassed unless one or more of the specified profiles are active. If a @Component or @Configuration class is marked with @Profile({"p1", "p2"}), that class is not registered or processed unless profiles 'p1' or 'p2' have been activated. If a given profile is prefixed with the NOT operator (!), the annotated element is registered only if the profile is not active. For example, given @Profile({"p1", "!p2"}), registration will occur if profile 'p1' is active **or** if profile 'p2' is not active.
    - @Profile can also be declared at the method level to include only one particular bean of a configuration class (for example, for alternative variants of a particular bean), as the following example shows:
    ```
    @Configuration
    public class AppConfig {

        @Bean("dataSource")
        @Profile("development") 
        public DataSource standaloneDataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:com/bank/config/sql/schema.sql")
                .addScript("classpath:com/bank/config/sql/test-data.sql")
                .build();
        }

        @Bean("dataSource")
        @Profile("production") 
        public DataSource jndiDataSource() throws Exception {
            Context ctx = new InitialContext();
            return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
        }
    }
    ```
    - With @Profile on @Bean methods, a special scenario may apply: In the case of overloaded @Bean methods of the same Java method name (analogous to constructor overloading), a @Profile condition needs to be consistently declared on all overloaded methods. If the conditions are inconsistent, only the condition on the first declaration among the overloaded methods matters. Therefore, @Profile can not be used to select an overloaded method with a particular argument signature over another. Resolution between all factory methods for the same bean follows Spring’s constructor resolution algorithm at creation time.
    - If you want to define alternative beans with different profile conditions, use distinct Java method names that point to the same bean name by using the @Bean name attribute, as shown in the preceding example. If the argument signatures are all the same (for example, all of the variants have no-arg factory methods), this is the only way to represent such an arrangement in a valid Java class in the first place (since there can only be one method of a particular name and argument signature).

- XML Bean Definition Profiles
    - The XML counterpart is the profile attribute of the <beans> element. Our preceding sample configuration can be rewritten in two XML files, as follows:
    ```
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:jdbc="http://www.springframework.org/schema/jdbc"
        xmlns:jee="http://www.springframework.org/schema/jee"
        xsi:schemaLocation="...">

        <!-- other bean definitions -->

        <beans profile="development">
            <jdbc:embedded-database id="dataSource">
                <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
                <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
            </jdbc:embedded-database>
        </beans>

        <beans profile="production">
            <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
        </beans>
    </beans>
    ```
    - The spring-bean.xsd has been constrained to allow such elements only as the last ones in the file. This should help provide flexibility without incurring clutter in the XML files.
    - The XML counterpart does not support the profile expressions described earlier. It is possible, however, to negate a profile by using the ! operator. It is also possible to apply a logical “and” by nesting the profiles, as the following example shows:
    ```
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:jdbc="http://www.springframework.org/schema/jdbc"
        xmlns:jee="http://www.springframework.org/schema/jee"
        xsi:schemaLocation="...">

        <!-- other bean definitions -->

        <beans profile="production">
            <beans profile="us-east">
                <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/> 
                // the dataSource bean is exposed if both the production and us-east profiles are active.
            </beans>
        </beans>
    </beans>
    ```
- Activating a Profile
    - Now that we have updated our configuration, we still need to instruct Spring which profile is active. If we started our sample application right now, we would see a NoSuchBeanDefinitionException thrown, because the container could not find the Spring bean named dataSource.

    - Activating a profile can be done in several ways, but the most straightforward is to do it programmatically against the Environment API which is available through an ApplicationContext. The following example shows how to do so:
    ```
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.getEnvironment().setActiveProfiles("development");
    ctx.register(SomeConfig.class, StandaloneDataConfig.class, JndiDataConfig.class);
    ctx.refresh();
    ```
    - In addition, you can also declaratively activate profiles through the spring.profiles.active property, which may be specified through system environment variables, JVM system properties, servlet context parameters in web.xml, or even as an entry in JNDI (see PropertySource Abstraction). In integration tests, active profiles can be declared by using the @ActiveProfiles annotation in the spring-test module (see context configuration with environment profiles).
    - Note that profiles are not an “either-or” proposition. You can activate multiple profiles at once. Programmatically, you can provide multiple profile names to the setActiveProfiles() method, which accepts String…​ varargs. The following example activates multiple profiles:
    ```
    ctx.getEnvironment().setActiveProfiles("profile1", "profile2");
    ```
    - Declaratively, spring.profiles.active may accept a comma-separated list of profile names, as the following example shows:
    ```
    -Dspring.profiles.active="profile1,profile2"
    ```
- Default Profile
    - The default profile represents the profile that is enabled by default. Consider the following example:
    ```
    @Configuration
    @Profile("default")
    public class DefaultDataConfig {

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:com/bank/config/sql/schema.sql")
                .build();
        }
    }
    ```
    - If no profile is active, the dataSource is created. You can see this as a way to provide a default definition for one or more beans. If any profile is enabled, the default profile does not apply.
    - You can change the name of the default profile by using setDefaultProfiles() on the Environment or ,declaratively, by using the spring.profiles.default property.

### PropertySource Abstraction
    - Spring’s Environment abstraction provides search operations over a configurable hierarchy of property sources. Consider the following listing:
    ```
    ApplicationContext ctx = new GenericApplicationContext();
    Environment env = ctx.getEnvironment();
    boolean containsMyProperty = env.containsProperty("my-property");
    System.out.println("Does my environment contain the 'my-property' property? " + containsMyProperty);
    ```
    - In the preceding snippet, we see a high-level way of asking Spring whether the my-property property is defined for the current environment. To answer this question, the Environment object performs a search over a set of PropertySource objects. A PropertySource is a simple abstraction over any source of key-value pairs, and Spring’s StandardEnvironment is configured with two PropertySource objects — one representing the set of JVM system properties (System.getProperties()) and one representing the set of system environment variables (System.getenv()).
    - Concretely, when you use the StandardEnvironment, the call to env.containsProperty("my-property") returns true if a my-property system property or my-property environment variable is present at runtime.
    - The search performed is hierarchical. By default, system properties have precedence over environment variables. So, if the my-property property happens to be set in both places during a call to env.getProperty("my-property"), the system property value “wins” and is returned. Note that property values are not merged but rather completely overridden by a preceding entry.
        - For a common StandardServletEnvironment, the full hierarchy is as follows, with the highest-precedence entries at the top:
            - ServletConfig parameters (if applicable — for example, in case of a DispatcherServlet context)
            - ServletContext parameters (web.xml context-param entries)
            - JNDI environment variables (java:comp/env/ entries)
            - JVM system properties (-D command-line arguments)
            - JVM system environment (operating system environment variables)
    - Most importantly, the entire mechanism is configurable. Perhaps you have a custom source of properties that you want to integrate into this search. To do so, implement and instantiate your own PropertySource and add it to the set of PropertySources for the current Environment. The following example shows how to do so:
    ```
    ConfigurableApplicationContext ctx = new GenericApplicationContext();
    MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
    sources.addFirst(new MyPropertySource());
    ```
    - In the preceding code, MyPropertySource has been added with highest precedence in the search. If it contains a my-property property, the property is detected and returned, in favor of any my-property property in any other PropertySource. The MutablePropertySources API exposes a number of methods that allow for precise manipulation of the set of property sources.
    - Using @PropertySource
        - The @PropertySource annotation provides a convenient and declarative mechanism for adding a PropertySource to Spring’s Environment.
        - Given a file called app.properties that contains the key-value pair testbean.name=myTestBean, the following @Configuration class uses @PropertySource in such a way that a call to testBean.getName() returns myTestBean:
        ```
        @Configuration
        @PropertySource("classpath:/com/myco/app.properties")
        public class AppConfig {

            @Autowired
            Environment env;

            @Bean
            public TestBean testBean() {
                TestBean testBean = new TestBean();
                testBean.setName(env.getProperty("testbean.name"));
                return testBean;
            }
        }
        ```
        - Any ${…​} placeholders present in a @PropertySource resource location are resolved against the set of property sources already registered against the environment, as the following example shows:
        ```
        @Configuration
        @PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
        public class AppConfig {

            @Autowired
            Environment env;

            @Bean
            public TestBean testBean() {
                TestBean testBean = new TestBean();
                testBean.setName(env.getProperty("testbean.name"));
                return testBean;
            }
        }
        ```
        - Assuming that my.placeholder is present in one of the property sources already registered (for example, system properties or environment variables), the placeholder is resolved to the corresponding value. If not, then default/path is used as a default. If no default is specified and a property cannot be resolved, an IllegalArgumentException is thrown.

        - The @PropertySource annotation is repeatable, according to Java 8 conventions. However, all such @PropertySource annotations need to be declared at the same level, either directly on the configuration class or as meta-annotations within the same custom annotation. Mixing direct annotations and meta-annotations is not recommended, since direct annotations effectively override meta-annotations.

    - Placeholder Resolution in Statements
        - Historically, the value of placeholders in elements could be resolved only against JVM system properties or environment variables. This is no longer the case. Because the Environment abstraction is integrated throughout the container, it is easy to route resolution of placeholders through it. This means that you may configure the resolution process in any way you like. You can change the precedence of searching through system properties and environment variables or remove them entirely. You can also add your own property sources to the mix, as appropriate.

        - Concretely, the following statement works regardless of where the customer property is defined, as long as it is available in the Environment:
        ```
        <beans>
            <import resource="com/bank/service/${customer}-config.xml"/>
        </beans>
        ```

    - Registering a LoadTimeWeaver
        - The LoadTimeWeaver is used by Spring to dynamically transform classes as they are loaded into the Java virtual machine (JVM).
        - To enable load-time weaving, you can add the @EnableLoadTimeWeaving to one of your @Configuration classes, as the following example shows:
        ```
        @Configuration
        @EnableLoadTimeWeaving
        public class AppConfig {
        }
        ```
    - Alternatively, for XML configuration, you can use the context:load-time-weaver element:
    ```
    <beans>
        <context:load-time-weaver/>
    </beans>
    ```
    - Once configured for the ApplicationContext, any bean within that ApplicationContext may implement LoadTimeWeaverAware, thereby receiving a reference to the load-time weaver instance. This is particularly useful in combination with Spring’s JPA support where load-time weaving may be necessary for JPA class transformation. Consult the [LocalContainerEntityManagerFactoryBean javadoc](https://docs.spring.io/spring-framework/docs/5.2.9.RELEASE/javadoc-api/org/springframework/orm/jpa/LocalContainerEntityManagerFactoryBean.html) for more detail. For more on AspectJ load-time weaving, see [Load-time Weaving with AspectJ](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop-aj-ltw) in the Spring Framework.

### Additional Capabilities of the ApplicationContext
- As discussed in the chapter introduction, the **org.springframework.beans.factory** package provides basic functionality for managing and manipulating beans, including in a programmatic way. The **org.springframework.context** package adds the ApplicationContext interface, which extends the BeanFactory interface, in addition to extending other interfaces to provide additional functionality in a more application framework-oriented style. Many people use the ApplicationContext in a completely declarative fashion, not even creating it programmatically, but instead relying on support classes such as ContextLoader to automatically instantiate an ApplicationContext as part of the normal startup process of a Java EE web application.

- To enhance BeanFactory functionality in a more framework-oriented style, the context package also provides the following functionality:
    - Access to messages in i18n-style, through the MessageSource interface.
    - Access to resources, such as URLs and files, through the ResourceLoader interface.
    - Event publication, namely to beans that implement the ApplicationListener interface, through the use of the ApplicationEventPublisher interface.
    - Loading of multiple (hierarchical) contexts, letting each be focused on one particular layer, such as the web layer of an application, through the HierarchicalBeanFactory interface.

- Internationalization using MessageSource
    - The ApplicationContext interface extends an interface called MessageSource and, therefore, provides internationalization (“i18n”) functionality. Spring also provides the HierarchicalMessageSource interface, which can resolve messages hierarchically. Together, these interfaces provide the foundation upon which Spring effects message resolution. The methods defined on these interfaces include:

        - String getMessage(String code, Object[] args, String default, Locale loc): The basic method used to retrieve a message from the MessageSource. When no message is found for the specified locale, the default message is used. Any arguments passed in become replacement values, using the MessageFormat functionality provided by the standard library.

        - String getMessage(String code, Object[] args, Locale loc): Essentially the same as the previous method but with one difference: No default message can be specified. If the message cannot be found, a NoSuchMessageException is thrown.

        - String getMessage(MessageSourceResolvable resolvable, Locale locale): All properties used in the preceding methods are also wrapped in a class named MessageSourceResolvable, which you can use with this method.

    - When an ApplicationContext is loaded, it automatically searches for a MessageSource bean defined in the context. The bean must have the name messageSource. If such a bean is found, all calls to the preceding methods are delegated to the message source. If no message source is found, the ApplicationContext attempts to find a parent containing a bean with the same name. If it does, it uses that bean as the MessageSource. If the ApplicationContext cannot find any source for messages, an empty DelegatingMessageSource is instantiated in order to be able to accept calls to the methods defined above.

    - Spring provides two MessageSource implementations, ResourceBundleMessageSource and StaticMessageSource. Both implement HierarchicalMessageSource in order to do nested messaging. The StaticMessageSource is rarely used but provides programmatic ways to add messages to the source. The following example shows ResourceBundleMessageSource:
    ```
    <beans>
        <bean id="messageSource"
                class="org.springframework.context.support.ResourceBundleMessageSource">
            <property name="basenames">
                <list>
                    <value>format</value>
                    <value>exceptions</value>
                    <value>windows</value>
                </list>
            </property>
        </bean>
    </beans>
    ```

    - The example assumes that you have three resource bundles called format, exceptions and windows defined in your classpath. Any request to resolve a message is handled in the JDK-standard way of resolving messages through ResourceBundle objects. For the purposes of the example, assume the contents of two of the above resource bundle files are as follows:
    ```
        # in format.properties
        message=Alligators rock!
        # in exceptions.properties
        argument.required=The {0} argument is required.
    ```
    - The next example shows a program to run the MessageSource functionality. Remember that all ApplicationContext implementations are also MessageSource implementations and so can be cast to the MessageSource interface.
    ```
    public static void main(String[] args) {
        MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
        String message = resources.getMessage("message", null, "Default", Locale.ENGLISH);
        System.out.println(message);
    }
    ```
    - The resulting output from the above program is as follows:
    ```
        Alligators rock!
    ```
    - To summarize, the MessageSource is defined in a file called beans.xml, which exists at the root of your classpath. The messageSource bean definition refers to a number of resource bundles through its basenames property. The three files that are passed in the list to the basenames property exist as files at the root of your classpath and are called format.properties, exceptions.properties, and windows.properties, respectively.

    - The next example shows arguments passed to the message lookup. These arguments are converted into String objects and inserted into placeholders in the lookup message.
    ```
    <beans>

        <!-- this MessageSource is being used in a web application -->
        <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
            <property name="basename" value="exceptions"/>
        </bean>

        <!-- lets inject the above MessageSource into this POJO -->
        <bean id="example" class="com.something.Example">
            <property name="messages" ref="messageSource"/>
        </bean>

    </beans>

    public class Example {

        private MessageSource messages;

        public void setMessages(MessageSource messages) {
            this.messages = messages;
        }

        public void execute() {
            String message = this.messages.getMessage("argument.required",
                new Object [] {"userDao"}, "Required", Locale.ENGLISH);
            System.out.println(message);
        }
    }

    // The resulting output from the invocation of the execute() method is as follows:
    The userDao argument is required.
    ```
    - With regard to internationalization (“i18n”), Spring’s various MessageSource implementations follow the same locale resolution and fallback rules as the standard JDK ResourceBundle. In short, and continuing with the example messageSource defined previously, if you want to resolve messages against the British (en-GB) locale, you would create files called format_en_GB.properties, exceptions_en_GB.properties, and windows_en_GB.properties, respectively.

    - Typically, locale resolution is managed by the surrounding environment of the application. In the following example, the locale against which (British) messages are resolved is specified manually:
    ```
    # in exceptions_en_GB.properties
    argument.required=Ebagum lad, the ''{0}'' argument is required, I say, required.

    public static void main(final String[] args) {
        MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
        String message = resources.getMessage("argument.required",
            new Object [] {"userDao"}, "Required", Locale.UK);
        System.out.println(message);
    }
    ```
    - The resulting output from the running of the above program is as follows:
    ```
        Ebagum lad, the 'userDao' argument is required, I say, required.
    ```
    - You can also use the MessageSourceAware interface to acquire a reference to any MessageSource that has been defined. Any bean that is defined in an ApplicationContext that implements the MessageSourceAware interface is injected with the application context’s MessageSource when the bean is created and configured.

    - As an alternative to ResourceBundleMessageSource, Spring provides a ReloadableResourceBundleMessageSource class. This variant supports the same bundle file format but is more flexible than the standard JDK based ResourceBundleMessageSource implementation. In particular, it allows for reading files from any Spring resource location (not only from the classpath) and supports hot reloading of bundle property files (while efficiently caching them in between). See the ReloadableResourceBundleMessageSource javadoc for details.

### Standard and Custom Events
- Event handling in the ApplicationContext is provided through the ApplicationEvent class and the ApplicationListener interface. If a bean that implements the ApplicationListener interface is deployed into the context, every time an ApplicationEvent gets published to the ApplicationContext, that bean is notified. Essentially, this is the standard Observer design pattern.
- As of Spring 4.2, the event infrastructure has been significantly improved and offers an [annotation-based model](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#context-functionality-events-annotation) as well as the ability to publish any arbitrary event (that is, an object that does not necessarily extend from ApplicationEvent). When such an object is published, we wrap it in an event for you.
- The following table describes the standard events that Spring provides:


|Event|	Explanation|
|---|---|
|ContextRefreshedEvent|Published when the ApplicationContext is initialized or refreshed (for example, by using the refresh() method on the ConfigurableApplicationContext interface). Here, “initialized” means that all beans are loaded, post-processor beans are detected and activated, singletons are pre-instantiated, and the ApplicationContext object is ready for use. As long as the context has not been closed, a refresh can be triggered multiple times, provided that the chosen ApplicationContext actually supports such “hot” refreshes. For example, XmlWebApplicationContext supports hot refreshes, but GenericApplicationContext does not.|
|ContextStartedEvent|Published when the ApplicationContext is started by using the start() method on the ConfigurableApplicationContext interface. Here, “started” means that all Lifecycle beans receive an explicit start signal. Typically, this signal is used to restart beans after an explicit stop, but it may also be used to start components that have not been configured for autostart (for example, components that have not already started on initialization).|
|ContextStoppedEvent|Published when the ApplicationContext is stopped by using the stop() method on the ConfigurableApplicationContext interface. Here, “stopped” means that all Lifecycle beans receive an explicit stop signal. A stopped context may be restarted through a start() call.|
|ContextClosedEvent|Published when the ApplicationContext is being closed by using the close() method on the ConfigurableApplicationContext interface or via a JVM shutdown hook. Here, "closed" means that all singleton beans will be destroyed. Once the context is closed, it reaches its end of life and cannot be refreshed or restarted.|
|RequestHandledEvent|A web-specific event telling all beans that an HTTP request has been serviced. This event is published after the request is complete. This event is only applicable to web applications that use Spring’s DispatcherServlet.|
|ServletRequestHandledEvent|A subclass of RequestHandledEvent that adds Servlet-specific context information.|

- You can also create and publish your own custom events. The following example shows a simple class that extends Spring’s ApplicationEvent base class:
```
public class BlockedListEvent extends ApplicationEvent {

    private final String address;
    private final String content;

    public BlockedListEvent(Object source, String address, String content) {
        super(source);
        this.address = address;
        this.content = content;
    }

    // accessor and other methods...
}
```
- To publish a custom ApplicationEvent, call the publishEvent() method on an ApplicationEventPublisher. Typically, this is done by creating a class that implements ApplicationEventPublisherAware and registering it as a Spring bean. The following example shows such a class:
```
public class EmailService implements ApplicationEventPublisherAware {

    private List<String> blockedList;
    private ApplicationEventPublisher publisher;

    public void setBlockedList(List<String> blockedList) {
        this.blockedList = blockedList;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void sendEmail(String address, String content) {
        if (blockedList.contains(address)) {
            publisher.publishEvent(new BlockedListEvent(this, address, content));
            return;
        }
        // send email...
    }
}
```
- At configuration time, the Spring container detects that EmailService implements ApplicationEventPublisherAware and automatically calls setApplicationEventPublisher(). In reality, the parameter passed in is the Spring container itself. You are interacting with the application context through its ApplicationEventPublisher interface.

- To receive the custom ApplicationEvent, you can create a class that implements ApplicationListener and register it as a Spring bean. The following example shows such a class:
```
public class BlockedListNotifier implements ApplicationListener<BlockedListEvent> {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    public void onApplicationEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```
- Notice that ApplicationListener is generically parameterized with the type of your custom event (BlockedListEvent in the preceding example). This means that the onApplicationEvent() method can remain type-safe, avoiding any need for downcasting. You can register as many event listeners as you wish, but note that, by default, event listeners receive events synchronously. This means that the publishEvent() method blocks until all listeners have finished processing the event. One advantage of this synchronous and single-threaded approach is that, when a listener receives an event, it operates inside the transaction context of the publisher if a transaction context is available. If another strategy for event publication becomes necessary, see the javadoc for Spring’s ApplicationEventMulticaster interface and SimpleApplicationEventMulticaster implementation for configuration options.

- The following example shows the bean definitions used to register and configure each of the classes above:
```
<bean id="emailService" class="example.EmailService">
    <property name="blockedList">
        <list>
            <value>known.spammer@example.org</value>
            <value>known.hacker@example.org</value>
            <value>john.doe@example.org</value>
        </list>
    </property>
</bean>

<bean id="blockedListNotifier" class="example.BlockedListNotifier">
    <property name="notificationAddress" value="blockedlist@example.org"/>
</bean>
```
- Putting it all together, when the sendEmail() method of the emailService bean is called, if there are any email messages that should be blocked, a custom event of type BlockedListEvent is published. The blockedListNotifier bean is registered as an ApplicationListener and receives the BlockedListEvent, at which point it can notify appropriate parties.

- Spring’s eventing mechanism is designed for simple communication between Spring beans within the same application context. However, for more sophisticated enterprise integration needs, the separately maintained Spring Integration project provides complete support for building lightweight, pattern-oriented, event-driven architectures that build upon the well-known Spring programming model.

- Annotation-based Event Listeners
    - As of Spring 4.2, you can register an event listener on any public method of a managed bean by using the @EventListener annotation. The BlockedListNotifier can be rewritten as follows:
    ```
    public class BlockedListNotifier {

        private String notificationAddress;

        public void setNotificationAddress(String notificationAddress) {
            this.notificationAddress = notificationAddress;
        }

        @EventListener
        public void processBlockedListEvent(BlockedListEvent event) {
            // notify appropriate parties via notificationAddress...
        }
    }
    ```
    - The method signature once again declares the event type to which it listens, but, this time, with a flexible name and without implementing a specific listener interface. The event type can also be narrowed through generics as long as the actual event type resolves your generic parameter in its implementation hierarchy.

    - If your method should listen to several events or if you want to define it with no parameter at all, the event types can also be specified on the annotation itself. The following example shows how to do so:
    ```
    @EventListener({ContextStartedEvent.class, ContextRefreshedEvent.class})
    public void handleContextStart() {
        // ...
    }
    ```
    - It is also possible to add additional runtime filtering by using the condition attribute of the annotation that defines a SpEL expression , which should match to actually invoke the method for a particular event.

    - The following example shows how our notifier can be rewritten to be invoked only if the content attribute of the event is equal to my-event:
    ```
    @EventListener(condition = "#blEvent.content == 'my-event'")
    public void processBlockedListEvent(BlockedListEvent blockedListEvent) {
        // notify appropriate parties via notificationAddress...
    }
    ```
    - Each SpEL expression evaluates against a dedicated context. The following table lists the items made available to the context so that you can use them for conditional event processing:
    
    |Name|Location|Description|Example|
    |---|---|---|---|---|
    |Event|root object|The actual ApplicationEvent.|#root.event or event|
    |Arguments array|root object|The arguments (as an object array) used to invoke the method.|#root.args or args; args[0] to access the first argument, etc.|
    |Argument name|evaluation context|The name of any of the method arguments. If, for some reason, the names are not available (for example, because there is no debug information in the compiled byte code), individual arguments are also available using the #a<#arg> syntax where <#arg> stands for the argument index (starting from 0).|#blEvent or #a0 (you can also use #p0 or #p<#arg> parameter notation as an alias|

    - Note that #root.event gives you access to the underlying event, even if your method signature actually refers to an arbitrary object that was published.

    - If you need to publish an event as the result of processing another event, you can change the method signature to return the event that should be published, as the following example shows:
    ```
    @EventListener
    public ListUpdateEvent handleBlockedListEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress and
        // then publish a ListUpdateEvent...
    }
    ```
    - This new method publishes a new ListUpdateEvent for every BlockedListEvent handled by the method above. If you need to publish several events, you can return a Collection of events instead.
    - This feature is not supported for asynchronous listeners.

- Asynchronous Listeners
    - If you want a particular listener to process events asynchronously, you can reuse the regular @Async support. The following example shows how to do so:
    ```
    @EventListener
    @Async
    public void processBlockedListEvent(BlockedListEvent event) {
        // BlockedListEvent is processed in a separate thread
    }
    ```
    - Be aware of the following limitations when using asynchronous events:

        - If an asynchronous event listener throws an Exception, it is not propagated to the caller. See AsyncUncaughtExceptionHandler for more details.

        - Asynchronous event listener methods cannot publish a subsequent event by returning a value. If you need to publish another event as the result of the processing, inject an ApplicationEventPublisher to publish the event manually.

- Ordering Listeners
    - If you need one listener to be invoked before another one, you can add the @Order annotation to the method declaration, as the following example shows:
    ```
    @EventListener
    @Order(42)
    public void processBlockedListEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
    ```
- Generic Events
    - You can also use generics to further define the structure of your event. Consider using an EntityCreatedEvent<T> where T is the type of the actual entity that got created. For example, you can create the following listener definition to receive only EntityCreatedEvent for a Person:
    ```
    @EventListener
    public void onPersonCreated(EntityCreatedEvent<Person> event) {
        // ...
    }
    ```
    - Due to type erasure, this works only if the event that is fired resolves the generic parameters on which the event listener filters (that is, something like class PersonCreatedEvent extends EntityCreatedEvent<Person> { …​ }).

    - In certain circumstances, this may become quite tedious if all events follow the same structure (as should be the case for the event in the preceding example). In such a case, you can implement ResolvableTypeProvider to guide the framework beyond what the runtime environment provides. The following event shows how to do so:
    ```
    public class EntityCreatedEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {
        public EntityCreatedEvent(T entity) {
            super(entity);
        }

        @Override
        public ResolvableType getResolvableType() {
            return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getSource()));
        }
    }
    ```
    - This works not only for ApplicationEvent but any arbitrary object that you send as an event.


### Convenient Access to Low-level Resources
- For optimal usage and understanding of application contexts, you should familiarize yourself with Spring’s Resource abstraction, as described in Resources.

- An application context is a ResourceLoader, which can be used to load Resource objects. A Resource is essentially a more feature rich version of the JDK java.net.URL class. In fact, the implementations of the Resource wrap an instance of java.net.URL, where appropriate. A Resource can obtain low-level resources from almost any location in a transparent fashion, including from the classpath, a filesystem location, anywhere describable with a standard URL, and some other variations. If the resource location string is a simple path without any special prefixes, where those resources come from is specific and appropriate to the actual application context type.

- You can configure a bean deployed into the application context to implement the special callback interface, ResourceLoaderAware, to be automatically called back at initialization time with the application context itself passed in as the ResourceLoader. You can also expose properties of type Resource, to be used to access static resources. They are injected into it like any other properties. You can specify those Resource properties as simple String paths and rely on automatic conversion from those text strings to actual Resource objects when the bean is deployed.

- The location path or paths supplied to an ApplicationContext constructor are actually resource strings and, in simple form, are treated appropriately according to the specific context implementation. For example ClassPathXmlApplicationContext treats a simple location path as a classpath location. You can also use location paths (resource strings) with special prefixes to force loading of definitions from the classpath or a URL, regardless of the actual context type.

### Convenient ApplicationContext Instantiation for Web Applications
- You can create ApplicationContext instances declaratively by using, for example, a ContextLoader. Of course, you can also create ApplicationContext instances programmatically by using one of the ApplicationContext implementations.

- You can register an ApplicationContext by using the ContextLoaderListener, as the following example shows:
```
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/daoContext.xml /WEB-INF/applicationContext.xml</param-value>
</context-param>

<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```
- The listener inspects the contextConfigLocation parameter. If the parameter does not exist, the listener uses /WEB-INF/applicationContext.xml as a default. When the parameter does exist, the listener separates the String by using predefined delimiters (comma, semicolon, and whitespace) and uses the values as locations where application contexts are searched. Ant-style path patterns are supported as well. Examples are /WEB-INF/*Context.xml (for all files with names that end with Context.xml and that reside in the WEB-INF directory) and /WEB-INF/**/*Context.xml (for all such files in any subdirectory of WEB-INF).

### Deploying a Spring ApplicationContext as a Java EE RAR File

- It is possible to deploy a Spring ApplicationContext as a RAR file, encapsulating the context and all of its required bean classes and library JARs in a Java EE RAR deployment unit. This is the equivalent of bootstrapping a stand-alone ApplicationContext (only hosted in Java EE environment) being able to access the Java EE servers facilities. RAR deployment is a more natural alternative to a scenario of deploying a headless WAR file — in effect, a WAR file without any HTTP entry points that is used only for bootstrapping a Spring ApplicationContext in a Java EE environment.

- RAR deployment is ideal for application contexts that do not need HTTP entry points but rather consist only of message endpoints and scheduled jobs. Beans in such a context can use application server resources such as the JTA transaction manager and JNDI-bound JDBC DataSource instances and JMS ConnectionFactory instances and can also register with the platform’s JMX server — all through Spring’s standard transaction management and JNDI and JMX support facilities. Application components can also interact with the application server’s JCA WorkManager through Spring’s TaskExecutor abstraction.

- See the javadoc of the SpringContextResourceAdapter class for the configuration details involved in RAR deployment.

- For a simple deployment of a Spring ApplicationContext as a Java EE RAR file:

    - Package all application classes into a RAR file (which is a standard JAR file with a different file extension). .Add all required library JARs into the root of the RAR archive. .Add a META-INF/ra.xml deployment descriptor (as shown in the javadoc for SpringContextResourceAdapter) and the corresponding Spring XML bean definition file(s) (typically META-INF/applicationContext.xml).

    - Drop the resulting RAR file into your application server’s deployment directory.

- Such RAR deployment units are usually self-contained. They do not expose components to the outside world, not even to other modules of the same application. Interaction with a RAR-based ApplicationContext usually occurs through JMS destinations that it shares with other modules. A RAR-based ApplicationContext may also, for example, schedule some jobs or react to new files in the file system (or the like). If it needs to allow synchronous access from the outside, it could (for example) export RMI endpoints, which may be used by other application modules on the same machine.

### The BeanFactory
- The BeanFactory API provides the underlying basis for Spring’s IoC functionality. Its specific contracts are mostly used in integration with other parts of Spring and related third-party frameworks, and its DefaultListableBeanFactory implementation is a key delegate within the higher-level GenericApplicationContext container.

- BeanFactory and related interfaces (such as BeanFactoryAware, InitializingBean, DisposableBean) are important integration points for other framework components. By not requiring any annotations or even reflection, they allow for very efficient interaction between the container and its components. Application-level beans may use the same callback interfaces but typically prefer declarative dependency injection instead, either through annotations or through programmatic configuration.

- Note that the core BeanFactory API level and its DefaultListableBeanFactory implementation do not make assumptions about the configuration format or any component annotations to be used. All of these flavors come in through extensions (such as XmlBeanDefinitionReader and AutowiredAnnotationBeanPostProcessor) and operate on shared BeanDefinition objects as a core metadata representation. This is the essence of what makes Spring’s container so flexible and extensible.

- BeanFactory or ApplicationContext?
    - This section explains the differences between the BeanFactory and ApplicationContext container levels and the implications on bootstrapping.

    - **You should use an ApplicationContext unless you have a good reason for not doing so**, with GenericApplicationContext and its subclass AnnotationConfigApplicationContext as the common implementations for custom bootstrapping. These are the primary entry points to Spring’s core container for all common purposes: loading of configuration files, triggering a classpath scan, programmatically registering bean definitions and annotated classes, and (as of 5.0) registering functional bean definitions.

    - Because **an ApplicationContext includes all the functionality of a BeanFactory**, it is generally recommended over a plain BeanFactory, except for scenarios where full control over bean processing is needed. Within an ApplicationContext (such as the GenericApplicationContext implementation), several kinds of beans are detected by convention (that is, by bean name or by bean type — in particular, post-processors), while a plain DefaultListableBeanFactory is agnostic about any special beans.

    - For many extended container features, such as annotation processing and AOP proxying, the BeanPostProcessor extension point is essential. If you use only a plain DefaultListableBeanFactory, such post-processors do not get detected and activated by default. This situation could be confusing, because nothing is actually wrong with your bean configuration. Rather, in such a scenario, the container needs to be fully bootstrapped through additional setup.

    - The following table lists features provided by the BeanFactory and ApplicationContext interfaces and implementations.

    |Feature|BeanFactory|ApplicationContext|
    |---|---|---|
    |Bean instantiation/wiring|Yes|Yes|
    |Integrated lifecycle management|No|Yes|
    |Automatic BeanPostProcessor registration|No|Yes|
    |Automatic BeanFactoryPostProcessor registration|No|Yes|
    |Convenient MessageSource access (for internalization)|No|Yes|
    |Built-in ApplicationEvent publication mechanism|No|Yes|

    - To explicitly register a bean post-processor with a DefaultListableBeanFactory, you need to programmatically call addBeanPostProcessor, as the following example shows:
    ```
    DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
    // populate the factory with bean definitions

    // now register any needed BeanPostProcessor instances
    factory.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
    factory.addBeanPostProcessor(new MyBeanPostProcessor());

    // now start using the factory
    ```
    - In both cases, the explicit registration steps are inconvenient, which is why the various ApplicationContext variants are preferred over a plain DefaultListableBeanFactory in Spring-backed applications, especially when relying on BeanFactoryPostProcessor and BeanPostProcessor instances for extended container functionality in a typical enterprise setup.

    - An AnnotationConfigApplicationContext has all common annotation post-processors registered and may bring in additional processors underneath the covers through configuration annotations, such as @EnableTransactionManagement. At the abstraction level of Spring’s annotation-based configuration model, the notion of bean post-processors becomes a mere internal container detail.