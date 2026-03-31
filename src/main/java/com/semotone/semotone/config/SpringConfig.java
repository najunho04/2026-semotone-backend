package com.semotone.semotone.config;

import com.semotone.semotone.domain.user.controller.UserController;
import com.semotone.semotone.domain.user.repository.UserRepository;
import com.semotone.semotone.domain.user.repository.UserRepositoryImpl;
import com.semotone.semotone.domain.user.service.UserService;
import com.semotone.semotone.domain.user.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean
    public UserRepository userRepository() {
        // 인터페이스 타입으로 구현체를 반환하여 스프링 컨테이너에 등록합니다.
        // 나중에 다른 DB로 바꾸고 싶다면 이 줄의 new 객체만 쓱 바꾸면 됩니다!
        return new UserRepositoryImpl();
    }

    // 2. Service 객체 생성 (방금 만든 Repository를 생성자로 쏙 넣어줍니다)
    @Bean
    public UserService userService() {
        return new UserServiceImpl(userRepository());
    }

    // 3. Controller 객체 생성 (방금 만든 Service를 생성자로 쏙 넣어줍니다)
    @Bean
    public UserController userController() {
        return new UserController(userService());
    }
}
