

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Semaphore1 {
	public static Semaphore writer = new Semaphore(1); // 세마포어 생성 
    public static Semaphore reader = new Semaphore(1);
    public static int readers = 0, waitTime = 0; // 리더의 수, Writer가 대기한 대기 시간
    public static String share = "before"; // 공유 자원
    public static boolean arrived = false, readLock = false, writeLock = false; // Writer 요청이 들어왔는지 나타내는 변수, Writer가 사용중인지 나타내는 변수
    
    public static Queue<SomeResource> queue = new LinkedList<>();
    public static Queue<SomeResource> readyQueue = new LinkedList<>();
    
    public static Queue<SomeResource> readerPQ = new PriorityQueue<>();
    public static Queue<SomeResource> readyReaderPQ = new PriorityQueue<>();
    
    public static Queue<SomeResource> writerPQ = new PriorityQueue<>();
    public static Queue<SomeResource> readyWriterPQ = new PriorityQueue<>();

	public static void main(String[] args) {
		
		// Reader 스레드 생성
		new Thread() {
			public void run() {
				for(int i=1; i<=10; i++) {
					SomeResource resource = new SomeResource(i, 0);
					queue.offer(resource);
					readerPQ.offer(resource);
					resource.controller();
				}
			}
		}.start();
		
		
		// Writer 스레드 생성
		new Thread() {
			public void run() {
				for(int i=1; i<=1; i++) {
					SomeResource resource = new SomeResource(100+i, 0);
					queue.offer(resource);
					writerPQ.offer(resource);
					resource.controller();
				}
			}
		}.start();
		
	}
	
	
	static class SomeResource implements Comparable<SomeResource> {
		
		public int semaNum;
		public int waitTime;
		
		public SomeResource(int semaNum) {
			this.semaNum = semaNum;
		}
		
		public SomeResource(int semaNum, int waitTime) {
			this.semaNum = semaNum; 
			this.waitTime = waitTime;
		}
		
		public void controller() {
			if (queue.size() == 55) {
				System.out.println("queue.size : " + queue.size());
				while(!queue.isEmpty()) {
					SomeResource resource = queue.poll();
					if (resource.semaNum <= 100 && writerPQ.peek().waitTime < 5) { 
						// 리더가 큐에서 뽑히면서 라이터의 대기시간이 10 아래라면
						readerPQ.poll();
						resource.useReader();
						
					} else if (resource.semaNum <= 100 && writerPQ.peek().waitTime >= 5) { 
						// 리더가 큐에서 뽑히면서 라이터의 대기시간이 10 이상이라면

						if (resource.waitTime >= writerPQ.peek().waitTime) {
							// 만약 리더의 대기시간이 라이터의 대기시간보다 길다면
							readerPQ.poll();
							resource.useReader();
							
						} else {
							// 라이터가 더 오래 대기했다면
							writerPQ.poll();
							writeLock = true;
							resource.useWriter();
							
						}
					} else { // 라이터가 큐에서 뽑혔다면
						writerPQ.poll();
						writeLock = true;
						resource.useWriter();
					}
					
					
					Aging();
				}
			} 
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
			
			System.out.println("readerPQ.size : " + readerPQ.size() + ", writerPQ.size : " + writerPQ.size());
			System.out.println("readerPQ.peek() : " + readerPQ.peek().waitTime + ", writerPQ.peek() : " + writerPQ.peek().waitTime);
			
		}
		
		public void useReader() {
			// if (semaNum == 1) System.out.println("(Reader 도착)");
	        try {
	        	System.out.println("(Reader 도착) : " + semaNum);
	        	
	        	
	        	if (writeLock) { // 라이터가 쓰고있다면
	        		// writer가 10번 이상 기다렸으면서 동시에 리더가 대기한 시간보다 더 오래 기다렸다면 
	        		reader.acquire(); // 리더는 멈추고 라이터에게 양보 
	        	} 
	        	
	        	reader.acquire();
            	readers += 1; 
                if (readers == 1) { // Reader가 하나라도 실행중이라면 Writer는 대기시킴 
                	writer.acquire(); // Writer 접근 대기 (Writer P연산) 
                }
                
                if (!readerPQ.isEmpty()) {
                	readerPQ.poll();
                }
                // Aging(); // 다른 모든 스레드에 대기시간을 부여한다.
                
                reader.release();
                
                
                // Reader 임계 영역 
                System.out.println("(Read "+semaNum+") : " + share + ", readers : " + readers); 
                
                
                readers -= 1; 
                if (readers == 0) { 
                	// writeLock = true; // Reader는 대기시킴
                	
                	if (!writerPQ.isEmpty()) {
                		writeLock = true;
                		writer.release(); // Writer 접근 허용 (Writer V연산)
                		System.out.println("(Reader) readers == 0 ");
                	} 
                	
                } 
                
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	    }
		
		public void useWriter() {
	        try {
	        	System.out.println("(Writer 도착) : " + this.semaNum);
	        	
	        	System.out.println("(Writer) Wait");
	        	if (readers > 0 || !readerPQ.isEmpty() || writeLock) { // 리더가 실행중이거나 리더큐가 대기중이거나 다른 라이터가 사용중이라면
	        		writer.acquire(); // Writer P연산 ( V연산이 호출될때까지 대기 )
	        	}
	        	writeLock = true;
        		
        		if (!writerPQ.isEmpty()) {
	        		writerPQ.poll(); // 최고 대기시간의 라이터를 하나 빼준다.
	        	}
            	// Aging();
        		
        		System.out.println("(Writer) Signal");
            	
            	// Writer 임계 영역 진입
            	// arrived = false;
            	share = "after " + this.semaNum;
            	System.out.println("(Writer "+semaNum+")");

            	if (!writerPQ.isEmpty() && !readerPQ.isEmpty()) { // 둘다 존재한다면
            		System.out.println("1");
	        		if (writerPQ.peek().waitTime >= 10 && readerPQ.peek().waitTime < writerPQ.peek().waitTime) {
	        			System.out.println("2");
	        			// 라이터의 대기시간이 10 이상이면서 리더의 대기시간보다 더 많다면
	            		writer.release(); // 라이터를 실행
	        		} else { // 그게 아니라면 리더 실행시킴
	        			System.out.println("2_2");
	        			writeLock = false;
	        			reader.release(); // Writer가 쓰느라 대기중이던 Reader들에게 읽기를 허용시킴
	        		}
            	} else if (readerPQ.isEmpty() && !writerPQ.isEmpty()) { // 리더큐에는 대기중인 리더가 없으면서 라이터가 대기중이라면
            		System.out.println("3");
            		writer.release(); // 라이터를 실행
            	} else if (!readerPQ.isEmpty() && writerPQ.isEmpty()) { // 리더는 존재하며 라이터는 존재하지 않는경우
            		System.out.println("4");
            		writeLock = false;
                	reader.release(); // Writer가 쓰느라 대기중이던 Reader들에게 읽기를 허용시킴
            	} else if (readerPQ.isEmpty() && writerPQ.isEmpty()){ // 둘다 없다면
            		System.out.println("4_4");
            		writeLock = false;
            		reader.release();
            	} else {
            		System.out.println("ELSE.. : " + writeLock);
            	}

	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
		
		@Override
		public int compareTo(SomeResource r1) {
			if (this.waitTime < r1.waitTime) {
				return -1;
			}
			return 1;
		}
		
	}
	
}