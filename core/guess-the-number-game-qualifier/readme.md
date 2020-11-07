## Source
https://www.baeldung.com/spring-bean

## Bean Configuration
First off, let's decorate the Company class with the @Component annotation:
```
public class Address {
    private String street;
    private int number;
 
    public Address(String street, int number) {
        this.street = street;
        this.number = number;
    }
 
    // getters and setters
}

@Component
public class Company {
    private Address address;
    public Company(Address address) { this.address = address; }

    // getter, setter and other properties
}
```  
Here's a configuration class supplying bean metadata to an IoC container:  
```
@Configuration
@ComponentScan(basePackageClasses = Company.class)
public class Config {
    @Bean
    public Address getAddress() {
        return new Address("High Street", 1000);
    }
}
```

The configuration class produces a bean of type Address.  
 
It also carries the @ComponentScan annotation, which instructs the container to looks for beans in the package containing the Company class.  

When a Spring IoC container constructs objects of those types, all the objects are called Spring beans as they are managed by the IoC container.  


## IoC in Action
Since we defined beans in a configuration class, we'll need an instance of the AnnotationConfigApplicationContext class to build up a container:
```
ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
```
A quick test verifies the existence as well as property values of our beans:
```
Company company = context.getBean("company", Company.class);
assertEquals("High Street", company.getAddress().getStreet());
assertEquals(1000, company.getAddress().getNumber());
 ```