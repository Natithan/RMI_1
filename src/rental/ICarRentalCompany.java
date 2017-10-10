package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ICarRentalCompany extends Remote{

	/********
	 * NAME *
	 ********/

	public abstract String getName() throws RemoteException;

	public abstract List<String> getRegions() throws RemoteException;

	public abstract boolean hasRegion(String region) throws RemoteException;

	/*************
	 * CAR TYPES *
	 *************/

	public abstract Collection<CarType> getAllCarTypes() throws RemoteException;

	public abstract CarType getCarType(String carTypeName) throws RemoteException;

	// mark
	public abstract boolean isAvailable(String carTypeName, Date start, Date end) throws RemoteException;

	public abstract Set<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException;

	/****************
	 * RESERVATIONS *
	 ****************/

	public abstract Quote createQuote(ReservationConstraints constraints,
			String client) throws ReservationException, RemoteException;

	public abstract Reservation confirmQuote(Quote quote)
			throws ReservationException, RemoteException;

	public abstract void cancelReservation(Reservation res) throws RemoteException;

	public abstract List<Reservation> getReservationsByRenter(String clientName) throws RemoteException;

	public abstract int getNumberOfReservationsForCarType(String carType) throws RemoteException;


}