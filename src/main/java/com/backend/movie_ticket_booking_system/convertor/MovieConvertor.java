package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.request.MovieRequest;

public class MovieConvertor {

    public static Movie movieDtoToMovie(MovieRequest movieRequest) {

        return Movie.builder()
                .movieName(movieRequest.getMovieName())
                .duration(movieRequest.getDuration())
                .genre((movieRequest.getGenre()))
                .language(movieRequest.getLanguage())
                .releaseDate(movieRequest.getReleaseDate())
                .description(movieRequest.getDescription())
                .movieImage(movieRequest.getMovieImage())
                .isBanner(movieRequest.getIsBanner() != null ? movieRequest.getIsBanner() : false)
                .build();
    }
}