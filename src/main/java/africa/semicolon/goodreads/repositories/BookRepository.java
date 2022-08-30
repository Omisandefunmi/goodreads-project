package africa.semicolon.goodreads.repositories;

import africa.semicolon.goodreads.models.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {

}
