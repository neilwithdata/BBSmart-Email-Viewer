package com.bbsmart.pda.blackberry.smartview.multi;

public final class Semaphore {

	// Semaphore state: (> 0, available); (<= 0, not available)
	private int value;

	/**
	 * Semaphore() creates and returns a reference to a new available binary
	 * Semaphore preconditions: none postconditions: new free binary Semaphore
	 * created
	 */
	public Semaphore() {
		value = 1;
	}

	/**
	 * Semaphore(initial) creates and returns a reference to a new Semaphore
	 * with starting value initial preconditions: none postconditions: new
	 * Semaphore created with value initial
	 */
	public Semaphore(int initial) {
		value = initial;
	}

	/**
	 * procure() attempts to gain access to the protected critical region,
	 * waiting until the state specifies this is possible. preconditions: none
	 * postconditions: InterruptedException thrown if interrupted during wait
	 * wait is finished. Semaphore state is decremented.
	 */
	public synchronized void procure() throws InterruptedException {
		while (value <= 0) {
			wait();
		}
		value--;
	}

	/**
	 * vacate() exits the protected critical region, notifying a thread waiting
	 * on this to occur. preconditions: none; postconditions: Semaphore state is
	 * incremented. thread waiting is notified
	 */
	public synchronized void vacate() {
		value++;
		notify();
	}
}