package vn.iotstar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            Role userRole = roleRepository
                    .findByName("ROLE_USER")
                    .orElseGet(() ->
                        roleRepository.save(
                            Role.builder()
                                .name("ROLE_USER")
                                .build()
                        )
                    );

            Role adminRole = roleRepository
                    .findByName("ROLE_ADMIN")
                    .orElseGet(() ->
                        roleRepository.save(
                            Role.builder()
                                .name("ROLE_ADMIN")
                                .build()
                        )
                    );

            // Seed default account: lyvomyhoa (Lý Võ Mỹ Hoa - 24133017)
            User lyvomyhoa = userRepository.findByUsername("lyvomyhoa")
                    .or(() -> userRepository.findByEmail("lyvomyhoa@gmail.com"))
                    .orElse(null);

            if (lyvomyhoa == null) {
                lyvomyhoa = User.builder()
                        .username("lyvomyhoa")
                        .email("lyvomyhoa@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Lý Võ Mỹ Hoa")
                        .role(adminRole)
                        .enabled(true)
                        .build();
                userRepository.save(lyvomyhoa);
            } else {
                lyvomyhoa.setRole(adminRole);
                lyvomyhoa.setEnabled(true);
                lyvomyhoa.setFullName("Lý Võ Mỹ Hoa");
                userRepository.save(lyvomyhoa);
            }

            // Seed admin
            User admin = userRepository.findByUsername("admin")
                    .or(() -> userRepository.findByEmail("trunghnpk@gmail.com"))
                    .orElse(null);

            if (admin == null) {
                admin = User.builder()
                        .username("admin")
                        .email("trunghnpk@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Administrator")
                        .role(adminRole)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
            } else {
                admin.setRole(adminRole);
                admin.setEnabled(true);
                userRepository.save(admin);
            }

            // Seed trunghn
            User trunghn = userRepository.findByUsername("trunghn")
                    .or(() -> userRepository.findByEmail("trunghn@hcmute.edu.vn"))
                    .orElse(null);

            if (trunghn == null) {
                trunghn = User.builder()
                        .username("trunghn")
                        .email("trunghn@hcmute.edu.vn")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Nguyễn Hữu Trung")
                        .role(userRole)
                        .enabled(true)
                        .build();
                userRepository.save(trunghn);
            } else {
                trunghn.setRole(userRole);
                trunghn.setEnabled(true);
                userRepository.save(trunghn);
            }

            // Seed user01 (for compatibility with existing tests/seeds)
            User user01 = userRepository.findByUsername("user01")
                    .or(() -> userRepository.findByEmail("user01@gmail.com"))
                    .orElse(null);

            if (user01 == null) {
                user01 = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Nguyễn Hữu Trung")
                        .role(userRole)
                        .enabled(true)
                        .build();
                userRepository.save(user01);
            } else {
                user01.setRole(userRole);
                user01.setEnabled(true);
                userRepository.save(user01);
            }
        };
    }
}
