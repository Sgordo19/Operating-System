// Rajay Trowers
// MLQ Class

import java.util.*;

public class MLQ {

    // PROCESS CLASS
    static class Process {
        String pid;
        int arrival;
        int burst;
        int start = -1;       // -1 indicates not started
        int completion = 0;
        int queue;            // 1=High, 2=Medium, 3=Low

        Process(String pid, int arrival, int burst, int queue) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.queue = queue;
        }
    }

    public static void runMLQTest() { //TO ACCEPT USER INPUTS
        Scanner sc = new Scanner(System.in);

        System.out.println("\n======================================");
        System.out.println("       MLQ SCHEDULER INPUT");
        System.out.println("======================================");
        
        System.out.print("Enter total number of processes: ");
        int n = sc.nextInt();

        List<Process> processes = new ArrayList<>();

        // Loop to get input for each process individually
        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Enter details for Process " + (i + 1) + " ---");
            
            System.out.print("Process ID (e.g., P1): ");
            String pid = sc.next();

            System.out.print("Arrival Time:          ");
            int arrival = sc.nextInt();

            System.out.print("Burst Time:            ");
            int burst = sc.nextInt();

            System.out.print("Queue Level (1, 2, or 3): ");
            int queue = sc.nextInt();

            processes.add(new Process(pid, arrival, burst, queue));
        }

        System.out.println("\nInput Complete! Starting Simulation...");
        
        // Run the Logic
        runMLQ(processes);

        System.out.println("\n=== END OF MLQ TEST ===\n");
    }


    //  MAIN SCHEDULING LOGIC
    public static void runMLQ(List<Process> processes) { //TO BE CALL IN MAIN.JAVA
        
        // Separate processes into queues
        List<Process> q1 = new ArrayList<>();  // High priority
        List<Process> q2 = new ArrayList<>();  // Medium priority
        List<Process> q3 = new ArrayList<>();  // Low priority

        for (Process p : processes) {
            if (p.queue == 1) q1.add(p);
            else if (p.queue == 2) q2.add(p);
            else q3.add(p);
        }

        // Sort by arrival time initially to ensure fairness within queues
        q1.sort(Comparator.comparingInt(p -> p.arrival));
        q2.sort(Comparator.comparingInt(p -> p.arrival));
        q3.sort(Comparator.comparingInt(p -> p.arrival));

        System.out.println("\nMULTI-LEVEL QUEUE (MLQ) SCHEDULING STARTED.....");

        int currentTime = 0;

        // Execute Queues Sequentially (High -> Medium -> Low)
        currentTime = runFCFS(q1, currentTime, 1);
        currentTime = runSJF(q2, currentTime, 2);
        currentTime = runFCFS(q3, currentTime, 3);

        // Combine all for final reporting
        List<Process> all = new ArrayList<>();
        all.addAll(q1);
        all.addAll(q2);
        all.addAll(q3);

        printMetrics(all, currentTime);
    }


    //  ALGORITHM: FCFS (First Come First Serve)
    private static int runFCFS(List<Process> queue, int time, int qLevel) {
        if (queue.isEmpty()) return time;

        System.out.println("\n--- Executing Queue " + qLevel + " (FCFS) ---");

        for (Process p : queue) {
            // If CPU is idle, jump to arrival time
            if (time < p.arrival) {
                time = p.arrival;
            }

            p.start = time;
            time += p.burst;
            p.completion = time;

            System.out.printf("Process %s completed at time %d%n", p.pid, p.completion);
        }
        return time;
    }

    //  ALGORITHM: SJF (Shortest Job First - Non-Preemptive)
    private static int runSJF(List<Process> queue, int time, int qLevel) {
        if (queue.isEmpty()) return time;

        System.out.println("\n--- Executing Queue " + qLevel + " (SJF) ---");

        List<Process> ready = new ArrayList<>();
        int completedCount = 0;

        while (completedCount < queue.size()) {
            // Add newly arrived processes to ready queue
            for (Process p : queue) {
                if (p.arrival <= time && p.start == -1 && !ready.contains(p)) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                // Optimization: Jump to next arrival instead of looping time++
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : queue) {
                    if (p.start == -1 && p.arrival < nextArrival) {
                        nextArrival = p.arrival;
                    }
                }
                
                // If we found a future process, jump to it. Otherwise increment time.
                if (nextArrival != Integer.MAX_VALUE) {
                    time = Math.max(time, nextArrival);
                } else {
                    time++;
                }
                continue;
            }

            // Select shortest job from ready queue
            Process shortest = Collections.min(ready, Comparator.comparingInt(p -> p.burst));
            ready.remove(shortest);

            shortest.start = time;
            time += shortest.burst;
            shortest.completion = time;

            System.out.printf("Process %s completed at time %d%n", shortest.pid, shortest.completion);
            completedCount++;
        }
        return time;
    }

    //  METRICS & TABLE PRINTING

    private static void printMetrics(List<Process> processes, int totalTime) {
        double totalWT = 0, totalTAT = 0, totalRT = 0;
        long totalBurst = 0;

        System.out.println("\n=============================================================================================================");
        System.out.println("                                          FINAL METRICS FOR MLQ                                              ");
        System.out.println("=============================================================================================================");
        
        // Table Header with Full Words
        System.out.printf("%-12s %-15s %-12s %-12s %-18s %-15s %-18s %-15s%n",
                "Process ID", "Arrival Time", "Burst Time", "Start Time", "Completion Time", "Waiting Time", "Turnaround Time", "Response Time");
        
        System.out.println("-------------------------------------------------------------------------------------------------------------");

        for (Process p : processes) {
            int tat = p.completion - p.arrival;  // Turnaround
            int wt = tat - p.burst;              // Waiting
            int rt = p.start - p.arrival;        // Response

            totalWT += wt;
            totalTAT += tat;
            totalRT += rt;
            totalBurst += p.burst;

            System.out.printf("%-12s %-15d %-12d %-12d %-18d %-15d %-18d %-15d%n",
                    p.pid, p.arrival, p.burst, p.start, p.completion, wt, tat, rt);
        }

        int n = processes.size();
        double avgWT = n > 0 ? totalWT / n : 0;
        double avgTAT = n > 0 ? totalTAT / n : 0;
        double avgRT = n > 0 ? totalRT / n : 0;
        
        // Utilization logic: (Total time CPU was busy / Total Simulation Time) * 100
        double utilization = totalTime > 0 ? ((double) totalBurst / totalTime) * 100 : 0;
        
        double throughput = totalTime > 0 ? (double) n / totalTime : 0;

        System.out.println("=============================================================================================================");
        System.out.printf("Average Waiting Time:    %.2f%n", avgWT);
        System.out.printf("Average Turnaround Time: %.2f%n", avgTAT);
        System.out.printf("Average Response Time:   %.2f%n", avgRT);
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("CPU Utilization:         %.2f%%%n", utilization);
        System.out.printf("Throughput:              %.4f processes/unit time%n", throughput);
        System.out.println("=============================================================================================================");
    }
}