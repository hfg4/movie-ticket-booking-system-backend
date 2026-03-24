package com.backend.movie_ticket_booking_system.request;

import com.backend.movie_ticket_booking_system.enums.Genre;
import com.backend.movie_ticket_booking_system.enums.Language;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.sql.Date;

@Data
public class MovieRequest {
    @NotBlank(message = "Movie name is required")
    @Size(min = 1, max = 200, message = "Movie name must be between 1 and 200 characters")
    private String movieName;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotNull(message = "Release date is required")
    private Date releaseDate;

    @NotNull(message = "Genre is required")
    private Genre genre;

    @NotNull(message = "Language is required")
    private Language language;

    private String actors;

    private String description;
    private String movieImage;
    private Boolean isBanner;
}
