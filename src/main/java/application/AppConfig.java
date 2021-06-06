package application;


import spring.ComponentScan;

//自定义包扫描类，把service 包扫描下来
@ComponentScan("application.service")
public class AppConfig {
}
