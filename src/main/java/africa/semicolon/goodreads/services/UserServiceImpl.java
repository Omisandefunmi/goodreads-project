package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.controllers.requestsAndResponses.UpdateProfileRequest;
import africa.semicolon.goodreads.dtos.UserDto;

import africa.semicolon.goodreads.events.SendMessageEvent;
import africa.semicolon.goodreads.exceptions.GoodReadsException;
import africa.semicolon.goodreads.models.Role;
import africa.semicolon.goodreads.models.User;
import africa.semicolon.goodreads.models.VerificationMessageRequest;
import africa.semicolon.goodreads.repositories.UserRepository;
import africa.semicolon.goodreads.security.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, ApplicationEventPublisher applicationEventPublisher,
                           BCryptPasswordEncoder bCryptPasswordEncoder, TokenProvider tokenProvider){
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenProvider = tokenProvider;
    }
    @Override
    public UserDto createUserAccount(String host, CreateAccountRequest request) throws GoodReadsException {
        validate(request.getEmail());
        User user = new User(request.getFirstName(), request.getLastName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()));

        user.setDateJoined(LocalDate.now());
        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenForVerification(String.valueOf(savedUser.getId()));
        VerificationMessageRequest message = VerificationMessageRequest.builder()
                .subject("VERIFY EMAIL")
                .sender("omisandefunmi@gmail.com")
                .receiver(request.getEmail())
                .domainUrl(host)
                .verificationToken(token)
                .userFullName(String.format("%s %s", request.getFirstName(), request.getFirstName()))
                .build();

        SendMessageEvent event = new SendMessageEvent(message);
        applicationEventPublisher.publishEvent(event);

        return modelMapper.map(savedUser, UserDto.class);

    }

    @Override
    public UserDto findUserById(String userId) throws GoodReadsException {
        User user = userRepository.findUserById(Long.parseLong(userId)).orElseThrow(
                () -> new GoodReadsException(String.format("User with id %s not found",userId), 400));
        return  modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public UserDto updateUserProfile(String id, UpdateProfileRequest request) throws GoodReadsException {
        User userToBeUpdated = userRepository.findUserById(Long.parseLong(id)).orElseThrow(() ->
                new GoodReadsException(String.format("User with id %s not found", id), 404));
        User updatedUser = modelMapper.map(request, User.class);
        updatedUser.setId(userToBeUpdated.getId());
        updatedUser.setDateJoined(userToBeUpdated.getDateJoined());
        updatedUser.setRoles(userToBeUpdated.getRoles());
        updatedUser.setVerified(userToBeUpdated.isVerified());

        userRepository.save(updatedUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto findUserByEmail(String email) throws GoodReadsException {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new GoodReadsException(String.format("User with email %s not found", email), 404));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public void verifyUser(String token) throws GoodReadsException {
        Claims claims = tokenProvider.getAllClaimsFromJWTToken(token);
        Function<Claims, String> getSubjectFromClaim = Claims::getSubject;
        Function<Claims, Date> getExpirationDateFromClaim = Claims::getExpiration;
        Function<Claims, Date> getIssuedAtDateFromClaim = Claims::getIssuedAt;

        String userId = getSubjectFromClaim.apply(claims);
        if(userId == null){
            throw new GoodReadsException("User id not found in verification token", 404);
        }

        Date expiryDate = getExpirationDateFromClaim.apply(claims);
        if (expiryDate == null){
            throw new GoodReadsException("Expiry date not found in verification token", 404);
        }

        Date issuedAtDate = getIssuedAtDateFromClaim.apply(claims);
        if(issuedAtDate == null){
            throw new GoodReadsException("Issued at date not found in verification token", 404);
        }

        if(expiryDate.compareTo(issuedAtDate) > 14.4){
            throw new GoodReadsException("Verification has already expired", 404);
        }

        User user = findUserByIdInternal(userId);
        if (user == null){
            throw new GoodReadsException("User id does not exist", 404);
        }
        user.setVerified(true);
        userRepository.save(user);
    }

    private User findUserByIdInternal(String userId) {
        return userRepository.findUserById(Long.parseLong(userId)).orElse(null);
    }


    private void validate(String email) throws GoodReadsException {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if(user != null){
            throw new GoodReadsException("User already exists", 400);
        }
    }

    @Override @SneakyThrows
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = userRepository.findUserByEmail(email).orElse(null);
        if(user != null){
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user.getRoles()));
        }
        return null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles){
        return roles.stream()
                .map(
                        role -> new SimpleGrantedAuthority(role.getRoleType().name())
                )
                .collect(Collectors.toSet());
    }
}
