package study;
import ch.qos.logback.core.net.server.Client;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class MyTest {
    public interface ClientDao { }
    public static class ClientDaoImpl implements ClientDao{
        public ClientDaoImpl(){
            System.out.println(this);
        }
    }
    public interface ClientService{
        public void setClientDao(ClientDao clientDao);
    }
    public static class ClientServiceImpl implements ClientService{
        public void setClientDao(ClientDao clientDao){

        }
    }

    @Configuration
    public static class AppConfig {

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

    public static void main(String[] args) {
        AppConfig a = new AppConfig();
        ClientService a1 = a.clientService1(); // study.MyTest$ClientDaoImpl@1f17ae12
        ClientService a2 = a.clientService2(); // study.MyTest$ClientDaoImpl@4d405ef7
    }


}
