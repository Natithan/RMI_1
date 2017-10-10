package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

import rental.CarType;
import rental.ICarRentalCompany;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;

public class Client extends AbstractTestBooking {

	/********
	 * MAIN *
	 ********/

	public static void main(String[] args) throws Exception {

		System.setSecurityManager(null);

		String carRentalCompanyName = "Hertz";

		// An example reservation scenario on car rental company 'Hertz' would
		// be...
		Client client = new Client("simpleTrips", carRentalCompanyName);
		client.run();

	}

	private ICarRentalCompany carRentalCompany;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Client(String scriptFile, String carRentalCompanyName) {
		super(scriptFile);
		// TODO Auto-generated method stub

		try {
			Registry registry = LocateRegistry.getRegistry();
			this.carRentalCompany = (ICarRentalCompany) registry
					.lookup(carRentalCompanyName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Check which car types are available in the given period and print this
	 * list of car types.
	 * 
	 * @param start
	 *            start time of the period
	 * @param end
	 *            end time of the period
	 * @throws Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected void checkForAvailableCarTypes(Date start, Date end) throws Exception 
	{
		Set<CarType> availableCarTypes = this.carRentalCompany.getAvailableCarTypes(start, end);
		
		for (CarType carType : availableCarTypes)
		{
			System.err.println(carType);
		}
	}

	/**
	 * Retrieve a quote for a given car type (tentative reservation).
	 * 
	 * @param clientName
	 *            name of the client
	 * @param start
	 *            start time for the quote
	 * @param end
	 *            end time for the quote
	 * @param carType
	 *            type of car to be reserved
	 * @param region
	 *            region in which car must be available
	 * @return the newly created quote
	 * 
	 * @throws Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected Quote createQuote(String clientName, Date start, Date end,
			String carType, String region) throws Exception {		
		
		ReservationConstraints constraints = new ReservationConstraints(start, end, carType, region);
		return this.carRentalCompany.createQuote(constraints, clientName);
	
	}

	/**
	 * Confirm the given quote to receive a final reservation of a car.
	 * 
	 * @param quote
	 *            the quote to be confirmed
	 * @return the final reservation of a car
	 * 
	 * @throws Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected Reservation confirmQuote(Quote quote) throws Exception {
		return this.carRentalCompany.confirmQuote(quote);
	}

	/**
	 * Get all reservations made by the given client.
	 * 
	 * @param clientName
	 *            name of the client
	 * @return the list of reservations of the given client
	 * 
	 * @throws Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected List<Reservation> getReservationsByRenter(String clientName) throws Exception 
	{	
		return this.carRentalCompany.getReservationsByRenter(clientName);		
	}

	/**
	 * Get the number of reservations for a particular car type.
	 * 
	 * @param carType
	 *            name of the car type
	 * @return number of reservations for the given car type
	 * 
	 * @throws Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected int getNumberOfReservationsForCarType(String carType)
			throws Exception {
		return this.carRentalCompany.getNumberOfReservationsForCarType(carType);
	}
}