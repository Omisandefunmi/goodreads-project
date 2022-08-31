package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.controllers.requestsAndResponses.UpdateProfileRequest;
import africa.semicolon.goodreads.dtos.UserDto;
import africa.semicolon.goodreads.exceptions.GoodReadException;
import africa.semicolon.goodreads.models.User;


import java.util.List;


public interface UserService {
    UserDto createUserAccount(String host, CreateAccountRequest request) throws GoodReadException;
    UserDto  findUserById(String userId) throws GoodReadException;

    List<UserDto> findAllUsers();
    UserDto updateUserProfile(String id, UpdateProfileRequest request) throws GoodReadException;
    UserDto findUserByEmail(String email) throws GoodReadException;

    void verifyUser(String token) throws GoodReadException;
}
