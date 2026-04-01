package com.semotone.semotone.config;

import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.ai.repository.AiRepositoryImpl;
import com.semotone.semotone.domain.ai.repository.aiRepository;
import com.semotone.semotone.domain.ai.service.AiService;
import com.semotone.semotone.domain.ai.service.AiServiceImpl;
import com.semotone.semotone.domain.post.controller.PostController;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.post.repository.PostRepositoryImpl;
import com.semotone.semotone.domain.post.service.PostService;
import com.semotone.semotone.domain.post.service.PostServiceImpl;
import com.semotone.semotone.domain.user.controller.UserController;
import com.semotone.semotone.domain.user.repository.UserRepository;
import com.semotone.semotone.domain.user.repository.UserRepositoryImpl;
import com.semotone.semotone.domain.user.service.UserService;
import com.semotone.semotone.domain.user.service.UserServiceImpl;
import com.semotone.semotone.util.GeminiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    // 1. 파라미터로 Firestore를 받습니다.
    // 스프링이 FirebaseConfig에서 생성된 Firestore 빈을 알아서 주입해 줍니다!
    @Bean
    public UserRepository userRepository(Firestore firestore) {
        // 인터페이스 타입으로 구현체를 반환하여 스프링 컨테이너에 등록합니다.
        // 나중에 다른 DB로 바꾸고 싶다면 이 줄의 new 객체만 쓱 바꾸면 됩니다!
        return new UserRepositoryImpl(firestore);
    }

    // 2. Service 객체 생성 (방금 만든 Repository를 생성자로 쏙 넣어줍니다)
    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    // 3. Controller 객체 생성 (방금 만든 Service를 생성자로 쏙 넣어줍니다)
    @Bean
    public UserController userController(UserService userService) {
        return new UserController(userService);
    }

    @Bean
    public PostRepository postRepository(Firestore firestore){
        return new PostRepositoryImpl(firestore);
    }

    @Bean
    public GeminiClient geminiClient() {
        // 실제 API 키를 발급받은 후 "INPUT-GEMINI-API-KEY"를 교체하세요
        String apiKey = "INPUT-GEMINI-API-KEY";
        return new GeminiClient(apiKey);
    }

    @Bean
    public aiRepository aiRepository(Firestore firestore) {
        return new AiRepositoryImpl(firestore);
    }

    @Bean
    public AiService aiService(aiRepository aiRepository, PostRepository postRepository, GeminiClient geminiClient) {
        return new AiServiceImpl(aiRepository, postRepository, geminiClient);
    }

    @Bean
    public PostService postService(PostRepository postRepository, UserService userService, AiService aiService){
        return new PostServiceImpl(postRepository, userService, aiService);
    }

    @Bean
    public PostController postController(PostService postService, AiService aiService){
        return new PostController(postService, aiService);
    }

}
