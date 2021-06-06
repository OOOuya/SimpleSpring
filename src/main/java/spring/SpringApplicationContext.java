package spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 接受和解析配置类、得到Bean容器
 *
 * @return
 */
public class SpringApplicationContext {
    //`BeanDefinitionMap`（存放所有Bean的定义信息） **容器池**
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //`singletonObjects`（存放单例对象） **线程池**
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    //`BeanPostProcessorList` 创建Bean的额外AOP操作列表
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class configClass;

    //配置类接受和解析
    public SpringApplicationContext(Class configClass) throws Exception {
        this.configClass = configClass;
        scan(configClass);
        //遍历beanDefinitionMap，根据BeanDefinition判断是否是单例
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope() != null && beanDefinition.getScope().equals("singleton")){
                //(在创建对象的时候，就要初始化单例池)是单例，调用createBean创建对应的bean容器，把对象放入singletonObjects
                //判断不能是接口
                if (!beanDefinition.getClazz().isInterface()) {

                    Object bean = createBean(beanName, beanDefinition);
                    System.out.println("把" + bean.getClass() + "装入singletonObjects");
                    singletonObjects.put(beanName, bean);
                }
            }
        }

    }

    private void scan(Class configClass) throws ClassNotFoundException {
        //解析配置类 ：把ComponentScan注解解析出来->得到对应的扫描路径
        ComponentScan componentScan = (ComponentScan)configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScan.value();
        path = path.replace(".", "/");
        //得到 `applicationContext`的类加载器
        ClassLoader classLoader = SpringApplicationContext.class.getClassLoader();
        //通过类加载器得到当前目录
        URL resource = classLoader.getResource(path);
        //判断是否是目录，得到目录下的所有类的路径
        File file = new File(resource.getFile());
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                String absolutePath = f.getAbsolutePath();
                if (absolutePath.endsWith(".class")){
                    //截取classes~.class的路径，把路径转换为 `com.spring.xxx`的类包形式
                    absolutePath = absolutePath.substring(absolutePath.indexOf("application"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\", ".");
                    //通过类加载器得到类对象
                    Class<?> clazz = classLoader.loadClass(absolutePath);
                    //反射判断类有没有component注解，如果有，表示当前类是一个Bean容器
                    if (clazz.isAnnotationPresent(Component.class)){
                        //通过`isAssginableFrom`判断当前类是否实现了`BeanPostProcessor`接口（即判断当前类是否是BeanPostProcessor的实现类）
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                            try {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                //如果有，说明这个类并用于AOP操作，使用getBean，生成BeanPOstProcessor实例，将其存入 `BeanPostProcessorList`
                                beanPostProcessorList.add(instance);
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                        //生成`BeanDefinition`对象作为当前类的类定义，并设置clazz属性
                        BeanDefinition beanDefinition = new BeanDefinition();
                        //判断当前bean是单例Bean还是原型prototype Bean
                        //反射得到`Component`注解里的名字，
                        Component component = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = component.value();
                        //如果scope注解存在，反射得到Scope注解
                        if (clazz.isAnnotationPresent(Scope.class)){
                            Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                            //把scope注解的值存入`BeanDefinition`的scope属性
                            beanDefinition.setScope(scope.value());
                        }
                        beanDefinition.setClazz(clazz);
                        //把BeanDefinition存入BeanDefinitionMap
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                }
            }
        }
    }

    //从容器中返回bean
    public Object getBean(String beanClassName) throws Exception {
        //通过传入的beanName从BeanDefinitionMap中获取对应的BeanDefinition
        if (beanDefinitionMap.containsKey(beanClassName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanClassName);
            //判断：如果是单例对象，通过beanName返回`singletonObjects`的单例
            if (beanDefinition.getScope() != null && beanDefinition.getScope().equals("singleton")){
                //在调用构造函数之后，就不需要再创建单例的Bean对象了,从singletonObjects中拿对象
                System.out.println("从singletonObjects拿" + singletonObjects.get(beanClassName));
                return singletonObjects.get(beanClassName);
            }
            //如果不是单例对象，调用createBean创建新的bean（原型每次调用都会创建新的bean）
            else{
                return createBean(beanClassName, beanDefinition);
            }
        }else {
            throw new Exception("不存在 "+ beanClassName + "！！");
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        //通过传入的BeanDefinition，得到其Clazz属性
        Class clazz = beanDefinition.getClazz();

        //通过clazz的无参构造方法反射得到类实例对象（*实例化*）
        Object instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            //遍历判断类的字段，反射判断字段是否有AutoWired注解
            for (Field field : clazz.getDeclaredFields()) {
                ////如果有注解，传入对象的String名调用getBean从BeanDefinitionMap中取BeanDefinition，从而得到字段的bean容器对象
                if (field.isAnnotationPresent(Autowired.class)){
                    try {
                        System.out.println("初始化" + instance  + "绑定" + field.getName());
                        Object bean = getBean(field.getName());
                        //通过set方法把字段的bean对象和实例对象instance关联起来
                        field.setAccessible(true);
                        field.set(instance, bean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //如果类实例对象实现了 `BeanNameAware`接口，spring会设置类的名称
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            //判断如果类实例对象实现了 `InitializingBean`接口，spring会调用`afterPropertiesSet()`
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }


            //循环`beanPostProcessorList`，调用`postProcessBeforeInitialization`进行实例初始化前的操作
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
