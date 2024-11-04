package com.reserveit.service.impl;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.repository.CompanyRepository;
import com.reserveit.repository.ReservationRepository;
import com.reserveit.service.ReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, CompanyRepository companyRepository) {
        this.reservationRepository = reservationRepository;
        this.companyRepository = companyRepository;
    }

    public ReservationDto createReservation(ReservationDto reservationDto) {
        // Basic validation
        if (reservationDto.getCustomerName() == null || reservationDto.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (reservationDto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException("Number of people must be greater than 0");
        }

        // Find the company (restaurant) by id
        Optional<Company> companyOpt = companyRepository.findById(reservationDto.getCompanyId());
        if (companyOpt.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found with id: " + reservationDto.getCompanyId());
        }

        Company company = companyOpt.get();

        Reservation reservation = convertToEntity(reservationDto);
        reservation.setCompany(company);  // Set the company (restaurant)
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Optional<Reservation> existingReservation = reservationRepository.findById(id);

        if (existingReservation.isPresent()) {
            Reservation updatedReservation = convertToEntity(reservationDto);
            updatedReservation.setId(id); // Use Long for the reservation id
            reservationRepository.save(updatedReservation);
            return convertToDto(updatedReservation);
        }

        throw new IllegalArgumentException("Reservation not found with id: " + id);
    }

    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setCustomerName(reservation.getCustomerName());
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());

        // Set both companyId and companyName in the DTO
        if (reservation.getCompany() != null) {
            dto.setCompanyId(reservation.getCompany().getId());
            dto.setCompanyName(reservation.getCompany().getName());  // Set companyName
        } else {
            dto.setCompanyName("Unknown Company");  // Default value if no company is set
        }

        return dto;
    }

    private Reservation convertToEntity(ReservationDto dto) {
        Reservation reservation = new Reservation();
        reservation.setCustomerName(dto.getCustomerName());
        reservation.setReservationDate(LocalDate.parse(dto.getReservationDate()));
        reservation.setNumberOfPeople(dto.getNumberOfPeople());


        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + dto.getCompanyId()));
        reservation.setCompany(company);

        return reservation;
    }
}
