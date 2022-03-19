

class DiningPhilosophers { // 식사하는 철학자 

	 public static void main(String args[]) { 
		  
	     Semaphore chopSticks[];
	     Philosopher woman[];

	 // Create an array of five Semaphore Object Reference Handles
	     chopSticks = new Semaphore[5];

	 // Create five Semaphore Objects and assign to the array
	     for (int i=0; i<5; i++)
	         chopSticks[i] = new Semaphore(1); // Semaphore initial value=1

	 // Create an array of five Philosopher Thread Object Reference Handles
	     woman = new Philosopher[5];
	 // Create and initiate five Philosopher Thread Objects

	     for (int i=0; i<5; i++) {

	         woman[i] = new Philosopher(i, chopSticks);

	         woman[i].start();

	     }
	 }
	}

	//The Semaphore class contains methods declared as synchronized.

	//A monitor therefore, will ensure that access to Semaphore methods is

	//mutually exclusive among threads.

	class Semaphore {
		
	 private int value;
	 
	 public Semaphore(int value) {
	     this.value = value;
	 }

	 public synchronized void p() {
	     while (value == 0) {
	         try {
	             System.out.println("ChopStick in use");
	             wait();       // The calling thread waits until semaphore
	                       // becomes free
	         } catch(InterruptedException e) {}
	     }
	     value = value - 1;
	 }

	 public synchronized void v() {
	     value = value + 1;
	     notify();
	 }
	}

	class Philosopher extends Thread {

	 private int myName;

	 private Semaphore chopSticks[];



	//

	//This is the constructor function which is executed when a Philosopher

	//thread is first created

	//

	 public Philosopher(int myName, Semaphore chopSticks[]) {

	     this.myName = myName;    // 'this' distinguishes the local private

	                       // variable from the parameter

	     this.chopSticks = chopSticks;

	 }
	//

	//This is what each philosopher thread executes

	//

	 public void run() {

	     while (true) {

	         System.out.println("Philosopher "+myName+" thinking.");

	         try {

	             sleep ((int)(Math.random()*20000));

	         } catch (InterruptedException e) {}

	         System.out.println("Philosopher "+myName+" hungry.");

	         chopSticks[myName].p();       // Acquire right

	         chopSticks[(myName+1)%5].p(); // Acquire left

	         System.out.println("Philosopher "+myName+" eating.");

	         try {

	             sleep ((int)(Math.random()*10000));

	         } catch (InterruptedException e) {}

	         chopSticks[myName].v();       // Release right

	         chopSticks[(myName+1)%5].v(); // Release left
	         
	     }

	 }

	}
