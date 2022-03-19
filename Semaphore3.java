

import java.util.concurrent.Semaphore;

public class Semaphore3 {
	public static Semaphore writer = new Semaphore(1); // 세마포어 생성 
    public static Semaphore reader = new Semaphore(1);
    public static int readers = 0; // 리더의 수
    public static String share = "before"; // 공유 자원
    public static boolean writeLock = false; // Writer가 사용중인지 나타내는 변수

	public static void main(String[] args) {
		
		// Reader 스레드 생성
		new Thread() {
			public void run() {
				for(int i=1; i<=100; i++) {
					SomeResource resource = new SomeResource(i);
					resource.useReader();
				}
			}
		}.start();
		
		
		// Writer 스레드 생성
		new Thread() {
			public void run() {
				SomeResource resource = new SomeResource(1);
				resource.useWriter();
			}
		}.start();
		
		
	}
	
	
	static class SomeResource {
		
		private int semaNum;
		
		public SomeResource(int semaNum) {
			this.semaNum = semaNum;
		}
		
		public void useReader() {
			if (semaNum == 1) System.out.println("(Reader 도착)");
	        try {
	        	
	        	if (writeLock) { // 현재 Writer가 쓰고 있으면 
            		reader.acquire(); // Reader는 대기함
            	}
	        	
            	readers += 1;
                if (readers == 1) { // Reader가 하나라도 실행중이라면 Writer는 대기시킴
                	writer.acquire(); // Writer 접근 대기 (Writer P연산)
                }
                
                
                // Reader 임계 영역
                System.out.println("(Read "+semaNum+") : " + share);
                
                
                readers -= 1;
                if (readers == 0) { 
                	writeLock = true; // Reader는 대기시킴
                	writer.release(); // Writer 접근 허용 (Writer V연산)
                }
                
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	    }
		
		public void useWriter() {
	        try {
	        	System.out.println("(Writer 도착)");
	        	
            	writer.acquire(); // Writer P연산 
            	
            	// Writer 임계 영역 진입
            	share = "after";
            	System.out.println("(Writer "+semaNum+")");
            	
            	writeLock = false; // Reader 진입 허용
            	reader.release(); // Writer가 쓰느라 대기중이던 Reader들에게 읽기를 허용시킴
            	
            	writer.release();
	            
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
		
	}
	
}