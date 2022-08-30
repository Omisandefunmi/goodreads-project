package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
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

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, ApplicationEventPublisher applicationEventPublisher, BCryptPasswordEncoder bCryptPasswordEncoder, TokenProvider tokenProvider){
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

    private void validate(String email) throws GoodReadException {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if(user != null){
            throw new GoodReadException("User already exists", 400);
        }
    }

}
