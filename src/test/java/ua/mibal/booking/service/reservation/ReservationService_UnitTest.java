/*
 * Copyright (c) 2024. Mykhailo Balakhon mailto:9mohapx9@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.mibal.booking.service.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import ua.mibal.booking.model.dto.response.ReservationDto;
import ua.mibal.booking.model.entity.Reservation;
import ua.mibal.booking.model.entity.User;
import ua.mibal.booking.model.exception.UserHasNoAccessToReservationException;
import ua.mibal.booking.model.mapper.ReservationMapper;
import ua.mibal.booking.model.request.ReservationRequest;
import ua.mibal.booking.repository.ReservationRepository;
import ua.mibal.booking.service.UserService;
import ua.mibal.booking.service.reservation.component.ReservationBuilder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Pageable.unpaged;
import static ua.mibal.booking.model.entity.embeddable.Role.ROLE_MANAGER;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">9mohapx9@gmail.com</a>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationService_UnitTest {

    private ReservationService service;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private UserService userService;
    @Mock
    private ReservationBuilder reservationBuilder;

    @Mock
    private Reservation reservation;
    @Mock
    private ReservationDto reservationDto;
    @Mock
    private User user;
    @Mock
    private User anotherUser;
    @Mock
    private ReservationRequest request;

    @BeforeEach
    void setup() {
        service = new ReservationService(reservationRepository, reservationMapper, userService, reservationBuilder);
    }

    @Test
    void getAllByUser() {
        String email = "userEmail123";

        when(reservationRepository.findAllByUserEmail(email, unpaged()))
                .thenReturn(new PageImpl<>(List.of(reservation, reservation, reservation)));
        when(reservationMapper.toDto(reservation))
                .thenReturn(reservationDto);

        var actual = service.getAllByUser(email, unpaged());

        assertEquals(
                new PageImpl<>(List.of(reservationDto, reservationDto, reservationDto)),
                actual
        );
    }

    @Test
    void getAll() {
        when(reservationRepository.findAll(unpaged()))
                .thenReturn(new PageImpl<>(List.of(reservation, reservation, reservation)));
        when(reservationMapper.toDto(reservation))
                .thenReturn(reservationDto);

        var actual = service.getAll(unpaged());

        assertEquals(
                new PageImpl<>(List.of(reservationDto, reservationDto, reservationDto)),
                actual
        );
    }

    @Test
    void rejectReservation_by_MANAGER() {
        Long id = 1L;
        String email = "userEmail123";
        String reason = "reason";


        when(reservationRepository.findByIdFetchRejections(id))
                .thenReturn(Optional.of(reservation));
        when(userService.getOne(email))
                .thenReturn(user);
        when(user.is(ROLE_MANAGER))
                .thenReturn(true);

        service.rejectReservation(id, email, reason);

        verify(reservation, times(1))
                .reject(user, reason);
    }

    @Test
    void rejectReservation_by_USER() {
        Long id = 1L;
        String email = "userEmail123";
        String reason = "reason";


        when(reservationRepository.findByIdFetchRejections(id))
                .thenReturn(Optional.of(reservation));
        when(userService.getOne(email))
                .thenReturn(user);
        when(user.is(ROLE_MANAGER))
                .thenReturn(false);
        when(reservation.getUser())
                .thenReturn(user);

        service.rejectReservation(id, email, reason);

        verify(reservation, times(1))
                .reject(user, reason);
    }

    @Test
    void rejectReservation_should_throw_UserHasNoAccessToReservationException() {
        Long id = 1L;
        String email = "userEmail123";
        String reason = "reason";

        when(reservationRepository.findByIdFetchRejections(id))
                .thenReturn(Optional.of(reservation));
        when(userService.getOne(email))
                .thenReturn(user);
        when(user.is(ROLE_MANAGER))
                .thenReturn(false);
        when(reservation.getUser())
                .thenReturn(anotherUser);

        assertThrows(UserHasNoAccessToReservationException.class,
                () -> service.rejectReservation(id, email, reason));
    }

    @Test
    void reserve() {
        when(reservationBuilder.buildBy(request))
                .thenReturn(reservation);

        service.reserve(request);

        verify(reservationRepository, times(1))
                .save(reservation);
    }
}
