package com.example.bankcards.service;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO create(String username, String rawPassword, String email, String fullName, Set<String> rolesInput) {
        if (userRepository.existsByUsername(username)) throw new BadRequestException("Username exists");
        if (userRepository.existsByEmail(email)) throw new BadRequestException("Email exists");
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEmail(email);
        u.setFullName(fullName);
        Set<UserRole> roles = new HashSet<>();
        if (rolesInput != null && !rolesInput.isEmpty()) {
            for (String r : rolesInput) roles.add(UserRole.valueOf(r.toUpperCase()));
        } else roles.add(UserRole.USER);
        u.setRoles(roles);
        return toDTO(userRepository.save(u));
    }
	@Transactional(readOnly = true)
	public List<UserDTO> all() { return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList()); }
	@Transactional(readOnly = true)
	public UserDTO byId(Long id) {
		User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		return toDTO(u);
	}
	@Transactional(readOnly = true)
	public UserDTO currentProfile() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User current = userRepository.findByUsername(auth.getName())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return toDTO(current);
	}
	@Transactional public void delete(Long id) { if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User not found"); userRepository.deleteById(id); }

	private UserDTO toDTO(User u) {
		UserDTO d = new UserDTO();
		d.setId(u.getId()); d.setUsername(u.getUsername()); d.setEmail(u.getEmail()); d.setFullName(u.getFullName());
		d.setRoles(u.getRoles().stream().map(UserRole::name).collect(Collectors.toSet()));
		return d;
	}
}

