package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.TheaterConvertor;
import com.backend.movie_ticket_booking_system.entities.Theater;
import com.backend.movie_ticket_booking_system.entities.TheaterSeat;
import com.backend.movie_ticket_booking_system.enums.SeatType;
import com.backend.movie_ticket_booking_system.exceptions.TheaterDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TheaterIsExist;
import com.backend.movie_ticket_booking_system.repositories.TheaterRepository;
import com.backend.movie_ticket_booking_system.request.TheaterRequest;
import com.backend.movie_ticket_booking_system.request.TheaterSeatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public String addTheater(TheaterRequest request) {
        Theater existingTheater = theaterRepository.findByAddress(request.getAddress());
        if (existingTheater != null) {
            if (Boolean.TRUE.equals(existingTheater.getIsActive())) {
                 throw new TheaterIsExist();
            }
            existingTheater.setIsActive(true);
            existingTheater.setName(request.getName());
            existingTheater.setCity(request.getCity());
            existingTheater.setState(request.getState());
            existingTheater.setCountry(request.getCountry());
            theaterRepository.save(existingTheater);
            return "Theater reactivated successfully";
        }

        Theater theater = TheaterConvertor.theaterDtoToTheater(request);
        theaterRepository.save(theater);

        return "Theater saved successfully";
    }

    @SuppressWarnings("unused")
    public String addTheaterSeats(TheaterSeatRequest request) {
        Theater theater = findTheaterByAddressOrThrow(request.getAddress());

        if (!theater.getTheaterSeatList().isEmpty()) {
             return "Theater previously configured; retaining existing seats";
        }

        int seatsPerRow = request.getNoOfSeatInRow();
        int classicSeats = request.getNoOfClassicSeat();
        int premiumSeats = request.getNoOfPremiumSeat();

        generateAndAddSeats(theater, classicSeats, premiumSeats, seatsPerRow);

        theaterRepository.save(theater);

        return "Theater seats added successfully";
    }

    private void generateAndAddSeats(Theater theater, int classicCount, int premiumCount, int seatsPerRow) {
        List<TheaterSeat> seatList = theater.getTheaterSeatList();

        int row = 1;
        int seatInRow = 0;
        char column = 'A';

        // Classic seats first
        seatList.addAll(generateSeats(theater, classicCount, seatsPerRow, row, seatInRow, column, SeatType.STANDARD));

        // Then premium seats (continuing from where classic left off)
        int lastRow = (classicCount + seatsPerRow - 1) / seatsPerRow;
        int seatsInLastRow = classicCount % seatsPerRow;
        if (seatsInLastRow == 0) seatsInLastRow = seatsPerRow;

        row = lastRow;
        seatInRow = seatsInLastRow;
        column = (char) ('A' + seatsInLastRow);

        if (seatInRow == seatsPerRow) {
            row++;
            seatInRow = 0;
            column = 'A';
        }

        seatList.addAll(generateSeats(theater, premiumCount, seatsPerRow, row, seatInRow, column, SeatType.PREMIUM));
    }

    private List<TheaterSeat> generateSeats(Theater theater, int count, int seatsPerRow, int startRow, int startSeatInRow, char startColumn, SeatType type) {
        List<TheaterSeat> seats = new java.util.ArrayList<>();

        int row = startRow;
        int seatInRow = startSeatInRow;
        char column = startColumn;

        for (int i = 0; i < count; i++) {
            String seatNo = row + String.valueOf(column);

            TheaterSeat seat = new TheaterSeat();
            seat.setSeatNo(seatNo);
            seat.setSeatType(type);
            seat.setTheater(theater);

            seats.add(seat);

            // Move to next seat
            column++;
            seatInRow++;

            if (seatInRow == seatsPerRow) {
                seatInRow = 0;
                row++;
                column = 'A';
            }
        }

        return seats;
    }

    public Theater getTheaterById(Integer id) {
        return theaterRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(TheaterDoesNotExist::new);
    }

    public Theater getTheaterByAddress(String address) {
        Theater theater = theaterRepository.findByAddressAndIsActiveTrue(address);
        if (theater == null) {
            throw new TheaterDoesNotExist();
        }
        return theater;
    }

    private Theater findTheaterByAddressOrThrow(String address) {
        return Optional.ofNullable(theaterRepository.findByAddressAndIsActiveTrue(address))
                .orElseThrow(TheaterDoesNotExist::new);
    }

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAllByIsActiveTrue();
    }

    public String updateTheater(Integer id, TheaterRequest request) {
        Theater theater = getTheaterById(id); // reuses validation

        if (request.getName() != null) {
            theater.setName(request.getName());
        }

        if (request.getAddress() != null && !request.getAddress().equals(theater.getAddress())) {
            if (theaterRepository.findByAddress(request.getAddress()) != null) {
                throw new TheaterIsExist();
            }
            theater.setAddress(request.getAddress());
        }

        theaterRepository.save(theater);
        return "Theater updated successfully";
    }

    public String deleteTheater(Integer id) {
        Theater theater = getTheaterById(id);
        
        theater.setIsActive(false);
        theaterRepository.save(theater);
        return "Theater deleted successfully";
    }
    @SuppressWarnings("unused")
    public void updateTheaterSeats(Integer theaterId, List<TheaterSeat> updatedSeats) {
        Optional<Theater> theaterOpt = theaterRepository.findById(theaterId);
        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExist();
        }

        Theater theater = theaterOpt.get();
        List<TheaterSeat> currentSeats = theater.getTheaterSeatList();

        for (TheaterSeat updatedSeat : updatedSeats) {
            for (TheaterSeat currentSeat : currentSeats) {
                if (currentSeat.getSeatNo().equals(updatedSeat.getSeatNo())) {
                    currentSeat.setSeatType(updatedSeat.getSeatType());
                    break;
                }
            }
        }

        theaterRepository.save(theater);
    }
}
