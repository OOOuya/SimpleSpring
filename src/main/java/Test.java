import application.AppConfig;
import application.service.UserServiceImpl;
import spring.SpringApplicationContext;

public class Test {
    AppConfig appConfig;

    public static void main(String[] args) throws Exception {
        SpringApplicationContext springApplicationContext = new SpringApplicationContext(AppConfig.class);
        //如果userService是单例bean，多次调用getBean得到的还是一个对象
        //如果不是单例bean，多次调用就不是一个对象
        UserServiceImpl userServiceImpl = (UserServiceImpl) springApplicationContext.getBean("userServiceImpl");
        userServiceImpl.test();
    }
}
