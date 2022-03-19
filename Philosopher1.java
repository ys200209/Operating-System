

import java.util.concurrent.Semaphore;

class Philosopher1 extends Thread { // 식사하는 철학자 문제 해결
    private int id; // 철학자 고유번호
    private Semaphore left_fork, right_fork; // 왼쪽, 오른쪽 포크
    
    public Philosopher1(int id, Semaphore left_fork, Semaphore right_fork) {
        this.id = id;
        this.left_fork = left_fork;
        this.right_fork = right_fork;
    }
    
    public void run() {
        try {
        	
        	for(int i=0; i<1; i++) { // 두번씩 식사를 하도록. 
        		if(id % 2 == 0) { // 고유번호가 짝수라면 자신의 왼쪽 포크부터 선점함
            		left_fork.acquire(); // 왼쪽 포크에 P연산 (세마포어 값을 1만큼 감소시켜 0으로 만든다)
            		right_fork.acquire(); // 오른쪽 포크에 P연산 
                }
                else { // 고유 번호가 홀수라면 자신의 오른쪽 포크부터 선점함
                	right_fork.acquire(); // 오른쪽 포크에 P연산 
                	left_fork.acquire(); // 왼쪽 포크에 P연산 
                }
            	
            	System.out.println("Philosopher " + id + " eating"); // 두개의 포크을 들고 식사
            	right_fork.release(); // 오른쪽 포크에 V연산 (0이었던 세마포어 값을 1만큼 증가시켜 1로 만든다)
                left_fork.release(); // 왼쪽 포크에 V연산
                System.out.println("Philosopher " + id + " thinking");
        	}
            
        } catch (InterruptedException e) {}
    }
    
    public static void main(String[] args) {
    	int count = 5; // 철학자의 수
    	
        Semaphore[] fork = new Semaphore[count]; // 철학자의 수만큼 포크 생성
        for(int i = 0; i < count; i++) {
        	fork[i] = new Semaphore(1); // 하나의 포크마다 한명씩만 사용할 수 있도록 세마포어 값을 1로 설정
        } 
        Philosopher1[] phil = new Philosopher1[count]; // 철학자를 count 갯수만큼 생성 
        
        // 특정 철학자의 고유 번호와 자신의 양 옆 포크를 초기화시킴
        for(int i = 0; i < count; i++) {
        	phil[i] = new Philosopher1(i, fork[i], fork[(i + 1) % count]); 
        }
        
        for(int i = 0; i < count; i++) {
        	phil[i].start(); // 식사 시작
        }
            
    }
}
