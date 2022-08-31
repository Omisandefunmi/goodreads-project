package africa.semicolon.goodreads.config;

import africa.semicolon.goodreads.models.Role;
import africa.semicolon.goodreads.models.User;
import africa.semicolon.goodreads.models.enums.RoleType;
import africa.semicolon.goodreads.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class SetUpDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SetUpDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(userRepository.findUserByEmail("adminuser@gmail.com").isEmpty()){
            User adminUser = new User("admin", "user", "adminuser@gmail.com",
                    passwordEncoder.encode("0000"), RoleType.ROLE_ADMIN);
            adminUser.setDateJoined(LocalDate.now());
            log.info("creating admin user");
            userRepository.save(adminUser);
            log.info("saving admin user");
        }
    }
}
