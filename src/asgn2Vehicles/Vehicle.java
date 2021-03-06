/**
 * 
 * This file is part of the CarParkSimulator Project, written as 
 * part of the assessment for INB370, semester 1, 2014. 
 *
 * CarParkSimulator
 * asgn2Vehicles 
 * 19/04/2014
 * 
 */
package asgn2Vehicles;

import asgn2Exceptions.VehicleException;
import asgn2Simulators.Constants;



/**
 * Vehicle is an abstract class specifying the basic state of a vehicle and the methods used to 
 * set and access that state. A vehicle is created upon arrival, at which point it must either 
 * enter the car park to take a vacant space or become part of the queue. If the queue is full, then 
 * the vehicle must leave and never enters the car park. The vehicle cannot be both parked and queued 
 * at once and both the constructor and the parking and queuing state transition methods must 
 * respect this constraint. 
 * 
 * Vehicles are created in a neutral state. If the vehicle is unable to park or queue, then no changes 
 * are needed if the vehicle leaves the carpark immediately.
 * Vehicles that remain and can't park enter a queued state via {@link #enterQueuedState() enterQueuedState} 
 * and leave the queued state via {@link #exitQueuedState(int) exitQueuedState}. 
 * Note that an exception is thrown if an attempt is made to join a queue when the vehicle is already 
 * in the queued state, or to leave a queue when it is not. 
 * 
 * Vehicles are parked using the {@link #enterParkedState(int, int) enterParkedState} method and depart using 
 * {@link #exitParkedState(int) exitParkedState}
 * 
 * Note again that exceptions are thrown if the state is inappropriate: vehicles cannot be parked or exit 
 * the car park from a queued state. 
 * 
 * The method javadoc below indicates the constraints on the time and other parameters. Other time parameters may 
 * vary from simulation to simulation and so are not constrained here.  
 * 
 * @author hogan
 * @author Jarrod Eades n8855722
 * 
 */
public abstract class Vehicle {
	
	// Initialise the private variables 
	
	private int arrivalTime = 0;
	private int parkingTime = 0;
	private int departureTime = 0;
	private int exitQueueTime = 0;
	
	private String vehID = "000AAA";
	
	private boolean parked = false;
	private boolean queued = false;
	private boolean satisfied = false;
	private boolean wasParked = false;
	private boolean wasQueued = false;
	
	/**
	 * Vehicle Constructor 
	 * @param vehID String identification number or plate of the vehicle
	 * @param arrivalTime int time (minutes) at which the vehicle arrives and is 
	 *        either queued, given entry to the car park or forced to leave
	 * @throws VehicleException if arrivalTime is <= 0 
	 */
	public Vehicle(String vehID,int arrivalTime) throws VehicleException  {
		
		// Throw an exception if arrivalTime is not valid
		if(zeroIsGreaterThanOrEqualTo(arrivalTime)){
			throw new VehicleException("The arrival time is less than or equal to zero.");
		}
		
		// Store the values
		this.vehID = vehID;
		this.arrivalTime = arrivalTime;
	}
	
	/**
	 * Transition vehicle to parked state (mutator)
	 * Parking starts on arrival or on exit from the queue, but time is set here
	 * @param parkingTime int time (minutes) at which the vehicle was able to park
	 * @param intendedDuration int time (minutes) for which the vehicle is intended to remain in the car park.
	 *  	  Note that the parkingTime + intendedDuration yields the departureTime
	 * @throws VehicleException if the vehicle is already in a parked or queued state, if parkingTime < 0, 
	 *         or if intendedDuration is less than the minimum prescribed in asgnSimulators.Constants
	 */
	public void enterParkedState(int parkingTime, int intendedDuration) throws VehicleException {
		
		// Throw an exceptions if parkingTime is invalid 
		if(zeroIsGreaterThan(parkingTime)){
			throw new VehicleException("Parking time is less than zero.");
		}
		
		// Throw an exception if queued or parked
		queuedOrParked();
		
		// Throw an exception if intended duration is invalid
		invalidCheckOf(intendedDuration);
		
		
		// Initialise the departure time of this vehicle
		this.departureTime = parkingTime + intendedDuration;
	
		// Set the vehicle as parked
		this.satisfied = true;
		this.parkingTime = parkingTime;
		this.parked = true;
		this.wasParked = true;
	}
	
