package com.bookrecommend.book_recommend_be.security.userdetails;

import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("username not found: " + username);
        }

        if (user.isBan()) {
            throw new DisabledException("user is banned");
        }

//        if (!user.isActivate()) {
//            throw new DisabledException("user is not activated");
//        }

        return AppUserDetails.buildUserDetails(user);
    }
}

