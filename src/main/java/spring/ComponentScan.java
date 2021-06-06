package spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//runtime：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
@Retention(RetentionPolicy.RUNTIME)
/**用于描述类、接口(包括注解类型) 或enum声明 Class, interface  */
@Target(ElementType.TYPE)
public @interface ComponentScan {
    String value() default "";
}
