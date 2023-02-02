package africa.semicolon.goodreads.services;

import africa.semicolon.goodreads.controllers.requestsAndResponses.CreateAccountRequest;
import africa.semicolon.goodreads.controllers.requestsAndResponses.UpdateProfileRequest;
import africa.semicolon.goodreads.dtos.UserDto;
import africa.semicolon.goodreads.exceptions.GoodReadsException;


import java.util.List;


public interface UserService {
    UserDto createUserAccount(String host, CreateAccountRequest request) throws GoodReadsException;
    UserDto  findUserById(String userId) throws GoodReadsException;

    List<UserDto> findAllUsers();
    UserDto updateUserProfile(String id, UpdateProfileRequest request) throws GoodReadsException;
    UserDto findUserByEmail(String email) throws GoodReadsException;

    void verifyUser(String token) throws GoodReadsException;
}
