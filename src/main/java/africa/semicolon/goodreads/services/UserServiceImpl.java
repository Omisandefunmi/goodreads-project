package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.controllers.requestsAndResponses.UpdateProfileRequest;
import africa.semicolon.goodreads.dtos.UserDto;

import africa.semicolon.goodreads.events.SendMessageEvent;
import africa.semicolon.goodreads.exceptions.GoodReadException;
import africa.semicolon.goodreads.models.User;
import africa.semicolon.goodreads.models.VerificationMessageRequest;
import africa.semicolon.goodreads.repositories.UserRepository;
import africa.semicolon.goodreads.security.jwt.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
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
    public UserDto createUserAccount(String host, CreateAccountRequest request) throws GoodReadException {
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
    public UserDto findUserById(String userId) throws GoodReadException {
        User user = userRepository.findUserById(Long.parseLong(userId)).orElseThrow(
                () -> new GoodReadException(String.format("User with id %s not found",userId), 400));
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
    public UserDto updateUserProfile(String id, UpdateProfileRequest request) throws GoodReadException {
        User userToBeUpdated = userRepository.findUserById(Long.parseLong(id)).orElseThrow(() ->
                new GoodReadException(String.format("User with id %s not found", id), 404));
        User updatedUser = modelMapper.map(request, User.class);
        updatedUser.setId(userToBeUpdated.getId());
        updatedUser.setDateJoined(userToBeUpdated.getDateJoined());
        updatedUser.setRoles(userToBeUpdated.getRoles());
        updatedUser.setIsVerified(userToBeUpdated.getIsVerified());

        userRepository.save(updatedUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto findUserByEmail(String email) throws GoodReadException {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new GoodReadException(String.format("User with email %s not found", email), 404));
        return modelMapper.map(user, UserDto.class);
    }


    private void validate(String email) throws GoodReadException {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if(user != null){
            throw new GoodReadException("User already exists", 400);
        }
    }

}