	/**
	 * Transition vehicle to queued state (mutator) 
	 * Queuing formally starts on arrival and ceases with a call to {@link #exitQueuedState(int) exitQueuedState}
	 * @throws VehicleException if the vehicle is already in a queued or parked state
	 */
	public void enterQueuedState() throws VehicleException {
		
		// Throw an exception if queued or parked
		queuedOrParked();
		
		// Queue the vehicle
		this.queued = true;
		this.wasQueued = true;
	}
	
	/**
	 * Transition vehicle from parked state (mutator) 
	 * @param departureTime int holding the actual departure time 
	 * @throws VehicleException if the vehicle is not in a parked state, is in a queued 
	 * 		  state or if the revised departureTime < parkingTime
	 */
	public void exitParkedState(int departureTime) throws VehicleException {
		
		// Throw an exception if not in the expected state
		isNotInCorrectState(this.parked, this.queued);
				
		// Throw an exception if the departureTime is invalid
		parkingTimeGreaterThanOrEqualTo(departureTime);
		
		// Exit the parked state of the vehicle
		this.departureTime = departureTime;
		this.parked = false;
	}

	/**
	 * Transition vehicle from queued state (mutator) 
	 * Queuing formally starts on arrival with a call to {@link #enterQueuedState() enterQueuedState}
	 * Here we exit and set the time at which the vehicle left the queue
	 * @param exitTime int holding the time at which the vehicle left the queue 
	 * @throws VehicleException if the vehicle is in a parked state or not in a queued state, or if 
	 *  exitTime is not later than arrivalTime for this vehicle
	 */
	public void exitQueuedState(int exitTime) throws VehicleException {
		
		// Throw an exception if not in expected state
		isNotInCorrectState(this.queued, this.parked);
		
		// Throw an exception if exitTime invalid
		isExitingNotLaterThanArrival(exitTime);
		
		// Exit the queued state of the vehicle
		queued = false;
		
		// Change the boolean satisfied depending on queue time
		exitTimeGreaterThanMaxQueueTime(exitTime);
		
		// Set the exitTime
		exitQueueTime = exitTime;
	}
	
	/**
	 * Simple getter for the arrival time 
	 * @return the arrivalTime
	 */
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	/**
	 * Simple getter for the departure time from the car park
	 * Note: result may be 0 before parking, show intended departure 
	 * time while parked; and actual when archived
	 * @return the departureTime
	 */
	public int getDepartureTime() {
		return this.departureTime;
	}
	
	/**
	 * Simple getter for the parking time
	 * Note: result may be 0 before parking
	 * @return the parkingTime
	 */
	public int getParkingTime() {
		return this.parkingTime;
	}

	/**
	 * Simple getter for the vehicle ID
	 * @return the vehID
	 */
	public String getVehID() {
		return this.vehID;
	}

	/**
	 * Boolean status indicating whether vehicle is currently parked 
	 * @return true if the vehicle is in a parked state; false otherwise
	 */
	public boolean isParked() {
		return this.parked;
	}

	/**
	 * Boolean status indicating whether vehicle is currently queued
	 * @return true if vehicle is in a queued state, false otherwise 
	 */
	public boolean isQueued() {
		return this.queued;
	}
	
	/**
	 * Boolean status indicating whether customer is satisfied or not
	 * Satisfied if they park; dissatisfied if turned away, or queuing for too long 
	 * Note that calls to this method may not reflect final status 
	 * @return true if satisfied, false if never in parked state or if queuing time exceeds max allowable 
	 */
	public boolean isSatisfied() {
		return this.satisfied;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");
		String vehInfo = "";
		
		vehInfo += "Vehicle vehID: " + getVehID() + newLine;
		vehInfo += "Arrival Time: " + getArrivalTime() + newLine;
		
		if(wasQueued()){
			vehInfo += "Exit from Queue: " + exitQueueTime + newLine;
			vehInfo += "Queuing Time: " + (exitQueueTime - getArrivalTime()) + newLine;
		}else{
			vehInfo += "Vehicle was not queued" + newLine;
		}

		if(wasParked){
			vehInfo += "Entry to Car Park: " + getParkingTime() + newLine;			
			vehInfo += "Exit from Car Park: " + getDepartureTime() + newLine;
			vehInfo += "ParkingTime: " + (getDepartureTime() - getParkingTime()) + newLine;
		}else{
			vehInfo += "Vehicle was not parked" + newLine;
		}
		
		if(isSatisfied()){
			vehInfo += "Customer was satisfied" + newLine;
		} else{
			vehInfo += "Customer was not satisfied" + newLine;
		}
	
		return vehInfo;
	}

