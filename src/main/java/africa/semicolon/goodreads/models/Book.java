package africa.semicolon.goodreads.models;

import africa.semicolon.goodreads.models.enums.AgeRate;
import africa.semicolon.goodreads.models.enums.Category;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document("books")

public class Book {
    @Id
    private String id;
    private String title;
    private AgeRate ageRate;
    private String author;
    private String description;
    private String coverImageUrl;
    private Category category;



}

