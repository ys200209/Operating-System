

import java.util.*;
import java.util.concurrent.Semaphore;


class Project {
   //스레드가 작업중일때 다른 스레드가 접근하지 못하게 하기위해 세마포어 생성
   public static Semaphore Sema_process = new Semaphore(1); 
  
   //스레드를 임의의 순서대로 진행 시키기 위해 큐에 스레드를 담기위해 생성
   public static Queue<Process> queue = new LinkedList<>();
   
   
   static int count = 0;
   
   //나눠 줄 수 있는 자원 수
   static int avail[] = {3, 3, 2};
    
   //현재 사용되는 스레드 수 
   static int P = 5;
   
   //현재 사용되는 자원의 수 
   static int R = 3;
   
   //스레드가 충족해야 할 자원의 수
   static int[][] need = new int[P][R]; 
   
   static int random[] = new int[P]; // 임의의 순서를 지정하기 위한 변수
   static int[] safeSeq = new int[P];   //안정상태임을 나타내기 위한 배열
   static int[] work = new int[R];    //자원의 종류 개수를 담기위한 배열 
   static boolean[] finish = new boolean[P]; // 프로세스 연산을 마쳤음을 나타내는 finish 배열 생성 
   
   static boolean[] checkDeadLock = new boolean[P]; // 교착 상태에 걸린 스레드들을 검사 했는지를 체크하는 변수
   
   public static Process[] deadLockProcesses = new Process[P];// 데드락에 걸린 프로세스들을 각 프로세스에 번호에 해당하는 인덱스에 담아두는 배열
   public static boolean[] deadLock = new boolean[P];// 프로세스별로 데드락 여부를 나타내는 불린형 배열
   
   
   public static int[] score = new int[P]; //패널티 값을 담기위한 배열(값이 적을수록 최적의 결과)
   public static int deleteProcess = 0; // 몇번째 프로세스를 지우는게 최적일지 담아두는 변수
   
   public static int min_score = 0; // 파라미터로 받아온 교착상태인 프로세스를 종료했을때 얼마만큼의 패널티가 있는지 측정하는 변수

    
    public static void main(String[] args) {
    
    
      Random r = new Random();
      //임의의 스레드 출력을 위해서 난수를 발생시킴 
      for(int i=0; i<P; i++){
         random[i] = r.nextInt(5); // 1 ~ 5까지의 난수 
         for(int j=0; j<i; j++){
            if(random[i] == random[j]){
               i--;
            }
         }
      }
      
      // 요구하는 최대 자원수
      int maxm[][] = {{7, 5, 3},
                        {3, 2, 2},
                        {9, 0, 2},
                        {2, 2, 2},
                        {4, 3, 3}};
   
      //현재 보유한 자원수 
      int allot[][] = {{0, 1, 0},
                        {2, 0, 0},
                        {3, 0, 2},
                        {2, 1, 1},
                        {0, 0, 2}};
      
      
     // 자원 종류의 갯수만큼 available 자원을 work로 넘겨 연산을 수행
     for (int i = 0; i < R; i++) {
         work[i] = avail[i]; 
      }
      
     System.out.println("work : " + Arrays.toString(work));
      
     //지정한 스레드 개수만큼 스레드를 생성
      for(int i=0; i<P; i++) {
        //난수를 index에 삽입해서 
         int index = random[i];
         //임의의 순서로 스레드가 동작하게 된다. 
         Process process = new Process(index, maxm[index], allot[index]);
         //입력이 들어온 순서대로 응답해주기 위해서 큐를 사용해 스레드를 큐에 삽입
         queue.offer(process);
         //스레드 동작시킴
         process.start();
      }
      
      //교착상타를 확인하고 보고하고 연관된 스레드 종료를 위한 Observer 스레드 동작시킴 
      Observer observer = new Observer();
      observer.start();
      

    }
   
    //실제 작업을 수행하는 스레드 생성 
    static class Process extends Thread {
       private int processNum;   //스레드 번호
       private int[] max;      //스레드가 가질수 있는 최대 자원수
       private int[] allot;      //스레드가 현재 보유한 자원수 
      
       public Process(int processNum, int[] max, int[] allot) {
          this.processNum = processNum;
          this.max = max;
          this.allot = allot;
       }
       
       public int getProcessNum() {
          return this.processNum;
       }
       
       public int[] getMax() {
          return this.max;
       }
       
       public int[] getAllot() {
          return this.allot;
       }
       
       //스레드가 가질수 있는 최대 자원수에서 스레드가 현재 보유한 자원수를 빼는 과정을 통해서
       //스레드가 충족해야 할 자원의 수를 알아내기 위한 메서드 
       public void calculateNeed(int need[][], int processNum, int maxm[], int allot[]) {
          for (int j = 0 ; j < R ; j++)
           // 들어온 프로세스(스레드)의 번호에 따른 need 자원을 초기화해줌.
             need[processNum][j] = maxm[j] - allot[j]; 
       }
       
