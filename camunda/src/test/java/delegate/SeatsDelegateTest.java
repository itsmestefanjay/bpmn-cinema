package delegate;

import de.novatec.bpm.delegate.SeatsDelegate;
import de.novatec.bpm.model.Reservation;
import de.novatec.bpm.service.SeatService;
import de.novatec.bpm.variable.ProcessVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeatsDelegateTest {

    @Mock
    private SeatService seatService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private SeatsDelegate delegate;

    @Test
    void test_releaseSeats_original_seats_are_released_when_set() {
        // given
        Reservation reservation = createReservation("A1", "A2");
        given(execution.getVariable(ProcessVariables.SEATS_AVAILABLE.getName())).willReturn(true);
        given(execution.getVariable(ProcessVariables.RESERVATION.getName())).willReturn(reservation);

        // when
        delegate.releaseSeats(execution);

        // then
        verify(seatService, times(1)).releaseSeats(reservation.getSeats());
    }

    @Test
    void test_releaseSeats_alternative_seats_are_released_when_original_not_available() {
        // given
        given(execution.getVariable(ProcessVariables.SEATS_AVAILABLE.getName())).willReturn(false);
        List<String> altSeats = Arrays.asList("C1", "C2");
        given(execution.getVariable(ProcessVariables.ALT_SEATS.getName())).willReturn(altSeats);

        // when
        delegate.releaseSeats(execution);

        // verify
        verify(seatService, times(1)).releaseSeats(altSeats);

    }

    @Test
    void test_setAltSeats_alternative_seats_are_fetched_and_set() {
        // given
        List<String> altSeats = Arrays.asList("C4", "C5");
        Reservation reservation = createReservation("D3", "D4");
        given(execution.getVariable(ProcessVariables.RESERVATION.getName())).willReturn(reservation);
        given(seatService.getAlternativeSeats(anyList())).willReturn(altSeats);

        // when
        delegate.setAltSeats(execution);

        // then
        verify(seatService, times(1)).getAlternativeSeats(reservation.getSeats());
        verify(execution, times(1)).setVariable(ProcessVariables.ALT_SEATS.getName(), altSeats);
    }

    @Test
    void test_reserveSeats_original_seats_are_reserved_when_available_and_price_is_calculated() {
        // given
        given(execution.getVariable(ProcessVariables.SEATS_AVAILABLE.getName())).willReturn(true);
        List<String> seats = Arrays.asList("D3", "D4");
        Reservation reservation = createReservation(seats);
        given(execution.getVariable(ProcessVariables.RESERVATION.getName())).willReturn(reservation);
        given(seatService.getTicketPrice(anyList())).willReturn(100);

        // when
        delegate.reserveSeats(execution);

        // then
        verify(execution, times(1)).getVariable(ProcessVariables.SEATS_AVAILABLE.getName());
        verify(execution, times(1)).getVariable(ProcessVariables.RESERVATION.getName());
        verify(seatService, times(1)).getTicketPrice(reservation.getSeats());
        assertThat(reservation.getPrice()).isEqualTo(100);
        assertThat(reservation.getSeats()).isEqualTo(seats);
        verify(seatService, times(1)).reserveSeats(reservation.getSeats());
        verify(execution, times(1)).setVariable(ProcessVariables.RESERVATION.getName(), reservation);
        verify(execution, times(0)).getVariable(ProcessVariables.ALT_SEATS.getName());
    }

    @Test
    void test_reserveSeats_alternative_seats_are_reserved_and_price_calculated() {
        // given
        given(execution.getVariable(ProcessVariables.SEATS_AVAILABLE.getName())).willReturn(false);
        List<String> altSeats = Arrays.asList("F3", "F4");
        given(execution.getVariable(ProcessVariables.ALT_SEATS.getName())).willReturn(altSeats);
        Reservation reservation = createReservation();
        given(execution.getVariable(ProcessVariables.RESERVATION.getName())).willReturn(reservation);
        given(seatService.getTicketPrice(anyList())).willReturn(100);

        // when
        delegate.reserveSeats(execution);

        // then
        verify(execution, times(1)).getVariable(ProcessVariables.SEATS_AVAILABLE.getName());
        verify(execution, times(1)).getVariable(ProcessVariables.RESERVATION.getName());
        verify(seatService, times(1)).getTicketPrice(reservation.getSeats());
        assertThat(reservation.getPrice()).isEqualTo(100);
        assertThat(reservation.getSeats()).isEqualTo(altSeats);
        verify(seatService, times(1)).reserveSeats(reservation.getSeats());
        verify(execution, times(1)).setVariable(ProcessVariables.RESERVATION.getName(), reservation);
        verify(execution, times(1)).getVariable(ProcessVariables.ALT_SEATS.getName());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void test_checkSeatAvailabilty_seat_availability_is_set(boolean availability) {
        // given
        List<String> seats = Arrays.asList("D3", "D4");
        Reservation reservation = createReservation(seats);
        given(execution.getVariable(ProcessVariables.RESERVATION.getName())).willReturn(reservation);
        given(seatService.seatsAvailable(seats)).willReturn(availability);

        // when
        delegate.checkSeatAvailabilty(execution);

        // then
        verify(seatService, times(1)).seatsAvailable(seats);
        verify(execution, times(1)).setVariable(ProcessVariables.SEATS_AVAILABLE.getName(), availability);
    }

    @Test
    void offerAltSeats() {
        // given
        given(execution.getVariable(ProcessVariables.ALT_SEATS.getName())).willReturn(Arrays.asList("A1", "A2"));

        // when
        delegate.offerAltSeats(execution);

        // then
        verify(execution, times(1)).getVariable(ProcessVariables.ALT_SEATS.getName());
        verify(execution, times(1)).getBusinessKey();
        Mockito.verifyNoMoreInteractions(execution);
    }

    private Reservation createReservation(String... seat) {
        Reservation reservation = new Reservation();
        List<String> seats = Arrays.asList(seat);
        reservation.setSeats(seats);
        return reservation;
    }

    private Reservation createReservation(List<String> seats) {
        Reservation reservation = new Reservation();
        reservation.setSeats(seats);
        return reservation;
    }
}