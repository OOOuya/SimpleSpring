##  Spring包

### `applicationContext`

#### 属性：

`BeanDefinitionMap`（存放所有Bean的定义信息） **容器池**

`singletonObjects`（存放单例对象） **线程池**

`BeanPostProcessorList` 创建Bean的额外AOP操作列表

*都是concurrentHashMap类型*

#### 方法：

1. 接受配置类

2. 解析配置类`scan(Class configClass)`

   1. 把ComponentScan注解解析出来

   2. 得到对应包扫描路径

   3. 找到类，得到klass对象

      1. 得到 `applicationContext`的类加载器

      2. 通过类加载器得到当前目录

      3. 遍历目录下的所有文件，得到所有.class结尾的类，得到类的路径

      4. 把路径转换为 `com.spring.xxx`的类包形式

      5. 反射通过类加载器得到类对象

      6. 反射判断类有没有component注解，如果有，表示当前类是一个Bean容器

      7. 通过`isAssginableFrom`判断当前类是否实现了`BeanPostProcessor`接口（即判断当前类是否是BeanPostProcessor的实现类）

         1. 如果有，说明这个类并用于AOP操作，使用getBean，生成BeanPOstProcessor实例，将其存入 `BeanPostProcessorList`

         > [ java中isAssignableFrom()方法与instanceof关键字用法及通过反射配合注解为字段设置默认值_](https://blog.csdn.net/qq_36666651/article/details/81215221)

      8. 生成`BeanDefinition`对象作为当前类的类定义，并设置clazz属性

      9. 判断当前bean是单例Bean还是原型prototype Bean

         *如果userService是单例bean，多次调用getBean得到的还是一个对象*

         - 反射得到`Component`注解里的名字，反射得到Scope注解

           *如果没有scope注解，表示当前是一个单例对象*

      10. 把scope注解的值存入`BeanDefinition`的scope属性

      11. 把BeanDefinition存入BeanDefinitionMap

3. 构造方法

   1. 调用scan(AppConfig a)方法
   2. 遍历beanDefinitionMap，根据BeanDefinition判断是否是单例
   3. 是单例，调用createBean创建对应的bean容器，把对象放入singletonObjects

4. `getBean(String beanName)方法`

   1. 通过传入的beanName从BeanDefinitionMap中获取对应的BeanDefinition

   2. 判断：如果是单例对象，通过beanName返回`singletonObjects`的单例

      因此在调用构造函数之后，就不需要再创建单例的Bean对象了

   3. 如果不是单例对象，调用createBean创建新的bean（原型每次调用都会创建新的bean）

   因此，要定义单例池 `ConcurrentHashMap<String, Object>`

5. 创建原型Bean：`createBean(String beanName, BeanDefinition b)`

   1. 通过传入的BeanDefinition，得到其Clazz属性

   2. 通过clazz的无参构造方法反射得到类实例对象（*实例化*）

      **接下来是依赖注入的部分**

   3. 遍历判断类的字段的类，反射判断字段是否有AutoWired注解

   4. 如果有注解，传入对象的String名调用getBean从BeanDefinitionMap中取BeanDefinition，从而得到字段的bean容器对象

   5. 通过set方法把字段的bean对象和实例对象instance关联起来

      **思考** (Aware回调)

      对于`String beanName`，并不是bean对象，我们想要封装，就需要提供`BeanNameAware`接口，提供setBeanName方法，用于为实现类创建Bean名称

      1. 因此，我们判断：如果类实例对象实现了 `BeanNameAware`接口，spring会设置类的名称

      **思考** (初始化InitializingBean) Spring提供的类初始化

      1. 判断如果类实例对象实现了 `InitializingBean`接口，spring会调用`afterPropertiesSet()`

      **思考**(我们需要在创建bean的前后都做一些操作，类似于AOP) `BeanPostProcesser`

      ​	因此，创建spring包下的BeanPostProcesser接口



      1. 循环`beanPostProcessorList`，调用`postProcessBeforeInitialization`进行实例初始化前的操作





### spring的配置文件类 `AppConfig`

1. 注解类

   1. 定义Spring的注解类 `ComponentScan`

      1. 加入Retention注解

         ```java
         @Retention(RetentionPolicy.RUNTIME)
         ```

         > [@Retention注解作用_m0_37840000的博客-CSDN博客](https://blog.csdn.net/m0_37840000/article/details/80921775)

      2. 加入Target注解

         ```java
         @Target(ElementType.TYPE)
         ```

         > [java元注解 @Target注解用法 - 就这个名字好 - 博客园 (cnblogs.com)](https://www.cnblogs.com/unknows/p/10261539.html)

      3. 加入注解的值

         ```java
         String value() default "";
         ```

         > [Java8之default关键字_博客园-CSDN博客](https://blog.csdn.net/qq_39629277/article/details/102950402?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-0&spm=1001.2101.3001.4242)

   2. 定义 `component`注解

   3. 定义`Scope`注解

   4. 定义`Autowired`注解



### 接口

#### BeanPostProcesser

(我们需要在InitializingBean的前后都做一些操作，类似于AOP)

当spring扫描到UserBeanPostProcesser有component注解，并且实现了BeanPostProcesser，说明这是一个特殊的类

1. 在实例化bean前进行的操作 `postProcessBeforeInitialization(Object instance, String beanName)`

2. 在实例化bean后进行的操作 `postProcessAfterInitialization(Object instance, String beanName)`



#### BeanNameAware

对于`String beanName`，并不是bean对象，我们想要封装，就需要提供`BeanNameAware`接口，提供setBeanName方法，用于为实现类创建Bean名称

```java
void setBeanName(String name);
```

#### InitializingBean

Spring提供的类初始化

`afterPropertiesSet()`

## service包

1. 定义service包及包下的类



1. `UserBeanPostProcesser`实现了BeanPostProcesser接口，并且加上注解 `Component`

   1. 在继承的两个方法中执行 **AOP逻辑**

      1. 通过JDK的动态代理的newInstance方法生成**BeanPostProcesser**代理对象，返回代理对象

         > 在spring中，进行AOP实际上就是生成了生成一个BeanPostProcessor对象，将其放入容器中
         >
         > 并在invoke中寻找对应的切点，执行切点的方法

