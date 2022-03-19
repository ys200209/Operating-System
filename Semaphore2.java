

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Semaphore2 {
   public static Semaphore writer = new Semaphore(1); // 세마포어 생성 
    public static Semaphore reader = new Semaphore(1);
    public static int readers = 0, waitTime = 0; // 리더의 수, Writer가 대기한 대기 시간
    public static long readStartTime, writeStartTime;
    public static String share = "before"; // 공유 자원
    public static boolean arived = false, writeLock = false; // Writer 요청이 들어왔는지 나타내는 변수, Writer가 사용중인지 나타내는 변수

    public static Queue<SomeResource> readerQueue = new LinkedList<>();
    public static Queue<SomeResource> writerQueue = new LinkedList<>();
    
    public static Queue<SomeResource> readerPQ = new PriorityQueue<>();
    public static Queue<SomeResource> writerPQ = new PriorityQueue<>();
    
   public static void main(String[] args) {
      
      // Reader 스레드 생성
      new Thread() {
         public void run() {
            for(int i=1; i<=50; i++) {
            	readStartTime = System.currentTimeMillis();
            	SomeResource resource = new SomeResource(i, 0, readStartTime);
            	readerQueue.offer(resource);
                resource.useReader();
            }
         }
      }.start();
      
      
      // Writer 스레드 생성
   	  new Thread() {
   		  public void run() {
   		  	  for(int i=1; i<=10; i++) {
   		  		  writeStartTime = System.currentTimeMillis();
   		  		  SomeResource resource = new SomeResource(100+i, 0, writeStartTime);
   		  		  writerQueue.offer(resource);
 				  resource.useWriter();
   			  }
   		  }
   	  }.start();
   }
   
   
   static class SomeResource implements Comparable<SomeResource>{
      
	   	  private int semaNum;
	      private int waitTime;
	      private long startTime;
	      
	      public SomeResource(int semaNum, int waitTime, long startTime) {
	         this.semaNum = semaNum;
	         this.waitTime = waitTime;
	         this.startTime = startTime;
	      }
	      
	      public void Aging() { // 에이징 기법
	    	// 리더의 대기시간을 늘려주는 부분
	    	if (!readerPQ.isEmpty()) { // 대기중인 라이터가 아직 남아있다면 ( 라이터의 대기 시간을 1씩 늘려주는 부분 )
	    		readerPQ.forEach((o) -> { // object (resource) // 다른 프로세스가 빠져나갔으니 남은 writer의 모든 대기 시간을 1만큼 증가시켜줌
	    			this.waitTime = o.waitTime + 1;
	    		});
	    	}

	    	// 라이터의 대기시간을 늘려주는 부분
	    	if (!writerPQ.isEmpty()) { // 대기중인 라이터가 아직 남아있다면 ( 라이터의 대기 시간을 1씩 늘려주는 부분 )
	    		writerPQ.forEach((o) -> { // object (resource) // 다른 프로세스가 빠져나갔으니 남은 writer의 모든 대기 시간을 1만큼 증가시켜줌
	    			this.waitTime = o.waitTime + 1;
	    		});
	    	}
	      }
      
      public void useReader() {
         if (semaNum == 1) System.out.println("(Reader arrive)");
    	  
           try {
        	   // System.out.println("(Reader arrive) : " + semaNum);
        	   
        	   
               if (writeLock) { // 현재 Writer가 쓰고 있으면
            	   // System.out.println("writeLock = true");
            	   reader.acquire(); // Reader는 대기함
               }
               
               /*if ( System.currentTimeMillis() - writerQueue.peek().startTime >= 1000) {
            	   
               }*/

               /*if (writerPQ.peek().waitTime == 20) {
            	   
               }*/
               
               readers += 1;
               if (readers == 1) { // Reader가 하나라도 실행중이라면 Writer는 대기시킴
                   writer.acquire(); // Writer 접근 대기 (Writer P연산)
               }
               
               
               // Reader 임계 영역
               Thread.sleep(100);
               
               readerPQ.poll();
               System.out.println("(Read "+semaNum+") : " + share + ", waitTime = " + 
            		   (System.currentTimeMillis() - readerQueue.poll().startTime));
                
               
               reader.acquire();
               readers -= 1;
               if (readers == 0) { 
                   writer.release(); // Writer 접근 허용 (Writer V연산)
               } 
               reader.release();
                
           } catch (Exception e) {
             e.printStackTrace();
           }
       }
      
      public void useWriter() {
           try {
        	   
               System.out.println("(Writer arrive)");
              
               if (readers > 0 || !readerPQ.isEmpty()) {
            	   writer.acquire(); // Writer P연산 ( V연산이 호출될때까지 대기 )
               }
               writeLock = true;
              writerPQ.poll();
               
               // Writer 임계 영역 진입
               share = "after " + semaNum;
               System.out.println("(Writer 임계 영역 사용)" + ", waitTime = " + 
            		   (System.currentTimeMillis() - writerQueue.poll().startTime));
               
               writeLock = false;
               
               reader.release(); // Writer가 쓰느라 대기중이던 Reader들에게 읽기를 허용시킴
               writer.release();
               
               
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
      
      @Override
      public int compareTo(SomeResource o1) {
    	  if (this.waitTime < o1.waitTime) {
    		  return -1;
    	  }
    	  return 1;
      }
      
   }
   
}