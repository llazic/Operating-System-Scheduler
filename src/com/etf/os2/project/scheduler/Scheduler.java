package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public abstract class Scheduler {
	public abstract Pcb get(int cpuId);

	public abstract void put(Pcb pcb);

	public static Scheduler createScheduler(String[] args) {
		Scheduler sch = null;

		if (args[0].startsWith("s") || args[0].startsWith("S")) {
			double tau = Double.parseDouble(args[1]);
			boolean preemptive = Boolean.parseBoolean(args[2]);
			try {
				sch = new SJF(tau, preemptive);
			} catch (GNeispravnaVrednostAlfa e) {
				System.out.println(e);
				System.exit(2);
			}
		} else if (args[0].startsWith("m") || args[0].startsWith("M")) {
			long[] timeSlices = new long[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				timeSlices[i - 1] = Integer.parseInt(args[i]);
			}
			sch = new MFQS(timeSlices);
		} else if (args[0].startsWith("c") || args[0].startsWith("C")) {
			sch = new CFS();
		} else if (args[0].startsWith("os") || args[0].startsWith("OS") || args[0].startsWith("Os")) {
			double tau = Double.parseDouble(args[1]);
			boolean preemptive = Boolean.parseBoolean(args[2]);
			try {
				sch = new OSJF(tau, preemptive);
			} catch (GNeispravnaVrednostAlfa e) {
				System.out.println(e);
				System.exit(2);
			}
		} else {
			System.out.println("Neispravno uneti parametri algoritma!");
			System.exit(1);
		}

		return sch;
	}
}
