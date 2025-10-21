package com.bookrecommend.book_recommend_be.service.book;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.*;
import com.bookrecommend.book_recommend_be.repository.AuthorRepository;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.BookTypeRepository;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService implements IBookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookTypeRepository bookTypeRepository;
    private final ModelMapper modelMapper;
    private final MinioClient minioClient;

    @Override
    public Page<BookResponse> getBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable)
                .map(book -> modelMapper.map(book, BookResponse.class));
    }

    @Override
    public Page<BookResponse> getNewestBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findNewestBooks(pageable)
                .map(book -> modelMapper.map(book, BookResponse.class));
    }

    @Override
    public Page<BookResponse> getMostReadBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findMostReadBooks(pageable)
                .map(book -> modelMapper.map(book, BookResponse.class));
    }

    @Override
    public Page<BookResponse> getBooksByGenre(Long genreId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByGenre(genreId, pageable)
                .map(book -> modelMapper.map(book, BookResponse.class));
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return modelMapper.map(book, BookResponse.class);
    }

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());

        Set<Author> finalAuthors = new HashSet<>();
        for (String authorName : request.getAuthorNames()) {
            Author author = authorRepository.findByNameIgnoreCase(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            finalAuthors.add(author);
        }
        book.setAuthors(finalAuthors);

        List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
        if (genres.size() != request.getGenreIds().size()) {
            throw new ResourceNotFoundException("One or more genres not found");
        }
        book.setGenres(new HashSet<>(genres));

        if (request.getFormats() == null || request.getFormats().isEmpty()) {
            throw new IllegalArgumentException("Book must have at least one format");
        }

        List<BookFormat> formats = request.getFormats().stream()
                .map(f -> {
                    BookType type = bookTypeRepository.findById(f.getTypeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Format type not found"));
                    BookFormat format = new BookFormat();
                    format.setBook(book);
                    format.setType(type);
                    format.setContentUrl(f.getContentUrl());
                    format.setTotalPages(f.getTotalPages());
                    format.setFileSizeKb(f.getFileSizeKb());
                    return format;
                })
                .toList();

        book.setFormats(formats);

        Book savedBook = bookRepository.save(book);
        return modelMapper.map(savedBook, BookResponse.class);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);

        modelMapper.typeMap(BookRequest.class, Book.class)
                .addMappings(mapper -> {
                    mapper.skip(Book::setAuthors);
                    mapper.skip(Book::setGenres);
                    mapper.skip(Book::setFormats);
                })
                .map(request, book);

        Set<Author> finalAuthors = new HashSet<>();
        for (String authorName : request.getAuthorNames()) {
            Author author = authorRepository.findByNameIgnoreCase(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            finalAuthors.add(author);
        }
        book.setAuthors(finalAuthors);

        List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
        if (genres.size() != request.getGenreIds().size()) {
            throw new ResourceNotFoundException("One or more genres not found");
        }
        book.setGenres(new HashSet<>(genres));

        book.getFormats().clear();
        List<BookFormat> formats = request.getFormats().stream()
                .map(f -> {
                    BookType type = bookTypeRepository.findById(f.getTypeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Format type not found"));
                    BookFormat format = new BookFormat();
                    format.setBook(book);
                    format.setType(type);
                    format.setContentUrl(f.getContentUrl());
                    format.setTotalPages(f.getTotalPages());
                    format.setFileSizeKb(f.getFileSizeKb());
                    return format;
                })
                .toList();
        book.getFormats().addAll(formats);

        Book updatedBook = bookRepository.save(book);
        return modelMapper.map(updatedBook, BookResponse.class);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        bookRepository.delete(book);
    }

    @Override
    public Page<BookResponse> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.searchBooks(keyword, pageable)
                .map(book -> modelMapper.map(book, BookResponse.class));
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }
}
