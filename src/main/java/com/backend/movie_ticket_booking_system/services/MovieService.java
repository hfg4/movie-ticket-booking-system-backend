package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.MovieConvertor;
import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.enums.Genre;
import com.backend.movie_ticket_booking_system.exceptions.MovieAlreadyExist;
import com.backend.movie_ticket_booking_system.exceptions.MovieDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.request.MovieRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {


    private final MovieRepository movieRepository;


    private final com.backend.movie_ticket_booking_system.repositories.TicketRepository ticketRepository;

    public String addMovie(MovieRequest movieRequest) {
        Movie movieByName = movieRepository.findByMovieNameAndIsDeletedFalse(movieRequest.getMovieName());

        if (movieByName != null && movieByName.getLanguage().equals(movieRequest.getLanguage())) {
            throw new MovieAlreadyExist();
        }

        Movie movie = MovieConvertor.movieDtoToMovie(movieRequest);
        movie.setRating(0.0); // Initialize with 0

        movieRepository.save(movie);
        return "The movie has been added successfully";
    }

    public String addMovieImage(String movieImage, @Valid MovieRequest movieRequest) {
        Movie movieByName = movieRepository.findByMovieNameAndIsDeletedFalse(movieRequest.getMovieName());

        if (movieByName != null && movieByName.getLanguage().equals(movieRequest.getLanguage())) {
            throw new MovieAlreadyExist();
        }

        Movie movie = MovieConvertor.movieDtoToMovie(movieRequest);
        movie.setMovieImage(movieImage);
        movie.setRating(0.0); // Initialize with 0

        movieRepository.save(movie);
        return "The movie and image have been added successfully";
    }

    public Movie getMovieById(Integer movieId) {
        Optional<Movie> movieOpt = movieRepository.findByIdAndIsDeletedFalse(movieId);

        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        Movie movie = movieOpt.get();
        movie.setRating(calculateAverageRating(movieId));
        return movie;
    }

    public List<Movie> getAllMovies(String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        List<Movie> movies = movieRepository.findAllByIsDeletedFalse(sort);
        for (Movie movie : movies) {
            movie.setRating(calculateAverageRating(movie.getId()));
        }
        return movies;
    }

    public Page<Movie> getAllMovies(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Movie> moviePage = movieRepository.findAllByIsDeletedFalse(pageable);
        for (Movie movie : moviePage.getContent()) {
            movie.setRating(calculateAverageRating(movie.getId()));
        }
        return moviePage;
    }
@SuppressWarnings("unused")
    public List<Movie> getAllMovies() {
        List<Movie> movies = movieRepository.findAllByIsDeletedFalse();
        for (Movie movie : movies) {
            movie.setRating(calculateAverageRating(movie.getId()));
        }
        return movies;
    }

    private Double calculateAverageRating(Integer movieId) {
        Double avg = ticketRepository.getAverageRatingForMovie(movieId);
        if (avg == null) return 0.0;
        // Round to 1 decimal place
        return Math.round(avg * 10.0) / 10.0;
    }

    public Movie getMovieByName(String movieName) {
        Movie movie = movieRepository.findByMovieNameAndIsDeletedFalse(movieName);

        if (movie == null) {
            throw new MovieDoesNotExist();
        }

        movie.setRating(calculateAverageRating(movie.getId()));
        return movie;
    }

    public List<Movie> searchMoviesByActor(String actorName) {
        List<Movie> movies = movieRepository.findByActorsContainingIgnoreCaseAndIsDeletedFalse(actorName);
        for (Movie movie : movies) {
            movie.setRating(calculateAverageRating(movie.getId()));
        }
        return movies;
    }

    public String updateMovie(Integer movieId, MovieRequest movieRequest) {
        Optional<Movie> movieOpt = movieRepository.findByIdAndIsDeletedFalse(movieId);

        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        Movie movie = movieOpt.get();

        if (movieRequest.getMovieName() != null) {
            movie.setMovieName(movieRequest.getMovieName());
        }
        if (movieRequest.getDuration() != null) {
            movie.setDuration(movieRequest.getDuration());
        }
        // Rating is now calculated, removing manual update
        if (movieRequest.getReleaseDate() != null) {
            movie.setReleaseDate(movieRequest.getReleaseDate());
        }
        if (movieRequest.getGenre() != null) {
            movie.setGenre(movieRequest.getGenre());
        }
        if (movieRequest.getActors() != null) {
            movie.setActors(movieRequest.getActors());
        }
        if (movieRequest.getLanguage() != null) {
            movie.setLanguage(movieRequest.getLanguage());
        }
        if (movieRequest.getDescription() != null) {
            movie.setDescription(movieRequest.getDescription());
        }
        if(movieRequest.getMovieImage() != null){
            movie.setMovieImage(movieRequest.getMovieImage());
        }
        if (movieRequest.getIsBanner() != null) {
            movie.setIsBanner(movieRequest.getIsBanner());
        }

        movieRepository.save(movie);
        return "Movie updated successfully";
    }

    @org.springframework.transaction.annotation.Transactional
    public String setMovieBanner(Integer movieId) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(movieId)
                .orElseThrow(MovieDoesNotExist::new);

        // Toggle banner status
        movie.setIsBanner(movie.getIsBanner() == null || !movie.getIsBanner());
        
        movieRepository.save(movie);
        return "Movie banner status updated successfully";
    }

    public String deleteMovie(Integer movieId) {
        Optional<Movie> movieOpt = movieRepository.findByIdAndIsDeletedFalse(movieId);

        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        Movie movie = movieOpt.get();
        movie.setIsDeleted(true);
        movieRepository.save(movie);
        return "Movie deleted successfully";
    }

}
