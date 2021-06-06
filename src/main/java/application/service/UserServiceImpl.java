package application.service;

import spring.*;

import java.lang.annotation.Annotation;

@Component("userServiceImpl")
@Scope("singleton")
//public class UserServiceImpl implements BeanNameAware
public class UserServiceImpl implements InitializingBean {
    @Autowired
    OrderService orderService;

    public String getBeanName() {
        return beanName;
    }

    String beanName;


    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
    }


    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("xxxx");
    }
}
