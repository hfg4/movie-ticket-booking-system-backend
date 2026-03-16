package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.request.MovieRequest;
import com.backend.movie_ticket_booking_system.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/addNew")
    public ResponseEntity<String> addMovie(@Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovie(movieRequest));
    }

    @PostMapping("/addNew/{movieImage}")
    public ResponseEntity<String> addMovieWithImage(@PathVariable String movieImage, @Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovieImage(movieImage, movieRequest));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovieById(movieId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        return ResponseEntity.ok(movieService.getAllMovies(sortBy, sortDir));
    }

    @GetMapping("/all/paginated")
    public ResponseEntity<Page<Movie>> getAllMoviesPaginated(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        return ResponseEntity.ok(movieService.getAllMovies(pageNo, pageSize, sortBy, sortDir));
    }

    @GetMapping("/name/{movieName}")
    public ResponseEntity<Movie> getMovieByName(@PathVariable String movieName) {
        return ResponseEntity.ok(movieService.getMovieByName(movieName));
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<String> updateMovie(@PathVariable Integer movieId, @Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieRequest));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }
}