package com.accenture.officehub.officehub_api.repository.inmemory;

import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public List<Reservation> findAll() {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getId))
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream().filter(r -> r.getId().equals(id)).findFirst().map(this::copy);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation mutable = copy(reservation);
        if (mutable.getId() == null) {
            mutable.setId(sequence.getAndIncrement());
        }
        reservations.removeIf(r -> r.getId().equals(mutable.getId()));
        reservations.add(mutable);
        return copy(mutable);
    }

    @Override
    public void saveAll(List<Reservation> newReservations) {
        reservations.clear();
        long maxId = 0L;
        for (Reservation reservation : newReservations) {
            Reservation copy = copy(reservation);
            reservations.add(copy);
            if (copy.getId() != null && copy.getId() > maxId) {
                maxId = copy.getId();
            }
        }
        sequence.set(maxId + 1);
    }

    private Reservation copy(Reservation reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getRoomId(),
                reservation.getRoom(),
                reservation.getUser(),
                reservation.getDate(),
                reservation.getStart(),
                reservation.getEnd(),
                reservation.getStatus()
        );
    }
}
