package com.driver;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class RepositoryClass {

    HashMap<String, Airport> airportHashMap = new HashMap<>();
    HashMap<Integer, Flight> flightHashMap = new HashMap<>();
    HashMap<City, List<Integer>> fromCityHashMap  = new HashMap<>();
    HashMap<City, List<Integer>> toCityHashMap = new HashMap<>();
    HashMap<Integer, Integer> noOfPassengerInFlight  = new HashMap<>();
    HashMap<Integer, Integer> passengerFlightPair  = new HashMap<>();
    HashMap<Integer, Integer> passengerBookingCountPair = new HashMap<>();
    HashMap<Integer, Passenger> passengerHashMap = new HashMap<>();

    public void addAirport(Airport airport){

        airportHashMap.put(airport.getAirportName(),airport);
    }

    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName

        int maxTerminal = 0;
        String maxAirportName = "";
        for(String airportName : airportHashMap.keySet()){
            int terminal = airportHashMap.get(airportName).getNoOfTerminals();
            if(terminal > maxTerminal){
                maxTerminal = terminal;
                maxAirportName = airportName;
            }else if (terminal == maxTerminal){
                if(airportName.compareTo(maxAirportName) < 0){
                    maxAirportName = airportName;
                }
            }
        }
        return maxAirportName;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.

        double shortestDistance = -1;
        for (Integer flightId : flightHashMap.keySet()){
            if(flightHashMap.get(flightId).getFromCity() == fromCity && flightHashMap.get(flightId).getToCity() == toCity){
                shortestDistance = Math.min(shortestDistance,flightHashMap.get(flightId).getDuration());
            }
        }
        return shortestDistance;
    }

    public int getNumberOfPeopleOn(Date date,String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        City city = airportHashMap.get(airportName).getCity();
        Integer totalNumberOfPeopleFromCity = getPassengersInFlight(fromCityHashMap.get(city));
        Integer totalNumberOfPeopleToCity = getPassengersInFlight(toCityHashMap.get(city));
        return totalNumberOfPeopleFromCity + totalNumberOfPeopleToCity;
    }

    public Integer getPassengersInFlight(List<Integer> listOfFlights){
        Integer totalNumberOfPeople = 0;

        for(Integer flightId : flightHashMap.keySet()){
            totalNumberOfPeople += noOfPassengerInFlight.get(flightId);
        }
        return totalNumberOfPeople;
    }

    public int calculateFlightFare(Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int price = 3000 + noOfPassengerInFlight.get(flightId) * 50;
        return price;

    }


    public String bookATicket(Integer flightId,Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        if (noOfPassengerInFlight.get(flightId) == flightHashMap.get(flightId).getMaxCapacity()){
            return  "FAILURE";
        }
        else if(passengerFlightPair.containsKey(passengerId)){
            return "FAILURE";
        }
        else{
            passengerFlightPair.put(passengerId,flightId);
            passengerBookingCountPair.put(passengerId,passengerBookingCountPair.getOrDefault(passengerId,0)+1);
            noOfPassengerInFlight.put(flightId,noOfPassengerInFlight.getOrDefault(flightId,0)+1);
            return "SUCCESS";
        }
    }

    public String cancelATicket(Integer flightId,Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        if(!flightHashMap.containsKey(flightId) || !passengerHashMap.containsKey(passengerId) || !passengerFlightPair.containsKey(passengerId)){
            return "FAILURE";
        }else{
            passengerFlightPair.remove(passengerId);
            passengerBookingCountPair.put(passengerId,passengerBookingCountPair.get(passengerId)-1);
            noOfPassengerInFlight.put(flightId,noOfPassengerInFlight.get(flightId)-1);
            return "SUCCESS";
        }
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        if(passengerBookingCountPair.containsKey(passengerId))return passengerBookingCountPair.get(passengerId);
        else return 0;
    }

    public void addFlight(Flight flight){

        //Return a "SUCCESS" message string after adding a flight.
        flightHashMap.put(flight.getFlightId(),flight);
        noOfPassengerInFlight.put(flight.getFlightId(),0);
        fromCityHashMap.put(flight.getFromCity(),new ArrayList<>());
        toCityHashMap.put(flight.getToCity(),new ArrayList<>());
    }

    public String getAirportNameFromFlightId(Integer flightId){

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        City fromCity = null;
        for(City city : fromCityHashMap.keySet()){
            List<Integer> listOfFlights = fromCityHashMap.get(city);
            if(listOfFlights.contains(flightId)){
                fromCity = city;
                break;
            }
        }
        for(String airportName : airportHashMap.keySet()){
            if(airportHashMap.get(airportName).getAirportName().equals(fromCity.toString()))return airportName;
        }
        return null;
    }

    public int calculateRevenueOfAFlight(Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight

        int passengerCount = noOfPassengerInFlight.get(flightId);
        int totalRevenue = 3000 * passengerCount + (passengerCount - 1)*(passengerCount)/2 * 50;
        return totalRevenue;
    }

    public void addPassenger(Passenger passenger){

        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.

        passengerHashMap.put(passenger.getPassengerId(),passenger);
        passengerBookingCountPair.put(passenger.getPassengerId(),0);
    }

}