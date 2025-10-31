package com.bookrecommend.book_recommend_be.service.genre;

import com.bookrecommend.book_recommend_be.dto.request.GenreRequest;
import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.Genre;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<GenreResponse> getGenres(int page, int size, String keyword, String sortOption) {
        Sort sort = resolveSort(sortOption);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Genre> specification = buildSpecification(keyword);
        return genreRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        String name = normalizeName(request.getName());
        validateUniqueName(name, null);

        Genre genre = new Genre();
        genre.setName(name);
        genre.setDescription(normalizeDescription(request.getDescription()));

        Genre saved = genreRepository.save(genre);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        Genre genre = findGenreOrThrow(id);

        String name = normalizeName(request.getName());
        validateUniqueName(name, id);

        genre.setName(name);
        genre.setDescription(normalizeDescription(request.getDescription()));

        Genre saved = genreRepository.save(genre);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = findGenreOrThrow(id);

        Set<Book> relatedBooks = new HashSet<>(genre.getBooks());
        relatedBooks.forEach(book -> book.getGenres().remove(genre));
        if (!relatedBooks.isEmpty()) {
            bookRepository.saveAll(relatedBooks);
        }

        genreRepository.delete(genre);
    }

    private Specification<Genre> buildSpecification(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return (root, query, cb) -> cb.conjunction();
        }

        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        String likePattern = "%" + normalized + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), likePattern),
                cb.like(cb.lower(root.get("description")), likePattern)
        );
    }

    private Sort resolveSort(String sortOption) {
        String normalized = StringUtils.hasText(sortOption)
                ? sortOption.trim().toLowerCase(Locale.ROOT)
                : "name-asc";

        return switch (normalized) {
            case "name-desc" -> Sort.by(Sort.Order.desc("name").ignoreCase());
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "id");
            default -> Sort.by(Sort.Order.asc("name").ignoreCase());
        };
    }

    private Genre findGenreOrThrow(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    private void validateUniqueName(String name, Long excludeId) {
        boolean exists = excludeId == null
                ? genreRepository.existsByNameIgnoreCase(name)
                : genreRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (exists) {
            throw new IllegalArgumentException("Genre name already exists");
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeDescription(String description) {
        return StringUtils.hasText(description) ? description.trim() : null;
    }

    private GenreResponse mapToResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getDescription()
        );
    }
}
