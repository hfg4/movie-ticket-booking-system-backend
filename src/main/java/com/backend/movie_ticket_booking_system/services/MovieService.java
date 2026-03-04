package com.backend.movie_ticket_booking_system.services;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.movie_ticket_booking_system.convertor.MovieConvertor;
import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.exceptions.MovieAlreadyExist;
import com.backend.movie_ticket_booking_system.exceptions.MovieDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.request.MovieRequest;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public String addMovie(MovieRequest movieRequest) {
        Movie movieByName = movieRepository.findByMovieName(movieRequest.getMovieName());

        if (movieByName != null && movieByName.getLanguage().equals(movieRequest.getLanguage())) {
            throw new MovieAlreadyExist();
        }

        Movie movie = MovieConvertor.movieDtoToMovie(movieRequest);

        movieRepository.save(movie);
        return "The movie has been added successfully";
    }

    public String addMovieImage(String movieImage, @Valid MovieRequest movieId) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }
        Movie movie = movieOpt.get();
        movie.setMovieImage(movieImage);
        movieRepository.save(movie);
        return "The movie image has been added successfully";
    }

    public Movie getMovieById(Integer movieId) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);

        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        return movieOpt.get();
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieByName(String movieName) {
        Movie movie = movieRepository.findByMovieName(movieName);

        if (movie == null) {
            throw new MovieDoesNotExist();
        }

        return movie;
    }

    public String updateMovie(Integer movieId, MovieRequest movieRequest) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);

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
        if (movieRequest.getRating() != null) {
            movie.setRating(movieRequest.getRating());
        }
        if (movieRequest.getReleaseDate() != null) {
            movie.setReleaseDate(movieRequest.getReleaseDate());
        }
        if (movieRequest.getGenre() != null) {
            movie.setGenre(movieRequest.getGenre());
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

        movieRepository.save(movie);
        return "Movie updated successfully";
    }

    public String deleteMovie(Integer movieId) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);

        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        movieRepository.deleteById(movieId);
        return "Movie deleted successfully";
    }

}
