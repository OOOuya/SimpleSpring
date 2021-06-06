package spring;

public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object instance, String beanName);
    Object postProcessAfterInitialization(Object instance, String beanName);
}
