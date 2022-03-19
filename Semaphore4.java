import java.util.*;
import java.util.concurrent.Semaphore;

public class Semaphore4 {   //수행시간 측정을 위해 만들어둔 클래스
    public static int num, sum = 0; 
    /*
    num = 사용자로부터 입력받은 스레드 개수   
    sum = 수행시간 측정을 위해 임계영역 수행할때매다 더해주는 값 
    */ 
    public static long startTime;   //수행 시간 측정시작을 위한 변수 선언


    public static void main(String[] args) {
       final SomeResource resource = new SomeResource(3);   //스레드 내부에서 수행 할 작업설정
       Thread t;   //스레드 객체 선언
      
      Scanner sc = new Scanner(System.in); //입력을 받기 위한 Scanner클래스 선언
      
      System.out.print("스레드 개수를 입력하세요 : ");
      num = sc.nextInt(); // 정수를 입력받음

       // 시작 시간 설정
       startTime = System.currentTimeMillis();   //수행시간 측정시작
       for(int i = 1 ; i <= num ; i++) {   //입력받은 정수만큼 스레드를 생성
            t = new Thread(new Runnable() {
                public void run() {
                    resource.use();
                }
            });
            t.start();   //스레드 수행
        }
       
    }
 }


 class SomeResource {

    private final Semaphore semaphore;
    private final int maxThread;
    
    public SomeResource(int maxThread) {   //생성자
       this.maxThread = 0;
       this.semaphore = new Semaphore(maxThread);   //작업설정시 3값을 주었으므로 최대 3개의 스레드를 수용가능한 세마포어 클래스를 만든다.(큐의 개수가 3개)
    }
    
    public void use() {
       try {
          semaphore.acquire(); // Thread 가 semaphore에게 시작을 알림 (P연산)
          
          System.out.println("[" + Thread.currentThread().getName() + "]" 
                                  + semaphore.availablePermits() + "개의 Thread 사용가능" );
          
          // semaphore.availablePermits() 사용가능한 Thread의 숫자
          
          Semaphore4.sum += 1; // 임계 영역 실행, 수행할때마다 값을 1씩 더해준다. 
          
          semaphore.release(); // Thread 가 semaphore에게 종료를 알림 (V연산)

          if (Semaphore4.sum == Semaphore4.num) {      //임계영역 수행할때마다 더해준 값이랑 사용자로 부터 입력한 스레드값이 같아지면
                System.out.println("finish");   //스레드가 종료되었음을알리고 
                System.out.println("실행 시간 : " + (System.currentTimeMillis()-Semaphore4.startTime));      //실행시간을 측정한다.
          }
          
       } catch (InterruptedException e) {
          e.printStackTrace();
       }
    }
 }

