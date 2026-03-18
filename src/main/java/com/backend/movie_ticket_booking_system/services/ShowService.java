package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.ShowConvertor;
import com.backend.movie_ticket_booking_system.entities.*;
import com.backend.movie_ticket_booking_system.enums.SeatType;
import com.backend.movie_ticket_booking_system.exceptions.MovieDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.ShowDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TheaterDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.repositories.ShowRepository;
import com.backend.movie_ticket_booking_system.repositories.TheaterRepository;
import com.backend.movie_ticket_booking_system.request.ShowRequest;
import com.backend.movie_ticket_booking_system.request.ShowSeatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ShowRepository showRepository;

    public String addShow(ShowRequest showRequest) {
        Show show = ShowConvertor.showDtoToShow(showRequest);

        Optional<Movie> movieOpt = movieRepository.findById(showRequest.getMovieId());
        if (movieOpt.isEmpty()) {
            throw new MovieDoesNotExist();
        }

        Optional<Theater> theaterOpt = theaterRepository.findById(showRequest.getTheaterId());
        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExist();
        }

        Theater theater = theaterOpt.get();
        Movie movie = movieOpt.get();

        show.setMovie(movie);
        show.setTheater(theater);
        show = showRepository.save(show);

        movie.getShows().add(show);
        theater.getShowList().add(show);

        movieRepository.save(movie);
        theaterRepository.save(theater);

        return String.valueOf(show.getShowId());
    }

    public String associateShowSeats(ShowSeatRequest showSeatRequest) {
        Optional<Show> showOpt = showRepository.findById(showSeatRequest.getShowId());
        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExist();
        }

        Show show = showOpt.get();
        Theater theater = show.getTheater();

        List<TheaterSeat> theaterSeatList = theater.getTheaterSeatList();
        List<ShowSeat> showSeatList = show.getShowSeatList();

        for (TheaterSeat theaterSeat : theaterSeatList) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setTheaterSeat(theaterSeat);

            if (theaterSeat.getSeatType().equals(SeatType.STANDARD)) {
                showSeat.setPrice((double) showSeatRequest.getPriceOfClassicSeat());
            } else {
                showSeat.setPrice((double) showSeatRequest.getPriceOfPremiumSeat());
            }

            showSeat.setShow(show);
            showSeat.setIsFoodIncluded(Boolean.FALSE);
            showSeat.setIsAvailable(Boolean.TRUE);

            showSeatList.add(showSeat);
        }

        showRepository.save(show);
        return "Show seats have been associated successfully";
    }

    public Show getShowById(Integer showId) {
        return showRepository.findByShowIdAndIsDeletedFalse(showId)
                .orElseThrow(ShowDoesNotExist::new);
    }

    public List<Show> getAllShows() {
        return showRepository.findAllByIsDeletedFalse();
    }

    public List<Show> getAllShowsOfMovie(Integer movieId) {
        if (movieRepository.findByIdAndIsDeletedFalse(movieId).isEmpty()) {
            throw new MovieDoesNotExist();
        }
        return showRepository.getAllShowsOfMovie(movieId);
    }

    public String updateShow(Integer showId, ShowRequest showRequest) {
        Show show = showRepository.findByShowIdAndIsDeletedFalse(showId)
                .orElseThrow(ShowDoesNotExist::new);

        if (showRequest.getShowStartTime() != null) {
            show.setShowTime(showRequest.getShowStartTime());
        }
        if (showRequest.getShowDate() != null) {
            show.setShowDate(showRequest.getShowDate());
        }
        if (showRequest.getMovieId() != null) {
            Movie movie = movieRepository.findByIdAndIsDeletedFalse(showRequest.getMovieId())
                    .orElseThrow(MovieDoesNotExist::new);
            show.setMovie(movie);
        }
        if (showRequest.getTheaterId() != null) {
            Theater theater = theaterRepository.findById(showRequest.getTheaterId())
                    .orElseThrow(TheaterDoesNotExist::new);
            show.setTheater(theater);
        }

        showRepository.save(show);
        return "Show updated successfully";
    }

    public String deleteShow(Integer showId) {
        Show show = showRepository.findByShowIdAndIsDeletedFalse(showId)
                .orElseThrow(ShowDoesNotExist::new);
        
        show.setIsDeleted(true);
        showRepository.save(show);
        return "Show deleted successfully";
    }

    public String updateShowSeats(Integer showId, List<ShowSeat> updatedSeats) {
        Optional<Show> showOpt = showRepository.findById(showId);
        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExist();
        }

        Show show = showOpt.get();
        List<ShowSeat> currentSeats = show.getShowSeatList();

        for (ShowSeat updatedSeat : updatedSeats) {
            for (ShowSeat currentSeat : currentSeats) {
                if (currentSeat.getTheaterSeat() != null && updatedSeat.getTheaterSeat() != null &&
                    currentSeat.getTheaterSeat().getSeatNo().equals(updatedSeat.getTheaterSeat().getSeatNo())) {
                    currentSeat.setPrice(updatedSeat.getPrice());
                    // Important: If types changed, we might need to update the nested object too
                    if (currentSeat.getTheaterSeat() != null && updatedSeat.getTheaterSeat() != null) {
                        currentSeat.getTheaterSeat().setSeatType(updatedSeat.getTheaterSeat().getSeatType());
                    }
                    break;
                }
            }
        }

        showRepository.save(show);
        return "Show seats updated successfully";
    }
}