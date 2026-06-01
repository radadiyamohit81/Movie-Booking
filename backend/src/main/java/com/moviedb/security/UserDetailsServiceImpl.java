package com.moviedb.security;

import com.moviedb.model.User;
import com.moviedb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bridges our User entity with Spring Security's UserDetailsService contract.
 *
 * WHY @Transactional(readOnly = true)?
 *   loadUserByUsername is called on every authenticated request (via JwtFilter).
 *   readOnly=true tells Hibernate to skip dirty-checking and use a read-only
 *   connection, reducing overhead for this high-frequency path.
 *
 * WHY SimpleGrantedAuthority(user.getRole())?
 *   The role stored in the DB is already "ROLE_USER" / "ROLE_ADMIN" which is
 *   exactly what Spring Security expects for hasRole() / hasAuthority() checks.
 *   No translation needed.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }
}