       @Override
       public void run() {
          //스레드가 충족해야 할 자원의 수를 알아내기 위한 메서드 
           calculateNeed(need, this.getProcessNum(), this.getMax(), this.getAllot());
           
           //세마포어를 사용해서 스레드가 현재 작업 공간을 사용하고 있을때 다른 스레드가 접근히자 못하게 함 
           try {
              Sema_process.acquire();
           } catch (InterruptedException e) { }
           
           //큐에서 가장 먼저 들어온 스레드를 꺼내서 
           Process p = queue.poll();
           System.out.println("프로세스 번호 : " + p.getProcessNum());
           //자신이 교착상태인지 아닌지를 체크함
           isSafe(p);
           
           //모든 작업을 마무리 하였으므로 release메서드를 사용해서 다른 스레드 접근을 허용해줌
           Sema_process.release();
          
       }
       
       // 현재 프로세스들이 교착상태가 일어나는지 아닌지 확인하고 교착상태가 발생하는 프로세스를 담아주는 메서드
       public static void isSafe(Process p) { 
          //수행했던 프로세스를 다시 수행하지 않기 위한 finish배열을 통해서 
           if (finish[p.getProcessNum()] == false) { // 아직 수행하지 않은 프로세스라면 
            //j를 for문의 변수가 아니라 전역 변수로 설정해서 j의 개수가 R과 같아지는 상황에도 이용 가능하도록 함
             int j; 
             for (j = 0; j < R; j++) { // 리소스를 하나씩 탐색하면서 
               if (need[p.getProcessNum()][j] > work[j]) { // 요구하는 자원이 현재 나눠줄 수 있는 자원수보다 많으면
                   break; // 멈춘다.
               } 
            }

             if (j == R) { // 만약 다 수행되고 j의 개수가 R과 같아졌을때 즉, 모든 요구자원을 나눠줄 수 있는 상태라면 
                try {
                   Thread.sleep(100);
                   
                    for (int k = 0 ; k < R ; k++) {// 자원의 개수만큼 탐색 
                       //모든 자원을 나눠줄 수 있는 상태 즉, 자원을 다 빌려주고 
                       //자원의 개수만큼 반납하는 개념이므로 work에 현재 보유 자원수를 더해줌
                       work[k] += p.getAllot()[k]; 
                    }
                  
                    safeSeq[count++] = p.processNum; // 안정상태의 순서를 기록한다.

                    
                    finish[p.getProcessNum()] = true; // finish 배열의 해당 값을 true로 하여 해당 번호의 수행을 마쳤음을 의미해줌
                    deadLock[p.getProcessNum()] = false; //deadLock에 false를 줘서 안정상태이므로 탐지 할 필요가 없음을 알림
                } catch (InterruptedException e) { } 
                
             } else {
                //모든 요구자원을 나눠줄 수 없는 상태라면 deadLock에 true를 줘서 
                 //안정상태가 아니므로 탐지 할 필요가 있음을 알림
                  deadLock[p.getProcessNum()] = true; 
                  //deadLock배열의 인덱스와 동일한 실질적 스레드를 담는 deadLockProcesses에 스레드를 담음
                  deadLockProcesses[p.getProcessNum()] = p; 
             }
          }
           //큐에 남아있는 자원이 없을때 즉, 모든 스레드를 사용해 봣을때 
           if (queue.isEmpty()) {
              //그때의 안정상태 배열을 출력 
              System.out.print("안정상태 : " );
              for (int i = 0; i < count ; i++) {
                  System.out.print(safeSeq[i] + " ");
              }
                 
              System.out.println();
             
           }
       }
       
