package africa.semicolon.goodreads.controllers;

import africa.semicolon.goodreads.controllers.requestsAndResponses.ApiResponse;
import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.exceptions.GoodReadException;
import africa.semicolon.goodreads.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<?> createUser(HttpServletRequest httpRequest, @RequestBody @Valid @NotNull CreateAccountRequest request) throws GoodReadException {
        String host = httpRequest.getRequestURL().toString();
        int index = host.indexOf("/", host.indexOf("/", host.indexOf("/"))+2);
        host = host.substring(0, index+1);
        log.info("Host --> {}", host);

        ApiResponse apiResponse = ApiResponse.builder()
                .data(userService.createUserAccount(host, request))
                .message("user created successfully")
                .status("success")
                .build();

        log.info("Returning CreateUser response");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
