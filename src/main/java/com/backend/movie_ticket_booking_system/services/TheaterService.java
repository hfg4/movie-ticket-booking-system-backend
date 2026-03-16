package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.TheaterConvertor;
import com.backend.movie_ticket_booking_system.entities.Theater;
import com.backend.movie_ticket_booking_system.entities.TheaterSeat;
import com.backend.movie_ticket_booking_system.enums.SeatType;
import com.backend.movie_ticket_booking_system.exceptions.TheaterDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TheaterIsExist;
import com.backend.movie_ticket_booking_system.exceptions.TheaterIsNotExist;
import com.backend.movie_ticket_booking_system.repositories.TheaterRepository;
import com.backend.movie_ticket_booking_system.request.TheaterRequest;
import com.backend.movie_ticket_booking_system.request.TheaterSeatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TheaterService {

    @Autowired
    private TheaterRepository theaterRepository;
@SuppressWarnings("unused")
    public String addTheater(TheaterRequest theaterRequest) throws TheaterIsExist {
        if (theaterRepository.findByAddress(theaterRequest.getAddress()) != null) {
            throw new TheaterIsExist();
        }

        Theater theater = TheaterConvertor.theaterDtoToTheater(theaterRequest);

        theaterRepository.save(theater);
        return "Theater has been saved Successfully";
    }
    @SuppressWarnings("unused")
    public String addTheaterSeat(TheaterSeatRequest entryDto) throws TheaterIsNotExist {
        if (theaterRepository.findByAddress(entryDto.getAddress()) == null) {
            throw new TheaterIsNotExist();
        }

        Integer noOfSeatsInRow = entryDto.getNoOfSeatInRow();
        Integer noOfPremiumSeats = entryDto.getNoOfPremiumSeat();
        Integer noOfClassicSeat = entryDto.getNoOfClassicSeat();
        String address = entryDto.getAddress();

        Theater theater = theaterRepository.findByAddress(address);

        List<TheaterSeat> seatList = theater.getTheaterSeatList();

        int counter = 1;
        int fill = 0;
        char ch = 'A';

        for (int i = 1; i <= noOfClassicSeat; i++) {
            String seatNo = Integer.toString(counter) + ch;

            ch++;
            fill++;
            if (fill == noOfSeatsInRow) {
                fill = 0;
                counter++;
                ch = 'A';
            }

            TheaterSeat theaterSeat = new TheaterSeat();
            theaterSeat.setSeatNo(seatNo);
            theaterSeat.setSeatType(SeatType.STANDARD);
            theaterSeat.setTheater(theater);
            seatList.add(theaterSeat);
        }

        for (int i = 1; i <= noOfPremiumSeats; i++) {
            String seatNo = Integer.toString(counter) + ch;

            ch++;
            fill++;
            if (fill == noOfSeatsInRow) {
                fill = 0;
                counter++;
                ch = 'A';
            }

            TheaterSeat theaterSeat = new TheaterSeat();
            theaterSeat.setSeatNo(seatNo);
            theaterSeat.setSeatType(SeatType.PREMIUM);
            theaterSeat.setTheater(theater);
            seatList.add(theaterSeat);
        }

        theaterRepository.save(theater);

        return "Theater Seats have been added successfully";
    }

    public Theater getTheaterById(Integer theaterId) {
        Optional<Theater> theaterOpt = theaterRepository.findById(theaterId);

        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExist();
        }

        return theaterOpt.get();
    }

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    public Theater getTheaterByAddress(String address) {
        Theater theater = theaterRepository.findByAddress(address);

        if (theater == null) {
            throw new TheaterDoesNotExist();
        }

        return theater;
    }

    public String updateTheater(Integer theaterId, TheaterRequest theaterRequest) {
        Optional<Theater> theaterOpt = theaterRepository.findById(theaterId);

        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExist();
        }

        Theater theater = theaterOpt.get();

        if (theaterRequest.getName() != null) {
            theater.setName(theaterRequest.getName());
        }
        if (theaterRequest.getAddress() != null) {
            Theater existingTheater = theaterRepository.findByAddress(theaterRequest.getAddress());
            if (existingTheater != null && !existingTheater.getId().equals(theaterId)) {
                throw new TheaterIsExist();
            }
            theater.setAddress(theaterRequest.getAddress());
        }

        theaterRepository.save(theater);
        return "Theater updated successfully";
    }

    public String deleteTheater(Integer theaterId) {
        Optional<Theater> theaterOpt = theaterRepository.findById(theaterId);

        if (theaterOpt.isEmpty()) {
            throw new TheaterDoesNotExist();
        }

        theaterRepository.deleteById(theaterId);
        return "Theater deleted successfully";
    }
}
