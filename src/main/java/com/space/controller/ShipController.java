package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

    private ShipService service;

    public ShipController() {
    }

    @Autowired
    public ShipController(ShipService service) {
        this.service = service;
    }

    @RequestMapping(value = "/ships", method = RequestMethod.GET)
    public List<Ship> getAllShip (@RequestParam (value = "name", required = false) String name,
                                  @RequestParam (value = "planet", required = false) String planet,
                                  @RequestParam (value = "shipType", required = false) ShipType shipType,
                                  @RequestParam (value = "after", required = false) Long after,
                                  @RequestParam (value = "before", required = false) Long before,
                                  @RequestParam (value = "isUsed", required = false) Boolean isUsed,
                                  @RequestParam (value = "minSpeed", required = false) Double minSpeed,
                                  @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
                                  @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
                                  @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
                                  @RequestParam (value = "minRating", required = false) Double minRating,
                                  @RequestParam (value = "maxRating", required = false) Double maxRating,
                                  @RequestParam (value = "order", required = false) ShipOrder order,
                                  @RequestParam (value = "pageNumber", required = false) Integer pageNumber,
                                  @RequestParam (value = "pageSize", required = false) Integer pageSize
    ){
        final List<Ship> ships = new ArrayList<>(service.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating));
        final List<Ship> sortedShips = service.sortShip(ships, order);
        return service.getShipFromPage(sortedShips, pageNumber, pageSize);
    }

    @RequestMapping(value = "/ships", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Ship> createShip (@RequestBody Ship ship) {
        if (ship != null
                && ship.getName() != null && isStringValid(ship.getName())
                && ship.getPlanet() != null && isStringValid(ship.getPlanet())
                && ship.getShipType() != null
                && ship.getProdDate() != null && isDateValid(ship.getProdDate())
                && ship.getSpeed() != null && isSpeedValid(ship.getSpeed())
                && ship.getCrewSize() != null && isCrewSizeValid(ship.getCrewSize())) {

            if (ship.getUsed() == null) ship.setUsed(false);
            return new ResponseEntity<>(service.createShip(ship), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long id, @RequestBody Ship ship) {
        if (id == null || id <= 0 || ship == null
                || ship.getName() != null && !isStringValid(ship.getName())
                || ship.getPlanet() != null && !isStringValid(ship.getPlanet())
                || ship.getProdDate() != null && !isDateValid(ship.getProdDate())
                || ship.getSpeed() != null && !isSpeedValid(ship.getSpeed())
                || ship.getCrewSize() != null && !isCrewSizeValid(ship.getCrewSize())) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Ship updateShip = service.updateShip(id, ship);

        return updateShip == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(updateShip, HttpStatus.OK);
    }

    @DeleteMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> deleteShip (@PathVariable("id") Long id) {
        if(id == 0 || id < 0) {
            return new ResponseEntity<Ship>(HttpStatus.BAD_REQUEST);
        }
        if(service.getShipById(id) != null) {
            service.deleteShip(id);
            return new ResponseEntity<Ship>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> getShipById (@PathVariable("id") Long id) {
        Ship ship = service.getShipById(id);
        if(id == 0 || id < 0) {
            return new ResponseEntity<Ship>(HttpStatus.BAD_REQUEST);
        }
        if(service.getShipById(id) != null) {
            return new ResponseEntity<Ship>(ship, HttpStatus.OK);
        } else {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/ships/count", method = RequestMethod.GET)
    public Integer getShipsCount (@RequestParam (value = "name", required = false) String name,
                                  @RequestParam (value = "planet", required = false) String planet,
                                  @RequestParam (value = "shipType", required = false) ShipType shipType,
                                  @RequestParam (value = "after", required = false) Long after,
                                  @RequestParam (value = "before", required = false) Long before,
                                  @RequestParam (value = "isUsed", required = false) Boolean isUsed,
                                  @RequestParam (value = "minSpeed", required = false) Double minSpeed,
                                  @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
                                  @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
                                  @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
                                  @RequestParam (value = "minRating", required = false) Double minRating,
                                  @RequestParam (value = "maxRating", required = false) Double maxRating
    ){
        final List<Ship> ships = new ArrayList<>(service.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating));
        return ships.size();
    }

    private boolean isStringValid(String param) {
        return !param.isEmpty() && param.length() <= 50;
    }

    private boolean isSpeedValid(Double speed) {
        double result = new BigDecimal(speed).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return result >= 0.01 && result <= 0.99;

    }

    private boolean isCrewSizeValid(Integer size) {
        return size > 0 && size < 10_000;
    }

    private boolean isDateValid(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2800);
        Date from = calendar1.getTime();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 3019);
        Date to = calendar2.getTime();

        return date.getTime() > 0 && date.after(from) && date.before(to);
    }
}
