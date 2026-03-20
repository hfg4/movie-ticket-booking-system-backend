package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.enums.Language;
import com.backend.movie_ticket_booking_system.exceptions.MovieAlreadyExist;
import com.backend.movie_ticket_booking_system.exceptions.MovieDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.repositories.TicketRepository;
import com.backend.movie_ticket_booking_system.request.MovieRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KIỂM THỬ HỘP TRẮNG - MovieService
 *
 * Kiểm tra từng nhánh logic (branch coverage) bên trong các phương thức,
 * sử dụng Mockito để mock repository, không cần database thật.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private MovieService movieService;

    private MovieRequest movieRequest;
    private Movie existingMovie;

    @BeforeEach
    void setUp() {
        movieRequest = new MovieRequest();
        movieRequest.setMovieName("Avengers: Endgame");
        movieRequest.setDuration(181);
        movieRequest.setReleaseDate(Date.valueOf("2024-04-26"));
        movieRequest.setGenre("Action");
        movieRequest.setLanguage(Language.ENGLISH);
        movieRequest.setDescription("The Avengers assemble once more.");

        existingMovie = Movie.builder()
                .id(1)
                .movieName("Avengers: Endgame")
                .duration(181)
                .rating(4.5)
                .language(Language.ENGLISH)
                .genre("Action")
                .isDeleted(false)
                .build();
    }

    // =========================================================================
    // WB01: addMovie() — Phim chưa tồn tại → thêm thành công
    // Branch: movieByName == null → bỏ qua if → save
    // =========================================================================
    @Test
    @DisplayName("WB01 - addMovie: Phim mới chưa tồn tại → thêm thành công")
    void addMovie_WhenMovieNotExist_ShouldSaveSuccessfully() {
        // Arrange: repository trả về null (chưa có phim trùng tên)
        when(movieRepository.findByMovieNameAndIsDeletedFalse("Avengers: Endgame")).thenReturn(null);
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);

        // Act
        String result = movieService.addMovie(movieRequest);

        // Assert
        assertEquals("The movie has been added successfully", result);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    // =========================================================================
    // WB02: addMovie() — Phim trùng tên + trùng ngôn ngữ → MovieAlreadyExist
    // Branch: movieByName != null && language match → throw exception
    // =========================================================================
    @Test
    @DisplayName("WB02 - addMovie: Phim trùng tên và ngôn ngữ → throw MovieAlreadyExist")
    void addMovie_WhenDuplicateNameAndLanguage_ShouldThrowException() {
        // Arrange: trả về phim đã tồn tại với cùng ngôn ngữ
        when(movieRepository.findByMovieNameAndIsDeletedFalse("Avengers: Endgame")).thenReturn(existingMovie);

        // Act & Assert
        assertThrows(MovieAlreadyExist.class, () -> movieService.addMovie(movieRequest));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    // =========================================================================
    // WB03: addMovie() — Trùng tên nhưng khác ngôn ngữ → thêm thành công
    // Branch: movieByName != null && language NOT match → bỏ qua if → save
    // =========================================================================
    @Test
    @DisplayName("WB03 - addMovie: Trùng tên nhưng khác ngôn ngữ → thêm thành công")
    void addMovie_WhenSameNameDifferentLanguage_ShouldSaveSuccessfully() {
        // Arrange: phim trùng tên nhưng ngôn ngữ khác (VIETNAMESE thay vì ENGLISH)
        Movie vietnameseVersion = Movie.builder()
                .id(2)
                .movieName("Avengers: Endgame")
                .language(Language.VIETNAMESE)
                .build();
        when(movieRepository.findByMovieNameAndIsDeletedFalse("Avengers: Endgame")).thenReturn(vietnameseVersion);
        when(movieRepository.save(any(Movie.class))).thenReturn(existingMovie);

        // Act: request là ENGLISH, DB đã có VIETNAMESE → cho phép thêm
        String result = movieService.addMovie(movieRequest);

        // Assert
        assertEquals("The movie has been added successfully", result);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    // =========================================================================
    // WB04: getMovieById() — Movie tồn tại → trả về Movie + tính rating
    // Branch: movieOpt.isPresent → tính calculateAverageRating → return
    // =========================================================================
    @Test
    @DisplayName("WB04 - getMovieById: Movie tồn tại → trả về với rating đã tính")
    void getMovieById_WhenExists_ShouldReturnMovieWithCalculatedRating() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(existingMovie));
        when(ticketRepository.getAverageRatingForMovie(1)).thenReturn(4.567);

        // Act
        Movie result = movieService.getMovieById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Avengers: Endgame", result.getMovieName());
        assertEquals(4.6, result.getRating()); // 4.567 → round to 4.6
    }

    // =========================================================================
    // WB05: getMovieById() — Movie không tồn tại → throw MovieDoesNotExist
    // Branch: movieOpt.isEmpty → throw
    // =========================================================================
    @Test
    @DisplayName("WB05 - getMovieById: Movie không tồn tại → throw MovieDoesNotExist")
    void getMovieById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MovieDoesNotExist.class, () -> movieService.getMovieById(999));
    }

    // =========================================================================
    // WB06: updateMovie() — Cập nhật từng field (kiểm tra null check branches)
    // Branch: mỗi if(field != null) → set field
    // =========================================================================
    @Test
    @DisplayName("WB06 - updateMovie: Chỉ cập nhật các field có giá trị (not null)")
    void updateMovie_ShouldOnlyUpdateNonNullFields() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(existingMovie));

        MovieRequest updateRequest = new MovieRequest();
        updateRequest.setMovieName("Avengers: Updated");
        updateRequest.setDuration(200);
        // releaseDate, genre, language = null → không cập nhật

        // Act
        String result = movieService.updateMovie(1, updateRequest);

        // Assert
        assertEquals("Movie updated successfully", result);
        assertEquals("Avengers: Updated", existingMovie.getMovieName());
        assertEquals(200, existingMovie.getDuration());
        // language giữ nguyên vì updateRequest.getLanguage() = null
        assertEquals(Language.ENGLISH, existingMovie.getLanguage());
        verify(movieRepository, times(1)).save(existingMovie);
    }

    // =========================================================================
    // WB07: deleteMovie() — Soft delete (isDeleted = true)
    // Branch: movieOpt.isPresent → set isDeleted = true → save
    // =========================================================================
    @Test
    @DisplayName("WB07 - deleteMovie: Soft delete, set isDeleted = true")
    void deleteMovie_WhenExists_ShouldSoftDelete() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(existingMovie));

        // Act
        String result = movieService.deleteMovie(1);

        // Assert
        assertEquals("Movie deleted successfully", result);
        assertTrue(existingMovie.getIsDeleted()); // isDeleted phải là true
        verify(movieRepository, times(1)).save(existingMovie);
    }

    // =========================================================================
    // WB08: deleteMovie() — Movie không tồn tại → throw
    // Branch: movieOpt.isEmpty → throw MovieDoesNotExist
    // =========================================================================
    @Test
    @DisplayName("WB08 - deleteMovie: Movie không tồn tại → throw MovieDoesNotExist")
    void deleteMovie_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MovieDoesNotExist.class, () -> movieService.deleteMovie(999));
        verify(movieRepository, never()).save(any());
    }

    // =========================================================================
    // WB09: calculateAverageRating() — avg = null → return 0.0
    // Branch: avg == null → return 0.0 (gián tiếp qua getMovieById)
    // =========================================================================
    @Test
    @DisplayName("WB09 - calculateAverageRating: Không có rating nào → trả về 0.0")
    void calculateAverageRating_WhenNull_ShouldReturnZero() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(existingMovie));
        when(ticketRepository.getAverageRatingForMovie(1)).thenReturn(null);

        // Act
        Movie result = movieService.getMovieById(1);

        // Assert
        assertEquals(0.0, result.getRating());
    }

    // =========================================================================
    // WB10: calculateAverageRating() — avg = 4.567 → return 4.6 (round 1 decimal)
    // Branch: avg != null → round(avg * 10) / 10
    // =========================================================================
    @Test
    @DisplayName("WB10 - calculateAverageRating: avg=4.567 → làm tròn 4.6")
    void calculateAverageRating_WhenHasValue_ShouldRoundToOneDecimal() {
        // Arrange
        when(movieRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(existingMovie));
        when(ticketRepository.getAverageRatingForMovie(1)).thenReturn(4.567);

        // Act
        Movie result = movieService.getMovieById(1);

        // Assert: Math.round(4.567 * 10.0) / 10.0 = Math.round(45.67) / 10.0 = 46 / 10.0 = 4.6
        assertEquals(4.6, result.getRating());
    }
}