       public static void DFS(Process process) { // 재귀함수 DFS (Deep First Search: 깊이 우선 탐색)
           
           if (process == null) return; // 만약 0부터 P번쨰까지의 프로세스가 파라미터로 들어왔는데 해당 프로세스가 교착상태가 아니라면 재귀함수를 종료함. 
           // ( DFS는 삭제 시뮬레이션을 구현한 메서드이기 때문에 교착상태가 아니라면 삭제할 필요가 없으니까. )
           
           int index = process.getProcessNum(); // 파라미터로 들어온 프로세스의 번호를 index 변수에 받아옴
           
           for (int i=0; i<deadLock.length; i++) { // 모든 프로세스를 탐색함
               if (!checkDeadLock[index] && deadLock[index]) { // 파라미터로 들어온 프로세스가 만약 교착상태면서 아직 검사한적이 없는 프로세스라면
                  
                  checkDeadLock[index] = true; // 파라미터로 받아온 교착상태인 프로세스를 검사한적 있다고 표시함. ( 이 프로세스는 이제 삭제하여 존재하지 않을것이라고 가정 )
                  min_score++; // 패널티 점수를 하나 늘림 
                  
                  for(int j=0; j<R; j++) { 
                     work[j] += process.getAllot()[j]; // 삭제했다고 가정하고 해당 프로세스의 자원을 획득해오는 과정. 
                     // ( 그럼 남은 교착상태 프로세스들에게 할당해줄 수 있는 자원이 늘어날것이다. )
                  }
                  
                  DFS(deadLockProcesses[i]); // 0번째부터 P번째 프로세스까지 하나하나 가상으로 삭제를 수행하는 탐색 알고리즘 호출.
                  
                  for(int j=0; j<R; j++) {
                      work[j] -= process.getAllot()[j]; // 삭제를 통해 자원을 회수하기 전으로 회귀
                  }
                  
                  min_score--; // 패널티 점수를 늘리기 전으로 회귀
                  checkDeadLock[index] = false; // 재귀호출에서 빠져나옴으로써 검사를 하기 전으로 회귀
               }
            }
           
           if (score[process.getProcessNum()] > min_score && min_score != 0) {
              // 현재 자신의 패널티 점수와 재귀호출을 진행하며 얻은 패널티 점수를 비교해 좀 더 패널티 점수가 적은, 즉 실제로 삭제하기에 최적의 프로세스를 가져옴
              // 그러나 패널티 점수가 0일 경우는 삭제 시뮬레이션을 돌려보지 않았다는 의미이기에 이 경우는 삭제할 프로세스를 담는게 의미가 없어서 제외한다.
              score[process.getProcessNum()] = min_score; // 패널티 점수를 최솟값으로 변경
              
              System.out.println(Arrays.toString(score));
           }
        }
       
       
    }
    
    //교착상타를 확인하고 보고하고 연관된 스레드 종료를 위한 Observer 스레드
    static class Observer extends Thread {
           
           @Override
           public void run() {
              int deleteProcess = 0; // 삭제하기에 최적의 프로세스를 담아두는 변수
              try {
            	  while(true) {
            		  boolean isDead = false;
            		//5초 간격으로 지속적으로 교착상태를 확인
                      Thread.sleep(5000);
                      //어느 부분이 교착상태인가를 알아보기위해, 교착상태 유무에 따른 배열 출력 
                      System.out.println(Arrays.toString(deadLock));
                      
                      //스레드의 패널티를 담는 score배열을 초기화 
                      for(int i=0; i<P; i++) {
                         score[i] = (int)1e9;
                      }
                      
                      //프로세스의 수만큼 for문을 반복 
                      for(int i=0; i<P; i++) {
                         if (deadLock[i]) {
                         // 재귀함수 호출 (성능 테스트) : 데드락이 걸린 프로세스들을 하나씩 제거하여 최적의 결과를 내는지 체크하는 메서드
                        	 isDead = true;
                             Process.DFS(deadLockProcesses[i]); 
                              
                         }
                      }
                      
                      if (!isDead) {
                    	  System.out.println("더이상 교착상태 프로세스는 존재하지 않습니다.");
                    	  return;
                      }
                      
                      System.out.println("교착상태가 발생하였습니다.");
                     
                      for(int i=1; i<score.length; i++) { 
                         //우리가 삭제해야하는것이 패널티가 최소인 프로세스 이므로 그에 해당하는 번호를 가져옴
                         if (score[deleteProcess] > score[i]) {
                            deleteProcess = i;
                         }
                      }
                      
                      for(int j=0; j<R; j++) { 
                         // 교착 프로세스를 삭제하기 때문에 해당 프로세스가 가지고 있던 자원을 회수함.
                          work[j] += deadLockProcesses[deleteProcess].getAllot()[j]; 
                       }
                      deadLockProcesses[deleteProcess].interrupt(); // 교착 프로세스 삭제.
                      deadLock[deleteProcess] = false; // 해당 프로세스를 삭제했기 때문에 교착 상태값을 해제함.
                      finish[deleteProcess] = true; 
                      
                      //현재 삭제한 스레드를 알려주기 위한 출력문을 출력
                      System.out.println("삭제한 프로세스 번호 = " + deleteProcess);
                      
                      for(int i=0; i<deadLock.length; i++) { // 이제 남은 데드락을 탐색하며 아직 교착상태인 프로세스가 남아있는지 확인
                         if (deadLock[i]) {
                            System.out.println("아직 교착상태인 프로세스 : " + i);
                            //교착상태가 발생하는 프로세스를 다시 isSafe 메서드에 담아줌으로서 이 시점에서 안정상태가 되는지 다시 탐색
                              Process.isSafe(deadLockProcesses[i]);
                         }
                      }
            	  }
              } catch (InterruptedException e) { }
           }
        }
    
}