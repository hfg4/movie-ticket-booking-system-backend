package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.request.MovieRequest;
import com.backend.movie_ticket_booking_system.services.CloudinaryService;
import com.backend.movie_ticket_booking_system.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;
    private final CloudinaryService cloudinaryService;

    public MovieController(MovieService movieService, CloudinaryService cloudinaryService) {
        this.movieService = movieService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/addNew")
    public ResponseEntity<String> addMovie(@Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovie(movieRequest));
    }

    @PostMapping("/addNewWithImageUrl")
    public ResponseEntity<String> addMovieWithImage(@RequestParam("imageUrl") String movieImage, @Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovieImage(movieImage, movieRequest));
    }

    @PostMapping(value = "/addNewWithImageUpload", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addMovieWithImageUpload(
            @RequestPart("movie") @Valid MovieRequest movieRequest,
            @RequestPart("image") MultipartFile image) {
        try {
            String imageUrl = cloudinaryService.uploadImage(image);
            return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovieImage(imageUrl, movieRequest));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{movieId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadMovieImage(@PathVariable Integer movieId, @RequestPart("image") MultipartFile image) {
        try {
            String imageUrl = cloudinaryService.uploadImage(image);
            MovieRequest movieRequest = new MovieRequest();
            movieRequest.setMovieImage(imageUrl);
            return ResponseEntity.ok(movieService.updateMovie(movieId, movieRequest));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
        }
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
    
    @GetMapping("/actor/{actorName}")
    public ResponseEntity<List<Movie>> searchMoviesByActor(@PathVariable String actorName) {
        return ResponseEntity.ok(movieService.searchMoviesByActor(actorName));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam("name") String movieName) {
        return ResponseEntity.ok(movieService.searchMovies(movieName));
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<String> updateMovie(@PathVariable Integer movieId, @Valid @RequestBody MovieRequest movieRequest) {
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieRequest));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }

    @PutMapping("/{movieId}/banner")
    public ResponseEntity<String> setMovieBanner(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.setMovieBanner(movieId));
    }
}
