package africa.semicolon.goodreads.controllers;

import africa.semicolon.goodreads.controllers.requestsAndResponses.ApiResponse;
import africa.semicolon.goodreads.exceptions.GoodReadsException;
import africa.semicolon.goodreads.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class BookController {
    private BookService bookService;
    private List<String> validFileExtensions;
    private List<String> validImageExtensions;

    public BookController(BookService bookService, List<String> validFileExtension, List<String> validImageExtension) {
        this.bookService = bookService;
        this.validImageExtensions = Arrays.asList(".png", ".jpg",".jpeg");
        this.validFileExtensions = Arrays.asList(".txt", ".pdf", ".doc", ".docx", ".csv",
                ".epub", ".xlsx");;
    }
    @GetMapping("/upload")
    public ResponseEntity<?> getUploadUrls(
            @RequestParam("fileExtension") @Valid @NotBlank @NotNull String fileExtension,
            @RequestParam("imageExtension") @Valid @NotBlank @NotNull String imageExtension){
        try {
            if (!validFileExtensions.contains(fileExtension)){
                throw new GoodReadsException("file extension not accepted", 400);
            }
            if (!validImageExtensions.contains(imageExtension)){
                throw new GoodReadsException("image extension not accepted", 400);
            }
            Map<String, String> map = bookService.generateUploadURLs(fileExtension, imageExtension).get();
            ApiResponse apiResponse = ApiResponse.builder()
                    .status("success")
                    .message("upload urls created")
                    .data(map)
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        } catch (GoodReadsException | ExecutionException | InterruptedException e) {
            log.info(e.getMessage());
            e.printStackTrace();
            ApiResponse apiResponse = ApiResponse.builder()
                    .status("fail")
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
