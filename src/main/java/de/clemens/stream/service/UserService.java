package de.clemens.stream.service;

import de.clemens.stream.entity.Role;
import de.clemens.stream.entity.User;
import de.clemens.stream.repository.UserRepository;
import de.clemens.stream.security.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public List<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        User existingUser = userRepository.findById(user.getEmail()).orElse(null);

        if (existingUser == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Existing user, check if the password has changed
            if (!existingUser.getPassword().equals(user.getPassword())) {
                // Password has changed, hash the new password
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        return userRepository.save(user);
    }

    public void deleteUser(String email) {
        userRepository.deleteById(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            return new ApplicationUser(optionalUser.get());
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public User registerUser(String email, String username, String password, String passwordConfirm) {
        
        if(!password.equals(passwordConfirm)) return null;

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);

        Role userRole = roleService.getRoleByName("USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return saveUser(user);
    }

    public boolean validateUser(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        return optionalUser.filter(user -> passwordEncoder.matches(password, user.getPassword())).isPresent();
    }
}
