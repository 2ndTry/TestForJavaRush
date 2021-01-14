package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService{

    private ShipRepository repository;

    public ShipServiceImpl() {
    }

    @Autowired
    public ShipServiceImpl(ShipRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Ship> getAllShips(String name,
                               String planet,
                               ShipType shipType,
                               Long after,
                               Long before,
                               Boolean isUsed,
                               Double minSpeed,
                               Double maxSpeed,
                               Integer minCrewSize,
                               Integer maxCrewSize,
                               Double minRating,
                               Double maxRating
    )
    {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        List<Ship> ships = new ArrayList<>();
        repository.findAll().forEach(ship -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && !ship.getShipType().equals(shipType)) return;
            if (afterDate != null && !ship.getProdDate().after(afterDate)) return;
            if (beforeDate != null && !ship.getProdDate().before(beforeDate)) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;
            ships.add(ship);
        });
        return ships;
    }

    @Override
    public Ship createShip(Ship ship) {
        shipRating(ship);
        return repository.save(ship);
    }

    @Override
    public Ship updateShip(Long id, Ship newShip) {
        Ship oldShip = repository.findById(id).orElse(null);

        if (oldShip != null) {
            if (newShip.getName() != null) oldShip.setName(newShip.getName());
            if (newShip.getPlanet() != null) oldShip.setPlanet(newShip.getPlanet());
            if (newShip.getShipType() != null) oldShip.setShipType(newShip.getShipType());
            if (newShip.getProdDate() != null) oldShip.setProdDate(newShip.getProdDate());
            if (newShip.getUsed() != null) oldShip.setUsed(newShip.getUsed());
            if (newShip.getSpeed() != null) oldShip.setSpeed(newShip.getSpeed());
            if (newShip.getCrewSize() != null) oldShip.setCrewSize(newShip.getCrewSize());

            shipRating(oldShip);
            repository.save(oldShip);
        }
        return oldShip;
    }

    @Override
    public void deleteShip(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Ship getShipById(Long id) {
        return repository.findById(id).orElse(null);
    }



    @Override
    public List<Ship> sortShip(List<Ship> shipList, ShipOrder order) {
        if (order != null){
            shipList.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    default: return 0;
                }
            } );
        }
        return shipList;
    }

    @Override
    public List<Ship> getShipFromPage(List<Ship> shipList, Integer pageNumber, Integer pageSize) {
        final int page = pageNumber == null ? 0 : pageNumber;
        final int size = pageSize == null ? 3 : pageSize;
        int from = page * size;
        int to = (page + 1) * size;
        if (to > shipList.size()) {
            to = shipList.size();
        }
        return shipList.subList(from, to);
    }

    private void shipRating(Ship ship) {
        double k = ship.getUsed() ? 0.5 : 1.0;
        int shipProdYear = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).getYear();
        double rating = BigDecimal
                .valueOf((ship.getSpeed() * k * 80) / (3019 - shipProdYear + 1))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        ship.setRating(rating);
    }
}
