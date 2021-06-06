package application.service;

import spring.BeanPostProcessor;
import spring.Component;

@Component
public class UserBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object instance, String beanName) {
        System.out.println("初始化前");
        if (instance.equals("userServiceImpl")){
            ((UserServiceImpl)instance).setBeanName("name:userServiceImpl");
        }
        return instance;
    }

    @Override
    public Object postProcessAfterInitialization(Object instance, String beanName) {
        System.out.println("初始化" + beanName + "后");
        return instance;
    }
}
