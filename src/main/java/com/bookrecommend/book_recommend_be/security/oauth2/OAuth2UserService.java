package com.bookrecommend.book_recommend_be.security.oauth2;

import com.bookrecommend.book_recommend_be.model.Role;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.RoleRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(provider, oAuth2User);
    }

    private OAuth2User processOAuth2User(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, provider);

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Can't take email from provider: " + provider);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = createUser(attributes, email, provider);
            log.info("Created new OAuth2 user from {}: {}", provider, email);
        } else if (!user.isActivate()) {
            user.setActivate(true);
            userRepository.save(user);
        }

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority(user.getRole().getName())),
                attributes,
                "email"
        );
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private User createUser(Map<String, Object> attributes, String email, String provider) {
        String name = (String) attributes.getOrDefault("name", "User");
        String picture = null;
        String providerId = null;

        if ("google".equalsIgnoreCase(provider)) {
            picture = (String) attributes.get("picture");
            providerId = (String) attributes.get("sub");

        } else if ("facebook".equalsIgnoreCase(provider)) {
            providerId = (String) attributes.get("id");

            Object pictureObj = attributes.get("picture");
            if (pictureObj instanceof Map) {
                Object dataObj = ((Map<?, ?>) pictureObj).get("data");
                if (dataObj instanceof Map) {
                    Object urlObj = ((Map<?, ?>) dataObj).get("url");
                    if (urlObj instanceof String) {
                        picture = (String) urlObj;
                    }
                }
            }
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = User.builder()
                .email(email)
                .username(email.split("@")[0])
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .avatarUrl(picture)
                .fullName(name)
                .role(role)
                .activate(true)
                .build();

        return userRepository.save(user);
    }
}