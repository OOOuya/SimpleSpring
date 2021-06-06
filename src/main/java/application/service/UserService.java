package application.service;

import spring.Component;
import spring.Scope;

@Component("userService")
@Scope("singleton")
public interface UserService {
      void test();
}
