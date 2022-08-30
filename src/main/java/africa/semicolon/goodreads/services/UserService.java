package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.dtos.UserDto;
import africa.semicolon.goodreads.exceptions.GoodReadException;
import org.springframework.stereotype.Service;


public interface UserService {
    public UserDto createUserAccount(String host, CreateAccountRequest request) throws GoodReadException;
}
