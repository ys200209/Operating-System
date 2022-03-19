

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class 에이징기법 {
	public static Semaphore writer = new Semaphore(1); // 세마포어 생성 
    public static Semaphore reader = new Semaphore(1);
    public static int readers = 0, waitTime = 0; // 리더의 수, Writer가 대기한 대기 시간
    public static long readStartTime, writeStartTime;
    public static String share = "before"; // 공유 자원
    public static boolean arived = false, writeLock = false; // Writer 요청이 들어왔는지 나타내는 변수, Writer가 사용중인지 나타내는 변수

    public static Queue<SomeResource> readerQueue = new LinkedList<>();
    public static Queue<SomeResource> writerQueue = new LinkedList<>();
    
   public static void main(String[] args) {
	   System.out.println("시작");
      
      // Reader 스레드 생성
      new Thread() {
         public void run() {
            for(int i=1; i<=100; i++) {
            	SomeResource resource1 = new SomeResource(i, System.currentTimeMillis());
            	readerQueue.offer(resource1);
                resource1.useReader();
            }
         }
      }.start();
      
      
      // Writer 스레드 생성
   	  new Thread() {
   		  public void run() {
   			for(int i=1; i<=100; i++) {
   				SomeResource resource2 = new SomeResource(100+i, System.currentTimeMillis());
 		  		writerQueue.offer(resource2);
				resource2.useWriter();
 			}
   		  }
   	  }.start();
   }
   
   static class SomeResource {
	   private int semaNum;
	   private long waitTime;
	      
	   public SomeResource(int semaNum, long waitTime) {
	       this.semaNum = semaNum;
	       this.waitTime = waitTime;
	   }
		   
	   public void useReader() {
		     try {
		  	     System.out.println("(Reader arrive) : " + semaNum);

		         if (writeLock) { // 현재 Writer가 쓰고 있으면
		        	 reader.acquire(); // Reader는 대기함
		         }
		         
		         // 에이징 기법 (대기시간이 특이점을 넘긴 시점부터 리더와 라이터를 비교하며 실행)
		         if (!writerQueue.isEmpty()) { // 쓰기 요청이 들어왔다면
		        	 if ((System.currentTimeMillis() - writerQueue.peek().waitTime) > 500) { // 라이터의 대기 시간이 0.5초 이상이라면
		        		 if (readerQueue.peek().waitTime < writerQueue.peek().waitTime) { // 그러면서 리더의 대기시간보다도 더 오래 기다렸다면
		        			 reader.acquire(); // Reader는 대기함
		        		 } 
		        		 // else : 라이터의 대기 시간이 0.5초 이상이지만 리더의 대기시간보다는 짧다면 그냥 리더를 실행시킴.
			         }
		         }
		         
		         readers += 1;
		         
		         // Reader 임계 영역
		         Thread.sleep(100);
		         
		         System.out.println("(Read "+semaNum+") : " + share + ", waitTime = " + 
		      		   (System.currentTimeMillis() - readerQueue.poll().waitTime));
		         
		         readers -= 1;
		         if (readers == 0 && readerQueue.isEmpty() && !writerQueue.isEmpty()) { 
		        	 // 리더가 실행중이 아니고 리더 요청이 안들어오면서 라이터 요청만 들어왔을 때
		             writer.release(); // Writer 접근 허용 (Writer V연산)
		         }
		          
		     } catch (Exception e) {
		       e.printStackTrace();
		     }
		 }

	public void useWriter() {
	     try {
	         System.out.println("(Writer arrive) : " + semaNum);
	        
	         if (!writerQueue.isEmpty()) {
	        	 
	        	 if (readers > 0 || !readerQueue.isEmpty() || writeLock) { // 리더가 사용중이거나 리더큐에서 대기중이라면 라이터는 wait.
		        	 writer.acquire(); // Writer P연산 ( V연산이 호출될때까지 대기 )
		         } 
		         // Writer 임계 영역 진입
		         writeLock = true;
		         
		         Thread.sleep(100);
		         
		         share = "after " + semaNum;
		         System.out.println("(Writer 임계 영역 사용)" + ", waitTime = " + 
		      		   (System.currentTimeMillis() - writerQueue.poll().waitTime));
		         
		         
		         
		         writeLock = false;
		         
		         if (readerQueue.isEmpty()) { // 리더가 아직 요청이 안들어왔다면
		        	 writer.release(); // 라이터를 실행
		         } else if (!readerQueue.isEmpty()){ // 리더가 현재 요청 대기중이라면
		        	 reader.release(); // 리더를 실행
		         }
	         }
	         
	     } catch (InterruptedException e) {
	         e.printStackTrace();
	     }
	 }

   }

}