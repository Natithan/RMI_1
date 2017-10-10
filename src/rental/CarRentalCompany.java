package rental;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarRentalCompany implements ICarRentalCompany{

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	
	private List<String> regions;
	private String name;
	private List<Car> cars;
	private Map<String,CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
		this.cars = cars;
		setRegions(regions);
		for(Car car:cars)
			carTypes.put(car.getType().getName(), car.getType());
		logger.log(Level.INFO, this.toString());
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#getName()
	 */

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

    /***********
     * Regions *
     **********/
    private void setRegions(List<String> regions) {
        this.regions = regions;
    }
    
    /* (non-Javadoc)
	 * @see rental.ICarRentalCompany#getRegions()
	 */
    public List<String> getRegions() {
        return this.regions;
    }
    
    /* (non-Javadoc)
	 * @see rental.ICarRentalCompany#hasRegion(java.lang.String)
	 */
    public boolean hasRegion(String region) {
        return this.regions.contains(region);
    }
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#getAllCarTypes()
	 */

	public Collection<CarType> getAllCarTypes() {
		return carTypes.values();
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#getCarType(java.lang.String)
	 */
	public CarType getCarType(String carTypeName) {
		if(carTypes.containsKey(carTypeName))
			return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	// mark
	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#isAvailable(java.lang.String, java.util.Date, java.util.Date)
	 */
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		if(carTypes.containsKey(carTypeName)) {
			return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
		} else {
			throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
		}
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#getAvailableCarTypes(java.util.Date, java.util.Date)
	 */
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}
		return availableCarTypes;
	}
	
	/*********
	 * CARS *
	 *********/
	
	private Car getCar(int uid) {
		for (Car car : cars) {
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (Car car : cars) {
			if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
				availableCars.add(car);
			}
		}
		return availableCars;
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#createQuote(rental.ReservationConstraints, java.lang.String)
	 */

	public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
				
		if(!regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");

		CarType type = getCarType(constraints.getCarType());
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#confirmQuote(rental.Quote)
	 */
	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
	                + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#cancelReservation(rental.Reservation)
	 */
	public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}
	
	public List<Reservation> getReservationsByRenter(String clientName)
			throws RemoteException {
		
		List<Reservation> reservationsByClient = new ArrayList<Reservation>(); 
		
		for (Car car: this.cars){
			for (Reservation reservation : car.getReservations()){
				if (reservation.getCarRenter().equals(clientName)){
					reservationsByClient.add(reservation);
				}
			}
		}
		return reservationsByClient;
	}
	
	public int getNumberOfReservationsForCarType(String carType) {
		int numberOfReservations = 0;
		for (Car car: this.cars){
//		System.out.println("Reservation size: " + car.getReservations().size());
//		System.out.prntln("carTypeEquals: " + car.getType().equals(carType));
//		System.out.println("Car.GetType: " + car.getType().getName() + " carType: " + carType);
//		System.out.println("Nb of reservations: " + numberOfReservations);
			if (car.getType().getName().equals(carType)){
				numberOfReservations+=car.getReservations().size();
			}
		}
		return numberOfReservations;
	}
	
	/* (non-Javadoc)
	 * @see rental.ICarRentalCompany#toString()
	 */
	@Override
	public String toString() {
		return String.format("<%s> CRC is active in regions %s and serving with %d car types", name, listToString(regions), carTypes.size());
	}
	
	private static String listToString(List<? extends Object> input) {
		StringBuilder out = new StringBuilder();
		for (int i=0; i < input.size(); i++) {
			if (i == input.size()-1) {
				out.append(input.get(i).toString());
			} else {
				out.append(input.get(i).toString()+", ");
			}
		}
		return out.toString();
	}


	
}