	/**
	 * Boolean status indicating whether vehicle was ever parked
	 * Will return false for vehicles in queue or turned away 
	 * @return true if vehicle was or is in a parked state, false otherwise 
	 */
	public boolean wasParked() {
		return this.wasParked;
	}

	/**
	 * Boolean status indicating whether vehicle was ever queued
	 * @return true if vehicle was or is in a queued state, false otherwise 
	 */
	public boolean wasQueued() {
		return this.wasQueued;
	}

	/**
	 * Checks to see if the vehicle is in a queued or parked state.
	 * @throws VehicleException if the vehicle is already in a queued or parked state
	 */
	private void queuedOrParked() throws VehicleException{
		if(this.queued || this.parked){
			throw new VehicleException("This vehicle is already parked or queued.");
		}
	}

	/**
	 * A comparison between the parameter and zero, checking if zero is 
	 * greater than or equal to the parameter
	 * @param value int which needs to be compared to zero 
	 */
	private boolean zeroIsGreaterThanOrEqualTo(int value){
		return 0 >= value;
	}
	
	/**
	 * A comparison between the parameter and zero, checking if zero is 
	 * greater than the parameter.
	 * @param value int which needs to be compared to zero
	 */
	private boolean zeroIsGreaterThan(int value){
		return 0 > value;
	}

	/**
	 * A check to see if intendedDuration is invalid and 
	 * throws an exception if necessary 
	 * @param intendedDuration int the estimation of a vehicles stay.
	 * @throws VehicleException if intendedDuration is less than the minimum 
	 * prescribed in asgnSimulators.Constants
	 */
	private void invalidCheckOf(int intendedDuration) throws VehicleException{
		if(intendedDuration < Constants.MINIMUM_STAY){
			throw new VehicleException("Intended duration is less than the minimum stay.");
		}	
	}

	/**
	 * Checks that the current state is the correct one
	 * @param stateThatShouldBeIn boolean the state that the vehicle is expected
	 * to be in
	 * @param stateThatShouldNotBeIn boolean the state that the vehicle is expected 
	 * not to be in
	 * @throws VehicleException - if not in the correct state
	 */
	private void isNotInCorrectState(boolean stateThatShouldBeIn, boolean stateThatShouldNotBeIn) throws VehicleException{
		if(!stateThatShouldBeIn || stateThatShouldNotBeIn){
			throw new VehicleException("This vehicle is queued or not parked.");
		}
		
	}


	/**
	 * Check the departure time is not invalid
	 * @param departureTime int holding the actual departure time 
	 * @throws VehicleException if the revised departureTime < parkingTime
	 */
	private void parkingTimeGreaterThanOrEqualTo(int departureTime) throws VehicleException{
		if(departureTime < this.parkingTime){
			throw new VehicleException("The departure time is less than the parking time.");
		}
	}

	/**
	 * Check the exitTime is not later than arrivalTime for this vehicle
	 * @param exitTime int holding the time at which the vehicle left the queue 
	 * @throws VehicleException if exitTime is not later than arrivalTime for this vehicle
	 */
	private void isExitingNotLaterThanArrival(int exitTime) throws VehicleException{
		if(exitTime <= this.arrivalTime){
			throw new VehicleException("This vehicle is exiting at the same time "
					+ "or before the arrival.");
		}	
	}

	/**
	 * Setting the customer satisfaction depending on the queue time
	 * @param exitTime int holding the time at which the vehicle left the queue 
	 */
	private void exitTimeGreaterThanMaxQueueTime(int exitTime){
		if(exitTime >= Constants.MAXIMUM_QUEUE_TIME){
			satisfied = false;
		}	
	}

}